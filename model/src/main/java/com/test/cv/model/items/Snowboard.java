package com.test.cv.model.items;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity(name="snowboard")
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Snowboard extends SportsItem {

	@Column
	private SnowboardProfile profile;
	
	public SnowboardProfile getProfile() {
		return profile;
	}

	public void setProfile(SnowboardProfile profile) {
		this.profile = profile;
	}
}
