package com.test.cv.model.cv;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.test.cv.model.text.Texts;

public class Skill {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@OneToOne(cascade={CascadeType.ALL})
	private Name name;

	@OneToOne(cascade={CascadeType.ALL})
	private Texts description;
	
	@OneToMany(cascade={})
	private List<SkillCategory> categories;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}
	
	public Texts getDescription() {
		return description;
	}

	public void setDescription(Texts description) {
		this.description = description;
	}

	public List<SkillCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<SkillCategory> categories) {
		this.categories = categories;
	}
}
