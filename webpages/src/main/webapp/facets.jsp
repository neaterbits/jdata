<html style='height: 100%'>
<head>
<link rel="stylesheet" type="text/css" href="css/spinner.css">
<link rel="stylesheet" type="text/css" href="css/facets.css">
<link rel="stylesheet" type="text/css" href="css/searchview.css">
<link rel="stylesheet" type="text/css" href="css/adview.css">

<script src="js/utils.js" type="text/javascript"></script>
<script src="js/base64.js" type="text/javascript"></script>
<script src="js/navigator.js" type="text/javascript"></script>
<script src="js/navigator_overlay.js" type="text/javascript"></script>
<script src="js/element_fader.js" type="text/javascript"></script>
<script src="js/photoview_navigator.js" type="text/javascript"></script>
<script src="js/adview.js" type="text/javascript"></script>
<script src="js/facetpath.js" type="text/javascript"></script>
<script src="js/facetviewelements.js" type="text/javascript"></script>
<script src="js/facetview.js" type="text/javascript"></script>
<script src="js/facetmodel.js" type="text/javascript"></script>
<script src="js/facetcontroller.js" type="text/javascript"></script>

<script src="js/gallery_base.js" type="text/javascript"></script>
<script src="js/gallery_sizes.js" type="text/javascript"></script>
<script src="js/gallery_displaystate.js" type="text/javascript"></script>
<script src="js/gallery_caches.js" type="text/javascript"></script>
<script src="js/gallery_cache_items.js" type="text/javascript"></script>
<script src="js/gallery_cache_all_complete.js" type="text/javascript"></script>
<script src="js/gallery_cache_all_provisional_some_complete.js" type="text/javascript"></script>
<script src="js/gallery_mode_base.js" type="text/javascript"></script>
<script src="js/gallery_mode_width_specific.js" type="text/javascript"></script>
<script src="js/gallery_mode_width_hint.js" type="text/javascript"></script>
<script src="js/gallery_mode_height_specific.js" type="text/javascript"></script>
<script src="js/gallery_mode_height_hint.js" type="text/javascript"></script>
<script src="js/gallery_item_factory_base.js" type="text/javascript"></script>
<script src="js/gallery_item_factory_simple_static_size.js" type="text/javascript"></script>
<script src="js/gallery_item_factory_simple_dynamic_size.js" type="text/javascript"></script>
<script src="js/gallery_item_factory_rental_apartment.js" type="text/javascript"></script>
<script src="js/gallery.js" type="text/javascript"></script>

<script src="js/ajax.js" type="text/javascript"></script>
<script src="js/searchview.js" type="text/javascript"></script>
</head>
<body style='height: 100%; margin: 0; position: relative;'>
<div style='width: 100%; height: 100%; margin: 0; padding: 0.5em; box-sizing: border-box; '>
	
	<!-- For padding to the right of search panel -->
	<div id="leftSidePaddingContainer" style='width: 25%; height: 100%;'>
	
	<div id="leftSideSearchPanel" style='width: 100%; height: 100%;'>
		<div id="leftSideSearchPanelFlex">
			<div id="fulltextAndResultsDiv">
				<div id="fulltextDiv">
					 <div id="fulltextRow" class="infoRow">
					 	<div id="fullTextInputs">
							<input id="fulltextInput" type="text" placeholder="Enter any search word here"/>
							<input id="fulltextButton" type="button" value="Search"/>
						</div>
						
					</div>
					
					 <div id="resultsRow" class="infoRow">
					 	<div id="resultsAndSorting">
						 	<div id="numberOfItemsDiv">
							 	<span id="numberOfItemsLabel">Hits:</span>
							 	<span id="numberOfItemsCount"></span>
							</div>
			
							<select id="sortListBox">
							</select>
					 	</div>
					 	
					</div>
				</div>
			</div>
				

			<div id='facets'></div>
		</div>
	</div>
	</div>
	
	<!-- TODO not set wrapper? Create wrapper in gallery so not setting style on gallery -->
	<div id="gallery_wrapper" style='display: inline-block; width: 74.5%; height: 100%; margin : 0; padding: 0; vertical-align: top;'>
	 	<div id="hideSidebarDiv">
		 	<div id="hideSidebarFlex">
				<div id="hideSidebarBorder">
					<span id="hideSidebarArrow">&lt;&lt;</span>
					<span id="hideSidebarText">Hide</span>
				</div>
			</div>
		</div>
	<div id = "gallery_frame" style='display: inline-block; width: 100%; height: 100%;'>
		<div id='gallery' style='height: 100%'></div>
	</div>
	</div>
</div>

<!-- Single ad view, displayed atop of gallery -->
<div id="single_ad_view_wrapper">
	<div id="ad_view_navigation_div">
		<div id="ad_view_last_button" class="ad_view_navigation_button ad_view_header_button">&lt;</div>
		<div id="ad_view_next_button" class="ad_view_navigation_button ad_view_header_button">&gt;</div>
	</div>
	
	<div id="ad_view_close_button" class="ad_view_header_button">
		<div id="ad_view_close_button_inner">x</div>
	</div>
