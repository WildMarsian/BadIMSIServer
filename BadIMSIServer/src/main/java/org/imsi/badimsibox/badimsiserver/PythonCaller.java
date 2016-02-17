package org.imsi.badimsibox.badimsiserver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Python Caller is a class that calls python code and return the process class
 */

public class PythonCaller {
    private static String contextPath = "/opt/badimsicore/";	
    private final String program = "python"; 
    private final String cmd;
    
    public PythonCaller(String[] scriptPath) {
        if(scriptPath.length == 0) {
            throw new IllegalArgumentException("No args found");
        }
    	Objects.requireNonNull(scriptPath);  
    	StringBuilder sb = new StringBuilder();
        sb.append(program);
        for (String string : scriptPath) {
            sb.append(" ");
            sb.append(string);
        }
        this.cmd = sb.toString();
    }

    public Process process() throws IOException {
        Runtime rt = Runtime.getRuntime();
        return rt.exec(cmd);
    }

    public static String getContextPath() {
        return contextPath;
    }

    public static String getCleanPath() {
        URL location = PythonCaller.class.getProtectionDomain().getCodeSource()
                .getLocation();
        String path = location.getFile();
        return new File(path).getParent()+"/"+new File(path).getName();
    }

    private static void noArgsError() {
        System.out.println("No arguments were found");
    }

    private static void errorOnScript(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg+" ");
        }
        System.out.println("Error on the script : no script found for "+sb.toString());
    }
}
