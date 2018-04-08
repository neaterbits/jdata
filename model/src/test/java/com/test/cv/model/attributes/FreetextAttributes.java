package com.test.cv.model.attributes;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.cv.model.ItemAttribute;
import com.test.cv.model.items.sports.Snowboard;

import junit.framework.TestCase;

public class FreetextAttributes extends TestCase {

	public void testFreetextAttribute() {
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

		final ItemAttribute descriptionAttribute = snowboardAttributes.getByName("descriptionHtml");

		assertThat(descriptionAttribute).isNotNull();
		assertThat(descriptionAttribute.isFreetext()).isTrue();

		final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");
		assertThat(widthAttribute.isFreetext()).isFalse();
	}

}
