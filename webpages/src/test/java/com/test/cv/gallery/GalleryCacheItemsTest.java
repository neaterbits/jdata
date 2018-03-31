package com.test.cv.gallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.test.cv.jsutils.ConstructRequest;
import com.test.cv.jsutils.JSFunction;
import com.test.cv.jsutils.JSInvocable;

import static com.test.cv.gallery.DownloadInvocation.dataString;

public class GalleryCacheItemsTest extends BaseGalleryTest {

	private GalleryCacheItems prepareRuntime(List<DownloadInvocation> downloadRequests) throws IOException {

		final Map<String, Object> bindings = new HashMap<>();

		final Function<Object [], Object> modelDownloadItems = (params) -> {
				System.out.println("modelDownloadItems: Got params " + Arrays.toString(params));

				final Double startIndexDouble = (Double)params[0];
				final Double countDouble = (Double)params[1];

				final JSFunction callback = (JSFunction)params[2];
				
				final DownloadInvocation invocation = new DownloadInvocation(startIndexDouble.intValue(), countDouble.intValue(), callback);
				
				downloadRequests.add(invocation);
				
				return null;
		};
		
		
		final Object callback = createJSFunctionCallback(modelDownloadItems);

		
		final ConstructRequest constructRequest = new ConstructRequest("GalleryCacheItems", 20, callback);
		
		final JSInvocable invocable = super.prepareGalleryRuntime(bindings, constructRequest);
		
		return new GalleryCacheItems(invocable, constructRequest.getInstance());
	}
	
	private Object prepareUpdateVisibleAreaCallback(List<UpdateCompletion> completedUpdates) {
		// The function that is called back when completed download of all items
		final Function<Object[], Object> updateVisibleAreaComplete = (params) -> {
			
			System.out.println("## updateVisibleAreaComplete");

			final int startIndexDouble = (Integer)params[0];
			final int countDouble = (Integer)params[1];

			final UpdateCompletion update = new UpdateCompletion(startIndexDouble, countDouble, (Object[])params[2]);

			completedUpdates.add(update);

			return null;
		};
		
		final Object updateVisibleAreaCompleteCallback = createJSFunctionCallback(updateVisibleAreaComplete);

		return updateVisibleAreaCompleteCallback;
	}

	public void testScript() throws IOException {

		final List<DownloadInvocation> downloadRequests = new ArrayList<>();

		final GalleryCacheItems cacheItems = prepareRuntime(downloadRequests);
		
		// List for tracking updated items
		final List<UpdateCompletion> completedUpdates = new ArrayList<>();
		final Object updateVisibleAreaCompleteCallback = prepareUpdateVisibleAreaCallback(completedUpdates);

		// Create cache loader
		// final Object galleryCacheItems = 
			// jsRuntime.invokeConstructor("GalleryCacheItems", 20, modelDownloadItems);
		
		
		// No download requests until item downloaded
		assertThat(downloadRequests.size()).isEqualTo(0);

		final int firstVisibleIndex = 0;
		final int visibleCount = 4;
		final int totalNumberOfItems = 20;
		
		cacheItems.updateVisibleArea(firstVisibleIndex, visibleCount, totalNumberOfItems, updateVisibleAreaCompleteCallback);
		
		// Should now have one download request
		assertThat(downloadRequests.size()).isEqualTo(1);

		final DownloadInvocation downloadRequest = downloadRequests.get(0);
		assertThat(downloadRequest.getStartIndex()).isEqualTo(0);
		assertThat(downloadRequest.getCount()).isEqualTo(4);
		
		assertThat(completedUpdates.size()).isEqualTo(0);
		// Trigger downloaded (it. response from Ajax download)
		downloadRequest.onDownloaded();
		
		// This ought to now have triggered complete display to be downloade
		assertThat(completedUpdates.size()).isEqualTo(1);
		
		final UpdateCompletion completedUpdate = completedUpdates.get(0);
		
		assertThat(completedUpdate.startIndex).isEqualTo(0);
		assertThat(completedUpdate.visibleCount).isEqualTo(4);
		assertThat(completedUpdate.data.length).isEqualTo(4);
		assertThat(completedUpdate.data[0]).isEqualTo(dataString(0, 4, 0));
		assertThat(completedUpdate.data[1]).isEqualTo(dataString(0, 4, 1));
		assertThat(completedUpdate.data[2]).isEqualTo(dataString(0, 4, 2));
		assertThat(completedUpdate.data[3]).isEqualTo(dataString(0, 4, 3));
	}

	// Tests issue where responses from network occuring in reverse order,
	// eg when scrolling view
	public void testNetworkOrderSwitch() throws IOException {
		
		final List<DownloadInvocation> downloadRequests = new ArrayList<>();

		final GalleryCacheItems cacheItems = prepareRuntime(downloadRequests);
		
		// List for tracking updated items
		final List<UpdateCompletion> completedUpdates = new ArrayList<>();
		final Object updateVisibleAreaCompleteCallback = prepareUpdateVisibleAreaCallback(completedUpdates);
	
		
		// No download requests until item downloaded
		assertThat(downloadRequests.size()).isEqualTo(0);
		
		cacheItems.updateVisibleArea(
				0, 4, 20, 
				updateVisibleAreaCompleteCallback);
		
		// Should now have one download request
		assertThat(downloadRequests.size()).isEqualTo(1);

		// Invoke once more to simulate scrolling
		cacheItems.updateVisibleArea(
				2, 4, 20, 
				updateVisibleAreaCompleteCallback);

		// Should now have one more download request
		assertThat(downloadRequests.size()).isEqualTo(2);

		// Trigger response to second request, then to first
		// This should still result in completed-result to happen on second request as that is the newest one
		assertThat(downloadRequests.get(1).getStartIndex()).isEqualTo(4); // Starts at 4 since not re-requesting already performed request for items 0-3
		assertThat(downloadRequests.get(1).getCount()).isEqualTo(2);
		downloadRequests.get(1).onDownloaded();
		
		// No completed-calls yet since we only responded for items 4-5
		assertThat(completedUpdates.size()).isEqualTo(0);
		
		downloadRequests.get(0).onDownloaded();
		
		// This ought to now have triggered complete display to be downloaded
		assertThat(completedUpdates.size()).isEqualTo(1);
		
		final UpdateCompletion completedUpdate = completedUpdates.get(0);
		
		assertThat(completedUpdate.startIndex).isEqualTo(2);
		assertThat(completedUpdate.visibleCount).isEqualTo(4);
		assertThat(completedUpdate.data.length).isEqualTo(4);
		assertThat(completedUpdate.data[0]).isEqualTo(dataString(0, 4, 2)); // Note! first index 0, index 2 of 4 since resulting from first updateVisibleArea() call
		assertThat(completedUpdate.data[1]).isEqualTo(dataString(0, 4, 3)); // Note! first index 0, index 3 of 4 since resulting from first updateVisibleArea() call
		
		assertThat(completedUpdate.data[2]).isEqualTo(dataString(4, 2, 0)); // Start at 4, index 0 out of count 2
		assertThat(completedUpdate.data[3]).isEqualTo(dataString(4, 2, 1)); // Start at 4, index 1 out of count 2
	}

	private static class UpdateCompletion {
		private final int startIndex;
		private final int visibleCount;
		private final Object [] data; // Data that has been downloaded
		
		UpdateCompletion(int startIndex, int visibleCount, Object[] data) {
			this.startIndex = startIndex;
			this.visibleCount = visibleCount;
			this.data = data;
		}
	}
	
}
