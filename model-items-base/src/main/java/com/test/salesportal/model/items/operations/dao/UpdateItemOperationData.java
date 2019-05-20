package com.test.salesportal.model.items.operations.dao;

import javax.xml.bind.annotation.XmlRootElement;

import com.test.salesportal.model.items.Item;

@XmlRootElement
public class UpdateItemOperationData extends BaseUpdateItemOperationData {

	public UpdateItemOperationData() {

	}

	public UpdateItemOperationData(String userId, Item item) {
		super(userId, item);
	}
}
