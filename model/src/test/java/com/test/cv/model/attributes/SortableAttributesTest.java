package com.test.cv.model.attributes;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.cv.model.ItemAttribute;
import com.test.cv.model.items.vehicular.Car;

import junit.framework.TestCase;

public class SortableAttributesTest extends TestCase {

	public void testAttribute() {
		final ClassAttributes carAttributes = ClassAttributes.getFromClass(Car.class);

		final ItemAttribute odometerAttribute = carAttributes.getByName("odometer");

		assertThat(odometerAttribute).isNotNull();
		assertThat(odometerAttribute.isSortable()).isTrue();
		
		final ItemAttribute fuelAttribute = carAttributes.getByName("fuel");

		assertThat(fuelAttribute).isNotNull();
		assertThat(fuelAttribute.isSortable()).isFalse();
	}
}
