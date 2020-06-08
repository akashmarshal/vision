package com.vision.vb;

import java.util.List;

public class CatalogAccessVb extends CommonVb {
	private static final long serialVersionUID = 1L;
	
	private String catalogType = "";
	private int catalogStatusNt = 0;
	private int catalogStatus = -1;
	private int	userGroupAt = 0;//USER_GROUP_AT
	private String userGroup = "";//USER_GROUP
	private int	userProfileAt =  0;//USER_PROFILE_AT
	private String userProfile = "";//USER_PROFILE
	private int	CatalogID =  0;
	private String subCategoryDesc = "";
	private String reportIdDesc = "";
	private List<CatalogAccessVb> children;
	private int displayOrder = 1;
	
	
	public String getCatalogType() {
		return catalogType;
	}
	public void setCatalogType(String catalogType) {
		this.catalogType = catalogType;
	}
	public int getCatalogStatusNt() {
		return catalogStatusNt;
	}
	public void setCatalogStatusNt(int catalogStatusNt) {
		this.catalogStatusNt = catalogStatusNt;
	}
	public int getCatalogStatus() {
		return catalogStatus;
	}
	public void setCatalogStatus(int catalogStatus) {
		this.catalogStatus = catalogStatus;
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
	public String getSubCategoryDesc() {
		return subCategoryDesc;
	}
	public void setSubCategoryDesc(String subCategoryDesc) {
		this.subCategoryDesc = subCategoryDesc;
	}
	public String getReportIdDesc() {
		return reportIdDesc;
	}
	public void setReportIdDesc(String reportIdDesc) {
		this.reportIdDesc = reportIdDesc;
	}
	public List<CatalogAccessVb> getChildren() {
		return children;
	}
	public void setChildren(List<CatalogAccessVb> children) {
		this.children = children;
	}
	public int getDisplayOrder() {
		return displayOrder;
	}
	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
	public int getCatalogID() {
		return CatalogID;
	}
	public void setCatalogID(int catalogID) {
		CatalogID = catalogID;
	}
	
}
