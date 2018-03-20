package com.test.cv.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Facet {
	public String value();

	public String superAttribute() default "";

	public IntegerRange [] integerRanges() default {};
	public DecimalRange [] decimalRanges() default {};

	public String trueString() default "";
	public String falseString() default "";
}
