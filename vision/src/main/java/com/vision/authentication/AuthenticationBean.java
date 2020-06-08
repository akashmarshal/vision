/**
 * 
 */
package com.vision.authentication;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.vision.dao.CommonDao;
import com.vision.dao.VisionUsersDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.MenuVb;
import com.vision.vb.ProfileData;
import com.vision.vb.UserRestrictionVb;
import com.vision.vb.VisionUsersVb;

@Component
public class AuthenticationBean {

	public static Logger logger = LoggerFactory.getLogger(AuthenticationBean.class);
	@Autowired
	private VisionUsersDao visionUsersDao;
	@Autowired
	private CommonDao commonDao;
	private ServletContext servletContext;
	@Autowired
	private JavaMailSender mailSender;

	public boolean processLogin(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("***processLogin***");
		HttpSession httpses = request.getSession();
		if (httpses == null || httpses.getAttribute("ntLoginDetails") == null) {
			request.setAttribute("status", "LoginError");
			logger.error("Invalid user Login Id");
			return false;
		}
		
//		String visionAppToken = String.valueOf(httpses.getAttribute("visionUserToken"));		
		
		String userId = (String) httpses.getAttribute("ntLoginDetails");
		try {
			if (ValidationUtil.isValid(userId)) {
				String strUserName = userId.indexOf("\\") >= 0
						? userId.substring(userId.indexOf("\\"), userId.length() - 1)
						: userId;
				VisionUsersVb visionUsersVb = new VisionUsersVb();
				visionUsersVb.setUserLoginId(strUserName.toUpperCase());
				visionUsersVb.setRecordIndicator(0);
				visionUsersVb.setUserStatus(0);
				List<VisionUsersVb> lUsers = visionUsersDao.getActiveUserByUserLoginId(visionUsersVb);
				if (lUsers == null || lUsers.isEmpty() || lUsers.size() > 1) {
					request.setAttribute("status", "LoginError");
					logger.error("User does not exists or more than one user exists with same login id[" + strUserName
							+ "]");
					return false;
				}
				VisionUsersVb lUser = ((ArrayList<VisionUsersVb>) lUsers).get(0);
				lUser.setLastSuccessfulLoginDate(lUser.getLastActivityDate());
				visionUsersDao.updateActivityDateByUserLoginId(lUser);

				
				if ("Y".equalsIgnoreCase(lUser.getUpdateRestriction())) {
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
				
				
				ArrayList<Object> result = getMenuForUser(lUser);
				httpses.setAttribute("userDetails", lUser);
				httpses.setAttribute("menuDetails", result);

				/*COMMENTED FOR CBG BANK*/
				/* 31-Oct-2016 - Fetching Prompt Data based on User Login ID */
				/*List<VisionUsersVb> lUsersPromptData = visionUsersDao.getPromptDataByUserLoginId(visionUsersVb);
				if (lUsersPromptData == null || lUsersPromptData.isEmpty() || lUsersPromptData.size() > 1) {
					request.setAttribute("status", "LoginError");
					logger.error("Default prompt not exists[" + strUserName + "]");
					return false;
				}*/
		/*		VisionUsersVb lUsersPrmptData = ((ArrayList<VisionUsersVb>) lUsersPromptData).get(0);
				httpses.setAttribute("DEFAULT_CTRY_LEBOOK",
						lUsersPrmptData.getCountry() + "-" + lUsersPrmptData.getLeBook());
				httpses.setAttribute("DEFAULT_LEGAL_VECHICLE", lUsersPrmptData.getLegalVehicle());
				httpses.setAttribute("DEFAULT_OUC", lUsersPrmptData.getOucAttribute());
				httpses.setAttribute("DEFAULT_LEB_DESCRIPTION", lUsersPrmptData.getLeBookDesc());*/

			} else {
				logger.error("Unusual case for login context");
				return false;
			}
		} catch (Exception e) {
			logger.error("Exception in AuthenticationBean : " + e.getMessage(), e);
			request.setAttribute("status", "LoginError");
			return false;
		}
		return true;
	}

	/**
	 * Returns the Profiles and Menu Items for the logged in user.
	 * 
	 * @return ArrayList<Object>. Index 0 contains list of profiles of the top level
	 *         menus and index 1 contains list of MenuVb.
	 */
	public ArrayList<Object> getMenuForUser(VisionUsersVb lCurrentUser) {
		if (lCurrentUser == null) {
			throw new RuntimeCustomException("Invalida session. Please reload the application.");
		}
		ArrayList<MenuVb> resultMenu = new ArrayList<MenuVb>();
		ArrayList<Object> result = new ArrayList<Object>();
		try {
			List<ProfileData> topLvlMenuList = commonDao.getTopLevelMenu(lCurrentUser.getVisionId());
			if (topLvlMenuList != null && !topLvlMenuList.isEmpty()) {
				for (ProfileData profileData : topLvlMenuList) {
					MenuVb lMenuVb = new MenuVb();
					lMenuVb.setMenuName(profileData.getMenuItem());
					lMenuVb.setMenuGroup(profileData.getMenuGroup());
//					lMenuVb.setRecordIndicator(0);
					lMenuVb.setMenuStatus(0);
					ArrayList<MenuVb> childMenu = commonDao.getSubMenuItemsForMenuGroup(profileData.getMenuGroup());
					ArrayList<MenuVb> resultChilds = new ArrayList<MenuVb>();
					if (childMenu != null && !childMenu.isEmpty()) {
						while (childMenu.size() > 0) {
							MenuVb tmpMenuVb = childMenu.get(0);
							childMenu.remove(tmpMenuVb);
							if ("N".equalsIgnoreCase(tmpMenuVb.getSeparator()))
								tmpMenuVb.setChildren(getAllChilds(tmpMenuVb, childMenu));
							resultChilds.add(tmpMenuVb);
						}
					}
					lMenuVb.setChildren(resultChilds);
					resultMenu.add(lMenuVb);
				}
			}
			result.add(topLvlMenuList);
			result.add(resultMenu);
		} catch (Exception e) {
			logger.error(
					"Exception in getting menu for the user[" + lCurrentUser.getVisionId() + "]. : " + e.getMessage(),
					e);
			throw new RuntimeCustomException("Failed to retrieve menu for your profile. Please contact System Admin.");
		}
		return result;
	}

	public boolean isFileExists(String currentScreenName) {
		try {
			String realPath = servletContext.getRealPath(currentScreenName + "Help.pdf");
			File lFile = new File(realPath);
			return lFile.exists();
		} catch (Exception exception) {
			return false;
		}
	}

	private ArrayList<MenuVb> getAllChilds(MenuVb tmpMenuVb, List<MenuVb> pChildMenu) {
		ArrayList<MenuVb> childMenus = new ArrayList<MenuVb>();
		while (pChildMenu.size() > 0) {
			MenuVb menuVb = pChildMenu.get(0);
			if (tmpMenuVb.getMenuSequence() == menuVb.getParentSequence()
					&& tmpMenuVb.getMenuSequence() != menuVb.getMenuSequence()) {
				childMenus.add(menuVb);
				pChildMenu.remove(menuVb);
				menuVb.setChildren(getAllChilds(menuVb, pChildMenu));
			} else {
				break;
			}
		}
		return childMenus;
	}

	public void updateUnsuccessfulLoginAttempts(String userId) {
		try {
			visionUsersDao.updateUnsuccessfulLoginAttempts(userId);
		} catch (Exception e) {
			logger.error("Exception in AuthenticationBean : " + e.getMessage(), e);
		}
	}

	public String getUnsuccessfulLoginAttempts(String userId) {
		try {
			if (!ValidationUtil.isValid(userId)) {
				return "0";
			}
			String legalVehicle = visionUsersDao.getUnsuccessfulLoginAttempts(userId);
			return legalVehicle;
		} catch (Exception e) {
			logger.error("Exception in getting the Login attempts : " + e.getMessage(), e);
			return null;
		}
	}

	public String getMaxLoginAttempts() {
		try {
			String MaxLogin = getCommonDao().findVisionVariableValue("MAX_LOGIN");
			if (!ValidationUtil.isValid(MaxLogin))
				MaxLogin = "3";
			return MaxLogin;
		} catch (Exception e) {
			logger.error("Exception in getting the Max Login attempts : " + e.getMessage(), e);
			return null;
		}
	}

	public String getUserStatus(String userId) {
		try {
			if (!ValidationUtil.isValid(userId)) {
				return "0";
			}
			String userStatus = visionUsersDao.getNonActiveUsers(userId);
			return userStatus;
		} catch (Exception e) {
			logger.error("Exception in getting the User Status : " + e.getMessage(), e);
			return null;
		}
	}

	public String findVisionVariableValuePasswordURL() {
		try {
			String PasswordResetURL = getCommonDao().findVisionVariableValue("PASSWORD_RESETURL");
			return PasswordResetURL;
		} catch (Exception e) {
			logger.error("Exception in getting the Password Reset URL : " + e.getMessage(), e);
			return null;
		}
	}

	public String findVisionVariableValuePasswordResrtTime() {
		try {
			String PasswordResetTime = getCommonDao().findVisionVariableValue("Password_ResetTime");
			return PasswordResetTime;
		} catch (Exception e) {
			logger.error("Exception in getting the Password Reset Time : " + e.getMessage(), e);
			return null;
		}
	}

	public ExceptionCode callProcToPopulateForgotPasswordEmail(VisionUsersVb vObject, String resultForgotBy) {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			vObject = getVisionUsersDao().callProcToPopulateForgotPasswordEmail(vObject, resultForgotBy);
			exceptionCode.setErrorMsg(vObject.getErrorMessage());
			exceptionCode.setErrorCode(Integer.parseInt(vObject.getStatus()));
			return exceptionCode;
		} catch (RuntimeCustomException rex) {
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			logger.error(((vObject == null) ? "vObject is Null" : vObject.toString()));
			exceptionCode = rex.getCode();
			return exceptionCode;
		}
	}

