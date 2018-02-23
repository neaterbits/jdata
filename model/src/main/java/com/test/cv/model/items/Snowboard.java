package com.test.cv.model.items;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

@Entity(name="snowboard")
@XmlRootElement
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
