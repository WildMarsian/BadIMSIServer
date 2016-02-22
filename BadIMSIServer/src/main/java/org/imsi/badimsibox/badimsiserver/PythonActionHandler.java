package org.imsi.badimsibox.badimsiserver;

import java.io.InputStream;
import java.io.OutputStream;

public interface PythonActionHandler {
	public void accept(InputStream stdin, OutputStream stdout, InputStream inputStream, int returnCode);
}
