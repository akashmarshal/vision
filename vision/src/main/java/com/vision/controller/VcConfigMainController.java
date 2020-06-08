package com.vision.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.ConnectorFileUploadMapperVb;
import com.vision.vb.DCManualQueryVb;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.DesignAnalysisVb;
import com.vision.vb.FileInfoVb;
import com.vision.vb.UserRestrictionVb;
import com.vision.vb.VcConfigMainCRUDVb;
import com.vision.vb.VcConfigMainLODWrapperVb;
import com.vision.vb.VcConfigMainTreeVb;
import com.vision.vb.VcConfigMainVb;
import com.vision.vb.VcMainDataSourceMetaDataVb;
import com.vision.wb.DCManualQueryWb;
import com.vision.wb.DSConnectorWb;
import com.vision.wb.LevelOfDisplayWb;
import com.vision.wb.VcConfigMainWb;
import com.vision.wb.VisionUploadWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "configCatalog")
@Api(value = "configCatalog", description = "Operations pertaining to Catalog Configuration")
public class VcConfigMainController {

	@Autowired
	private VcConfigMainWb vcConfigMainWb;
	@Autowired
	private DSConnectorWb dsConnectorWb;
	@Autowired
	private DCManualQueryWb dcManualQueryWb;
	@Autowired
	private VisionUploadWb visionUploadWb;


