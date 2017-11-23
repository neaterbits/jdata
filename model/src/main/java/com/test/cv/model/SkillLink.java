package com.test.cv.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class SkillLink extends Link {

	@ManyToOne
	private Skill skill;

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}
}
