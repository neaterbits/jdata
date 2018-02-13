package com.test.cv.dao.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.test.cv.dao.CVStorageException;
import com.test.cv.dao.ICVDAO;
import com.test.cv.model.cv.CV;
import com.test.cv.model.cv.Language;
import com.test.cv.xml.CVType;
import com.test.cv.xmlstorage.api.IItemStorage;
import com.test.cv.xmlstorage.api.StorageException;

public class XMLCVDAO extends XMLBaseDAO implements ICVDAO {

	private static final JAXBContext jaxbContext;

	static {
		try {
			jaxbContext = JAXBContext.newInstance(CVType.class);
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to initialize JAXB context", ex);
		}
	}

	public XMLCVDAO(IItemStorage xmlStorage) {
		super(jaxbContext, xmlStorage);
	}
	
	private static final String CV_ID = "cv";
	
	private <T> CV queryCV(String userId, T param, BiFunction<CVType, T, CV> convert) throws CVStorageException {
		final CV ret;

		try {
			final InputStream inputStream = xmlStorage.getXMLForItem(userId, CV_ID);

			if (inputStream == null) {
				ret = null;
			}
			else {
				try {
					final CVType xmlCV = (CVType)unmarshaller.unmarshal(inputStream);
					
					ret = convert.apply(xmlCV, param);
				} catch (JAXBException ex) {
					throw new IllegalStateException("Failed to unmarshall", ex);
				}
				finally {
					try {
						inputStream.close();
					} catch (IOException ex) {
						throw new IllegalStateException("Failed to close input stream", ex);
					}
				}
			}
		}
		catch (StorageException ex) {
			throw new CVStorageException("Failed to retrieve CV", ex); 
		}
		
		return ret;
	}

	@Override
	public CV findCV(String userId, Language... languages) throws CVStorageException {

		return queryCV(userId, languages, (xmlCV, l) -> ConvertXMLToModel.convertToModel(xmlCV, l));

	}
	

	@Override
	public CV findCVForEdit(String userId) throws CVStorageException {

		return queryCV(userId, null, (xmlCV, l) -> ConvertXMLToModel.convertToModel(xmlCV, null));

	}
	
	private void storeCV(String userId, CV cv, Function<CV, CVType> convertCV) throws CVStorageException {
		final CVType converted = convertCV.apply(cv);

		try {
			store(userId, CV_ID, converted);
		} catch (XMLStorageException ex) {
			throw new CVStorageException("Failed to store CV", ex);
		}
	}
		
	@Override
	public void createCV(String userId, CV cv) throws CVStorageException {
		
		storeCV(userId, cv, c -> ConvertModelToXML.convertToXML(c));
		
	}

	@Override
	public void updateCV(String userId, CV cv) throws CVStorageException {

		storeCV(userId, cv, c -> ConvertModelToXML.convertToXML(c));

	}

	@Override
	public void deleteCV(String userId) throws CVStorageException {

		try {
			xmlStorage.deleteAllItemFiles(userId, CV_ID);
		} catch (StorageException ex) {
			throw new CVStorageException("Caught exception while deleting XML", ex);
		}
	}

	@Override
	public void close() throws Exception {
		// Nothing to do
	}

}
