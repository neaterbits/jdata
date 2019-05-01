package com.test.salesportal.gallery;

import static com.test.salesportal.gallery.stubs.DownloadInvocation.dataString;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.test.salesportal.gallery.stubs.DownloadInvocation;
import com.test.salesportal.gallery.wrappers.GalleryCacheItems;
import com.test.salesportal.jsutils.ConstructRequest;
import com.test.salesportal.jsutils.JSFunction;
import com.test.salesportal.jsutils.JSInvocable;

public class GalleryCacheItemsTest extends BaseGalleryTest {

	private GalleryCacheItems prepareRuntime(List<DownloadInvocation> downloadRequests, int cachedBeforeAndAfter) throws IOException {

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

		
		final ConstructRequest constructRequest = new ConstructRequest("GalleryCacheItems", cachedBeforeAndAfter, callback);
		
		final JSInvocable invocable = super.prepareGalleryRuntime(bindings, constructRequest);
		
		return new GalleryCacheItems(invocable, constructRequest.getInstance());
	}
	
	private Object prepareUpdateVisibleAreaCallback(List<UpdateCompletion> completedUpdates) {
		// The function that is called back when completed download of all items
		final Function<Object[], Object> updateVisibleAreaComplete = (params) -> {
			
			final int startIndexDouble = (Integer)params[0];
			final int countDouble = (Integer)params[1];

			final UpdateCompletion update = new UpdateCompletion(startIndexDouble, countDouble, (Object[])params[2]);

			completedUpdates.add(update);

			return null;
		};
		
		final Object updateVisibleAreaCompleteCallback = createJSFunctionCallback(updateVisibleAreaComplete);

		return updateVisibleAreaCompleteCallback;
	}

