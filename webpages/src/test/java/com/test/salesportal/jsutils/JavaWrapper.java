package com.test.salesportal.jsutils;

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
	
	protected final <T> T[] getJSArray(Object jsArrayObject, Class<T> memberClass) {
		return invocable.getJSArray(jsArrayObject, memberClass);
	}
	
	protected final JSInvocable getInvocable() {
		return invocable;
	}

	protected static int nonNullNumberToExactInt(Object o) {
		final int result;

		if (o == null) {
			throw new IllegalStateException("o is null");
		}
		else if (o instanceof Double) {
			final Double d = (Double)o;

			if (Math.round(d) != d) {
				throw new IllegalStateException("Not an integer: ");
			}
			
			result = d.intValue();
		}
		else if (o instanceof Integer) {
			result = (Integer)o;
		}
		else {
			throw new IllegalStateException("Unknown type for " + o.getClass());
		}

		return result;
	}
}
