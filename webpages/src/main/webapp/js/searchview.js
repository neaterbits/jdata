/**
 * Search view of combined facet and gallery views.
 * 
 * This has to be combined since they require the same model
 * 
 */


function SearchView(
		searchUrl,
		thumbsUrl,
		facetsDiv,
		galleryDivId) {

	var facetModel = new FacetModel();

	// creates HTML elements
	var viewElements = new FacetViewElements();
	
	var t = this;
	
	// View related logic
	var facetView = new FacetView(facetsDiv, viewElements, function(criteria) {
		console.log("Criteria updated: " + criteria);
		t._refreshFromCriteria(criteria.types, criteria.criteria, function () {
			console.log('refresh done');
		});

		console.log("called refresh");
	});

	var facetController = new FacetController(facetModel, facetView);
	facetView.init(facetController);

	this.searchUrl = searchUrl;
	this.thumbsUrl = thumbsUrl;
	this.facetModel = facetModel;
	this.facetController = facetController;
	this.gallery = _initGallery(this, galleryDivId);

	this.hasPerformedInitialSearch = false;
	this.curResponse = null;
	
	/*
	var buf = new ArrayBuffer(4);
	
	var view = new DataView(buf);
	
	view.setInt8(0, 97);
	view.setInt8(1, 98);
	view.setInt8(2, 99);
	view.setInt8(3, 100);
	
	console.log('Base 64: ' + base64_encode(view, 0, 4));
	
	throw "exit searchview";
	*/

	this._getInitial = function(onsuccess) {

		var t = this;

		// Post to get initial for all known
		this._postAjax(this.searchUrl, function(response) {
			t.curResponse = response;

			t._updateFacets(response.facets, onsuccess);

			// Refresh gallery, will call back to galleryModel (ie. in this file) that were passed to Gallery constructor
			t.gallery.refresh(response.items.length)
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
		
		var url = this.searchUrl;
		
		for (var i = 0; i < types.length; ++ i) {
			url += i == 0 ? "?" : "&";
			url += "itemType=";
			url += types[i];
		}

		// Call REST service with criteria
		this._sendAjax(url, 'POST', 'json', 'application/json', JSON.stringify(criteria), function(response) {
			
			t._updateFacets(response.facets, onsuccess);

			//t._refreshGallery(response.items);
		});
	}

	this._updateFacets = function(facets, onsuccess) {
		this.facetModel.updateFacets(facets);

		this.facetController.refresh(false); // TODO true if caused by full-search update, must pass search criteria in response? Or some userData

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
		this._sendAjax(url, 'POST', 'json', null, null, onsuccess);
	}
	
	this._sendAjax = function(url, method, responseType, requestContentType, requestContent, onsuccess) {
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

		request.send(requestContent);
	};
	
	
	function _initGallery(searchView, galleryDivId) {
		
		return new Gallery(
				galleryDivId,
				{
					columnSpacing : 20,
					rowSpacing : 20,
					widthHint : 300,
					heightHint : 300
				},
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
					createRowContainer : function () { return document.createElement('div'); },
					
					appendToContainer : function(container, element) { container.append(element); },
					getElementWidth  : function(element) {  return element.clientWidth;  },
					getElementHeight : function(element) {  return element.clientHeight; },
					
					setElementHeight : function(element, heightPx) {
						element.style.height = heightPx + 'px';
					},

					setCSSClasses : function(element, classes) {
						element.setAttribute('class', classes);
					},
					
					applyItemStyles : function(itemElement, rowHeight, itemWidth, itemHeight, spacing, visible) {
					
						itemElement.setAttribute('class', 'galleryItem');
						
						var styling = 'position : relative; ' +
							/*
							'display : inline-block; ' +
							*/
							'float : left; ' +
							'margin-left : ' + spacing + 'px; ';
						
						if (itemHeight != null) {
							styling += 'top : ' + (rowHeight - itemHeight) / 2 + 'px; ';
							styling += 'height : ' + itemHeight + 'px; ';
						}
						
						if (itemWidth != null) {
							
							// TODO: setter ikke width her
							'width : ' + itemWidth + 'px; ';
						}
						
						if (!visible) {
							// set hidden if we need to find item size
							styling += 'visibility: hidden; '
						}
							
						itemElement.setAttribute('style', styling);
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
		var div = document.createElement('div');

		var provisionalImage = document.createElement('div');
		
		div.setAttribute('class', 'provisionalImage');

		provisionalImage.style.width = data.thumbWidth;
		provisionalImage.style.height = data.thumbHeight;

		div.append(provisionalImage);
		
		var textDiv = document.createElement('div');

		// Add index as a text to the element
		var textSpan = document.createElement('span');
		
		textSpan.innerHTML = data.title;

		// Don't make text wider that thumb
		textDiv.style.width = data.thumbWidth;
		textDiv.style['text-align'] = 'center';
			
		textDiv.append(textSpan);

		div.append(textDiv);

		//textDiv.setAttribute('style', 'text-align : center;');
		
		return div;
	}
	
	function _makeGalleryImageItem(index, provisionalData, imageData) {
		var div = document.createElement('div');
		
		var image = document.createElement('img');

		image.setAttribute('class', 'thumbnailImage');

		image.width = provisionalData.thumbWidth;
		image.height = provisionalData.thumbHeight;
		
		/*
		provisionalImage.style.width = thumbWidth;
		provisionalImage.style.height = thumbHeight;
		 */
		
		/*
		var url = URL.createObjectURL(imageData);
		image.src = url;

		image.onload = function() {
			URL.revokeObjectURL(url);
		}
		 */
		
		image.src = imageData;
		
		div.append(image);
		
		var textDiv = document.createElement('div');
		
		// Add index as a text to the element
		var textSpan = document.createElement('span');
		
		textSpan.innerHTML = provisionalData.title;

		textDiv.style.width = provisionalData.thumbWidth;
		textDiv.style['text-align'] = 'center';

		textDiv.append(textSpan);
		
		div.append(textDiv);

		return div;
	}
	
	this._getGalleryProvisionalData = function(index, count, onsuccess) {
		// Gallery was updated as a result of a query result and this.curResponse was updated,
		// so just pass that back right away
		
		if (index !== 0 || count !== this.curResponse.items.length) {
			throw "Expected to get all data";
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
					
					images.push(data);
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

	
	
	// For base-64 encoding bytes
	function base64_encode(dataView, start, length) {

		const base64chars = [
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'+', '/'
		];

		var result = "";
		
		var buf = { buffer : 0, bufferedBits : 0 };
		
		for (var i = 0; i < length;) {
			
			// console.log('start of loop, ' + i + ': ' + buf.buffer + '/0x' + hexNum(buf.buffer) + ', ' + buf.bufferedBits);
			
			if (buf.bufferedBits <= 8) {
				// May read another byte, shift remaining up to upper
				
				var b = dataView.getUint8(start + i);
				
				// or in read byte after current number of bits
				var freeSpace = 16 - buf.bufferedBits;
				var r = b << (freeSpace - 8);

				buf.buffer |= r;
				
				// Now is 8 more
				buf.bufferedBits += 8;
			
				// One more element processed
				++ i;
			}

			// console.log('after read, ' + i + ': ' + buf.buffer + '/0x' + hexNum(buf.buffer) + ', ' + buf.bufferedBits);
			
			result += _base64_one(buf, base64chars);
		}
		
		// Now convert any remaining data
		while (buf.bufferedBits > 0) {
			result += _base64_one(buf, base64chars);
		}
		
		var mod = result.length % 4;
		if (mod != 0) {
			var remainder = 4 - remainder;

			for (var i = 0; i < remainder; ++ i) {
				result += '=';
			}
		}

		return result;
	}
	
	function _base64_one(buf, base64chars) {

		// Can now encode from bits
		var c = buf.buffer >> 10;
		// console.log('Encode 0x' + hexNum(c) + ' : ' + base64chars[c]);
		
		if (c < 0 || c >= 64) {
			throw "char to be encoded out of range: " + c;
		}

		// Got 6 uppermost bits, so skip those
		buf.buffer = (buf.buffer << 6) & 0x0000FFFF;
		buf.bufferedBits -= 6;

		var s = base64chars[c];
		
		if (typeof s === 'undefined') {
			throw 'Undefined for ' + c;
		}
		
		return s;
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
		
}
