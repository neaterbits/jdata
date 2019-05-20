package com.test.salesportal.model.items.vehicular;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

import com.test.salesportal.model.items.annotations.DecimalRange;
import com.test.salesportal.model.items.annotations.DisplayAttribute;
import com.test.salesportal.model.items.annotations.Facet;
import com.test.salesportal.model.items.annotations.FacetEntity;
import com.test.salesportal.model.items.annotations.IntegerRange;

@Entity(name="boat")
@FacetEntity(value = "Boats", propertyOrder = {
		"propulsionType",
		"lengthOverall",
		"condition",
		"engineHours"
})
@XmlRootElement
public class Boat extends Vehicle {

	@Column
	@Facet(
		decimalRanges={
			@DecimalRange(upper=10.0),
			@DecimalRange(lower=10.0, upper=20.0),
			@DecimalRange(lower=20.0, upper=30.0),
			@DecimalRange(lower=30.0, upper=40.0),
			@DecimalRange(lower=40.0, upper=50.0),
			@DecimalRange(lower=50.0)
	})
	@DisplayAttribute("Overall length")
	private BigDecimal lengthOverall;

	@Column
	@Facet
	@DisplayAttribute("Propulsion type")
	private BoatPropulsionType propulsionType;

	@Column
	@Facet(
		integerRanges={
			@IntegerRange(upper=500),
			@IntegerRange(lower=500, upper=1000),
			@IntegerRange(lower=1000, upper=5000),
			@IntegerRange(lower=5000, upper=10000),
			@IntegerRange(lower=10000)
	})
	@DisplayAttribute("Engine hours")
	private Integer engineHours;
	
	public BigDecimal getLengthOverall() {
		return lengthOverall;
	}

	public void setLengthOverall(BigDecimal lengthOverall) {
		this.lengthOverall = lengthOverall;
	}

	public BoatPropulsionType getPropulsionType() {
		return propulsionType;
	}

	public void setPropulsionType(BoatPropulsionType propulsionType) {
		this.propulsionType = propulsionType;
	}

	public Integer getEngineHours() {
		return engineHours;
	}

	public void setEngineHours(Integer engineHours) {
		this.engineHours = engineHours;
	}
}
