package com.test.cv.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.test.cv.model.text.Text;

@Entity
public class ItemPhotoCategory {

	@GeneratedValue(strategy=GenerationType.AUTO)
	@Id
	private long id;

	@Column(nullable=false, unique=true)
	private String humaneReadableId;

	@OneToOne(optional=false)
	private Text name;
	
	@OneToOne
	private Text description;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getHumaneReadableId() {
		return humaneReadableId;
	}
	
	public void setHumaneReadableId(String humaneReadableId) {
		this.humaneReadableId = humaneReadableId;
	}
	
	public Text getName() {
		return name;
	}
	
	public void setName(Text name) {
		this.name = name;
	}

	public Text getDescription() {
		return description;
	}

	public void setDescription(Text description) {
		this.description = description;
	}
}
