/**
 * TagList and DataSource Connector CRUD
**/
package com.vision.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.ConnectorFileUploadMapperVb;
import com.vision.vb.DCManualQueryVb;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.DsConnectorLODWrapperVb;
import com.vision.vb.FileInfoVb;
import com.vision.wb.DCManualQueryWb;
import com.vision.wb.DSConnectorWb;
import com.vision.wb.LevelOfDisplayWb;
import com.vision.wb.VcConfigMainWb;
import com.vision.wb.VisionUploadWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/dataSourceConnector")
@Api(value="/dataSourceConnector",description="Handling DataSource Connector")
public class DSConnectorController {

	@Autowired
	private DSConnectorWb dsConnectorWb;

	@Autowired
	private DCManualQueryWb dcManualQueryWb;
	
	@Autowired
	private VisionUploadWb visionUploadWb;

	@Autowired
	private VcConfigMainWb vcConfigMainWb;
	
	@Autowired
	LevelOfDisplayWb levelOfDisplayWb;
	
	/*-------------------------------------CONNECTOR LIST SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getAll", method = RequestMethod.POST)
	@ApiOperation(value = "Get All Data Connectors",notes = "Returns list of all Data Connectors from Vision Dynamic Hash Var",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAllDataConnector(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<DSConnectorVb> dbScriptPopList = dsConnectorWb.getAllDataConnectors(dsConnectorVb);
			if(dbScriptPopList==null || dbScriptPopList.size()==0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No data found", null, dsConnectorVb);
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Data Connector Listing", dbScriptPopList, dsConnectorVb);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------GET SPECIFIC CONNECTOR SERVICE-------------------------------------------*/
	@RequestMapping(path = "/get", method = RequestMethod.POST)
	@ApiOperation(value = "Get Data Connector", notes = "Returns list of specific data connector", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getSpecificDsConnector(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			if (!ValidationUtil.isValid(dsConnectorVb.getMacroVar())
					&& !ValidationUtil.isValid(dsConnectorVb.getScriptType())) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Parameters are missing", "");
				return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
			}
			if ("M_QUERY".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				DCManualQueryVb vObj = new DCManualQueryVb();
				vObj.setQueryId(dsConnectorVb.getMacroVar());
				List<DCManualQueryVb> queryList = dcManualQueryWb.getSpecificManualQuery(vObj);
				if (queryList.size() == 0) {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No data found", vObj);
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "List Specific Connector",
							queryList, vObj);
				}
			} else if ("MACROVAR".equalsIgnoreCase(dsConnectorVb.getScriptType())
					|| "DATABASE".equalsIgnoreCase(dsConnectorVb.getScriptType())
					|| "FILE".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				DSConnectorVb vObj = new DSConnectorVb();
				vObj.setMacroVar(dsConnectorVb.getMacroVar().toUpperCase());
				List<DSConnectorVb> queryList = dsConnectorWb.getSpecificConnectorByHashList(vObj);
				if (queryList.size() == 0) {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No data found", vObj);
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "List Specific Connector",
							queryList, vObj);
				}
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------PREVIEW CONNECTOR SERVICE-------------------------------------------*/
	@RequestMapping(path = "/previewData", method = RequestMethod.POST)
	@ApiOperation(value = "Listing File Columns and Data", notes = "Returns list of data and columns ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> previewData(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		Map<String, Object> returnMap = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			if (ValidationUtil.isValid(dsConnectorVb.getMacroVar())) {
				if ("FILE".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
					List<ConnectorFileUploadMapperVb> connectorFileUploadMapperList = dsConnectorWb.getDsConnectorDao()
							.getConnectorFileUploadMapperDetails(dsConnectorVb);

					if (ValidationUtil.isValidList(connectorFileUploadMapperList)) {
						returnMap = new HashMap<String, Object>();
						returnMap.put("TABLENAME", connectorFileUploadMapperList.get(0).getFileTableName());
						List tblNamesList = new ArrayList<>(connectorFileUploadMapperList.size());
						for (ConnectorFileUploadMapperVb connectorFileUploadMapperVb : connectorFileUploadMapperList) {
							tblNamesList.add(connectorFileUploadMapperVb.getFileTableName());
						}
						returnMap.put("TABLENAMES", tblNamesList);

						exceptionCode = dsConnectorWb.returnTableDataForConnectorFileMapper(connectorFileUploadMapperList.get(0).getSelfBiMappingTableName(), returnMap);

					}
				}
			}
			if (exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
						"Preview file data for " + dsConnectorVb.getMacroVar() + "", exceptionCode.getResponse());
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
	
