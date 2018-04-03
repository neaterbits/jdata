package com.test.cv.facets;

final class TestFacetViewElements implements FacetViewElements<

		ViewContainer,
		ViewContainer,
		
		ViewContainer,
		ViewList,
		
		ViewTypeContainer,
		ViewTypeList,
		ViewTypeListElement,
		
		ViewAttributeList,
		ViewAttributeListElement,
		
		ViewAttributeValueList,
		ViewAttributeRangeList,
		
		ViewAttributeValueElement,
		ViewAttributeValueElement,
		ViewCheckbox>{
	

	private final ViewRootContainer rootContainer = new ViewRootContainer();
	
	@Override
	public ViewContainer getRootContainer(String divId) {
		return rootContainer;
	}

	@Override
	public ViewTypeContainer createTypeContainer(ViewContainer parentElement, String text, boolean isExpanded,
			boolean checked) {

		return new ViewTypeContainer(parentElement, text);
	}

	@Override
	public ViewTypeList createTypeList(ViewContainer parentElement, boolean isRoot) {
		return new ViewTypeList(parentElement, isRoot);
	}

	@Override
	public ViewTypeListElement createTypeListElement(ViewTypeList parentElement, String text) {
		return new ViewTypeListElement(parentElement, text);
	}

	@Override
	public ViewAttributeList createAttributeList(ViewContainer parentElement) {
		return new ViewAttributeList(parentElement);
	}

	@Override
	public ViewAttributeListElement createAttributeListElement(ViewAttributeList parentElement, String text, boolean isExpanded) {
		return new ViewAttributeListElement(parentElement, text);
	}

	@Override
	public ViewAttributeValueList createAttributeValueList(ViewAttributeListElement parentElement) {
		return new ViewAttributeValueList(parentElement);
	}

	@Override
	public ViewAttributeRangeList createAttributeRangeList(ViewAttributeListElement parentElement) {
		return new ViewAttributeRangeList(parentElement);
	}

	@Override
	public CreateAttributeValueElementResult<ViewAttributeValueElement, ViewCheckbox> createAttributeValueElement(ViewList parentElement, Object value, int matchCount,
			boolean hasSubAttributes, boolean isExpanded, boolean checked) {

		final ViewAttributeValueElement valueElement = new ViewAttributeValueElement(
				parentElement,
				value,
				matchCount,
				hasSubAttributes,
				isExpanded,
				checked);
		
		final ViewCheckbox checkbox = new ViewCheckbox(parentElement);

		return new CreateAttributeValueElementResult<>(valueElement, checkbox);
	}

	@Override
	public void setCheckboxOnClick(ViewCheckbox checkbox, Object onClicked) {
		checkbox.setOnClicked(onClicked);
	}
}
