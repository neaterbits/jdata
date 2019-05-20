package com.test.salesportal.model.cv;

import javax.persistence.Entity;

@Entity
public class Education extends CVItem {

	@Override
	public <T, R> R visit(ItemVisitor<T, R> visitor, T param) {
		return visitor.onEducation(this, param);
	}
}
