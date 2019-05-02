package com.test.salesportal.jsutils;

import java.lang.reflect.Array;

import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.JSObject;

@SuppressWarnings("restriction")
final class JSRuntime implements JSInvocable {
	private final ScriptContext scriptContext;
	private final CompiledScript compiledScript;

	JSRuntime(ScriptContext scriptContext, CompiledScript compiledScript) {
		
		if (scriptContext == null) {
			throw new IllegalArgumentException("scriptContext == null");
		}
		
		if (compiledScript == null) {
			throw new IllegalArgumentException("scriptContext == null");
		}

		this.scriptContext = scriptContext;
		this.compiledScript = compiledScript;
	}

	@Override
	public Object invokeConstructor(String function, Object ... args) {
		final Object result;

		final JSObject f = (JSObject)scriptContext.getAttribute(function, ScriptContext.ENGINE_SCOPE);

		result = f.newObject(args);

		return result;
	}
	
	@Override
	public Object invokeFunction(String function, Object ... args) {
		final Object result;
		
		if (Boolean.TRUE) {
			final JSObject f = (JSObject)scriptContext.getAttribute(function, ScriptContext.ENGINE_SCOPE);
			
			result = f.call(null, args);
		}
		else {
			try {
				result = ((Invocable)compiledScript.getEngine()).invokeFunction(function, args);
			} catch (NoSuchMethodException | ScriptException ex) {
				throw new IllegalStateException("Exception while invoking method", ex);
			}
		}

		return result;
	}
	
	@Override
	public Object invokeFunctionObject(Object function, Object... args) {
		return ((JSObject)function).call(null, args);
	}

	@Override
	public Object getVariable(String name) {
		return scriptContext.getAttribute(name);
	}

	@Override
	public Object invokeMethod(Object obj, String method, Object ... args) {
		
		if (obj == null) {
			throw new IllegalArgumentException("obj == null");
		}

		final Object result;
		
		final JSObject jsObj = (JSObject)obj;

		if (Boolean.TRUE) {
			final JSObject m = (JSObject)jsObj.getMember(method);	
			
			result = m.call(obj, args);
		}
		else {
			try {
				result = ((Invocable)compiledScript.getEngine()).invokeMethod(obj, method, args);
			} catch (NoSuchMethodException | ScriptException ex) {
				throw new IllegalStateException("Exception while invoking method", ex);
			}
		}
		
		return result;
	}

	@Override
	public Object getProperty(Object obj, String property) {

		if (obj == null) {
			throw new IllegalArgumentException("obj == null");
		}
		
		final JSObject jsObj = (JSObject)obj;

		return jsObj.getMember(property);
	}

	@Override
	public <T> T[] getJSArray(Object jsArrayObject, Class<T> memberClass) {
	
		final JSObject jsObject = (JSObject)jsArrayObject;
		
		if (!jsObject.isArray()) {
			throw new IllegalArgumentException("Not an array");
		}
		
		final int arrayLength = jsObject.values().size();
		
		@SuppressWarnings("unchecked")
		final T[] resultArray =(T[]) Array.newInstance(memberClass, arrayLength);
		
		for (int i = 0; i < arrayLength; ++ i) {
			
			@SuppressWarnings("unchecked")
			final T element = (T)jsObject.getSlot(i);
			
			resultArray[i] = element;
		}
		
		return resultArray;
	}

	
}
