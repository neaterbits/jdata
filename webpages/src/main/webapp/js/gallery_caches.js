/**
 *  
 *  
 * Must cache in display a number of items before and after the view,
 * eg. 3 times the visible display area, for smooth scrolling.
 * If scrolling fast, one either way has to redraw the complete view, so for a large numbers of elements
 * there is no need to cache all images.
 * 
 * There are thus 3 levels of caching.
 *  - cache all elements, useful for a small number of elements since
 *    the scrollbars will then show at correct size.
 *    
 *  - cache all provisional data (eg. titles and image size) to show provisional data 
 *    for all sizes when scrolling (without calling REST services to do this),
 *    cache only a few pages of complete-elements above and below visible area.
 *    
 *  - for huge number items, cache same amount of complete-elements as above but only
 *    cache some of the provisional-data, download on-demand.
 *    
 *    
 * This is implemented as a cache base class and subclasses, responsibilities:
 *  - keep arrays of downloaded data, expose API for access
 *  - track start and end vertical offsets in virtual display area of complete-images
 *  - keep arrays of created DOM elements and heights of all rows so that
 *    can figure out what images to download
 *  - download data through callback
 *  - make sure cache downloads items as soon as it can
 *  
 * 
 */

function GalleryCacheBase(gallerySizes, galleryModel, galleryView, initialTotalNumberOfItems) {
	
	this.gallerySizes = gallerySizes;
	this.galleryModel = galleryModel;
	this.galleryView = galleryView;
	this.totalNumberOfItems = initialTotalNumberOfItems;

	// Some common data for all these views

	// The row divs (ie DOM div elements that each hold a row) that are currently
	// held (and added to the DOM)
	// For the cache-all-elements, this will hold rows of elements for all data in the virtual array
	this.cachedRowDivs = []
	
	
	// Index into virtual array of first item that is displayd, eg first item on first
	// row in this.cachedRowDivs is really showing the item at the below index
	// in the virtual array of data elements to display, eg:
	// - for cache-all-elements, this is always 0 as we create DOM elements for all data at once
	// - for partly caching, this index will point to virtual array data for the first cached element.
	//
	this.firstCachedIndex = 0;

	// Similarly, index into virtual array of last elements that is cached, eg.
	// the last element on the last row in cachedRowDivs displays the data referenced by this index
	// thus this index will point to virtual array data for the last cached elemnt
	//
	// thus we will always have that:
	// sum of elements under this.cachedRowDivs == this.lastCachedIndex - this.fiestCachedIndex + 1
	// 
	// for cache-all-elements, this will always [length of virtual array - 1],
	// while for partly views, this will be updated as user scrolls
	
	this.lastCachedIndex = 0;
	
	// First Y position (pixel line) in scrollable view that we currently have added DOM elements for,
	// might be before visible area
	this.firstY = 0;

	
	// Last Y position (pixel line) in scrollable view that we currently have added DOM elements for
	// might be after visible area
	this.lastY = 0;
}

GalleryCacheBase.prototype = Object.create(GalleryBase.prototype);


/**
 * Update dimensions of visible area, eg if window is resized.
 */

GalleryCacheBase.prototype.updateDimensions = function() {
	throw "TODO: update based on dimensions of inner div";
}

/**
 * Complete refresh, passing in new total number of items from datamodel
 */
GalleryCacheBase.prototype.refresh = function(level, totalNumberOfItems, widthMode, heightMode) {
	throw "Implement in subclass";
}

//Update y position within scrollable view, startYPos is offset into beginning of that view for first
//visible line.
// This is here for doc purposes since is a polymorphic method
GalleryCacheBase.prototype.updateOnScroll = function(yPos) {
	
}

GalleryCacheBase.prototype._getRowWidth = function() {
	return this._getVisibleWidth();
}

GalleryCacheBase.prototype._getTotalNumberOfItems = function() {
	return this.totalNumberOfItems;
}

GalleryCacheBase.prototype._getVisibleWidth = function() {
	return this.galleryView.getElementWidth(this.renderDiv);;
};

GalleryCacheBase.prototype._getVisibleHeight = function() {
	return this.galleryView.getElementHeight(this.outerDiv);
};

