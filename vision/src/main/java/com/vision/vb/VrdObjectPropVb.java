package com.vision.vb;

import org.springframework.stereotype.Component;

/**
 * @author DD
 *
 */
@Component
public class VrdObjectPropVb extends CommonVb{
	private String vrdObjectId = "";
	private String objTagId = "";
	private String htmlTagProperty = "";
	private int objTagStatusNt = 1;
	private int objTagStatus = 0;
	private String objTagDesc = "";
	private String objTagIconLink = "";
	private String radiantColor = "false";
	
	public String getVrdObjectId() {
		return vrdObjectId;
	}
	public void setVrdObjectId(String vrdObjectId) {
		this.vrdObjectId = vrdObjectId;
	}
	public String getObjTagId() {
		return objTagId;
	}
	public void setObjTagId(String objTagId) {
		this.objTagId = objTagId;
	}
	public String getHtmlTagProperty() {
		return htmlTagProperty;
	}
	public void setHtmlTagProperty(String htmlTagProperty) {
		this.htmlTagProperty = htmlTagProperty;
	}
	public int getObjTagStatusNt() {
		return objTagStatusNt;
	}
	public void setObjTagStatusNt(int objTagStatusNt) {
		this.objTagStatusNt = objTagStatusNt;
	}
	public int getObjTagStatus() {
		return objTagStatus;
	}
	public void setObjTagStatus(int objTagStatus) {
		this.objTagStatus = objTagStatus;
	}
	public String getObjTagDesc() {
		return objTagDesc;
	}
	public void setObjTagDesc(String objTagDesc) {
		this.objTagDesc = objTagDesc;
	}
	public String getObjTagIconLink() {
		return objTagIconLink;
	}
	public void setObjTagIconLink(String objTagIconLink) {
		this.objTagIconLink = objTagIconLink;
	}
	public String getRadiantColor() {
		return radiantColor;
	}
	public void setRadiantColor(String radiantColor) {
		this.radiantColor = radiantColor;
	}
}
