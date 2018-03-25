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

function GalleryCacheAllComplete(visibleHeight) {
	GalleryCacheBase.call(this, visibleHeight);
}

GalleryCacheAllComplete.prototype = Object.create(GalleryCacheBase.prototype);

GalleryCacheAllComplete.prototype.updateVisibleHeigth = function(visibleHeight) {
	// Nothing to do here since we download all data at once and
	// does not have to add or remove from cache, method here for doc purposes
	
	Object.getProtoTypeOf(GalleryCacheAllComplete.prototype).updateVisibleHeigt.call(this, visibleHeight);
}

// Refresh from data, startYPos in y pos in complete scrollable view
// returns approximate complete size of view
GalleryCacheAllComplete.prototype.refresh = function(startYPos) {
	
}

// Update y position within scrollable view, startYPos is offset into beginning of that view for first
// visible line
GalleryCacheAllComplete.prototype.updateOnScroll = function(curY) {
	// Nothing to do since we have added all elements to the DOM already,
	// the browser will render the scrolling
}




