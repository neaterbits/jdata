package com.test.cv.gallery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.test.cv.jsutils.BaseJSTest;
import com.test.cv.jsutils.ConstructRequest;
import com.test.cv.jsutils.JSInvocable;

public class BaseGalleryTest extends BaseJSTest {
	
	/**
	 * Prepare a runtime with bindings and requests to construct any number of instances from
	 * objects
	 * @param bindings global variables to be added before evaluating script
	 * @param constructRequests objects to be constructed from classes, will be updated with constructed objects
	 * 
	 * @return invocable
	 * 
	 * @throws IOException
	 */

	final JSInvocable prepareGalleryRuntime(Map<String, Object> bindings, ConstructRequest ... constructRequests) throws IOException {

		final Map<String, Object> b = new HashMap<>(bindings);

		// Add console corresponding to web browser console
		b.put("console", new Console());
		
		final JSInvocable jsRuntime = super.prepareMavenWebAppScripts(b, constructRequests,
				"gallery_base.js",
				"gallery_caches.js",
				"gallery_cache_items.js",
				"gallery_cache_all_provisional_some_complete.js",
				"gallery_mode_base.js",
				"gallery_mode_width_hint.js",
				"gallery_mode_width_specific.js",
				"gallery_mode_height_hint.js",
				"gallery_mode_height_specific.js");


		return jsRuntime;
	}

	public static class Console {
		public void log(String s) {
			System.out.println("console.log: " + s);;
		}
	}
}
