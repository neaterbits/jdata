package com.test.salesportal.dao.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import com.test.salesportal.common.StringUtil;
import com.test.salesportal.dao.ISearchCursor;
import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttribute;
import com.test.salesportal.model.items.SortAttributeAndOrder;
import com.test.salesportal.model.items.SortOrder;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.model.items.base.TitlePhotoItem;
import com.test.salesportal.search.criteria.ComparisonCriterium;
import com.test.salesportal.search.criteria.Criterium;
import com.test.salesportal.search.criteria.InCriterium;
import com.test.salesportal.search.criteria.InCriteriumValue;
import com.test.salesportal.search.criteria.Range;
import com.test.salesportal.search.criteria.RangesCriterium;
import com.test.salesportal.search.facets.FacetUtils;
import com.test.salesportal.search.facets.ItemsFacets;

public class JPASearchDAO extends JPABaseDAO implements ISearchDAO {

	private final ItemTypes itemTypes;
	
	public JPASearchDAO(String persistenceUnitName, ItemTypes itemTypes) {
		super(persistenceUnitName);

		if (itemTypes == null) {
			throw new IllegalArgumentException("itemTypes == null");
		}
		
		this.itemTypes = itemTypes;
	}
	
	public JPASearchDAO(EntityManagerFactory entityManagerFactory, ItemTypes itemTypes) {
		super(entityManagerFactory);
		
		this.itemTypes = itemTypes;
	}

	// Search for criteria on all attributes on a particular type
	@Override
	public ISearchCursor search(
			List<Class<? extends Item>> types,
			String freeText,
			List<Criterium> criteria,
			List<SortAttributeAndOrder> sortOrder,
			boolean returnSortAttributeValues,
			Set<ItemAttribute> fieldAttributes,
			Set<ItemAttribute> facetAttributes) {

		final ISearchCursor searchCursor;

		if (types == null) {
			throw new IllegalArgumentException("types == null");
		}
		
		if (types.isEmpty()) {
			searchCursor = ISearchCursor.emptyCursor();
		}
		else {
			// Must dynamically construct criteria from database by mapping to table
			final LinkedHashMap<EntityType<?>, String> entityTypeToVarName = new LinkedHashMap<>(types.size());
			
			final Map<Class<? extends Item>, String> itemTypeToVarName = new HashMap<>(types.size());
			
			for (Class<? extends Item> type : types) {
				final EntityType<?> entity = entityManager.getMetamodel().entity(type);
	
				if (entity == null) {
					throw new IllegalArgumentException("no entity for type " + type.getName());
				}
				
				if (entityTypeToVarName.containsKey(entity)) {
					throw new IllegalArgumentException("Duplicate entity " + entity.getName());
				}
	
				final String varName = "item" + entity.getName();
				
				entityTypeToVarName.put(entity, varName);
				itemTypeToVarName.put(type, varName);
			}
	
			final StringBuilder fromListBuilder = new StringBuilder();
			
			boolean first = true;
			for (Map.Entry<EntityType<?>, String> entry : entityTypeToVarName.entrySet()) {
				final EntityType<?> entity = entry.getKey();
				final String entityName = entity.getName();
				final String itemVarName = entry.getValue();
	
				if (first) {
					first = false;
				}
				else {
					fromListBuilder.append(", ");
				}
	
				fromListBuilder.append(entityName).append(' ').append(itemVarName);
			}
	
			final String fromList = fromListBuilder.toString();
	
			final String whereClause;
			final List<Object> allParams;
			
			if (criteria != null && !criteria.isEmpty()) {
				
				// Must order criteria by type so that we search on the right item
				// However some criteria may be for base types as well, but we can just apply those to all types
				final StringBuilder whereSb = new StringBuilder(" where ");
				
				allParams = constructWhereClause(entityTypeToVarName, criteria, whereSb);
		
				whereClause = whereSb.toString();
			}
			else {
				whereClause = "";
				allParams = null;
			}
			
			final String orderBy;
			
			
			if (sortOrder != null && !sortOrder.isEmpty()) {
				final StringBuilder orderBySb = new StringBuilder();
				
				constructOrderBy(itemTypeToVarName, sortOrder, orderBySb);
				
				orderBy = orderBySb.toString();
			}
			else {
				orderBy = null;
			}
			
			final String trimmedToNullFreetext = StringUtil.trimToNull(freeText);
			
			if ((facetAttributes == null || facetAttributes.isEmpty()) && trimmedToNullFreetext == null) {
				searchCursor = makeSearchCursorForNonFacetedOrFreetextQuery(entityTypeToVarName, fromListBuilder.toString(), whereClause, orderBy, allParams);
			}
			else {
				searchCursor = makeSearchCursorForFacetedOrFreetextQuery(
						entityTypeToVarName, 
						facetAttributes,
						fromList,
						whereClause,
						trimmedToNullFreetext,
						orderBy,
						allParams);
			}
		}

		return searchCursor;
	}

