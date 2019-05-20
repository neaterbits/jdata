package com.test.salesportal.model.items.sales;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class PhysicalItem extends RetailItem {

	// measurements in centimeters
	@Column(precision=15, scale=3)
	private BigDecimal width;
	
	@Column(precision=15, scale=3)
	private BigDecimal height;
	
	@Column(precision=15, scale=3)
	private BigDecimal length;
	
	public BigDecimal getWidth() {
		return width;
	}
	
	public void setWidth(BigDecimal width) {
		this.width = width;
	}
	
	public BigDecimal getHeight() {
		return height;
	}
	
	public void setHeight(BigDecimal height) {
		this.height = height;
	}
	
	public BigDecimal getLength() {
		return length;
	}

	public void setLength(BigDecimal length) {
		this.length = length;
	}
}
