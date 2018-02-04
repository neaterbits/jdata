package com.test.cv.model.social;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.test.cv.model.user.User;

public class UserToUserRelation extends Relation {

	// user we have a relation to
	@OneToOne(optional=false)
	private User user;

	@OneToMany(cascade={CascadeType.ALL})
	private List<RelationFacet> facets;
	
	@OneToOne(cascade={CascadeType.ALL})
	private Reccomendation reccomendation;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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