GalleryCacheBase.prototype._getColumnSpacing = function() {
	return this.gallerySizes.getColumnSpacing();
};

GalleryCacheBase.prototype._getRowSpacing = function() {
	return this.gallerySizes.getRowSpacing();
};

// Set the div that we are going to render into (ie. add row DOM elements to)
GalleryCacheBase.prototype.setGalleryDivs = function(outerDiv, renderDiv) {
	this.outerDiv = outerDiv;
	this.renderDiv = renderDiv;
}

GalleryCacheBase.prototype._getRenderDiv = function() {
	return this.renderDiv;
}

GalleryCacheBase.prototype._setScrollableHeight = function(height) {
	this.galleryView.setElementHeight(this.renderDiv, height);
}

GalleryCacheBase.prototype._getScrollableHeight = function(height) {
	return this.galleryView.getElementHeight(this.renderDiv);
}

GalleryCacheBase.prototype._makeProvisionalElement = function (index, itemWidth, itemHeight) {
	return this.galleryView.makeProvisionalHTMLElement(index, this.provisionalDataArray[index]);
}

/**
 * Clear all contents by removing all divs
 */
GalleryCacheBase.prototype._clear = function(level) {
	while (this.renderDiv.firstChild) {
		this.renderDiv.removeChild(this.renderDiv.firstChild);
	}
}

/**
 * return {
 * 		index, - index of last element rendered
 *  	yPos - yPos of below last element rendered, ie. start of next row
 * }
 */

GalleryCacheBase.prototype._addProvisionalDivs = function(level, startIndex, startPos, numColumns, heightToAdd) {
	
	this.enter(level, '_addProvisionalDivs', [
		'startIndex', startIndex,
		'startPos', startPos,
		'numColumns', numColumns,
		'heightToAdd', heightToAdd
	]);
	
	var t = this;
	
	var result = this._addDivs(level + 1, startIndex, startPos, numColumns, heightToAdd, function(index, itemWidth, itemHeight) {
		return t._makeProvisionalElement(index, itemWidth, itemHeight);
	});

	this.exit(level, '_addProvisionalDivs', JSON.stringify(result));

	return result;
}

/**
 * Add divs from current pos downwards, updating this.lastCachedIndex and this.lastY
 * 
 * Updates this.firstCachedIndex and this.firstY
 * 
 * startIndex - index of element to start at
 * startPos - the y position we should start rendering element at
 * numColumns - the number of items that are rendered per row
 * heightToAdd - the height of scrollable view area we should update
 *                 which might be less than the real visible area if we are just adding items to a partly updated area
 *                 
 * return {
 * 		index, - index of last element rendered
 *  	yPos - yPos of below last element rendered, ie. start of next row
 * }
 * 
 */
GalleryCacheBase.prototype._addDivs = function(level, startIndex, startPos, numColumns, heightToAdd, makeElement) {
	
	this.enter(level, 'addDivs', [ 'level',  level, 'startIndex', startIndex, 'startPos', startPos, 'numColumns', numColumns, 'heightToAdd', heightToAdd]);

	var t = this;
	
	var addRowDiv = function (rowDiv) {
		t.cachedRowDivs.push(rowDiv);
		t.galleryView.appendToContainer(t.renderDiv, rowDiv);
	};

	var lastRendered = this._addDivsWithAddFunc(level + 1, startIndex, startPos, numColumns, heightToAdd, true, addRowDiv, makeElement);

	this.exit(level, 'addDivs', JSON.stringify(lastRendered));

	return lastRendered;
}

/**
 * Adding divs before the currently visible ones.
 * 
 * startIndex - element to start adding upwards
 * startPos - y position of where to start adding, eg bottom border of where to start add
 * numColumns - items in a row
 * heightToAdd - height of items to add, may add outside of currently visible area
 * 
 * return {
 * 		index, - index of last element rendered, ie most towards start since we are prepending upwards
 *  	yPos - yPos of above last element rendered, ie. most towards start sine we are prepending upwards
 * }
 */
