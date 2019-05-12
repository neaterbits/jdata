/**
 * Simple gallery item factory with static size elements
 */

function SimpleStaticSizeGalleryItemFactory(ajax, getThumbUrl, adView) {

	const ITEM_WIDTH = 300;
	const ITEM_HEIGHT = 300;
	
	const TEXT_HEIGHT = 60;
	
	const IMAGE_WIDTH = ITEM_WIDTH;
	const IMAGE_HEIGHT = ITEM_HEIGHT - TEXT_HEIGHT;

	this.ajax = ajax;
	this.getThumbUrl = getThumbUrl;
	this.adView = adView;

	this.getGalleryConfig = function() {
		return {
			columnSpacing : 20,
			rowSpacing : 20,
			width : ITEM_WIDTH,
			height : ITEM_HEIGHT
		};
	}
	
	this.getItemFields = function () {
		return [ 'publicationDate', 'price', 'currency' ];
	}

	this.makeProvisionalItem = function(index, data) {
		var div = document.createElement('div');
		
		/*
		div.style.width = ITEM_WIDTH;
		div.style.height = ITEM_HEIGHT;
		div.style.display = 'inline-block';
		*/

		var provisionalImage = document.createElement('div');
		
		provisionalImage.setAttribute('class', 'provisionalImage');

		provisionalImage.style.width = data.thumbWidth;
		provisionalImage.style.height = data.thumbHeight;
		provisionalImage.style.margin = 'auto';
		provisionalImage.style.display = 'block';
		
		div.append(provisionalImage);
		
		_appendText(div, index, -1, data);
		
		div.onclick = function() {
			if (!adView.isDisplayed()) {
				adView.displayAd(data.id);
			}
		};
		
		return div;
	}
	
	this.makeImageItem = function(index, provisionalData, imageData) {
		var div = document.createElement('div');
		
		div.style.width = ITEM_WIDTH;
		div.style.height = ITEM_HEIGHT;

		div.style.position = 'relative';

		var t = this;
		
		var numItemThumbnails = imageData.numItemThumbnails;

		var image = document.createElement('img');

		var navigator = new NavigatorOverlay(
				numItemThumbnails,
				div,
				ITEM_WIDTH,
				ITEM_HEIGHT,
				function() { return ITEM_WIDTH / 6; },
				function() { return ITEM_HEIGHT / 2; },
				function (toShow, callback) {
					t._onNavigate(image, provisionalData, toShow, callback);
				});
		

		image.setAttribute('class', 'thumbnailImage');

		div.style['overflow'] = 'hidden';

		image.style.margin = 'auto';
		image.style.display = 'block';
		
		_adjustImageWidthAndHeight(image, provisionalData.thumbWidth, provisionalData.thumbHeight);

		image.src = imageData.data;

		div.append(image);
		
		_appendText(div, index, imageData.numItemThumbnails, provisionalData);
		
		div.onclick = function() {
			if (!adView.isDisplayed()) {
				adView.displayAd(provisionalData.id);
			}
		};

		return div;
	}
	
	this._onNavigate = function(image, provisionalData, toShow, callback) {
		
		var itemId = provisionalData.id;

		var url = this.getThumbUrl(itemId, toShow);
		
		this.ajax.getAjax(
				url,
				'arraybuffer',
				function (buffer) {
					
					callback();
					
					var dataView = new DataView(buffer);
					
					// base 64 encode binary data
					var encoded = base64_encode(dataView, 0, dataView.byteLength);

					var data = 'data:image/jpeg;base64,' + encoded;
					
					image.src = data;

					_adjustImageWidthAndHeight(image, image.naturalWidth, image.naturalHeight);
				}
		);
	}
	
	function _appendText(div, index, numItemThumbnails, provisionalData) {
		
		var textDiv = document.createElement('div');
		
		
		// textDiv.style.width = 
		
		// Add index as a text to the element
		var textSpan = document.createElement('div');
		textSpan.setAttribute('class', 'galleryItemTextDiv');
		textSpan.style.display = 'block';
		
		var titleSpan = document.createElement('span');
		titleSpan.innerHTML = '' + numItemThumbnails + ' ' + _makeTitle(index, provisionalData.title);
		titleSpan.setAttribute('class', 'galleryItemTitleSpan');
		textSpan.append(titleSpan);

		var currency = provisionalData.fields[2];
		
		var priceSpan = document.createElement('span');
		priceSpan.innerHTML = _makePrice(provisionalData.fields[1], currency);
		priceSpan.setAttribute('class', 'galleryItemPriceSpan');
		textSpan.append(priceSpan);
		
		textDiv.style.width = ITEM_WIDTH;
		textDiv.style['text-align'] = 'center';

		textDiv.append(textSpan);
		
		div.append(textDiv);
	}
	
	function _makePrice(price, currency) {
		
		var result;
		
		if (typeof currency === 'undefined' || currency === null || currency === 'USD') {
			result = '&#36;' + price;
		}
		else {
			result = price;
		}
		
		return result;
	}
	
	function _adjustImageWidthAndHeight(image, width, height) {
		adjustImageWidthAndHeight(image, width, height, IMAGE_WIDTH, IMAGE_HEIGHT);
	}
}
