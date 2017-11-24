package com.test.cv.xmlstorage.api;

import java.io.InputStream;

public interface IXMLStorage {

	InputStream getCVXMLForUser(String userId) throws StorageException;

}
