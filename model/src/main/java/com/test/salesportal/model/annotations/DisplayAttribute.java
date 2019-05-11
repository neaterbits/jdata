package com.test.salesportal.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attribute to be displayed, eg. in ad details view
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DisplayAttribute {

	// Inherited by facets, if not set there
	public String value();

	public String trueString() default "";
	public String falseString() default "";

}
