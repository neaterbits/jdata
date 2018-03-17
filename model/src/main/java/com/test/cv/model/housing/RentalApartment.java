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
	@Facet(decimalRanges={
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
	@Facet
	private Laundry laundry;
	
	@Column
	@Facet
	private Parking parking;
	
	@Column
	@Facet
	private Boolean smoking;

	public BigDecimal getCostPerMonth() {
		return costPerMonth;
	}

	public void setCostPerMonth(BigDecimal costPerMonth) {
		this.costPerMonth = costPerMonth;
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

	public Boolean getSmoking() {
		return smoking;
	}

	public void setSmoking(Boolean smoking) {
		this.smoking = smoking;
	}
}
