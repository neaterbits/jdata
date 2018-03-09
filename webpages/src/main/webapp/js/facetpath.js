/**
 * Declares a path in within facet/type hierarchy
 * We can have types within types, eg. "Skis" under "Sports"
 * where Ski inherits from Sport, or "Apartments" under "Housing"
 *  
 */


function FacetPath(levels) {

	if (typeof levels !== 'undefined') {
		this.levels = levels;
	}
	else {
		this.levels = [];
	}
	
	this.addLevel = function(kind, index) {
		var level = new FacetPathLevel(kind, index);

		levels.push(level);
	}
	
	this.getNumLevels = function() {
		return levels.length;
	}
	
	this.getLevel = function(index) {
		return levels[index];
	}
	
	// Iterate over path and an adjacent datastructure
	this.iterate = function(initial, expectedKind, getNext) {
		var pathLevel;
		
		var cur = initial;
		
		for (var i = 0; i < this.levels.length; ++ i) {
			pathLevel = this.levels[i];

			if (pathLevel.kind === 'type') {
				cur = cur.types[pathLevel.index];
			}
			else if (pathLevel.kind === 'attribute') {
				cur = cur.attributes[pathLevel.index];
			}
			else {
				throw "Unknown path level kind: " + pathLevel.kind;
			}
		}
		
		if (pathLevel.kind !== expectedKind) {
			throw "Expected " + expectedKind + " path level kind, got: " + pathLevel.kind;
		}

		return cur;
	}
}

function FacetPathLevel(kind, index) {
	this.kind = kind;
	this.index = index;
}
