<html>
<head>
<script src="js/gallery.js" type="text/javascript"></script>
</head>
<body>
<h2>Hello World!</h2>
<div id='gallery'></div>
</body>
<script type="text/javascript">
	window.onload = function() {
		var gallery = new Gallery('gallery', 20, 20,
		function(index) {
			var div = document.createElement('div');
			
			// Add index as a text to the element
			var textElement = document.createElement('span');

			textElement.innerHTML = "" + index;
			
			div.append(textElement);
			
			textElement.setAttribute('style', 'text-align : center;');
			
			return div;
		},
		function (index) { 
			
		},
		function (index, provisional, image) {
			return provisional;
		});
		
		gallery.refresh(function (initial, eachItem, metaDataComplete) {
			
			var items = [
				[ 'Item1', 240, 120 ],
				[ 'Item2', 230, 240 ],
				[ 'Item3', 130, 215 ],
				[ 'Item4', 120, 165 ],
				[ 'Item5', 240, 120 ]
			];
			
			items = [];
			
			// Add a lot of items just to test srolling when using many items
			for (var i = 0; i < 100000; ++ i) {
				items.push(['Item' + i, 240, 120]);
			}
			
			// Tell gallery about number of items before adding item metadata
			initial(items.length);
			
			// Add all metadata, ie. title and dimensions
			for (var i = 0; i < items.length; ++ i) {
				var item = items[i];
				
				eachItem(item[0], item[1], item[2]);
			}

			// Tell gallery that we have added all metadata and that it can perform the refresh
			metaDataComplete();
		});
	}
</script>
</html>
