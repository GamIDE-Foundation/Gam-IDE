package org.gampiot.robok.feature.editor.languages.java.autocomplete;
/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 */
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.completion.Comparators;
import io.github.rosemoe.sora.lang.completion.Filters;
import io.github.rosemoe.sora.lang.completion.FuzzyScoreOptions;
import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.text.TextUtils;
import io.github.rosemoe.sora.util.MutableInt;

import org.gampiot.robok.feature.editor.languages.java.models.Method;
import org.gampiot.robok.feature.editor.languages.java.models.Variable;
import org.gampiot.robok.feature.editor.languages.java.store.JavaClasses;
import org.gampiot.robok.feature.editor.languages.java.store.RDKClasses;

import java.lang.Class;
import java.lang.ClassNotFoundException;

/**
 * Identifier auto-completion.
 * <p>
 * You can use it to provide identifiers, but you can't update the given {@link CompletionPublisher}
 * if it is used. If you have to mix the result, then you should call {@link CompletionPublisher#setComparator(Comparator)}
 * with null first. Otherwise, your completion list may be corrupted. And in that case, you must do the sorting
 * work by yourself and then add your items.
 *
 * @author Rosemoe
 */
public class IdentifierAutoComplete {

    /**
     * @deprecated Use {@link Comparators}
     */
    @Deprecated
    private final static Comparator<CompletionItem> COMPARATOR = (p1, p2) -> {
        var cmp1 = asString(p1.desc).compareTo(asString(p2.desc));
        if (cmp1 < 0) {
            return 1;
        } else if (cmp1 > 0) {
            return -1;
        }
        return asString(p1.label).compareTo(asString(p2.label));
    };
    private String[] keywords;
    private boolean keywordsAreLowCase;
    private Map<String, Object> keywordMap;
    private HashMap<String, Variable> variables;
    private HashMap<String, Method> methods;
    private String methodName;
    
    //ItemList
    List<CompletionItem> completionItemList = null;
    
    //Java Classes
    public final static JavaClasses javaClasses = new JavaClasses();
    
    public IdentifierAutoComplete() { }

    public IdentifierAutoComplete(String[] keywords) {
        this();
        setKeywords(keywords, true);
    }

    private static String asString(CharSequence str) {
        return (str instanceof String ? (String) str : str.toString());
    }

