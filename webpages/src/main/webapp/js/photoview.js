/**
 * Simple view for displaying photos
 */

function PhotoView(outerDiv, getPhotoUrl) {

	
	this.outerDiv = outerDiv;
	this.getPhotoUrl = getPhotoUrl;
	
	this.initPhotoView = function() {

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
			t.outerDiv.style.display = 'none';

			t.photoViewDisplayed = false;

			e.stopPropagation();
		}

		navigationDiv.append(nextButton);
		navigationDiv.append(closeButton);

		this.outerDiv.append(header);
		this.outerDiv.append(navigationDiv);
		this.outerDiv.append(imageDiv);
		
		return this.outerDiv;
	}

	this.displayPhoto = function(itemId, photoCount) {

		if (photoCount < 1) {
			throw "No photos to display";
		}
		
		var photoView = this.outerDiv;

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