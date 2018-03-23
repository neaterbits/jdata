/**
 * We have an item height-hint, use that to compute height in pixels
 */

function GalleryModeHeightHint() {
	
}

GalleryModeHeightHint.prototype = Object.create(GalleryModeWidthBase.prototype);


GalleryModeHeightHint.prototype.computeHeight = function(config, rowSpacing, numColumns, totalNumberOfItems) {

	if (typeof config.heightHint === 'undefined') {
		throw "config.heightHint is undefined";
	}

	// We can compute from display area width, same code as for specific width
	return this._computeHeight(config.heightHint, rowSpacing, numColumns, totalNumberOfItems);
}
