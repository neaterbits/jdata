package com.test.cv.gallery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.cv.gallery.api.CacheItems;
import com.test.cv.gallery.api.CacheItemsFactory;
import com.test.cv.gallery.api.GalleryConfig;
import com.test.cv.gallery.api.GalleryModel;
import com.test.cv.gallery.api.GalleryView;
import com.test.cv.gallery.api.HintGalleryConfig;
import com.test.cv.gallery.stubs.DisplayState;
import com.test.cv.gallery.stubs.DownloadInvocation;
import com.test.cv.gallery.stubs.GalleryCacheItemsStub;
import com.test.cv.gallery.stubs.GalleryModelStub;
import com.test.cv.gallery.stubs.GalleryViewStub;
import com.test.cv.gallery.stubs.MakeDownloadData;
import com.test.cv.gallery.stubs.UpdateVisibleAreaRequest;
import com.test.cv.gallery.stubs.html.Div;
import com.test.cv.gallery.stubs.html.Element;
import com.test.cv.gallery.stubs.modeldata.GalleryItemData;
import com.test.cv.gallery.stubs.modeldata.ProvisionalData;
import com.test.cv.gallery.wrappers.GalleryCacheAllProvisionalSomeComplete;
import com.test.cv.jsutils.ConstructRequest;
import com.test.cv.jsutils.JSInvocable;

public class GalleryCacheAllProvisionalSomeCompleteTest extends BaseGalleryTest {

	private GalleryCacheAllProvisionalSomeComplete prepareRuntime(
			GalleryConfig config,
			GalleryModel galleryModel,
			GalleryView<?, ?> galleryView,
			CacheItemsFactory galleryCacheItemsFactory
			) throws IOException {
		
		if (config == null) {
			throw new IllegalArgumentException("config == null");
		}

		final Map<String, Object> bindings = new HashMap<>();

		final List<ConstructRequest> constructRequests = new ArrayList<>();
		final Object cacheItemsFactory;
		
		if (galleryCacheItemsFactory == null) {
			// Create from default JS class if not a Java implementation was passed
			final ConstructRequest constructRequest = new ConstructRequest("GalleryCacheItemsFactory");
			
			constructRequests.add(constructRequest);

			cacheItemsFactory = constructRequest;
		}
		else {
			cacheItemsFactory = galleryCacheItemsFactory;
		}

		final ConstructRequest gallerySizes = new ConstructRequest("GallerySizes", config);
		final ConstructRequest galleryCache = new ConstructRequest("GalleryCacheAllProvisionalSomeComplete", gallerySizes, galleryModel, galleryView, cacheItemsFactory, 0);

		constructRequests.add(gallerySizes);
		constructRequests.add(galleryCache);

		final JSInvocable invocable = super.prepareGalleryRuntime(bindings, constructRequests.toArray(new ConstructRequest[constructRequests.size()]));
		
		return new GalleryCacheAllProvisionalSomeComplete(
				invocable,
				galleryCache.getInstance(),
				galleryModel, galleryView);
	}
	
	private static class CacheAndModel {
		private final GalleryCacheAllProvisionalSomeComplete cache;
		private final GalleryModelStub galleryModel;
		
		CacheAndModel(GalleryCacheAllProvisionalSomeComplete cache, GalleryModelStub model) {
			this.cache = cache;
			this.galleryModel = model;
		}
	}

	private CacheAndModel createCache(GalleryConfig config, CacheItemsFactory cacheItemsFactory, GalleryItemData [] itemData) throws IOException {

		return createCache(
				config,
				cacheItemsFactory,
				(firstIndex, count, indexInSet) -> itemData[firstIndex].getProvisionalData(),
				(firstIndex, count, indexInSet) -> itemData[firstIndex].getCompleteData());
	}

