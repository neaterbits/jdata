package com.test.cv.model.cv;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Custom extends DescribedItem {

	@Column
	private List<Text> name;
	
	@Override
	public <T, R> R visit(ItemVisitor<T, R> visitor, T param) {
		return visitor.onCustom(this, param);
	}

	public List<Text> getName() {
		return name;
	}

	public void setName(List<Text> name) {
		this.name = name;
	}
}
