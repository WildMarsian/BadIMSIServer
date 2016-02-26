package org.imsi.badimsibox.badimsiserver;

import java.io.IOException;
import java.util.Objects;

/**
 * Class to manager calls to launch Python scripts
 * @author WarenUT TTAK
 */
public class PythonManager {

    /**
     *
     * @param command
     * @return
     * @throws IOException
     */
    public Process run(String[] command) throws IOException {
        Objects.requireNonNull(command);
        if (command.length == 0) {
            throw new IllegalArgumentException("No command specified");
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        return pb.start();
    }
}