	/*-------------------------------------GET ALL VISION CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getAll", method = RequestMethod.POST)
	@ApiOperation(value = "Catalog Listing", notes = "Get a list of catalog from source, Vision Catalog", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAllVcCatalog(@RequestBody VcConfigMainVb vcConfigMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<VcConfigMainVb> queryList = vcConfigMainWb.getQueryPopupVcConfigMain(vcConfigMainVb);
			if(queryList==null || queryList.size()==0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No data found", null, vcConfigMainVb);
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Catalog Listing", queryList,vcConfigMainVb);
			}
			
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------GET SPECIFIC VISION CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/get", method = RequestMethod.POST)
	@ApiOperation(value = "Get catalog related details", notes = "Get Database connectivity and Catalog tables details for specific catalog", response = ResponseEntity.class)
	/*public ResponseEntity<JSONExceptionCode> editSpecificVcCatalog(@RequestParam("catalogId") String catalogId,
			@RequestParam("vcStatus") int vcStatus) {*/
	public ResponseEntity<JSONExceptionCode> editSpecificVcCatalog(@RequestBody VcConfigMainVb vcConfigMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			vcConfigMainVb.setRecordIndicator(0);
			ExceptionCode exceptionCode = vcConfigMainWb.getQueryResultsVcConfigMain(vcConfigMainVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getResponse());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------Defining Connector Specific Tables SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getConnectorTables", method = RequestMethod.POST)
	@ApiOperation(value = "Listing Connector Tables", notes = "Returns list of data connector tables", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getConnectorTables(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		List<VcMainDataSourceMetaDataVb> treeStructuredCols = new ArrayList<VcMainDataSourceMetaDataVb>();

		try {
			if("M_QUERY".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				JSONArray result = new JSONArray();
				JSONObject obj =new JSONObject();
				obj.put("name",dsConnectorVb.getMacroVar());
				obj.put("id",1);
				result.add( obj );
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
						"Defining tables for " + dsConnectorVb.getMacroVar() + "", result);
			} else {
				DSConnectorVb vObj = dsConnectorWb.getSpecificConnector(dsConnectorVb);
				if (ValidationUtil.isValid(vObj.getMacroVarScript())) {
					List<VcConfigMainTreeVb> treeVbList = null;
					if(dsConnectorVb.getDynamicScript() != null && dsConnectorVb.getDynamicScript().length>0) {
						treeVbList = new ArrayList<VcConfigMainTreeVb>(dsConnectorVb.getDynamicScript().length);
						for(Object obj : dsConnectorVb.getDynamicScript()) {
							HashMap<Object, Object> sam = (HashMap<Object, Object>) obj;
							VcConfigMainTreeVb treeVb = new VcConfigMainTreeVb();
							for(Map.Entry entry : sam.entrySet()) {
								String key = String.valueOf(entry.getKey());
								try {
									Method setter = VcConfigMainTreeVb.class.getMethod("set"+key.substring(0,1).toUpperCase()
											+key.substring(1,key.length()), String.class);
									setter.invoke(treeVb, String.valueOf(entry.getValue()));
								} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								}
							}
							treeVbList.add(treeVb);
						}
					}
					
					
					if ("DATABASE".equalsIgnoreCase(vObj.getScriptType())) {
						String dbScript = vcConfigMainWb.getDbScript(dsConnectorVb.getMacroVar());
						exceptionCode = vcConfigMainWb.formConnectorTables(dsConnectorVb.getMacroVar(), dbScript, treeVbList);
					} else if ("FILE".equalsIgnoreCase(vObj.getScriptType())) {
						List<ConnectorFileUploadMapperVb> connectorFileUploadMapperList = dsConnectorWb.getDsConnectorDao()
								.getConnectorFileUploadMapperDetails(dsConnectorVb);
						if (ValidationUtil.isValidList(connectorFileUploadMapperList)) {
							JSONArray result = new JSONArray();
							int index = 1;
							for (ConnectorFileUploadMapperVb connectorFileUploadMapperVb : connectorFileUploadMapperList) {
								JSONObject obj =new JSONObject();
								obj.put("name",connectorFileUploadMapperVb.getFileTableName());
								obj.put("id",index);
								result.add(obj);
								index++;
							}
							exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
							exceptionCode.setOtherInfo(result);
						}
					}
				}
				if (ValidationUtil.isValid(vObj.getMacroVarScript()) && exceptionCode != null
						&& exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
							"Defining tables for " + dsConnectorVb.getMacroVar() + "", exceptionCode.getOtherInfo());
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
							exceptionCode.getOtherInfo());
				}
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException | SecurityException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------Defining Connector Specific Views SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getConnectorViews", method = RequestMethod.POST)
	@ApiOperation(value = "Listing Connector Views", notes = "Returns list of data connector views", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getConnectorViews(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			String dbScript = vcConfigMainWb.getDbScript(dsConnectorVb.getMacroVar());
			exceptionCode = vcConfigMainWb.formConnectorViews(dsConnectorVb.getMacroVar(), dbScript);
			if (exceptionCode != null && exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
						"Defining views for " + dsConnectorVb.getMacroVar() + "", exceptionCode.getOtherInfo());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------Defining Connector Specific Table and its specific columns SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getConnectorTableCols", method = RequestMethod.POST)
	@ApiOperation(value = "Listing Connector table columns", notes = "Returns list of data connector table columns", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getConnectorTableCols(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			if("M_QUERY".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				exceptionCode = vcConfigMainWb.getColumnMetaDataScriptForVcQueries(dsConnectorVb.getMacroVar());
				if(exceptionCode != null && exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
							"Defining tables and columns for " + dsConnectorVb.getMacroVar() + "",exceptionCode.getResponse(),
							dsConnectorVb);
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
							dsConnectorVb);
				}
			} else {
				
				DSConnectorVb vObj = dsConnectorWb.getSpecificConnector(dsConnectorVb);
				if (ValidationUtil.isValid(vObj.getMacroVarScript())) {
					if ("DATABASE".equalsIgnoreCase(vObj.getScriptType())) {
						String dbScript = vcConfigMainWb.getDbScript(dsConnectorVb.getMacroVar());
						exceptionCode = vcConfigMainWb.formConnectorTableSpecificCols(dsConnectorVb.getMacroVar(), dbScript,dsConnectorVb.getTableName());
					} else if ("FILE".equalsIgnoreCase(vObj.getScriptType())) {
						List<ConnectorFileUploadMapperVb> connectorFileUploadMapperList = dsConnectorWb.getDsConnectorDao()
								.getConnectorFileUploadMapperDetails(dsConnectorVb);
						if (ValidationUtil.isValidList(connectorFileUploadMapperList)) {
							String dynamicTableName = connectorFileUploadMapperList.stream()
									.filter(vb -> dsConnectorVb.getTableName().equalsIgnoreCase(vb.getFileTableName()))
									.findFirst().get().getSelfBiMappingTableName();
							exceptionCode = dsConnectorWb.getColumnNamesForDynamicTableName(dsConnectorVb, dynamicTableName);
						}
					}
				}
				
				if (exceptionCode != null && exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					List<VcMainDataSourceMetaDataVb> treeStructuredCols = (List<VcMainDataSourceMetaDataVb>) exceptionCode.getOtherInfo();
					if (treeStructuredCols.size() > 0) {
						jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
								"Defining tables and columns for " + dsConnectorVb.getMacroVar() + "",
								exceptionCode.getOtherInfo());
					} else {
						jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Records Found",
								exceptionCode.getOtherInfo());
					}
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
							exceptionCode.getOtherInfo());
				}
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------Defining Connector Specific Views and its specific columns SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getConnectorViewCols", method = RequestMethod.POST)
	@ApiOperation(value = "Listing Connector view columns", notes = "Returns list of data connector view columns", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getConnectorViewCols(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			String dbScript = vcConfigMainWb.getDbScript(dsConnectorVb.getMacroVar());
			exceptionCode = vcConfigMainWb.formConnectorViewSpecificCols(dsConnectorVb.getMacroVar(), dbScript,
					dsConnectorVb.getTableName());
			if (exceptionCode != null && exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				List<VcMainDataSourceMetaDataVb> treeStructuredCols = (List<VcMainDataSourceMetaDataVb>) exceptionCode
						.getOtherInfo();
				if (treeStructuredCols.size() > 0) {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
							"Defining views and columns for " + dsConnectorVb.getMacroVar() + "",
							exceptionCode.getOtherInfo());
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Records Found",
							exceptionCode.getOtherInfo());
				}
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-----------------------------------------User Group SERVICE------------------------------------------------------------------------------------*/
	@RequestMapping(path = "/userGroup", method = RequestMethod.GET)
	@ApiOperation(value = "User Group Listing", notes = "Returns list of user group from profile privileges", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getUserGroup() {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<Object> queryList = vcConfigMainWb.getQueryUserGroup();
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Listing User Group", queryList);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	@RequestMapping(path = "/saveLevelOfDisplay", method = RequestMethod.POST)
	@ApiOperation(value = "Catalog - Level Of Display", notes = "save level of display for catalog", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> levelOfDisplay(@RequestBody VcConfigMainLODWrapperVb vcConfigMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			ExceptionCode exceptionCode = vcConfigMainWb.doInsertRecordForAccessControl(vcConfigMainVb, true);

			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Access Control - Successful",
						null);
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						null);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------ADD VISION CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/add", method = RequestMethod.POST)
	@ApiOperation(value = "Add Catalog", notes = "Add a catalog and level of display to vision catalog and catalog access", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> addVcCatalog(@RequestBody VcConfigMainVb vcConfigMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			vcConfigMainVb.setRecordIndicator(0);
			ExceptionCode exceptionCode = vcConfigMainWb.insertRecord(vcConfigMainVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------MODIFY VISION CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/modify", method = RequestMethod.POST)
	@ApiOperation(value = "Modify Catalog", notes = "Modify a catalog and level of display to vision catalog and catalog access", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> modifyVcCatalog(@RequestBody VcConfigMainVb vcConfigMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			vcConfigMainVb.setRecordIndicator(0);
			ExceptionCode exceptionCode = vcConfigMainWb.modifyRecord(vcConfigMainVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------DELETE VISION CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/delete", method = RequestMethod.POST)
	@ApiOperation(value = "Delete Catalog", notes = "Delete a catalog and level of display to vision catalog and catalog access", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> deleteVcCatalog(@RequestBody VcConfigMainVb vcConfigMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			vcConfigMainVb.setRecordIndicator(0);
			ExceptionCode exceptionCode = vcConfigMainWb.deleteRecord(vcConfigMainVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------Defining Connector Specific Table and its specific columns SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getConnectorTableWithColsInfo", method = RequestMethod.POST)
	@ApiOperation(value = "Listing Connector table columns", notes = "Returns list of data connector table columns", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getConnectorTableWithCols(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			DSConnectorVb vObj = dsConnectorWb.getSpecificConnector(dsConnectorVb);
			if(vObj!=null && ValidationUtil.isValid(vObj.getMacroVarScript())){
				String dbScript = vObj.getMacroVarScript();
				if("FILE".equalsIgnoreCase(vObj.getScriptType())) {
					  if(ValidationUtil.isValid(dbScript)) {
						    FileInfoVb fileInfoVb= new FileInfoVb();
						    fileInfoVb.setName(CommonUtils.getValue(dbScript, "NAME"));
						    fileInfoVb.setExtension(CommonUtils.getValue(dbScript, "EXTENSION"));
						    fileInfoVb.setDelimiter(CommonUtils.getValue(dbScript, "DELIMITER"));
						    fileInfoVb.setDate(String.valueOf(visionUploadWb.getDateTimeInMS(vObj.getDateLastModified(), "dd-M-yyyy hh:mm:ss")));
						    exceptionCode = dsConnectorWb.processFile(fileInfoVb, vObj,"");
					  }
				}else if("MACROVAR".equalsIgnoreCase(vObj.getScriptType()) || "DATABASE".equalsIgnoreCase(vObj.getScriptType())) {
					exceptionCode = vcConfigMainWb.formConnectorTablesAndColumns(dsConnectorVb.getMacroVar(), dbScript);					
				}
			}else {
				DCManualQueryVb dcManualQueryVb = new DCManualQueryVb();
				dcManualQueryVb.setQueryId(dsConnectorVb.getMacroVar());
				dcManualQueryVb.setDatabaseType("MACROVAR");
				List<DCManualQueryVb> queryList = dcManualQueryWb.getSpecificManualQuery(dcManualQueryVb);
				if (queryList.size() > 0) {
					List<VcMainDataSourceMetaDataVb> returnList = new ArrayList<VcMainDataSourceMetaDataVb>();
					dcManualQueryVb =queryList.get(0);
					if ("TRUE".equalsIgnoreCase(dcManualQueryVb.getQueryValidFlag())) {	
						List<VcMainDataSourceMetaDataVb> treeStructuredChild = new ArrayList<VcMainDataSourceMetaDataVb>();
						Matcher colMatcher = Pattern.compile("\\<column\\>(.*?)\\<\\/column\\>", Pattern.DOTALL)
								.matcher(queryList.get(0).getQueryColumnXML());
						List<VcMainDataSourceMetaDataVb> childList = new ArrayList<VcMainDataSourceMetaDataVb>();
						while (colMatcher.find()) {
							String colData = colMatcher.group(1);
							String style = CommonUtils.getValueForXmlTag(colData, "name");
							String type = CommonUtils.getValueForXmlTag(colData, "type");
							VcMainDataSourceMetaDataVb vObjColDSMD = new VcMainDataSourceMetaDataVb();
							vObjColDSMD.setTableName(dsConnectorVb.getMacroVar());
							vObjColDSMD.setColumnName(style);
							vObjColDSMD.setColumnType(type);
							treeStructuredChild.add(vObjColDSMD);
		        			childList.add(new VcMainDataSourceMetaDataVb(dsConnectorVb.getMacroVar(), style, null));
						}
						if (treeStructuredChild.size() > 0) {
							returnList.add(new VcMainDataSourceMetaDataVb(dsConnectorVb.getMacroVar(), "",
									childList));
						}
		    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", returnList);
						return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
					} else {
						jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Not a valid query",
								dsConnectorVb);
						return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
					}
				}
			}
			if (exceptionCode != null && exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				List<VcMainDataSourceMetaDataVb> treeStructuredCols = (List<VcMainDataSourceMetaDataVb>) exceptionCode.getOtherInfo();
				if (treeStructuredCols.size() > 0) {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
							"Defining tables and columns for " + dsConnectorVb.getMacroVar() + "",exceptionCode.getOtherInfo());
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Records Found",exceptionCode.getOtherInfo());
				}
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------SMART SEARCH CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/smartSearchFilter", method = RequestMethod.POST, 	consumes="application/json", produces = "application/json")
	@ApiOperation(value = "Smart Search Filter", notes = "Smart search for all columns displayed in grid", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> smartSearchFilter(@RequestBody VcConfigMainVb vcConfigMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<VcConfigMainVb> queryList = vcConfigMainWb.getQuerySmartSearchFilter(vcConfigMainVb);
			if(queryList.size()>0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "List Filtered Catalog",queryList,vcConfigMainVb);	
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No records found",vcConfigMainVb);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------LIST ALIAS CATALOG TABLES BY CATALOG ID QUERY SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getTableColumnData", method = RequestMethod.POST)
	@ApiOperation(value = "Listing of Catalog TABLE & COLUMN DATA by tableid and Catalog id", notes = "Returns list of Tables and column details based on Catalog& table id", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getTableColumn(@RequestBody VcConfigMainTreeVb vcTreeVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			VcConfigMainTreeVb treeVb = vcConfigMainWb.getTableColumnByTableCatalogId(vcTreeVb);
			if(treeVb != null && treeVb.getChildren()!=null && treeVb.getChildren().size()>0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, treeVb.getChildren().size()+" Columns listed for Table ID : "+vcTreeVb.getTableId(),treeVb);
			} else {
				 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Tables Column Data found for the CatalogId :"+vcTreeVb.getCatalogId(),null); 
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------LIST TABLE AND COLUMN ALIAS NAME BY CATALOG ID SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getTableColumnAlias", method = RequestMethod.POST)
	@ApiOperation(value = "Listing of Catalog TABLE & COLUMN ALIAS by Catalog id", notes = "Returns list of Tables and column Alais names based on Catalog id", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getTableColumnAlias(@RequestBody VcConfigMainVb VcConfigMainVb){
		JSONExceptionCode jsonExceptionCode = null;
		try {
			String catalogId=VcConfigMainVb.getCatalogId();
			Integer status=VcConfigMainVb.getVcStatus();
			if((!ValidationUtil.isValid(catalogId)) || (!ValidationUtil.isValid(status))){
				 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "CatalogId/Status parameter is missing",null); 
			}else {
				List queryList = vcConfigMainWb.getTableColumnAlias(VcConfigMainVb);
				if(queryList.size()>0) {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, queryList.size()+ " Tables Column listed for Catalog",queryList);
				 }else {
					 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Tables Columns found for this Catalog :"+catalogId,null); 
				 }
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	
	/*-------------------------------------GET SPECIFIC VISION CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getDistinctCatalogSrc", method = RequestMethod.POST)
	@ApiOperation(value = "Get distinct catalog source with its type", notes = "Get distinct catalog source with its type specific to catalog id and status", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getDistinctCatalogSrc(@RequestBody VcConfigMainVb vcConfigMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			vcConfigMainVb.setRecordIndicator(0);
			ExceptionCode exceptionCode = vcConfigMainWb.getQueryResultsForDistinctCatalogSrc(vcConfigMainVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getResponse());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	@RequestMapping(path = "/getRestrictionTree", method = RequestMethod.GET)
	@ApiOperation(value = "Get user restriction tree", notes = "Get user restriction tree from ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getRestrictionTree() {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			ExceptionCode exceptionCode = new ExceptionCode();
			List<UserRestrictionVb> restrictionList;
			try {
				restrictionList = vcConfigMainWb.getVcConfigMainDao().getRestrictionTree();
				exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
				exceptionCode.setResponse(restrictionList);
			} catch(Exception e) {
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg(e.getMessage());
			}
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getResponse());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------SaveAll ADD/MODIFY/DELETE - table/Column/Relation SERVICE-------------------------------------------*/
	@RequestMapping(value = "/saveAll", method = RequestMethod.POST)
	@ApiOperation(value = "Save All Catalog Table Column & Relation properties", notes = "Saves all Catalog table column relation properties", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> saveAllVcTableColumnRelation(
			@RequestBody VcConfigMainCRUDVb vcMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = vcConfigMainWb.doSaveOperationsWIP(vcMainVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Catalog save successful",null);
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),null);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------GET SPECIFIC VISION CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getRelationDetailsForCatalog", method = RequestMethod.POST)
	@ApiOperation(value = "Get full relation details for a catalog", notes = "Get full relation details based on catalog id and status", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getRelationDetailsForCatalog(@RequestBody VcConfigMainVb vcConfigMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			vcConfigMainVb.setRecordIndicator(0);
			ExceptionCode exceptionCode = vcConfigMainWb.getRelationDetailsForCatalog(vcConfigMainVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getResponse());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	@RequestMapping(path = "/getLevelOfDisplay", method = RequestMethod.POST)
	@ApiOperation(value = "Retrive Data Source - Level Of Display", notes = "Get level of display for Data Source", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getLevelOfDisplay(@RequestBody VcConfigMainLODWrapperVb vcConfigMainLODWrapperVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = vcConfigMainWb.getLODForCatalog(vcConfigMainLODWrapperVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Share Successful",
						exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						null);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-----------------------------------------Publish Catalog SERVICE------------------------------------------------------------------------------------*/
	@RequestMapping(path = "/catalogPublish", method = RequestMethod.POST)
	@ApiOperation(value = "Catalog Publish", notes = "Move work in progress data to audit for publish", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> catalogPublish(@RequestBody VcConfigMainVb vcConfigMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = vcConfigMainWb.doPublishToMain(vcConfigMainVb);
			if (exceptionCode != null && exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	

	@RequestMapping(path = "/deleteCatalog", method = RequestMethod.POST)
	@ApiOperation(value = "Delete Catalog ", notes = "Delete Catalog", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> deleteCatalog(@RequestBody VcConfigMainVb catalogVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = vcConfigMainWb.deleteCatalog(catalogVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Delete Successful",
						exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						null);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
}