	private CacheAndModel createCache(GalleryConfig config, CacheItemsFactory cacheItemsFactory, MakeDownloadData makeProvisionalData, MakeDownloadData makeCompleteData) throws IOException {
		// Represent the divs added to the webpage
		final Div outer = new Div(800, 600);
		final Div inner = new Div(800, null); // height unknown, must be set by gallery
		
		makeProvisionalData = (startIndex, count, index) -> new ProvisionalData(240, 240);
		
		final GalleryView<Div, Element> galleryView = new GalleryViewStub();
		final GalleryModelStub galleryModel = new GalleryModelStub(this::getJSFunction, makeProvisionalData, makeCompleteData);

		final GalleryCacheAllProvisionalSomeComplete cache = prepareRuntime(config, galleryModel, galleryView, cacheItemsFactory);

		cache.setGalleryDivs(outer, inner);
		
		return new CacheAndModel(cache, galleryModel);
	}
	
	private static GalleryItemData [] createGalleryItemData(int totalNumberOfItems, int itemWidth, int itemHeight) {
		final GalleryItemData[] items = new GalleryItemData[totalNumberOfItems];
		
		for (int i = 0; i < items.length; ++ i) {
			items[i] = new GalleryItemData(itemHeight, itemHeight);
		}
		
		return items;
	}
	
	public void testInitialDownloadWithCacheItems() throws IOException {

		final int totalNumberOfItems = 20;
		final GalleryConfig config = new HintGalleryConfig(20, 20, 240, 240);
		final GalleryItemData [] items = createGalleryItemData(totalNumberOfItems, 240, 240);
		
		final CacheAndModel cm = createCache(config, null, items);
		
		assertThat(cm.galleryModel.getProvisionalRequestCount()).isEqualTo(0);

		
		cm.cache.refreshWithJSObjs(totalNumberOfItems);
		
		// Should get initial reqest for provisional data
		assertThat(cm.galleryModel.getProvisionalRequestCount()).isEqualTo(1);
		
		// Call back with provisional
		final DownloadInvocation initialProvisional = cm.galleryModel.getProvisionalRequestAt(0);
		cm.galleryModel.clearProvisionalRequests();
		
		assertThat(initialProvisional.getStartIndex()).isEqualTo(0);
		assertThat(initialProvisional.getCount()).isEqualTo(totalNumberOfItems);
		
		// Call back with data, will generate strings to send back
		initialProvisional.onDownloaded();

		// Should try to download complete-items as well
		assertThat(cm.galleryModel.getCompleteRequestCount()).isEqualTo(1);
	}

	private static class GalleryCacheItemsFactoryStub implements CacheItemsFactory {
		private final CacheItems cacheItems;
		
		private boolean isInvoked;

		GalleryCacheItemsFactoryStub(CacheItems cacheItems) {
			this.cacheItems = cacheItems;
		}

		@Override
		public CacheItems createCacheItems(int cachedBeforeAndAfter, OnCacheDownload onDownloadItem) {
			
			if (isInvoked) {
				throw new IllegalStateException("Already invoked");
			}

			this.isInvoked = true;

			return cacheItems;
		}
	}
	
