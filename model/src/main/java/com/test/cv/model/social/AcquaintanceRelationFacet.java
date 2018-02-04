package com.test.cv.model.social;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class AcquaintanceRelationFacet extends RelationFacet {

	@Column(nullable=false)
	private AquintanceType type;

	public AquintanceType getType() {
		return type;
	}

	public void setType(AquintanceType type) {
		this.type = type;
	}
}
