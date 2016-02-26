package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.Vertx;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Class to handle SMS reading in live with a new Thread
 *
 * @author WarenUT TTAK
 */
public class SmsManager {

    private final Object lock = new Object();
    
    private final Vertx vertx;
    private final String[] command;
    private final int refreshTime;

    private Thread thread = null;
    private Exception error = null;

    /**
     *
     * @param command
     * @param timeToSleep
     * @param vertx
     */
    public SmsManager(String[] command, int timeToSleep, Vertx vertx) {
        this.vertx = Objects.requireNonNull(vertx);
        this.command = Objects.requireNonNull(command);
        this.refreshTime = timeToSleep;
    }

    /**
     *
     */
    public void start() {
        thread = new Thread(() -> {
            PythonManager manager = new PythonManager();
            while (true) {
                if (Thread.interrupted()) {
                    return;
                }
                try {
                    Process p = manager.run(command);
                    if (p.exitValue() == 0) {
                        BufferedReader bf
                                = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        bf.lines().forEach(line -> {
                            String removeParenthesis = line.replaceAll("[()]", "");
                            String[] splitted = removeParenthesis.split(",");
                            String first = splitted[0].replaceAll("['']", "");
                            String second = splitted[1].replaceAll("['']", "");
                            vertx.eventBus().publish("sms.new", new Sms(first, second).toJson());
                        });
                        Thread.sleep(refreshTime);
                    } else {
                        BadIMSILogger.getLogger().log(Level.SEVERE, "Process stopped unexpectedly", p.exitValue());
                    }
                } catch (IOException | InterruptedException ex) {
                    BadIMSILogger.getLogger()
                            .log(Level.SEVERE, "Critical error while reading SMS", ex);
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