<html style='height: 100%'>
<head>

<link rel="stylesheet" type="text/css" href="css/facets.css">
<link rel="stylesheet" type="text/css" href="css/searchview.css">

<script src="js/facetpath.js" type="text/javascript"></script>
<script src="js/facetviewelements.js" type="text/javascript"></script>
<script src="js/facetview.js" type="text/javascript"></script>
<script src="js/facetmodel.js" type="text/javascript"></script>
<script src="js/facetcontroller.js" type="text/javascript"></script>


<script src="js/gallery_base.js" type="text/javascript"></script>
<script src="js/gallery_sizes.js" type="text/javascript"></script>
<script src="js/gallery_caches.js" type="text/javascript"></script>
<script src="js/gallery_cache_items.js" type="text/javascript"></script>
<script src="js/gallery_cache_all_complete.js" type="text/javascript"></script>
<script src="js/gallery_cache_all_provisional_some_complete.js" type="text/javascript"></script>
<script src="js/gallery_mode_base.js" type="text/javascript"></script>
<script src="js/gallery_mode_width_specific.js" type="text/javascript"></script>
<script src="js/gallery_mode_width_hint.js" type="text/javascript"></script>
<script src="js/gallery_mode_height_specific.js" type="text/javascript"></script>
<script src="js/gallery_mode_height_hint.js" type="text/javascript"></script>
<script src="js/gallery.js" type="text/javascript"></script>

<script src="js/searchview.js" type="text/javascript"></script>
</head>
<body style='height: 100%; margin: 0;'>
<div style='width: 100%; height: 100%; margin: 0; padding: 0.5em; box-sizing: border-box;'>
	
	<div id="fulltextAndFacets" style='width: 30%; height: 100%;'>
		<div id="fulltextDiv">
			 <div id="fulltextRow" class="infoRow">
				<input id="fulltextInput" type="text" placeholder="Enter any search word here"/>
				<input id="fulltextButton" type="button" value="Search"/>
				<span id="viewLogin"><span>Personal</span><span>></span></span>
			</div>
			
			 <div id="resultsRow" class="infoRow">
			 	<div id="numberOfItemsDiv">
				 	<span id="numberOfItemsLabel"># found:</span>
				 	<span id="numberOfItemsCount">12345</span>
				</div>

				<select id="sortListBox">
					<option>Price - low to high</option>
					<option>Price - high to low</option>
				</select>
			 	
			</div>
		</div>
		<div id='facets' style='display: block; width: 100%;'></div>
	</div>
	
	<!-- TODO not set wrapper? Create wrapper in gallery so not setting style on gallery -->
	<div id = "gallery_frame" style='display: inline-block; width: 65%; height: 100%;'>
		<div id='gallery' style='height: 100%'></div>
	</div>
</div>
</body>
<script type="text/javascript">
	window.onload = function() {
		
		var useTestData = false;
			
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
		var url = "http://localhost:8080/search";

		if (testdata) {
			url += "?testdata=" + testdata;
		}

		return url;
	}

	function getThumbsUrl(testdata) {
		var url = "http://localhost:8080/search/thumbnails";

		if (testdata) {
			url += "?testdata=" + testdata;
		}

		return url;
	}
</script>
</html>
