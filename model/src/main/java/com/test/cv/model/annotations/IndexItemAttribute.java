package com.test.cv.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface IndexItemAttribute {

	public String name() default "";
	
	// Whether to store value in index for later retrieval 
	public boolean storeValue() default false;
}
