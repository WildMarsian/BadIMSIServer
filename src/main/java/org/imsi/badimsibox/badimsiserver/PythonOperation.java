package org.imsi.badimsibox.badimsiserver;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author WarenUT
 */
@FunctionalInterface
public interface PythonOperation {
    public void accept(InputStream in, OutputStream out, InputStream error);
}
