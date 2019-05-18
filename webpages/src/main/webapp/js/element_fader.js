/**
 * For fading effects between two elements, eg. img or div
 * 
 */

function ElementFader(outerDiv, tagName, createElement) {

	const DISPLAYED = 'data-displayed';
	
	this.outerDiv = outerDiv;
	this.createElement = createElement;
	this.tagName = tagName;
	
	var elements = _initElements(outerDiv, tagName, createElement);
	
	console.log('## initialized elements ' + JSON.stringify(elements));
	
	this.getFadeInElement = function() {
		return _getDisplayed(this.outerDiv, this.tagName).hidden;
	}

	this.getFadeOutElement = function() {
		return _getDisplayed(this.outerDiv, this.tagName).displayed;
	}

	this.crossFade = function() {

		console.log('');
		console.log('## start crossfade');
		
		console.log('## get displayed tagname ' + this.tagName);
		
		var displayState = _getDisplayed(this.outerDiv, this.tagName);

		/*
		var fadeOutListener = function() {
			
			console.log('## faded out ' + displayState.displayed.src
					+ ' ' + getComputedStyle(displayState.displayed).opacity);
			
			displayState.displayed.classList.remove('photo_view_fade_out');
			displayState.displayed.removeEventListener('animationend', fadeOutListener);
		}
		
		displayState.displayed.addEventListener('animationend', fadeOutListener);

		var fadeInListener = function() {
			
			console.log('## faded in ' + displayState.hidden.src + ' ' + 
					getComputedStyle(displayState.hidden).opacity);
			
			displayState.hidden.classList.remove('photo_view_fade_in');
			displayState.hidden.removeEventListener('animationend', fadeInListener);
		}
		
		displayState.hidden.addEventListener('animationend', fadeInListener);

		displayState.displayed.classList.add('photo_view_fade_out');
		displayState.hidden.classList.add('photo_view_fade_in');
		*/

		
		displayState.displayed.removeAttribute(DISPLAYED);
		displayState.hidden.setAttribute(DISPLAYED, true);
		
		displayState.displayed.style.opacity = 0;
		displayState.hidden.style.opacity = 1;
	}
	
	function _getElements(outerDiv, tagName) {
	
		var elements = outerDiv.getElementsByTagName(tagName);
		
		return elements;
	}

	
	function _initElements(outerDiv, tagName, createElement) {
		
		outerDiv.style.position = 'relative';
		
		var elements = _getElements(outerDiv, tagName);
		
		var result;
		
		var displayed;
		var hidden;
		
		if (elements.length == 0) {
			
			var element1 = createElement();
			
			element1.setAttribute(DISPLAYED, true);
			
			displayed = element1;
			
			var element2 = createElement();
			
			hidden = element2;
			
			outerDiv.append(element1);
			
			element1.style.position = 'absolute';
			element1.style.left = 0;
			
			outerDiv.append(element2);

			element2.style.position = 'absolute';
			element2.style.left = 0;
			
			element1.classList.add('photo_view_fade');
			element2.classList.add('photo_view_fade');

		}
		
		return elements;
	}
	
	function _getDisplayed(outerDiv, tagName) {
		
		var elements = _getElements(outerDiv, tagName);
		
		if (elements.length != 2) {
			throw "Expected elements " + elements.length;
		}

		if (elements[0].hasAttribute(DISPLAYED)) {
			displayed = elements[0];
			hidden = elements[1];
		}
		else if (elements[1].hasAttribute(DISPLAYED)) {
			displayed = elements[1];
			hidden = elements[0];
		}
		else {
			throw "Neither element is displayed";
		}
		
		return { 'displayed' : displayed, 'hidden' : hidden };
	}
}