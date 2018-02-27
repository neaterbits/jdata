package com.test.cv.dao.jpa;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import com.test.cv.dao.IFoundItem;
import com.test.cv.dao.ISearchCursor;
import com.test.cv.model.Item;
import com.test.cv.search.facets.ItemsFacets;

final class JPASearchCursor implements ISearchCursor {
	private final TypedQuery<Long> countQuery;
	private final TypedQuery<Long> idQuery;
	private final TypedQuery<Item> itemQuery;

	public JPASearchCursor(TypedQuery<Long> countQuery, TypedQuery<Long> idQuery, TypedQuery<Item> itemQuery) {
		this.countQuery = countQuery;
		this.idQuery = idQuery;
		this.itemQuery = itemQuery;
	}

	@Override
	public List<String> getItemIDs(int initialIdx, int count) {

		idQuery.setFirstResult(initialIdx);
		idQuery.setMaxResults(count);

		return idQuery.getResultList().stream()
				.map(id -> String.valueOf(id))
				.collect(Collectors.toList());
	}

	@Override
	public List<IFoundItem> getItems(int initialIdx, int count) {
		itemQuery.setFirstResult(initialIdx);
		itemQuery.setMaxResults(count);
		
		final List<Item> items = itemQuery.getResultList();

		return items.stream()
				.map(i -> new JPAFoundItem(i))
				.collect(Collectors.toList());
	}

	@Override
	public int getTotalMatchCount() {
		return countQuery.getSingleResult().intValue();
	}

	@Override
	public ItemsFacets getFacets() {
		throw new UnsupportedOperationException("TODO");
	}
}
