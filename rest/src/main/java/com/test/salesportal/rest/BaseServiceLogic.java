package com.test.salesportal.rest;

import com.test.salesportal.common.EnvVariables;

public abstract class BaseServiceLogic {

	public static final int THUMBNAIL_MAX_SIZE = 300;

	public static boolean isTest() {
		final String test = System.getenv(EnvVariables.SALESPORTAL_LOCALHOST_TEST);

		return "true".equals(test);
	}
}
