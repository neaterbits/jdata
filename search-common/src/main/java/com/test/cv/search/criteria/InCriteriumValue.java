package com.test.cv.search.criteria;

import java.util.List;

public final class InCriteriumValue<T extends Comparable<T>> {

	private final T value;
	private final List<Criterium> subCritera;

	public InCriteriumValue(T value, List<Criterium> subCritera) {
		if (value == null) {
			throw new IllegalArgumentException("value == null");
		}

		this.value = value;
		this.subCritera = subCritera;
	}

	public T getValue() {
		return value;
	}

	public List<Criterium> getSubCritera() {
		return subCritera;
	}
}

