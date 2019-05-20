package com.test.salesportal.model.items.vehicular;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.test.salesportal.model.items.annotations.DisplayAttribute;
import com.test.salesportal.model.items.annotations.Facet;
import com.test.salesportal.model.items.base.VehicleCondition;
import com.test.salesportal.model.items.sales.RetailItem;

@MappedSuperclass
public abstract class Vehicle extends RetailItem {

	@Column
	@Facet
	@DisplayAttribute("Condition")
	private VehicleCondition condition;

	public VehicleCondition getCondition() {
		return condition;
	}

	public void setCondition(VehicleCondition condition) {
		this.condition = condition;
	}
}
