package com.test.salesportal.model.items;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.annotations.Freetext;
import com.test.salesportal.model.annotations.IndexItemAttribute;
import com.test.salesportal.model.annotations.ServiceAttribute;
import com.test.salesportal.model.annotations.Sortable;


@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class BaseItem extends Item {

	@Column
	@IndexItemAttribute(storeValue=true) // store in index so that can be retrieved straight from index
	private String originalUrl;
	
	@Sortable(value="Publication date", priority=1)
	@Column
	private OffsetDateTime publicationDate;

	@Freetext
	@ServiceAttribute
	@Column
	private String descriptionHtml;
	
	@ServiceAttribute
	@Column
	private String latitude;
	
	@ServiceAttribute
	@Column
	private String longtitude;
	
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

	@XmlJavaTypeAdapter(OffsetDateTimeAdapter.class)
	public final OffsetDateTime getPublicationDate() {
		return publicationDate;
	}

	public final void setPublicationDate(OffsetDateTime publicationDate) {
		this.publicationDate = publicationDate;
	}

	public String getDescriptionHtml() {
		return descriptionHtml;
	}

	public void setDescriptionHtml(String descriptionHtml) {
		this.descriptionHtml = descriptionHtml;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(String longtitude) {
		this.longtitude = longtitude;
	}
}
