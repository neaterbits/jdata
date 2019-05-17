/**
 * 
 */

function AdView(div, loadItemDataByIndex, getPhotoUrl) {
	
	const PHOTO_WIDTH = 600;
	const PHOTO_HEIGHT = 450;
	
	if (typeof div === 'undefined' || div === null) {
		throw "No div given";
	}
	
	this.div = div;
	this.loadItemDataByIndex = loadItemDataByIndex;
	this.getPhotoUrl = getPhotoUrl;
	
	this.displayed = false;
	
	var t = this;

	var closeButton = document.getElementById('ad_view_close_button');
	
	var lastButton = document.getElementById('ad_view_last_button');
	var nextButton = document.getElementById('ad_view_next_button');
	
	this.navigator = new Navigator(
			0,
			1,
			lastButton,
			nextButton,
			function (toShow, callback) {
				
				t._loadAndRenderAd(toShow, function() {
					callback();
				});
			}
	);
	
	closeButton.onclick = function() {
		div.style.display = 'none';

		t.displayed = false;
	}

	this.displayAd = function(itemIndex, itemCount) {
	
		if (this.displayed) {
			throw "Already displayed";
		}

		this.navigator.reset(itemIndex, itemCount);

		this._loadAndRenderAd(itemIndex, function() {
			t.div.style.display = 'inline-block';
			t.displayed = true;
		});
	}

	this._loadAndRenderAd = function(itemIndex, onDisplayed) {

		var t = this;
		
		this.loadItemDataByIndex(
				itemIndex,
				function(itemData) {
					t._constructAd(itemData);
					
					onDisplayed();
				},
				function (errorMessage) {
					t._displayErrorMessage(errorMessage);
				}
		);
	};
	
	this._constructAd = function(itemData) {
		
		var titleElement = document.getElementById('ad_view_title');
		
		titleElement.innerHTML = itemData.serviceAttributes.title;

		this._updateDetails(itemData);

		this._updatePhotos(itemData);

		this._updateMap(itemData);
		
		var descriptionDiv = document.getElementById('ad_view_description_div');
		
		descriptionDiv.innerHTML = itemData.serviceAttributes.descriptionHtml;
	}
	
	this._updateDetails = function(itemData) {
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
	}
	
	this._updatePhotos = function(itemData) {
		
		var photosDiv = document.getElementById('ad_view_photos_div');
		
		photosDiv.style.width = PHOTO_WIDTH;
		photosDiv.style.height = PHOTO_HEIGHT;
	
		var photoDiv = document.getElementById('ad_view_photo_div');
		
		photoDiv.style.width = PHOTO_WIDTH;
		photoDiv.style.height = PHOTO_HEIGHT;
		
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
	}
	
	this._updateMap = function(itemData) {

		var mapIFrame = document.getElementById('ad_view_map_iframe');
		
		var latitude = itemData.serviceAttributes.latitude;
		var longtitude = itemData.serviceAttributes.longtitude;
		
		if (typeof latitude !== 'undefined' && typeof longtitude !== 'undefined') {
			mapIFrame.src = "https://www.google.com/maps/embed/v1/place"
				+ "?q=+" + latitude + ",+" + longtitude
				+ "&key=AIzaSyA_out7Cciix5uutqxCEKQg0qFmHln9HtQ";
		}
	}
	
	this.isDisplayed = function() {
		return this.displayed;
	}
}

