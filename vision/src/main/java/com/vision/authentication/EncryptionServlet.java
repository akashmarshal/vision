package com.vision.authentication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.JCryptionUtil;

@RestController
public class EncryptionServlet {
	
	@Autowired
	static JCryptionUtil jCryptionUtil;
	@Autowired
	static SessionContextHolder sessionContextHolder;
	
	/*@CrossOrigin(origins = "http://localhost:4200")*/
	@GetMapping(value="generateKeypair")
	static ResponseEntity<JSONExceptionCode> genrateKey(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		JSONExceptionCode responseCode = new JSONExceptionCode();
		
		try {
			String connectionId = request.getSession(true).getId();
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(1024);
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			PublicKey publicKey = keyPair.getPublic();
			PrivateKey privateKey = keyPair.getPrivate();
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec rsaPublicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
			RSAPrivateKeySpec rsaPrivateKeySpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);
//			System.out.println("Public Module:"+ rsaPublicKeySpec.getModulus()+ "Public Exponent:"+ rsaPublicKeySpec.getPublicExponent());
			TreeMap<String, Object> responseMap = new TreeMap<String, Object>();
			responseMap.put("rsaPublicKeySpec", rsaPublicKeySpec);
			responseMap.put("rsaPrivateKeySpec", rsaPrivateKeySpec);
			RSAPublicKey rsaPublicKey = generateRSAPublicKey(keyPair);
			RSAPrivateKey rsaPrivateKey = generateRSAPrivateKey(keyPair);
			responseMap.put("publicKey", rsaPublicKey);
//			responseMap.put("privateKey", rsaPrivateKey);
			
			HttpSession ssn = request.getSession(true);
			sessionContextHolder.addOrUpdate(connectionId, ssn);
			byte[] encryptedData = encryptData("Sunoida@123", rsaPublicKeySpec);
			/*byte[] encryptedData1 = encryptData("Sunoida@123", rsaPublicKeySpec);
			byte[] encryptedData2 = encryptData("Sunoida@123", rsaPublicKeySpec);
			
			
			decryptData(rsaPrivateKeySpec, encryptedData);
			decryptData(rsaPrivateKeySpec, encryptedData1);
			decryptData(rsaPrivateKeySpec, encryptedData2);*/
			
			String temporaryToken = EncryptionServlet.genrateKeyWithoutSessionStorage();
			SessionContextHolder.addTokenForConnectionId(temporaryToken, connectionId, keyPair, rsaPublicKeySpec, rsaPrivateKeySpec, encryptedData);
			responseMap.put("temporary-token", temporaryToken);
			ConnectionHolder connectionHolder = SessionContextHolder.getconnectionClassFromTempToken(temporaryToken);
//			responseMap.put("encrypt-token", keyPair);
			responseCode.setResponse(responseMap);
			return new ResponseEntity<JSONExceptionCode>(responseCode, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	static String genrateKeyWithoutSessionStorage() throws IOException{
		try {
			KeyPair keyPair = jCryptionUtil.generateKeypair(512);
			StringBuffer output = new StringBuffer();
			String e = JCryptionUtil.getPublicKeyExponent(keyPair);
			String n = JCryptionUtil.getPublicKeyModulus(keyPair);
			String md = String.valueOf(JCryptionUtil.getMaxDigits(512));
			return n;
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	public static RSAPublicKey generateRSAPublicKey(KeyPair keyPair) throws Exception {
		KeyFactory keyFac = null;
		try {
			keyFac = KeyFactory.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
		} catch (NoSuchAlgorithmException ex) {
			throw new Exception(ex.getMessage());
		}
		RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(((RSAPublicKey) keyPair.getPublic()).getModulus(), ((RSAPublicKey) keyPair.getPublic()).getPublicExponent());
		try {
			return (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
		} catch (InvalidKeySpecException ex) {
			throw new Exception(ex.getMessage());
		}
	}
	
	public static RSAPrivateKey generateRSAPrivateKey(KeyPair keyPair) throws Exception {
		KeyFactory keyFac = null;
		try {
			keyFac = KeyFactory.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
		} catch (NoSuchAlgorithmException ex) {
			throw new Exception(ex.getMessage());
		}
		RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(((RSAPrivateKey) keyPair.getPrivate()).getModulus(), ((RSAPrivateKey) keyPair.getPrivate()).getPrivateExponent());
		try {
			return (RSAPrivateKey) keyFac.generatePrivate(priKeySpec);
		} catch (InvalidKeySpecException ex) {
			throw new Exception(ex.getMessage());
		}
	}
	
	private static byte[] encryptData(String data, RSAPublicKeySpec rsaPublicKeySpec) {
//		System.out.println("-------Encryption Started-------");
//		System.out.println("Data before encryption :" + data);
		byte[] dateToEncrypt = data.getBytes();
		byte[] encryptedData = null;
		try {
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PublicKey pubKey = fact.generatePublic(rsaPublicKeySpec);
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			encryptedData = cipher.doFinal(dateToEncrypt);
//			System.out.println("Encrypted Data : " + encryptedData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encryptedData;
	}
	
	private static void decryptData(RSAPrivateKeySpec rsaPrivateKeySpec, byte[] encryptedData) {
//		System.out.println("-------Decryption Started-------");
		byte[] decryptedData = null;
		try {
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = fact.generatePrivate(rsaPrivateKeySpec);
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] javaDecryptedData = cipher.doFinal(encryptedData);
//			System.out.println("Java Decrypted Data : " + new String(javaDecryptedData));
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println("-------Decryption Completed-------");
	}
}
