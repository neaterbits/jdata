/**
 * Default facet view consisting of a set nested divs
 * 
 * The facet view is encapsulates by indices, the model has to be called to get all the names to display
 */

function FacetView(divId, facetViewElements) {

	this.divId = divId;
	this.facetViewElements = facetViewElements;
	
	this.init = function(controller) {
		this.controller = controller;
	};
	
	this.getViewElementFactory = function() {
		return this.facetViewElements;
	}

	this.initFromModel = function(model) {
		// Iterate through whole model and add divs and lists as necessary
		// This is for building the UI fast at startup
		// For updates, we will dynamically add and remove elements
		
		// First element callback is always an array of types. Pass in a FacetTypeContainer
		
		var rootElement = document.getElementById(divId);
		var t = this;

		// Iterate through facet model, called back for each single element (like type or attribute)
		// and list element (subtype or attribute value/range)
		model.iterate(
				null,
				
				// Array of elements
				function (kind, length, cur) {

					// For each element, add to div-model
					if (kind === 'type') {
						
						console.log("Type array of length " + length + ", cur=" + JSON.stringify(cur));
						
						// This is a type-array.
						// If cur is null, this is the root element
						
						var isRoot = cur == null;
						var parentDiv = isRoot
							? rootElement
							: cur.getViewElement();

						var typeListElement = t.getViewElementFactory().createTypeList(parentDiv, isRoot);

						cur = new FacetTypeList(t.getViewElementFactory(), typeListElement);
					}
					else if (kind === 'attribute') {
						console.log("Attribute array of length " + length + ", cur=" + JSON.stringify(cur));

						var viewElementFactory = cur.getViewElementFactory();

						// Array of attributes
						var attributeListElement = viewElementFactory.createAttributeList(cur.getViewElement());

						cur = new FacetAttributeList(viewElementFactory, attributeListElement);
					}
					else if (kind === 'attributeValue' || kind === 'attributeRange') {
						console.log("Attribute value array of length " + length + ", cur=" + JSON.stringify(cur));
						
						var viewElementFactory = cur.getViewElementFactory();

						// Array of attributes
						var attributeListElement = viewElementFactory.createAttributeValueList(cur.getViewElement());

						cur = new FacetAttributeValueList(viewElementFactory, attributeListElement);
						
					}
					else {
						throw "Neither type nor attribute: " + kind;
					}
					
					return cur;
				},

				// Array element, create list element
				function (kind, element, index, cur) {

					var viewElementFactory = cur.getViewElementFactory();

					// For each element, add to div-model
					if (kind == 'type') {
						console.log("Type element " + element.typeDisplayName + ", cur=" + JSON.stringify(cur));
						
						// Add a div for the particular type, will have a box for expanding the type
						var typeElement = viewElementFactory.createTypeContainer(cur.getViewElement(), element.typeDisplayName);
						
						cur = new FacetTypeContainer(viewElementFactory, typeElement, element.typeDisplayName);
					}
					else if (kind == 'attribute') {
						console.log("Attribute element " + element.displayName + ", cur=" + JSON.stringify(cur));

						// Attribute within a type in list of attributes
						var attributeElement = viewElementFactory.createAttributeListElement(cur.getViewElement(), element.name);
						
						cur = new FacetAttribute(viewElementFactory, attributeElement);
					}
					else if (kind == 'attributeValue') {
						console.log("Attribute value element " + element.value + ", cur=" + JSON.stringify(cur));

						var hasSubAttributes = typeof element.subAttributes !== 'undefined';
						
						// Attribute within a type in list of attributes
						var attributeElement = viewElementFactory.createAttributeValueElement(
								cur.getViewElement(),
								element.value,
								element.matchCount,
								hasSubAttributes);
						
						cur = new FacetAttributeValue(viewElementFactory, attributeElement.listItem, attributeElement.checkboxItem);
					}
					else if (kind == 'attributeRange') {
						console.log("Attribute value element " + element.value + ", cur=" + JSON.stringify(cur));
						
						var text = '';
						
						text += (typeof element.lower !== 'undefined' ? element.lower : ' ');
						
						text += ' - ';
						text += (typeof element.upper !== 'undefined' ? element.upper : '');

						// Attribute within a type in list of attributes
						var attributeElement = viewElementFactory.createAttributeValueElement(cur.getViewElement(), text, element.matchCount);
						
						cur = new FacetAttributeValue(viewElementFactory, attributeElement);
					}
					else {
						throw "Neither type nor attribute: " + kind;
					}
					
					return cur;
				});
		
		var x = 123;
		
	};
	
	this.isAttributeSelected = function(path) {
		var attribute = this._findAttribute(path);
		
		return attribute.isSelected();
	};

	// Find the FacetAttribute for this path
	this._findAttribute = function(path) {

		var found = path.iterate(this.rootDiv, function(cur, pathLevel) {
			if (pathLevel.kind === 'type') {
				cur = cur.types[pathLevel.index];
			}
			else if (pathLevel.kind === 'attribute') {
				cur = cur.attributes[pathLevel.index];
			}
			else {
				throw "Unknown path level kind: " + pathLevel.kind;
			}
	
			return cur;
		});

		return found;
	};
	
	this._findPathLevel = function(path) {
		var pathLevel;
		
		for (var i = 0; i < path.getNumLevels(); ++ i) {
			pathLevel = path.getLevel(i);

			if (pathLevel.kind === 'type') {
				cur = cur.types[pathLevel.index];
			}
			else if (pathLevel.kind === 'attribute') {
				cur = cur.attributes.getAttribute(pathLevel.index);
			}
			else {
				throw "Unknown path level kind: " + pathLevel.kind;
			}
		}

		return pathLevel;
	};

	
	function FacetsElementBase(viewElementFactory, viewElement) {

		if (typeof viewElement === 'undefined' || viewElement == null) {
			throw "No view element: " + viewElement;
		}
		
		this.viewElementFactory = viewElementFactory;
		this.viewElement = viewElement;
	}
	
	FacetsElementBase.prototype.getViewElementFactory = function() {
		return this.viewElementFactory;
	}
	
	FacetsElementBase.prototype.getViewElement = function() {
		return this.viewElement;
	}
	
	// Container for a facet type
	function FacetTypeContainer(viewElementFactory, container) {
		FacetsElementBase.call(this, viewElementFactory, container);
	}
	
	FacetTypeContainer.prototype = Object.create(FacetsElementBase.prototype);

	// List of facet types ("Snowboards", "Skis", "Apartments") 
	function FacetTypeList(viewElementFactory, listItem) {
		FacetsElementBase.call(this, viewElementFactory, listItem);
	}

	FacetTypeList.prototype = Object.create(FacetsElementBase.prototype);

	// List of faceted attributes ("With", "Price")
	function FacetAttributeList(viewElementFactory, listItem) {
		FacetsElementBase.call(this, viewElementFactory, listItem);
	
		this.expand = function() {
			this.facetViewElements.expand();
		}
	}

	FacetAttributeList.prototype = Object.create(FacetsElementBase.prototype);

	// One attribute for a type, eg "Length" under "Skis"
	// will link to a checkbox to select
	function FacetAttribute(viewElementFactory, listItem) {
		FacetsElementBase.call(this, viewElementFactory, listItem);
	}
	
	FacetAttribute.prototype = Object.create(FacetsElementBase.prototype);

	// List of faceted attributes ("With", "Price")
	function FacetAttributeValueList(viewElementFactory, listItem) {
		FacetsElementBase.call(this, viewElementFactory, listItem);
	
		this.expand = function() {
			this.facetViewElements.expand();
		}
	}

	FacetAttributeValueList.prototype = Object.create(FacetsElementBase.prototype);

	function FacetAttributeValue(viewElementFactory, listItem, checkboxItem) {
		FacetsElementBase.call(this, viewElementFactory, listItem);
	
		this.checkboxItem = checkboxItem;
	
		this.isSelected = function() {
			return facetViewElements.isSelected(checkboxItem);
		}
	}
	
	FacetAttributeValue.prototype = Object.create(FacetsElementBase.prototype);
}
