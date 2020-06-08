package com.vision.authentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vision.authentication.service.AuthenticationService;
import com.vision.dao.VisionUsersDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.VisionUsersVb;

import jcifs.util.Base64;

@RestController
public class AuthenticateCredential {

	@Autowired
	VisionUsersDao visionUsersDao;
	@Autowired
	private AuthenticationService authenticationService;

	@PostMapping(value = "authenticate")
	public ResponseEntity<JSONExceptionCode> getUserAndMenuDetailsForResponseWthToken(HttpServletRequest request,
			HttpServletResponse response) {
		LinkedHashMap<String, Object> responseMap = new LinkedHashMap<String, Object>();
		HttpStatus status = HttpStatus.OK;
		JSONExceptionCode exceptionCode = new JSONExceptionCode();
		HttpSession session = request.getSession();
		try {
			responseMap.put("token",
					"VISION" + Base64.encode((request.getAttribute("tempTokenStorage") + "").getBytes()));
			Date expirationDate = SessionContextHolder.getTokenProps(request.getAttribute("tempTokenStorage") + "")
					.getNxtExpireDate();

			List<Object> menuSessionList = new ArrayList<Object>();
			if (session.getAttribute("menuDetails") != null) {
				menuSessionList = (List<Object>) session.getAttribute("menuDetails");
			} else {
				menuSessionList = null;
			}

			responseMap.put("expired_on", expirationDate.getTime());
			responseMap.put("user_details", session.getAttribute("userDetails"));
			if (menuSessionList != null && menuSessionList.size() > 1) {
				responseMap.put("menu_details", menuSessionList.get(0));
				responseMap.put("menu_hierarchy", menuSessionList.get(1));
			}

			exceptionCode.setResponse(responseMap);

			SessionContextHolder.removeTempTokenForConnectionId(request.getHeader("temporary-token"));

			/*
			 * Set the values to the header of the response
			 * 
			 * HttpHeaders headers = new HttpHeaders(); headers.add("VisionAuthenticate",
			 * "VISION"+Base64.encode((request.getAttribute("tempTokenStorage")+"").getBytes
			 * ())); return new ResponseEntity<JSONExceptionCode>(exceptionCode, headers,
			 * status);
			 */
			return new ResponseEntity<JSONExceptionCode>(exceptionCode, status);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}

	@PostMapping(value = "forgotUsername")
	public ResponseEntity<JSONExceptionCode> responseForForgotPasswordEmail(HttpServletRequest request,
			HttpServletResponse response) {
		JSONExceptionCode responseCode = null;
		HttpSession ssn = request.getSession(true);

		String pwdResetURL = authenticationService.findVisionVariableValuePasswordURL();
		String pwdResetTime = authenticationService.findVisionVariableValuePasswordResrtTime();
		ExceptionCode exceptionCode = null;

		VisionUsersVb vObj = new VisionUsersVb();
		String requestmailId = request.getParameter("emailID");
		String resultForgotBy = ValidationUtil.isValid(request.getParameter("txtResultForgotBy"))
				? request.getParameter("txtResultForgotBy")
				: "Username";

		if (ValidationUtil.isValid(resultForgotBy)) {
			vObj.setUserEmailId(requestmailId);
			vObj.setPwdResetTime(pwdResetTime);
			vObj.setPasswordResetURL(pwdResetURL);
			exceptionCode = authenticationService.callProcToPopulateForgotPasswordEmail(vObj, resultForgotBy);
			if (exceptionCode.getErrorCode() == 1) {
//				ssn.setAttribute("forgotPasswordErrorStatus", exceptionCode.getErrorMsg());
				responseCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), null);
			} else if (exceptionCode.getErrorCode() == 0) {
				vObj.setUserName(exceptionCode.getErrorMsg().split("@")[0]);
				vObj.setUserLoginId(exceptionCode.getErrorMsg().split("@")[1]);
				vObj.setVisionId(Integer.parseInt(exceptionCode.getErrorMsg().split("@")[2]));
				ssn.setAttribute("reqVisionID", vObj.getVisionId());
				String ciphertext = ValidationUtil.passwordEncryptWithUrlEncode(Integer.toString(vObj.getVisionId()));
				vObj.setScreenName(ciphertext);
				exceptionCode = authenticationService.doSendEmail(vObj, resultForgotBy);
				if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					vObj.setEmailStatus("S");
					responseCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
							null);
				} else {
					vObj.setEmailStatus("E");
//					ssn.setAttribute("forgotPasswordErrorStatus", exceptionCode.getErrorMsg());
					responseCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
							null);
				}
			}
		} else {
			responseCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Configuration Issue", null);
		}

		return new ResponseEntity<JSONExceptionCode>(responseCode, HttpStatus.OK);
	}

	@PostMapping(value = "refreshToken")
	public ResponseEntity<JSONExceptionCode> updateToken(HttpServletRequest request, HttpServletResponse response) {
		JSONExceptionCode exceptionCode = new JSONExceptionCode();
		LinkedHashMap<String, Object> responseMap = new LinkedHashMap<String, Object>();
		try {
			byte[] oldToken = authenticationService.getTokenForLdap(request);
			String connectionId = authenticationService.getConnectionId(request);
			if (SessionContextHolder.isTokenAvailable(new String(oldToken))) {
				String newToken = EncryptionServlet.genrateKeyWithoutSessionStorage();
				SessionContextHolder.updateToken(new String(oldToken), connectionId, newToken);
				if (SessionContextHolder.isTokenValidWthConnectionId(newToken, connectionId)) {
					responseMap.put("token", "VISION" + Base64.encode(newToken.getBytes()));
					responseMap.put("expired_on", SessionContextHolder.getTokenProps(newToken).getNxtExpireDate());
				} else {
					throw new RuntimeCustomException("Problem in refreshing token");
				}
			} else {
				throw new RuntimeCustomException("Token Expire");
			}
			exceptionCode.setResponse(responseMap);
			return new ResponseEntity<JSONExceptionCode>(exceptionCode, HttpStatus.OK);
		} catch (Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
	}
}