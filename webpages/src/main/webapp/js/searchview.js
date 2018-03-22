/**
 * Search view of combined facet and gallery views.
 * 
 * This has to be combined since they require the same model
 * 
 */


function SearchView(
		searchUrl,
		thumbsUrl,
		facetModel,
		facetController,
		galleryDivId) {

	this.searchUrl = searchUrl;
	this.thumbsUrl = thumbsUrl;
	this.facetModel = facetModel;
	this.facetController = facetController;
	this.gallery = _initGallery(this, galleryDivId);

	this.hasPerformedInitialSearch = false;
	this.curResponse = null;

	this._getInitial = function(onsuccess) {

		var t = this;

		// Post to get initial for all known
		this._postAjax(this.searchUrl, function(response) {
			t.curResponse = response;

			t._updateFacets(response.facets, onsuccess);

			t._refreshGallery(response.items);
		});
	}

	this.getItems = function() {
		return response.items;
	}

	this.setSearchUrl = function(url) {
		this.searchUrl = url;
	}

	this._refreshFromCriteria = function(types, criteria, onsuccess) {
		
		var t = this;
		
		// Call REST service with criteria
		this._postAjax(serviceURL, 'POST', criteria, function(response) {
			t._updateFacets(response.facets, onsuccess);

			t._refreshGallery(response.items);
		});
	}

	this._updateFacets = function(facets, onsuccess) {
		this.facetModel.updateFacets(facets);

		this.facetController.refresh();

		onsuccess();
	}
	
	// Search based on current criteria
	this.refresh = function(completeRefresh) {

		var t = this;

		if (!completeRefresh && this.hasPerformedInitialSearch) {
			// Get selected criteria from facet controller
			var criteria = this.facetController.collectCriteriaAndTypesFromSelections();

			this._refreshFromCriteria(criteria.types, criteria.criteria, function() {});
		}
		else {
			this._getInitial(function() {
				t.hasPerformedInitialSearch = true;
			});
		}
	}

	this._postAjax = function(url, onsuccess) {
		this._sendAjax(url, 'POST', 'json', onsuccess);
	}
	
	this._sendAjax = function(url, method, responseType, onsuccess) {
		var request = new XMLHttpRequest();

		if (responseType != null) {
			request.responseType = responseType;
		}

		request.onreadystatechange = function() {

			if (this.readyState == 4 && this.status == 200) {
				onsuccess(this.response);
			}
		};

		request.open(method, url, true);

		request.send();
	};
	
	
	function _initGallery(searchView, galleryDivId) {
		
		return new Gallery(galleryDivId, 20, 20,
				// Create element
				_makeGalleryProvisionalItem,
				function (index, count) { searchView._getThumbnailImages(index, count); },
				function (index, provisional, image) {
					return provisional;
				});
	}
	
	function _makeGalleryProvisionalItem(index, title, thumbWidth, thumbHeight) {
		var div = document.createElement('div');

		var provisionalImage = document.createElement('div');

		provisionalImage.style.width = thumbWidth;
		provisionalImage.style.height = thumbHeight;

		div.append(provisionalImage);
		
		var textDiv = document.createElement('div');

		// Add index as a text to the element
		var textSpan = document.createElement('span');
		
		textSpan.innerHTML = title;

		textDiv.append(textSpan);

		div.append(textDiv);

		textDiv.setAttribute('style', 'text-align : center;');
		
		return div;
	}
	
	this._getThumbnailImages = function(index, count) {
		var itemIds = "";
		
		console.log('## adding itemIds from ' + index + ", " + count + " items");

		for (var i = 0; i < count; ++ i) {
			var itemId = this.curResponse.items[index + i].id;

			console.log('# adding at ' + (index + i));

			itemIds += "&itemId=";
			itemIds += itemId;
		}

		var url = this.thumbsUrl + itemIds;

		function _hexDigit(x) {
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
		
		function hexdump(buffer, start, count) {

			var s = "";
			var hexView = new DataView(response);

			console.log('Hex dump:');
			var lineStart = 0;
			for (var i = 0; i < 200; ++ i) {
				
				var b = hexView.getInt8(i);

				var digit1 = _hexDigit(b >> 4);
				var digit2 = _hexDigit(b & 0x0000000F);

				s += digit1;
				s += digit2;
				
				if (s.length >= 32) {
					console.log("" + i + "/" + lineStart + ": " + s);
					s = "";
					lineStart = i;
				}
			}
		}
		
		this._sendAjax(url, 'GET', 'arraybuffer', function(response) {

			console.log('## Got images response: ' + response.byteLength);

			var dataView = new DataView(response);
			var offset = 0;

			for (;;) {
				var thumbSize = dataView.getInt32(offset);
				
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

				console.log('## ' + offset + ': got thumb of size ' + thumbSize + " with mime type '" + mimeType + "'");
				
				offset += thumbSize;
				
				if (thumbSize > 0) {
					// Render thumb in view
				}
				
				if (offset >= response.byteLength) {
					break;
				}
			}
			
		});
	}

	this._refreshGallery = function(items) {

		this.gallery.refresh(function (initial, eachItem, metaDataComplete) {
			// Add a lot of items just to test scrolling when using many items
			
			console.log("Gallery: adding " + items.length + " items");

			// Tell gallery about number of items before adding item metadata
			initial(items.length);
			
			// Add all metadata, ie. title and dimensions
			for (var i = 0; i < items.length; ++ i) {
				var item = items[i];

				console.log("Gallery: adding item " + i + " : " + print(item));

				eachItem(item.title, item.thumbWidth, item.thumbHeight);
			}

			// Tell gallery that we have added all metadata and that it can perform the refresh
			metaDataComplete();
		});
	}

	function print(obj) {
		return JSON.stringify(obj, null, 2);
	}
}
