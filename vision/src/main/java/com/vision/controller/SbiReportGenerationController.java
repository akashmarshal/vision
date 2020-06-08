package com.vision.controller;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.Constants;
import com.vision.vb.VcReportGenerationVb;
import com.vision.wb.SbiReportGenerationWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "sbiReportGeneration")
@Api(value="sbiReportGeneration", description="Report Generation")
public class SbiReportGenerationController{
	
	@Autowired
	SbiReportGenerationWb sbiReportGenerationWb;
	
//	-------------------------------------Report Prompts-------------------------------------------
	@RequestMapping(path = "/getPromptData", method = RequestMethod.POST)
	@ApiOperation(value = "Get Prompt Data",notes = "Returns list of prompt and data for the report",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getVcGencreatePrompts(@RequestBody VcReportGenerationVb vcReportGenerationVb) throws ParserConfigurationException, SAXException, IOException {
		JSONExceptionCode jsonExceptionCode  = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try{
			exceptionCode = sbiReportGenerationWb.getVrdPromptData(vcReportGenerationVb);
			if(exceptionCode.getErrorCode()!=0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Listing report prompts and data", exceptionCode.getRequest());
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),"");
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

//	-------------------------------------Report Prompts-------------------------------------------
	@RequestMapping(path = "/getPageDesign/{pageNumber}", method = RequestMethod.POST)
	@ApiOperation(value = "Get Prompt Data", notes = "Returns list of prompt and data for the report", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getVcGencreatePageDesign(@PathVariable("pageNumber") Integer pageNumber,
			@RequestBody VcReportGenerationVb vcReportGenerationVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			vcReportGenerationVb.setPageNo(String.valueOf(pageNumber));
			exceptionCode = sbiReportGenerationWb.getVcReportGenerationPageDesign(vcReportGenerationVb);
			if (exceptionCode.getErrorCode() != 0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
						"Listing report prompts and data", exceptionCode.getRequest());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						"");
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
//	-------------------------------------Report Prompts-------------------------------------------
	@RequestMapping(path = "/generateSubReport", method = RequestMethod.POST)
	@ApiOperation(value = "Get Prompt Data", notes = "Generate Subreport with Report ID, Sub-report ID and Prompt Values", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getVcGencreateSubReport(@RequestBody VcReportGenerationVb vcReportGenerationVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = sbiReportGenerationWb.getVcReportGenerationPageDesign(vcReportGenerationVb);
			if (exceptionCode.getErrorCode() != 0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
						"Listing report prompts and data", exceptionCode.getRequest());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						"");
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

}