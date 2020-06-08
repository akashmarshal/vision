/**
 * DESIGN & ANALYSIS SERVICE 
 * Listing of all Design query,Manual query,Catalogs,Tree,Columns,Relationships based on VisionID
 *  
**/
package com.vision.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.DCManualQueryVb;
import com.vision.vb.DesignAnalysisVb;
import com.vision.vb.DesignAndAnalysisMagnifierVb;
import com.vision.vb.VcConfigMainColumnsVb;
import com.vision.vb.VcConfigMainTreeVb;
import com.vision.vb.VcConfigMainVb;
import com.vision.vb.VcForCatalogTableRelationVb;
import com.vision.vb.VcForQueryReportFieldsVb;
import com.vision.vb.VcForQueryReportFieldsWrapperVb;
import com.vision.vb.VcForQueryReportVb;
import com.vision.vb.WidgetDesignVb;
import com.vision.wb.DCManualQueryWb;
import com.vision.wb.DesignAnalysisWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "designAnalysis")
@Api(value = "designAnalysis", description = "Operations pertaining to Design and Analysis")
public class DesignAnalysisController {

	@Autowired
	private DesignAnalysisWb designAnalysisWb;
	
	@Autowired
	private DCManualQueryWb dcManualQueryWb;

	/*-------------------------------------GET ALL DESIGN QUERIES SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getAll", method = RequestMethod.POST)
	@ApiOperation(value = "Get All Design Query", notes = "Returns list of all Design Query from VCDQ queries access using VisionId", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAllDesignQuery(@RequestBody DesignAnalysisVb designVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<DesignAnalysisVb> queryList = designAnalysisWb.getAllDesignQuery(designVb);
			if(queryList!=null && queryList.size()>0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, queryList.size()+" - Design Query Listing",
						queryList,designVb);
			} else {
				 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Design query results found for this User",null,
						 designVb); 
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------GET ALL MANUAL QUERIES SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getAllMQ", method = RequestMethod.POST)
	@ApiOperation(value = "Get All Manual Query", notes = "Returns list of all Manual Query from Vc Queries using VisionId", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAllManualQuery(@RequestBody DCManualQueryVb manualQueryVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<DCManualQueryVb> queryList = designAnalysisWb.getAllManualQuery(manualQueryVb);
			if(queryList!=null &&  queryList.size()>0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, queryList.size()+" - Manual Query Listing",
						queryList,manualQueryVb);
			} else {
				 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Manual query results found for this User",null,
						 manualQueryVb); 
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------LIST CATALOGS SERVICE-------------------------------------------*/
	@RequestMapping(path = "/listCatalog", method = RequestMethod.GET)
	@ApiOperation(value = "Listing of Catalog", notes = "Returns list of all Catalogs based on UserGroup & Profile", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getUserCatalogs() {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<VcConfigMainVb> queryList = designAnalysisWb.getUserCatalogs();
			if(queryList.size()>0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, queryList.size()+" - Catalog Listing",
						queryList);
			} else {
				 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Catalogs results found for this User",
						 null); 
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------LIST CATALOG TREES QUERY SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getQueryTree", method = RequestMethod.POST)
	@ApiOperation(value = "Listing of Design Query Catalog Trees ", notes = "Returns list of all Trees based on Catalog using UserGroup", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getQueryTree(@RequestBody VcConfigMainTreeVb treeVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			if(!ValidationUtil.isValid(treeVb.getCatalogId())) {
				 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "CatalogId parameter is missing",null); 
			} else {
				List<VcConfigMainTreeVb> queryList = designAnalysisWb.getQueryTree(treeVb);
				if(queryList.size()>0) {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, queryList.size()+" Trees listed for Catalog",
							queryList);
				} else {
					 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Design Query Catalog Trees found",
							 null); 
				}
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------LIST CATALOG TREES COLUMNS QUERY SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getQueryTreeColumns", method = RequestMethod.POST)
	@ApiOperation(value = "Listing of Design Query Catalog Tree Columns", notes = "Returns list of all Design Query Tree Columns based UserGroup", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getQueryTreeColumns(@RequestBody VcConfigMainTreeVb treeVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			if(!ValidationUtil.isValid(treeVb.getCatalogId()) || !ValidationUtil.isValid(treeVb.getTableId())) {
				 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Parameters are missing",null); 
			} else {
				List<VcConfigMainColumnsVb> queryList = designAnalysisWb.getQueryTreeColumns(treeVb);
				if(queryList.size()>0) {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, queryList.size()+" Columns listed for Catalog Tree",
							queryList);
				 } else {
					 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Catalog Tree Columns found",
							 null); 
				 }
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------LIST CATALOG TREES COLUMNS RELATION QUERY SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getQueryTreeColumnsRelations", method = RequestMethod.POST)
	@ApiOperation(value = "Listing of Design Query Tree Column Relations", notes = "Returns list of all Design Query Tree Column Relations based on UserGroup", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getQueryTreeColumnsRelations(@RequestBody VcForCatalogTableRelationVb VcForCatalogTableRelationVb){
		//("catalogId") String catalogId,@RequestParam("fromTableId") String fromTableId,@RequestParam("toTableId") String toTableId) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			String catalogId=VcForCatalogTableRelationVb.getCatalogId();
			String fromTableId=VcForCatalogTableRelationVb.getFromTableId();
			String toTableId=VcForCatalogTableRelationVb.getToTableId();
			if((!ValidationUtil.isValid(catalogId)) && (catalogId=="") || (!ValidationUtil.isValid(fromTableId)) && (fromTableId=="")  || (!ValidationUtil.isValid(toTableId)) && (toTableId=="")) {
				 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Parameters are missing",null); 
			} else {
				List<VcForCatalogTableRelationVb> queryList = designAnalysisWb.getQueryTreeColumnsRelations(VcForCatalogTableRelationVb);
				if(queryList.size()>0) {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, queryList.size()+" Relation listed for Catalog table Column",
							queryList);
				} else {
					 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Catalog Tree Column Relations found",
								null); 
				}
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------SAVE REPORT SERVICE-------------------------------------------*/
	@RequestMapping(path = "/save", method = RequestMethod.POST)
	@ApiOperation(value = "Adding/Saving designed query", notes = "Add and Save the Design Query ", response = ResponseEntity.class)
	/*public ResponseEntity<JSONExceptionCode> saveReport(@RequestBody VcForQueryReportFieldsWrapperVb vcForQueryReportFieldsWrapperVb,@RequestParam("hashArr") String[] hashArr, 
			@RequestParam("hashValArr") String[] hashValArr ) throws DataAccessException, SAXException {*/
	public ResponseEntity<JSONExceptionCode> saveReport(@RequestBody VcForQueryReportFieldsWrapperVb vObjectMain) throws DataAccessException, SAXException {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode=new ExceptionCode();
		try {
			Set<String> uniqueTableIdSet = new HashSet<String>();
			
			for(VcForQueryReportFieldsVb rCReportFieldsVb: vObjectMain.getReportFields()) {
				if(ValidationUtil.isValid(rCReportFieldsVb.getTabelId())) {
					uniqueTableIdSet.add(rCReportFieldsVb.getTabelId());
				}
			}
			
			vObjectMain.getMainModel().setBaseTableId(designAnalysisWb.returnBaseTableId(vObjectMain.getMainModel().getCatalogId()));
			if(!ValidationUtil.isValid(vObjectMain.getMainModel().getTableName()))
			exceptionCode = designAnalysisWb.executeCatalogQuery(vObjectMain, uniqueTableIdSet.stream().collect(Collectors.toList()), false, true);
			else {
				exceptionCode.setErrorCode(1);
			}
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				String hashArray[] = vObjectMain.getHashArray();
				if(hashArray!=null && hashArray.length>0) {
					String hashValueArray[] = vObjectMain.getHashValueArray();
					
					StringBuffer hashScript = new StringBuffer();
					int index = 0;
					for (String hashVar : hashArray) {
						hashScript.append("{"+hashVar+":#CONSTANT$@!"+hashValueArray[index]+"#}");
						index++;
					}
					vObjectMain.getMainModel().setHashVariableScript(String.valueOf(hashScript));
				}
				
				exceptionCode = designAnalysisWb.saveReport(vObjectMain);
				if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo(),vObjectMain);
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,"Saved Successfully" ,exceptionCode,vObjectMain);
				}
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo(),vObjectMain);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------LOAD REPORT SERVICE-------------------------------------------*/
	@RequestMapping(path = "/loadReport", method = RequestMethod.POST)
	@ApiOperation(value = "Load ReportIds", notes = "Load ReportIds for Specific Catalog", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> loadReport(@RequestBody DesignAnalysisVb designAnalysisVb) { //@RequestParam("catalogueId") String catalogueId
		JSONExceptionCode jsonExceptionCode = null;
		try {
				ExceptionCode exceptionCode = designAnalysisWb.getReportNamesForCatalog(designAnalysisVb);	
				List<VcForQueryReportVb> result =(List<VcForQueryReportVb>) exceptionCode.getResponse();
				if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo());
				}else {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, result.size()+ " Report Loaded" ,exceptionCode.getResponse());
				}
		
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------JOIN FORMATION SERVICE-------------------------------------------*/
	@RequestMapping(path = "/returnJoinCondition", method = RequestMethod.POST)
	@ApiOperation(value = "Return JoinCondition", notes = "Return JoinCondition for Query logic", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> returnJoinCondition(@RequestBody VcForQueryReportFieldsWrapperVb vcForQueryReportFieldsWrapperVb ) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode=new ExceptionCode();
		try {
			Set<String> uniqueTableIdSet = new HashSet<String>();
			VcForQueryReportVb vcForQueryReportVb=vcForQueryReportFieldsWrapperVb.getMainModel();
			for(VcForQueryReportFieldsVb rCReportFieldsVb: vcForQueryReportFieldsWrapperVb.getReportFields()) {
				if(ValidationUtil.isValid(rCReportFieldsVb.getTabelId())){
					uniqueTableIdSet.add(rCReportFieldsVb.getTabelId());
				}
			}
			
			Integer joinType= designAnalysisWb.designAnalysisDao.getJoinTypeInCatalog(vcForQueryReportVb.getCatalogId());
			/* Query needed data from Vc_Relations Table */
			ArrayList getDataAL = designAnalysisWb.designAnalysisDao.getData(vcForQueryReportVb.getCatalogId(),joinType);
			
			vcForQueryReportVb.setBaseTableId(designAnalysisWb.returnBaseTableId(vcForQueryReportVb.getCatalogId()));
			exceptionCode = designAnalysisWb.returnJoinCondition(vcForQueryReportVb, uniqueTableIdSet.stream().collect(Collectors.toList()), joinType, getDataAL);
			if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,"DynamicJoinFormation and TableId's" ,exceptionCode.getResponse());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}	
	
	/*-------------------------------------EXECUTE REPORT SERVICE-------------------------------------------*/
	@RequestMapping(path = "/execute", method = RequestMethod.POST)
	@ApiOperation(value = "Execute designed Query Report", notes = "Load Design Query ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> executeReport(@RequestBody VcForQueryReportFieldsWrapperVb vObjectMain) throws DataAccessException, SAXException {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode=null;
		try {
			
			Set<String> uniqueTableIdSet = new HashSet<String>();
			
			for(VcForQueryReportFieldsVb rCReportFieldsVb: vObjectMain.getReportFields()) {
				if(ValidationUtil.isValid(rCReportFieldsVb.getTabelId())){
					uniqueTableIdSet.add(rCReportFieldsVb.getTabelId());
				}
			}
			
			vObjectMain.getMainModel().setBaseTableId(designAnalysisWb.returnBaseTableId(vObjectMain.getMainModel().getCatalogId()));
			exceptionCode = designAnalysisWb.executeCatalogQuery(vObjectMain, uniqueTableIdSet.stream().collect(Collectors.toList()), true, false);
			
			if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getResponse(),vObjectMain);
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,"Table Executed Successfully" ,exceptionCode,vObjectMain);//.getResponse()
			}
		
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------ANALYSIS LIST SERVICE-------------------------------------------*/
	@RequestMapping(path="/getAnalysis",method=RequestMethod.GET)
	@ApiOperation(value="List Analysis",notes="Listing all Analysis by usergroup,userprofile and userid",response=ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAnalysislist(){
		JSONExceptionCode jsonExceptionCode=null;
		ExceptionCode exceptionCode=new ExceptionCode();
		try {
			List<VcForQueryReportVb> arrListResult=designAnalysisWb.getAnalysisList();
			if (arrListResult.size()<=0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg()," No list found ");
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,arrListResult.size()+ " List of Analysis" ,arrListResult);//.getResponse()
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode,HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode=new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode,HttpStatus.OK);
		}
	}
	
	/*-------------------------------------REPORTS LIST SERVICE-------------------------------------------*/
	@RequestMapping(path="/getReports",method=RequestMethod.GET)
	@ApiOperation(value="List Reports",notes="Listing all Reports by usergroup,userprofile and userid",response=ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getReportsList(){
		JSONExceptionCode jsonExceptionCode=null;
		ExceptionCode exceptionCode=new ExceptionCode();
		try {
			List<VcForQueryReportVb> arrListResult=designAnalysisWb.getReportsList();
			if (arrListResult.size()<=0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg()," No list found ");
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,arrListResult.size()+ " Reports" ,arrListResult);//.getResponse()
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode,HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode=new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode,HttpStatus.OK);
		}
	}
	
	/*-------------------------------------INTERACTIVE DASHBOARDS LIST SERVICE-------------------------------------------*/
	@RequestMapping(path="/getInteractiveDashboard",method=RequestMethod.GET)
	@ApiOperation(value="Listing from Interactive Dashboard",notes="Listing all from Interactive Dashboard",response=ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getInteractiveDashboardList(){
		JSONExceptionCode jsonExceptionCode=null;
		ExceptionCode exceptionCode=new ExceptionCode();
		try {
			List<Map<String, Object>> arrListResult=designAnalysisWb.getInteractiveDashboardList();
			if (arrListResult.size()<=0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg()," No data found ");
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,arrListResult.size()+ " Sequences for Interactive Dashboard" ,arrListResult);//.getResponse()
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode,HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode=new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode,HttpStatus.OK);
		}
	}

	@RequestMapping(path="/getMagnefierData",method=RequestMethod.POST)
	@ApiOperation(value="Get Data For Magnefier Function",notes="Get Data For Magnefier Function",response=ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getMagnefierData(@RequestBody DesignAndAnalysisMagnifierVb magnefierVb){
		JSONExceptionCode jsonExceptionCode=null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			DCManualQueryVb dcManualQueryVb = new DCManualQueryVb();
			dcManualQueryVb.setQueryId(magnefierVb.getQueryId());
			List<DCManualQueryVb> queryList = dcManualQueryWb.getSpecificManualQuery(dcManualQueryVb);
			if(queryList.size()==0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No records found", dcManualQueryVb);
			} else {
				dcManualQueryVb = queryList.get(0);
				
				if(!queryList.get(0).getQueryColumnXML().contains("<name>"+magnefierVb.getUseColumn()+"</name>")
						&& !queryList.get(0).getQueryColumnXML().contains("<name>"+magnefierVb.getDisplayColumn()+"</name>")) {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg()," Column names requested are invalid ");
				} else {
					exceptionCode = CommonUtils.formHashList(dcManualQueryVb);
					if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
						String[] hashArr = (String[]) exceptionCode.getOtherInfo();
						String[] hashValArr = (String[]) exceptionCode.getRequest();
						String dbScript = dcManualQueryWb.getDbScript(dcManualQueryVb.getDatabaseConnectivityDetails());
						if(ValidationUtil.isValid(dbScript)) {
							exceptionCode =  designAnalysisWb.executeManualQueryForManifierDetails(dcManualQueryVb, dbScript, hashArr, hashValArr, magnefierVb);
							if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
								jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "List Specific Query", exceptionCode.getResponse());
							} else {
								jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg()," Problem in executing query ");
							}
						} else {
							jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg()," Problem in fetching connection configuration for variable : "+dcManualQueryVb.getDatabaseConnectivityDetails());
						}
					} else {
						jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg()," Problem with Hashvariable ");
					}
				}
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode,HttpStatus.OK);
		}catch(RuntimeCustomException rex){
			jsonExceptionCode=new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,rex.getMessage(),"");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode,HttpStatus.OK);
		}
	}
	
	@RequestMapping(path = "/getHashVariableListForSavedReport", method = RequestMethod.POST)
	@ApiOperation(value = "Execute designed Query Report", notes = "Load Design Query ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getHashVariableListForSavedReport(@RequestBody VcForQueryReportVb vObjectMain) throws DataAccessException, SAXException {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode=null;
		try {
			exceptionCode = designAnalysisWb.getReportDetailFromReportDefs(vObjectMain);
			if(exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION) {
				VcForQueryReportFieldsWrapperVb wrapperVb = (VcForQueryReportFieldsWrapperVb) exceptionCode.getResponse();
				Map<String, String> hashVariableMap = new HashMap<String, String>();
				if(ValidationUtil.isValid(wrapperVb.getMainModel().getHashVariableScript())) {
					Matcher regexMatcher = Pattern.compile("\\{(.*?):#(.*?)\\$@!(.*?)\\#}", Pattern.DOTALL).matcher(wrapperVb.getMainModel().getHashVariableScript());
					while (regexMatcher.find()) {
						hashVariableMap.put(regexMatcher.group(1), regexMatcher.group(3));
					}
				}
				exceptionCode.setResponse(hashVariableMap);
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "",exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getResponse());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	@RequestMapping(path = "/executeSaved", method = RequestMethod.POST)
	@ApiOperation(value = "Execute designed Query Report", notes = "Load Design Query ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> executeSavedReport(@RequestBody VcForQueryReportFieldsWrapperVb vObjectMain) throws DataAccessException, SAXException {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode=null;
		try {
			
			VcForQueryReportVb reportVb = vObjectMain.getMainModel();
			exceptionCode = designAnalysisWb.getReportDetailFromReportDefs(reportVb);
			VcForQueryReportFieldsWrapperVb wrapperVb = (VcForQueryReportFieldsWrapperVb) exceptionCode.getResponse();
			if(exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION) {
				wrapperVb.setHashArray(vObjectMain.getHashArray());
				wrapperVb.setHashValueArray(vObjectMain.getHashValueArray());
				wrapperVb.getMainModel().setStartIndex(vObjectMain.getMainModel().getStartIndex());
				wrapperVb.getMainModel().setLastIndex(vObjectMain.getMainModel().getLastIndex());
			}
			
			Set<String> uniqueTableIdSet = new HashSet<String>();
			
			for(VcForQueryReportFieldsVb rCReportFieldsVb: wrapperVb.getReportFields()) {
				if(ValidationUtil.isValid(rCReportFieldsVb.getTabelId())){
					uniqueTableIdSet.add(rCReportFieldsVb.getTabelId());
				}
			}
			wrapperVb.getMainModel().setTableName(vObjectMain.getMainModel().getTableName());
			wrapperVb.getMainModel().setBaseTableId(designAnalysisWb.returnBaseTableId(wrapperVb.getMainModel().getCatalogId()));
			exceptionCode = designAnalysisWb.executeCatalogQuery(wrapperVb, uniqueTableIdSet.stream().collect(Collectors.toList()), true, false);
			
			if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getResponse(),wrapperVb);
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,"Table Executed Successfully" ,exceptionCode,wrapperVb);//.getResponse()
			}
		
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	@RequestMapping(path = "/getHashVariableList", method = RequestMethod.POST)
	@ApiOperation(value = "Get Hash Variable Listing", notes = "Get Hash Variable Listing Based On Selected Columns ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getHashVariableList(@RequestBody VcForQueryReportFieldsWrapperVb vObjectMain) throws DataAccessException, SAXException {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode=null;
		try {
			Set<String> uniqueTableIdSet = new HashSet<String>();
			
			for(VcForQueryReportFieldsVb rCReportFieldsVb: vObjectMain.getReportFields()) {
				if(ValidationUtil.isValid(rCReportFieldsVb.getTabelId())){
					uniqueTableIdSet.add(rCReportFieldsVb.getTabelId());
				}
			}
			
			vObjectMain.getMainModel().setBaseTableId(designAnalysisWb.returnBaseTableId(vObjectMain.getMainModel().getCatalogId()));
			exceptionCode = designAnalysisWb.returnHashVariableListing(vObjectMain, uniqueTableIdSet.stream().collect(Collectors.toList()));
			
			if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,"Table Executed Successfully" ,exceptionCode);//.getResponse()
			}
		
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	@RequestMapping(path = "/getReportListCatalogBased", method = RequestMethod.POST)
	@ApiOperation(value = "Get Hash Variable Listing", notes = "Get Hash Variable Listing Based On Selected Columns ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getReportListCatalogBased(@RequestBody VcForQueryReportVb vObject) throws DataAccessException, SAXException {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode=null;
		try {
			exceptionCode = designAnalysisWb.getReportDetailListingFromReportDefs(vObject);
			if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,"Table Executed Successfully" ,exceptionCode);//.getResponse()
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	@RequestMapping(path = "/loadReportMetaData", method = RequestMethod.POST)
	@ApiOperation(value = "Execute designed Query Report", notes = "Load Design Query ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> loadReportMetaData(@RequestBody VcForQueryReportVb vObjectMain) throws DataAccessException, SAXException {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode=null;
		try {
			exceptionCode = designAnalysisWb.getReportDetailFromReportDefs(vObjectMain);
			if(exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "",exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getResponse());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	@RequestMapping(path = "/deleteDesignQuery", method = RequestMethod.POST)
	@ApiOperation(value = "Delete Design Query", notes = "Delete Design Query", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> deleteDesignQuery(@RequestBody DesignAnalysisVb designVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = designAnalysisWb.deleteDesignQuery(designVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Delete Successful",
						exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						null);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (Exception rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	

	/*-------------------------------------SMART SEARCH CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/smartSearchFilter", method = RequestMethod.POST, 	consumes="application/json", produces = "application/json")
	@ApiOperation(value = "Smart Search Filter", notes = "Smart search for all columns displayed in grid", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> smartSearchFilter(@RequestBody DesignAnalysisVb designVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<DesignAnalysisVb> queryList = designAnalysisWb.getQuerySmartSearchFilter(designVb);
			if(queryList.size()>0) {
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
	
	@RequestMapping(path = "/generateDynamicDate", method = RequestMethod.POST)
	@ApiOperation(value = "generate dynamic date with parameters", notes = "generate Dynamic Date", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> generateDynamicDate(@RequestBody VcForQueryReportFieldsVb vObjectMain) throws DataAccessException, SAXException {
		JSONExceptionCode jsonExceptionCode = null;
		String dynamciDate1=null;
		String dynamciDate2=null;
		try {
			
			if(ValidationUtil.isValid(vObjectMain.getDynamicStartFlag()) && vObjectMain.getDynamicStartFlag().equalsIgnoreCase("y")) {
				dynamciDate1 = designAnalysisWb.generateDynamicDate(vObjectMain.getDynamicStartDate(),vObjectMain.getDynamicStartOperator(),vObjectMain.getDynamicDateFormat(),vObjectMain.getJavaFormatDesc());
			}
			if(ValidationUtil.isValid(vObjectMain.getDynamicEndFlag()) && vObjectMain.getDynamicEndFlag().equalsIgnoreCase("y")) {
			    dynamciDate2 = designAnalysisWb.generateDynamicDate(vObjectMain.getDynamicEndDate(),vObjectMain.getDynamicEndOperator(),vObjectMain.getDynamicDateFormat(),vObjectMain.getJavaFormatDesc());
			}
			vObjectMain.setValue1(dynamciDate1);
			vObjectMain.setValue2(dynamciDate2);
			if(dynamciDate1 != null || dynamciDate2 !=null) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "",vObjectMain);
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Not a Valid Date ",vObjectMain);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK); 
		}
	}
}