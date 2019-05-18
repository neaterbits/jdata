/**
 * 
 */

function PhotoViewWithNavigator(
		itemId,
		photoCount,
		outerDiv, // image,
		containerWidth, containerHeight,
		getPhotoUrl,
		changeSpinner) {
	
	var t = this;
	
	this.outerDiv = outerDiv;
//	this.image = image;
	this.getPhotoUrl = getPhotoUrl;
	this.containerWidth = containerWidth;
	this.containerHeight = containerHeight;
	this.changeSpinner = changeSpinner;
	
	this.navigator = new NavigatorOverlay(
			photoCount,
			outerDiv,
			true,
			containerWidth,
			containerHeight,
			function() { return 65; },
			function() { return 150; },
			function(toShow, callback) {
				t._displayPhoto(toShow, false, function() {
					
					callback();
				})
				
			});

	this.fader = new ElementFader(outerDiv, 'img', function () { return new Image(); });
	
	// May adjust image to negative margin for cropping
	outerDiv.style['overflow'] = 'hidden';

	this.displayPhoto = function(index, onComplete) {
		this._displayPhoto(index, true, onComplete);
	}
	
	var noImg = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
	
	this.clearPhoto = function() {
		this.fader.getFadeInElement().src = noImg;
		this.fader.getFadeOutElement().src = noImg;
	
		this.navigator.reset(0, 0);
	}
	
	this._displayPhoto = function(index, initial, onComplete) {
		
		// Set image URL to download image
		var url = this.getPhotoUrl(itemId, index);

		var t = this;

		// Image we are cross fading to
		var fadeInImg = this.fader.getFadeInElement();
		
		if (!initial && this.changeSpinner) {
			changeSpinner(true);
		}

		fadeInImg.src = url;
		
		fadeInImg.onload = function() {

			var size = adjustImageWidthAndHeight(
					fadeInImg,
					fadeInImg.naturalWidth, fadeInImg.naturalHeight,
					containerWidth, containerHeight);
			
			if (size.width < containerWidth) {
				fadeInImg.style.left = (containerWidth - size.width) / 2;
			}
			else {
				fadeInImg.style.left = 0;
			}

			t.fader.crossFade();

			if (t.changeSpinner) {
				changeSpinner(false);
			}
			
			fadeInImg.onload = null;
			
			onComplete();
		}
		
		// return { 'displayed' : displayed, 'hidden' : hidden };
	}
}
