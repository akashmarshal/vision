package com.vision.wb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import com.vision.dao.AbstractDao;
import com.vision.dao.CommonDao;
import com.vision.dao.DSConnectorDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.DeepCopy;
import com.vision.util.ValidationUtil;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.DsConnectorLODWrapperVb;
import com.vision.vb.FileInfoVb;
import com.vision.vb.LevelOfDisplayUserVb;
import com.vision.vb.LevelOfDisplayVb;
import com.vision.vb.VcConfigMainVb;
import com.vision.vb.VcMainDataSourceMetaDataVb;

@Component
public class DSConnectorWb extends AbstractDynaWorkerBean<DSConnectorVb> {
	 
	@Autowired
	public DSConnectorDao dsConnectorDao;
	
	@Autowired
	private VcConfigMainWb vcConfigMainWb;
	
	
	@Autowired
	private CommonDao commonDao;
	
	@Autowired
	private AllFormatsUploadWb allFormatsUploadWb;
	
	@Autowired
	private VisionUploadWb visionUploadWb;
	
	@Override
	protected AbstractDao<DSConnectorVb> getScreenDao() {
		return dsConnectorDao;
	}
	
	@Override
	protected void setVerifReqDeleteType(DSConnectorVb vObject){
		vObject.setStaticDelete(false);
		vObject.setVerificationRequired(false);
	}
	@Override
	protected void setAtNtValues(DSConnectorVb vObject){
		vObject.setRecordIndicatorNt(7);
		vObject.setRecordIndicator(0);
		vObject.setVcrTypeNt(1056);
		vObject.setScriptTypeAt(1083);
		vObject.setVcrStatusNt(1);
	}
	public List<DSConnectorVb> getAllDataConnectors(DSConnectorVb vObj) {
		try {
			return dsConnectorDao.getAllDataConnectorsLists(vObj);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public List<DSConnectorVb> getSpecificConnectorByHashList(DSConnectorVb vObj) {
		List<DSConnectorVb> dbScriptPopList = null;		
		try {
			vObj = dsConnectorDao.getSpecificConnectorDetails(vObj);
			if(vObj !=null) {
				if("FILE".equalsIgnoreCase(vObj.getScriptType())) {
					dbScriptPopList = new ArrayList<DSConnectorVb>();
				    vObj.setFileName(CommonUtils.getValue(vObj.getMacroVarScript(), "NAME"));
				    vObj.setExtension(CommonUtils.getValue(vObj.getMacroVarScript(), "EXTENSION"));
				    vObj.setDelimiter(CommonUtils.getValue(vObj.getMacroVarScript(), "DELIMITER"));
				    dbScriptPopList.add(vObj);
				}else {
					vObj.setMacroVarType(CommonUtils.getValue(vObj.getMacroVarScript(),"DATABASE_TYPE"));
					if(!ValidationUtil.isValid(vObj.getMacroVarType())) {
						throw new RuntimeCustomException("Data not maintained properly");
					}else {
						dbScriptPopList = getDisplayTagList(vObj.getMacroVarType());
						if (ValidationUtil.isValid(vObj.getMacroVarScript()) && ("MACROVAR".equalsIgnoreCase(vObj.getScriptType()) || "DATABASE".equalsIgnoreCase(vObj.getScriptType()))) {
							for(DSConnectorVb data:dbScriptPopList) {
								data.setTagValue(CommonUtils.getValue(vObj.getMacroVarScript(),data.getTagName()));
								data.setMacroVarType(vObj.getMacroVarType());
								data.setMacroVar(vObj.getMacroVar());
								data.setDescription(vObj.getDescription());
							}
						}
					}
				}
			}
			return dbScriptPopList;
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public DSConnectorVb getSpecificConnector(DSConnectorVb vObj) {
		try {
			return dsConnectorDao.getSpecificConnectorDetails(vObj);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public String getScriptValue(String getMacroVar) {
		try {
			return commonDao.getScriptValue(getMacroVar);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public List<DSConnectorVb> getDisplayTagList(String macroVarType) {
		try {
			return dsConnectorDao.getDisplayTagList(macroVarType);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	public List getAllValidConnectors() {
		try {
			return dsConnectorDao.getAllValidConnectors();
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	public ExceptionCode processFile(FileInfoVb fileInfoVb, DSConnectorVb dsConnectorVb, String method){
		ExceptionCode exceptionCode = new ExceptionCode();
		JSONExceptionCode jsonExceptionCode = null;
		try {
			String uploadFilePathFromDB = getVisionVariableValue("VISION_CONNECTOR_DATA_FILE_PATH");
			//uploadFilePathFromDB = "C:\\connectorsData\\"; 
			if (uploadFilePathFromDB != null && !uploadFilePathFromDB.isEmpty()) {
				File file = new File(uploadFilePathFromDB + fileInfoVb.getName() + "_" + dsConnectorVb.getMaker() + "_" + dsConnectorVb.getMacroVar() + "_" + fileInfoVb.getDate() + "." + fileInfoVb.getExtension());				// String fileExtension = FilenameUtils.getExtension(fileName);
				byte[] bFile = Files.readAllBytes(file.toPath());
				fileInfoVb.setData(bFile);
				jsonExceptionCode = allFormatsUploadWb.processFile(fileInfoVb,method);
				dsConnectorVb.setTableSize(fileInfoVb.getTableSize());
			}
			if (!ValidationUtil.isValid(uploadFilePathFromDB)
					|| jsonExceptionCode.getStatus() != Constants.SUCCESSFUL_OPERATION) {
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg("Problem in processing the file");
				if (!ValidationUtil.isValid(uploadFilePathFromDB)) {
					exceptionCode.setErrorMsg("Upload directory not maintained");
				}

				return exceptionCode;
			} else {
				exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
				exceptionCode.setOtherInfo(jsonExceptionCode.getResponse());
				return exceptionCode;
			}
		}catch(Exception e){
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg("Problem in processing the file - Cause:"+e.getMessage());
			return exceptionCode;
		}
	}
	public ExceptionCode processFileForDataInsert(FileInfoVb fileInfoVb, DSConnectorVb dsConnectorVb, String method) {
		try {
			ExceptionCode exceptionCode = new ExceptionCode();
			JSONExceptionCode jsonExceptionCode = null;
			String uploadFilePathFromDB = getVisionVariableValue("VISION_CONNECTOR_DATA_FILE_PATH");
     	//	uploadFilePathFromDB = "C:\\connectorsData\\";   
			if (uploadFilePathFromDB != null && !uploadFilePathFromDB.isEmpty()) {
				File file = new File(uploadFilePathFromDB + fileInfoVb.getName() + "_" + dsConnectorVb.getMaker() + "_" + dsConnectorVb.getMacroVar() + "_" + fileInfoVb.getDate() + "." + fileInfoVb.getExtension());				// String fileExtension = FilenameUtils.getExtension(fileName);
				byte[] bFile = Files.readAllBytes(file.toPath());
				fileInfoVb.setData(bFile);
				fileInfoVb.setMacroVar(dsConnectorVb.getMacroVar());
				jsonExceptionCode = allFormatsUploadWb.processFileForDataInsert(fileInfoVb,method);
				dsConnectorVb.setTableSize(fileInfoVb.getTableSize());
			}
			if (!ValidationUtil.isValid(uploadFilePathFromDB) || jsonExceptionCode.getStatus() != Constants.SUCCESSFUL_OPERATION) {
				throw new RuntimeCustomException(!ValidationUtil.isValid(uploadFilePathFromDB)?"Upload directory not maintained":jsonExceptionCode.getMessage());
			} else {
				exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
				exceptionCode.setOtherInfo(jsonExceptionCode.getResponse());
				return exceptionCode;
			}
		} catch(Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public String dynamicScriptCreation(DSConnectorVb dsConnectorVb) {
	    StringBuffer variableScript = new StringBuffer("{DATABASE_TYPE:#CONSTANT$@!"+dsConnectorVb.getMacroVarType()+"#}");
		JSONObject extractVbData = new JSONObject(dsConnectorVb);
		JSONArray eachColData = (JSONArray) extractVbData.getJSONArray("dynamicScript");
		for (int i = 0; i < eachColData.length(); i++) {
            JSONObject ss = eachColData.getJSONObject(i); 
            String ch=  fixJSONObject(ss);
			 JSONObject extractData = new JSONObject(ch);
            String tag = extractData.getString("TAG");
            String encryption = extractData.getString("ENCRYPTION");
            String value = extractData.getString("VALUE");
            variableScript.append("{"); 
            if(ValidationUtil.isValid(encryption) && encryption.equalsIgnoreCase("Yes")){
//            	value=ValidationUtil.passwordEncrypt(value);
            	variableScript.append(tag+":#ENCRYPT$@!"+value+"#");
            }else {
            	variableScript.append(tag+":#CONSTANT$@!"+value+"#");
            }
            variableScript.append("}");
         }
	    return String.valueOf(variableScript);
	}
	
	public static String fixJSONObject(JSONObject obj) {
	    String jsonString = obj.toString();
	    for(int i = 0; i<obj.names().length(); i++){
	        try{
	            jsonString=jsonString.replace(obj.names().getString(i),
	            obj.names().getString(i).toUpperCase());
	        } catch(JSONException e) {
	            e.printStackTrace();
	        }
	    }
	    return jsonString;
	}
	
	@Transactional(rollbackForClassName={"com.vision.exception.RuntimeCustomException"})
	public ExceptionCode uploadAndUpdateRecordVisionDynamicHash(DSConnectorVb lsData, MultipartFile[] files, DSConnectorVb vObject, boolean isAdd){
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			List<String> tablsToDropList = null;
			FileInfoVb fileInfoVb = new FileInfoVb();
			fileInfoVb.setHeaderCheck(vObject.getHeaderCheck());
			fileInfoVb.setDelimiter(vObject.getDelimiter());
			String curDate = commonDao.getSystemDate12Hr();
			for (MultipartFile uploadedFile : files) {
				String extension = FilenameUtils.getExtension(uploadedFile.getOriginalFilename());
				fileInfoVb.setName(uploadedFile.getOriginalFilename().toUpperCase());
				fileInfoVb.setData(uploadedFile.getBytes());
				fileInfoVb.setExtension(extension);
				fileInfoVb.setDate(curDate);
			}
			exceptionCode = insertModifyRecordToVisionDynamicHash(vObject, fileInfoVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				DsConnectorLODWrapperVb dsLODWrapperVb = new DsConnectorLODWrapperVb();
				dsLODWrapperVb.setMainModel(vObject);
				List<LevelOfDisplayUserVb> singleUserList = new ArrayList<LevelOfDisplayUserVb>(1);
				singleUserList.add(new LevelOfDisplayUserVb(((Long) vObject.getMaker()).intValue()));
				dsLODWrapperVb.setLodUserList(singleUserList); 
				int result = Constants.SUCCESSFUL_OPERATION;
				if(isAdd)
					result = dsConnectorDao.doInsertRecordForDataConnectorLOD(dsLODWrapperVb, true);
				if (result == Constants.SUCCESSFUL_OPERATION) {
					exceptionCode = visionUploadWb.uploadMultipartFile(files, lsData, vObject.getMacroVar(), curDate);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
						if (!ValidationUtil.isValid(exceptionCode.getErrorMsg()))
							exceptionCode.setErrorMsg("Failed to upload file");
					}
					if (!isAdd) {
						tablsToDropList = modifyDynamicallyCreatedTables(vObject);
					}
					ExceptionCode insertExceptionCode = null;
					DSConnectorVb vObj = getSpecificConnector(vObject);
					if (vObj != null && ValidationUtil.isValid(vObj)
							&& ValidationUtil.isValid(vObj.getMacroVarScript())) {
						if ("FILE".equalsIgnoreCase(vObj.getScriptType())) {
							String dbScript = vcConfigMainWb.getDbScript(vObject.getMacroVar());
							if (ValidationUtil.isValid(dbScript)) {
								fileInfoVb.setName(CommonUtils.getValue(dbScript, "NAME"));
								fileInfoVb.setExtension(CommonUtils.getValue(dbScript, "EXTENSION"));
								fileInfoVb.setDelimiter(CommonUtils.getValue(dbScript, "DELIMITER"));
								fileInfoVb.setDate(String.valueOf(visionUploadWb
										.getDateTimeInMS(CommonUtils.getValue(dbScript, "DATE"), "dd-M-yyyy hh:mm:ss")));
								fileInfoVb.setSheetName(vObject.getTableName());
								insertExceptionCode = processFileForDataInsert(fileInfoVb, vObj, "PREVIEW");
							}
						}
					}
					if (insertExceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
						if (ValidationUtil.isValidList(tablsToDropList)) {
							tablsToDropList.forEach(tableName -> {
								dsConnectorDao.dropTablesForUpload(tableName);
							});
						}
					} else {
						exceptionCode.setErrorMsg(insertExceptionCode.getErrorMsg());
						exceptionCode.setErrorCode(insertExceptionCode.getErrorCode());
						throw new RuntimeCustomException(insertExceptionCode.getErrorMsg());
					}
				} else {
						exceptionCode.setErrorMsg("File Upload failed and Table Dynamic upload failed");
					    throw new RuntimeCustomException(exceptionCode.getErrorMsg());
				}
			} else {
					exceptionCode.setErrorMsg("File Upload failed and Table Dynamic upload failed");
				    throw new RuntimeCustomException(exceptionCode.getErrorMsg());
			}
			return exceptionCode;
		}catch (RuntimeCustomException rcException) {
			throw rcException;
		}catch(Exception ex){
			logger.error("Error in Add.",ex);
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			throw new RuntimeCustomException(ex.getMessage());
		}
	}
	
	public ExceptionCode insertModifyRecordToVisionDynamicHash(DSConnectorVb vObject, FileInfoVb fileInfoVb){
		ExceptionCode exceptionCode  = null;
		try{
			DSConnectorVb lsData=dsConnectorDao.getSpecificConnectorDetails(vObject); 
			if(!ValidationUtil.isValid(vObject.getDelimiter())) {
				vObject.setDelimiter("");
			}
			if(ValidationUtil.isValid(fileInfoVb.getName())) {
				String fileName = fileInfoVb.getName();
				int pos = fileName.lastIndexOf(".");
				String justName = pos > 0 ? fileName.substring(0, pos) : fileName;
			    StringBuffer macroVarScript = new StringBuffer();
			    macroVarScript.append("{NAME:#CONSTANT$@!"+justName+"#}");
			    macroVarScript.append("{EXTENSION:#CONSTANT$@!"+fileInfoVb.getExtension()+"#}");
			    macroVarScript.append("{DELIMITER:#CONSTANT$@!"+vObject.getDelimiter()+"#}");
			    macroVarScript.append("{DATE:#CONSTANT$@!"+fileInfoVb.getDate()+"#}");
				vObject.setMacroVarScript(String.valueOf(macroVarScript));
				vObject.setDateCreation(fileInfoVb.getDate());
				vObject.setDateLastModified(fileInfoVb.getDate());
			}
			vObject.setScriptType("FILE");
			if(ValidationUtil.isValid(lsData) && ValidationUtil.isValid(lsData.getMacroVarScript())) {
				exceptionCode =dsConnectorDao.doUpdateApprRecord(vObject);
			}else if(ValidationUtil.isValid(vObject) && ValidationUtil.isValid(vObject.getMacroVarScript())) {
				exceptionCode =dsConnectorDao.doInsertApprRecord(vObject);
			}
			dsConnectorDao.fetchMakerVerifierNames(vObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Insert/Modify Exception In Vision Dynamic Hash Var" + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}
	}
	public ExceptionCode testConnection(String dbScript){
		ExceptionCode exceptionCode = new ExceptionCode();
		Connection con = null;
		try{
			exceptionCode = getConnection(dbScript);
			if(exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION){
				con = (Connection) exceptionCode.getResponse();
			}else{
				return exceptionCode;
			}
			if(con==null){
				exceptionCode.setErrorMsg("Undefined exception when trying to connect DB with given credentials");
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				return exceptionCode;
			}
		}catch(Exception e){
			e.printStackTrace();
			exceptionCode.setErrorMsg(e.getMessage());
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			return exceptionCode;
		}finally{
			try{
				if(con!=null){
					con.close();
					exceptionCode.setErrorMsg("Test connection successful");
					exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
					return exceptionCode;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		exceptionCode.setErrorMsg("Test connection successful");
		exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
		return exceptionCode;
	}
	
	public ExceptionCode getConnection(String dbScript){
		ExceptionCode exceptionCode = new ExceptionCode();
		Connection con = null;
		String dbIP = "";
		String jdbcUrl = "";
		String dbServiceName = CommonUtils.getValue(dbScript, "SERVICE_NAME");
		String dbOracleSid = CommonUtils.getValue(dbScript, "SID");
		String dbUserName = CommonUtils.getValue(dbScript, "USER");
		if(!ValidationUtil.isValid(dbUserName))
			dbUserName = CommonUtils.getValue(dbScript, "DB_USER");
		String dbPassWord = CommonUtils.getValue(dbScript, "PWD");
		if(!ValidationUtil.isValid(dbPassWord))
			dbPassWord = CommonUtils.getValue(dbScript, "DB_PWD");
		String dbPortNumber = CommonUtils.getValue(dbScript, "DB_PORT");
		String dataBaseName = CommonUtils.getValue(dbScript, "DB_NAME");
		String dataBaseType = CommonUtils.getValue(dbScript, "DATABASE_TYPE");
		String dbInstance = CommonUtils.getValue(dbScript, "DB_INSTANCE");
		String dbIp = CommonUtils.getValue(dbScript, "DB_IP");		
		String serverName = CommonUtils.getValue(dbScript, "SERVER_NAME");
		String version = CommonUtils.getValue(dbScript, "JAR_VERSION");
		
		String hostName = dbServiceName;
		if(!ValidationUtil.isValid(hostName)){
			hostName = dbOracleSid;
		}
		if(ValidationUtil.isValid(dbIp))
			dbIP = dbIp;
		try{
			if("ORACLE".equalsIgnoreCase(dataBaseType)){
				jdbcUrl = "jdbc:oracle:thin:@"+dbIP+":"+dbPortNumber+":"+hostName;
				con = dsConnectorDao.getDbConnection(jdbcUrl, dbUserName, dbPassWord, "ORACLE", version);
			}else if("MSSQL".equalsIgnoreCase(dataBaseType)){
				if(ValidationUtil.isValid(dbInstance) && ValidationUtil.isValid(hostName)){
					jdbcUrl = "jdbc:sqlserver://"+dbIP+":"+dbPortNumber+";instanceName="+dbInstance+";databaseName="+hostName;
				}else if(ValidationUtil.isValid(dbInstance) && !ValidationUtil.isValid(hostName)){
					jdbcUrl = "jdbc:sqlserver://"+dbIP+":"+dbPortNumber+";instanceName="+dbInstance;
				}else if(!ValidationUtil.isValid(dbInstance) && ValidationUtil.isValid(hostName)){
					jdbcUrl = "jdbc:sqlserver://"+dbIP+":"+dbPortNumber+";databaseName="+hostName;
				}else{
					jdbcUrl = "jdbc:sqlserver://"+dbIP+":"+dbPortNumber+";databaseName="+hostName;
				}
				con = dsConnectorDao.getDbConnection(jdbcUrl, dbUserName, dbPassWord, "MSSQL", version);
			}else if("MYSQL".equalsIgnoreCase(dataBaseType)){
				if(ValidationUtil.isValid(dbInstance) && ValidationUtil.isValid(dataBaseName)){
					jdbcUrl = "jdbc:mysql://"+dbIp+":"+dbPortNumber+"//instanceName="+dbInstance+"//databaseName="+dataBaseName;
				}else if(ValidationUtil.isValid(dbInstance) && !ValidationUtil.isValid(dataBaseName)){
					jdbcUrl = "jdbc:mysql://"+dbIp+":"+dbPortNumber+"//instanceName="+dbInstance;
				}else if(!ValidationUtil.isValid(dbInstance) && ValidationUtil.isValid(dataBaseName)){
					jdbcUrl = "jdbc:mysql://"+dbIp+":"+dbPortNumber+"/"+dataBaseName;
				}else{
					jdbcUrl = "jdbc:mysql://"+dbIp+":"+dbPortNumber+"/"+dataBaseName;
				}	
				con = dsConnectorDao.getDbConnection(jdbcUrl, dbUserName, dbPassWord, "MYSQL", version);
			}else if("POSTGRESQL".equalsIgnoreCase(dataBaseType)){
				jdbcUrl = "jdbc:postgresql://"+dbIP+":"+dbPortNumber+"/"+dataBaseName;
				con = dsConnectorDao.getDbConnection(jdbcUrl, dbUserName, dbPassWord, "POSTGRESQL", version);
			}else if("SYBASE".equalsIgnoreCase(dataBaseType)){
				jdbcUrl="jdbc:sybase:Tds:"+dbIP+":"+dbPortNumber+"?ServiceName="+hostName;
				con = dsConnectorDao.getDbConnection(jdbcUrl, dbUserName, dbPassWord, "SYBASE", version);
			}else if("INFORMIX".equalsIgnoreCase(dataBaseType)){
				jdbcUrl = "jdbc:informix-sqli://"+dbIP+":"+dbPortNumber+"/"+dataBaseName+":informixserver="+serverName;
				con = dsConnectorDao.getDbConnection(jdbcUrl, dbUserName, dbPassWord, "INFORMIX", version);
			}
			if(con==null){
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg("Problem in gaining connection with datasource");
				return exceptionCode;
			}
		}catch(ClassNotFoundException e){
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg("Problem in gaining connection - Cause:"+e.getMessage());
			return exceptionCode;
		} catch (SQLException e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg("Problem in gaining connection - Cause:"+e.getMessage());
			return exceptionCode;
		}catch(Exception e){
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg("Problem in gaining connection - Cause:"+e.getMessage());
			return exceptionCode;
		}
		exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
		exceptionCode.setResponse(con);
		return exceptionCode;
	}
	public ExceptionCode insertRecordForDSConnector(DSConnectorVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<DSConnectorVb> deepCopy = new DeepCopy<DSConnectorVb>();
		DSConnectorVb clonedObject = null;
		try{
			setAtNtValues(vObject);
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copy(vObject);
			doFormateData(vObject);
			exceptionCode = doValidate(vObject);
			if(exceptionCode!=null && exceptionCode.getErrorMsg()!=""){
				return exceptionCode;
			}
			exceptionCode = dsConnectorDao.doInsertApprRecordforDSConnector(vObject);
			getScreenDao().fetchMakerVerifierNames(vObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception In Data Connector" + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			return exceptionCode;
		}
	}

	public ExceptionCode modifyRecordForDSConnector(DSConnectorVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<DSConnectorVb> deepCopy = new DeepCopy<DSConnectorVb>();
		DSConnectorVb clonedObject = null;
		try{
			setAtNtValues(vObject);
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copy(vObject);
			exceptionCode = dsConnectorDao.doUpdateApprRecordforDSConnector(vObject);
			getScreenDao().fetchMakerVerifierNames(vObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Modify Exception In Data Connector" + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			rex.printStackTrace();
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			return exceptionCode;
		}
	}
	
	public ExceptionCode deleteRecordForDSConnector(DSConnectorVb vObject){
		ExceptionCode exceptionCode  = null;
			exceptionCode = dsConnectorDao.validateConnectorData(vObject);
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				dsConnectorDao.moveMainDataToAD(vObject);
				dsConnectorDao.doDeleteAppr(vObject);
				dsConnectorDao.doDeleteApprLevelOfDisplay(vObject);
				if(vObject.getScriptType().equalsIgnoreCase("file")) {
				dsConnectorDao.doDeleteMappingTable(vObject);
				}
				exceptionCode.setResponse(dsConnectorDao.getAllValidConnectors());
				exceptionCode.setErrorMsg("DELETE Successfull");
			}else {			
				exceptionCode.setResponse(exceptionCode.getResponse());
				exceptionCode.setErrorMsg("Cannot DELETE- connectors used in catalog");
			}
			return exceptionCode;
             			
		}
	public ExceptionCode modifyRecordToVisionDynamicHashWithoutFile(DSConnectorVb vObject){
		ExceptionCode exceptionCode  = new ExceptionCode();
		try{
			DSConnectorVb lsData=dsConnectorDao.getSpecificConnectorDetails(vObject);
			if(lsData!=null) {
				vObject.setMacroVarScript("{NAME:#CONSTANT$@!"+lsData.getFileName().substring(0, lsData.getFileName().indexOf("."))+"#}{EXTENSION:#CONSTANT$@!"+lsData.getExtension()+"#}"+"{DELIMITER:#CONSTANT$@!"+vObject.getDelimiter()+"#}"+"{DATE:#CONSTANT$@!"+lsData.getDateLastModified()+"#}");
			}
			vObject.setScriptType("FILE");
			if(ValidationUtil.isValid(lsData) && ValidationUtil.isValid(lsData.getMacroVarScript())) {
				int result = dsConnectorDao.doUpdateAppr(vObject);
				if(result != Constants.SUCCESSFUL_OPERATION) {
					exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				} else {
					exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
				}
			}else if(ValidationUtil.isValid(vObject) && ValidationUtil.isValid(vObject.getMacroVarScript())) {
				exceptionCode.setErrorCode(Constants.ATTEMPT_TO_MODIFY_UNEXISTING_RECORD);
			}
			dsConnectorDao.fetchMakerVerifierNames(vObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Insert/Modify Exception In Vision Dynamic Hash Var" + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}
	}
	
	public List<DSConnectorVb> getQuerySmartSearchFilter(DSConnectorVb queryPopupObj){
		List<DSConnectorVb> arrListLocal = new ArrayList<DSConnectorVb>();
		try{
			setVerifReqDeleteType(queryPopupObj);
			doFormateDataForQuery(queryPopupObj);
			List<DSConnectorVb> arrListResult = dsConnectorDao.getQuerySmartSearchFilter(queryPopupObj);
			if(arrListResult == null){
				//arrListLocal.add(queryPopupObj);
			}else{
				arrListLocal.addAll(arrListResult);
			}
			return arrListLocal;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Exception in getting the Connector Smart Search Filter results.", ex);
			return null;
		}
	}
	@Transactional(rollbackForClassName = { "com.vision.exception.RuntimeCustomException" })
	public ExceptionCode doInsertRecordForAccessControl(DsConnectorLODWrapperVb dsLODWrapperVb, String switchOption, boolean isMain) throws RuntimeCustomException {
		ExceptionCode exceptionCode = new ExceptionCode();
		int result = Constants.ERRONEOUS_OPERATION;
		try {
			String sysDate = commonDao.getSystemDate();
			dsLODWrapperVb.getMainModel().setDateCreation(sysDate);
			dsLODWrapperVb.getMainModel().setDateLastModified(sysDate);
			switch (switchOption) {
			case "CONNECTOR": {
				result = dsConnectorDao.doInsertRecordForDataConnectorLOD(dsLODWrapperVb, isMain);
				break;
			}
			case "M_QUERY": {
				result = dsConnectorDao.doInsertRecordForManualQueries(dsLODWrapperVb, isMain);
				break;
			}
			case "D_QUERY": {
				result = dsConnectorDao.doInsertRecordForDesignQueries(dsLODWrapperVb, isMain);
				break;
			}
			}
			exceptionCode.setErrorCode(result);
			return exceptionCode;
		} catch (RuntimeCustomException rcException) {
			throw rcException;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	public ExceptionCode getLODForDataSource(DsConnectorLODWrapperVb dsLODWrapperVb, String switchOption) throws RuntimeCustomException {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			String sysDate = commonDao.getSystemDate();
			dsLODWrapperVb.getMainModel().setDateCreation(sysDate);
			dsLODWrapperVb.getMainModel().setDateLastModified(sysDate);
			switch (switchOption) {
			case "CONNECTOR": {
				exceptionCode.setResponse(dsConnectorDao.getRecordForDataConnectorLOD(dsLODWrapperVb));
				exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
				break;
			}
			case "M_QUERY": {
				exceptionCode.setResponse(dsConnectorDao.getRecordForManualQueries(dsLODWrapperVb));
				exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
				break;
			}
			case "D_QUERY": {
				exceptionCode.setResponse(dsConnectorDao.getRecordForDesignQueries(dsLODWrapperVb));
				exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
				break;
			}
			
			
			}
			return exceptionCode;
		} catch (RuntimeCustomException rcException) {
			throw rcException;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}

	public List<String> modifyDynamicallyCreatedTables(DSConnectorVb vObject) {
		return dsConnectorDao.modifyDynamicallyCreatedTables(vObject);
	}
	
	public ExceptionCode returnTableDataForConnectorFileMapper (String tableName, Map<String, Object> returnMap){
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode.setResponse(getDsConnectorDao().returnTableDataForConnectorFileMapper(tableName, returnMap));
			if(exceptionCode.getResponse()!=null)
				exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
			else
				return CommonUtils.getResultObject("Dataconnector", Constants.NO_RECORDS_FOUND, "Preview", null);
		} catch(Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorMsg(e.getMessage());
		}
		return exceptionCode;
	}

	public ExceptionCode getColumnNamesForDynamicTableName(DSConnectorVb dsConnectorVb, String dynamicTableName) {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			List<String> columnList = dsConnectorDao.getColumnNamesForDynamicTableName(dynamicTableName);
			ArrayList<VcMainDataSourceMetaDataVb> arrayListTree = new ArrayList<VcMainDataSourceMetaDataVb>();
			for(String columnName : columnList) {
				VcMainDataSourceMetaDataVb vObjColDSMD = new VcMainDataSourceMetaDataVb();
				vObjColDSMD.setTableName(dsConnectorVb.getTableName());
				vObjColDSMD.setColumnName(columnName);
				vObjColDSMD.setColumnType("Y");
				arrayListTree.add(vObjColDSMD);
			}
			exceptionCode.setOtherInfo(arrayListTree);
			exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
			return exceptionCode;
		} catch(Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg(e.getMessage());
			return exceptionCode;
		}
	}
	
	public DSConnectorDao getDsConnectorDao() {
		return dsConnectorDao;
	}

	public void setDsConnectorDao(DSConnectorDao dsConnectorDao) {
		this.dsConnectorDao = dsConnectorDao;
	}
}
