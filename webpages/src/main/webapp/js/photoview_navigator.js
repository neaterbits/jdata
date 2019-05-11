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
		
		console.log('## displayPhoto ' + index);

		// Set image URL to download image
		this.image.src = this.getPhotoUrl(itemId, index);
		this.image.style.width = containerWidth;
		this.image.style.height = containerHeight;
		
		adjustImageWidthAndHeight(
				image,
				image.naturalWidth, image.naturalHeight,
				containerWidth, containerHeight);
	}
}