package robok.compiler.logic.method;

import android.content.*;
import android.widget.*;
import android.graphics.*;

import robok.util.terminal.RobokTerminal;
import robok.compiler.logic.LogicCompilerListener;

import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;

public class MethodCaller {

    private Map<String, Method> methodsMap;
    private Context context;
    
    private Methods methodsInstance;
    private RobokTerminal terminal;
    
    public MethodCaller(Context context, LogicCompilerListener compiler) {
        this.context = context;
        methodsMap = new HashMap<>();
        methodsInstance = new Methods(context, compiler);
        terminal = new RobokTerminal ();
        
        try {
            methodsMap.put("showToast", Methods.class.getDeclaredMethod("showToast", String.class));
            methodsMap.put("createButton", Methods.class.getDeclaredMethod("createButton", String.class, String.class));
            methodsMap.put("createText", Methods.class.getDeclaredMethod("createText", String.class, String.class));
            methodsMap.put("openTerminal", Methods.class.getDeclaredMethod("openTerminal"));
			methodsMap.put("showDialog", Methods.class.getDeclaredMethod("showDialog", String.class, String.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            terminal.addErrorLog("IDE Error: ", e.toString());
        }
    }

    public void callMethod(String methodName,  Object... args) {
        Method method = methodsMap.get(methodName);
        if (method != null) {
            try {
                method.invoke(methodsInstance, args);
            } catch (Exception e) {
                e.printStackTrace();
                terminal.addErrorLog("Call Method Error: ", e.toString());
            }
        } else {
            terminal.addErrorLog("Method not found: ", methodName);
        }
    }
}
