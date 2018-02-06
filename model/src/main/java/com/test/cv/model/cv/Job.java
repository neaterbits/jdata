package com.test.cv.model.cv;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.test.cv.model.text.Texts;

@Entity
public class Job extends Work {

	@Column(nullable=false)
	private String employerName;

	@OneToOne(cascade={CascadeType.ALL})
	private Texts position;
	
	@Override
	public <T, R> R visit(ItemVisitor<T, R> visitor, T param) {
		return visitor.onJob(this, param);
	}

	public String getEmployerName() {
		return employerName;
	}

	public void setEmployerName(String employerName) {
		this.employerName = employerName;
	}

	public Texts getPosition() {
		return position;
	}

	public void setPosition(Texts position) {
		this.position = position;
	}
}
