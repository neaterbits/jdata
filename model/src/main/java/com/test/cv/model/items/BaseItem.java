package com.test.cv.model.items;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.test.cv.model.Item;
import com.test.cv.model.annotations.Facet;


@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class BaseItem extends Item {

	@Facet
	@Column
	private String make;

	@Facet
	@Column
	private String model;
	
	@Facet
	@Column
	private Integer productionYear;

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

	public void setProductionYear(Integer productionYear) {
		this.productionYear = productionYear;
	}
}
