package com.vision.vb;

public class RsChartsVb extends CommonVb {
	
	private static final long serialVersionUID = 1L;
	private int chartTypeNt = 0;
	private int chartType = -1;
	private int rsdStatusNt = 0;
	private int rsdStatus = -1;
	private String categoryA = "";
	private String categoryB = "";
	private String categoryC = "";
	private String categoryD = "";
	private String mcFlag = "";
	private String chartTypeDesc = ""; 
	
	public int getChartTypeNt() {
		return chartTypeNt;
	}
	public void setChartTypeNt(int chartTypeNt) {
		this.chartTypeNt = chartTypeNt;
	}
	public int getChartType() {
		return chartType;
	}
	public void setChartType(int chartType) {
		this.chartType = chartType;
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
	public String getCategoryA() {
		return categoryA;
	}
	public void setCategoryA(String categoryA) {
		this.categoryA = categoryA;
	}
	public String getCategoryB() {
		return categoryB;
	}
	public void setCategoryB(String categoryB) {
		this.categoryB = categoryB;
	}
	public String getCategoryC() {
		return categoryC;
	}
	public void setCategoryC(String categoryC) {
		this.categoryC = categoryC;
	}
	public String getCategoryD() {
		return categoryD;
	}
	public void setCategoryD(String categoryD) {
		this.categoryD = categoryD;
	}
	public String getMcFlag() {
		return mcFlag;
	}
	public void setMcFlag(String mcFlag) {
		this.mcFlag = mcFlag;
	}
	public String getChartTypeDesc() {
		return chartTypeDesc;
	}
	public void setChartTypeDesc(String chartTypeDesc) {
		this.chartTypeDesc = chartTypeDesc;
	}
}