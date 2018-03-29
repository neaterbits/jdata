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
 *    - download async for a number of items after visible area as pre-download for upwards scrolling
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
 * Constructor
 *  - cachedBeforeAndAfter - cache this number before and after visible area, ie if == 20, then we will download in total 40 elements for outside visible area.
 *  
 */

function GalleryCacheItems(cachedBeforeAndAfter, modelDownloadItems) {
	this.totalNumberOfItems = 0;
	
	this.cachedBeforeAndAfter = cachedBeforeAndAfter;
	
	this.curVisibleIndex = 0;
	this.curVisibleCount = 0;

	this.updateSequenceNo = 0;

	// Queue of downloads to be scheduled
	this.downloadQueue = [];
	
	// Downloads that are in progress
	this.ongoingDownloads = [];
	
	this.modelDownloadItems = modelDownloadItems;
	
	this.cachedData = null;
}


GalleryCacheItems.prototype = Object.create(GalleryBase.prototype);

GalleryCacheItems.prototype._clear = function(array, arrayIndex, count) {
	for (var i = 0; i < count; ++ i) {
		array[arrayIndex + i] = null;
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


GalleryCacheItems.prototype._getFirstIndexInCache = function(level, visibleIndex) {
	
	this.enter(level, '_getFirstIndexInCache', ['visibleIndex', visibleIndex]);
	
	var firstCachedIndex;
	
	firstCachedIndex = this.curVisibleIndex - this.cachedBeforeAndAfter;
	
	if (firstCachedIndex < 0) {
		firstCachedIndex = 0;
	}
	
	this.exit(level, '_getFirstIndexInCache', firstCachedIndex);
	
	return firstCachedIndex;
}

GalleryCacheItems.prototype._getLastIndexInCache = function(level, visibleIndex, visibleCount, totalNumberOfItems) {
	
	this.enter(level, '_getLastIndexInCache', [
		'visibleIndex', visibleIndex,
		'visibleCount', visibleCount,
		'totalNumberOfItems', totalNumberOfItems
	],
	[ 'this.cachedBeforeAndAfter', this.cachedBeforeAndAfter ]
	);
	
	var lastCachedIndex;
	
	if (totalNumberOfItems === 0) {
		throw "No cached items";
	}
	else {
		lastCachedIndex = visibleIndex + visibleCount + this.cachedBeforeAndAfter - 1;
	
		if (lastCachedIndex >= totalNumberOfItems) {
			lastCachedIndex = totalNumberOfItems - 1;
		}
	}
	
	this.exit(level, '_getLastIndexInCache', lastCachedIndex);
	
	return lastCachedIndex;
}

GalleryCacheItems.prototype.updateVisibleArea = function(level, firstVisibleIndex, visibleCount, totalNumberOfItems, onAllVisibleDownloaded) {

	this.enter(level, 'updateVisibleArea',
			['firstVisibleIndex', firstVisibleIndex, 'visibleCount', visibleCount, 'totalNumberOfItems', totalNumberOfItems],
			['this.curVisibleIndex', this.curVisibleIndex, 'this.curVisibleCount', this.curVisbleCount]);

	this.withinUpdateVisibleAreaFunction = true;
	
	if (this.cachedData == null) {
		if (this.totalNumberOfItems !== 0) {
			throw "Expected 0 nuber of items for initial invocation";
		}
		
		if (totalNumberOfItems === 0) {
			throw "TODO: handle total number";
		}

		this.cachedData = this._allocateCacheArray(level, firstVisibleIndex, visibleCount, totalNumberOfItems);
	}
	
	var lastVisibleIndex = firstVisibleIndex + visibleCount - 1;

	var layout = this._computeNewCacheArrayLayout(level + 1, firstVisibleIndex, visibleCount);

	// Check for overlap can only be overlap if we had any items at all in the galery
	if (this.totalNumberOfItems > 0) {
		// First see if there is any overlap with current visible area and then figure what to download
		var curFirstCachedIndex = this._getFirstIndexInCache(level + 1, this.curVisibleIndex);
		var curLastCachedIndex  = this._getLastIndexInCache(level + 1, this.curVisibleIndex, this.curVisibleCount, this.totalNumberOfItems);

		var curCached = curLastCachedIndex - curFirstCachedIndex + 1;
		if (this.cachedData.length !== curCached) {
			throw "lastCached - firstCached does not match cached data array: " + this.cachedData.length + "/" + curCached;
		}

		// There are items already so might be overlap between old and new array of cached items
		this.log(level, "Look for overlapping area after scroll: firstVisibleIndex=" + firstVisibleIndex +
				", curFirstCachedIndex=" + curFirstCachedIndex + ", lastVisibleIndex=" + lastVisibleIndex + ", curLastCachedIndex=" + curLastCachedIndex);

		this.cachedData = this._copyOverAnyOverlapping(level + 1, layout, firstVisibleIndex, curFirstCachedIndex, lastVisibleIndex, curLastCachedIndex);

		this._downloadForVisibleAndPreloadAreas(level + 1, layout, firstVisibleIndex, visibleCount, totalNumberOfItems, onAllVisibleDownloaded);
	}
	else {
		
		// No items were downloaded so no overlap
		if (totalNumberOfItems == 0) {
			// No items download so nothing to cache
			throw "TODO: handle empty gallery";
		}
		else {
			
			this.log(level, 'Re-allocate cache array since no overlap');

			// Just swap array for a new one since none of the previously cached ones apply since gallery is empty
			this.cachedData = this._allocateCacheArray(level + 1, firstVisibleIndex, visibleCount, totalNumberOfItems);

			// Download for visible area and preload
			this._downloadForVisibleAndPreloadAreas(level + 1, layout, firstVisibleIndex, visibleCount, totalNumberOfItems, onAllVisibleDownloaded);
		}
	}
	

	// Update visibility and total
	this.curVisibleIndex = firstVisibleIndex;
	this.curVisibleCount = visibleCount;
	this.totalNumberOfItems = totalNumberOfItems;

	this.withinUpdateVisibleAreaFunction = false;

	this.exit(level, ['updateVisibleArea']);
}

GalleryCacheItems.prototype._allocateCacheArray = function(level, firstVisibleIndex, visibleCount, totalNumberOfItems) {
	
	this.enter(level, '_allocateCacheArray', [
		'firstVisibleIndex', firstVisibleIndex,
		'visibleCount', visibleCount,
		'totalNumberOfItems', totalNumberOfItems
	]);
	
	var nextFirstCachedIndex = this._getFirstIndexInCache(level, firstVisibleIndex);
	var nextLastCachedIndex = this._getLastIndexInCache(level + 1, firstVisibleIndex, visibleCount, totalNumberOfItems);
	
	var arraySize = nextLastCachedIndex - nextFirstCachedIndex + 1;
	
	var array = new Array(arraySize);
	this._clear(array, 0, arraySize);

	this.exit(level, '_allocateCacheArray', array.length);

	return array;
}

GalleryCacheItems.prototype._copyOverAnyOverlapping = function(level, layout, firstVisibleIndex, curFirstCachedIndex, lastVisibleIndex, curLastCachedIndex) {
	
	this.enter(level, '_copyOverAnyOverlapping', [
		'layout', JSON.stringify(layout),
		'firstVisibleIndex', firstVisibleIndex,
		'curFirstCachedIndex', curFirstCachedIndex,
		'lastVisibleIndex', lastVisibleIndex,
		'curLastCachedIndex', curLastCachedIndex
	]);
	
	var overlapFirstIndex;
	var overlapLastIndex;

	if (firstVisibleIndex >= curFirstCachedIndex && lastVisibleIndex <= curLastCachedIndex) {
		overlapFirstIndex = firstVisibleIndex;
		overlapLastIndex = lastVisibleIndex;
	}
	else if (firstVisibleIndex < curFirstCachedIndex && lastVisibleIndex >= curFirstCachedIndex && lastVisibleIndex <= curLastCachedIndex) {
		overlapFirstIndex = curFirstCachedIndex;
		overlapLastIndex = lastVisibleIndex;
	}
	else if (firstVisibleIndex > curFirstCachedIndex && firstVisibleIndex <= curLastCachedIndex && lastVisibleIndex > curLastCachedIndex) {
		overlapFirstIndex = firstVisibleIndex;
		overlapLastIndex = curLastCachedIndex;
	}
	else if (firstVisibleIndex < curFirstCachedIndex && lastVisibleIndex > curLastCachedIndex) {
		throw "!! visible area superset of cached area !!";
	}
	else {
		throw "Unhandled area state";
	}
	
	if (overlapLastIndex < overlapFirstIndex) {
		throw "overlapLastIndex < overlapFirstIndex: overlapLastIndex=" + overlapLastIndex + ", overlapFirstIndex=" + overlapFirstIndex;
	}

	var newArrayLength = layout.lastCachedIndex - layout.firstCachedIndex + 1;
	var newArray;

	if (overlapFirstIndex != -1 && overlapLastIndex != -1) {
		// Overlap at some coordinate in the virtual array.
		// Create a new array and jus copy the overlapping area over, fill the rest with null
		
		newArray = new Array(newArrayLength);

		var numOverlapping = overlapLastIndex - overlapFirstIndex + 1;
		
		this.log(level, 'Found number of overlapping ' + numOverlapping + ' from ' + overlapFirstIndex + ' to ' + overlapLastIndex);
		
		// First cache array index of overlapping
		var dstFirstOverlapping = overlapFirstIndex - layout.firstCachedIndex;
		
		// null value for all up to overlap
		
		this.log(level, 'Adding null entries up to ' + dstFirstOverlapping);

		for (var i = 0; i < dstFirstOverlapping; ++ i) {
			newArray[i] = null;
		}
		
		// Copy all overlapping items' refs from current array
		var srcFirstOverlapping = overlapFirstIndex - curFirstCachedIndex;

		this.log(level, 'Adding overlapping entries to dst from ' + dstFirstOverlapping + ", src " + srcFirstOverlapping + ', count ' + numOverlapping);

		for (var i = 0; i < numOverlapping; ++ i) {

			var srcIdx = srcFirstOverlapping + i;
			var dstIdx = dstFirstOverlapping + i;

			this.log(level + 1, 'Copying from array indices ' + srcIdx + ' to ' + dstIdx + ' : ' + this.cachedData[srcIdx]);

			newArray[dstIdx] = this.cachedData[srcIdx];
		}
		
		var dstIdxAfterOverlapping = dstFirstOverlapping + numOverlapping;
		
		this.log(level, 'Adding null entries to dst from ' + dstIdxAfterOverlapping + ', up to ' + (newArray.length - 1));

		// null value for remaining items
		for (var i = dstIdxAfterOverlapping; i < newArray.length; ++ i) {
			newArray[i] = null;
		}
	}
	else if (overlapFirstIndex != -1 || overlapLastIndex != -1) {
		throw "One of overlapFirstIndx and overlapLastIndex set, must set both";
	}
	else {
		// No overlap, just create a new empty array with all nulls
		
		newArray = new Array(newArrayLength);
		
		for (var i = 0; i < newArrayLength; ++ i) {
			this.cachedData[i] = null;
		}
	}

	this.exit(level, '_copyOverAnyOverlapping', printArray(newArray));
	
	return newArray;
}

GalleryCacheItems.prototype._downloadForVisibleAndPreloadAreas = function(level, layout, firstVisibleIndex, visibleCount, totalNumberOfItems, onAllVisibleDownloaded) {

	this.enter(level, '_downloadForVisibleAndPreloadAreas', [
		'layout', JSON.stringify(layout),
		'firstVisibleIndex', firstVisibleIndex,
		'visibleCount', visibleCount,
		'totalNumberOfItems', totalNumberOfItems
	]);
	
	// We need to move items from one cache array to a new one if there is overlap, ie. reuse what can be reused
	// if there is an overlapping in scrolling.
	// Easiest is just to find the overlapping area
	

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
	
	var indexIntoCacheArray = firstVisibleIndex - layout.firstCachedIndex;
	
	this._downloadItems(level + 1, indexIntoCacheArray, firstVisibleIndex, visibleCount, true, function (index, count, data) {
		if (updateSequenceNoAtStartOfDownload < t.updateSequenceNo) {
			// We can ignore this invocation since outdated
			t.log(level, '!! items downloaded but sequence number does not match !! ' + updateSequenceNoAtStartOfDownload + '/' + t.updateSequenceNo);
		}
		else {
			
			if (index !== firstVisibleIndex) {
				throw "Index mismatch";
			}

			if (visibleCount !== count) {
				throw "Count mismatch";
			}

			if (data.length !== count) {
				throw "Mismatch between data.length and count: " + data.length + "/" + count;
			}
			t.log(level, '!! items downloaded and sequence number matches !!');

			// No user updates (scrolling/resize) of visible area since request was called,
			// call back to user so can update screen elements
			onAllVisibleDownloaded(index, count, data);
		}
	});

	this._downloadItems(
			level + 1,
			0,
			layout.firstCachedIndex,
			layout.numBeforeVisible,
			false);
	
	if (layout.firstCachedIndex + layout.numBeforeVisible + visibleCount + layout.numAfterVisible - 1 !== layout.lastCachedIndex) {
		throw "Mismatch in computed indices";
	}
	
	this._downloadItems(
			level + 1,
			layout.numBeforeVisible + visibleCount,
			layout.lastCachedIndex - layout.numAfterVisible + 1,
			layout.numAfterVisible,
			false);

	this.exit(level, '_downloadForVisibleAndPreloadAreas');
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
GalleryCacheItems.prototype._computeNewCacheArrayLayout = function(level, firstVisibleIndex, visibleCount, totalNumberOfItems) {
	
	this.enter(level, '_computeNewCacheArrayLayout', ['firstVisibleIndex', firstVisibleIndex, 'visibleCount', visibleCount]);

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
	if (nextIndexAfter >= totalNumberOfItems) {
		throw "After total number of items: nextIndexAfter=" + nextIndexAfter + ", totalNumberOfItems=" + totalNumberOfItems;
	}
	
	var remaining = totalNumberOfItems - nextIndexAfter - 1;
		
	if (remaining < this.cachedBeforeAndAfter) {
		// At the end of scrollview, can only download remaining
		numAfterVisible = remaining;
	}
	else {
		// Can download complete set of entries after
		numAfterVisible = this.cachedBeforeAndAfter;
	}
	
	var result = {
		firstCachedIndex : firstCachedIndex,
		numBeforeVisible : numBeforeVisible,
		
		lastCachedIndex : firstCachedIndex + numBeforeVisible + visibleCount + numAfterVisible - 1,
		numAfterVisible : numAfterVisible
	};

	this.exit(level, '_computeNewCacheArrayLayout', JSON.stringify(result));
	
	return result;
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


GalleryCacheItems.prototype._downloadItems = function(level, cacheIndex, itemIndex, itemCount, fetchImmediately, onDownloaded) {

	this.enter(level, '_downloadItems', [
		'cacheIndex', cacheIndex,
		'itemIndex', itemIndex,
		'itemCount', itemCount,
		'fetchImmediately', fetchImmediately],
		['this.ongoingDownloads', JSON.stringify(this.ongoingDownloads)]);
	
	if (typeof this.cachedData === 'undefined') {
		throw "Cached data is undefined";
	}
	
	var fetchFunction;
	
	var t = this;
	
	if (fetchImmediately) {
		fetchFunction = function(downloadRequest) {
			// Call on model to download
			
			t._startModelDownloadAndRemoveFromDownloadQueueWhenDone(level + 1, downloadRequest, function(i, c, downloadedData) {

				// Schedule anything from download queue if necessary
				// and no ongoing downloads
				t._scheduleFromDownloadQueue(level + 1);

				// Call user callback
				onDownloaded(i, c, downloadedData);
			});
		};
	}
	else {
		// Preloading, just add to download queue
		
		var t = this;
		
		fetchFunction = function(downloadRequest) {
			t.downloadQueue.push(downloadRequest);

			// Schedule anything from download queue if necessary
			// and no ongoing downloads
			t._scheduleFromDownloadQueue(level + 1);
		};
	}

	// Check whether item already downloaded or has an ongoing download
	var state = 'START';
	var firstNotDownloaded;
	
	for (var i = 0; i < itemCount; ++ i) {

		var cached = this.cachedData[cacheIndex + i];
		
		var downloadedOrDownloading;
	
		if (typeof cached === 'undefined') {
			throw "Undefined item at index " + (cacheIndex + i);
		}
	
		if (cached == null) {
			// Is there already a download in progress for this item?
			// If so we can just skip this one
			downloadedOrDownloading = this._isDownloadingItemAt(level + 1, itemIndex + i);
		}
		else {
			downloadedOrDownloading = true; // Already downloaded
		}

//		console.log('### downloadedOrDownloading ' + i + '/' + (itemIndex + i) + ': ' + downloadedOrDownloading + ', cachedData[' + (cacheIndex + i) + ']=' + cached);

		switch (state) {
		case 'START':
			if (downloadedOrDownloading) {
				state = 'DOWNLOADED';
			}
			else {
				state = 'NOT_DOWNLOADED';
				firstNotDownloaded = 0; // i == 0 here
			}
			break;
		
		case 'DOWNLOADED':
			if (!downloadedOrDownloading) {
				// Switch to array of not-downloade
				state = 'NOT_DOWNLOADED';
				firstNotDownloaded = i;
			}
			break;
			
		case 'NOT_DOWNLOADED':
			if (downloadedOrDownloading) {
				state = 'DOWNLOADED';
				
				// Start a download for all the items that we had not downloaded
				var subIndex = itemIndex + firstNotDownloaded;
				var subCount = i - firstNotDownloaded;
				
				var downloadRequest = new GalleryCacheDownloadRequest(itemIndex, itemCount, subIndex, subCount, onDownloaded);
				
				fetchFunction(downloadRequest);
			}
			break;
			
		default:
			throw "Unknown state '" + state + "'";
		}

	}

	if (state == 'NOT_DOWNLOADED') {
		// Download rest of entries

		var subIndex = itemIndex + firstNotDownloaded;
		var subCount = i - firstNotDownloaded;

//		console.log('### download rest (' + subCount +') from ' + firstNotDownloaded + '/' + itemIndex);

		var downloadRequest = new GalleryCacheDownloadRequest(itemIndex, itemCount, subIndex, subCount, onDownloaded);

		fetchFunction(downloadRequest);
	}

	this.exit(level, '_downloadItems');
}

/**
 * Check whether there is an ongoing download (ie. waiting for async response)
 * for the specified item index (into virtual array). If so, there is no need to start another one.
 */
GalleryCacheItems.prototype._isDownloadingItemAt = function(level, itemIndex) {
	for (var i = 0; i < this.ongoingDownloads.length; ++ i) {
		var downloadRequest = this.ongoingDownloads[i];
		
		// Compare to sub index/count, not total as total may be split in multiple requests (eg.multiple queued request may have the same total)
		if (itemIndex >= downloadRequest.subIndex && itemIndex < downloadRequest.subIndex + downloadRequest.subCount) {
			this.log(level, 'Found in-progress download for item index: ' + itemIndex + ', ' + JSON.stringify(downloadRequest));
			return true;
		}
	}
	
	return false;
}

GalleryCacheItems.prototype._startModelDownloadAndRemoveFromDownloadQueueWhenDone = function(level, downloadRequest, onDownloaded) {
	// Add to ongoing downloads
	
	this.enter(level, '_startModelDownloadAndRemoveFromDownloadQueueWhenDone', [
		'downloadRequest', JSON.stringify(downloadRequest)
	]);
	
	if (this.downloadQueue.includes(downloadRequest)) {
		throw "Scheduled download request that is still in download queue";
	}
	
	if (this.ongoingDownloads.includes(downloadRequest)) {
		throw "Already scheduled download request " + JSON.stringify(downloadRequest) + ": " + JSON.stringify(this.ongoingDownloads); 
	}

	this.ongoingDownloads.push(downloadRequest);
	
	var t = this;
	
	this.modelDownloadItems(downloadRequest.subIndex, downloadRequest.subCount, function(data) {

		var level = 0;
		
		t.enter(level, 'onModelItemsDownloaded',
			[ 'downloadRequest', 		JSON.stringify(downloadRequest) ],
			[ 'this.ongoingDownloads', 	JSON.stringify(t.ongoingDownloads) ]
		);
		
		// Make sure to remove from ongoing requests since we now have a response
		var index = t.ongoingDownloads.indexOf(downloadRequest);
		if (index < 0) {
			throw "Downloadrequest not among on downloads";
		}
		
		var removed = t.ongoingDownloads.splice(index, 1);
		
		if (removed.length !== 1 || removed[0] !== downloadRequest) {
			throw "Item not removed: " + printArray(removed)  + '/' + downloadRequest;
		}

		t._processDownloadResponse(level + 1, downloadRequest, data);

		t.exit(level, 'onModelItemsDownloaded');
	});

	this.exit(level, '_startModelDownloadAndRemoveFromDownloadQueueWhenDone');
}

GalleryCacheItems.prototype._processDownloadResponse = function(level, downloadRequest, data) {
	
	this.enter(level, '_processDownloadResponse', [
		'downloadRequest', JSON.stringify(downloadRequest),
		'data-length', data.length
	]);
	
	if (this.withinUpdateVisibleAreaFunction) {
		// Does not work to find current indices below if we are called back directly from model
		// and not asynchronously (as from Ajax) since that would mean this.curVisibleIndex, this.curVisibleCount and this.totalNumberOfItems
		// would not have been updated yet in the upper stack execution
		throw "Called from within updateItems function stack context, this means that this.curVisibleIndex and this.curVisibleCount below have not been updated yet";
	}

	var firstIndexInCache = this._getFirstIndexInCache(level + 1, this.curVisibleIndex);
	var lastIndexInCache = this._getLastIndexInCache(level + 1, this.curVisibleIndex, this.curVisibleCount, this.totalNumberOfItems);

	// Add result to cache if still applicable
	this._addDownloadedDataToCacheIfStillOverlaps(
			level + 1,
			downloadRequest.subIndex, downloadRequest.subCount,
			data,
			firstIndexInCache, lastIndexInCache);

	// Check whether we now have received all data for the completed requested area, if so we can call back
	// on download

	var allDownloaded;
	
	// If anything falls outside of the visible area, we just ignore this and call back anyway since
	// this is handled by caller by looking at sequence numbers, just skipping callbacks if user moved
	// visible area
	
	var allDownloaded = true;

	var totalDownloadedArray = new Array(downloadRequest.totalCount);
	
	for (var i = 0; i < downloadRequest.totalCount; ++ i) {
		var index = i + downloadRequest.totalIndex;
		
		if (index >= firstIndexInCache && index < lastIndexInCache) {
			var cacheArrayIndex = index - firstIndexInCache;
			
			var cached = this.cachedData[cacheArrayIndex];
			
			if (cached == null) {
				allDownloaded = false;
				break;
			}
			
			totalDownloadedArray[i] = cached.data;
		}
	}
	
	if (allDownloaded) {
		// no null-entries in initially downloaded range, run callback on all data
		downloadRequest.onTotalDownloaded(downloadRequest.totalIndex, downloadRequest.totalCount, totalDownloadedArray);
	}

	this.exit(level, '_processDownloadResponse');
}

GalleryCacheItems.prototype._addDownloadedDataToCacheIfStillOverlaps = function(level, index, count, data, firstIndexInCache, lastIndexInCache) {
	
	this.enter(level, '_addDownloadedDataToCacheIfStillOverlaps', [
		'index', index,
		'count', count,
		'data-lenght', data.length,
		'firstIndexInCache', firstIndexInCache,
		'lastIndexInCache', lastIndexInCache
	]);
	
	if (index > lastIndexInCache || index + count < firstIndexInCache) {
		// No overlap so do nothing
		this.enter(level, 'No overlap with current cache index so skip');
	}
	else if (index >= firstIndexInCache) {
		
		var dstOffset = index - firstIndexInCache;
		
		for (var i = 0; i < count; ++ i) {
			var dstIndex = firstIndexInCache + dstOffset + i;

			if (dstIndex > lastIndexInCache) {
				break;
			}
			
			var dataElement = data[i];

			this.log(level, 'Copy from post-overlapping data[' + i + '] to this.cachedData[' + dstIndex + '] at item index ' + (index + i)
					+ ': ' + (dataElement != null ? '<nonnull>' : '<null>'));
			
			this._setCachedDataItem(dstOffset + i, new GalleryCacheItem(dataElement));
		}
	}
	else if (index < firstIndexInCache) {
		var srcOffset = firstIndexInCache - index;
		
		for (var i = 0; i < count; ++ i) {
			var srcIndex = index + srcOffset + i;
				
			if (srcIndex < firstIndexInCache) {
				continue;
			}
			
			if (firstIndexInCache + i > lastIndexInCache) {
				throw "Out of range";
			}

			var dataElement = data[srcOffset + i];
			this.log(level, 'Copy from pre-overlapping data[' + i + '] to this.cachedData[' + dstIndex + '] at item index ' + (index + i)
					+ ': ' + (dataElement != null ? '<nonnull>' : '<null>'));

			this._setCachedDataItem(cachedData[i], new GalleryCacheItem(dataElement));
		}
	}
	else {
		throw "Unreachable code";
	}
	
	this.exit(level, '_addDownloadedDataToCacheIfStillOverlaps');
}

GalleryCacheItems.prototype._setCachedDataItem = function(arrayIndex, item) {
	if (typeof item === 'undefined') {
		throw "Adding undefined at " + arrayIndex;
	}

	this.cachedData[arrayIndex] = item;
}

GalleryCacheItems.prototype._scheduleFromDownloadQueue = function(level) {
	
	this.enter(level, '_scheduleFromDownloadQueue', ['inprogress', this.ongoingDownloads.length, 'queued', this.downloadQueue.length]);
	
	if (this.ongoingDownloads.length == 0 && this.downloadQueue.length > 0) {
		
		var downloadRequest = this.queuedDownloads[0];

		// Remove first element
		this.queuedDownloads.splice(0, 1);
		
		var t = this;
		
		this._startModelDownloadAndRemoveFromDownloadQueueWhenDone(level + 1, downloadRequest, function (downloadRequest, downloadedData) {
			
			t._scheduleFromDownloadQueue();
			
			downloadRequest.onDownloaded(downloadRequest, downloadedData);
		});
	}
	
	this.exit(level, '_scheduleFromDownloadQueue');
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

function GalleryCacheDownloadRequest(totalIndex, totalCount, subIndex, subCount, onDownloaded) {
	this.totalIndex = totalIndex;
	this.totalCount = totalCount;
	this.subIndex = subIndex; 
	this.subCount = subCount;
	this.onTotalDownloaded = onDownloaded;
}

/**
 * Must have a class for cache array items since 
 * downloaded data may be null and we need to test for null/non-null in cache data array
 * with regards to whether downloaded or not (as opposed to a download that returned null-items, eg. an item with no thumbnail image)
 * 
 */

function GalleryCacheItem(data) {
	this.data = data;
}


function printArray(a) {
	var s;
	
	if (a == null) {
		s = 'null';
	}
	else {
		s = '';
		
		for (var i = 0; i < a.length; ++ i) {
			if (i > 0) {
				s+= ',';
				
				s += '' + a[i]; 
			}
		}
	}
	
	return s;
}
