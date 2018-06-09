/**
 * Code for creating the HTML elements that are displayed in the gallery
 */

function SimpleGalleryItemFactory() {

	this.getGalleryConfig = function() {
		return {
			columnSpacing : 20,
			rowSpacing : 20,
			widthHint : 300,
			heightHint : 300
		};
	}
	
	this.getItemFields = function () {
		return null;
	}


	this.makeProvisionalItem = function(index, data) {
		var div = document.createElement('div');

		var provisionalImage = document.createElement('div');
		
		provisionalImage.setAttribute('class', 'provisionalImage');

		provisionalImage.style.width = data.thumbWidth;
		provisionalImage.style.height = data.thumbHeight;

		div.append(provisionalImage);
		
		var textDiv = document.createElement('div');

		// Add index as a text to the element
		var textSpan = document.createElement('span');
		
		textSpan.innerHTML = _makeTitle(index, data.title);

		// Don't make text wider than thumb
		textDiv.style.width = data.thumbWidth;
		textDiv.style['text-align'] = 'center';
			
		textDiv.append(textSpan);

		div.append(textDiv);

		//textDiv.setAttribute('style', 'text-align : center;');
		
		return div;
	}
	
	this.makeImageItem = function(index, provisionalData, imageData) {
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
		
		textSpan.innerHTML = _makeTitle(provisionalData.title);

		textDiv.style.width = provisionalData.thumbWidth;
		textDiv.style['text-align'] = 'center';

		textDiv.append(textSpan);
		
		div.append(textDiv);

		return div;
	}

}

function RentalApartmentGalleryItemFactory() {
	var ITEM_WIDTH = 430;
	var ITEM_HEIGHT = 115;
	
	this.getGalleryConfig = function() {
		return {
			columnSpacing : 20,
			rowSpacing : 20,
			width : ITEM_WIDTH,
			height : ITEM_HEIGHT
		};
	}
	
	this.getItemFields = function () {
		return ['address', 'price'];
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
		
		return textDiv;
	}
}

function appendBr(element) {
	var br = document.createElement('br');
	
	element.append(br);
}

function _makeTitle(index, title) {
	return '' + index + ': ' + title;
}
