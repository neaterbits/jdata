package com.test.salesportal.notifications.aws;

import com.test.salesportal.notifications.aws.AWSSMSSender;

import junit.framework.TestCase;

public class AWSSMSSenderTest extends TestCase {

	public void testSendSMS() {
		final AWSSMSSender sender = new AWSSMSSender();
		
		sender.sendSMS("Jaja","+59167130400", "Testmelding");
	}
}
