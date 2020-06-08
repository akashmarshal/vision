package com.vision.authentication.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.vision.authentication.AuthenticationBean;
import com.vision.dao.CommonDao;
import com.vision.dao.VisionUsersDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.VisionUsersVb;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jcifs.util.Base64;
import jespa.security.SecurityProviderException;
import jespa.util.LogStream;

@Component
public class AuthenticationService {
	public static Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
	@Autowired
	private CommonDao commonDao;
	@Autowired
	VisionUsersDao visionUsersDao;
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private JavaMailSenderImpl javaMailSenderImpl;
	@Autowired
	Configuration config;

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
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			/*MimeMessage msg = prepareEmail(vObject, resultForgotBy);
			getMailSender().send(msg);*/
			MimeMessage message = prepareEmail(vObject, resultForgotBy);
			getMailSender().send(message);
			exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
			exceptionCode.setErrorMsg("Mail sent successfully");
		} catch (MailAuthenticationException e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg("Mail sending failed");
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		} catch (Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg("Mail sending failed");
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}
		return exceptionCode;
	}

	/*private MimeMessage prepareEmail(final VisionUsersVb vObject, final String resultForgotBy)
			throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setTo("dakshina.deenadayalan@sunoida.com");
		helper.setText("Greetings :)");
		helper.setSubject("Mail From Spring Boot");
		return message;
	}*/

	private MimeMessage prepareEmail(final VisionUsersVb vObject, final String resultForgotBy)
			throws MessagingException {
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message,true);
		String msgBody = "";
		try {
			helper.setTo("dakshina.deenadayalan@sunoida.com");
			ClassPathResource file = new ClassPathResource("SR_EMAIL_FORGOT_PASSWORD.vm");
			helper.addAttachment("SR_EMAIL_FORGOT_PASSWORD.vm", file);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("subject", "Trouble Signing In");
			map.put("emailScheduler", vObject);
			if("Username".equalsIgnoreCase(resultForgotBy)) {
				msgBody = FreeMarkerTemplateUtils.processTemplateIntoString(config.getTemplate("SR_EMAIL_FORGOT_USERNAME.vm"), map);
			} else if("Password".equalsIgnoreCase(resultForgotBy)) {
				msgBody = FreeMarkerTemplateUtils.processTemplateIntoString(config.getTemplate("SR_EMAIL_FORGOT_PASSWORD.vm"), map);
			}
			helper.setText(msgBody,true);
			message.setSubject("Trouble Signing In");
		} catch (IOException | TemplateException e) {
			e.printStackTrace();
		}
		return message;
	}

	public int doPasswordResetInsertion(VisionUsersVb vObj) {
		int intValue = getCommonDao().doPasswordResetInsertion(vObj);
		return intValue;
	}

	public byte[] getTokenForLdap(HttpServletRequest req) {
		String hdr = req.getHeader("VisionAuthenticate");
		if (hdr != null) {
			if (hdr.startsWith("VISION")) {
				return Base64.decode(hdr.substring(6));
			} else {
				return null;
			}
		}
		return null;
	}
	
	public String getConnectionId(HttpServletRequest req) throws SecurityProviderException {
		String cid = req.getHeader("Jespa-Connection-Id");
		if (cid != null) {
			return cid;
		}
		return req.getSession(true).getId();
	}
	
	public CommonDao getCommonDao() {
		return commonDao;
	}

	public void setCommonDao(CommonDao commonDao) {
		this.commonDao = commonDao;
	}

	public VisionUsersDao getVisionUsersDao() {
		return visionUsersDao;
	}

	public void setVisionUsersDao(VisionUsersDao visionUsersDao) {
		this.visionUsersDao = visionUsersDao;
	}

	public JavaMailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public JavaMailSenderImpl getJavaMailSenderImpl() {
		return javaMailSenderImpl;
	}

	public void setJavaMailSenderImpl(JavaMailSenderImpl javaMailSenderImpl) {
		this.javaMailSenderImpl = javaMailSenderImpl;
	}
}