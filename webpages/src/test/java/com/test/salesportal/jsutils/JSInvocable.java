package com.test.salesportal.jsutils;

public interface JSInvocable {

	Object invokeConstructor(String function, Object ... args);
	
	Object invokeFunction(String function, Object ... args);

	Object invokeFunctionObject(Object function, Object ... args);
	
	Object invokeMethod(Object obj, String method, Object ... args);

	Object getProperty(Object obj, String property);
	
	Object getVariable(String name);
	
	/**
	 * Convert a JS object that is an array onto a Java typed array
	 * 
	 * @param jsArrayObject - as returned from the JS runtime
	 * @param memberClass - class that members should be converted to
	 * 
	 * @return array of converted members
	 */
	<T> T [] getJSArray(Object jsArrayObject, Class<T> memberClass);
}
