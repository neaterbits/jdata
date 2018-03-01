package com.test.cv.search.facets;

import java.util.List;

public class ItemsFacets {

	private final List<TypeFacets> types;

	public ItemsFacets(List<TypeFacets> types) {
		
		final long uniqueCount = types.stream()
				.map(t -> t.getType())
				.distinct()
				.count();
		
		if (uniqueCount < types.size()) {
			throw new IllegalArgumentException("Repeating types");
		}
		
		this.types = types;
	}

	public List<TypeFacets> getTypes() {
		return types;
	}
}
