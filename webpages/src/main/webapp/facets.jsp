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
<div id='facets'></div>
</body>
<script type="text/javascript">
	window.onload = function() {
		
		var facetModel = new FacetModel();

		// creates HTML elements
		var viewElements = new FacetViewElements();
		
		// View related logic
		var view = new FacetView('facets', viewElements);
		
		var controller = new FacetController(facetModel, view);
		view.init(controller);

		var searchView = new SearchView(
					"http://localhost:8080/search?test=true",
					facetModel,
					controller);
		
		searchView.refresh();
	}
</script>
</html>
