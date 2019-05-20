package com.test.salesportal.model.items.sales;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.test.salesportal.model.items.FacetFiltering;
import com.test.salesportal.model.items.annotations.DisplayAttribute;
import com.test.salesportal.model.items.annotations.Facet;
import com.test.salesportal.model.items.annotations.NumericAttributeFiltering;
import com.test.salesportal.model.items.annotations.Sortable;
import com.test.salesportal.model.items.base.BaseItem;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class PurchasableItem extends BaseItem {

	@Column
	@Sortable
	private BigDecimal price;
	
	@Column
	@Facet
	@DisplayAttribute("Currency")
	private String currency;

	@NumericAttributeFiltering(FacetFiltering.INPUT)
	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
}
