/**
 * Class for keeping display state, eg. for gallery cached rendering.
 * 
 * All public calls (not prefixed with '_') are immutable
 * 
 * Kept as a separate class for maintainability.
 * displayState record keeps track of where we are in terms of what is rendered
 * right now. It is kept as a separate JS object so that it can be passed around and returned by functions/methods.
 * 
 * Note that there is no track of index of first visible item as this can be computed on the fly from
 * current y position.
 * 
 * It has the following fields:
 *
 * firstVisibleY  - y pos of first pixel visible into the scrollable area, so 100 if user scrolled 100 pixels down. Includes row spacing.
 * firstRenderedY - y pos into scrollable area that is rendered, ie rows has been added.So if user scrolls to 100
 *                  this will probably stay at 0 since we keep some rows before and after the visible area.
 * firstVisibleIndex - index into display of first visible item, ie. first item of which image or spacing is visible in the display.
 * firstRenderedIndex  -  index into virtual model array of first item that is rendered (not visible) eg the element at firstRenderedY.
 *                        This might be completely outside of visible area.
 *
 * lastVisibleY - firstVisibleY + visibleHeight - 1, eg. the y position within scroll area of the last visible line in the viewport.
 *                If user has scrolled to 100 and viewport is 300px of height, this would be 399.
 * lastRenderedY - y pos of last pixel of last rendered element, including row spacing.
 * lastVisibleIndex - index into virtual model array of last visible item, ie. item or spacing within visible display area.
 * lastRenderedIndex - index into virtual array og last rendered element, will always be the last element on a row since all items on a row are aligned
 *                     and always add a complete row div with all of them.
 *                     
 * renderState - array for maintaining whether for each index between firstRenderedIndex and lastRenderedIndex (inclusive)
 *               have rendered complete or only provisional data.
 */

var DisplayState = function () {

}

DisplayState.prototype = Object.create(GalleryBase.prototype);

// For renderState[] items
var RENDER_STATE_PROVISIONAL = 1;
var RENDER_STATE_COMPLETE = 2;

DisplayState._fields = [
	'firstVisibleIndex',
	'lastVisibleIndex',
	'firstRenderedIndex',
	'lastRenderedIndex',
	'firstVisibleY',
	'lastVisibleY',
	'firstRenderedY',
	'lastRenderedY'
];


DisplayState.createEmptyDisplayState = function() {

	var displayState = new DisplayState();

	displayState.firstVisibleY = 0;
	displayState.firstRenderedY = 0; // renders a bit outside of display since adding complete rows
	displayState.firstVisibleIndex = 0;
	displayState.firstRenderedIndex = 0;
	displayState.lastVisibleY = 0;
	displayState.lastRenderedY = 0; // renders a bit outside of display since adding complete rows
	displayState.lastVisibleIndex = 0;
	displayState.lastRenderedIndex = 0;
	
	// Must have one entry so that renderState.length == lastRenderedIndex - firstRenderedIndex + 1 above
	displayState.renderState = [ RENDER_STATE_PROVISIONAL ];

	return displayState;
}

DisplayState.createFromFields = function(fieldsToApply) {

	var displayState = new DisplayState();

	DisplayState.prototype._applyFields(fieldsToApply);

	for (var i = 0; i < DisplayState._fields.length; ++ i) {
		var field = DisplayState._fields[i];
		
		if (typeof fieldsToApply[field] === 'undefined') {
			throw "No value for field " + field;
		}
	}

	displayState._applyFields(fieldsToApply);

	DisplayState._validateIndices(
			displayState.firstVisibleIndex,
			displayState.lastVisibleIndex,
			displayState.firstRenderedIndex,
			displayState.lastRenderedIndex);

	DisplayState._validateY(
			displayState.firstVisibleY,
			displayState.lastVisibleY,
			displayState.firstRenderedY,
			displayState.lastRenderedY);

	// Create render-state from last-rendered - firstRendered
	var numRenderedItems = displayState.lastRenderedIndex - displayState.firstRenderedIndex + 1;

	displayState.renderState = new Array(numRenderedItems);

	for (var i = 0; i < numRenderedItems.length; ++ i) {
		displayState.renderState[i] = RENDER_STATE_PROVISIONAL;
	}

	return displayState;
}

