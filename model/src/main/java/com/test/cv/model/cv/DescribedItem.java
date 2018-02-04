package com.test.cv.model.cv;

import java.util.List;

import javax.persistence.OneToMany;

public abstract class DescribedItem extends Item {

	@OneToMany
	private List<Text> summary;

	@OneToMany
	private List<Text> description;

	public List<Text> getSummary() {
		return summary;
	}

	public void setSummary(List<Text> summary) {
		this.summary = summary;
	}

	public List<Text> getDescription() {
		return description;
	}

	public void setDescription(List<Text> description) {
		this.description = description;
	}
}
