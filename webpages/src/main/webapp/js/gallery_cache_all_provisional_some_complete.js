/**
 * Downloads all provisional data on complete refresh (eg. titles and thumb sizes)
 * then download complete-data (eg. image thumbs) on demand, eg keep 3 pages of images above and below the
 * currently visible pages.
 * 
 */

function GalleryCacheAllProvisionalSomeComplete(config, galleryModel, galleryView, initialTotalNumberOfItems) {
	GalleryCacheBase.call(this, config, galleryModel, galleryView, initialTotalNumberOfItems);
	
	this.visibleElements = null;
}

GalleryCacheAllProvisionalSomeComplete.prototype = Object.create(GalleryCacheBase.prototype);

//returns approximate complete size of view
GalleryCacheAllProvisionalSomeComplete.prototype.refresh = function(level, totalNumberOfItems, widthMode, heightMode) {
	
	var t = this;
	
	// Mechanism for downloading complete-data on the fly as user scrolls
	this.cacheItems = new GalleryCacheItems(20, function(index, count, onDownloaded) {
		t.galleryModel.getCompleteData(index, count, onDownloaded);
	});
	
	var t = this;

	// Placeholder div at the beginnig which we can use set the start
	// of rendered divs without adding all divs from the beginning and down
	// so with a million entries in the virtual data array and visible area in the middle of this,
	// we just adjust height of placeholder to be up to visible area, then add gallery elements to view.
	// As the user scrolls, we update this height and replace the elements to view
	
	if (typeof this.upperPlaceHolder === 'undefined') {
		this.upperPlaceHolder = document.createElement('div');
		this._getRenderDiv().append(this.upperPlaceHolder);
	}

	this.totalNumberOfItems = totalNumberOfItems;
	
	this.galleryModel.getProvisionalData(0, totalNumberOfItems, function(provisionalDataArray) {
		t.provisionalDataArray = provisionalDataArray;
		// completed metadata build, now compute and rerender
		t.render(level + 1, widthMode, heightMode);
	});
}

GalleryCacheAllProvisionalSomeComplete.prototype.render = function(level, widthMode, heightMode) {

	// Get the width of element to compute how many elements there are room for
	var numColumns = widthMode.computeNumColumns(this.config, this.columnSpacing, this._getVisibleWidth());
	
	this.numColumns = numColumns;
	
	this.log(level, 'Thumbs per row: ' + numColumns);
	
	// Have thumbs per row, now compute height
	var height = this._computeHeight(heightMode, numColumns);
	
	// Set height of complete scrollable area, this might have to be adjusted as we scroll
	// but must be set in order to have scrollbars appear correctly relative to number of elements in virtual array
	this._setScrollableHeight(height);
	
	this.log(level, 'Height: ' + height);

	// We can now render within the visible area by adding divs and displaying them as we scroll
	// at a relative position to the display area
	
	// or use a canvas, but that would require backing area for the gallery, so rather just use number of divs for which we update relative area

	// Compute the starting point of every element
	
	// What is the current start offset of scrolling?
	
	// Set the offset of each element to that, but what about sizes? Once we scroll an element out, we must add a new one
	
	var visibleHeight = this._getVisibleHeight();
	
	this.log(level, 'Visible height: ' + visibleHeight);
	
	// Start at the current ones
	this._addProvisionalDivs(level + 1, 0, 0, numColumns, visibleHeight);
}


