package com.vision.vb;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author DD
 *
 */
@Component
@Scope("prototype")
public class VisionUsersVb extends CommonVb {

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
	private int sbuCodeAt = 0;//SBU_CODE_AT
	private String sbuCode = "";//SBU_CODE
	private String productAttribute = "";//PRODUCT_ATTRIBUTE
	private String accountOfficer = "";//ACCOUNT_OFFICER
	private int	userStatusNt = 0;//USER_STATUS_NT
	private int	userStatus = -1;//USER_STATUS
	private String userStatusDate = "";//USER_STATUS_DATE
	private String lastSuccessfulLoginDate = "";//Non DB Field
	private String lastUnsuccessfulLoginDate = "";
	private String lastUnsuccessfulLoginAttempts = "";

	private String gcidAccess = "-1";
	private String logInPassWord = "";
	private boolean passwordChanged = false;
	
	private String fileNmae = "";
	private boolean proilePictureChange = false;
	
	private String staffId = "";
	private String staffName = "";
	private String stfcountry = "";//COUNTRY
	private String stfleBook = "";

	private String enableWidgets = "N";

	private String leBookDesc = "";//LE_BOOk_DESC
	private String pwdResetTime = "";
	private String passwordResetURL = "";
	private String errorMessage = "";
	private String status = "";
	private String emailStatus = "";
	
	private List<UserRestrictionVb> restrictionList = null;
	
	private String legalVehicleCleb = "";
	
	private String autoUpdateRestriction = "";
	private String prefLanguage = "";
	private String mainPrefLanguage = "";
	private int prefLanguageAt = 1080;
	private AdUserVb adUserVb;
	private String userMember = "";
	private String buildAccessRestriction ="";
	private String userGroupProfile ="";
	private String remoteHostName = "";
	
	public String getStaffId() {
		return staffId;
	}

	public void setStaffId(String staffId) {
		this.staffId = staffId;
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

	public String getPwdResetTime() {
		return pwdResetTime;
	}

	public void setPwdResetTime(String pwdResetTime) {
		this.pwdResetTime = pwdResetTime;
	}

	public String getPasswordResetURL() {
		return passwordResetURL;
	}

	public void setPasswordResetURL(String passwordResetURL) {
		this.passwordResetURL = passwordResetURL;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEmailStatus() {
		return emailStatus;
	}

	public void setEmailStatus(String emailStatus) {
		this.emailStatus = emailStatus;
	}

	public List<UserRestrictionVb> getRestrictionList() {
		return restrictionList;
	}

	public void setRestrictionList(List<UserRestrictionVb> restrictionList) {
		this.restrictionList = restrictionList;
	}

	public String getLegalVehicleCleb() {
		return legalVehicleCleb;
	}

	public void setLegalVehicleCleb(String legalVehicleCleb) {
		this.legalVehicleCleb = legalVehicleCleb;
	}

	public String getAutoUpdateRestriction() {
		return autoUpdateRestriction;
	}

	public void setAutoUpdateRestriction(String autoUpdateRestriction) {
		this.autoUpdateRestriction = autoUpdateRestriction;
	}

	public String getPrefLanguage() {
		return prefLanguage;
	}

	public void setPrefLanguage(String prefLanguage) {
		this.prefLanguage = prefLanguage;
	}

	public String getMainPrefLanguage() {
		return mainPrefLanguage;
	}

	public void setMainPrefLanguage(String mainPrefLanguage) {
		this.mainPrefLanguage = mainPrefLanguage;
	}

	public int getPrefLanguageAt() {
		return prefLanguageAt;
	}

	public void setPrefLanguageAt(int prefLanguageAt) {
		this.prefLanguageAt = prefLanguageAt;
	}

	public AdUserVb getAdUserVb() {
		return adUserVb;
	}

	public void setAdUserVb(AdUserVb adUserVb) {
		this.adUserVb = adUserVb;
	}

	public String getUserMember() {
		return userMember;
	}

	public void setUserMember(String userMember) {
		this.userMember = userMember;
	}

	public String getBuildAccessRestriction() {
		return buildAccessRestriction;
	}

	public void setBuildAccessRestriction(String buildAccessRestriction) {
		this.buildAccessRestriction = buildAccessRestriction;
	}

	public String getUserGroupProfile() {
		return userGroupProfile;
	}

	public void setUserGroupProfile(String userGroupProfile) {
		this.userGroupProfile = userGroupProfile;
	}

	public String getRemoteHostName() {
		return remoteHostName;
	}

	public void setRemoteHostName(String remoteHostName) {
		this.remoteHostName = remoteHostName;
	}

}