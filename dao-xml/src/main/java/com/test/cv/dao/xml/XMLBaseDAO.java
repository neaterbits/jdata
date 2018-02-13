package com.test.cv.dao.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.test.cv.dao.CVStorageException;
import com.test.cv.xmlstorage.api.IItemStorage;
import com.test.cv.xmlstorage.api.StorageException;

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
	
	final void store(String userId, String itemId, Object converted) throws XMLStorageException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			marshaller.marshal(converted, baos);
		} catch (JAXBException ex) {
			throw new XMLStorageException("Failed to marshal XML", ex);
		}
		
		try {
			xmlStorage.storeXMLForItem(userId, itemId, new ByteArrayInputStream(baos.toByteArray()));
		} catch (StorageException ex) {
			throw new XMLStorageException("Failed to store to XML storage", ex);
		}
	}
	
	protected static class XMLStorageException extends Exception {
		private static final long serialVersionUID = 1L;

		public XMLStorageException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
