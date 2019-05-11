/**
 * Search view of combined facet and gallery views.
 * 
 * This has to be combined since they require the same model
 * 
 */

function SearchView(
		searchUrl,
		thumbsUrl,
		ajax,
		facetsDiv,
		galleryDivId,
		searchTextInputId,
		searchButtonId,
		searchHitsCountId,
		sortListboxId,
		galleryItemFactory) {

	// Reasons for refreshing
	var REFRESH_INITIAL = 0; 			// Initial refresh, ie. when retrieving initial model
	var REFRESH_SEARCH_TEXT_CHANGE = 1; // Search button pressed
	var REFRESH_API_TRIGGERED = 2; 		// Refresh called from outside
	var REFRESH_FACET_SELECTION = 3; 	// User selected checkbox in facets
	var REFRESH_SORT_CHANGE = 4;		// User selected other sort order
	
	var facetModel = new FacetModel();

	// creates HTML elements
	var viewElements = new FacetViewElements();
	
	var t = this;
	
	// View related logic
	var facetView = new FacetView(facetsDiv, viewElements, function(criteria) {
		console.log("Criteria updated: " + criteria);
		t._refreshFromCriteria(criteria.types, criteria.criteria, REFRESH_FACET_SELECTION, function () {
			console.log('refresh done');
		});

		console.log("called refresh");
	});

	var facetController = new FacetController(facetModel, facetView);
	facetView.init(facetController);

	this.searchUrl = searchUrl;
	this.thumbsUrl = thumbsUrl;
	
	this.ajax = ajax;
	this.galleryItemFactory = galleryItemFactory;
	
	this.facetView = facetView;
	this.facetModel = facetModel;
	this.facetController = facetController;
	this.gallery = _initGallery(this, galleryDivId);

	this.hasPerformedInitialSearch = false;
	this.curResponse = null;

	this.searchTextInputElement = document.getElementById(searchTextInputId);
	this.searchHitsCountElement = document.getElementById(searchHitsCountId);
	this.sortListboxElement = document.getElementById(sortListboxId);
	
	var searchButtonElement = document.getElementById(searchButtonId);
	
	searchButtonElement.onclick = function(e) {
		if (t.curResponse != null) {
			t._refresh(REFRESH_SEARCH_TEXT_CHANGE);
		}
	}
	
	/* Click search button instead
	this.searchTextInputElement.onchange = function(e) {
		if (t.curResponse != null) {
			t.refresh(false);
		}
	};
	*/

	this.sortListboxElement.onchange = function(e) {
		// Updated sort order, change sort order of result
		// Call for new result set from server
		if (t.curResponse != null) {
			t._refresh(REFRESH_SORT_CHANGE);
		}
	};

	this._downloadAll = function(onsuccess) {

		var t = this;

		// Post to get initial for all known
		var url = this.searchUrl + '?itemType=_all_';
		url = this._appendFields(url);
		
		this._postAjax(url, function(response) {
			t.curResponse = response;

			t._updateFacets(response.facets, REFRESH_INITIAL);

			// Refresh gallery, will call back to galleryModel (ie. in this file) that were passed to Gallery constructor
			t.gallery.refresh(response.items.length)

			t._updateSearchHitsCount(response);
			t._updateSortOrderFromResponse(response.sortOrders);

			onsuccess();
		});
	}

	this.getItems = function() {
		return response.items;
	}

	this.setSearchUrl = function(url) {
		this.searchUrl = url;
	}

	this._getCurrentSortOrder = function() {
		var index = this.sortListboxElement.selectedIndex;
		
		return index >= 0 ? this.sortListboxElement.options[index].value : null;
	}

	this._refreshFromCriteria = function(types, criteria, reason, onsuccess) {
		
		var t = this;
		
		var url = this.searchUrl;
		
		for (var i = 0; i < types.length; ++ i) {
			url += i == 0 ? "?" : "&";
			url += "itemType=";
			url += types[i];
		}
		
		// Get currently selected sort attribute
		var sortOrder = this._getCurrentSortOrder();
		
		if (sortOrder != null && sortOrder !== '') {
			url += "&sortOrder=" + sortOrder;
		}

		url = this._appendFields(url);

		var freeText = this.searchTextInputElement.value;

		if (freeText != '') {
			url += '&freeText=' + encodeURIComponent(freeText);
		}

		// Call REST service with criteria
		this._sendAjax(url, 'POST', 'json', 'application/json', JSON.stringify(criteria), function(response) {
			
			t.curResponse = response;
			
			t._updateFacets(response.facets, reason);

			t.gallery.refresh(response.items.length);
			
			t._updateSearchHitsCount(response);
			t._updateSortOrderFromResponse(response.sortOrders);
			
			onsuccess();
		});
	}
	
	this._appendFields = function(url) {
		var fields = this.galleryItemFactory.getItemFields();
		
		if (fields != null && fields.length > 0) {
			for (var i = 0; i < fields.length; ++ i) {
				console.log('## append ' + fields[i]);
				url += '&field=' + fields[i];
			}
		}

		return url;
	}
	
	this._updateSearchHitsCount = function(response) {
		this.searchHitsCountElement.innerHTML = '' + response.totalItemMatchCount;
	}
	
	this._updateSortOrderFromResponse = function(sortOrders) {
		
		var existingSelection = this._getCurrentSortOrder();

		while (this.sortListboxElement.firstChild) {
			this.sortListboxElement.removeChild(this.sortListboxElement.firstChild);
		}

		var indexOfExistingSelection = -1;

		for (var i = 0; i < sortOrders.length; ++ i) {
			var sortOrder = sortOrders[i];
			
			var option = this._createOptionElement(sortOrder.name, sortOrder.displayName);

			if (sortOrder.name === existingSelection) {
				indexOfExistingSelection = i;
			}
			
			append(this.sortListboxElement, option);
		}
		
		if (indexOfExistingSelection != -1) {
			// Existing selection still available, set that
			this.sortListboxElement.selectedIndex = indexOfExistingSelection;
		}
	}
	
	this._createOptionElement = function(name, text) {
		var option = document.createElement('option');
		
		option.value = name;
		option.innerHTML = text;
		
		return option;
	}

	this._updateFacets = function(facets, reason) {
		
		console.log('## updateFacets, reason=' + reason);
		
		this.facetModel.updateFacets(facets);
		
		// If this is not a facet selection or just a sort order change, we will have to update facets completely
		var isFullUpdate = reason != REFRESH_FACET_SELECTION && reason != REFRESH_SORT_CHANGE;
		
		this.facetController.refresh(isFullUpdate);
	}
	
	// Search based on current criteria
	this.refresh = function() {
		this._refresh(REFRESH_API_TRIGGERED);
	}

	this._refresh = function(reason) {

		var t = this;
		var completeRefresh =    reason === REFRESH_API_TRIGGERED
							  || reason === REFRESH_INITIAL;

		if (!completeRefresh && this.hasPerformedInitialSearch) {
			// Get selected criteria from facet controller
			var criteria = t.facetView.collectCriteriaAndTypesFromSelections();

			this._refreshFromCriteria(criteria.types, criteria.criteria, reason, function() {});
		}
		else {
			this._downloadAll(function() {
				t.hasPerformedInitialSearch = true;
			});
		}
	}

	this._postAjax = function(url, onsuccess) {
		this._sendAjax(url, 'POST', 'json', null, null, onsuccess);
	}
	
	this._sendAjax = function(url, method, responseType, requestContentType, requestContent, onsuccess) {
		ajax.sendAjax(url, method, responseType, requestContentType, requestContent, onsuccess);
	};

	function _initGallery(searchView, galleryDivId) {
		
		var appendToContainer = function(container, element) { container.append(element); };
		var setElementHeight = function(element, heightPx) {
			element.style.height = heightPx + 'px';
		};
		
		return new Gallery(
				galleryDivId,
				galleryItemFactory.getGalleryConfig(),
				// Create element
				{ 
					getProvisionalData 	: function (index, count, onsuccess) { searchView._getGalleryProvisionalData(index, count, onsuccess); },
					getCompleteData 	: function (index, count, onsuccess) { searchView._getGalleryThumbnailImages(index, count, onsuccess); }
				},
				{
					makeProvisionalHTMLElement 	: _makeGalleryProvisionalItem,
					makeCompleteHTMLElement 	: _makeGalleryImageItem,
					
					// Element access methods
					createUpperPlaceHolder : function () { return document.createElement('div'); },
					createRowContainer : function (rowNo) { return document.createElement('div'); },
					
					clearRenderContainer : function (container) {
						while (container.firstChild) {
							container.removeChild(container.firstChild);
						}
					},
					appendPlaceholderToRenderContainer	: appendToContainer,
					appendRowToRenderContainer 			: appendToContainer,
					appendItemToRowContainer 			: appendToContainer,
					
					prependRowToRenderContainer : function(container, row, curFirstRow) { container.insertBefore(row, curFirstRow); },

					getNumElements : function(container) { return container.childNodes.length; },
					replaceProvisionalWithComplete : function(container, index, element) { container.replaceChild(element, container.childNodes[index]);  },
					getElement : function(container, index) { return container.childNodes[index]; },
					getElementWidth  : function(element) {  return element.clientWidth;  },
					getElementHeight : function(element) {  return element.clientHeight; },
					removeRowFromContainer : function(container, element) { container.removeChild(element); },

					setRenderContainerHeight : setElementHeight,
					setPlaceHolderHeight : setElementHeight,

					setCSSClasses : function(element, classes) {
						element.setAttribute('class', classes);
					},
					
					applyItemStyles : function(itemElement, rowHeight, itemWidth, itemHeight, spacing, visible) {
					
						itemElement.setAttribute('class', 'galleryItem');
						
						itemElement.style.position = 'relative';
						itemElement.style.float = 'left';
						itemElement.style['margin-left'] = spacing + 'px';
						
						if (itemHeight != null) {
							itemElement.style.top = '' + (rowHeight - itemHeight) / 2 + 'px';
							itemElement.style.height = '' + itemHeight + 'px';
						}
						
						if (itemWidth != null) {
							
							// TODO: setter ikke width her
							itemElement.style.width = itemWidth + 'px';
						}
						
						if (!visible) {
							// set hidden if we need to find item size
							itemElement.style.visibility = 'hidden';
						}
					},
					
					applyRowContainerStyling : function(rowDiv, y, width, height) {
						
						rowDiv.setAttribute('class', 'galleryRowDiv');
						
						rowDiv.setAttribute('style',
								//'position : relative; ' +
								'top :  ' + y + 'px; ' +
								'width : ' + width + 'px; ' +
								'height : ' + height + 'px;');
	
					}
				}
		);
	}

	function _makeGalleryProvisionalItem(index, data) {
		return galleryItemFactory.makeProvisionalItem(index, data);
	}
	
	function _makeGalleryImageItem(index, provisionalData, imageData) {
		return galleryItemFactory.makeImageItem(index, provisionalData, imageData);
	}
	
	this._getGalleryProvisionalData = function(index, count, onsuccess) {
		// Gallery was updated as a result of a query result and this.curResponse was updated,
		// so just pass that back right away
		
		if (index !== 0 || count !== this.curResponse.items.length) {
			throw "Expected to get all data: " + index + "/" + count + "/" + this.curResponse.items.length;
		}

		onsuccess(this.curResponse.items);
	}
	
	this._getGalleryThumbnailImages = function(index, count, onsuccess) {
		var itemIds = "";
		
		for (var i = 0; i < count; ++ i) {
			var itemId = this.curResponse.items[index + i].id;

			itemIds += i == 0 ? "?" : "&";
			
			itemIds += "itemId=";
			itemIds += itemId;
		}

		var url = this.thumbsUrl + itemIds;


		function hexdump(buffer, start, count) {

			var s = "";
			var hexView = new DataView(response);

			console.log('Hex dump:');
			var lineStart = 0;
			for (var i = 0; i < 200; ++ i) {
				
				var b = hexView.getInt8(i);

				var digit1 = hexDigit(b >> 4);
				var digit2 = hexDigit(b & 0x0000000F);

				s += digit1;
				s += digit2;
				
				if (s.length >= 32) {
					console.log("" + i + "/" + lineStart + ": " + s);
					s = "";
					lineStart = i;
				}
			}
		}
		
		this._sendAjax(url, 'GET', 'arraybuffer', null, null, function(response) {

			var dataView = new DataView(response);
			var offset = 0;
			
			var images = [];

			for (;;) {
				var thumbSize = dataView.getInt32(offset);
				
				offset += 4;
				
				var numItemThumbnails = dataView.getInt32(offset);
				
				offset += 4;

				var mimeType = "";
				// read content type as 0-terminated string
				for (;;) {
					var int8 = dataView.getInt8(offset ++);
					if (int8 == 0) {
						break;
					}
					else {
						mimeType += String.fromCharCode(int8);
					}
				}
				
				if (thumbSize > 0) {
					var base64 = base64_encode(dataView, offset, thumbSize);
					
					// console.log('## base 64: ' + base64.length + " from " + offset + ", size " + thumbSize + ' :\n' + base64);
					
					// Render thumb in view, create the 'data:' part of <img>
					var data = 'data:' + mimeType + ';base64,' + base64;
					
					// var buf = response.slice(offset, offset + thumbSize);
					// var data = new Blob([new Uint8Array(response, offset, thumbSize)]);
					
					images.push({ 'data' : data, 'numItemThumbnails' : numItemThumbnails });
				}
				else {
					images.push(null);
				}

				offset += thumbSize;

				if (offset >= response.byteLength) {
					break;
				}
			}

			onsuccess(images);
		});
	}


	function print(obj) {
		return JSON.stringify(obj, null, 2);
	}

	
	
	
	function hexNum(n) {

		var chars = [];
		
		if (n == 0) {
			chars.push("0");
		}
		else{
			while (n > 0) {
				chars.push(hexDigit(n % 16));
				
				n >>= 4;
			}
		}

		return chars.reverse().join("");
	}
	
	function hexDigit(x) {
		var s;
		
		if (x < 10) {
			s = "" + x;
		}
		else if (x > 15) {
			throw "Digit out of range: " + x;
		}
		else {
			s = String.fromCharCode(65 + (x - 9));
		}
		
		return s;
	}
		
	function append(parent, element) {

		if (typeof parent === 'undefined') {
			throw 'append: parent == undefined';
		}

		if (typeof parent.appendChild === 'undefined') {
			throw 'append: not an element';
		}
		parent.appendChild(element);
	}
}
