package com.test.salesportal.facets;

import java.io.IOException;
import java.util.Map;

import com.test.salesportal.jsutils.BaseJSTest;
import com.test.salesportal.jsutils.ConstructRequest;
import com.test.salesportal.jsutils.JSInvocable;

public class BaseFacetsTest extends BaseJSTest {

	final JSInvocable prepareGalleryRuntime(Map<String, Object> bindings, ConstructRequest ... constructRequests) throws IOException {

		final JSInvocable jsRuntime = super.prepareMavenWebAppScripts(bindings, constructRequests,
				"facetview.js",
				"facetmodel.js",
				"facetcontroller.js");

		return jsRuntime;
	}

}
