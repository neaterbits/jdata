package com.test.salesportal.model.items.attributes;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.attributes.ClassAttributes;

import junit.framework.TestCase;

public class SortableAttributesTest extends TestCase {

	public void testAttribute() {
		final ClassAttributes carAttributes = ClassAttributes.getFromClass(TestCar.class);

		final ItemAttribute odometerAttribute = carAttributes.getByName("odometer");

		assertThat(odometerAttribute).isNotNull();
		assertThat(odometerAttribute.isSortable()).isTrue();
		
		final ItemAttribute fuelAttribute = carAttributes.getByName("fuel");

		assertThat(fuelAttribute).isNotNull();
		assertThat(fuelAttribute.isSortable()).isFalse();
	}
}
