package com.test.salesportal.rest.items.model;

import java.util.List;
import java.util.Map;

public class ServiceItem {

	private Integer photoCount;
	private List<ServiceItemAttribute> displayAttributes;
	private Map<String, Object> serviceAttributes;

	public ServiceItem() {

	}

	public ServiceItem(Integer photoCount, List<ServiceItemAttribute> attributes, Map<String, Object> serviceAttributes) {
		this.photoCount = photoCount;
		this.displayAttributes = attributes;
		this.serviceAttributes = serviceAttributes;
	}

	public Integer getPhotoCount() {
		return photoCount;
	}

	public void setPhotoCount(Integer photoCount) {
		this.photoCount = photoCount;
	}

	public List<ServiceItemAttribute> getDisplayAttributes() {
		return displayAttributes;
	}

	public void setDisplayAttributes(List<ServiceItemAttribute> displayAttributes) {
		this.displayAttributes = displayAttributes;
	}

	public Map<String, Object> getServiceAttributes() {
		return serviceAttributes;
	}

	public void setServiceAttributes(Map<String, Object> serviceAttributes) {
		this.serviceAttributes = serviceAttributes;
	}
}