	/*-------------------------------------PREVIEW SPECIFIC TABLE DATA FOR CONNECTOR SERVICE-------------------------------------------*/
	@RequestMapping(path = "/previewSpecificTableData", method = RequestMethod.POST) //table name - sheet name
	@ApiOperation(value = "Listing File Columns and Data for specific table", notes = "Returns list of data and columns ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> previewDataForSpecificTable(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		Map<String, Object> returnMap = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			if (ValidationUtil.isValid(dsConnectorVb.getMacroVar())) {
				List<ConnectorFileUploadMapperVb> connectorFileUploadMapperList = dsConnectorWb.getDsConnectorDao()
						.getConnectorFileUploadMapperDetails(dsConnectorVb);

				if (ValidationUtil.isValidList(connectorFileUploadMapperList)) {
					returnMap = new HashMap<String, Object>();
					returnMap.put("TABLENAME", dsConnectorVb.getTableName());
					List tblNamesList = new ArrayList<>(connectorFileUploadMapperList.size());
					for (ConnectorFileUploadMapperVb connectorFileUploadMapperVb : connectorFileUploadMapperList) {
						tblNamesList.add(connectorFileUploadMapperVb.getFileTableName());
					}
					returnMap.put("TABLENAMES", tblNamesList);
					String currentDynamicTableName = connectorFileUploadMapperList.stream().filter(vb -> dsConnectorVb.getTableName().equalsIgnoreCase(vb.getFileTableName())).findFirst().get().getSelfBiMappingTableName();
					if(ValidationUtil.isValid(currentDynamicTableName))
						exceptionCode = dsConnectorWb.returnTableDataForConnectorFileMapper(currentDynamicTableName, returnMap);
					else
						exceptionCode.setErrorMsg("No records for table "+dsConnectorVb.getTableName());
				}
			}
			if (exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
						"Preview file data for " + dsConnectorVb.getMacroVar() + "", exceptionCode.getResponse());
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
	
