package com.test.cv.model.items.vehicular;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.test.cv.model.annotations.Facet;
import com.test.cv.model.items.BaseItem;
import com.test.cv.model.items.RetailItem;
import com.test.cv.model.items.VehicleCondition;

@MappedSuperclass
public abstract class Vehicle extends RetailItem {

	@Column
	@Facet("Condition")
	private VehicleCondition condition;

	public VehicleCondition getCondition() {
		return condition;
	}

	public void setCondition(VehicleCondition condition) {
		this.condition = condition;
	}
}
