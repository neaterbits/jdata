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
		
		typeTitleDiv.onclick = accordion.onclick;

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
	
	// TODO there are some issue in this, try collapse Snowboards, then Sports, then expand both again
	this._makeAccordion = function(listsDiv) {
		var wrapperDiv = document.createElement('div');
		
		wrapperDiv.setAttribute('class', 'facetsAccordion')
		
		wrapperDiv.style.height = 'auto';
		
		append(wrapperDiv, listsDiv);

		var t = wrapperDiv;
		
		var onclick = function(event) {
			var heightToSet = listsDiv.offsetHeight;
		
			if (t.getAttribute('data-accordion-in-progress') === true) {
				event.stopPropagation();
				return;
			}
			
			if (wrapperDiv.clientHeight) {
				
				t.setAttribute('data-accordion-in-progress', true)
				
				wrapperDiv.style.height = listsDiv.clientHeight + 'px';
				
				// TODO does not work on first click if doing this without timeout
				// See original method at https://stackoverflow.com/questions/25096068/css-animation-to-hide-and-show-content-like-an-accordion
				setTimeout(function() {
					wrapperDiv.style.height = 0 + 'px';
					t.setAttribute('data-accordion-in-progress', false)
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
	
	this._getListsDiv = function(parentElement) {

		if (typeof parentElement === 'undefined') {
			throw '_getListsDiv: not an element';
		}

		if (typeof parentElement.getElementsByClassName == 'undefined') {
			throw "_getListsDiv: Not an element: " + parentElement;
		}

		return parentElement.getElementsByClassName('facetListsDiv')[0];
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
			var listsDiv = this._getListsDiv(parentElement);

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

		var listsDiv = this._getListsDiv(parentElement);

		var ul = document.createElement('ul');

		append(listsDiv, ul);

		ul.setAttribute("class", "facetAttributeList");
		
		return ul;
	}
	
	this.createAttributeListElement = function(parentElement, text) {
		var li = document.createElement('li');
		
		li.style['list-style'] = 'none';

		// In order to have a clickable title, we must add a div

		var attributeTitleDiv = document.createElement('div');
		attributeTitleDiv.innerHTML = "<span class='attributeTitle'>" + text + "</span>";

		var attributeDiv = document.createElement('div');

		append(li, attributeTitleDiv);

		var accordion = this._makeAccordion(attributeDiv);

		attributeTitleDiv.onclick = accordion.onclick;
		
		//li.innerHTML = "<span class='attributeListElement'>" + text + "</span>";

		append(li, accordion.element);

		append(parentElement, li);

		li.setAttribute("class", "facetAttributeListElement");

		return attributeDiv;
	}

	// Nested within attribute
	this.createAttributeValueList = function(parentElement) {
		var ul = document.createElement('ul');

		append(parentElement, ul);

		ul.setAttribute("class", "facetAttributeValueList");
		
		return ul;
	}

	this.createAttributeValueElement = function(parentElement, value, matchCount, hasSubAttributes) {
		var li = document.createElement('li');
		
		li.style['list-style'] = 'none';
		
		var checkbox = document.createElement('input');
		checkbox.type = 'checkbox';
		
		var span = document.createElement('span');
		span.setAttribute('class', 'attributeValueElement');
		span.innerHTML = value + ' (' + matchCount + ')';

		if (hasSubAttributes) {
			// There are sub attributes so we have to make sure they are
			var valueNameDiv = document.createElement('div');

			append(valueNameDiv, checkbox);
			append(valueNameDiv, span);


			append(li, valueNameDiv);

			var listDiv = document.createElement('div');
			listDiv.setAttribute('class', 'facetListsDiv');
	
			var accordion = this._makeAccordion(listDiv);

			span.onclick = accordion.onclick;

			append(li, accordion.element);
		}
		else {
		
			append(li, checkbox);
			append(li, span);
		}
			
		append(parentElement, li);

		li.setAttribute("class", "facetAttributeValueElement");

		return { 'listItem' : li, 'checkboxItem' : checkbox };
	}


	function append(parent, element) {

		if (typeof parent === 'undefined') {
			throw 'append: not an element';
		}

		if (typeof parent.appendChild === 'undefined') {
			throw 'append: not an element';
		}
		parent.appendChild(element);
	}
}
