package org.imsi.badimsibox.badimsiserver;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PythonHandler {
    public static enum HandlerState {
        INIT, RUN, ERROR, STOPPED
    }
    public final static int PROCESSRUNNING = -1;

    private final Object lock = new Object();
    private final PythonActionHandler actionHandler;
    private final String commandToExec;

    private HandlerState currentState = HandlerState.INIT;
    private Exception exceptionError = null;
    private int processExitValue = PROCESSRUNNING;
    private Thread thread = null;

    /**
     * 
     * @param command
     * @param actionHandler
     */
    public PythonHandler(String[] command, PythonActionHandler actionHandler) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(actionHandler);
        if (command.length == 0) {
            throw new IllegalArgumentException("Python handle need a command to execute");
        }
        this.actionHandler = actionHandler;
        StringBuilder str = new StringBuilder();
        for (String s : command) {
            str.append(" ");
            str.append(s);
        }
        commandToExec = str.toString();
    }

    /**
     *
     */
    public void start() {
        thread = new Thread(() -> {
            Runtime rt = Runtime.getRuntime();
            Process p;
            try {
                p = rt.exec(commandToExec);
                actionHandler.accept(p.getInputStream(), p.getOutputStream(), p.getErrorStream());
            } catch (IOException ex) {
                synchronized (lock) {
                    Logger.getLogger(PythonHandler.class.getName()).log(Level.SEVERE, null, ex);
                    exceptionError = ex;
                    currentState = HandlerState.ERROR;
                    return;
                }
            }
            synchronized(lock) {
                processExitValue = p.exitValue();
                currentState = HandlerState.STOPPED;
            }
        });
        thread.start();
        synchronized (lock) {
            currentState = HandlerState.RUN;
        }
    }

    /**
     * 
     * @return 
     */
    public int getExitValue() {
        synchronized(lock) {
            return processExitValue;
        }
    }

    /**
     *
     * @return
     */
    public Exception getError() {
        synchronized (lock) {
            return exceptionError;
        }
    }

    /**
     *
     * @return
     */
    public HandlerState getState() {
        synchronized (lock) {
            return currentState;
        }
    }
}
