/**
 * Search view of combined facet and gallery views.
 * 
 * This has to be combined since they require the same model
 * 
 */


function SearchView(
		serviceUrl,
		facetModel,
		facetController,
		gallery) {

	this.serviceUrl = serviceUrl;
	this.facetModel = facetModel;
	this.facetController = facetController;
	this.gallery = gallery;

	this.hasPerformedInitialSearch = false;

	this._getInitial = function(onsuccess) {

		var t = this;

		// Post to get initial for all known
		this._postAjax(this.serviceUrl, function(response) {
			t._updateFacets(response.facets, onsuccess);

			t._refreshGallery(response.items);
		});
	}

	this.setServiceUrl = function(url) {
		this.serviceUrl = url;
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
		var request = new XMLHttpRequest();

		request.responseType = 'json';

		request.onreadystatechange = function() {

			if (this.readyState == 4 && this.status == 200) {
				onsuccess(this.response);
			}
		};

		request.open('POST', url, true);

		request.send();
	};
	
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
