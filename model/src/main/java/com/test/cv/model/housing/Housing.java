package com.test.cv.model.housing;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.test.cv.model.annotations.Facet;
import com.test.cv.model.items.BaseItem;

@MappedSuperclass
public class Housing extends BaseItem {
	
	@Column
	@Facet("Number of rooms")
	private Integer numberOfRooms;

	@Column
	@Facet("Number of bedrooms")
	private Integer numberOfBedrooms;

	@Column
	@Facet("Number of bathrooms")
	private BigDecimal numberOfBathrooms; // can have 1.5 bathrooms, eg one with shower and toilet, one with only topilet

	@Column
	@Facet("Squarage")
	private BigDecimal squarage;
	
	@Column
	private Date availableDate;
	
	@Column
	@Facet("Wheelchair accesible")
	private Boolean wheelchairAccessible; 

	public Integer getNumberOfRooms() {
		return numberOfRooms;
	}

	public void setNumberOfRooms(Integer numberOfRooms) {
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

