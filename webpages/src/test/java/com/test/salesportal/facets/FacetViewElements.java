package com.test.salesportal.facets;

/**
 * Corresponds to API to facetviewelement.js which creates all the DOM elements.
 * facetview.js is independent of DOM.
 */

public interface FacetViewElements<
	VIEW_ELEMENT,
	CONTAINER extends VIEW_ELEMENT,
	ROOT_OR_TYPE_CONTAINER extends CONTAINER,
	
	TYPE_OR_ATTRIBUTEVALUE_LIST_ELEMENT,
	ATTRIBUTE_VALUE_OR_RANGE_LIST,
	
	TYPE_CONTAINER,
	TYPE_LIST,
	TYPE_LIST_ELEMENT,
	
	ATTRIBUTE_LIST,
	ATTRIBUTE_ELEMENT,
	
	ATTRIBUTE_VALUE_LIST extends ATTRIBUTE_VALUE_OR_RANGE_LIST,
	ATTRIBUTE_RANGE_LIST extends ATTRIBUTE_VALUE_OR_RANGE_LIST,
	
	ATTRIBUTE_VALUE extends TYPE_OR_ATTRIBUTEVALUE_LIST_ELEMENT,
	ATTRIBUTE_RANGE,
	
	CHECKBOX
	> {

	CONTAINER getRootContainer(String divId);

	TYPE_CONTAINER createTypeContainer(CONTAINER parentElement, String text, boolean isExpanded, boolean checked);

	TYPE_LIST createTypeList(ROOT_OR_TYPE_CONTAINER parentElement, boolean isRoot);

	TYPE_LIST_ELEMENT createTypeListElement(TYPE_LIST parentElement, String text);
	
	// Attribute list might occur inder type or under attribute value (subattribute)
	ATTRIBUTE_LIST createAttributeList(TYPE_OR_ATTRIBUTEVALUE_LIST_ELEMENT parentElement);

	ATTRIBUTE_ELEMENT createAttributeListElement(ATTRIBUTE_LIST parentElement, String text, boolean isExpanded);

	ATTRIBUTE_VALUE_LIST createAttributeValueList(ATTRIBUTE_ELEMENT parentElement);
	
	ATTRIBUTE_RANGE_LIST createAttributeRangeList(ATTRIBUTE_ELEMENT parentElement);
	
	CreateAttributeValueElementResult<ATTRIBUTE_VALUE, CHECKBOX> createAttributeValueElement(
			ATTRIBUTE_VALUE_OR_RANGE_LIST parentElement,
			Object value,
			int matchCount,
			boolean hasSubAttributes,
			boolean isExpanded,
			boolean checked);

	void setCheckboxOnClick(CHECKBOX checkbox, Object onClicked);

	void removeElement(CONTAINER container, VIEW_ELEMENT element);

	void updateAttributeValueElement(VIEW_ELEMENT element, Object value, int matchCount);
}
