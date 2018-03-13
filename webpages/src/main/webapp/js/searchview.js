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
		});
	}
	
	
	this._refreshFromCriteria = function(types, criteria, onsuccess) {
		
		var t = this;
		
		// Call REST service with criteria
		this._postAjax(serviceURL, 'POST', criteria, function(response) {
			t._updateFacets(response.facets, onsuccess);
		});
	}

	this._updateFacets = function(facets, onsuccess) {
		this.facetModel.updateFacets(facets);

		this.facetController.refresh();

		onsuccess();
	}
	
	// Search based on
	this.refresh = function() {

		var t = this;

		if (this.hasPerformedInitialSearch) {
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
}