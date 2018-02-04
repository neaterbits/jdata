package com.test.cv.model.cv;

import javax.persistence.Entity;

@Entity
public class SelfEmployed extends Work {

	@Override
	public <T, R> R visit(ItemVisitor<T, R> visitor, T param) {
		return visitor.onSelfEmployed(this, param);
	}
}
