package com.test.salesportal.model.items.base;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;

import com.test.salesportal.model.items.annotations.IndexItemAttribute;
import com.test.salesportal.model.items.photo.ItemPhoto;
import com.test.salesportal.model.items.photo.ItemPhotoThumbnail;

public class TitlePhotoItem extends TitleItem {

	// Attributes here for cascade delete only, must be accessed through DAO queries
	@OneToMany(cascade={CascadeType.REMOVE})
	private List<ItemPhotoThumbnail> thumbnails;

	@OneToMany // Cascade deleted through thumbnail (cascade={CascadeType.REMOVE})
	private List<ItemPhoto> photos;

	// Cached values for thumb width and height, only applicable for JPA
	@IndexItemAttribute(storeValue=true)
	@Column
	private Integer thumbWidth;
	
	@IndexItemAttribute(storeValue=true)
	@Column
	private Integer thumbHeight;

	public Integer getThumbWidth() {
		return thumbWidth;
	}

	public void setThumbWidth(Integer thumbWidth) {
		this.thumbWidth = thumbWidth;
	}

	public Integer getThumbHeight() {
		return thumbHeight;
	}

	public void setThumbHeight(Integer thumbHeight) {
		this.thumbHeight = thumbHeight;
	}
}
