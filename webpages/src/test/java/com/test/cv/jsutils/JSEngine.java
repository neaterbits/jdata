package com.test.cv.jsutils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.function.Function;

public interface JSEngine {

	public default JSInvocable prepare(String string, Map<String, Object> bindings, ConstructRequest ... constructRequests) {
		try {
			return prepare(new StringReader(string), bindings, constructRequests);
		} catch (IOException ex) {
			throw new IllegalStateException("IOException when reading from String", ex);
		}
	}

	JSInvocable prepare(Reader reader, Map<String, Object> bindings, ConstructRequest ... constructRequests) throws IOException;
	
	
	/**
	 * Create a function-callback that we can pass as a parameter to JS call and
	 * have called back from JS
	 * 
	 * @param function that takes in a set of parameters and returns a value (or null if not supposed to have a return value - as is often the case with continuations)
	 * 
	 * @return an object representing the function, that can be passed as paramter to eg. .invokeFunction()
	 */
	Object createJSFunctionCallback(Function<Object [], Object> function);
	

	/**
	 * Get a callable function abstraction for something that is
	 * probably a JS function. Ie. when Java object referenced from JS engine is invoked with a parameter
	 * that is a JS function (eg. a continuation)
	 * 
	 * @param object the object that represents the JS function
	 * 
	 * @return a wrapper that is callable from Java so that we can call back
	 */
	
	JSFunction getJSFunction(Object object);
}
