package org.imsi.badimsibox.badimsiserver;

import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author
 */
public class PythonManager {

    /**
     *
     * @param command
     * @return
     * @throws IOException
     */
    public Process run(String command) throws IOException {
        Objects.requireNonNull(command);
        if (command.length() == 0) {
            throw new IllegalArgumentException("No command specified");
        }
        Runtime rt = Runtime.getRuntime();
        return rt.exec(command);
    }
}
