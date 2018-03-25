/**
 * Downloads all provisional data on complete refresh (eg. titles and thumb sizes)
 * then download complete-data (eg. image thumbs) on demand, eg keep 3 pages of images above and below the
 * currently visible pages.
 * 
 */

function GalleryCacheAllProvisionalSomeComplete(config, galleryModel, galleryView, initialTotalNumberOfItems) {
	GalleryCacheBase.call(this, config, galleryModel, galleryView, initialTotalNumberOfItems);
}

GalleryCacheAllProvisionalSomeComplete.prototype = Object.create(GalleryCacheBase.prototype);

//returns approximate complete size of view
GalleryCacheAllProvisionalSomeComplete.prototype.refresh = function(level, totalNumberOfItems, widthMode, heightMode) {
	var t = this;

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
	
	this.height = height;
	
	this.log(level, 'Height: ' + height);

	// We can now render within the visible area by adding divs and displaying them as we scroll
	// at a relative position to the display area
	
	// or use a canvas, but that would require backing area for the gallery, so rather just use number of divs for which we update relative area

	// Compute the starting point of every element
	
	// What is the current start offset of scrolling?
	
	// Set the offset of each element to that, but what about sizes? Once we scroll an element out, we must add a new one
	
	// Start at the current ones
	this._addProvisionalDivs(level + 1, 0, 0, numColumns, this._getVisibleHeight());
}


//Update y position within scrollable view, startYPos is offset into beginning of that view for first
//visible line
GalleryCacheAllProvisionalSomeComplete.prototype.updateOnScroll = function(level, yPos) {
	
	this.enter(level, 'updateOnScroll', ['yPos', yPos], ['this.firstY', this.firstY]);
	
	var curFirstY = this.firstY;
	
	this._updateOnScroll(yPos);
	
	// Start a timer to check whether user has stopped scrolling,
	// we are not going to update the DOM as longs as user is scrolling as
	// for large number of items we will not be able to update fast enough and
	// it will make scrolling less smooth
	
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

	this.exit(level, 'updateOnScroll');
}


// Helper method for update on scroll
GalleryCacheAllProvisionalSomeComplete.prototype._updateOnScroll = function(curY) {
	// See if we have something that was not visible earlier scrolled into view
	
	var level = 0;
	
	this.enter(level, 'updateOnScroll', ['curY', curY], [ 'firstY',  this.firstY,  'lastY', this.lastY ]);

	if (curY + this._getVisibleHeight() < this.firstY) {
		this.log(level, 'Scrolled to view completely above previous');

		// We are scrolling upwards totally out of current area
		this._redrawCompletelyAt(level + 1, curY);
	}
	else if (curY < this.firstY) {
		// Scrolling partly above visible area
		var heightToAdd = this.firstY - curY;

		this.log(level, 'Scrolled to view partly above previous, must add ' + heightToAdd);

		// Must add items before this one, so must be prepended to the divs already shown
		this._prependDivs(level + 1, this.firstCachedIndex - 1, this.firstY, this.numColumns, heightToAdd);
	}
	else if (curY > this.lastY) {
		// We are scrolling downwards totally out of visible area, just add items for the pos in question
		this.log(level, 'Scrolled to completely below previous curY ' + curY + ' visibleHeight ' + this._getVisibleHeight() + ' > lastY ' + this.lastY);

		this._redrawCompletelyAt(level + 1, curY);
	}
	else if (this.lastY - curY < this._getVisibleHeight()) {
		// Scrolling down partly out of visible area
		// First figure out how much visible space that must be added
		var heightToAdd = this._getVisibleHeight() - (this.lastY - curY);

		this.log(level, 'Scrolled to view partly below previous, must add ' + heightToAdd);

		// Do we need to add one or more rows? Should do so without removing existing rows,
		// just add new ones below current ones.

		// Start-index to add is the one immediately after last-index
		this._addProvisionalDivs(level + 1, this.lastCachedIndex + 1, this.lastY, this.numColumns, heightToAdd);
		
		// Do not update this.firstCachedIndex or this.firstY since we are appending
		// TODO perhaps remove rows that have scrolled out of sight
	}
	
	this.exit(level, 'updateOnScroll');
};


/**
 * Download and show complete items in a certain model range, provisional items
 * for this range must have been downloaded already.
 * 
 *  - level - debug log indentation level
 *  - firstModelItemIndex - index into model virtual array where to start
 *  - itemCount number of items to update
 */

