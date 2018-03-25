/**
 * A class for tracking cached item data, can be used for complete and provisional items
 * 
 * This caches data as it is read asynchronously, teh DOM elements
 * are tracked in the gallery cache.
 * 
 * 
 * 
 * The main difficulty here is
 *  - items at any certain point will be downloaded in multiple chunks for speed
 *    - download async just for visible area, for quick display update as possible
 *    - download async for a number of items before visible area as pre-download for upwards scrolling
 *    - download async for a nuber of items after visible area as pre-download for upwards scrolling
 *    
 *  - thus we might have some items downloaded but not all
 *  - downloads might be partly or wholly outdated when async response returns, we must copy over only the required elements.
 *    and issue downloads for all items that we are missing as the user scrolls.
 *    
 *  - one ought to remove elements from cache once they are outside of visible area, eg set aray elements to null
 *  
 *  - we want to prioritized download for visible area so download those first
 *  - we want to cancel other downloads  that are obsolete but have not yet started.
 * 
 * We implement this with an array of items where items can be null if not completed download.
 * 
 */

/**
 * Constuctor
 *  - cacheBeforeAndAfter - cache this number before and after visible area, ie if == 20, then we will download in total 40 elements for outside visible area.
 *  
 */

function GalleryCacheItems(cacheBeforeAndAfter, modelDownloadItems) {
	this.totalNumberOfItems = 0;
	
	this.cacheBeforeAndAfter = cacheBeforeAndAfter;
	
	this.curVisibleIndex = 0;
	this.curVisibleCount = 0;

	this.updateSequenceNo = 0;

	var arraySize = cachedBeforeAndAfter + this.curVisibleCount;
	
	this.cachedItems = new Array(arraySize);
	this._clear(arraySize);

	// Queue of downloads to be scheduled
	this.downloadQueue = [];
	
	// Downloads that are in progress
	this.ongoingDownloads = [];
	
	this.modelDownloadItems = modelDownloadItems;
}

GalleryCacheItems.prototype._clear = function(arrayIndex, count) {
	for (var i = 0; i < count: ++ i) {
		this.cachedItems[arrayIndex + i] = null;
	}
}

/**
 * Update visible area when user scrolls or resizes, will cause us to trigger all necessary downloads.
 * 
 * - firstVisibleIndex - index into virtual array of data elements for first element that is (partly or wholly) visible in scrollable view.
 * - visibleCount - number of visible elements, we need to download at least this many (if not already downloaded) but we would usually download elements above and below,
 *                  for making slow scrolling smoother.
 *                  
 *  - onAllVisibleDownloaded - callback function to be called when all visible have been updated, so that display can be updated.
 *  						   This might be called back immediately if requested images were already downloaded, eg if scrolling downwards and elements were preloaded.
 *  
 *  !! NOTE !! the onAllVisibleDownloaded will not be called for this invocation if .updateVisibleArea() has been called again while waiting for download response.
 *  		   This makes it easier for the caller as it does not have to check for whether response is out of date
 *             and easier in implementation here since if more calls have been done since download was issues, we just store away the response data
 *             so that it can be used as data for the latest invocation.
 *             
 *             Or put another way, as the user scrolled and this method is invoked, this might result in a lot of scattered downloads.
 *             For any download result we just update list of cached data and check whether we have all data downloaded for the *latest*
 *             call to updateVisibleArea() and call the callback this latest invocation, even if it was an earlier invocation to updateVisibleArea() that
 *             triggered the download.
 *             
 *             Caller then just have to invoke this with a callback but not rely on always getting a response to all callback - which ought not matter,
 *             caller only wants to be called back when a call to updateVisibleArea() results in the complete set of elements being visible
 *             so that whole visible display can be updated at once (eg. all thumbs shown at once, instead of one thumb shown at a time as they are downloaded).
 * 
 */


GalleryCacheItems.prototype._getFirstIndexInCache = function() {
	var firstCachedIndex = this.curVisibleIndex - this.cachedBeforeAndAfter;
	if (firstCachedIndex < 0) {
		firstCachedIndex = 0;
	}
	
	return firstCachedIndex;
}

GalleryCacheItems.prototype._getLastIndexInCache = function() {
	var lastCachedIndex = this.curVisibleIndex + this.curVisibleCount + this.cachedBeforeAndAfter - 1;
	if (lastCachedIndex >= this.totalNumberOfItems) {
		lastCachedIndex = this.totalNumberOfItems - 1;
	}
	
	return lastCachedIndex;
}

