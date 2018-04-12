/**
 * Downloads all provisional data on complete refresh (eg. titles and thumb sizes)
 * then download complete-data (eg. image thumbs) on demand, eg keep 3 pages of images above and below the
 * currently visible pages.
 * 
 * - gallerySizes - interface for computing number of columns, getting sizes etc
 * - galleryModel - data model for downloading provisional and complete items
 * - galleryView - for creating and updating UI elements, this class does not access DOM directly, easier to unit test
 * - initialTotalNumberOfItems - total number of items to be displayed, if known
 * 
 */

function GalleryCacheAllProvisionalSomeComplete(gallerySizes, galleryModel, galleryView, galleryCacheItemsFactory, initialTotalNumberOfItems) {
	GalleryCacheBase.call(this, gallerySizes, galleryModel, galleryView, initialTotalNumberOfItems);

	this.displayState = null;
	
	this.galleryCacheItemsFactory = galleryCacheItemsFactory;
}

GalleryCacheAllProvisionalSomeComplete.prototype = Object.create(GalleryCacheBase.prototype);

//returns approximate complete size of view
GalleryCacheAllProvisionalSomeComplete.prototype.refresh = function(level, totalNumberOfItems) {
	
	this.enter(level, 'refresh', ['totalNumberOfItems', totalNumberOfItems]);
	
	// Remove any added divs
	this._clear(level + 1);

	var t = this;
	
	// Mechanism for downloading complete-data on the fly as user scrolls
	this.cacheItems = this.galleryCacheItemsFactory.createCacheItems(20, function(index, count, onDownloaded) {
		t.galleryModel.getCompleteData(index, count, onDownloaded);
	});
	
	var t = this;

	// Placeholder div at the beginning which we can use set the start
	// of rendered divs without adding all divs from the beginning and down
	// so with a million entries in the virtual data array and visible area in the middle of this,
	// we just adjust height of placeholder to be up to visible area, then add gallery elements to view.
	// As the user scrolls, we update this height and replace the elements to view
	
	if (typeof this.upperPlaceHolder === 'undefined') {
		this.upperPlaceHolder = t.galleryView.createUpperPlaceHolder();
		this.galleryView.appendToContainer(this._getRenderDiv(), this.upperPlaceHolder);
	}

	this.totalNumberOfItems = totalNumberOfItems;

	// Get all provisional data, complete-data is gathered dynamically
	this.galleryModel.getProvisionalData(0, totalNumberOfItems, function(provisionalDataArray) {
		t.provisionalDataArray = provisionalDataArray;

		// completed metadata build, now compute and re-render with provisional data
		t._render(level + 1);
	});
	
	this.exit(level, 'refresh');
}

GalleryCacheAllProvisionalSomeComplete.prototype._render = function(level) {

	// Get the width of element to compute how many elements there are room for
	var numColumns = this._computeNumColumns();
	
	this.numColumns = numColumns;
	
	this.log(level, 'Thumbs per row: ' + numColumns);

	// Have thumbs per row, now compute height
	var height = this.gallerySizes.computeHeightFromNumColumns(this._getTotalNumberOfItems(), numColumns);

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
	
	// Start at index 0 and render on
	var rendered = this._addProvisionalDivs(level + 1, 0, 0, numColumns, visibleHeight);

	if (rendered != null) {
		this.displayState = DisplayState.createFromFields({
			firstVisibleY : 0,
			firstRenderedY : 0, // renders a bit outside of display since adding complete rows
			firstVisibleIndex : 0,
			firstRenderedIndex : 0,
			lastVisibleY : visibleHeight - 1,
			lastRenderedY : rendered.yPos - 1, // renders a bit outside of display since adding complete rows. -1 since we should get last t pos
			lastVisibleIndex : rendered.index,
			lastRenderedIndex : rendered.index
		});

		// Update complete-rendering as well
		this._downloadAndRenderComplete(level, this.displayState);
	}

	this._updateHeightIfApproximation(level + 1, this.displayState);
}

// For unit test checkes
GalleryCacheAllProvisionalSomeComplete.prototype.whiteboxGetDisplayState = function() {
	return this.displayState;
}

/**
 * If we are running with heightHint, we must update height of the display based on lastest last-rendered and index.
 * If we are at last element, this ought to add up to height being yPos of after last rendered.
 * 
 */