    public void setKeywords(String[] keywords, boolean lowCase) {
        this.keywords = keywords;
        keywordsAreLowCase = lowCase;
        var map = new HashMap<String, Object>();
        if (keywords != null) {
            for (var keyword : keywords) {
                map.put(keyword, true);
            }
        }
        keywordMap = map;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setVariables(HashMap<String, Variable> variables){
        this.variables = variables;
    }
    
    public void setMethods(HashMap<String, Method> methods){
        this.methods = methods;
    }
    /**
     * Make completion items for the given arguments.
     * Provide the required arguments passed by {@link Language#requireAutoComplete(ContentReference, CharPosition, CompletionPublisher, Bundle)}
     *
     * @param prefix The prefix to make completions for.
     */
    public void requireAutoComplete(
            @NonNull ContentReference reference, String line, @NonNull CharPosition position,
            @NonNull String prefix, @NonNull CompletionPublisher publisher, @Nullable Identifiers userIdentifiers, String currentMethod) {
         
         this.methodName = currentMethod;
         checkCodeType(line, new CodeTypeListener(){
              @Override
              public void onClassReceiver(String className) {
                   //showMessage("Classe: " + className);
                   completionItemList = createCompletionClassesItemList(prefix, userIdentifiers);
              }
              @Override
              public void onIdentifierReceiver(String identifierName) {
                   //showMessage("Identificador ou variável: " + identifierName);
                   completionItemList = createCompletionIdentifiersAndKeywordsItemList(prefix, userIdentifiers);
              }
              @Override
              public void onVariableIdentifier(String variableName, String fieldOrMethodName) {
                   //showMessage("Método ou campo " + fieldOrMethodName + "de " + variableName);
              }
              @Override
              public void onStaticIdentifierReceiver(String className, String staticFieldOrMethodName) {
                  //showMessage("Método estático ou campo estático:" + staticFieldOrMethodName + " de " + className);
              }
         });
        var comparator = Comparators.getCompletionItemComparator(reference, position, completionItemList);
        publisher.addItems(completionItemList);
        publisher.setComparator(comparator);
    }
    
    public List<CompletionItem> createCompletionClassesItemList(
         @NonNull String className,
         @Nullable Identifiers userIdentifiers
    ) {
         final var keywordMap = this.keywordMap;
         int prefixLength = className.length();
         if (prefixLength == 0) {
              return Collections.emptyList();
         }
         var result = new ArrayList<CompletionItem>();
        
         List<Class<?>> dest = new ArrayList<>();
         
         var rdkClassesManager = new RDKClasses();
         HashMap<String, String> rdkClasses = rdkClassesManager.getClasses();
        
         filterJavaClasses(className, dest, javaClasses.getClasses());
         filterJavaClasses(className, dest, rdkClasses);
         for (var word : dest) {
              //if (keywordMap == null || !keywordMap.containsKey(clazz.getSimpleName()))
              result.add(new SimpleCompletionItem(word.getSimpleName(), word.getName(), prefixLength, word.getSimpleName())
                   .kind(CompletionItemKind.Class));
         }
         return result;        
    }

    public List<CompletionItem> createCompletionIdentifiersAndKeywordsItemList(
            @NonNull String prefix, @Nullable Identifiers userIdentifiers
    ) {
        int prefixLength = prefix.length();
        if (prefixLength == 0) {
            return Collections.emptyList();
        }
        var result = new ArrayList<CompletionItem>();
        final var keywordArray = keywords;
        final var lowCase = keywordsAreLowCase;
        final var keywordMap = this.keywordMap;
        var match = prefix.toLowerCase(Locale.ROOT);

        if (keywordArray != null) {
            if (lowCase) {
                for (var kw : keywordArray) {
                    var fuzzyScore = Filters.fuzzyScoreGracefulAggressive(prefix,
                            prefix.toLowerCase(Locale.ROOT),
                            0, kw, kw.toLowerCase(Locale.ROOT), 0, FuzzyScoreOptions.getDefault());

                    var score = fuzzyScore == null ? -100 : fuzzyScore.getScore();

                    if (kw.startsWith(match) || score >= -20) {
                        result.add(new SimpleCompletionItem(kw, "Keyword", prefixLength, kw)
                                .kind(CompletionItemKind.Keyword));
                    }
                }
            } else {
                for (var kw : keywordArray) {
                    var fuzzyScore = Filters.fuzzyScoreGracefulAggressive(prefix,
                            prefix.toLowerCase(Locale.ROOT),
                            0, kw, kw.toLowerCase(Locale.ROOT), 0, FuzzyScoreOptions.getDefault());

                    var score = fuzzyScore == null ? -100 : fuzzyScore.getScore();

                    if (kw.toLowerCase(Locale.ROOT).startsWith(match) || score >= -20) {
                        result.add(new SimpleCompletionItem(kw, "Keyword", prefixLength, kw)
                                .kind(CompletionItemKind.Keyword));
                    }
                }
            }
        }
        if (userIdentifiers != null) {
            List<String> dest = new ArrayList<>();

            userIdentifiers.filterIdentifiers(prefix, dest);
            for (var word : dest) {
                if (keywordMap == null || !keywordMap.containsKey(word))
                    result.add(new SimpleCompletionItem(word, "Identifier", prefixLength, word)
                            .kind(CompletionItemKind.Identifier));
            }
        }
        
        if(variables != null){
            
            List<Variable> dest = new ArrayList<>();
            
            filterIdentifiersVariables(prefix, dest, methodName, variables);
            for (var word : dest) {
                
                if (keywordMap == null || !keywordMap.containsKey(word.getCode()))
                    result.add(new SimpleCompletionItem(word.getCode(), word.getType(), prefixLength, word.getCode())
                            .kind(CompletionItemKind.Variable));
            }
        }
            
        if(methods != null){
            
            List<Method> dest = new ArrayList<>();
            
            filterIdentifiersMethods(prefix, dest, methods);
            for (var word : dest) {
                
                if (keywordMap == null || !keywordMap.containsKey(word.getName())){
                    String parameters = "";
                    if(!word.getParameters().isEmpty()){
                    parameters = word.getParameters().toString();
                    parameters = parameters.replace("[","");
                    parameters = parameters.replace("]", "");
                        }
                    
                    result.add(new SimpleCompletionItem(word.getName(), word.getName() + "(" + parameters + ")", prefixLength, word.getName() + "()")
                            .kind(CompletionItemKind.Method));
                    }
            }
        }
        
        /*    
        for(String title : variables.keySet()){
                
                
                if(title.startsWith(prefix)){
                    
                
            
            Variable vari = variables.get(title);
            String[] parts = title.split(":");
            
            
        result.add(new SimpleCompletionItem(parts[1], vari.getType(), prefixLength, parts[1])
                            .kind(CompletionItemKind.Variable));
                    }
        }
        }*/
        return result;
    }

    /**
     * Make completion items for the given arguments.
     * Provide the required arguments passed by {@link Language#requireAutoComplete(ContentReference, CharPosition, CompletionPublisher, Bundle)}
     *
     * @param prefix The prefix to make completions for.
     */
   /* @Deprecated
    public void requireAutoComplete(
            @NonNull String prefix, @NonNull CompletionPublisher publisher, @Nullable Identifiers userIdentifiers) {
        publisher.setComparator(COMPARATOR);
        publisher.setUpdateThreshold(0);
        publisher.addItems(createCompletionItemList(prefix, userIdentifiers));
    }*/

    public void filterIdentifiersVariables(@NonNull String prefix, @NonNull List<Variable> dest, String methodName, HashMap<String, Variable> variables) {
            
                
                    for (String ip : variables.keySet()){
                        String[] parts = ip.split(":");
                        
                       if(parts[0].equals(methodName) || parts[0].equals("global")){
                            String s = parts[1];
            
                        var fuzzyScore = Filters.fuzzyScoreGracefulAggressive(prefix,
                                prefix.toLowerCase(Locale.ROOT),
                                0, s, s.toLowerCase(Locale.ROOT), 0, FuzzyScoreOptions.getDefault());

                        var score = fuzzyScore == null ? -100 : fuzzyScore.getScore();

                        if ((TextUtils.startsWith(s, prefix, true) || score >= -20)  && !(prefix.length() == s.length() && TextUtils.startsWith(prefix, s, false)) || (prefix.equalsIgnoreCase(s))) {
                            var vari = variables.get(ip);
                        //    vari.setType(vari.getType());
                            dest.add(vari);
                        }
                        }
                    
                }
            }
    
    public void filterIdentifiersMethods(@NonNull String prefix, @NonNull List<Method> dest, HashMap<String, Method> methods) {
            
                
                    for (String s : methods.keySet()){
                      //  String[] parts = ip.split(":");
                        
                      // if(parts[0].equals(methodName) || parts[0].equals("global")){
                          //  String s = parts[1];
            
                        var fuzzyScore = Filters.fuzzyScoreGracefulAggressive(prefix,
                                prefix.toLowerCase(Locale.ROOT),
                                0, s, s.toLowerCase(Locale.ROOT), 0, FuzzyScoreOptions.getDefault());

                        var score = fuzzyScore == null ? -100 : fuzzyScore.getScore();

                        if ((TextUtils.startsWith(s, prefix, true) || score >= -20)  && !(prefix.length() == s.length() && TextUtils.startsWith(prefix, s, false)) || (prefix.equalsIgnoreCase(s))) {
                            var method = methods.get(s);
                        //    vari.setType(vari.getType());
                            dest.add(method);
                        }
                      //  }
                    
                }
            }
    
    
    public void filterJavaClasses(@NonNull String prefix, List<Class<?>> dest, HashMap<String, String> classes){
         for (String s : classes.keySet()) {
             var fuzzyScore 
                 = Filters.fuzzyScoreGracefulAggressive(prefix,
                     prefix.toLowerCase(Locale.ROOT),
                     0, 
                     s, 
                     s.toLowerCase(Locale.ROOT), 
                     0,
                     FuzzyScoreOptions.getDefault()
                 );
             var score = fuzzyScore == null ? -100 : fuzzyScore.getScore();
             if ((TextUtils.startsWith(s, prefix, true) || score >= -20)  && !(prefix.length() == s.length() && TextUtils.startsWith(prefix, s, false)) || (prefix.equalsIgnoreCase(s))){
                 try {
                     Class<?> clazz = Class.forName(classes.get(s));
                     dest.add(clazz);
                 } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                 }                
			 }
	     }
    }
        