GalleryCacheAllProvisionalSomeComplete.prototype._showComplete = function(level, firstModelItemIndex, itemCount) {
	
	function getRowItemDivs(rowDiv) {
		var itemsThisRow = rowDiv.childNodes.length;
		
		// Store new elements in array and then replace all at once
		var rowWidthHeights = [];
		for (var j = 0; j < itemsThisRow; ++ j) {
			var itemElement = rowDiv.childNodes[j];

			rowWidthHeights.push({ width : itemElement.clientWidth})
		}
		
		return rowWidthHeights;
	}
	
	this.enter(level, '_showComplete', [ 'firstModelItemIndex', firstModelItemIndex, 'itemCount', itemCount]);

		
	// Not scrolled since timeout started, load images

	var t = this;
	
	// Call external functions to load images
	this.galleryModel.getCompleteData(firstModelItemIndex, itemCount, function(completeDataArray) {
		
		var rowNo = firstModelItemIndex / t.numColumns;

		var rowWidth = t._getRowWidth();
		var numRows = t.cachedRowDivs.length;
		var numRowsTotal = ((t._getTotalNumberOfItems() - 1) / t.numColumns) + 1;

		for (var row = 0, i = firstModelItemIndex; row < numRows && i < itemCount; ++ row) {
			
			var rowDiv = t.cachedRowDivs[row];
			var itemsThisRow = rowDiv.childNodes.length;
			
			// Store new elements in array and then replace all at once
			var rowWidthHeights = getRowItemDivs(rowDiv);
			
			t._addRowItems(level + 1, rowDiv, i, itemsThisRow, numRowsTotal, rowWidth,
					function (index, itemWidth, itemHeight) {
				
						var completeData = completeDataArray[index];
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

//							newRowItems.push(element);
					});
			
			i += itemsThisRow;

			var updatedRowWidthHeights = getRowItemDivs(rowDiv);

			for (var j = 0; j < itemsThisRow; ++ j) {
				var prevDim = rowWidthHeights[j];
				var curDim  = updatedRowWidthHeights[j];

				if (prevDim.width !== curDim.width || prevDim.height !== curDim.height) {
					throw "Gallery item dimensions changed between provisional and updated";
				}
			}
		}
	});
	
	this.exit(level, '_showComplete');
}


GalleryCacheAllProvisionalSomeComplete.prototype._redrawCompletelyAt = function(level, curY) {

	this.enter(level, 'redrawCompletelyAt', [ 'curY', curY ]);
	
	var elem = this._findElementPos(level + 1, curY);
	
	this.log(level, 'Element start index: ' + elem.firstItemIndex + ', removing all rows: ' + this.cachedRowDivs.length);

	this.upperPlaceHolder.setAttribute('style', 'height : '+ curY + ';');

	// Remove all row elements since we will just generate them anew after the initial div
	// used for making sure the rows show up at the right virtual y index
	for (var i = 0; i < this.cachedRowDivs.length; ++ i) {
		
		var toRemove = this.cachedRowDivs[i];

		this.log(level, 'Removing element ' + toRemove);

		this._getInnerElement().removeChild(toRemove);
	}

	this.cachedRowDivs = new Array();

	this.firstCachedIndex = elem.firstItemIndex;
	this.firstY = curY;

	this._addProvisionalDivs(level + 1, elem.firstItemIndex, elem.firstRowYPos, this.numColumns, this._getVisibleHeight());
	
	this.exit(level, 'redrawCompletelyAt');
};

GalleryCacheAllProvisionalSomeComplete.prototype._findElementPos = function(level, yPos) {
	
	this.enter(level, 'findElementPos', [ 'yPos', yPos ])

	// Go though heights list until we find the one that intersects with this y pos
	var y = 0;
	
	var elem = null;
	
	for (var i = 0; i < this.widths.length; i += this.numColumns) {
		var numColumns = i + this.numColumns >= this.widths.length
			? this.widths.length - i
			: this.numColumns; 
		
		var nextY = y;
		nextY += this.rowSpacing;
		
		var rowMaxHeight = this._findRowMaxItemHeight(i, numColumns);
		
		nextY += rowMaxHeight;
		
		if (y <= yPos && nextY >= yPos) {
			elem = { 'firstRowYPos' : y, 'firstItemIndex' : i };
		}
		
		y = nextY;
	}

	this.exit(level, 'findElementPos', JSON.stringify(elem));
	
	return elem;
}
