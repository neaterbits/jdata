
/**
 * Model, exposes nested facets. Facets are referenced by an integer array to traverse the hierarchy
 * since each level of facets are arrays
 */

function FacetModel(serviceUrl, allowCrossOrigin) {
	
	this.serviceUrl = serviceUrl;
	this.allowCrossOrigin = allowCrossOrigin;
	
	this.getInitial = function(onsuccess) {

		var t = this;

		// Post to get initial for all known
		this._postAjax(this.serviceUrl, function(response) {

			t._updateFacets(response);
			
			onsuccess();
		});
	}
	
	
	this.refresh = function(types, criteria, onsuccess) {
		
		var t = this;
		
		// Call REST service with criteria
		this._postAjax(serviceURL, 'POST', criteria, function(response) {
			t._updateFacets(response);

			onsuccess();
		});
	}

	this._postAjax = function(url, onsuccess) {
		var request = new XMLHttpRequest();

		request.responseType = 'json';

		request.onreadystatechange = function() {

			if (this.readyState == 4 && this.status == 200) {
				onsuccess(this.response);
			}
		};

		request.open('POST', url, true);
		
		request.send();
	};
	
	this._updateFacets = function(response) {
		this.types = response.facets.types;
	};
	
	this.getTypeId = function(typeIdx) {
		return this.types[typeIdx].name;
	}

	// Iterate over whole model, calling functions for each array and each array element
	this.iterate = function(callerRootElement, onArray, onArrayElement) {

		if (typeof this.types === 'undefined') {
			throw "No types defined for model";
		}
		
		this._iterate(this.types, 'type', callerRootElement, onArray, onArrayElement);
	};

	this._iterate = function(modelCurArray, kind, callerCur, onArray, onArrayElement) {

		callerCur = onArray(kind, modelCurArray.length, callerCur);

		for (var i = 0; i < modelCurArray.length; ++ i) {
			var element = modelCurArray[i];

			callerCur = onArrayElement(kind, element, i, callerCur);

			if (kind === 'type') {

				if (typeof element.subTypes !== 'undefined') {
					this._iterate(element.subTypes, 'type', callerCur, onArray, onArrayElement);
				}

				if (typeof element.attributes !== 'undefined') {
					this._iterate(element.attribute, 'attribute', callerCur, onArray, onArrayElement);
				}
			}
			else if (kind === 'attribute') {
				if (typeof element.subAttribtues !== 'undefined') {
					// Recursive attributes, eg County under State

					this._iterate(element.subAttributes, 'attribute', callerCur, onArray, onArrayElement);
				}
			}
			else {
				throw "Unknown kind: " + kind;
			}
		}
	};

	// Get names of types at some level
	this.getsTypeNames = function(path) {

		var found = path.iterate(this.rootDiv, 'type', function(cur, pathLevel) {
			if (pathLevel.kind === 'type') {
				cur = cur.types[pathLevel.index];
			}
			else if (pathLevel.kind === 'attribute') {
				cur = cur.attrubutes[pathLevel.index];
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
