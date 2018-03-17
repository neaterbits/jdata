package com.test.cv.model.housing;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.test.cv.model.annotations.Facet;
import com.test.cv.model.items.BaseItem;

@MappedSuperclass
public class Housing extends BaseItem {
	
	@Column
	@Facet
	private Integer numberOfRooms;

	@Column
	@Facet
	private Integer numberOfBedrooms;

	@Column
	@Facet
	private Integer numberOfBathrooms;

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

	public Integer getNumberOfBathrooms() {
		return numberOfBathrooms;
	}

	public void setNumberOfBathrooms(Integer numberOfBathrooms) {
		this.numberOfBathrooms = numberOfBathrooms;
	}
}


