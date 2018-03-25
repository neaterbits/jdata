/**
 * Base gallery modes
 */


function GalleryModeBase() {
	
}



function GalleryModeWidthBase() {
	GalleryModeBase.call();
}

GalleryModeWidthBase.prototype = Object.create(GalleryModeBase.prototype);

GalleryModeWidthBase.prototype._computeNumberOfColumns = function(itemWidth, columnSpacing, displayAreaWidth) {

	if (typeof itemWidth === 'undefined') {
		throw "itemWidth is undefined";
	}

	if (typeof columnSpacing === 'undefined') {
		throw "columnSpacing is undefined";
	}
	
	if (typeof displayAreaWidth === 'undefined') {
		throw "Display area width is undefined";
	}
	
	var found = -1;
	
	if (itemWidth <= 0) {
		throw "itemWidth <= 0 : " + itemWidth;
	}

	if (columnSpacing < 0) {
		throw "columnSpacing < 0";
	}

	for (var numColumns = 1;; ++ numColumns) {
		if (itemWidth * numColumns + columnSpacing * (numColumns + 1) > displayAreaWidth) {
			if (numColumns == 1) {
				throw "Not enough space for one column";
			}
			
			found = numColumns - 1;
			break;
		}
	}
	
	return found;
}


GalleryModeWidthBase.prototype._computeHeight = function(itemHeight, rowSpacing, numColumns, totalNumberOfItems) {
	
	var numRows = ((totalNumberOfItems - 1) / numColumns) + 1;
	
	return numRows * itemHeight + rowSpacing * (numRows + 1);
}

