package com.test.salesportal.index.elasticsearch.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticsearch.AWSElasticsearch;
import com.amazonaws.services.elasticsearch.AWSElasticsearchClient;
import com.amazonaws.services.elasticsearch.AWSElasticsearchClientBuilder;
import com.amazonaws.services.elasticsearch.model.DescribeElasticsearchDomainRequest;
import com.amazonaws.services.elasticsearch.model.DescribeElasticsearchDomainResult;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.index.ItemIndexException;
import com.test.salesportal.index.elasticsearch.ElasticSearchIndex;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.base.ItemTypes;

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

	public AWSElasticseachIndex(AWSCredentialsProvider credentialsProvider, Regions region, String esDomain, ItemTypes itemTypes, ItemAttribute titleAttribute) throws ItemIndexException {
		super(getEndpointURL(credentialsProvider, region, esDomain), itemTypes, titleAttribute);
	}
}
