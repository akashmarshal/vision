package com.vision.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.Constants;
import com.vision.vb.AlphaSubTabVb;
import com.vision.vb.NumSubTabVb;
import com.vision.wb.AlphaNumTabWb;
import com.vision.wb.AlphaSubTabWb;
import com.vision.wb.NumSubTabWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "numalpha")
@Api(value="numalpha", description="Obtaining alpha tab and num tab values for drop down fields")
public class NumAlphaTabController{
	
	@Autowired
	AlphaNumTabWb alphaNumTabWb;
	
	@Autowired
	AlphaSubTabWb alphaSubTabWb;
	
	@Autowired
	NumSubTabWb numSubTabWb;
	
	/*-------------------------------------NUM TAB SERVICE-------------------------------------------*/
	@GetMapping(value = "/getNumTab")
	@ApiOperation(value = "Returns list of available num sub tab values",
	notes = "Pass a num tab value in request param, to get numsub tab values in list",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getNumTab(@RequestParam("pNumTab") int pNumTab){
		JSONExceptionCode jsonExceptionCode  = null;
		try {
			List<NumSubTabVb> collTemp = numSubTabWb.findActiveNumSubTabsByNumTab(pNumTab);
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", collTemp);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@GetMapping(value = "/getNumTabCol")
	@ApiOperation(value = "Returns list of num sub tab values for specified columns",
	notes = "Based on specified column, sub tab values will be provided in list",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getNumTab(@RequestParam("pNumTab") int pNumTab, @RequestParam("columns") String columns){
		JSONExceptionCode jsonExceptionCode  = null;
		try {
			List collTemp = numSubTabWb.findActiveNumSubTabsByNumTabCols(pNumTab, columns);
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", collTemp);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------ALPHA TAB SERVICE-------------------------------------------*/
	@GetMapping(value = "/getAlphaTab")
	@ApiOperation(value = "Returns list of available alpha sub tab values",
	notes = "Pass a alpha tab value in request param, to get alphasub tab values in list",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAlphaTab(@RequestParam("pAlphaTab") int pAlphaTab){
		JSONExceptionCode jsonExceptionCode  = null;
		try {
			List<AlphaSubTabVb> collTemp = alphaSubTabWb.findActiveAlphaSubTabsByAlphaTab(pAlphaTab);
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", collTemp);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@GetMapping(value = "/getAlphaTabCol")
	@ApiOperation(value = "Returns list of alpha sub tab values for specified columns",
	notes = "Based on specified column, alpha subtab values will be provided in list",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAlphaTabCol(@RequestParam("pAlphaTab") int pAlphaTab, @RequestParam("columns") String columns){
		JSONExceptionCode jsonExceptionCode  = null;
		try {
			List collTemp = alphaSubTabWb.findActiveAlphaSubTabsByAlphaTabCols(pAlphaTab, columns);
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", collTemp);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
}