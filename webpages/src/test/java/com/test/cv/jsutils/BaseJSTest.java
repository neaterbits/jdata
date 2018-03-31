package com.test.cv.jsutils;

import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptEngine;
import javax.script.CompiledScript;
import javax.script.Compilable;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import junit.framework.TestCase;

@SuppressWarnings("restriction")
public class BaseJSTest extends TestCase {

	private final ScriptEngine engine;
	
	protected BaseJSTest() {
	    final ScriptEngineManager factory = new ScriptEngineManager();
	    
	    this.engine = factory.getEngineByName("nashorn");
	}
	
	protected final JSScript compileJS(String js) {
		return compileJS(new StringReader(js));
	}
	
	protected final JSScript compileJS(Reader js) {

		CompiledScript compiled;

		try {
			compiled = ((Compilable)engine).compile(js);
		} catch (ScriptException ex) {
			throw new IllegalArgumentException("Failed to compile script", ex);
		}
		
		return new JSScript(compiled);
	}

	protected final Object eval(String js) {
		try {
			return engine.eval(js);
		} catch (ScriptException ex) {
			throw new IllegalStateException("Failed to execute JS", ex);
		}
	}

	protected final Object registerJSFunctionCallback(Function<Object [], Object> function) {

		return new BaseJSObject(JSObjectType.FUNCTION) {

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
}
