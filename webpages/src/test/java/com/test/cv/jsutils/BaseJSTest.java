package com.test.cv.jsutils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import com.test.cv.common.IOUtil;

import junit.framework.TestCase;

public class BaseJSTest extends TestCase {

	private final JSEngine engine;
	
	protected BaseJSTest() {
		this.engine = new NashornEngine();
	}
	
	protected final Object createJSFunctionCallback(Function<Object [], Object> function) {
		return engine.createJSFunctionCallback(function);
	}
	
	protected final JSFunction getJSFunction(Object object) {
		return engine.getJSFunction(object);
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
	
	protected final JSInvocable prepareMavenWebAppScripts(Map<String, Object> bindings, ConstructRequest [] constructRequests, String ... scripts) throws IOException {
		
		final String s = readMavenWebAppScripts(scripts);
		
		return engine.prepare(s, bindings, constructRequests);
	}
}
