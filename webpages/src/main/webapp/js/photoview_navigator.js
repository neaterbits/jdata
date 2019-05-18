/**
 * 
 */

function PhotoViewWithNavigator(
		itemId,
		photoCount,
		outerDiv, // image,
		containerWidth, containerHeight,
		getPhotoUrl) {
	
	var t = this;
	
	this.outerDiv = outerDiv;
//	this.image = image;
	this.getPhotoUrl = getPhotoUrl;
	this.containerWidth = containerWidth;
	this.containerHeight = containerHeight;
	
	var navigator = new NavigatorOverlay(
			photoCount,
			outerDiv,
			true,
			containerWidth,
			containerHeight,
			function() { return 65; },
			function() { return 150; },
			function(toShow, callback) {
				t.displayPhoto(toShow, function() {
					
					callback();
				})
				
			});

	this.fader = new ElementFader(outerDiv, 'img', function () { return new Image(); });
	
	// May adjust image to negative margin for cropping
	outerDiv.style['overflow'] = 'hidden';

	this.displayPhoto = function(index, onComplete) {
		
		// Set image URL to download image
		var url = this.getPhotoUrl(itemId, index);

		var t = this;

		// Image we are cross fading to
		var fadeInImg = this.fader.getFadeInElement();
		
		fadeInImg.src = url;
		
		fadeInImg.onload = function() {

			var size = adjustImageWidthAndHeight(
					fadeInImg,
					fadeInImg.naturalWidth, fadeInImg.naturalHeight,
					containerWidth, containerHeight);
			
			if (size.width < containerWidth) {
				fadeInImg.style.left = (containerWidth - size.width) / 2;
			}

			t.fader.crossFade();
			
			onComplete();
		}
		
		// return { 'displayed' : displayed, 'hidden' : hidden };
	}
}
