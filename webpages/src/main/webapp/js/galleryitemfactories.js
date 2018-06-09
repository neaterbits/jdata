/**
 * 
 */

function SimpleGalleryItemFactory() {

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
		
		textSpan.innerHTML = _makeTitle(index, provisionalData.title);

		textDiv.style.width = provisionalData.thumbWidth;
		textDiv.style['text-align'] = 'center';

		textDiv.append(textSpan);
		
		div.append(textDiv);

		return div;
	}

}

function _makeTitle(index, title) {
	return '' + index + ': ' + title;
}