GalleryCacheBase.prototype._prependDivs = function(level, startIndex, startPos, numColumns, heightToAdd) {
	
	this.enter(level, 'prependDivs', [ 'startIndex', startIndex, 'startPos', startPos, 'numColumns', numColumns, 'heightToAdd', heightToAdd ])
	
	// Reuse _addDivs by computing the height of rows we must add and then add from there 
	var t = this;
	
	var startIndexPositionOnRow = startIndex % numColumns;
	
	if (startIndexPositionOnRow != numColumns - 1) {
		throw "startIndex should be last item on a row: idx " + startIndexPositionOnRow + " / numColumns " + numColumns;
	}
	
	var rowItem = null;

	var firstItemOnRow = startIndex - numColumns + 1;
	
	if (firstItemOnRow % numColumns != 0) {
		throw "Expected to be first item";
	}

	// item to add before
	var firstRowDiv = this.cachedRowDivs.length == 0
			? null
			: this.cachedRowDivs[0];

	var lastRendered = this._addDivsWithAddFunc(level + 1, firstItemOnRow, startPos, numColumns, heightToAdd, false,
			function (rowDiv) {
				if (firstRowDiv == null) {
					t.cachedRowDivs.push(rowDiv);
					t.galleryView.appendToContainer(innerDiv, rowDiv);
				}
				else {
					t.cachedRowDivs.splice(0, 0, rowDiv); // insert at beginning of row
					t._renderDiv.insertBefore(rowDiv, firstRowDiv);
				}
			},
			function(index, itemWidth, itemHeight) {
				return t._makeProvisionalElement(index, itemWidth, itemHeight);
			});

	this.exit(level, 'prependDivs', JSON.stringify(lastRendered));

	return lastRendered;
};

/**
 * Helper to add row divs with element divs to the display
 * 
 * - level - debug stack level, for indented logging
 * - startIndex - index of first item in virtual array to display
 * - startPos - start y position into scrollable dislay. Ie, not into visible display, we might add divs outside of visible area for later smooth scrolling.
 * - numColumns - number of columns in row
 * - heightToAdd - number of pixels to add, ie. stop adding rows when we have added this many pixels. Useful eg. when adding ites outside of visible area
 *                 for scrolling. Eg for partly cached view, we might add rows for 3 * visible height before and after visible view so that scolling with arrow keys appears smooth.
 *                 
 *  - downwards - true if adding items downwards from startPos, false if adding upwards. This way we an reuse this method for adding rows both before and after visible area,
 *  			  in addition to adding items for visible area when performing a refresh (or user scrolled quickly outside the complete cached area).
 *  
 *  - addRowDiv - function(rowDiv) for adding the newly created row <div> element to the DOM 
 */

