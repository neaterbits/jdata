package com.test.cv.search.facets;

import java.util.List;

import com.test.cv.model.ItemAttribute;

/**
 * Simple eg. non-range facet match
 */
public class IndexSimpleFacetedAttributeResult extends IndexFacetedAttributeResult {

	private final int matchCount;
	private final List<IndexFacetedAttributeResult> subFacets;
	public IndexSimpleFacetedAttributeResult(ItemAttribute attribute, int matchCount, List<IndexFacetedAttributeResult> subFacets) {
		super(attribute);

		this.matchCount = matchCount;
		this.subFacets = subFacets;
	}

	public int getMatchCount() {
		return matchCount;
	}

	public List<IndexFacetedAttributeResult> getSubFacets() {
		return subFacets;
	}
}
