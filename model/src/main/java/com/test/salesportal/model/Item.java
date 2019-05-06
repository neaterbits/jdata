package com.test.salesportal.model;

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

import com.test.salesportal.model.annotations.Freetext;
import com.test.salesportal.model.annotations.IndexItemAttribute;
import com.test.salesportal.model.annotations.IndexItemAttributeTransient;
import com.test.salesportal.model.annotations.Sortable;

// Base class for all storable structured items
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class Item {

	public static final String MODEL_VERSION = "modelVersion";
	
	@IndexItemAttributeTransient
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	// Only there to support indexing ID as string in Lucene so can retrieve based on ID
	@IndexItemAttribute(name="id", storeValue=true)
	private String idString;
	
	@IndexItemAttribute(storeValue=true)
	@Column(nullable=false)
	private long modelVersion;
	
	@Sortable(value="Title", priority=5)
	@Freetext
	@IndexItemAttribute(storeValue=true) // must store for quick-lookup in search results
	@Column(nullable=false)
	private String title;
	
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
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIdString() {
		return idString;
	}

	public void setIdString(String idString) {
		this.idString = idString;
	}

	public long getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(long modelVersion) {
		this.modelVersion = modelVersion;
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
