package com.test.salesportal.model.operations.dao;

import com.test.salesportal.model.Item;

public abstract class BaseUpdateItemOperationData extends BaseOperationData {

	private Item item;

	public BaseUpdateItemOperationData() {

	}

	public BaseUpdateItemOperationData(String userId, Item item) {
		super(userId);
		
		if (item == null) {
			throw new IllegalArgumentException("item == null");
		}
		
		this.item = item;
	}

	public final Item getItem() {
		return item;
	}

	public final void setItem(Item item) {
		this.item = item;
	}
}
