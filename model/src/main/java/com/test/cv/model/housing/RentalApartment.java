package com.test.cv.model.housing;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

import com.test.cv.model.annotations.DecimalRange;
import com.test.cv.model.annotations.Facet;

@Entity(name="rental_apartment")
@XmlRootElement
public class RentalApartment extends Housing {

	@Column
	@Facet(value = "Cost per month",
			decimalRanges={
			@DecimalRange(upper=500),
			@DecimalRange(lower=500,  upper=1000),
			@DecimalRange(lower=1000, upper=1500),
			@DecimalRange(lower=1500, upper=2000),
			@DecimalRange(lower=2000, upper=2500),
			@DecimalRange(lower=2500, upper=3000),
			@DecimalRange(lower=3000, upper=3500),
			@DecimalRange(lower=3500)
	})
	private BigDecimal costPerMonth;
	
	@Column
	@Facet("Apartment type")
	private ApartmentType apartmentType;
	
	@Column
	@Facet("Laundry")
	private Laundry laundry;
	
	@Column
	@Facet("Parking")
	private Parking parking;
	
	@Column
	@Facet("Furnishment")
	private Furnishment furnishment;

	@Column
	@Facet("Smoking allowed")
	private Boolean smoking;

	@Column
	@Facet("Cats allowed")
	private Boolean cats;

	@Column
	@Facet("Dogs allowed")
	private Boolean dogs;

	public BigDecimal getCostPerMonth() {
		return costPerMonth;
	}

	public void setCostPerMonth(BigDecimal costPerMonth) {
		this.costPerMonth = costPerMonth;
	}
	
	public ApartmentType getApartmentType() {
		return apartmentType;
	}

	public void setApartmentType(ApartmentType apartmentType) {
		this.apartmentType = apartmentType;
	}

	public Laundry getLaundry() {
		return laundry;
	}

	public void setLaundry(Laundry laundry) {
		this.laundry = laundry;
	}

	public Parking getParking() {
		return parking;
	}

	public void setParking(Parking parking) {
		this.parking = parking;
	}
	
	public Furnishment getFurnishment() {
		return furnishment;
	}

	public void setFurnishment(Furnishment furnishment) {
		this.furnishment = furnishment;
	}

	public Boolean getSmoking() {
		return smoking;
	}

	public void setSmoking(Boolean smoking) {
		this.smoking = smoking;
	}

	public Boolean getCats() {
		return cats;
	}

	public void setCats(Boolean cats) {
		this.cats = cats;
	}

	public Boolean getDogs() {
		return dogs;
	}

	public void setDogs(Boolean dogs) {
		this.dogs = dogs;
	}
}