GalleryCacheItems.prototype.updateVisibleArea = function(firstVisibleIndex, visibleCount, totalNumberOfItems, onAllVisibleDownloaded) {
	
	// First see if there is any overlap with current visible area and then figure what to download
	var curFirstCachedIndex = this._getFirstIndexInCache();
	var curLastCachedIndex = this._getLastIndexInCache();

	if (cachedItems.length !== curLastCachedIndex - curFirstCachedIndex + 1) {
		throw "lastCaches - firstCache does not match cached items array";
	}

	var lastVisibleIndex = firstVisibleIndex + visibleCount - 1;

	var layout = this._computeNewCacheArrayLayout(firstVisibleIndex, visibleCount);

	var overlapFirstIndex;
	var overlapLastIndex;
	
	if (firstVisibleIndex >= curLastCachedIndex && lastVisibleIndex <= curLastCachedIndex) {
		overlapFirstIndex = firstVisibleIndex;
		overlapLastIndex = lastVisibleIndex;
	}
	else if (firstVisibleIndex < curFirstCachedIndex && lastVisibleIndex < curLastCachedIndex) {
		overlapFirstIndex = curFirstCachedIndex;
		overlapLastIndex = lastVisibleIndex;
	}
	else if (firstVisibleIndex > curFirstCachedIndex && lastVisibleIndex > curLastCachedIndex) {
		overlapFirstIndex = firstVisibleIndex;
		overlapLastIndex = curLastCachedIndex;
	}
	else if (firstVisibleIndex < curFirstCachedIndex && lastVisibleIndex > curLastCachedIndex) {
		throw "!! visible area superset of cached area !!";
	}
	else {
		throw "Unhandled area state";
	}

	
	// We need to move items from one cache array to a new one if there is overlap, ie. reuse what can be reused
	// if there is an overlapping in scrolling.
	// Easiest is just to find the overlapping area
	
	var newArrayLength = layout.lastCachedIndex - layout.firstCachedIndex + 1;

	if (overlapFirstIndex != -1 && overlapLastIndex != -1) {
		// Overlap at some coordinate in the virtual array.
		// Create a new array and jus copy the overlapping area over, fill the rest with null
		
		var newArray = new Array(newArrayLength);

		var numOverlapping = operlapLastIndex - overlapFirstIndex + 1;
		
		// First cache array index of overlapping
		var dstFirstOverlapping = overlapFirstIndex - layout.firstCachedIndex;
		
		// null value for all up to overlap
		for (var i = 0; i < dstFirstOverlapping; ++ i) {
			newArray[i] = null;
		}
		
		// Copy all overlapping items' refs from current array
		var srcFirstOverlapping = overlapFirstIndex - curFirstCachedIndex;
		
		for (var i = 0; i < numOverlapping; ++ i) {
			newArray[dstFirstOverlapping + i] + this.
		}
		
		// null value for remaining items
		for (var i = dstFirstOverlapping + numOverlapping; i < newArray.length; ++ i) {
			newArray[i] = null;
		}

		// Set cache array to be new one
		this.cacheArray = newArray;
	}
	else if (overlapFirstIndex != -1 || overlapLastIndex != -1) {
		throw "One of overlapFirstIndx and overlapLastIndex set, must set both";
	}
	else {
		// No overlap, just create a new empty array with all nulls
		
		this.cacheArray = new Array(newArrayLength);
		
		for (var i = 0; i < newArrayLength; ++ i) {
			this.cacheArray[i] = null;
		}
	}

	// Download cached items above and below.
	// If already in cache (as is likely), nothing will be downloaded
	
	// Items before

	// Just remove any items in download queue since we are doing all preloads anew anyways
	this.downloadQueue = [];
	
	// Track if there have been more requests scheduled since last time
	// If so, we do not bother to call back to user if we retrieve all items since
	// visible area has been updated anyways
	
	++ this.updateSequenceNo;
	
	var updateSequenceNoAtStartOfDownload = this.updateSequenceNo;
	var t = this;
	
	this._downloadItems(nextVisibleIndexInCacheArray, firstVisibleIndex, visibleCount, true, function (data) {
		if (updateSequenceNoAtStartOfDownload < t.updateSequenceNo) {
			// We can ignore this invocation since outdated
		}
		else {
			// No user updates (scrolling/resize) of visible area since request was called,
			// call back to user so can update screen elements
			onAllVisibleDownloaded();
		}
	});

	this._downloadItems(
			0,
			layout.firstCachedIndex,
			layout.numBeforeVisible,
			false);
	
	this._downloadItems(
			layout.numBeforeVisible + visibleCount,
			layout.lastCachedIndex,
			layout.numAfterVisible,
			false);
	
	
	// Update visibility
	this.curVisibleIndex = firstVisibleIndex;
	this.curVisibleCount = visibleCount;
}

