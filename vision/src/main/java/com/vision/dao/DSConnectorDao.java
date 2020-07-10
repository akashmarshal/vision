package com.vision.dao;


import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vision.authentication.SessionContextHolder;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.Paginationhelper;
import com.vision.util.ValidationUtil;
import com.vision.vb.SmartSearchVb;
import com.vision.vb.ConnectorFileUploadMapperVb;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.DsConnectorLODWrapperVb;
import com.vision.vb.LevelOfDisplayUserVb;
import com.vision.vb.LevelOfDisplayVb;
import com.vision.vb.SbiReportPromptsVb;
import com.vision.vb.VcReportGenerationVb;
import com.vision.vb.XmlJsonUploadVb;
import com.vision.wb.VisionUploadWb;

@Component
public class DSConnectorDao extends AbstractDao<DSConnectorVb> {

	@Autowired
	DataSource datasource;
	@Autowired
	VisionUploadWb visionUploadWb;
	@Autowired
	VisionUsersDao visionUsersDao;
	
	@Override
	protected void setServiceDefaults() {
		serviceName = "Datasource Connector";
		serviceDesc = "Datasource Connector";
		tableName = "VISION_DYNAMIC_HASH_VAR";
		childTableName = "VISION_DYNAMIC_HASH_VAR";
		intCurrentUserId = SessionContextHolder.getContext().getVisionId();
		userGroup = SessionContextHolder.getContext().getUserGroup();
		userProfile = SessionContextHolder.getContext().getUserProfile();
	}

