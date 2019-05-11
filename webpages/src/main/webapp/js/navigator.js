/**
 * For last/next navigators on images and ads
 * 
 */

function Navigator(numItems, lastNavigatorDiv, nextNavigatorDiv, startUpdate) {
	
	this.curShownIndex = 0;
	this.numItems = numItems;
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
}
