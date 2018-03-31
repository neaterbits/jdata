package com.test.cv.jsutils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

public interface JSEngine {

	public default JSInvocable prepare(String string, Map<String, Object> bindings, ConstructRequest ... constructRequests) {
		try {
			return prepare(new StringReader(string), bindings, constructRequests);
		} catch (IOException ex) {
			throw new IllegalStateException("IOException when reading from String", ex);
		}
	}

	JSInvocable prepare(Reader reader, Map<String, Object> bindings, ConstructRequest ... constructRequests) throws IOException;
}
