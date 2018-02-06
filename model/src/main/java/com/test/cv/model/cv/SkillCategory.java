package com.test.cv.model.cv;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.test.cv.model.text.Texts;
import com.test.cv.model.user.User;

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
	@ManyToOne(optional=true)
	private User user;
	
	@OneToOne(cascade={CascadeType.ALL})
	private Name name;

	@OneToOne(cascade={CascadeType.ALL})
	private Texts description;

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

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public Texts getDescription() {
		return description;
	}

	public void setDescription(Texts description) {
		this.description = description;
	}
}
