package com.test.cv.gallery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.cv.gallery.api.CacheItems;
import com.test.cv.gallery.api.CacheItemsFactory;
import com.test.cv.gallery.api.GalleryConfig;
import com.test.cv.gallery.api.GalleryModel;
import com.test.cv.gallery.api.GalleryView;
import com.test.cv.gallery.api.HintGalleryConfig;
import com.test.cv.gallery.stubs.DownloadInvocation;
import com.test.cv.gallery.stubs.GalleryCacheItemsStub;
import com.test.cv.gallery.stubs.GalleryModelStub;
import com.test.cv.gallery.stubs.GalleryViewStub;
import com.test.cv.gallery.stubs.MakeDownloadData;
import com.test.cv.gallery.stubs.UpdateVisibleAreaRequest;
import com.test.cv.gallery.stubs.html.Div;
import com.test.cv.gallery.stubs.html.Element;
import com.test.cv.gallery.stubs.html.ElementSize;
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
	
	private CacheAndModel createCache(GalleryConfig config, CacheItemsFactory cacheItemsFactory) throws IOException {
		// Represent the divs added to the webpage
		final Div outer = new Div(800, 600);
		final Div inner = new Div(800, null); // height unknown, must be set by gallery
		
		final MakeDownloadData makeProvisionalData = (startIndex, count, index) -> new ElementSize(240, 240);
		
		final GalleryView<Div, Element> galleryView = new GalleryViewStub();
		final GalleryModelStub galleryModel = new GalleryModelStub(this::getJSFunction, makeProvisionalData);

		final GalleryCacheAllProvisionalSomeComplete cache = prepareRuntime(config, galleryModel, galleryView, cacheItemsFactory);

		cache.setGalleryDivs(outer, inner);
		
		return new CacheAndModel(cache, galleryModel);
	}
	
	public void testInitialDownloadWithCacheItems() throws IOException {

		final GalleryConfig config = new HintGalleryConfig(20, 20, 240, 240);

		final CacheAndModel cm = createCache(config, null);
		
		assertThat(cm.galleryModel.getProvisionalRequestCount()).isEqualTo(0);

		final int totalNumberOfItems = 20;
		
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

	public void testScrollingWithStubbedCacheItemsAndHeightHintAll240x240() throws IOException {

		final GalleryConfig config = new HintGalleryConfig(10, 10, 240, 240);
		final GalleryCacheItemsStub cacheItems = new GalleryCacheItemsStub(this::getJSFunction);

		final CacheAndModel cm = createCache(config, new GalleryCacheItemsFactoryStub(cacheItems));

		cm.cache.refresh(100);

		// Trigger download completion of all provisional data
		cm.galleryModel.getProvisionalRequestAt(0).onDownloaded();

		// Should now have a request for updating visible area
		assertThat(cacheItems.getUpdateRequestCount()).isEqualTo(1);
		UpdateVisibleAreaRequest request = cacheItems.getRequestAt(0);

		assertThat(request.getFirstVisibleIndex()).isEqualTo(0);
		assertThat(request.getVisibleCount()).isEqualTo(9); // 9 elements since (240 + 20) * 2 = 520 < 600
		assertThat(request.getTotalNumberOfItems()).isEqualTo(100);
		
		// Scroll cache and check that calls for correct update of visible area

		// Clear list
		cacheItems.clearUpdateRequests();

		// Scroll 20 px down i area, should get new request
		cm.cache.updateOnScroll(20);
		
		// Should not be necessary to perform any new downloads
		assertThat(cacheItems.getUpdateRequestCount()).isEqualTo(0);
		
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

		cacheItems.clearUpdateRequests();

		cm.cache.updateOnScroll(950);

		// Ought to require two new items
		assertThat(cacheItems.getUpdateRequestCount()).isEqualTo(1);
		request = cacheItems.getRequestAt(0);

		assertThat(request.getFirstVisibleIndex()).isEqualTo(3 * 3); // 3 rows scrolled out 
		assertThat(request.getVisibleCount()).isEqualTo(12);
		assertThat(request.getTotalNumberOfItems()).isEqualTo(100);
	}
}
