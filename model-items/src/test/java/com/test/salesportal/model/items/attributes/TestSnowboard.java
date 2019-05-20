package com.test.salesportal.model.items.attributes;

import java.math.BigDecimal;

import javax.persistence.Column;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.annotations.DecimalRange;
import com.test.salesportal.model.items.annotations.DisplayAttribute;
import com.test.salesportal.model.items.annotations.Facet;
import com.test.salesportal.model.items.annotations.FacetAttribute;
import com.test.salesportal.model.items.annotations.FacetAttributes;
import com.test.salesportal.model.items.annotations.Freetext;
import com.test.salesportal.model.items.annotations.ServiceAttribute;
import com.test.salesportal.model.items.annotations.Sortable;

@FacetAttributes({
	@FacetAttribute(name="width", displayName = "Width", decimalRanges={
			@DecimalRange(upper=27.0),
			@DecimalRange(lower=27.0,upper=29.0),
			@DecimalRange(lower=29.0,upper=31.0),
			@DecimalRange(lower=31.0,upper=33.0),
			@DecimalRange(lower=33.0)
	}),
})
public class TestSnowboard extends Item {

	@Freetext
	@ServiceAttribute
	@Column
	private String descriptionHtml;

	@Sortable(priority=4)
	@Facet
	@DisplayAttribute("Make")
	@Column
	private String make;
	
	private BigDecimal width;

	public String getDescriptionHtml() {
		return descriptionHtml;
	}

	public void setDescriptionHtml(String descriptionHtml) {
		this.descriptionHtml = descriptionHtml;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public BigDecimal getWidth() {
		return width;
	}
	
	public void setWidth(BigDecimal width) {
		this.width = width;
	}
}
