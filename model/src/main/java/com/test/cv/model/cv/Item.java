package com.test.cv.model.cv;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.test.cv.model.text.Text;

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
	private List<Text> exitReason;

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

	public List<Text> getExitReason() {
		return exitReason;
	}

	public void setExitReason(List<Text> exitReason) {
		this.exitReason = exitReason;
	}
}
