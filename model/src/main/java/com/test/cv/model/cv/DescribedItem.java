package com.test.cv.model.cv;

import javax.persistence.CascadeType;
import javax.persistence.OneToOne;

import com.test.cv.model.text.Text;

public abstract class DescribedItem extends Item {

	@OneToOne(cascade={CascadeType.ALL})
	private Text summary;

	@OneToOne(cascade={CascadeType.ALL})
	private Text description;

	public Text getSummary() {
		return summary;
	}

	public void setSummary(Text summary) {
		this.summary = summary;
	}

	public Text getDescription() {
		return description;
	}

	public void setDescription(Text description) {
		this.description = description;
	}
}
