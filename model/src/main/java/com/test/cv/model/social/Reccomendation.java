package com.test.cv.model.social;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.test.cv.model.text.Texts;

@Entity
public class Reccomendation {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	@OneToOne(optional=false, cascade={CascadeType.ALL})
	private Texts text;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Texts getText() {
		return text;
	}

	public void setText(Texts text) {
		this.text = text;
	}
}
