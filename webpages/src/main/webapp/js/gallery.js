/**
 * Gallery of unlimited size that renders from virtual REST services
 * 
 * divId - ID of element that will be root
 * columnSpacing - horizontal spacing
 * rowSpacing - vertical spacing
 * galleryModel - user implementation of gallery model
 * galleryView - user implementation of gallery view
 * 
 * return - new HTML element or just return the provisional one if could be updated
 * 
 */

/**
 * View functions:
 * 
 * Make DOM element to be shown while images are being loaded from server
 *  - index - index of element to show in virtual array
 *  - title - tile text of element
 *  - itemWidth - width of complete item
 *  - itemHeight - height of complete item
 * 
 * makeProvisionalHTMLElement(index, title, itemWidth, itemHeight)
 * 
 * 
 * Make the DOM element to show after image has been loaded from server.
 * 
 * makeImageHTMLElement(title, imagedata)
 *  - title - title text for image
 *  - imagedata - image data, user specific (eg. a Blob)
 */

/**
 * Model functions
 * 
 * getImages(firstIndex, count, onsuccess) 
 *  - firstIndex - index into virtual array of images
 *  - count - number of items to get images for, starting at firstIndex
 *  - onsuccess - function to be called back with an array of elements that represents the images (user specific)

 */

function Gallery(divId, columnSpacing, rowSpacing, galleryModel, galleryView) {
	
	if (typeof columnSpacing != 'number') {
		throw 'Columnspacing is not an int ' + typeof columnSpacing;
	}
	
	this.divId = divId;
	this.columnSpacing = columnSpacing;
	this.rowSpacing = rowSpacing;
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
	
	//document.getElementById(divId).append(innerDiv);
	
	/**
	 * refresh with passing in function for getting titles and thumb sizes
	 */
	this.refresh = function(getTitlesAndThumbSizes) {

		var level = 0;
		
		this.enter(level, 'refresh', []);

		var t = this;

		// get all information and update view accordingly
		getTitlesAndThumbSizes(
				
				function (count) {
					t.titles = new Array();
					t.widths = new Array();
					t.heights = new Array();
				},
				
				function(title, width, height) {
					t.titles.push(title);
					t.widths.push(width);
					t.heights.push(height);
				},
				
				function () {
					// completed metadata build, now compute and rerender
					t._computeAndRender(level + 1);
				}
		);

		this.exit(level, 'refresh');
	};
	
	this.getVisibleHeight = function() {
		return this._getInnerElement().clientHeight;
	};

	this._computeAndRender = function (level) {
		
		this.enter(level, 'computeAndRender', []);

		// Get the width of element to compute how many elements there are room for
		var itemsPerRow = this._computeItemsPerRow();
		
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
		this._addDivs(level + 1, 0, 0, itemsPerRow, this.getVisibleHeight());
		
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
		if (timeoutStartY == curY) {
			
			// Not scrolled since timeout started, load images
			console.log('Load images from ' + timeoutStartY);

			var t = this;

			// Call external functions to load images
			this.galleryModel.getImages(firstIndex, count, function(imageDataArray) {

				var rowNo = firstIndex / t.itemsPerRow;

				var rowWidth = t._getRowWidth();
				var numRows = t.rowDivs.length;

				for (var row = 0, i = 0; row < numRows && i < count; ++ row) {
					
					// tallest item in row
					var rowMaxHeight = t._findRowMaxItemHeight(row, t.itemsPerRow);
					
					// height of this row, eg first and last row may have additional spacing so items are taller
					var rowHeight = t._getRowHeight(rowMaxHeight, row, numRows);

					var rowDiv = t.rowDivs[row];
					var itemsThisRow = rowDiv.childNodes.length;
					
					// Store new elements in array and then replace all at once
					var newRowItems = [];
					
					t._addRowItems(level + 1, rowDiv, i, itemsThisRow, rowWidth, rowHeight,
							function (index, title, itemWidth, itemHeight) {

								var imageData = imageDataArray[index];
								var item;
								
								if (imageData == null) {
									item = rowDiv.childNodes[index - i];
								}
								else if (typeof imageData === 'undefined') {
									throw "Image data undefined at: " + index;
								}
								else {
									item = t.galleryView.makeImageHTMLElement(title, imageData);
								}
								
								return item;
							},
							function (element) {
								newRowItems.push(element);
							});
			
					for (var c = 0; c < itemsThisRow && i < count; ++ c) {
						
						var rowItem = rowDiv.childNodes[c];
						
						if (typeof rowItem === 'undefined' || rowItem == null) {
							throw "No row item";
						}

						// Replace element
						rowDiv.replaceChild(newRowItems[c], rowItem)
						
						++ i;
					}
				}
			});
		}
	}
	
	this._updateOnScroll = function(curY) {
		// See if we have something that was not visible earlier scrolled into view
		
		var level = 0;
		
		this.enter(level, 'updateOnScroll', ['curY', curY], [ 'firstY',  this.firstY,  'lastY', this.lastY ]);

		if (curY + this.getVisibleHeight() < this.firstY) {
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
			this.log(level, 'Scrolled to completely below previous curY ' + curY + ' visibleHeight ' + this.getVisibleHeight() + ' > lastY ' + this.lastY);

			this._redrawCompletelyAt(level + 1, curY);
		}
		else if (this.lastY - curY < this.getVisibleHeight()) {
			// Scrolling down partly out of visible area
			// First figure out how much visible space that must be added
			var heightToAdd = this.getVisibleHeight() - (this.lastY - curY);

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
		
		var elem = this._findElementPos(curY);
		
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

		this._addDivs(level + 1, elem.firstItemIndex, elem.firstRowYPos, this.itemsPerRow, this.getVisibleHeight());
		
		this.exit(level, 'redrawCompletelyAt');
	};
	
	this._findElementPos = function(yPos) {
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
		
		return elem;
	}
	
	this._findRowMaxItemHeight = function(rowFirstIndex, itemsPerRow) {

		var rowMaxHeight = 0;

		for (var j = 0; j < itemsPerRow && (rowFirstIndex + j) < this.heights.length; ++ j) {
			var index = rowFirstIndex + j;
			var itemHeight = this.heights[index];

			if (itemHeight > rowMaxHeight) {
				rowMaxHeight = itemHeight;
			}
		}
		
		return rowMaxHeight;
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
		
		var numRows = ((this.widths.length - 1) / itemsPerRow) + 1;
		var rowNo = startIndex / itemsPerRow;

		var rowWidth = this._getRowWidth();

		var lastRenderedElement = null;
		
		for (var i = startIndex; i < this.widths.length; i += (downwards ? itemsPerRow : -itemsPerRow)) {

			// Last row might not have a full number of items
			var itemsThisRow = i + itemsPerRow >= this.widths.length
				? this.widths.length - i
				: itemsPerRow; 
			
			var rowDiv = document.createElement('div');

			// tallest item in row
			var rowMaxHeight = this._findRowMaxItemHeight(i, itemsPerRow);
			
			// height of this row, eg first and last row may have additional spacing so items are taller
			var rowHeight = this._getRowHeight(rowMaxHeight, rowNo, numRows);

			rowNo = rowNo + (downwards ? 1 : -1);

			var t = this;
			// Add row items to the row
			this._addRowItems(level + 1, rowDiv, i, itemsThisRow, rowWidth, rowHeight,
					function (index, title, itemWidth, itemHeight) {
						return t.galleryView.makeProvisionalHTMLElement(index, title, itemWidth, itemHeight);
					},
					function (element) {
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

			addRowDiv(rowDiv);

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
	this._addRowItems = function(level, rowDiv, firstItemIndex, itemsThisRow, rowWidth, rowHeight, makeElement, addElement) {
		var x = 0;

		var totalRowItemWidths = 0;
		for (var j = 0; j < itemsThisRow; ++ j) {
			var index = firstItemIndex + j;

			totalRowItemWidths += this.widths[index];
		}

		for (var j = 0; j < itemsThisRow; ++ j) {
			var index = firstItemIndex + j;

			var itemWidth = this.widths[index];
			var itemHeight = this.heights[index];

			var itemElement = makeElement(index, this.titles[index], itemWidth, itemHeight);

			// Add to model at relative offsets
			
			var spacing = (rowWidth - totalRowItemWidths) / (itemsThisRow + 1);

			// this.log(level, 'set spacing to ' + spacing + '/' + rowWidth + '/' + totalRowItemWidths + '/' + itemsThisRow);
			
			this._applyItemStyles(itemElement, rowHeight, itemWidth, itemHeight, spacing);

			addElement(itemElement);
			
			x += itemWidth;
		}
	}
	
	this._applyItemStyles = function(itemElement, rowHeight, itemWidth, itemHeight, spacing) {
		itemElement.setAttribute('style',
				'position : relative; ' +
				/*
				'display : inline-block; ' +
				*/
				'float : left; ' +
				'margin-left : ' + spacing + 'px; ' +
				'top : ' + (rowHeight - itemHeight) / 2 + 'px; ' +
				'width : ' + itemWidth + '; ' +
				'height : ' + itemHeight + '; ' +
				'background-color : white; ');
		
	}

	this._computeItemsPerRow = function() {
		// Must look at all widths and find the most number of thumbs there are rooms for
		// Start out with the initial ones and adjust

		var width = this.width;
		
		// Start out with width
		var itemsPerRow = width;
		
		var done;
		
		do {
			var inRow = 0;
			var pixForRow = 0;
			
			done = true;
			
			for (var i = 0; i < this.widths.length; ++ i) {
				pixForRow += this.columnSpacing; // initial spacing
				pixForRow += this.widths[i];
				
				if (pixForRow + this.columnSpacing > width) {
					// Not room for any more
					if (inRow < itemsPerRow) {
						// Found to be space for less per row than previously found,
						// we must compute all anew
						itemsPerRow = inRow;
						done = false;
						break;
					}
				}
				else {
					++ inRow;
					
					if (inRow == itemsPerRow) {
						// There was room for as many as we had previously reduced to,
						// just continue on next row
						inRow = 0;
						pixForRow = 0;
					}
				}
			}
		} while (!done);
		
		return itemsPerRow;
	}
	
	this._computeHeight = function(itemsPerRow) {
		
		var height = 0;
		
		for (var i = 0; i < this.heights.length; i += 3) {
			
			height += this.rowSpacing; // before each row

			var rowMaxHeight = this._findRowMaxItemHeight(i, itemsPerRow);
			
			height += rowMaxHeight;
		}
		
		height += this.rowSpacing; // after last row
		
		return height;
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
