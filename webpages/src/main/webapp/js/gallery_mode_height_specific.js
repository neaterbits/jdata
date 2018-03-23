/**
 * We have a specific item height, use that to compute height in pixels
 */

function GalleryModeHeightSpecific() {
	
}

GalleryModeHeightSpecific.prototype = Object.create(GalleryModeWidthBase.prototype);


GalleryModeHeightSpecific.prototype.computeHeight = function(config, rowSpacing, numColumns, totalNumberOfItems) {

	if (typeof config.height === 'undefined') {
		throw "config.height is undefined";
	}

	// We can compute from display area width, same code as for specific width
	return this._computeHeight(config.height, rowSpacing, numColumns, totalNumberOfItems);
}
