/**
 * Gallery of unlimited size that renders from virtual REST services.
 * Renders any kind of content, does not have to be images. Followes MVC model, fallery only handles triggering download of DOM elements (from user specified Model)
 * as user scrolls, calls user code (user specified View) to craete DOM elements and then gallery adds to appropriate place in gallery.
 * 
 * Summary of features:
 *  - unlimited scroll
 *  - handles both setting all items being hardcoded to same size and variable-sized items (requires width-hint for computing number of columns and height hint for approximation of
 *    total virtual height of gallery)
 *  - for small numbers of data, might download complete model so that can compute number of columns and scrollable height completely correct (so that scrolling behaves as expected).
 * 
 * 
 * Gallery has two phases in displaying data while scrolling,
 *  - provisional - just shows quickly-downloadable data, eg. low-bandwidth data. For a typical image gallery, this could be captions and thumbnail sizes so that it could show caption
 *                  and a frame with some default image until thumbnails has been downloaded
 *  - complete - downloads and shows complete galery item, eg. for a imagae gallery, also downloads and shows thumbnail.
 *  
 *  Depending on the number of items in the gallery it might:
 *   - for a small gallery, just download all content for both phases and keep in memory
 *   - for a medium gallery, download all provisional information and cache it, download complete information as the user scrolls to a new part of gallery.
 *   - for a really large gallery, download both provisional and complete content while user scrolls. These can be run in parallel, provisional information aught to return faster. 
 *   
 *   
 *  Gallery has two ways to specify item size, hint and exact.
 *  Width hint, gallery will use this for approximation.
 *  Eg heightHint will allow it to compute approximate total size of gallery (scrollable) area.
 *  
 *  !! NOTE !! heightHint also has the effect of creating rows of different height, eg each row is as tall as the tallest element on this row.
 *  Since heightHint is meant as an approximation, gallery must just use the height of every element on *that row* to figure out row height for *that row*.
 *  It cannot look at height of all rows because it might not have that complete information available at any point in time, that is for galleries with too many elements to
 *  have them all downloaded at the same time (for computing a common row height, max size for all items). The gallery might for large data sets, only keep in memory (and constructed as DOM elements), only this elements that are visible,
 *  perhaps also some nearby ones so that if the user does slow scrolling (eg. with keyboard arrow keys), those DOM elements are ready to be scrolled into display.
 *  However if user specifies and absolute height, this will be the height of rows (+ spacing between rows).
 * 
 * 
 * API
 * 
 * Constructor
 * ===========
 * Gallery(divId, config, galleryModel, galleryView)
 * 
 * divId - ID of element that will be root
 * config - rendering configuration
 * galleryModel - user implementation of gallery model
 * galleryView - user implementation of gallery view
 * 
 * config above is a JS object with properties as follows.
 * One of width and widthHint must be specified.
 * One of height and heightHint must be specified.
 * 
 * columnSpacing - horizontal spacing between items
 * rowSpacing - vertical spacing between items
 * width - items will have exactly this width, no matter size of content. If content execeeds this width, parts will be hidden (or perhaps scrollbars added to the singleitem).
 * widthHint - approximate average width, gallery will use this hint to compute number of columns. If items in a row execceds place allocated (eg. all items are wider than widthHint),
 *             then content may be hidden or the horizontal scrollbars are added to the gallery while that row is visible (overflow : scroll)
 * height - items will have exactly this height, overflowing content is hidden (or perhaps scrollbars added to the single item)
 * heightHint - approximate average height,  gallery will use this hint to compute height of virtual view in pixels so that scrollbars reflect the virtual (scrollable) size of the gallery.
 * 				Approximation ought to be fine for large number of items since scrollbar is quite small anyways. For small numbers of items (eg. 2-three times visible area),
 * 				gallery might just render all elements in order to have scrollbar size correctly reflect number of items (eg. scrollable area is set to correct height).
 * 
 * Refresh
 * =======
 * 
 * Refresh gallery from new data (eg. after change of search criteria for which images to show, or changing sort order)
 * 
 * .refresh(totalNumberOfItems)
 *   - totalNumberOfItems - total number of items that will be displayed, so gallery known what indices to iterate over
 * 
 */

