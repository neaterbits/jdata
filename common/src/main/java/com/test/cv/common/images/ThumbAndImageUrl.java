package com.test.cv.common.images;

public class ThumbAndImageUrl {

	private String thumbUrl;
	private Integer thumbWidth;
	private Integer thumbHeight;
	private String imageUrl;
	
	public ThumbAndImageUrl() {

	}

	public ThumbAndImageUrl(String thumbUrl, Integer thumbWidth, Integer thumbHeight, String imageUrl) {
		this.thumbUrl = thumbUrl;
		this.thumbWidth = thumbWidth;
		this.thumbHeight = thumbHeight;
		this.imageUrl = imageUrl;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public Integer getThumbWidth() {
		return thumbWidth;
	}

	public void setThumbWidth(Integer thumbWidth) {
		this.thumbWidth = thumbWidth;
	}

	public Integer getThumbHeight() {
		return thumbHeight;
	}

	public void setThumbHeight(Integer thumbHeight) {
		this.thumbHeight = thumbHeight;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
