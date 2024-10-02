package org.gampiot.robok.feature.editor.languages.java.store;

/*
 *  This file is part of Robok © 2024.
 *
 *  Robok is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Robok is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Robok.  If not, see <https://www.gnu.org/licenses/>.
 */ 

/*
* RobokClasses
* Class used to store robok classes
* Only those necessary for development with Robok.
* @author ThDev-Only
*/

import android.content.Context;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public class RDKFileMapper {
    
    private Context context;
    private HashMap<String, String> robokClasses;
    private String atuallyRDK = "RDK-1";
    private File rdkDirectory;
    
    public RDKFileMapper(Context context){
        this.context = context;
        robokClasses = new HashMap<>();
        rdkDirectory = new File(context.getFilesDir(), atuallyRDK + "/rdk/");
        
    }
    
    public void load(){
        this.robokClasses = mapRdkClasses();
    }
    
    public HashMap<String, String> getClasses() {
        return this.robokClasses;
    }
    
    // Method that returns a HashMap with the names of the classes and their formatted directories
    public HashMap<String, String> mapRdkClasses() {
        File rdkFolder = rdkDirectory;

        if (rdkFolder.exists() && rdkFolder.isDirectory()) {
            mapClassesRecursively(rdkFolder, "", robokClasses);
        }else{
            robokClasses.put("ErrorRdkNaoExiste", "com.error.rdk.ErrorRdkNaoExiste");
        }

        return robokClasses;
    }

    // Recursive method to map .class classes into the directory
    private void mapClassesRecursively(File folder, String packageName, HashMap<String, String> robokClasses) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // Update package name as you cycle through folders
                String newPackageName = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                mapClassesRecursively(file, newPackageName, robokClasses);
            } else if (file.getName().endsWith(".class")) {
                // Remove the .class extension and map the class
                String className = file.getName().replace(".class", "");
                String classPath = packageName + "." + className;
                robokClasses.put(className, classPath);
            }
        }
    }

    //Method to create URLClassLoader from base directory
    public URLClassLoader getClassLoader() throws Exception {
        File file = rdkDirectory;

        // Converts the base directory to a URL
        URL url = file.toURI().toURL();
        URL[] urls = new URL[]{url};

        // Returns the URLClassLoader for loading classes
        return new URLClassLoader(urls);
    }

    /*public static void main(String[] args) throws Exception {
        // Diretório base onde os arquivos .class estão localizados
        String rdkDirectory = "/caminho/para/sua/classe/";

        // Cria o HashMap mapeando os arquivos .class
        HashMap<String, String> robokClasses = 

        // Exemplo de uso do HashMap
        System.out.println(robokClasses.get("Physics")); // Exemplo: robok.physics.Physics

        // Cria o URLClassLoader
        URLClassLoader classLoader = getClassLoader(rdkDirectory);

        // Carrega as classes com base no nome completo (pacote + nome da classe)
        Class<?> class1 = classLoader.loadClass(robokClasses.get("Physics"));
        Class<?> class2 = classLoader.loadClass(robokClasses.get("Graphic"));
        Class<?> class3 = classLoader.loadClass(robokClasses.get("Util"));

        // Exibe os nomes das classes carregadas
        System.out.println("Classe 1: " + class1.getName());
        System.out.println("Classe 2: " + class2.getName());
        System.out.println("Classe 3: " + class3.getName());

        // Fecha o classLoader após o uso
        classLoader.close();
    }*/
}