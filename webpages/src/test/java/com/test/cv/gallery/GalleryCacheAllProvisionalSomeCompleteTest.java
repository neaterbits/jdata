package com.test.cv.gallery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.cv.gallery.api.GalleryConfig;
import com.test.cv.gallery.api.GalleryModel;
import com.test.cv.gallery.api.GalleryView;
import com.test.cv.gallery.api.HintGalleryConfig;
import com.test.cv.gallery.stubs.DownloadInvocation;
import com.test.cv.gallery.stubs.GalleryModelStub;
import com.test.cv.gallery.stubs.GalleryViewStub;
import com.test.cv.gallery.stubs.html.Div;
import com.test.cv.gallery.stubs.html.Element;
import com.test.cv.gallery.wrappers.GalleryCacheAllProvisionalSomeComplete;
import com.test.cv.jsutils.ConstructRequest;
import com.test.cv.jsutils.JSInvocable;

public class GalleryCacheAllProvisionalSomeCompleteTest extends BaseGalleryTest {

	private GalleryCacheAllProvisionalSomeComplete prepareRuntime(
			GalleryConfig config,
			GalleryModel galleryModel,
			GalleryView<?, ?> galleryView
			) throws IOException {
		
		if (config == null) {
			throw new IllegalArgumentException("config == null");
		}

		final Map<String, Object> bindings = new HashMap<>();

		final ConstructRequest gallerySizes = new ConstructRequest("GallerySizes", config);
		final ConstructRequest constructRequest = new ConstructRequest("GalleryCacheAllProvisionalSomeComplete", gallerySizes, galleryModel, galleryView, 0);
			
		final JSInvocable invocable = super.prepareGalleryRuntime(bindings, gallerySizes, constructRequest);
		
		return new GalleryCacheAllProvisionalSomeComplete(
				invocable,
				constructRequest.getInstance(),
				galleryModel, galleryView);
	}
	
	public void testSimple() throws IOException {

		final GalleryConfig config = new HintGalleryConfig(20, 20, 240, 240);

		// Represent the divs added to the webpage
		final Div outer = new Div(800, 600);
		final Div inner = new Div(800, null); // height unknown, must be set by gallery
		
		final GalleryView<Div, Element> galleryView = new GalleryViewStub();
		
		
		final GalleryModelStub galleryModel = new GalleryModelStub(this::getJSFunction);

		final GalleryCacheAllProvisionalSomeComplete cache = prepareRuntime(config, galleryModel, galleryView);

		cache.setGalleryDivs(outer, inner);
		
		assertThat(galleryModel.downloadProvisional.size()).isEqualTo(0);

		final int totalNumberOfItems = 20;
		
		cache.refreshWithJSObjs(totalNumberOfItems);
		
		// Should get initial reqest for provisional data
		assertThat(galleryModel.downloadProvisional.size()).isEqualTo(1);
		
		// Call back with provisional
		final DownloadInvocation initialProvisional = galleryModel.downloadProvisional.get(0);
		galleryModel.downloadProvisional.clear();
		
		assertThat(initialProvisional.getStartIndex()).isEqualTo(0);
		assertThat(initialProvisional.getCount()).isEqualTo(totalNumberOfItems);
		
		// Call back with data, will generate strings to send back
		initialProvisional.onDownloaded();
		
		// Should try to download complete-items as well
		assertThat(galleryModel.downloadComplete.size()).isEqualTo(0);
	}

}
