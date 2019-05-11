/**
 * Simple gallery item factory with static size elements
 */

function SimpleStaticSizeGalleryItemFactory(ajax, getThumbUrl) {

	const ITEM_WIDTH = 300;
	const ITEM_HEIGHT = 300;
	
	const TEXT_HEIGHT = 60;
	
	const IMAGE_WIDTH = ITEM_WIDTH;
	const IMAGE_HEIGHT = ITEM_HEIGHT - TEXT_HEIGHT;
	

	this.ajax = ajax;
	this.getThumbUrl = getThumbUrl;

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
		
		return div;
	}
	
	this.makeImageItem = function(index, provisionalData, imageData) {
		var div = document.createElement('div');
		
		div.style.width = ITEM_WIDTH;
		div.style.height = ITEM_HEIGHT;

		div.style.position = 'relative';
		
		var curShownThumbnail = 0;
		
		var currentImageDiv = document.createElement('div')
		
		
		currentImageDiv.style.position = 'absolute';
		currentImageDiv.style.left = 0;
		currentImageDiv.style.top = 0;
		currentImageDiv.style.width = ITEM_WIDTH;
		currentImageDiv.style['z-index'] = 50;
		
		currentImageDiv.setAttribute('class', 'galleryCurrentThumbDiv');
		
		_setGalleryItemOverlayVisible(currentImageDiv, false);

		var numItemThumbnails = imageData.numItemThumbnails;

		_updateCurrentImageDivText(currentImageDiv, curShownThumbnail, numItemThumbnails);
		
		div.append(currentImageDiv);
		
		var lastNavigatorDiv = _createNavigatorDiv(
				div,
				'galleryLastThumbDiv',
				'galleryLastThumbArrowDiv',
				function (navigatorWidth) { return 0; });

		var nextNavigatorDiv = _createNavigatorDiv(
				div,
				'galleryNextThumbDiv',
				'galleryNextThumbArrowDiv',
				function (navigatorWidth) { return ITEM_WIDTH - navigatorWidth; });
		
		var isLastNavigatorEnabled = function(curShown) {
			return curShownThumbnail != 0;
		};
		
		var isNextNavigatorEnabled = function() {
			return curShownThumbnail < numItemThumbnails - 1;
		};
		
		var updateNavigators = function() {
			_setGalleryItemOverlayVisible(lastNavigatorDiv, isLastNavigatorEnabled(curShownThumbnail));
			_setGalleryItemOverlayVisible(nextNavigatorDiv, isNextNavigatorEnabled(curShownThumbnail));
		};
		
		div.onmouseover = function() {
			_setGalleryItemOverlayVisible(currentImageDiv, true);
			
			updateNavigators();
		};
		
		div.onmouseout = function() {
			
			_setGalleryItemOverlayVisible(currentImageDiv, false);
			_setGalleryItemOverlayVisible(lastNavigatorDiv, false);
			_setGalleryItemOverlayVisible(nextNavigatorDiv, false);

			/*
			lastNavigatorDiv.visibility = 'hidden';
			nextNavigatorDiv.visibility = 'hidden';
			*/
		};
		
		var image = document.createElement('img');

		image.setAttribute('class', 'thumbnailImage');

		div.style['overflow'] = 'hidden';

		image.style.margin = 'auto';
		image.style.display = 'block';
		
		_adjustImageWidthAndHeight(image, provisionalData.thumbWidth, provisionalData.thumbHeight);

		image.src = imageData.data;

		var t = this;

		lastNavigatorDiv.onclick = function() {

			t._navigate(
					image,
					provisionalData,
					isLastNavigatorEnabled,
					function () { return curShownThumbnail - 1; },
					function() {
						-- curShownThumbnail;
						_updateCurrentImageDivText(currentImageDiv, curShownThumbnail, numItemThumbnails);
					},
					updateNavigators
			);
		};

		nextNavigatorDiv.onclick = function() {

			t._navigate(
					image,
					provisionalData,
					isNextNavigatorEnabled,
					function () { return curShownThumbnail + 1; },
					function() {
						++ curShownThumbnail;
						_updateCurrentImageDivText(currentImageDiv, curShownThumbnail, numItemThumbnails);
					},
					updateNavigators
			);
		};

		div.append(image);
		
		_appendText(div, index, imageData.numItemThumbnails, provisionalData);
		
		return div;
	}
	
	
	function _setGalleryItemOverlayVisible(div, visible) {
		div.style.display = visible ? 'inline-block' : 'none';
	}
	
	function _updateCurrentImageDivText(div, cur, count) {
		div.innerHTML = 'Showing image ' + (cur + 1) + ' out of ' + count;
	}
	
	this._navigate = function(image, provisionalData, isNavigatorEnabled, getToShowThumbnailNo, changeCurShownIndex, updateNavigatorsVisibility) {
		
		if (isNavigatorEnabled()) {
			
			var itemId = provisionalData.id;

			console.log('## navigator enabled');
			
			var toShow = getToShowThumbnailNo();
			
			var url = this.getThumbUrl(itemId, toShow);
			
			console.log('## load thumb from ' + url);
			
			this.ajax.getAjax(
					url,
					'arraybuffer',
					function (buffer) {
						changeCurShownIndex();
						
						updateNavigatorsVisibility();
						
						var dataView = new DataView(buffer);
						
						// base 64 encode binary data
						var encoded = base64_encode(dataView, 0, dataView.byteLength);
	
						var data = 'data:image/jpeg;base64,' + encoded;
						
						var jsImage = new Image(300, 300);
						jsImage.src = data;
						
						console.log('## image width ' + jsImage.clientWidth + '/' + jsImage);
						
						image.src = data;
						console.log('## image width ' + image.naturalWidth + '/' + image.naturalHeight);

						_adjustImageWidthAndHeight(image, image.naturalWidth, image.naturalHeight);
					}
			);
		}
		else {
			console.log('## navigator not enabled');
		}
	}
	
	function _createNavigatorDiv(div, navigatorDivCSSClass, arrowDivCSSClass, getLeft, getTop) {
		
		var navigatorWidth = ITEM_WIDTH / 6;
		var navigatorHeight = ITEM_HEIGHT / 2;
		
		var navigatorDiv = document.createElement('div');
		navigatorDiv.style['z-index'] = 50;
		navigatorDiv.style.position = 'absolute';
		navigatorDiv.style.width = navigatorWidth;
		navigatorDiv.style.height = navigatorHeight;
		navigatorDiv.style.left = getLeft(navigatorWidth);
		navigatorDiv.style.top = (ITEM_HEIGHT - navigatorHeight) / 2;
		navigatorDiv.style.display = 'none';
		
		var arrowDiv = document.createElement('div');
		
		arrowDiv.setAttribute('class', arrowDivCSSClass);
		
		navigatorDiv.append(arrowDiv);
		
		navigatorDiv.setAttribute('class', navigatorDivCSSClass);
		
		div.append(navigatorDiv);
	
		return navigatorDiv;
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
		var size = _computeWidthAndHeight(
				width,
				height,
				IMAGE_WIDTH,
				IMAGE_HEIGHT);
		
		image.width = size.width;
		image.height = size.height;

		if (size.crop > 0) {
			
			var cropAttributeValue = '-' + (size.crop / 2) + 'px';
			
			image.style['margin-left'] = cropAttributeValue;
			image.style['margin-right'] = cropAttributeValue;
		}
		else {
			image.style['margin-left'] = 'auto';
			image.style['margin-right'] = 'auto';
		}
	}
	
	/**
	 * Scale image to fill available space
	 *  - always scale to bbHeight so that text appears the same spot always
	 *  - allow for some scaling aspect ratio change
	 */
	function _computeWidthAndHeight(width, height, bbWidth, bbHeight) {

		var thumbWidth;
		var thumbHeight;
		
		thumbHeight = bbHeight;
		
		var cropped;

		if (height > bbHeight) {
			// Scale down to height
		
			var scaleDown = bbHeight / height;
			
			cropped = _computeCrop(Math.floor(width * scaleDown), bbWidth);
		}
		else if (height == bbHeight) {
			cropped = _computeCrop(width, bbWidth);
		}
		else if (height < bbHeight) {
			var scaleUp = bbHeight / height;
			
			cropped = _computeCrop(Math.floor(width * scaleUp), bbWidth);
		}
		else {
			throw "Unreachable code";
		}

		return { 'width' : cropped.width, 'height' : thumbHeight, 'crop' : cropped.crop };
	}

	function _computeCrop(thumbWidth, bbWidth) {

		var crop;
		
		var adjustedWidth = thumbWidth;
		
		if (thumbWidth > bbWidth) {
			crop = thumbWidth - bbWidth;
		}
		else {
			crop = 0;
		}
		
		return { 'width' : adjustedWidth, 'crop' : crop };
	}
}
