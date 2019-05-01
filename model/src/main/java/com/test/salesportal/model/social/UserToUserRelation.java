package com.test.salesportal.model.social;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.test.salesportal.model.user.User;

public class UserToUserRelation extends Relation {

	// user we have a relation to
	@OneToOne(optional=false)
	private User firstUser;

	@OneToOne(optional=false)
	private User secondUser;

	@OneToMany(cascade={CascadeType.ALL})
	private List<RelationFacet> facets;
	
	@OneToOne(cascade={CascadeType.ALL})
	private Reccomendation reccomendation;

	public User getFirstUser() {
		return firstUser;
	}

	public void setFirstUser(User user) {
		this.firstUser = user;
	}

	public User getSecondUser() {
		return secondUser;
	}

	public void setSecondUser(User user) {
		this.secondUser = user;
	}

	public List<RelationFacet> getFacets() {
		return facets;
	}

	public void setFacets(List<RelationFacet> facets) {
		this.facets = facets;
	}

	public Reccomendation getReccomendation() {
		return reccomendation;
	}

	public void setReccomendation(Reccomendation reccomendation) {
		this.reccomendation = reccomendation;
	}
}