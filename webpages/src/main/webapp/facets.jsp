<html>
<head>
<link rel="stylesheet" type="text/css" href="css/facets.css">
<script src="js/facetpath.js" type="text/javascript"></script>
<script src="js/facetviewelements.js" type="text/javascript"></script>
<script src="js/facetview.js" type="text/javascript"></script>
<script src="js/facetmodel.js" type="text/javascript"></script>
<script src="js/facetcontroller.js" type="text/javascript"></script>
<script src="js/searchview.js" type="text/javascript"></script>
</head>
<body>
<h2>Test</h2>
<input id="use_test_data" type="checkbox" checked>Use test data<br/>
<div id='facets'></div>
</body>
<script type="text/javascript">
	window.onload = function() {
		
		var useTestData = false;
		
		document.getElementById("use_test_data").checked = useTestData;
		
		
		var facetModel = new FacetModel();

		// creates HTML elements
		var viewElements = new FacetViewElements();
		
		// View related logic
		var view = new FacetView('facets', viewElements);
		
		var controller = new FacetController(facetModel, view);
		view.init(controller);
		
		var searchView = new SearchView(
					getServiceUrl(useTestData),
					facetModel,
					controller);
		
		searchView.refresh(true);
		
		document.getElementById("use_test_data").onchange = function(e) {

			var checked = e.target.checked;
			
			searchView.setServiceUrl(getServiceUrl(checked));
			
			searchView.refresh(true);
			
			return false;
		}
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
