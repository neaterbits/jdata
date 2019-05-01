package com.test.salesportal.model.items;

import javax.persistence.Column;

import com.test.salesportal.model.annotations.Facet;
import com.test.salesportal.model.annotations.Sortable;

public abstract class RetailItem extends BaseItem {

	@Sortable(priority=4)
	@Facet("Make")
	@Column
	private String make;

	@Sortable(priority=3)
	@Facet(value = "Model", superAttribute="make")
	@Column
	private String model;
	
	@Sortable(priority=2)
	@Facet("Production year")
	@Column
	private Integer productionYear;
	
	@Facet("Seller")
	@Column
	private Seller seller;

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getProductionYear() {
		return productionYear;
	}

	public Seller getSeller() {
		return seller;
	}

	public void setSeller(Seller seller) {
		this.seller = seller;
	}

	public void setProductionYear(Integer productionYear) {
		this.productionYear = productionYear;
	}
}
