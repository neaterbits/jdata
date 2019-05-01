package com.test.salesportal.dao;

import java.util.Date;

import com.test.salesportal.model.login.LoginStatus;

public class LoginCode {
	private final LoginStatus loginStatus;
	private final String code;
	private final Date timeGenerated;

	public LoginCode(LoginStatus loginStatus, String code, Date timeGenerated) {
		
		if (loginStatus == null) {
			throw new IllegalArgumentException("loginStatus == null");
		}

		if (code == null) {
			throw new IllegalArgumentException("code == null");
		}

		if (timeGenerated == null) {
			throw new IllegalArgumentException("timeGenerated == null");
		}

		this.loginStatus = loginStatus;
		this.code = code;
		this.timeGenerated = timeGenerated;
	}

	public LoginStatus getLoginStatus() {
		return loginStatus;
	}

	public String getCode() {
		return code;
	}

	public Date getTimeGenerated() {
		return timeGenerated;
	}
}
