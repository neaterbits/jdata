package com.test.cv.xmlstorage.model.images;

import javax.xml.bind.annotation.XmlElement;

public class Image {

	private String id;
	private ImageData thumb;
	
	// Either has image data for photo, or just an URL reference to photo
	private ImageData photo;
	private String photoUrl;

	@XmlElement
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlElement
	public ImageData getThumb() {
		return thumb;
	}

	public void setThumb(ImageData thumb) {
		this.thumb = thumb;
	}

	@XmlElement
	public ImageData getPhoto() {
		return photo;
	}

	public void setPhoto(ImageData photo) {
		this.photo = photo;
	}

	@XmlElement
	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}
}
