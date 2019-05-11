package com.test.salesportal.model.housing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

import com.test.salesportal.model.annotations.DecimalRange;
import com.test.salesportal.model.annotations.DisplayAttribute;
import com.test.salesportal.model.annotations.Facet;
import com.test.salesportal.model.annotations.FacetAttribute;
import com.test.salesportal.model.annotations.FacetAttributes;
import com.test.salesportal.model.annotations.FacetEntity;

@Entity(name="rental_apartment")
@FacetEntity(value = "Rental apartments", propertyOrder = {
	"apartmentType",
	"price",
	"squarage",
	"city",
	"numberOfRooms",
	"numberOfBedrooms",
	"numberOfBathrooms",
	"parking",
	"furnishment",
	"appliances",
	"internet",
	"floors",
	"buildingFloors",
	"laundry",
	"storeRoom",
	"currency",
	"costPeriod",
})
@FacetAttributes({
	@FacetAttribute(name="price", displayName="Price", decimalRanges={
			@DecimalRange(upper=500),
			@DecimalRange(lower=500,  upper=1000),
			@DecimalRange(lower=1000, upper=1500),
			@DecimalRange(lower=1500, upper=2000),
			@DecimalRange(lower=2000, upper=2500),
			@DecimalRange(lower=2500, upper=3000),
			@DecimalRange(lower=3000, upper=3500),
			@DecimalRange(lower=3500)
	})
})
@XmlRootElement
public class RentalApartment extends Housing {

	
	@Column
	@Facet
	@DisplayAttribute("Cost period")
	private CostPeriod costPeriod;
	
	@Column
	@Facet
	@DisplayAttribute("Apartment type")
	private ApartmentType apartmentType;
	
	@Column
	@Facet
	@DisplayAttribute("Condition")
	private ApartmentCondition condition;

	@Column
	@Facet
	@DisplayAttribute("Renter")
	private ApartmentRenter renter;
	
	@Column
	@Facet
	@DisplayAttribute("Agent company")
	private String agentCompany;

	@Column
	@Facet(superAttribute = "agentCompany")
	@DisplayAttribute("Agent")
	private String agent;

	@Column
	@Facet
	@DisplayAttribute("Floor")
	private Integer floor;

	@Column
	@Facet
	@DisplayAttribute(("Building floors"))
	private Integer buildingFloors;

	@Column
	@Facet
	@DisplayAttribute("Laundry")
	private Laundry laundry;
	
	@Column
	@Facet
	@DisplayAttribute("Parking")
	private Parking parking;
	
	@Column
	@Facet
	@DisplayAttribute("Furnishment")
	private Furnishment furnishment;

	@Column
	@Facet
	@DisplayAttribute("Appliances")
	private Appliances appliances;

	@Column
	@Facet
	@DisplayAttribute("Storeroom")
	private StoreRoom storeRoom;

	@Column
	@Facet
	@DisplayAttribute("Heating")
	private Heating heating;

	@Column
	@Facet
	@DisplayAttribute("Air conditioning")
	private AirConditioning airConditioning;

	@Column
	@Facet
	@DisplayAttribute("Internet")
	private Internet internet;

	@Column
	@Facet
	@DisplayAttribute(value = "Smoking allowed", trueString="Yes", falseString="No")
	private Boolean smoking;

	@Column
	@Facet
	@DisplayAttribute(value = "Cats allowed", trueString="Yes", falseString="No")
	private Boolean cats;

	@Column
	@Facet
	@DisplayAttribute(value = "Dogs allowed", trueString="Yes", falseString="No")
	private Boolean dogs;

	public CostPeriod getCostPeriod() {
		return costPeriod;
	}

	public void setCostPeriod(CostPeriod costPeriod) {
		this.costPeriod = costPeriod;
	}

	public ApartmentType getApartmentType() {
		return apartmentType;
	}

	public void setApartmentType(ApartmentType apartmentType) {
		this.apartmentType = apartmentType;
	}
	
	public ApartmentCondition getCondition() {
		return condition;
	}

	public void setCondition(ApartmentCondition condition) {
		this.condition = condition;
	}

	public ApartmentRenter getRenter() {
		return renter;
	}

	public void setRenter(ApartmentRenter renter) {
		this.renter = renter;
	}
	
	public String getAgentCompany() {
		return agentCompany;
	}

	public void setAgentCompany(String agentCompany) {
		this.agentCompany = agentCompany;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}
	
	public Integer getFloor() {
		return floor;
	}

	public void setFloor(Integer floor) {
		this.floor = floor;
	}

	public Integer getBuildingFloors() {
		return buildingFloors;
	}

	public void setBuildingFloors(Integer buildingFloors) {
		this.buildingFloors = buildingFloors;
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

	public Appliances getAppliances() {
		return appliances;
	}

	public void setAppliances(Appliances appliances) {
		this.appliances = appliances;
	}

	public StoreRoom getStoreRoom() {
		return storeRoom;
	}

	public void setStoreRoom(StoreRoom storeRoom) {
		this.storeRoom = storeRoom;
	}

	public Heating getHeating() {
		return heating;
	}

	public void setHeating(Heating heating) {
		this.heating = heating;
	}

	public AirConditioning getAirConditioning() {
		return airConditioning;
	}

	public void setAirConditioning(AirConditioning airConditioning) {
		this.airConditioning = airConditioning;
	}

	public Internet getInternet() {
		return internet;
	}

	public void setInternet(Internet internet) {
		this.internet = internet;
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
