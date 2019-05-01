package com.test.salesportal.jsutils;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

final class JSScript implements JSEvaluatable {
	private final CompiledScript compiledScript;

	JSScript(CompiledScript compiledScript) {
		this.compiledScript = compiledScript;
	}
	
	JSRuntime run() {
		return eval(new HashMap<>());
	}
	
	@Override
	public JSRuntime eval(Map<String, Object> map) {
		
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
