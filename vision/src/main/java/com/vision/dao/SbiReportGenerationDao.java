package com.vision.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;

import com.vision.dao.ReportWriterDao.TreePromptCallableStatement;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.AlphaSubTabVb;
import com.vision.vb.DCManualQueryVb;
import com.vision.vb.PromptIdsVb;
import com.vision.vb.PromptTreeVb;
import com.vision.vb.SbiReportPromptsVb;
import com.vision.vb.VcReportGenerationVb;
import com.vision.vb.VrdObjectPropVb;

@Component
public class SbiReportGenerationDao extends AbstractDao<VcReportGenerationVb> {

	@Override
	protected RowMapper getMapper() {
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				DCManualQueryVb vObj = new DCManualQueryVb();
				vObj.setQueryId(rs.getString("QUERY_ID"));
				vObj.setQueryDescription(rs.getString("QUERY_DESCRIPTION"));
				vObj.setDatabaseType(rs.getString("DATABASE_TYPE"));
				vObj.setDatabaseConnectivityDetails(rs.getString("DATABASE_CONNECTIVITY_DETAILS"));
				vObj.setLookupDataLoading(rs.getString("LOOKUP_DATA_LOADING"));
				vObj.setSqlQuery(ValidationUtil.isValid(rs.getString("SQL_QUERY")) ? rs.getString("SQL_QUERY") : "");
				vObj.setStgQuery1(ValidationUtil.isValid(rs.getString("STG_QUERY1")) ? rs.getString("STG_QUERY1") : "");
				vObj.setStgQuery2(ValidationUtil.isValid(rs.getString("STG_QUERY2")) ? rs.getString("STG_QUERY2") : "");
				vObj.setStgQuery3(ValidationUtil.isValid(rs.getString("STG_QUERY3")) ? rs.getString("STG_QUERY3") : "");
				vObj.setPostQuery(ValidationUtil.isValid(rs.getString("POST_QUERY")) ? rs.getString("POST_QUERY") : "");
				vObj.setVcqStatusNt(rs.getInt("VCQ_STATUS_NT"));
				vObj.setVcqStatus(rs.getInt("VCQ_STATUS"));
				vObj.setDbStatus(rs.getInt("VCQ_STATUS"));
				vObj.setRecordIndicatorNt(rs.getInt("RECORD_INDICATOR_NT"));
				vObj.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
				vObj.setMaker(rs.getLong("MAKER"));
				vObj.setVerifier(rs.getLong("VERIFIER"));
				vObj.setInternalStatus(rs.getInt("INTERNAL_STATUS"));
				vObj.setDateCreation(rs.getString("DATE_CREATION"));
				vObj.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				return vObj;
			}
		};
		return mapper;
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

	@SuppressWarnings("unchecked")
	public List<DCManualQueryVb> findActiveVcQueries(String queryId) throws DataAccessException {
		if (ValidationUtil.isValid(queryId)) {
			String sql = "SELECT * FROM VC_QUERIES WHERE VCQ_STATUS = 0 and QUERY_ID =? AND QUERY_VALID_FLAG = 'S' ORDER BY QUERY_ID";
			Object[] lParams = new Object[1];
			lParams[0] = queryId;
			return getJdbcTemplate().query(sql, lParams, getMapper());
		} else {
			String sql = "SELECT * FROM VC_QUERIES WHERE VCQ_STATUS = 0 ORDER BY QUERY_ID";
			return getJdbcTemplate().query(sql, getMapper());
		}
	}

	public String getManualCalendarRange(String from, String to) {
		String calenderQuery = "SELECT TO_CHAR(PREV_YEAR,'DD-MON-RRRR')|| '@-@'||TO_CHAR(FUT_YEAR,'DD-MON-RRRR') BUSINESS_DATE FROM "
				+ "(SELECT TO_DATE(SYSDATE - " + from + ",'DD-MON-RRRR') AS PREV_YEAR, " + "TO_DATE(SYSDATE + " + to
				+ ",'DD-MON-RRRR') AS FUT_YEAR FROM DUAL)";
		return getJdbcTemplate().queryForObject(calenderQuery, String.class);
	}

	private RowMapper getPromptTreeDataMapper() {
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				PromptTreeVb promptTreeVb = new PromptTreeVb();
				promptTreeVb.setField1(rs.getString("FIELD_1"));
				promptTreeVb.setField2(rs.getString("FIELD_2"));
				promptTreeVb.setField3(rs.getString("FIELD_3"));
				promptTreeVb.setField4(rs.getString("FIELD_4"));
				promptTreeVb.setPromptId(rs.getString("PROMPT_ID"));
				return promptTreeVb;
			}
		};
		return mapper;
	}

	private RowMapper getPromptTreeMapper() {
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				AlphaSubTabVb promptVb = new AlphaSubTabVb();
				promptVb.setAlphaSubTab(rs.getString("FIELD_1"));
				promptVb.setDescription(rs.getString("FIELD_2"));
				return promptVb;
			}
		};
		return mapper;
	}

	public List<PromptTreeVb> getTreePromptData(PromptIdsVb prompt) {
		setServiceDefaults();
		String query = "SELECT FIELD_1, FIELD_2, FIELD_3, FIELD_4, PROMPT_ID FROM "
				+ "PROMPTS_STG WHERE VISION_ID = ? AND SESSION_ID= ? AND PROMPT_ID = ? ";
		String sessionId = String.valueOf(System.currentTimeMillis());
		CallableStatementCreator creator = new TreePromptCallableStatement(prompt, sessionId,
				String.valueOf(intCurrentUserId));
		CallableStatementCallback callBack = (CallableStatementCallback) creator;
		PromptTreeVb result = (PromptTreeVb) getJdbcTemplate().execute(creator, callBack);
		if (result != null && "0".equalsIgnoreCase(result.getStatus())) {
			String[] params = new String[3];
			params[0] = String.valueOf(intCurrentUserId);
			params[1] = sessionId;
			params[2] = prompt.getPromptId();
			prompt.setFilterStr(result.getFilterString());
			if (ValidationUtil.isValid(prompt.getSortStr())) {
				query = query + " " + prompt.getSortStr();
			}

			List<PromptTreeVb> tempPromptsList = getJdbcTemplate().query(query, params, getPromptTreeDataMapper());
			query = query.substring(query.indexOf("FROM "), query.indexOf("Order By") - 1);
			query = "DELETE " + query;
			int cout = getJdbcTemplate().update(query, params);
			return tempPromptsList;
		} else if (result != null && "1".equalsIgnoreCase(result.getStatus())) {
			return new ArrayList<PromptTreeVb>(0);
		}
		throw new RuntimeCustomException(result.getErrorMessage());
	}

	public List<AlphaSubTabVb> getComboPromptData(PromptIdsVb prompt, PromptTreeVb promptInputVb) {
		setServiceDefaults();
		String query = "SELECT FIELD_1, FIELD_2, FIELD_3, FIELD_4, PROMPT_ID FROM PROMPTS_STG WHERE VISION_ID = ? AND SESSION_ID= ? AND PROMPT_ID = ?";
		if ("CALENDAR".equalsIgnoreCase(prompt.getPromptType())) {
			query = "SELECT to_char(min(to_date(field_1,'DD-MM-RRRR')),'DD-MON-RRRR') FIELD_1, to_char(max(to_date(field_1,'DD-MM-RRRR')),'DD-MON-RRRR') FIELD_2 FROM PROMPTS_STG WHERE VISION_ID = ? AND SESSION_ID= ? AND PROMPT_ID = ?";
		}
		Connection con = null;
		strCurrentOperation = "Prompts";
		CallableStatement cs = null;
		try {
			if (!ValidationUtil.isValid(prompt.getPromptScript())) {
				strErrorDesc = "Invalid prompt script in Prompt Ids table for Prompt Id[" + prompt.getPromptId() + "].";
				throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
			}
			String sessionId = String.valueOf(System.currentTimeMillis());
			con = getConnection();
			cs = con.prepareCall("{call " + prompt.getPromptScript() + "}");
			int parameterCount = prompt.getPromptScript().split("\\?").length - 1;

			if (parameterCount != 7 && parameterCount > 6) {
				cs.setString(1, String.valueOf(intCurrentUserId));
				cs.setString(2, sessionId);
				cs.setString(3, prompt.getPromptId());
				if (promptInputVb == null) {
					cs.setString(4, "");// Filter Condition
				} else {
					cs.setString(4, promptInputVb.getField1());// Filter Condition
				}
				cs.registerOutParameter(5, java.sql.Types.VARCHAR);// filterString
				cs.registerOutParameter(6, java.sql.Types.VARCHAR); // Status
				cs.registerOutParameter(7, java.sql.Types.VARCHAR); // Category (T-Trigger error,V-Validation Error)
			} else if (parameterCount == 7) {
				cs.setString(1, String.valueOf(intCurrentUserId));
				cs.setString(2, sessionId);
				cs.setString(3, prompt.getPromptId());
				if (promptInputVb == null) {
					cs.setString(4, "");// Filter Condition
					cs.setString(5, "");// Filter Condition
				} else {
					cs.setString(4, promptInputVb.getField1());// Filter Condition
					cs.setString(5, promptInputVb.getField2());// Filter Condition
				}
				cs.registerOutParameter(6, java.sql.Types.VARCHAR); // Status
				cs.registerOutParameter(7, java.sql.Types.VARCHAR); // Category (T-Trigger error,V-Validation Error)
			} else if (parameterCount == 6) {
				cs.setString(1, String.valueOf(intCurrentUserId));
				cs.setString(2, sessionId);
				cs.setString(3, prompt.getPromptId());
				if (promptInputVb == null) {
					cs.setString(4, "");// Filter Condition
				} else {
					cs.setString(4, promptInputVb.getField1());// Filter Condition
				}
				cs.registerOutParameter(5, java.sql.Types.VARCHAR); // Status
				cs.registerOutParameter(6, java.sql.Types.VARCHAR); // Error Message
			} else {
				cs.setString(1, String.valueOf(intCurrentUserId));
				cs.setString(2, sessionId);
				cs.setString(3, prompt.getPromptId());
				cs.registerOutParameter(4, java.sql.Types.VARCHAR); // Status
				cs.registerOutParameter(5, java.sql.Types.VARCHAR); // Error Message
			}
			/*
			 * P_Status = -1, if there is an error; P_ErrorMsg will contain the error string
			 * p_Status = 0, if procedure executes successfully. P_ErrorMsg will contain
			 * nothing in this case p_Status = 1, Procedure has fetched NO records for the
			 * given query criteria. P_ErrorMsg will contain nothing.
			 */
			ResultSet rs = cs.executeQuery();
			PromptTreeVb promptTreeVb = new PromptTreeVb();
			if (parameterCount != 7 && parameterCount > 6) {
				promptTreeVb.setFilterString(cs.getString(5));
				prompt.setFilterStr(cs.getString(5));
				promptTreeVb.setStatus(cs.getString(6));
				promptTreeVb.setErrorMessage(cs.getString(7));
			} else if (parameterCount == 7) {
				promptTreeVb.setStatus(cs.getString(6));
				promptTreeVb.setErrorMessage(cs.getString(7));
			} else if (parameterCount == 6) {
				promptTreeVb.setStatus(cs.getString(5));
				promptTreeVb.setErrorMessage(cs.getString(6));
			} else {
				promptTreeVb.setStatus(cs.getString(4));
				promptTreeVb.setErrorMessage(cs.getString(5));
			}
			rs.close();
			if (promptTreeVb != null && "0".equalsIgnoreCase(promptTreeVb.getStatus())) {
				String[] params = new String[3];
				params[0] = String.valueOf(intCurrentUserId);
				params[1] = sessionId;
				params[2] = prompt.getPromptId();
				if (ValidationUtil.isValid(prompt.getSortStr())) {
					query = query + " " + prompt.getSortStr();
				}
				List<AlphaSubTabVb> tempPromptsList = null;
				if ("CALENDAR".equalsIgnoreCase(prompt.getPromptType())) {
					tempPromptsList = getJdbcTemplate().query(query, params, getPromptCalendarMapper());
				} else {
					tempPromptsList = getJdbcTemplate().query(query, params, getPromptTreeMapper());
				}
				query = query.toUpperCase();
				if (query.indexOf("ORDER BY") > 0) {
					query = query.substring(query.indexOf("FROM "), query.indexOf("ORDER BY") - 1);
				} else {
					query = query.substring(query.indexOf("FROM "), query.length());
				}
				query = "DELETE " + query;
				int count = getJdbcTemplate().update(query, params);

				return tempPromptsList;
			} else if (promptTreeVb != null && "1".equalsIgnoreCase(promptTreeVb.getStatus())) {
				return new ArrayList<AlphaSubTabVb>(0);
			}
			throw new RuntimeCustomException(promptTreeVb.getErrorMessage());
		} catch (SQLException ex) {
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException("", "", ex));
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		} catch (Exception ex) {
			ex.printStackTrace();
			strErrorDesc = ex.getMessage().trim();
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		} finally {
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
		}
	}

	class TreePromptCallableStatement implements CallableStatementCreator, CallableStatementCallback {
		private PromptIdsVb vObject = null;
		private String currentTimeAsSessionId = null;
		private String visionId = null;

		public TreePromptCallableStatement(PromptIdsVb vObject, String currentTimeAsSessionId, String visionId) {
			this.vObject = vObject;
			this.currentTimeAsSessionId = currentTimeAsSessionId;
			this.visionId = visionId;
		}

		public CallableStatement createCallableStatement(Connection connection) throws SQLException {
			CallableStatement cs = connection.prepareCall("{call " + vObject.getPromptScript() + "}");
			cs.setString(1, visionId);
			cs.setString(2, currentTimeAsSessionId);
			cs.setString(3, vObject.getPromptId());
			cs.registerOutParameter(4, java.sql.Types.VARCHAR);// filterString
			cs.registerOutParameter(5, java.sql.Types.VARCHAR); // Status
			/*
			 * P_Status = -1, if there is an error; P_ErrorMsg will contain the error string
			 * p_Status = 0, if procedure executes successfully. P_ErrorMsg will contain
			 * nothing in this case p_Status = 1, Procedure has fetched NO records for the
			 * given query criteria. P_ErrorMsg will contain nothing.
			 */
			cs.registerOutParameter(6, java.sql.Types.VARCHAR); // Error Message
			return cs;
		}

		public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
			ResultSet rs = cs.executeQuery();
			PromptTreeVb promptTreeVb = new PromptTreeVb();
			promptTreeVb.setFilterString(cs.getString(4));
			promptTreeVb.setStatus(cs.getString(5));
			promptTreeVb.setErrorMessage(cs.getString(6));
			rs.close();
			return promptTreeVb;
		}
	}

	private RowMapper getPromptCalendarMapper() {
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				PromptTreeVb promptTreeVb = new PromptTreeVb();
				promptTreeVb.setField1(rs.getString("FIELD_1"));
				promptTreeVb.setField2(rs.getString("FIELD_2"));
				return promptTreeVb;
			}
		};
		return mapper;
	}

	public VcReportGenerationVb getVrdPageDesign(VcReportGenerationVb dObj) throws DataAccessException {
		String sql = "Select REPORT_ID, PAGE_ID, PAGE_TITLE, HEADER_XML_CONTENT, FOOTER_XML_CONTENT, PROMPT_PAGE_SORT, REPORT_XML_CONTENT, "
				+ "PAGE_SORT, VRD_PAGE_STATUS_NT, VRD_PAGE_STATUS, RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, "
				+ "DATE_LAST_MODIFIED, DATE_CREATION FROM  VRD_PROMPT_DESIGN WHERE REPORT_ID = ? AND PAGE_ID = ?";
		Object[] args = { dObj.getReportId(), dObj.getPageNo() };

		ResultSetExtractor<VcReportGenerationVb> rse = new ResultSetExtractor<VcReportGenerationVb>() {
			@Override
			public VcReportGenerationVb extractData(ResultSet rs) throws SQLException, DataAccessException {
				VcReportGenerationVb reportVb = null;
				if (rs.next()) {
					reportVb = new VcReportGenerationVb();
					reportVb.setReportId(rs.getString("REPORT_ID"));
					reportVb.setPageId(rs.getInt("PAGE_ID"));
					reportVb.setPageTitle(rs.getString("PAGE_TITLE"));
					reportVb.setHeaderXmlContent(rs.getString("HEADER_XML_CONTENT"));
					reportVb.setFooterXmlContent(rs.getString("FOOTER_XML_CONTENT"));
					reportVb.setPromptPageSort(rs.getString("PROMPT_PAGE_SORT"));
					reportVb.setReportXmlContent(rs.getString("REPORT_XML_CONTENT"));
					reportVb.setPageSort(rs.getInt("PAGE_SORT"));
					reportVb.setDbStatus(rs.getInt("VRD_PAGE_STATUS"));
					reportVb.setRecordIndicatorNt(rs.getInt("RECORD_INDICATOR_NT"));
					reportVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
					reportVb.setMaker(rs.getInt("MAKER"));
					reportVb.setVerifier(rs.getInt("VERIFIER"));
					reportVb.setInternalStatus(rs.getInt("INTERNAL_STATUS"));
					reportVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
					reportVb.setDateCreation(rs.getString("DATE_CREATION"));
				}
				return reportVb;
			}
		};

		return getJdbcTemplate().query(sql, args, rse);
	}

	public VcReportGenerationVb getVrdMainReportData(VcReportGenerationVb vObj) {
		String sql = "SELECT * FROM VRD_DESIGN WHERE UPPER(REPORT_ID) = UPPER(?) AND VRD_STATUS = 0";
		Object lParams[] = { vObj.getReportId() };
		ResultSetExtractor<VcReportGenerationVb> rse = new ResultSetExtractor<VcReportGenerationVb>() {
			public VcReportGenerationVb extractData(ResultSet rs) throws SQLException, DataAccessException {
				VcReportGenerationVb vcReportGenerationVb = null;
				while (rs.next()) {
					vcReportGenerationVb = new VcReportGenerationVb();
					vcReportGenerationVb.setReportId(rs.getString("REPORT_ID"));
					vcReportGenerationVb.setReportDescription(rs.getString("REPORT_DESCRIPTION"));
					vcReportGenerationVb.setReportTitle(rs.getString("REPORT_TITLE"));
					vcReportGenerationVb.setVrdConditionalXml(rs.getString("CONDITIONAL_XML"));
					vcReportGenerationVb.setVrdQueryXml(rs.getString("QUERY_XML"));
				}
				return vcReportGenerationVb;
			}

		};
		return getJdbcTemplate().query(sql, lParams, rse);
	}

	public VcReportGenerationVb getVrdSubReportData(VcReportGenerationVb vObj) {
		String sql = "SELECT * FROM VRD_REPORT_DESIGN WHERE UPPER (REPORT_ID) = UPPER (?) AND UPPER (SUB_REPORT_ID) = UPPER (?)";
		Object lParams[] = { vObj.getReportId(), vObj.getSubReportId() };
		ResultSetExtractor<VcReportGenerationVb> rse = new ResultSetExtractor<VcReportGenerationVb>() {
			public VcReportGenerationVb extractData(ResultSet rs) throws SQLException, DataAccessException {
				VcReportGenerationVb vcReportGenerationVb = null;
				while (rs.next()) {
					vcReportGenerationVb.setReportId(rs.getString("REPORT_ID"));
					vcReportGenerationVb.setSubReportId(rs.getString("SUB_REPORT_ID"));
					vcReportGenerationVb.setReportType(rs.getString("REPORT_TYPE"));
					vcReportGenerationVb.setPageId(rs.getInt("PAGE_ID"));
					vcReportGenerationVb.setReportContext(rs.getString("REPORT_CONTEXT"));
				}
				return vcReportGenerationVb;
			}

		};
		return getJdbcTemplate().query(sql, lParams, rse);
	}
	
	public List<VrdObjectPropVb> findActiveColorPaletteFromObjProperties(String objTagId) throws DataAccessException {
		String sql = "SELECT * FROM VRD_OBJECT_PROPERTIES WHERE VRD_OBJECT_ID='Palette' AND OBJ_TAG_ID='"+objTagId+"' AND OBJ_TAG_STATUS=0 ORDER BY OBJ_TAG_ID";
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				VrdObjectPropVb vObj = new VrdObjectPropVb();
				vObj.setVrdObjectId(rs.getString("VRD_OBJECT_ID"));
				vObj.setObjTagId(rs.getString("OBJ_TAG_ID"));
				if(ValidationUtil.isValid(rs.getString("HTML_TAG_PROPERTY")))
					vObj.setHtmlTagProperty(CommonUtils.getValueForXmlTag(rs.getString("HTML_TAG_PROPERTY"), "COLOR_CODE"));
				vObj.setObjTagStatus(rs.getInt("OBJ_TAG_STATUS"));
				vObj.setObjTagDesc(rs.getString("OBJ_TAG_DESC"));
				vObj.setObjTagIconLink(rs.getString("OBJ_TAG_ICON_LINK"));
				return vObj;
			}
		};
		return  getJdbcTemplate().query(sql, mapper);
	}
	
	public String getChartXmlFormObjProperties(String chartType){
		try {
			String sql = "select HTML_TAG_PROPERTY from VRD_OBJECT_PROPERTIES where "
					/*+ "VRD_OBJECT_ID='Col3D' AND "*/
					+ "UPPER(OBJ_TAG_ID)=UPPER('"+chartType+"')";
			return getJdbcTemplate().queryForObject(sql, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}	
	
	
	
}