package com.test.cv.model.items.vehicular;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

import com.test.cv.model.annotations.Facet;
import com.test.cv.model.annotations.FacetEntity;
import com.test.cv.model.annotations.IntegerRange;
import com.test.cv.model.annotations.Sortable;

@Entity(name="car")
@FacetEntity(value="Cars", propertyOrder = {
	"carType",
	"size",
	"fuel",
	"transmission",
	"cylinders",
	"drive",
	"titleStatus",
	"odometer",
	"paintColor"
	})
@XmlRootElement
public class Car extends Vehicle {
	
	@Column
	@Facet("Type")
	private CarType carType;
	
	@Column
	@Facet("Size")
	private CarSize size;

	@Column
	@Facet("Fuel")
	private Fuel fuel;

	@Column
	@Facet("Transmission")
	private Transmission transmission;

	@Column
	@Facet("Cylinders")
	private Integer cylinders;

	@Column
	@Facet("Drive")
	private Drive drive;

	@Column
	@Facet("Title status")
	private TitleStatus titleStatus;
	
	@Sortable
	@Column
	@Facet(value = "Odometer", integerRanges = {
			@IntegerRange(upper=50000),
			@IntegerRange(lower=50000,  upper=100000),
			@IntegerRange(lower=100000, upper=150000),
			@IntegerRange(lower=150000, upper=200000),
			@IntegerRange(lower=200000, upper=250000),
			@IntegerRange(lower=250000, upper=300000),
			@IntegerRange(lower=300000),
	})
	private Integer odometer;
	
	@Column
	@Facet("Paint color")
	private String paintColor;
	
	@Column
	private String vin;

	public CarType getCarType() {
		return carType;
	}

	public void setCarType(CarType carType) {
		this.carType = carType;
	}

	public CarSize getSize() {
		return size;
	}

	public void setSize(CarSize size) {
		this.size = size;
	}

	public Fuel getFuel() {
		return fuel;
	}

	public void setFuel(Fuel fuel) {
		this.fuel = fuel;
	}

	public Transmission getTransmission() {
		return transmission;
	}

	public void setTransmission(Transmission transmission) {
		this.transmission = transmission;
	}

	public Integer getCylinders() {
		return cylinders;
	}

	public void setCylinders(Integer cylinders) {
		this.cylinders = cylinders;
	}

	public Drive getDrive() {
		return drive;
	}

	public void setDrive(Drive drive) {
		this.drive = drive;
	}

	public TitleStatus getTitleStatus() {
		return titleStatus;
	}

	public void setTitleStatus(TitleStatus titleStatus) {
		this.titleStatus = titleStatus;
	}

	public Integer getOdometer() {
		return odometer;
	}

	public void setOdometer(Integer odometer) {
		this.odometer = odometer;
	}

	public String getPaintColor() {
		return paintColor;
	}

	public void setPaintColor(String paintColor) {
		this.paintColor = paintColor;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}
}
