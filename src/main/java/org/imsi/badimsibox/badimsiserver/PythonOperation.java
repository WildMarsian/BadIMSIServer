package org.imsi.badimsibox.badimsiserver;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface used to define the behaviour of a thread after the Python 
 * process finished correctly
 * @author WarenUT TTAK
 */
@FunctionalInterface
public interface PythonOperation {

    /**
     * Define the behaviour of the thread after executing the Python 
     * process correctly, so there is no error stream handler
     * @param in : the input stream to handle
     * @param out : the output stream to handle
     */
    public void accept(InputStream in, OutputStream out);
}
