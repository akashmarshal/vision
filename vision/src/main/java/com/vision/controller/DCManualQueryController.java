/**
 * Catalog - Data Connector - Manual Query 
 * We have add/modify/delete/validate manual query
 * While add we will list hash tag to get hash value and we will list columns from respective tables with data type
 *  
**/
package com.vision.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.AlphaSubTabVb;
import com.vision.vb.DCManualQueryVb;
import com.vision.vb.DesignAnalysisVb;
import com.vision.wb.DCManualQueryWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "dcManualQuery")
@Api(value="dcManualQuery", description="Operations pertaining to manual query for catalog")
public class DCManualQueryController {
	
	@Autowired
	private DCManualQueryWb dcManualQueryWb;
	
	/*-------------------------------------GET ALL VC QUERIES SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getAll/{startIndex}/{lastIndex}/{totalRows}", method = RequestMethod.GET)
	@ApiOperation(value = "Get All Manual Query",notes = "Returns list of all Manual Query from Vc Queries",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAllDcManualQuery(@PathVariable("startIndex") Integer startIndex, @PathVariable("lastIndex") Integer lastIndex, @PathVariable("totalRows") Integer totalRows) {
		JSONExceptionCode jsonExceptionCode  = null;
		try{
			DCManualQueryVb dcManualQueryVb = new DCManualQueryVb();
			List<DCManualQueryVb> queryList = dcManualQueryWb.getAllDcManualQuery(dcManualQueryVb);
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Manual Query Listing", queryList);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}	
	}
	
	/*-------------------------------------GET SPECIFIC VC QUERIES SERVICE-------------------------------------------*/
	@RequestMapping(path = "/get", method = RequestMethod.GET)
	@ApiOperation(value = "Manual Query Listing for specific query",
	notes = "Pass a query id in request param, to get specificied manual query data in list",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getSpecificDcManualQuery(@RequestParam("queryId") String queryId) {
		JSONExceptionCode jsonExceptionCode  = null;
		try{
			DCManualQueryVb dcManualQueryVb = new DCManualQueryVb();
			dcManualQueryVb.setQueryId(queryId);
			List<DCManualQueryVb> queryList = dcManualQueryWb.getSpecificManualQuery(dcManualQueryVb);
			if(queryList.size()==0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "No records found", dcManualQueryVb);
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "List Specific Query", queryList);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}	
	}
	
