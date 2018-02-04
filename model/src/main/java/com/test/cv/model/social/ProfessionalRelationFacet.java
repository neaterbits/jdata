package com.test.cv.model.social;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.test.cv.model.cv.SkillsAquiringItem;

@Entity
public class ProfessionalRelationFacet extends RelationFacet {

	// Which job related item?
	@OneToOne(optional=false)
	private SkillsAquiringItem item;
	
	public SkillsAquiringItem getItem() {
		return item;
	}
	
	public void setItem(SkillsAquiringItem item) {
		this.item = item;
	}
	
}
