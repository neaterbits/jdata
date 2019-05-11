/**
 * 
 */

function AdView(div, loadItemData, getPhotoUrl) {
	
	const PHOTO_WIDTH = 600;
	const PHOTO_HEIGHT = 450;
	
	if (typeof div === 'undefined' || div === null) {
		throw "No div given";
	}
	
	this.div = div;
	this.loadItemData = loadItemData;
	this.getPhotoUrl = getPhotoUrl;
	
	this.displayed = false;
	
	var t = this;

	/*
	div.onclick = function() {
		div.style.display = 'none';

		t.displayed = false;
	}
	*/

	this.displayAd = function(itemId) {
		
		if (this.displayed) {
			throw "Already displayed";
		}
		
		var t = this;
		
		loadItemData(
				itemId,
				function(itemData) {
					t._displayAd(itemData);
					
					t.div.style.display = 'inline-block';
					t.displayed = true;
				},
				function (errorMessage) {
					t._displayErrorMessage(errorMessage);
				}
		);
	};
	
	this._displayAd = function(itemData) {
		
		var titleElement = document.getElementById('ad_view_title');
		
		titleElement.innerHTML = itemData.serviceAttributes.title;

		var attributes = itemData.displayAttributes;
		
		var detailsDiv = document.getElementById('ad_view_details_div');
		detailsDiv.setAttribute('class', 'adViewDetailsDiv');
		
		removeAll(detailsDiv);
		
		for (var i = 0; i < attributes.length; ++ i) {
			var attribute = attributes[i];

			var attributeDiv = document.createElement('div');
			attributeDiv.setAttribute('class', 'adViewAttributeDiv');
			detailsDiv.append(attributeDiv);
			
			var attributeNameSpan = document.createElement('span');
			attributeNameSpan.innerHTML = attribute.name;
			attributeNameSpan.setAttribute('class', 'adViewAttributeNameSpan');
			attributeDiv.append(attributeNameSpan);

			var attributeValueSpan = document.createElement('span');
			attributeValueSpan.innerHTML = '' + attribute.value;
			attributeValueSpan.setAttribute('class', 'adViewAttributeValueSpan');
			attributeDiv.append(attributeValueSpan);
		}
		
		var photosDiv = document.getElementById('ad_view_photos_div');
		
		photosDiv.style.width = PHOTO_WIDTH;
		photosDiv.style.height = PHOTO_HEIGHT;
		
		var photoDiv = document.getElementById('ad_view_photo_div');
		var photoImage = document.getElementById('ad_view_photo');
		
		if (itemData.photoCount > 0) {
			var photoViewer = new PhotoViewWithNavigator(
				itemData.serviceAttributes.id,
				itemData.photoCount,
				photoDiv,
				photoImage,
				PHOTO_WIDTH, PHOTO_HEIGHT,
				this.getPhotoUrl
			);
		
			photoViewer.displayPhoto(0);
		}
		
		var descriptionDiv = document.getElementById('ad_view_description_div');
		
		console.log('## setting description to ' + itemData.serviceAttributes.descriptionHtml);
		
		descriptionDiv.innerHTML = itemData.serviceAttributes.descriptionHtml;
	}
	
	this.isDisplayed = function() {
		
		console.log('## ad view is displayed ' + this.displayed);
		
		return this.displayed;
	}
}

