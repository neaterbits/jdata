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
 *  - firstCachedIndex - index into virtual array of images
 *  - count - number of items to get images for, starting at firstCachedIndex
 *  - onsuccess - function to be called back with an array of elements that represents the complete data (user specific), must be <count> length.
 */

function Gallery(divId, config, galleryModel, galleryView) {
	
	this.divId = divId;
	
	this.config = config;
	
	// Start out by setting cache to null since we will
	// create this upon initial refresh with total number of items

	this.galleryModel = galleryModel;
	this.galleryView = galleryView;
	
	this.cachedRowDivs = new Array();

	this.firstCachedIndex = 0; // index of first visible element
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

	// Set inner and outer dimensions
	var outerDiv = document.getElementById(this.divId);

	outerDiv.style.width = '100%';
	outerDiv.style.height = '100%';
	outerDiv.style.overflow = 'auto';
	outerDiv.style['background-color'] = 'blue';
	
	this.innerDiv.style.width = '100%';
	this.innerDiv.style.height = '100%';
	this.innerDiv.style.display = 'block';
	
	var t = this;

	// Add scroll listener. cache may not have been created yet but ought to have been
	// before user starts scrolling
	outerDiv.addEventListener('scroll', function(e) {
		// figure out how far we have scrolled into the div
		var clientRects = t.innerDiv.getBoundingClientRect(); // innerDiv.getClientRects()[0];
		var viewYPos = - (clientRects.top - t.innerDiv.offsetTop);
		
		t.cache.updateOnScroll(0, viewYPos);
	});

	
	/**
	 * Refresh gallery, typically when some search criteria has changed.
	 * Since caches may apply different download strategies (some download all provisional and complete data, others only partially)
	 * we will let the cache retrieve data from model.
	 * 
	 */ 
	// Since 
	this.refresh = function(totalNumberOfItems) {

		var level = 0;
		
		this.enter(level, 'refresh', ['totalNumberOfItems', totalNumberOfItems]);

		// TODO here we could choose cache implementations, eg if showing far fewer or far more items
		if (this.cache == null) {
			// Initial refresh
			this.cache = new GalleryCacheAllProvisionalSomeComplete(this.config, this.galleryModel, this.galleryView, totalNumberOfItems);
			
			this.cache.setRenderDiv(this.innerDiv);
		}
		else {
			// Just refresh existing cache
		}
		
		// Refresh, passing in a function for retrieving provisional items
		this.cache.refresh(level + 1, totalNumberOfItems, this.widthMode, this.heightMode);

		this.exit(level, 'refresh');
	};
}


Gallery.prototype = Object.create(GalleryBase.prototype);

