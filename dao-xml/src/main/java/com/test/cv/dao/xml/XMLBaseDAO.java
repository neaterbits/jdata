package com.test.cv.dao.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.test.cv.xmlstorage.api.IItemStorage;

abstract class XMLBaseDAO {
	final IItemStorage xmlStorage;
	final Marshaller marshaller;
	final Unmarshaller unmarshaller;
	
	XMLBaseDAO(JAXBContext jaxbContext, IItemStorage xmlStorage) {
		if (xmlStorage == null) {
			throw new IllegalArgumentException("xmlStorage == null");
		}
		
		this.xmlStorage = xmlStorage;

		try {
			this.marshaller = jaxbContext.createMarshaller();
			this.unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to create JAXB context", ex);
		}
	}
}
