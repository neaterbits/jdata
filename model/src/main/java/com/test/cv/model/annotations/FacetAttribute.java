package com.test.cv.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FacetAttribute {
	
	public String name();

	public String displayName() default "";
	
	public IntegerRange [] integerRanges() default {};
	public DecimalRange [] decimalRanges() default {};
	
}