	private void checkDisplayState(
			GalleryCacheAllProvisionalSomeComplete cache,
			int firstVisibleIndex, int lastVisibleIndex, int firstRenderedIndex, int lastRenderedIndex,
			int firstVisibleY, int lastVisibleY, int firstRenderedY, int lastRenderedY) {
		
		final DisplayState ds = cache.whiteboxGetDisplayState();
		
		assertThat(ds.getFirstVisibleIndex()).isEqualTo(firstVisibleIndex);
		assertThat(ds.getLastVisibleIndex()).isEqualTo(lastVisibleIndex);
		assertThat(ds.getFirstRenderedIndex()).isEqualTo(firstRenderedIndex);
		assertThat(ds.getLastRenderedIndex()).isEqualTo(lastRenderedIndex);
		
		assertThat(ds.getFirstVisibleY()).isEqualTo(firstVisibleY);
		assertThat(ds.getLastVisibleY()).isEqualTo(lastVisibleY);
		assertThat(ds.getFirstRenderedY()).isEqualTo(firstRenderedY);
		assertThat(ds.getLastRenderedY()).isEqualTo(lastRenderedY);
	}
	
	
	private void checkDisplayStateIs(GalleryCacheAllProvisionalSomeComplete cache, int firstIndex, int lastIndex, Predicate<Boolean> predicate) {
		final DisplayState ds = cache.whiteboxGetDisplayState();
		
		for (int i = firstIndex; i <= lastIndex; ++ i) {
			final boolean complete = ds.hasRenderStateComplete(i);
			
			assertThat(predicate.test(complete)).isTrue();
		}
	}
	
	
	private void checkDisplayStateIsComplete(GalleryCacheAllProvisionalSomeComplete cache, int firstIndex, int lastIndex) {
		checkDisplayStateIs(cache, firstIndex, lastIndex, complete -> complete);
	}
	
	private void checkDisplayStateIsAllProvisional(GalleryCacheAllProvisionalSomeComplete cache) {
		final DisplayState ds = cache.whiteboxGetDisplayState();

		checkDisplayStateIs(cache, ds.getFirstRenderedIndex(), ds.getLastRenderedIndex(), complete -> !complete);
	}
	
	public void testComputeIndexOfLastOnRow() throws IOException {
		final GalleryConfig config = new HintGalleryConfig(20, 20, 240, 240);
		final GalleryItemData [] items = createGalleryItemData(20, 240, 240);

		final CacheAndModel cm = createCache(config, null, items);
		
		assertThat(cm.cache.computeIndexOfLastOnRowStartingWithIndexWithArgs(0, 1, 3)).isEqualTo(0);
		assertThat(cm.cache.computeIndexOfLastOnRowStartingWithIndexWithArgs(1, 1, 3)).isEqualTo(1);
		assertThat(cm.cache.computeIndexOfLastOnRowStartingWithIndexWithArgs(2, 1, 3)).isEqualTo(2);

		assertThat(cm.cache.computeIndexOfLastOnRowStartingWithIndexWithArgs(0, 2, 3)).isEqualTo(1);
		assertThat(cm.cache.computeIndexOfLastOnRowStartingWithIndexWithArgs(2, 2, 3)).isEqualTo(2);
		assertThat(cm.cache.computeIndexOfLastOnRowStartingWithIndexWithArgs(2, 2, 4)).isEqualTo(3);
	}

