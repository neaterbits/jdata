package com.test.cv.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.criteria.RangeCriteria;
import com.test.cv.dao.criteria.SearchCriteria;import com.test.cv.dao.criteria.SingleValueCriteria;
import com.test.cv.model.Item;

public class JPASearchDAO extends JPABaseDAO implements ISearchDAO {

	public JPASearchDAO(String persistenceUnitName) {
		super(persistenceUnitName);
	}

	// Search for criteria on all attributes on a particular type
	@Override
	public ISearchCursor search(Class<? extends Item> type /*, String freeText */, SearchCriteria... criteria) {

		// Must dynamically construct criteria from database by mapping to table
		
		final EntityType<?> entity = entityManager.getMetamodel().entity(type);
		
		if (entity == null) {
			throw new IllegalArgumentException("no entity for type " + type.getName());
		}

		final StringBuilder whereSb = new StringBuilder(" where ");

		final List<Object> params = constructWhereClause(criteria, whereSb, entity);

		final String whereClause = whereSb.toString();

		final TypedQuery<Long> idQuery   = entityManager.createQuery("select item.id from item Item" + whereClause, Long.class);
		final TypedQuery<Item> itemQuery = entityManager.createQuery("from Item " + whereClause, Item.class);

		addParams(idQuery, params);
		addParams(itemQuery, params);

		return new JPASearchCursor(idQuery, itemQuery);
	}

	private static void addParams(TypedQuery<?> query, List<Object> params) {
		for (int i = 0; i < params.size(); ++ i) {
			query.setParameter(i, params.get(i));
		}
	}
	
	private static List<Object> constructWhereClause(SearchCriteria [] criteria, StringBuilder sb, EntityType<?> entity) {
		
		final String itemVar = "item";

		final List<Object> params = new ArrayList<>(criteria.length * 2); // * 2 because of possible range params

		int paramNo = 0;
		
		for (int i = 0; i < criteria.length; ++ i) {
			
			final SearchCriteria c = criteria[i];

			final String attrName = c.getAttribute().getName();
			final Attribute<?, ?> attr = entity.getAttribute(attrName);
			
			if (attr == null) {
				throw new IllegalStateException("No attibute with name " + attrName + " in entity " + entity.getName());
			}
			
			if (i > 0) {
				sb.append(" and");
			}
			
			sb.append(' ').append(itemVar).append(',').append(entity.getName());

			if (c instanceof SingleValueCriteria<?>) {
				
				final SingleValueCriteria<?> sc = (SingleValueCriteria<?>)c;
				
				sb.append(" = ").append(":param").append(paramNo ++);
				
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
