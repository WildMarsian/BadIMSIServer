package org.imsi.badimsibox.badimsiserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

/**
 * Python Caller is a class that calls python code and return the process class
 * On Maven, script path is along JAVA code source files, check execution rights!!
 */

public class PythonCaller {
	private static String contextPath = "/opt/badimsicore/";
	private final String cmd;
	private final PythonActionHandler actionHandler;
	
	public PythonCaller(String[] scriptPath, PythonActionHandler pythonActionHandler) {
		if (scriptPath.length == 0) {
			throw new IllegalArgumentException("No args found");
		}
		Objects.requireNonNull(scriptPath);
		StringBuilder sb = new StringBuilder();
		// sb.append(program);
		for (String string : scriptPath) {
			sb.append(" ");
			sb.append(string);
		}
		this.actionHandler = pythonActionHandler;
		this.cmd = sb.toString();
	}
	
	public static String getContextPath() {
		return contextPath;
	}
	
	public void exec() throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec(cmd);
		p.waitFor();
		actionHandler.accept(p.getInputStream(), p.getOutputStream(), p.getErrorStream(), p.exitValue());
	}

	public static String getCleanPath() {
		URL location = PythonCaller.class.getProtectionDomain().getCodeSource().getLocation();
		String path = location.getFile();
		return new File(path).getParent() + "/" + new File(path).getName();
	}

	private static void noArgsError() {
		System.out.println("No arguments were found");
	}

	private static void errorOnScript(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			sb.append(arg + " ");
		}
		System.out.println("Error on the script : no script found for " + sb.toString());
	}
	
	public static void main(String[] args) throws InterruptedException {
		String[] cmd = {"./scripts/test.py", "-i", "file.input", "-o", "file.output"};
		PythonCaller pythonCaller = new PythonCaller(cmd, (input, output, error, returnCode) -> {
			if(returnCode == 0) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
				bufferedReader.lines().forEach(l -> {
					System.out.println(l);
				});	
			}
			else {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(error));
				bufferedReader.lines().forEach(l -> {
					System.out.println(l);
				});	
			}
		});
		
		try {
			pythonCaller.exec();
		}catch(IOException ie) {
			System.out.println("File not found");
		}
		
	}
}