GalleryCacheBase.prototype._addDivsWithAddFunc = function(level, startIndex, startPos, numColumns, heightToAdd, downwards, addRowDiv, makeElement) {

	this.enter(level, 'addDivsWithAddFunc', [
		'startIndex', startIndex,
		'startPos', startPos,
		'numColumns', numColumns,
		'heightToAdd', heightToAdd,
		'downwards', downwards ]);
	
	var rowsAdded = 0;
	var heightAdded = 0;
	
	// Add divs until there is not more room in display or there are no more items
	
	var y = startPos;
	
	var numRows = Math.floor((this._getTotalNumberOfItems() - 1) / numColumns) + 1;

	// If not at last element + 1 and does not start at a numColumns number, there is an issue at the caller side
	if (startIndex % numColumns != 0 && startIndex != this._getTotalNumberOfItems()) {
		throw "Start index not at start of column: " + startIndex + "/" + numColumns + ", total=" + this._getTotalNumberOfItems();
	}

	var rowNo = Math.floor(startIndex / numColumns);

	var rowWidth = this._getRowWidth();

	var lastRenderedElement = null;
	
	var i;
	var itemsThisRow;
	
	for (i = startIndex;
			i < this._getTotalNumberOfItems();
			// use itemsThisRows instead of numColumns below in case this is last row with < numColumns
			// so that return value returns the right value for lastRendered.index
			i += (downwards ? itemsThisRow : -itemsThisRow)) {

		// Last row might not have a full number of items
		itemsThisRow = i + numColumns >= this._getTotalNumberOfItems()
			? this.totalNumberOfItems - i
			: numColumns;
		
		var rowDiv = this.galleryView.createRowContainer();

		this.galleryView.setCSSClasses(rowDiv, 'gallery_row');

		// Add before adding elements so that we can add hidden row items and compute their size
		addRowDiv(rowDiv);

		rowNo = rowNo + (downwards ? 1 : -1);

		var t = this;

		// Add row items to the row
		var rowHeight = this._addRowItems(level + 1, rowDiv, i, itemsThisRow, numRows, rowWidth,
				function (index, itemWidth, itemHeight) {
					return makeElement(index, itemWidth, itemHeight);
				},
				function (element, indexInRow) {
					t.galleryView.appendToContainer(rowDiv, element);
				});

		this.log(level, 'Added row no ' + rowNo + ', first elem ' + i + ' at y pos ' + y + ' of height ' + rowHeight);

		++ rowsAdded;
		
		this.galleryView.applyRowContainerStyling(rowDiv, y, this.width, rowHeight);
	
		y += (downwards ? rowHeight : -rowHeight);
		heightAdded += rowHeight;

		if (heightAdded >= heightToAdd) {
			var offset = itemsThisRow - 1;
			
			lastRenderedElement = { 'index' : i + (downwards ? offset : -offset), 'yPos' :  y };
			break;
		}
	}
	
	if (lastRenderedElement == null) {
		if (rowsAdded > 0) {
			// Added rows but never reached heightAdded >= heightToAdd which means we
			// we we reached < 0 or > total, depending direction of adding
			var lastIndex = (downwards ? i - 1 : i + 1);
			lastRenderedElement = { 'index' : lastIndex /* itemsThisRow already added */, 'yPos' :  y };
		}
		else {
			// Just return null which means no rows added
		}
	}

	this.log(level, 'addDivsWithAddFunc added ' + rowsAdded + ' rows');

	this.exit(level, 'addDivsWithAddFunc', lastRenderedElement);

	return lastRenderedElement;
}


/**
 * Helper to complete column spacing for a given row. If only widthHint is given in config, then this spacing may vary
 * depending on width of constructed item DOM element.
 * 
 * - rowWidth - width of available space in row, in pixels
 * - totalRowItemWidths - sum of widths for item DOM elements on this row
 * - itemsThisRow - number of elements on this row. Last row might have fewer elements, eg 5 elements and 3 per row makes for 2 in the last row 
 * 
 */

GalleryCacheBase.prototype._computeColumnSpacing = function(rowWidth, totalRowItemWidths, itemsThisRow) {
	var spacing = (rowWidth - totalRowItemWidths) / (itemsThisRow + 1);

	return spacing;
}

/**
 * Helper to compute height of row content. If config.widthHint is set instead of hardcoding with config.width, then this must be computed
 * from the tallest element in the row. If config.width is set, then all are same width.
 * 
 *  - rowMaxHeight - height of talles row item DOM element
 *  - rowNo - index of row into total number of rows, ie. first row is 0. Together with numRows below, this is used
 *            to figure out if we are on the first or last row or any inbetween. First and last row require extra spacing above/below
 *  - numRows - total number of rows for display whole virtual array.
 *  
 */

GalleryCacheBase.prototype._getRowHeight = function(rowMaxHeight, rowNo, numRows) {
	var rowHeigth;

	rowHeight = rowMaxHeight + this._getRowSpacing();

	return rowHeight;
}


/**
 * Helper to add the items in one gallery row. If width or height not hardcoded in config,
 * this will first add elements to DOM as hidden, then get width and height and adjust accordingly for centering the items
 * when changing items to being visible (remove visibility : hidden from styling)
 */
