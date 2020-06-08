/**
 * Extracting data from uploaded file
 * Parsing XML/JSON/TXT/XLS/CSV file data for preview columns and data
 * If fUpload =true -- upload file to server also. Default it is true 
 * 
 */

package com.vision.controller;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.xml.sax.SAXException;

import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.FileInfoVb;
import com.vision.wb.AllFormatsUploadWb;
import com.vision.wb.VisionUploadWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/allFormats")
@Api(value="allFormats",description="Extracting all format of file data from Server")
public class AllFormatsUploadController {
	
	@Autowired
	private VisionUploadWb visionUploadWb;
	@Autowired
	private AllFormatsUploadWb allFormatsUploadWb;
	
	/*-------------------------------------UPLOAD FILE FOR PARSING-------------------------------------------*/
	@RequestMapping(path = "/parseFileData", method = RequestMethod.POST)
	@ApiOperation(value = "Parse Multipart file data",notes = "Returns mulitpart file data from server",response = ResponseEntity.class)
	public ResponseEntity < JSONExceptionCode > parseFileData(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws IOException, DataAccessException, SAXException, ParserConfigurationException {
		JSONExceptionCode jsonExceptionCode = null;
		boolean fUpload=false;
		try {
		   boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		   if(!isMultipart){
				return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload error",null), HttpStatus.EXPECTATION_FAILED);
		   }
		   MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		   Iterator<String> fileNames = multipartRequest.getFileNames();		   
		   while (fileNames.hasNext()) {
			    String fileName = (String) fileNames.next();
			    String extension = FilenameUtils.getExtension(file.getOriginalFilename());
			    MultipartFile mfile = multipartRequest.getFile(fileName);
			    FileInfoVb fileInfoVb = new FileInfoVb();
			    fileInfoVb.setName(mfile.getOriginalFilename());
			    fileInfoVb.setData(mfile.getBytes());
			    fileInfoVb.setExtension(extension);
			    if(ValidationUtil.isValid(fileInfoVb.getName())){
			    	jsonExceptionCode = allFormatsUploadWb.processFile(fileInfoVb,"");
			    	if(jsonExceptionCode.getStatus() != Constants.SUCCESSFUL_OPERATION){
						return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure",jsonExceptionCode.getResponse()), HttpStatus.EXPECTATION_FAILED);
			    	}else {
			    		if(fUpload == true) {
			    			DSConnectorVb vObj = new DSConnectorVb();
			    			ExceptionCode exceptionCode = visionUploadWb.doDataConnectorUpload(fileInfoVb,vObj,"");
			    			if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
								return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure",jsonExceptionCode.getResponse()), HttpStatus.EXPECTATION_FAILED);
			    			}
			    		}
			    	}
			    }
			}
			return new ResponseEntity < JSONExceptionCode > (jsonExceptionCode, HttpStatus.OK);
		} catch(RuntimeCustomException rex) {
			return new ResponseEntity < JSONExceptionCode > (new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", rex.getMessage()), HttpStatus.EXPECTATION_FAILED);
		} catch(SAXException e) {
			return new ResponseEntity < JSONExceptionCode > (new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", e.getMessage()), HttpStatus.EXPECTATION_FAILED);
		}
	}

}