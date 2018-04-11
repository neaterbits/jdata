package com.test.cv.gallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.test.cv.gallery.stubs.DisplayState;
import com.test.cv.jsutils.JSInvocable;

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
	
	public void testDisplayState() throws IOException {
		final DisplayState displayState = createDisplayState();

		assertThat(displayState.getFirstVisibleIndex()).isEqualTo(0);
		assertThat(displayState.getLastVisibleIndex()).isEqualTo(0);
		
		assertThat(displayState.getFirstRenderedIndex()).isEqualTo(0);
		assertThat(displayState.getLastRenderedIndex()).isEqualTo(0);
		
		assertThat(displayState.getFirstVisibleY()).isEqualTo(0);
		assertThat(displayState.getLastVisibleY()).isEqualTo(0);
		
		assertThat(displayState.getFirstRenderedY()).isEqualTo(0);
		assertThat(displayState.getLastRenderedY()).isEqualTo(0);
	
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
}
