package com.test.salesportal.model.operations;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Base class for journal operations, stored for transactional support mainly across S3 and Elasticsearch
 */

@Entity
public class Operation {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	/**
	 * Time this was created, for sorting oldest first and for debug purposes.
	 */
	@Column(nullable=false)
	private Date createdTime;

	@Column(nullable=false)
	private boolean completed;
	
	@Column(nullable=false)
	private byte [] data;

	/**
	 * User for which this was performed in context of, or null if none.
	 */
	@Column
	private String userId;

	/**
	 * Set to a value when some cluster node tries to reapply operation.
	 * Allows for
	 *  - checking that no other node is reapplying
	 *  - timing out the reapply operation, eg. after 5 minutes and reapplyTime still set,
	 *    one should safely be able to reapply, even if says is held by other node.
	 *  
	 */
	
	@Column
	private Date storeTime;

	/**
	 * Id set when aquiring an operation for reapplication
	 * so that upon completion will complete only operations
	 * aquired by oneself.
	 */
	
	@Column
	private String aquireId;
	
	public Operation() {

	}

	public Operation(Date createdTime, byte [] data, String userId) {

		if (createdTime == null) {
			throw new IllegalArgumentException("createdTime == null");
		}
		
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}
		
		this.createdTime = createdTime;
		this.data = data;
		this.userId = userId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public final long getModelVersion() {
		return id;
	}
	
	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getStoreTime() {
		return storeTime;
	}

	public void setStoreTime(Date reapplyTime) {
		this.storeTime = reapplyTime;
	}

	public String getAquireId() {
		return aquireId;
	}

	public void setAquireId(String aquireId) {
		this.aquireId = aquireId;
	}
}
