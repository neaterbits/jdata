package com.test.salesportal.model.items.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegerRange {
	public int lower() default Integer.MIN_VALUE;
	public int upper() default Integer.MAX_VALUE;
}
