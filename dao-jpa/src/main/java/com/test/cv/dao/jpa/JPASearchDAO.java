package com.test.cv.dao.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.SortAttribute;
import com.test.cv.model.SortAttributeAndOrder;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.model.items.TypeInfo;
import com.test.cv.search.criteria.ComparisonCriterium;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.criteria.InCriterium;
import com.test.cv.search.criteria.Range;
import com.test.cv.search.criteria.RangesCriterium;
import com.test.cv.search.facets.FacetUtils;
import com.test.cv.search.facets.ItemsFacets;

public class JPASearchDAO extends JPABaseDAO implements ISearchDAO {

	public JPASearchDAO(String persistenceUnitName) {
		super(persistenceUnitName);
	}
	
	public JPASearchDAO(EntityManagerFactory entityManagerFactory) {
		super(entityManagerFactory);
	}

	// Search for criteria on all attributes on a particular type
	@Override
	public ISearchCursor search(
			List<Class<? extends Item>> types
			/*, String freeText */,
			List<Criterium> criteria,
			List<SortAttributeAndOrder> sortOrder,
			Set<ItemAttribute> facetAttributes) {

		// Must dynamically construct criteria from database by mapping to table
		final LinkedHashMap<EntityType<?>, String> typeToVarName = new LinkedHashMap<>(types.size());
		
		for (Class<? extends Item> type : types) {
			final EntityType<?> entity = entityManager.getMetamodel().entity(type);

			if (entity == null) {
				throw new IllegalArgumentException("no entity for type " + type.getName());
			}
			
			if (typeToVarName.containsKey(entity)) {
				throw new IllegalArgumentException("Duplicate entity " + entity.getName());
			}

			typeToVarName.put(entity, "item" + entity.getName());
		}

		final StringBuilder fromListBuilder = new StringBuilder();
		
		boolean first = true;
		for (Map.Entry<EntityType<?>, String> entry : typeToVarName.entrySet()) {
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
			
			allParams = constructWhereClause(typeToVarName, criteria, whereSb);
	
			whereClause = whereSb.toString();
		}
		else {
			whereClause = "";
			allParams = null;
		}
		
		final ISearchCursor searchCursor;
		
		if (facetAttributes == null || facetAttributes.isEmpty()) {
			searchCursor = makeSearchCursorForNonFacetedQuery(typeToVarName, fromListBuilder.toString(), whereClause, allParams);
		}
		else {
			searchCursor = makeSearchCursorForFacetedQuery(typeToVarName, facetAttributes, fromList, whereClause, allParams);
		}

		return searchCursor;
	}

	private ISearchCursor makeSearchCursorForNonFacetedQuery(LinkedHashMap<EntityType<?>, String> typeToVarName, String fromList, String whereClause, List<Object> allParams) {

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

		final TypedQuery<Long> countQuery = entityManager.createQuery("select " + countBilder.toString() + " from " + fromList + " " + whereClause, Long.class);
		final TypedQuery<Long> idQuery   = entityManager.createQuery("select " + itemIdBuilder.toString() + " from " + fromList + " " + whereClause, Long.class);
		final Query itemQuery = entityManager.createQuery("select " + itemBuilder.toString() +" from " + fromList + " " + whereClause);

		if (allParams != null) {
			addParams(countQuery, allParams);
			addParams(idQuery, allParams);
			addParams(itemQuery, allParams);
		}
		
		return new JPASearchCursor(countQuery, idQuery, itemQuery);
	}

	private ISearchCursor makeSearchCursorForFacetedQuery(
			LinkedHashMap<EntityType<?>, String> typeToVarName,
			Set<ItemAttribute> facetedAttributes,
			String fromList,
			String whereClause,
			List<Object> allParams) {
		
		final Query itemQuery = buildItemQuery(typeToVarName, facetedAttributes, fromList, whereClause);

		if (allParams != null) {
			addParams(itemQuery, allParams);
		}
		
		// Now we have queries for all matching items, also returning faceted attributes so we can count them
		@SuppressWarnings("unchecked")
		final List<Item> results = (List<Item>)itemQuery.getResultList();

		final List<JPASearchItem> items = new ArrayList<>(results.size());

		for (Item item : results) {
			final JPASearchItem searchItem = new JPASearchItem(item);
			
			items.add(searchItem);
		}
		
		// We can share code with Lucene mapping for building facets
		final ItemsFacets facets = FacetUtils.computeFacets(results, facetedAttributes, new FacetUtils.FacetFunctions<Item, Object>() {
			
			@Override
			public boolean isType(Item item, String typeName) {
				return typeName.equals(ItemTypes.getTypeName(item));
			}

			@Override
			public Object getField(Item item, String fieldName) {
				final TypeInfo typeInfo = ItemTypes.getTypeInfo(item);
				
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
		});

		return new JPASearchCursorWithFacets(items, facets);
	}
	
	private Query buildItemQuery(LinkedHashMap<EntityType<?>, String> typeToVarName,
			Set<ItemAttribute> facetedAttributes,
			String fromList,
			String whereClause) {
		
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

		final Query fromQuery = entityManager.createQuery("from " + fromBuilder.toString() + " " + whereClause);

		return fromQuery;
	}

	// Not in use, favoring just to get all items
	private Query buildItemAndFacetAttributesQuery(
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

				params.add(Arrays.asList(ic.getValues()));
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
