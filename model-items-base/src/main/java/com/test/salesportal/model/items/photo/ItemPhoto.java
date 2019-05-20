package com.test.salesportal.model.items.photo;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.test.salesportal.model.items.Item;

/**
 * Photo of an item, with item photo category
 */
@Entity
public class ItemPhoto {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	@OneToOne(optional=false)
	private Item item;
	
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
	
	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
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
