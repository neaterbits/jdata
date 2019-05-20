package com.test.salesportal.model.items.attributes;

import junit.framework.TestCase;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.attributes.ClassAttributes;

public class FacetedAttributesTest extends TestCase {
	
	// @FacetAttribute annotation
	public void testSubclassFacet() {
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(TestSnowboard.class);

		final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");

		assertThat(widthAttribute).isNotNull();
		
		assertThat(widthAttribute.isFaceted()).isTrue();
		assertThat(widthAttribute.getIntegerRanges()).isNull();
		assertThat(widthAttribute.getDecimalRanges()).isNotNull();
		assertThat(widthAttribute.getDecimalRanges().length).isEqualTo(5);
		assertThat(widthAttribute.getDecimalRanges()[0].getLower()).isNull();
		assertThat(widthAttribute.getDecimalRanges()[4].getUpper()).isNull();
	}

	// @Facet annotation
	public void testDirectFacet() {

		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(TestSnowboard.class);

		final ItemAttribute makeAttribute = snowboardAttributes.getByName("make");

		assertThat(makeAttribute).isNotNull();
		
		assertThat(makeAttribute.isFaceted()).isTrue();
		assertThat(makeAttribute.getIntegerRanges()).isNull();
		assertThat(makeAttribute.getDecimalRanges()).isNull();
	}
}
