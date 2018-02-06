package com.test.cv.model.cv;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.test.cv.model.text.Translation;

@Entity
public class Project extends SkillsAquiringItem {

	@Column
	private String client;

	@OneToMany
	private List<Translation> position;
	
	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public List<Translation> getPosition() {
		return position;
	}

	public void setPosition(List<Translation> position) {
		this.position = position;
	}
	
	@Override
	public <T, R> R visit(ItemVisitor<T, R> visitor, T param) {
		return visitor.onProject(this, param);
	}
}