DisplayState.prototype.addCurYToDisplayState = function(level, curY, visibleHeight, displayStateFields) {

	this.enter(level, 'DisplayState.addCurYToDisplayState', [
		'curY', curY,
		'visibleHeight', visibleHeight,
		'displayStateFields', JSON.stringify(displayStateFields)
	]);
	
	var copy = this._makeCopy();

	copy._applyFields(displayStateFields)
	copy._addCurYToDisplayState(curY, visibleHeight);

	// Must scroll render-state as well according to updated values
	copy.renderState = this.scrollVirtualArrayView(
			level + 1,
			this.renderState,
			
			// current index
			this.firstRenderedIndex,
			this.lastRenderedIndex,
			
			// overlap indices and new array indices, same values
			displayStateFields.firstRenderedIndex,
			displayStateFields.lastRenderedIndex,
			
			displayStateFields.firstRenderedIndex,
			displayStateFields.lastRenderedIndex,
			
			RENDER_STATE_PROVISIONAL // default value for when scrolling to a new area
	); 
	
	copy._checkRenderState();

	// Must look for overlap and move over any items that overlap between the two arrays into a new array,
	// maintaining indices and any already set items as we scroll
	
	this.exit(level, 'DisplayState.addCurYToDisplayState', copy.toDebugString());

	return copy;
}

DisplayState.prototype._addCurYToDisplayState = function(curY, visibleHeight) {
	this.firstVisibleY 	= curY;
	this.lastVisibleY	= curY + visibleHeight - 1;
}

DisplayState.prototype._makeCopy = function() {
	
	var copy = new DisplayState();
	

	copy.firstVisibleIndex 	= this.firstVisibleIndex;
	copy.lastVisibleIndex 	= this.lastVisibleIndex;

	copy.firstRenderedIndex	= this.firstRenderedIndex;
	copy.lastRenderedIndex 	= this.lastRenderedIndex;

	copy.firstVisibleY 		= this.firstVisibleY;
	copy.lastVisibleY 		= this.lastVisibleY;

	copy.firstRenderedY 	= this.firstRenderedY;
	copy.lastRenderedY 		= this.lastRenderedY;

	copy.renderState = []
	
	for (var i = 0; i < this.renderState.length; ++ i) {
		copy.renderState[i] = this.renderState[i];
	}

	return copy;
}

DisplayState.prototype.updateFields = function(toApply) {
	var copy = this._makeCopy();
	
	copy._applyFields(toApply);
	
	return copy;
}


DisplayState.prototype._applyFields = function(toApply) {

	for (var i = 0; i < DisplayState._fields.length; ++ i) {
		var field = DisplayState._fields[i];
		
		if (typeof toApply[field] !== 'undefined') {
			this[field] = toApply[field];
		}
	}
}

DisplayState.prototype.sameWithUpdatedDisplayArea = function(curY, visibleHeight) {
	var updated = this._makeCopy();
	
	// This is mutable
	updated._addCurYToDisplayState(curY, visibleHeight);

	return updated;
}

DisplayState.prototype.sameWithUpdatedVisibleIndices = function(firstVisibleIndex, lastVisibleIndex) {
	var updated = this._makeCopy();

	updated.firstVisibleIndex = firstVisibleIndex;
	updated.lastVisibleIndex = lastVisibleIndex;

	return updated;
}

