package com.test.cv.jsutils;

import java.util.Map;

public interface JSEvaluatable {
	JSRuntime eval(Map<String, Object> map);
}
