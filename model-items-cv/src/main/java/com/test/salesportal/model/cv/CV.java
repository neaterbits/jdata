package com.test.salesportal.model.cv;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.user.User;

@Entity
public class CV extends Item {

	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@OneToOne(optional=false)
	private User user;
	
	@OneToOne(optional=false)
	private Personalia personalia;
	
	@OneToMany(cascade={CascadeType.ALL})
	private List<CVItem> items;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Personalia getPersonalia() {
		return personalia;
	}

	public void setPersonalia(Personalia personalia) {
		this.personalia = personalia;
	}

	public List<CVItem> getItems() {
		return items;
	}

	public void setItems(List<CVItem> items) {
		this.items = items;
	}
}	
