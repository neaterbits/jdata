package com.test.cv.dao.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.test.cv.common.ItemId;
import com.test.cv.index.ItemIndex;
import com.test.cv.index.ItemIndexException;
import com.test.cv.model.Item;
import com.test.cv.model.ItemAttributeValue;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.xmlstorage.api.IItemStorage;
import com.test.cv.xmlstorage.api.StorageException;

abstract class XMLBaseDAO {
	final IItemStorage xmlStorage;
	final ItemIndex index;
	final Marshaller marshaller;
	final Unmarshaller unmarshaller;
	
	XMLBaseDAO(JAXBContext jaxbContext, IItemStorage xmlStorage, ItemIndex index) {
		if (xmlStorage == null) {
			throw new IllegalArgumentException("xmlStorage == null");
		}
		
		if (index == null) {
			throw new IllegalArgumentException("index == null");
		}
		
		this.xmlStorage = xmlStorage;
		this.index = index;

		try {
			this.marshaller = jaxbContext.createMarshaller();
			this.unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to create JAXB context", ex);
		}
	}
	
	final void store(String userId, String itemId, Object converted, Class<? extends Item> itemType, List<ItemAttributeValue<?>> attributeValues) throws XMLStorageException {
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
		
		// TODO index on an async-queue with retries
		try {
			index.indexItemAttributes(itemType, ItemTypes.getTypeName(itemType), attributeValues);
		} catch (ItemIndexException ex) {
			try {
				xmlStorage.deleteAllItemFiles(userId, itemId);
			} catch (StorageException ex2) {
				throw new XMLStorageException("Failed to delete stored XML after failed indexing", ex2);
			}
			throw new XMLStorageException("Failed to index", ex);
		}
	}
	
	protected static Map<String, Integer> makeItemIdToIndexMap(ItemId [] itemIds) {
		final Map<String, Integer> map = new HashMap<>(itemIds.length);
		for (int i = 0; i < itemIds.length; ++ i) {
			map.put(itemIds[i].getItemId(), i);
		}

		return map;
	}
	
	protected static class XMLStorageException extends Exception {
		private static final long serialVersionUID = 1L;

		public XMLStorageException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
