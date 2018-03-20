package com.test.cv.search.facets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.test.cv.model.ItemAttribute;

/**
 * Single value eg. non-range facet match
 */
public class IndexSingleValueFacetedAttributeResult extends IndexFacetedAttributeResult {

	private final Map<Object, IndexSingleValueFacet> values;
	
	// Count for elements where there were no match
	private int noAttributeValueCount;
	
	public IndexSingleValueFacetedAttributeResult(ItemAttribute attribute, Map<Object, IndexSingleValueFacet> values) {
		super(attribute);

		this.values = values;
	}
	
	public List<IndexSingleValueFacet> getValues() {
		return new ArrayList<>(values.values());
	}
	
	public IndexSingleValueFacet getForValue(Object value) {
		return values.get(value);
	}
	
	public void putForValue(Object value, IndexSingleValueFacet valueFacet) {
		values.put(value, valueFacet);
	}
	
	public void addToNoAttributeValueCount() {
		++ this.noAttributeValueCount;
	}
	
	public int getNoAttributeValueCount() {
		return noAttributeValueCount;
	}
}
