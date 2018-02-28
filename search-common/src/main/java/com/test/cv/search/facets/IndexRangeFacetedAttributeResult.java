package com.test.cv.search.facets;

import com.test.cv.model.ItemAttribute;

/**
 * Facet result for ranges of values
 */
public final class IndexRangeFacetedAttributeResult extends IndexFacetedAttributeResult {

	// Matchcounts corresponding to the ranges in corresponding ItemAttribute
	private final int [] matchCounts;

	public IndexRangeFacetedAttributeResult(ItemAttribute attribute, int[] matchCounts) {
		super(attribute);
		
		if (matchCounts == null) {
			throw new IllegalArgumentException("matchCounts == null");
		}

		this.matchCounts = matchCounts;
	}

	public int[] getMatchCounts() {
		return matchCounts;
	}
}
