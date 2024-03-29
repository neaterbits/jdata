package com.test.salesportal.facets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import com.test.salesportal.jsutils.ConstructRequest;
import com.test.salesportal.jsutils.JSInvocable;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeIntegerRangeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedTypeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;
import com.test.salesportal.rest.search.model.facetresult.SearchRangeFacetedAttributeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacet;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacetedAttributeResult;

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
	
	private static final String CAR_TYPE_NAME = "car";
	private static final String CAR_DISPLAY_NAME = "Cars";
	
	public void testFacets() throws IOException {
		
		final TestFacetViewElements viewElements = new TestFacetViewElements();
		
		final FacetController controller = prepareRuntime(viewElements);
		assertThat(controller).isNotNull();

		// Must set model
		
		final SearchFacetsResult facetsResult = new SearchFacetsResult();
		
		final SearchFacetedTypeResult carTypeResult = new SearchFacetedTypeResult();
		
		carTypeResult.setType(CAR_TYPE_NAME);
		carTypeResult.setDisplayName(CAR_DISPLAY_NAME);
		
		final SearchSingleValueFacetedAttributeResult productionYearAttr
			= new SearchSingleValueFacetedAttributeResult("productionYear", "Production year", 1);
		
		productionYearAttr.setValues(Arrays.asList(
				new SearchSingleValueFacet(2005, 2),
				new SearchSingleValueFacet(2007, 1)
		));
		
		final SearchSingleValueFacetedAttributeResult carTypeAttr
			= new SearchSingleValueFacetedAttributeResult("carType", "Type", 0);
		
		carTypeAttr.setValues(Arrays.asList(
				new SearchSingleValueFacet("Sedan", 1),
				new SearchSingleValueFacet("Compact", 1),
				new SearchSingleValueFacet("Convertible", 1)
		));

		final SearchRangeFacetedAttributeResult odometerAttr = new SearchRangeFacetedAttributeResult("odometer", "Odometer", 2);
	
		odometerAttr.setRanges(Arrays.asList(
				new SearchFacetedAttributeIntegerRangeResult(null, 100000, 0),
				new SearchFacetedAttributeIntegerRangeResult(100000, 200000, 1),
				new SearchFacetedAttributeIntegerRangeResult(200000, 300000, 0),
				new SearchFacetedAttributeIntegerRangeResult(300000, null, 0)
		));

		carTypeResult.setAttributes(Arrays.asList(productionYearAttr, carTypeAttr, odometerAttr));
		
		facetsResult.setTypes(Arrays.asList(
				carTypeResult
		));
		
		controller.getModel().updateFacets(facetsResult);
		
		// Initial load of data
		controller.refresh();
		
		// Now look into viewElements and check that we have loaded some data

		// Check that has created the elements in question
		checkAddedSubElements(viewElements);
		
		// Refresh with exactly same model and check that have the same items
		// This means that the refresh comparison algorithm works correctly
		// either by only removing items if not present in search result anymore,
		// and not adding elements twice (ie. it should diff to existing)
		controller.getModel().updateFacets(facetsResult);
		controller.refresh();
		checkAddedSubElements(viewElements);
		
		// Update model and refresh again for view to be updated
		final SearchFacetsResult updatedFacets = new SearchFacetsResult();
		final SearchFacetedTypeResult updatedCars = new SearchFacetedTypeResult(CAR_TYPE_NAME, CAR_DISPLAY_NAME);

		updatedCars.setAttributes(Arrays.asList(productionYearAttr, odometerAttr));
		updatedFacets.setTypes(Arrays.asList(updatedCars));

		controller.getModel().updateFacets(updatedFacets);
		controller.refresh();
		
	}
	
	
	private void checkAddedSubElements(TestFacetViewElements viewElements) {
		final ViewTypeList rootTypeList = viewElements.getRootTypeList();
		
		assertThat(rootTypeList).isNotNull();
		assertThat(rootTypeList.getSubElements().size()).isEqualTo(1);
		
		final ViewTypeContainer cars = rootTypeList.getSubElements().get(0);
		assertThat(cars.getText()).isEqualTo(CAR_DISPLAY_NAME);
		
		// Ought to have two have one attribute sublist (could be both subtypes and attreibute list) 
		assertThat(cars.getSubElements().size()).isEqualTo(1);
		
		final ViewAttributeList attributeList = (ViewAttributeList)cars.getSubElements().get(0);
		
		assertThat(attributeList.getSubElements().size()).isEqualTo(3);

		final ViewAttributeListElement productionYearElement = attributeList.getSubElements().get(0);
		
		assertThat(productionYearElement.getText()).isEqualTo("Production year");
		assertThat(productionYearElement.getSubElements().size()).isEqualTo(1);
		
		final ViewAttributeValueList productionYearValues = (ViewAttributeValueList)productionYearElement.getSubElements().get(0);

		assertThat(productionYearValues.getSubElements().size()).isEqualTo(3);

		checkSubElement(productionYearValues, 0, 2005, 2);
		checkSubElement(productionYearValues, 1, 2007, 1);
		checkSubElement(productionYearValues, 2, "Other", 1);

		final ViewAttributeListElement carTypeElement = attributeList.getSubElements().get(1);
		assertThat(carTypeElement.getText()).isEqualTo("Type");
		
		final ViewAttributeValueList carTypeValues = (ViewAttributeValueList)carTypeElement.getSubElements().get(0);

		assertThat(carTypeValues.getSubElements().size()).isEqualTo(3);

		checkSubElement(carTypeValues, 0, "Sedan", 1);
		checkSubElement(carTypeValues, 1, "Compact", 1);
		checkSubElement(carTypeValues, 2, "Convertible", 1);

		
		final ViewAttributeListElement odometerElement = attributeList.getSubElements().get(2);
		
		assertThat(odometerElement.getText()).isEqualTo("Odometer");
		
		assertThat(odometerElement.getSubElements().size()).isEqualTo(1);
		
		final ViewAttributeRangeList rangeList
				= (ViewAttributeRangeList)odometerElement.getSubElements().get(0);
		
		assertThat(rangeList.getSubElements().size()).isEqualTo(5);

		checkSubElement(rangeList, 0, "  - 100000", 0);
		checkSubElement(rangeList, 1, "100000 - 200000", 1);
		checkSubElement(rangeList, 2, "200000 - 300000", 0);
		checkSubElement(rangeList, 3, "300000 - ", 0);
		checkSubElement(rangeList, 4, "Unknown", 2);
	}
	
	private void checkSubElement(ViewList<ViewAttributeValueElement> list, int index, Object expectedValue, int matchCount) {
		assertThat(list.getSubElements().get(index).getValue()).isEqualTo(expectedValue);
		assertThat(list.getSubElements().get(index).getMatchCount()).isEqualTo(matchCount);
		assertThat(list.getSubElements().get(index).hasSubAttributes()).isFalse();
		assertThat(list.getSubElements().get(index).isExpanded()).isFalse();
		assertThat(list.getSubElements().get(index).isChecked()).isTrue();
	}
	
}
