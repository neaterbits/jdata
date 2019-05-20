package com.test.salesportal.model.items.attributes;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.annotations.DisplayAttribute;
import com.test.salesportal.model.items.annotations.Facet;
import com.test.salesportal.model.items.annotations.Sortable;

public class TestCar extends Item {

	@Facet
	@DisplayAttribute(("Fuel"))
	private String fuel;

	@Sortable
	@DisplayAttribute("Odometer")
	private Integer odometer;

	public String getFuel() {
		return fuel;
	}

	public void setFuel(String fuel) {
		this.fuel = fuel;
	}

	public Integer getOdometer() {
		return odometer;
	}

	public void setOdometer(Integer odometer) {
		this.odometer = odometer;
	}
}
