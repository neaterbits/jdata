package com.test.cv.model.items;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.test.cv.model.Item;
import com.test.cv.model.annotations.Freetext;


@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class BaseItem extends Item {

	@Column
	private String originalUrl;
	

	@Freetext
	@Column
	private String descriptionHtml;
	
	// Non-searchable sub items, eg items that are part of the same package and
	// not sold separately, eg ski poles that belong to skies or snowboard bindings not sold separately
	// TODO better way to model this?
	// private List<Item> subItems;

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	/*
	public List<Item> getSubItems() {
		return subItems;
	}

	public void setSubItems(List<Item> subItems) {
		this.subItems = subItems;
	}
	*/

	public String getDescriptionHtml() {
		return descriptionHtml;
	}

	public void setDescriptionHtml(String descriptionHtml) {
		this.descriptionHtml = descriptionHtml;
	}
	
}
