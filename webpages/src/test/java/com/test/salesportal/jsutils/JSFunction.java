package com.test.salesportal.jsutils;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

@SuppressWarnings("restriction")
public final class JSFunction {
	private final ScriptObjectMirror delegate;

	JSFunction(ScriptObjectMirror delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("delegate == null");
		}

		this.delegate = delegate;
	}

	public Object call(Object ... params) {
		return delegate.call(null, params);
	}

	// In case calling with a param that is in fact an array
	public Object callWithArray(Object [] params) {
		return delegate.call(null, (Object)params);
	}
}
