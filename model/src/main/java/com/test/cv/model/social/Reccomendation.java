package com.test.cv.model.social;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.test.cv.model.text.Text;

public class Reccomendation {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@OneToMany(cascade={ CascadeType.ALL })
	private List<Text> text;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
