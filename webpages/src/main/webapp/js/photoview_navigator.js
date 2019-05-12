/**
 * 
 */

function PhotoViewWithNavigator(
		itemId,
		photoCount,
		outerDiv, image,
		containerWidth, containerHeight,
		getPhotoUrl) {
	
	var t = this;
	
	this.outerDiv = outerDiv;
	this.image = image;
	this.getPhotoUrl = getPhotoUrl;
	this.containerWidth = containerWidth;
	this.containerHeight = containerHeight;
	
	var navigator = new NavigatorOverlay(
			photoCount,
			outerDiv,
			containerWidth,
			containerHeight,
			function() { return 65; },
			function() { return 150; },
			function(toShow, callback) {
				t.displayPhoto(toShow)
				
				callback();
			});
	
	this.displayPhoto = function(index) {
		
		// Set image URL to download image

		this.image.onload = function() {
			adjustImageWidthAndHeight(
					image,
					image.naturalWidth, image.naturalHeight,
					containerWidth, containerHeight);
		}
		
		this.image.src = this.getPhotoUrl(itemId, index);
	}
}