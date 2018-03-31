package com.test.cv.jsutils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.test.cv.common.IOUtil;

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
	
	protected final JSEvaluatable compileJS(String js) {
		return compileJS(new StringReader(js));
	}
	
	protected final JSEvaluatable compileJS(Reader js) {

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

	private File getMavenWebAppScriptDir() {
		final String file = getClass().getClassLoader().getResource("get_dir_file.txt").getPath();

		final File projectBaseDir = new File(file).getParentFile().getParentFile().getParentFile()
				.getParentFile().getParentFile().getParentFile();

		final File scriptDir = new File(projectBaseDir, "src/main/webapp/js");

		/*
		final File projectBaseDir = new File(file).getParentFile().getParentFile();
		final File scriptDir = new File(projectBaseDir, "webpages/js");
		*/
		
		if (!scriptDir.exists()) {
			throw new IllegalStateException("Could not find script dir " + scriptDir);
		}

		return scriptDir;
	}

	/**
	 * Read a number of JS scripts from  src/main/webapps/js of current project and return
	 * Requires a file get_dir_file.txt to exist under src/test/resources to find current directory using classloader resource loader
	 * TODO perhaps find better way to do this
	 * 
	 * @param scripts file names to read (no paths)
	 * 
	 * @return scripts concatenated into one string
	 * @throws IOException 
	 */
	protected final String readMavenWebAppScripts(String ... scripts) throws IOException {
		
		if (scripts.length == 0) {
			throw new IllegalArgumentException("No scripts specified");
		}
		
		final File dir = getMavenWebAppScriptDir();
		
		// Read all scripts and concatenate them
		final StringBuilder sb = new StringBuilder();
		
		for (String scriptFile : scripts) {
			final String script = IOUtil.readFileToString(new File(dir, scriptFile));
			
			sb.append(script);
			
			sb.append("\n\n");
			
		}
		
		return sb.toString();
	}
	
	protected final JSEvaluatable compileMavenWebAppScripts(String ... scripts) throws IOException {
		return compileJS(readMavenWebAppScripts(scripts));
	}
}
