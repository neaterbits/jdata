/**
 * Interface implementation for instantiating divs and such,
 * separating that from the view code
 * this allowing to switch what type of HTML elements are created
 * without too much hassle
 */

function FacetViewElements() {
	
	this.createTypeContainer = function(parentElement, text) {
		var typeDiv = document.createElement('div');
		
		typeDiv.setAttribute("class", "facetType");
		
		append(parentElement, typeDiv);

		// Type container contains of two divs, one of title name
		// and one for subtypes and attributes
		var typeTitleDiv = document.createElement('div');
		typeTitleDiv.innerHTML = "<span class='typeTitle'>" + text + "</span>";
		append(typeDiv, typeTitleDiv);
		
		var listsDiv = document.createElement('div');
		
		listsDiv.setAttribute('class', 'facetListsDiv');
		
		// Make it possible to show and hide the below lists
		var accordion = this._makeAccordion(listsDiv); // this._makeShowHide(listsDiv);
		
		typeDiv.onclick = accordion.onclick;

		append(typeDiv, accordion.element);


		return typeDiv;
	}
	
	this._makeShowHide = function(listsDiv) {
		
		var onclick = function (event) {
			var display = listsDiv.style.display;
			
			if (display === 'block' || display === '' || typeof display === 'undefined') {
				listsDiv.style.display = 'none';
			}
			else {
				listsDiv.style.display = 'block';
			}
		};
		
		return { 'element' : listsDiv, 'onclick' : onclick };
	}
	
	
	this._makeAccordion = function(listsDiv) {
		var wrapperDiv = document.createElement('div');
		
		wrapperDiv.setAttribute('class', 'facetsAccordion')
		
		wrapperDiv.style.height = 'auto';
		
		append(wrapperDiv, listsDiv);
		
		var onclick = function(event) {
			var heightToSet = listsDiv.offsetHeight;
			
			if (wrapperDiv.clientHeight) {
				wrapperDiv.style.height = listsDiv.clientHeight + 'px';
				
				// TODO does not work on first click if doing this without timeout
				// See original method at https://stackoverflow.com/questions/25096068/css-animation-to-hide-and-show-content-like-an-accordion
				setTimeout(function() {
					wrapperDiv.style.height = 0 + 'px';
				},
				100);
			}
			else {
				wrapperDiv.style.height = heightToSet + 'px'; 
			}
			
			event.stopPropagation();
		};
		
		return { 'element' : wrapperDiv, 'onclick' : onclick };
	}
	
	// Nested within type container
	this.createTypeList = function(parentElement, isRoot) {
		var ul = document.createElement('ul');
		
		if (isRoot) {
			// append straight under root
			append(parentElement, ul);
		}
		else{
			// TODO perhaps find other way? But should not pass lsistDiv back to caller in createTypeContainer()
			var listsDiv = parentElement.getElementsByClassName('facetListsDiv')[0]; // parentElement.childNodes.item(1);
			
			append(listsDiv, ul);
		}

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

		if (typeof parent.appendChild === 'undefined') {
			throw 'not an element';
		}
		parent.appendChild(element);
	}
}
