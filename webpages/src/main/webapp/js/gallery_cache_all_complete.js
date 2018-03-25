/**
 * Always download and cache all complete elements
 * eg download both title and thumb sizes in provisional step, and
 * download all complete elements.
 * 
 * This is the simplest implementation wise as we just download everything right away
 * and build all DOM elements in the gallery, gives the smoothest scrolling and also
 * makes scrollbars appear the right size for few items.
 * 
 * This is however not workable for larger number of items.
 *
 */

function GalleryCacheAllComplete(config, galleryModel, galleryView, initialTotalNumberOfItems) {
	GalleryCacheBase.call(this, config, galleryModel, galleryView, initialTotalNumberOfItems);
}

GalleryCacheAllComplete.prototype = Object.create(GalleryCacheBase.prototype);


// Refresh from data, startYPos in y pos in complete scrollable view
// returns approximate complete size of view
GalleryCacheAllComplete.prototype.refresh = function(level, totalNumberOfItems, widthMode, heightMode) {
	
	this.totalNumberOfItems = totalNumberOfItems;

	var t = this;

	this.galleryModel.getProvisionalData(0, totalNumberOfItems, function(provisionalDataArray) {

		// Just read complete data for all elements while we are at it
		
		t.galleryModel.getCompleteData(0, totalNumberOfItems, function(completeDataArray) {
			
			t._render(level + 1, provisionalDataArray, completeDataArray, widthMode, heightMode);
		});
	});
}

GalleryCacheAllComplete.prototype._render = function(level, provisionalDataArray, completeDataArray, widthMode, heightMode) {

	// Just append all divs straight away
	var startIndex = 0;
	var startPos = 0;
	var numColumns = widthMode.computeNumColumns(this.config, this.columnSpacing, this._getVisibleWidth());
	var heightToAdd = this._computeHeight(heightMode, numColumns);
	
	var t = this;
	
	this._addDivs(level, startIndex, startPos, numColumns, heightToAdd, function(index, itemWidth, itemHeight) {
		var element;
		
		var provisionalData = provisionalDataArray[index];
		var completeData = completeDataArray[index];

		if (completeData == null) {
			element = t.galleryView.makeProvisionalHTMLElement(index, provisionalData);
		}
		else {
			element = t.galleryView.makeCompleteHTMLElement(index, provisionalData, completeData);
		}
		
		return element;
	});
}

// Update y position within scrollable view, startYPos is offset into beginning of that view for first
// visible line
GalleryCacheAllComplete.prototype.updateOnScroll = function(curY) {
	// Nothing to do since we have added all elements to the DOM already,
	// the browser will render the scrolling
}




