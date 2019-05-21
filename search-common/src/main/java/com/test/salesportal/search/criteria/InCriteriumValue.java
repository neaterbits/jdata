package com.test.salesportal.search.criteria;

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

	public boolean hasSubCriteria() {
		return subCritera != null && !subCritera.isEmpty();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((subCritera == null) ? 0 : subCritera.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InCriteriumValue<?> other = (InCriteriumValue<?>) obj;
		if (subCritera == null) {
			if (other.subCritera != null)
				return false;
		} else if (!subCritera.equals(other.subCritera))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InCriteriumValue [value=" + value + ", subCritera=" + subCritera + "]";
	}
}

