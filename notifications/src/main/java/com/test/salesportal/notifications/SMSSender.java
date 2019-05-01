package com.test.salesportal.notifications;

public interface SMSSender {

	void sendSMS(String senderId, String phoneNo, String message);
	
}
