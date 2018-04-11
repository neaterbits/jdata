package com.test.cv.jsutils;

public abstract class JavaWrapper {
	private final JSInvocable invocable;
	private final Object jsObject;

	protected JavaWrapper(JSInvocable invocable, Object jsObject) {
		if (invocable == null) {
			throw new IllegalArgumentException("invocable == null");
		}

		if (jsObject == null) {
			throw new IllegalArgumentException("jsObject == null");
		}

		this.invocable = invocable;
		this.jsObject = jsObject;
	}

	protected final Object invokeMethod(String method, Object ... params) {
		return invocable.invokeMethod(jsObject, method, params);
	}
	
	protected final Object getProperty(String property) {
		return invocable.getProperty(jsObject, property);
	}
	
	protected final JSInvocable getInvocable() {
		return invocable;
	}
}