GalleryCacheAllProvisionalSomeComplete.prototype._updateHeightIfApproximation = function(level, displayState) {
	
	this.enter(level, '_updateHeightIfApproximation', ['displayState', displayState.toDebugString()]);

	if (this.gallerySizes.getSpecificHeightOrNull() == null) {
		
		// Only height hint, must update remaning height as has not been set accurately
		
		var currentHeight = this._getScrollableHeight();
		var lastRenderedY = displayState.lastRenderedY;
		
		if (currentHeight < lastRenderedY) {
			throw "currentHeight < lastRenderedY";
		}
		
		// Computes height after last-rendered element
		var remainingElements
			= this._getTotalNumberOfItems()
				- displayState.lastVisibleIndex
				- 1; // since last visible is index, so if 3 elements total, last element is index 2

		// Now divide up remaining space on those elements
		var remainingSpace = currentHeight - lastRenderedY;

		var recomputedHeight = this.gallerySizes.computeHeightFromVisible(remainingElements, this._getVisibleWidth());

		this.log(level, 'remainingElements: ' + remainingElements +', remainingSpace: ' + remainingSpace + ', recomputedHeight: ' + recomputedHeight);

		if (remainingSpace != recomputedHeight) {

			var diff = recomputedHeight - remainingSpace;

			// diff might be negative here
			if (diff < 0 && (-diff) > currentHeight) {
				throw new "Diff more than current height: " + (-diff);
			}

			var newHeight = currentHeight + diff;

			this.log(level, 'Computed new height from current ' + currentHeight + ' and diff ' + diff + ': ' + newHeight);

			// Set height to account for diff
			this._setScrollableHeight(newHeight);
		}
	}

	this.exit(level, '_updateHeightIfApproximation');
}


//Update y position within scrollable view, startYPos is offset into beginning of that view for first
//visible line
GalleryCacheAllProvisionalSomeComplete.prototype.updateOnScroll = function(level, yPos) {
	
	this.enter(level, 'updateOnScroll', ['yPos', yPos], ['this.displayState', this.displayState.toDebugString()]);
	
	var lastDisplayState = this.displayState;
	
	// Updates first and last cached item index base on y position
	this.displayState = this._updateOnScroll(level + 1, yPos, this.displayState);
	
	if (lastDisplayState === this.displayState) {
		throw "Expected updated displayState instance to be returned";
	}
	
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
	
	// Only call cache to load items if display has changed
	if (   lastDisplayState == null
		|| lastDisplayState.firstVisibleIndex != this.displayState.firstVisibleIndex
		|| lastDisplayState.lastVisibleIndex != this.displayState.lastVisibleIndex) {
	
		// Update cache view to point to new display area, it will also preload elements around display area
		this._downloadAndRenderComplete(level + 1, this.displayState);
	}

	this._updateHeightIfApproximation(level + 1, this.displayState);

	this.exit(level, 'updateOnScroll');
}

GalleryCacheAllProvisionalSomeComplete.prototype._downloadAndRenderComplete = function(level, displayState) {
	
	this.enter(level, '_downloadAndRenderComplete', ['displayState', displayState.toDebugString()]);

	var visibleCount = displayState.lastVisibleIndex - displayState.firstVisibleIndex + 1;
	
	var t = this;

	// Check whether we already have downloaded and displayed complete-data for whole visible area
	var firstIndex = this.displayState.firstVisibleIndex;

	// Look at displayState to see if all is downloaded, if not we just call to cache to download all
	// since cache maintains its own state of what it has downloaded or not
	// and will not download again something this has been downloaded before.
	// This test is just here to assure that we will not call to cache if we already
	// have added row <div> elements with complete-data (eg. full thumbnail)
	// for all the now visible elements
	var hasRenderedCompleteForAll = true;
	
	for (var i = 0; i < visibleCount; ++ i) {
		var index = firstIndex + i; // virtual array index
		
		if (!this.displayState.hasRenderStateComplete(index)) {
			// Found one item for which we do not have rendered complete-state
			hasRenderedCompleteForAll = false;
			break;
		}
	}
	
	if (hasRenderedCompleteForAll) {
		// Already rendered completely for all (eg. thumbnail, not just title and default image frame)
		// so no need to call on cache
	}
	else {
		// At least one item in visible area was not rendered (and popbaly not downloaded)
		// call onto cache to update and download asynchronously if necessary, we are called back below
		// to update display, showing complete-data (eg. downloaded thumbnail image)
		
		// TODO also add callback for preload data since we would want to precreate divs? Test whether is good enough without
		this.cacheItems.updateVisibleArea(
				level + 1,
				firstIndex,
				visibleCount,
				this.totalNumberOfItems,
				
				function (index, count, downloadedData) {
					
					// Only called when haven't scrolled (eg no other call to updateVisibleArea)
					if (index !== displayState.firstVisibleIndex) {
						throw "Index mismatch: requested=" + displayState.firstVisibleIndex + ", retrieved: " + index;
					}
					if (count !== visibleCount) {
						throw "Count mismatch: " + count + "/" + visibleCount;
					}
					if (downloadedData.length !== visibleCount) {
						throw "Number of items mismatch count, expected " + visibleCount + ", got " + downloadedData.length;
					}
	
					// Can now update rows from data
					t.displayState = t._showCompleteForRows(0, index, count, downloadedData, t.displayState);
				});
	}
	
	this.exit(level, '_downloadAndRenderComplete');
}


