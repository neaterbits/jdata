package com.test.cv.gallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;

import com.test.cv.gallery.api.GalleryConfig;
import com.test.cv.gallery.api.HintGalleryConfig;
import com.test.cv.gallery.wrappers.GalleryCacheBase;
import com.test.cv.jsutils.ConstructRequest;
import com.test.cv.jsutils.JSInvocable;

/**
 * Tests for various base/helper functionality
 * 
 */
public class GalleryCacheBaseTest extends BaseGalleryTest {
	
	private GalleryCacheBase createGalleryCacheBase(GalleryConfig config, int totalNumberOfItems) throws IOException {

		final ConstructRequest gallerySizes = new ConstructRequest("GallerySizes", config);

		final ConstructRequest galleryCacheBase = new ConstructRequest(
				"GalleryCacheBase",
				gallerySizes,
				null, // model
				null, // view
				totalNumberOfItems);

		final JSInvocable runtime = prepareGalleryRuntime(new HashMap<>(), gallerySizes, galleryCacheBase);

		return new GalleryCacheBase(runtime, galleryCacheBase.getInstance());
	}
	
	private static final GalleryConfig GALLERY_CONFIG
			= new HintGalleryConfig(20, 20, 240, 240);

	public void testGetTotalNumberOfItems() throws IOException {
		GalleryCacheBase cacheBase = createGalleryCacheBase(GALLERY_CONFIG, 0);
		assertThat(cacheBase.whiteboxGetTotalNumberOfItems()).isEqualTo(0);

		cacheBase = createGalleryCacheBase(GALLERY_CONFIG, 1);
		assertThat(cacheBase.whiteboxGetTotalNumberOfItems()).isEqualTo(1);

		cacheBase = createGalleryCacheBase(GALLERY_CONFIG, 23);
		assertThat(cacheBase.whiteboxGetTotalNumberOfItems()).isEqualTo(23);

		cacheBase = createGalleryCacheBase(GALLERY_CONFIG, 100);
		assertThat(cacheBase.whiteboxGetTotalNumberOfItems()).isEqualTo(100);
	}

	public void testComputeNumRowsTotal() throws IOException {
		GalleryCacheBase cacheBase = createGalleryCacheBase(GALLERY_CONFIG, 0);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(2)).isEqualTo(0);

		cacheBase = createGalleryCacheBase(GALLERY_CONFIG, 1);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(2)).isEqualTo(1);

		cacheBase = createGalleryCacheBase(GALLERY_CONFIG, 23);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(1)).isEqualTo(23);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(2)).isEqualTo(12);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(3)).isEqualTo(8);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(4)).isEqualTo(6);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(5)).isEqualTo(5);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(6)).isEqualTo(4);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(7)).isEqualTo(4);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(8)).isEqualTo(3);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(9)).isEqualTo(3);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(10)).isEqualTo(3);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(11)).isEqualTo(3);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(12)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(13)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(15)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(16)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(17)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(18)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(19)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(20)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(21)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(22)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(23)).isEqualTo(1);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(24)).isEqualTo(1);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(25)).isEqualTo(1);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(26)).isEqualTo(1);

		cacheBase = createGalleryCacheBase(GALLERY_CONFIG, 20);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(1)).isEqualTo(20);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(2)).isEqualTo(10);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(3)).isEqualTo(7);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(4)).isEqualTo(5);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(5)).isEqualTo(4);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(6)).isEqualTo(4);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(7)).isEqualTo(3);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(8)).isEqualTo(3);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(9)).isEqualTo(3);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(10)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(11)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(12)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(13)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(15)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(16)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(17)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(18)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(19)).isEqualTo(2);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(20)).isEqualTo(1);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(21)).isEqualTo(1);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(22)).isEqualTo(1);
		assertThat(cacheBase.whiteboxComputeNumRowsTotalFromNumColumns(23)).isEqualTo(1);
	}

	public void testComputeIndexOfLastOnRow() throws IOException {

		GalleryCacheBase cb = createGalleryCacheBase(GALLERY_CONFIG, 0);
		
		assertThat(cb.whiteboxComputeIndexOfLastOnRowStartingWithIndexWithArgs(0, 1, 3)).isEqualTo(0);
		assertThat(cb.whiteboxComputeIndexOfLastOnRowStartingWithIndexWithArgs(1, 1, 3)).isEqualTo(1);
		assertThat(cb.whiteboxComputeIndexOfLastOnRowStartingWithIndexWithArgs(2, 1, 3)).isEqualTo(2);

		assertThat(cb.whiteboxComputeIndexOfLastOnRowStartingWithIndexWithArgs(0, 2, 3)).isEqualTo(1);
		assertThat(cb.whiteboxComputeIndexOfLastOnRowStartingWithIndexWithArgs(2, 2, 3)).isEqualTo(2);
		assertThat(cb.whiteboxComputeIndexOfLastOnRowStartingWithIndexWithArgs(2, 2, 4)).isEqualTo(3);
	}
}
