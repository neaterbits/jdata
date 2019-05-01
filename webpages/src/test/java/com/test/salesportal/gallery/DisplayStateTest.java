package com.test.salesportal.gallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.test.salesportal.gallery.stubs.DisplayState;
import com.test.salesportal.jsutils.JSInvocable;

public class DisplayStateTest extends BaseGalleryTest {

	private DisplayState createDisplayState() throws IOException {

		final JSInvocable runtime = prepareGalleryRuntime(new HashMap<>());
		
		final Object displayStateVar = runtime.getVariable("DisplayState");
		assertThat(displayStateVar).isNotNull();
		
		final Object createEmptyDisplayStateFunction = runtime.getProperty(displayStateVar, "createEmptyDisplayState");
		assertThat(createEmptyDisplayStateFunction).isNotNull();

		final Object obj = runtime.invokeFunctionObject(createEmptyDisplayStateFunction);
		
		assertThat(obj).isNotNull();
		
		return new DisplayState(runtime, obj);
	}
	
	public void testDisplayStateSetCreateEmoty() throws IOException {
		final DisplayState displayState = createDisplayState();

		assertThat(displayState.getFirstVisibleIndex()).isEqualTo(0);
		assertThat(displayState.getLastVisibleIndex()).isEqualTo(0);
		
		assertThat(displayState.getFirstRenderedIndex()).isEqualTo(0);
		assertThat(displayState.getLastRenderedIndex()).isEqualTo(0);
		
		assertThat(displayState.getFirstVisibleY()).isEqualTo(0);
		assertThat(displayState.getLastVisibleY()).isEqualTo(0);
		
		assertThat(displayState.getFirstRenderedY()).isEqualTo(0);
		assertThat(displayState.getLastRenderedY()).isEqualTo(0);
	}
	
	public void testDisplayStateSetAllComplete() throws IOException {
		final DisplayState displayState = createDisplayState();

		final Map<String, Object> map = new HashMap<>();
		
		final int firstRendered = 1;
		final int lastRendered = 20;

		map.put("firstVisibleIndex", 5);
		map.put("lastVisibleIndex", 10);

		map.put("firstRenderedIndex", firstRendered);
		map.put("lastRenderedIndex", lastRendered);
		
		map.put("firstRenderedY", 250);
		map.put("lastRenderedY", 2499);

		DisplayState updated = displayState.addCurYToDisplayState(1000, 500, map);
		
		assertThat(updated.getFirstVisibleIndex()).isEqualTo(5);
		assertThat(updated.getLastVisibleIndex()).isEqualTo(10);
		
		assertThat(updated.getFirstRenderedIndex()).isEqualTo(1);
		assertThat(updated.getLastRenderedIndex()).isEqualTo(20);
		
		assertThat(updated.getFirstVisibleY()).isEqualTo(1000);
		assertThat(updated.getLastVisibleY()).isEqualTo(1499);
		
		assertThat(updated.getFirstRenderedY()).isEqualTo(250);
		assertThat(updated.getLastRenderedY()).isEqualTo(2499);

		final int renderedCount = lastRendered - firstRendered + 1;
		
		// Check that we are in provisional render state
		for (int i = firstRendered; i <= lastRendered; ++ i) {
			assertThat(updated.hasRenderStateComplete(i)).isFalse();
		}
		
		// Now mark half of them as rendered
		updated = updated.setRenderStateComplete(firstRendered, renderedCount);
		for (int i = firstRendered; i <= lastRendered; ++ i) {
			assertThat(updated.hasRenderStateComplete(i)).isTrue();
		}
	}

	public void testDisplayStateSetAllInTwoOperationsAndThenWholeArrayAgain() throws IOException {
		final DisplayState displayState = createDisplayState();

		final Map<String, Object> map = new HashMap<>();
		
		map.put("firstVisibleIndex", 5);
		map.put("lastVisibleIndex", 10);

		map.put("firstRenderedIndex", 1);
		map.put("lastRenderedIndex", 20);
		
		map.put("firstRenderedY", 250);
		map.put("lastRenderedY", 2499);

		DisplayState updated = displayState.addCurYToDisplayState(1000, 500, map);
		
		// Check that we are in provisional render state
		for (int i = 1; i <= 20; ++ i) {
			assertThat(updated.hasRenderStateComplete(i)).isFalse();
		}
		
		// Now mark half of them as rendered
		updated = updated.setRenderStateComplete(1, 10);
		for (int i = 1; i <= 10; ++ i) {
			assertThat(updated.hasRenderStateComplete(i)).isTrue();
		}
		for (int i = 11; i <= 20; ++ i) {
			assertThat(updated.hasRenderStateComplete(i)).isFalse();
		}

		// Mark the rest
		updated = updated.setRenderStateComplete(11, 10);
		for (int i = 1; i <= 20; ++ i) {
			assertThat(updated.hasRenderStateComplete(i)).isTrue();
		}
		
		// And mark all at once and check again
		updated = updated.setRenderStateComplete(1, 20);
		for (int i = 1; i <= 20; ++ i) {
			assertThat(updated.hasRenderStateComplete(i)).isTrue();
		}
	}
}
