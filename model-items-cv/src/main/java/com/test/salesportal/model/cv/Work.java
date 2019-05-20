package com.test.salesportal.model.cv;

import java.util.List;

import javax.persistence.OneToMany;

public abstract class Work extends SkillsAquiringItem {

	// Sub-projects, in-house or external consultancy projets
	private List<Project> projects;

	@OneToMany
	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}
}