/**
 * View functions:
 * 
 * Try to figure height of non-image parts of element?
 * 
 * Make DOM element to be shown while images are being loaded from server
 * 
 * makeProvisionalHTMLElement(index, provisionalData, itemWidth, itemHeight)
 * 
 *  - index - index in virtual array of element to show
 *  - provisionalData - provisional data for element, user specific
 *  - itemWidth - width of item display area
 *  - itemHeight - height of item display area
 *  
 * return provisional HTML element to shown (eg. a div element)
 *  
 *  
 * Make the DOM element to show after complete data has been loaded from server.
 * 
 * makeCompleteHTMLElement(index, provisionalData, completeData, itemWidth, itemHeight)
 *
 *  - index - index in virtual array of element to show
 *  - provisionalData - provisional data for element, user specific
 *  - completeData - complete data for element, user specific
 *  - itemWidth - width of item display area
 *  - itemHeight - height of item display area
 *  
 * return - new HTML element or just return null if could not be updated and one ought to display the provisional one
 * 
 */

/**
 * Model functions
 *
 * Get provisional data asynchronously (suitable for Ajax calls)
 * getProvisionalData(index, count, onsuccess)
 *  - index - index into virtual array displayed (shown left-to-right, top-to-bottom)
 *  - count - number of items to get images for, starting at index
 *  - onsuccess - function to be called back with an array of elements that represents downloaded data. Array elements is user specific and will be passed to view.
 *                Array must be <count> length
 * 
 * Get complete data asynchronously (suitable for Ajax calls)
 * getCompleteData(index, count, onsuccess) 
 *  - firstIndex - index into virtual array of images
 *  - count - number of items to get images for, starting at firstIndex
 *  - onsuccess - function to be called back with an array of elements that represents the complete data (user specific), must be <count> length.
 */

