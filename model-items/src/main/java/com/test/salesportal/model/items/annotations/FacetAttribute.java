package com.test.salesportal.model.items.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FacetAttribute {
	
	public String name();

	public String displayName() default "";
	
	public String superAttribute() default "";

	public IntegerRange [] integerRanges() default {};
	public DecimalRange [] decimalRanges() default {};

	public String trueString() default "";
	public String falseString() default "";
}
