package com.test.cv.model.items.vehicular;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.test.cv.model.annotations.Facet;
import com.test.cv.model.items.BaseItem;
import com.test.cv.model.items.Condition;

@MappedSuperclass
public abstract class Vehicle extends BaseItem {

	@Column
	@Facet("Condition")
	private Condition condition;

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}
}
