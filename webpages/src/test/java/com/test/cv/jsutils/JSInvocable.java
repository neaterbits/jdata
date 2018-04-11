package com.test.cv.jsutils;

public interface JSInvocable {

	Object invokeConstructor(String function, Object ... args);
	
	Object invokeFunction(String function, Object ... args);
	
	Object invokeMethod(Object obj, String method, Object ... args);

	Object getProperty(Object obj, String property);
}
