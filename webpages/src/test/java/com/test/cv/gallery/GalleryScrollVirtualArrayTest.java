package com.test.cv.gallery;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.cv.gallery.wrappers.GalleryBase;
import com.test.cv.jsutils.ConstructRequest;
import com.test.cv.jsutils.JSInvocable;

public class GalleryScrollVirtualArrayTest extends BaseGalleryTest {

	private GalleryBase createGalleryBase() throws IOException {
		final ConstructRequest galleryBase = new ConstructRequest("GalleryBase");
		
		final JSInvocable invocable = super.prepareGalleryRuntime(new HashMap<String, Object>(), galleryBase);
	
		return new GalleryBase(invocable, galleryBase.getInstance());
	}

	private static final String arrayString(int start, int idx) {
		return arrayString(start + idx);
	}

	private static final String arrayString(int index) {
		return "Array element at virtual index " + index;
	}

	private static String [] sequence(int start, int count) {
		final String [] array = new String[count];
		
		for (int i = 0; i < count; ++ i) {
			array[i] = arrayString(start, i);
		}
		
		return array;
	}

	private static void checkIsSequence(String [] array, int start, int offset, int count, Function<Integer, String> makeArrayElement) {

		for (int i = 0; i < count; ++ i) {
			final String expectedElement = makeArrayElement.apply(start + offset + i);
			
			assertThat(array[i + offset]).isEqualTo(expectedElement);
		}
	}
	
	public void testChecksSizeOfInputArray() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		
		final String [] beforeScroll = sequence(100, 40);
		
		try {
			galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149, // this is 50, array is 40
				20, 70,
				20, 70,
				"default value");
		