	/*-------------------------------------CONNECTOR TAGLIST SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getTagListDbCon", method = RequestMethod.POST)
	@ApiOperation(value = "Get Tag list",notes = "Returns a tag list from Macrovar_tagging",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getTagListDbCon(@RequestBody DSConnectorVb  dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<DSConnectorVb> dbScriptPopList = dsConnectorWb.getDisplayTagList(dsConnectorVb.getMacroVarType());
			if(dbScriptPopList.size()>0) {
				 jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Display specific macro tag list", dbScriptPopList,dsConnectorVb);
		    }else {
			 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Results Found",
						null);
		    }
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------ADD CONNECTOR SERVICE-------------------------------------------*/
	@RequestMapping(path = "/add", method = RequestMethod.POST)
	@ApiOperation(value = "Add Data Connector",notes = "Add a data connector to Vision Dynamic Hash Var",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> addConnector(@RequestBody DSConnectorVb dsConnectorVb) throws ParseException {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			String macroVarScript= dsConnectorWb.dynamicScriptCreation(dsConnectorVb);
		    dsConnectorVb.setMacroVarScript(macroVarScript);
			ExceptionCode exceptionCode = dsConnectorWb.insertRecordForDSConnector(dsConnectorVb);
			if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo());
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	/*-------------------------------------Test CONNECTOR SERVICE-------------------------------------------*/
	@RequestMapping(path = "/test", method = RequestMethod.POST)
	@ApiOperation(value = "Test Data Connector",notes = "Test a data connector",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> testConnector(@RequestBody DSConnectorVb dsConnectorVb)
			throws ParseException {
		JSONExceptionCode jsonExceptionCode = null;
		Connection con = null;
		try {
			String macroVarScript= dsConnectorWb.dynamicScriptCreation(dsConnectorVb);
			ExceptionCode exceptionCode = CommonUtils.getConnection(macroVarScript);
			con = (Connection) exceptionCode.getResponse();
			if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
			}
		}

	}


	/*-------------------------------------MODIFY/UPDATE CONNECTOR SERVICE-------------------------------------------*/
	@RequestMapping(path = "/modify", method = RequestMethod.POST)
	@ApiOperation(value = "Update Data Connector",notes = "Updates a data connector",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> modifyConnector(@RequestBody DSConnectorVb dsConnectorVb){
		JSONExceptionCode jsonExceptionCode  = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			String macroVarScript= dsConnectorWb.dynamicScriptCreation(dsConnectorVb);
		    dsConnectorVb.setMacroVarScript(macroVarScript);
			exceptionCode = dsConnectorWb.modifyRecordForDSConnector(dsConnectorVb);
			exceptionCode.setActionType("Modify");
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
	
	/*-------------------------------------Delete specific data Connector -------------------------------------------*/
	@RequestMapping(path = "/delete", method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete Data Connector",notes = "Deletes a data connector",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> deleteConnector(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = null;
		try {
			if ("M_QUERY".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				DCManualQueryVb vObj = new DCManualQueryVb();
				vObj.setQueryId(dsConnectorVb.getMacroVar());
				vObj.setPreviousActionType("isNotValid");
				exceptionCode = dcManualQueryWb.deleteManualQuery(dsConnectorVb);
			} else if ("MACROVAR".equalsIgnoreCase(dsConnectorVb.getScriptType())
					|| "DATABASE".equalsIgnoreCase(dsConnectorVb.getScriptType()) || "FILE".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				exceptionCode = dsConnectorWb.deleteRecordForDSConnector(dsConnectorVb);
			}
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
	
	/*-------------------------------------TEST/Validate DATASOURCE CONNECTOR SERVICE-------------------------------------------*/
	@RequestMapping(path = "/validate", method = RequestMethod.POST)
	@ApiOperation(value = "Validate Data Connector",notes = "Validates a datasource connector connectivity ",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> testDbConnectivity(@RequestParam(name="connectorName") String connectorName) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = null;
		try {
			String macroVarName = dsConnectorWb.getScriptValue(connectorName);
			exceptionCode = dsConnectorWb.testConnection(macroVarName);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getOtherInfo());
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-------------------------------------CONNECTOR LIST SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getAllValidConnectors", method = RequestMethod.GET)
	@ApiOperation(value = "Get All Data Connectors",notes = "Returns list of all Data Connectors from Vision Dynamic Hash Var",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAllValidConnectors() {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List list = dsConnectorWb.getAllValidConnectors();
			 if(list.size()>0) {
				 jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Data Connector Listing", list);
			 }else {
				 jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Results Found", null);
			 }
			
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-------------------------------------Upload Files Rest Service-------------------------------*/
	@RequestMapping(path = "/uploadFilesToFtp", method = RequestMethod.POST)
	@ApiOperation(value = "Uploading Multipart files to Server", notes = "Upload files to Server and store data to vision dynamic hash", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> uploadFilesToFtp(@RequestParam("file") MultipartFile[] files,
			@RequestParam("macroVar") String macroVar, @RequestParam("macroVarDesc") String macroVarDesc,
			@RequestParam("delimiter") String delimiter,@RequestParam("headerCheck") char HeaderCheck) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			DSConnectorVb vObject = new DSConnectorVb();
			if (files.length <= 0) {
				return new ResponseEntity<JSONExceptionCode>(
						new JSONExceptionCode(Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload file error", null),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				macroVar=macroVar.toUpperCase();
				vObject.setMacroVar(macroVar);
				vObject.setDescription(macroVarDesc);
				vObject.setDelimiter(delimiter);
				vObject.setHeaderCheck(HeaderCheck);
				DSConnectorVb dsConnectorVb = dsConnectorWb.getSpecificConnector(vObject);
				if (dsConnectorVb!=null && ValidationUtil.isValid(dsConnectorVb.getMacroVarScript())) {
					return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,
							"Connector Name is already Used", exceptionCode.getOtherInfo(), vObject), HttpStatus.OK);
				}
				exceptionCode = dsConnectorWb.uploadAndUpdateRecordVisionDynamicHash(dsConnectorVb, files, vObject, true);
				/* Old code without Transactional rollback - Changed by DD
				 * exceptionCode = visionUploadWb.uploadMultipartFile(files, lsData, macroVar);
				if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					FileInfoVb fileVb = (FileInfoVb) exceptionCode.getOtherInfo();
					exceptionCode = dsConnectorWb.insertModifyRecordToVisionDynamicHash(vObject, fileVb);
				}*/
			}
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "File Upload Success",
						exceptionCode.getOtherInfo(), vObject);
			} else {
				if (!ValidationUtil.isValid(exceptionCode.getErrorMsg()))
					exceptionCode.setErrorMsg("Failed to upload file and Table Dynamic upload Failed");
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo(), vObject);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (Exception e) {
			if (!ValidationUtil.isValid(e.getMessage()))
				exceptionCode.setErrorMsg("Failed to upload file");
			else
				exceptionCode.setErrorMsg(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<JSONExceptionCode>(
					new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), null),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/*-------------------------------------Modify Vision Dynamic -------------------------------*/
	@RequestMapping(path = "/modifyVisionDynamic", method = RequestMethod.POST)
	@ApiOperation(value = "Modify Vision dynamic hash data based on connector",notes = "Modify Vision dynamic hash data based on connector",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> modifyVisionDynamic(@RequestParam("macroVar") String macroVar,@RequestParam("macroVarDesc") String macroVarDesc){
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try{
		   DSConnectorVb vObject =new DSConnectorVb();
		   vObject.setMacroVar(macroVar);
		   vObject.setDescription(macroVarDesc);
		   DSConnectorVb lsData = dsConnectorWb.getSpecificConnector(vObject); 
		   if(lsData!=null &&  ValidationUtil.isValid(lsData.getMacroVarScript())) {
		   	if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION){
				vObject.setScriptType("FILE");
				FileInfoVb fileInfoVb = new FileInfoVb();
				exceptionCode=dsConnectorWb.insertModifyRecordToVisionDynamicHash(vObject, fileInfoVb);
	    	}
		   }
		   if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
			   jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Modify Vision Dynamic hash", exceptionCode.getOtherInfo(), vObject);
			}else {
			   jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Modify Failed to Vision Dynamic hash",exceptionCode.getOtherInfo(), vObject);
			}
		   return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch (Exception e) {
			RuntimeCustomException ex = (RuntimeCustomException)e;
			e.printStackTrace();
			return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, ex.getMessage(),null), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/*-------------------------------------List Files Rest Service-------------------------------*/
	@RequestMapping(path = "/listFilesFromConnectors", method = RequestMethod.GET)
	@ApiOperation(value = "Listing Uploaded files",notes = "Listing files from Connector folders",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> listingFilesFromConnectors(@RequestParam("macroVar") String macroVar){
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try{
			if(ValidationUtil.isValid(macroVar)) {
				macroVar=macroVar.toUpperCase();
				exceptionCode = visionUploadWb.listFilesFromConnectors(macroVar);
		    }
		    if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
			   jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "List Files", exceptionCode.getResponse(), null);
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Fail to list file",exceptionCode.getResponse(), null);
			}
		   return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch (Exception e) {
			RuntimeCustomException ex = (RuntimeCustomException)e;
			e.printStackTrace();
			return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, ex.getMessage(),null), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/*-------------------------------------Download Files Rest Service-------------------------------*/
	@RequestMapping(path = "/downloadFilesFromConnector", method = RequestMethod.POST)
	@ApiOperation(value = "Downloading files", notes = "Download files", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> downloadFilesFromConnector(@QueryParam("fileName") String fileName,@QueryParam("connectorDir") String connectorDir,
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			FileInfoVb fileInfoVb = new FileInfoVb();
			if(ValidationUtil.isValid(fileName) && ValidationUtil.isValid(connectorDir)) {
				String fileExtension = FilenameUtils.getExtension(fileName).toLowerCase();
				fileInfoVb.setName(fileName);
				fileInfoVb.setExtension(fileExtension);
				ExceptionCode exceptionCode = visionUploadWb.fileDownloadFromConnector(fileInfoVb,connectorDir);
				if(exceptionCode.getErrorCode() == 1) {
					request.setAttribute("fileExtension", fileExtension);
					request.setAttribute("fileName", fileName);
					request.setAttribute("filePath", exceptionCode.getResponse());
					visionUploadWb.setExportXlsServlet(request, response);
					if(response.getStatus() == 404) {
						jsonExceptionCode =	new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "File not found", null);
					}else{
						jsonExceptionCode =	new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", response);
					}
				}else{
					jsonExceptionCode =	new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Unable to read file", null);
				}
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.FILE_UPLOAD_REMOTE_ERROR, "Parameters are missing", null);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			return new ResponseEntity<JSONExceptionCode>(
					new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), null),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/*-------------------------------------XmlToJson Rest Service-------------------------------*/
	@RequestMapping(path = "/xmlToJson", method = RequestMethod.POST)
	@ApiOperation(value = "XML To JSON",notes = "XML To JSON Convertion",response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> xmlToJson(@RequestParam("file") MultipartFile[] file){
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try{
			 if (file.length<=0) {
				   return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.FILE_UPLOAD_REMOTE_ERROR, "XML Upload File is missing",null), HttpStatus.EXPECTATION_FAILED);
			 }else {			
				exceptionCode=visionUploadWb.xmlToJson(file);
			 }
		    if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Converted XML To JSON", exceptionCode.getResponse(), null);
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,exceptionCode.getErrorMsg() ,exceptionCode.getResponse(), null);
			}
		   return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}catch (Exception e) {
			RuntimeCustomException ex = (RuntimeCustomException)e;
			e.printStackTrace();
			return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, ex.getMessage(),null), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@RequestMapping(path = "/saveLevelOfDisplay", method = RequestMethod.POST)
	@ApiOperation(value = "Data Source - Level Of Display", notes = "save level of display for Data Source", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> levelOfDisplay(@RequestBody DsConnectorLODWrapperVb dsLODWrapperVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			DSConnectorVb dsConnectorVb = dsLODWrapperVb.getMainModel();
			if ("M_QUERY".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				exceptionCode = dsConnectorWb.doInsertRecordForAccessControl(dsLODWrapperVb, "M_QUERY", true);
			} else if ("MACROVAR".equalsIgnoreCase(dsConnectorVb.getScriptType())
					|| "DATABASE".equalsIgnoreCase(dsConnectorVb.getScriptType())
					|| "FILE".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				exceptionCode = dsConnectorWb.doInsertRecordForAccessControl(dsLODWrapperVb, "CONNECTOR", true);
			}else if ("D_QUERY".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				exceptionCode = dsConnectorWb.doInsertRecordForAccessControl(dsLODWrapperVb, "D_QUERY", true);
			}
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Share Successful",
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
	
	
	/*-------------------------------------Upload Files Rest Service-------------------------------*/
	@RequestMapping(path = "/modifyUploadFilesToFtp", method = RequestMethod.POST)
	@ApiOperation(value = "Uploading Multipart files to Server", notes = "Upload files to Server and store data to vision dynamic hash", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> modifyUploadFilesToFtp(@RequestParam("file") MultipartFile[] files,
			@RequestParam("macroVar") String macroVar, @RequestParam("macroVarDesc") String macroVarDesc,
			@RequestParam("delimiter") String delimiter,@RequestParam("headerCheck") char headerCheck) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			DSConnectorVb vObject = new DSConnectorVb();
			macroVar=macroVar.toUpperCase();
			vObject.setMacroVar(macroVar);
			vObject.setDescription(macroVarDesc);
			vObject.setDelimiter(delimiter);
			vObject.setHeaderCheck(headerCheck);
			if (files.length <= 0) {
				exceptionCode = dsConnectorWb.modifyRecordToVisionDynamicHashWithoutFile(vObject);
			} else {
				DSConnectorVb dsConnectorVb = dsConnectorWb.getSpecificConnector(vObject);
				if (dsConnectorVb!=null && ValidationUtil.isValid(dsConnectorVb.getMacroVarScript()) && !"FILE".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
					return new ResponseEntity<JSONExceptionCode>(new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,
							"Not a valid file upload connector", exceptionCode.getOtherInfo(), vObject), HttpStatus.OK);
				}
				exceptionCode = dsConnectorWb.uploadAndUpdateRecordVisionDynamicHash(dsConnectorVb, files, vObject, false);
			}
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "File Upload Success",
						exceptionCode.getOtherInfo(), vObject);
			} else {
				if (!ValidationUtil.isValid(exceptionCode.getErrorMsg()))
					exceptionCode.setErrorMsg("Failed to upload file");
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo(), vObject);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (Exception e) {
			if (!ValidationUtil.isValid(e.getMessage()))
				exceptionCode.setErrorMsg("Failed to upload file");
			else
				exceptionCode.setErrorMsg(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<JSONExceptionCode>(
					new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), null),
					HttpStatus.OK);
		}
	}
	
	/*-------------------------------------SMART SEARCH CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/smartSearchFilter", method = RequestMethod.POST, 	consumes="application/json", produces = "application/json")
	@ApiOperation(value = "Smart Search Filter", notes = "Smart search for all columns displayed in grid", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> smartSearchFilter(@RequestBody DSConnectorVb dsConnectorVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<DSConnectorVb> queryList = dsConnectorWb.getQuerySmartSearchFilter(dsConnectorVb);
			if(queryList.size()>0) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "List Filtered Connectors",queryList,dsConnectorVb);	
			}else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No records found",dsConnectorVb);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	
	@RequestMapping(path = "/getLevelOfDisplay", method = RequestMethod.POST)
	@ApiOperation(value = "Retrive Data Source - Level Of Display", notes = "Get level of display for Data Source", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getLevelOfDisplay(@RequestBody DsConnectorLODWrapperVb dsLODWrapperVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			DSConnectorVb dsConnectorVb = dsLODWrapperVb.getMainModel();
			if ("M_QUERY".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				exceptionCode = dsConnectorWb.getLODForDataSource(dsLODWrapperVb, "M_QUERY");
			} else if ("MACROVAR".equalsIgnoreCase(dsConnectorVb.getScriptType())
					|| "DATABASE".equalsIgnoreCase(dsConnectorVb.getScriptType())
					|| "FILE".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				exceptionCode = dsConnectorWb.getLODForDataSource(dsLODWrapperVb, "CONNECTOR");
			}else if ("D_QUERY".equalsIgnoreCase(dsConnectorVb.getScriptType())) {
				exceptionCode = dsConnectorWb.getLODForDataSource(dsLODWrapperVb, "D_QUERY");
			} 
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
}