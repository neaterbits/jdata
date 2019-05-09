package com.test.salesportal.model.items;

import java.math.BigDecimal;

import javax.persistence.Column;

import com.test.salesportal.model.annotations.Facet;
import com.test.salesportal.model.annotations.Sortable;

public abstract class PurchasableItem extends BaseItem {

	@Column
	@Sortable
	private BigDecimal price;
	
	@Column
	@Facet("Currency")
	private String currency;

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