	public void testScrollingWithStubbedCacheItemsAndHeightHintAll240x240() throws IOException {

		final GalleryConfig config = new HintGalleryConfig(10, 10, 240, 240);
		final GalleryItemData [] items = createGalleryItemData(100, 240, 240);

		final GalleryCacheItemsStub cacheItems = new GalleryCacheItemsStub(this::getJSFunction, (firstIndex, count, i) -> items[firstIndex].getCompleteData());

		final CacheAndModel cm = createCache(config, new GalleryCacheItemsFactoryStub(cacheItems), items);

		cm.cache.refresh(items.length);

		// Trigger download completion of all provisional data
		cm.galleryModel.getProvisionalRequestAt(0).onDownloaded();

		// Should now have a request for updating visible area
		assertThat(cacheItems.getUpdateRequestCount()).isEqualTo(1);
		UpdateVisibleAreaRequest request = cacheItems.getRequestAt(0);

		assertThat(request.getFirstVisibleIndex()).isEqualTo(0);
		assertThat(request.getVisibleCount()).isEqualTo(9); // 9 elements since (240 + 20) * 2 = 520 < 600
		assertThat(request.getTotalNumberOfItems()).isEqualTo(100);

		checkDisplayState(cm.cache, 0, 8, 0, 8, 0, 599, 0, 749);
		checkDisplayStateIsAllProvisional(cm.cache);

		cacheItems.getRequestAt(0).onComplete(); // Trigger all complete-data downloaded event
		checkDisplayStateIsComplete(cm.cache, 0, 8); // 0-8 is complete, ie. all rendered 

		// Scroll cache and check that calls for correct update of visible area

		// Clear list
		cacheItems.clearUpdateRequests();

		// Scroll 20 px down i area, should get new request
		cm.cache.updateOnScroll(20);
		
		// Should not be necessary to perform any new downloads
		assertThat(cacheItems.getUpdateRequestCount()).isEqualTo(0);
		
		checkDisplayState(cm.cache, 0, 8, 0, 8, 20, 619, 0, 749);
		checkDisplayStateIsComplete(cm.cache, 0, 8); // 0-8 is complete, ie. all rendered 

		
		// Scroll to 400, ought to be necessary to download new items
		// since has load 9 items = (240 + 10) * 3 = 750
		// So scroll to 500 would overlap by 20 pixels to next row
		// thus should cause a call for update of display area
		cm.cache.updateOnScroll(400);

		assertThat(cacheItems.getUpdateRequestCount()).isEqualTo(1);
		request = cacheItems.getRequestAt(0);

		assertThat(request.getFirstVisibleIndex()).isEqualTo(3);
		assertThat(request.getVisibleCount()).isEqualTo(9);
		assertThat(request.getTotalNumberOfItems()).isEqualTo(100);

		checkDisplayState(cm.cache, 3, 11, 0, 11, 400, 999, 0, 999);

		cacheItems.getRequestAt(0).onComplete(); // Trigger all complete-data downloaded event
		checkDisplayStateIsComplete(cm.cache, 0, 11);
		
		// Is now at 750 + 250 = 1000, scroll to 900
		cacheItems.clearUpdateRequests();
		
		cm.cache.updateOnScroll(900);

		// Ought to require two new items
		assertThat(cacheItems.getUpdateRequestCount()).isEqualTo(1);
		request = cacheItems.getRequestAt(0);

		assertThat(request.getFirstVisibleIndex()).isEqualTo(3 * 3); // 3 rows scrolled out 
		assertThat(request.getVisibleCount()).isEqualTo(9);
		assertThat(request.getTotalNumberOfItems()).isEqualTo(100);

		// Scroll to 950, should be same first-index but now there are 12 visible items
		// because for each row is 250 so for visible height of 600 there is
		// 950-1000, 1000-1250, 1250-1500 and 1500-1550

		checkDisplayState(cm.cache, 9, 17, 0, 17, 900, 1499, 0, 1499);

		cacheItems.getRequestAt(0).onComplete(); // Trigger all complete-data downloaded event
		checkDisplayStateIsComplete(cm.cache, 0, 17);

		cacheItems.clearUpdateRequests();
		cm.cache.updateOnScroll(950);

		// Ought to require two new items
		assertThat(cacheItems.getUpdateRequestCount()).isEqualTo(1);
		request = cacheItems.getRequestAt(0);

		assertThat(request.getFirstVisibleIndex()).isEqualTo(3 * 3); // 3 rows scrolled out 
		assertThat(request.getVisibleCount()).isEqualTo(12);
		assertThat(request.getTotalNumberOfItems()).isEqualTo(100);

		checkDisplayState(cm.cache, 9, 20, 0, 20, 950, 1549, 0, 1749);
		cacheItems.getRequestAt(0).onComplete(); // Trigger all complete-data downloaded event
		checkDisplayStateIsComplete(cm.cache, 0, 20);

		// Scroll up again a bit to 850
		cacheItems.clearUpdateRequests();
		cm.cache.updateOnScroll(850);

		// Ought to require no new items since already within firstRendered/lastRendered range
		assertThat(cacheItems.getUpdateRequestCount()).isEqualTo(0);

		// 850-1000, 1000-1250, 1250-1450
		checkDisplayState(cm.cache, 9, 17, 0, 17, 850, 1449, 0, 1799);
	}
}
