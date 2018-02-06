package com.test.cv.model.social;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class AcquaintanceRelationFacet extends RelationFacet {

	@Column(nullable=false)
	private AcquaintanceType type;

	public AcquaintanceType getType() {
		return type;
	}

	public void setType(AcquaintanceType type) {
		this.type = type;
	}
}
