package org.imsi.badimsibox.badimsiserver;

import java.io.IOException;
import java.util.Objects;

/**
 * Class to manager calls to launch Python scripts
 * @author WarenUT TTAK
 */
public class PythonManager {

    /**
     * Run the specified command in a new process
     * @param command : the command to execute
     * @return : the process created to execute the command
     * @throws IOException : if the specified command is not found or can't be
     * executed for some reason
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