package com.test.cv.model.cv;

import java.util.List;

import javax.persistence.OneToMany;

public abstract class SkillsAquiringItem extends DescribedItem {
	
	@OneToMany
	private List<Skill> skills;

	public List<Skill> getSkills() {
		return skills;
	}

	public void setSkills(List<Skill> skills) {
		this.skills = skills;
	}
}
