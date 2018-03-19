package com.test.cv.model.items.sports;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

import com.test.cv.model.annotations.DecimalRange;
import com.test.cv.model.annotations.Facet;
import com.test.cv.model.annotations.FacetAttribute;
import com.test.cv.model.annotations.FacetAttributes;

// Belongs to superclass so must define here
@FacetAttributes({
	@FacetAttribute(name="width", displayName = "Width", decimalRanges={
			@DecimalRange(upper=27.0),
			@DecimalRange(lower=27.0,upper=29.0),
			@DecimalRange(lower=29.0,upper=31.0),
			@DecimalRange(lower=31.0,upper=33.0),
			@DecimalRange(lower=33.0)
	}),

	@FacetAttribute(name="length", displayName = "Height", decimalRanges={
			@DecimalRange(upper=150.0),
			@DecimalRange(lower=150.0,upper=160.0),
			@DecimalRange(lower=160.0,upper=170),
			@DecimalRange(lower=170)
	})
})
@Entity(name="snowboard")
@XmlRootElement
public class Snowboard extends SportsItem {

	@Facet("Profile")
	@Column
	private SnowboardProfile profile;
	
	public SnowboardProfile getProfile() {
		return profile;
	}

	public void setProfile(SnowboardProfile profile) {
		this.profile = profile;
	}
}