	/*------------------------------------- Get Hash List-------------------------------------------*/
	@RequestMapping(path = "/getHashList", method = RequestMethod.POST)
	@ApiOperation(value = "Hash List Listing",
	notes = "Get hash list for specified manual query",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getHashList(@RequestBody DCManualQueryVb dcManualQueryVb){
		JSONExceptionCode jsonExceptionCode  = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = CommonUtils.formHashList(dcManualQueryVb);
			if(exceptionCode.getErrorCode()!=0) {
				String[] hashArr = null;
				String[] hashValue = null;
				List < Object > collTemp = new ArrayList < Object > ();
				
			    int size = (Integer)exceptionCode.getResponse();
			    if(size>0) {
					hashArr = new String[size];
				    hashArr = (String[]) exceptionCode.getOtherInfo();
				    hashValue = new String[size];
				    hashValue = (String[]) exceptionCode.getRequest();
				    collTemp.add(hashArr);
				    collTemp.add(hashValue);
				}
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", collTemp);
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Error forming hash list","");
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------Add Manual Query-------------------------------------------*/
	@RequestMapping(path = "/add", method = RequestMethod.POST)
	@ApiOperation(value = "Add Manual Query",
	notes = "Validate and add a manual query to vc queries",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> addManualQuery(@RequestBody DCManualQueryVb dcManualQueryVb, @RequestParam("forceFlag") boolean forceFlag){
		JSONExceptionCode jsonExceptionCode  = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			if(forceFlag ==true) {
				 if(ValidationUtil.isValid(dcManualQueryVb.getQueryColumnXML())) {
					 dcManualQueryVb.setQueryValidFlag("S");	 
				 }else {
					 dcManualQueryVb.setQueryValidFlag("F");
				 }
				 dcManualQueryVb.setPreviousActionType("isNotValid");
				exceptionCode = dcManualQueryWb.insertRecord(dcManualQueryVb);
			}else if(forceFlag ==false && !ValidationUtil.isValid(dcManualQueryVb.getQueryColumnXML())){
				exceptionCode = CommonUtils.getResultObject("VC Query", Constants.WE_HAVE_ERROR_DESCRIPTION, "Add", "Validate before proceeding to save your changes");
			}
			if(exceptionCode.getErrorCode() ==Constants.SUCCESSFUL_OPERATION){
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getOtherInfo());
				if("Add".equalsIgnoreCase(exceptionCode.getActionType())){
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getRequest());
				}
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------Modify Manual Query-------------------------------------------*/
	@RequestMapping(path = "/modify", method = RequestMethod.POST)
	@ApiOperation(value = "Modify Manual Query",
	notes = "Validate and modify a manual query from vc queries",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> modifyManualQuery(@RequestBody DCManualQueryVb dcManualQueryVb, @RequestParam("forceFlag") boolean forceFlag){
		JSONExceptionCode jsonExceptionCode  = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			if(forceFlag ==true) {
				 if(ValidationUtil.isValid(dcManualQueryVb.getQueryColumnXML())) {
					 dcManualQueryVb.setQueryValidFlag("S");	 
				 }else {
					 dcManualQueryVb.setQueryValidFlag("F");
				 }
				 dcManualQueryVb.setPreviousActionType("isNotValid");
				 exceptionCode = dcManualQueryWb.modifyRecord(dcManualQueryVb);
			}else if(forceFlag ==false && !ValidationUtil.isValid(dcManualQueryVb.getQueryColumnXML())){
				exceptionCode = CommonUtils.getResultObject("VC Query", Constants.WE_HAVE_ERROR_DESCRIPTION, "Modify", "Validate before proceeding to save your changes");
			}
			if(exceptionCode.getErrorCode() ==Constants.SUCCESSFUL_OPERATION){
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getOtherInfo());
				if("Modify".equalsIgnoreCase(exceptionCode.getActionType())){
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getRequest());
				}
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------ValidateQuery Manual Query-------------------------------------------*/
	@RequestMapping(path = "/validate", method = RequestMethod.POST)
	@ApiOperation(value = "Validate Manual Query",
	notes = "Validate a manual query by connecting the data source and executing the query",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> validateManualQuery(@RequestBody DCManualQueryVb dcManualQueryVb, @RequestParam("hashArr") String[] hashArr, @RequestParam("hashValArr") String[] hashValArr){
		JSONExceptionCode jsonExceptionCode  = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			String dbScript = dcManualQueryWb.getDbScript(dcManualQueryVb.getDatabaseConnectivityDetails());
				   exceptionCode = dcManualQueryWb.validateSqlQuery(dcManualQueryVb, dbScript, hashArr, hashValArr);

			if(exceptionCode.getErrorCode() ==Constants.SUCCESSFUL_OPERATION){
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "VC Query - Validation - Successful", exceptionCode.getRequest());
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------Delete Manual Query-------------------------------------------*/
	@RequestMapping(path = "/delete", method = RequestMethod.POST)
	@ApiOperation(value = "Delete Manual Query",
	notes = "Delete a manual query from vc queries",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> deleteManualQuery(@RequestBody DCManualQueryVb dcManualQueryVb){
		JSONExceptionCode jsonExceptionCode  = null;
		try {
			dcManualQueryVb.setPreviousActionType("isNotValid");
			ExceptionCode exceptionCode = dcManualQueryWb.deleteRecord(dcManualQueryVb);
			if(exceptionCode.getErrorCode() ==Constants.SUCCESSFUL_OPERATION){
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getOtherInfo());
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	/*-------------------------------------GET Database connectivity details-------------------------------------------*/
	@RequestMapping(path = "/getdbCon", method = RequestMethod.GET)
	@ApiOperation(value = "Listing Connector Details in Manual Query",
	notes = "Listing Connector Details in Manual Query",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAllConnectorDetails(@RequestParam("searchPattern") String searchPattern) {
		JSONExceptionCode jsonExceptionCode  = null;
		try{
			DCManualQueryVb dcManualQueryVb = new DCManualQueryVb();
			dcManualQueryVb.setLocale(searchPattern);
			List<AlphaSubTabVb> queryList = dcManualQueryWb.getQuerySmartSearchResults(dcManualQueryVb);
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "List Connector Details", queryList);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}	
	}
	
	/*-------------------------------------GET ALL VC QUERIES BASED ON QUERY TYPE SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getAllMagQuery", method = RequestMethod.POST)
	@ApiOperation(value = "Get All Query Based on Query Type",notes = "Returns list of all Manuary Query from Vc Queries with query type",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAllDcManualQueryBasedOnQueryType(@RequestParam("queryType") Integer queryType) {
		JSONExceptionCode jsonExceptionCode  = null;
		try{
			List<DCManualQueryVb> queryList = dcManualQueryWb.getAllDcManualQueryBasedOnQueryType(queryType);
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Manual Query Listing", queryList);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}	
	}
	
	/*-------------------------------------GET COLUMNS BASED ON QUERY SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getAllQueryCols", method = RequestMethod.POST)
	@ApiOperation(value = "Get All Columns Based on Query",notes = "Returns list of all columns based on Manuary Query from Vc Queries with query type",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAllQueryCols(@RequestParam("queryID") String queryID) {
		JSONExceptionCode jsonExceptionCode  = null;
		try{
			String colList = dcManualQueryWb.getColsBasedOnDcManualQuery(queryID);
			if(ValidationUtil.isValid(colList)) {
				ExceptionCode exceptionCode=CommonUtils.XmlToJson(colList);
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Manual Query Column Listing", exceptionCode.getResponse());
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No records found", "");	
			}
			
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}	
	}
	

	/*-------------------------------------SMART SEARCH CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/smartSearchFilter", method = RequestMethod.POST, 	consumes="application/json", produces = "application/json")
	@ApiOperation(value = "Smart Search Filter", notes = "Smart search for all columns displayed in grid", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> smartSearchFilter(@RequestBody DCManualQueryVb designVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<DCManualQueryVb> queryList = dcManualQueryWb.getQuerySmartSearchFilter(designVb);
			if(	ValidationUtil.isValidList(queryList)) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "List Filtered Design Query",queryList,designVb);	
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No records found",null,designVb);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
}