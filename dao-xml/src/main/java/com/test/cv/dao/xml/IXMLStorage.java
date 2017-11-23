package com.test.cv.dao.xml;

import java.io.InputStream;

public interface IXMLStorage {

	InputStream getCVXMLForUser(String userId);
	
}
