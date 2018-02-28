package com.test.cv.dao.jpa;

import java.util.ArrayList;
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
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.criteria.RangeCriteria;
import com.test.cv.search.criteria.SingleValueCriteria;

public class JPASearchDAO extends JPABaseDAO implements ISearchDAO {

	public JPASearchDAO(String persistenceUnitName) {
		super(persistenceUnitName);
	}
	
	public JPASearchDAO(EntityManagerFactory entityManagerFactory) {
		super(entityManagerFactory);
	}

	// Search for criteria on all attributes on a particular type
	@Override
	public ISearchCursor search(List<Class<? extends Item>> types /*, String freeText */, List<Criterium> criteria, List<ItemAttribute> facetAttributes) {

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
		final StringBuilder countBilder = new StringBuilder();
		final StringBuilder itemIdBuilder = new StringBuilder();
		final StringBuilder itemBuilder = new StringBuilder();
		
		final Set<Map.Entry<EntityType<?>, String>> entrySet = typeToVarName.entrySet();

	
		boolean first = true;
		for (Map.Entry<EntityType<?>, String> entry : entrySet) {
			final EntityType<?> entity = entry.getKey();
			final String entityName = entity.getName();
			final String itemVarName = entry.getValue();

			if (first) {
				first = false;
			}
			else {
				fromListBuilder.append(", ");
				countBilder.append(" + ");
				itemIdBuilder.append(", ");
				itemBuilder.append(", ");
			}

			fromListBuilder.append(entityName).append(' ').append(itemVarName);
			countBilder.append("count(").append(itemVarName).append(")");
			itemIdBuilder.append(itemVarName).append(".id");
			
			itemBuilder
				.append(itemVarName).append(".id, ")
				.append(itemVarName).append(".title, ")
				.append(itemVarName).append(".thumbWidth, ")
				.append(itemVarName).append(".thumbHeight ");
		}

		final String fromList = fromListBuilder.toString();

		final String whereClause;
		final List<Object> allParams;
		
		if (criteria != null && !criteria.isEmpty()) {
			
			// Must order criteria by type so that we search on the right item
			// However some criteria may be for base types as well, but we can just apply those to all types
			final StringBuilder whereSb = new StringBuilder(" where ");
			
			allParams = new ArrayList<>();
			
			for (Map.Entry<EntityType<?>, String> entry : typeToVarName.entrySet()) {

				final EntityType<?> entity = entry.getKey();
				final String itemJPQLVarName = entry.getValue();

				// Find all criteria that applies to this type
				final List<Criterium> criteriaForThisType = criteria.stream()
					.filter(c -> {
						final Class<? extends Item> itemType = c.getAttribute().getItemType();
						
						return itemType.equals(entity.getJavaType());
					})
					.collect(Collectors.toList());
				
				final List<Object> thisTypeParams = constructWhereClause(criteriaForThisType, whereSb, entity, itemJPQLVarName, allParams.size());
				
				allParams.addAll(thisTypeParams);
			}
	
			whereClause = whereSb.toString();
		}
		else {
			whereClause = "";
			allParams = null;
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
			
			sb.append(itemJPQLVarName).append('.').append(attrName);

			if (c instanceof SingleValueCriteria<?>) {
				
				final SingleValueCriteria<?> sc = (SingleValueCriteria<?>)c;
				
				sb.append(' ').append(sc.getComparisonOperator().getMathString()).append(":param").append(paramNo ++);
				
				params.add(sc.getValue());
			}
			else if (c instanceof RangeCriteria<?>) {
				final RangeCriteria<?> rc = (RangeCriteria<?>)c;

				sb.append(" ( ");

				sb.append(' ').append(attrName);
				sb.append(' ').append(rc.includeLower() ? ">=" : ">").append(' ').append(":param").append(paramNo ++);

				sb.append(" and ");

				sb.append(' ').append(attrName);
				sb.append(' ').append(rc.includeUpper() ? "<=" : "<").append(' ').append(":param").append(paramNo ++);

				sb.append(" ) ");

				params.add(rc.getLowerValue());
				params.add(rc.getUpperValue());
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
