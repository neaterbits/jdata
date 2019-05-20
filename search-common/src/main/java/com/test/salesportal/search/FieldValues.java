package com.test.salesportal.search;

import com.test.salesportal.model.items.PropertyAttribute;

public interface FieldValues<T extends PropertyAttribute> {

	Object getValue(T attribute);
}
