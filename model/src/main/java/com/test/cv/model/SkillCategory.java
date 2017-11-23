package com.test.cv.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"skillId", "user"})})
public class SkillCategory {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	// human readable ID
	@Column
	private String skillCategoryId;

	// Belongs to a user?
	@ManyToOne
	private User user;
	
	@OneToMany
	private List<Text> name;

	@OneToMany
	private List<Text> description;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSkillCategoryId() {
		return skillCategoryId;
	}

	public void setSkillCategoryId(String skillCategoryId) {
		this.skillCategoryId = skillCategoryId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<Text> getName() {
		return name;
	}

	public void setName(List<Text> name) {
		this.name = name;
	}

	public List<Text> getDescription() {
		return description;
	}

	public void setDescription(List<Text> description) {
		this.description = description;
	}
}
