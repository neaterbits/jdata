package com.test.salesportal.model.items;

import java.time.OffsetDateTime;

public final class TZDateAttributeValue extends ItemAttributeValue<OffsetDateTime> {

	public TZDateAttributeValue(ItemAttribute attribute, OffsetDateTime value) {
		super(attribute, value);
	}
}
