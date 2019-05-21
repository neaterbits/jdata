package com.test.salesportal.rest.search.all.cache;

import java.util.Arrays;
import java.util.List;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.model.criteria.SearchCriteriumValue;
import com.test.salesportal.rest.search.model.criteria.SearchRange;


class SearchKeyMatchUtil {

	static boolean matchesSearchKey(SearchKey searchKey, Item item, ItemTypes itemTypes) {
		
		final TypeInfo itemTypeInfo = itemTypes.getTypeInfo(item);
		
		return matchesSearchCriteria(searchKey.getCriteria(), item, itemTypeInfo)
				&& ItemTypes.matchesFreeText(searchKey.getFreeText(), item, itemTypeInfo);
				
		
	}
	
	private static boolean matchesSearchCriteria(List<SearchCriterium> criteria, Item item, TypeInfo itemTypeInfo) {
	
		boolean matches;
		
		if (criteria == null) {
			matches = true;
		}
		else {
			matches = true;
		
			for (SearchCriterium criterium : criteria) {
				if (!matchesSearchCriterium(criterium, item, itemTypeInfo)) {
					matches = false;
					break;
				}
			}
		}
		
		return matches;
	}
	
	private static boolean matchesSearchCriterium(SearchCriterium criterium, Item item, TypeInfo typeInfo) {
		
		final boolean matches;
		
		if (!criterium.getType().equals(typeInfo.getTypeName())) {
			matches = false;
		}
		else {
			final ItemAttribute attribute = typeInfo.getAttributes().getByName(criterium.getAttribute());
			
			if (attribute == null) {
				throw new IllegalStateException();
			}
			
			final Object attributeValueObject = attribute.getObjectValue(item);
			
			if (attributeValueObject == null) {
				
				matches = criterium.getOtherSelected() != null && criterium.getOtherSelected();
			}
			else {

				if (attribute.isSingleValue()) {
					
					boolean matchFound = false;
					
					for (SearchCriteriumValue searchCriteriumValue : criterium.getValues()) {
						
						if (matchesSearchCriteriumValue(searchCriteriumValue, attributeValueObject, item, typeInfo)) {
							matchFound = true;
							break;
						}
					}
					
					matches = matchFound || (criterium.getOtherSelected() != null && criterium.getOtherSelected());
					
				}
				else if (attribute.isRange()) {

					boolean matchFound = false;
					
					for (SearchRange searchRange : criterium.getRanges()) {
						if (matchesSearchRange(attribute, searchRange, attributeValueObject)) {
							matchFound = true;
							break;
						}
					}
					
					matches = matchFound;
				}
				else {
					throw new UnsupportedOperationException();
				}
			}
		}
		
		return matches;
	}
	
	private static boolean matchesSearchCriteriumValue(SearchCriteriumValue searchCriteriumValue, Object valueObject, Item item, TypeInfo itemTypeInfo) {
		
		boolean matches = searchCriteriumValue.getValue().equals(valueObject);
		
		if (matches && searchCriteriumValue.getSubCriteria() != null) {
			matches = matchesSearchCriteria(Arrays.asList(searchCriteriumValue.getSubCriteria()), item, itemTypeInfo);
		}
		
		return matches;
	}
	
	private static boolean matchesSearchRange(ItemAttribute attribute, SearchRange searchRange, Object valueObject) {
		
		final boolean matches;
		
		if (valueObject == null) {
			matches = false;
		}
		else {
			matches = SearchRangeUtil.matches(
					valueObject,
					attribute,
					searchRange.getLower(),
					searchRange.includeLower(),
					searchRange.getUpper(),
					searchRange.includeUpper());
		}
		
		return matches;
	}
}
