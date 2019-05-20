package com.test.salesportal.model.items.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DecimalRange {
	public double lower() default Double.MIN_VALUE;
	public double upper() default Double.MAX_VALUE;
}
