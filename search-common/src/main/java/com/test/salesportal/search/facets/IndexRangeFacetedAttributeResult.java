package com.test.salesportal.search.facets;

import com.test.salesportal.model.items.ItemAttribute;

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

	@Override
	public boolean hasValueOrRangeMatches() {

		for (int matchCount : matchCounts) {
			if (matchCount > 0) {
				return true;
			}
		}

		return false;
	}



	public int[] getMatchCounts() {
		return matchCounts;
	}
}
