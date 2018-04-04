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
			this.view.refreshFromNewModel(this.model);
		}
		else {
			this.view.initFromModel(this.model);

			this.viewInitialized = true;
		}
	}
}