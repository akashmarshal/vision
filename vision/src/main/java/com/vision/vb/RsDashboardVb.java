package com.vision.vb;

public class RsDashboardVb extends CommonVb {


	private static final long serialVersionUID = 1L;
	private String reportId = "";
	private String groupReportId = "";
	private Long displayOrder = null;
	private String doubleWidthFlag = "";
	private int defaultChartTypeNt = 0;
	private int defaultChartType = -1;
	private String mcFlag = "";
	private String axisX = "";
	private String axisY = "";
	private String axisZ = "";
	private int rsdStatusNt = 0;
	private int rsdStatus = -1;
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	public String getGroupReportId() {
		return groupReportId;
	}
	public void setGroupReportId(String groupReportId) {
		this.groupReportId = groupReportId;
	}
	public Long getDisplayOrder() {
		return displayOrder;
	}
	public void setDisplayOrder(Long displayOrder) {
		this.displayOrder = displayOrder;
	}
	public String getDoubleWidthFlag() {
		return doubleWidthFlag;
	}
	public void setDoubleWidthFlag(String doubleWidthFlag) {
		this.doubleWidthFlag = doubleWidthFlag;
	}
	public int getDefaultChartTypeNt() {
		return defaultChartTypeNt;
	}
	public void setDefaultChartTypeNt(int defaultChartTypeNt) {
		this.defaultChartTypeNt = defaultChartTypeNt;
	}
	public int getDefaultChartType() {
		return defaultChartType;
	}
	public void setDefaultChartType(int defaultChartType) {
		this.defaultChartType = defaultChartType;
	}
	public String getMcFlag() {
		return mcFlag;
	}
	public void setMcFlag(String mcFlag) {
		this.mcFlag = mcFlag;
	}
	public String getAxisX() {
		return axisX;
	}
	public void setAxisX(String axisX) {
		this.axisX = axisX;
	}
	public String getAxisY() {
		return axisY;
	}
	public void setAxisY(String axisY) {
		this.axisY = axisY;
	}
	public String getAxisZ() {
		return axisZ;
	}
	public void setAxisZ(String axisZ) {
		this.axisZ = axisZ;
	}
	public int getRsdStatusNt() {
		return rsdStatusNt;
	}
	public void setRsdStatusNt(int rsdStatusNt) {
		this.rsdStatusNt = rsdStatusNt;
	}
	public int getRsdStatus() {
		return rsdStatus;
	}
	public void setRsdStatus(int rsdStatus) {
		this.rsdStatus = rsdStatus;
	}
}