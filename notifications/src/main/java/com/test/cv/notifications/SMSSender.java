package com.test.cv.notifications;

public interface SMSSender {

	void sendSMS(String senderId, String phoneNo, String message);
	
}
