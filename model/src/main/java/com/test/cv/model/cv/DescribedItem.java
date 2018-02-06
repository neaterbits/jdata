package com.test.cv.model.cv;

import javax.persistence.CascadeType;
import javax.persistence.OneToOne;

import com.test.cv.model.text.Texts;

public abstract class DescribedItem extends Item {

	@OneToOne(cascade={CascadeType.ALL})
	private Texts summary;

	@OneToOne(cascade={CascadeType.ALL})
	private Texts description;

	public Texts getSummary() {
		return summary;
	}

	public void setSummary(Texts summary) {
		this.summary = summary;
	}

	public Texts getDescription() {
		return description;
	}

	public void setDescription(Texts description) {
		this.description = description;
	}
}
