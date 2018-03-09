<html>
<head>
<script src="js/facetpath.js" type="text/javascript"></script>
<script src="js/facetviewelements.js" type="text/javascript"></script>
<script src="js/facetview.js" type="text/javascript"></script>
<script src="js/facetmodel.js" type="text/javascript"></script>
<script src="js/facetcontroller.js" type="text/javascript"></script>
</head>
<body>
<h2>Hello World!</h2>
<div id='facets'></div>
</body>
<script type="text/javascript">
	window.onload = function() {
		
		var model = new FacetModel("http://localhost:8080/search?test=true", true);
		
		// creates HTML elements
		var viewElements = new FacetViewElements();
		
		// View related logic
		var view = new FacetView('facets', viewElements);
		
		var controller = new FacetController(model, view);
		view.init(controller);

		controller.refresh();
	}
</script>
</html>