	public void testSingleUpdateRequest() throws IOException {

		final List<DownloadInvocation> downloadRequests = new ArrayList<>();

		final GalleryCacheItems cacheItems = prepareRuntime(downloadRequests, 20);
		
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
		
		cacheItems.updateVisibleArea(0, firstVisibleIndex, visibleCount, totalNumberOfItems, updateVisibleAreaCompleteCallback);
		
		// Should now have one download request
		assertThat(downloadRequests.size()).isEqualTo(1);

		final DownloadInvocation downloadRequest = downloadRequests.get(0);
		assertThat(downloadRequest.getStartIndex()).isEqualTo(0);
		assertThat(downloadRequest.getCount()).isEqualTo(4);
		
		assertThat(completedUpdates.size()).isEqualTo(0);
		// Trigger downloaded (it. response from Ajax download)
		downloadRequest.onDownloaded();
		
		// This ought to now have triggered complete display to be downloaded
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
	public void testNetworkOrderSwitchOfUpdateResult() throws IOException {
		
		final List<DownloadInvocation> downloadRequests = new ArrayList<>();

		final GalleryCacheItems cacheItems = prepareRuntime(downloadRequests, 20);
		
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
	
	public void testScrollBelowInitialElement() throws IOException {
		// Fix issue for when updating visible area so that first row of virtual array
		// no longer included in cached data
		final List<DownloadInvocation> downloadRequests = new ArrayList<>();

		final GalleryCacheItems cacheItems = prepareRuntime(downloadRequests, 1);
		
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
		
		cacheItems.updateVisibleArea(0, firstVisibleIndex, visibleCount, totalNumberOfItems, updateVisibleAreaCompleteCallback);
		// Should now have one download request
		assertThat(downloadRequests.size()).isEqualTo(1);

		DownloadInvocation downloadRequest = downloadRequests.get(0);
		assertThat(downloadRequest.getStartIndex()).isEqualTo(0);
		assertThat(downloadRequest.getCount()).isEqualTo(4);
		
		assertThat(completedUpdates.size()).isEqualTo(0);
		// Trigger downloaded (it. response from Ajax download)
		downloadRequest.onDownloaded();

		// This ought to now have triggered complete display to be downloaded
		assertThat(completedUpdates.size()).isEqualTo(1);
		
		downloadRequests.clear();
		completedUpdates.clear();
		
		// Here comes the real test, scroll down more so that first preloaded index > 0
		cacheItems.updateVisibleArea(0, firstVisibleIndex + 2, visibleCount, totalNumberOfItems, updateVisibleAreaCompleteCallback);
		assertThat(downloadRequests.size()).isEqualTo(1);
		
		downloadRequest = downloadRequests.get(0);
		assertThat(downloadRequest.getStartIndex()).isEqualTo(4); // since already download 2-3
		assertThat(downloadRequest.getCount()).isEqualTo(2);
		
		assertThat(completedUpdates.size()).isEqualTo(0);
		
		downloadRequest.onDownloaded();
		assertThat(completedUpdates.size()).isEqualTo(1);
		
		// Once more to trigger bug since now this.curVisibleIndex is not in sync with size of cached data
		cacheItems.updateVisibleArea(0, firstVisibleIndex + 4, visibleCount, totalNumberOfItems, updateVisibleAreaCompleteCallback);
				
	}

	/*
	 * issue, retrieving entry 0-5,
	 * while awaiting response, is scrolled to 2-5 which scrolls virtual array and 0-1 is removed since not in visible are
	 * (and cachedBeforeAndAfter might be null).
	 * This means that we will not find complete-array for initial response and second request was never run due to already ongoing request.

	 * Fixes?

	 * - scroll virtual array after retrieving initial response? Not very intuitive since visible area is updated immediately.
	 * - keep a list of requests that have not been run because of overlapping requests and check those as well. Simpler solution probably.
	 * 
	 * - or just check state after any response request against cached items for *display area* ? Checking against first and last checked index
	 * or first and last index of the request is not really relevant, we only need to know that we have the elements
	 * for current display area (ie. from latest call to )
	 * 
	 */

	public void testReproduceBugNotLoadingWhenScrollingDownBecauseOfInitialNotCopiedInScrollVirtualArray() throws IOException {
		final List<DownloadInvocation> downloadRequests = new ArrayList<>();

		// Preload 2 items before and after
		final GalleryCacheItems cacheItems = prepareRuntime(downloadRequests, 20); // preload triggers issue
		
		// List for tracking updated items
		final List<UpdateCompletion> completedUpdates = new ArrayList<>();
		final Object updateVisibleAreaCompleteCallback = prepareUpdateVisibleAreaCallback(completedUpdates);

		// Download initial 0-3
		cacheItems.updateVisibleArea(0, 4, 20, updateVisibleAreaCompleteCallback);
		assertThat(downloadRequests.size()).isEqualTo(1);
		DownloadInvocation downloadRequest = downloadRequests.get(0);
		
		assertThat(downloadRequest.getStartIndex()).isEqualTo(0);
		assertThat(downloadRequest.getCount()).isEqualTo(4);

		downloadRequest.onDownloaded();
		
		assertThat(completedUpdates.size()).isEqualTo(1);
		completedUpdates.clear();
		downloadRequests.clear();

		// Update 0 - 5 to trigger download from 4-5
		cacheItems.updateVisibleArea(0, 6, 20, updateVisibleAreaCompleteCallback);
		
		assertThat(downloadRequests.size()).isEqualTo(1);
		downloadRequest = downloadRequests.get(0);
		
		assertThat(downloadRequest.getStartIndex()).isEqualTo(4);
		assertThat(downloadRequest.getCount()).isEqualTo(2);

		downloadRequests.clear();
		
		// Run another update for 2-5, this will scroll cache and set index 0-2 to null
		cacheItems.updateVisibleArea(2, 4, 20, updateVisibleAreaCompleteCallback);
		
		// This should not have triggered any download request since is overlapping
		assertThat(downloadRequests.size()).isEqualTo(0);
		
		downloadRequest.onDownloaded();
		
		// Should now have a complete-update even if download-request is mapped to the initial update for 4-5
		// but would not pass check of complete scroll-area since 0-1 is null
		assertThat(completedUpdates.size()).isEqualTo(1);
	}

	/* 
	 * The issue above was not triggered in the case below where we have no cached before and after (0 passed to prepareRuntime())
	 * since this.cachedData would only consist items from visible area and therefore with existing code (checking for all set from firstCached to lastCached)
	 * have all elements loaded.
	 */
	public void testReproduceBugNotLoadingWhenScrollingDownBecauseOfInitialNotCopiedInScrollVirtualArray_NoCachedBeforAndAfter() throws IOException {
		final List<DownloadInvocation> downloadRequests = new ArrayList<>();

		// Preload 2 items before and after
		final GalleryCacheItems cacheItems = prepareRuntime(downloadRequests, 0);
		
		// List for tracking updated items
		final List<UpdateCompletion> completedUpdates = new ArrayList<>();
		final Object updateVisibleAreaCompleteCallback = prepareUpdateVisibleAreaCallback(completedUpdates);

		// Download initial
		cacheItems.updateVisibleArea(0, 4, 20, updateVisibleAreaCompleteCallback);
		assertThat(downloadRequests.size()).isEqualTo(1);
		DownloadInvocation downloadRequest = downloadRequests.get(0);
		
		assertThat(downloadRequest.getStartIndex()).isEqualTo(0);
		assertThat(downloadRequest.getCount()).isEqualTo(4);

		downloadRequest.onDownloaded();

		assertThat(completedUpdates.size()).isEqualTo(1);
		completedUpdates.clear();

		downloadRequests.clear();
		
		// Update 0 - 6 to trigger download
		cacheItems.updateVisibleArea(0, 6, 20, updateVisibleAreaCompleteCallback);
		
		assertThat(downloadRequests.size()).isEqualTo(1);
		downloadRequest = downloadRequests.get(0);
		
		assertThat(downloadRequest.getStartIndex()).isEqualTo(4);
		assertThat(downloadRequest.getCount()).isEqualTo(2);

		downloadRequests.clear();
		
		// Run another update for 2-5, this will scroll cache and set index 0-2 to null
		cacheItems.updateVisibleArea(2, 4, 20, updateVisibleAreaCompleteCallback);
		
		// This should not have triggered any download request since is overlapping
		assertThat(downloadRequests.size()).isEqualTo(0);
		
		downloadRequest.onDownloaded();
		
		// Should now have a complete-update even if download-request is mapped to the initial update for 0-5
		assertThat(completedUpdates.size()).isEqualTo(1);
	}
	

	public void testDownloadResponseOutOfOrder_NoCachedBeforeAndAfter() throws IOException {
		final List<DownloadInvocation> downloadRequests = new ArrayList<>();

		// Preload 2 items before and after
		final GalleryCacheItems cacheItems = prepareRuntime(downloadRequests, 0);
		
		// List for tracking updated items
		final List<UpdateCompletion> completedUpdates = new ArrayList<>();
		final Object updateVisibleAreaCompleteCallback = prepareUpdateVisibleAreaCallback(completedUpdates);

		// Update 0 - 6 to trigger download
		cacheItems.updateVisibleArea(0, 6, 20, updateVisibleAreaCompleteCallback);
		
		assertThat(downloadRequests.size()).isEqualTo(1);
		final DownloadInvocation downloadRequest = downloadRequests.get(0);
		
		assertThat(downloadRequest.getStartIndex()).isEqualTo(0);
		assertThat(downloadRequest.getCount()).isEqualTo(6);

		downloadRequests.clear();
		
		// Run another update for 2-5, this will scroll cache and set index 0-2 to null
		cacheItems.updateVisibleArea(2, 4, 20, updateVisibleAreaCompleteCallback);
		
		// This should not have triggered any download request since is overlapping
		assertThat(downloadRequests.size()).isEqualTo(0);
		
		downloadRequest.onDownloaded();
		
		// Should now have a complete-update even if download-request is mapped to the initial update for 0-5
		assertThat(completedUpdates.size()).isEqualTo(1);
	}
	

	public void testPreloadBeforeAndAfter() throws IOException {
		final List<DownloadInvocation> downloadRequests = new ArrayList<>();

		// Preload 2 items before and after
		final GalleryCacheItems cacheItems = prepareRuntime(downloadRequests, 2);
		
		// List for tracking updated items
		final List<UpdateCompletion> completedUpdates = new ArrayList<>();
		final Object updateVisibleAreaCompleteCallback = prepareUpdateVisibleAreaCallback(completedUpdates);

		// No download requests until item downloaded
		assertThat(downloadRequests.size()).isEqualTo(0);

		cacheItems.updateVisibleArea(0, 4, 20, updateVisibleAreaCompleteCallback);
		
		// Should now have one download request
		assertThat(downloadRequests.size()).isEqualTo(1);
		final DownloadInvocation downloadRequest = downloadRequests.get(0);
		
		downloadRequests.clear();
		
		System.out.println("## trigger onDownloaded");

		// Responding to this download request ought to cause one request for two items after this one, which are the preload requests
		downloadRequest.onDownloaded();

		assertThat(downloadRequests.size()).isEqualTo(1);
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
