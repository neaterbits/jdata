package com.test.cv.search.criteria;

import java.util.List;

public final class InCriteriumValue<T extends Comparable<T>> {

	private final T value;
	private final List<InCriterium<?>> subCritera;

	public InCriteriumValue(T value, List<InCriterium<?>> subCritera) {
		if (value == null) {
			throw new IllegalArgumentException("value == null");
		}

		this.value = value;
		this.subCritera = subCritera;
	}

	public T getValue() {
		return value;
	}

	public List<InCriterium<?>> getSubCritera() {
		return subCritera;
	}
}

