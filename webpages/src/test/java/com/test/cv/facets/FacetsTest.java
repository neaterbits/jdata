package com.test.cv.facets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import com.test.cv.jsutils.ConstructRequest;
import com.test.cv.jsutils.JSInvocable;
import com.test.cv.rest.SearchFacetedAttributeIntegerRangeResult;
import com.test.cv.rest.SearchFacetedTypeResult;
import com.test.cv.rest.SearchFacetsResult;
import com.test.cv.rest.SearchRangeFacetedAttributeResult;
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

		assertThat(productionYearValues.getSubElements().get(0).getValue()).isEqualTo(2005);
		assertThat(productionYearValues.getSubElements().get(0).getMatchCount()).isEqualTo(2);
		assertThat(productionYearValues.getSubElements().get(0).hasSubAttributes()).isFalse();
		assertThat(productionYearValues.getSubElements().get(0).isExpanded()).isFalse();
		assertThat(productionYearValues.getSubElements().get(0).isChecked()).isTrue();

		assertThat(productionYearValues.getSubElements().get(1).getValue()).isEqualTo(2007);
		assertThat(productionYearValues.getSubElements().get(1).getMatchCount()).isEqualTo(1);
		assertThat(productionYearValues.getSubElements().get(1).hasSubAttributes()).isFalse();
		assertThat(productionYearValues.getSubElements().get(1).isExpanded()).isFalse();
		assertThat(productionYearValues.getSubElements().get(1).isChecked()).isTrue();

		assertThat(productionYearValues.getSubElements().get(2).getValue()).isEqualTo("Other");
		assertThat(productionYearValues.getSubElements().get(2).getMatchCount()).isEqualTo(1);
		assertThat(productionYearValues.getSubElements().get(2).hasSubAttributes()).isFalse();
		assertThat(productionYearValues.getSubElements().get(2).isExpanded()).isFalse();
		assertThat(productionYearValues.getSubElements().get(2).isChecked()).isTrue();

		final ViewAttributeListElement carTypeElement = attributeList.getSubElements().get(1);
		assertThat(carTypeElement.getText()).isEqualTo("Type");
		
		final ViewAttributeValueList carTypeValues = (ViewAttributeValueList)carTypeElement.getSubElements().get(0);

		assertThat(carTypeValues.getSubElements().size()).isEqualTo(3);

		assertThat(carTypeValues.getSubElements().get(0).getValue()).isEqualTo("Sedan");
		assertThat(carTypeValues.getSubElements().get(0).getMatchCount()).isEqualTo(1);
		assertThat(carTypeValues.getSubElements().get(0).hasSubAttributes()).isFalse();
		assertThat(carTypeValues.getSubElements().get(0).isExpanded()).isFalse();
		assertThat(carTypeValues.getSubElements().get(0).isChecked()).isTrue();

		assertThat(carTypeValues.getSubElements().get(1).getValue()).isEqualTo("Compact");
		assertThat(carTypeValues.getSubElements().get(1).getMatchCount()).isEqualTo(1);
		assertThat(carTypeValues.getSubElements().get(1).hasSubAttributes()).isFalse();
		assertThat(carTypeValues.getSubElements().get(1).isExpanded()).isFalse();
		assertThat(carTypeValues.getSubElements().get(1).isChecked()).isTrue();

		assertThat(carTypeValues.getSubElements().get(2).getValue()).isEqualTo("Convertible");
		assertThat(carTypeValues.getSubElements().get(2).getMatchCount()).isEqualTo(1);
		assertThat(carTypeValues.getSubElements().get(2).hasSubAttributes()).isFalse();
		assertThat(carTypeValues.getSubElements().get(2).isExpanded()).isFalse();
		assertThat(carTypeValues.getSubElements().get(2).isChecked()).isTrue();

		
		final ViewAttributeListElement odometerElement = attributeList.getSubElements().get(2);
		
		assertThat(odometerElement.getText()).isEqualTo("Odometer");
		
		assertThat(odometerElement.getSubElements().size()).isEqualTo(1);
		
		final ViewAttributeRangeList rangeList
				= (ViewAttributeRangeList)odometerElement.getSubElements().get(0);
		
		assertThat(rangeList.getSubElements().size()).isEqualTo(5);

		assertThat(rangeList.getSubElements().get(0).getValue()).isEqualTo("  - 100000");
		assertThat(rangeList.getSubElements().get(0).getMatchCount()).isEqualTo(0);
		assertThat(rangeList.getSubElements().get(0).hasSubAttributes()).isFalse();
		assertThat(rangeList.getSubElements().get(0).isExpanded()).isFalse();
		assertThat(rangeList.getSubElements().get(0).isChecked()).isTrue();

		assertThat(rangeList.getSubElements().get(1).getValue()).isEqualTo("100000 - 200000");
		assertThat(rangeList.getSubElements().get(1).getMatchCount()).isEqualTo(1);
		assertThat(rangeList.getSubElements().get(1).hasSubAttributes()).isFalse();
		assertThat(rangeList.getSubElements().get(1).isExpanded()).isFalse();
		assertThat(rangeList.getSubElements().get(1).isChecked()).isTrue();

		assertThat(rangeList.getSubElements().get(2).getValue()).isEqualTo("200000 - 300000");
		assertThat(rangeList.getSubElements().get(2).getMatchCount()).isEqualTo(0);
		assertThat(rangeList.getSubElements().get(2).hasSubAttributes()).isFalse();
		assertThat(rangeList.getSubElements().get(2).isExpanded()).isFalse();
		assertThat(rangeList.getSubElements().get(2).isChecked()).isTrue();

		assertThat(rangeList.getSubElements().get(3).getValue()).isEqualTo("300000 - ");
		assertThat(rangeList.getSubElements().get(3).getMatchCount()).isEqualTo(0);
		assertThat(rangeList.getSubElements().get(3).hasSubAttributes()).isFalse();
		assertThat(rangeList.getSubElements().get(3).isExpanded()).isFalse();
		assertThat(rangeList.getSubElements().get(3).isChecked()).isTrue();

		assertThat(rangeList.getSubElements().get(4).getValue()).isEqualTo("Unknown");
		assertThat(rangeList.getSubElements().get(4).getMatchCount()).isEqualTo(2);
		assertThat(rangeList.getSubElements().get(4).hasSubAttributes()).isFalse();
		assertThat(rangeList.getSubElements().get(4).isExpanded()).isFalse();
		assertThat(rangeList.getSubElements().get(4).isChecked()).isTrue();

		
		// Update model and refresh again for view to be updated
		final SearchFacetsResult updatedFacets = new SearchFacetsResult();
		final SearchFacetedTypeResult updatedCars = new SearchFacetedTypeResult(CAR_TYPE_NAME, CAR_DISPLAY_NAME);
		
		updatedCars.setAttributes(Arrays.asList(productionYearAttr, odometerAttr));
		
		updatedFacets.setTypes(Arrays.asList(updatedCars));

		controller.getModel().updateFacets(updatedFacets);
		controller.refresh();
	}
}
