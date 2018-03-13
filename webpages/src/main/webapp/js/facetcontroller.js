/**
 * A view that implements your typical left-hand side faceted view
 * with ranges and checkboxes and counts.
 *  
 * 
 * Supports hierarchial facets, eg. county under state
 *
 *  [checkbox] Sports equipment
 *    [checkbox] Bikes (125)
 *    [checkbox] Skis  (50)
 *
 * Whenever selecting or deselecting a checkbox, will collect all selections
 * and issue a new search, then update the facet lists and seletions based
 * on facets within the result
 * 
 */
/**
 * Constructor
 * Params
 * 
 *  divId - ID of div to render in, just add as an empty div to your html. 
 *  performSearch - function that takes criteria as parameters, asynchronously returns
 *  list of facets by type
 *  {
 *  	types = [
 *  	{
 *  		type : 'skis',
 *  		attributes : [
 *   			{ 
 *   				'name' : 'make',
 *   				'values' : [
 *   					{ 'value' : 'Volkl', 	'matchCount' : 120 },
 *   					{ 'value' : 'Blizzard', 'matchCount' : 53  } 
 *   				]
 *   			},
 *  			{
 *  				'name' : 'length',
 *  				ranges : [
 *  					{ 'lower' : 150.0, 'upper' : 160.0, 'matchCount' : 30 },
 *  					{ 'lower' : 150.0, 'upper' : 160.0, 'matchCount' : 30 },
 *  				]
 *  		]
 *     },
 *     {
 *     		type : 'bikes',
 *     		...
 *     }
 *     ];
 *  }
 *  
 *  
 */

function FacetController(facetModel, facetView) {

	this.view = facetView; // MVC view interface
	this.model = facetModel; // MVC model interface
 	
	// The collapsable divs that contain information for each active facet
	// These are nested within type selection?
	// Does it make sense to search for multiple types? Perhaps
	this.facetViews = [];
	
	// Each facet view have nested facetviews and/or list of checkbox items
	this.selectItems = [];
	
	this.viewInitialized = false;

	this.refresh = function() {
		
		if (this.viewInitialized) {
			this._updateUI();
		}
		else {
			this.view.initFromModel(this.model);

			this.viewInitialized = true;
		}
	}
	
	this._updateUI = function() {
		// Topmost facetview is the list of matching types
		
		// Must merge the result with facet views that are already there
		// Remove views that are not there anymore, add new ones
		// Keep expanded-state for views that are expanded
		// We do the diffing in the controller, view maps a path to a div
		
		
		// TODO what about common attributes like location? Shared for multiple types
		// TODO should select probably at same level as type but could possibly override in subtype, might be confusing
		// TODO probably best to share or make it selectable through annotation.
		// TODO location can be shared but length/width/height does not make sense since is very different ranges depending on type of item
	};
	
	this.collectCriteriaAndTypesFromSelections = function() {

		var types = {};
		var criteria = [];
		
		// Go through each part of current view to see what is selected
		var numTypes = this.model.getTypeCount();
		
		for (var typeIdx = 0; typeIdx < numTypes; ++ typeIdx) {
			// For each type, ask checkbox status on each attribute
			var attributes = model.getAttributes([typeIdx]);
			
			// TODO recurse into subattributes
			
			for (var attrIdx = 0; attrIdx < attributes.length; ++ attrIdx) {
				
				var attribute = attributes[attrIdx];
				
				// Number of possible selections
				var selectionCount = attribute.getSelectionCount();
				
				// Number of selected items
				var selectedCount = 0;
				for (var selectionIdx = 0; selectionIdx < selectionCount; ++ selectionIdx) {
					if (this.view.isAttributeSelected(typeIdx, attrIdx, selectionIdx)) {
						++ selectedCount;
					}
				}

				if (selectedCount > 0) {
					criterium = this._makeCriterium(attribute, type, selectionCount);
					
					criteria.push(criterium);
					
					types[type] = '';
				}
			}
		}

		return { 'types' : Object.keys(types), criteria };
	};

	this._makeCriterium = function(attribute, type, selectionCount) {

		// There is a selection, add criterium
		var criterium = {
				'type' : type,
				'attribute' : attribute.getName()
		};

		var values = [];

		var numAdded = 0;
		for (var selectionIdx = 0; selectionIdx < selectionCount; ++ selectionIdx) {
			var selected = this.view.isAttributeSelected(typeIdx, attrIdx, selectionIdx);

			if (attribute.isValueAttribute()) {
				var value = attribute.getValue(selectionIdx);

				values.push(value);
			}
			else if (attribute.isRangeAttribute()) {
				var range = attribute.getRange(selectionIdx);

				values.push( { 'lower' :  range.getLower(), 'includeLower' : true, 'upper' : range.getUpper(), 'includeUpper' : false } );
			}
			else {
				throw "Neither value nor range attribute: " + attribute.getName();
			}
		}
		
		values += ' ]';
		
		criterium.values = values;
		
		return criterium;
	}
}