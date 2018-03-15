/**
 * Facet view logic that creates the layout and collects selected criteria on checkbox clicks
 * but does not operate on the DOM directly, rather calls separate instance for that
 */


function FacetView(divId, facetViewElements) {

	var ITER_CONTINUE = 1; 	// Continue recursive iteration
	var ITER_BREAK = 2; 	// Break out of iteration, also current level (eg for arrays, skip the rest of indices)
	var ITER_SKIP_SUB = 3;	// Skip recursion into sub elements but continue all iteration at same level 
	
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
		this.rootTypes = model.iterate(
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

						var typeList = new FacetTypeList(t.getViewElementFactory(), cur != null ? cur.getModelType() : null, typeListElement);
						 
						if (cur != null) { // If not root type list
							cur.setTypeList(typeList);
						}
						
						cur = typeList;
					}
					else if (kind === 'attribute') {
						console.log("Attribute array of length " + length + ", cur=" + print(cur));

						cur = t._addFacetAttributeList(cur.getViewElementFactory(), cur);
					}
					else if (kind === 'attributeValue') {
						console.log("Attribute value array of length " + length + ", cur=" + print(cur));
						
						cur = t._addFacetAttributeValueList(cur.getViewElementFactory(), cur);
					}
					else if (kind === 'attributeRange') {
						console.log("Attribute range array of length " + length + ", cur=" + print(cur));
						
						cur = t._addFacetAttributeRangeList(cur.getViewElementFactory(), cur);
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
						
						cur = t._addFacetType(viewElementFactory, cur, element);
					}
					else if (kind == 'attribute') {
						console.log("Attribute element " + element.displayName + ", cur=" + print(cur));

						cur = t._addFacetAttribute(viewElementFactory, cur, element);
					}
					else if (kind == 'attributeValue') {
						console.log("Attribute value element " + element.value + ", cur=" + print(cur));
						
						cur = t._addFacetSingleValue(viewElementFactory, cur, element);
					}
					else if (kind == 'attributeRange') {
						console.log("Attribute value element " + element.value + ", cur=" + print(cur));
						
						cur = t._addFacetAttributeRange(viewElementFactory, cur, element);
					}
					else {
						throw "Neither type nor attribute: " + kind;
					}
					
					return cur;
				});
		
		var x = 123;
		
	};
	
	this._addFacetAttributeList = function(viewElementFactory, cur) {
		var viewElementFactory = cur.getViewElementFactory();

		// Array of attributes
		var attributeListElement = viewElementFactory.createAttributeList(cur.getViewElement());

		var attributeList = new FacetAttributeList(viewElementFactory, cur.getModelType(), attributeListElement);
		
		cur.setAttributeList(attributeList);
		
		return attributeList;
	}

	this._addFacetAttributeValueList = function(viewElementFactor, cur) {
		var viewElementFactory = cur.getViewElementFactory();

		// Array of attributes
		var attributeListElement = viewElementFactory.createAttributeValueList(cur.getViewElement());

		var attributeValueList = new FacetAttributeValueList(viewElementFactory, cur.getModelType(), cur.getAttributeId(), attributeListElement);
		
		cur.setAttributeValueOrRangeList(attributeValueList);

		return attributeValueList;
	}
	
	this._addFacetAttributeRangeList = function(viewElementFactor, cur) {
		var viewElementFactory = cur.getViewElementFactory();

		// Array of attributes
		var attributeListElement = viewElementFactory.createAttributeRangeList(cur.getViewElement());

		var attributeRangeList = new FacetAttributeRangeList(viewElementFactory, cur.getModelType(), cur.getAttributeId(), attributeListElement);
		
		cur.setAttributeValueOrRangeList(attributeRangeList);

		return attributeRangeList;
	}
	
	this._addFacetType = function(viewElementFactory, cur, element) {
		// Add a div for the particular type, will have a box for expanding the type
		var typeElement = viewElementFactory.createTypeContainer(cur.getViewElement(), element.typeDisplayName);
		
		var typeContainer = new FacetTypeContainer(viewElementFactory, element.type, typeElement, element.typeDisplayName);
		
		cur.addType(typeContainer);

		return typeContainer;
	}

	this._addFacetAttribute = function(viewElementFactory, cur, element) {
		// Attribute within a type in list of attributes
		var attributeElement = viewElementFactory.createAttributeListElement(cur.getViewElement(), element.name);
		
		var attribute = new FacetAttribute(viewElementFactory, cur.getModelType(), element.id, attributeElement);
		
		cur.addAttribute(attribute);
		
		return attribute;
	}

	this._addFacetSingleValue = function(viewElementFactory, cur, element) {
		var hasSubAttributes = typeof element.subAttributes !== 'undefined';
		
		// Attribute within a type in list of attributes
		var attributeElement = viewElementFactory.createAttributeValueElement(
				cur.getViewElement(),
				element.value,
				element.matchCount,
				hasSubAttributes);
		
		var attributeValue = new FacetAttributeSingleValue(
								viewElementFactory,
								cur.getModelType(),
								cur.getAttributeId(),
								element.value,
								attributeElement.listItem,
								attributeElement.checkboxItem);
		
		this._setAttributeCheckboxListener(viewElementFactory, attributeValue, hasSubAttributes);
				
		cur.addValue(attributeValue);

		return attributeValue;
	}
	
	this._addFacetAttributeRange = function(viewElementFactory, cur, element) {
		var text = '';
		
		text += (typeof element.lower !== 'undefined' ? element.lower : ' ');
		
		text += ' - ';
		text += (typeof element.upper !== 'undefined' ? element.upper : '');

		// Attribute within a type in list of attributes
		var attributeElement = viewElementFactory.createAttributeValueElement(cur.getViewElement(), text, element.matchCount);
		
		var attributeRange = new FacetAttributeRange(
				viewElementFactory,
				cur.getModelType(),
				cur.getAttributeId(),
				element,
				attributeElement.listItem,
				attributeElement.checkboxItem);

		this._setAttributeCheckboxListener(viewElementFactory, attributeRange, false);

		console.log('cur type: ' + cur.getClassName());
		cur.addRange(attributeRange);

		return attributeRange;
	};
	
	this._setAttributeCheckboxListener = function(viewElementFactory, attributeValue, hasSubAttributes) {
		
		var t = this;

		var onCheckboxClicked = function(checked) {
			if (hasSubAttributes) {
				// We must update the state of all sub attributes. If checked, update all sub attributes as checked.
				// otherwise mark all as non-checked
				
				// Iterate recursively
				attributeValue.iterate(function(kind, obj) {
					console.log('Sub obj: ' + kind + ", " + JSON.stringify(obj));
					if (kind === 'FacetAttributeSingleValue' || kind == 'FacetAttributeRange') {
						// Set checked-property to same
						obj.getViewElementFactory().setCheckBoxChecked(obj.checkboxItem, checked);
					}
				});
			}
			
			var criteria = t.collectCriteriaAndTypesFromSelections();
			
			console.log('search criteria changed:\n' + print(criteria));
		};

		viewElementFactory.setCheckboxOnClick(attributeValue.checkboxItem, onCheckboxClicked);
	}

	this.collectCriteriaAndTypesFromSelections = function() {

		var types = {};
		var criteria = [];
		
		this.rootTypes.iterate(function(className, obj) {
			if (className == 'FacetAttributeValueList' || className === 'FacetAttributeRangeList') {
				
				var attributeValues = className == 'FacetAttributeValueList'
						? obj.getValues()
						: obj.getRanges();
				
				// Iterate all values and figure how many are selected
				var numSelected = 0;
				
				for (var i = 0; i < attributeValues.length; ++ i) {
					var value = attributeValues[i];

					if (obj.getViewElementFactory().isCheckboxSelected(value.checkboxItem)) {
						++ numSelected;
					}
				}
				
				if (numSelected > 0) {
					
					// Add type so that query can have list of which types attribute are from (for faster search)
					types[obj.getModelType()] = '';

					var criterium = {
						type : obj.getModelType(),
						attribute : obj.getAttributeId(),
					};
					
					
					// Scan again to add values to search criteria
					for (var i = 0; i < attributeValues.length; ++ i) {
						var value = attributeValues[i];

						if (obj.getViewElementFactory().isCheckboxSelected(value.checkboxItem)) {
							++ numSelected;
							
							if (value.getClassName() === 'FacetAttributeSingleValue') {
								if (typeof criterium.values === 'undefined') {
									criterium.values = [];
								}
								criterium.values.push({ value : value.getModelValue() });
							}
							else if (value.getClassName() === 'FacetAttributeRange') {
								if (typeof criterium.ranges === 'undefined') {
									criterium.ranges = [];
								}

								var mr = value.getModelRange();
								
								var range = { };
								
								if (typeof mr.lower !== 'undefined') {
									range.lower = mr.lower;
									range.includeLower = true;
								}
								
								if (typeof mr.upper !== 'undefined') {
									range.upper = mr.upper;
									range.includeUpper = true;
								}
								
								criterium.ranges.push(range);
							}
						}
					}
					
					criteria.push(criterium);
				}
			}
		});

		return { 'types' : Object.keys(types), criteria };
	};

	this._makeCriterium = function(attributeId, type, selectionCount) {

		// There is a selection, add criterium
		var criterium = {
				'type' : type,
				'attribute' : attributeId
		};

		var values = [];

		var numAdded = 0;
		for (var selectionIdx = 0; selectionIdx < selectionCount; ++ selectionIdx) {
			var selected = this.view.isAttributeSelected(typeIdx, attrIdx, selectionIdx);

			if (attribute.isValueAttribute()) {
				var value = attribute.getValue(selectionIdx);

				values.push(value);
			}
			else if (attribute.isRangeAttribute()) {
				var range = attribute.getRange(selectionIdx);

				values.push( { 'lower' :  range.getLower(), 'includeLower' : true, 'upper' : range.getUpper(), 'includeUpper' : false } );
			}
			else {
				throw "Neither value nor range attribute: " + attribute.getName();
			}
		}
		
		values += ' ]';
		
		criterium.values = values;
		
		return criterium;
	}

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

	FacetsElementBase.prototype.getClassName = function() {
		return this.className;
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
		
		var exitCode = ITER_CONTINUE;

		if (typeof iterable !== 'undefined') {
			var iter = iterable.iterate(each, iterable);
			
			if (iter == ITER_SKIP_SUB) {
				throw "Should not return ITER_SKIP_SUB from sub elements";
			}
			
			exitCode = iter;
		}
		
		return exitCode;
	}

	FacetsElementBase.prototype.iterate = function(each) {
		this._iterateSub(function(className, obj, parent) {

			each(className, obj);
			
			return ITER_CONTINUE;
		});
	}
	
	FacetsElementBase.prototype.iterateUntilReturnFalse = function(each) {
		return this._iterateSub(function(className, obj, parent) {
			return each(className, obj, parent);
		});
	}

	
	FacetsElementBase.prototype._iterateArray = function(array, each) {
		
		var exitCode = ITER_CONTINUE;
		
		for (var i = 0; i < array.length; ++ i) {
			var iter = array[i]._iterateCurAndSub(each, this);
			
			if (iter == ITER_CONTINUE ) {
				// Continue loop
			}
			else if (iter == ITER_BREAK) {
				exitCode = ITER_BREAK;
				break;
			}
			else if (iter == ITER_SKIP_SUB) {
				throw "Should not return ITER_SKIP_SUB from sub elements";
			}
			else {
				throw "Unknown iter code: " + iter;
			}
		}

		return exitCode;
	}


	FacetsElementBase.prototype._iterateCurAndSub = function(each, parent) {
		
		var iter = each(this.className, this, parent);
		var exitCode;
		
		if (iter == ITER_CONTINUE ) {
			exitCode = this._iterateSub(each);
		}
		else if (iter == ITER_BREAK) {
			exitCode = ITER_BREAK;
		}
		else if (iter == ITER_SKIP_SUB) {
			exitCode = ITER_CONTINUE;
		}
		else {
			throw "Unknown iter code: " + iter;
		}

		return exitCode;
	}

	// Container for a facet type
	function FacetTypeContainer(viewElementFactory, modelType, container) {
		FacetsElementBase.call(this, 'FacetTypeContainer', viewElementFactory, modelType, container);
	}
	
	FacetTypeContainer.prototype = Object.create(FacetsElementBase.prototype);

	FacetTypeContainer.prototype._iterateSub = function(each) {

		var iter = this._iterateIfDefined(this.typeList, each);
		if (iter === ITER_BREAK) {
			return ITER_BREAK;
		}

		iter = this._iterateIfDefined(this.attributeList, each);
		if (iter === ITER_BREAK) {
			return ITER_BREAK;
		}

		return ITER_CONTINUE;
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
		return this._iterateArray(this.types, each);
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
		return this._iterateArray(this.attributes, each);
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
		return this.attributeValueOrRangeList._iterateCurAndSub(each, this);
	}

	FacetAttribute.prototype.setAttributeValueOrRangeList = function(attributeValueOrRangeList) {
		
		checkNonNull(attributeValueOrRangeList);
		
		this.attributeValueOrRangeList = attributeValueOrRangeList;
	}

	// List of faceted attributes ("With", "Price")
	function FacetAttributeValueList(viewElementFactory, modelType, attributeId, listItem) {
		FacetsElementBase.call(this, 'FacetAttributeValueList', viewElementFactory, modelType, listItem);

		this.attributeId = attributeId;
		this.values = [];
	}

	FacetAttributeValueList.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttributeValueList.prototype._iterateSub = function(each) {
		return this._iterateArray(this.values, each);
	}

	FacetAttributeValueList.prototype.getAttributeId = function() {
		return this.attributeId;
	}

	
	FacetAttributeValueList.prototype.addValue = function(value) {

		checkNonNull(value);

		this.values.push(value);
	}

	FacetAttributeValueList.prototype.getValues = function() {
		return this.values;
	}

	function FacetAttributeRangeList(viewElementFactory, modelType, attributeId, listItem) {
		FacetsElementBase.call(this, 'FacetAttributeRangeList', viewElementFactory, modelType, listItem);

		this.attributeId = attributeId;
		this.ranges = [];
	}

	FacetAttributeRangeList.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttributeRangeList.prototype._iterateSub = function(each) {
		return this._iterateArray(this.ranges, each);
	}

	FacetAttributeRangeList.prototype.getAttributeId = function() {
		return this.attributeId;
	}
	
	FacetAttributeRangeList.prototype.addRange = function(range) {

		checkNonNull(range);

		this.ranges.push(range);
	}

	FacetAttributeRangeList.prototype.getRanges = function() {
		return this.ranges;
	}

	function FacetAttributeValue(className, viewElementFactory, modelType, attributeId, listItem, checkboxItem) {
		FacetsElementBase.call(this, className, viewElementFactory, modelType, listItem);
	
		this.attributeId = attributeId;
		this.checkboxItem = checkboxItem;
	}
	
	FacetAttributeValue.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttributeValue.prototype.getAttributeId = function() {
		return this.attributeId;
	}

	FacetAttributeValue.prototype._iterateSub = function(each) {
		if (typeof this.attributeList !== 'undefined') {
			return this.attributeList._iterateCurAndSub(each, this);
		}
		
		return ITER_CONTINUE;
	}

	// For subattributes
	FacetAttributeValue.prototype.setAttributeList = function(attributeList) {
		
		checkNonNull(attributeList);

		this.attributeList = attributeList;
	}
	
	function FacetAttributeSingleValue(viewElementFactory, modelType, attributeId, modelValue, listItem, checkboxItem) {
		FacetAttributeValue.call(this, 'FacetAttributeSingleValue', viewElementFactory, attributeId, modelType, listItem, checkboxItem);

		this.modelValue = modelValue;
	}

	FacetAttributeSingleValue.prototype = Object.create(FacetAttributeValue.prototype);

	FacetAttributeSingleValue.prototype.getModelValue = function() {
		return this.modelValue;
	}
	
	
	function FacetAttributeRange(viewElementFactory, modelType, attributeId, modelRange, listItem, checkboxItem) {
		FacetAttributeValue.call(this, 'FacetAttributeRange', viewElementFactory, attributeId, modelType, listItem, checkboxItem);

		this.modelRange = modelRange;
	}

	FacetAttributeRange.prototype = Object.create(FacetAttributeValue.prototype);

	FacetAttributeRange.prototype.getModelRange = function() {
		return this.modelRange;
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
