<html style='height: 100%'>
<head>
<link rel="stylesheet" type="text/css" href="css/facets.css">
<script src="js/facetpath.js" type="text/javascript"></script>
<script src="js/facetviewelements.js" type="text/javascript"></script>
<script src="js/facetview.js" type="text/javascript"></script>
<script src="js/facetmodel.js" type="text/javascript"></script>
<script src="js/facetcontroller.js" type="text/javascript"></script>


<script src="js/gallery_base.js" type="text/javascript"></script>
<script src="js/gallery_caches.js" type="text/javascript"></script>
<script src="js/gallery_cache_all_provisional_some_complete.js" type="text/javascript"></script>
<script src="js/gallery_mode_base.js" type="text/javascript"></script>
<script src="js/gallery_mode_width_specific.js" type="text/javascript"></script>
<script src="js/gallery_mode_width_hint.js" type="text/javascript"></script>
<script src="js/gallery_mode_height_specific.js" type="text/javascript"></script>
<script src="js/gallery_mode_height_hint.js" type="text/javascript"></script>
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
		<div id='gallery' style='height: 100%'></div>
	</div>
</div>
</body>
<script type="text/javascript">
	window.onload = function() {
		
		var useTestData = false;
		
		document.getElementById("use_test_data").checked = useTestData;
		
			
		var searchView = new SearchView(
					getSearchUrl(useTestData),
					getThumbsUrl(useTestData),
					'facets',
					'gallery');

		searchView.refresh(true);
		
		document.getElementById("use_test_data").onchange = function(e) {

			var checked = e.target.checked;
			
			searchView.setServiceUrl(getServiceUrl(checked));
			
			searchView.refresh(true);
			
			return false;
		}
	}
	
	
	function getSearchUrl(testdata) {
		var url = "http://localhost:8080/search?test=true";

		if (testdata) {
			url += "&testdata=" + testdata;
		}

		return url;
	}

	function getThumbsUrl(testdata) {
		var url = "http://localhost:8080/search/thumbnails?test=true";

		if (testdata) {
			url += "&testdata=" + testdata;
		}

		return url;
	}
</script>
</html>