			fail("Should have thrown parameter check exception due to array length not matching first and last index");
		}
		catch (RuntimeException ex) {
		}
	}

	
	public void testScrollToCompletelyAbove() throws IOException {

		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				20, 69,
				20, 69,
				"default value");

		assertThat(afterScroll.length).isEqualTo(50);

		checkIsSequence(afterScroll, 20, 0, 50, index -> "default value");
	}

	public void testScrollToCompletelyBelow() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				200, 249,
				200, 249,
				"default value");

		assertThat(afterScroll.length).isEqualTo(50);

		checkIsSequence(afterScroll, 200, 0, 50, index -> "default value");
	}

	public void testScrollToCompletelyAboveOffset1() throws IOException {

		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				50, 99,
				50, 99,
				"default value");

		assertThat(afterScroll.length).isEqualTo(50);

		checkIsSequence(afterScroll, 50, 0, 50, index -> "default value");
	}

	public void testScrollToCompletelyBelowOffset1() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				150, 199,
				150, 199,
				"default value");

		assertThat(afterScroll.length).isEqualTo(50);

		checkIsSequence(afterScroll, 150, 0, 50, index -> "default value");
	}

	public void testScrollToOverlapAboveOffset1() throws IOException {

		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				50, 100,
				50, 100,
				"default value");

		assertThat(afterScroll.length).isEqualTo(51);

		checkIsSequence(afterScroll, 50, 0, 50, index -> "default value");
		checkIsSequence(afterScroll, 50, 50, 1, index -> arrayString(100)); // last element is overlapping
	}

	public void testScrollToOverlapBelowOffset1() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				149, 199,
				149, 199,
				"default value");

		assertThat(afterScroll.length).isEqualTo(51);

		checkIsSequence(afterScroll, 149, 0, 1, index -> arrayString(149)); // first element is overlapping
		checkIsSequence(afterScroll, 150, 1, 50, index -> "default value");
	}

	
	public void testScrollToOverlapAboveOffsetAllMinus1() throws IOException {

		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				50, 148,
				50, 148,
				"default value");

		assertThat(afterScroll.length).isEqualTo(99);

		checkIsSequence(afterScroll, 50, 0, 50, index -> "default value");
		checkIsSequence(afterScroll, 50, 50, 49, index -> arrayString(index)); // last element is overlapping
	}

	public void testScrollToOverlapBelowOffsetAllMinus1() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				101, 199,
				101, 199,
				"default value");

		assertThat(afterScroll.length).isEqualTo(99);

		checkIsSequence(afterScroll, 101, 0, 49, index -> arrayString(index)); // first element is overlapping
		checkIsSequence(afterScroll, 101, 49, 50, index -> "default value");
	}

	public void testScrollToOverlapAboveOffsetAll() throws IOException {

		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				50, 149,
				50, 149,
				"default value");

		assertThat(afterScroll.length).isEqualTo(100);

		checkIsSequence(afterScroll, 50, 0, 50, index -> "default value");
		checkIsSequence(afterScroll, 50, 50, 50, index -> arrayString(index)); // last element is overlapping
	}

	public void testScrollToOverlapBelowOffsetAll() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				100, 199,
				100, 199,
				"default value");

		assertThat(afterScroll.length).isEqualTo(100);

		checkIsSequence(afterScroll, 100, 0, 50, index -> arrayString(index)); // first element is overlapping
		checkIsSequence(afterScroll, 100, 50, 50, index -> "default value");
	}

	public void testScrollToOverlapCompletely() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				100, 149,
				100, 149,
				"default value");

		assertThat(afterScroll.length).isEqualTo(50);

		checkIsSequence(afterScroll, 100, 0, 50, index -> arrayString(index)); // first element is overlapping
	}

	public void testScrollToOverlapWithinWithSameFirstIndex() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				100, 124,
				100, 124,
				"default value");

		assertThat(afterScroll.length).isEqualTo(25);

		checkIsSequence(afterScroll, 100, 0, 25, index -> arrayString(index)); // first element is overlapping
	}
	
	public void testScrollToOverlapWithinWithSameLastIndex() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				125, 149,
				125, 149,
				"default value");

		assertThat(afterScroll.length).isEqualTo(25);

		checkIsSequence(afterScroll, 125, 0, 25, index -> arrayString(index)); // first element is overlapping
	}

	public void testScrollToOverlapWithin() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				115, 134,
				115, 134,
				"default value");

		assertThat(afterScroll.length).isEqualTo(20);

		checkIsSequence(afterScroll, 115, 0, 20, index -> arrayString(index)); // first element is overlapping
	}

	public void testScrollToOverlapWithDifferenOverlapIndexAndnewArrayIndex() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		final String [] afterScroll = galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				115, 134,
				110, 139,
				"default value");

		assertThat(afterScroll.length).isEqualTo(30);

		checkIsSequence(afterScroll, 110, 0, 5, index -> "default value");
		checkIsSequence(afterScroll, 110, 5, 20, index -> arrayString(index)); // first element is overlapping
		checkIsSequence(afterScroll, 110, 25, 5, index -> "default value");
	}


	public void testNewArrayFirstIndexAboveOverlapFirstIndexThrowsException() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		try {
			galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				115, 134,
				125, 134,
				"default value");

			fail("Expected exception");
		}
		catch (RuntimeException ex) {
			assertThat(ex.getMessage()).isEqualTo("firstOverlapCheckIndex < newArrayFirstViewIndex");
		}
	}

	public void testNewArrayLastIndexBelowOverlapFirstIndexThrowsException() throws IOException {
		final GalleryBase galleryBase = createGalleryBase();
		final String [] beforeScroll = sequence(100, 50);
		
		try {
			galleryBase.scrollVirtualArrayView(
				String.class,
				beforeScroll,
				100, 149,
				115, 134,
				115, 129,
				"default value");

			fail("Expected exception");
		}
		catch (RuntimeException ex) {
			assertThat(ex.getMessage()).isEqualTo("lastOverlapCheckIndex > newArrayLastViewIndex");
		}
	}

	// TODO perhaps more test for when overlap indices and new array indices do not match
}
