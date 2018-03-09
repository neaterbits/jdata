/**
 * Interface implementation for instantiating divs and such,
 * separating that from the view code
 * this allowing to switch what type of HTML elements are created
 * without too much hassle
 */

function FacetViewElements() {
	
	this.createTypeContainer = function() {
		var typeDiv = document.createElement('div');
		
		typeDiv.setAttribute("class", "facetType");
		
		return typeDiv;
	}
	
	// Nested within type container
	this.createTypeList = function() {
		var ul = document.createElement('ul');
		
		ul.setAttribute("class", "facetTypeList");
		
		return ul;
	}
	
	this.createTypeListElement = function() {
		var li = document.createElement('li');
		
		li.setAttribute("class", "facetTypeListElement");
		
		return li;
	}

	// Nested within type container
	this.createAttributeList = function() {
		var ul = document.createElement('ul');
		
		ul.setAttribute("class", "facetAttributeList");
		
		return ul;
	}
	
	this.createAttributeListElement = function() {
		var li = document.createElement('li');
		
		li.setAttribute("class", "facetAttributeListElement");
		// TODO add checkbox item 
		return { 'listItem: ' : li, 'checkboxItem ' : null };
	}
}