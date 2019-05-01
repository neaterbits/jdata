package com.test.salesportal.model.items;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.annotations.Freetext;
import com.test.salesportal.model.annotations.IndexItemAttribute;


@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class BaseItem extends Item {

	@Column
	@IndexItemAttribute(storeValue=true) // store in index so that can be retrieved straight from index
	private String originalUrl;
	
	@Column
	private Date publicationDate;
	

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

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	public String getDescriptionHtml() {
		return descriptionHtml;
	}

	public void setDescriptionHtml(String descriptionHtml) {
		this.descriptionHtml = descriptionHtml;
	}
	
}
