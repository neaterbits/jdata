package com.test.salesportal.model.items.operations.dao;

import javax.xml.bind.annotation.XmlRootElement;

import com.test.salesportal.model.items.Item;

@XmlRootElement
public class StoreItemOperationData extends BaseUpdateItemOperationData {

	public StoreItemOperationData() {

	}

	public StoreItemOperationData(String userId, Item item) {
		super(userId, item);
	}
}