/**
 * Compute layout of cached-array.
 * Array may change in size, depending of whether we are in the middle of scrolable data
 * or at start or end. Eg if at start, the first element cached is a visible one
 * so we will not preload anything before that. Likewise if scrolled to the bottom.
 * 
 * - firstVisibleIndex - first index into virtual array to display
 * - visibleCount - number of visible items
 * 
 */
GalleryCacheItems.prototype._computeNewCacheArrayLayout = function(firstVisibleIndex, visibleCount) {

	var firstCachedIndex = firstVisibleIndex - this.cachedBeforeAndAfter;
	if (firstCachedIndex < 0) {
		firstCachedIndex = 0;
		numBeforeVisible = firstVisibleIndex;
	}
	else {
		numBeforeVisible = this.cachedBeforeAndAfter;
	}
		
	// Items after
	var nextIndexAfter = firstVisibleIndex + visibleCount;
	if (nextIndexAfter >= this.totalNumberOfItems) {
		throw "After total number of items"
	}
	
	var remaining = this.totalNumberOfItems - nextIndexAfter - 1;
		
	if (remaining < this.cachedBeforeAndAfter) {
		// At the end of scrollview, can only download remaining
		numAfterVisible = remaining;
	}
	else {
		// Can download complete set of entries after
		numAfterVisible = this.cachedBeforeAndAfter;
	}
	
	return {
		firstCachedIndex : firstCachedIndex,
		numBeforeVisible : numBeforeVisible,
		
		lastCachedIndex : lastCachedIndex,
		numAfterVisible : numAfterVisible
	};
}

/**
 * Download a number of items.
 * 
 * - cacheIndex - index into cache array at where to store the result
 * - itemIndex - index into virtual array of first item to download
 * - count - number of items to download
 * - fetchImmediately - whether to fetch data immediately (for visible items) or add them to queue (run when outstanding requests are done)
 * 
 * This will add downloads to the queue or schedule immediately, depending
 */


GalleryCacheItems.prototype._downloadItems = function(cacheIndex, itemIndex, itemCount, fetchImmediately, onDownloaded) {

	var fetchFunction;
	
	if (fetchImmediately) {
		fetchFunction = function(index, count) {
			// Call on model to download
			
			startModelDownloadAndRemoveFromDownloadQueueWhenDone(index, count, function(i, c, downloadedData) {

				// Schedule anything from download queue if necessary
				// and no ongoing downloads
				t._scheduleFromDownloadQueue();

				// Call user callback
				onDownloaded(i, c, downloadedData);
			});
		};
	}
	else {
		// Preloading, just add to download queue
		
		var t = this;
		
		fetchFunction = function(index, count) {
			this.downloadQueue.push(new GalleryCacheDownloadReqeust(index, count, onDownloaded));

			// Schedule anything from download queue if necessary
			// and no ongoing downloads
			t._scheduleFromDownloadQueue();
		};
	}

	// Check whether item already downloaded or has an ongoing download
	var state = 'START';
	var firstNotDownloaded;
	
	for (var i = 0; i < itemCount; ++ i) {

		var cached = this.cachedItems[cacheIndex + i];
		
		var downloadedOrDownloading;
			
		if (cached == null) {
			// Is there already a download in progress for this item?
			// If so we can just skip this one
			downloadedOrDownloading = this._isDownloadingItemAt(itemIndex);
		}
		else {
			downloadedOrDownloading = true; // Already downloaded
		}
		
		switch (state) {
		case 'START':
			if (downLoadedOrDownloading) {
				state = 'DOWNLOADED';
			}
			else {
				state = 'NOT_DOWNLOADED';
				firstNotDownloaded = 0; // i == 0 here
			}
			break;
		
		case 'DOWNLOADED':
			if (!downLoadedOrDownLoading) {
				// Switch to array of not-downloade
				state = 'NOT_DOWNLOADED';
				firstNotDownloaded = i;
			}
			break;
			
		case 'NOT_DOWNLOADED':
			if (downloadLoadedOrDownLoading) {
				state = 'DOWNLOADED';
				// Start a download for all the items that we had not downloaded
				
				fetchFunction(itemIndex + firstNotDownloaded, i - firstNotDownloaded);
			}
			break;
			
		default:
			throw "Unknown state '" + state + "'";

		}

		if (state == 'NOT_DOWNLOADED') {
			// Download rest of entries
			fetchFunction(itemIndx + firstNotDownloaded, i - firstNotDownloaded);
		}
	}
}

