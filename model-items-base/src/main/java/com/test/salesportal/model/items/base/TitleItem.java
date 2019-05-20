package com.test.salesportal.model.items.base;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.annotations.Freetext;
import com.test.salesportal.model.items.annotations.IndexItemAttribute;
import com.test.salesportal.model.items.annotations.ServiceAttribute;
import com.test.salesportal.model.items.annotations.Sortable;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class TitleItem extends Item {

	@Sortable(value="Title", priority=5)
	@Freetext
	@IndexItemAttribute(storeValue=true) // must store for quick-lookup in search results
	@ServiceAttribute
	@Column(nullable=false)
	private String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
