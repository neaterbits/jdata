package com.test.cv.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Photo of an item, with item photo category
 */
public class ItemPhoto {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@Column(nullable=false)
	private String mimeType;
	
	@OneToMany(cascade={CascadeType.REMOVE, CascadeType.REFRESH})
	private List<ItemPhotoCategory> categories;
	
	@Column(nullable=false)
	private byte [] data;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public List<ItemPhotoCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<ItemPhotoCategory> categories) {
		this.categories = categories;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
