package com.vision.authentication;

import java.io.IOException;
import java.security.KeyPair;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vision.util.JCryptionUtil;

/**
 * Servlet implementation class EncryptionServlet
 */
public class EncryptionServlet_Old extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public EncryptionServlet_Old() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		genrateKey(request, response,false);
	}
	static void genrateKey(HttpServletRequest request, HttpServletResponse response,Boolean gKey) throws IOException{
		
			if (request.getParameter("generateKeypair") != null || gKey==true) {
		
				JCryptionUtil jCryptionUtil = new JCryptionUtil();
		
				KeyPair keys = null;
				if (request.getSession().getAttribute("keys") == null) {
					keys = jCryptionUtil.generateKeypair(512);
					request.getSession().setAttribute("keys", keys);
				}else{
					keys = (KeyPair) request.getSession().getAttribute("keys");
				}
				StringBuffer output = new StringBuffer();
		
				String e = JCryptionUtil.getPublicKeyExponent(keys);
				String n = JCryptionUtil.getPublicKeyModulus(keys);
				String md = String.valueOf(JCryptionUtil.getMaxDigits(512));
		
				output.append("{\"e\":\"");
				output.append(e);
				output.append("\",\"n\":\"");
				output.append(n);
				output.append("\",\"maxdigits\":\"");
				output.append(md);
				output.append("\"}");
		
				output.toString();
				if(gKey==false){
					response.getOutputStream().print(
							output.toString().replaceAll("\r", "").replaceAll("\n", "")
									.trim());
				}
			} else {
				if(gKey==false){
					response.getOutputStream().print(String.valueOf(false));
				}
			}
	}
	
}