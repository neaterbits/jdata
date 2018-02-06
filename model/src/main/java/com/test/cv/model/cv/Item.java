package com.test.cv.model.cv;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.test.cv.model.text.Texts;

@Entity
public abstract class Item {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@Column
	private Date startTime;
	
	@Column
	private Date endTime;
	
	@Column
	private Texts exitReason;

	public abstract <T, R> R visit(ItemVisitor<T, R> visitor, T param); 
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

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

	public Texts getExitReason() {
		return exitReason;
	}

	public void setExitReason(Texts exitReason) {
		this.exitReason = exitReason;
	}
}
