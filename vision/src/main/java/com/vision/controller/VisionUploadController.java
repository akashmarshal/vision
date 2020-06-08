package com.vision.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vision.download.ExportXlsServlet;
import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.FileInfoVb;
import com.vision.wb.VisionUploadWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "visionUpload")
@Api(value="/visionUpload",description="Operations pertaining to Handle Files to/from Server")
public class VisionUploadController {
	
	@Autowired
	private VisionUploadWb visionUploadWb;

	@Autowired
	ExportXlsServlet exportXlsServlet;
	
	/*-------------------------------------Upload Files Rest Service-------------------------------*/
	@RequestMapping(path = "/uploadFilesToFtp", method = RequestMethod.POST)
	@ApiOperation(value = "Uploading Multipart files to Server",notes = "Upload files to Server",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> uploadFilesToFtp(@RequestParam("file") MultipartFile[] files){
		ExceptionCode exceptionCode = null;
		try{
		   if (files.length<=0) {
			   return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload file error",null), HttpStatus.EXPECTATION_FAILED);
		   }else {		  
			   for(MultipartFile uploadedFile : files) {
				   String extension = FilenameUtils.getExtension(uploadedFile.getOriginalFilename());
				   FileInfoVb fileInfoVb = new FileInfoVb();
				    fileInfoVb.setName(uploadedFile.getOriginalFilename());
				    fileInfoVb.setData(uploadedFile.getBytes());
				    fileInfoVb.setExtension(extension);
				    if(ValidationUtil.isValid(fileInfoVb.getName())){
				    	exceptionCode = visionUploadWb.doUpload(fileInfoVb.getName(), fileInfoVb.getData(), fileInfoVb.getExtension());
				    	if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
							return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Fail",null), HttpStatus.EXPECTATION_FAILED);
				    	}
				    }
				  }
			  	}
		   return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Upload","Success"), HttpStatus.OK);
		}catch (Exception e) {
			RuntimeCustomException ex = (RuntimeCustomException)e;
			e.printStackTrace();
			return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, ex.getMessage(),null), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/*-------------------------------------Download Files Rest Service-------------------------------*/
	@RequestMapping(path = "/downloadFilesFromFtp", method = RequestMethod.GET)
	@ApiOperation(value = "Downloading Multipart files to Server",notes = "Download files from server",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> downloadFilesFromFtp(@QueryParam("fileName") String fileName, @QueryParam("fileExtension") String fileExtension, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		try{
			ExceptionCode exceptionCode = visionUploadWb.fileDownload(1,fileName,"","", fileExtension);
			if(exceptionCode.getErrorCode()==1) {
				request.setAttribute("fileName", fileName);	
				request.setAttribute("fileExtension", fileExtension);
				request.setAttribute("filePath", exceptionCode.getResponse());
				exportXlsServlet.doPost(request,response); 
				if(response.getStatus()==404) {
					return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,"File not found",null), HttpStatus.EXPECTATION_FAILED);
				}
				return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success",exceptionCode.getResponse()), HttpStatus.OK);
			}else {
				return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure",null), HttpStatus.EXPECTATION_FAILED);
			}
		}catch(RuntimeCustomException rex){
			return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(),null), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/*-------------------------------------List Rest Service-------------------------------*/
	@RequestMapping(path = "/listFilesFromSftp", method = RequestMethod.GET)
	@ApiOperation(value = "Listing Multipart files from Server",notes = "List files from Server",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> listRestFilesFromFtpServer(@QueryParam("groupBy") String groupBy,@QueryParam("dirType") int dirType){
		ExceptionCode exceptionCode = null;
		try {
			if("T".equalsIgnoreCase(groupBy)){
				groupBy = "Table";
			}else if("B".equalsIgnoreCase(groupBy)){
				groupBy = "Build";
			}else {
				groupBy = "Date";
			}
			exceptionCode = visionUploadWb.listFilesFromFtpServer(dirType, groupBy);
			return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success",exceptionCode.getResponse()), HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(),null), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
}