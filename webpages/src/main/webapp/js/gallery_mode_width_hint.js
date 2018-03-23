/**
 * We have a width-hint, use that to compute number of columns
 */

function GalleryModeWidthHint() {
}

GalleryModeWidthHint.prototype = Object.create(GalleryModeWidthBase.prototype);


GalleryModeWidthHint.prototype.computeNumColumns = function(config, columnSpacing, displayAreaWidth) {

	if (typeof config.widthHint === 'undefined') {
		throw "config.widthHint is undefined";
	}

	// We can compute from display area width, same code as for specific width
	return this._computeNumberOfColumns(config.widthHint, columnSpacing, displayAreaWidth);
}
