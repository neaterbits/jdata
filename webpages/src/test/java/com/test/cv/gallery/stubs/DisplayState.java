package com.test.cv.gallery.stubs;

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
		final int result;
		final Object o = super.getProperty(property);
		
		// Sometimes Double and sometimes Integer
		// depending on what the JIT has decided
		if (o == null) {
			throw new IllegalStateException("Property " + property + " is null");
		}
		else if (o instanceof Double) {
			final Double d = (Double)o;

			if (Math.round(d) != d) {
				throw new IllegalStateException("Not an integer");
			}
			
			result = d.intValue();
		}
		else if (o instanceof Integer) {
			result = (Integer)o;
		}
		else {
			throw new IllegalStateException("Unknown type for " + o.getClass());
		}
		
		return result;
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
}
