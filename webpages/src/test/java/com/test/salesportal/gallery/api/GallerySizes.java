package com.test.salesportal.gallery.api;

/**
 * For computing various gallery dimensions from sizes
 * 
 */
public interface GallerySizes {

	/**
	 * Compute number of columns that can be displayed.
	 * Will be computed by taking specific or hinted width and columnspacing into account
	 * 
	 * @param visibleWidth current width of visible display area
	 * 
	 * @return number of columns
	 */
	
	int computeNumColumns(int visibleWidth);
	
	/**
	 * Get current column spacing, ie minimum number of spacing
	 * between elements. Each item will have spacing / 2 number of pixels
	 * before and after each item, also the first item on a row.
	 * So if column spacing is 20 pixels, the will be
	 * | 10px '--- item here ---' 20px '--- item here---' | 
	 * 
	 * Thus columnspacing ought to be dividable by 2 (columnSpacing % 2 == 0)
	 * 
	 * @return column spacing in pixels
	 */
	int getColumnSpacing();
	
	/**
	 * Same as colunSpacing but between rows, ie. vertically.
	 * @return
	 */
	
	int getRowSpacing();
	
	/**
	 * Compute height of a number of elements, from height hint or specific height,
	 * taking row spacing into account.
	 * 
	 * This is useful both for computing or (or approximating) the initial
	 * height of the scrollable area or if initiallly an approximation,
	 * recalculate it as user scrolls.
	 * 
	 * It will do this by first calculating number of columns
	 * 
	 * @param numberOfElements number of elements to compute for
	 * @param visibleWidth current width of visible area
	 */
	int computeHeightFromVisible(int numberOfElements, int visibleWidth);
	
	/**
	 * Same as computeHeightFromVisible but compute 
	 * 
	 * @param numberOfElements number of elements to compute for
	 * @param numColunms number of columns in gallery
	 * 
	 * @return computed height
	 */
	
	int computeHeightFromNumColumns(int numberOfElements, int numColunms);

	/**
	 * Get height of a single element, ought
	 * to be specific or approximate height + row spacing
	 * (rowspacing / 2) before and after element
	 */
	
	int getHeightOfOneElement();
}
