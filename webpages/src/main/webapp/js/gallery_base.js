/**
 * Base class functions for all gallery based objects
 *  
 */

function GalleryBase() {

}

/**
 * Helper function to recreate an array that is part of a larger virtual array,
 * copy over any overlapping items and setting the rest to some default value.
 * Allows for copying just a subset of the overlap between old and new array, but in the normal case
 * one would have firstOverlapCheckIndex == newArrayFirstViewIndex and lastOverlapCheckIndex == newArrayLastViewIndex
 * to just copy anything that overlaps between the arrays. firstOverlapCheckIndex and lastOverlapCheckIndex must be within
 * new array indices.
 * 
 * - array - the input array to copy from, this represents a view into virtual array (eg. scrolling cache for a gallery)
 * - prevFirstViewIndex - the index into virtual array of first element in array
 * - prevLastViewIndex - the index into virtual array last element in array
 * 
 * - firstOverlapCheckIndex - first index into virtual array at which to start checking for overlap  
 * - lastOverlapCheckIndex - last index into virtual array at which to start checking for overlap
 * 
 * - newArrayFirstViewIndex - the index into virtual array of first item om newly created array
 * - newArrayLastViewIndex - the index into virtual array of last item of newly created array
 * 
 * - defaultValue - the default value to add to spaces that do not overlap
 * 
 */

GalleryBase.prototype.scrollVirtualArrayView = function(level,
		array,
		prevFirstViewIndex, prevLastViewIndex,
		firstOverlapCheckIndex, lastOverlapCheckIndex,
		newArrayFirstViewIndex, newArrayLastViewIndex,
		defaultValue) {
	
	this.enter(level, 'scrollVirtualArrayView', [
		'prevFirstViewIndex', prevFirstViewIndex,
		'prevLastViewIndex', prevLastViewIndex,
		'firstOverlapCheckIndex', firstOverlapCheckIndex,
		'lastOverlapCheckIndex', lastOverlapCheckIndex,
		'newArrayFirstViewIndex', newArrayFirstViewIndex,
		'newArrayLastViewIndex', newArrayLastViewIndex,
		'defaultValue', defaultValue
	]);
	
	this.checkNonNull(prevFirstViewIndex);
	this.checkNonNull(prevLastViewIndex);
	this.checkNonNull(firstOverlapCheckIndex);
	this.checkNonNull(lastOverlapCheckIndex);
	this.checkNonNull(newArrayFirstViewIndex);
	this.checkNonNull(newArrayLastViewIndex);
	
	var numArrayEntries = prevLastViewIndex - prevFirstViewIndex + 1;
	
	if (numArrayEntries !== array.length) {
		throw "numArrayEntries !== array.length: " + numArrayEntries + "/" + array.length;
	}

	// Basic parameter checks
	if (prevLastViewIndex < prevFirstViewIndex) {
		throw "prevLastViewIndex < prevFirstViewIndex";
	}

	if (lastOverlapCheckIndex < firstOverlapCheckIndex) {
		throw "astOverlapCheckIndex < firstOverlapCheckIndex";
	}

	if (newArrayLastViewIndex < newArrayFirstViewIndex) {
		throw "newArrayLastViewIndex < newArrayFirstViewIndex";
	}
	
	// Check that overlap check indices are within new array indices
	if (firstOverlapCheckIndex > newArrayLastViewIndex) {
		throw "firstOverlapCheckIndex > newArrayLastViewIndex";
	}
	
	if (lastOverlapCheckIndex < newArrayFirstViewIndex) {
		throw "lastOverlapCheckIndex < newArrayFirstViewIndex";
	}

	if (lastOverlapCheckIndex > newArrayLastViewIndex) {
		throw "lastOverlapCheckIndex > newArrayLastViewIndex";
	}
	
	if (firstOverlapCheckIndex < newArrayFirstViewIndex) {
		throw "firstOverlapCheckIndex < newArrayFirstViewIndex";
	}

	var overlapFirstIndex;
	var overlapLastIndex;

	if (firstOverlapCheckIndex >= prevFirstViewIndex && lastOverlapCheckIndex <= prevLastViewIndex) {
		// Overlap check area entirely within previous
		overlapFirstIndex = firstOverlapCheckIndex;
		overlapLastIndex = lastOverlapCheckIndex;
	}
	else if (firstOverlapCheckIndex < prevFirstViewIndex && lastOverlapCheckIndex >= prevFirstViewIndex && lastOverlapCheckIndex <= prevLastViewIndex) {
		// Overlap check area overlapping from before and into upto whole prev area
		overlapFirstIndex = prevFirstViewIndex;
		overlapLastIndex = lastOverlapCheckIndex;
	}
	else if (firstOverlapCheckIndex >= prevFirstViewIndex && firstOverlapCheckIndex <= prevLastViewIndex && lastOverlapCheckIndex > prevLastViewIndex) {
		// Overlap check area overlapping from after and into and upto beginning of prev area
		overlapFirstIndex = firstOverlapCheckIndex;
		overlapLastIndex = prevLastViewIndex;
	}
	else if (firstOverlapCheckIndex < prevFirstViewIndex && lastOverlapCheckIndex > prevLastViewIndex) {
		throw "!! overlap area superset of cached area !!";
	}
	else if (lastOverlapCheckIndex < prevFirstViewIndex) {
		// Completely above
		overlapFirstIndex = -1;
		overlapLastIndex = -1;
	}
	else if (firstOverlapCheckIndex > prevLastViewIndex) {
		// Completely below
		overlapFirstIndex = -1;
		overlapLastIndex = -1;
	}
	else {
		throw "Unhandled area state";
	}
	
	if (overlapLastIndex < overlapFirstIndex) {
		throw "overlapLastIndex < overlapFirstIndex: overlapLastIndex=" + overlapLastIndex + ", overlapFirstIndex=" + overlapFirstIndex;
	}

	var newArrayLength = newArrayLastViewIndex - newArrayFirstViewIndex + 1;
	var newArray;

	if (overlapFirstIndex != -1 && overlapLastIndex != -1) {
		// Overlap at some coordinate in the virtual array.
		// Create a new array and just copy the overlapping area over, fill the rest with null
		
		newArray = new Array(newArrayLength);

		var numOverlapping = overlapLastIndex - overlapFirstIndex + 1;
		
		// First cache array index of overlapping
		this.log(level, 'Found number of overlapping ' + numOverlapping + ' from ' + overlapFirstIndex + ' to ' + overlapLastIndex);
		
		var dstFirstOverlapping = overlapFirstIndex - newArrayFirstViewIndex;
		
		// null value for all up to overlap
		
		this.log(level, 'Adding default value entries up to ' + dstFirstOverlapping);

		for (var i = 0; i < dstFirstOverlapping; ++ i) {
			newArray[i] = defaultValue;
		}
		
		// Copy all overlapping items' refs from current array
		var srcFirstOverlapping = overlapFirstIndex - prevFirstViewIndex;

		this.log(level, 'Adding overlapping entries to dst from ' + dstFirstOverlapping + ", src " + srcFirstOverlapping + ', count ' + numOverlapping);

		for (var i = 0; i < numOverlapping; ++ i) {

			var srcIdx = srcFirstOverlapping + i;
			var dstIdx = dstFirstOverlapping + i;

			this.log(level + 1, 'Copying from array indices ' + srcIdx + ' to ' + dstIdx + ' : ' + array[srcIdx]);

			newArray[dstIdx] = array[srcIdx];
		}
		
		var dstIdxAfterOverlapping = dstFirstOverlapping + numOverlapping;
		
		this.log(level, 'Adding default value entries to dst from ' + dstIdxAfterOverlapping + ', up to ' + (newArray.length - 1));

		// null value for remaining items
		for (var i = dstIdxAfterOverlapping; i < newArray.length; ++ i) {
			newArray[i] = defaultValue;
		}
	}
	else if (overlapFirstIndex != -1 || overlapLastIndex != -1) {
		throw "One of overlapFirstIndx and overlapLastIndex set, must set both";
	}
	else {
		// No overlap, just create a new empty array with all nulls
		
		newArray = new Array(newArrayLength);
		
		for (var i = 0; i < newArrayLength; ++ i) {
			newArray[i] = defaultValue;
		}
	}

	this.exit(level, 'scrollVirtualArrayView', printArray(newArray));
	
	return newArray;
}


