package com.test.cv.facets;

// Container since may have subattributes
final class ViewAttributeValueElement extends ViewContainer<ViewElement> {
	
	private Object value;
	private int matchCount;
	private final boolean hasSubAttributes;
	private final boolean isExpanded;
	private final boolean checked;
	
	ViewAttributeValueElement(ViewList<?> parentElement, Object value, int matchCount,
			boolean hasSubAttributes, boolean isExpanded, boolean checked) {
		super(parentElement);
		
		if (value == null) {
			throw new IllegalArgumentException("value == null");
		}
		
		this.value = value;
		this.matchCount = matchCount;
		this.hasSubAttributes = hasSubAttributes;
		this.isExpanded = isExpanded;
		this.checked = checked;
	}

	void updateValue(Object value, int matchCount) {
		if (value == null) {
			throw new IllegalArgumentException("value == null");
		}

		this.value = value;
		this.matchCount = matchCount;
	}

	Object getValue() {
		return value;
	}

	int getMatchCount() {
		return matchCount;
	}

	boolean hasSubAttributes() {
		return hasSubAttributes;
	}

	boolean isExpanded() {
		return isExpanded;
	}

	boolean isChecked() {
		return checked;
	}

	@Override
	public String toString() {
		return "[value=" + value + ", matchCount=" + matchCount + "]";
	}
}
