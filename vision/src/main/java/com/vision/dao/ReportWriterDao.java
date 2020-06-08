package com.vision.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vision.authentication.SessionContextHolder;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.ColumnHeadersVb;
import com.vision.vb.CommonVb;
import com.vision.vb.CustomersDlyVb;
import com.vision.vb.PromptIdsVb;
import com.vision.vb.PromptTreeVb;
import com.vision.vb.RCReportFieldsVb;
import com.vision.vb.RWAccessVb;
import com.vision.vb.ReportStgVb;
import com.vision.vb.ReportsWriterVb;
import com.vision.vb.RsDashboardVb;
import com.vision.vb.ScheduleReportVb;
import com.vision.vb.TemplateStgVb;

@Component
public class ReportWriterDao extends AbstractDao<ReportsWriterVb>{
	@Override
	protected void setServiceDefaults(){
		serviceName = "ReportWriter";
		serviceDesc = "ReportWriter";;
		tableName = "REPORT_SUITE";
		childTableName = "REPORT_SUITE";
		intCurrentUserId = SessionContextHolder.getContext().getVisionId();
	}
	public List<ReportsWriterVb> getDistinctReportCategoryForUser(){
		setServiceDefaults();
		final int intKeyFieldsCount = 1;
		String query = new String("select DISTINCT RSA.REPORT_CATEGORY, ALPHA_SUBTAB_DESCRIPTION, ALPHA_SUB_TAB from RS_ACCESS RSA "+
			"JOIN ALPHA_SUB_TAB ON ALPHA_TAB=REPORT_CATEGORY_AT AND ALPHA_SUB_TAB = REPORT_CATEGORY "+
			"JOIN VISION_USERS VU ON RSA.USER_PROFILE = VU.USER_PROFILE AND RSA.USER_GROUP = VU.USER_GROUP "+
			"JOIN REPORT_SUITE RS ON RS.REPORT_CATEGORY = RSA.REPORT_CATEGORY AND RS_STATUS =0 AND RA_STATUS =0 "+
			"WHERE VISION_ID= ? and ALPHA_SUB_TAB != 'MASKED' AND ALPHA_SUBTAB_STATUS =0 ORDER BY ALPHA_SUB_TAB ");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = intCurrentUserId;
		try{
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					ReportsWriterVb reporterWriterVb = new ReportsWriterVb();
					reporterWriterVb.setReportId(rs.getString("REPORT_CATEGORY"));
					reporterWriterVb.setReportDescription(rs.getString("ALPHA_SUBTAB_DESCRIPTION"));
					return reporterWriterVb;
				}
			};
			return  getJdbcTemplate().query(query, params, mapper);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			return null;
		}
	}
	public List<ReportsWriterVb> getDistinctSubCategoryForUserByReportCat(String reportCategory){
		setServiceDefaults();
		final int intKeyFieldsCount = 2;
		String query = new String("with t as ("+
				"select CASE WHEN RWAccess.SUB_CATEGORY = 'NA' THEN "+
				"concatenate_list(CURSOR(SELECT DISTINCT SUB_CATEGORY FROM REPORT_SUITE RS WHERE RS.REPORT_CATEGORY = RWAccess.REPORT_CATEGORY "
				+ " AND REPORT_TYPE IN ('L','D','R','C','X','E', 'V') AND RS_STATUS =0 ) ) "+
				"ELSE RWAccess.SUB_CATEGORY END  as SUB_CATEGORY FROM RS_ACCESS RWAccess " +
				"JOIN VISION_USERS VU ON RWAccess.USER_PROFILE = VU.USER_PROFILE AND RWAccess.USER_GROUP = VU.USER_GROUP " +
				"where REPORT_CATEGORY = ? AND VISION_ID=? AND RWAccess.RA_STATUS = 0) "+
				"select ALPHA_SUB_TAB as REPORT_CATEGORY, ALPHA_SUBTAB_DESCRIPTION FROM ALPHA_SUB_TAB WHERE ALPHA_TAB = 453 AND ALPHA_SUB_TAB IN ( "+
				"select trim(x.column_value.extract('e/text()')) cols "+ 
				"from t t, table (xmlsequence(xmltype('<e><e>' || replace(t.SUB_CATEGORY,',','</e><e>')|| '</e></e>').extract('e/e'))) x) order by alpha_sub_Tab");
		Object params[] = new Object[intKeyFieldsCount];
			   params[0] = reportCategory;
			   params[1] = intCurrentUserId;
		try{
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					ReportsWriterVb reporterWriterVb = new ReportsWriterVb();
					reporterWriterVb.setReportId(rs.getString("REPORT_CATEGORY"));
					reporterWriterVb.setReportDescription(rs.getString("ALPHA_SUBTAB_DESCRIPTION"));
					return reporterWriterVb;
				}
			};
			return getJdbcTemplate().query(query, params, mapper);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			return null;
		}
	}
	
	public List<RWAccessVb> getRwAccessForUserBy(String reportCategory, String subCategory){
		setServiceDefaults();
		final int intKeyFieldsCount = 3;
		String query = new String("SELECT REPORT_CATEGORY, SUB_CATEGORY, REPORT_ID " +
				"FROM RS_ACCESS RSA JOIN VISION_USERS VU ON RSA.USER_PROFILE = VU.USER_PROFILE AND RSA.USER_GROUP = VU.USER_GROUP " +
				"WHERE  VISION_ID = ? AND REPORT_CATEGORY = ? AND SUB_CATEGORY = ?");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = intCurrentUserId;
		params[1] = reportCategory;
		params[2] = subCategory;
		try{
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					RWAccessVb rwAccessVb = new RWAccessVb();
					rwAccessVb.setReportCategory(rs.getString("REPORT_CATEGORY"));
					rwAccessVb.setSubCategory(rs.getString("SUB_CATEGORY"));
					rwAccessVb.setReportId(rs.getString("REPORT_ID"));
					return rwAccessVb;
				}
			};
			return getJdbcTemplate().query(query, params, mapper);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			return null;
		}
	}
	public List<ReportsWriterVb> getReportsBy(String reportCategory, String subCategory){
		setServiceDefaults();
		final int intKeyFieldsCount = 2;
		String query = new String("SELECT PROMPT_POSITION_1,PROMPT_POSITION_2,PROMPT_POSITION_3,PROMPT_POSITION_4,PROMPT_POSITION_5,PROMPT_POSITION_6, REPORT_ID, REPORT_DESCRIPTION, REPORT_TITLE, REPORT_CATEGORY_AT, REPORT_CATEGORY, SUB_CATEGORY_AT, SUB_CATEGORY, " +
				"LABEL_ROW_COUNT, LABEL_COL_COUNT, REP_PROCEDURE, FILTER_STRING, " +
				"PROMPT_ID_1, PROMPT_ID_2, PROMPT_ID_3, PROMPT_ID_4, PROMPT_ID_5, PROMPT_ID_6, "+
				//+ "PROMPT_MACRO_MAPPINGS_1, PROMPT_MACRO_MAPPINGS_2, PROMPT_MACRO_MAPPINGS_3, PROMPT_MACRO_MAPPINGS_4, PROMPT_MACRO_MAPPINGS_5, PROMPT_MACRO_MAPPINGS_6, "
				"ORIENTATION, RS_STATUS_NT, RS_STATUS, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION, RECORD_INDICATOR_NT, " +
				"RECORD_INDICATOR, REPORT_TYPE, DISPLAY_TYPE, DD_FLAG, DD_REPORT_ID, SCALLING_FACTOR, TEMPLATE_NAME, BURST_FLAG, "+
				"PROMPT_LABEL_1, PROMPT_LABEL_2, PROMPT_LABEL_3, PROMPT_LABEL_4, PROMPT_LABEL_5, PROMPT_LABEL_6 " +
				"FROM REPORT_SUITE WHERE REPORT_CATEGORY = ? AND SUB_CATEGORY = ? AND RS_STATUS=0 AND REPORT_TYPE  IN ('L','D','R','C','X','E', 'V') ORDER BY DISPLAY_ORDER ");
		
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = reportCategory;
		params[1] = subCategory;
		try{
			return getJdbcTemplate().query(query, params, getMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			return null;
		}
	}
	public List<ReportsWriterVb> getReportSuiteDataByDDReportId(String drillDownReportId){
		setServiceDefaults();
		String query = new String("SELECT PROMPT_POSITION_1,PROMPT_POSITION_2,PROMPT_POSITION_3,PROMPT_POSITION_4,PROMPT_POSITION_5,PROMPT_POSITION_6, REPORT_ID, REPORT_DESCRIPTION, REPORT_TITLE, REPORT_CATEGORY_AT, REPORT_CATEGORY, SUB_CATEGORY_AT, SUB_CATEGORY, " +
				"LABEL_ROW_COUNT, LABEL_COL_COUNT, REP_PROCEDURE, FILTER_STRING, " +
				"PROMPT_ID_1, PROMPT_ID_2, PROMPT_ID_3, PROMPT_ID_4, PROMPT_ID_5, PROMPT_ID_6, "+
//				"PROMPT_MACRO_MAPPINGS_1, PROMPT_MACRO_MAPPINGS_2, PROMPT_MACRO_MAPPINGS_3, PROMPT_MACRO_MAPPINGS_4, PROMPT_MACRO_MAPPINGS_5, PROMPT_MACRO_MAPPINGS_6, "+
				"ORIENTATION, RS_STATUS_NT, RS_STATUS, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION, RECORD_INDICATOR_NT, " +
				"RECORD_INDICATOR, REPORT_TYPE, DISPLAY_TYPE, DD_FLAG, DD_REPORT_ID, SCALLING_FACTOR, TEMPLATE_NAME, BURST_FLAG ,"+
				"PROMPT_LABEL_1, PROMPT_LABEL_2, PROMPT_LABEL_3, PROMPT_LABEL_4, PROMPT_LABEL_5, PROMPT_LABEL_6 " +
				"FROM REPORT_SUITE WHERE REPORT_ID = '"+drillDownReportId+"'");
		try{
			return getJdbcTemplate().query(query, getMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			return null;
		}
	}	
	public List<ReportsWriterVb> getReportsBy(String reportCategory, String subCategory, String reportId){
		setServiceDefaults();
		final int intKeyFieldsCount = 3;
		String query = new String("SELECT PROMPT_POSITION_1,PROMPT_POSITION_2,PROMPT_POSITION_3,PROMPT_POSITION_4,PROMPT_POSITION_5,PROMPT_POSITION_6, REPORT_ID, REPORT_DESCRIPTION, REPORT_TITLE, REPORT_CATEGORY_AT, REPORT_CATEGORY, SUB_CATEGORY_AT, SUB_CATEGORY, " +
				"LABEL_ROW_COUNT, LABEL_COL_COUNT, REP_PROCEDURE, FILTER_STRING, " +
				"PROMPT_ID_1, PROMPT_ID_2, PROMPT_ID_3, PROMPT_ID_4, PROMPT_ID_5, PROMPT_ID_6, "+
//				"PROMPT_MACRO_MAPPINGS_1, PROMPT_MACRO_MAPPINGS_2, PROMPT_MACRO_MAPPINGS_3, PROMPT_MACRO_MAPPINGS_4, PROMPT_MACRO_MAPPINGS_5, PROMPT_MACRO_MAPPINGS_6 , "+
				"ORIENTATION, RS_STATUS_NT, RS_STATUS, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION, RECORD_INDICATOR_NT, " +
				"RECORD_INDICATOR, REPORT_TYPE, DISPLAY_TYPE, DD_FLAG, DD_REPORT_ID, SCALLING_FACTOR, TEMPLATE_NAME, BURST_FLAG," +
				"PROMPT_LABEL_1, PROMPT_LABEL_2, PROMPT_LABEL_3, PROMPT_LABEL_4, PROMPT_LABEL_5, PROMPT_LABEL_6 " +
				"FROM REPORT_SUITE WHERE REPORT_CATEGORY = ? AND SUB_CATEGORY = ? AND REPORT_ID = ? AND RECORD_INDICATOR=0 AND RS_STATUS=0" +
				" AND REPORT_TYPE  IN ('L','D','R','C','X','E')");
		Object params[] = new Object[intKeyFieldsCount];
			   params[0] = reportCategory;
			   params[1] = subCategory;
			   params[2] = reportId;
		try{
			return getJdbcTemplate().query(query, params, getMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			return null;
		}
	}
	
	public List<PromptIdsVb> getQueryForPrompts(String string) throws DataAccessException{
		setServiceDefaults();
		int intKeyFieldsCount = 1;
		String query = new String("SELECT PROMPT_ID, PROMPT_DESC, PROMPT_STRING, PROMPT_SCRIPT, PROMPT_TYPE_AT, PROMPT_TYPE, " +
				"DEPENDENT_PROMPT_ID, PROMPT_STATUS_NT, PROMPT_STATUS, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION, " +
				"RECORD_INDICATOR_NT, RECORD_INDICATOR, SORT_STRING,SCHEDULE_SCRIPT,SCHEDULE_SCRIPT_ORDER FROM PROMPT_IDS WHERE PROMPT_ID = ?");
		try{
			Object params[] = new Object[intKeyFieldsCount];
			params[0] = string;
			return getJdbcTemplate().query(query, params, getPromptIdsMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			return null;
		}
	}
	public List<PromptIdsVb> getPromptsForCatalog() {
		setServiceDefaults();
		String query = new String("SELECT PROMPT_ID, PROMPT_DESC, PROMPT_STRING, PROMPT_SCRIPT, PROMPT_TYPE_AT, PROMPT_TYPE, " +
				"DEPENDENT_PROMPT_ID, PROMPT_STATUS_NT, PROMPT_STATUS, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION, " +
				"RECORD_INDICATOR_NT, RECORD_INDICATOR, SORT_STRING,SCHEDULE_SCRIPT,SCHEDULE_SCRIPT_ORDER,PROMPT_LOGIC,PROMPT_LOGIC_AT FROM PROMPT_IDS " +
				"WHERE PROMPT_TYPE IN ('COMBO','TEXT','FTEXT','NTEXT','UNTEXT')");
		try{
			return getJdbcTemplate().query(query, getPromptIdsMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			return null;
		}
	}
	public List<ColumnHeadersVb> getColumnHeaders(PromptTreeVb promptTree) {
		setServiceDefaults();
		int intKeyFieldsCount = 2;
		String query = new String("SELECT REPORT_ID, SESSION_ID, LABEL_ROW_NUM, LABEL_COL_NUM, CAPTION, COLUMN_WIDTH, COL_TYPE, ROW_SPAN, COL_SPAN, NUMERIC_COLUMN_NO, DB_COLUMN " +
				"FROM COLUMN_HEADERS_STG WHERE REPORT_ID = ? AND SESSION_ID=? ORDER BY 3,4");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = promptTree.getReportId();
		params[1] = promptTree.getSessionId();
		try{
			return getJdbcTemplate().query(query, params, getColumnHeadersMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			return null;
		}
	}	
	public List<ReportStgVb> getReportsStgData(PromptTreeVb promptTree) {
		setServiceDefaults();
		int intKeyFieldsCount = 2;
		String query = new String(
						 "SELECT REPORT_ID, "
						 +" SESSION_ID, "
						 +"  CAPTION_COLUMN_1, " 
						 +" CAPTION_COLUMN_2, " 
						 +" CAPTION_COLUMN_3, " 
						 +" CAPTION_COLUMN_4, "
						 +" CAPTION_COLUMN_5, "   
						 +" trim(DATA_COLUMN_1) DATA_COLUMN_1,"
						 +" trim(DATA_COLUMN_2) DATA_COLUMN_2," 
						 +" trim(DATA_COLUMN_3) DATA_COLUMN_3,"
						 +" trim(DATA_COLUMN_4) DATA_COLUMN_4,"
						 +" trim(DATA_COLUMN_5) DATA_COLUMN_5, "
						 +" trim(DATA_COLUMN_6) DATA_COLUMN_6, "
						 +" trim(DATA_COLUMN_7) DATA_COLUMN_7, "
						 +" trim(DATA_COLUMN_8) DATA_COLUMN_8, "
						 +" trim(DATA_COLUMN_9) DATA_COLUMN_9, "
						 +" trim(DATA_COLUMN_10) DATA_COLUMN_10, " 
						 +" trim(DATA_COLUMN_11) DATA_COLUMN_11, "  
						 +" trim(DATA_COLUMN_12) DATA_COLUMN_12, " 
						 +" trim(DATA_COLUMN_13) DATA_COLUMN_13, " 
						 +" trim(DATA_COLUMN_14) DATA_COLUMN_14, " 
						 +" trim(DATA_COLUMN_15) DATA_COLUMN_15, " 
						 +" trim(DATA_COLUMN_16) DATA_COLUMN_16, " 
						 +" trim(DATA_COLUMN_17) DATA_COLUMN_17, " 
						 +" trim(DATA_COLUMN_18) DATA_COLUMN_18, " 
						 +" trim(DATA_COLUMN_19) DATA_COLUMN_19, "  
						 +" trim(DATA_COLUMN_20) DATA_COLUMN_20, " 
						 +" trim(DATA_COLUMN_21) DATA_COLUMN_21, " 
						 +" trim(DATA_COLUMN_22) DATA_COLUMN_22, " 
						 +" trim(DATA_COLUMN_23) DATA_COLUMN_23, " 
						 +" trim(DATA_COLUMN_24) DATA_COLUMN_24, " 
						 +" trim(DATA_COLUMN_25) DATA_COLUMN_25, " 
						 +" trim(DATA_COLUMN_26) DATA_COLUMN_26, " 
						 +" trim(DATA_COLUMN_27) DATA_COLUMN_27, "  
						 +" trim(DATA_COLUMN_28) DATA_COLUMN_28, " 
						 +" trim(DATA_COLUMN_29) DATA_COLUMN_29, " 
						 +" trim(DATA_COLUMN_30) DATA_COLUMN_30, " 
						 +" trim(DATA_COLUMN_31) DATA_COLUMN_31, " 
						 +" trim(DATA_COLUMN_32) DATA_COLUMN_32, " 
						 +" trim(DATA_COLUMN_33) DATA_COLUMN_33, " 
						 +" trim(DATA_COLUMN_34) DATA_COLUMN_34, " 
						 +" trim(DATA_COLUMN_35) DATA_COLUMN_35, "  
						 +" trim(DATA_COLUMN_36) DATA_COLUMN_36, " 
						 +" trim(DATA_COLUMN_37) DATA_COLUMN_37, " 
						 +" trim(DATA_COLUMN_38) DATA_COLUMN_38, " 
						 +" trim(DATA_COLUMN_39) DATA_COLUMN_39, " 
						 +" trim(DATA_COLUMN_40) DATA_COLUMN_40, "
						 +" FORMAT_TYPE, "
						 +" DD_KEY_ID,   "
						 +" DD_KEY_VALUE, " 
						 +" DD_KEY_LABEL " 
						 +" FROM REPORTS_STG "
						 +" WHERE REPORT_ID = ? "
						 +" AND SESSION_ID= ? "
						 +" ORDER BY SORT_FIELD");
				
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = promptTree.getReportId();
		params[1] = promptTree.getSessionId();
		try{
			return getJdbcTemplate().query(query, params, getReportsStgMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			return null;
		}
	}
	public List<ReportStgVb> getReportsStgMaxData(PromptTreeVb promptTree) {
		setServiceDefaults();
		int intKeyFieldsCount = 2;
		String totalQuery = "SELECT COUNT(1) FROM REPORTS_STG WHERE REPORT_ID = '"+promptTree.getReportId()+"' AND SESSION_ID= '"+promptTree.getSessionId()+"' ORDER BY SORT_FIELD";
		promptTree.setTotalRows(getJdbcTemplate().queryForObject(totalQuery, Long.class));
		String query = new String("SELECT REPORT_ID, "
				 +" SESSION_ID, "
				 +"  CAPTION_COLUMN_1, " 
				 +" CAPTION_COLUMN_2, " 
				 +" CAPTION_COLUMN_3, " 
				 +" CAPTION_COLUMN_4, "
				 +" CAPTION_COLUMN_5, "   
				 +" trim(DATA_COLUMN_1) DATA_COLUMN_1,"
				 +" trim(DATA_COLUMN_2) DATA_COLUMN_2," 
				 +" trim(DATA_COLUMN_3) DATA_COLUMN_3,"
				 +" trim(DATA_COLUMN_4) DATA_COLUMN_4,"
				 +" trim(DATA_COLUMN_5) DATA_COLUMN_5, "
				 +" trim(DATA_COLUMN_6) DATA_COLUMN_6, "
				 +" trim(DATA_COLUMN_7) DATA_COLUMN_7, "
				 +" trim(DATA_COLUMN_8) DATA_COLUMN_8, "
				 +" trim(DATA_COLUMN_9) DATA_COLUMN_9, "
				 +" trim(DATA_COLUMN_10) DATA_COLUMN_10, " 
				 +" trim(DATA_COLUMN_11) DATA_COLUMN_11, "  
				 +" trim(DATA_COLUMN_12) DATA_COLUMN_12, " 
				 +" trim(DATA_COLUMN_13) DATA_COLUMN_13, " 
				 +" trim(DATA_COLUMN_14) DATA_COLUMN_14, " 
				 +" trim(DATA_COLUMN_15) DATA_COLUMN_15, " 
				 +" trim(DATA_COLUMN_16) DATA_COLUMN_16, " 
				 +" trim(DATA_COLUMN_17) DATA_COLUMN_17, " 
				 +" trim(DATA_COLUMN_18) DATA_COLUMN_18, " 
				 +" trim(DATA_COLUMN_19) DATA_COLUMN_19, "  
				 +" trim(DATA_COLUMN_20) DATA_COLUMN_20, " 
				 +" trim(DATA_COLUMN_21) DATA_COLUMN_21, " 
				 +" trim(DATA_COLUMN_22) DATA_COLUMN_22, " 
				 +" trim(DATA_COLUMN_23) DATA_COLUMN_23, " 
				 +" trim(DATA_COLUMN_24) DATA_COLUMN_24, " 
				 +" trim(DATA_COLUMN_25) DATA_COLUMN_25, " 
				 +" trim(DATA_COLUMN_26) DATA_COLUMN_26, " 
				 +" trim(DATA_COLUMN_27) DATA_COLUMN_27, "  
				 +" trim(DATA_COLUMN_28) DATA_COLUMN_28, " 
				 +" trim(DATA_COLUMN_29) DATA_COLUMN_29, " 
				 +" trim(DATA_COLUMN_30) DATA_COLUMN_30, " 
				 +" trim(DATA_COLUMN_31) DATA_COLUMN_31, " 
				 +" trim(DATA_COLUMN_32) DATA_COLUMN_32, " 
				 +" trim(DATA_COLUMN_33) DATA_COLUMN_33, " 
				 +" trim(DATA_COLUMN_34) DATA_COLUMN_34, " 
				 +" trim(DATA_COLUMN_35) DATA_COLUMN_35, "  
				 +" trim(DATA_COLUMN_36) DATA_COLUMN_36, " 
				 +" trim(DATA_COLUMN_37) DATA_COLUMN_37, " 
				 +" trim(DATA_COLUMN_38) DATA_COLUMN_38, " 
				 +" trim(DATA_COLUMN_39) DATA_COLUMN_39, " 
				 +" trim(DATA_COLUMN_40) DATA_COLUMN_40, "
				 +" FORMAT_TYPE, "
				 +" DD_KEY_ID,   "
				 +" DD_KEY_VALUE, " 
				 +" DD_KEY_LABEL " 
				 +" FROM REPORTS_STG "
				 +" WHERE REPORT_ID = ? "
				 +" AND SESSION_ID= ? "
				 +" ORDER BY SORT_FIELD");
		
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = promptTree.getReportId();
		params[1] = promptTree.getSessionId();
		try{
			return getJdbcTemplate().query(query, params, getReportsStgMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			return null;
		}
	}	
	public int deleteReportsStgData(PromptTreeVb vObject){
		String query = "DELETE FROM REPORTS_STG WHERE REPORT_ID = ?  And SESSION_ID = ? ";
		Object args[] = {vObject.getReportId(), vObject.getSessionId()};
		return getJdbcTemplate().update(query,args);
	}
	public int deleteColumnHeadersData(PromptTreeVb vObject){
		String query = "DELETE FROM COLUMN_HEADERS_STG WHERE REPORT_ID = ?  And SESSION_ID = ? ";
		Object args[] = {vObject.getReportId(), vObject.getSessionId()};
		return getJdbcTemplate().update(query,args);
	}	
	public void deletePromptData() {
		setServiceDefaults();
		String query = new String("DELETE FROM PROMPTS_STG WHERE VISION_ID = ?");
		try{
			String[] params = new String[1];
			params[0] = String.valueOf(intCurrentUserId);
			getJdbcTemplate().update(query, params);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
		}
	}
	public List<PromptTreeVb> getTreePromptData(PromptIdsVb prompt){
		setServiceDefaults();
		String query = "SELECT FIELD_1, FIELD_2, FIELD_3, FIELD_4, PROMPT_ID FROM " +
				"PROMPTS_STG WHERE VISION_ID = ? AND SESSION_ID= ? AND PROMPT_ID = ? ";
		String sessionId = String.valueOf(System.currentTimeMillis());
		CallableStatementCreator creator = new TreePromptCallableStatement(prompt, sessionId, String.valueOf(intCurrentUserId));
		CallableStatementCallback callBack = (CallableStatementCallback)creator;
		PromptTreeVb result = (PromptTreeVb) getJdbcTemplate().execute(creator, callBack);
		if(result != null && "0".equalsIgnoreCase(result.getStatus())){
			String[] params = new String[3];
			params[0] = String.valueOf(intCurrentUserId);
			params[1] = sessionId;
			params[2] = prompt.getPromptId();
			prompt.setFilterStr(result.getFilterString());
			if(ValidationUtil.isValid(prompt.getSortStr())){
				query = query+ " " + prompt.getSortStr();
			}
			
			List<PromptTreeVb> tempPromptsList = getJdbcTemplate().query(query, params, getPromptTreeMapper()); 
			query = query.substring(query.indexOf("FROM "), query.indexOf("Order By")-1);
			query = "DELETE "+query;
			int cout = getJdbcTemplate().update(query, params);
			return tempPromptsList;			
		}else if(result != null && "1".equalsIgnoreCase(result.getStatus())){
			return new ArrayList<PromptTreeVb>(0);
		}
		throw new RuntimeCustomException(result.getErrorMessage());
	}
	public List<PromptTreeVb> getComboPromptData(PromptIdsVb prompt, PromptTreeVb promptInputVb){
		setServiceDefaults();
		String query = "SELECT FIELD_1, FIELD_2, FIELD_3, FIELD_4, PROMPT_ID FROM PROMPTS_STG WHERE VISION_ID = ? AND SESSION_ID= ? AND PROMPT_ID = ?";
		if("CALENDAR".equalsIgnoreCase(prompt.getPromptType())){
			query = "SELECT to_char(min(to_date(field_1,'DD-MM-RRRR')),'DD-MON-RRRR') FIELD_1, to_char(max(to_date(field_1,'DD-MM-RRRR')),'DD-MON-RRRR') FIELD_2 FROM PROMPTS_STG WHERE VISION_ID = ? AND SESSION_ID= ? AND PROMPT_ID = ?";
		}
		Connection con = null;
		strCurrentOperation = "Prompts";
		CallableStatement cs =  null;
		try{
			if(!ValidationUtil.isValid(prompt.getPromptScript())){
				strErrorDesc = "Invalid prompt script in Prompt Ids table for Prompt Id["+prompt.getPromptId()+"].";
				throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
			}
			String sessionId = String.valueOf(System.currentTimeMillis());
			con = getConnection();
			cs = con.prepareCall("{call "+prompt.getPromptScript()+"}");
			int parameterCount = prompt.getPromptScript().split("\\?").length -1;

			if(parameterCount != 7 && parameterCount > 6){
				cs.setString(1, String.valueOf(intCurrentUserId));
		        cs.setString(2, sessionId);
	        	cs.setString(3, prompt.getPromptId());
	        	if(promptInputVb == null){
		        	cs.setString(4, "");//Filter Condition	        		
	        	}else{
		        	cs.setString(4, promptInputVb.getField1());//Filter Condition
	        	}
	        	cs.registerOutParameter(5, java.sql.Types.VARCHAR);//filterString
		        cs.registerOutParameter(6, java.sql.Types.VARCHAR); //Status 
		        cs.registerOutParameter(7, java.sql.Types.VARCHAR); //Category (T-Trigger error,V-Validation Error)
			}else if(parameterCount == 7){
				cs.setString(1, String.valueOf(intCurrentUserId));
		        cs.setString(2, sessionId);
	        	cs.setString(3, prompt.getPromptId());
	        	if(promptInputVb == null){
		        	cs.setString(4, "");//Filter Condition	
		        	cs.setString(5, "");//Filter Condition	        		
	        	}else{
		        	cs.setString(4, promptInputVb.getField1());//Filter Condition
		        	cs.setString(5, promptInputVb.getField2());//Filter Condition
	        	}
		        cs.registerOutParameter(6, java.sql.Types.VARCHAR); //Status 
		        cs.registerOutParameter(7, java.sql.Types.VARCHAR); //Category (T-Trigger error,V-Validation Error)
			}else if(parameterCount == 6){
				cs.setString(1, String.valueOf(intCurrentUserId));
		        cs.setString(2, sessionId);
	        	cs.setString(3, prompt.getPromptId());
	        	if(promptInputVb == null){
		        	cs.setString(4, "");//Filter Condition	        		
	        	}else{
		        	cs.setString(4, promptInputVb.getField1());//Filter Condition
	        	}
		        cs.registerOutParameter(5, java.sql.Types.VARCHAR); //Status 
		        cs.registerOutParameter(6, java.sql.Types.VARCHAR);  //Error Message
			}else{
				cs.setString(1, String.valueOf(intCurrentUserId));
		        cs.setString(2, sessionId);
	        	cs.setString(3, prompt.getPromptId());
		        cs.registerOutParameter(4, java.sql.Types.VARCHAR); //Status 
		        cs.registerOutParameter(5, java.sql.Types.VARCHAR); //Error Message
			}
		      /*P_Status = -1, if there is an error; P_ErrorMsg will contain the error string
		  				p_Status =  0, if procedure executes successfully. P_ErrorMsg will contain nothing in this case
		  				p_Status =  1, Procedure has fetched NO records for the given query criteria. P_ErrorMsg will contain nothing.*/
			ResultSet rs = cs.executeQuery();
			PromptTreeVb promptTreeVb = new PromptTreeVb();
	        if(parameterCount != 7 && parameterCount > 6){
	            promptTreeVb.setFilterString(cs.getString(5));
	            prompt.setFilterStr(cs.getString(5));
	            promptTreeVb.setStatus(cs.getString(6));
	            promptTreeVb.setErrorMessage(cs.getString(7));
	        }else if(parameterCount ==7){
	            promptTreeVb.setStatus(cs.getString(6));
	            promptTreeVb.setErrorMessage(cs.getString(7));
	        }else if(parameterCount ==6){
	            promptTreeVb.setStatus(cs.getString(5));
	            promptTreeVb.setErrorMessage(cs.getString(6));
	        }else{
	        	promptTreeVb.setStatus(cs.getString(4));
	            promptTreeVb.setErrorMessage(cs.getString(5));
	        }
		    rs.close();
			if(promptTreeVb != null && "0".equalsIgnoreCase(promptTreeVb.getStatus())){
				String[] params = new String[3];
				params[0] = String.valueOf(intCurrentUserId);
				params[1] = sessionId;
				params[2] = prompt.getPromptId();
				if(ValidationUtil.isValid(prompt.getSortStr())){
					query = query+ " " + prompt.getSortStr();
				}
				List<PromptTreeVb> tempPromptsList =null;
				if("CALENDAR".equalsIgnoreCase(prompt.getPromptType())){
					tempPromptsList = getJdbcTemplate().query(query, params, getPromptCalendarMapper());
				}else{
					tempPromptsList = getJdbcTemplate().query(query, params, getPromptTreeMapper());
				}
				query=query.toUpperCase();
				if(query.indexOf("ORDER BY")>0){
					query = query.substring(query.indexOf("FROM "), query.indexOf("ORDER BY")-1);
				}else{
					query = query.substring(query.indexOf("FROM "), query.length());
				}
				query = "DELETE "+query;
				int count  = getJdbcTemplate().update(query, params);
				
				return tempPromptsList;
			}else if(promptTreeVb != null && "1".equalsIgnoreCase(promptTreeVb.getStatus())){
				return new ArrayList<PromptTreeVb>(0);
			}
			throw new RuntimeCustomException(promptTreeVb.getErrorMessage());
		}catch(SQLException ex){
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException("","",ex));
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}catch(Exception ex){
			ex.printStackTrace();
			strErrorDesc = ex.getMessage().trim();
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}finally{
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
		}
	}
	class TreePromptCallableStatement implements CallableStatementCreator,CallableStatementCallback  {
		private PromptIdsVb vObject = null;
		private String currentTimeAsSessionId = null;
		private String visionId = null; 
		public TreePromptCallableStatement(PromptIdsVb vObject,String currentTimeAsSessionId,String visionId){
			this.vObject = vObject;
			this.currentTimeAsSessionId = currentTimeAsSessionId;
			this.visionId = visionId;
		}
		public CallableStatement createCallableStatement(Connection connection) throws SQLException {
			CallableStatement cs = connection.prepareCall("{call "+vObject.getPromptScript()+"}");
			cs.setString(1, visionId);
	        cs.setString(2, currentTimeAsSessionId);
        	cs.setString(3, vObject.getPromptId());
        	cs.registerOutParameter(4, java.sql.Types.VARCHAR);//filterString
	        cs.registerOutParameter(5, java.sql.Types.VARCHAR); //Status 
	        /*P_Status = -1, if there is an error; P_ErrorMsg will contain the error string
			p_Status =  0, if procedure executes successfully. P_ErrorMsg will contain nothing in this case
			p_Status =  1, Procedure has fetched NO records for the given query criteria. P_ErrorMsg will contain nothing.*/
	        cs.registerOutParameter(6, java.sql.Types.VARCHAR);  //Error Message
			return cs;
		}

		public Object doInCallableStatement(CallableStatement cs)	throws SQLException, DataAccessException {
            ResultSet rs = cs.executeQuery();
            PromptTreeVb promptTreeVb = new PromptTreeVb();
            promptTreeVb.setFilterString(cs.getString(4));
            promptTreeVb.setStatus(cs.getString(5));
            promptTreeVb.setErrorMessage(cs.getString(6));
 	        rs.close();
            return promptTreeVb;
		}
	}
	public PromptTreeVb callProcToPopulateReportData(ReportsWriterVb reportsWriterVb, List<PromptIdsVb> prompts){
			setServiceDefaults();
			strCurrentOperation = "Query";
			strErrorDesc = "";
			Connection con = null;
			CallableStatement cs =  null;
			PromptTreeVb promptTreeVb = new PromptTreeVb();
			try{
				if(!ValidationUtil.isValid(reportsWriterVb.getProcedure1())){
					strErrorDesc = "Invalid Procedure in Report Writer table for report Id["+reportsWriterVb.getReportId()+"].";
					throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
				}
				String scalingFactor = "1";
				if(ValidationUtil.isValid(reportsWriterVb.getScalingFactor()))
					scalingFactor = reportsWriterVb.getScalingFactor().replaceAll(",", "");
				String[] promptValues = new String[8];
				if(prompts != null && !prompts.isEmpty()){
					//Set space for the prompts where the no Prompts are available
					for(int i = prompts.size(); i<8;i++){
						promptValues[i] = "";
					}
					for(int i =0; i< prompts.size() ;i++){
						PromptIdsVb promptIdsVb = prompts.get(i);
						if(!ValidationUtil.isValid(promptIdsVb.getSelectedValue1()) || !ValidationUtil.isValid(promptIdsVb.getSelectedValue1().getField1()))
							promptValues[i] =  "";
						else
							promptValues[i] =  promptIdsVb.getSelectedValue1().getField1();
						if("RANGE".equalsIgnoreCase(promptIdsVb.getPromptType())){
							if(!ValidationUtil.isValid(promptIdsVb.getSelectedValue2()) || !ValidationUtil.isValid(promptIdsVb.getSelectedValue2().getField1()))
								promptValues[i] = new String(promptValues[i] + " - ");
							else
								promptValues[i] = new String(promptValues[i] + " - " +promptIdsVb.getSelectedValue2().getField1());
						}
					}
				}else{
					for(int i = 0; i<8;i++){
						promptValues[i] = "";
					}
				}
				String sessionId = "";
				//For the dash boards the session id will be set by the caller.
				if(ValidationUtil.isValid(reportsWriterVb.getSessionId())){
					sessionId = reportsWriterVb.getSessionId();
				}else{
					sessionId = String.valueOf(System.currentTimeMillis()); 
				}
				con = getConnection();
				cs = con.prepareCall("{call "+reportsWriterVb.getProcedure1()+"(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
				cs.setString(1, String.valueOf(intCurrentUserId));
		        cs.setString(2, sessionId);
		        cs.setString(3, reportsWriterVb.getReportId());//Report Id
	        	cs.setString(4, scalingFactor);//P_Scaling_Factor
	        	cs.setString(5, promptValues[0]);//P_Prompt_Value_1
	        	cs.setString(6, promptValues[1]);//P_Prompt_Value_2
	        	cs.setString(7, promptValues[2]);//P_Prompt_Value_3
	        	cs.setString(8, promptValues[3]);//P_Prompt_Value_4
	        	cs.setString(9, promptValues[4]);//P_Prompt_Value_5
	        	cs.setString(10, promptValues[5]);//P_Prompt_Value_6
	        	cs.setString(11, promptValues[6]);//P_Prompt_Value_7
	        	cs.setString(12, promptValues[7]);//P_Prompt_Value_8
		        cs.registerOutParameter(13, java.sql.Types.VARCHAR); //Status
		        /*P_Status = -1, if there is an error; P_ErrorMsg will contain the error string
  				p_Status =  0, if procedure executes successfully. P_ErrorMsg will contain nothing in this case
  				p_Status =  1, Procedure has fetched NO records for the given query criteria. P_ErrorMsg will contain nothing.*/
		        cs.registerOutParameter(14, java.sql.Types.VARCHAR); //Error Message
				ResultSet rs = cs.executeQuery();
				promptTreeVb.setSessionId(sessionId);
				promptTreeVb.setReportId(reportsWriterVb.getReportId());
	            promptTreeVb.setStatus(cs.getString(13));
	            promptTreeVb.setErrorMessage(cs.getString(14));
			    rs.close();
			    if("-1".equalsIgnoreCase(promptTreeVb.getStatus())){
			    	strErrorDesc = promptTreeVb.getErrorMessage();
					throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
			    }
			}catch(Exception ex){
				ex.printStackTrace();
				strErrorDesc = ex.getMessage().trim();
				throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
			}finally{
				JdbcUtils.closeStatement(cs);
				DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
			}
		return promptTreeVb;
	}
	public PromptTreeVb callProcToPopulateDrillDownReportData(ReportsWriterVb reportsWriterVb, List<PromptIdsVb> prompts){
		setServiceDefaults();
		strCurrentOperation = "Query";
		strErrorDesc = "";
		Connection con = null;
		CallableStatement cs =  null;
		PromptTreeVb promptTreeVb = new PromptTreeVb();
		try{
			if(!ValidationUtil.isValid(reportsWriterVb.getProcedure1())){
				strErrorDesc = "Invalid Procedure in Report Writer table for report Id["+reportsWriterVb.getReportId()+"].";
				throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
			}
			String scalingFactor = "1";
			if(ValidationUtil.isValid(reportsWriterVb.getScalingFactor()))
				scalingFactor = reportsWriterVb.getScalingFactor().replaceAll(",", "");
			String[] promptValues = new String[8];
			if(prompts != null && !prompts.isEmpty()){
				//Set space for the prompts where the no Prompts are available
				for(int i = prompts.size(); i<8;i++){
					promptValues[i] = "";
				}
				for(int i =0; i< prompts.size() ;i++){
					PromptIdsVb promptIdsVb = prompts.get(i);
					if(!ValidationUtil.isValid(promptIdsVb.getSelectedValue1()) || !ValidationUtil.isValid(promptIdsVb.getSelectedValue1().getField1()))
						promptValues[i] =  "";
					else
						promptValues[i] =  promptIdsVb.getSelectedValue1().getField1();
					if("RANGE".equalsIgnoreCase(promptIdsVb.getPromptType())){
						if(!ValidationUtil.isValid(promptIdsVb.getSelectedValue2()) || !ValidationUtil.isValid(promptIdsVb.getSelectedValue2().getField1()))
							promptValues[i] = new String(promptValues[i] + " - ");
						else
							promptValues[i] = new String(promptValues[i] + " - " +promptIdsVb.getSelectedValue2().getField1());
					}
				}
			}else{
				for(int i = 0; i<8;i++){
					promptValues[i] = "";
				}
			}
			
			String sessionId = "";
			//For the dash boards the session id will be set by the caller.
			if(ValidationUtil.isValid(reportsWriterVb.getSessionId())){
				sessionId = reportsWriterVb.getSessionId();
			}else{
				sessionId = String.valueOf(System.currentTimeMillis()); 
			}
			con = getConnection();
			cs = con.prepareCall("{call "+reportsWriterVb.getProcedure1()+"(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
			cs.setString(1, String.valueOf(intCurrentUserId));
	        cs.setString(2, sessionId);
	        cs.setString(3, reportsWriterVb.getReportId());
        	cs.setString(4, scalingFactor);//P_Scaling_Factor
        	cs.setString(5, promptValues[0]);//P_Prompt_Value_1
        	cs.setString(6, promptValues[1]);//P_Prompt_Value_2
        	cs.setString(7, promptValues[2]);//P_Prompt_Value_3
        	cs.setString(8, promptValues[3]);//P_Prompt_Value_4
        	cs.setString(9, promptValues[4]);//P_Prompt_Value_5
        	cs.setString(10, promptValues[5]);//P_Prompt_Value_5
        	cs.setString(11, promptValues[6]);//Level 2
        	cs.setString(12, promptValues[7]);//Level 3
	        cs.registerOutParameter(13, java.sql.Types.VARCHAR); //Status
	        /*P_Status = -1, if there is an error; P_ErrorMsg will contain the error string
				p_Status =  0, if procedure executes successfully. P_ErrorMsg will contain nothing in this case
				p_Status =  1, Procedure has fetched NO records for the given query criteria. P_ErrorMsg will contain nothing.*/
	        cs.registerOutParameter(14, java.sql.Types.VARCHAR); //Error Message
			ResultSet rs = cs.executeQuery();
			promptTreeVb.setSessionId(sessionId);
			promptTreeVb.setReportId(reportsWriterVb.getReportId());
            promptTreeVb.setStatus(cs.getString(13));
            promptTreeVb.setErrorMessage(cs.getString(14));
		    rs.close();
		    if("-1".equalsIgnoreCase(promptTreeVb.getStatus())){
		    	strErrorDesc = promptTreeVb.getErrorMessage();
				throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		    }
		}catch(Exception ex){
			ex.printStackTrace();
			strErrorDesc = ex.getMessage().trim();
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}finally{
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
		}
	return promptTreeVb;
}	
	public PromptTreeVb callProcToPopulateListReportData(ReportsWriterVb reportsWriterVb, List<PromptIdsVb> prompts) {
		setServiceDefaults();
		strCurrentOperation = "Query";
		strErrorDesc = "";
		Connection con = null;
		CallableStatement cs =  null;
		PromptTreeVb promptTreeVb = new PromptTreeVb();
		try{
			if(!ValidationUtil.isValid(reportsWriterVb.getProcedure1())){
				strErrorDesc = "Invalid Procedure in Report Writer table for report Id["+reportsWriterVb.getReportId()+"].";
				throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
			}
			String scalingFactor = "1";
			if(ValidationUtil.isValid(reportsWriterVb.getScalingFactor()))
				scalingFactor = reportsWriterVb.getScalingFactor().replaceAll(",", "");
			String[] promptValues = new String[8];
			if(prompts != null && !prompts.isEmpty()){
				//Set space for the prompts where the no Prompts are available
				for(int i = prompts.size(); i<8;i++){
					promptValues[i] = "";
				}
				for(int i =0; i< prompts.size() ;i++){
					PromptIdsVb promptIdsVb = prompts.get(i);
					if(!ValidationUtil.isValid(promptIdsVb.getSelectedValue1()) || !ValidationUtil.isValid(promptIdsVb.getSelectedValue1().getField1()))
						promptValues[i] =  "";
					else
						promptValues[i] =  promptIdsVb.getSelectedValue1().getField1();
					if("RANGE".equalsIgnoreCase(promptIdsVb.getPromptType())){
						if(!ValidationUtil.isValid(promptIdsVb.getSelectedValue2()) || !ValidationUtil.isValid(promptIdsVb.getSelectedValue2().getField1()))
							promptValues[i] = new String(promptValues[i] + " - ");
						else
							promptValues[i] = new String(promptValues[i] + " - " +promptIdsVb.getSelectedValue2().getField1());
					}
				}
			}else{
				for(int i = 0; i<8;i++){
					promptValues[i] = "";
				}
			}
			
			String sessionId = "";
			//For the dash boards the session id will be set by the caller.
			if(ValidationUtil.isValid(reportsWriterVb.getSessionId())){
				sessionId = reportsWriterVb.getSessionId();
			}else{
				sessionId = String.valueOf(System.currentTimeMillis());
			}
			con = getConnection();
			cs = con.prepareCall("{call "+reportsWriterVb.getProcedure1()+"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
			cs.setString(1, String.valueOf(intCurrentUserId));
	        cs.setString(2, sessionId);
	        cs.setString(3, reportsWriterVb.getReportId());
        	cs.setString(4, scalingFactor);//P_Scaling_Factor
        	cs.setString(5, promptValues[0]);//P_Prompt_Value_1
        	cs.setString(6, promptValues[1]);//P_Prompt_Value_2
        	cs.setString(7, promptValues[2]);//P_Prompt_Value_3
        	cs.setString(8, promptValues[3]);//P_Prompt_Value_4
        	cs.setString(9, promptValues[4]);//P_Prompt_Value_5
        	cs.setString(10, promptValues[5]);//P_Prompt_Value_6
        	cs.setString(11, promptValues[6]);//P_Prompt_Value_7
        	cs.setString(12, promptValues[7]);//P_Prompt_Value_8
        	cs.registerOutParameter(13, java.sql.Types.VARCHAR); //Table Name
	        cs.registerOutParameter(14, java.sql.Types.VARCHAR); //Status 
	        /*P_Status = -1, if there is an error; P_ErrorMsg will contain the error string
				p_Status =  0, if procedure executes successfully. P_ErrorMsg will contain nothing in this case
				p_Status =  1, Procedure has fetched NO records for the given query criteria. P_ErrorMsg will contain nothing.*/
	        cs.registerOutParameter(15, java.sql.Types.VARCHAR); //Error Message
			ResultSet rs = cs.executeQuery();
			promptTreeVb.setSessionId(sessionId);
			promptTreeVb.setReportId(reportsWriterVb.getReportId());
			promptTreeVb.setTableName(cs.getString(13));
            promptTreeVb.setStatus(cs.getString(14));
            promptTreeVb.setErrorMessage(cs.getString(15));
		    rs.close();
		    if("-1".equalsIgnoreCase(promptTreeVb.getStatus())){
		    	strErrorDesc = promptTreeVb.getErrorMessage();
				throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		    }
		}catch(Exception ex){
			ex.printStackTrace();
			strErrorDesc = ex.getMessage().trim();
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}finally{
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
		}
		return promptTreeVb;
	}
	private String columnNameErr="";
	public String getListReportDataAsXMLString(PromptTreeVb promptTree, final List<String> coloumNames) {
		String query = "";
		try{
			query = "SELECT COUNT(1) FROM "+promptTree.getTableName();
			promptTree.setTotalRows(getJdbcTemplate().queryForObject(query, Long.class));
			query = "SELECT * FROM "+promptTree.getTableName()+" where SORT_FIELD < 1001 ORDER BY SORT_FIELD ";
			ResultSetExtractor mapper = new ResultSetExtractor() {
				public Object extractData(ResultSet rs)  throws SQLException, DataAccessException {
					StringBuilder lResult = new StringBuilder("");
					lResult.append("<tableData>");
					while(rs.next()){
						lResult.append("<tableRow>");
						for(String columnName:coloumNames){
							String value ="";
							columnNameErr = columnName;
							if(columnName.contains(","))
							{
						        String[] parts = columnName.split(",");
								columnName=parts[0];
								columnNameErr = columnName;
								value = rs.getString(parts[0]);
							}else{
								value = rs.getString(columnName);
							}

							if(value == null) value=""; 
						    lResult.append("<").append(columnName).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnName).append(">");
						}
						try{
							String HEADERTITLE_TEXTValue = rs.getString("HEADERTITLE_TEXT");
							if(HEADERTITLE_TEXTValue == null) HEADERTITLE_TEXTValue=""; 
							lResult.append("<").append("headerText").append(">").append(StringEscapeUtils.escapeXml(HEADERTITLE_TEXTValue)).append("</").append("headerText").append(">");
						}catch(Exception e){
							//Do nothing this is a format column
						}
						try{
							String value = rs.getString("FORMAT_TYPE");
							if(value == null) value=""; 
							lResult.append("<").append("formatType").append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append("formatType").append(">");
						}catch(Exception e){
							//Do nothing this is a format column
						}
						lResult.append("</tableRow>");
					}
					lResult.append("</tableData>");
					return lResult.toString();
				}
			};
			return (String)getJdbcTemplate().query(query, mapper);
		}catch(BadSqlGrammarException ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException( "","", ex.getSQLException()));
			strErrorDesc =strErrorDesc+" - "+columnNameErr;
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = ex.getMessage(); 
			strErrorDesc =strErrorDesc+" - "+columnNameErr;
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}
	}
	public String getListReportDataForExportAsXMLStringWithMultipleHeaders(PromptTreeVb promptTree, final List<ColumnHeadersVb> columnHeaders, long min, long max) {
		String query = "";
		try{
			query = "SELECT T1.* FROM "+promptTree.getTableName()+" T1 WHERE SORT_FIELD <="+max+" AND SORT_FIELD >"+min +" ORDER BY SORT_FIELD";
			ResultSetExtractor mapper = new ResultSetExtractor() {
				public Object extractData(ResultSet rs)  throws SQLException, DataAccessException {
					StringBuffer lResult = new StringBuffer("");
					lResult.append("<tableData>");
				     while(rs.next()){
					    lResult.append("<tableRow>");
					    for(ColumnHeadersVb columnHeader:columnHeaders){
				    	    String value = "";
				    	    if(ValidationUtil.isValid(columnHeader.getDbColumnName())){
						    	if(columnHeader.getDbColumnName().contains(",")){
							        String[] parts = columnHeader.getDbColumnName().split(",");
									columnNameErr = parts[0];
									value = rs.getString(parts[0]);
								    if(value == null) value="";
								    lResult.append("<").append(columnNameErr).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnNameErr).append(">");
								}else{
									columnNameErr = columnHeader.getDbColumnName();
									value = rs.getString(columnHeader.getDbColumnName());
								    if(value == null) value="";
								    lResult.append("<").append(columnHeader.getDbColumnName()).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnHeader.getDbColumnName()).append(">");
								}
				    	    }else{
						    	if(columnHeader.getCaption().contains(",")){
							        String[] parts = columnHeader.getCaption().split(",");
									columnNameErr = parts[0];
									value = rs.getString(parts[0]);
								    if(value == null) value="";
								    lResult.append("<").append(columnNameErr).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnNameErr).append(">");
								}else{
									columnNameErr = columnHeader.getCaption();
									value = rs.getString(columnHeader.getCaption());
								    if(value == null) value="";
								    lResult.append("<").append(columnHeader.getCaption()).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnHeader.getCaption()).append(">");
								}
				    	    }
					    }
						try{
							String HEADERTITLE_TEXTValue = rs.getString("HEADERTITLE_TEXT");
							if(HEADERTITLE_TEXTValue == null) HEADERTITLE_TEXTValue=""; 
							lResult.append("<").append("headerText").append(">").append(StringEscapeUtils.escapeXml(HEADERTITLE_TEXTValue)).append("</").append("headerText").append(">");
						}catch(Exception e){
							//Do nothing this is a format column
						}
						try{
							String value = rs.getString("FORMAT_TYPE");
							if(value == null) value=""; 
							lResult.append("<").append("formatType").append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append("formatType").append(">");
						}catch(Exception e){
							//Do nothing this is a format column
						}
					    lResult.append("</tableRow>");
				     }
				     lResult.append("</tableData>");
					return lResult.toString();
				}
			};
			return (String)getJdbcTemplate().query(query, mapper);
		}catch(BadSqlGrammarException ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException( "","", ex.getSQLException())); 
			strErrorDesc =strErrorDesc+" - "+columnNameErr;
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = ex.getMessage(); 
			strErrorDesc =strErrorDesc+" - "+columnNameErr;
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}
	}
	public String getListReportDataForExportAsXMLString(PromptTreeVb promptTree, final List<ColumnHeadersVb> columnHeaders, long min, long max) {
		String query = "";
		try{
			query = "SELECT T1.* FROM "+promptTree.getTableName()+" T1 WHERE SORT_FIELD <="+max+" AND SORT_FIELD >"+min +" ORDER BY SORT_FIELD";
			ResultSetExtractor mapper = new ResultSetExtractor() {
				public Object extractData(ResultSet rs)  throws SQLException, DataAccessException {
					StringBuffer lResult = new StringBuffer("");
					lResult.append("<tableData>");
				     while(rs.next()){
					    lResult.append("<tableRow>");
					    for(ColumnHeadersVb columnHeader:columnHeaders){
				    	    String value = "";
				    	    if(ValidationUtil.isValid(columnHeader.getCaption())){
						    	if(columnHeader.getCaption().contains(",")){
							        String[] parts = columnHeader.getCaption().split(",");
									columnNameErr = parts[0];
									value = rs.getString(parts[0]);
								    if(value == null) value="";
								    lResult.append("<").append(columnNameErr).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnNameErr).append(">");
								}else{
									columnNameErr = columnHeader.getCaption();
									value = rs.getString(columnHeader.getCaption());
								    if(value == null) value="";
								    lResult.append("<").append(columnHeader.getCaption()).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnHeader.getCaption()).append(">");
								}
				    	    }else{
						    	if(columnHeader.getCaption().contains(",")){
							        String[] parts = columnHeader.getCaption().split(",");
									columnNameErr = parts[0];
									value = rs.getString(parts[0]);
								    if(value == null) value="";
								    lResult.append("<").append(columnNameErr).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnNameErr).append(">");
								}else{
									columnNameErr = columnHeader.getCaption();
									value = rs.getString(columnHeader.getCaption());
								    if(value == null) value="";
								    lResult.append("<").append(columnHeader.getCaption()).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnHeader.getCaption()).append(">");
								}
				    	    }
						  }
						try{
							String HEADERTITLE_TEXTValue = rs.getString("HEADERTITLE_TEXT");
							if(HEADERTITLE_TEXTValue == null) HEADERTITLE_TEXTValue=""; 
							lResult.append("<").append("headerText").append(">").append(StringEscapeUtils.escapeXml(HEADERTITLE_TEXTValue)).append("</").append("headerText").append(">");
						}catch(Exception e){
							//Do nothing this is a format column
						}
						try{
							String value = rs.getString("FORMAT_TYPE");
							if(value == null) value=""; 
							lResult.append("<").append("formatType").append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append("formatType").append(">");
						}catch(Exception e){
							//Do nothing this is a format column
						}
					    lResult.append("</tableRow>");
				     }
				     lResult.append("</tableData>");
					return lResult.toString();
				}
			};
			return (String)getJdbcTemplate().query(query, mapper);
		}catch(BadSqlGrammarException ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException( "","", ex.getSQLException())); 
			strErrorDesc =strErrorDesc+" - "+columnNameErr;
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = ex.getMessage(); 
			strErrorDesc =strErrorDesc+" - "+columnNameErr;
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}
	}	
	public List<Object> getCBKReportDataAsXMLString(PromptTreeVb promptTree, long min, long max) {
		String query = "";
		try{
			query = "SELECT * FROM "+promptTree.getTableName()+"  WHERE SORT_FIELD <="+max+" AND SORT_FIELD >"+min +" ORDER BY SORT_FIELD";
			ResultSetExtractor mapper = new ResultSetExtractor() {
				public Object extractData(ResultSet rs)  throws SQLException, DataAccessException {
					StringBuffer lResult = new StringBuffer("");
					lResult.append("<tableData>");
					ResultSetMetaData metadata = rs.getMetaData();
					List<ColumnHeadersVb> columnHeaders = new ArrayList<ColumnHeadersVb>(metadata.getColumnCount());
					for(int i=1;i<= metadata.getColumnCount(); i++){
						ColumnHeadersVb columnHeader = new ColumnHeadersVb(); 
						columnHeader.setCaption(metadata.getColumnLabel(i));
						switch(metadata.getColumnType(i)){
							case  -7: case  -6: case  5: case  -5: case  4: case  3: case  2: 
							case  6: case  7: case  8: columnHeader.setColType("N"); break;
							default: columnHeader.setColType("T");
						}
						columnHeaders.add(columnHeader);
					}
					while(rs.next()){
						lResult.append("<tableRow>");
						for(ColumnHeadersVb columnHeader:columnHeaders){
							String value = rs.getString(columnHeader.getCaption());
							if(value == null) value="";
							lResult.append("<").append(columnHeader.getCaption()).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnHeader.getCaption()).append(">");
						}
						lResult.append("</tableRow>");
					}
					lResult.append("</tableData>");
					List<Object> result = new ArrayList<Object>(2);
					result.add(columnHeaders);
					result.add(lResult.toString());
					return result;
				}
			};
			return (List<Object>)getJdbcTemplate().query(query, mapper);
		}catch(BadSqlGrammarException ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException( "","", ex.getSQLException())); 
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = ex.getMessage(); 
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}
	}
	public List<RCReportFieldsVb> getCBKReportData(ReportsWriterVb reportWriterVb, List<PromptIdsVb> prompts,PromptTreeVb promptTree, long min, long max) {
		String query = "";
		try{
			query = "SELECT TAB_ID, ROW_ID, COL_ID, CELL_DATA, COL_TYPE, CREATE_NEW,SHEET_NAME,FORMAT_TYPE,FILE_NAME FROM TEMPLATES_STG WHERE SORT_FIELD <=? AND SORT_FIELD >? " +
					"AND SESSION_ID=? ORDER BY TAB_ID,ROW_ID,COL_ID";
			ResultSetExtractor mapper = new ResultSetExtractor() {
				public Object extractData(ResultSet rs)  throws SQLException, DataAccessException {
					List<RCReportFieldsVb> result = new ArrayList<RCReportFieldsVb>();
					while(rs.next()){
						RCReportFieldsVb fieldsVb = new RCReportFieldsVb();
						fieldsVb.setTabelId(rs.getString("TAB_ID"));
						fieldsVb.setRowId(rs.getString("ROW_ID"));
						fieldsVb.setColId(rs.getString("COL_ID"));
						fieldsVb.setValue1(rs.getString("CELL_DATA"));
						fieldsVb.setColType(rs.getString("COL_TYPE"));
						fieldsVb.setSheetName(rs.getString("SHEET_NAME"));
						fieldsVb.setRowStyle(rs.getString("FORMAT_TYPE"));
						fieldsVb.setExcelFileName(rs.getString("FILE_NAME"));
						//M - Mandatory create new Line. Y -- Create new Line only if does not exists N-- Do not create new Line.
						fieldsVb.setCreateNew("Y".equalsIgnoreCase(rs.getString("CREATE_NEW")) || "M".equalsIgnoreCase(rs.getString("CREATE_NEW")) ? true:false);
						fieldsVb.setCreateNewRow(rs.getString("CREATE_NEW"));
						result.add(fieldsVb);
					}
					return result;
				}
			};
			Object[] promptValues = new Object[3];
			promptValues[0] = max;
			promptValues[1] = min;
			promptValues[2] = promptTree.getSessionId();
			int retVal =InsetAuditTrialDataForReports(reportWriterVb, prompts);
			if(retVal!=Constants.SUCCESSFUL_OPERATION){
				logger.error("Error  inserting into rs_Schedule Audit");
			}
			
			return (List<RCReportFieldsVb>)getJdbcTemplate().query(query, promptValues, mapper);
		}catch(BadSqlGrammarException ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException( "","", ex.getSQLException())); 
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = ex.getMessage(); 
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}
	}
	public void deleteListReportTable(PromptTreeVb promptTree) {
		setServiceDefaults();
		String query = new String("DROP TABLE "+promptTree.getTableName());
		try
		{
			getJdbcTemplate().update(query);
		}
		catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
		}
	}	
	private RowMapper getPromptTreeMapper() {
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
	private RowMapper getPromptTreeCascadeMapper() {
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				PromptTreeVb promptTreeVb = new PromptTreeVb();
				promptTreeVb.setField1(rs.getString("FIELD_1"));
				promptTreeVb.setField2(rs.getString("FIELD_2"));
				promptTreeVb.setField3(rs.getString("FIELD_3"));
				promptTreeVb.setField4(rs.getString("FIELD_4"));
				promptTreeVb.setPromptId(rs.getString("PROMPT_ID"));
				promptTreeVb.setFilterString(rs.getString("PROMPT_LOGIC"));
				return promptTreeVb;
			}
		};
		return mapper;
	}
	private RowMapper getPromptIdsMapper() {
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				PromptIdsVb promptIdsVb = new PromptIdsVb();
				promptIdsVb.setPromptId(rs.getString("PROMPT_ID"));
				promptIdsVb.setPromptDesc(rs.getString("PROMPT_DESC"));
				promptIdsVb.setPromptString(rs.getString("PROMPT_STRING"));
				promptIdsVb.setPromptScript(rs.getString("PROMPT_SCRIPT"));
				promptIdsVb.setPromptType(rs.getString("PROMPT_TYPE"));
				promptIdsVb.setPromptTypeAt(rs.getInt("PROMPT_TYPE_AT"));
				promptIdsVb.setDependentPromptId(rs.getString("DEPENDENT_PROMPT_ID"));
				promptIdsVb.setSortStr(rs.getString("SORT_STRING"));
				promptIdsVb.setPromptStatusNt(rs.getInt("PROMPT_STATUS_NT"));
				promptIdsVb.setPromptStatus(rs.getInt("PROMPT_STATUS"));
				promptIdsVb.setRecordIndicatorNt(rs.getInt("RECORD_INDICATOR_NT"));
				promptIdsVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
				promptIdsVb.setMaker(rs.getLong("MAKER"));
				promptIdsVb.setVerifier(rs.getLong("VERIFIER"));
				promptIdsVb.setInternalStatus(rs.getInt("INTERNAL_STATUS"));
				promptIdsVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				promptIdsVb.setDateCreation(rs.getString("DATE_CREATION"));
				promptIdsVb.setScheduleScript(rs.getString("SCHEDULE_SCRIPT"));
				promptIdsVb.setScheduleSortOrder(rs.getString("SCHEDULE_SCRIPT_ORDER"));
				//promptIdsVb.setPromptLogic(rs.getString("PROMPT_LOGIC"));
				//promptIdsVb.setPromptLogicAt(rs.getInt("PROMPT_LOGIC_AT"));
				return promptIdsVb;
			}
		};
		return mapper;
	}
	private RowMapper getColumnHeadersMapper(){
		RowMapper mapper = new RowMapper(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				ColumnHeadersVb columnHeadersVb = new ColumnHeadersVb();
				columnHeadersVb.setReportId(rs.getString("REPORT_ID"));
				columnHeadersVb.setSessionId(rs.getString("SESSION_ID"));
				columnHeadersVb.setLabelRowNum(rs.getInt("LABEL_ROW_NUM"));
				columnHeadersVb.setLabelColNum(rs.getInt("LABEL_COL_NUM"));
				columnHeadersVb.setCaption(rs.getString("CAPTION"));
				columnHeadersVb.setColumnWidth(rs.getLong("COLUMN_WIDTH"));
				columnHeadersVb.setColType(rs.getString("COL_TYPE"));
				columnHeadersVb.setRowSpanNum(rs.getInt("ROW_SPAN"));
				columnHeadersVb.setRowspan(rs.getInt("ROW_SPAN"));
				columnHeadersVb.setNumericColumnNo(rs.getInt("NUMERIC_COLUMN_NO"));
				columnHeadersVb.setColSpanNum(rs.getInt("COL_SPAN"));
				columnHeadersVb.setColspan(rs.getInt("COL_SPAN"));
				columnHeadersVb.setDbColumnName(rs.getString("DB_COLUMN"));
				return columnHeadersVb;
			}
		};
		return mapper;
	}
	private RowMapper getReportsStgMapper(){
		RowMapper mapper = new RowMapper(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				ReportStgVb reportStgVb = new ReportStgVb();
				reportStgVb.setReportId(rs.getString("REPORT_ID"));
				reportStgVb.setSessionId(rs.getString("SESSION_ID"));
				reportStgVb.setCaptionColumn1(rs.getString("CAPTION_COLUMN_1"));
				reportStgVb.setCaptionColumn2(rs.getString("CAPTION_COLUMN_2"));
				reportStgVb.setCaptionColumn3(rs.getString("CAPTION_COLUMN_3"));
				reportStgVb.setCaptionColumn4(rs.getString("CAPTION_COLUMN_4"));
				reportStgVb.setCaptionColumn5(rs.getString("CAPTION_COLUMN_5"));
				reportStgVb.setDataColumn1(rs.getString("DATA_COLUMN_1"));
				reportStgVb.setDataColumn2(rs.getString("DATA_COLUMN_2"));
				reportStgVb.setDataColumn3(rs.getString("DATA_COLUMN_3"));
				reportStgVb.setDataColumn4(rs.getString("DATA_COLUMN_4"));
				reportStgVb.setDataColumn5(rs.getString("DATA_COLUMN_5"));
				reportStgVb.setDataColumn6(rs.getString("DATA_COLUMN_6"));
				reportStgVb.setDataColumn7(rs.getString("DATA_COLUMN_7"));
				reportStgVb.setDataColumn8(rs.getString("DATA_COLUMN_8"));
				reportStgVb.setDataColumn9(rs.getString("DATA_COLUMN_9"));
				reportStgVb.setDataColumn10(rs.getString("DATA_COLUMN_10"));
				reportStgVb.setDataColumn11(rs.getString("DATA_COLUMN_11"));
				reportStgVb.setDataColumn12(rs.getString("DATA_COLUMN_12"));
				reportStgVb.setDataColumn13(rs.getString("DATA_COLUMN_13"));
				reportStgVb.setDataColumn14(rs.getString("DATA_COLUMN_14"));
				reportStgVb.setDataColumn15(rs.getString("DATA_COLUMN_15"));
				reportStgVb.setDataColumn16(rs.getString("DATA_COLUMN_16"));
				reportStgVb.setDataColumn17(rs.getString("DATA_COLUMN_17"));
				reportStgVb.setDataColumn18(rs.getString("DATA_COLUMN_18"));
				reportStgVb.setDataColumn19(rs.getString("DATA_COLUMN_19"));
				reportStgVb.setDataColumn20(rs.getString("DATA_COLUMN_20"));
				reportStgVb.setDataColumn21(rs.getString("DATA_COLUMN_21"));
				reportStgVb.setDataColumn22(rs.getString("DATA_COLUMN_22"));
				reportStgVb.setDataColumn23(rs.getString("DATA_COLUMN_23"));
				reportStgVb.setDataColumn24(rs.getString("DATA_COLUMN_24"));
				reportStgVb.setDataColumn25(rs.getString("DATA_COLUMN_25"));
				reportStgVb.setDataColumn26(rs.getString("DATA_COLUMN_26"));
				reportStgVb.setDataColumn27(rs.getString("DATA_COLUMN_27"));
				reportStgVb.setDataColumn28(rs.getString("DATA_COLUMN_28"));
				reportStgVb.setDataColumn29(rs.getString("DATA_COLUMN_29"));
				reportStgVb.setDataColumn30(rs.getString("DATA_COLUMN_30"));
				reportStgVb.setDataColumn31(rs.getString("DATA_COLUMN_31"));
				reportStgVb.setDataColumn32(rs.getString("DATA_COLUMN_32"));
				reportStgVb.setDataColumn33(rs.getString("DATA_COLUMN_33"));
				reportStgVb.setDataColumn34(rs.getString("DATA_COLUMN_34"));
				reportStgVb.setDataColumn35(rs.getString("DATA_COLUMN_35"));
				reportStgVb.setDataColumn36(rs.getString("DATA_COLUMN_36"));
				reportStgVb.setDataColumn37(rs.getString("DATA_COLUMN_37"));
				reportStgVb.setDataColumn38(rs.getString("DATA_COLUMN_38"));
				reportStgVb.setDataColumn39(rs.getString("DATA_COLUMN_39"));
				reportStgVb.setDataColumn40(rs.getString("DATA_COLUMN_40"));
				reportStgVb.setFormatType(rs.getString("FORMAT_TYPE"));
				if(rs.getString("DD_KEY_ID")!=null)
					reportStgVb.setDdKeyFieldId(rs.getString("DD_KEY_ID"));
				else
					reportStgVb.setDdKeyFieldId("");
				if(rs.getString("DD_KEY_VALUE")!=null)
					reportStgVb.setDdKeyFieldValue(rs.getString("DD_KEY_VALUE"));
				else
					reportStgVb.setDdKeyFieldValue("");
				if(rs.getString("DD_KEY_LABEL")!=null)
					reportStgVb.setDdKeyFieldLabel(rs.getString("DD_KEY_LABEL"));
				else
					reportStgVb.setDdKeyFieldLabel("");

				return reportStgVb;
			}
		};
		return mapper;
	}
	@Override
	protected RowMapper getMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				ReportsWriterVb reporterWriterVb = new ReportsWriterVb();
				reporterWriterVb.setPromptPosition1(rs.getString("PROMPT_POSITION_1"));
				reporterWriterVb.setPromptPosition2(rs.getString("PROMPT_POSITION_2"));
				reporterWriterVb.setPromptPosition3(rs.getString("PROMPT_POSITION_3"));
				reporterWriterVb.setPromptPosition4(rs.getString("PROMPT_POSITION_4"));
				reporterWriterVb.setPromptPosition5(rs.getString("PROMPT_POSITION_5"));
				reporterWriterVb.setPromptPosition6(rs.getString("PROMPT_POSITION_6"));
				reporterWriterVb.setReportId(rs.getString("REPORT_ID"));
				reporterWriterVb.setReportDescription(rs.getString("REPORT_DESCRIPTION"));
				reporterWriterVb.setReportTitle(rs.getString("REPORT_TITLE"));
				reporterWriterVb.setReportCategoryAt(rs.getString("REPORT_CATEGORY_AT"));
				reporterWriterVb.setReportCategory(rs.getString("REPORT_CATEGORY"));
				reporterWriterVb.setSubCategoryAt(rs.getString("SUB_CATEGORY_AT"));
				reporterWriterVb.setSubCategory(rs.getString("SUB_CATEGORY"));
				reporterWriterVb.setLabelRowCount(rs.getInt("LABEL_ROW_COUNT"));
				reporterWriterVb.setLabelColCount(rs.getInt("LABEL_COL_COUNT"));
				reporterWriterVb.setProcedure1(rs.getString("REP_PROCEDURE"));
				reporterWriterVb.setFilterString(rs.getString("FILTER_STRING"));
				reporterWriterVb.setPromptId1(rs.getString("PROMPT_ID_1"));
				reporterWriterVb.setPromptId2(rs.getString("PROMPT_ID_2"));
				reporterWriterVb.setPromptId3(rs.getString("PROMPT_ID_3"));
				reporterWriterVb.setPromptId4(rs.getString("PROMPT_ID_4"));
				reporterWriterVb.setPromptId5(rs.getString("PROMPT_ID_5"));
				reporterWriterVb.setPromptId6(rs.getString("PROMPT_ID_6"));
