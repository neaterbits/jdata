package com.test.salesportal.common;

import java.util.function.Function;

public class ArrayUtil {
	public static <T, R> R [] convertArray(T [] input, Function<Integer, R []> createArray, Function<T, R> convert) {
		final R []output = createArray.apply(input.length);
		
		for (int i = 0; i < input.length; ++ i) {
			output[i] = convert.apply(input[i]);
		}

		return output;
	}
}