<div id="single_ad_view">
	<h1 id="ad_view_title"></h1>
	<div>
		<div id="ad_view_details_div">
		</div>
		
		<div id="ad_view_main_div">
			<div id="ad_view_photos_div">
				<div id="ad_view_photo_div">
				<!-- 
					<img id="ad_view_photo"/>
				 -->
					<div id="photo_spinner" class="sk-fading-circle" style="width=50px; height=50px; z-index: 100; display: none;">
					  <div class="sk-circle1 sk-circle"></div>
					  <div class="sk-circle2 sk-circle"></div>
					  <div class="sk-circle3 sk-circle"></div>
					  <div class="sk-circle4 sk-circle"></div>
					  <div class="sk-circle5 sk-circle"></div>
					  <div class="sk-circle6 sk-circle"></div>
					  <div class="sk-circle7 sk-circle"></div>
					  <div class="sk-circle8 sk-circle"></div>
					  <div class="sk-circle9 sk-circle"></div>
					  <div class="sk-circle10 sk-circle"></div>
					  <div class="sk-circle11 sk-circle"></div>
					  <div class="sk-circle12 sk-circle"></div>
					</div>
				</div>
			</div>
			<div id="ad_view_description_div"></div>
		</div>
		<div id="ad_view_map_and_contact_div">
			<div id="ad_view_map_div">
				<iframe id="ad_view_map_iframe"
					width="300"
					height="300"
					style="border:0">
				</iframe>
			</div>
		</div>
	</div>
</div>
</div>

<div id="mid_spinner" class="sk-fading-circle" style="z-index: 100; display: none;">
  <div class="sk-circle1 sk-circle"></div>
  <div class="sk-circle2 sk-circle"></div>
  <div class="sk-circle3 sk-circle"></div>
  <div class="sk-circle4 sk-circle"></div>
  <div class="sk-circle5 sk-circle"></div>
  <div class="sk-circle6 sk-circle"></div>
  <div class="sk-circle7 sk-circle"></div>
  <div class="sk-circle8 sk-circle"></div>
  <div class="sk-circle9 sk-circle"></div>
  <div class="sk-circle10 sk-circle"></div>
  <div class="sk-circle11 sk-circle"></div>
  <div class="sk-circle12 sk-circle"></div>
</div>
</body>
<script type="text/javascript">
	window.onload = function() {
		
		var useTestData = false;
		
		var ajax = new Ajax();
		
		var getPhotoCountUrl = function(itemId) {
			return getBaseUrl() + "/items/" + itemId + "/photoCount";
		}

		var getPhotoUrl = function(itemId, photoNo) {
			return getBaseUrl() + "/items/" + itemId + "/photos/" + photoNo;
		}

		var getThumbUrl = function(itemId, thumbNo) {
			return getBaseUrl() + "/items/" + itemId + "/thumbs/" + thumbNo;
		}

		var loadItemByItemId = function(itemId, onSuccess, onError) {
			ajax.getAjax(
				getBaseUrl() + "/items/" + itemId,
				'json',
				onSuccess
			);
		}
		
		var searchView = new SearchView(
					getSearchUrl(useTestData),
					getThumbsUrl(useTestData),
					ajax,
					'facets',
					'gallery',
					'fulltextInput',
					'fulltextButton',
					'numberOfItemsCount',
					'sortListBox',
					loadItemByItemId,
					function (loadItemDataByIndex) {
						return new AdView(
								document.getElementById('single_ad_view_wrapper'),
								loadItemDataByIndex,
								getPhotoUrl,
								function (showSpinner) {
									
									
									var display = showSpinner ? 'block' : 'none';
									
									document.getElementById('mid_spinner').style.display = display;
								});
					},
					function (getAdView) {
						return new SimpleStaticSizeGalleryItemFactory(ajax, getThumbUrl, getAdView);
					}
					// new SimpleDynamicSizeGalleryItemFactory()
					// new RentalApartmentGalleryItemFactory(ajax, getPhotoCountUrl, getPhotoUrl)
		);

		searchView.refresh(true);
	}
	
	function getBaseUrl() {
		return "http://localhost:8080";
	}
	
	function getItemsBaseUrl() {
		return getBaseUrl() + '/items'
	}
	
	function getSearchBaseUrl() {
		return getBaseUrl() + "/searchpaged";
	}
	
	function getSearchUrl(testdata) {
		var url = getSearchBaseUrl();

		if (testdata) {
			url += "?testdata=" + testdata;
		}

		return url;
	}
	
	function getThumbsUrl(testdata) {
		var url = getSearchUrl() + "/thumbnails";

		if (testdata) {
			url += "?testdata=" + testdata;
		}

		return url;
	}
</script>
</html>
