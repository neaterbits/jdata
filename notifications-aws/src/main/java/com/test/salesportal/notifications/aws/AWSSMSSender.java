package com.test.salesportal.notifications.aws;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.test.salesportal.notifications.SMSSender;

public class AWSSMSSender implements SMSSender {

	@Override
	public void sendSMS(String senderId, String phoneNo, String message) {

		final AWSCredentialsProvider provider = new DefaultAWSCredentialsProviderChain();
		
		final AmazonSNSClientBuilder builder = AmazonSNSClient.builder();
		
		builder.setCredentials(provider);
		builder.setRegion(Regions.EU_WEST_1.getName())
		
		// currently not supported in London region
		// builder.setRegion(Regions.EU_WEST_2.getName())
		
		;

		final AmazonSNS sns = builder.build();

		final Map<String, MessageAttributeValue> map = new HashMap<>();
		
		map.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
				.withStringValue("Promotional")
				.withDataType("String"));
		
		if (senderId != null) {
			map.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
				.withStringValue(senderId)
				.withDataType("String"));
		}	
		sns.publish(new PublishRequest()
				.withMessage(message)
				.withPhoneNumber(phoneNo)
				.withMessageAttributes(map));
	}

}
