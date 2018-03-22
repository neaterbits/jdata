<html style='height: 100%'>
<head>
<link rel="stylesheet" type="text/css" href="css/facets.css">
<script src="js/facetpath.js" type="text/javascript"></script>
<script src="js/facetviewelements.js" type="text/javascript"></script>
<script src="js/facetview.js" type="text/javascript"></script>
<script src="js/facetmodel.js" type="text/javascript"></script>
<script src="js/facetcontroller.js" type="text/javascript"></script>
<script src="js/gallery.js" type="text/javascript"></script>
<script src="js/searchview.js" type="text/javascript"></script>
</head>
<body style='height: 100%'>
<h2>Test</h2>
<input id="use_test_data" type="checkbox" checked>Use test data<br/>
<div style='width: 100%; height: 90%; margin: 0; padding: 0; border-box;'>
	<div id='facets' style='display: inline-block; width: 30%; height: 100%; margin: 0; padding: 0; box-sizing: border-box; overflow: scroll'></div>
	
	<!-- TODO not set wrapper? Create wrapper in gallery so not setting style on gallery -->
	<div style='display: inline-block; width: 65%; height: 100%; margin: 0; padding: 0; box-sizing: border-box;'>
		<div id='gallery'></div>
	</div>
</div>
</body>
<script type="text/javascript">
	window.onload = function() {
		
		var useTestData = false;
		
		document.getElementById("use_test_data").checked = useTestData;
		
		
		var facetModel = new FacetModel();

		// creates HTML elements
		var viewElements = new FacetViewElements();
		
		// View related logic
		var facetView = new FacetView('facets', viewElements);
		
		var facetController = new FacetController(facetModel, facetView);
		facetView.init(facetController);
		
		var gallery = initGallery();
		
		var searchView = new SearchView(
					getServiceUrl(useTestData),
					facetModel,
					facetController,
					gallery);

		searchView.refresh(true);
		
		document.getElementById("use_test_data").onchange = function(e) {

			var checked = e.target.checked;
			
			searchView.setServiceUrl(getServiceUrl(checked));
			
			searchView.refresh(true);
			
			return false;
		}
	}
	
	function initGallery() {
		var gallery = new Gallery('gallery', 20, 20,
				// Create element
				function(index, title, thumbWidth, thumbHeight) {

					var div = document.createElement('div');

					var provisionalImage = document.createElement('div');

					provisionalImage.style.width = thumbWidth;
					provisionalImage.style.height = thumbHeight;

					div.append(provisionalImage);
					
					var textDiv = document.createElement('div');

					// Add index as a text to the element
					var textSpan = document.createElement('span');
					
					textSpan.innerHTML = title;

					textDiv.append(textSpan);

					div.append(textDiv);

					textDiv.setAttribute('style', 'text-align : center;');
					
					return div;
				},
				function (index) { 
					// Return images for index
				},
				function (index, provisional, image) {
					return provisional;
				});

		return gallery;
	}
	
	
	function getServiceUrl(testdata) {
		var url = "http://localhost:8080/search?test=true";

		if (testdata) {
			url += "&testdata=" + testdata;
		}

		return url;
	}
</script>
</html>
