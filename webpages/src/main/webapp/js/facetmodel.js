
/**
 * Model, exposes nested facets. Facets are referenced by an integer array to traverse the hierarchy
 * since each level of facets are arrays
 */

function FacetModel() {
	
	this.updateFacets = function(facets) {
		this.types = facets.types;
	}

	this.getTypeId = function(typeIdx) {
		return this.types[typeIdx].name;
	}

	// Iterate over whole model, calling functions for each array and each array element
	this.iterate = function(callerRootElement, onArray, onArrayElement) {

		if (typeof this.types === 'undefined') {
			throw "No types defined for model";
		}

		return this._iterate(this.types, 'type', callerRootElement, onArray, null, onArrayElement, null);
	}

	this.iterateWithElementEnd = function(callerRootElement, onArray, onArrayEnd, onArrayElement, onArrayElementEnd) {

		if (typeof this.types === 'undefined') {
			throw "No types defined for model";
		}

		return this._iterate(this.types, 'type', callerRootElement, onArray, onArrayEnd, onArrayElement, onArrayElementEnd);
	}

	this._iterate = function(modelCurArray, kind, callerCur, onArray, onArrayEnd, onArrayElement, onArrayElementEnd) {

		var arrayCur = onArray(kind, modelCurArray.length, callerCur);

		for (var i = 0; i < modelCurArray.length; ++ i) {
			var element = modelCurArray[i];
	
			var arrayElementCur = onArrayElement(kind, element, i, arrayCur);

			if (kind === 'type') {

				if (typeof element.subTypes !== 'undefined' && element.subTypes != null) {
					this._iterate(element.subTypes, 'type', arrayElementCur, onArray, onArrayEnd, onArrayElement, onArrayElementEnd);
				}

				if (typeof element.attributes !== 'undefined' && element.attributes != null) {
					this._iterate(element.attributes, 'attribute', arrayElementCur, onArray, onArrayEnd, onArrayElement, onArrayElementEnd);
				}
			}
			else if (kind === 'attribute') {
				
				if (element.filtering === 'INPUT') {
					
					var inputArrayCur = onArray('attributeInput', 0, arrayElementCur);

					if (onArrayEnd != null) {
						onArrayEnd('attributeInput', inputArrayCur);
					}
				}
				else if (typeof element.values !== 'undefined' && element.values != null) {
					
					var valuesArrayCur = onArray('attributeValue', element.values.length, arrayElementCur);
					
					for (var attrValueIdx = 0; attrValueIdx < element.values.length; ++ attrValueIdx) {
						var attrValue = element.values[attrValueIdx];
						
						var attrValueCur = onArrayElement('attributeValue', attrValue, attrValueIdx, valuesArrayCur);
						
						if (typeof attrValue.subAttributes !== 'undefined' && attrValue.subAttributes != null) {
							// Recursive attributes, eg County under State

							this._iterate(attrValue.subAttributes, 'attribute', attrValueCur, onArray, onArrayEnd, onArrayElement, onArrayElementEnd);
						}

						if (onArrayElementEnd != null) {
							onArrayElementEnd('attributeValue', attrValueIdx, attrValueCur);
						}
					} 

					if (typeof element.noAttributeValueCount !== 'undefined' && element.noAttributeValueCount > 0) {
						// There are elements that have no value for this element, add an element for this too
						var attrValueCur = onArrayElement('attributeValueUnknown', { matchCount : element.noAttributeValueCount }, attrValueIdx, valuesArrayCur);

						if (onArrayElementEnd != null) {
							onArrayElementEnd('attributeValueUnknown', attrValueIdx, attrValueCur);
						}
					}

					if (onArrayEnd != null) {
						onArrayEnd('attributeValue', valuesArrayCur);
					}
				}
				else if (typeof element.ranges !== 'undefined' && element.ranges != null) {
					var rangesArrayCur = onArray('attributeRange', element.ranges.length, arrayElementCur);
					
					for (var attrRangeIdx = 0; attrRangeIdx < element.ranges.length; ++ attrRangeIdx) {
						var attrRange = element.ranges[attrRangeIdx];
						
						var attrRangeCur = onArrayElement('attributeRange', attrRange, attrRangeIdx, rangesArrayCur);
	
						if (onArrayElementEnd != null) {
							onArrayElementEnd('attributeRange', attrValueIdx, attrRangeCur);
						}
					} 

					if (typeof element.noAttributeValueCount !== 'undefined' && element.noAttributeValueCount > 0) {
						// There are elements that have no value for this element, add an element for this too
						var attrRangeCur = onArrayElement('attributeRangeUnknown', { matchCount : element.noAttributeValueCount }, attrValueIdx, rangesArrayCur);
						
						if (onArrayElementEnd != null) {
							onArrayElementEnd('attributeRangeUnknown', attrValueIdx, attrRangeCur);
						}
					}

					if (onArrayEnd != null) {
						onArrayEnd('attributeRange', rangesArrayCur);
					}
				}
				
				if (onArrayElementEnd != null) {
					onArrayElementEnd(kind, i, arrayElementCur);
				}
			}
			else {
				throw "Unknown kind: " + kind;
			}
		}

		if (onArrayEnd != null) {
			onArrayEnd(kind, arrayCur);
		}

		return arrayCur;
	};

	// Get names of types at some level
	this.getsTypeNames = function(path) {

		var found = path.iterate(this.rootDiv, 'type', function(cur, pathLevel) {
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
		
		var names = [];

		for (var i = 0; i < found.types.length; ++ i) {
			names.push(this.types[i].type);
		}
		
		return names;
	};
	
	this.getAttributeArray = function(path) {
		// Return wrapper objects for 
		
		var found = path.iterate(this.rootDiv, 'attribute', function(cur, pathLevel) {
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

		var attributes = this._makeAttributeArray(found.attributes);

		return attributes;
	};
	
	this._makeAttributeArray = function(attrArray) {
		
		var result = [];
		
		// Create wrapper objects for each attribute
		for (var i = 0; i < attrArray.length; ++ i) {

			var attr = attrArray[i];
			var attrObj;

			if (typeof attr.values !== 'undefined') {
				attrObj = new ValueFacetAttribute(attr);
			}
			else if (typeof attr.ranges !== 'undefined') {
				attrObj = new RangeFacetAttribute(attr);
			}
			else {
				throw "Neither value nor range attribute: " + attr.name;
			}
		}
	};
	

	// FacetAttribute base class
	function FacetAttribute(attr) {
		this.attr = attr;
	}
	
	FacetAttribute.prototype.getName = function() {
		return this.attr.name;
	}
	
	FacetAttribute.prototype.getDisplayName = function() {
		return this.attr.displayName;
	}
	
	// ValueFacetAttribute class
	function ValueFacetAttribute(attr) {
		FacetAttribute.call(attr);
	}

	ValueFacetAttribute.prototype = Object.create(FacetAttribute.prototype);

	ValueFacetAttribute.prototype.isValueAttribute = function() {
		return true;
	}

	ValueFacetAttribute.prototype.isRangeAttribute = function() {
		return false;
	}

	ValueFacetAttribute.prototype.isRangeAttribute = function() {
		return false;
	}

	ValueFacetAttribute.prototype.getSelectionCount = function() {
		return this.attr.values.length;
	}

	ValueFacetAttribute.prototype.getValue = function(index) {
		return this.attr.values[index];
	}

	ValueFacetAttribute.prototype.hasSubAttributes = function() {
		return this.attr.hasSubAttributes;
	}

	// RangeFacetAttribute class
	function RangeFacetAttribute(attr) {
		FacetAttribute.call(attr);
	}

	RangeFacetAttribute.prototype = Object.create(FacetAttribute.prototype);

	RangeFacetAttribute.prototype.isValueAttribute = function() {
		return false;
	}

	RangeFacetAttribute.prototype.isRangeAttribute = function() {
		return true;
	}

	ValueFacetAttribute.prototype.getSelectionCount = function() {
		return this.attr.ranges.length;
	}
}