//Update y position within scrollable view, startYPos is offset into beginning of that view for first
//visible line
GalleryCacheAllProvisionalSomeComplete.prototype.updateOnScroll = function(level, yPos) {
	
	this.enter(level, 'updateOnScroll', ['yPos', yPos], ['this.firstY', this.firstY]);
	
	var curFirstY = this.firstY;
	
	if (this.visibleElements == null) {
		this.visibleElements = {
			firstVisibleY : 0,
			firstRenderedY : 0, // renders a bit outside of display since adding complete rows
			firstVisibleIndex : 0,
			lastVisibleY : 0,
			lastRenderedY : 0, // renders a bit outside of display since adding complete rows
			lastVisibleIndex : 0
		};
	}
	
	// Updates first and last cached item index base on y position
	this.visibleElements = this._updateOnScroll(level + 1, yPos, this.visibleElements);
	
	// Start a timer to check whether user has stopped scrolling,
	// we are not going to update the DOM as longs as user is scrolling as
	// for large number of items we will not be able to update fast enough and
	// it will make scrolling less smooth
	
	/*
	if (!this.scrollTimeoutSet) { // avoid having multiple timeouts
	
		this.scrollTimeoutSet = true;
		
		var t = this;
		
		setTimeout(function() {
				if (curFirstY === t.firstY) {
					// Show complete-version since not scrolled ( t.firstY was not updated by _updateOnScroll() )
					t._showComplete(level + 1, t.firstCachedIndex, t.lastCachedIndex - t.firstCachedIndex + 1);
				}

				t.scrollTimeoutSet = false;
		},
		100);
	}
	*/
	
	// Update cache view to point to new display area, it will also preload elements around display area
	
	var visibleCount = this.visibleElements.lastVisibleIndex - this.visibleElements.firstVisibleIndex + 1;

	var visibleElements = this.visibleElements;
	
	var t = this;
	
	// TODO also add callback for preload data since we would want to precreate divs? Test whether is good enough without
	this.cacheItems.updateVisibleArea(
			level + 1,
			this.visibleElements.firstVisibleIndex,
			visibleCount,
			this.totalNumberOfItems,
			
			function (index, count, downloadedData) {
				
				// Only called when haven't scrolled (eg no other call to updateVisibleArea)
				if (index !== visibleElements.firstVisibleIndex) {
					throw "Index mismatch: requested=" + visibleElements.firstVisibleIndex + ", retrieved: " + index;
				}
				if (count !== visibleCount) {
					throw "Count mismatch: " + count + "/" + visibleCount;
				}
				if (downloadedData.length !== visibleCount) {
					throw "Number of items mismatch count, expected " + visibleCount + ", got " + downloadedData.length;
				}

				// Can now update rows from data
				t._showCompleteForRows(0, index, count, downloadedData);
			});

	this.exit(level, 'updateOnScroll');
}


// Helper method for update on scroll
GalleryCacheAllProvisionalSomeComplete.prototype._updateOnScroll = function(level, curY, prevDisplayed) {

	this.enter(level, '_updateOnScroll',
			[ 'curY', curY, 'prevDisplayed', JSON.stringify(prevDisplayed)],
			[ 'firstY',  this.firstY,  'lastY', this.lastY, '_getVisibleHeigt()', this._getVisibleHeight() ],);
	
	// See if we have something that was not visible earlier scrolled into view

	var firstRenderedY;
	var lastRenderedY;
	var firstVisibleIndex;
	var lastVisibleIndex;
	
	var posAndIndex = this._findElementYPosAndItemIndex(level + 1, curY);
	
	var lastRendered;
	
	if (curY + this._getVisibleHeight() < prevDisplayed.firstVisibleY) {
		this.log(level, 'Scrolled to view completely above previous');

		// We are scrolling upwards totally out of current area
		lastRendered = this._redrawCompletelyAt(level + 1, curY, posAndIndex);
		
		firstRenderedY = posAndIndex.rowYPos;
		lastRenderedY = lastRendered.yPos;
		firstVisibleIndex = posAndIndex.rowItemIndex;
		lastVisibleIndex = lastRendered.index;
	}
	else if (curY < prevDisplayed.firstVisibleY) {
		// Scrolling partly above visible area
		var heightToAdd = prevDisplayed.firstVisibleY - curY;

		this.log(level, 'Scrolled to view partly above previous, must add ' + heightToAdd);

		// Must add items before this one, so must be prepended to the divs already shown
		lastRendered = this._prependDivs(level + 1, prevDisplayed.firstVisibleIndex - 1, prevDisplayed.firstVisibleY, this.numColumns, heightToAdd);

		firstRenderedY = lastRendered.rowYPos;
		firstVisibleIndex = lastRendered.rowItemIndex;

		throw "TODO figure out last rendered after prepending"
		lastRenderedY = lastRendered.yPos;
		lastVisibleIndex = lastRendered.index;
	}
	else if (curY > prevDisplayed.lastVisibleY) {
		// We are scrolling downwards totally out of visible area, just add items for the pos in question
		this.log(level, 'Scrolled to completely below previous curY ' + curY + ' visibleHeight ' + this._getVisibleHeight() + ' > lastY ' + this.lastY);

		lastRendered = this._redrawCompletelyAt(level + 1, curY, posAndIndex);

		firstRenderedY = posAndIndex.rowYPos;
		lastRenderedY = lastRendered.yPos;
		firstVisibleIndex = posAndIndex.rowItemIndex;
		lastVisibleIndex = lastRendered.index;
	}
	else if (prevDisplayed.lastVisibleY - curY < this._getVisibleHeight()) {
		// Scrolling down partly out of visible area
		// First figure out how much visible space that must be added
		var heightToAdd = this._getVisibleHeight() - (prevDisplayed.lastVisibleY - curY);

		this.log(level, 'Scrolled to view partly below previous, must add ' + heightToAdd);

		// Do we need to add one or more rows? Should do so without removing existing rows,
		// just add new ones below current ones.

		// Start-index to add is the one immediately after last-index
		lastRendered = this._addProvisionalDivs(level + 1, prevDisplayed.lastVisibleIndex + 1, prevDisplayed.lastVisibleY, this.numColumns, heightToAdd);

		if (lastRendered == null) {
			// Nothing was rendered, ie. did not scroll any new items into display
			// so just return old values
			firstRenderedY 		= prevDisplayed.firstRenderedY;
			lastRenderedY 		= prevDisplayed.lastRenderedY;
			firstVisibleIndex 	= prevDisplayed.firstVisibleIndex;
			lastVisibleIndex 	= prevDisplayed.lastVisibleIndex;
		}
		else {
			firstRenderedY = posAndIndex.rowYPos;
			lastRenderedY = lastRendered.yPos;
			firstVisibleIndex = posAndIndex.rowItemIndex;
			lastVisibleIndex = lastRendered.index;
		}
	}
	else {
		this.log(level, 'Did not match any test');
	}
	
	var displayed = {
		firstVisibleY : curY,
		firstRenderedY : firstRenderedY,
		firstVisibleIndex : firstVisibleIndex,
		lastVisibleY : curY + this._getVisibleHeight() - 1,
		lastRenderedY : lastRenderedY,
		lastVisibleIndex : lastVisibleIndex
	};

	this.exit(level, '_updateOnScroll', JSON.stringify(displayed));
	
	return displayed;
};



