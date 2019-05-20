package com.test.salesportal.model.cv;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.test.salesportal.model.text.Text;

@Entity
public class Job extends Work {

	@Column(nullable=false)
	private String employerName;

	@OneToOne(cascade={CascadeType.ALL})
	private Text position;
	
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

	public Text getPosition() {
		return position;
	}

	public void setPosition(Text position) {
		this.position = position;
	}
}
