package com.test.salesportal.model.items.attributes;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.attributes.ClassAttributes;

import junit.framework.TestCase;

public class FreetextAttributes extends TestCase {

	public void testFreetextAttribute() {
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(TestSnowboard.class);

		final ItemAttribute descriptionAttribute = snowboardAttributes.getByName("descriptionHtml");

		assertThat(descriptionAttribute).isNotNull();
		assertThat(descriptionAttribute.isFreetext()).isTrue();

		final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");
		assertThat(widthAttribute.isFreetext()).isFalse();
	}

}