GalleryCacheBase.prototype._addRowItems = function(level, rowDiv, indexOfFirstInRow, itemsThisRow, numRowsTotal, rowWidth, makeElement, addElement) {

	/*
	this.enter(level, "_addRowItems", [
		'indexOfFirstInRow', indexOfFirstInRow,
		'itemsThisRow', itemsThisRow,
		'numRowsTotal', numRowsTotal,
		'rowWidth', rowWidth])
	*/
	
	// Make sure first in row is indeed at first
	var numColumns = this._computeNumColumns();
	var indexIntoRow = indexOfFirstInRow % numColumns;
	
	if (indexIntoRow != 0) {
		throw "First item not at start of row: " + indexIntoRow;
	}

	var itemWidth = this.gallerySizes.getSpecificWidthOrNull();
	var itemHeight = this.gallerySizes.getSpecificHeightOrNull();
	
	var x = 0;

	var spacing = 0;
	
	if (itemWidth != null) {
		var totalRowItemWidths = itemsThisRow * itemWidth;
		
		spacing = this._computeColumnSpacing(rowWidth, totalRowItemsWidth, itemsThisRow);
	}

	var mustComputeDimensions = itemWidth == null || itemHeight == null;
	
	// Do not show the item if must compute dimensions
	var visible = !mustComputeDimensions;
	
	var rowHTMLElements = [];

	// Loop though and add, might render with visibility : hidden if width or height not known
	// since we then have to get these and adjust position and spacing accordingly

	var rowHeight = null;
	
	if (itemHeight != null) {
		// hardcoded height so row height same as item height
		rowHeight = this._getRowHeight(itemHeight, index, numRowsTotal);
	}
	

	for (var j = 0; j < itemsThisRow; ++ j) {
		var index = indexOfFirstInRow + j;

		var itemElement = makeElement(index, itemWidth, itemHeight);

		// Add to model at relative offsets
		// this.log(level, 'set spacing to ' + spacing + '/' + rowWidth + '/' + totalRowItemWidths + '/' + itemsThisRow);
		
		this._applyItemStyles(itemElement, rowHeight, itemWidth, itemHeight, spacing, visible);

		addElement(itemElement, j);
		
		rowHTMLElements.push(itemElement);
		
		x += itemWidth;
	}

	this.log(level, 'mustComputeDimensions: ' + mustComputeDimensions + ', rowHeight: ' + rowHeight);

	if (mustComputeDimensions) {
		
		var totalRowItemsWidth = 0;
		var largestItemHeight = 0;
		
		// HTML elements are not visible but we might retrieve their dimensions
		for (var i = 0; i < rowHTMLElements.length; ++ i) {
			var elem = rowHTMLElements[i];
			
			// console.log('## computed client width: ' + this.galleryView.getElementWidth(elem) + ', height:' + this.galleryView.getElementHeight(elem) + ': ' + elem.parentNode + ', ' + elem.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode);
			
			totalRowItemsWidth += this.galleryView.getElementWidth(elem);
			
			var elemHeight = this.galleryView.getElementHeight(elem);
			
			if (elemHeight > largestItemHeight) {
				largestItemHeight = elemHeight;
			}
		}
		
		rowHeight = this._getRowHeight(largestItemHeight, index, numRowsTotal);

		// console.log('## row height from largestItemHeight=' + largestItemHeight + ', index=' + index + ', numRowsTotal=' + numRowsTotal + ' : '  + rowHeight);

		spacing = this._computeColumnSpacing(rowWidth, totalRowItemsWidth, itemsThisRow);

		if (itemHeight == null) {
			itemHeight = largestItemHeight;
		}
		
		visible = true;

		// Update style to show item with given width, height and spacing
		for (var i = 0; i < rowHTMLElements.length; ++ i) {
			var elem = rowHTMLElements[i];

			var width = itemWidth == null ? this.galleryView.getElementWidth(elem) : itemWidth;
			var height = largestItemHeight;
			
			this._applyItemStyles(elem, rowHeight, width, height, spacing, visible);
		}
		
		visible = true;
	}
	
	// this.exit(level, '_addRowItems', rowHeight);

	return rowHeight;
}

GalleryCacheBase.prototype._computeNumColumns = function() {
	return this.gallerySizes.computeNumColumns(this._getVisibleWidth());
}

// Apply necessary styling to set dimensions and placement of an item
GalleryCacheBase.prototype._applyItemStyles = function(itemElement, rowHeight, itemWidth, itemHeight, spacing, visible) {
	this.galleryView.applyItemStyles(itemElement, rowHeight, itemWidth, itemHeight, spacing, visible);
}
