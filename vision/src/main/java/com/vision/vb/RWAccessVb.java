package com.vision.vb;


public class RWAccessVb extends CommonVb{

	private static final long serialVersionUID = 1L;
	private String reportCategory = "";
	private String reportCategoryAt = "-1";
	private String reportCategoryDesc = "";
	private String subCategory = "";
	private String subCategoryAt = "-1";
	private String reportId = "";
	private int rwStatusNt = 0;
	private int rwStatus = -1;
	private int	userGroupAt = 0;//USER_GROUP_AT
	private String userGroup = "";//USER_GROUP
	private int	userProfileAt =  0;//USER_PROFILE_AT
	private String userProfile = "";//USER_PROFILE
	
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
	public String getReportCategory() {
		return reportCategory;
	}
	public void setReportCategory(String reportCategory) {
		this.reportCategory = reportCategory;
	}
	public String getReportCategoryAt() {
		return reportCategoryAt;
	}
	public void setReportCategoryAt(String reportCategoryAt) {
		this.reportCategoryAt = reportCategoryAt;
	}
	public String getReportCategoryDesc() {
		return reportCategoryDesc;
	}
	public void setReportCategoryDesc(String reportCategoryDesc) {
		this.reportCategoryDesc = reportCategoryDesc;
	}
	public String getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}
	public String getSubCategoryAt() {
		return subCategoryAt;
	}
	public void setSubCategoryAt(String subCategoryAt) {
		this.subCategoryAt = subCategoryAt;
	}
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	public int getRwStatusNt() {
		return rwStatusNt;
	}
	public void setRwStatusNt(int rwStatusNt) {
		this.rwStatusNt = rwStatusNt;
	}
	public int getRwStatus() {
		return rwStatus;
	}
	public void setRwStatus(int rwStatus) {
		this.rwStatus = rwStatus;
	}
}
