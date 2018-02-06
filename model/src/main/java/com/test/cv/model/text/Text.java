package com.test.cv.model.text;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

public class Text {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@OneToMany(cascade = { CascadeType.ALL })
	private List<Translation> texts;
	
	public Text() {
		
	}
	
	public Text(List<Translation> texts) {
		this.texts = texts;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<Translation> getTexts() {
		return texts;
	}

	public void setTexts(List<Translation> texts) {
		this.texts = texts;
	}
}
