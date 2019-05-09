/**
 * Gallery item factory for rental apartments
 */

function RentalApartmentGalleryItemFactory(ajax, getPhotoCountUrl, getPhotoUrl) {
	var ITEM_WIDTH = 430;
	var ITEM_HEIGHT = 115;
	
	this.ajax = ajax;
	this.getPhotoCountUrl = getPhotoCountUrl;
	this.getPhotoUrl = getPhotoUrl;
	
	this.getGalleryConfig = function() {
		return {
			columnSpacing : 20,
			rowSpacing : 20,
			width : ITEM_WIDTH,
			height : ITEM_HEIGHT
		};
	}
	
	this.getItemFields = function () {
		return ['address', 'price', 'originalUrl'];
	}

	this.makeProvisionalItem = function(index, data) {
		var div = document.createElement('div');

		div.style.display = 'block';
		div.style.width  = ITEM_WIDTH;
		div.style.height = ITEM_HEIGHT;

		var provisionalImage = document.createElement('div');
		
		provisionalImage.setAttribute('class', 'provisionalImage');

		provisionalImage.style.width = data.thumbWidth;
		provisionalImage.style.height = data.thumbHeight;

		var innerDiv = this._makeDiv(index, data, provisionalImage);

		div.append(innerDiv);
		
		return div;
	}

	this.makeImageItem = function(index, provisionalData, imageData) {
		var div = document.createElement('div');

		var image = document.createElement('img');

		image.setAttribute('class', 'thumbnailImage');

		image.width = provisionalData.thumbWidth;
		image.height = provisionalData.thumbHeight;
		
		image.src = imageData;

		var innerDiv = this._makeDiv(index, provisionalData, image);

		div.append(innerDiv);
		
		return div;
	}
	
	this._makeDiv = function(index, provisionalData, imageElement) {
		return this._centerDiv(this._makeInnerDiv(index, provisionalData, imageElement));
	}
	
	this._centerDiv = function(inner) {
		var outer = document.createElement('div');
		
		outer.style.display = 'table';

		var inner2 = document.createElement('div');

		inner2.style.display = 'table-cell';
		inner2.style['vertical-align'] = 'middle';
		
		inner2.append(inner);

		outer.append(inner2);
		
		return outer;
	}

	this.photoViewDisplayed = false;
	
	this._makeInnerDiv = function(index, provisionalData, imageElement) {

		var div = document.createElement('div');

		div.style.display = 'inline-block';
		div.style.width  = ITEM_WIDTH;
		// div.style.height = ITEM_HEIGHT;

		imageElement.style.display = 'inline-block';
		
		div.append(imageElement);
		
		var textDiv = this._makeTextDiv(index, provisionalData);

		textDiv.style.display = 'inline-block';
		textDiv.style.width = ITEM_WIDTH - provisionalData.thumbWidth - 20;

		div.append(textDiv);
		
		var itemId = provisionalData.id;

		var photoView = this._createPhotoView();

		div.append(photoView);
		
		var t = this;
		
		div.onclick = function(e) {

			if (photoView.style.display == 'none' && !t.photoViewDisplayed) {
				
				var getPhotoCountUrl = t.getPhotoCountUrl(itemId);

				// asynchronously get resonse type
				t.ajax.getAjax(getPhotoCountUrl, 'text', function(response) {

					console.log('## got response: "' + response + '"');

					var photoCount = parseInt(response);

					console.log('## got photoCount: ' + photoCount);

					// Still not displayed
					if (photoCount > 0 && photoView.style.display == 'none' && !t.photoViewDisplayed) {
						t._displayPhotoView(photoView, itemId, photoCount);

						t.photoViewDisplayed = true;
					}
				});
			}
		}

		return div;
	}
	
	this._makeTextDiv = function(index, provisionalData) {
		var textDiv = document.createElement('div');
		
		// Add index as a text to the element
		var textSpan = document.createElement('span');

//		textSpan.innerHTML = _makeTitle(index, provisionalData.title);
		textSpan.innerHTML = provisionalData.title;

//		textDiv.style['text-align'] = 'center';
		
		textDiv.style['vertical-align'] = 'top';
		textDiv.style['font-size'] = '14px';

		textDiv.append(textSpan);
		
		appendBr(textDiv);

		var span2 = document.createElement('span');
		span2.innerHTML = provisionalData.fields[0];
		textDiv.append(span2);

		appendBr(textDiv);
		
		var span3 = document.createElement('span');
		span3.innerHTML = "Price: " + provisionalData.fields[1] + ' lari / month';
		textDiv.append(span3);
		
		var link = document.createElement('a');
		
		link.href = provisionalData.fields[2];
		
		link.innerHTML = "Link to ad";

		appendBr(textDiv);
		textDiv.append(link);
		
		return textDiv;
	}

	
	this._createPhotoView = function() {

		var outerDiv = document.createElement('div');
		
		outerDiv.style['z-index'] = 100;
		outerDiv.style.display = 'none';
		outerDiv.style.position = 'relative';

		outerDiv.style['background-color'] = 'white';

		var header = document.createElement('div');
		
		header.innerHTML = "Photo view";

		var imageDiv = document.createElement('div');
		
		imageDiv.style.width = '800px';
		imageDiv.style.height = '600px';
		imageDiv.setAttribute('class', 'imageDiv');

		var img = document.createElement('img');
		img.setAttribute('class', 'image');
		imageDiv.append(img);

		var navigationDiv = document.createElement('div');
		
		var nextButton = document.createElement('input');
		nextButton.type = 'button';
		nextButton.value = 'Next';

		nextButton.setAttribute('class', 'nextButton');

		var closeButton = document.createElement('input');
		closeButton.type = 'button';
		closeButton.value = 'Close';
		
		var t = this;
		
		closeButton.onclick = function(e) {
			outerDiv.style.display = 'none';

			t.photoViewDisplayed = false;

			e.stopPropagation();
		}

		navigationDiv.append(nextButton);
		navigationDiv.append(closeButton);

		outerDiv.append(header);
		outerDiv.append(navigationDiv);
		outerDiv.append(imageDiv);
		
		return outerDiv;
	}

	this._displayPhotoView = function(photoView, itemId, photoCount) {

		if (photoCount < 1) {
			throw "No photos to display";
		}

		var img = photoView.getElementsByClassName('image')[0];
		
		// Set image URL to download image
		img.src = this.getPhotoUrl(itemId, 0);

		photoView.style.display = 'block';
		
		var nextButton = photoView.getElementsByClassName('nextButton')[0];
		
		var t = this;
		
		var photoNo = { field : 0 };
		
		if (photoCount === 1) {
			this._enableDisableButton(nextButton, false);
			nextButton.onclick = null;
		}
		else {
			this._enableDisableButton(nextButton, true);

			nextButton.onclick = function(e) {
				++ photoNo.field;
				img.src = getPhotoUrl(itemId, photoNo.field);
				e.stopPropagation();
				
				if (photoNo.field >= photoCount - 1) {
					t._enableDisableButton(nextButton, false);
				}
			};
		}
		nextButton.clicked
	}
	
	this._enableDisableButton = function(button, enabled) {
		if (enabled) {
			button.removeAttribute('disabled');
		}
		else {
			button.disabled = 'disabled';
		}
	}
	
}

function appendBr(element) {
	var br = document.createElement('br');
	
	element.append(br);
}
