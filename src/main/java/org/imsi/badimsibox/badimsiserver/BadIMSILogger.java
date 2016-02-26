package org.imsi.badimsibox.badimsiserver;

import java.util.logging.Logger;

/**
 * Class to define the single logger used in the server
 * @author WarenUT TTAK
 */
public class BadIMSILogger {
    /**
     * Server use only one logger for all classes
     * @return the single logger of the server
     */
    public static Logger getLogger() {
        return Logger.getLogger("badimsiserver");
    }
}
