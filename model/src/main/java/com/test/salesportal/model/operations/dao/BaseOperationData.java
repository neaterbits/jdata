package com.test.salesportal.model.operations.dao;

public abstract class BaseOperationData {

	private String userId;
	
	public BaseOperationData() {

	}

	public BaseOperationData(String userId) {

		if (userId == null) {
			throw new IllegalArgumentException("userId == null");
		}
		
		this.userId = userId;
	}

	public final String getUserId() {
		return userId;
	}

	public final void setUserId(String userId) {
		this.userId = userId;
	}
}
