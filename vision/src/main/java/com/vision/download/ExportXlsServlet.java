package com.vision.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Component;

import com.vision.authentication.SessionContextHolder;
import com.vision.util.ValidationUtil;


@Component
public class ExportXlsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		File lFile = null;
		try {
			int currentUser =  SessionContextHolder.getContext().getVisionId();

			String fileName = (String) request.getAttribute("fileName");
			String fileType=(String) request.getAttribute("fileExtension");
			fileType="."+fileType.toLowerCase();
			String filePath =(String) request.getAttribute("filePath");
			if(!ValidationUtil.isValid(fileName)){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			if(!ValidationUtil.isValid(filePath)){
				filePath = System.getProperty("java.io.tmpdir");
				if(!ValidationUtil.isValid(filePath)){
					filePath = System.getenv("TMP");
				}
			}
			if(ValidationUtil.isValid(filePath)){
				filePath = filePath + File.separator;
			}
		
			if(".PDF".equalsIgnoreCase(fileType)){
				response.addHeader("Content-Type", "application/pdf"); 
				response.setContentType("application/pdf");
				fileName = fileName.substring(0, fileName.indexOf(".pdf"));
				lFile = new File(filePath+ValidationUtil.encode(fileName)+".pdf");
				fileName = fileName+".pdf";
			}else if(".xlsx".equalsIgnoreCase(fileType)){
				response.addHeader("Content-Type", "application/excel"); 
				response.setContentType("application/vnd.ms-excel");
				fileName = fileName.substring(0, fileName.indexOf(".xlsx"));
				lFile = new File(filePath+ValidationUtil.encode(fileName)+".xlsx");
				fileName = fileName+".xlsx";
			}else if(fileName.endsWith("txt") || fileName.endsWith("log") || fileName.endsWith("err")){
				response.addHeader("Content-Type", "application/txt");
				response.setContentType("application/txt");
				lFile = new File(filePath+fileName);
			}else if(".csv".equalsIgnoreCase(fileType)){
				response.addHeader("Content-Type", "application/txt");
				response.setContentType("application/txt");
				fileName = fileName.substring(0, fileName.indexOf(".csv"));
				lFile = new File(filePath+ValidationUtil.encode(fileName)+".csv");
				fileName = fileName+".csv";
			}else if(fileName.endsWith("zip")){
				response.addHeader("Content-Type", "application/zip");
				response.setContentType("application/zip");
				lFile = new File(filePath+fileName);
			}else if(fileName.endsWith("tar")){
				response.addHeader("Content-Type", "application/x-tar");
				response.setContentType("application/x-tar");
				lFile = new File(filePath+fileName);
			}else if(".xml".equalsIgnoreCase(fileType)){
			    response.addHeader("Content-Type", "application/xml"); 
			    response.setContentType("application/xml");
				lFile = new File(filePath+ValidationUtil.encode(fileName));
			}else if(".json".equalsIgnoreCase(fileType)){
			    response.addHeader("Content-Type", "application/json"); 
			    response.setContentType("application/json");
				lFile = new File(filePath+ValidationUtil.encode(fileName));
			}else{
				response.addHeader("Content-Type", "application/txt");
				response.setContentType("application/txt");
				lFile = new File(filePath+fileName);
			}
			response.setHeader("Content-Disposition","attachment; filename=\"" + fileName + "\"");
			if(lFile == null || !lFile.exists()){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			FileInputStream fis = new FileInputStream(lFile);
			// Write the output
			OutputStream out = response.getOutputStream();
			FileUtil.copyStream(fis, out);
			out.flush();
			out.close();
			fis.close();
			lFile.delete();
		}catch (Exception e){
			throw new ServletException("Exception in File download Servlet", e);
		}finally{
			
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
}
