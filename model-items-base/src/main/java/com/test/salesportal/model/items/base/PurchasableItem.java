package com.test.salesportal.model.items.base;

import java.math.BigDecimal;

import javax.persistence.Column;

import com.test.salesportal.model.items.FacetFiltering;
import com.test.salesportal.model.items.annotations.DisplayAttribute;
import com.test.salesportal.model.items.annotations.Facet;
import com.test.salesportal.model.items.annotations.NumericAttributeFiltering;
import com.test.salesportal.model.items.annotations.Sortable;

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
