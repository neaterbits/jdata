package com.test.salesportal.model.cv;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.text.Text;

@Entity
public abstract class CVItem extends Item {

	@Column
	private Date startTime;
	
	@Column
	private Date endTime;
	
	@Column
	private Text exitReason;

	public abstract <T, R> R visit(ItemVisitor<T, R> visitor, T param); 
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Text getExitReason() {
		return exitReason;
	}

	public void setExitReason(Text exitReason) {
		this.exitReason = exitReason;
	}
}
