package com.test.cv.facets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import com.test.cv.jsutils.ConstructRequest;
import com.test.cv.jsutils.JSInvocable;
import com.test.cv.rest.SearchFacetedTypeResult;
import com.test.cv.rest.SearchFacetsResult;
import com.test.cv.rest.SearchSingleValueFacet;
import com.test.cv.rest.SearchSingleValueFacetedAttributeResult;

public class FacetsTest extends BaseFacetsTest {

	private FacetController prepareRuntime(TestFacetViewElements viewElements) throws IOException {
		
		final ConstructRequest modelRequest = new ConstructRequest("FacetModel");
		final ConstructRequest viewRequest = new ConstructRequest("FacetView", "rootDiv", viewElements);
		final ConstructRequest controllerRequest = new ConstructRequest("FacetController", modelRequest, viewRequest);
		
		final JSInvocable invocable = super.prepareGalleryRuntime(new HashMap<>(), modelRequest, viewRequest, controllerRequest);

		final FacetModel model = new FacetModel(invocable, modelRequest.getInstance());
		final FacetView view = new FacetView(invocable, modelRequest.getInstance());

		return new FacetController(invocable, controllerRequest.getInstance(), model, view);
	}
	
	public void testFacets() throws IOException {
		
		final TestFacetViewElements viewElements = new TestFacetViewElements();
		
		final FacetController controller = prepareRuntime(viewElements);
		assertThat(controller).isNotNull();

		// Must set model
		
		final SearchFacetsResult facetsResult = new SearchFacetsResult();
		
		final SearchFacetedTypeResult carTypeResult = new SearchFacetedTypeResult();
		
		carTypeResult.setType("car");
		carTypeResult.setDisplayName("Cars");
		
		final SearchSingleValueFacetedAttributeResult productionYearAttr
			= new SearchSingleValueFacetedAttributeResult("productionYear", "Production year", 1);
		
		productionYearAttr.setValues(Arrays.asList(
				new SearchSingleValueFacet(2005, 2),
				new SearchSingleValueFacet(2007, 1)
		));
		
		carTypeResult.setAttributes(Arrays.asList(productionYearAttr));
		
		facetsResult.setTypes(Arrays.asList(
				carTypeResult
		));
		
		controller.getModel().updateFacets(facetsResult);
		
		// Initial load of data
		controller.refresh();
				
	}

}
