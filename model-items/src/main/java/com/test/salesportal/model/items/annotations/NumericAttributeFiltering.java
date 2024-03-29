package com.test.salesportal.model.items.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.test.salesportal.model.items.FacetFiltering;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NumericAttributeFiltering {
	public FacetFiltering value();
}
