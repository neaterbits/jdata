package com.test.cv.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

// Base class for all storable structured items
public abstract class Item {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	// Attributes here for cascade delete only, must be accessed through DAO queries
	@OneToMany(cascade={CascadeType.REMOVE})
	private List<ItemPhotoThumbnail> thumbnails;

	@OneToMany(cascade={CascadeType.REMOVE})
	private List<ItemPhoto> photos;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