function Gallery(divId, config, galleryModel, galleryView) {
	
	this.divId = divId;
	
	this.config = config;

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

	this.galleryModel = galleryModel;
	this.galleryView = galleryView;
	this.width = 800;

	this.rowDivs = new Array();

	this.firstIndex = 0; // index of first visible element
	this.firstY = 0; // y position in virtual fiv of first visible element

	// Store functions for later
	var outerDiv = document.getElementById(divId);
	outerDiv.setAttribute('style', 'overflow:scroll');

	// Create inner scrollable area and add it to outer div
	this.innerDiv = document.createElement('div');
	document.getElementById(divId).append(this.innerDiv);

	this.upperPlaceHolder = document.createElement('div');
	
	this.innerDiv.append(this.upperPlaceHolder);

	if (typeof config.width !== 'undefined') {
		this.widthMode = new GalleryModeWidthSpecific();
	}
	else if (typeof config.widthHint !== 'undefined') {
		this.widthMode = new GalleryModeWidthHint();
	}
	else {
		throw "Neither width nor width hint specified in config, specify one of them";
	}

	if (typeof config.height !== 'undefined') {
		this.heightMode = new GalleryModeHeightSpecific();
	}
	else if (typeof config.heightHint !== 'undefined') {
		this.heightMode = new GalleryModeHeightHint();
	}
	else {
		throw "Neither height nor height hint specified in config, specify one of them";
	}

	//document.getElementById(divId).append(innerDiv);
	
	/**
	 * refresh with passing in function for getting titles and thumb sizes
	 */
	this.refresh = function(totalNumberOfItems) {

		var level = 0;
		
		this.enter(level, 'refresh', ['totalNumberOfItems', totalNumberOfItems]);

		this.totalNumberOfItems = totalNumberOfItems;

		var t = this;

		// get all information and update view accordingly
		// TODO for really large galleries, just get parts
		galleryModel.getProvisionalData(0, totalNumberOfItems, function(provisionalDataArray) {
			t.provisionalDataArray = provisionalDataArray;
			// completed metadata build, now compute and rerender
			t._computeAndRender(level + 1);
		});

		this.exit(level, 'refresh');
	};

	this._getVisibleWidth = function() {
		return this._getInnerElement().clientWidth;
	};

	this._getVisibleHeight = function() {
		return this._getInnerElement().clientHeight;
	};

	this._computeAndRender = function (level) {
		
		this.enter(level, 'computeAndRender', []);

		// Get the width of element to compute how many elements there are room for
		var itemsPerRow = this.widthMode.computeNumColumns(this.config, this.columnSpacing, this._getVisibleWidth());
		
		this.itemsPerRow = itemsPerRow;
		
		this.log(level, 'Thumbs per row: ' + itemsPerRow);
		
		// Have thumbs per row, now compute height
		var height = this._computeHeight(itemsPerRow);
		
		this.height = height;
		
		this.log(level, 'Height: ' + height);
		
		// Must set element height
		// TODO use jQuery?
		var outerDiv = this._getOuterElement();
		var innerDiv = this._getInnerElement();

		outerDiv.style.width = this.width;
		outerDiv.style.height = '100%';
		outerDiv.style.overflow = 'auto';
		outerDiv.style['background-color'] = 'blue';
		
		innerDiv.style.width = this.width;
		innerDiv.style.height = '100%';
		innerDiv.style.display = 'block';
		
		var t = this;

		// We can now render within the visible area by adding divs and displaying them as we scroll
		// at a relative position to the display area
		
		// or use a canvas, but that would require backing area for the gallery, so rather just use number of divs for which we update relative area

		// Compute the starting point of every element
		
		// What is the current start offset of scrolling?
		
		// Set the offset of each element to that, but what about sizes? Once we scroll an element out, we must add a new one
		
		// Start at the current ones
		this._addDivs(level + 1, 0, 0, itemsPerRow, this._getVisibleHeight());
		
		// Add scroll listener
		this._getOuterElement().addEventListener('scroll', function(e) {
			// figure out how far we have scrolled into the div
			var clientRects = innerDiv.getBoundingClientRect(); // innerDiv.getClientRects()[0];
			var viewYPos = - (clientRects.top - innerDiv.offsetTop);
			
			t._updateOnScroll(viewYPos);
			
			var curFirstY = t.firstY;
			
			if (!t.scrollTimeoutSet) { // avoid having multiple timeouts
			
				t.scrollTimeoutSet = true;
				
				setTimeout(function() {
						t._getImagesIfNotScrolled(level + 1, curFirstY, t.firstY, t.firstIndex, t.lastIndex - t.firstIndex + 1);
						t.scrollTimeoutSet = false;
					},
					100);
			}
			
		});

		this.exit(level, 'computeAndRender');
	}
	
	this._getImagesIfNotScrolled = function(level, timeoutStartY, curY, firstIndex, count) {
		
		this.enter(level, '_getImagesIfNotScrolled', [ 'timeoutStartY', timeoutStartY, 'curY', curY, 'firstIndex', firstIndex, 'count', count]);

		if (timeoutStartY == curY) {
			
			// Not scrolled since timeout started, load images

			var t = this;
			
			// Call external functions to load images
			this.galleryModel.getCompleteData(firstIndex, count, function(completeDataArray) {
				
				var rowNo = firstIndex / t.itemsPerRow;

				var rowWidth = t._getRowWidth();
				var numRows = t.rowDivs.length;
				var numRowsTotal = ((t._getTotalNumberOfItems() - 1) / t.itemsPerRow) + 1;

				for (var row = 0, i = firstIndex; row < numRows && i < count; ++ row) {
					
					var rowDiv = t.rowDivs[row];
					var itemsThisRow = rowDiv.childNodes.length;
					
					// Store new elements in array and then replace all at once
					//var newRowItems = [];
					
					t._addRowItems(level + 1, rowDiv, i, itemsThisRow, numRowsTotal, rowWidth,
							function (index, provisionalData, itemWidth, itemHeight) {
						
								var completeData = completeDataArray[index];
								var item;
								
								if (completeData == null) {
									item = rowDiv.childNodes[index - i];
								}
								else if (typeof completeData === 'undefined') {
									throw "Image data undefined at: " + index;
								}
								else {
									item = t.galleryView.makeCompleteHTMLElement(provisionalData, completeData);
								}
								
								return item;
							},
							function (element, rowIndex) {
								rowDiv.replaceChild(element, rowDiv.childNodes[rowIndex]);

	//							newRowItems.push(element);
							});
					
					i += itemsThisRow;
			/*
					for (var c = 0; c < itemsThisRow && i < count; ++ c) {
						
						var rowItem = rowDiv.childNodes[c];
						
						if (typeof rowItem === 'undefined' || rowItem == null) {
							throw "No row item";
						}

						// Replace element
						rowDiv.replaceChild(newRowItems[c], rowItem)
						
						++ i;
					}
				*/
				}
			});
		}
		
		this.exit(level, '_getImagesIfNotScrolled');
	}
	
	this._updateOnScroll = function(curY) {
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
			this._prependDivs(level + 1, this.firstIndex - 1, this.firstY, this.itemsPerRow, heightToAdd);
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
			this._addDivs(level + 1, this.lastIndex + 1, this.lastY, this.itemsPerRow, heightToAdd);
			
			// Do not update this.firstIndex or this.firstY since we are appending
			// TODO perhaps remove rows that have scrolled out of sight
		}
		
		this.exit(level, 'updateOnScroll');
	};
	
	this._redrawCompletelyAt = function(level, curY) {

		this.enter(level, 'redrawCompletelyAt', [ 'curY', curY ]);
		
		var elem = this._findElementPos(level + 1, curY);
		
		this.log(level, 'Element start index: ' + elem.firstItemIndex + ', removing all rows: ' + this.rowDivs.length);

		this.upperPlaceHolder.setAttribute('style', 'height : '+ curY + ';');

		// Remove all row elements since we will just generate them anew after the initial div
		// used for making sure the rows show up at the right virtual y index
		for (var i = 0; i < this.rowDivs.length; ++ i) {
			
			var toRemove = this.rowDivs[i];

			this.log(level, 'Removing element ' + toRemove);

			this._getInnerElement().removeChild(toRemove);
		}

		this.rowDivs = new Array();

		this.firstIndex = elem.firstItemIndex;
		this.firstY = curY;

		this._addDivs(level + 1, elem.firstItemIndex, elem.firstRowYPos, this.itemsPerRow, this._getVisibleHeight());
		
		this.exit(level, 'redrawCompletelyAt');
	};
	
	this._findElementPos = function(level, yPos) {
		
		this.enter(level, 'findElementPos', [ 'yPos', yPos ])

		// Go though heights list until we find the one that intersects with this y pos
		var y = 0;
		
		var elem = null;
		
		for (var i = 0; i < this.widths.length; i += this.itemsPerRow) {
			var itemsPerRow = i + this.itemsPerRow >= this.widths.length
				? this.widths.length - i
				: this.itemsPerRow; 
			
			var nextY = y;
			nextY += this.rowSpacing;
			
			var rowMaxHeight = this._findRowMaxItemHeight(i, itemsPerRow);
			
			nextY += rowMaxHeight;
			
			if (y <= yPos && nextY >= yPos) {
				elem = { 'firstRowYPos' : y, 'firstItemIndex' : i };
			}
			
			y = nextY;
		}

		this.exit(level, 'findElementPos', JSON.stringify(elem));
		
		return elem;
	}
	
	/**
	 * Adding divs before the currently visible ones
	 * 
	 * startIndex - element to start adding upwards
	 * startPos - y position of where to start adding, eg bottom border of where to start add
	 * itemsPerRow - items in a row
	 * heightToAdd - height of items to add
	 * 
	 */
	this._prependDivs = function(level, startIndex, startPos, itemsPerRow, heightToAdd) {
		
		this.enter(level, 'prependDivs', [ 'startIndex', startIndex, 'startPos', startPos, 'itemsPerRow', itemsPerRow, 'heightToAdd', heightToAdd ])
		
		// Reuse _addDivs by computing the height of rows we must add and then add from there 
		var t = this;
		
		var startIndexPositionOnRow = startIndex % itemsPerRow;
		
		if (startIndexPositionOnRow != itemsPerRow - 1) {
			throw "startIndex should be last item on a row: idx " + startIndexPositionOnRow + " / itemsPerRow " + itemsPerRow;
		}
		
		var rowItem = null;

		var firstItemOnRow = startIndex - itemsPerRow + 1;
		
		if (firstItemOnRow % itemsPerRow != 0) {
			throw "Expected to be first item";
		}

		// item to add before
		var firstRowDiv = this.rowDivs.length == 0
				? null
				: this.rowDivs[0];

		var lastRendered = this._addDivsWithAddFunc(level + 1, firstItemOnRow, startPos, itemsPerRow, heightToAdd, false, function (rowDiv) {
			
			if (firstRowDiv == null) {
				t.rowDivs.push(rowDiv);
				t.innerDiv.append(rowDiv);
			}
			else {
				t.rowDivs.splice(0, 0, rowDiv); // insert at beginning of row
				t.innerDiv.insertBefore(rowDiv, firstRowDiv);
			}
		});

		this.firstIndex = lastRendered.index; // last drawn element
		this.firstY = lastRendered.yPos;

		this.exit(level, 'prependDivs');
	};
	
	/**
	 * Add divs for current pos
	 * 
	 * startIndex - index of element to start at
	 * startPos - the y position we should start rendering element at
	 * itemsPerRow - the number of items that are rendered per row
	 * visibleHeight - the height of visible area we shall update,
	 *                 which might be less than the real visible area if we are just adding items to a partly updated area
	 */
	this._addDivs = function(level, startIndex, startPos, itemsPerRow, heightToAdd) {
		
		this.enter(level, 'addDivs', [ 'level',  level, 'startIndex', startIndex, 'startPos', startPos, 'itemsPerRow', itemsPerRow, 'heightToAdd', heightToAdd]);
	
		var t = this;

		var lastRendered = this._addDivsWithAddFunc(level + 1, startIndex, startPos, itemsPerRow, heightToAdd, true, function (rowDiv) {
			t.rowDivs.push(rowDiv);
			t.innerDiv.append(rowDiv);
		});
		
		this.lastIndex = lastRendered.index; // last drawn element
		this.lastY = lastRendered.yPos;

		this.log(level, 'firstY set to ' + this.firstY + ' and not changed, set lastIndex to ' + lastRendered.index + ',lastY to ' + lastRendered.yPos);

		this.exit(level, 'addDivs');
	}
	
	this._getRowWidth = function() {
		return this.width;
	}

	
	this._addDivsWithAddFunc = function(level, startIndex, startPos, itemsPerRow, heightToAdd, downwards, addRowDiv) {

		this.enter(level, 'addDivsWithAddFunc', [
			'startIndex', startIndex,
			'startPos', startPos,
			'itemsPerRow', itemsPerRow,
			'heightToAdd', heightToAdd,
			'downwards', downwards ]);
		
		var rowsAdded = 0;
		var heightAdded = 0;
		
		// Add divs until there is not more room in display or there are no more items
		
		
		var y = startPos;
		
		var numRows = ((this._getTotalNumberOfItems() - 1) / itemsPerRow) + 1;
		var rowNo = startIndex / itemsPerRow;

		var rowWidth = this._getRowWidth();

		var lastRenderedElement = null;
		
		for (var i = startIndex; i < this._getTotalNumberOfItems(); i += (downwards ? itemsPerRow : -itemsPerRow)) {

			// Last row might not have a full number of items
			var itemsThisRow = i + itemsPerRow >= this._getTotalNumberOfItems()
				? this.widths.length - i
				: itemsPerRow; 
			
			var rowDiv = document.createElement('div');
			
			rowDiv.setAttribute('class', 'gallery_row');

			// Add before adding elements so that we can add hidden row items and compute their size
			addRowDiv(rowDiv);

			rowNo = rowNo + (downwards ? 1 : -1);

			var t = this;

			// Add row items to the row
			var rowHeight = this._addRowItems(level + 1, rowDiv, i, itemsThisRow, numRows, rowWidth,
					function (index, provisionalData, itemWidth, itemHeight) {
						return t.galleryView.makeProvisionalHTMLElement(index, provisionalData, itemWidth, itemHeight);
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

			if (heightAdded >= heightToAdd) {

				lastRenderedElement = { 'index' : i + itemsThisRow - 1, 'yPos' :  y };
				break;
			}
		}

		this.log(level, 'addDivsWithAddFunc added ' + rowsAdded + ' rows');

		this.exit(level, 'addDivsWithAddFunc', lastRenderedElement);

		return lastRenderedElement;
	}
	
	
	this._computeColumnSpacing = function(rowWidth, totalRowItemWidths, itemsThisRow) {
		var spacing = (rowWidth - totalRowItemWidths) / (itemsThisRow + 1);

		return spacing;
	}
	
	this._getRowHeight = function(rowMaxHeight, rowNo, numRows) {
		var rowHeigth;

		if (rowNo == 0 || rowNo == numRows - 1) {
			rowHeight = rowMaxHeight + this.rowSpacing + (this.rowSpacing / 2);
		}
		else {
			rowHeight = rowMaxHeight + this.rowSpacing;
		}

		return rowHeight;
	}

	/**
	 * Helper to add the items in one gallery row
	 */
	this._addRowItems = function(level, rowDiv, firstItemIndex, itemsThisRow, numRowsTotal, rowWidth, makeElement, addElement) {

		var itemWidth = null;
		var itemHeight = null;
		
		if (typeof this.config.width !== 'undefined') {
			itemWidth = this.config.width;
		}
		
		if (typeof config.height !== 'undefined') {
			itemHeight = config.height;
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
			var index = firstItemIndex + j;

			var itemElement = makeElement(index, this.provisionalDataArray[index], itemWidth, itemHeight);

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
	
	this._applyItemStyles = function(itemElement, rowHeight, itemWidth, itemHeight, spacing, visible) {
		
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
	
	this._getTotalNumberOfItems = function() {
		// TODO do not download complete data array
		return this.totalNumberOfItems;
	}

	
	this._computeHeight = function(itemsPerRow) {
		return this.heightMode.computeHeight(this.config, this.rowSpacing, itemsPerRow, this._getTotalNumberOfItems());
	}
	
	this._getOuterElement = function() {
		return document.getElementById(this.divId);
	}

	this._getInnerElement = function() {
		return this.innerDiv;
	}

	this.enter = function(level, functionName, args) {
		var argsString;
		
		if (typeof args === 'undefined') {
			argsString = '';
		}
		else {
			argsString = '';
		
			if (args.length % 2 != 0) {
				throw 'Number of arguments to ' + functionName + ' not an even number: ' + args.length;
			}
			
			for (var i = 0; i < args.length; i += 2) {
				if (i > 0) {
					argsString += ', ';
				}

				argsString += args[i] + '=' + args[i + 1];
			}
		}
		
		
		this._log(level, 'ENTER ' + functionName + '(' + argsString + ')');
	};
	
	
	this.exit = function(level, functionName, returnValue) {
		
		var returnString = typeof returnValue === 'undefined'
			? ""
			: " = " + returnValue;
		
		this._log(level, 'EXIT ' + functionName + '()' + returnString);
	};

	this.log = function(level, text) {
		this._log(level + 1, text);
	};

	this._log = function(level, text) {
		console.log(this.indent(level) + text);
	};
	
	this.indent = function(level) {
		var s = "";

		for (var i = 0; i < level; ++ i) {
			s += "  ";
		}
		
		return s;
	}
}
