/**
 * Base class functions for all gallery based objects
 *  
 */

function GalleryBase() {
	
}

GalleryBase.prototype.enter = function(level, functionName, args) {
	var argsString;
	
	if (typeof args === 'undefined') {
		argsString = '';
	}
	else {
		argsString = '';
	
		if (args.length % 2 != 0) {
			throw 'Number of arguments to ' + functionName + ' not an even number: ' + args.length;
		}
		
		for (var i = 0; i < args.length; i += 2) {
			if (i > 0) {
				argsString += ', ';
			}

			argsString += args[i] + '=' + args[i + 1];
		}
	}
	
	
	this._log(level, 'ENTER ' + functionName + '(' + argsString + ')');
};

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

