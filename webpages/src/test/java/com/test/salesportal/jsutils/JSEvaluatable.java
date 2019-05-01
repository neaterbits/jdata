package com.test.salesportal.jsutils;

import java.util.Map;

public interface JSEvaluatable {
	JSRuntime eval(Map<String, Object> map);
}
