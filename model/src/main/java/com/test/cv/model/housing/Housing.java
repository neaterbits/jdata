package com.test.cv.model.housing;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.test.cv.model.annotations.DecimalRange;
import com.test.cv.model.annotations.Facet;
import com.test.cv.model.items.BaseItem;

@MappedSuperclass
public class Housing extends BaseItem {

	@Column
	@Facet("City")
	private String city;

	@Column
	@Facet(value = "Area", superAttribute = "city")
	private String area;

	@Column
	@Facet(value = "District", superAttribute = "area")
	private String district;

	@Column
	@Facet("Number of rooms")
	private BigDecimal numberOfRooms;

	@Column
	@Facet("Number of bedrooms")
	private Integer numberOfBedrooms;

	@Column
	@Facet("Number of bathrooms")
	private BigDecimal numberOfBathrooms; // can have 1.5 bathrooms, eg one with shower and toilet, one with only topilet

	@Column
	@Facet("Building status")
	private BuildingStatus buildingStatus;
	
	@Column
	@Facet(value = "Squarage",
	/*
			decimalRanges = {
					@DecimalRange(upper=500),
					@DecimalRange(lower=500, upper=1000),
					@DecimalRange(lower=1000, upper=1500),
					@DecimalRange(lower=1500, upper=2000),
					@DecimalRange(lower=2500)
			}
	*/
			decimalRanges = {
					@DecimalRange(upper=20),
					@DecimalRange(lower=20, upper=30),
					@DecimalRange(lower=30, upper=40),
					@DecimalRange(lower=40, upper=50),
					@DecimalRange(lower=50, upper=60),
					@DecimalRange(lower=60, upper=70),
					@DecimalRange(lower=70, upper=80),
					@DecimalRange(lower=80, upper=90),
					@DecimalRange(lower=90, upper=100),
					@DecimalRange(lower=100)
			}
	)
	private BigDecimal squarage;
	
	@Column
	private Date availableDate;
	
	@Column
	@Facet(value = "Wheelchair accesible", trueString="Yes", falseString="No")
	private Boolean wheelchairAccessible; 

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public BigDecimal getNumberOfRooms() {
		return numberOfRooms;
	}

	public void setNumberOfRooms(BigDecimal numberOfRooms) {
		this.numberOfRooms = numberOfRooms;
	}

	public Integer getNumberOfBedrooms() {
		return numberOfBedrooms;
	}

	public void setNumberOfBedrooms(Integer numberOfBedrooms) {
		this.numberOfBedrooms = numberOfBedrooms;
	}

	public BigDecimal getNumberOfBathrooms() {
		return numberOfBathrooms;
	}

	public void setNumberOfBathrooms(BigDecimal numberOfBathrooms) {
		this.numberOfBathrooms = numberOfBathrooms;
	}
	
	public BuildingStatus getBuildingStatus() {
		return buildingStatus;
	}

	public void setBuildingStatus(BuildingStatus buildingStatus) {
		this.buildingStatus = buildingStatus;
	}

	public BigDecimal getSquarage() {
		return squarage;
	}

	public void setSquarage(BigDecimal squarage) {
		this.squarage = squarage;
	}

	public Date getAvailableDate() {
		return availableDate;
	}

	public void setAvailableDate(Date availableDate) {
		this.availableDate = availableDate;
	}

	public Boolean getWheelchairAccessible() {
		return wheelchairAccessible;
	}

	public void setWheelchairAccessible(Boolean wheelchairAccessible) {
		this.wheelchairAccessible = wheelchairAccessible;
	}
}

