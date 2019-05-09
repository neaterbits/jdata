/**
 * Simple gallery item factory with static size elements
 */

function SimpleStaticSizeGalleryItemFactory() {

	const ITEM_WIDTH = 300;
	const ITEM_HEIGHT = 300;
	
	const TEXT_HEIGHT = 60;
	
	const IMAGE_WIDTH = ITEM_WIDTH;
	const IMAGE_HEIGHT = ITEM_HEIGHT - TEXT_HEIGHT;
	
	this.getGalleryConfig = function() {
		return {
			columnSpacing : 20,
			rowSpacing : 20,
			width : ITEM_WIDTH,
			height : ITEM_HEIGHT
		};
	}
	
	this.getItemFields = function () {
		return [ 'publicationDate', 'price' ];
	}

	this.makeProvisionalItem = function(index, data) {
		var div = document.createElement('div');
		
		/*
		div.style.width = ITEM_WIDTH;
		div.style.height = ITEM_HEIGHT;
		div.style.display = 'inline-block';
		*/

		var provisionalImage = document.createElement('div');
		
		provisionalImage.setAttribute('class', 'provisionalImage');

		provisionalImage.style.width = data.thumbWidth;
		provisionalImage.style.height = data.thumbHeight;
		provisionalImage.style.margin = 'auto';
		provisionalImage.style.display = 'block';
		
		div.append(provisionalImage);
		
		_appendText(div, index, data);
		
		return div;
	}
	
	this.makeImageItem = function(index, provisionalData, imageData) {
		var div = document.createElement('div');
		
		div.style.width = ITEM_WIDTH;
		div.style.height = ITEM_HEIGHT;
		
		var image = document.createElement('img');

		image.setAttribute('class', 'thumbnailImage');

		var size = _computeWidthAndHeight(
				provisionalData.thumbWidth,
				provisionalData.thumbHeight,
				IMAGE_WIDTH,
				IMAGE_HEIGHT);
		
		image.width = size.width;
		image.height = size.height;

		image.style.margin = 'auto';
		image.style.display = 'block';

		div.style['overflow'] = 'hidden';

		if (size.crop > 0) {
			
			var cropAttributeValue = '-' + (size.crop / 2) + 'px';
			
			image.style['margin-left'] = cropAttributeValue;
			image.style['margin-right'] = cropAttributeValue;
		}

		/*
		provisionalImage.style.width = thumbWidth;
		provisionalImage.style.height = thumbHeight;
		 */
		
		/*
		var url = URL.createObjectURL(imageData);
		image.src = url;

		image.onload = function() {
			URL.revokeObjectURL(url);
		}
		 */
		
		image.src = imageData;
		
		div.append(image);
		
		_appendText(div, index, provisionalData);
		
		return div;
	}
	
	function _appendText(div, index, provisionalData) {
		
		var textDiv = document.createElement('div');
		
		// textDiv.style.width = 
		
		// Add index as a text to the element
		var textSpan = document.createElement('span');
		
		var titleSpan = document.createElement('span');
		
		titleSpan.innerHTML = _makeTitle(index, provisionalData.title);
		
		textSpan.append(titleSpan);

		textDiv.style.width = ITEM_WIDTH;
		textDiv.style['text-align'] = 'center';

		textDiv.append(textSpan);
		
		div.append(textDiv);
	}
	
	/**
	 * Scale image to fill available space
	 *  - always scale to bbHeight so that text appears the same spot always
	 *  - allow for some scaling aspect ratio change
	 */
	function _computeWidthAndHeight(width, height, bbWidth, bbHeight) {

		var thumbWidth;
		var thumbHeight;
		
		thumbHeight = bbHeight;
		
		var cropped;

		if (height > bbHeight) {
			// Scale down to height
		
			var scaleDown = bbHeight / height;
			
			cropped = _computeCrop(Math.floor(width * scaleDown), bbWidth);
		}
		else if (height == bbHeight) {
			cropped = _computeCrop(width, bbWidth);
		}
		else if (height < bbHeight) {
			var scaleUp = bbHeight / height;
			
			cropped = _computeCrop(Math.floor(width * scaleUp), bbWidth);
		}
		else {
			throw "Unreachable code";
		}

		return { 'width' : cropped.width, 'height' : thumbHeight, 'crop' : cropped.crop };
	}

	function _computeCrop(thumbWidth, bbWidth) {

		var crop;
		
		var adjustedWidth = thumbWidth;
		
		if (thumbWidth > bbWidth) {
			crop = thumbWidth - bbWidth;
		}
		else {
			crop = 0;
		}
		
		return { 'width' : adjustedWidth, 'crop' : crop };
	}
	

	/*
	function _computeWidthAndHeight(width, height, bbWidth, bbHeight) {

		var thumbWidth;
		var thumbHeight;
		
		if (width <= bbWidth && height <= bbHeight) {
			thumbWidth = width;
			thumbHeight = height;
		}
		else {
			if (width >= height) {
				// landscape
				var scaleDown = bbWidth / width;
				
				thumbWidth = bbWidth;
				thumbHeight = Math.floor(height * scaleDown);
			}
			else {
				var scaleDown = bbHeight / height;
				thumbWidth = Math.floor(width * scaleDown);
				thumbHeight = bbHeight;
			}
		}
		
		return { 'width' : thumbWidth, 'height' : thumbHeight };
	}
	*/
}
