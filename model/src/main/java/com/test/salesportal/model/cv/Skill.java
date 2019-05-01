package com.test.salesportal.model.cv;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.test.salesportal.model.text.Text;

public class Skill {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@OneToOne(cascade={CascadeType.ALL})
	private Name name;

	@OneToOne(cascade={CascadeType.ALL})
	private Text description;
	
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
	
	public Text getDescription() {
		return description;
	}

	public void setDescription(Text description) {
		this.description = description;
	}

	public List<SkillCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<SkillCategory> categories) {
		this.categories = categories;
	}
}