	public ExceptionCode doSendEmail(VisionUsersVb vObject, String resultForgotBy) {
		ExceptionCode exceptionCode = null;
		try {
			MimeMessage msg = prepareEmail(vObject, resultForgotBy);
			getMailSender().send(msg);
			exceptionCode = CommonUtils.getResultObject("Your Email", Constants.SUCCESSFUL_OPERATION, "hasBeenSent",
					"");
		} catch (MailAuthenticationException e) {
			e.printStackTrace();
			exceptionCode = CommonUtils.getResultObject("Your Email", Constants.ERRONEOUS_OPERATION, "hasNotBeenSent",
					"");
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		} catch (Exception e) {
			e.printStackTrace();
			exceptionCode = CommonUtils.getResultObject("Your Email", Constants.ERRONEOUS_OPERATION, "hasNotBeenSent",
					"");
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}
		return exceptionCode;
	}

	private MimeMessage prepareEmail(final VisionUsersVb vObject, final String resultForgotBy)
			throws MessagingException {

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setTo("dakshina.deenadayalan@sunoida.com");
		helper.setText("Greetings :)");
		helper.setSubject("Mail From Spring Boot");
		return message;

		/*
		 * MimeMessagePreparator msg = new MimeMessagePreparator(){
		 * 
		 * @Override public void prepare(MimeMessage mimeMessage) throws Exception {
		 * Map<String , Object> map = new HashMap<String, Object>(); String msgBody =
		 * ""; MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true,
		 * "UTF-8"); message.setFrom("ddmoorthy94@gmail.com"); message.
		 * if(ValidationUtil.isValid(vObject.getUserEmailId())){
		 * message.setTo(vObject.getUserEmailId()); } map.put("subject",
		 * "Trouble Signing In"); map.put("emailScheduler", vObject);
		 * if("Username".equalsIgnoreCase(resultForgotBy)){ msgBody =
		 * VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(),
		 * "com/vision/wb/SR_EMAIL_FORGOT_USERNAME.vm", map); }else
		 * if("Password".equalsIgnoreCase(resultForgotBy)){ msgBody =
		 * VelocityEngineUtils.mergeTemplateIntoString(getVelocityEngine(),
		 * "com/vision/wb/SR_EMAIL_FORGOT_PASSWORD.vm", map); }
		 * message.setText(msgBody,true); message.setSentDate(new Date());
		 * message.setSubject("Trouble Signing In"); }
		 * 
		 * }; return msg;
		 */
	}

	public int doPasswordResetInsertion(VisionUsersVb vObj) {
		int intValue = getCommonDao().doPasswordResetInsertion(vObj);
		return intValue;
	}

	public VisionUsersVb getUserDetails() {
		return SessionContextHolder.getContext();
	}

	public VisionUsersDao getVisionUsersDao() {
		return visionUsersDao;
	}

	public void setVisionUsersDao(VisionUsersDao visionUsersDao) {
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

	public JavaMailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

}