// Helper method for update on scroll
GalleryCacheAllProvisionalSomeComplete.prototype._updateOnScroll = function(level, curY, prevDisplayed) {

	this.enter(level, '_updateOnScroll',
			[ 'curY', curY, 'prevDisplayed', prevDisplayed.toDebugString()],
			[ 'displayState', this.displayState.toDebugString(), '_getVisibleHeight()', this._getVisibleHeight() ]);
	
	// See if we have something that was not visible earlier scrolled into view
	var initialUpdate;

	if (prevDisplayed == null) {

		prevDisplayed = DisplayState.createEmptyDisplayState();

		initialUpdate = true;
	}
	else {
		initialUpdate = false;
	}

	// Some checks
	if (prevDisplayed.lastRenderedY < prevDisplayed.lastVisibleY) {
		throw "prevDisplayed.lastRenderedY < prevDisplayed.lastVisibleY : "
			+ prevDisplayed.lastRenderedY + "/" + prevDisplayed.lastVisibleY;
	}

	if (prevDisplayed.firstRenderedY > prevDisplayed.firstVisibleY) {
		throw "prevDisplay.firstRenderedY > prevDisplayed.firstVisibleY";
	}

	var firstRenderedY;
	var lastRenderedY;
	var firstVisibleIndex;
	var lastVisibleIndex;

	var posAndIndex = this._findElementYPosAndItemIndex(level + 1, curY);

	var lastRendered;

	var displayed;

	if (curY + this._getVisibleHeight() < prevDisplayed.firstVisibleY) {
		this.log(level, 'Scrolled to view completely above previous');

		// Check if is within rendered area
		if (curY >= prevDisplayed.firstRenderedY) {
			// Still within what we have rendered (eg. due to preloading)
			// That means we can adjust current Y pos and return
			displayed = this._updatedDisplayAreaAndVisibleIndices(curY, prevDisplayed);
		}
		else {
			var heightFromCurYToFirstRendered = curY - prevDisplayed.firstRenderedY;
	
			// We are scrolling upwards totally out of current area
			lastRendered = this._redrawCompletelyAt(level + 1, curY, posAndIndex);
	
			displayed = this._addCurYToDisplayState(level + 1, curY, prevDisplayed, {
				firstRenderedY		: posAndIndex.rowYPos,
				lastRenderedY		: lastRendered.yPos - 1,
				firstVisibleIndex	: posAndIndex.rowItemIndex,
				lastVisibleIndex	: lastRendered.index,
				firstRenderedIndex	: posAndIndex.rowItemIndex,
				lastRenderedIndex	: lastRendered.index
			});
		}
	}
	else if (curY < prevDisplayed.firstVisibleY) {
		// Scrolling partly above visible area
		var heightFromCurYToFirstRendered = curY - prevDisplayed.firstRenderedY;
		var visibleHeight = this._getVisibleHeight();

		this.log(level, 'Scrolled to view partly above previous, must add ? Height from first rendered to curY ' + heightFromCurYToFirstRendered + ', visible ' + visibleHeight);

		if (heightFromCurYToFirstRendered > visibleHeight) {
			// Already rendered, so no need to add rows upwards
			displayed = this._updatedDisplayAreaAndVisibleIndices(curY, prevDisplayed);
		}
		else {
			var heightToAdd = prevDisplayed.firstVisibleY - curY;

			// Must add items before this one, so must be prepended to the divs already shown
			lastRendered = this._prependDivs(level + 1, prevDisplayed.firstVisibleIndex - 1, prevDisplayed.firstVisibleY, this.numColumns, heightToAdd);
	
			firstRenderedY = lastRendered.rowYPos;
			firstVisibleIndex = lastRendered.rowItemIndex;
	
			throw "TODO figure out last rendered after prepending"
			lastRenderedY = lastRendered.yPos;
			lastVisibleIndex = lastRendered.index;
		}
	}
	else if (curY > prevDisplayed.lastVisibleY) {
		// We are scrolling downwards totally out of visible area, just add items for the pos in question
		this.log(level, 'Scrolled to completely below previous curY ' + curY +
				' visibleHeight ' + this._getVisibleHeight() + ' > lastVisibleY ' + prevDisplayed.lastVisibleY +
				', posAndIndex=' + JSON.stringify(posAndIndex));

		// Check if also below last rendered
		var visibleHeight = this._getVisibleHeight();
		
		if (curY + visibleHeight <= prevDisplayed.lastRenderedY) {
			// Still within rendered area
			displayed = this._updatedDisplayAreaAndVisibleIndices(curY, prevDisplayed);
		}
		else {
		
			lastRendered = this._redrawCompletelyAt(level + 1, curY, posAndIndex);
			
			displayed = this._addCurYToDisplayState(level + 1, curY, prevDisplayed, {
				firstRenderedY		: posAndIndex.rowYPos,
				lastRenderedY		: lastRendered.yPos - 1,
				firstVisibleIndex	: posAndIndex.rowItemIndex,
				lastVisibleIndex	: lastRendered.index,
				firstRenderedIndex	: posAndIndex.rowItemIndex,
				lastRenderedIndex	: lastRendered.index
			});
		}
	}
	else if (curY > prevDisplayed.firstVisibleY) { // Scrolled downwards but not completely out of visible area, since that was tested on above
		// Scrolling down partly out of visible area
		// First figure out how much visible space that must be added
		// + 1 because lastVisibleY is within visibleHeight. Eg after initial rendering of height 100 then lastVisibleY is 99
		
		var heightFromCurYToLastRendered = prevDisplayed.lastRenderedY + 1 - curY;
		var visibleHeight = this._getVisibleHeight();

		this.log(level, 'Scrolled to view partly below previous, must add ? Height from curY to last rendered ' + heightFromCurYToLastRendered +
				', visible ' + visibleHeight);

		if (visibleHeight < heightFromCurYToLastRendered) {
			// We have added rows outside new visible-area so we do not have to add any rows
			displayed = this._updatedDisplayAreaAndVisibleIndices(curY, prevDisplayed);
		}
		else {
			var heightToAdd = visibleHeight - heightFromCurYToLastRendered;

			this.log(level, 'Scrolled to view partly below previous, must add ' + heightToAdd);
	
			// Do we need to add one or more rows? Should do so without removing existing rows,
			// just add new ones below current ones.
	
			// Start-index to add is the one immediately after last-index
			// unless this is initial update, in which case we should update from index 0 (startIndex would be 1 if not testing for this)
			var startIndex = initialUpdate ? 0 : prevDisplayed.lastRenderedIndex + 1;
	
			// TODO we must look at lastRenderedIndex here to see if items already added, if so there is no need to add
	
			var startYPos = prevDisplayed.lastRenderedY + 1;
			
			lastRendered = this._addProvisionalDivs(level + 1, startIndex, startYPos, this.numColumns, heightToAdd);
	
			if (lastRendered == null) {
				// Nothing was rendered, ie. did not scroll any new items into display
				// so just return old values
				this.log('No rows added so keeping same displayState');
				displayed = this._sameDisplayStateWithUpdatedDisplayArea(curY, prevDisplayed);
			}
			else {
				// Scrolled downwards a bit, update based on downwards scroll
				this.log('Rows added below so updating displayState');

				displayed = this._addCurYToDisplayState(level + 1, curY, prevDisplayed, {
					firstRenderedY		: prevDisplayed.firstRenderedY, // until we remove some items at the from of list when scrolling downwards
					lastRenderedY		: lastRendered.yPos - 1,

					firstVisibleIndex	: posAndIndex.rowItemIndex, // computed from curY above
					lastVisibleIndex	: lastRendered.index, // last visible is the same as rendered index if we got here, since we had to add divs

					firstRenderedIndex 	: prevDisplayed.firstRenderedIndex, // until we remove some items at the from of list when scrolling downwards
					lastRenderedIndex	: lastRendered.index
					// TODO this is not always correct since we might be rendered preloaded?
	 			});
			}
		}
	}
	else if (curY === prevDisplayed.firstVisibleY) {
		// Scroll called without any change in coordinates
		displayed = this._sameDisplayStateWithUpdatedDisplayArea(curY, prevDisplayed);
	}
	else {
		console.log('## _updateOnScroll: Did not match any test');

		displayed = this._sameDisplayStateWithUpdatedDisplayArea(curY, prevDisplayed);
	}

	/*
	var displayed = {
		firstVisibleY : curY,
		firstRenderedY : firstRenderedY,
		firstVisibleIndex : firstVisibleIndex,
		lastVisibleY : curY + this._getVisibleHeight() - 1,
		lastRenderedY : lastRenderedY,
		lastVisibleIndex : lastVisibleIndex
	};
	*/

	this.exit(level, '_updateOnScroll', '' + displayed.toDebugString());
	
	return displayed;
};

