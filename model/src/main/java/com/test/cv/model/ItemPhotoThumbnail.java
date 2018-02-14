package com.test.cv.model;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class ItemPhotoThumbnail {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@Column(nullable=false) // thumbnails and photos are ordered when presented, index 0 is the default appearing in listings
	private int index;
	
	@ManyToOne(optional=false)
	private Item item;
	
	@Column(nullable=false)
	private String mimeType;

	@OneToOne(optional=false, cascade={CascadeType.REMOVE})
	private ItemPhoto photo;
	
	@Column(nullable=false)
	private byte [] data;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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
	
	public ItemPhoto getPhoto() {
		return photo;
	}

	public void setPhoto(ItemPhoto photo) {
		this.photo = photo;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
