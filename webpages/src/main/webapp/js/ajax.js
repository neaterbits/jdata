/**
 * Helper for Ajax calls
 */

function Ajax() {

	this.getAjax = function(url, responseType, onsuccess) {
		this.sendAjax(url, 'GET', responseType, null, null, onsuccess);
	}

	this.sendAjax = function(url, method, responseType, requestContentType, requestContent, onsuccess) {
		var request = new XMLHttpRequest();

		if (responseType != null) {
			request.responseType = responseType;
		}
		
		var getResponseData;
		
		switch (responseType) {
		case 'json':
		case 'arraybuffer':
			getResponseData = function(t) {
				return t.response;
			}
			break;
			
		case 'text':
			getResponseData = function(t) {
				return t.responseText;
			}
			break;
			
		default:
			throw "Unknown responseType " + responseType;
		}

		request.onreadystatechange = function() {

			if (this.readyState == 4 && this.status == 200) {
				
				var data = getResponseData(this);
				
				onsuccess(data);
			}
		};

		request.open(method, url, true);

		request.send(requestContent);
	};

}