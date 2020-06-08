package com.vision.vb;

public class SmartSearchVb {

	private static final long serialVersionUID = -2010835009684844752L;
	
	private String object = "";
	private String criteria = "";
	private String value = "";
	private String joinType="";
	
	public String getJoinType() {
		return joinType;
	}
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	public String getCriteria() {
		return criteria;
	}
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}	
	

}