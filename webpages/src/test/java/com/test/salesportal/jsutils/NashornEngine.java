package com.test.salesportal.jsutils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.test.salesportal.common.IOUtil;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

@SuppressWarnings("restriction")
public class NashornEngine implements JSEngine {
	private final ScriptEngine engine;
	
	public NashornEngine() {
	    final ScriptEngineManager factory = new ScriptEngineManager();
	    
	    this.engine = factory.getEngineByName("nashorn");
	}

	private JSEvaluatable compileJS(String js) {
		return compileJS(new StringReader(js));
	}
	
	private JSEvaluatable compileJS(Reader js) {

		CompiledScript compiled;

		try {
			compiled = ((Compilable)engine).compile(js);
		} catch (ScriptException ex) {
			throw new IllegalArgumentException("Failed to compile script", ex);
		}
		
		return new JSScript(compiled);
	}
	
	
	@Override
	public final Object createJSFunctionCallback(Function<Object [], Object> function) {

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


	@Override
	public JSFunction getJSFunction(Object object) {
		return new JSFunction((ScriptObjectMirror)object);
	}

	protected final Object eval(String js) {
		try {
			return engine.eval(js);
		} catch (ScriptException ex) {
			throw new IllegalStateException("Failed to execute JS", ex);
		}
	}

	
	@Override
	public JSInvocable prepare(String string, Map<String, Object> bindings, ConstructRequest... constructRequests) {
		return prepareFromString(string, bindings, constructRequests);
	}

	@Override
	public JSInvocable prepare(Reader reader, Map<String, Object> bindings, ConstructRequest... constructRequests) throws IOException {
		return prepareFromString(IOUtil.readAll(reader), bindings, constructRequests);
	}

	private JSInvocable prepareFromString(String string, Map<String, Object> bindings, ConstructRequest... constructRequests) {
		final Map<String, Object> b = new HashMap<>(bindings);
		
		// Does not seem like invoking constructors are working properly, so just generate helper functions here
		// and compile/evaluate those as well
		// So create factory methods here
		
		String s = string;
		

		for (int i = 0; i < constructRequests.length; ++ i) {
			
			final ConstructRequest request = constructRequests[i];
		
			s += buildConstructFunction(request);
		}
		
		// Construct function
		
		final JSInvocable invocable = compileJS(s).eval(b);
		
		// Invoke all construct requests to create instances
		for (int i = 0; i < constructRequests.length; ++ i) {
			
			final ConstructRequest request = constructRequests[i];

			final Object [] paramsCopy = Arrays.copyOf(request.getParams(), request.getParams().length);
			
			for (int paramNo = 0; paramNo < paramsCopy.length; ++ paramNo) {
				if (paramsCopy[paramNo] instanceof ConstructRequest) {
					// Dependency to other constructed instance, ought to have been constructed already
					// so just swap out param
					ConstructRequest found = null;
					for (int j = 0; j < i; ++ j) {
						if (constructRequests[j] == paramsCopy[paramNo]) {
							found = constructRequests[j];
							break;
						}
					}
					
					if (found == null) {
						throw new IllegalStateException("No prior constructrequest found for " + paramsCopy[paramNo]);
					}
					
					if (found.getInstance() == null) { 
						throw new IllegalStateException("No instance for dependency " + found + " for " + paramsCopy[paramNo]);
					}
					
					paramsCopy[paramNo] = found.getInstance();
				}
			}
			
			// Check if a
			
			final Object instance = invocable.invokeFunction(constructFunctionName(request), paramsCopy);
			
			if (instance == null) {
				throw new IllegalStateException("Failed to create instance for " + request.getJsClass());
			}
			
			request.setInstance(instance);
		}
		
		return invocable;
	}
	
	private static String buildConstructFunction(ConstructRequest request) {
		final StringBuilder function = new StringBuilder();

		function.append("function ").append(constructFunctionName(request)).append('(');
		
		final Object [] params = request.getParams();

		paramNames(function, params);
		
		function.append(") {\n");
		
		function.append(" return new ").append(request.getJsClass()).append('(');
		
		paramNames(function, params);
		
		function.append(");\n");
		
		function.append("}\n");

		return function.toString();
	}
	
	private static void paramNames(StringBuilder sb, Object [] params) {
		for (int j = 0; j < params.length; ++ j) {
			
			if (j > 0) {
				sb.append(", ");
			}
			
			sb.append("param").append(j);
		}
	}
	
	private static String constructFunctionName(ConstructRequest request) {
		return "_construct_" + request.getJsClass();
	}

	private static String constructParamName(ConstructRequest request, int paramNo) {
		return "_construct_param_" + request.getJsClass() + "_" + paramNo;
	}
}
