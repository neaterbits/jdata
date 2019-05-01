package com.test.salesportal.model.items.vehicular;

import com.test.salesportal.model.AttributeEnum;

public enum CarType implements AttributeEnum {
	SEDAN("Sedan"),
	HATCHBACK("Hatchback"),
	CONVERTIBLE("Convertible"),
	MINI_VAN("Mini-van"),
	SUV("SUV"),
	TRUCK("Truck"),
	COUPE("Coupe"),
	PICKUP("Pickup"),
	VAN("Van"),
	WAGON("Wagon"),
	OFFROAD("Offroad"),
	BUS("Bus"); // TODO separate main type for bus?
	
	private final String displayName;

	private CarType(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
