
/**
 * Class that maintains gallery sizes
 * 
 */

function GallerySizes(config) {

	this.config = config;

	if (typeof config.columnSpacing === 'undefined') {
		this.columnSpacing = 20;
	}
	else {
		this.columnSpacing = config.columnSpacing;
	}
	
	if (typeof config.rowSpacing === 'undefined') {
		this.rowSpacing = 20;
	}
	else {
		this.rowSpacing = config.rowSpacing;
	}

	if (typeof config.width !== 'undefined') {
		this.widthMode = new GalleryModeWidthSpecific();
	}
	else if (typeof config.widthHint !== 'undefined') {
		this.widthMode = new GalleryModeWidthHint();
	}
	else {
		throw "Neither width nor width hint specified in config, specify one of them";
	}

	if (typeof config.height !== 'undefined') {
		this.heightMode = new GalleryModeHeightSpecific();
	}
	else if (typeof config.heightHint !== 'undefined') {
		this.heightMode = new GalleryModeHeightHint();
	}
	else {
		throw "Neither height nor height hint specified in config, specify one of them";
	}

	this.computeNumColumns = function(visibleWidth) {
		return this.widthMode.computeNumColumns(this.config, this.columnSpacing, visibleWidth)	
	}
	
	this.getColumnSpacing = function() {
		return this.columnSpacing;
	}
	
	this.getRowSpacing = function() {
		return this.rowSpacing;
	}
	
	this.getHeightOfOneElement = function() {
		var heightOfOneElement = typeof this.config.widthHint !== 'undefined'
			? this.config.widthHint
			: this.config.width;

		heightOfOneElement += this.getRowSpacing();

		return heightOfOneElement;
	}

	this.computeHeightFromVisible = function(numberOfElements, visibleWidth) {
		return this.computeHeightFromNumColumns(
				numberOfElements,
				this.computeNumColumns(visibleWidth));
	}
		
	this.computeHeightFromNumColumns = function(numberOfElements, numColumns) {
		return this.heightMode.computeHeight(
				this.config,
				this.getRowSpacing(),
				numColumns,
				numberOfElements);
	}

	this.getSpecificWidthOrNull = function() {
		return typeof this.config.width !== 'undefined'
			? itemWidth = this.config.width
			: null;
	}
	
	this.getSpecificHeightOrNull = function() {
		return typeof this.config.height !== 'undefined'
			? itemHeight = this.config.height
			: null;
	}
}
