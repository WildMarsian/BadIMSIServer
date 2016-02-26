package org.imsi.badimsibox.badimsiserver;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Class to handle process treatment for real time operations
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
     *
     * @param command
     * @param timeToSleep
     */
    public SynchronousThreadManager(String[] command, int timeToSleep) {
        this.command = Objects.requireNonNull(command);
        this.refreshTime = timeToSleep;
    }

    /**
     *
     * @param operation
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
                    if (p.exitValue() == 0) {
                        operation.accept(p.getInputStream(), null); // TODO test null values
                        Thread.sleep(refreshTime);
                    } else {
                        BadIMSILogger.getLogger().log(Level.SEVERE, "Process stopped unexpectedly, trying again", p.exitValue());
                    }
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
     *
     */
    public void stop() {
        thread.interrupt();
    }

    /**
     *
     * @return
     */
    public boolean status() {
        return thread.isInterrupted();
    }

    /**
     *
     * @return
     */
    public Exception getError() {
        synchronized (lock) {
            return error;
        }
    }
}
