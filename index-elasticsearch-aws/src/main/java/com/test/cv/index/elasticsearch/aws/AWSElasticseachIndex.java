package com.test.cv.index.elasticsearch.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticsearch.AWSElasticsearch;
import com.amazonaws.services.elasticsearch.AWSElasticsearchClient;
import com.amazonaws.services.elasticsearch.AWSElasticsearchClientBuilder;
import com.amazonaws.services.elasticsearch.model.DescribeElasticsearchDomainRequest;
import com.amazonaws.services.elasticsearch.model.DescribeElasticsearchDomainResult;
import com.test.cv.index.ItemIndex;
import com.test.cv.index.elasticsearch.ElasticSearchIndex;

public class AWSElasticseachIndex extends ElasticSearchIndex implements ItemIndex {
	
	private static String getEndpointURL(AWSCredentialsProvider credentialsProvider, Regions region, String esDomain) {
		final AWSElasticsearchClientBuilder builder = AWSElasticsearchClient.builder();
		
		builder.setRegion(region.getName());
		builder.setCredentials(credentialsProvider);
		
		final AWSElasticsearch client = builder.build();
		
		final DescribeElasticsearchDomainResult domainResult = client.describeElasticsearchDomain(new DescribeElasticsearchDomainRequest()
				.withDomainName(esDomain));
		
		final String endpoint = domainResult.getDomainStatus().getEndpoint();
		
		System.out.println("## Got ES endpoint: " + endpoint);

		return endpoint;
	}

	public AWSElasticseachIndex(AWSCredentialsProvider credentialsProvider, Regions region, String esDomain) {
		super(getEndpointURL(credentialsProvider, region, esDomain));
	}
}