/*				if(rs.getString("PROMPT_MACRO_MAPPINGS_1") != null)
					reporterWriterVb.setPromptMacroMappings1(rs.getString("PROMPT_MACRO_MAPPINGS_1"));
				else
					reporterWriterVb.setPromptMacroMappings1("");
				if(rs.getString("PROMPT_MACRO_MAPPINGS_2") != null)
					reporterWriterVb.setPromptMacroMappings2(rs.getString("PROMPT_MACRO_MAPPINGS_2"));
				else
					reporterWriterVb.setPromptMacroMappings2("");
				if(rs.getString("PROMPT_MACRO_MAPPINGS_3") != null)
					reporterWriterVb.setPromptMacroMappings3(rs.getString("PROMPT_MACRO_MAPPINGS_3"));
				else
					reporterWriterVb.setPromptMacroMappings3("");
				if(rs.getString("PROMPT_MACRO_MAPPINGS_4") != null)
					reporterWriterVb.setPromptMacroMappings4(rs.getString("PROMPT_MACRO_MAPPINGS_4"));
				else
					reporterWriterVb.setPromptMacroMappings4("");
				if(rs.getString("PROMPT_MACRO_MAPPINGS_5") != null)
					reporterWriterVb.setPromptMacroMappings5(rs.getString("PROMPT_MACRO_MAPPINGS_5"));
				else
					reporterWriterVb.setPromptMacroMappings5("");
				if(rs.getString("PROMPT_MACRO_MAPPINGS_6") != null)
					reporterWriterVb.setPromptMacroMappings6(rs.getString("PROMPT_MACRO_MAPPINGS_6"));
				else
					reporterWriterVb.setPromptMacroMappings6("");*/
				reporterWriterVb.setOrientation(rs.getString("ORIENTATION"));
				reporterWriterVb.setRwStatusNt(rs.getInt("RS_STATUS_NT"));
				reporterWriterVb.setRwStatus(rs.getInt("RS_STATUS"));
				reporterWriterVb.setMaker(rs.getLong("MAKER"));
				reporterWriterVb.setVerifier(rs.getLong("VERIFIER"));
				reporterWriterVb.setInternalStatus(rs.getInt("INTERNAL_STATUS"));
				reporterWriterVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				reporterWriterVb.setDateCreation(rs.getString("DATE_CREATION"));
				reporterWriterVb.setRecordIndicatorNt(rs.getInt("RECORD_INDICATOR_NT"));
				reporterWriterVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
				reporterWriterVb.setReportType(rs.getString("REPORT_TYPE"));
				reporterWriterVb.setDisplayType(rs.getString("DISPLAY_TYPE"));
				reporterWriterVb.setDrillDownId(rs.getString("DD_REPORT_ID"));
				if("Y".equalsIgnoreCase(rs.getString("DD_FLAG")))
					reporterWriterVb.setDrillFlag(true);
				reporterWriterVb.setScalingFactor(rs.getString("SCALLING_FACTOR"));
				reporterWriterVb.setTemplateName(rs.getString("TEMPLATE_NAME"));
				reporterWriterVb.setBurstFlag(rs.getString("BURST_FLAG"));
				if(rs.getString("PROMPT_LABEL_1")!=null){
					reporterWriterVb.setPromptLabel1(rs.getString("PROMPT_LABEL_1"));
				}else{
					reporterWriterVb.setPromptLabel1("");
				}
				if(rs.getString("PROMPT_LABEL_2")!=null){
					reporterWriterVb.setPromptLabel2(rs.getString("PROMPT_LABEL_2"));
				}else{
					reporterWriterVb.setPromptLabel2("");
				}
				if(rs.getString("PROMPT_LABEL_3")!=null){
					reporterWriterVb.setPromptLabel3(rs.getString("PROMPT_LABEL_3"));
				}else{
					reporterWriterVb.setPromptLabel3("");
				}
				if(rs.getString("PROMPT_LABEL_4")!=null){
					reporterWriterVb.setPromptLabel4(rs.getString("PROMPT_LABEL_4"));
				}else{
					reporterWriterVb.setPromptLabel4("");
				}
				if(rs.getString("PROMPT_LABEL_5")!=null){
					reporterWriterVb.setPromptLabel5(rs.getString("PROMPT_LABEL_5"));
				}else{
					reporterWriterVb.setPromptLabel5("");
				}
				if(rs.getString("PROMPT_LABEL_6")!=null){
					reporterWriterVb.setPromptLabel6(rs.getString("PROMPT_LABEL_6"));
				}else{
					reporterWriterVb.setPromptLabel6("");
				}				
				return reporterWriterVb;
			}
		};
		return mapper;
	}
	@Transactional(rollbackForClassName={"com.vision.exception.RuntimeCustomException"})
	public void doDeletePromptsStgData(PromptTreeVb vObject) {
		try {
			deleteReportsStgData(vObject);
			deleteColumnHeadersData(vObject);
		}catch(Exception ex){
			//Do nothing
		}
	}
	public void callProcToCleanUpTables(ReportsWriterVb vObj) {
		Connection con = null;
		CallableStatement cs =  null;
		try{
			con = getConnection();
			cs = con.prepareCall("{call PR_RS_CLEANUP(?, ?, ?, ?, ?)}");
	        cs.setString(1, String.valueOf(intCurrentUserId));//Report Id
	        cs.setString(2, vObj.getSessionId());//Group Report Id
	        cs.setString(3, vObj.getReportId());//Group Report Id
	        cs.registerOutParameter(4, java.sql.Types.VARCHAR);//Chart type list
	        cs.registerOutParameter(5, java.sql.Types.VARCHAR); //Status 
	    	ResultSet rs = cs.executeQuery();
		    rs.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
		}
	}
	public List<PromptTreeVb> getScheduleTreePromptData(PromptIdsVb prompt){
		setServiceDefaults();
		String query = "SELECT FIELD_1, FIELD_2, FIELD_3, FIELD_4, PROMPT_ID FROM " +
				"PROMPTS_STG WHERE VISION_ID = ? AND SESSION_ID= ? AND PROMPT_ID = ? ";
		String sessionId = String.valueOf(System.currentTimeMillis());
		CallableStatementCreator creator = new TreeSchedulePromptCallableStatement(prompt, sessionId, String.valueOf(intCurrentUserId));
		CallableStatementCallback callBack = (CallableStatementCallback)creator;
		PromptTreeVb result = (PromptTreeVb) getJdbcTemplate().execute(creator, callBack);
		if(result != null && "0".equalsIgnoreCase(result.getStatus())){
			String[] params = new String[3];
			params[0] = String.valueOf(intCurrentUserId);
			params[1] = sessionId;
			params[2] = prompt.getPromptId();
			prompt.setFilterStr(result.getFilterString());
			if(ValidationUtil.isValid(prompt.getScheduleSortOrder())){ 
				query = query+ " " + prompt.getScheduleSortOrder();
			}
			return getJdbcTemplate().query(query, params, getPromptTreeMapper());
		}else if(result != null && "1".equalsIgnoreCase(result.getStatus())){
			return new ArrayList<PromptTreeVb>(0);
		}
		throw new RuntimeCustomException(result.getErrorMessage());
	}
	public List<PromptTreeVb> getScheduleComboPromptData(PromptIdsVb prompt, PromptTreeVb promptInputVb){
		setServiceDefaults();
		String query = "SELECT FIELD_1, FIELD_2, FIELD_3, FIELD_4, PROMPT_ID FROM PROMPTS_STG WHERE VISION_ID = ? AND SESSION_ID= ? AND PROMPT_ID = ?";
		Connection con = null;
		strCurrentOperation = "Prompts";
		CallableStatement cs =  null;
		try{
			if(!ValidationUtil.isValid(prompt.getScheduleScript())){
				strErrorDesc = "Invalid Procedure";
				throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
			}
			String sessionId = String.valueOf(System.currentTimeMillis());
			con = getConnection();
			cs = con.prepareCall("{call "+prompt.getScheduleScript()+"}");
			int parameterCount = prompt.getScheduleScript().split("\\?").length -1;
			if(parameterCount > 6){
				cs.setString(1, String.valueOf(intCurrentUserId));
		        cs.setString(2, sessionId);
	        	cs.setString(3, prompt.getPromptId());
	        	if(promptInputVb == null){
		        	cs.setString(4, "");//Filter Condition
	        	}else{
		        	cs.setString(4, promptInputVb.getField1());//Filter Condition
	        	}
	        	cs.registerOutParameter(5, java.sql.Types.VARCHAR);//filterString
		        cs.registerOutParameter(6, java.sql.Types.VARCHAR); //Status 
		        cs.registerOutParameter(7, java.sql.Types.VARCHAR); //Category (T-Trigger error,V-Validation Error)
			}else if(parameterCount == 6){
				cs.setString(1, String.valueOf(intCurrentUserId));
		        cs.setString(2, sessionId);
	        	cs.setString(3, prompt.getPromptId());
	        	if(promptInputVb == null){
		        	cs.setString(4, "");//Filter Condition
	        	}else{
		        	cs.setString(4, promptInputVb.getField1());//Filter Condition
	        	}
		        cs.registerOutParameter(5, java.sql.Types.VARCHAR); //Status 
		        cs.registerOutParameter(6, java.sql.Types.VARCHAR);  //Error Message
			}else{
				cs.setString(1, String.valueOf(intCurrentUserId));
		        cs.setString(2, sessionId);
	        	cs.setString(3, prompt.getPromptId());
		        cs.registerOutParameter(4, java.sql.Types.VARCHAR); //Status 
			        cs.registerOutParameter(5, java.sql.Types.VARCHAR); //Error Message
			}
	        /*P_Status = -1, if there is an error; P_ErrorMsg will contain the error string
	  				p_Status =  0, if procedure executes successfully. P_ErrorMsg will contain nothing in this case
	  				p_Status =  1, Procedure has fetched NO records for the given query criteria. P_ErrorMsg will contain nothing.*/
		
			ResultSet rs = cs.executeQuery();
			PromptTreeVb promptTreeVb = new PromptTreeVb();
	        if(parameterCount > 6){
	            promptTreeVb.setFilterString(cs.getString(5));
	            prompt.setFilterStr(cs.getString(5));
	            promptTreeVb.setStatus(cs.getString(6));
	            promptTreeVb.setErrorMessage(cs.getString(7));
	        }else if(parameterCount ==6){
	            promptTreeVb.setStatus(cs.getString(5));
	            promptTreeVb.setErrorMessage(cs.getString(6));
	        }else{
	        	promptTreeVb.setStatus(cs.getString(4));
	            promptTreeVb.setErrorMessage(cs.getString(5));
	        }
		    rs.close();
			if(promptTreeVb != null && "0".equalsIgnoreCase(promptTreeVb.getStatus())){
				String[] params = new String[3];
				params[0] = String.valueOf(intCurrentUserId);
				params[1] = sessionId;
				params[2] = prompt.getPromptId();
				if(ValidationUtil.isValid(prompt.getScheduleSortOrder())){ 
					query = query+ " " + prompt.getScheduleSortOrder();
				}
				return getJdbcTemplate().query(query, params, getPromptTreeMapper());
			}else if(promptTreeVb != null && "1".equalsIgnoreCase(promptTreeVb.getStatus())){
				return new ArrayList<PromptTreeVb>(0);
			}
			throw new RuntimeCustomException(promptTreeVb.getErrorMessage());
		}catch(SQLException ex){
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException("","",ex));
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}catch(Exception ex){
			ex.printStackTrace();
			strErrorDesc = ex.getMessage().trim();
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}finally{
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
		}
	}
	class TreeSchedulePromptCallableStatement implements CallableStatementCreator,CallableStatementCallback  {
		private PromptIdsVb vObject = null;
		private String currentTimeAsSessionId = null;
		private String visionId = null; 
		public TreeSchedulePromptCallableStatement(PromptIdsVb vObject,String currentTimeAsSessionId,String visionId){
			this.vObject = vObject;
			this.currentTimeAsSessionId = currentTimeAsSessionId;
			this.visionId = visionId;
		}
		public CallableStatement createCallableStatement(Connection connection) throws SQLException {
			CallableStatement cs = connection.prepareCall("{call "+vObject.getScheduleScript()+"}");
			cs.setString(1, visionId);
	        cs.setString(2, currentTimeAsSessionId);
        	cs.setString(3, vObject.getPromptId());
        	cs.registerOutParameter(4, java.sql.Types.VARCHAR);//filterString
	        cs.registerOutParameter(5, java.sql.Types.VARCHAR); //Status 
	        /*P_Status = -1, if there is an error; P_ErrorMsg will contain the error string
			p_Status =  0, if procedure executes successfully. P_ErrorMsg will contain nothing in this case
			p_Status =  1, Procedure has fetched NO records for the given query criteria. P_ErrorMsg will contain nothing.*/
	        cs.registerOutParameter(6, java.sql.Types.VARCHAR);  //Error Message
			return cs;
		}

		public Object doInCallableStatement(CallableStatement cs)	throws SQLException, DataAccessException {
            ResultSet rs = cs.executeQuery();
            PromptTreeVb promptTreeVb = new PromptTreeVb();
            promptTreeVb.setFilterString(cs.getString(4));
            promptTreeVb.setStatus(cs.getString(5));
            promptTreeVb.setErrorMessage(cs.getString(6));
 	        rs.close();
            return promptTreeVb;
		}
	}
	//Added by Deepak to get the Customer Name for Customer Profitability Report INM
	public String getCustomerName(CustomersDlyVb vObject) throws DataAccessException {
		if(!ValidationUtil.isValid(vObject)){
			return null;
		}
		String sql = "SELECT CUSTOMER_NAME FROM CUSTOMERS_DLY WHERE CUSTOMER_ID = ? ";
		Object[] lParams = new Object[1];
		lParams[0] = vObject.getCustomerId();
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				CustomersDlyVb customersDlyVb = new CustomersDlyVb();
				customersDlyVb.setCustomerName(rs.getString("CUSTOMER_NAME"));
				return customersDlyVb;
			}
		};
		List<CustomersDlyVb> selfAppList = getJdbcTemplate().query(sql, lParams, mapper);
		if(selfAppList != null && !selfAppList.isEmpty()){
			return selfAppList.get(0).getCustomerName();
		}
		return null;
	}
		
	public int insetAuditTrailReportData(ReportsWriterVb vObject){
		String query = "Insert Into AUDIT_TRAIL_REPORTS (REFERENCE_NO, REPORT_ID, MAKER, RUN_DATE, REPORT_TYPE, RUN_TYPE, FORMAT_TYPE, SCHEDULE_TYPE,"+
				" EMAIL_TO, EMAIL_CC, SCALLING_FACTOR, PROMPT_VALUE_1, PROMPT_VALUE_2, PROMPT_VALUE_3, PROMPT_VALUE_4, PROMPT_VALUE_5, PROMPT_VALUE_6, "
				+ "PROMPT_VALUE_1_DESC, PROMPT_VALUE_2_DESC, PROMPT_VALUE_3_DESC, PROMPT_VALUE_4_DESC, PROMPT_VALUE_5_DESC, PROMPT_VALUE_6_DESC, CATALOG_QUERY)"+
				" Values (?, ?, ?, Sysdate, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "; 
		Object args[] = {vObject.getRefrenceId(), vObject.getReportId(), vObject.getMaker() , vObject.getReportType(), vObject.getRunType(),
				vObject.getFormatType() , vObject.getScheduleType(), vObject.getEmailTo(), vObject.getEmailCc(), vObject.getScallingFactor(), vObject.getPromptValue1(), 
				vObject.getPromptValue2(),  vObject.getPromptValue3(), vObject.getPromptValue4(), vObject.getPromptValue5(), vObject.getPromptValue6(), 
				vObject.getPromptValue1Desc(), vObject.getPromptValue2Desc(), vObject.getPromptValue3Desc(), vObject.getPromptValue4Desc(), vObject.getPromptValue5Desc(), 
				vObject.getPromptValue6Desc(),  vObject.getCatalogQuery()};
		return getJdbcTemplate().update(query,args);
	}
	
	public void InsetAuditTrialData(ReportsWriterVb reportsWriterVb, List<PromptIdsVb> prompts,String type){
		String[] promptValues = new String[8];
		String[] promptValuesDesc = new String[8];
		if(prompts != null && !prompts.isEmpty()){
			//Set space for the prompts where the no Prompts are available
			for(int i = prompts.size(); i<8;i++){
				promptValues[i] = "";
			}
			for(int i = prompts.size(); i<8;i++){
				promptValuesDesc[i] = "";
			}
			for(int i =0; i< prompts.size() ;i++){
				PromptIdsVb promptIdsVb = prompts.get(i);
				if(!ValidationUtil.isValid(promptIdsVb.getSelectedValue1()) || !ValidationUtil.isValid(promptIdsVb.getSelectedValue1().getField1()))
					promptValues[i] =  "";
				else
					promptValues[i] =  promptIdsVb.getSelectedValue1().getField1();
				if("RANGE".equalsIgnoreCase(promptIdsVb.getPromptType())){
					if(!ValidationUtil.isValid(promptIdsVb.getSelectedValue2()) || !ValidationUtil.isValid(promptIdsVb.getSelectedValue2().getField1()))
						promptValues[i] = new String(promptValues[i] + " - ");
					else
						promptValues[i] = new String(promptValues[i] + " - " +promptIdsVb.getSelectedValue2().getField1());
				}
				if(!ValidationUtil.isValid(promptIdsVb.getPromptString())){
					  promptValuesDesc[i]="";
					}else{
					promptValuesDesc[i]=promptIdsVb.getPromptString();
					}
			}
		}else{
			for(int i = 0; i<8;i++){
				promptValues[i] = "";
				promptValuesDesc[i]="";
			}
		}
		if(type=="mail"){
			ScheduleReportVb scheduleReportVb = new ScheduleReportVb();
			scheduleReportVb.setRefrenceId(CommonUtils.getReferenceNo());
			scheduleReportVb.setReportId(reportsWriterVb.getReportId());
			scheduleReportVb.setReportType("R");
			scheduleReportVb.setRunType("M");
			scheduleReportVb.setFormatType(reportsWriterVb.getReportType());
			scheduleReportVb.setScallingFactor(reportsWriterVb.getScalingFactor());
			scheduleReportVb.setPromptValue1(promptValues[0]);
			scheduleReportVb.setPromptValue2(promptValues[1]);
			scheduleReportVb.setPromptValue3(promptValues[2]);
			scheduleReportVb.setPromptValue4(promptValues[3]);
			scheduleReportVb.setPromptValue5(promptValues[4]);
			scheduleReportVb.setPromptValue6(promptValues[5]);
			insetAuditTrailReportData(scheduleReportVb);
		}
		if(type=="reportsuite"){
			ReportsWriterVb reportsWriterauditVb = new ReportsWriterVb();
			reportsWriterauditVb.setRefrenceId(CommonUtils.getReferenceNo());
			reportsWriterauditVb.setReportId(reportsWriterVb.getReportId());
			reportsWriterauditVb.setMaker(SessionContextHolder.getContext().getVisionId());
			reportsWriterauditVb.setReportType("R");
			reportsWriterauditVb.setRunType("M");
			reportsWriterauditVb.setScallingFactor(reportsWriterVb.getScalingFactor());
			reportsWriterauditVb.setPromptValue1(promptValues[0]);
			reportsWriterauditVb.setPromptValue2(promptValues[1]);
			reportsWriterauditVb.setPromptValue3(promptValues[2]);
			reportsWriterauditVb.setPromptValue4(promptValues[3]);
			reportsWriterauditVb.setPromptValue5(promptValues[4]);
			reportsWriterauditVb.setPromptValue6(promptValues[5]);
			reportsWriterauditVb.setPromptValue1Desc(promptValuesDesc[0]);
			reportsWriterauditVb.setPromptValue2Desc(promptValuesDesc[1]);
			reportsWriterauditVb.setPromptValue3Desc(promptValuesDesc[2]);
			reportsWriterauditVb.setPromptValue4Desc(promptValuesDesc[3]);
			reportsWriterauditVb.setPromptValue5Desc(promptValuesDesc[4]);
			reportsWriterauditVb.setPromptValue6Desc(promptValuesDesc[5]);
			insetAuditTrailReportData(reportsWriterauditVb);
		}
	}
	
	public int insetAuditTrailReportData(ScheduleReportVb vObject){
		String query = "Insert Into AUDIT_TRAIL_REPORTS (REFERENCE_NO, REPORT_ID, MAKER, RUN_DATE, REPORT_TYPE, RUN_TYPE, FORMAT_TYPE, SCHEDULE_TYPE,"+
				" EMAIL_TO, EMAIL_CC, SCALLING_FACTOR, PROMPT_VALUE_1, PROMPT_VALUE_2, PROMPT_VALUE_3, PROMPT_VALUE_4, PROMPT_VALUE_5, PROMPT_VALUE_6, "
				+ "PROMPT_VALUE_1_DESC, PROMPT_VALUE_2_DESC, PROMPT_VALUE_3_DESC, PROMPT_VALUE_4_DESC, PROMPT_VALUE_5_DESC, PROMPT_VALUE_6_DESC, CATALOG_QUERY)"+
				" Values (?, ?, ?, Sysdate, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "; 
		Object args[] = {vObject.getRefrenceId(), vObject.getReportId(), String.valueOf(intCurrentUserId), vObject.getReportType(), vObject.getRunType(),
				vObject.getFormatType() , vObject.getScheduleType(), vObject.getEmailTo(), vObject.getEmailCc(), vObject.getScallingFactor(), vObject.getPromptValue1(), 
				vObject.getPromptValue2(),  vObject.getPromptValue3(), vObject.getPromptValue4(), vObject.getPromptValue5(), vObject.getPromptValue6(), 
				vObject.getPromptValue1Desc(), vObject.getPromptValue2Desc(), vObject.getPromptValue3Desc(), vObject.getPromptValue4Desc(), vObject.getPromptValue5Desc(), 
				vObject.getPromptValue6Desc(),  vObject.getCatalogQuery()};
		return getJdbcTemplate().update(query,args);
	}
	
	public void setPdfWidhtAndHeight(ReportsWriterVb reportsWriterVb){
		setServiceDefaults();
		final int intKeyFieldsCount = 1;
		String query = new String("SELECT  NVL(PDF_WIDTH, 842) PDF_WIDTH, NVL(PDF_HEIGHT, 595) PDF_HEIGHT FROM REPORT_SUITE WHERE REPORT_ID = ? ");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = reportsWriterVb.getReportId();
		try{
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					ReportsWriterVb reporterWriterVb = new ReportsWriterVb();
					reporterWriterVb.setPdfWidth(rs.getInt("PDF_WIDTH"));
					reporterWriterVb.setPdfHeight(rs.getInt("PDF_HEIGHT"));
					return reporterWriterVb;
				}
			};
			List<ReportsWriterVb> list = getJdbcTemplate().query(query, params, mapper);
			if(list!=null && list.size()>0){
				reportsWriterVb.setPdfWidth(list.get(0).getPdfWidth());
				reportsWriterVb.setPdfHeight(list.get(0).getPdfHeight());
			}
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
		}
	}	
	
	public int InsetAuditTrialDataForReports(ReportsWriterVb reportsWriterVb, List<PromptIdsVb> prompts){

		reportsWriterVb.setReportType("R");
		reportsWriterVb.setRunType("M");
		reportsWriterVb.setRefrenceId(CommonUtils.getReferenceNo());
    	reportsWriterVb.setMaker(SessionContextHolder.getContext().getVisionId());

		String[] promptValuesDesc = new String[8];
		String[] promptValues = new String[8];
		if(prompts != null && !prompts.isEmpty()){
			//Set space for the prompts where the no Prompts are available
			for(int i = prompts.size(); i<8;i++){
				promptValues[i] = "";
			}
			for(int i = prompts.size(); i<8;i++){
				promptValuesDesc[i] = "";
			}
			for(int i =0; i< prompts.size() ;i++){
				PromptIdsVb promptIdsVb = prompts.get(i);
				if(!ValidationUtil.isValid(promptIdsVb.getSelectedValue1()) || !ValidationUtil.isValid(promptIdsVb.getSelectedValue1().getField1())){
					promptValues[i] =  "";
				}else{
					promptValues[i] =  promptIdsVb.getSelectedValue1().getField1();
				}
				if("RANGE".equalsIgnoreCase(promptIdsVb.getPromptType())){
					if(!ValidationUtil.isValid(promptIdsVb.getSelectedValue2()) || !ValidationUtil.isValid(promptIdsVb.getSelectedValue2().getField1())){
						promptValues[i] = new String(promptValues[i] + " - ");
					}else{
						promptValues[i] = new String(promptValues[i] + " - " +promptIdsVb.getSelectedValue2().getField1());
					}
				}
				if(!ValidationUtil.isValid(promptIdsVb.getPromptString())){
					promptValuesDesc[i]="";
				}else{
					promptValuesDesc[i]=promptIdsVb.getPromptString();
				}
			}
		}else{
			for(int i = 0; i<8;i++){
				promptValues[i] = "";
				promptValuesDesc[i]="";
			}
		}
		reportsWriterVb.setScallingFactor(reportsWriterVb.getScalingFactor());
		reportsWriterVb.setPromptValue1(promptValues[0]);
		reportsWriterVb.setPromptValue2(promptValues[1]);
		reportsWriterVb.setPromptValue3(promptValues[2]);
		reportsWriterVb.setPromptValue4(promptValues[3]);
		reportsWriterVb.setPromptValue5(promptValues[4]);
		reportsWriterVb.setPromptValue6(promptValues[5]);
		reportsWriterVb.setPromptValue1Desc(promptValuesDesc[0]);
		reportsWriterVb.setPromptValue2Desc(promptValuesDesc[1]);
		reportsWriterVb.setPromptValue3Desc(promptValuesDesc[2]);
		reportsWriterVb.setPromptValue4Desc(promptValuesDesc[3]);
		reportsWriterVb.setPromptValue5Desc(promptValuesDesc[4]);
		reportsWriterVb.setPromptValue6Desc(promptValuesDesc[5]);
		int intValue = insetAuditTrailReportData(reportsWriterVb);
		return intValue;
	}
	
	public String getListReportDataForExportAsXMLStringHeader(PromptTreeVb promptTree, final List<ColumnHeadersVb> columnHeaders, long min, long max) {
		String query = "";
		try{
			query = "SELECT T1.* FROM "+promptTree.getTableName()+" T1 WHERE SORT_FIELD < 1 ORDER BY SORT_FIELD";
			ResultSetExtractor mapper = new ResultSetExtractor() {
				public Object extractData(ResultSet rs)  throws SQLException, DataAccessException {
					String lResult="";
				     while(rs.next()){
							String value = rs.getString("FORMAT_TYPE");
							if("FHT".equalsIgnoreCase(value)){
								return lResult="FHT";
							}
				     }
					return lResult;
				}
			};
			return (String)getJdbcTemplate().query(query, mapper);
		}catch(BadSqlGrammarException ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException( "","", ex.getSQLException())); 
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = ex.getMessage(); 
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}
	}
	private RowMapper getTemplateStgMapper(){
		RowMapper mapper = new RowMapper(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				TemplateStgVb templateStgVb = new TemplateStgVb();
				templateStgVb.setReportId(rs.getString("REPORT_ID"));
				templateStgVb.setSessionId(rs.getString("SESSION_ID"));
				templateStgVb.setTemplateId(rs.getString("TEMPLATE_ID"));
				templateStgVb.setTabId(rs.getString("TAB_ID"));
				templateStgVb.setRowId(rs.getString("ROW_ID"));;
				templateStgVb.setColId(rs.getString("COL_ID"));
				templateStgVb.setCellData(rs.getString("CELL_DATA"));
				templateStgVb.setColType(rs.getString("COL_TYPE"));
				templateStgVb.setSortField(rs.getString("SORT_FIELD"));
				templateStgVb.setCreateNew(rs.getString("CREATE_NEW"));
				return templateStgVb;
			}
		};
		return mapper;
	}
	
	public List<ReportsWriterVb> getSubReportsForReport(ReportsWriterVb reportsWriterVb) {
		setServiceDefaults();
		final int intKeyFieldsCount = 1;
		String query = new String("SELECT RS.PROMPT_POSITION_1,RS.PROMPT_POSITION_2,RS.PROMPT_POSITION_3,RS.PROMPT_POSITION_4,RS.PROMPT_POSITION_5, RS.PROMPT_POSITION_6,RS.REPORT_ID, RS.REPORT_DESCRIPTION, RS.REPORT_TITLE, RS.REPORT_CATEGORY_AT, RS.REPORT_CATEGORY, RS.SUB_CATEGORY_AT, " +
				"RS.SUB_CATEGORY, RS.LABEL_ROW_COUNT, RS.LABEL_COL_COUNT, RS.REP_PROCEDURE, RS.FILTER_STRING, " +
				"RS.PROMPT_ID_1, RS.PROMPT_ID_2, RS.PROMPT_ID_3, RS.PROMPT_ID_4, RS.PROMPT_ID_5, RS.PROMPT_ID_6, RS.ORIENTATION, "+
				//+ "RS.PROMPT_MACRO_MAPPINGS_1, RS.PROMPT_MACRO_MAPPINGS_2, RS.PROMPT_MACRO_MAPPINGS_3, RS.PROMPT_MACRO_MAPPINGS_4, RS.PROMPT_MACRO_MAPPINGS_5, RS.PROMPT_MACRO_MAPPINGS_6 ," +
				"RS.RS_STATUS_NT, RS.RS_STATUS, RS.MAKER, RS.VERIFIER, RS.INTERNAL_STATUS, RS.DATE_LAST_MODIFIED, RS.DATE_CREATION, RS.RECORD_INDICATOR_NT, " +
				"RS.RECORD_INDICATOR, RS.REPORT_TYPE, RS.DISPLAY_TYPE, RS.DD_FLAG, RS.DD_REPORT_ID, RS.SCALLING_FACTOR, RS.TEMPLATE_NAME, RS.BURST_FLAG, "
				+ "RS.PROMPT_LABEL_1, RS.PROMPT_LABEL_2, RS.PROMPT_LABEL_3, RS.PROMPT_LABEL_4, RS.PROMPT_LABEL_5, RS.PROMPT_LABEL_6 " +
				"FROM REPORT_SUITE RS JOIN RS_XBRL RSD on RSD.REPORT_ID=RS.REPORT_ID WHERE RSD.GROUP_REPORT_ID = ? ORDER BY RSD.DISPLAY_ORDER");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = reportsWriterVb.getReportId();
		try{
			return getJdbcTemplate().query(query, params, getMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? " query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			return null;
		}
	}
	
	public RsDashboardVb getXBRL(ReportsWriterVb parentReportsWriterVb, ReportsWriterVb reportsWriterVb) {
		setServiceDefaults();
		final int intKeyFieldsCount = 2;
		String query = new String("SELECT REPORT_ID, GROUP_REPORT_ID, DISPLAY_ORDER, DOUBLE_WIDTH_FLAG, DEFAULT_CHART_TYPE_NT,"+
				"DEFAULT_CHART_TYPE, "+"MC_FLAG, AXIS_X, AXIS_Y, AXIS_Z, RSD_STATUS_NT, RSD_STATUS, RECORD_INDICATOR_NT, RECORD_INDICATOR,"+
				"MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION FROM RS_XBRL RSD WHERE RSD.GROUP_REPORT_ID = ? " +
				"AND RSD.REPORT_ID = ? ORDER BY RSD.DISPLAY_ORDER");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = parentReportsWriterVb.getReportId();
		params[1] = reportsWriterVb.getReportId();
		try{
			return (RsDashboardVb) getJdbcTemplate().queryForObject(query, params, getRSDMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			return null;
		}
	}
	private RowMapper getRSDMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
				RsDashboardVb rsDashboardVb = new RsDashboardVb();
				rsDashboardVb.setReportId(rs.getString("REPORT_ID"));
				rsDashboardVb.setGroupReportId(rs.getString("GROUP_REPORT_ID"));
				rsDashboardVb.setDisplayOrder(rs.getLong("DISPLAY_ORDER"));
				rsDashboardVb.setDoubleWidthFlag(rs.getString("DOUBLE_WIDTH_FLAG"));
				rsDashboardVb.setDefaultChartTypeNt(rs.getInt("DEFAULT_CHART_TYPE_NT"));
				rsDashboardVb.setDefaultChartType(rs.getInt("DEFAULT_CHART_TYPE"));
				rsDashboardVb.setMcFlag(rs.getString("MC_FLAG"));
				rsDashboardVb.setAxisX(rs.getString("AXIS_X"));
				rsDashboardVb.setAxisY(rs.getString("AXIS_Y"));
				rsDashboardVb.setAxisZ(rs.getString("AXIS_Z"));
				rsDashboardVb.setRsdStatusNt(rs.getInt("RSD_STATUS_NT"));
				rsDashboardVb.setRsdStatus(rs.getInt("RSD_STATUS"));
				rsDashboardVb.setMaker(rs.getLong("MAKER"));
				rsDashboardVb.setVerifier(rs.getLong("VERIFIER"));
				rsDashboardVb.setInternalStatus(rs.getInt("INTERNAL_STATUS"));
				rsDashboardVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				rsDashboardVb.setDateCreation(rs.getString("DATE_CREATION"));
				rsDashboardVb.setRecordIndicatorNt(rs.getInt("RECORD_INDICATOR_NT"));
				rsDashboardVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
				return rsDashboardVb;
			}
		};
		return mapper;
	}
	public String findVisionVariableValueLoc(String pVariableName) throws DataAccessException {
		if(!ValidationUtil.isValid(pVariableName)){
			return null;
		}
		String sql = "select VALUE FROM VISION_VARIABLES where UPPER(VARIABLE) = UPPER(?)";
		Object[] lParams = new Object[1];
		lParams[0] = pVariableName;
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				CommonVb commonVb = new CommonVb();
				commonVb.setMakerName(rs.getString("VALUE"));
				return commonVb;
			}
		};
		List<CommonVb> commonVbs = getJdbcTemplate().query(sql, lParams, mapper);
		if(commonVbs != null && !commonVbs.isEmpty()){
			return commonVbs.get(0).getMakerName();
		}
		return null;
	}
	public List<TemplateStgVb> getTemplateStgData(PromptTreeVb promptTree) {
		setServiceDefaults();
		int intKeyFieldsCount = 2;
		String query = new String(
						 " SELECT REPORT_ID, SESSION_ID, TEMPLATE_ID, TAB_ID, ROW_ID, COL_ID, "
						 +" CELL_DATA, COL_TYPE, SORT_FIELD, CREATE_NEW "
						 +" FROM TEMPLATES_STG "
						 +" WHERE REPORT_ID = ? AND SESSION_ID= ? "
						 +" ORDER BY Tab_ID,Row_Id,Sort_Field,Col_Id");
				
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = promptTree.getReportId();
		params[1] = promptTree.getSessionId();
		try{
			return getJdbcTemplate().query(query, params, getTemplateStgMapper());
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			return null;
		}
	}
	
	public int getMaxOfRowsInHeader(PromptTreeVb promptTree) {
		setServiceDefaults();
		int intKeyFieldsCount = 2;
		String query = new String("SELECT max(LABEL_ROW_NUM) FROM COLUMN_HEADERS_STG WHERE REPORT_ID = ? AND SESSION_ID=? ");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = promptTree.getReportId();
		params[1] = promptTree.getSessionId();
		try{
			return getJdbcTemplate().queryForObject(query, params, Integer.class);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			return 1;
		}
	}
	public int getMaxOfColumCountInHeader(PromptTreeVb promptTree) {
		setServiceDefaults();
		int intKeyFieldsCount = 2;
		String query = new String("SELECT MAX(LABEL_COL_NUM) FROM COLUMN_HEADERS_STG WHERE REPORT_ID = ? AND SESSION_ID=? ");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = promptTree.getReportId();
		params[1] = promptTree.getSessionId();
		try{
			return getJdbcTemplate().queryForObject(query, params, Integer.class);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			return 1;
		}
	}	
	public int getCaptionColumnCount(ReportsWriterVb vOBject) {
		setServiceDefaults();
		int intKeyFieldsCount = 1;
		String query = new String("select LABEL_COL_COUNT from report_suite where report_id = ? ");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = vOBject.getReportId();
		try{
			return getJdbcTemplate().queryForObject(query, params, Integer.class);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			return 1;
		}
	}
	public String getVisionBusinessDate(String CountryId){
		Object args[] = {CountryId};
		return getJdbcTemplate().queryForObject("select TO_CHAR(BUSINESS_DATE,'DD-Mon-RRRR') BUSINESS_DATE  from Vision_Business_Day  WHERE COUNTRY ||'-'|| LE_BOOK=?",
				args,String.class);
	}
	
	public String getDependentPrompt(String reportId, String depPromptString){
		Object args[] = {reportId};
		return getJdbcTemplate().queryForObject("select "+depPromptString+" from report_suite  WHERE report_Id=?",
				args,String.class);
	}
	
	public String getKeyValue(String source, String key){
		try {
			Matcher regexMatcher = Pattern.compile(key+"(.*?\\$@!)(.*?)\\#}").matcher(source);
			String findValue = regexMatcher.find()? regexMatcher.group(2) :"";
			return ValidationUtil.isValid(findValue)?findValue:"";
		} catch (Exception e) {
			return null;
		}
	}
	public String getKeyData(String source, String key){
		try {
			Matcher regexMatcher = Pattern.compile(key+"(.*?\\#)(.*?)\\$@!").matcher(source);
			String findValue = regexMatcher.find()? regexMatcher.group(2) :"";
			return ValidationUtil.isValid(findValue)?findValue:"";
		} catch (Exception e) {
			return null;
		}
	}
	
	public String[] getKeyValue(String source){
		try {
			String[] arr = new String[3];
			Matcher regexMatcher = Pattern.compile("\\{(.*?):#(.*?)\\$@!(.*?)$").matcher(source);
			arr[0] = regexMatcher.find()? regexMatcher.group(1) :"";
			arr[1] = regexMatcher.group(2);
			arr[2] = regexMatcher.group(3);
			return arr;
		} catch (Exception e) {
			return null;
		}
	}
	public List<PromptTreeVb> getCascadePromptData(PromptIdsVb prompt, PromptTreeVb promptInputVb, ReportsWriterVb vObj){
		setServiceDefaults();
		String query = "SELECT FIELD_1, FIELD_2, FIELD_3, FIELD_4, PROMPT_ID FROM PROMPTS_STG WHERE VISION_ID = ? AND SESSION_ID= ? AND PROMPT_ID = ?";
		Connection con = null;
		strCurrentOperation = "Prompts";
		CallableStatement cs =  null;
		try{
			if(!ValidationUtil.isValid(prompt.getPromptScript())){
				strErrorDesc = "Invalid prompt script in Prompt Ids table for Prompt Id["+prompt.getPromptId()+"].";
				throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
			}
			String paramMap="";
			String paramMapData="";

			String macro = getDependentPrompt(vObj.getReportId(), vObj.getTemplateName());
			if(ValidationUtil.isValid(macro)){
				macro =macro.trim();
			}
			String[] otherColValue = macro.split("\\#}");
	        boolean promptFlag=false;
	        boolean promptFlagData=false;

			if(vObj.isChecked()){
				paramMap =vObj.getFilterString();				
			}else{
				int cntData=0;
			    for(int k=0; k<otherColValue.length; k++){
					String source = otherColValue[k];
					if(ValidationUtil.isValid(source)){
						String[] outPut = getKeyValue(source);
						String formation=outPut[0]+"=>?";
						if("PROMPTID".equalsIgnoreCase(outPut[1])){
					       promptFlag =true;
						}
						if(ValidationUtil.isValid(paramMap)){
							if("PROMPT_ID_1".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue1())){
								formation=outPut[0]+"=>"+"'"+vObj.getPromptValue1()+"'";
								cntData=cntData+1;
								paramMapData= paramMapData+","+formation;
							}else if("PROMPT_ID_2".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue2())){
								formation=outPut[0]+"=>"+"'"+vObj.getPromptValue2()+"'";
								cntData=cntData+1;
								paramMapData= paramMapData+","+formation;

							}else if("PROMPT_ID_3".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue3())){
								formation=outPut[0]+"=>"+"'"+vObj.getPromptValue3()+"'";
								cntData=cntData+1;
								paramMapData= paramMapData+","+formation;
							}else if("PROMPT_ID_4".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue4())){
								formation=outPut[0]+"=>"+"'"+vObj.getPromptValue4()+"'";
								cntData=cntData+1;
								paramMapData= paramMapData+","+formation;
							}else if("PROMPT_ID_5".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue5())){
								formation=outPut[0]+"=>"+"'"+vObj.getPromptValue5()+"'";
								cntData=cntData+1;
								paramMapData= paramMapData+","+formation;
							}else if("PROMPT_ID_6".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue6())){
								formation=outPut[0]+"=>"+"'"+vObj.getPromptValue6()+"'";
								cntData=cntData+1;
								paramMapData= paramMapData+","+formation;
							}else{
								cntData=cntData+1;	
							}
							paramMap= paramMap+","+formation;
							if("CONSTANT".equalsIgnoreCase(outPut[1])){
								paramMapData= paramMapData+","+outPut[0]+"=>"+"'"+outPut[2]+"'";
									if(paramMap.contains("?")){
										paramMap =paramMap.replace("?","'"+outPut[2]+"'");
									}
							}
						}else{
							if(promptFlag ==true){
								if("PROMPT_ID_1".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue1())){
									formation=outPut[0]+"=>"+"'"+vObj.getPromptValue1()+"'";
									cntData=cntData+1;
								}else if("PROMPT_ID_2".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue2())){
									formation=outPut[0]+"=>"+"'"+vObj.getPromptValue2()+"'";
									cntData=cntData+1;
								}else if("PROMPT_ID_3".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue3())){
									formation=outPut[0]+"=>"+"'"+vObj.getPromptValue3()+"'";
									cntData=cntData+1;
								}else if("PROMPT_ID_4".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue4())){
									formation=outPut[0]+"=>"+"'"+vObj.getPromptValue4()+"'";
									cntData=cntData+1;
								}else if("PROMPT_ID_5".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue5())){
									formation=outPut[0]+"=>"+"'"+vObj.getPromptValue5()+"'";
									cntData=cntData+1;
								}else if("PROMPT_ID_6".equalsIgnoreCase(outPut[2]) && ValidationUtil.isValid(vObj.getPromptValue6())){
									formation=outPut[0]+"=>"+"'"+vObj.getPromptValue6()+"'";
									cntData=cntData+1;
								}
							}
							if("CONSTANT".equalsIgnoreCase(outPut[1])){
								if(formation.contains("?")){
									formation =formation.replace("?","'"+outPut[2]+"'");
									cntData=cntData+1;
								}
							}
							paramMapData= formation;
							paramMap=formation;
						}
					}
				}
			    int cnt=paramMapData.split("=>").length-1;
			    if(cntData==otherColValue.length && cntData==cnt){
			    	promptFlagData=true;
			    }
			}
			String sessionId = String.valueOf(System.currentTimeMillis());
			if(promptFlag==false){

				con = getConnection();
				if(ValidationUtil.isValid(paramMap)){
					cs = con.prepareCall("{CALL "+prompt.getPromptScript()+"(P_Vision_id=>'"+(int)intCurrentUserId+"',P_Session_Id=>'"+sessionId+"',P_Prompt_Id=>'"+prompt.getPromptId()+"', P_Status=>?, P_ErrorMsg=>?, "+paramMap+")}");
					System.out.println("CALL "+prompt.getPromptScript()+"(P_Vision_id=>'"+(int)intCurrentUserId+"',P_Session_Id=>'"+sessionId+"',P_Prompt_Id=>'"+prompt.getPromptId()+"', P_Status=>?,P_ErrorMsg=>? , "+paramMap+")");
				}else{
					cs = con.prepareCall("{CALL "+prompt.getPromptScript()+"(P_Vision_id=>'"+(int)intCurrentUserId+"',P_Session_Id=>'"+sessionId+"',P_Prompt_Id=>'"+prompt.getPromptId()+"', P_Status=>?,P_ErrorMsg=>?)}");
				}
				cs.registerOutParameter("P_Status", java.sql.Types.VARCHAR);//filterString
				cs.registerOutParameter("P_ErrorMsg", java.sql.Types.VARCHAR);//filterString
		
				if(vObj.isChecked()){
					
				}else{
			      for(int k=0; k<otherColValue.length; k++){
						String source = otherColValue[k];
						String[] outPut = getKeyValue(source);
						String formation=outPut[0]+"=>?";
						if("PROMPTID".equalsIgnoreCase(outPut[1])){
					       cs.setString(outPut[0], "");
						}/*else{
					        cs.setString(outPut[0], outPut[2]);
						}*/
					}
				}
			}else if(promptFlagData==true){
				con = getConnection();
				if(ValidationUtil.isValid(paramMapData)){
					cs = con.prepareCall("{CALL "+prompt.getPromptScript()+"(P_Vision_id=>'"+(int)intCurrentUserId+"',P_Session_Id=>'"+sessionId+"',P_Prompt_Id=>'"+prompt.getPromptId()+"',  P_Status=>?,P_ErrorMsg=>?, "+paramMapData+")}");
					System.out.println("CALL "+prompt.getPromptScript()+"(P_Vision_id=>'"+(int)intCurrentUserId+"',P_Session_Id=>'"+sessionId+"', P_Prompt_Id=>'"+prompt.getPromptId()+"',  P_Status=>?,P_ErrorMsg=>?,"+paramMapData+")");
				}
				cs.registerOutParameter("P_Status", java.sql.Types.VARCHAR);//filterString
				cs.registerOutParameter("P_ErrorMsg", java.sql.Types.VARCHAR);//filterString
			}else{
				return new ArrayList<PromptTreeVb>(0);
			}
			
			ResultSet rs = cs.executeQuery();
			PromptTreeVb promptTreeVb = new PromptTreeVb();
	        promptTreeVb.setStatus(cs.getString("P_Status"));
	        promptTreeVb.setErrorMessage(cs.getString("P_ErrorMsg"));
	        
		    rs.close();
			if(promptTreeVb != null && "0".equalsIgnoreCase(promptTreeVb.getStatus())){
				String[] params = new String[3];
				params[0] = String.valueOf(intCurrentUserId);
				params[1] = sessionId;
				params[2] = prompt.getPromptId();
				if(ValidationUtil.isValid(prompt.getSortStr())){
					query = query+ " " + prompt.getSortStr();
				}
				
				List<PromptTreeVb> tempPromptsList = getJdbcTemplate().query(query, params, getPromptTreeMapper());
				
				if(query.indexOf("Order By")>0){
					query = query.substring(query.indexOf("FROM "), query.indexOf("Order By")-1);
				}else{
					query = query.substring(query.indexOf("FROM "), query.length());
				}
				query = "DELETE "+query;
				int cout = getJdbcTemplate().update(query, params);
				
				return tempPromptsList;
//				return getJdbcTemplate().query(query, params, getPromptTreeMapper());
			}else if(promptTreeVb != null && "1".equalsIgnoreCase(promptTreeVb.getStatus())){
				return new ArrayList<PromptTreeVb>(0);
			}
			throw new RuntimeCustomException(promptTreeVb.getErrorMessage());
		}catch(SQLException ex){
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException("","",ex));
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}catch(Exception ex){
			strErrorDesc = ex.getMessage().trim();
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}finally{
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
		}
	}
	
	public String getFullListReportDataAsXMLString(PromptTreeVb promptTree, final List<String> coloumNames) {
		String query = "";
		try{
			query = "SELECT COUNT(1) FROM "+promptTree.getTableName();
			promptTree.setTotalRows(getJdbcTemplate().queryForObject(query, Long.class));
			query = "SELECT * FROM "+promptTree.getTableName()+" ORDER BY SORT_FIELD ";
			ResultSetExtractor mapper = new ResultSetExtractor() {
				public Object extractData(ResultSet rs)  throws SQLException, DataAccessException {
					StringBuilder lResult = new StringBuilder("");
					lResult.append("<tableData>");
					while(rs.next()){
						lResult.append("<tableRow>");
						for(String columnName:coloumNames){
							String value ="";
							columnNameErr = columnName;
							if(columnName.contains(","))
							{
						        String[] parts = columnName.split(",");
								columnName=parts[0];
								columnNameErr = columnName;
								value = rs.getString(parts[0]);
							}else{
								value = rs.getString(columnName);
							}

							if(value == null) value=""; 
						    lResult.append("<").append(columnName).append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append(columnName).append(">");
						}
						try{
							String HEADERTITLE_TEXTValue = rs.getString("HEADERTITLE_TEXT");
							if(HEADERTITLE_TEXTValue == null) HEADERTITLE_TEXTValue=""; 
							lResult.append("<").append("headerText").append(">").append(StringEscapeUtils.escapeXml(HEADERTITLE_TEXTValue)).append("</").append("headerText").append(">");
						}catch(Exception e){
							//Do nothing this is a format column
						}
						try{
							String value = rs.getString("FORMAT_TYPE");
							if(value == null) value=""; 
							lResult.append("<").append("formatType").append(">").append(StringEscapeUtils.escapeXml(value)).append("</").append("formatType").append(">");
						}catch(Exception e){
							//Do nothing this is a format column
						}
						lResult.append("</tableRow>");
					}
					lResult.append("</tableData>");
					return lResult.toString();
				}
			};
			return (String)getJdbcTemplate().query(query, mapper);
		}catch(BadSqlGrammarException ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = parseErrorMsg(new UncategorizedSQLException( "","", ex.getSQLException()));
			strErrorDesc =strErrorDesc+" - "+columnNameErr;
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			strErrorDesc = ex.getMessage(); 
			strErrorDesc =strErrorDesc+" - "+columnNameErr;
			throw buildRuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ReportsWriterVb> findServerCredentials(String enironmentVariable, String node, String serverName) throws DataAccessException {
		String sql ="";
		if(!ValidationUtil.isValid(node)){
			 sql = "SELECT NODE_IP,NODE_USER, NODE_PWD FROM VISION_NODE_CREDENTIALS WHERE SERVER_ENVIRONMENT='"+enironmentVariable+"' AND SERVER_NAME IN ('"+serverName+"')";
		}else{
			 sql = "SELECT NODE_IP,NODE_USER, NODE_PWD FROM VISION_NODE_CREDENTIALS WHERE SERVER_ENVIRONMENT='"+enironmentVariable+"' AND NODE_NAME='"+node+"'";
		}
		return getJdbcTemplate().query(sql, getCredentialsMapper());
	}
	
	protected RowMapper getCredentialsMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				ReportsWriterVb reportsWriterVb = new ReportsWriterVb();
				reportsWriterVb.setHostName(rs.getString("NODE_IP"));
				reportsWriterVb.setUserName(rs.getString("NODE_USER"));
				reportsWriterVb.setPassword(rs.getString("NODE_PWD"));
				return reportsWriterVb;
			}
		};
		return mapper;
	}
	public boolean checkExist(String variableName){
		  Object params[] = new Object[1];
		  params[0] = variableName;
		  boolean returnValue;
		  List<Map<String, Object>> sizeResult =getJdbcTemplate().queryForList("select VARIABLE_NAME from VISION_DYNAMIC_HASH_VAR WHERE SCRIPT_TYPE='FTPSCRIPT' AND UPPER(VARIABLE_NAME)=UPPER(?) ", params); 
		  if(sizeResult.size() <= 0)
			  returnValue =false;
		  else
			  returnValue =true;
		  return returnValue;
		 }	
}