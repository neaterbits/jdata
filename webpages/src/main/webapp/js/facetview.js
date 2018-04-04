/**
 * Facet view logic that creates the layout and collects selected criteria on checkbox clicks
 * but does not operate on the DOM directly, rather calls separate instance for that
 */


function FacetView(divId, facetViewElements, onCriteriaChanged) {

	var ITER_CONTINUE = 1; 	// Continue recursive iteration
	var ITER_BREAK = 2; 	// Break out of iteration, also current level (eg for arrays, skip the rest of indices)
	var ITER_SKIP_SUB = 3;	// Skip recursion into sub elements but continue all iteration at same level 
	
	this.divId = divId;
	this.facetViewElements = facetViewElements;
	this.onCriteriaChanged = onCriteriaChanged;
	
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
		
		var rootElement = this.facetViewElements.getRootContainer(divId);
		var t = this;

		// Iterate through facet model, called back for each single element (like type or attribute)
		// and list element (subtype or attribute value/range)
		this.rootTypes = model.iterate(
				null,
				
				// Array of elements
				function (kind, length, cur) {

					// For each element, add to div-model
					if (kind === 'type') {
						
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
						cur = t._addFacetAttributeList(cur.getViewElementFactory(), cur);
					}
					else if (kind === 'attributeValue') {
						cur = t._addFacetAttributeValueList(cur.getViewElementFactory(), cur);
					}
					else if (kind === 'attributeRange') {
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
						cur = t._addFacetType(viewElementFactory, cur, element);
					}
					else if (kind == 'attribute') {
						cur = t._addFacetAttribute(viewElementFactory, cur, element);
					}
					else if (kind == 'attributeValue') {
						cur = t._addFacetSingleValue(viewElementFactory, cur, element);
					}
					else if (kind == 'attributeRange') {
						cur = t._addFacetAttributeRange(viewElementFactory, cur, element);
					}
					else if (kind === 'attributeValueUnknown')
						// 'Other' text for values
						cur = t._addFacetOther(viewElementFactory, cur, element);
					
					else if (kind === 'attributeRangeUnknown') {
						// 'Unknown' text for ranges
						cur = t._addFacetUnknown(viewElementFactory, cur, element);
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
	
	this._addFacetType = function(viewElementFactory, cur, element, index) {
		// Add a div for the particular type, will have a box for expanding the type
		var typeElement = viewElementFactory.createTypeContainer(
				cur.getViewElement(),
				element.displayName,
				true, // TODO expand if not root?
				true);
		
		var typeContainer = new FacetTypeContainer(viewElementFactory, element.type, typeElement, element.displayName);
		
		cur.addType(typeContainer, index);

		return typeContainer;
	}

	this._addFacetAttribute = function(viewElementFactory, cur, element, index) {
		// Attribute within a type in list of attributes
		var attributeElement = viewElementFactory.createAttributeListElement(cur.getViewElement(), element.name, false);
		
		var attribute = new FacetAttribute(viewElementFactory, cur.getModelType(), element.id, attributeElement);
		
		cur.addAttribute(attribute, index);
		
		return attribute;
	}

	this._addFacetSingleValue = function(viewElementFactory, cur, element, index) {
		var hasSubAttributes = typeof element.subAttributes !== 'undefined' && element.subAttributes != null;
		
		var displayValue = isNotNull(element.displayValue)
			? element.displayValue
			: element.value;
		
		// Attribute within a type in list of attributes
		var attributeElement = viewElementFactory.createAttributeValueElement(
				cur.getViewElement(),
				displayValue,
				element.matchCount,
				hasSubAttributes,
				false,
				true);
		
		var attributeValue = new FacetAttributeSingleValue(
								viewElementFactory,
								cur.getModelType(),
								cur.getAttributeId(),
								element.value,
								element.displayValue,
								attributeElement.listItem,
								attributeElement.checkboxItem);
		
		this._setAttributeCheckboxListener(viewElementFactory, attributeValue, hasSubAttributes);
				
		cur.addValue(attributeValue, index);

		return attributeValue;
	}
	
	this._addFacetAttributeRange = function(viewElementFactory, cur, element, index) {
		var text = '';
		
		text += (typeof element.lower !== 'undefined' && element.lower != null ? element.lower : ' ');
		
		text += ' - ';
		text += (typeof element.upper !== 'undefined' && element.upper != null ? element.upper : '');

		// Attribute within a type in list of attributes
		var attributeElement = viewElementFactory.createAttributeValueElement(
				cur.getViewElement(),
				text,
				element.matchCount,
				false,
				false,
				true);
		
		var attributeRange = new FacetAttributeRange(
				viewElementFactory,
				cur.getModelType(),
				cur.getAttributeId(),
				element,
				attributeElement.listItem,
				attributeElement.checkboxItem);

		this._setAttributeCheckboxListener(viewElementFactory, attributeRange, false);

		cur.addRange(attributeRange, index);

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
					if (kind === 'FacetAttributeSingleValue' || kind == 'FacetAttributeRange') {
						// Set checked-property to same
						obj.getViewElementFactory().setCheckBoxChecked(obj.checkboxItem, checked);
					}
				});
			}
			
			var criteria = t.collectCriteriaAndTypesFromSelections();

			t.onCriteriaChanged(criteria);
		};

		viewElementFactory.setCheckboxOnClick(attributeValue.checkboxItem, onCheckboxClicked);
	}

	// For adding 'other' or 'unknown' elements, ie. where items have no value for some attribute
	this._addFacetOther = function(viewElementFactory, cur, element, index) {
		var attributeValue = this._addFacetUnknownValueOrRange(viewElementFactory, cur, element, index, 'Other');

		cur.addValue(attributeValue, index);
	}

	this._addFacetUnknown = function(viewElementFactory, cur, element, index) {
		var attributeRange = this._addFacetUnknownValueOrRange(viewElementFactory, cur, element, index, 'Unknown');
		
		cur.addRange(attributeRange, index);
	}

	this._addFacetUnknownValueOrRange = function(viewElementFactory, cur, element, index, displayText) {

		// Attribute within a type in list of attributes
		var attributeElement = viewElementFactory.createAttributeValueElement(
				cur.getViewElement(),
				displayText,
				element.matchCount,
				false,
				false,
				true);

		var attributeValue = new FacetAttributeOtherOrUnknown(
								viewElementFactory,
								cur.getModelType(),
								cur.getAttributeId(),
								attributeElement.listItem,
								attributeElement.checkboxItem);

		this._setAttributeCheckboxListener(viewElementFactory, attributeValue, false);
				
		return attributeValue;
	}
	
	/**
	 * Refresh from a new model, removing adding new types or attributes, removing those no longer present.
	 * We do that without rebuilding the whole thing, this way current selection state is maintained
	 */
	this.refreshFromNewModel = function(model) {
		
		// First iterate over all items and set them as not in use, this will allow us to remove unused elements later
		this.rootTypes.iterate(function(className, obj) { obj.setInUse(false); })

		// Iterate over model to mark all elements that are still there as in use
		this._markAllStillInDataModelAsInUse(model);

		// Remove unused elements first so that indices become correct when inserting elements from model afterwards
		this._removeElementsNotInUse(model);
		
		// Mark elements as used
		// Also add UI elements for any new elements from model
		this._addAllNewElementsFromDataModel(model);
	}
	
	this._addAllNewElementsFromDataModel = function(model) {

		var t = this;
		
		// Must wrap root types list in similar to FacetTypeContainer so that works for initial access
		// cur below is really parent element from view model
		var root = {
			getTypeList : function() {
				return t.rootTypes;
			}
		};

		model.iterate(
				root, // instead of this.rootTypes,
				
				// Array of elements
				function (kind, length, cur) {
					
					if (kind === 'type') {
						// FacetTypeList under FacetTypeContainer
						if (isNotNull(cur.getTypeList())) {
							// Existing list
							cur = cur.getTypeList();
						}
						else {
							cur = t._addTypeList(cur.getViewElementFactory(), cur);
						}
					}
					else if (kind === 'attribute') {
						// Cur may be FacetTypeContainer or FacetAttributeValue (in case of subattributes)
						if (isNotNull(cur.getAttributeList())) {
							cur = cur.getAttributeList();
						}
						else {
							cur = t._addAttributeList(cur.getViewElementFactory(), cur);
						}
					}
					else if (kind === 'attributeValue') {
						if (isNotNull(cur.getAttributeValueOrRangeList())) {
							cur = cur.getAttributeValueOrRangeList();
						}
						else {
							cur = t._addFacetAttributeValueList(cur.getViewElementFactory(), cur);
						}
					}
					else if (kind === 'attributeRange') {
						if (isNotNull(cur.getAttributeValueOrRangeList())) {
							cur = cur.getAttributeValueOrRangeList();
						}
						else {
							cur = t._addFacetAttributeRangeList(cur.getViewElementFactory(), cur);
						}
					}
					else {
						throw "Unknown data model element: " + kind;
					}

					return cur;
				},
				
				// Array element
				function (kind, element, index, cur) {

					if (kind == 'type') {

						// cur is FacetTypeList
						var sub = cur.findType(element.type);
						if (sub == null) {
							// Add type element at index, may be in the middle of other entries in the type list
							cur = t._addFacetType(cur.getViewElementFactory(), cur, element, index);
						}
						else {
							cur = sub;
						}
					}
					else if (kind == 'attribute') {
						// FacetTypeContainer or FacetAttributeSingleValue (for subattributes)
						var sub = cur.findAttribute(element.id);
						if (sub == null) {
							cur = t._addFacetAttribute(cur.getViewElementFactory(), cur, element, index);
						}
						else {
							cur = sub;
						}
					}
					else if (kind === 'attributeValue') {
						// cur is FacetAttributeValueList
						var sub = cur.findAttributeValue(element.value);
						if (sub == null) {
							cur = t._addFacetAttributeValue(cur.getViewElementFactory(), cur, element, index);
						}
						else {
							cur = sub;
						}
					}
					else if (kind === 'attributeRange') {
						// cur is FacetAttributeValueList
						var sub = cur.findRange(element);
						
						if (cur == null) {
							cur = t._addFacetAttributeRange(cur.getViewElementFactory(), cur, element, index);
						}
						else {
							cur = sub;
						}
					}
					else if (kind === 'attributeValueUnknown') {
						// cur is FacetAttributeValueList
						var sub = cur.findValueUnknown();
						
						if (cur == null) {
							cur = t._addFacetOther(cur.getViewElementFactory(), cur, element);
						}
						else {
							cur = sub;
						}
					}
					else if (kind === 'attributeRangeUnknown') {
						// cur is FacetAttributeValueList
						var sub = cur.findValueUnknown();
						
						if (cur == null) {
							cur = t._addFacetOther(cur.getViewElementFactory(), cur, element);
						}
						else {
							cur = sub;
						}
					}
					else {
						throw "Unknown data element type for addAllNew: " + kind;
					}
					
					return cur;
				});
	};

	this._markAllStillInDataModelAsInUse = function(model) {
		
		// Must wrap root types list in similar to FacetTypeContainer so that works for initial access
		// cur below is really parent element from view model
		var t = this;
		var root = {
			getTypeList : function() {
				return t.rootTypes;
			}
		};
		
		model.iterate(
				root, // instead of this.rootTypes, which is a FacetTypeList

				// model array, eg type list
				function (kind, length, cur) {
					if (kind === 'type') {
						// FacetTypeList under FacetTypeContainer

						if (isNotNull(cur.getTypeList())) {
							// Existing list
							cur.getTypeList().setInUse(true);
							
							cur = cur.getTypeList();
						}
					}
					else if (kind === 'attribute') {
						// Cur may be FacetTypeContainer or FacetAttributeValue (in case of subattributes)
						if (isNotNull(cur.getAttributeList())) {
							// Existing list
							cur.getAttributeList().setInUse(true);
							
							cur = cur.getAttributeList();
						}
					}
					else if (kind === 'attributeValue' || kind === 'attributeRange') {
						
						var valueOrRangeList = cur.getAttributeValueOrRangeList();

						if (isNotNull(valueOrRangeList)) {
							valueOrRangeList.setInUse(true);
							
							cur = valueOrRangeList;
						}
					}
					else {
						throw "Unknown data model element: " + kind;
					}

					return cur;
				},

				function (kind, element, index, cur) {
					var sub;
					
					if (kind == 'type') {
						// cur is FacetTypeList
						sub = cur.findType(element.type);
					}
					else if (kind == 'attribute') {
						// cur is FacetAttributeList
						sub = cur.findAttribute(element.id);
					}
					else if (kind === 'attributeValue') {
						// cur is FacetAttributeValueList
						sub = cur.findAttributeValue(element.value);
					}
					else if (kind === 'attributeRange') {
						// cur is FacetAttributeValueList
						sub = cur.findRange(element);
					}
					else if (kind === 'attributeValueUnknown') {
						sub = cur.findValueUnknown();
					}
					else if (kind === 'attributeRangeUnknown') {
						sub = cur.findValueUnknown();
					}
					else {
						throw "Unknown data element type for markAllStillInUse: " + kind;
					}

					if (sub != null) {
						sub.setInUse(true);

						cur = sub;
					}
					
					return cur;
 				});
	}

	this._removeElementsNotInUse = function() {
		this.rootTypes.iterateWithReturnValue(function (className, obj, parent) {
			
			var iter;

			if (!obj.isInUse()) {
				// Object no longer in use so must remove from DOM and skip recursing to sub-elements
				// since we only need to remove this element from the DOM
				
				// This depends on the type of element this is. It might be
				// - a type, if type is no longer matched
				// - an attribute, if no matching elements have attribute set
				// - a typelist, if no subtypes match
				// - an attribute list, if type or single attribute value no longer has any attributes for matching elements
				// - an attribute value, if no matching elements have this value for the attribute
				// - an attribute range, if no matching elements are within this range for the attribute
				
				
				// It might not be
				// - an attribute value list, since an empty such would mean the attribute should not be returned in the first place
				// - an attribute range list, since an empty such would mean the attribute should not be returned in the first place

				if (className == 'FacetAttributeValueList' || className === 'FacetAttributeRangeList') {
					throw "Classname " + className + " should either be in use or not appear here";
				}

				parent.removeSub(obj);

				// Remove from DOM

				obj.getViewElementFactory().removeElement(parent.getViewElement(), obj.getViewElement());
				
				iter = ITER_SKIP_SUB;
			}
			else {
				iter = ITER_CONTINUE;
			}
			
			return iter;
		});
	}
	
	this.collectCriteriaAndTypesFromSelections = function() {

		var types = {};
		var toplevelCriteria = [];
		
		// A stack for figuring out whether should add to model as subattributes to a current value
		// Eg model is a subattribute of make and hence should put model criteria under make so
		// can qualify that in DB query (eg two different makes withs same model or more likely two states in a country with counties wtih same name)
		var valuesStack = [];
		
		this.rootTypes.iterate(function(className, obj) {
			if (className == 'FacetAttributeValueList' || className === 'FacetAttributeRangeList') {

				// Get array of either FacetAttributeSingleValue or FacetAttributeRange
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
				
				var criterium = null;
				
				if (numSelected > 0) {
					
					// Add type so that query can have list of which types attribute are from (for faster search)
					types[obj.getModelType()] = '';

					// Are we in a subattribute?
					if (valuesStack.length != 0) {
						var valueStackItem = valuesStack[valuesStack.length - 1];
						
						if (valueStackItem.type !== 'value') {
							throw "Expected value item on stack, got " + JSON.stringify(valueStackItem);
						}

						var listStackItem = valuesStack[valuesStack.length - 2];
						if (listStackItem.type !== 'list') {
							throw "Expected list item on stack, got " + JSON.stringify(listStackItem);
						}
						
						// Append as sub-criteria to last list
						var criteriumValue = null; // JS object, not model value
						
						for (var i = 0; i < listStackItem.criterium.values.length; ++ i) {
							var v = listStackItem.criterium.values[i];
							
							if (v.value === valueStackItem.modelValue) {
								criteriumValue = v;
								break;
							}
						}

						if (criteriumValue == null) {
							throw "No criterium value for " + valueStackItem.modelValue;
						}

						if (typeof criteriumValue.subCriteria === 'undefined') {
							criteriumValue.subCriteria = [];
						}

						criterium = {
								type : obj.getModelType(),
								attribute : obj.getAttributeId(),
						};

						// Add to sub-criteria array
						criteriumValue.subCriteria.push(criterium);
					}
					else {
						criterium = {
							type : obj.getModelType(),
							attribute : obj.getAttributeId(),
						};

						// push to toplevel
						toplevelCriteria.push(criterium);
					}
					
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
							else if (value.getClassName() == 'FacetAttributeOtherOrUnknown') {
								criterium.otherSelected = true;
							}
						}
					}
				}

				valuesStack.push({ type : 'list', criterium : criterium })
			}
			else if (className === 'FacetAttributeSingleValue') {
				valuesStack.push({ type : 'value', modelValue : obj.getModelValue() })
			}
		},
		
		// after recursion to subelements
		function(className, obj) {
			if (className == 'FacetAttributeValueList' || className === 'FacetAttributeRangeList') {
				var item = valuesStack.pop();
				
				if (item.type !== 'list') {
					throw "Expected list";
				}
			}
			else if (className === 'FacetAttributeSingleValue') {
				var item = valuesStack.pop();
				
				if (item.type !== 'value') {
					throw "Expected value";
				}
			}
		});

		return { 'types' : Object.keys(types), 'criteria' : toplevelCriteria };
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
			throw "No view element: " + viewElement + "/" + typeof viewelement;
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

	FacetsElementBase.prototype.isInUse = function() {
		return this.inUse;
	}

	FacetsElementBase.prototype.setInUse = function(inUse) {
		this.inUse = inUse;
	}
	
	FacetsElementBase.prototype._iterateIfDefinedAndNonNull = function(iterable, before, after) {

		var exitCode = ITER_CONTINUE;

		if (isNotNull(iterable)) {
			
			var iter = iterable._iterateCurAndSub(before, after, this);

			if (iter == ITER_SKIP_SUB) {
				throw "Should not return ITER_SKIP_SUB from sub elements";
			}

			exitCode = iter;
		}

		return exitCode;
	}
	
	function indent(level) {
		var s = '';
		
		for (var i = 0; i < level; ++ i) {
			s += '  ';
		}

		return s;
	}
	
	var DEBUG_ITERATE = false;

	FacetsElementBase.prototype._wrapIfDebug = function(before, after) {
		var subAfter;
		
		if (DEBUG_ITERATE) {
			var stack = []
			
			subBefore = function (className, obj, parent) {
				
				console.log(indent(stack.length) + 'ITER START ' + obj.className + ': ' + obj.toDebugString());
				
				stack.push({});
				
				return before(className, obj, parent);
			};
			
			subAfter = function (className, obj) {
				
				if (typeof obj === 'undefined') {
					throw "Undefined for classname " + className;
				}

				if (after != null) {
					after(className, obj);
				}

				stack.pop();
				
				console.log(indent(stack.length) + 'ITER END ' + obj.className + ': ' + obj.toDebugString());
			};
		}
		else {
			subBefore = before;
			subAfter = after;
		}

		return { before : subBefore, after : subAfter}
	}
	
	FacetsElementBase.prototype.iterate = function(before, after) {
		
		if (typeof before === 'undefined') {
			throw "before callback is undefined";
		}
		
		if (typeof after === 'undefined') {
			// Set to null for simpler test
			after = null;
		}
		
		var callbacks = this._wrapIfDebug(
			function(className, obj, parent) {
				before(className, obj);
				
				return ITER_CONTINUE;
			},
			after);

		this._iterateSub(callbacks.before, callbacks.after);
	}
	
	FacetsElementBase.prototype.iterateWithReturnValue = function(each) {
		
		var stack = [];
		
		
		var callbacks = this._wrapIfDebug(function(className, obj, parent) {
				return each(className, obj, parent);
			},
			null); // no after-function
		
		return this._iterateSub(callbacks.before, callbacks.after);
	}

	
	FacetsElementBase.prototype._iterateArray = function(array, before, after) {
		
		var exitCode = ITER_CONTINUE;
		
		for (var i = 0; i < array.length; ++ i) {
			var iter = array[i]._iterateCurAndSub(before, after, this);
			
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


	FacetsElementBase.prototype._iterateCurAndSub = function(before, after, parent) {
		
		var iter = before(this.className, this, parent);
		var exitCode;
		
		if (iter == ITER_CONTINUE ) {
			exitCode = this._iterateSub(before, after);
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
		
		if (after !== null) {
			after(this.className, parent);
		}

		return exitCode;
	}

	FacetsElementBase.prototype._checkAndRemoveFromArray = function(array, obj) {
		checkNonNull(array);
		checkNonNull(obj);
		
		var index = array.indexOf(obj);
		
		if (index < 0) {
			throw "Element not found in array";
		}
		
		var removed = array.splice(index, 1);
		
		if (removed.length != 1) {
			throw "No such item : " + obj;
		}
		else if (removed[0] !== obj) {
			throw "Rmoved other object: " + removed[0];
		}
	}
	
	FacetsElementBase.prototype._addToArrayWithOptionalIndex = function(array, index, obj) {

		checkNonNull(obj);
		
		if (isNotNull(index) && index < array.length) {
			array.splice(index, 0, obj);
		}
		else {
			array.push(obj);
		}
	}
	
	FacetsElementBase.prototype._arrayFindAttributeIdx = function(attributes, attributeId) {
		checkNonNull(attributes);
		checkNonNull(attributeId);
	
		for (var i = 0; i < attributes.length; ++ i) {
			if (attributes[i].attributeId === attributeId) {
				return i;
			}
		}

		return -1;
	}

	FacetsElementBase.prototype._arrayHasAttribute = function(attributes, attributeId) {
		return this._arrayFindAttributeIdx(attributes, attributeId) >= 0;
	}
	
	FacetsElementBase.prototype._arrayFindWithClassNameIdx = function(array, className) {
		checkNonNull(array);
		checkNonNull(className);
		
		for (var i = 0; i < array.length; ++ i) {
			if (array[i].className === className) {
				return i;
			}
		}

		return -1;
	}
	
	FacetsElementBase.prototype._arrayFindWithClassName = function(array, className) {
		var idx = this._arrayFindWithClassNameIdx(array, className);

		return idx >= 0 ? array[idx] : null;
	}
			
	// Container for a facet type
	function FacetTypeContainer(viewElementFactory, modelType, container) {
		FacetsElementBase.call(this, 'FacetTypeContainer', viewElementFactory, modelType, container);
	}
	
	FacetTypeContainer.prototype = Object.create(FacetsElementBase.prototype);

	FacetTypeContainer.prototype.toDebugString = function() {
		return this.modelType;
	}

	FacetTypeContainer.prototype._iterateSub = function(before, after) {

		var iter = this._iterateIfDefinedAndNonNull(this.typeList, before, after);
		if (iter === ITER_BREAK) {
			return ITER_BREAK;
		}

		iter = this._iterateIfDefinedAndNonNull(this.attributeList, before, after);
		if (iter === ITER_BREAK) {
			return ITER_BREAK;
		}

		return ITER_CONTINUE;
	}

	FacetTypeContainer.prototype.getTypeList = function() {
		return this.typeList;
	}

	FacetTypeContainer.prototype.setTypeList = function(typeList) {
		checkNonNull(typeList);
		
		this.typeList = typeList;
	}

	FacetTypeContainer.prototype.getAttributeList = function() {
		return this.attributeList;
	}

	FacetTypeContainer.prototype.setAttributeList = function(attributeList) {
		checkNonNull(attributeList);
		
		this.attributeList = attributeList;
	}
	
	FacetTypeContainer.prototype.removeSub = function(obj) {
		checkNonNull(obj);
		
		if (obj === this.typeList) {
			this.typeList = null;
		}
		else if (obj === attributeList) {
			this.attributeList = null;
		}
		else {
			throw "obj matched neither typeList nor attributeList";
		}
	}

	// List of facet types ("Snowboards", "Skis", "Apartments") 
	function FacetTypeList(viewElementFactory, modelType, listItem) {
		FacetsElementBase.call(this, 'FacetTypeList', viewElementFactory, modelType, listItem);

		this.types = [];
	}

	FacetTypeList.prototype = Object.create(FacetsElementBase.prototype);

	FacetTypeList.prototype.toDebugString = function() {
		return this.modelType + '[' + this.types.length + ']';
	}

	FacetTypeList.prototype._iterateSub = function(before, after) {
		return this._iterateArray(this.types, before, after);
	}

	FacetTypeList.prototype.findTypeIdx = function(typeName) {

		for (var i = 0; i < this.types.length; ++ i) {
			if (this.types[i].modelType === typeName) {
				return i;
			}
		}
		
		return -1;
	}

	FacetTypeList.prototype.findType = function(typeName) {
		var idx = this.findTypeIdx(typeName);

		return idx >= 0 ? this.types[idx] : null;
	}

	FacetTypeList.prototype.hasType = function(typeName) {
		return this.findTypeIdx(typeName) >= 0;
	}

	FacetTypeList.prototype.addType = function(type, index) {
		checkNonNull(type);
	
		this._addToArrayWithOptionalIndex(this.types, index, type);
	}

	FacetTypeList.prototype.removeSub = function(obj) {
		this._checkAndRemoveFromArray(this.types, obj);
	}

	// List of faceted attributes ("With", "Price")
	function FacetAttributeList(viewElementFactory, modelType, listItem) {
		FacetsElementBase.call(this, 'FacetAttributeList', viewElementFactory, modelType, listItem);

		this.attributes = [];
	}

	FacetAttributeList.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttributeList.prototype.toDebugString = function() {
		return '[' + this.attributes.length + ']';
	}

	FacetAttributeList.prototype._iterateSub = function(before, after) {
		return this._iterateArray(this.attributes, before, after);
	}

	FacetAttributeList.prototype.hasAttribute = function(attributeId) {
		return this._arrayHasAttribute(this.attributes, attributeId);
	}
	
	FacetAttributeList.prototype.findAttribute = function(attributeId) {
		var idx = this._arrayFindAttributeIdx(this.attributes, attributeId);
		
		return idx >= 0 ? this.attributes[idx] : null;
	}

	FacetAttributeList.prototype.addAttribute = function(attribute, index) {
		checkNonNull(attribute);
		
		this._addToArrayWithOptionalIndex(this.attributes, index, attribute);
	}

	FacetAttributeList.prototype.removeSub = function(obj) {
		this._checkAndRemoveFromArray(this.attributes, obj);
	}

	// One attribute for a type, eg "Length" under "Skis"
	// will link to a checkbox to select
	function FacetAttribute(viewElementFactory, modelType, attributeId, listItem) {
		FacetsElementBase.call(this, 'FacetAttribute', viewElementFactory, modelType, listItem);
		
		this.attributeId = attributeId;
	}
	
	FacetAttribute.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttribute.prototype.getAttributeId = function() {
		return this.attributeId;
	}

	FacetAttribute.prototype.toDebugString = function() {
		return this.attributeId + '[' + this.attributeValueOrRangeList.length + ']';
	}

	FacetAttribute.prototype._iterateSub = function(before, after) {
		return this.attributeValueOrRangeList._iterateCurAndSub(before, after, this);
	}

	FacetAttribute.prototype.setAttributeValueOrRangeList = function(attributeValueOrRangeList) {
		
		checkNonNull(attributeValueOrRangeList);
		
		this.attributeValueOrRangeList = attributeValueOrRangeList;
	}
	
	FacetAttribute.prototype.getAttributeValueOrRangeList = function() {
		return this.attributeValueOrRangeList;
	}

	FacetAttribute.prototype.removeSub = function(obj) {
		checkNonNull(obj);
		
		if (obj !== this.attributeValueOrRangeList) {
			throw "Obj is not attributeValueOrRangeList";
		}
		
		this.attributeValueOrRangeList = null;
	}

	// List of faceted attributes ("With", "Price")
	function FacetAttributeValueList(viewElementFactory, modelType, attributeId, listItem) {
		FacetsElementBase.call(this, 'FacetAttributeValueList', viewElementFactory, modelType, listItem);

		this.attributeId = attributeId;
		this.values = [];
	}

	FacetAttributeValueList.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttributeValueList.prototype._iterateSub = function(before, after) {
		return this._iterateArray(this.values, before, after);
	}

	FacetAttributeValueList.prototype.getAttributeId = function() {
		return this.attributeId;
	}

	FacetAttributeValueList.prototype.toDebugString = function() {
		return this.attributeId + '[' + this.values.length + ']';
	}
	
	FacetAttributeValueList.prototype.addValue = function(value, index) {

		checkNonNull(value);

		this._addToArrayWithOptionalIndex(this.values, index, value);
	}

	FacetAttributeValueList.prototype.getValues = function() {
		return this.values;
	}

	FacetAttributeValueList.prototype.removeSub = function(obj) {
		this._checkAndRemoveFromArray(this.values, obj);
	}
	
	FacetAttributeValueList.prototype._findAttributeValueIdx = function(value) {
		for (var i = 0; i < this.values.length; ++ i) {
			if (this.values[i].modelValue === value) {
				return i;
			}
		}
		
		return -1;
	}

	FacetAttributeValueList.prototype.findAttributeValue = function(value) {
		var idx = this._findAttributeValueIdx(value);
		
		return idx >= 0 ? this.values[idx] : null;
	}
		
	FacetAttributeValueList.prototype.findValueUnknown = function() {
		return this._arrayFindWithClassName(this.values, 'FacetAttributeOtherOrUnknown');
	}

	function FacetAttributeRangeList(viewElementFactory, modelType, attributeId, listItem) {
		FacetsElementBase.call(this, 'FacetAttributeRangeList', viewElementFactory, modelType, listItem);

		this.attributeId = attributeId;
		this.ranges = [];
	}

	FacetAttributeRangeList.prototype = Object.create(FacetsElementBase.prototype);

	FacetAttributeRangeList.prototype.toDebugString = function() {
		return this.attributeId + '[' + this.ranges.length + ']';
	}

	FacetAttributeRangeList.prototype._iterateSub = function(before, after) {
		return this._iterateArray(this.ranges, before, after);
	}

	FacetAttributeRangeList.prototype.getAttributeId = function() {
		return this.attributeId;
	}
	
	FacetAttributeRangeList.prototype.addRange = function(range, index) {

		checkNonNull(range);

		this._addToArrayWithOptionalIndex(this.ranges, index, range);
	}

	FacetAttributeRangeList.prototype.getRanges = function() {
		return this.ranges;
	}

	FacetAttributeRangeList.prototype.removeSub = function(obj) {
		this._checkAndRemoveFromArray(this.ranges, obj);
	}

	FacetAttributeRangeList.prototype._findRangeIdx = function(range) {
		for (var i = 0; i < this.ranges.length; ++ i) {
			var r = this.ranges[i];
			
			// null == undefined below
			if (r.modelRange.lower == range.lower && r.modelRange.upper == range.upper) {
				return i;
			}
		}
		
		return -1;
	}
	

	FacetAttributeRangeList.prototype.findRange = function(range) {
		var idx = this._findRangeIdx(range);
		
		return idx >= 0 ? this.ranges[idx] : null;
	}
	
	FacetAttributeRangeList.prototype.hasRange = function(range) {
		return this._findRangeIdx(range) >= 0;
	}

	FacetAttributeRangeList.prototype.findValueUnknown = function() {
		return this._arrayFindWithClassName(this.ranges, 'FacetAttributeOtherOrUnknown');
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

	FacetAttributeValue.prototype.toDebugString = function() {
		return this.attributeId;
	}

	FacetAttributeValue.prototype._iterateSub = function(before, after) {
		if (isNotNull(this.attributeList)) {
			return this.attributeList._iterateCurAndSub(before, after, this);
		}
		
		return ITER_CONTINUE;
	}

	FacetAttributeValue.prototype.removeSub = function(obj) {
		checkNonNull(obj);
		
		if (obj !== this.attributeList) {
			throw "Obj is not attributelist";
		}
		
		this.attributeList = null;
	}

	// For subattributes
	FacetAttributeValue.prototype.getAttributeList = function() {
		return this.attributeList;
	}

	FacetAttributeValue.prototype.setAttributeList = function(attributeList) {
		
		checkNonNull(attributeList);

		this.attributeList = attributeList;
	}

	FacetAttributeValue.prototype.hasAttribute = function(attributeId) {
		return this._arrayHasAttribute(this.attributesList, attributeId);
	}

	function FacetAttributeSingleValue(viewElementFactory, modelType, attributeId, modelValue, displayValue, listItem, checkboxItem) {
		FacetAttributeValue.call(this, 'FacetAttributeSingleValue', viewElementFactory, modelType, attributeId, listItem, checkboxItem);

		this.modelValue = modelValue;
		this.displayValue = displayValue;
	}

	FacetAttributeSingleValue.prototype = Object.create(FacetAttributeValue.prototype);

	
	FacetAttributeValue.prototype.getAttributeId = function() {
		return this.attributeId;
	}

	FacetAttributeValue.prototype.toDebugString = function() {
		return this.attributeId + '=' + this.modelValue;
	}

	FacetAttributeSingleValue.prototype.getModelValue = function() {
		return this.modelValue;
	}
	
	
	function FacetAttributeRange(viewElementFactory, modelType, attributeId, modelRange, listItem, checkboxItem) {
		FacetAttributeValue.call(this, 'FacetAttributeRange', viewElementFactory, modelType, attributeId, listItem, checkboxItem);

		this.modelRange = modelRange;
	}

	FacetAttributeRange.prototype = Object.create(FacetAttributeValue.prototype);

	FacetAttributeRange.prototype.getAttributeId = function() {
		return this.attributeId;
	}

	FacetAttributeRange.prototype.toDebugString = function() {
		return this.attributeId + '=' + JSON.stringify(this.modelRange);
	}

	FacetAttributeRange.prototype.getModelRange = function() {
		return this.modelRange;
	}

	function FacetAttributeOtherOrUnknown(viewElementFactory, modelType, attributeId, listItem, checkboxItem) {
		FacetAttributeValue.call(this, 'FacetAttributeOtherOrUnknown', viewElementFactory, modelType, attributeId, listItem, checkboxItem);
	}

	FacetAttributeOtherOrUnknown.prototype = Object.create(FacetAttributeValue.prototype);

	function isNotNull(obj) {
		return typeof obj !== 'undefined' && obj != null;
	}

	function isUndefinedOrNull(obj) {
		return typeof obj === 'undefined' || obj == null;
	}

	function checkNonNull(obj) {
		if (isUndefinedOrNull(obj)) {
			throw "obj not set: " + obj;
		}
	}
	
	function print(obj) {
		return JSON.stringify(obj, null, 2);
	}
}

