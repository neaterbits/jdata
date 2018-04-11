/**
 * Base class functions for all gallery based objects
 *  
 */

function GalleryBase() {

}

GalleryBase.prototype.enter = function(level, functionName, args, states) {
	var argsString;
	
	if (typeof args === 'undefined') {
		argsString = '';
	}
	else {
		argsString = this._arrayToString(args);
	}
	
	var logString = 'ENTER ' + functionName + '(' + argsString + ')';
	
	if (typeof states !== 'undefined') {
		logString += ' state=[' + this._arrayToString(states) + ']';
	}
	
	this._log(level, logString);
};

GalleryBase.prototype._arrayToString = function(array) {
	arrayString = '';
	
	if (array.length % 2 != 0) {
		throw 'Number of arguments to ' + functionName + ' not an even number: ' + array.length;
	}
	
	for (var i = 0; i < array.length; i += 2) {
		if (i > 0) {
			arrayString += ', ';
		}

		arrayString += array[i] + '=' + array[i + 1];
	}
	
	return arrayString;
}


GalleryBase.prototype.exit = function(level, functionName, returnValue) {
	
	var returnString = typeof returnValue === 'undefined'
		? ""
		: " = " + returnValue;
	
	this._log(level, 'EXIT ' + functionName + '()' + returnString);
};

GalleryBase.prototype.log = function(level, text) {
	this._log(level + 1, text);
};

GalleryBase.prototype._log = function(level, text) {
	console.log(this.indent(level) + text);
};

GalleryBase.prototype.indent = function(level) {
	var s = "";

	for (var i = 0; i < level; ++ i) {
		s += "  ";
	}
	
	return s;
}

GalleryBase.prototype.isNotNull = function(obj) {
	return typeof obj !== 'undefined' && obj != null;
}

GalleryBase.prototype.isUndefinedOrNull = function(obj) {
	return typeof obj === 'undefined' || obj == null;
}

GalleryBase.prototype.checkNonNull = function(obj) {
	if (this.isUndefinedOrNull(obj)) {
		throw "obj not set: " + obj;
	}
}
