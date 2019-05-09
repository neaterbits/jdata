package com.test.salesportal.rest.search.all;

import java.util.List;

import com.test.salesportal.common.CollectionUtil;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeResult;

public class AllSearchTestUtil {

	@SuppressWarnings("unchecked")
	public static <T extends SearchFacetedAttributeResult> T findAttribute(List<SearchFacetedAttributeResult> attributes, String attrId) {
		return (T)CollectionUtil.find(attributes, attribute -> attribute.getId().equals(attrId));
	}

}
