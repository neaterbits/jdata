package com.test.cv.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class Job extends Work {

	@Column
	private String employerName;

	@OneToMany
	private List<Text> position;
	
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

	public List<Text> getPosition() {
		return position;
	}

	public void setPosition(List<Text> position) {
		this.position = position;
	}
}
