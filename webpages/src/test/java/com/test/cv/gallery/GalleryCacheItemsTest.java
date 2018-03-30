package com.test.cv.gallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.test.cv.common.IOUtil;

public class GalleryCacheItemsTest extends BaseJSTest {

	private File getScriptDir() {
		final String file = getClass().getClassLoader().getResource("get_dir_file.txt").getPath();

		final File projectBaseDir = new File(file).getParentFile().getParentFile().getParentFile()
				.getParentFile().getParentFile().getParentFile();

		final File scriptDir = new File(projectBaseDir, "src/main/webapp/js");

		/*
		final File projectBaseDir = new File(file).getParentFile().getParentFile();
		final File scriptDir = new File(projectBaseDir, "webpages/js");
		*/
		
		if (!scriptDir.exists()) {
			throw new IllegalStateException("Could not find script dir " + scriptDir);
		}

		return scriptDir;
	}

	public void testScript() throws IOException {

		final File scriptDir = getScriptDir();

		final String baseScript1 = IOUtil.readFileToString(new File(scriptDir, "gallery_base.js"));
		final String baseScript2 = IOUtil.readFileToString(new File(scriptDir, "gallery_caches.js"));

		final String itemsScript = IOUtil.readFileToString(new File(scriptDir, "gallery_cache_items.js"));

		final String allScripts = baseScript1 + baseScript2 + itemsScript;

		// Add a test call
		final String testCall = 
			  "function createGalleryCacheItems() {"	
			+ "  var items = new GalleryCacheItems(20, modelDownloadItems);\n"
			+ "  \n"
			+ "  return items;\n"
			+ "}\n"
				
			+ allScripts;
		
		final JSScript jsScript = compileJS(testCall);
		
		final Map<String, Object> bindings = new HashMap<>();

		
		final List<DownloadInvocation> downloadRequests = new ArrayList<>();
		
		final Function<Object [], Object> modelDownloadItems = (params) -> {
				System.out.println("modelDownloadItems: Got params " + Arrays.toString(params));

				final Double startIndexDouble = (Double)params[0];
				final Double countDouble = (Double)params[1];

				final JSFunction callback = (JSFunction)params[2];
				
				final DownloadInvocation invocation = new DownloadInvocation(startIndexDouble.intValue(), countDouble.intValue(), callback);
				
				downloadRequests.add(invocation);
				
				return null;
		};
		
		bindings.put("modelDownloadItems", registerJSFunctionCallback(modelDownloadItems));
		bindings.put("console", new Console());

		// Evaluate any vars
		final JSRuntime jsRuntime = jsScript.eval(bindings);

		/*
		final JSScript s2 = compileJS("function xyz() { };");
		s2.run().invokeFunction("xyz");
		*/
		
		
		final Object galleryCacheItems = jsRuntime.invokeFunction("createGalleryCacheItems");
		

		// No download requests until item downloaded
		assertThat(downloadRequests.size()).isEqualTo(0);

		
		final int debugIndentLevel = 0;
		final int firstVisibleIndex = 0;
		final int visibleCount = 4;
		final int totalNumberOfItems = 20;
		
		jsRuntime.invokeMethod(galleryCacheItems, "updateVisibleArea",
				debugIndentLevel,
				firstVisibleIndex,
				visibleCount,
				totalNumberOfItems);

		// Should now have one download request
		assertThat(downloadRequests.size()).isEqualTo(1);
		assertThat(downloadRequests.get(0).startIndex).isEqualTo(0);
		assertThat(downloadRequests.get(0).count).isEqualTo(4);
		
	}
	
	private static class DownloadInvocation {
		private final int startIndex;
		private final int count;
		private final JSFunction callback;
		
		DownloadInvocation(int startIndex, int count, JSFunction callback) {
			this.startIndex = startIndex;
			this.count = count;
			this.callback = callback;
		}

		void onDownloaded() {
			
			// Just return an array of strings, this would be images or thumb sizes or similar for a real gallery
			final String [] result = new String[count];
			
			for (int i = 0; i < result.length; ++ i) {
				result[i] = String.valueOf("Downloaded-item at index" + (startIndex + i) + " / " + i + " out of " + count);
			}
			
			callback.call(result);
		}
	}
	
	public static class Console {
		public void log(String s) {
			System.out.println("console.log: " + s);;
		}
	}
}