    /**
     * Interface for saving identifiers
     *
     * @author Rosemoe
     * @see IdentifierAutoComplete.DisposableIdentifiers
     */
    public interface Identifiers {

        /**
         * Filter identifiers with the given prefix
         *
         * @param prefix The prefix to filter
         * @param dest   Result list
         */
        void filterIdentifiers(@NonNull String prefix, @NonNull List<String> dest);

    }
    
    /**
     * Interface for check Code Type
     *
     * @author ThDev-only
     */
    public static interface CodeTypeListener {
        // Callback when a class is identified
        void onClassReceiver(String className);

        // Callback when a variable or identifier is identified
        void onIdentifierReceiver(String identifierName);

        // Callback when a method or field of a variable is identified
        void onVariableIdentifier(String variableName, String fieldOrMethodName);

        // Callback when a static identifier is identified
        void onStaticIdentifierReceiver(String className, String staticFieldOrMethodName);
    }
    
    /**
     * method check Code Type
     *
     * @author ThDev-only
     */
    private void checkCodeType(String code, CodeTypeListener listener) {
		if (code.isEmpty()) {
			return;
		}

		// Use regex to split the text based on anything that is not a letter or a dot
		String[] words = code.split("[^a-zA-Z.]");

		// Get the last relevant word
		String lastWord = "";
		for (int i = words.length - 1; i >= 0; i--) {
			if (!words[i].isEmpty()) {
				lastWord = words[i];
				break;
			}
		}

		// Initialize the variable to store the name of the identifier or class
		String name = "";

		// Check if the last character is a dot
		if (code.endsWith(".")) {
			name = lastWord.replace(".", "");
			if (Character.isUpperCase(lastWord.charAt(0))) {
				listener.onStaticIdentifierReceiver(name, ".");
			} else {
				listener.onVariableIdentifier(name, ".");
			}
			return;
		}

		// Continue with the logic to identify the type of input
		if (lastWord.contains(".")) {
			String[] parts = lastWord.split("\\.");
			if (parts.length > 1) {
				String prefix = parts[0];
				String suffix = parts[1];

				name = prefix;

				if (Character.isUpperCase(prefix.charAt(0))) {
					listener.onStaticIdentifierReceiver(name, suffix);
				} else {
					listener.onVariableIdentifier(name, suffix);
				}
			} else {
				name = lastWord;
				if (Character.isUpperCase(lastWord.charAt(0))) {
					listener.onClassReceiver(name);
				} else {
					listener.onIdentifierReceiver(name);
				}
			}
		} else {
			name = lastWord;
			if (Character.isUpperCase(lastWord.charAt(0))) {
				listener.onClassReceiver(name);
			} else {
				listener.onIdentifierReceiver(name);
			}
		}
	}

