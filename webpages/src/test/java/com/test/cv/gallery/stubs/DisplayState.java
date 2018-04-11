package com.test.cv.gallery.stubs;

import java.util.Map;

import com.test.cv.jsutils.JSInvocable;
import com.test.cv.jsutils.JavaWrapper;

/**
 * For white-box testing current display state from JS object
 */
public final class DisplayState extends JavaWrapper {

	public DisplayState(JSInvocable invocable, Object jsObject) {
		super(invocable, jsObject);
	}
	
	private int getIntProperty(String property) {
		final Object o = super.getProperty(property);
		
		// Sometimes Double and sometimes Integer
		// depending on what the JIT has decided
		if (o == null) {
			throw new IllegalStateException("Property " + property + " is null");
		}
		
		return nonNullNumberToExactInt(o);
	}
	
	public int getFirstRenderedY() {
		return getIntProperty("firstRenderedY");
	}
	
	public int getLastRenderedY() {
		return getIntProperty("lastRenderedY");
	}
	
	public int getFirstVisibleIndex() {
		return getIntProperty("firstVisibleIndex");
	}
	
	public int getLastVisibleIndex() {
		return getIntProperty("lastVisibleIndex");
	}
	
	public int getFirstRenderedIndex() {
		return getIntProperty("firstRenderedIndex");
	}
	
	public int getLastRenderedIndex() {
		return getIntProperty("lastRenderedIndex");
	}
	
	public int getFirstVisibleY() {
		return getIntProperty("firstVisibleY");
	}
	
	public int getLastVisibleY() {
		return getIntProperty("lastVisibleY");
	}

	public DisplayState setRenderStateComplete(int index, int count) {
		final Object jsObj = invokeMethod("setRenderStateComplete", index, count);

		return new DisplayState(getInvocable(), jsObj);
	}

	public boolean hasRenderStateComplete(int index) {
		return (Boolean)invokeMethod("hasRenderStateComplete", index);
	}

	public DisplayState addCurYToDisplayState(int curY, int visibleHeight, Map<String, Object> displayStateFields) {
		final Object jsObj = invokeMethod("addCurYToDisplayState", curY, visibleHeight, displayStateFields);
		
		return new DisplayState(getInvocable(), jsObj);
	}
}
