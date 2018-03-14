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
						
						console.log("Type array of length " + length + ", cur=" + print(cur));
						
						// This is a type-array.
						// If cur is null, this is the root element
						
						var isRoot = cur == null;
						var parentDiv = isRoot
							? rootElement
							: cur.getViewElement();

						var typeListElement = t.getViewElementFactory().createTypeList(parentDiv, isRoot);

						var typeList = new FacetTypeList(t.getViewElementFactory(), cur != null ? cur.getModelType() :null, typeListElement);
						 
						if (cur != null) { // If not root type list
							cur.setTypeList(typeList);
						}
						
						cur = typeList;
					}
					else if (kind === 'attribute') {
						console.log("Attribute array of length " + length + ", cur=" + print(cur));

						var viewElementFactory = cur.getViewElementFactory();

						// Array of attributes
						var attributeListElement = viewElementFactory.createAttributeList(cur.getViewElement());

						var attributeList = new FacetAttributeList(viewElementFactory, cur.getModelType(), attributeListElement);
						
						cur.setAttributeList(attributeList);
						
						cur = attributeList;
					}
					else if (kind === 'attributeValue' || kind === 'attributeRange') {
						console.log("Attribute value array of length " + length + ", cur=" + print(cur));
						
						var viewElementFactory = cur.getViewElementFactory();

						// Array of attributes
						var attributeListElement = viewElementFactory.createAttributeValueList(cur.getViewElement());

						var attributeValueList = new FacetAttributeValueList(viewElementFactory, cur.getModelType(), cur.getAttributeId(), attributeListElement);
						
						cur.setAttributeValueList(attributeValueList);

						cur = attributeValueList;
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
						console.log("Type element " + element.typeDisplayName + ", cur=" + print(cur));
						
						// Add a div for the particular type, will have a box for expanding the type
						var typeElement = viewElementFactory.createTypeContainer(cur.getViewElement(), element.typeDisplayName);
						
						var typeContainer = new FacetTypeContainer(viewElementFactory, element.type, typeElement, element.typeDisplayName);
						
						cur.addType(typeContainer);
						
						cur = typeContainer;
					}
					else if (kind == 'attribute') {
						console.log("Attribute element " + element.displayName + ", cur=" + print(cur));

						// Attribute within a type in list of attributes
						var attributeElement = viewElementFactory.createAttributeListElement(cur.getViewElement(), element.name);
						
						var attribute = new FacetAttribute(viewElementFactory, cur.getModelType(), element.id, attributeElement);
						
						cur.addAttribute(attribute);
						
						cur = attribute;
					}
					else if (kind == 'attributeValue') {
						console.log("Attribute value element " + element.value + ", cur=" + print(cur));

						var hasSubAttributes = typeof element.subAttributes !== 'undefined';
						
						// Attribute within a type in list of attributes
						var attributeElement = viewElementFactory.createAttributeValueElement(
								cur.getViewElement(),
								element.value,
								element.matchCount,
								hasSubAttributes);
						
						var attributeValue = new FacetAttributeValue(
												viewElementFactory,
												cur.getModelType(),
												cur.getAttributeId(),
												attributeElement.listItem,
												attributeElement.checkboxItem);
						
						var onCheckboxClicked = function(checked) {
							if (hasSubAttributes) {
								// We must update the state of all sub attributes. If checked, update all sub attributes as checked.
								// otherwise mark all as non-checked
								
								// Iterate recursively
								attributeValue.iterate(function(kind, obj) {
									console.log('Sub obj: ' + kind + ", " + JSON.stringify(obj));
									if (kind === 'FacetAttributeValue') {
										// Set checked-property to same
										obj.getViewElementFactory().setCheckBoxChecked(obj.checkboxItem, checked);
									}
								});
							}
						};

						viewElementFactory.setCheckboxOnClick(attributeElement.checkboxItem, onCheckboxClicked);
						
						cur.addValue(attributeValue);
						
						cur = attributeValue;
					}
					else if (kind == 'attributeRange') {
						console.log("Attribute value element " + element.value + ", cur=" + print(cur));
						
						var text = '';
						
						text += (typeof element.lower !== 'undefined' ? element.lower : ' ');
						
						text += ' - ';
						text += (typeof element.upper !== 'undefined' ? element.upper : '');

						// Attribute within a type in list of attributes
						var attributeElement = viewElementFactory.createAttributeValueElement(cur.getViewElement(), text, element.matchCount);
						
						var attributeRange = new FacetAttributeValue(
								viewElementFactory,
								cur.getModelType(),
								cur.getAttributeId(),
								attributeElement.listItem,
								attributeElement.checkboxItem);

						// For the purpose of UI object tree, we just use addValue() since there is not much difference between ranges and values,
						// we only look at the value of the checkbox
						cur.addValue(attributeRange);

						cur = attributeRange;
					}
					else {
						throw "Neither type nor attribute: " + kind;
					}
					
					return cur;
				});
		
		var x = 123;
		
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

	
	function FacetsElementBase(className, viewElementFactory, modelType, viewElement) {

		if (typeof viewElement === 'undefined' || viewElement == null) {
			throw "No view element: " + viewElement;
		}
		
		this.className = className;
		this.viewElementFactory = viewElementFactory;
		this.modelType = modelType;
		this.viewElement = viewElement;
	}
	
	FacetsElementBase.prototype.getViewElementFactory = function() {
		return this.viewElementFactory;
	}
	
	FacetsElementBase.prototype.getViewElement = function() {
		return this.viewElement;
	}

	FacetsElementBase.prototype.getModelType = function() {
		return this.modelType;
	}

	FacetsElementBase.prototype._iterateIfDefined = function(iterable, each) {
		if (typeof iterable !== 'undefined') {
			iterable.iterate(each);
		}
	}

	FacetsElementBase.prototype._iterateArray = function(array, each) {
		array.forEach(function (e) { e._iterateCurAndSub(each); } )
	}

	FacetsElementBase.prototype.iterate = function(each) {
		this._iterateSub(each);
	}

	FacetsElementBase.prototype._iterateCurAndSub = function(each) {
		each(this.type, this);
		
		this._iterateSub(each);
	}

	// Container for a facet type
	function FacetTypeContainer(viewElementFactory, modelType, container) {
		FacetsElementBase.call(this, 'FacetTypeContainer', viewElementFactory, modelType, container);
	}
	
	FacetTypeContainer.prototype = Object.create(FacetsElementBase.prototype);

	FacetTypeContainer.prototype._iterateSub = function(each) {
		this._iterateIfDefined(this.typeList, each)
		this._iterateIfDefined(this.attributeList, each)
	}

	FacetTypeContainer.prototype.setTypeList = function(typeList) {
		checkNonNull(typeList);
		
		this.typeList = typeList;
	}

	FacetTypeContainer.prototype.setAttributeList = function(attributeList) {
		checkNonNull(attributeList);
		
		this.attributeList = attributeList;
	}

	// List of facet types ("Snowboards", "Skis", "Apartments") 
	function FacetTypeList(viewElementFactory, modelType, listItem) {
		FacetsElementBase.call(this, 'FacetTypeList', viewElementFactory, modelType, listItem);

		this.types = [];
	}

	FacetTypeList.prototype = Object.create(FacetsElementBase.prototype);

	FacetTypeList.prototype._iterateSub = function(each) {
		this._iterateArray(this.types, each);
	}

	FacetTypeList.prototype.addType = function(type) {
		checkNonNull(type);
		
		this.types.push(type);
	}

	// List of faceted attributes ("With", "Price")
	function FacetAttributeList(viewElementFactory, modelType, listItem) {
		FacetsElementBase.call(this, 'FacetAttributeList', viewElementFactory, modelType, listItem);

		this.attributes = [];
	}

	FacetAttributeList.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttributeList.prototype._iterateSub = function(each) {
		this._iterateArray(this.attributes, each);
	}

	FacetAttributeList.prototype.addAttribute = function(attribute) {
		checkNonNull(attribute);
		
		this.attributes.push(attribute);
	}

	// One attribute for a type, eg "Length" under "Skis"
	// will link to a checkbox to select
	function FacetAttribute(viewElementFactory, modelType, attributeId, listItem) {
		FacetsElementBase.call(this, 'FacetAttribute', viewElementFactory, modelType, listItem);
		
		this.attributeId = attributeId;
	}
	
	FacetAttribute.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttribute.prototype.getAttributeId = function(each) {
		return this.attributeId;
	}

	FacetAttribute.prototype._iterateSub = function(each) {
		this.attributeValueList._iterateCurAndSub(each);
	}

	FacetAttribute.prototype.setAttributeValueList = function(attributeValueList) {
		
		checkNonNull(attributeValueList);
		
		this.attributeValueList = attributeValueList;
	}

	// List of faceted attributes ("With", "Price")
	function FacetAttributeValueList(viewElementFactory, modelType, attributeId, listItem) {
		FacetsElementBase.call(this, 'FacetAttributeValueList', viewElementFactory, modelType, listItem);

		this.attributeId = attributeId;
		this.values = [];
	}

	FacetAttributeValueList.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttributeValueList.prototype._iterateSub = function(each) {
		this._iterateArray(this.values, each);
	}

	FacetAttributeValueList.prototype.getAttributeId = function() {
		return this.attributeId;
	}

	FacetAttributeValueList.prototype.addValue = function(value) {

		checkNonNull(value);

		this.values.push(value);
	}

	function FacetAttributeValue(viewElementFactory, modelType, attributeId, listItem, checkboxItem) {
		FacetsElementBase.call(this, 'FacetAttributeValue', viewElementFactory, modelType, listItem);
	
		this.attributeId = attributeId;
		this.checkboxItem = checkboxItem;
	}
	
	FacetAttributeValue.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttributeValue.prototype.getAttributeId = function() {
		return this.attributeId;
	}

	FacetAttributeValue.prototype._iterateSub = function(each) {
		if (typeof this.attributeList !== 'undefined') {
			this.attributeList._iterateCurAndSub(each);
		}
	}

	// For subattributes
	FacetAttributeValue.prototype.setAttributeList = function(attributeList) {
		
		checkNonNull(attributeList);

		this.attributeList = attributeList;
	}
	

	function checkNonNull(obj) {
		if (typeof obj === 'undefined' || obj == null) {
			throw "obj not set: " + obj;
		}
	}
	
	function print(obj) {
		return JSON.stringify(obj, null, 2);
	}
}
