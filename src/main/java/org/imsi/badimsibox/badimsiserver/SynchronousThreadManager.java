package org.imsi.badimsibox.badimsiserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
 * Class used to launch a new thread to execute periodically a python script and
 * launch a special treatment if the process is executed correctly
 *
 * @author WarenUT TTAK
 */
public class SynchronousThreadManager {

    private final Object lock = new Object();

    private final String[] command;
    private final int refreshTime;
    private final String name;
    private final int maximumNumberOfFail;

    private Thread thread = null;
    private Exception error = null;

    /**
     * Contruct a new SynchronousThreadManager to execute a specified command
     * periodically each timeToSleep millisecond. A name is needed to identify
     * the thread in logs in case of error. A maximum number of successive
     * process fail before stopping the thread is also needed to avoid infinite
     * error loop
     *
     * @param command : the array of String which represent a command to execute
     * @param name : The name to identify the thread in logs
     * @param refreshTime : the time between two execution
     * @param maximumNumberOfFail : Maximum number of successive error before
     * automatic stop
     */
    private SynchronousThreadManager(String[] command, String name, int refreshTime, int maximumNumberOfFail) {
        this.name = name;
        this.command = command;
        this.refreshTime = refreshTime;
        this.maximumNumberOfFail = maximumNumberOfFail;
    }

    /**
     * Contruct a new SynchronousThreadManager to execute a specified command
     * periodically each timeToSleep millisecond. A name is needed to identify
     * the thread in logs in case of error. A maximum number of successive
     * process fail before stopping the thread is also needed to avoid infinite
     * error loop
     *
     * @param command : the array of String which represent a command to execute
     * @param name : The name to identify the thread in logs
     * @param refreshTime : the time between two execution
     * @param maximumNumberOfFail : Maximum number of successive error before
     * automatic stop
     * @return the created Synchronous Thread Manager
     */
    public static SynchronousThreadManager createSynchronousThread(String[] command, String name, int refreshTime, int maximumNumberOfFail) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Empty name not accepted");
        }
        if (refreshTime <= 0 || maximumNumberOfFail <= 0) {
            throw new IllegalArgumentException("Negative refresh time detected");
        }
        if (command == null || command.length < 1) {
            throw new IllegalArgumentException("No command to execute founded");
        }
        return new SynchronousThreadManager(command, name, refreshTime, maximumNumberOfFail);
    }

    /**
     * Used to launch the Python script in the thread managed by the class then
     * execute the specified operations contained in the functional interface
     * parameter. This operation will be launched only if the Python script was
     * correctly executed (the process returned 0)
     *
     * @param operation : the operations to execute
     */
    public void start(PythonOperation operation) {
        if (thread != null) {
            // Already started
            return;
        }

        thread = new Thread(() -> {
            PythonManager manager = new PythonManager();
            int numberOfFail = 0;
            while (true) {
                try {
                    Process p = manager.run(command);
                    p.waitFor();
                    if (p.exitValue() == 0) {
                        numberOfFail = 0;
                        BadIMSILogger.getLogger().log(Level.FINE, "Prcoess ended correctly, starting treatment in thread " + name);
                        operation.accept(p.getInputStream(), p.getOutputStream());
                    } else {
                        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        StringBuilder str = new StringBuilder();
                        br.lines().forEach(line -> {
                            str.append(line);
                        });
                        numberOfFail++;
                        if (numberOfFail > maximumNumberOfFail) {
                            // Stopping thread
                            Thread.currentThread().interrupt();
                        }
                        BadIMSILogger.getLogger().log(Level.SEVERE, "Process stopped unexpectedly in thread" + name + ", trying again", new Exception(str.toString()));
                    }
                    Thread.sleep(refreshTime);
                } catch (IOException ex) {
                    BadIMSILogger.getLogger().log(Level.SEVERE, "Critical error while executing process treatment in thread " + name, ex);
                    synchronized (lock) {
                        error = ex;
                    }
                    Thread.currentThread().interrupt();
                } catch (InterruptedException ex) {
                    BadIMSILogger.getLogger().log(Level.SEVERE, "Synchronous thread " + name + " stopped", ex);
                    return;
                }
            }
        });
        thread.start();
    }

    /**
     * Used to stop the started thread. Warning, it will not kill the thread
     * process but set the interrupted flag to True. The thread will check this
     * flag before each Python execution process.
     */
    public void stop() {
        if (thread != null || !thread.isInterrupted()) {
            BadIMSILogger.getLogger().log(Level.FINE, "Order to stop thread " + name + " executed");
            thread.interrupt();
        }
    }

    /**
     * Used to check the current state of the thread.
     *
     * @return True if the thread is running else return False
     */
    public boolean status() {
        if (thread == null) {
            return false;
        }
        return thread.isInterrupted();
    }

    /**
     * Used to know the Exception thrown by the running thread in case of error
     *
     * @return the Exception thrown
     */
    public Exception getError() {
        synchronized (lock) {
            return error;
        }
    }
}
