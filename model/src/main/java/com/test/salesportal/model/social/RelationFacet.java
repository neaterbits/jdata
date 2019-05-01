package com.test.salesportal.model.social;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * One facet of a relation between a user and someone else
 */
public abstract class RelationFacet {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	// Optional start date and end date
	@Column
	private Date startDate;
	@Column
	private Date endDate;

	@OneToOne(cascade={CascadeType.ALL})
	private Reccomendation reccomendation;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Reccomendation getReccomendation() {
		return reccomendation;
	}

	public void setReccomendation(Reccomendation reccomendation) {
		this.reccomendation = reccomendation;
	}
}
