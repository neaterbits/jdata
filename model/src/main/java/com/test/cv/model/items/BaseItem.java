package com.test.cv.model.items;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.test.cv.model.Item;
import com.test.cv.model.annotations.Facet;
import com.test.cv.model.annotations.Freetext;
import com.test.cv.model.annotations.Sortable;


@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class BaseItem extends Item {

	@Sortable(priority=4)
	@Facet("Make")
	@Column
	private String make;

	@Facet(value = "Model", superAttribute="make")
	@Column
	private String model;
	
	@Sortable(priority=4)
	@Facet("Production year")
	@Column
	private Integer productionYear;
	
	@Facet("Seller")
	@Column
	private Seller seller;

	@Freetext
	@Column
	private String descriptionHtml;
	
	// Non-searchable sub items, eg items that are part of the same package and
	// not sold separately, eg ski poles that belong to skies or snowboard bindings not sold separately
	// TODO better way to model this?
	private List<Item> subItems;

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

	public String getDescriptionHtml() {
		return descriptionHtml;
	}

	public void setDescriptionHtml(String descriptionHtml) {
		this.descriptionHtml = descriptionHtml;
	}
	
}
