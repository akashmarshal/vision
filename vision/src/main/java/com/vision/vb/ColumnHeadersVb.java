package com.vision.vb;

import java.io.Serializable;

public class ColumnHeadersVb implements Serializable {

	private static final long serialVersionUID = 1L;
	private String reportId = "";
	private String sessionId = "";
	private int labelRowNum;
	private int labelColNum;
	private String caption = "";
	private String colType = "";
	private Long columnWidth = 10l;
	private int colSpanNum;
	private int rowSpanNum;
	private String dbColumnName = "";
	private int numericColumnNo = 0;
	private int rowspan = 0;
	private int colspan = 0;
	
	public int getColSpanNum() {
		return colSpanNum;
	}
	public void setColSpanNum(int colSpanNum) {
		this.colSpanNum = colSpanNum;
	}
	public int getRowSpanNum() {
		return rowSpanNum;
	}
	public void setRowSpanNum(int rowSpanNum) {
		this.rowSpanNum = rowSpanNum;
	}
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public int getLabelRowNum() {
		return labelRowNum;
	}
	public void setLabelRowNum(int labelRowNum) {
		this.labelRowNum = labelRowNum;
	}
	public int getLabelColNum() {
		return labelColNum;
	}
	public void setLabelColNum(int labelColNum) {
		this.labelColNum = labelColNum;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public Long getColumnWidth() {
		return columnWidth;
	}
	public void setColumnWidth(Long columnWidth) {
		this.columnWidth = columnWidth;
	}
	public String getColType() {
		return colType;
	}
	public void setColType(String colType) {
		this.colType = colType;
	} 
	public String getDbColumnName() {
		return dbColumnName;
	}
	public void setDbColumnName(String dbColumnName) {
		this.dbColumnName = dbColumnName;
	}
	public int getNumericColumnNo() {
		return numericColumnNo;
	}
	public void setNumericColumnNo(int numericColumnNo) {
		this.numericColumnNo = numericColumnNo;
	}
	public int getRowspan() {
		return rowspan;
	}
	public void setRowspan(int rowspan) {
		this.rowspan = rowspan;
	}
	public int getColspan() {
		return colspan;
	}
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}

}
