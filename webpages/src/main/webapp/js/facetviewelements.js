/**
 * Interface implementation for instantiating divs and such,
 * separating that from the view code
 * this allowing to switch what type of HTML elements are created
 * without too much hassle
 */

function FacetViewElements() {

	var FACET_SINGLE_SELECTION_BUTTON = "button";
	
	this.facetSingleSelection = null;
	
	this.getRootContainer = function(divId) {
		return document.getElementById(divId);
	}

	this.createTypeContainer = function(parentElement, text, isExpanded, checked) {
		
		function expandedClass(expanded) {
			return expanded ? 'typeExpanderExpanded' :'typeExpanderCollapsed';
		}
		
		var typeDiv = document.createElement('div');
		
		typeDiv.setAttribute("class", "facetType");
		
		append(parentElement, typeDiv);

		// Type container contains of two divs, one of title name
		// and one for subtypes and attributes
		var typeTitleDiv = document.createElement('div');
		var titleHtml = "<div class='typeTitleDiv'>"
				+ "<div class='typeExpander " + expandedClass(isExpanded) + "'></div>"
				+ "<input type='checkbox' class='includeTypeCheckbox'/>";

		titleHtml += "<span class='typeTitle'>" + text + "</span>";
				
		if (this.facetSingleSelection === FACET_SINGLE_SELECTION_BUTTON) {
			titleHtml += 
				  "<span class='thisTypeOnlyContainer'>"
					+ "<input type='button' class='thisTypeOnlyButton' value='This only'/>"
				+ "</span>";
		}
				
		titleHtml += "</div>";

		
		typeTitleDiv.innerHTML = titleHtml;

		append(typeDiv, typeTitleDiv);
		
		var checkbox = typeTitleDiv.getElementsByClassName('includeTypeCheckbox')[0];
		checkbox.checked = checked;
		
		var listsDiv = document.createElement('div');
		
		listsDiv.setAttribute('class', 'facetListsDiv');
		
		var typeExpander = typeTitleDiv.getElementsByClassName('typeExpander')[0];
		
		// Make it possible to show and hide the below lists
		var accordion = this._makeAccordion(listsDiv, isExpanded, function (expanded) {
			var cl = expandedClass(expanded);
			
			typeExpander.className = 'typeExpander ' + cl;
		});
		
		var typeTitle = typeTitleDiv.getElementsByClassName('typeTitle')[0];

		typeExpander.onclick = accordion.onclick;
		typeTitle.onclick = accordion.onclick;

		var thisOnlyButton;
		
		if (this.facetSingleSelection === FACET_SINGLE_SELECTION_BUTTON) {
			thisOnlyButton = typeTitleDiv.getElementsByClassName('thisTypeOnlyButton')[0];
		}
		
		append(typeDiv, accordion.element);

		return { 'typeElement' : typeDiv, 'checkboxElement' : checkbox, 'thisOnlyButtonElement' : thisOnlyButton };
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
	this._makeAccordion = function(listsDiv, isExpanded, onexpandcollapse) {
		var wrapperDiv = document.createElement('div');
		
		wrapperDiv.setAttribute('class', 'facetsAccordion')
		
		if (isExpanded) {
			wrapperDiv.style.height = 'auto';
		}
		else {
			wrapperDiv.style.height = '0px';
		}
		
		append(wrapperDiv, listsDiv);

		var t = wrapperDiv;
		
		// TODO might use a data- attribute for the state instead on looking at wrapperDiv.clientDiv, seems to break sometimes.
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
					
					onexpandcollapse(false);
				},
				100);
			}
			else {
				// Any parent accordion must have heigt set to auto, otherwise this nested one will not be expanded
				// since parent accordion is hardcoded to a particular size
				for (var parentNode = wrapperDiv.parentNode; parentNode != null; parentNode = parentNode.parentNode) {
					if (parentNode.className === 'facetsAccordion') {
						parentNode.style.height = 'auto';
					}
				}

				wrapperDiv.style.height = heightToSet + 'px'; 
				onexpandcollapse(true);
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
			ul.style.margin = '0px';

			// append straight under root
			append(parentElement, ul);
		}
		else{
			// TODO perhaps find other way? But should not pass lsistDiv back to caller in createTypeContainer()
			var listsDiv = this._getListsDiv(parentElement);

			append(listsDiv, ul);
		}

		var classes = 'facetTypeList';
		
		if (isRoot) {
			classes += ' rootFacetTypeList'
		}
		
		ul.setAttribute("class", classes);
		
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
	
	this.createAttributeListElement = function(parentElement, text, isExpanded) {

		function expandedClass(expanded) {
			return expanded ? 'attributeExpanderExpanded' :'attributeExpanderCollapsed';
		}
	
		var li = document.createElement('li');
		
		li.style['list-style'] = 'none';

		// In order to have a clickable title, we must add a div

		var attributeTitleDiv = document.createElement('div');

		attributeTitleDiv.innerHTML =
			"<div class='attributeTitleDiv'>"
			+ "<div class='attributeExpander " + expandedClass(isExpanded) + "'></div>"
			+ "<span class='attributeTitle'>" + text + "</span>"
		  + "</div>";

		var attributeDiv = document.createElement('div');

		append(li, attributeTitleDiv);

		var accordion = this._makeAccordion(attributeDiv, isExpanded, function (expanded) {
			var cl = expandedClass(expanded);

			attributeTitleDiv.getElementsByClassName('attributeExpander')[0].className = 'attributeExpander ' + cl;
		});

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

	this.createAttributeRangesList = function(parentElement, refreshOnUpdate) {
		
		var viewElement;
		
		var ul = document.createElement('ul');

		append(parentElement, ul);

		ul.setAttribute("class", "facetAttributeRangeList");
		
		return ul;
	}

	this.createAttributeInputView = function(parentElement, refreshOnUpdate) {

		var div = document.createElement('div');
		
		div.setAttribute('class', 'facetRangeInputDiv');
		
		_addRangeInput(div, refreshOnUpdate, 'min', 'facetRangeLowerInput');
		_addRangeInput(div, refreshOnUpdate, 'max', 'facetRangeUpperInput');
		
		append(parentElement, div);

		return div;
	}
	
	function _addRangeInput(div, refreshOnUpdate, placeholderText, cssClass) {
		var input = document.createElement('input');
		
		input.type = 'number';
		input.placeholder = placeholderText;
		input.size = 10;
		input.min = 0;
		input.max = 100000000;
		input.onchange = function() {
			refreshOnUpdate();
		};
		
		input.setAttribute('class', cssClass);
		
		append(div, input);
	}
	
	this.getRangeInput = function(div) {
		
		var inputs = div.getElementsByTagName('input');
		
		return {
			'lower' : _toNumberOrNull(inputs[0].value),
			'upper' : _toNumberOrNull(inputs[1].value)
		};
	}
	
	function _toNumberOrNull(text) {
		return text !== '' ? new Number(text) : null;
	}

	this._makeAttributeValueText = function(value, matchCount) {
		return value + ' (' + matchCount + ')';
	}
	
	this.createAttributeValueElement = function(parentElement, value, matchCount, hasSubAttributes, isExpanded, checked) {
		var li = document.createElement('li');
		
		li.style['list-style'] = 'none';
		
		var checkbox = document.createElement('input');
		checkbox.setAttribute('class', 'attributeValueCheckbox');
		checkbox.type = 'checkbox';
		checkbox.checked = checked;

		var thisOnlyButton;
		var titleElement;

		titleElement = document.createElement('span');
		titleElement.setAttribute('class', 'attributeValueTitle');
		titleElement.innerHTML = this._makeAttributeValueText(value, matchCount);
		
		if (this.facetSingleSelection === FACET_SINGLE_SELECTION_BUTTON) {

			var thisAttrValueOnlyContainer = document.createElement('span');
			
			thisAttrValueOnlyContainer.setAttribute('class', 'thisAttributeValueOnlyContainer');
	
			thisAttrValueOnlyContainer.innerHTML = "<input type='button' class='thisAttributeValueOnlyButton' value='This only'/>";
	
			thisOnlyButton = thisAttrValueOnlyContainer.getElementsByTagName('input')[0];
		}

		if (hasSubAttributes) {
			
			function expandedClass(expanded) {
				return expanded ? 'attributeValueExpanderExpanded' :'attributeValueExpanderCollapsed';
			}

			// There are sub attributes so we have to make sure they are expandable
			var valueNameDiv = document.createElement('div');
			valueNameDiv.setAttribute('class', 'attributeValueTitleDiv');

			var attributeValueExpander = document.createElement('div');
			
			attributeValueExpander.className = 'attributeValueExpander ' + expandedClass(isExpanded);
			
			append(valueNameDiv, attributeValueExpander);
			append(valueNameDiv, checkbox);
			append(valueNameDiv, titleElement);

			if (this.facetSingleSelection === FACET_SINGLE_SELECTION_BUTTON) {
				append(valueNameDiv, thisAttrValueOnlyContainer);
			}

			append(li, valueNameDiv);

			var listDiv = document.createElement('div');
			listDiv.setAttribute('class', 'facetListsDiv');
	
			var accordion = this._makeAccordion(listDiv, isExpanded, function (expanded) {
				attributeValueExpander.className = 'attributeValueExpander ' + expandedClass(expanded);
			});

			titleElement.onclick = accordion.onclick;
			attributeValueExpander.onclick = accordion.onclick;

			append(li, accordion.element);
		}
		else {
			var valueNameDiv = document.createElement('div');
			
			valueNameDiv.setAttribute('class', 'attributeValueTitleDiv');

			append(valueNameDiv, checkbox);
			append(valueNameDiv, titleElement);
			if (this.facetSingleSelection === FACET_SINGLE_SELECTION_BUTTON) {
				append(valueNameDiv, thisAttrValueOnlyContainer);
			}
			
			append(li, valueNameDiv);
		}
			
		append(parentElement, li);

		li.setAttribute("class", "facetAttributeValueElement");

		return { 'listItem' : li, 'checkboxItem' : checkbox, 'thisOnlyItem' : thisOnlyButton };
	}

	this.updateAttributeValueElement = function(element, value, matchCount) {
		var text = this._makeAttributeValueText(value, matchCount);
		
		element.getElementsByClassName('attributeValueTitle')[0].innerHTML = text;
	}

	this.setCheckboxOnClick = function(checkbox, onCheckboxClicked) {
		checkbox.onclick = function(event) {
			onCheckboxClicked(checkbox.checked);
		};
	}

	this.setAttributeThisOnlyButtonOnClick = function(thisOnlyButton, onButtonClicked) {
		if (this.facetSingleSelection === FACET_SINGLE_SELECTION_BUTTON) {
			thisOnlyButton.onclick = function(event) {
				onButtonClicked();
			};
		}
	}

	this.setTypeThisOnlyButtonOnClick = function(thisOnlyButton, onButtonClicked) {

		if (this.facetSingleSelection === FACET_SINGLE_SELECTION_BUTTON) {
			thisOnlyButton.onclick = function(event) {
				onButtonClicked();
			};
		}
	}

	this.setCheckboxChecked = function(checkbox, checked) {
		checkbox.checked = checked;
	}

	this.isCheckboxChecked = function(checkbox) {
		return checkbox.checked;
	};
	
	this.removeElement = function(parent, sub) {
		parent.removeChild(sub);

		this._updateAccordionHeight(parent);
	}

	this.hideElement = function(sub) {
		sub.style.display = 'none';

		this._updateAccordionHeight(sub.parentNode);
	}

	this.showElement = function(sub) {
		sub.style.display = 'block';

		this._updateAccordionHeight(sub.parentNode);
	}

	this._updateAccordionHeight = function(parent) {
		if (true) {
			var accordionNode = parent.parentNode.parentNode;
	
			if (   accordionNode.className === 'facetsAccordion'
				&& accordionNode.style.height != '0px'
				&& accordionNode.getAttribute('data-accordion-in-progress') != true) {
	
				accordionNode.style.height = 'auto';
			}
		}
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
