package org.imsi.badimsibox.badimsiserver;

import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author thibaut arthur
 */
public class PythonManager {

    /**
     *
     * @param command
     * @return the exit code
     * @throws java.io.IOException
     */
    public Process run(String command) throws IOException {
        Objects.requireNonNull(command);
        if (command.length() == 0) {
            throw new IllegalArgumentException("Python handle need a command to execute");
        }
        Runtime rt = Runtime.getRuntime();
        return rt.exec(command);
    }
}