GalleryBase.prototype.enter = function(level, functionName, args, states) {
	var argsString;
	
	if (typeof args === 'undefined') {
		argsString = '';
	}
	else {
		argsString = this._arrayToString(args);
	}
	
	var logString = 'ENTER ' + functionName + '(' + argsString + ')';
	
	if (typeof states !== 'undefined') {
		logString += ' state=[' + this._arrayToString(states) + ']';
	}
	
	this._log(level, logString);
};

GalleryBase.prototype._arrayToString = function(array) {
	arrayString = '';
	
	if (array.length % 2 != 0) {
		throw 'Number of arguments to ' + functionName + ' not an even number: ' + array.length;
	}
	
	for (var i = 0; i < array.length; i += 2) {
		if (i > 0) {
			arrayString += ', ';
		}

		arrayString += array[i] + '=' + array[i + 1];
	}
	
	return arrayString;
}


GalleryBase.prototype.exit = function(level, functionName, returnValue) {
	
	var returnString = typeof returnValue === 'undefined'
		? ""
		: " = " + returnValue;
	
	this._log(level, 'EXIT ' + functionName + '()' + returnString);
};

GalleryBase.prototype.log = function(level, text) {
	this._log(level + 1, text);
};

GalleryBase.prototype._log = function(level, text) {
	console.log(this.indent(level) + text);
};

GalleryBase.prototype.indent = function(level) {
	var s = "";

	for (var i = 0; i < level; ++ i) {
		s += "  ";
	}
	
	return s;
}

GalleryBase.prototype.isNotNull = function(obj) {
	return typeof obj !== 'undefined' && obj != null;
}

GalleryBase.prototype.isUndefinedOrNull = function(obj) {
	return typeof obj === 'undefined' || obj == null;
}

GalleryBase.prototype.checkNonNull = function(obj) {
	if (this.isUndefinedOrNull(obj)) {
		throw "obj not set: " + obj;
	}
}
