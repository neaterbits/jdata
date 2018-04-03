package com.test.cv.facets;

public final class CreateAttributeValueElementResult<VIEW_ELEMENT, CHECKBOX> {

	private final VIEW_ELEMENT listItem;
	private final CHECKBOX checkboxItem;

	CreateAttributeValueElementResult(VIEW_ELEMENT listItem, CHECKBOX checkboxItem) {
		this.listItem = listItem;
		this.checkboxItem = checkboxItem;
	}

	public VIEW_ELEMENT getListItem() {
		return listItem;
	}

	public CHECKBOX getCheckboxItem() {
		return checkboxItem;
	}
}
