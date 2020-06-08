/**
 * 
 */
package com.vision.authentication;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import com.vision.dao.AlphaSubTabDao;
import com.vision.dao.CommonDao;
import com.vision.dao.VisionUsersDao;
import com.vision.dao.VisionUsersNewStructureDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.AdUserVb;
import com.vision.vb.MenuVb;
import com.vision.vb.ProfileData;
import com.vision.vb.UserRestrictionVb;
import com.vision.vb.VisionUsersNewVb;
import com.vision.vb.VisionUsersVb;

/**
 * Created  by: Bala Satya Praksh.B
 * Modified by: Deepak S on 19 Apr 2019
 * Client : UBA
 */
@Component
public class AuthenticationBeanNewStructure implements ServletContextAware{

	public static Logger logger = Logger.getLogger(AuthenticationBean.class);
	@Autowired
	private VisionUsersNewStructureDao visionUsersDao;
	@Autowired
	private CommonDao commonDao;
	private ServletContext servletContext;
	@Autowired
	private AlphaSubTabDao alphaSubTabDao;
	
	public boolean processLogin(HttpServletRequest request, HttpServletResponse response){
		logger.error("Process Login !!!");
		HttpSession httpses = request.getSession();
		if(httpses == null || httpses.getAttribute("ntLoginDetails") == null){
			request.setAttribute("status", "LoginError");
			logger.error("Invalid user Login Id");
			return false;
		}
		
		String userId = (String)httpses.getAttribute("ntLoginDetails");
		VisionUsersVb lUser = new VisionUsersVb(); 
		try{
			if(ValidationUtil.isValid(userId) && (httpses.getAttribute("userDetails") == null || ((VisionUsersVb)httpses.getAttribute("userDetails")).getVisionId() == 0)){
				String strUserName = userId.indexOf("\\") >=0 ? userId.substring(userId.indexOf("\\"), userId.length()-1) : userId;
				VisionUsersVb visionUsersVb = new VisionUsersVb();
				visionUsersVb.setUserLoginId(strUserName.toUpperCase());
				visionUsersVb.setRecordIndicator(0);
				visionUsersVb.setUserStatus(0);
				List<VisionUsersVb> lUsers = visionUsersDao.getActiveUserByUserLoginIdNew(visionUsersVb);

				VisionLdapADDeatils visionLdapADDeatils = new VisionLdapADDeatils();
				AdUserVb adUserVb = null;
				try {
					adUserVb = visionLdapADDeatils.getSearchResult(strUserName, "sAMAccountName");
					
				} catch (NamingException e) {
//					e.printStackTrace();
					adUserVb = new AdUserVb();
				}
				if(lUsers == null || lUsers.isEmpty()){
					System.out.println("New user Creating ");
					String memberof = adUserVb.getMemberOf();
					memberof =memberof.replaceAll("DC=ubagroup,DC=com", "").replaceAll("OU=Groups", "").replaceAll("CN=", "").replaceAll("", "").replaceAll("OU=", "");
					adUserVb.setMemberOf(memberof);
				
					VisionUsersNewVb visionUsersNewVb = new VisionUsersNewVb();
					visionUsersNewVb.setUserLoginId(strUserName.toUpperCase());
					visionUsersNewVb.setRecordIndicator(0);
					visionUsersNewVb.setUserStatus(0);
					visionUsersNewVb.setUserEmailId(adUserVb.getMail());
					visionUsersNewVb.setUserName(adUserVb.getName());
					visionUsersNewVb.setStaffId(adUserVb.getDescription());
					visionUsersNewVb.setUserStatusNt(1);
					visionUsersNewVb.setUserStatus(0);
					visionUsersNewVb.setUpdateRestriction("N");
					visionUsersNewVb.setUserGroupAt(1);
					visionUsersNewVb.setUserProfileAt(2);
					visionUsersNewVb.setLinkClebStaffId(adUserVb.getC()+"-"+"01-"+adUserVb.getDescription());
					String defaulstUserGroup =  commonDao.findVisionVariableValue("DEFAULT_USER_GROUP");
					String defaulstUserPrfile =  commonDao.findVisionVariableValue("DEFAULT_USER_PROFILE");
					String defaultUsrGrpProfile = defaulstUserGroup+"-"+defaulstUserPrfile;  
					if(!ValidationUtil.isValid(defaulstUserGroup)){
						defaulstUserGroup = "DEFAULT";
					}
					if(!ValidationUtil.isValid(defaultUsrGrpProfile)){
						defaultUsrGrpProfile = "DEFAULT";
					}
					visionUsersNewVb.setUserGroup(defaulstUserGroup);
					visionUsersNewVb.setUserProfile(defaulstUserPrfile);
					visionUsersNewVb.setUserGroupProfile(defaultUsrGrpProfile);
					visionUsersNewVb.setGcidAccess("N");
					visionUsersNewVb.setLastActivityDate(visionUsersDao.getSystemDate());
					visionUsersNewVb.setUserStatusDate(visionUsersDao.getSystemDate());
					visionUsersNewVb.setMaker(9999);
					visionUsersNewVb.setVerifier(9999);
					visionUsersNewVb.setAutoUpdateRestriction("Y");
					visionUsersNewVb.setAllowAutoProfileFlag("Y");
					visionUsersDao.doInsertionNewUserInAppr(visionUsersNewVb);
					logger.info("!!!!Authentication Bean - The new user is created in the Vision id:"+visionUsersNewVb.getVisionId()+"!!!!");
					lUsers = visionUsersDao.getActiveUserByUserLoginIdNew(visionUsersVb);						
				}
				if(lUsers.size() > 1){
					request.setAttribute("status", "LoginError");
					logger.error("User does not exists or more than one user exists with same login id["+strUserName+"]");
					return false;
				}
				lUser = ((ArrayList<VisionUsersVb> )lUsers).get(0);
				lUser.setAdUserVb(adUserVb);
				lUser.setRemoteAddress(request.getRemoteAddr());
				lUser.setLastSuccessfulLoginDate(lUser.getLastActivityDate());
				visionUsersDao.updateActivityDateByUserLoginId(lUser);
				String countryLeb = adUserVb.getC()+"-"+"01";
				if(!ValidationUtil.isValid(adUserVb.getC())){
					countryLeb = "NG-01";
				}
				String legalVehicle = visionUsersDao.getLegalVehicleStf(countryLeb);
				String legalVehicleDesc = visionUsersDao.getLegalVehicleStfDesc(legalVehicle);
				//User Access Restriction
				if("Y".equalsIgnoreCase(lUser.getUpdateRestriction()) || "Y".equalsIgnoreCase(lUser.getAutoUpdateRestriction())){
					
					/* Update restriction - Start */
					List<UserRestrictionVb> restrictionList = visionUsersDao.getRestrictionTree();
					
					Iterator<UserRestrictionVb> restrictionItr = restrictionList.iterator();
					
					while(restrictionItr.hasNext()) {
						UserRestrictionVb restrictionVb = restrictionItr.next();
						restrictionVb.setRestrictionSql(visionUsersDao.getVisionDynamicHashVariable(restrictionVb.getMacrovarName()));
					}
					
					restrictionList = visionUsersDao.doUpdateRestrictionToUserObject(lUser, restrictionList);
					lUser.setRestrictionList(restrictionList);
					/* Update restriction - End */
					
				}
				
				httpses.setAttribute("DEFAULT_CTRY_LEBOOK", countryLeb);
				httpses.setAttribute("DEFAULT_LEGAL_VECHICLE", legalVehicle);
			    httpses.setAttribute("DEFAULT_LEB_DESCRIPTION", legalVehicleDesc);
			    
			    //Vision User Member Group 
			    String memberOf = lUser.getAdUserVb().getMemberOf();
			    memberOf =memberOf.replaceAll("DC=ubagroup,DC=com", "").replaceAll("OU=Groups", "").replaceAll("CN=", "").replaceAll("", "").replaceAll("OU=", "");
			    lUser.getAdUserVb().setMemberOf(memberOf.replaceAll("'", "''"));
			    memberOf = lUser.getAdUserVb().getMemberOf();
				String arrMem[] = memberOf.split(",");
				ArrayList<String> memlst = new ArrayList<String>();
				String member = "";
				if(arrMem !=null && arrMem.length > 0){
					StringBuffer str = new StringBuffer();
					int idx = 0;
					for(String st: arrMem){
						str.append("'");
						str.append(st);
						str.append("'");
						if(idx != arrMem.length-1)
							str.append(",");
						idx++;
					}
					member = str.toString().toUpperCase();
					lUser.setUserMember(member);
				}
			    
				//Build Access Restriction
				String buildAccessRestriction = "";
				ExceptionCode exceptionCode = new ExceptionCode();
				exceptionCode = (ExceptionCode)visionUsersDao.getBuildAccessRestriction(lUser);
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION){
					buildAccessRestriction = (String)exceptionCode.getResponse();
					lUser.setBuildAccessRestriction(buildAccessRestriction);
				}else{
					logger.error("Error on getting Build Access Restriction!!");
					return false;
				}
				
				//User Group Profile
				String userGroupProfile = "";
				exceptionCode = new ExceptionCode();
				exceptionCode = (ExceptionCode)visionUsersDao.getUserGroupProfile(lUser);
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION){
					userGroupProfile = (String)exceptionCode.getResponse();
					lUser.setUserGroupProfile(userGroupProfile);
				}else{
					logger.error("Error on getting User Profile Access Restriction!!");
					return false;
				}
				
				//User Login Audit Log
				InetAddress addr = InetAddress.getByName(request.getRemoteAddr());
				String hostName = addr.getHostName();
				if(ValidationUtil.isValid(hostName) && hostName.contains(".")){
					hostName = hostName.substring(0, hostName.indexOf("."));
				}
				lUser.setRemoteHostName(hostName);
				//User Menu
				ArrayList<Object> result = getMenuForUserNew(lUser);
				ArrayList allLanguageList =(ArrayList) getAlphaSubTabDao().findActiveAlphaSubTabsByAlphaTab(1080);
				/*ArrayList allLanguageList = new ArrayList();*/
				httpses.setAttribute("languageList", allLanguageList);
				httpses.setAttribute("userDetails", lUser);
				httpses.setAttribute("menuDetails", result);
			}else if(httpses.getAttribute("userDetails") != null && ((VisionUsersVb)httpses.getAttribute("userDetails")).getVisionId() != 0){
				VisionUsersVb lUserDetails = getUserDetails();
				visionUsersDao.updateActivityDateByUserLoginId(lUserDetails);
				httpses.setAttribute("userDetails", lUserDetails);
			}else{
				logger.error("Unusual case for login context");
				return false;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			logger.error("Exception in AuthenticationBean : " +  e.getMessage(),e);
			request.setAttribute("status", "LoginError");
			return false;
		}
		try {
			writeUserLoginAudit(request, userId , "SUCCF", "Login Success", lUser.getVisionId());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Returns the Profiles and Menu Items for the logged in user.
	 * @return ArrayList<Object>. Index 0 contains list of profiles of the top level menus and index 1 contains list of MenuVb.
	 */
	public ArrayList<Object> getMenuForUser(VisionUsersVb lCurrentUser){
		if(lCurrentUser == null){
			throw new RuntimeCustomException("Invalida session. Please reload the application.");
		}
		ArrayList<MenuVb> resultMenu = new ArrayList<MenuVb>();
		ArrayList<Object> result = new ArrayList<Object>();
		String memberOf = lCurrentUser.getAdUserVb().getMemberOf();
		try{
			List<ProfileData> profileDataList = commonDao.getTopLevelMenu(lCurrentUser.getVisionId());
			if(profileDataList != null && !profileDataList.isEmpty()){
				for(ProfileData profileData:profileDataList){
					MenuVb lMenuVb = new MenuVb();
					lMenuVb.setMenuName(profileData.getMenuItem());
					lMenuVb.setMenuGroup(profileData.getMenuGroup());
					lMenuVb.setRecordIndicator(0);
					lMenuVb.setMenuStatus(0);
					ArrayList<MenuVb> childMenu = commonDao.getSubMenuItemsForMenuGroup(profileData.getMenuGroup(), profileData.getExcludeMenuProgramList());
					ArrayList<MenuVb> resultChilds = new ArrayList<MenuVb>();
					if(childMenu != null && !childMenu.isEmpty()){
						while(childMenu.size() > 0){
							MenuVb tmpMenuVb = childMenu.get(0);
							childMenu.remove(tmpMenuVb);
							if("N".equalsIgnoreCase(tmpMenuVb.getSeparator()))
								tmpMenuVb.setChildren(getAllChilds(tmpMenuVb,childMenu));
							resultChilds.add(tmpMenuVb);
						}
					}
					lMenuVb.setChildren(resultChilds);
					resultMenu.add(lMenuVb);
				}
			}
			result.add(profileDataList);
			result.add(resultMenu);
		}
		catch(Exception e){
			logger.error("Exception in getting menu for the user["+lCurrentUser.getVisionId()+"]. : " +  e.getMessage(),e);
			throw new RuntimeCustomException("Failed to retrieve menu for your profile. Please contact System Admin.");
		}
		return result;
	}
	public ArrayList<ProfileData>  getTopMenus(List<ProfileData> profileDataList){
		ArrayList<ProfileData> topMenu = new ArrayList<>();
		ProfileData  profileData  =profileDataList.get(0);
		topMenu.add(profileData);
		for(ProfileData profileDataNew:profileDataList){
			if(profileData.getMenuGroup() != profileDataNew.getMenuGroup()){
				profileData = new ProfileData();
				profileData.setMenuGroup(profileDataNew.getMenuGroup());
				profileData.setMenuItem(profileDataNew.getMenuItem());
				profileData.setProfileAdd(profileDataNew.getProfileAdd());
				profileData.setProfileModify(profileDataNew.getProfileModify());
				profileData.setProfileDelete(profileDataNew.getProfileDelete());
				profileData.setProfileInquiry(profileDataNew.getProfileInquiry());
				profileData.setProfileVerification(profileDataNew.getProfileVerification());
				profileData.setProfileUpload(profileDataNew.getProfileUpload());
				profileData.setMenuIcon(profileDataNew.getMenuIcon());
				profileData.setMaker(profileDataNew.getMaker());
				profileData.setVerifier(profileDataNew.getVerifier());
				
				topMenu.add(profileData);
			}
		}
		return topMenu;
	}
	public ArrayList<MenuVb>  setMenus(List<ProfileData> profileDataList, int menuGroup){
		ArrayList<MenuVb> childMenu = new ArrayList<>();
		for(ProfileData profileData:profileDataList){
			if(menuGroup == profileData.getMenuGroup()){
				MenuVb menuVb = new MenuVb();
				menuVb.setMenuProgram(profileData.getMakerName());
				menuVb.setMenuName(profileData.getVerifierName());
				menuVb.setParentSequence((int) profileData.getMaker());
				menuVb.setMenuSequence((int) profileData. getVerifier());
				menuVb.setMenuGroup(profileData.getMenuGroup());
				menuVb.setSeparator(profileData.getDateCreation());
				childMenu.add(menuVb);
			}
		}
		return childMenu;
	}
	public ArrayList<Object> getMenuForUserNew(VisionUsersVb lCurrentUser){
		if(lCurrentUser == null){
			throw new RuntimeCustomException("Invalida session. Please reload the application.");
		}
		ArrayList<MenuVb> resultMenu = new ArrayList<MenuVb>();
		ArrayList<Object> result = new ArrayList<Object>();
		String memberOf = lCurrentUser.getAdUserVb().getMemberOf();
		if(!ValidationUtil.isValid(memberOf)){
			memberOf="UBAGROUP=CN";	
		}
		try{
			List<ProfileData> profileMenuDataList = commonDao.getTopLevelMenu(lCurrentUser.getVisionId(), memberOf);
			List<ProfileData> profileDataList =  new ArrayList<>();
			if(profileMenuDataList != null && !profileMenuDataList.isEmpty()){
				profileDataList = getTopMenus(profileMenuDataList);
				for(ProfileData profileData:profileDataList){
					MenuVb lMenuVb = new MenuVb();
					lMenuVb.setMenuName(profileData.getMenuItem());
					lMenuVb.setMenuGroup(profileData.getMenuGroup());
					lMenuVb.setParentSequence((int) profileData.getMaker());
					lMenuVb.setMenuSequence((int) profileData.getVerifier());
					lMenuVb.setRecordIndicator(0);
					lMenuVb.setMenuStatus(0);
					ArrayList<MenuVb> childMenu = setMenus(profileMenuDataList, profileData.getMenuGroup());
					ArrayList<MenuVb> resultChilds = new ArrayList<MenuVb>();
					if(childMenu != null && !childMenu.isEmpty()){
						while(childMenu.size() > 0){
							MenuVb tmpMenuVb = childMenu.get(0);
							childMenu.remove(tmpMenuVb);
							if("N".equalsIgnoreCase(tmpMenuVb.getSeparator()))
								tmpMenuVb.setChildren(getAllChilds(tmpMenuVb,childMenu));
							resultChilds.add(tmpMenuVb);
						}
					}
					lMenuVb.setChildren(resultChilds);
					resultMenu.add(lMenuVb);
				}
			}
			result.add(profileDataList);
			result.add(resultMenu);
		}
		catch(Exception e){
			logger.error("Exception in getting menu for the user["+lCurrentUser.getVisionId()+"]. : " +  e.getMessage(),e);
			throw new RuntimeCustomException("Failed to retrieve menu for your profile. Please contact System Admin.");
		}
		return result;
	}
	public boolean isFileExists(String currentScreenName){
		try{
			String realPath = servletContext.getRealPath(currentScreenName+"Help.pdf");
			File lFile = new File(realPath);
			return lFile.exists();
		}catch (Exception exception) {
			return false;
		}	
	}
	private ArrayList<MenuVb> getAllChilds(MenuVb tmpMenuVb, List<MenuVb> pChildMenu) {
		ArrayList<MenuVb> childMenus  = new ArrayList<MenuVb>();
		while(pChildMenu.size() >0){
			MenuVb menuVb = pChildMenu.get(0);
			if(tmpMenuVb.getMenuSequence() == menuVb.getParentSequence() &&  tmpMenuVb.getMenuSequence() != menuVb.getMenuSequence()){
				childMenus.add(menuVb);
				pChildMenu.remove(menuVb);
				menuVb.setChildren(getAllChilds(menuVb,pChildMenu));
			}else{
				break;
			}
		}
		return childMenus;
	}
	public void updateUnsuccessfulLoginAttempts(String userId) {
		try{
			visionUsersDao.updateUnsuccessfulLoginAttempts(userId);
		}catch(Exception e){
			logger.error("Exception in AuthenticationBean : " +  e.getMessage(),e);
		}
	}
	
	public String getUnsuccessfulLoginAttempts(String userId) {
		
		try{
		    if(!ValidationUtil.isValid(userId)){
		    	return "0";
		    }
			String legalVehicle =visionUsersDao.getUnsuccessfulLoginAttempts(userId);
			return legalVehicle;
		}catch(Exception e){
			logger.error("Exception in getting the Login attempts : " +  e.getMessage(),e);
			return null;
		}
	}
	public String getMaxLoginAttempts() {
		try{
			String MaxLogin =  getCommonDao().findVisionVariableValue("MAX_LOGIN");
			if(!ValidationUtil.isValid(MaxLogin))
				MaxLogin = "3";
			return MaxLogin;
		}catch(Exception e){
			logger.error("Exception in getting the Max Login attempts : " +  e.getMessage(),e);
			return null;
		}
	}
	public String getUserStatus(String userId) {
		try{
			if(!ValidationUtil.isValid(userId)){
				return "0";
			}
			String userStatus =visionUsersDao.getNonActiveUsers(userId);
			return userStatus;
		}catch(Exception e){
			logger.error("Exception in getting the User Status : " +  e.getMessage(),e);
			return null;
		}
	}
	public void writeUserLoginAudit(HttpServletRequest request,String userLoginId,String status,String comments,int visionId) throws UnknownHostException{
		try {
			VisionUsersNewVb vObject = new VisionUsersNewVb();
			vObject.setUserLoginId(userLoginId);
			String ipAddress = request.getRemoteAddr();
			if("0:0:0:0:0:0:0:1".equalsIgnoreCase(ipAddress)){
				ipAddress = InetAddress.getLocalHost().getHostAddress() ;
			}
			logger.info("IP Address:"+ipAddress);
			logger.info("Temp Host :"+request.getRemoteHost());
			InetAddress inetAddress = InetAddress.getByName(ipAddress);
			vObject.setIpAddress(ipAddress);
			vObject.setRemoteHostName(inetAddress.getHostName());
			logger.info("Host Name:"+inetAddress.getHostName());
			vObject.setLoginStatus(status);
			vObject.setComments(comments);
			vObject.setVisionId(visionId);
			String str = ""; 
			String macAddress = ""; 
			vObject.setMacAddress(getCommonDao().getMacAddress(ipAddress));
			logger.info("MAC Address :"+vObject.getMacAddress());
			visionUsersDao.insertUserLoginAudit(vObject);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public VisionUsersVb getUserDetails(){
		return SessionContextHolder.getContext();
	}
	public VisionUsersNewStructureDao getVisionUsersDao() {
		return visionUsersDao;
	}
	public void setVisionUsersDao(VisionUsersNewStructureDao visionUsersDao) {
		this.visionUsersDao = visionUsersDao;
	}
	public CommonDao getCommonDao() {
		return commonDao;
	}
	public void setCommonDao(CommonDao commonDao) {
		this.commonDao = commonDao;
	}
	public void setServletContext(ServletContext arg0) {
		servletContext = arg0;
	}
	public AlphaSubTabDao getAlphaSubTabDao() {
		return alphaSubTabDao;
	}
	public void setAlphaSubTabDao(AlphaSubTabDao alphaSubTabDao) {
		this.alphaSubTabDao = alphaSubTabDao;
	}
}
