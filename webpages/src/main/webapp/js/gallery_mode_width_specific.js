/**
 * We have a width-hint, use that to compute number of columns
 * 
 */

function GalleryModeWidthSpecific() {
}

GalleryModeWidthSpecific.prototype = Object.create(GalleryModeWidthBase.prototype);


GalleryModeWidthSpecific.prototype.computeNumColumns = function(config, columnSpacing, displayAreaWidth) {

	if (typeof config.width === 'undefined') {
		throw "config.width is undefined";
	}

	// We can compute from display area width, same code as for specific width
	return this._computeNumberOfColumns(config.width, columnSpacing, displayAreaWidth);
}
