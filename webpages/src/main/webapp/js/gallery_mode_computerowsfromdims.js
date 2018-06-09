/**
 * Previous way of computing items per row,
 * gallery would have to know width of items
 * 
 */
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
			
			for (var i = 0; i < this._getTotalNumberOfElements(); ++ i) {
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

	this._addRowItems = function(level, rowDiv, firstItemIndex, itemsThisRow, rowWidth, makeElement, addElement) {

		if (typeof this.config.widthHint !== 'undefined' || typeof config.heightHint !== 'undefined') {
			// Must set visibility : hidden first in order to compute size and set same max size on all
		}
		else {
			// Size is hardcoded
		}
		
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
	
