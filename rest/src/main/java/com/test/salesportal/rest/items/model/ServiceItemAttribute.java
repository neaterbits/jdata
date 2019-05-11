package com.test.salesportal.rest.items.model;

public class ServiceItemAttribute {

	private String name;
	private Object value;
	
	public ServiceItemAttribute() {

	}

	public ServiceItemAttribute(String name, Object value) {

		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}
		
		final String trimmed = name.trim();
		
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		this.name = trimmed;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