    /**
     * This object is used only once. In other words, the object is generated every time the
     * text changes, and is abandoned when next time the text change.
     * <p>
     * In this case, the frequent allocation of memory is unavoidable.
     * And also, this class is not thread-safe.
     *
     * @author Rosemoe
     */
    public static class DisposableIdentifiers implements Identifiers {

        private final static Object SIGN = new Object();
        private final List<String> identifiers = new ArrayList<>(128);
        private HashMap<String, Object> cache;

        public void addIdentifier(String identifier) {
            if (cache == null) {
                throw new IllegalStateException("begin() has not been called");
            }
            if (cache.put(identifier, SIGN) == SIGN) {
                return;
            }
            identifiers.add(identifier);
        }

        /**
         * Start building the identifiers
         */
        public void beginBuilding() {
            cache = new HashMap<>();
        }

        /**
         * Free memory and finish building
         */
        public void finishBuilding() {
            cache.clear();
            cache = null;
        }

        @Override
        public void filterIdentifiers(@NonNull String prefix, @NonNull List<String> dest) {
            for (String identifier : identifiers) {
                var fuzzyScore = Filters.fuzzyScoreGracefulAggressive(prefix,
                        prefix.toLowerCase(Locale.ROOT),
                        0, identifier, identifier.toLowerCase(Locale.ROOT), 0, FuzzyScoreOptions.getDefault());

                var score = fuzzyScore == null ? -100 : fuzzyScore.getScore();

                if ((TextUtils.startsWith(identifier, prefix, true) || score >= -20) && !(prefix.length() == identifier.length() && TextUtils.startsWith(prefix, identifier, false))) {
                    dest.add(identifier);
                }
            }
        }
    }

