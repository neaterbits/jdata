package com.test.salesportal.model.lock;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

// Entity used for marking an item as locked, similar to having a lock file on the file system

@Entity
@Table(name="ITEM_LOCK")
public class ItemLock {

	@Id
	@Column(name="ITEM_ID", nullable=false)
	private String itemId;

	@Column(name="LOCKED", nullable=false)
	private boolean locked;

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
