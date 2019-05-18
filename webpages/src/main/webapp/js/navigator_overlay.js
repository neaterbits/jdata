/**
 * 
 */


function NavigatorOverlay(
		numItems,
		container,
		alwaysVisible,
		containerWidth, containerHeight,
		getWidth, getHeight,
		startUpdate) {
	
	_removeElementsWithClass(container, 'navigatorCurrentDiv');
	_removeElementsWithClass(container, 'navigatorLastDiv');
	_removeElementsWithClass(container, 'navigatorNextDiv');
	
	this.currentImageDiv = document.createElement('div')
	
	this.currentImageDiv.style.position = 'absolute';
	this.currentImageDiv.style.left = 0;
	this.currentImageDiv.style.top = 0;
	this.currentImageDiv.style.width = containerWidth;
	this.currentImageDiv.style['z-index'] = 50;
	
	this.currentImageDiv.setAttribute('class', 'navigatorCurrentDiv');

	_updateCurrentImageDivText(this.currentImageDiv, 0, numItems);

	_setItemOverlayVisible(this.currentImageDiv, alwaysVisible);

	container.append(this.currentImageDiv);
	
	this.lastNavigatorDiv = _createNavigatorDiv(
			container,
			'navigatorLastDiv',
			'navigatorLastArrowDiv',
			function (navigatorWidth) { return 0; },
			containerHeight,
			getWidth, getHeight);

	this.nextNavigatorDiv = _createNavigatorDiv(
			container,
			'navigatorNextDiv',
			'navigatorNextArrowDiv',
			function (navigatorWidth) { return containerWidth - navigatorWidth; },
			containerHeight,
			getWidth, getHeight);
	
	var t = this;
	
	var updateNavigators = function(navigatorState) {
		_setItemOverlayVisible(t.lastNavigatorDiv, navigatorState.isLastEnabled);
		_setItemOverlayVisible(t.nextNavigatorDiv, navigatorState.isNextEnabled);
	};
	
	var navigator = new Navigator(
			0,
			numItems,
			this.lastNavigatorDiv,
			this.nextNavigatorDiv,
			
			function (toShow, callback) {

				startUpdate(toShow, function() {
					
					var navigatorState = callback();

					updateNavigators(navigatorState);

					_updateCurrentImageDivText(t.currentImageDiv, navigatorState.index, navigatorState.count);
					
					return navigatorState;
				})
				
			}
	);
	
	if (alwaysVisible) {
		updateNavigators(navigator.getNavigatorState());
	}
	else {
		container.onmouseover = function() {
			_setItemOverlayVisible(t.currentImageDiv, true);
			
			updateNavigators(navigator.getNavigatorState());
		};
		
		container.onmouseout = function() {
			
			t._setVisible(false);
	
			/*
			lastNavigatorDiv.visibility = 'hidden';
			nextNavigatorDiv.visibility = 'hidden';
			*/
		};
	}
	
	this._setVisible = function(visible) {
		_setItemOverlayVisible(this.currentImageDiv, false);
		_setItemOverlayVisible(this.lastNavigatorDiv, false);
		_setItemOverlayVisible(this.nextNavigatorDiv, false);
	}
	
	this.reset = function(numItems) {
		this._setVisible(numItems === 0);
	}

	function _setItemOverlayVisible(div, visible) {
		div.style.display = visible ? 'inline-block' : 'none';
	}
	
	function _updateCurrentImageDivText(div, cur, count) {
		div.innerHTML = 'Showing image ' + (cur + 1) + ' out of ' + count;
	}
	

	function _createNavigatorDiv(
			div,
			navigatorDivCSSClass,
			arrowDivCSSClass,
			getLeft,
			containerHeight,
			getWidth,
			getHeight) {
		
		var navigatorWidth = getWidth();
		var navigatorHeight = getHeight();
		
		var navigatorDiv = document.createElement('div');
		navigatorDiv.style['z-index'] = 50;
		navigatorDiv.style.position = 'absolute';
		navigatorDiv.style.width = navigatorWidth;
		navigatorDiv.style.height = navigatorHeight;
		navigatorDiv.style.left = getLeft(navigatorWidth);
		navigatorDiv.style.top = (containerHeight - navigatorHeight) / 2;
		navigatorDiv.style.display = 'none';
		
		var arrowDiv = document.createElement('div');
		
		arrowDiv.setAttribute('class', arrowDivCSSClass);
		
		navigatorDiv.append(arrowDiv);
		
		navigatorDiv.setAttribute('class', navigatorDivCSSClass);
		
		div.append(navigatorDiv);
	
		return navigatorDiv;
	}

	function _removeElementsWithClass(element, className) {

		var found = element.getElementsByClassName(className);

		if (typeof found !== 'undefined' && found.length > 0) {
			
			for (var i = 0; i < found.length; ++ i) {
				found[i].remove();
			}
		}
	}
}