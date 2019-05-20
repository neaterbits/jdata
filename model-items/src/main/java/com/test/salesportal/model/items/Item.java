package com.test.salesportal.model.items;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import com.test.salesportal.model.items.annotations.IndexItemAttribute;
import com.test.salesportal.model.items.annotations.IndexItemAttributeTransient;
import com.test.salesportal.model.items.annotations.ServiceAttribute;

// Base class for all storable structured items
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="Item_Type")
public abstract class Item {

	public static final String MODEL_VERSION = "modelVersion";
	
	@IndexItemAttributeTransient
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	// Only there to support indexing ID as string in Lucene so can retrieve based on ID_VALUE
	@Transient
	@IndexItemAttribute(name="id", storeValue=true)
	@ServiceAttribute(name="id")
	private String idString;
	
	@IndexItemAttribute(storeValue=true)
	@Column(nullable=false)
	private long modelVersion;
	
	
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
}
