package org.imsi.badimsibox.badimsiserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
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

    private Thread thread = null;
    private Exception error = null;

    /**
     * Contruct a new SynchronousThreadManager to execute a specified command
     * periodically each timeToSleep millisecond
     *
     * @param command : the array of String which represent a command to execute
     * @param refreshTime : the time between two execution
     */
    public SynchronousThreadManager(String[] command, int refreshTime) {
        this.command = Objects.requireNonNull(command);
        this.refreshTime = refreshTime;
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
            return;
        }

        thread = new Thread(() -> {
            PythonManager manager = new PythonManager();
            while (true) {
                if (Thread.interrupted()) {
                    return;
                }
                try {
                    Process p = manager.run(command);
                    p.waitFor();
                    if (p.exitValue() == 0) {
                        operation.accept(p.getInputStream(), p.getOutputStream());
                    } else {
                        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        StringBuilder str = new StringBuilder();
                        br.lines().forEach(line -> {
                           str.append(line);
                        });
                        BadIMSILogger.getLogger().log(Level.SEVERE, "Process stopped unexpectedly, trying again", new Exception(str.toString()));
                    }
                    Thread.sleep(refreshTime);
                } catch (IOException | InterruptedException ex) {
                    BadIMSILogger.getLogger()
                            .log(Level.SEVERE, "Critical error while executing process treatment", ex);
                    Thread.currentThread().interrupt();
                    synchronized (lock) {
                        error = ex;
                    }
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
        if (!thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    /**
     * Used to check the current state of the thread.
     *
     * @return True if the thread is running else return False
     */
    public boolean status() {
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
