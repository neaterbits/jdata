package com.test.cv.search.facets;

import java.util.List;

public class ItemsFacets {

	private final List<TypeFacets> types;

	public ItemsFacets(List<TypeFacets> types) {
		this.types = types;
	}

	public List<TypeFacets> getTypes() {
		return types;
	}
}
