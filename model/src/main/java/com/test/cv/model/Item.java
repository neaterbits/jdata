package com.test.cv.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

// Base class for all storable structured items
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class Item {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	@Column(nullable=false)
	private String title;
	
	// Attributes here for cascade delete only, must be accessed through DAO queries
	@OneToMany(cascade={CascadeType.REMOVE})
	private List<ItemPhotoThumbnail> thumbnails;

	@OneToMany // Cascade deleted through thumbnail (cascade={CascadeType.REMOVE})
	private List<ItemPhoto> photos;

	// Cached values for thumb width and height, only applicble for JPA
	@Column
	private Integer thumbWidth;
	
	@Column
	private Integer thumbHeight;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

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
