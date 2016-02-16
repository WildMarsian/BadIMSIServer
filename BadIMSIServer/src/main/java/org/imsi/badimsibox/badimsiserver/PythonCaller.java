package org.imsi.badimsibox.badimsiserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;


/**
 * Python Caller is a class that calls python code and return the result in the stdout.
 */

public class PythonCaller {

	
	private static String contextPath = "/opt/badimsicore/";
	
    private final String program = "python"; 
    private final String cmd;
    private final StringBuilder resultSb = new StringBuilder();

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

    public int process() throws IOException {
        Runtime rt = Runtime.getRuntime();

        Process pr = rt.exec(cmd);
        BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;
        while ((line = bfr.readLine()) != null) {
            System.out.println(line);
            resultSb.append(line+"\n");
        }

        int val = 1;
        try {
            val = pr.waitFor(); 
            return val;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            return -1;
    }

    public static String getContextPath() {
		return contextPath;
	}
    
    public String getResultSb() {
		return resultSb.toString();
	}
    
    
    public static String getCleanPath() {
        URL location = PythonCaller.class.getProtectionDomain().getCodeSource()
                .getLocation();
        String path = location.getFile();
        return new File(path).getParent()+"/"+new File(path).getName();
    }

    public static void main(String[] args) {
        if(args.length == 0) {
            noArgsError();
        }

        if(args.length > 0) {
            PythonCaller pc = new PythonCaller(args);
            try {
                int exitValue = pc.process();
                System.out.println("Result: "+pc.getResultSb());
                System.out.println("Exit value : "+exitValue);

            } catch (IOException e) {
                errorOnScript(args);
                e.printStackTrace();
            }
        }

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