DisplayState._validateIndices = function(firstVisibleIndex, lastVisibleIndex, firstRenderedIndex, lastRenderedIndex) {
	
	if (firstVisibleIndex > lastVisibleIndex) {
		throw "firstVisibleIndex > lastVisibleIndex";
	}
	
	if (firstRenderedIndex > lastRenderedIndex) {
		throw "firstRenderedIndex > lastRenderedIndex";
	}
	
	
	if (firstVisibleIndex < firstRenderedIndex) {
		throw "firstVisibleIndex < firstRenderedIndex";
	}
 	
	if (lastVisibleIndex > lastRenderedIndex) {
		throw "lastVisibleIndex > lastRenderedIndex";
	}
}

DisplayState._validateY = function(firstVisibleY, lastVisibleY, firstRenderedY, lastRenderedY) {
	if (firstVisibleY > lastVisibleY) {
		throw "firstVisibleY > lastVisibleY";
	}
	
	if (firstRenderedY > lastRenderedY) {
		throw "firstRenderedY > lastRenderedY";
	}
	
	if (firstVisibleY < firstRenderedY) {
		throw "firstVisibleY < firstRenderedY";
	}
 	
	if (lastVisibleY > lastRenderedY) {
		throw "lastVisibleY > lastRenderedY " + lastVisibleY + "/" + lastRenderedY;
	}
}

DisplayState.prototype.sameWithUpdateVisibleIndices - function(firstVisibleIndex, lastVisibleIndex) {

	var updated = this._makeCopy();
	
	this._validateIndices(firstVisibleIndex, lastVisibleIndex, this.firstRenderedIndex, this.lastRenderedIndex);

	updated.firstVisibleIndex = firstVisibleIndex;
	updated.lastVisibleIndex = lastVisibleIndex;

	updated._checkRenderState();

	return updated;
}

DisplayState.prototype._checkRenderState = function(index) {
	var numRendered = this.lastRenderedIndex - this.firstRenderedIndex + 1;
	
	if (numRendered !== this.renderState.length) {
		throw "numRendered !== renderState.length: " + numRendered + "/" + this.renderState.length;
	}
}

DisplayState._checkRenderStateIndex = function(index) {
	if (index < this.firstRenderedIndex) {
		throw "index < this.firstRenderedIndex";
	}

	if (index > this.lastRenderedIndex) {
		throw "index > this.lastRenderedIndex";
	}
}

DisplayState.prototype.setRenderStateComplete = function(level, index, count) {

	this.enter(level, 'Display.setRenderStateComplete', [
		'index', index,
		'count', count
	],
	['this', this.toDebugString()]);
	
	var copy = this._makeCopy();
	
	copy._checkRenderState();

	DisplayState._checkRenderStateIndex(index);

	if (index + count - 1> copy.lastRenderedIndex) {
		throw "index + count > this.lastRenderedIndex: " + index + "/" + count + "/" + copy.lastRenderedIndex;
	}

	for (var i = 0; i < count; ++ i) {
		var offset = index - copy.firstRenderedIndex;

		copy.renderState[offset + i] = RENDER_STATE_COMPLETE;
	}

	copy._checkRenderState();

	this.exit(level, 'Display.setRenderStateComplete', copy.toDebugString());

	return copy;
}

DisplayState.prototype.hasRenderStateComplete = function(index) {
	this._checkRenderState();

	DisplayState._checkRenderStateIndex(index);

	var offset = index - this.firstRenderedIndex;

	return this.renderState[offset] == RENDER_STATE_COMPLETE;
}

DisplayState.prototype.toDebugString = function() {
	var s =
		"{ " +
		'fVI=' + this.firstVisibleIndex + ", " +
		'lVI=' + this.lastVisibleIndex + ", " +
	
		'fRI=' + this.firstRenderedIndex + ", " +
		'lRI=' + this.lastRenderedIndex + ", " +
	
		'fVY=' + this.firstVisibleY + ", " +
		'lVY=' + this.lastVisibleY + ", " +
	
		'fRY=' + this.firstRenderedY + ", " +
		'lRY=' + this.lastRenderedY +
		' }';

	return s;
}