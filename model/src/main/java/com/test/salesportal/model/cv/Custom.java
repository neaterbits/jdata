package com.test.salesportal.model.cv;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Custom extends DescribedItem {

	@Column
	private Name name;
	
	@Override
	public <T, R> R visit(ItemVisitor<T, R> visitor, T param) {
		return visitor.onCustom(this, param);
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}
}
