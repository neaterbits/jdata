/**
 * 
 */

function removeAll(container) {
	while (container.firstChild) {
		container.removeChild(container.firstChild);
	}
}

function adjustImageWidthAndHeight(image, width, height, containerWidth, containerHeight) {
	var size = _computeWidthAndHeight(
			width,
			height,
			containerWidth,
			containerHeight);
	
	image.width = size.width;
	image.height = size.height;

	if (size.crop > 0) {
		
		var cropAttributeValue = '-' + (size.crop / 2) + 'px';
		
		image.style['margin-left'] = cropAttributeValue;
		image.style['margin-right'] = cropAttributeValue;
	}
	else {
		image.style['margin-left'] = 'auto';
		image.style['margin-right'] = 'auto';
	}
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
