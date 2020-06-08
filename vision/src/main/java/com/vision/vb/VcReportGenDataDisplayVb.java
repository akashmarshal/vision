/**
 * @author DD
 */

package com.vision.vb;

public class VcReportGenDataDisplayVb {
	private String dataToDisplay="";
	private int rowId;
	private int colId;
	private int rowSpan;
	private int colSpan;
	private String caption="";
	private String colType="";
	private String formatType="";
	private String drillThrough="";
	private String groupBy="";
	private String styleForData="";
	private boolean isDrillDown = false;
	private String drillDownProp = "";
	private boolean isScalingFactor = false;
	private String scalingProp = "";
	
	public String getDataToDisplay() {
		return dataToDisplay;
	}

	public void setDataToDisplay(String dataToDisplay) {
		this.dataToDisplay = dataToDisplay;
	}

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	public int getColId() {
		return colId;
	}

	public void setColId(int colId) {
		this.colId = colId;
	}

	public int getRowSpan() {
		return rowSpan;
	}

	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	public int getColSpan() {
		return colSpan;
	}

	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getColType() {
		return colType;
	}

	public void setColType(String colType) {
		this.colType = colType;
	}

	public String getFormatType() {
		return formatType;
	}

	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}

	public String getDrillThrough() {
		return drillThrough;
	}

	public void setDrillThrough(String drillThrough) {
		this.drillThrough = drillThrough;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public String getStyleForData() {
		return styleForData;
	}

	public void setStyleForData(String styleForData) {
		this.styleForData = styleForData;
	}

	public boolean isDrillDown() {
		return isDrillDown;
	}

	public void setDrillDown(boolean isDrillDown) {
		this.isDrillDown = isDrillDown;
	}

	public String getDrillDownProp() {
		return drillDownProp;
	}

	public void setDrillDownProp(String drillDownProp) {
		this.drillDownProp = drillDownProp;
	}

	public boolean isScalingFactor() {
		return isScalingFactor;
	}

	public void setScalingFactor(boolean isScalingFactor) {
		this.isScalingFactor = isScalingFactor;
	}

	public String getScalingProp() {
		return scalingProp;
	}

	public void setScalingProp(String scalingProp) {
		this.scalingProp = scalingProp;
	}

}