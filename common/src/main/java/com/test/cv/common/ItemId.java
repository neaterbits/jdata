package com.test.cv.common;

// For direct lookup in S3 we need both userId and itemId since items are sorted under itemId

// TODO pass only unique itemId to save on bandwidth? Do lookup in ES to find userId and path to S3 though this is another roundtrip on server
public class ItemId {

	private String userId;
	private String itemId;
	
	public ItemId() {
		
	}

	public ItemId(String userId, String itemId) {
		this.userId = userId;
		this.itemId = itemId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
}