	private ISearchCursor makeSearchCursorForNonFacetedOrFreetextQuery(
			LinkedHashMap<EntityType<?>, String> typeToVarName,
			String fromList,
			String whereClause,
			String orderBy,
			List<Object> allParams) {

		final StringBuilder countBilder = new StringBuilder();
		final StringBuilder itemIdBuilder = new StringBuilder();
		final StringBuilder itemBuilder = new StringBuilder();
		
		boolean first = true;
		for (Map.Entry<EntityType<?>, String> entry : typeToVarName.entrySet()) {
			final String itemVarName = entry.getValue();

			if (first) {
				first = false;
			}
			else {
				countBilder.append(" + ");
				itemIdBuilder.append(", ");
				itemBuilder.append(", ");
			}

			countBilder.append("count(").append(itemVarName).append(")");
			itemIdBuilder.append(itemVarName).append(".id");
			
			appendItemColumns(itemBuilder, itemVarName).append(" ");
		}

		final TypedQuery<Long> countQuery = entityManager.createQuery(
				   "select " + countBilder.toString()
				+ " from " + fromList
				+ " " + whereClause, Long.class);
		
		final TypedQuery<Long> idQuery   = entityManager.createQuery(
					"select " + itemIdBuilder.toString()
				+ " from " + fromList
				+ " " + whereClause
				+ (orderBy != null && !orderBy.isEmpty() ? " " + orderBy : ""), Long.class);
		
		final Query itemQuery = entityManager.createQuery(
					"select " + itemBuilder.toString()
				+ " from " + fromList
				+ " " + whereClause
				+ (orderBy != null && !orderBy.isEmpty() ? " " + orderBy : ""));

		if (allParams != null) {
			addParams(countQuery, allParams);
			addParams(idQuery, allParams);
			addParams(itemQuery, allParams);
		}
		
		return new JPASearchCursor(countQuery, idQuery, itemQuery);
	}