/**
 * Download and show complete items in a certain model range, provisional items
 * for this range must have been downloaded already.
 * 
 *  - level - debug log indentation level
 *  - firstModelItemIndex - index into model virtual array where to start
 *  - itemCount number of items to update
 */

GalleryCacheAllProvisionalSomeComplete.prototype._getRowItemDivHeights = function(rowDiv) {
	var itemsThisRow = rowDiv.childNodes.length;
	
	// Store new elements in array and then replace all at once
	var rowWidthHeights = [];
	for (var j = 0; j < itemsThisRow; ++ j) {
		var itemElement = rowDiv.childNodes[j];

		rowWidthHeights.push({ width : itemElement.clientWidth, height : itemElement.clientHeight })
	}
	
	return rowWidthHeights;
}

/*
GalleryCacheAllProvisionalSomeComplete.prototype._showComplete = function(level, firstModelItemIndex, itemCount) {
	
	
	this.enter(level, '_showComplete', [ 'firstModelItemIndex', firstModelItemIndex, 'itemCount', itemCount]);

		
	// Not scrolled since timeout started, load images

	var t = this;
	
	// Call external functions to load images

	//	this.galleryModel.getCompleteData(firstModelItemIndex, itemCount, function(completeDataArray) {
	this.cacheView.getCompleteData(firstModelItemIndex, itemCount, function(completeDataArray) {
			
	});
	
	this.exit(level, '_showComplete');
}
*/


/**
 * Update to complete-items for rows, given indices and data to display
 * 
 * - firstModelItemIndex - index of first element into virtual array
 * - itemCount - number of items to update
 * - completeDataArray - array of complete-data for items, contains only itemCount entries 
 * 
 */
