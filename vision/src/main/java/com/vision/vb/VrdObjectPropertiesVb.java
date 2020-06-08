package com.vision.vb;

public class VrdObjectPropertiesVb  extends CommonVb {

	private String widgetOperation="";
	private String vrdObjectID = "";
	private String VrdObjectName = "";
	private String objPaletteID = "";
	private String objPaletteDesc = "";
	private String htmlTagProperty="";
	private int    objPaletteStatusNt = 1;
	private int    objPaletteStatus = 0;
	private String radiantColor = "false";
	private String objPaletteIconLink = "";
	

	public String getWidgetOperation() {
		return widgetOperation;
	}
	public void setWidgetOperation(String widgetOperation) {
		this.widgetOperation = widgetOperation;
	}
	public int getObjPaletteStatusNt() {
		return objPaletteStatusNt;
	}
	public void setObjPaletteStatusNt(int objPaletteStatusNt) {
		this.objPaletteStatusNt = objPaletteStatusNt;
	}
	public int getObjPaletteStatus() {
		return objPaletteStatus;
	}
	public void setObjPaletteStatus(int objPaletteStatus) {
		this.objPaletteStatus = objPaletteStatus;
	}
	public String getRadiantColor() {
		return radiantColor;
	}
	public void setRadiantColor(String radiantColor) {
		this.radiantColor = radiantColor;
	}
	public String getObjPaletteIconLink() {
		return objPaletteIconLink;
	}
	public void setObjPaletteIconLink(String objPaletteIconLink) {
		this.objPaletteIconLink = objPaletteIconLink;
	}
	
	public String getVrdObjectID() {
		return vrdObjectID;
	}
	public void setVrdObjectID(String vrdObjectID) {
		this.vrdObjectID = vrdObjectID;
	}
	public String getVrdObjectName() {
		return VrdObjectName;
	}
	public void setVrdObjectName(String vrdObjectName) {
		VrdObjectName = vrdObjectName;
	}
	public String getObjPaletteID() {
		return objPaletteID;
	}
	public void setObjPaletteID(String objPaletteID) {
		this.objPaletteID = objPaletteID;
	}
	public String getObjPaletteDesc() {
		return objPaletteDesc;
	}
	public void setObjPaletteDesc(String objPaletteDesc) {
		this.objPaletteDesc = objPaletteDesc;
	}
	public String getHtmlTagProperty() {
		return htmlTagProperty;
	}
	public void setHtmlTagProperty(String htmlTagProperty) {
		this.htmlTagProperty = htmlTagProperty;
	}
	
}
