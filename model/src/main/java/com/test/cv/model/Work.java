package com.test.cv.model;

import java.util.List;

import javax.persistence.OneToMany;

public abstract class Work extends DescribedItem {

	@OneToMany
	private List<Skill> skills;

	public List<Skill> getSkills() {
		return skills;
	}

	public void setSkills(List<Skill> skills) {
		this.skills = skills;
	}
}