    public static class SyncIdentifiers implements Identifiers {

        private final Lock lock = new ReentrantLock(true);
        private final Map<String, MutableInt> identifierMap = new HashMap<>();

        public void clear() {
            lock.lock();
            try {
                identifierMap.clear();
            } finally {
                lock.unlock();
            }
        }

        public void identifierIncrease(@NonNull String identifier) {
            lock.lock();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    identifierMap.computeIfAbsent(identifier, (x) -> new MutableInt(0)).increase();
                } else {
                    var counter = identifierMap.get(identifier);
                    if (counter == null) {
                        counter = new MutableInt(0);
                        identifierMap.put(identifier, counter);
                    }
                    counter.increase();
                }
            } finally {
                lock.unlock();
            }
        }

        public void identifierDecrease(@NonNull String identifier) {
            lock.lock();
            try {
                var count = identifierMap.get(identifier);
                if (count != null) {
                    if (count.decreaseAndGet() <= 0) {
                        identifierMap.remove(identifier);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void filterIdentifiers(@NonNull String prefix, @NonNull List<String> dest) {
            filterIdentifiers(prefix, dest, false);
        }

        public void filterIdentifiers(@NonNull String prefix, @NonNull List<String> dest, boolean waitForLock) {
            boolean acquired;
            if (waitForLock) {
                lock.lock();
                acquired = true;
            } else {
                try {
                    acquired = lock.tryLock(3, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    acquired = false;
                }
            }
            if (acquired) {
                try {
                    for (String s : identifierMap.keySet()) {
                        var fuzzyScore = Filters.fuzzyScoreGracefulAggressive(prefix,
                                prefix.toLowerCase(Locale.ROOT),
                                0, s, s.toLowerCase(Locale.ROOT), 0, FuzzyScoreOptions.getDefault());

                        var score = fuzzyScore == null ? -100 : fuzzyScore.getScore();

                        if ((TextUtils.startsWith(s, prefix, true) || score >= -20)  && !(prefix.length() == s.length() && TextUtils.startsWith(prefix, s, false))) {
                            dest.add(s);
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

    }


}