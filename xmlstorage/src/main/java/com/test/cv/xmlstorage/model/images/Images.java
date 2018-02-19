package com.test.cv.xmlstorage.model.images;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Root element for XML file specifying order of images
 */
@XmlRootElement
public class Images {

	// Ordered list that decides the order returned
	private List<Image> images;

	@XmlElement
	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}
}
