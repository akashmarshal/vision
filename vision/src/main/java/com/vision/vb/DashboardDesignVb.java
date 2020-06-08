package com.vision.vb;

import java.util.List;

public class DashboardDesignVb extends CommonVb {

	private String dashboardId = "";
	private String dashboardName="";
	private String dashboardDesc = "";
	private String dashboardDesign = "";
	private String dashboardDesignJson = "";
	private String filterContext = "";
	private String filterContextJson = "";
	private Integer rsdStatusNt=2000;
	private Integer rsdStatus=-1;
	private String privateFlag="";
	private String writeFlag="";
	private int	userGroupAt = 1;
	private String userGroup = "";
	private int	userProfileAt = 2;
	private String userProfile = "";
	private List<WidgetDesignVb> children = null;
	
	public String getDashboardId() {
		return dashboardId;
	}
	public void setDashboardId(String dashboardId) {
		this.dashboardId = dashboardId;
	}
	public String getDashboardName() {
		return dashboardName;
	}
	public void setDashboardName(String dashboardName) {
		this.dashboardName = dashboardName;
	}
	public String getDashboardDesc() {
		return dashboardDesc;
	}
	public void setDashboardDesc(String dashboardDesc) {
		this.dashboardDesc = dashboardDesc;
	}
	public String getDashboardDesign() {
		return dashboardDesign;
	}
	public void setDashboardDesign(String dashboardDesign) {
		this.dashboardDesign = dashboardDesign;
	}
	public String getDashboardDesignJson() {
		return dashboardDesignJson;
	}
	public void setDashboardDesignJson(String dashboardDesignJson) {
		this.dashboardDesignJson = dashboardDesignJson;
	}
	public String getFilterContext() {
		return filterContext;
	}
	public void setFilterContext(String filterContext) {
		this.filterContext = filterContext;
	}
	public String getFilterContextJson() {
		return filterContextJson;
	}
	public void setFilterContextJson(String filterContextJson) {
		this.filterContextJson = filterContextJson;
	}
	public Integer getRsdStatusNt() {
		return rsdStatusNt;
	}
	public void setRsdStatusNt(Integer rsdStatusNt) {
		this.rsdStatusNt = rsdStatusNt;
	}
	public Integer getRsdStatus() {
		return rsdStatus;
	}
	public void setRsdStatus(Integer rsdStatus) {
		this.rsdStatus = rsdStatus;
	}
	public List<WidgetDesignVb> getChildren() {
		return children;
	}
	public void setChildren(List<WidgetDesignVb> children) {
		this.children = children;
	}
	public String getPrivateFlag() {
		return privateFlag;
	}
	public void setPrivateFlag(String privateFlag) {
		this.privateFlag = privateFlag;
	}
	public String getWriteFlag() {
		return writeFlag;
	}
	public void setWriteFlag(String writeFlag) {
		this.writeFlag = writeFlag;
	}
	public int getUserGroupAt() {
		return userGroupAt;
	}
	public void setUserGroupAt(int userGroupAt) {
		this.userGroupAt = userGroupAt;
	}
	public String getUserGroup() {
		return userGroup;
	}
	public void setUserGroup(String userGroup) {
		this.userGroup = userGroup;
	}
	public int getUserProfileAt() {
		return userProfileAt;
	}
	public void setUserProfileAt(int userProfileAt) {
		this.userProfileAt = userProfileAt;
	}
	public String getUserProfile() {
		return userProfile;
	}
	public void setUserProfile(String userProfile) {
		this.userProfile = userProfile;
	}
	
	
}
