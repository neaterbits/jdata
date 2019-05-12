/**
 * For last/next navigators on images and ads
 * 
 */

function Navigator(itemIndex, numItems, lastNavigatorDiv, nextNavigatorDiv, startUpdate) {
	
	_reset(this, itemIndex, numItems);

	this.startUpdate = startUpdate;
	
	this._isLastNavigatorEnabled = function(curShown) {
		return curShown != 0;
	};
	
	this._isNextNavigatorEnabled = function(curShown) {
		return curShown < this.numItems - 1;
	};

	var t = this;

	lastNavigatorDiv.onclick = function(event) {

		t._navigate(
				function() { return t._isLastNavigatorEnabled(t.curShownIndex); },
				function () { return t.curShownIndex - 1; },
				function() {
					-- t.curShownIndex;
					
					return t.curShownIndex;
				}
		);

		event.stopPropagation();
	};

	nextNavigatorDiv.onclick = function(event) {

		t._navigate(
				function() { return t._isNextNavigatorEnabled(t.curShownIndex); },
				function () { return t.curShownIndex + 1; },
				function() {
					++ t.curShownIndex;
					
					return t.curShownIndex;
				}
		);

		event.stopPropagation();
	};
	
	this.reset = function(itemIndex, numItems) {
		_reset(this, itemIndex, numItems);
	}

	this._navigate = function(isNavigatorEnabled, getToShowIndex, changeCurShownIndex, startUpdate) {
		
		var enabled = isNavigatorEnabled();
		
		if (enabled) {
			
			var toShow = getToShowIndex();
			
			var t = this;
			
			this.startUpdate(toShow, function() {
				var index = changeCurShownIndex();
				
				return t._getNavigatorState(index);
			});
		}
		else {
			console.log('## navigator not enabled');
		}
	};


	this._getNavigatorState = function(index) {
		return {
			'index' : index,
			'count' : this.numItems,
			'isLastEnabled' : this._isLastNavigatorEnabled(index),
			'isNextEnabled' : this._isNextNavigatorEnabled(index)
		};
	};
	
	this.getNavigatorState = function() {
		return this._getNavigatorState(this.curShownIndex);
	};
	
	function _reset(navigator, itemIndex, numItems) {
		
		if (itemIndex >= numItems) {
			throw "itemIndex >= numItems";
		}
		
		if (itemIndex < 0) {
			throw "itemIndex < 0";
		}
		
		if (numItems < 0) {
			throw "numItems < 0";
		}
		
		navigator.curShownIndex = itemIndex;
		navigator.numItems = numItems;
	}
}