GalleryCacheItems.prototype._startModelDownloadAndRemoveFromDownloadQueueWhenDone = function(downloadRequest, onDownloaded) {
	// Add to ongoing downloads
	
	if (this.downloadQueue.includes(downloadRequest)) {
		throw "Scheduled download request that is still in download queue";
	}
	
	if (ongoingDownloads.includes(downloadRequest)) {
		throw "Already schedule download request";
	}

	this.ongoingDownloads.push(downloadRequest);
	
	var t = this;
	
	this.modelDownloadItems(downloadRequest.subIndex, downloadRequest.subCount, function(data) {
		
		// Make sure to remove from ongoing requests since we now have a response
		t.ongoingDownloads.remove(downloadRequest);
		
		// Add result to cache if still applicable
		t._addDownloadedDataToCacheIfStillOverlaps(downloadRequest.subIndex, downloadRequest.subCount, data);

		// Check whether we now have received all data for the completed requested area, if so we can call back
		// on download
		var firstCachedIndex = this._getFirstCachedIndex();
		var lastCachedIndex = this._getLastCachedIndex();

		var allDownloaded;
		
		// If anything falls outside of the visible area, we just ignore this and call back anyway since
		// this is handled by caller by looking at sequence numbers, just skipping callbacks if user moved
		// visible area
		
		var allDownloaded = true;
		
		for (var i = 0; i < downloadRequest.totalCount; ++ i) {
			var index = i + downloadRequest.totalIndex;
			
			if (index >= firstCachedIndex && index < lastCacshedIndex) {
				var cacheArrayIndex = index - firstCachedIndex;
				
				if (this.cacheArray[cacheArrayIndex] == null) {
					allDownloaded = false;
					break;
				}
			}
		}
		
		if (allDownloaded) {
			// no null-entries in initially downloaded range, run callback
			downloadRequest.onTotalDownloaded(downloadRequest.totalIndex, downloadRequest.totalCount, data);
		}
	});
}

GalleryCacheItems.prototype._addDownloadedDataToCacheIfStillOverlaps = function(index, count, data) {
	var firstIndexInCache = this._getFirstIndexInCache();
	var lastIndexInCache = this._lastIndexInCache();
	
	if (index > lastIndexInCache || index + count < firstIndexInCount) {
		// No overlap so do nothing
	}
	else if (index >= firstIndexInCache) {
		
		var dstOffset = index - firstIndexInCache;
		
		for (var i = 0, i < count; ++ i) {
			var dstIndex = firstIndexInCache + dstOffset + i;
				
			if (dstIndex > lastIndexInCache) {
				break;
			}
			
			this.cacheArray[dstOffset + i] = data[i];
		}
	}
	else if (index < firstIndexInCache) {
		var srcOffset = firstIndexInCache - index;
		
		for (var i = 0, i < count; ++ i) {
			var srcIndex = index + srcOffset + i;
				
			if (srcIndex < firstIndexInCache) {
				continue;
			}
			
			if (firstIndexInCache + i > lastIndexInCache) {
				throw "Out of range";
			}

			this.cacheArray[i] = data[srcOffset + i];
		}
	}
	else {
		throw "Unreachable code";
	}
	
}


GalleryCacheItems.prototype._scheduleFromDownloadQueue = function() {
	if (this.ongoingDownloads.length == 0 && this.queuedDownloads.length > 0) {
		
		var downloadRequest = this.queuedDownloads[0];

		// Remove first element
		this.queuedDownloads.splice(0, 1);
		
		var t = this;
		
		startModelDownloadAndRemoveFromDownloadQueueWhenDone(downloadRequest, function (downloadRequest, downloadedData)) {
			t._scheduleFromDownloadQueue();
			
			downloadRequest.onDownloaded(downloadRequest, downloadedData);
		});
	}
}

/**
 * A download request in download queue, tracks what is about to be downloaded or in the process
 * of being downloaded. This allows us to handle multiple downloads
 * and to cancel downloads that are no longer needed because user scrolled out of area to be downladed.
 * 
 * Constructor
 *  - totalIndex - when we are downloading a larger chunk but only sub parts are missing, this is index into the total chunk
 *                 eg. start of visible area.
 *  - totalCount - count for the case above, eg number of visible elements
 *  - subIndex - index into virtual array for this particular download (what we will query)
 *  - subCount - number of items to request
 *  - onDownloaded - callback for this particular chunk, will be called with this request as first parameter and data (an array of subCount elements)
 */

function GalleryCacheDownloadReqeust(totalIndex, totalCount, subIndex, subCount, onDownloaded) {
	this.totalIndex = totalIndex;
	this.totalCount = totalCount;
	this.subIndex = firstItemIndex; 
	this.subCount = subCount;
	this.onDownloaded = onDownloaded;
}
