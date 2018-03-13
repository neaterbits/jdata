/**
 * Interface implementation for instantiating divs and such,
 * separating that from the view code
 * this allowing to switch what type of HTML elements are created
 * without too much hassle
 */

function FacetViewElements() {
	
	this.createTypeContainer = function(parentElement, text) {
		var typeDiv = document.createElement('div');
		
		append(parentElement, typeDiv);
		
		var typeTitleDiv = document.createElement('div');
		
		typeTitleDiv.innerHTML = "<span class='typeTitle'>" + text + "</span>";

		append(typeDiv, typeTitleDiv);
		
		typeDiv.setAttribute("class", "facetType");
		
		return typeDiv;
	}
	
	// Nested within type container
	this.createTypeList = function(parentElement) {
		var ul = document.createElement('ul');
		
		append(parentElement, ul);

		ul.setAttribute("class", "facetTypeList");
		
		return ul;
	}
	
	this.createTypeListElement = function(parentElement, text) {
		var li = document.createElement('li');
		
		append(parentElement, li);

		li.setAttribute("class", "facetTypeListElement");
		
		return li;
	}

	// Nested within type container
	this.createAttributeList = function(parentElement) {
		var ul = document.createElement('ul');

		append(parentElement, ul);

		ul.setAttribute("class", "facetAttributeList");
		
		return ul;
	}
	
	this.createAttributeListElement = function(parentElement, text) {
		var li = document.createElement('li');

		li.innerHTML = "<span class='attributeListElement'>" + text + "</span>";

		append(parentElement, li);

		li.setAttribute("class", "facetAttributeListElement");
		// TODO add checkbox item 
		return { 'listItem: ' : li, 'checkboxItem ' : null };
	}

	function append(parent, element) {
		parent.appendChild(element);
	}
}
