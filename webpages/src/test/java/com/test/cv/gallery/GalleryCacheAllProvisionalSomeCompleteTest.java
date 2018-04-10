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

		final Div upperPlaceHolder = new Div();
		
		// Represent the divs added to the webpage
		final Div outer = new Div(800, 600);
		final Div inner = new Div(800, null); // height unknown, must be set by gallery
		
		final GalleryView<Div, Element> galleryView = new GalleryView<Div, Element>() {

			@Override
			public Div createUpperPlaceHolder() {
				return upperPlaceHolder;
			}

			@Override
			public void appendToContainer(Div container, Element toAdd) {
				container.append(toAdd);
			}

			@Override
			public Div createRowContainer() {
				return new Div();
			}

			@Override
			public int getElementWidth(Element element) {
				return element.width;
			}

			@Override
			public int getElementHeight(Element element) {
				return element.height;
			}

			@Override
			public void setElementHeight(Element element, int heightPx) {
				element.setHeight(heightPx);
			}

			@Override
			public void setCSSClasses(Element element, String classes) {
				// Nothing for now, this is for reference from CSS
			}

			@Override
			public Element makeProvisionalHTMLElement(int index, Object data) {
				if (data == null) {
					throw new IllegalArgumentException("data == null");
				}

				return new Element();
			}

			@Override
			public void applyItemStyles(Element element, Integer rowHeight, Integer itemWidth, Integer itemHeight,
					int spacing, boolean visible) {

				if (itemWidth != null) {
					element.setWidth(itemWidth);
				}
				
				if (itemHeight != null) {
					element.setHeight(itemHeight);
				}
			}

			@Override
			public void applyRowContainerStyling(Div rowContainer, int y, int width, int height) {

				rowContainer.setWidth(width);
				rowContainer.setHeight(height);
				
			}
		};
		
		final List<DownloadInvocation> downloadProvisional  = new ArrayList<>();

		final List<DownloadInvocation> downloadComplete = new ArrayList<>();
		
		final GalleryModel galleryModel = new GalleryModel() {

			@Override
			public void getProvisionalData(int index, int count, Object onSuccess) {
				downloadProvisional.add(new DownloadInvocation(index, count, getJSFunction(onSuccess)));
			}

			@Override
			public void getCompleteData(int index, int count, Object onSuccess) {
				downloadComplete.add(new DownloadInvocation(index, count, getJSFunction(onSuccess)));
			}
		};

		final GalleryCacheAllProvisionalSomeComplete cache = prepareRuntime(config, galleryModel, galleryView);

		cache.setGalleryDivs(outer, inner);
		
		assertThat(downloadProvisional.size()).isEqualTo(0);

		final int totalNumberOfItems = 20;
		
		cache.refreshWithJSObjs(totalNumberOfItems);
		
		// Should get initial reqest for provisional data
		assertThat(downloadProvisional.size()).isEqualTo(1);
		
		// Call back with provisional
		final DownloadInvocation initialProvisional = downloadProvisional.get(0);
		downloadProvisional.clear();
		
		assertThat(initialProvisional.getStartIndex()).isEqualTo(0);
		assertThat(initialProvisional.getCount()).isEqualTo(totalNumberOfItems);
		
		// Call back with data, will generate strings to send back
		initialProvisional.onDownloaded();
		
		// Should try to download complete-items as well
		assertThat(downloadComplete.size()).isEqualTo(0);
	}

	// Represents a div.
	private static class Div extends Element {
		
		private final List<Element> elements;
		
		Div() {
			this(null, null);
		}

		Div(Integer width, Integer height) {
			super(width, height);

			this.elements = new ArrayList<>();
		}
		
		void append(Element element) {
			if (element == null) {
				throw new IllegalArgumentException("element == null");
			}
			
			elements.add(element);
		}
	}
	
	private static class Element {
		private Integer width;
		private Integer height;
		
		Element() {
			this(null, null);
		}

		Element(Integer width, Integer height) {
			this.width = width;
			this.height = height;
		}
		
		void setWidth(int width) {
			this.width = width;
		}
		
		void setHeight(int height) {
			this.height = height;
		}
	}
	
}
