package com.test.salesportal.model.items.sales;

import javax.persistence.Column;

import com.test.salesportal.model.items.FacetFiltering;
import com.test.salesportal.model.items.annotations.DisplayAttribute;
import com.test.salesportal.model.items.annotations.Facet;
import com.test.salesportal.model.items.annotations.NumericAttributeFiltering;
import com.test.salesportal.model.items.annotations.Sortable;
import com.test.salesportal.model.items.base.PurchasableItem;

public abstract class RetailItem extends PurchasableItem {

	@Sortable(priority=4)
	@Facet
	@DisplayAttribute("Make")
	@Column
	private String make;

	@Sortable(priority=3)
	@Facet(superAttribute="make")
	@DisplayAttribute("Model")
	@Column
	private String model;
	
	@Sortable(priority=2)
	@Facet
	@NumericAttributeFiltering(FacetFiltering.INPUT)
	@DisplayAttribute("Production year")
	@Column
	private Integer productionYear;
	
	@Facet
	@DisplayAttribute("Seller")
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