	private ISearchCursor makeSearchCursorForFacetedOrFreetextQuery(
			LinkedHashMap<EntityType<?>, String> typeToVarName,
			Set<ItemAttribute> facetedAttributes,
			String fromList,
			String whereClause,
			String freeText,
			String orderBy,
			List<Object> allParams) {
		
		final Query itemQuery = buildItemQuery(typeToVarName, facetedAttributes, fromList, whereClause, orderBy);

		if (allParams != null) {
			addParams(itemQuery, allParams);
		}
		
		// Now we have queries for all matching items, also returning faceted attributes so we can count them
		@SuppressWarnings("unchecked")
		final List<TitlePhotoItem> withoutFreetextResult = (List<TitlePhotoItem>)itemQuery.getResultList();

		if (freeText != null && freeText.trim().isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		final List<TitlePhotoItem> results = freeText != null
				? withoutFreetextResult.stream()
						.filter(item -> ItemTypes.matchesFreeText(freeText, item, itemTypes.getTypeInfo(item)))
						.collect(Collectors.toList())
				: withoutFreetextResult;
		
		final List<JPASearchItem> items = new ArrayList<>(results.size());

		for (TitlePhotoItem item : results) {
			final JPASearchItem searchItem = new JPASearchItem(item);
			
			items.add(searchItem);
		}
		
		// We can share code with Lucene mapping for building facets
		final ItemsFacets facets;
		
		if (facetedAttributes == null || facetedAttributes.isEmpty()) {
			facets = ItemsFacets.emptyFacets();
		}
		else {
		
			facets = FacetUtils.computeFacets(
				results,
				facetedAttributes,
				itemTypes,
				new FacetUtils.FacetFunctions<TitlePhotoItem, Object>() {
				
				@Override
				public boolean isType(TitlePhotoItem item, String typeName) {
					return typeName.equals(ItemTypes.getTypeName(item));
				}
	
				@Override
				public Object getField(TitlePhotoItem item, String fieldName) {
					final TypeInfo typeInfo = itemTypes.getTypeInfo(item);
					
					return typeInfo.getAttributes().getByName(fieldName).getObjectValue(item);
				}
	
				@Override
				public Integer getIntegerValue(Object field) {
					return (Integer)field;
				}
	
				@Override
				public BigDecimal getDecimalValue(Object field) {
					return (BigDecimal)field;
				}
	
				@SuppressWarnings("unchecked")
				@Override
				public <T extends Enum<T>> T getEnumValue(Class<T> enumClass, Object field) {
					return (T)field;
				}
	
				@Override
				public Boolean getBooleanValue(Object field) {
					return (Boolean)field;
				}
	
				@Override
				public Object getObjectValue(ItemAttribute attribute, Object field) {
					return field;
				}
	
				@Override
				public boolean isNoValue(ItemAttribute attribute, Object field) {
					return false;
				}
			});
		}

		return new JPASearchCursorWithFacets(items, facets);
	}
	
	private Query buildItemQuery(LinkedHashMap<EntityType<?>, String> typeToVarName,
			Set<ItemAttribute> facetedAttributes,
			String fromList,
			String whereClause,
			String orderBy) {
		
		final StringBuilder fromBuilder = new StringBuilder();

		boolean first = true;
		for (Map.Entry<EntityType<?>, String> entry : typeToVarName.entrySet()) {
			final EntityType<?> entity = entry.getKey();
			final String itemVarName = entry.getValue();

			if (first) {
				first = false;
			}
			else {
				fromBuilder.append(", ");
			}
			
			fromBuilder.append(entity.getName()).append(' ').append(itemVarName);
		}

		final Query fromQuery = entityManager.createQuery(
				 "from " + fromBuilder.toString()
				+ " " + whereClause
				+ (orderBy != null && !orderBy.isEmpty() ? " " + orderBy : ""));

		return fromQuery;
	}

	// Not in use, favoring just to get all items
	@Deprecated
	Query buildItemAndFacetAttributesQuery(
			LinkedHashMap<EntityType<?>, String> typeToVarName,
			Set<ItemAttribute> facetedAttributes,
			String fromList,
			String whereClause) {

		// Must retrieve all entries in order to compute facets
		// Just iterate over all types and append all attributes for all kinds of results
		final StringBuilder itemBuilder = new StringBuilder();

		boolean first = true;
		for (Map.Entry<EntityType<?>, String> entry : typeToVarName.entrySet()) {
			final String itemVarName = entry.getValue();

			if (first) {
				first = false;
			}
			else {
				itemBuilder.append(", ");
			}

			appendItemColumns(itemBuilder, itemVarName).append(", ");

			// Append for facets?
			if (facetedAttributes == null || facetedAttributes.isEmpty()) {
				throw new IllegalStateException("Only for queries with faceted attributes");
			}

			boolean firstFaceted = true;
			for (ItemAttribute attribute : facetedAttributes) {
				if (firstFaceted) {
					firstFaceted = false;
				}
				else {
					itemBuilder.append(", ");
				}
				
				itemBuilder.append(itemVarName).append('.').append(attribute.getName());
			}
		}

		final Query itemQuery = entityManager.createQuery("select " + itemBuilder.toString() +" from " + fromList + " " + whereClause);

		return itemQuery;
	}
	
	private static StringBuilder appendItemColumns(StringBuilder itemBuilder, String itemVarName) {
		itemBuilder
			.append(itemVarName).append(".id, ")
			.append(itemVarName).append(".title, ")
			.append(itemVarName).append(".thumbWidth, ")
			.append(itemVarName).append(".thumbHeight");
		
		return itemBuilder;
	}

	private static List<Object> constructWhereClause(LinkedHashMap<EntityType<?>, String> typeToVarName, List<Criterium> criteria, StringBuilder whereSb) {
		final List<Object> allParams = new ArrayList<>();
		
		for (Map.Entry<EntityType<?>, String> entry : typeToVarName.entrySet()) {

			final EntityType<?> entity = entry.getKey();
			final String itemJPQLVarName = entry.getValue();

			final List<Object> thisTypeParams = addWhereClausesForCriteriaForOneType(entity, itemJPQLVarName, criteria, whereSb, allParams.size());
			
			allParams.addAll(thisTypeParams);
		}

		return allParams;
	}

	private static List<Object> addWhereClausesForCriteriaForOneType(EntityType<?> entity, String itemJPQLVarName, List<Criterium> criteria, StringBuilder whereSb, int paramNo) {
		// Find all criteria that applies to this type
		final List<Criterium> criteriaForThisType = criteria.stream()
			.filter(c -> {
				final Class<? extends Item> itemType = c.getAttribute().getItemType();
				
				return itemType.equals(entity.getJavaType());
			})
			.collect(Collectors.toList());
		
		final List<Object> thisTypeParams = constructWhereClause(criteriaForThisType, whereSb, entity, itemJPQLVarName, paramNo);

		return thisTypeParams;
	}

	private static void constructOrderBy(
			Map<Class<? extends Item>, String> itemTypeToVarName,
			List<SortAttributeAndOrder> sortOrders,
			StringBuilder orderBySb) {
		
		if (!sortOrders.isEmpty()) {
			orderBySb.append("order by ");
		}
		
		int orderByNo = 0;
		
		for (int i = 0; i < sortOrders.size(); ++ i) {
			
			final SortAttributeAndOrder sortOrder = sortOrders.get(i);
			
			final SortAttribute sortAttribute = sortOrder.getAttribute();
			
			for (Map.Entry<Class<? extends Item>, String> entry : itemTypeToVarName.entrySet()) {
				
				if (!sortAttribute.getDeclaringClass().isAssignableFrom(entry.getKey())) {
					continue;
				}
				
				if (orderByNo ++ > 0) {
					orderBySb.append(", ");
				}

				final String varName = entry.getValue();
	
				orderBySb.append(varName).append('.').append(sortAttribute.getName());
				
				if (sortOrder.getSortOrder() == SortOrder.DESCENDING) {
					orderBySb.append(" desc");
				}
			}
		}
	}

	private static void addParams(Query query, List<Object> params) {
		for (int i = 0; i < params.size(); ++ i) {
			query.setParameter("param" + i, params.get(i));
		}
	}
	
	private static List<Object> constructWhereClause(List<Criterium> criteria, StringBuilder sb, EntityType<?> entity, String itemJPQLVarName, int paramNo) {

		final List<Object> params = new ArrayList<>(criteria.size() * 2); // * 2 because of possible range params

		for (int i = 0; i < criteria.size(); ++ i) {
			
			final Criterium c = criteria.get(i);

			final String attrName = c.getAttribute().getName();
			final Attribute<?, ?> attr = entity.getAttribute(attrName);
			
			if (attr == null) {
				throw new IllegalStateException("No attibute with name " + attrName + " in entity " + entity.getName());
			}
			
			if (i > 0) {
				sb.append(" and ");
			}
			

			if (c instanceof ComparisonCriterium<?>) {
				
				final ComparisonCriterium<?> sc = (ComparisonCriterium<?>)c;
				
				sb.append(itemJPQLVarName).append('.').append(attrName);
				sb.append(' ').append(sc.getComparisonOperator().getMathString()).append(":param").append(paramNo ++);
				
				params.add(sc.getValue());
			}
			else if (c instanceof InCriterium<?>) {
				final InCriterium<?> ic = (InCriterium<?>)c;
				
				sb.append(itemJPQLVarName).append('.').append(attrName);
				
				sb.append(" in :param").append(paramNo ++);

				params.add(ic.getValues().stream().map(InCriteriumValue::getValue).collect(Collectors.toList()));
			}
			else if (c instanceof RangesCriterium<?, ?>) {
				final RangesCriterium<?, ?> rc = (RangesCriterium<?, ?>)c;
				
				sb.append(" ( ");
				
				for (int rangeIdx = 0; rangeIdx < rc.getRanges().length; ++ rangeIdx) {

					final Range<?> range = rc.getRanges()[rangeIdx];

					if (rangeIdx > 0) {
						sb.append(" or");
					}

					sb.append(" ( ");

					sb.append(' ').append(itemJPQLVarName).append('.').append(attrName);
					sb.append(' ').append(range.includeLower() ? ">=" : ">").append(' ').append(":param").append(paramNo ++);
	
					sb.append(" and ");
	
					sb.append(' ').append(attrName);
					sb.append(' ').append(range.includeUpper() ? "<=" : "<").append(' ').append(":param").append(paramNo ++);
	
					sb.append(" ) ");
	
					params.add(range.getLowerValue());
					params.add(range.getUpperValue());
				}
				
				sb.append(" ) ");
			}
			else {
				throw new UnsupportedOperationException("Unknown criteria type " + c.getClass());
			}
		}
		
		if (paramNo != params.size()) {
			throw new IllegalStateException("Params and paramNo mismatch");
		}
		
		return params;
	}
}