GalleryCacheAllProvisionalSomeComplete.prototype._addCurYToDisplayState = function(level, curY, prevDisplayed, displayStateFields) {

	var displayState = prevDisplayed.addCurYToDisplayState(
			level, // same level since not printing debug for this one, just delegating
			curY,
			this._getVisibleHeight(),
			displayStateFields);

	return displayState;
}

GalleryCacheAllProvisionalSomeComplete.prototype._sameDisplayStateWithUpdatedDisplayArea = function(curY, prevDisplayed) {
	return prevDisplayed.sameWithUpdatedDisplayArea(curY, this._getVisibleHeight());
}

GalleryCacheAllProvisionalSomeComplete.prototype._updatedDisplayAreaAndVisibleIndices = function(curY, prevDisplayed) {

	// First just adjust for curY, ie firstVisibleY and lastVisibleY
	var updated = this._sameDisplayStateWithUpdatedDisplayArea(curY, prevDisplayed);

	// Then find indices of elements
	var firstVisibleIndex = this._findElementYPosAndItemIndex(0, updated.firstVisibleY).rowItemIndex;
	
	var lastPos = this._findElementYPosAndItemIndex(0, updated.lastVisibleY);
	
	var indexOfLastOnRow = this._computeIndexOfLastOnRowStartingWithIndex(lastPos.rowItemIndex);

	var lastVisibleIndex = indexOfLastOnRow;

	return updated.sameWithUpdatedVisibleIndices(firstVisibleIndex, lastVisibleIndex);
}

