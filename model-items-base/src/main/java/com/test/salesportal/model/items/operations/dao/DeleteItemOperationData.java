package com.test.salesportal.model.items.operations.dao;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeleteItemOperationData extends BaseOperationData {

	private String itemId;

	public DeleteItemOperationData() {
		super();
	}

	public DeleteItemOperationData(String userId, String itemId) {
		super(userId);
		
		if (itemId == null) {
			throw new IllegalArgumentException("itemId == null");
		}
		
		this.itemId = itemId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
}
