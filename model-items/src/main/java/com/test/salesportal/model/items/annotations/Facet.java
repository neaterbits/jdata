package com.test.salesportal.model.items.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Facet {
	public String displayName() default "";

	public String superAttribute() default "";

	public IntegerRange [] integerRanges() default {};
	public DecimalRange [] decimalRanges() default {};

	// For boolean facets, what to show as true or false in display
	public String trueString() default "";
	public String falseString() default "";
}
