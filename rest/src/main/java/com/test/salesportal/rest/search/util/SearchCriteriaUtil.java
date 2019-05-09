package com.test.salesportal.rest.search.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.model.criteria.SearchCriteriumValue;
import com.test.salesportal.rest.search.model.criteria.SearchRange;
import com.test.salesportal.search.criteria.Criterium;
import com.test.salesportal.search.criteria.DecimalInCriterium;
import com.test.salesportal.search.criteria.DecimalRange;
import com.test.salesportal.search.criteria.DecimalRangesCriterium;
import com.test.salesportal.search.criteria.EnumInCriterium;
import com.test.salesportal.search.criteria.InCriteriumValue;
import com.test.salesportal.search.criteria.IntegerInCriterium;
import com.test.salesportal.search.criteria.IntegerRange;
import com.test.salesportal.search.criteria.IntegerRangesCriterium;
import com.test.salesportal.search.criteria.NoValueCriterium;
import com.test.salesportal.search.criteria.StringInCriterium;

public class SearchCriteriaUtil {

	public static List<Criterium> convertCriteria(SearchCriterium [] searchCriteria) {
		final List<Criterium> criteria = new ArrayList<>(searchCriteria.length);
		
		for (int i = 0; i < searchCriteria.length; ++ i) {
			final Criterium criterium = convertCriterium(searchCriteria[i]);
			
			if (criterium == null) {
				// Probably no checkboxes were selected
			}
			else {
				criteria.add(criterium);
			}
		}

		return criteria;
	}

	private static Criterium convertCriterium(SearchCriterium searchCriterium) {
		
		// Figure out the type first
		final String typeName = searchCriterium.getType();
		
		// This is a Java type, look it up from the types list
		final TypeInfo type = ItemTypes.getTypeByName(typeName);
		
		if (type == null) {
			throw new IllegalArgumentException("Unknown type " + typeName);
		}
		
		// Find the attribute
		final ItemAttribute attribute = type.getAttributes().getByName(searchCriterium.getAttribute());
		
		if (attribute == null) {
			throw new IllegalArgumentException("Unknown attribute " + searchCriterium.getAttribute() + " from type " + typeName);
		}

		final Criterium criterium;

		final boolean includeItemsWithNoValue = searchCriterium.getOtherSelected() != null
				? searchCriterium.getOtherSelected()
			: false;
		
		final SearchRange [] ranges = searchCriterium.getRanges();
		if (ranges != null) {
			
			switch (attribute.getAttributeType()) {
			case STRING:
				throw new UnsupportedOperationException("Range query for strings");
				
			case INTEGER:
				final IntegerRange [] integerRanges = new IntegerRange[ranges.length];
				
				for (int i = 0; i < ranges.length; ++ i) {
					final SearchRange range = ranges[i];

					final IntegerRange integerRange = new IntegerRange(
							(Integer)range.getLower(), range.includeLower(),
							(Integer)range.getUpper(), range.includeUpper());
					
					integerRanges[i] = integerRange;
				}
				criterium = new IntegerRangesCriterium(attribute, integerRanges, includeItemsWithNoValue);
				break;
				
			case DECIMAL:
				final DecimalRange [] decimalRanges = new DecimalRange[ranges.length];

				for (int i = 0; i < ranges.length; ++ i) {
					final SearchRange range = ranges[i];

					final DecimalRange decimalRange = new DecimalRange(
							toDecimal(range.getLower()), range.includeLower(),
							toDecimal(range.getUpper()), range.includeUpper());
					
					decimalRanges[i] = decimalRange;
				}

				criterium = new DecimalRangesCriterium(attribute, decimalRanges, includeItemsWithNoValue);
				break;
				
			default:
				throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
			}
		}
		else if (searchCriterium.getValues() != null) {
			
			switch (attribute.getAttributeType()) {
			case STRING:
				criterium = new StringInCriterium(attribute, convertCriteriaValues(searchCriterium, o -> (String)o), includeItemsWithNoValue);
				break;

			case INTEGER:
				criterium = new IntegerInCriterium(attribute, convertCriteriaValues(searchCriterium, o -> (Integer)o), includeItemsWithNoValue);
				break;

			case DECIMAL:
				criterium = new DecimalInCriterium(attribute, convertCriteriaValues(searchCriterium, o -> toDecimal(o)), includeItemsWithNoValue);
				break;
				
			case ENUM:
				// Find enum-class from attribute
				criterium = makeEnumCriterium(attribute, searchCriterium, includeItemsWithNoValue);
				break;

			default:
				throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
			}
		}
		else if (searchCriterium.getOtherSelected() != null && searchCriterium.getOtherSelected()) {
			// Only other-values, eg "other" selected
			criterium = new NoValueCriterium(attribute);
		}
		else {
			// Nothing selected for criterium so ignore
			criterium = null;
		}

		return criterium;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T extends Comparable<T>> List<InCriteriumValue<T>> convertCriteriaValues(SearchCriterium sc, Function<Object, T> convertValue) {
		
		final SearchCriteriumValue [] values = sc.getValues();
		
		final List<InCriteriumValue<T>> list = new ArrayList<>(values.length);
		
		for (int i = 0; i < values.length; ++ i) {
			final SearchCriteriumValue value = values[i];
			final SearchCriterium [] subCriteria = value.getSubCriteria();
			final List<Criterium> sub;

			if (subCriteria != null) {
				// Convert subcriteria as well
				sub = (List)convertCriteria(subCriteria);
			}
			else {
				sub = null;
			}
			
			final T converted = convertValue.apply(value.getValue());

			list.add(new InCriteriumValue<T>(converted, sub));
		}

		return list;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <E extends Enum<E>> EnumInCriterium<E> makeEnumCriterium(ItemAttribute attribute, SearchCriterium searchCriterium, boolean includeItemsWithNoValue) {
		final Class enumClass = attribute.getAttributeValueClass();

		return new EnumInCriterium<E>(
				attribute,
				convertCriteriaValues(searchCriterium, o -> (E)Enum.valueOf(enumClass, (String)o)),
				includeItemsWithNoValue);
	}

	private static BigDecimal toDecimal(Object rangeNo) {
		final BigDecimal result;
		
		if (rangeNo == null) {
			result = null;
		}
		else if (rangeNo instanceof Integer) {
			result = BigDecimal.valueOf((Integer)rangeNo);
		}
		else if (rangeNo instanceof Double) {
			result = BigDecimal.valueOf((Double)rangeNo);
		}
		else if (rangeNo instanceof BigDecimal) {
			result = (BigDecimal)rangeNo;
		}
		else {
			throw new IllegalArgumentException("Unknown rangeNo type " + rangeNo.getClass());
		}
		
		return result;
	}
}