GalleryCacheAllProvisionalSomeComplete.prototype._showCompleteForRows = function(level, firstModelItemIndex, itemCount, completeDataArray) {
	
	if (completeDataArray.length !== itemCount) {
		throw "Expected itemCount entries";
	}

	var rowWidth = this._getRowWidth();
	var numRows = this.cachedRowDivs.length;
	var numRowsTotal = ((this._getTotalNumberOfItems() - 1) / this.numColumns) + 1;
	
	for (var row = 0, i = firstModelItemIndex; row < numRows && i < itemCount; ++ row) {
		
		var rowDiv = this.cachedRowDivs[row];
		var itemsThisRow = rowDiv.childNodes.length;

		// Store new elements in array and then replace all at once
		var rowWidthHeights = this._getRowItemDivHeights(rowDiv);

		var t = this;

		// Replace row items
		this._addRowItems(level + 1, rowDiv, i, itemsThisRow, numRowsTotal, rowWidth,
				function (index, itemWidth, itemHeight) {
			
					var completeData = completeDataArray[index - firstModelItemIndex];
					var item;
					
					if (completeData == null) {
						item = rowDiv.childNodes[index - i];
					}
					else if (typeof completeData === 'undefined') {
						throw "Image data undefined at: " + index;
					}
					else {
						item = t.galleryView.makeCompleteHTMLElement(index, t.provisionalDataArray[index], completeData);
					}
					
					return item;
				},
				function (element, rowIndex) {
					rowDiv.replaceChild(element, rowDiv.childNodes[rowIndex]);
	
	//					newRowItems.push(element);
				});
		
		i += itemsThisRow;
	
		var updatedRowWidthHeights = this._getRowItemDivHeights(rowDiv);
	
		for (var j = 0; j < itemsThisRow; ++ j) {
			var prevDim = rowWidthHeights[j];
			var curDim  = updatedRowWidthHeights[j];
	
			if (prevDim.width !== curDim.width || prevDim.height !== curDim.height) {
				throw "Gallery item dimensions changed between provisional and updated for " + (i + j)
					+ " : prev=" + JSON.stringify(prevDim) + ", cur=" + JSON.stringify(curDim);
			}
		}
	}
}

GalleryCacheAllProvisionalSomeComplete.prototype._redrawCompletelyAt = function(level, curY, posAndIndex) {

	this.enter(level, 'redrawCompletelyAt', [ 'curY', curY ]);
	
	this.log(level, 'Element start index: ' + posAndIndex.rowItemIndex + ', removing all rows: ' + this.cachedRowDivs.length);

	this.upperPlaceHolder.setAttribute('style', 'height : '+ curY + ';');

	// Remove all row elements since we will just generate them anew after the initial div
	// used for making sure the rows show up at the right virtual y index
	for (var i = 0; i < this.cachedRowDivs.length; ++ i) {
		
		var toRemove = this.cachedRowDivs[i];

		this.log(level, 'Removing element ' + toRemove);

		this._getRenderDiv().removeChild(toRemove);
	}

	this.cachedRowDivs = new Array();

	this.firstCachedIndex = posAndIndex.rowItemIndex;
	this.firstY = curY;

	var lastRendered = this._addProvisionalDivs(level + 1, posAndIndex.rowItemIndex, posAndIndex.rowYPos, this.numColumns, this._getVisibleHeight());
	
	this.exit(level, 'redrawCompletelyAt', JSON.stringify(lastRendered));
	
	return lastRendered;
};


GalleryCacheAllProvisionalSomeComplete.prototype._findElementYPosAndItemIndex = function(level, yPos) {
	
	this.enter(level, 'findElementPos', [ 'yPos', yPos ])

	// Go though heights list until we find the one that intersects with this y pos
	var y = 0;

	var elem = null;
	
	// We do not know item heights, only an approximation in case of heightHint
	// so just figure out by multiplying
	
	// TODO move this out, should be in modes or other place, maybe in config?
	var heightOfOneElement = typeof this.config.widthHint !== 'undefined'
				? this.config.widthHint
				: this.config.width;

	heightOfOneElement += this.rowSpacing;
	
	// Now we have can divide to find start
	var itemIndex = Math.floor(yPos / heightOfOneElement);
	var rowYPos = heightOfOneElement * itemIndex;
	
	elem = { 'rowYPos' : rowYPos, 'rowItemIndex' : itemIndex };

	/*
	var y = yPos

	for (var i = 0; i < this.totalNumberOfItems; i += this.numColumns) {
		var itemsThisRow = i + this.numColumns >= this.totalNumberOfItems
			? this.totalNumberOfItems - i
			: this.numColumns; 
		
		var nextY = y;
		nextY += this.rowSpacing;
		
		var rowMaxHeight = this._findRowMaxItemHeight(i, itemsThisRow);
		
		nextY += rowMaxHeight;
		
		if (y <= yPos && nextY >= yPos) {
			elem = { 'rowYPos' : y, 'rowItemIndex' : i };
		}
		
		y = nextY;
	}
	*/

	this.exit(level, 'findElementPos', JSON.stringify(elem));
	
	return elem;
}
