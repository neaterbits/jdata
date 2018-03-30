package com.test.cv.gallery;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptEngine;
import javax.script.CompiledScript;
import javax.script.Compilable;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.script.Invocable;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import junit.framework.TestCase;

@SuppressWarnings("restriction")
public class BaseJSTest extends TestCase {

	private final ScriptEngine engine;
	
	BaseJSTest() {
	    final ScriptEngineManager factory = new ScriptEngineManager();
	    
	    this.engine = factory.getEngineByName("nashorn");
	}
	
	final JSScript compileJS(String js) {
		return compileJS(new StringReader(js));
	}
	
	final JSScript compileJS(Reader js) {

		CompiledScript compiled;

		try {
			compiled = ((Compilable)engine).compile(js);
		} catch (ScriptException ex) {
			throw new IllegalArgumentException("Failed to compile script", ex);
		}
		
		return new JSScript(compiled);
	}

	final Object eval(String js) {
		try {
			return engine.eval(js);
		} catch (ScriptException ex) {
			throw new IllegalStateException("Failed to execute JS", ex);
		}
	}

	final Object registerJSFunctionCallback(Function<Object [], Object> function) {

		return new BaseJSObject(JSObectType.FUNCTION) {

			@Override
			public Object call(Object obj, Object... args) {

				// Any function-parameters must be converted
				final Object [] converted = new Object[args.length];
				
				for (int i = 0; i < args.length; ++ i) {
					final Object arg = args[i];
					final Object dst;
					
					if (arg instanceof ScriptObjectMirror) {
						final ScriptObjectMirror objectMirror = (ScriptObjectMirror)arg;
						
						if (objectMirror.isFunction()) {
							dst = new JSFunction(objectMirror);
						}
						else if (objectMirror.isArray()) {
							final Object [] javaArray = new Object[objectMirror.size()];
							
							for (int arrayIndex = 0; arrayIndex < objectMirror.size(); ++ arrayIndex) {
								javaArray[arrayIndex] = objectMirror.getSlot(arrayIndex);
							}
							
							dst = javaArray;
						}
						else {
							dst = arg;
						}
					}
					else {
						dst = arg;
					}
					
					converted[i] = dst;
				}
				
				return function.apply(converted);
			}
		};
		
	}

	static class JSScript {
		private final CompiledScript compiledScript;

		JSScript(CompiledScript compiledScript) {
			this.compiledScript = compiledScript;
		}
		
		JSRuntime run() {
			return eval(new HashMap<>());
		}
		
		JSRuntime eval(Map<String, Object> map) {
			final Bindings bindings = compiledScript.getEngine().createBindings();

			final ScriptContext scriptContext = new SimpleScriptContext();

			map.forEach((name, value) -> bindings.put(name, value));

			scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

			try {
				compiledScript.eval(scriptContext);
			} catch (ScriptException ex) {
				throw new IllegalStateException("Failed to run script ", ex);
			}
			
			return new JSRuntime(scriptContext, compiledScript);
		}
	}
	
	static class JSRuntime {
		
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

		Object invokeFunction(String function, Object ... args) {
			final Object result;
			
			if (true) {
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
		
		Object invokeMethod(Object obj, String method, Object ... args) {
			
			final Object result;
			
			final JSObject jsObj = (JSObject)obj;

			if (true) {
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
	}

	private enum JSObectType {
		ARRAY,
		FUNCTION,
		OBJECT;
	}

	private static class BaseJSObject implements JSObject {

		private final JSObectType type;

		BaseJSObject(JSObectType type) {
			
			if (type == null) {
				throw new IllegalArgumentException("type == null");
			}
			
			this.type = type;
		}

		@Override
		public Object call(Object arg0, Object... arg1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object eval(String arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getClassName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getMember(String arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getSlot(int arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasMember(String arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasSlot(int arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isArray() {
			return type == JSObectType.ARRAY;
		}

		@Override
		public boolean isFunction() {
			return type == JSObectType.FUNCTION;
		}

		@Override
		public boolean isInstance(Object arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isInstanceOf(Object arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isStrictFunction() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<String> keySet() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object newObject(Object... arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeMember(String arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setMember(String arg0, Object arg1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setSlot(int arg0, Object arg1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public double toNumber() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Collection<Object> values() {
			throw new UnsupportedOperationException();
		}
	}
	
	static class JSFunction {

		private final ScriptObjectMirror delegate;

		JSFunction(ScriptObjectMirror delegate) {
			if (delegate == null) {
				throw new IllegalArgumentException("delegate == null");
			}

			this.delegate = delegate;
		}

		Object call(Object ... params) {
			return delegate.call(null, params);
		}

		// In case calling with a param that is in fact an array
		Object callWithArray(Object [] params) {
			return delegate.call(null, (Object)params);
		}
	}
}
