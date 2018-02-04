package com.test.cv.model.social;

import java.util.List;

import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.test.cv.model.user.User;

public class UserToUserRelation extends Relation {

	// user we have a relation to
	@OneToOne
	private User user;

	@OneToMany
	private List<RelationFacet> facets;

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
}