/**
 * Download and show complete items in a certain model range, provisional items
 * for this range must have been downloaded already.
 * 
 *  - level - debug log indentation level
 *  - firstModelItemIndex - index into model virtual array where to start
 *  - itemCount number of items to update
 */

GalleryCacheAllProvisionalSomeComplete.prototype._getRowItemDivHeights = function(rowDiv) {
	var itemsThisRow = this.galleryView.getNumElements(rowDiv);
	// Store new elements in array and then replace all at once
	var rowWidthHeights = [];

	for (var j = 0; j < itemsThisRow; ++ j) {

		var itemElement = this.galleryView.getElement(rowDiv, j);

		var div;

		var numElements = this.galleryView.getNumElements(itemElement);
		if (numElements === 2) {
			// Hack to get displayable text for element
			// div with title text is last element
			
			div = this.galleryView.getElement(itemElement, 1);
		}
	
		
		var html = typeof div === 'undefined' ? '<undefined>' : div.innerHTML;

		rowWidthHeights.push({
				width 	: this.galleryView.getElementWidth(itemElement),
				height 	: this.galleryView.getElementHeight(itemElement),
				html 	: html
		});
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
GalleryCacheAllProvisionalSomeComplete.prototype._showCompleteForRows = function(level, firstModelItemIndex, itemCount, completeDataArray, prevDisplayed) {
	
	this.enter(level, '_showCompleteForRows', [
		'firstModelItemIndex', firstModelItemIndex,
		'itemCount', itemCount,
		'completeDataArray', completeDataArray.length,
		'prevDisplayed', prevDisplayed.toDebugString()
	]);

	if (completeDataArray.length !== itemCount) {
		throw "Expected itemCount entries";
	}

	var rowWidth = this._getRowWidth();
	var numRows = this.cachedRowDivs.length;
	var numRowsTotal = this._computeNumRowsTotal();
	
	for (var row = 0, i = firstModelItemIndex; row < numRows && i < itemCount; ++ row) {

		var rowDiv = this.cachedRowDivs[row];
		
		var itemsThisRow = this.galleryView.getNumElements(rowDiv);


		// Store new elements in array and then replace all at once
		var rowWidthHeights = this._getRowItemDivHeights(rowDiv);

		var t = this;

		// Replace row items, even if says _addRowItems() it does replace items
		this._addRowItems(level + 1, rowDiv, i, itemsThisRow, numRowsTotal, rowWidth,
				function (index, itemWidth, itemHeight) {

					var completeData = completeDataArray[index - firstModelItemIndex];
					
					var item;

					if (completeData == null) {
						item = this.galleryView.getElement(rowDiv, index - i);
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
					t.galleryView.replaceElement(rowDiv, rowIndex, element);
				});

		
		var updatedRowWidthHeights = this._getRowItemDivHeights(rowDiv);

		for (var j = 0; j < itemsThisRow; ++ j) {
			var prevDim = rowWidthHeights[j];
			var curDim  = updatedRowWidthHeights[j];
	
			if (prevDim.width !== curDim.width || prevDim.height !== curDim.height) {

				var itemIndex = i + j;

				var provisionalData = this.provisionalDataArray[itemIndex];
				var completeData = this.cacheItems._debugGetCachedDataAtIndex(itemIndex);

				console.log("## Gallery item dimensions changed between provisional and updated for " + itemIndex + ", row " + j
					+ " : prev=" + JSON.stringify(prevDim) + ", cur=" + JSON.stringify(curDim) + ", provisional data " + JSON.stringify(provisionalData)
					);
				throw "throw exception"
			}
		}

		i += itemsThisRow;
	}

	var updatedDisplayState = prevDisplayed.setRenderStateComplete(firstModelItemIndex, itemCount);

	this.exit(level, '_showCompleteForRows', updatedDisplayState.toDebugString());
	
	return updatedDisplayState;
}

GalleryCacheAllProvisionalSomeComplete.prototype._redrawCompletelyAt = function(level, curY, posAndIndex) {

	this.enter(level, 'redrawCompletelyAt', [ 'curY', curY ]);

	this.log(level, 'Element start index: ' + posAndIndex.rowItemIndex + ', removing all rows: ' + this.cachedRowDivs.length);

	this.galleryView.setElementHeight(this.upperPlaceHolder, curY);

	// Remove all row elements since we will just generate them anew after the initial div
	// used for making sure the rows show up at the right virtual y index
	for (var i = 0; i < this.cachedRowDivs.length; ++ i) {
		
		var toRemove = this.cachedRowDivs[i];

		this.log(level, 'Removing element ' + toRemove);

		this.galleryView.removeElement(this._getRenderDiv(), toRemove);
	}

	this.cachedRowDivs = new Array();

	this.firstCachedIndex = posAndIndex.rowItemIndex;

	var lastRendered = this._addProvisionalDivs(level + 1, posAndIndex.rowItemIndex, posAndIndex.rowYPos, this.numColumns, this._getVisibleHeight());
	
	this.exit(level, 'redrawCompletelyAt', JSON.stringify(lastRendered));
	
	return lastRendered;
};


GalleryCacheAllProvisionalSomeComplete.prototype._findElementYPosAndItemIndex = function(level, yPos) {
	
	this.enter(level, '_findElementYPosAndItemIndex', [ 'yPos', yPos ])

	// Go though heights list until we find the one that intersects with this y pos
	var y = 0;

	var elem = null;
	
	// TODO this is not entirely correct since rows might be separate heights

	// We do not know item heights, only an approximation in case of heightHint
	// so just figure out by multiplying
	var heightOfOneElement = this.gallerySizes.getHeightOfOneElement();
	
	var numColumns = this._computeNumColumns();
	
	// Now we have can divide to find start
	var row = Math.floor(yPos / heightOfOneElement);
	
	var itemIndex = row * numColumns;
	
	var rowYPos = heightOfOneElement * row;
	
	elem = { 'rowYPos' : rowYPos, 'rowItemIndex' : itemIndex, 'row' : row };

	/*
	var y = yPos

	for (var i = 0; i < this.totalNumberOfItems; i += this.numColumns) {
		var itemsThisRow = i + this.numColumns >= this.totalNumberOfItems
			? this.totalNumberOfItems - i
			: this.numColumns; 
		
		var nextY = y;
		nextY += this._getRowSpacing();
		
		var rowMaxHeight = this._findRowMaxItemHeight(i, itemsThisRow);
		
		nextY += rowMaxHeight;
		
		if (y <= yPos && nextY >= yPos) {
			elem = { 'rowYPos' : y, 'rowItemIndex' : i };
		}
		
		y = nextY;
	}
	*/

	this.exit(level, '_findElementYPosAndItemIndex', JSON.stringify(elem));
	
	return elem;
}
