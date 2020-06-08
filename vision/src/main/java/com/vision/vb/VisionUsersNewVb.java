
package com.vision.vb;

import java.sql.Blob;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class VisionUsersNewVb extends CommonVb {

	private static final long serialVersionUID = -6687470125464332861L;
	
	private int	visionId =  0;//VISION_ID - Key Field
	private String userName = "";//USER_NAME
	private String userLoginId = "";//USER_LOGIN_ID - Key Field
	private String userEmailId = "";//USER_EMAIL_ID
	private String lastActivityDate = "";//LAST_ACTIVITY_DATE
	private int	userGroupAt = 0;//USER_GROUP_AT
	private String userGroup = "";//USER_GROUP
	private int	userProfileAt =  0;//USER_PROFILE_AT
	private String userProfile = "";//USER_PROFILE
	private String updateRestriction = "";//UPDATE_RESTRICTION
	private String legalVehicle = "";//LEGAL_VEHICLE
	private String legalVehicleDesc = "";//LEGAL_VEHICLE_DESC
	private String country = "";//COUNTRY
	private String leBook = "";//LE_BOOK
	private String regionProvince = "";//REGION_PROVINCE
	private String businessGroup = "";//BUSINESS_GROUP
	private String productSuperGroup = "";//PRODUCT_SUPER_GROUP
	private String oucAttribute = "";//OUC_ATTRIBUTE
	private String oucAttributeDesc = "";//OUC_ATTRIBUTE Description
	private int sbuCodeAt = 0;//SBU_CODE_AT
	private String sbuCode = "";//SBU_CODE
	private String productAttribute = "";//PRODUCT_ATTRIBUTE
	private String productAttributeDesc = "";//PRODUCT_ATTRIBUTE
	private String accountOfficer = "";//ACCOUNT_OFFICER
	private String aoName = "";//ACCOUNT_OFFICER
	private int	userStatusNt = 1;//USER_STATUS_NT
	private int	userStatus = -1;//USER_STATUS
	private String userStatusDate = "";//USER_STATUS_DATE
	private String lastSuccessfulLoginDate = "";//Non DB Field
	private String lastUnsuccessfulLoginDate = "";
	private String lastUnsuccessfulLoginAttempts = "";
	private String multipleLeghalVehicle = "";//multipleLeghalVehicle
	private String multipleLeBook = "";//multipleLeBook
	private List<VisionUsersNewVb> children;
	private String oucAttributeLevel = "";
	private String oucAttributeLevelDesc = "";
	private String productAttributeLevel = "";
	private String productAttributeLevelDesc = "";
	private String bankGroup = "";
	private String bankGroupDesc = "";
	private String parentSbu = "";
	private String parentSbuDesc = "";
	private String visionSbu = "";
	private String visionSbuDesc = "";
	private String userGroupProfile = "";//USER_GROUP PROFILE
	private String userGroupProfileDesc = "";//USER_GROUP PROFILE DESC
	private String autoGroupProfile = "";
	private String autoGroupProfileDesc = "";
	private String linkClebStaffId = "";
	private Blob userPhoto;
	private String allowADProfileFlag = "N";
	private String allowAutoProfileFlag = "N";
	private String countryLeb = "";
	private String countryLebAo = "";
	private String countryLebStaff = "";
	private String autoUpdateRestriction = "N";
	private String autoLegalVehicle = "";
	private String autoCountryLeb = "";
	private String autoOucAttribute = "";
	private int autoSbuCodeAt = 0;
	private String autoSbuCode = "";
	private String autoCountryLebAo = "";
	private String autoProductAttribute = "";
	private String autoCountryLebStaff = "";

	private String gcidAccess = "N";
	private String logInPassWord = "";
	private boolean passwordChanged = false;
	
	private String fileNmae = "";
	private boolean proilePictureChange = false;
	
	private String staffId = "";
	private String staffName = "";
	private String stfcountry = "";//COUNTRY
	private String stfleBook = "";

	private String enableWidgets = "N";
	private String remoteHostName = "";
	private String loginStatus = "";
	private String comments = "";
	private String ipAddress = "";
	private String macAddress = "";
	private AdUserVb adUserVb;
	private String userGroupDesc =""; //USER_GROUP_DESC
	
	private String prefLanguage = "en_US";
	private int prefLanguageAt = 1080; 
	private String mainPrefLanguage = "";
	
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	private String leBookDesc = "";//LE_BOOk_DESC

	public String getStaffId() {
		return staffId;
	}
	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public void setStaffId(String staffId) {
		this.staffId = staffId;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getLoginStatus() {
		return loginStatus;
	}
	public void setLoginStatus(String loginStatus) {
		this.loginStatus = loginStatus;
	}

	public String getStaffName() {
		return staffName;
	}

	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}

	public String getStfcountry() {
		return stfcountry;
	}

	public void setStfcountry(String stfcountry) {
		this.stfcountry = stfcountry;
	}

	public String getStfleBook() {
		return stfleBook;
	}

	public void setStfleBook(String stfleBook) {
		this.stfleBook = stfleBook;
	}

	public boolean isProilePictureChange() {
		return proilePictureChange;
	}

	public void setProilePictureChange(boolean proilePictureChange) {
		this.proilePictureChange = proilePictureChange;
	}

	public String getFileNmae() {
		return fileNmae;
	}

	public void setFileNmae(String fileNmae) {
		this.fileNmae = fileNmae;
	}
	private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
	
	public boolean isPasswordChanged() {
		return passwordChanged;
	}
	public void setPasswordChanged(boolean passwordChanged) {
		this.passwordChanged = passwordChanged;
	}
	public String getLogInPassWord() {
		return logInPassWord;
	}
	public void setLogInPassWord(String logInPassWord) {
		this.logInPassWord = logInPassWord;
	}
	public int getVisionId() {
		return visionId;
	}
	public void setVisionId(int maximId) {
		this.visionId = maximId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserLoginId() {
		return userLoginId;
	}
	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}
	public String getUserEmailId() {
		return userEmailId;
	}
	public void setUserEmailId(String userEmailId) {
		this.userEmailId = userEmailId;
	}
	public String getLastActivityDate() {
		return lastActivityDate;
	}
	public void setLastActivityDate(String lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
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
	public String getUpdateRestriction() {
		return updateRestriction;
	}
	public void setUpdateRestriction(String updateRestriction) {
		this.updateRestriction = updateRestriction;
	}
	public String getLegalVehicle() {
		return legalVehicle;
	}
	public void setLegalVehicle(String legalVehicle) {
		this.legalVehicle = legalVehicle;
	}
	public String getLegalVehicleDesc() {
		return legalVehicleDesc;
	}
	public void setLegalVehicleDesc(String legalVehicleDesc) {
		this.legalVehicleDesc = legalVehicleDesc;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getLeBook() {
		return leBook;
	}
	public void setLeBook(String leBook) {
		this.leBook = leBook;
	}
	public String getRegionProvince() {
		return regionProvince;
	}
	public void setRegionProvince(String regionProvince) {
		this.regionProvince = regionProvince;
	}
	public String getBusinessGroup() {
		return businessGroup;
	}
	public void setBusinessGroup(String businessGroup) {
		this.businessGroup = businessGroup;
	}
	public String getProductSuperGroup() {
		return productSuperGroup;
	}
	public void setProductSuperGroup(String productSuperGroup) {
		this.productSuperGroup = productSuperGroup;
	}
	public String getOucAttribute() {
		return oucAttribute;
	}
	public void setOucAttribute(String oucAttribute) {
		this.oucAttribute = oucAttribute;
	}
	public int getSbuCodeAt() {
		return sbuCodeAt;
	}
	public void setSbuCodeAt(int sbuCodeNt) {
		this.sbuCodeAt = sbuCodeNt;
	}
	public String getSbuCode() {
		return sbuCode;
	}
	public void setSbuCode(String sbuCode) {
		this.sbuCode = sbuCode;
	}
	public String getProductAttribute() {
		return productAttribute;
	}
	public void setProductAttribute(String productAttribute) {
		this.productAttribute = productAttribute;
	}
	public String getAccountOfficer() {
		return accountOfficer;
	}
	public void setAccountOfficer(String accountOfficer) {
		this.accountOfficer = accountOfficer;
	}
	public String getGcidAccess() {
		return gcidAccess;
	}
	public void setGcidAccess(String gcidAccess) {
		this.gcidAccess = gcidAccess;
	}
	public int getUserStatusNt() {
		return userStatusNt;
	}
	public void setUserStatusNt(int userStatusNt) {
		this.userStatusNt = userStatusNt;
	}
	public int getUserStatus() {
		return userStatus;
	}
	public void setUserStatus(int userStatus) {
		this.userStatus = userStatus;
	}
	public String getUserStatusDate() {
		return userStatusDate;
	}
	public void setUserStatusDate(String userStatusDate) {
		this.userStatusDate = userStatusDate;
	}
	public String getLastSuccessfulLoginDate() {
		return lastSuccessfulLoginDate;
	}
	public void setLastSuccessfulLoginDate(String lastSuccessfulLoginDate) {
		this.lastSuccessfulLoginDate = lastSuccessfulLoginDate;
	}
	public String getLastUnsuccessfulLoginDate() {
		return lastUnsuccessfulLoginDate;
	}
	public void setLastUnsuccessfulLoginDate(String lastUnsuccessfulLoginDate) {
		this.lastUnsuccessfulLoginDate = lastUnsuccessfulLoginDate;
	}
	public String getLastUnsuccessfulLoginAttempts() {
		return lastUnsuccessfulLoginAttempts;
	}
	public void setLastUnsuccessfulLoginAttempts(
			String lastUnsuccessfulLoginAttempts) {
		this.lastUnsuccessfulLoginAttempts = lastUnsuccessfulLoginAttempts;
	}

	public String getEnableWidgets() {
		return enableWidgets;
	}

	public void setEnableWidgets(String enableWidgets) {
		this.enableWidgets = enableWidgets;
	}

	public String getLeBookDesc() {
		return leBookDesc;
	}

	public void setLeBookDesc(String leBookDesc) {
		this.leBookDesc = leBookDesc;
	}

	public AdUserVb getAdUserVb() {
		return adUserVb;
	}

	public void setAdUserVb(AdUserVb adUserVb) {
		this.adUserVb = adUserVb;
	}

	public String getRemoteHostName() {
		return remoteHostName;
	}

	public void setRemoteHostName(String remoteHostName) {
		this.remoteHostName = remoteHostName;
	}

	public String getMultipleLeghalVehicle() {
		return multipleLeghalVehicle;
	}

	public void setMultipleLeghalVehicle(String multipleLeghalVehicle) {
		this.multipleLeghalVehicle = multipleLeghalVehicle;
	}

	public String getMultipleLeBook() {
		return multipleLeBook;
	}

	public void setMultipleLeBook(String multipleLeBook) {
		this.multipleLeBook = multipleLeBook;
	}

	public List<VisionUsersNewVb> getChildren() {
		return children;
	}

	public void setChildren(List<VisionUsersNewVb> children) {
		this.children = children;
	}

	public String getOucAttributeLevel() {
		return oucAttributeLevel;
	}

	public void setOucAttributeLevel(String oucAttributeLevel) {
		this.oucAttributeLevel = oucAttributeLevel;
	}

	public String getOucAttributeLevelDesc() {
		return oucAttributeLevelDesc;
	}

	public void setOucAttributeLevelDesc(String oucAttributeLevelDesc) {
		this.oucAttributeLevelDesc = oucAttributeLevelDesc;
	}

	public String getOucAttributeDesc() {
		return oucAttributeDesc;
	}

	public void setOucAttributeDesc(String oucAttributeDesc) {
		this.oucAttributeDesc = oucAttributeDesc;
	}

	public String getProductAttributeDesc() {
		return productAttributeDesc;
	}

	public void setProductAttributeDesc(String productAttributeDesc) {
		this.productAttributeDesc = productAttributeDesc;
	}

	public String getProductAttributeLevel() {
		return productAttributeLevel;
	}

	public void setProductAttributeLevel(String productAttributeLevel) {
		this.productAttributeLevel = productAttributeLevel;
	}

	public String getProductAttributeLevelDesc() {
		return productAttributeLevelDesc;
	}

	public void setProductAttributeLevelDesc(String productAttributeLevelDesc) {
		this.productAttributeLevelDesc = productAttributeLevelDesc;
	}

	public String getAoName() {
		return aoName;
	}

	public void setAoName(String aoName) {
		this.aoName = aoName;
	}

	public String getBankGroup() {
		return bankGroup;
	}

	public void setBankGroup(String bankGroup) {
		this.bankGroup = bankGroup;
	}

	public String getBankGroupDesc() {
		return bankGroupDesc;
	}

	public void setBankGroupDesc(String bankGroupDesc) {
		this.bankGroupDesc = bankGroupDesc;
	}

	public String getParentSbu() {
		return parentSbu;
	}

	public void setParentSbu(String parentSbu) {
		this.parentSbu = parentSbu;
	}

	public String getParentSbuDesc() {
		return parentSbuDesc;
	}

	public void setParentSbuDesc(String parentSbuDesc) {
		this.parentSbuDesc = parentSbuDesc;
	}

	public String getVisionSbu() {
		return visionSbu;
	}

	public void setVisionSbu(String visionSbu) {
		this.visionSbu = visionSbu;
	}

	public String getVisionSbuDesc() {
		return visionSbuDesc;
	}

	public void setVisionSbuDesc(String visionSbuDesc) {
		this.visionSbuDesc = visionSbuDesc;
	}

	public String getUserGroupProfile() {
		return userGroupProfile;
	}

	public void setUserGroupProfile(String userGroupProfile) {
		this.userGroupProfile = userGroupProfile;
	}

	public String getUserGroupProfileDesc() {
		return userGroupProfileDesc;
	}

	public void setUserGroupProfileDesc(String userGroupProfileDesc) {
		this.userGroupProfileDesc = userGroupProfileDesc;
	}

	public String getLinkClebStaffId() {
		return linkClebStaffId;
	}

	public void setLinkClebStaffId(String linkClebStaffId) {
		this.linkClebStaffId = linkClebStaffId;
	}

	public Blob getUserPhoto() {
		return userPhoto;
	}

	public void setUserPhoto(Blob userPhoto) {
		this.userPhoto = userPhoto;
	}

	public String getAllowADProfileFlag() {
		return allowADProfileFlag;
	}

	public void setAllowADProfileFlag(String allowADProfileFlag) {
		this.allowADProfileFlag = allowADProfileFlag;
	}

	public String getAllowAutoProfileFlag() {
		return allowAutoProfileFlag;
	}

	public void setAllowAutoProfileFlag(String allowAutoProfileFlag) {
		this.allowAutoProfileFlag = allowAutoProfileFlag;
	}

	public String getAutoGroupProfile() {
		return autoGroupProfile;
	}

	public void setAutoGroupProfile(String autoGroupProfile) {
		this.autoGroupProfile = autoGroupProfile;
	}

	public String getAutoGroupProfileDesc() {
		return autoGroupProfileDesc;
	}

	public void setAutoGroupProfileDesc(String autoGroupProfileDesc) {
		this.autoGroupProfileDesc = autoGroupProfileDesc;
	}

	public String getCountryLeb() {
		return countryLeb;
	}

	public void setCountryLeb(String countryLeb) {
		this.countryLeb = countryLeb;
	}

	public String getCountryLebAo() {
		return countryLebAo;
	}

	public void setCountryLebAo(String countryLebAo) {
		this.countryLebAo = countryLebAo;
	}

	public String getCountryLebStaff() {
		return countryLebStaff;
	}

	public void setCountryLebStaff(String countryLebStaff) {
		this.countryLebStaff = countryLebStaff;
	}

	public String getAutoUpdateRestriction() {
		return autoUpdateRestriction;
	}

	public void setAutoUpdateRestriction(String autoUpdateRestriction) {
		this.autoUpdateRestriction = autoUpdateRestriction;
	}

	public String getAutoLegalVehicle() {
		return autoLegalVehicle;
	}

	public void setAutoLegalVehicle(String autoLegalVehicle) {
		this.autoLegalVehicle = autoLegalVehicle;
	}

	public String getAutoCountryLeb() {
		return autoCountryLeb;
	}

	public void setAutoCountryLeb(String autoCountryLeb) {
		this.autoCountryLeb = autoCountryLeb;
	}

	public String getAutoOucAttribute() {
		return autoOucAttribute;
	}
	public void setAutoOucAttribute(String autoOucAttribute) {
		this.autoOucAttribute = autoOucAttribute;
	}
	public int getAutoSbuCodeAt() {
		return autoSbuCodeAt;
	}
	public void setAutoSbuCodeAt(int autoSbuCodeAt) {
		this.autoSbuCodeAt = autoSbuCodeAt;
	}
	public String getAutoSbuCode() {
		return autoSbuCode;
	}
	public void setAutoSbuCode(String autoSbuCode) {
		this.autoSbuCode = autoSbuCode;
	}
	public String getAutoCountryLebAo() {
		return autoCountryLebAo;
	}
	public void setAutoCountryLebAo(String autoCountryLebAo) {
		this.autoCountryLebAo = autoCountryLebAo;
	}
	public String getAutoProductAttribute() {
		return autoProductAttribute;
	}
	public void setAutoProductAttribute(String autoProductAttribute) {
		this.autoProductAttribute = autoProductAttribute;
	}
	public String getAutoCountryLebStaff() {
		return autoCountryLebStaff;
	}
	public void setAutoCountryLebStaff(String autoCountryLebStaff) {
		this.autoCountryLebStaff = autoCountryLebStaff;
	}
	public String getUserGroupDesc() {
		return userGroupDesc;
	}
	public void setUserGroupDesc(String userGroupDesc) {
		this.userGroupDesc = userGroupDesc;
	}
	public String getPrefLanguage() {
		return prefLanguage;
	}
	public void setPrefLanguage(String prefLanguage) {
		this.prefLanguage = prefLanguage;
	}
	public int getPrefLanguageAt() {
		return prefLanguageAt;
	}
	public void setPrefLanguageAt(int prefLanguageAt) {
		this.prefLanguageAt = prefLanguageAt;
	}
	public String getMainPrefLanguage() {
		return mainPrefLanguage;
	}
	public void setMainPrefLanguage(String mainPrefLanguage) {
		this.mainPrefLanguage = mainPrefLanguage;
	}
	
	
}