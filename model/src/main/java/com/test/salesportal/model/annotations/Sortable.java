package com.test.salesportal.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sortable {
	public String value() default "";
	
	/**
	 * Higher value means will appear earlier in list over sort alternatives
	 * and will be applied before in list of search orders for initial result.
	 * Attributes with same priority will be sorted alphabetically on property name.
	 * 
	 * @return sort order
	 */

	public int priority() default 1;
}
