package com.test.salesportal.model.items.operations.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.operations.Operation;

public class OperationDataMarshaller {

	private final JAXBContext jaxbContext;
	
	public OperationDataMarshaller(Class<? extends Item> [] itemTypeClasses) {
		
		final Class<?> [] operationDataClasses = new Class<?> [] {
			StoreItemOperationData.class,
			UpdateItemOperationData.class,
			DeleteItemOperationData.class
		};
	
		final Class<?> [] jaxbClasses = Arrays.copyOf(itemTypeClasses, itemTypeClasses.length + operationDataClasses.length);
		
		System.arraycopy(operationDataClasses, 0, jaxbClasses, itemTypeClasses.length, operationDataClasses.length);
		
		try {
			jaxbContext = JAXBContext.newInstance(jaxbClasses);
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to initialize JAXB context", ex);
		}
	}

	public byte [] encodeOperationData(BaseOperationData data) {
		
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}
		
		Marshaller marshaller;
		try {
			marshaller = jaxbContext.createMarshaller();
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to create marshaller", ex);
		}
		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
		
		try {
			marshaller.marshal(data, baos);
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to marshal", ex);
		}
		
		return baos.toByteArray();
	}
	
	public Unmarshaller createUnmarshaller() {
		
		final Unmarshaller unmarshaller;
		try {
			unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to create unmarshaller", ex);
		}
		
		return unmarshaller;
	}
	
	public BaseOperationData decodeOperationData(Unmarshaller unmarshaller, Operation operation) {
		
		try {
			return (BaseOperationData)unmarshaller.unmarshal(new ByteArrayInputStream(operation.getData()));
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to unmarshal input data", ex);
		}
	}
	
}
