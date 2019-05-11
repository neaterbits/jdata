/**
 * 
 */

function NavigatorOverlay(
		numItems,
		container,
		containerWidth, containerHeight,
		getWidth, getHeight,
		startUpdate) {
	
	var currentImageDiv = document.createElement('div')
	
	currentImageDiv.style.position = 'absolute';
	currentImageDiv.style.left = 0;
	currentImageDiv.style.top = 0;
	currentImageDiv.style.width = containerWidth;
	currentImageDiv.style['z-index'] = 50;
	
	currentImageDiv.setAttribute('class', 'navigatorCurrentDiv');

	_updateCurrentImageDivText(currentImageDiv, 0, numItems);

	_setItemOverlayVisible(currentImageDiv, false);

	container.append(currentImageDiv);
	
	var lastNavigatorDiv = _createNavigatorDiv(
			container,
			'navigatorLastDiv',
			'navigatorLastArrowDiv',
			function (navigatorWidth) { return 0; },
			containerHeight,
			getWidth, getHeight);

	var nextNavigatorDiv = _createNavigatorDiv(
			container,
			'navigatorNextDiv',
			'navigatorNextArrowDiv',
			function (navigatorWidth) { return containerWidth - navigatorWidth; },
			containerHeight,
			getWidth, getHeight);
	
	var updateNavigators = function(navigatorState) {
		_setItemOverlayVisible(lastNavigatorDiv, navigatorState.isLastEnabled);
		_setItemOverlayVisible(nextNavigatorDiv, navigatorState.isNextEnabled);
	};
	
	var t = this;

	var navigator = new Navigator(
			numItems,
			lastNavigatorDiv,
			nextNavigatorDiv,
			
			function (toShow, callback) {

				startUpdate(toShow, function() {
					
					var navigatorState = callback();

					updateNavigators(navigatorState);

					_updateCurrentImageDivText(currentImageDiv, navigatorState.index, navigatorState.count);
					
					return navigatorState;
				})
				
			}
	);
	
	container.onmouseover = function() {
		_setItemOverlayVisible(currentImageDiv, true);
		
		updateNavigators(navigator.getNavigatorState());
	};
	
	container.onmouseout = function() {
		
		_setItemOverlayVisible(currentImageDiv, false);
		_setItemOverlayVisible(lastNavigatorDiv, false);
		_setItemOverlayVisible(nextNavigatorDiv, false);

		/*
		lastNavigatorDiv.visibility = 'hidden';
		nextNavigatorDiv.visibility = 'hidden';
		*/
	};

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
}