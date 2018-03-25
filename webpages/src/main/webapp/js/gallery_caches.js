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

function GalleryCacheBase(config, galleryModel, galleryView, initialTotalNumberOfItems) {
	
	this.config = config;
	this.galleryModel = galleryModel;
	this.galleryView = galleryView;
	this.totalNumberOfItems = initialTotalNumberOfItems;

	if (typeof config.columnSpacing === 'undefined') {
		this.columnSpacing = 20;
	}
	else {
		this.columnSpacing = config.columnSpacing;
	}
	
	if (typeof config.rowSpacing === 'undefined') {
		this.rowSpacing = 20;
	}
	else {
		this.rowSpacing = config.rowSpacing;
	}
	
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

GalleryCacheBase.prototype._computeHeight = function(heightMode, numColumns) {
	return heightMode.computeHeight(this.config, this.rowSpacing, numColumns, this._getTotalNumberOfItems());
}

GalleryCacheBase.prototype._getVisibleWidth = function() {
	return this.renderDiv.clientWidth;
};

GalleryCacheBase.prototype._getVisibleHeight = function() {
	return this.outerDiv.clientHeight;
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
	this.renderDiv.style.height = height + 'px';
}

GalleryCacheBase.prototype._makeProvisionalElement = function (index, itemWidth, itemHeight) {
	return this.galleryView.makeProvisionalHTMLElement(index, this.provisionalDataArray[index]);
}

GalleryCacheBase.prototype._addProvisionalDivs = function(level, startIndex, startPos, numColumns, heightToAdd) {
	
	var t = this;
	
	this._addDivs(level, startIndex, startPos, numColumns, heightToAdd, function(index, itemWidth, itemHeight) {
		return t._makeProvisionalElement(index, itemWidth, itemHeight);
	});
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
 */
GalleryCacheBase.prototype._addDivs = function(level, startIndex, startPos, numColumns, heightToAdd, makeElement) {
	
	this.enter(level, 'addDivs', [ 'level',  level, 'startIndex', startIndex, 'startPos', startPos, 'numColumns', numColumns, 'heightToAdd', heightToAdd]);

	var t = this;
	
	var addRowDiv = function (rowDiv) {
		t.cachedRowDivs.push(rowDiv);
		t.renderDiv.append(rowDiv);
	};

	var lastRendered = this._addDivsWithAddFunc(level + 1, startIndex, startPos, numColumns, heightToAdd, true, addRowDiv, makeElement);
	
	this.lastCachedIndex = lastRendered.index; // last drawn element
	this.lastY = lastRendered.yPos;

	this.log(level, 'firstY set to ' + this.firstY + ' and not changed, set lastIndex to ' + lastRendered.index + ',lastY to ' + lastRendered.yPos);

	this.exit(level, 'addDivs');
}

/**
 * Adding divs before the currently visible ones.
 * 
 * startIndex - element to start adding upwards
 * startPos - y position of where to start adding, eg bottom border of where to start add
 * numColumns - items in a row
 * heightToAdd - height of items to add, may add outside of currently visible area
 * 
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
					t.innerDiv.append(rowDiv);
				}
				else {
					t.cachedRowDivs.splice(0, 0, rowDiv); // insert at beginning of row
					t._renderDiv.insertBefore(rowDiv, firstRowDiv);
				}
			},
			function(index, itemWidth, itemHeight) {
				return t._makeProvisionalElement(index, itemWidth, itemHeight);
			});

	this.firstCachedIndex = lastRendered.index; // last drawn element
	this.firstY = lastRendered.yPos;

	this.exit(level, 'prependDivs');
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
	
	var numRows = ((this._getTotalNumberOfItems() - 1) / numColumns) + 1;

	if (startIndex % numColumns != 0) {
		throw "Start index not at start of column: " + startIndx + "/" + numColumns;
	}

	var rowNo = startIndex / numColumns;

	var rowWidth = this._getRowWidth();

	var lastRenderedElement = null;
	
	for (var i = startIndex; i < this._getTotalNumberOfItems(); i += (downwards ? numColumns : -numColumns)) {

		// Last row might not have a full number of items
		var itemsThisRow = i + numColumns >= this._getTotalNumberOfItems()
			? this.totalNumberOfItems - i
			: numColumns; 
		
		var rowDiv = document.createElement('div');
		
		rowDiv.setAttribute('class', 'gallery_row');

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
					rowDiv.append(element);
				});
		
		this.log(level, 'Adding row no ' + rowNo + ', first elem ' + i + ' at y pos ' + y + ' of height ' + rowHeight);
		
		++ rowsAdded;

		rowDiv.setAttribute('style',
				//'position : relative; ' +
				'top :  ' + y + '; ' +
				'width : ' + this.width + '; ' +
				'height : ' + rowHeight + '; ' +
				'border : 1px solid black;' +
				'background-color : yellow; ');


		y += (downwards ? rowHeight : -rowHeight);
		heightAdded += rowHeight;

		console.log('_addDivsWith')
		if (heightAdded >= heightToAdd) {

			lastRenderedElement = { 'index' : i + itemsThisRow - 1, 'yPos' :  y };
			break;
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

	rowHeight = rowMaxHeight + this.rowSpacing;
	
	// TODO is this correct?
	if (rowNo == 0) {
		rowHeight += this.topSpacing;
	}
	else if (rowNo == numRows - 1) {
		rowHeight += this.bottomSpacing;
	}

	return rowHeight;
}


/**
 * Helper to add the items in one gallery row. If width or height not hardcoded in config,
 * this will first add elements to DOM as hidden, then get width and height and adjust accordingly for centering the items
 * when changing items to being visible (remove visibility : hidden from styling)
 */
GalleryCacheBase.prototype._addRowItems = function(level, rowDiv, indexOfFirstInRow, itemsThisRow, numRowsTotal, rowWidth, makeElement, addElement) {

	var itemWidth = null;
	var itemHeight = null;
	
	if (typeof this.config.width !== 'undefined') {
		itemWidth = this.config.width;
	}
	
	if (typeof this.config.height !== 'undefined') {
		itemHeight = this.config.height;
	}
	
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
	
	if (mustComputeDimensions) {
		
		var totalRowItemsWidth = 0;
		var largestItemHeight = 0;
		
		// HTML elements are not visible but we might retrieve their dimensions
		for (var i = 0; i < rowHTMLElements.length; ++ i) {
			var elem = rowHTMLElements[i];
			
			// console.log('## computed client width: ' + elem.clientWidth + ', height:' + elem.clientHeight + ': ' + elem.parentNode + ', ' + elem.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode);
			
			totalRowItemsWidth += elem.clientWidth;
			
			if (elem.clientHeight > largestItemHeight) {
				largestItemHeight = elem.clientHeight;
			}
		}
		
		rowHeight = this._getRowHeight(largestItemHeight, index, numRowsTotal);
		
		spacing = this._computeColumnSpacing(rowWidth, totalRowItemsWidth, itemsThisRow);

		if (itemHeight == null) {
			itemHeight = largestItemHeight;
		}
		
		visible = true;

		// Update style to show item with given width, height and spacing
		for (var i = 0; i < rowHTMLElements.length; ++ i) {
			var elem = rowHTMLElements[i];
			
			var width = itemWidth == null ? elem.clientWidth : itemWidth;
			var height = largestItemHeight;
			
			this._applyItemStyles(elem, rowHeight, width, height, spacing, visible);
		}
		
		visible = true;
	}

	return rowHeight;
}

// Apply necessary styling to set dimensions and placement of an item
GalleryCacheBase.prototype._applyItemStyles = function(itemElement, rowHeight, itemWidth, itemHeight, spacing, visible) {
	
	var styling = 'position : relative; ' +
		/*
		'display : inline-block; ' +
		*/
		'float : left; ' +
		'margin-left : ' + spacing + 'px; ' +
		'background-color : white; ';
	
	if (itemHeight != null) {
		styling += 'top : ' + (rowHeight - itemHeight) / 2 + 'px; ';
		styling += 'height : ' + itemHeight + '; ';
	}
	
	if (itemWidth != null) {
		'width : ' + itemWidth + '; ';
	}
	
	if (!visible) {
		// set hidden if we need to find item size
		styling += 'visibility: hidden; '
	}
		
	itemElement.setAttribute('style', styling);
}