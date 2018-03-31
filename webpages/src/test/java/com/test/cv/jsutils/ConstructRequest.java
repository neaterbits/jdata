package com.test.cv.jsutils;

// Request for constructing an instance of some class
public class ConstructRequest {
	private final String jsClass;
	private final Object [] params;
	
	private Object instance;
	
	public ConstructRequest(String jsClass, Object ... params) {
		
		if (jsClass == null) {
			throw new IllegalArgumentException("jsClass == null)");
		}

		if (params == null) {
			throw new IllegalArgumentException("params == null");
		}

		this.jsClass = jsClass;
		this.params = params;
	}

	String getJsClass() {
		return jsClass;
	}

	Object[] getParams() {
		return params;
	}

	public Object getInstance() {
		return instance;
	}

	void setInstance(Object instance) {
		if (instance == null) {
			throw new IllegalArgumentException("instance == null");
		}

		this.instance = instance;
	}
}