	public List<DSConnectorVb> getAllDataConnectorsLists(DSConnectorVb dObj) {
		setServiceDefaults();
		Vector<Object> params = new Vector<Object>();
		StringBuffer strBufApprove = new StringBuffer(
				" SELECT t1.VARIABLE_NAME CONNECTOR_ID, VARIABLE_DESCRIPTION DESCRIPTION, SCRIPT_TYPE TYPE, "+
					       " (SELECT ALPHA_SUBTAB_DESCRIPTION FROM ALPHA_SUB_TAB "+
					       "   WHERE ALPHA_TAB = 1082 AND ALPHA_SUB_TAB = T1.SCRIPT_TYPE) AS TYPE_DESC, "+
					       " VARIABLE_STATUS STATUS, (SELECT NUM_SUBTAB_DESCRIPTION FROM NUM_SUB_TAB "+
					       "   WHERE NUM_TAB = VARIABLE_STATUS_NT AND VARIABLE_STATUS = NUM_SUB_TAB) AS STATUS_DESC, "+
					       " (SELECT USER_NAME FROM VISION_USERS WHERE VISION_ID = t1.MAKER) AS MAKER_NAME, t1.MAKER, "+
					       " t1.VERIFIER, (SELECT USER_NAME FROM VISION_USERS WHERE VISION_ID = t1.VERIFIER) AS VERIFIER_NAME, "+
					       " TO_CHAR (t1.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, "+
					       " TO_CHAR (t1.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, "+
					       " 'TRUE' AS VALIDFLAG FROM VISION_DYNAMIC_HASH_VAR t1 LEFT JOIN DATACONNECTOR_LOD t2 ON (t1.VARIABLE_NAME = T2.VARIABLE_NAME) "+
					 " WHERE VARIABLE_TYPE = 2 AND VARIABLE_SCRIPT NOT LIKE ('{CONNECTIVITY_TYPE:#CONSTANT%') "+
					       " AND  ((SCRIPT_TYPE = 'MACROVAR' AND t1.VARIABLE_NAME = 'DEFAULT_SSBI_DB') OR   (SCRIPT_TYPE = 'FILE' )) "+
					       " AND (T1.MAKER = '"+intCurrentUserId+"' OR (T2.USER_GROUP = '"+userGroup+"' AND USER_PROFILE = '"+userProfile+"')) "+
					       " UNION "+
					" SELECT QUERY_ID CONNECTOR_ID, QUERY_DESCRIPTION DESCRIPTION, 'M_QUERY' TYPE, "+
					       " (SELECT ALPHA_SUBTAB_DESCRIPTION FROM ALPHA_SUB_TAB "+
					       "   WHERE ALPHA_TAB = 1082 AND ALPHA_SUB_TAB = 'M_QUERY') AS TYPE_DESC, "+
					       " VCQ_STATUS STATUS, (SELECT NUM_SUBTAB_DESCRIPTION FROM NUM_SUB_TAB "+
					       "   WHERE NUM_TAB = VCQ_STATUS_NT AND VCQ_STATUS = NUM_SUB_TAB) AS STATUS_DESC, "+
					       " (SELECT USER_NAME FROM VISION_USERS WHERE VISION_ID = t1.MAKER) AS MAKER_NAME, "+
					       " t1.MAKER, t1.VERIFIER, (SELECT USER_NAME FROM VISION_USERS "+
					       "   WHERE VISION_ID = t1.VERIFIER) AS VERIFIER_NAME, "+
					       " TO_CHAR (T1.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, "+
					       " TO_CHAR (T1.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, "+
					       " CASE QUERY_VALID_FLAG WHEN 'S' THEN 'TRUE' ELSE 'FALSE' END VALIDFLAG "+
					  " FROM VC_QUERIES t1 LEFT JOIN VCQD_QUERIES_ACCESS t2 ON (t1.QUERY_ID = t2.VCQD_QUERY_ID) "+
					  " where (T1.MAKER = '"+intCurrentUserId+"' OR (T2.USER_GROUP = '"+userGroup+"' AND USER_PROFILE = '"+userProfile+"')) ");
		StringBuffer strBufPending = null;
		strBufPending = null;
		try {
			StringBuffer strBufOrderBy = new StringBuffer(" Order By ");
			String orderBy = " Order By CONNECTOR_ID ";

			if (dObj.getSortHeaderColumnMap().size() > 0) {
				Map<String, String> columnMaP = new HashMap<String, String>();
				columnMaP = dObj.getSortHeaderColumnMap();

				for (Map.Entry<String, String> entry : columnMaP.entrySet()) {
					String key = entry.getKey();
					switch (key) {
					case "macroVar":
						CommonUtils.addToOrderByQuery("CONNECTOR_ID " + entry.getValue(), strBufOrderBy);
						break;

					case "description":
						CommonUtils.addToOrderByQuery("DESCRIPTION " + entry.getValue(), strBufOrderBy);
						break;

					case "scriptType":
						CommonUtils.addToOrderByQuery("TYPE " + entry.getValue(), strBufOrderBy);
						break;

					case "dateCreation":
						CommonUtils.addToOrderByQuery("DATE_CREATION " + entry.getValue(), strBufOrderBy);
						break;

					case "dateLastModified":
						CommonUtils.addToOrderByQuery("DATE_LAST_MODIFIED " + entry.getValue(), strBufOrderBy);
						break;

					case "makerName":
						CommonUtils.addToOrderByQuery("MAKER_NAME " + entry.getValue(), strBufOrderBy);
						break;

					case "verifierName":
						CommonUtils.addToOrderByQuery("VERIFIER_NAME " + entry.getValue(), strBufOrderBy);
						break;

					case "validFlag":
						CommonUtils.addToOrderByQuery("VALIDFLAG " + entry.getValue(), strBufOrderBy);
						break;

					case "vcrStatus":
						CommonUtils.addToOrderByQuery("STATUS_DESC " + entry.getValue(), strBufOrderBy);
						break;

					default:
					}
				}
				orderBy = String.valueOf(strBufOrderBy);
			}
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					DSConnectorVb dsConnectorVb = new DSConnectorVb();
					dsConnectorVb.setMacroVar(rs.getString("CONNECTOR_ID"));
					dsConnectorVb.setDescription(rs.getString("DESCRIPTION"));
					dsConnectorVb.setScriptType(rs.getString("TYPE"));
					dsConnectorVb.setMacroVarScript(rs.getString("TYPE_DESC"));
					dsConnectorVb.setVcrStatus(rs.getInt("STATUS"));
					dsConnectorVb.setVcrStatusDesc(rs.getString("STATUS_DESC"));
					dsConnectorVb.setMaker(rs.getLong("MAKER"));
					dsConnectorVb.setMakerName(rs.getString("MAKER_NAME"));
					dsConnectorVb.setVerifier(rs.getLong("VERIFIER"));
					dsConnectorVb.setVerifierName(rs.getString("VERIFIER_NAME"));
					dsConnectorVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
					dsConnectorVb.setDateCreation(rs.getString("DATE_CREATION"));
					dsConnectorVb.setValidFlag(rs.getString("VALIDFLAG"));
					fetchMakerVerifierNames(dsConnectorVb);
					return dsConnectorVb;
				}
			};
			return getQueryPopupResultsWithPend(dObj, strBufPending, strBufApprove, "", orderBy, params,mapper);
		} catch (Exception ex) {
			ex.printStackTrace();
			if (params != null)
				for (int i = 0; i < params.size(); i++)
					logger.error("objParams[" + i + "]" + params.get(i).toString());
			return null;
		}
	}
	
	public DSConnectorVb getSpecificConnectorDetails(DSConnectorVb dsConnectorVb) throws DataAccessException {
		String sql = " SELECT VARIABLE_NAME CONNECTOR_ID, VARIABLE_SCRIPT, VARIABLE_DESCRIPTION DESCRIPTION, CASE SCRIPT_TYPE "
				+ " WHEN 'MACROVAR' THEN 'DATABASE' ELSE SCRIPT_TYPE "
				+ " END TYPE, VARIABLE_STATUS STATUS, MAKER, VERIFIER, To_Char(DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, "
				+ " To_Char(DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION FROM VISION_DYNAMIC_HASH_VAR WHERE VARIABLE_TYPE = 2 AND SCRIPT_TYPE IN ('MACROVAR', 'FILE') "
				+ " and VARIABLE_SCRIPT  not like  ('{CONNECTIVITY_TYPE:#CONSTANT%') AND UPPER(VARIABLE_NAME)=UPPER('" + dsConnectorVb.getMacroVar() + "')";

		ResultSetExtractor<DSConnectorVb> rse = new ResultSetExtractor<DSConnectorVb>() {
			@Override
			public DSConnectorVb extractData(ResultSet rs) throws SQLException, DataAccessException {
				DSConnectorVb dsConnectorVb = null;
				if (rs.next()) {
					dsConnectorVb = new DSConnectorVb();
					dsConnectorVb.setMacroVar(rs.getString("CONNECTOR_ID"));
					dsConnectorVb.setMacroVarScript(rs.getString("VARIABLE_SCRIPT"));
					dsConnectorVb.setScriptType(rs.getString("TYPE"));
					dsConnectorVb.setDescription(rs.getString("DESCRIPTION"));
					dsConnectorVb.setInternalStatus(rs.getInt("STATUS"));
					dsConnectorVb.setMaker(rs.getLong("MAKER"));
					dsConnectorVb.setVerifier(rs.getLong("VERIFIER"));
					dsConnectorVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
					dsConnectorVb.setDateCreation(rs.getString("DATE_CREATION"));
					if (ValidationUtil.isValid(rs.getString("VARIABLE_SCRIPT")) && "FILE".equalsIgnoreCase(rs.getString("TYPE"))) {
						String fileName = CommonUtils.getValue(rs.getString("VARIABLE_SCRIPT"), "NAME") + "."
								+ CommonUtils.getValue(rs.getString("VARIABLE_SCRIPT"), "EXTENSION");
						dsConnectorVb.setFileName(fileName);
						dsConnectorVb.setExtension(CommonUtils.getValue(rs.getString("VARIABLE_SCRIPT"), "EXTENSION"));
						String delimiter = CommonUtils.getValue(rs.getString("VARIABLE_SCRIPT"), "DELIMITER");
						dsConnectorVb.setDelimiter(delimiter);
						dsConnectorVb.setDateLastModified(CommonUtils.getValue(rs.getString("VARIABLE_SCRIPT"), "DATE"));
					}
				}
					return dsConnectorVb;
			}
		};
		return getJdbcTemplate().query(sql,  rse);
	}
	
	public SbiReportPromptsVb getVrdPromptDesign(VcReportGenerationVb dObj) throws DataAccessException {
		String sql = "Select REPORT_ID ,  PROMPT_XML_CONTENT , PROMPT_PAGE_SORT, VRD_PROMPT_STATUS_NT, VRD_PROMPT_STATUS , RECORD_INDICATOR_NT , "
				+ "RECORD_INDICATOR , MAKER , VERIFIER , INTERNAL_STATUS , DATE_LAST_MODIFIED , DATE_CREATION  FROM  VRD_PROMPT_DESIGN WHERE REPORT_ID = ? ";
		Object[] lParams = new Object[1];
		lParams[0] = dObj.getReportId();

		ResultSetExtractor<SbiReportPromptsVb> rse = new ResultSetExtractor<SbiReportPromptsVb>() {
			@Override
			public SbiReportPromptsVb extractData(ResultSet rs) throws SQLException, DataAccessException {
				SbiReportPromptsVb promptsVb = null;
				if (rs.next()) {
					promptsVb = new SbiReportPromptsVb();
					promptsVb.setReportId(rs.getString("REPORT_ID"));
					promptsVb.setPromptXmlContent(rs.getString("PROMPT_XML_CONTENT"));
					promptsVb.setVrdPromptStatusNt(rs.getInt("VRD_PROMPT_STATUS_NT"));
					promptsVb.setVrdPromptStatus(rs.getInt("VRD_PROMPT_STATUS"));
					promptsVb.setRecordIndicatorNt(rs.getInt("RECORD_INDICATOR_NT"));
					promptsVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
					promptsVb.setMaker(rs.getInt("MAKER"));
					promptsVb.setVerifier(rs.getLong("VERIFIER"));
					promptsVb.setInternalStatus(rs.getInt("INTERNAL_STATUS"));
					promptsVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
					promptsVb.setDateCreation(rs.getString("DATE_CREATION"));
				}
				return promptsVb;
			}
		};

		return getJdbcTemplate().query(sql, lParams, rse);
	}
	
	public List<DSConnectorVb> getDisplayTagList(String macroVarType) throws DataAccessException {
		String sql = "select TAG_NAME, DISPLAY_NAME , MASKED_FLAG ,ENCRYPTION, MANDATORY_FLAG, TAG_TYPE from MACROVAR_TAGGING where MACROVAR_NAME='DB_DETAILS' AND MACROVAR_TYPE='"
				+ macroVarType + "' ORDER BY TAG_NO";
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				DSConnectorVb dsConnectorVb = new DSConnectorVb();
				dsConnectorVb.setTagName(rs.getString("TAG_NAME"));
				dsConnectorVb.setDisplayName(rs.getString("DISPLAY_NAME"));
				dsConnectorVb.setMaskedFlag(rs.getString("MASKED_FLAG"));
				dsConnectorVb.setEncryption(rs.getString("ENCRYPTION"));
				dsConnectorVb.setMandatoryFlag(rs.getString("MANDATORY_FLAG"));
				dsConnectorVb.setTagType(rs.getInt("TAG_TYPE"));
				return dsConnectorVb;
			}
		};
		return getJdbcTemplate().query(sql, mapper);
	}
	
	public List getAllValidConnectors() {
		String sql = "select * from ( "+
				"select SCRIPT_TYPE \"scriptType\", VARIABLE_NAME \"macroVar\", VARIABLE_DESCRIPTION \"description\" from vision_dynamic_hash_var where VARIABLE_NAME = 'DEFAULT_SSBI_DB' "+
				"UNION "+
				"select SCRIPT_TYPE \"scriptType\", VARIABLE_NAME \"macroVar\", VARIABLE_DESCRIPTION \"description\" from vision_dynamic_hash_var where SCRIPT_TYPE='FILE' "+
				"UNION "+
				"select 'M_QUERY' \"scriptType\", QUERY_ID \"macroVar\", QUERY_DESCRIPTION \"description\" from VC_QUERIES where  QUERY_CATEGORY=1"+
				") order by \"macroVar\" ";
		
		return getJdbcTemplate().queryForList(sql);
	}
	
	public Connection getDbConnection(String jdbcUrl, String username, String password, String type, String version)
			throws ClassNotFoundException, SQLException, Exception {
		Connection connection = null;
		if ("ORACLE".equalsIgnoreCase(type))
			Class.forName("oracle.jdbc.driver.OracleDriver");
		else if ("MSSQL".equalsIgnoreCase(type))
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		else if ("MYSQL".equalsIgnoreCase(type))
			Class.forName("com.mysql.jdbc.Driver");
		else if ("POSTGRESQL".equalsIgnoreCase(type))
			Class.forName("org.postgresql.Driver");
		else if ("SYBASE".equalsIgnoreCase(type))
			Class.forName("com.sybase.jdbc4.jdbc.SybDataSource");
		else if ("INFORMIX".equalsIgnoreCase(type))
			Class.forName("com.informix.jdbc.IfxDriver");

		connection = DriverManager.getConnection(jdbcUrl, username, password);
		return connection;
	}

	@Override
	protected List<DSConnectorVb> selectApprovedRecord(DSConnectorVb vObject) {
		return getQueryResults(vObject, Constants.STATUS_ZERO);
	}

	@Override
	public List<DSConnectorVb> getQueryResults(DSConnectorVb dObj, int intStatus) {
		List<DSConnectorVb> collTemp = null;
		final int intKeyFieldsCount = 1;
		String strQueryAppr = new String( " select VARIABLE_NAME, RECORD_INDICATOR, MAKER, VERIFIER, " +
				" TO_CHAR (DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, TO_CHAR (DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED " +
				" from VISION_DYNAMIC_HASH_VAR WHERE VARIABLE_TYPE=2 AND SCRIPT_TYPE IN ('MACROVAR','FILE') and VARIABLE_SCRIPT not like ('{CONNECTIVITY_TYPE:#CONSTANT%') " +
				" AND UPPER(VARIABLE_NAME) = UPPER(?) ORDER BY VARIABLE_NAME");
		try {
			Object objParams[] = new Object[intKeyFieldsCount];
			objParams[0] = dObj.getMacroVar();
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					DSConnectorVb dsConnectorVb = new DSConnectorVb();
					dsConnectorVb.setMacroVar(rs.getString("VARIABLE_NAME"));
					dsConnectorVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
					dsConnectorVb.setMaker(rs.getInt("MAKER"));
					dsConnectorVb.setVerifier(rs.getInt("VERIFIER"));
					dsConnectorVb.setDateCreation(rs.getString("DATE_CREATION"));
					dsConnectorVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
					return dsConnectorVb;
				}
			};
			collTemp = getJdbcTemplate().query(strQueryAppr.toString(), objParams, mapper);
			return collTemp;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error: getQueryResults Exception :   ");
			logger.error(((strQueryAppr == null) ? "strQueryAppr is Null" : strQueryAppr.toString()));
			return null;
		}
	}

	@Override
	protected int doInsertionAppr(DSConnectorVb vObject) {
		int result = 0;
		String query = "";
		if(!ValidationUtil.isValid(vObject.getScriptType())) {
			vObject.setScriptType("MACROVAR");
		}
		if(!ValidationUtil.isValid(vObject.getDateLastModified())) {
			query = " Insert into VISION_DYNAMIC_HASH_VAR (VARIABLE_NAME, VARIABLE_TYPE, SCRIPT_TYPE, "
					+ " VARIABLE_SCRIPT, SORT_ORDER, VARIABLE_STATUS, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION,VARIABLE_DESCRIPTION) "
					+ " Values (?, 2, ?, ?, 1, 0, 0, ?, ?, 0, SysDate, SysDate,?)";
			Object[] args =  { vObject.getMacroVar().toUpperCase(), vObject.getScriptType(), vObject.getMacroVarScript(), intCurrentUserId, intCurrentUserId,vObject.getDescription() };
			try {
				return getJdbcTemplate().update(query, args);
			} catch (Exception e) {
				e.printStackTrace();
				strErrorDesc = e.getMessage();
				logger.error("Insert Error in VISION_DYNAMIC_HASH_VAR: " + e.getMessage());
			}
		}else {
			query = " Insert into VISION_DYNAMIC_HASH_VAR (VARIABLE_NAME, VARIABLE_TYPE, SCRIPT_TYPE, "
					+ " VARIABLE_SCRIPT, SORT_ORDER, VARIABLE_STATUS, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION,VARIABLE_DESCRIPTION) "
					+ " Values (?, 2, ?, ?, 1, 0, 0, ?, ?, 0, TO_DATE('"+vObject.getDateLastModified()+"', 'DD-MM-YYYY HH24:MI:SS'), SysDate, ?)";
			Object[] args =  { vObject.getMacroVar().toUpperCase(), vObject.getScriptType(), vObject.getMacroVarScript(), intCurrentUserId, intCurrentUserId,vObject.getDescription() };
			try {
				return getJdbcTemplate().update(query, args);
			} catch (Exception e) {
				e.printStackTrace();
				strErrorDesc = e.getMessage();
				logger.error("Insert Error in VISION_DYNAMIC_HASH_VAR: " + e.getMessage());
			}
		}
		return result;
	}
	
	@Override
	public int doUpdateAppr(DSConnectorVb vObject) {
		int result = 0;
		String sql = "Update VISION_DYNAMIC_HASH_VAR Set VARIABLE_SCRIPT = ?, MAKER = ?, VERIFIER = ?,VARIABLE_DESCRIPTION =?, "
					+ "DATE_LAST_MODIFIED = SysDate Where VARIABLE_NAME = ?";
		Object[] params = { vObject.getMacroVarScript(), intCurrentUserId, intCurrentUserId, vObject.getDescription(),
				vObject.getMacroVar().toUpperCase() };
		try {
			return getJdbcTemplate().update(sql, params);
		} catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Update Error in VISION_DYNAMIC_HASH_VAR: " + e.getMessage());
		}
		return result;
	}

	@Override
	public int doDeleteAppr(DSConnectorVb vObject) {
		int result = 0;
		String query = "Delete From VISION_DYNAMIC_HASH_VAR Where VARIABLE_NAME = ?";
		Object[] args = { vObject.getMacroVar().toUpperCase() };
		try {
			return getJdbcTemplate().update(query, args);
		} catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Delete Error in VISION_DYNAMIC_HASH_VAR: " + e.getMessage());
		}
		return result;
	}

	@Transactional(rollbackForClassName = { "com.vision.exception.RuntimeCustomException" })
	public ExceptionCode doInsertApprRecordforDSConnector(DSConnectorVb vObject) throws RuntimeCustomException {
		ExceptionCode exceptionCode = null;
		strApproveOperation = Constants.ADD;
		strErrorDesc = "";
		strCurrentOperation = Constants.ADD;
		setServiceDefaults();
		try {
			return doInsertApprRecordForDSConnectorNonTrans(vObject);
		} catch (RuntimeCustomException rcException) {
			throw rcException;
		} catch (UncategorizedSQLException uSQLEcxception) {
			strErrorDesc = parseErrorMsg(uSQLEcxception);
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		} catch (Exception ex) {
			logger.error("Error in Add.", ex);
			logger.error(((vObject == null) ? "vObject is Null" : vObject.toString()));
			strErrorDesc = ex.getMessage();
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
	}

	protected ExceptionCode doInsertApprRecordForDSConnectorNonTrans(DSConnectorVb vObject)
			throws RuntimeCustomException {
		List<DSConnectorVb> collTemp = null;
		ExceptionCode exceptionCode = null;
		strCurrentOperation = Constants.ADD;
		strApproveOperation = Constants.ADD;
		setServiceDefaults();
		if ("RUNNING".equalsIgnoreCase(getBuildStatus(vObject))) {
			exceptionCode = getResultObject(Constants.BUILD_IS_RUNNING);
			throw buildRuntimeCustomException(exceptionCode);
		}
		vObject.setMaker(getIntCurrentUserId());
		collTemp = selectApprovedRecord(vObject);
		if (collTemp == null) {
			logger.error("Collection is null for Select Approved Record");
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		// If record already exists in the approved table, reject the addition
		if (collTemp.size() > 0) {
			int intStaticDeletionFlag = getStatus(((ArrayList<DSConnectorVb>) collTemp).get(0));
			if (intStaticDeletionFlag == Constants.PASSIVATE) {
				logger.error("Collection size is greater than zero - Duplicate record found, but inactive");
				exceptionCode = getResultObject(Constants.RECORD_ALREADY_PRESENT_BUT_INACTIVE);
				throw buildRuntimeCustomException(exceptionCode);
			} else {
				logger.error("Collection size is greater than zero - Duplicate record found");
				exceptionCode = getResultObject(Constants.DUPLICATE_KEY_INSERTION);
				throw buildRuntimeCustomException(exceptionCode);
			}
		}
		// Try inserting the record
		vObject.setRecordIndicator(Constants.STATUS_ZERO);
		vObject.setVerifier(getIntCurrentUserId());
		retVal = doInsertionAppr(vObject);
		if (retVal != Constants.SUCCESSFUL_OPERATION) {
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		/*// Try inserting the record
		retVal = doInsertionApprLevelOfDisplay(vObject);
		if (retVal != Constants.SUCCESSFUL_OPERATION) {
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}*/

		String systemDate = getSystemDate();
		vObject.setDateLastModified(systemDate);
		vObject.setDateCreation(systemDate);
		exceptionCode = writeAuditLog(vObject, null);
		if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
			exceptionCode = getResultObject(Constants.AUDIT_TRAIL_ERROR);
			throw buildRuntimeCustomException(exceptionCode);
		}
		return exceptionCode;
	}

/*	protected int doInsertionApprLevelOfDisplay(DSConnectorVb vObject) {
		int result = 0;
		String query = " Insert into DATACONNECTOR_LOD (VARIABLE_NAME, DATACONNECTOR_STATUS_NT, DATACONNECTOR_STATUS, "
				+ " MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION,RECORD_INDICATOR_NT, RECORD_INDICATOR,  USER_GROUP_AT, USER_GROUP, USER_PROFILE, USER_PROFILE_AT) "
				+ " Values (?, ?, ?, ?, ?, ?, SysDate, SysDate,?, ?, ?, ?, ?, ?)";
		Object[] args = { vObject.getMacroVar().toUpperCase(), vObject.getDataConnectorStatusNt(), vObject.getDataConnectorStatus(),
				intCurrentUserId, intCurrentUserId, vObject.getInternalStatus(), vObject.getRecordIndicatorNt(),
				vObject.getRecordIndicator(), vObject.getUserGroupAt(), "NA",
				"NA", vObject.getUserProfileAt()};
		try {
			return getJdbcTemplate().update(query, args);
		} catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Insert Error in DATACONNECTOR_LOD: " + e.getMessage());
		}
		return result;
	}*/

	@Transactional(rollbackForClassName = { "com.vision.exception.RuntimeCustomException" })
	public ExceptionCode doUpdateApprRecordforDSConnector(DSConnectorVb vObject) throws RuntimeCustomException {
		ExceptionCode exceptionCode = null;
		strApproveOperation = Constants.MODIFY;
		strErrorDesc = "";
		strCurrentOperation = Constants.MODIFY;
		setServiceDefaults();
		try {
			return doUpdateApprRecordForNonTransforDSConnector(vObject);
		} catch (RuntimeCustomException rcException) {
			throw rcException;
		} catch (UncategorizedSQLException uSQLEcxception) {
			strErrorDesc = parseErrorMsg(uSQLEcxception);
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		} catch (Exception ex) {
			logger.error("Error in Modify.", ex);
			logger.error(((vObject == null) ? "vObject is Null" : vObject.toString()));
			strErrorDesc = ex.getMessage();
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
	}

	protected ExceptionCode doUpdateApprRecordForNonTransforDSConnector(DSConnectorVb vObject)
			throws RuntimeCustomException {
		List<DSConnectorVb> collTemp = null;
		DSConnectorVb vObjectlocal = null;
		ExceptionCode exceptionCode = null;
		strApproveOperation = Constants.MODIFY;
		strErrorDesc = "";
		strCurrentOperation = Constants.MODIFY;
		setServiceDefaults();
		vObject.setMaker(getIntCurrentUserId());
		if ("RUNNING".equalsIgnoreCase(getBuildStatus(vObject))) {
			exceptionCode = getResultObject(Constants.BUILD_IS_RUNNING);
			throw buildRuntimeCustomException(exceptionCode);
		}
		collTemp = selectApprovedRecord(vObject);
		if (collTemp == null) {
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		// Even if record is not there in Appr. table reject the record
		if (collTemp.size() > 0) {
			vObjectlocal = ((ArrayList<DSConnectorVb>) collTemp).get(0);
		}

		if (collTemp.size() == 0) {
			exceptionCode = getResultObject(Constants.ATTEMPT_TO_MODIFY_UNEXISTING_RECORD);
			throw buildRuntimeCustomException(exceptionCode);
		}
		vObject.setRecordIndicator(Constants.STATUS_ZERO);
		vObject.setVerifier(getIntCurrentUserId());
		vObject.setDateCreation(vObjectlocal.getDateCreation());
		retVal = doUpdateAppr(vObject);
		if (retVal != Constants.SUCCESSFUL_OPERATION) {
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		/*retVal = doUpdateApprLevelOfDisplay(vObject);
		if (retVal != Constants.SUCCESSFUL_OPERATION) {
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}*/
		String systemDate = getSystemDate();
		vObject.setDateLastModified(systemDate);
		exceptionCode = writeAuditLog(vObject, vObjectlocal);
		if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
			exceptionCode = getResultObject(Constants.AUDIT_TRAIL_ERROR);
			throw buildRuntimeCustomException(exceptionCode);
		}
		return exceptionCode;
	}

	protected int doUpdateApprLevelOfDisplay(DSConnectorVb vObject) {
		int result = 0;
		String query = "Update DATACONNECTOR_LOD Set USER_GROUP = ?, USER_PROFILE = ?,  "
				+ "DATACONNECTOR_STATUS_NT = ?, DATACONNECTOR_STATUS = ?, RECORD_INDICATOR_NT = ?,"
				+ "RECORD_INDICATOR = ?, MAKER = ?, VERIFIER = ?, DATE_LAST_MODIFIED = SysDate Where VARIABLE_NAME = ?";
		Object[] args = { "NA", "NA", vObject.getDataConnectorStatusNt(), vObject.getDataConnectorStatus(), vObject.getRecordIndicatorNt(),
				vObject.getRecordIndicator(), vObject.getMaker(), vObject.getVerifier(), vObject.getMacroVar().toUpperCase() };
		try {
			return getJdbcTemplate().update(query, args);
		} catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Update Error in DATACONNECTOR_LOD_WIP: " + e.getMessage());
		}
		return result;
	}

	@Transactional(rollbackForClassName = { "com.vision.exception.RuntimeCustomException" })
	public ExceptionCode doDeleteApprRecordForDSConnector(DSConnectorVb vObject) throws RuntimeCustomException {
		ExceptionCode exceptionCode = null;
		strApproveOperation = Constants.DELETE;
		strErrorDesc = "";
		strCurrentOperation = Constants.DELETE;
		setServiceDefaults();
		try {
			return doDeleteApprRecordForNonTransForDSConnector(vObject);
		} catch (RuntimeCustomException rcException) {
			throw rcException;
		} catch (UncategorizedSQLException uSQLEcxception) {
			strErrorDesc = parseErrorMsg(uSQLEcxception);
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		} catch (Exception ex) {
			logger.error("Error in Delete.", ex);
			logger.error(((vObject == null) ? "vObject is Null" : vObject.toString()));
			strErrorDesc = ex.getMessage();
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
	}

	protected ExceptionCode doDeleteApprRecordForNonTransForDSConnector(DSConnectorVb vObject)
			throws RuntimeCustomException {
		List<DSConnectorVb> collTemp = null;
		ExceptionCode exceptionCode = null;
		strApproveOperation = Constants.DELETE;
		strErrorDesc = "";
		strCurrentOperation = Constants.DELETE;
		setServiceDefaults();
		DSConnectorVb vObjectlocal = null;
		vObject.setMaker(getIntCurrentUserId());
		if ("RUNNING".equalsIgnoreCase(getBuildStatus(vObject))) {
			exceptionCode = getResultObject(Constants.BUILD_IS_RUNNING);
			throw buildRuntimeCustomException(exceptionCode);
		}
		collTemp = selectApprovedRecord(vObject);
		if (collTemp == null) {
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		// If record already exists in the approved table, reject the addition
		if (collTemp.size() > 0) {
			int intStaticDeletionFlag = getStatus(((ArrayList<DSConnectorVb>) collTemp).get(0));
			if (intStaticDeletionFlag == Constants.PASSIVATE) {
				exceptionCode = getResultObject(Constants.CANNOT_DELETE_AN_INACTIVE_RECORD);
				throw buildRuntimeCustomException(exceptionCode);
			}
		} else {
			exceptionCode = getResultObject(Constants.ATTEMPT_TO_DELETE_UNEXISTING_RECORD);
			throw buildRuntimeCustomException(exceptionCode);
		}
		vObjectlocal = ((ArrayList<DSConnectorVb>) collTemp).get(0);
		vObject.setDateCreation(vObjectlocal.getDateCreation());
		if (vObject.isStaticDelete()) {
			vObjectlocal.setMaker(getIntCurrentUserId());
			vObject.setVerifier(getIntCurrentUserId());
			vObject.setRecordIndicator(Constants.STATUS_ZERO);
			setStatus(vObjectlocal, Constants.PASSIVATE);
			vObjectlocal.setVerifier(getIntCurrentUserId());
			vObjectlocal.setRecordIndicator(Constants.STATUS_ZERO);
			retVal = doUpdateAppr(vObjectlocal);

			if (retVal != Constants.SUCCESSFUL_OPERATION) {
				exceptionCode = getResultObject(retVal);
				throw buildRuntimeCustomException(exceptionCode);
			}
			doUpdateApprLevelOfDisplay(vObjectlocal);
			String systemDate = getSystemDate();
			vObject.setDateLastModified(systemDate);
		} else {
			
			DSConnectorVb lsData = getSpecificConnectorDetails(vObject);
			// delete the record from the Approve Table
			retVal = doDeleteAppr(vObject);
			if (retVal != Constants.SUCCESSFUL_OPERATION) {
				exceptionCode = getResultObject(retVal);
				throw buildRuntimeCustomException(exceptionCode);
			}
			doDeleteApprLevelOfDisplay(vObjectlocal);
			String systemDate = getSystemDate();
			vObject.setDateLastModified(systemDate);

			if (lsData != null && ValidationUtil.isValid(lsData)
					&& ValidationUtil.isValid(lsData.getMacroVarScript())) {
				if ("FILE".equalsIgnoreCase(vObject.getScriptType())) {
					visionUploadWb.setUploadDownloadDirFromDB();
					String upfileName = CommonUtils.getValue(lsData.getMacroVarScript(), "NAME");
					String extension = CommonUtils.getValue(lsData.getMacroVarScript(), "EXTENSION");
					System.out.println("EXISTS DATE:" + lsData.getDateLastModified());
					long lCurrentDate = visionUploadWb.getDateTimeInMS(lsData.getDateLastModified(),
							"dd-M-yyyy hh:mm:ss");
					System.out.println("lCurrentDate EXISTS DATE:" + lCurrentDate);
					String fileName = upfileName + "_" + lsData.getMaker() + "_" + lsData.getMacroVar() + "_"
							+ lCurrentDate + "." + extension;
					System.out.println("fileName EXISTS DATE:" + fileName);
					File lFile = new File(visionUploadWb.getConnectorUploadDir() + fileName);
					if (lFile.exists()) {
						if (lFile.renameTo(new File(visionUploadWb.getConnectorADUploadDir() + fileName))) {
							lFile.delete();
						} else {
							System.out.println("Problem during backup process");
						}
					}
				}
			}
		}
		if (retVal != Constants.SUCCESSFUL_OPERATION) {
			exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
		if (vObject.isStaticDelete()) {
			setStatus(vObjectlocal, Constants.STATUS_ZERO);
			setStatus(vObject, Constants.PASSIVATE);
			exceptionCode = writeAuditLog(vObject, vObjectlocal);
		} else {
			exceptionCode = writeAuditLog(null, vObject);
			vObject.setRecordIndicator(-1);
		}
		if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION) {
			exceptionCode = getResultObject(Constants.AUDIT_TRAIL_ERROR);
			throw buildRuntimeCustomException(exceptionCode);
		}
		return exceptionCode;
	}

	public int doDeleteApprLevelOfDisplay(DSConnectorVb vObject) {
		int result = 0;
		String query = "Delete From DATACONNECTOR_LOD Where VARIABLE_NAME = ?";
		Object[] args = { vObject.getMacroVar().toUpperCase() };
		try {
			return getJdbcTemplate().update(query, args);
		} catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Delete Error in DATACONNECTOR_LOD: " + e.getMessage());
		}
		return result;
	}

	@Override
	protected String getAuditString(DSConnectorVb vObject) {
		final String auditDelimiter = vObject.getAuditDelimiter();
		final String auditDelimiterColVal = vObject.getAuditDelimiterColVal();
		StringBuffer strAudit = new StringBuffer("");
		if (ValidationUtil.isValid(vObject.getMacroVar()))
			strAudit.append("VARIABLE_NAME" + auditDelimiterColVal + vObject.getMacroVar().trim().toUpperCase());
		else
			strAudit.append("VARIABLE_NAME" + auditDelimiterColVal + "NULL");
		strAudit.append(auditDelimiter);

		strAudit.append("VARIABLE_TYPE_NT" + auditDelimiterColVal + vObject.getVcrTypeNt());
		strAudit.append(auditDelimiter);
		strAudit.append("VARIABLE_TYPE" + auditDelimiterColVal + vObject.getVcrType());
		strAudit.append(auditDelimiter);
		strAudit.append("SCRIPT_TYPE_AT" + auditDelimiterColVal + vObject.getScriptTypeAt());
		strAudit.append(auditDelimiter);
		strAudit.append("SCRIPT_TYPE" + auditDelimiterColVal + vObject.getScriptType());
		strAudit.append(auditDelimiter);
		if (ValidationUtil.isValid(vObject.getMacroVarScript()))
			strAudit.append("VARIABLE_SCRIPT" + auditDelimiterColVal + vObject.getMacroVarScript());
		else
			strAudit.append("VARIABLE_SCRIPT" + auditDelimiterColVal + "NULL");
		strAudit.append(auditDelimiter);
		strAudit.append("SORT_ORDER" + auditDelimiterColVal + vObject.getSortOrder());
		strAudit.append(auditDelimiter);

		strAudit.append("VARIABLE_STATUS_NT" + auditDelimiterColVal + vObject.getVcrStatusNt());
		strAudit.append(auditDelimiter);
		if (vObject.getVcrStatus() == -1)
			vObject.setVcrStatus(0);
		strAudit.append("VARIABLE_STATUS" + auditDelimiterColVal + vObject.getVcrStatus());
		strAudit.append(auditDelimiter);
		strAudit.append("RECORD_INDICATOR_NT" + auditDelimiterColVal + vObject.getRecordIndicatorNt());
		strAudit.append(auditDelimiter);
		if (vObject.getRecordIndicator() == -1)
			vObject.setRecordIndicator(0);
		strAudit.append("RECORD_INDICATOR" + auditDelimiterColVal + vObject.getRecordIndicator());
		strAudit.append(auditDelimiter);
		strAudit.append("MAKER" + auditDelimiterColVal + vObject.getMaker());
		strAudit.append(auditDelimiter);
		strAudit.append("VERIFIER" + auditDelimiterColVal + vObject.getVerifier());
		strAudit.append(auditDelimiter);
		strAudit.append("INTERNAL_STATUS" + auditDelimiterColVal + vObject.getInternalStatus());
		strAudit.append(auditDelimiter);
		if (ValidationUtil.isValid(vObject.getDateLastModified()))
			strAudit.append("DATE_LAST_MODIFIED" + auditDelimiterColVal + vObject.getDateLastModified().trim());
		else
			strAudit.append("DATE_LAST_MODIFIED" + auditDelimiterColVal + "NULL");
		strAudit.append(auditDelimiter);

		if (ValidationUtil.isValid(vObject.getDateCreation()))
			strAudit.append("DATE_CREATION" + auditDelimiterColVal + vObject.getDateCreation().trim());
		else
			strAudit.append("DATE_CREATION" + auditDelimiterColVal + "NULL");
		strAudit.append(auditDelimiter);
		return strAudit.toString();
	}
	
	
	public List<DSConnectorVb> getQuerySmartSearchFilter(DSConnectorVb dObj) {
		setServiceDefaults();
		Vector<Object> params = new Vector<Object>();
		StringBuffer strApprove = new StringBuffer(
				" SELECT t1.VARIABLE_NAME CONNECTOR_ID, VARIABLE_DESCRIPTION DESCRIPTION, CASE SCRIPT_TYPE " +
						 " WHEN 'MACROVAR' THEN 'DATABASE' ELSE SCRIPT_TYPE END TYPE, "+
			       " (SELECT ALPHA_SUBTAB_DESCRIPTION FROM ALPHA_SUB_TAB "+
			       "   WHERE ALPHA_TAB = 1082 AND ALPHA_SUB_TAB = T1.SCRIPT_TYPE) AS TYPE_DESC, "+
			       " VARIABLE_STATUS STATUS, (SELECT NUM_SUBTAB_DESCRIPTION FROM NUM_SUB_TAB "+
			       "   WHERE NUM_TAB = VARIABLE_STATUS_NT AND VARIABLE_STATUS = NUM_SUB_TAB) AS STATUS_DESC, "+
			       " (SELECT USER_NAME FROM VISION_USERS WHERE VISION_ID = t1.MAKER) AS MAKER_NAME, t1.MAKER, "+
			       " t1.VERIFIER, (SELECT USER_NAME FROM VISION_USERS WHERE VISION_ID = t1.VERIFIER) AS VERIFIER_NAME, "+
			       " TO_CHAR (t1.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, "+
			       " TO_CHAR (t1.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, "+
			       " 'TRUE' AS VALIDFLAG FROM VISION_DYNAMIC_HASH_VAR t1 LEFT JOIN DATACONNECTOR_LOD t2 ON (t1.VARIABLE_NAME = T2.VARIABLE_NAME) "+
			 " WHERE VARIABLE_TYPE = 2 AND VARIABLE_SCRIPT NOT LIKE ('{CONNECTIVITY_TYPE:#CONSTANT%') "+
			       " AND  ((SCRIPT_TYPE = 'MACROVAR' AND t1.VARIABLE_NAME = 'DEFAULT_SSBI_DB') OR   (SCRIPT_TYPE = 'FILE' ))"+
			       " AND (T1.MAKER = '"+intCurrentUserId+"' OR (T2.USER_GROUP = '"+userGroup+"' AND USER_PROFILE = '"+userProfile+"')) "+
			       " UNION "+
			" SELECT QUERY_ID CONNECTOR_ID, QUERY_DESCRIPTION DESCRIPTION, 'M_QUERY' TYPE, "+
			       " (SELECT ALPHA_SUBTAB_DESCRIPTION FROM ALPHA_SUB_TAB "+
			       "   WHERE ALPHA_TAB = 1082 AND ALPHA_SUB_TAB = 'M_QUERY') AS TYPE_DESC, "+
			       " VCQ_STATUS STATUS, (SELECT NUM_SUBTAB_DESCRIPTION FROM NUM_SUB_TAB "+
			       "   WHERE NUM_TAB = VCQ_STATUS_NT AND VCQ_STATUS = NUM_SUB_TAB) AS STATUS_DESC, "+
			       " (SELECT USER_NAME FROM VISION_USERS WHERE VISION_ID = t1.MAKER) AS MAKER_NAME, "+
			       " t1.MAKER, t1.VERIFIER, (SELECT USER_NAME FROM VISION_USERS "+
			       "   WHERE VISION_ID = t1.VERIFIER) AS VERIFIER_NAME, "+
			       " TO_CHAR (T1.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, "+
			       " TO_CHAR (T1.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, "+
			       " CASE QUERY_VALID_FLAG WHEN 'S' THEN 'TRUE' ELSE 'FALSE' END VALIDFLAG "+
			  " FROM VC_QUERIES t1 LEFT JOIN VCQD_QUERIES_ACCESS t2 ON (t1.QUERY_ID = t2.VCQD_QUERY_ID) "+
			  " where (T1.MAKER = '"+intCurrentUserId+"'  OR (T2.USER_GROUP = '"+userGroup+"' AND USER_PROFILE = '"+userProfile+"')) ");
		
		StringBuffer strBufPending = null;
		
		strApprove = new StringBuffer("select * from ("+strApprove+")");
		
		try {
			String orderBy = " Order By CONNECTOR_ID ";

			if (dObj.getSmartSearchVb().size() > 0) {
				int count = 1;
				for (SmartSearchVb data: dObj.getSmartSearchVb()){
					if(count == dObj.getSmartSearchVb().size()) {
						data.setJoinType("");
					}
					String val = CommonUtils.criteriaBasedVal(data.getCriteria(), data.getValue());
					switch (data.getObject()) {
					case "macroVar":
						CommonUtils.addToQuerySearch(" upper(CONNECTOR_ID) "+ val, strApprove, data.getJoinType());
						break;
	
					case "description":
						CommonUtils.addToQuerySearch(" upper(DESCRIPTION) "+ val, strApprove, data.getJoinType());
						break;

					case "dateCreation":
						CommonUtils.addToQuerySearch(" upper(DATE_CREATION) "+ val, strApprove, data.getJoinType());
						break;

					case "dateLastModified":
						CommonUtils.addToQuerySearch(" upper(DATE_LAST_MODIFIED) "+ val, strApprove, data.getJoinType());
						break;

					case "makerName":
						CommonUtils.addToQuerySearch(" upper(MAKER_NAME)"+ val, strApprove, data.getJoinType());
						break;

					case "verifierName":
						CommonUtils.addToQuerySearch(" upper(VERIFIER_NAME)"+ val, strApprove, data.getJoinType());
						break;
						
					case "vcrStatusDesc":
						CommonUtils.addToQuerySearch(" upper(STATUS_DESC)"+ val, strApprove, data.getJoinType());
						break;
						
					case "macroVarScript":
						CommonUtils.addToQuerySearch(" upper(TYPE_DESC) "+ val, strApprove, data.getJoinType());
						break;
						
					default:
					}
					count++;
				}
			}
			//orderBy = String.valueOf(strBufOrderBy);
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					DSConnectorVb dsConnectorVb = new DSConnectorVb();
					dsConnectorVb.setMacroVar(rs.getString("CONNECTOR_ID"));
					dsConnectorVb.setDescription(rs.getString("DESCRIPTION"));
					dsConnectorVb.setScriptType(rs.getString("TYPE"));
					dsConnectorVb.setMacroVarScript(rs.getString("TYPE_DESC"));
					dsConnectorVb.setVcrStatus(rs.getInt("STATUS"));
					dsConnectorVb.setVcrStatusDesc(rs.getString("STATUS_DESC"));
					dsConnectorVb.setMaker(rs.getLong("MAKER"));
					dsConnectorVb.setMakerName(rs.getString("MAKER_NAME"));
					dsConnectorVb.setVerifier(rs.getLong("VERIFIER"));
					dsConnectorVb.setVerifierName(rs.getString("VERIFIER_NAME"));
					dsConnectorVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
					dsConnectorVb.setDateCreation(rs.getString("DATE_CREATION"));
					dsConnectorVb.setValidFlag(rs.getString("VALIDFLAG"));
					fetchMakerVerifierNames(dsConnectorVb);
					return dsConnectorVb;
				}
			};
			return getQueryPopupResultsWithPend(dObj, strBufPending, strApprove, "", orderBy, params,mapper);
		} catch (Exception ex) {
			ex.printStackTrace();
			if (params != null)
				for (int i = 0; i < params.size(); i++)
					logger.error("objParams[" + i + "]" + params.get(i).toString());
			return null;
		}
	}
	
	public int doInsertRecordForDataConnectorLOD(DsConnectorLODWrapperVb wrapperVb, boolean isMain) {
		DSConnectorVb mainObject = wrapperVb.getMainModel();
		String tableName = "DATACONNECTOR_LOD_WIP";
		if (isMain)
			tableName = "DATACONNECTOR_LOD";

		String sql = "DELETE FROM " + tableName + " WHERE VARIABLE_NAME = '" + mainObject.getMacroVar() + "'";

		try {
			getJdbcTemplate().update(sql);
		} catch (Exception e) {
		}

		try {
			if(ValidationUtil.isValidList(wrapperVb.getLodProfileList())) {
				sql = "INSERT INTO " + tableName
						+ " (VARIABLE_NAME,  USER_GROUP_AT, USER_GROUP, USER_PROFILE_AT, USER_PROFILE, DATACONNECTOR_STATUS, "
						+ "RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION) "
						+ "VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, To_Date(?, 'DD-MM-YYYY HH24:MI:SS'), To_Date(?, 'DD-MM-YYYY HH24:MI:SS'))";
				for (LevelOfDisplayVb vObject : wrapperVb.getLodProfileList()) {
					Object[] args = { mainObject.getMacroVar(),  vObject.getUserGroupAt(), vObject.getUserGroup(),
							vObject.getUserProfileAt(), vObject.getUserProfile(), "0", mainObject.getRecordIndicatorNt(),
							0, mainObject.getMaker(), mainObject.getVerifier(),
							0, mainObject.getDateLastModified(),
							mainObject.getDateCreation()};
					getJdbcTemplate().update(sql, args);
				}
			}
		
			return Constants.SUCCESSFUL_OPERATION;
		} catch (Exception e) {
			throw e;
		}
	}

	public int doInsertRecordForDesignQueries(DsConnectorLODWrapperVb wrapperVb, boolean isMain) {
		DSConnectorVb mainObject = wrapperVb.getMainModel();
		String tableName = "VCQD_QUERIES_ACCESS_WIP";
		if (isMain)
			tableName = "VCQD_QUERIES_ACCESS";

		String sql = "DELETE FROM " + tableName + " WHERE VCQD_QUERY_ID = '" + mainObject.getMacroVar() + "'";

		try {
			getJdbcTemplate().update(sql);
		} catch (Exception e) {
		}
		try {
			if(ValidationUtil.isValidList(wrapperVb.getLodProfileList())) {
				sql = "INSERT INTO " + tableName
						+ " (VCQD_QUERY_ID,  USER_GROUP_AT, USER_GROUP, USER_PROFILE_AT, USER_PROFILE, VCQDA_STATUS, QUERY_TYPE,   "
						+ "RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, To_Date(?, 'DD-MM-YYYY HH24:MI:SS'), To_Date(?, 'DD-MM-YYYY HH24:MI:SS'))";
				for (LevelOfDisplayVb vObject : wrapperVb.getLodProfileList()) {
					Object[] args = { mainObject.getMacroVar(),vObject.getUserGroupAt(), vObject.getUserGroup(),
							vObject.getUserProfileAt(), vObject.getUserProfile(), "0",1,
							mainObject.getRecordIndicatorNt(), 0,
							mainObject.getMaker(), mainObject.getVerifier(), 0,
							mainObject.getDateLastModified(), mainObject.getDateCreation() };
					getJdbcTemplate().update(sql, args);
				}
			}
			
		
			return Constants.SUCCESSFUL_OPERATION;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public int doInsertRecordForManualQueries(DsConnectorLODWrapperVb wrapperVb, boolean isMain) {
		DSConnectorVb mainObject = wrapperVb.getMainModel();
		String tableName = "VCQD_QUERIES_ACCESS_WIP";
		if (isMain)
			tableName = "VCQD_QUERIES_ACCESS";

		String sql = "DELETE FROM " + tableName + " WHERE VCQD_QUERY_ID = '" + mainObject.getMacroVar() + "'";

		try {
			getJdbcTemplate().update(sql);
		} catch (Exception e) {
		}
		try {
			if(ValidationUtil.isValidList(wrapperVb.getLodProfileList())) {
				sql = "INSERT INTO " + tableName
						+ " (VCQD_QUERY_ID, USER_GROUP_AT, USER_GROUP, USER_PROFILE_AT, USER_PROFILE, VCQDA_STATUS, QUERY_TYPE,   "
						+ "RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, To_Date(?, 'DD-MM-YYYY HH24:MI:SS'), To_Date(?, 'DD-MM-YYYY HH24:MI:SS'))";
				for (LevelOfDisplayVb vObject : wrapperVb.getLodProfileList()) {
					Object[] args = { mainObject.getMacroVar(), vObject.getUserGroupAt(), vObject.getUserGroup(),
							vObject.getUserProfileAt(), vObject.getUserProfile(), "0", 0,
							mainObject.getRecordIndicatorNt(), 0,
							mainObject.getMaker(), mainObject.getVerifier(), 0,
							mainObject.getDateLastModified(), mainObject.getDateCreation() };
					getJdbcTemplate().update(sql, args);
				}
			}
	
			return Constants.SUCCESSFUL_OPERATION;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public DsConnectorLODWrapperVb getRecordForDataConnectorLOD(DsConnectorLODWrapperVb vObjMain) {
		
		try {
			String sql = "SELECT USER_GROUP, USER_PROFILE FROM DATACONNECTOR_LOD " +
					" WHERE UPPER(VARIABLE_NAME) = UPPER('"+vObjMain.getMainModel().getMacroVar()+"') " +
					" ORDER BY USER_GROUP, USER_PROFILE";
			List<LevelOfDisplayVb> profileList = getJdbcTemplate().query(sql, new RowMapper<LevelOfDisplayVb>() {
				@Override
				public LevelOfDisplayVb mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new LevelOfDisplayVb(rs.getString("USER_GROUP"), rs.getString("USER_PROFILE"), null);
				}
			});
		
			vObjMain.setLodProfileList(ValidationUtil.isValidList(profileList)?profileList:null);
			return vObjMain;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	public DsConnectorLODWrapperVb getRecordForDesignQueries(DsConnectorLODWrapperVb vObjMain) {
		
		try {
			String sql = "SELECT USER_GROUP, USER_PROFILE FROM VCQD_QUERIES_ACCESS " +
					" WHERE UPPER(VCQD_QUERY_ID) = UPPER('"+vObjMain.getMainModel().getMacroVar()+"') " +
					" AND QUERY_TYPE=1 "+
					" ORDER BY USER_GROUP, USER_PROFILE";
			List<LevelOfDisplayVb> profileList = getJdbcTemplate().query(sql, new RowMapper<LevelOfDisplayVb>() {
				@Override
				public LevelOfDisplayVb mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new LevelOfDisplayVb(rs.getString("USER_GROUP"), rs.getString("USER_PROFILE"), null);
				}
			});
			
		
			
			vObjMain.setLodProfileList(ValidationUtil.isValidList(profileList)?profileList:null);
			return vObjMain;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public DsConnectorLODWrapperVb getRecordForManualQueries(DsConnectorLODWrapperVb vObjMain) {
		
		try {
			String sql = "SELECT USER_GROUP, USER_PROFILE FROM VCQD_QUERIES_ACCESS " +
					" WHERE UPPER(VCQD_QUERY_ID) = UPPER('"+vObjMain.getMainModel().getMacroVar()+"') " +
					" AND QUERY_TYPE=0 "+
					" ORDER BY USER_GROUP, USER_PROFILE";
			List<LevelOfDisplayVb> profileList = getJdbcTemplate().query(sql, new RowMapper<LevelOfDisplayVb>() {
				@Override
				public LevelOfDisplayVb mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new LevelOfDisplayVb(rs.getString("USER_GROUP"), rs.getString("USER_PROFILE"), null);
				}
			});
			
		
			vObjMain.setLodProfileList(ValidationUtil.isValidList(profileList)?profileList:null);
			return vObjMain;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void dynamicTableNameCreator(StringBuffer dynamicBufferString, long numberIncr)  {

		 //calculating the zeros to be generated
        int zeroCounts =  Constants.LONG_MAX_LENGTH- String.valueOf(numberIncr).length();
        //generating zeros
	          for(int i=0; i<zeroCounts; i++)
	          {
	        	  dynamicBufferString.append("0");
	          }
	          //9223372036854775807 is the max length in long
	      if(numberIncr < Long.MAX_VALUE)
          {
          	   numberIncr++;
          }
	          //finnaly appending zeros and charecters to the incremented value
	      dynamicBufferString.append(numberIncr);
	}

	public long generateTableSequence() throws DataAccessException {
			/*
			 * -- sequence creator for upload table mapping 
			--  max value will be as per the java long datatype prefrence 9223372036854775807
			  CREATE SEQUENCE UPLOAD_TABLE_SEQ_GENERATOR
			  MINVALUE 1
			  MAXVALUE 9223372036854775807
			  START WITH 1
			  INCREMENT BY 1
			  CACHE 20;
					*/
		long tableSequence = getJdbcTemplate().queryForObject("select UPLOAD_TABLE_SEQ_GENERATOR.nextval from dual", Long.class);
		return tableSequence;
	}

	public void doInsertOperationForUploadMappingTable(DSConnectorVb dsConnectorVb, StringBuffer dynamicTableName) throws DataAccessException {
		String query = "INSERT INTO CONNECTOR_FILE_UPLOAD_MAPPER( CONNECTOR_ID, FILE_TABLE_NAME, SELF_BI_MAPPING_TABLE_NAME) VALUES( ? , ? , ? )";
		Object[] args =  { dsConnectorVb.getMacroVar().toUpperCase(), dsConnectorVb.getTableName(),dynamicTableName.toString()};
		getJdbcTemplate().update(query, args);
	}
	

	public void createDynamicTableandMappingfields(String string) {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement();) {
			stmt.execute(string);
		} catch (Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
	}

	public List<String> modifyDynamicallyCreatedTables(DSConnectorVb vObject) {
		String sql = "SELECT SELF_BI_MAPPING_TABLE_NAME FROM CONNECTOR_FILE_UPLOAD_MAPPER WHERE UPPER(CONNECTOR_ID) = UPPER(?)";
		Object[] args = {vObject.getMacroVar()};
		
		List<String> tableNamesList = getJdbcTemplate().queryForList(sql, args, String.class);

		sql = "DELETE FROM CONNECTOR_FILE_UPLOAD_MAPPER WHERE UPPER(CONNECTOR_ID) = UPPER(?)";
		getJdbcTemplate().update(sql, args);
		
		return tableNamesList;
	}

	public void dropTablesForUpload(String tableName) {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement();) {
			String delQuery = "Drop table " + tableName + " purge";
			stmt.execute(delQuery);
		} catch (Exception e) {
		}
	}
	public List<ConnectorFileUploadMapperVb> getConnectorFileUploadMapperDetails(DSConnectorVb vObject) {
		String sql = "SELECT CONNECTOR_ID, FILE_TABLE_NAME, SELF_BI_MAPPING_TABLE_NAME FROM CONNECTOR_FILE_UPLOAD_MAPPER WHERE UPPER(CONNECTOR_ID) = UPPER(?)"
				+ "ORDER BY SELF_BI_MAPPING_TABLE_NAME";
		Object[] args = {vObject.getMacroVar()};
		
		return getJdbcTemplate().query(sql, args, new RowMapper<ConnectorFileUploadMapperVb>() {

			@Override
			public ConnectorFileUploadMapperVb mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConnectorFileUploadMapperVb connectorFileUploadMapperVb = new ConnectorFileUploadMapperVb();
				connectorFileUploadMapperVb.setConnectorId(rs.getString("CONNECTOR_ID"));
				connectorFileUploadMapperVb.setFileTableName(rs.getString("FILE_TABLE_NAME"));
				connectorFileUploadMapperVb.setSelfBiMappingTableName(rs.getString("SELF_BI_MAPPING_TABLE_NAME"));
				return connectorFileUploadMapperVb;
			}
		});
	}
	
	public Map<String, Object> returnTableDataForConnectorFileMapper (String tableName, Map<String, Object> returnMap){
		String sql = "SELECT * FROM " +tableName+ " where ROWNUM<101";
		return getJdbcTemplate().query(sql, new ResultSetExtractor<Map<String, Object>>() {
			@Override
			public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				List<XmlJsonUploadVb> colList = new ArrayList<XmlJsonUploadVb>();
				for(int index = 1 ; index <= columnCount ; index++) {
					colList.add(new XmlJsonUploadVb(rsmd.getColumnName(index)));
				}
				returnMap.put("COLUMNS", colList);
				LinkedHashMap<String, List> rowMap = new LinkedHashMap<String, List>();
				int rowIndex = 1;
				while(rs.next()) {
					List<XmlJsonUploadVb> rowDataList = new ArrayList<XmlJsonUploadVb>();
					for(XmlJsonUploadVb colVb : colList) {
						rowDataList.add(new XmlJsonUploadVb(ValidationUtil.isValid(rs.getString(colVb.getData()))?rs.getString(colVb.getData()):Constants.EMPTY));
					}
					rowMap.put("ROW"+rowIndex, rowDataList);
					rowIndex++;
				}
				returnMap.put("ROWS", rowMap);
				return returnMap;
			}
		});
	}
	
	public List<String> getColumnNamesForDynamicTableName(String tableName) {
		String sql = "SELECT COLUMN_NAME FROM USER_TAB_COLUMNS WHERE UPPER(TABLE_NAME) = UPPER(?) " +
				" ORDER BY COLUMN_ID ";
		try {
			Object[] args = {tableName};
			return getJdbcTemplate().queryForList(sql, args, String.class);
		} catch(Exception e) {
			throw e;
		}
	}
	
	public Connection returnConnection() {
		try {return getConnection();}catch(Exception e) {return null;}
	}

	public ExceptionCode validateConnectorData(DSConnectorVb vObject) {
		ExceptionCode exceptionCode = new ExceptionCode();
		try{
			List<String> MainRecord = getJdbcTemplate().queryForList("select CATALOG_ID from VC_TREE_SELFBI WHERE DATABASE_CONNECTIVITY_DETAILS ='"
							+ vObject.getMacroVar() + "' ", String.class);
			List<String> wipRecord = getJdbcTemplate().queryForList("select CATALOG_ID from VC_TREE_WIP WHERE  DATABASE_CONNECTIVITY_DETAILS ='"
							+ vObject.getMacroVar() + "' ", String.class);
			MainRecord.addAll(wipRecord);
			if (MainRecord.size() > 0) {
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setResponse(MainRecord);
			} else {
				exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
			}
		}catch(Exception e){
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
		}
		return exceptionCode;
	}
	public synchronized int getMaxVersionNumber(String macroVar) {
		String sql = "SELECT CASE WHEN MAX(VERSION_NO) IS NULL THEN 0 ELSE MAX(VERSION_NO) END VERSION_NO FROM VISION_DYNAMIC_HASH_VAR_AD WHERE VARIABLE_NAME = ? ";
		Object args[] = {macroVar};
		return getJdbcTemplate().queryForObject(sql, args, Integer.class);
	}
	public void moveMainDataToAD(DSConnectorVb vObject) {
		Integer versionNo =  getMaxVersionNumber(vObject.getMacroVar())+1;  
		getJdbcTemplate().update(" INSERT INTO VISION_DYNAMIC_HASH_VAR_AD(VARIABLE_NAME,VARIABLE_TYPE_NT,VARIABLE_TYPE,SCRIPT_TYPE_AT,SCRIPT_TYPE,VARIABLE_SCRIPT, " + 
				"  SORT_ORDER,VARIABLE_STATUS_NT,VARIABLE_STATUS,RECORD_INDICATOR_NT,RECORD_INDICATOR,MAKER,VERIFIER,INTERNAL_STATUS,DATE_LAST_MODIFIED, " + 
				"  DATE_CREATION,VARIABLE_DESCRIPTION,VERSION_NO)select VARIABLE_NAME,VARIABLE_TYPE_NT,VARIABLE_TYPE,SCRIPT_TYPE_AT,SCRIPT_TYPE, " + 
				"  VARIABLE_SCRIPT,SORT_ORDER,VARIABLE_STATUS_NT,VARIABLE_STATUS,RECORD_INDICATOR_NT,RECORD_INDICATOR,MAKER,VERIFIER,INTERNAL_STATUS, " + 
				"  DATE_LAST_MODIFIED,DATE_CREATION,VARIABLE_DESCRIPTION,"+versionNo+" from VISION_DYNAMIC_HASH_VAR WHERE VARIABLE_NAME = '"+vObject.getMacroVar()+"' " );
	}

	public void doDeleteMappingTable(DSConnectorVb vObject) {
	List<String> tableName = 	getJdbcTemplate().queryForList("select SELF_BI_MAPPING_TABLE_NAME from CONNECTOR_FILE_UPLOAD_MAPPER where UPPER(CONNECTOR_ID) = UPPER('"+vObject.getMacroVar()+"')", String.class);
	getJdbcTemplate().execute("DELETE FROM CONNECTOR_FILE_UPLOAD_MAPPER  where UPPER(CONNECTOR_ID) = UPPER('"+vObject.getMacroVar()+"')");
	tableName.stream().forEach(tablenm-> { try{getJdbcTemplate().execute("DROP TABLE "+tablenm+" PURGE");} catch(Exception e){e.printStackTrace();} });
	}              

}
