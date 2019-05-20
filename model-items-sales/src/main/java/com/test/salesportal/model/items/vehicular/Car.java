package com.test.salesportal.model.items.vehicular;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

import com.test.salesportal.model.items.FacetFiltering;
import com.test.salesportal.model.items.annotations.DecimalRange;
import com.test.salesportal.model.items.annotations.DisplayAttribute;
import com.test.salesportal.model.items.annotations.Facet;
import com.test.salesportal.model.items.annotations.FacetAttribute;
import com.test.salesportal.model.items.annotations.FacetAttributes;
import com.test.salesportal.model.items.annotations.FacetEntity;
import com.test.salesportal.model.items.annotations.IntegerRange;
import com.test.salesportal.model.items.annotations.NumericAttributeFiltering;
import com.test.salesportal.model.items.annotations.Sortable;
import com.test.salesportal.model.items.annotations.UpdateFacetDisplayName;

@Entity(name="car")
@FacetEntity(value="Cars", expandProperties = 4, propertyOrder = {
	"carType",
	"price",
	"productionYear",
	"odometer",
	"fuel",
	"transmission",
	"make",
	"size",
	"paintColor",
	"cylinders",
	"drive",
	"titleStatus"
	})
@FacetAttributes({
	@FacetAttribute(name="price", displayName="Price", decimalRanges={
			@DecimalRange(upper=5000),
			@DecimalRange(lower=5000,  upper=10000),
			@DecimalRange(lower=10000, upper=15000),
			@DecimalRange(lower=15000, upper=20000),
			@DecimalRange(lower=20000, upper=25000),
			@DecimalRange(lower=25000, upper=30000),
			@DecimalRange(lower=30000, upper=35000),
			@DecimalRange(lower=35000)
	})
})
@UpdateFacetDisplayName(attributeName="productionYear", updatedDisplayName="Model")
@XmlRootElement
public class Car extends Vehicle {
	
	@Column
	@Facet
	@DisplayAttribute("Type")
	private CarType carType;
	
	@Column
	@Facet
	@DisplayAttribute("Size")
	private CarSize size;

	@Column
	@Facet
	@DisplayAttribute(("Fuel"))
	private Fuel fuel;

	@Column
	@Facet
	@DisplayAttribute("Transmission")
	private Transmission transmission;

	@Column
	@Facet
	@DisplayAttribute("Cylinders")
	private Integer cylinders;

	@Column
	@Facet
	@DisplayAttribute("Drive")
	private Drive drive;

	@Column
	@Facet
	@DisplayAttribute("Title status")
	private TitleStatus titleStatus;
	
	@Sortable
	@Column
	@Facet(integerRanges = {
			@IntegerRange(upper=50000),
			@IntegerRange(lower=50000,  upper=100000),
			@IntegerRange(lower=100000, upper=150000),
			@IntegerRange(lower=150000, upper=200000),
			@IntegerRange(lower=200000, upper=250000),
			@IntegerRange(lower=250000, upper=300000),
			@IntegerRange(lower=300000),
	})
	@NumericAttributeFiltering(FacetFiltering.INPUT)
	@DisplayAttribute("Odometer")
	private Integer odometer;
	
	@Column
	@Facet
	@DisplayAttribute("Paint color")
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
