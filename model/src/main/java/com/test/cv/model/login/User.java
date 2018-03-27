package com.test.cv.model.login;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="LOGIN_USER")
public class User {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	@Version
	private long version;
	
	@Column(unique=true, nullable=false)
	private String phoneNo;
	
	@Column(nullable=false)
	private LoginStatus status;
	
	// Last code being sent to user via SMS
	@Column
	private String code;
	
	@Column
	private Date codeGeneratedTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
	
	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public LoginStatus getStatus() {
		return status;
	}

	public void setStatus(LoginStatus status) {
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Date getCodeGeneratedTime() {
		return codeGeneratedTime;
	}

	public void setCodeGeneratedTime(Date codeGeneratedTime) {
		this.codeGeneratedTime = codeGeneratedTime;
	}
	
	
}
