package com.vision.dao;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.vision.authentication.SessionContextHolder;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.Paginationhelper;
import com.vision.util.ValidationUtil;
import com.vision.vb.AlphaSubTabVb;
import com.vision.vb.DCManualQueryVb;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.NumSubTabVb;
import com.vision.vb.ReportsWriterVb;
import com.vision.vb.SmartSearchVb;
import com.vision.vb.VcConfigMainVb;
import com.vision.vb.VisionUsersVb;

@Component
public class DCManualQueryDao extends AbstractDao<DCManualQueryVb> {


	@Autowired
	NumSubTabDao numSubTabDao;
	
	@Autowired
	DataSource datasource;
	
	@Override
	protected void setServiceDefaults(){
		serviceName = "VC Query";
		serviceDesc = "VC Query";
		tableName = "VC_QUERIES";
		childTableName = "VC_QUERIES";
		VisionUsersVb vObj = SessionContextHolder.getContext();
		intCurrentUserId = vObj.getVisionId();
		userGroup = vObj.getUserGroup();
		userProfile = vObj.getUserProfile();
	}
	
	@SuppressWarnings("unchecked")
	public List<DCManualQueryVb> getAllDcManualQueryDetails(DCManualQueryVb dObj) throws DataAccessException {
		Vector<Object> params = new Vector<Object>();
		StringBuffer strBufApprove = new StringBuffer("SELECT QUERY_ID, QUERY_DESCRIPTION, DATABASE_TYPE_AT, DATABASE_TYPE, "+
					" DATABASE_CONNECTIVITY_DETAILS,LOOKUP_DATA_LOADING_AT, LOOKUP_DATA_LOADING, QUERY_VALID_FLAG, SQL_QUERY, "+
					" STG_QUERY1, STG_QUERY2, STG_QUERY3, POST_QUERY, VCQ_STATUS_NT, VCQ_STATUS, "+
					" RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER,INTERNAL_STATUS, "+
					" To_Char(DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, "+
					" To_Char(DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, "+
					" COLUMNS_METADATA, HASH_VARIABLE_SCRIPT FROM VC_QUERIES TAPPR ");
		StringBuffer strBufPending = null;
		strBufPending = null;
		try {
			String orderBy = " Order By QUERY_ID ";
			return getQueryPopupResultsForDCManualQuery(dObj, strBufPending, strBufApprove, "", orderBy, params, getMapper());
		} catch (Exception ex) {
			ex.printStackTrace();
			if (params != null)
				for (int i = 0; i < params.size(); i++)
					logger.error("objParams[" + i + "]" + params.get(i).toString());
			return null;
	
		}
	}
	@SuppressWarnings("unchecked")
	public List<DCManualQueryVb> getAllDcManualQueryBasedOnQueryType(Integer queryType) throws DataAccessException {
		setServiceDefaults();
		String sql = ("SELECT T1.QUERY_ID, T1.QUERY_DESCRIPTION FROM VC_QUERIES T1 LEFT JOIN VCQD_QUERIES_ACCESS T2" + 
				"      ON T1.QUERY_ID = T2.VCQD_QUERY_ID " + 
				"      WHERE (T1.MAKER = '"+intCurrentUserId+"'OR (T2.USER_GROUP = '"+userGroup+"' AND T2.USER_PROFILE = '"+userProfile+"')) "+
				"      AND T1.QUERY_CATEGORY="+queryType+" AND T1.QUERY_VALID_FLAG='S' Order By QUERY_ID  ");
		try{
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					DCManualQueryVb  dcManualQueryVb= new DCManualQueryVb();
					dcManualQueryVb.setQueryId(rs.getString("QUERY_ID"));
					dcManualQueryVb.setQueryDescription(rs.getString("QUERY_DESCRIPTION"));
					return dcManualQueryVb;
				}
			};
			return  getJdbcTemplate().query(sql, mapper);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Fetch Manual Query Based on Query Type");
			return null;
		}
	}
	
	public String getColumnListing(String queryID) throws DataAccessException, Exception{
		Object params[] = {queryID};
		String sql = new String("select COLUMNS_METADATA from VC_QUERIES WHERE UPPER(QUERY_ID)=UPPER(?)");
		return getJdbcTemplate().queryForObject(sql,params,String.class);
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
	
	public List<DCManualQueryVb> getSpecificManualQueryDetails(DCManualQueryVb dObj) throws DataAccessException {
		String sql = "SELECT QUERY_ID, QUERY_DESCRIPTION, DATABASE_TYPE_AT, DATABASE_TYPE, "+
				" DATABASE_CONNECTIVITY_DETAILS,LOOKUP_DATA_LOADING_AT, LOOKUP_DATA_LOADING, QUERY_VALID_FLAG, SQL_QUERY, "+
				" STG_QUERY1, STG_QUERY2, STG_QUERY3, POST_QUERY, VCQ_STATUS_NT, VCQ_STATUS, "+
				" RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER,INTERNAL_STATUS, "+
				" To_Char(DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, "+
				" To_Char(DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, "+
				" COLUMNS_METADATA, HASH_VARIABLE_SCRIPT,QUERY_CATEGORY FROM VC_QUERIES TAPPR WHERE QUERY_ID='" + dObj.getQueryId() + "'";
		
		if(ValidationUtil.isValid(dObj.getDatabaseType())){
			if("M_QUERY".equalsIgnoreCase(dObj.getDatabaseType())) {
				dObj.setDatabaseType("MACROVAR");
			}
			sql = sql +" AND DATABASE_TYPE = '"+ dObj.getDatabaseType()+"'";
		}
		try {
			return  getJdbcTemplate().query(sql, getMapper());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	@Override
	protected RowMapper getMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				DCManualQueryVb vObj = new DCManualQueryVb();
				vObj.setQueryId(rs.getString("QUERY_ID"));
				vObj.setQueryDescription(rs.getString("QUERY_DESCRIPTION"));
				vObj.setDatabaseType(rs.getString("DATABASE_TYPE"));
				vObj.setDatabaseConnectivityDetails(rs.getString("DATABASE_CONNECTIVITY_DETAILS"));
				vObj.setLookupDataLoading(rs.getString("LOOKUP_DATA_LOADING"));
				vObj.setSqlQuery(ValidationUtil.isValid(rs.getString("SQL_QUERY"))?rs.getString("SQL_QUERY"):"");
				vObj.setStgQuery1(ValidationUtil.isValid(rs.getString("STG_QUERY1"))?rs.getString("STG_QUERY1"):"");
				vObj.setStgQuery2(ValidationUtil.isValid(rs.getString("STG_QUERY2"))?rs.getString("STG_QUERY2"):"");
				vObj.setStgQuery3(ValidationUtil.isValid(rs.getString("STG_QUERY3"))?rs.getString("STG_QUERY3"):"");
				vObj.setPostQuery(ValidationUtil.isValid(rs.getString("POST_QUERY"))?rs.getString("POST_QUERY"):"");
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
				vObj.setQueryColumnXML(ValidationUtil.isValid(rs.getString("COLUMNS_METADATA"))?rs.getString("COLUMNS_METADATA"):"");
				vObj.setHashVariableScript(ValidationUtil.isValid(rs.getString("HASH_VARIABLE_SCRIPT"))?rs.getString("HASH_VARIABLE_SCRIPT"):"");
				vObj.setQueryValidFlag(ValidationUtil.isValid(rs.getString("QUERY_VALID_FLAG"))?"TRUE":"FALSE");
				vObj.setQueryType(rs.getInt("QUERY_CATEGORY"));
				fetchMakerVerifierNames(vObj);
				return vObj;
			}
		};
		return mapper;
	}
	protected List<DCManualQueryVb> getQueryPopupResultsForDCManualQuery(DCManualQueryVb dObj, StringBuffer pendingQuery,
			StringBuffer approveQuery, String whereNotExistsQuery, String orderBy, Vector<Object> params,RowMapper rowMapper) {
		if (!ValidationUtil.isValid(dObj.getTotalRows()))
			dObj.setTotalRows(0);
		Object objParams[] = null;
		int Ctr = 0;
		int Ctr2 = 0;
		List<DCManualQueryVb> result;

		objParams = new Object[params.size() * 2];

		for (Ctr = 0; Ctr < params.size(); Ctr++)
			objParams[Ctr] = (Object) params.elementAt(Ctr);
		for (Ctr2 = 0; Ctr2 < params.size(); Ctr2++, Ctr++)
			objParams[Ctr] = (Object) params.elementAt(Ctr2);

		Paginationhelper<DCManualQueryVb> paginationhelper = new Paginationhelper<DCManualQueryVb>();

		if (whereNotExistsQuery != null && !whereNotExistsQuery.isEmpty() && approveQuery != null)
			CommonUtils.addToQuery(whereNotExistsQuery, approveQuery);
		String query = "";
		if (approveQuery == null || pendingQuery == null) {
			if (approveQuery == null) {
				pendingQuery.append(orderBy);
				query = pendingQuery.toString();
			} else {
				approveQuery.append(orderBy);
				query = approveQuery.toString();
			}
		} else {
			query = approveQuery.toString() + " Union " + pendingQuery.toString();
		}
		if (dObj.getTotalRows() <= 0) {
			result = paginationhelper.fetchPage(getJdbcTemplate(), query, objParams, dObj.getStartIndex(),
					dObj.getLastIndex(), rowMapper == null ? getMapper() : rowMapper);
			dObj.setTotalRows(paginationhelper.getTotalRows());
		} else {
			result = paginationhelper.fetchPage(getJdbcTemplate(), query, objParams, dObj.getStartIndex(),
					dObj.getLastIndex(), dObj.getTotalRows(), rowMapper == null ? getMapper() : rowMapper);
		}
		return result;
	}
	
	
	public List<DCManualQueryVb> getQueryResults(DCManualQueryVb dObj, int intStatus){

		List<DCManualQueryVb> collTemp = null;
		final int intKeyFieldsCount = 1;
		String strQueryAppr = new String(" SELECT TAPPR.QUERY_ID, TAPPR.QUERY_DESCRIPTION, TAPPR.DATABASE_TYPE_AT, TAPPR.DATABASE_TYPE, TAPPR.DATABASE_CONNECTIVITY_DETAILS, "+
				" TAPPR.LOOKUP_DATA_LOADING_AT, TAPPR.LOOKUP_DATA_LOADING, TAPPR.QUERY_VALID_FLAG, TAPPR.SQL_QUERY, "+
				" TAPPR.STG_QUERY1, TAPPR.STG_QUERY2, TAPPR.STG_QUERY3, TAPPR.POST_QUERY, TAPPR.VCQ_STATUS_NT, TAPPR.VCQ_STATUS, "+
				" TAPPR.RECORD_INDICATOR_NT, TAPPR.RECORD_INDICATOR, TAPPR.MAKER, TAPPR.VERIFIER, "+
				" TAPPR.INTERNAL_STATUS, To_Char(TAPPR.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, "+
				" To_Char(TAPPR.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, "+
				" TAPPR.COLUMNS_METADATA, TAPPR.HASH_VARIABLE_SCRIPT, TAPPR.QUERY_CATEGORY "+
				" FROM VC_QUERIES TAPPR "+
				" Where UPPER(TAppr.QUERY_ID) = UPPER(?) ");
		String strQueryPend = new String(" SELECT TPEND.QUERY_ID, TPEND.QUERY_DESCRIPTION, TPEND.DATABASE_TYPE_AT, TPEND.DATABASE_TYPE, TPEND.DATABASE_CONNECTIVITY_DETAILS, "+
				" TPEND.LOOKUP_DATA_LOADING_AT, TPEND.LOOKUP_DATA_LOADING, TPEND.QUERY_VALID_FLAG, TPEND.SQL_QUERY, "+
				" TPEND.STG_QUERY1, TPEND.STG_QUERY2, TPEND.STG_QUERY3, TPEND.POST_QUERY, TPEND.VCQ_STATUS_NT, TPEND.VCQ_STATUS, "+
				" TPEND.RECORD_INDICATOR_NT, TPEND.RECORD_INDICATOR, TPEND.MAKER, TPEND.VERIFIER, "+
				" TPEND.INTERNAL_STATUS, TO_CHAR(TPEND.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, "+
				" TO_CHAR(TPEND.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, "+
				" TPEND.COLUMNS_METADATA, TPEND.HASH_VARIABLE_SCRIPT, TPEND.QUERY_CATEGORY  "+
				" FROM VC_QUERIES_PEND TPEND "+
				" WHERE UPPER(TPEND.QUERY_ID) = UPPER(?) ");

		Object objParams[] = new Object[intKeyFieldsCount];
		objParams[0] = new String(dObj.getQueryId().toUpperCase());//[QUERY_ID]

		try
		{if(!dObj.isVerificationRequired()){intStatus =0;}
			if(intStatus == 0)
			{
				logger.info("Executing approved query");
				collTemp = getJdbcTemplate().query(strQueryAppr.toString(),objParams,getMapper());
			}else{
				logger.info("Executing pending query");
				collTemp = getJdbcTemplate().query(strQueryPend.toString(),objParams,getMapper());
			}
			return collTemp;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Error: getQueryResults Exception :   ");
			if(intStatus == 0)
				logger.error(((strQueryAppr == null) ? "strQueryAppr is Null" : strQueryAppr.toString()));
			else
				logger.error(((strQueryPend == null) ? "strQueryPend is Null" : strQueryPend.toString()));

			if (objParams != null)
				for(int i=0 ; i< objParams.length; i++)
					logger.error("objParams[" + i + "]" + objParams[i].toString());
			return null;
		}
	}
	@Override
	protected List<DCManualQueryVb> selectApprovedRecord(DCManualQueryVb vObject){
		return getQueryResults(vObject, Constants.STATUS_ZERO);
	}
	@Override
	protected List<DCManualQueryVb> doSelectPendingRecord(DCManualQueryVb vObject){
		return getQueryResults(vObject, Constants.STATUS_PENDING);
	}
	@Override
	protected int getStatus(DCManualQueryVb records){return records.getVcqStatus();}
	@Override
	protected void setStatus(DCManualQueryVb vObject,int status){vObject.setVcqStatus(status);}
	@Override
	protected int doInsertionAppr(DCManualQueryVb vObject){
		String query = " INSERT INTO VC_QUERIES (QUERY_ID, QUERY_DESCRIPTION, DATABASE_TYPE_AT, DATABASE_TYPE, DATABASE_CONNECTIVITY_DETAILS,"+
				" LOOKUP_DATA_LOADING_AT, LOOKUP_DATA_LOADING, VCQ_STATUS_NT, VCQ_STATUS, "+
				" RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, "+
				" DATE_LAST_MODIFIED, DATE_CREATION, QUERY_VALID_FLAG,SQL_QUERY, STG_QUERY1, STG_QUERY2, STG_QUERY3, POST_QUERY, COLUMNS_METADATA, HASH_VARIABLE_SCRIPT,QUERY_CATEGORY) "+
				" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, Sysdate, Sysdate, ?, ?, ?, ?, ?, ?, ?, ?,?)";
		Object[] args = {vObject.getQueryId(), vObject.getQueryDescription(), vObject.getDatabaseTypeAt(), "MACROVAR", vObject.getDatabaseConnectivityDetails(), 
				vObject.getLookupDataLoadingAt(), vObject.getLookupDataLoading(), vObject.getVcqStatusNt(), vObject.getVcqStatus(),
				vObject.getRecordIndicatorNt(), vObject.getRecordIndicator(), vObject.getMaker(), vObject.getVerifier(), vObject.getInternalStatus(),
				vObject.getQueryValidFlag() };
		int result=0;
		
		try{
			return getJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					int argumentLength = args.length;
					PreparedStatement ps = connection.prepareStatement(query);
					for (int i = 1; i <= argumentLength; i++) {
						ps.setObject(i, args[i - 1]);
					}

					String clobData = ValidationUtil.isValid(vObject.getSqlQuery())?vObject.getSqlQuery():"";//SQL_QUERY
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery1())?vObject.getStgQuery1():"";	//STG_QUERY1
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery2())?vObject.getStgQuery2():"";	//STG_QUERY2
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery3())?vObject.getStgQuery3():"";	//STG_QUERY3
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getPostQuery())?vObject.getPostQuery():"";	//POST_QUERY
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getQueryColumnXML())?vObject.getQueryColumnXML():"";	//Columns_METADATA
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
				
					clobData = ValidationUtil.isValid(vObject.getHashVariableScript())?vObject.getHashVariableScript():"";	//HASH_VARIABLE_SCRIPT
					clobData = clobData.replaceAll("@HASH@", "#");
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					ps.setObject(++argumentLength, vObject.getQueryType());
					return ps;
				}
			});
			
		}catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Insert Error in VC_QUERIES: "+e.getMessage());
		}
		return result;
	}
	
	
	@Override
	protected int doInsertionPend(DCManualQueryVb vObject){
		String query = " INSERT INTO VC_QUERIES_PEND (QUERY_ID, QUERY_DESCRIPTION, DATABASE_TYPE_AT, DATABASE_TYPE, DATABASE_CONNECTIVITY_DETAILS,"+
				" LOOKUP_DATA_LOADING_AT, LOOKUP_DATA_LOADING, VCQ_STATUS_NT, VCQ_STATUS, "+
				" RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, "+
				" DATE_LAST_MODIFIED, DATE_CREATION, QUERY_VALID_FLAG,SQL_QUERY, STG_QUERY1, STG_QUERY2, STG_QUERY3, POST_QUERY, COLUMNS_METADATA, HASH_VARIABLE_SCRIPT) "+
				" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, Sysdate, Sysdate, ?, ?, ?, ?, ?, ?, ?, ?)";
		Object[] args = {vObject.getQueryId(), vObject.getQueryDescription(), vObject.getDatabaseTypeAt(), "MACROVAR", vObject.getDatabaseConnectivityDetails(), 
				vObject.getLookupDataLoadingAt(), vObject.getLookupDataLoading(), vObject.getVcqStatusNt(), vObject.getVcqStatus(),
				vObject.getRecordIndicatorNt(), vObject.getRecordIndicator(), vObject.getMaker(), vObject.getVerifier(), vObject.getInternalStatus(),
				vObject.getQueryValidFlag() };
		int result=0;
		try{
			return getJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					int argumentLength = args.length;
					PreparedStatement ps = connection.prepareStatement(query);
					for(int i=1;i<=argumentLength;i++){
						ps.setObject(i,args[i-1]);
					}
					String clobData = ValidationUtil.isValid(vObject.getSqlQuery())?vObject.getSqlQuery():"";//SQL_QUERY
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery1())?vObject.getStgQuery1():"";	//STG_QUERY1
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery2())?vObject.getStgQuery2():"";	//STG_QUERY2
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery3())?vObject.getStgQuery3():"";	//STG_QUERY3
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getPostQuery())?vObject.getPostQuery():"";	//POST_QUERY
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getQueryColumnXML())?vObject.getQueryColumnXML():"";	//Columns_METADATA
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
				
					clobData = ValidationUtil.isValid(vObject.getHashVariableScript())?vObject.getHashVariableScript():"";	//HASH_VARIABLE_SCRIPT
					clobData = clobData.replaceAll("@HASH@", "#");
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					return ps;
				}
			});
			
		}catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Update Error in VC_QUERIES: "+e.getMessage());
		}
		return result;
	}
	
	@Override
	protected int doInsertionPendWithDc(DCManualQueryVb vObject){
		String query = " INSERT INTO VC_QUERIES_PEND (QUERY_ID, QUERY_DESCRIPTION, DATABASE_TYPE_AT, DATABASE_TYPE, DATABASE_CONNECTIVITY_DETAILS,"+
				" LOOKUP_DATA_LOADING_AT, LOOKUP_DATA_LOADING, VCQ_STATUS_NT, VCQ_STATUS, "+
				" RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, "+
				" DATE_LAST_MODIFIED, DATE_CREATION, QUERY_VALID_FLAG,SQL_QUERY, STG_QUERY1, STG_QUERY2, STG_QUERY3, POST_QUERY, COLUMNS_METADATA, HASH_VARIABLE_SCRIPT) "+
				" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, Sysdate, Sysdate, ?, ?, ?, ?, ?, ?, ?, ?)";
		Object[] args = {vObject.getQueryId(), vObject.getQueryDescription(), vObject.getDatabaseTypeAt(), "MACROVAR", vObject.getDatabaseConnectivityDetails(), 
				vObject.getLookupDataLoadingAt(), vObject.getLookupDataLoading(), vObject.getVcqStatusNt(), vObject.getVcqStatus(),
				vObject.getRecordIndicatorNt(), vObject.getRecordIndicator(), vObject.getMaker(), vObject.getVerifier(), vObject.getInternalStatus(),
				vObject.getQueryValidFlag() };
		int result=0;
		try{
			
			return getJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					int argumentLength = args.length;
					PreparedStatement ps = connection.prepareStatement(query);
					for(int i=1;i<=argumentLength;i++){
						ps.setObject(i,args[i-1]);
					}
					String clobData = ValidationUtil.isValid(vObject.getSqlQuery())?vObject.getSqlQuery():"";//SQL_QUERY
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery1())?vObject.getStgQuery1():"";	//STG_QUERY1
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery2())?vObject.getStgQuery2():"";	//STG_QUERY2
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery3())?vObject.getStgQuery3():"";	//STG_QUERY3
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getPostQuery())?vObject.getPostQuery():"";	//POST_QUERY
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getQueryColumnXML())?vObject.getQueryColumnXML():"";	//Columns_METADATA
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
				
					clobData = ValidationUtil.isValid(vObject.getHashVariableScript())?vObject.getHashVariableScript():"";	//HASH_VARIABLE_SCRIPT
					clobData = clobData.replaceAll("@HASH@", "#");
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					return ps;
				}
			});
			
		}catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Insert Error in VC_QUERIES: "+e.getMessage());
		}
		return result;
	}
	@Override
	protected int doUpdateAppr(DCManualQueryVb vObject){
		String query = " UPDATE VC_QUERIES SET SQL_QUERY = ?, STG_QUERY1 = ?, STG_QUERY2 = ?, STG_QUERY3 = ?, POST_QUERY = ?, COLUMNS_METADATA = ?, HASH_VARIABLE_SCRIPT = ?, "+
				" QUERY_DESCRIPTION = ?, DATABASE_TYPE_AT = ?, DATABASE_TYPE = ?, DATABASE_CONNECTIVITY_DETAILS = ?, LOOKUP_DATA_LOADING_AT = ?, "+
				" LOOKUP_DATA_LOADING = ?, "+
				" VCQ_STATUS_NT = ?, VCQ_STATUS = ?, RECORD_INDICATOR_NT = ?, RECORD_INDICATOR = ?, MAKER = ?, VERIFIER = ?, INTERNAL_STATUS = ?, "+
				" DATE_LAST_MODIFIED = SYSDATE ,QUERY_VALID_FLAG = ?,QUERY_CATEGORY = ?  WHERE QUERY_ID = ?";
		Object[] args = {vObject.getQueryDescription(), vObject.getDatabaseTypeAt(), "MACROVAR", vObject.getDatabaseConnectivityDetails(), vObject.getLookupDataLoadingAt(),
				vObject.getLookupDataLoading(), vObject.getVcqStatusNt(),
				vObject.getVcqStatus(), vObject.getRecordIndicatorNt(), vObject.getRecordIndicator(), vObject.getMaker(), vObject.getVerifier(), 
				vObject.getInternalStatus(),vObject.getQueryValidFlag(),vObject.getQueryType(),	vObject.getQueryId()};
		int result=0;
		try{
			
			return getJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(query);
					int psIndex = 0;
					String clobData = ValidationUtil.isValid(vObject.getSqlQuery())?vObject.getSqlQuery():"";	//SQL_QUERY
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery1())?vObject.getStgQuery1():"";	//STG_QUERY1
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery2())?vObject.getStgQuery2():"";	//STG_QUERY2
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery3())?vObject.getStgQuery3():"";	//STG_QUERY3
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getPostQuery())?vObject.getPostQuery():"";	//POST_QUERY
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getQueryColumnXML())?vObject.getQueryColumnXML():"";	//Columns_METADATA
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
				
					clobData = ValidationUtil.isValid(vObject.getHashVariableScript())?vObject.getHashVariableScript():"";	//HASH_VARIABLE_SCRIPT
					clobData = clobData.replaceAll("@HASH@", "#");
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					for(int i=1;i<=args.length;i++){
						ps.setObject(++psIndex,args[i-1]);	
					}
					
					return ps;
				}
			});
			
		}catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Update Error in VC_QUERIES: "+e.getMessage());
		}
		return result;
	}
	
	
	@Override
	protected int doUpdatePend(DCManualQueryVb vObject){
		String query = " UPDATE VC_QUERIES_PEND SET SQL_QUERY = ?, STG_QUERY1 = ?, STG_QUERY2 = ?, STG_QUERY3 = ?, POST_QUERY = ?, COLUMNS_METADATA = ?, HASH_VARIABLE_SCRIPT = ?, "+
				" QUERY_DESCRIPTION = ?, DATABASE_TYPE_AT = ?, DATABASE_TYPE = ?, DATABASE_CONNECTIVITY_DETAILS = ?, LOOKUP_DATA_LOADING_AT = ?, "+
				" LOOKUP_DATA_LOADING = ?, "+
				" VCQ_STATUS_NT = ?, VCQ_STATUS = ?, RECORD_INDICATOR_NT = ?, RECORD_INDICATOR = ?, MAKER = ?, VERIFIER = ?, INTERNAL_STATUS = ?, "+
				" DATE_LAST_MODIFIED = SYSDATE ,QUERY_VALID_FLAG = ? WHERE QUERY_ID = ?";
		Object[] args = {vObject.getQueryDescription(), vObject.getDatabaseTypeAt(), "MACROVAR", vObject.getDatabaseConnectivityDetails(), vObject.getLookupDataLoadingAt(),
				vObject.getLookupDataLoading(), vObject.getVcqStatusNt(),
				vObject.getVcqStatus(), vObject.getRecordIndicatorNt(), vObject.getRecordIndicator(), vObject.getMaker(), vObject.getVerifier(), vObject.getInternalStatus(),vObject.getQueryValidFlag(),
				vObject.getQueryId()};
		int result=0;
		try{
			
			return getJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(query);
					int psIndex = 0;
					String clobData = ValidationUtil.isValid(vObject.getSqlQuery())?vObject.getSqlQuery():"";	//SQL_QUERY
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery1())?vObject.getStgQuery1():"";	//STG_QUERY1
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery2())?vObject.getStgQuery2():"";	//STG_QUERY2
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getStgQuery3())?vObject.getStgQuery3():"";	//STG_QUERY3
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getPostQuery())?vObject.getPostQuery():"";	//POST_QUERY
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObject.getQueryColumnXML())?vObject.getQueryColumnXML():"";	//Columns_METADATA
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
				
					clobData = ValidationUtil.isValid(vObject.getHashVariableScript())?vObject.getHashVariableScript():"";	//HASH_VARIABLE_SCRIPT
					clobData = clobData.replaceAll("@HASH@", "#");
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					for(int i=1;i<=args.length;i++){
						ps.setObject(++psIndex,args[i-1]);	
					}
					
					return ps;
				}
			});
		}catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Update Error in VC_QUERIES: "+e.getMessage());
		}
		return result;
	}
	@Override
	protected int doDeleteAppr(DCManualQueryVb vObject){
		int result=0;
		String query = "Delete From VCQD_QUERIES_ACCESS Where VCQD_QUERY_ID = ?";
		Object[] args = {vObject.getQueryId()};
		try {
			getJdbcTemplate().update(query,args);
			
			query = "Delete From VC_QUERIES Where QUERY_ID = ?";
			
			return getJdbcTemplate().update(query,args);
		}catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Delete Error in VC_QUERIES: "+e.getMessage());
		}
		return result;
	}
	@Override
	protected int deletePendingRecord(DCManualQueryVb vObject){
		int result=0;
		String query = "Delete From VC_QUERIES_PEND Where QUERY_ID = ?";
		Object[] args = {vObject.getQueryId()};
		try {
			return getJdbcTemplate().update(query,args);
		}catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			logger.error("Delete Error in VC_QUERIES: "+e.getMessage());
		}
		return result;
	}
	
	public List<DCManualQueryVb> getQueryPopupResults(DCManualQueryVb dObj){
		
		Vector<Object> params = new Vector<Object>();
		StringBuffer strBufApprove = new StringBuffer("SELECT TAPPR.QUERY_ID, TAPPR.QUERY_DESCRIPTION, TAPPR.DATABASE_TYPE_AT, TAPPR.DATABASE_TYPE, "
				+ "TAPPR.DATABASE_CONNECTIVITY_DETAILS, TAPPR.LOOKUP_DATA_LOADING_AT, TAPPR.LOOKUP_DATA_LOADING, TAPPR.QUERY_VALID_FLAG, TAPPR.SQL_QUERY, "
				+ "TAPPR.STG_QUERY1, TAPPR.STG_QUERY2, TAPPR.STG_QUERY3, TAPPR.POST_QUERY, TAPPR.VCQ_STATUS_NT, TAPPR.VCQ_STATUS, "
				+ "TAPPR.RECORD_INDICATOR_NT, TAPPR.RECORD_INDICATOR, TAPPR.MAKER, TAPPR.VERIFIER, TAPPR.INTERNAL_STATUS, "
				+ "TO_CHAR(TAPPR.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, TO_CHAR(TAPPR.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, "
				+ "TAPPR.COLUMNS_METADATA, TAPPR.HASH_VARIABLE_SCRIPT "
				+ "FROM VC_QUERIES TAPPR ");
		String strWhereNotExists = new String( " NOT EXISTS (SELECT 'X' FROM VC_QUERIES_PEND TPEND WHERE TPEND.QUERY_ID = TAPPR.QUERY_ID)");
		StringBuffer strBufPending = new StringBuffer("SELECT TPEND.QUERY_ID, TPEND.QUERY_DESCRIPTION, TPEND.DATABASE_TYPE_AT, TPEND.DATABASE_TYPE, "
				+ "TPEND.DATABASE_CONNECTIVITY_DETAILS, TPEND.LOOKUP_DATA_LOADING_AT, TPEND.LOOKUP_DATA_LOADING, TPEND.QUERY_VALID_FLAG , TPEND.SQL_QUERY, "
				+ "TPEND.STG_QUERY1, TPEND.STG_QUERY2, TPEND.STG_QUERY3, TPEND.POST_QUERY, TPEND.VCQ_STATUS_NT, TPEND.VCQ_STATUS, "
				+ "TPEND.RECORD_INDICATOR_NT, TPEND.RECORD_INDICATOR, TPEND.MAKER, TPEND.VERIFIER, TPEND.INTERNAL_STATUS, "
				+ "TO_CHAR(TPEND.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED, TO_CHAR(TPEND.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION, "
				+ "TPEND.COLUMNS_METADATA, TPEND.HASH_VARIABLE_SCRIPT "
				+ "FROM VC_QUERIES_PEND TPEND ");
		try
		{
			
			if (dObj.getVcqStatus() != -1){
				params.addElement(new Integer(dObj.getVcqStatus()));
				CommonUtils.addToQuery("TAppr.VCQ_STATUS = ?", strBufApprove);
				CommonUtils.addToQuery("TPEND.VCQ_STATUS = ?", strBufPending);
			}

			/*if (ValidationUtil.isValid(dObj.getQueryId())){
				params.addElement("%" + dObj.getQueryId().toUpperCase() + "%");
				CommonUtils.addToQuery("UPPER(TAppr.QUERY_ID) LIKE ?", strBufApprove);
				CommonUtils.addToQuery("UPPER(TPEND.QUERY_ID) LIKE ?", strBufPending);
			}
			*/
			if (ValidationUtil.isValid(dObj.getQueryId())){
				params.addElement(dObj.getQueryId().toUpperCase());
				CommonUtils.addToQuery("UPPER(TAppr.QUERY_ID) = ?", strBufApprove);
				CommonUtils.addToQuery("UPPER(TPEND.QUERY_ID) = ?", strBufPending);
			}
			
			if (ValidationUtil.isValid(dObj.getQueryDescription())){
				params.addElement("%" + dObj.getQueryDescription().toUpperCase() + "%");
				CommonUtils.addToQuery("UPPER(TAPPR.QUERY_DESCRIPTION) LIKE ?", strBufApprove);
				CommonUtils.addToQuery("UPPER(TPend.QUERY_DESCRIPTION) LIKE ?", strBufPending);
			}

			if (ValidationUtil.isValid(dObj.getDatabaseType())){
				if("M_QUERY".equalsIgnoreCase(dObj.getDatabaseType())) {
					dObj.setDatabaseType("MACROVAR");
				}
				params.addElement(dObj.getDatabaseType().toUpperCase());
				CommonUtils.addToQuery("UPPER(TAppr.DATABASE_TYPE) = ?", strBufApprove);
				CommonUtils.addToQuery("UPPER(TPend.DATABASE_TYPE) = ?", strBufPending);
			}
			
		  /*if (ValidationUtil.isValid(dObj.getDatabaseType())){
				params.addElement("%" + dObj.getDatabaseType().toUpperCase() + "%");
				CommonUtils.addToQuery("UPPER(TAppr.DATABASE_TYPE) LIKE ?", strBufApprove);
				CommonUtils.addToQuery("UPPER(TPend.DATABASE_TYPE) LIKE ?", strBufPending);
			}
		 */
			if (ValidationUtil.isValid(dObj.getDatabaseConnectivityDetails())){
				params.addElement("%" + dObj.getDatabaseConnectivityDetails().toUpperCase() + "%");
				CommonUtils.addToQuery("UPPER(TAppr.DATABASE_CONNECTIVITY_DETAILS) LIKE ?", strBufApprove);
				CommonUtils.addToQuery("UPPER(TPend.DATABASE_CONNECTIVITY_DETAILS) LIKE ?", strBufPending);
			}
			
			/*if (ValidationUtil.isValid(dObj.getLookupDataLoading()) && !"-1".equalsIgnoreCase(dObj.getLookupDataLoading())){
				params.addElement("%" + dObj.getLookupDataLoading().toUpperCase() + "%");
				CommonUtils.addToQuery("UPPER(TAppr.LOOKUP_DATA_LOADING) LIKE ?", strBufApprove);
				CommonUtils.addToQuery("UPPER(TPend.LOOKUP_DATA_LOADING) LIKE ?", strBufPending);
			}*/
			
			//check if the column [RECORD_INDICATOR] should be included in the query
			if (dObj.getRecordIndicator() != -1){
				if (dObj.getRecordIndicator() > 3){
					params.addElement(new Integer(0));
					CommonUtils.addToQuery("TAppr.RECORD_INDICATOR > ?", strBufApprove);
					CommonUtils.addToQuery("TPend.RECORD_INDICATOR > ?", strBufPending);
				}else{
					params.addElement(new Integer(dObj.getRecordIndicator()));
					CommonUtils.addToQuery("TAppr.RECORD_INDICATOR = ?", strBufApprove);
					CommonUtils.addToQuery("TPend.RECORD_INDICATOR = ?", strBufPending);
				}
			}
			String orderBy=" Order By QUERY_ID ";
			return getQueryPopupResults(dObj,strBufPending, strBufApprove, strWhereNotExists, orderBy, params);
			
		}catch(Exception ex){
			
			ex.printStackTrace();
			logger.error(((strBufApprove==null)? "strBufApprove is Null":strBufApprove.toString()));
			logger.error("UNION");
			logger.error(((strBufPending==null)? "strBufPending is Null":strBufPending.toString()));

			if (params != null)
				for(int i=0 ; i< params.size(); i++)
					logger.error("objParams[" + i + "]" + params.get(i).toString());
			return null;
		}
	}
	
	public List<AlphaSubTabVb> getQuerySmartSearchResults(DCManualQueryVb dObj) {
		StringBuffer strBufApprove = new StringBuffer(
				" SELECT VARIABLE_NAME, VARIABLE_DESCRIPTION FROM VISION_DYNAMIC_HASH_VAR  WHERE  SCRIPT_TYPE = 'MACROVAR'  "
						+ " AND VARIABLE_TYPE = 2 AND VARIABLE_STATUS = 0 AND UPPER (VARIABLE_SCRIPT) LIKE UPPER ('{DATABASE%')");
		
		strBufApprove= strBufApprove.append(" AND UPPER(VARIABLE_NAME) LIKE '%" + dObj.getLocale().toUpperCase() + "%'");
		strBufApprove= strBufApprove.append(" ORDER BY CASE WHEN VARIABLE_NAME = 'DEFAULT_VISION_DB' then 1 end, VARIABLE_NAME");
		
		String query=strBufApprove.toString();
			
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				AlphaSubTabVb dObj = new AlphaSubTabVb();
				dObj.setAlphaSubTab(rs.getString("VARIABLE_NAME"));
				dObj.setDescription(rs.getString("VARIABLE_DESCRIPTION"));
				return dObj;
			}
		};
		List<AlphaSubTabVb> vObj = getJdbcTemplate().query(query,  mapper);
		return vObj;
	}
	
	@Override
	protected String frameErrorMessage(DCManualQueryVb vObject, String strOperation){
		// specify all the key fields and their values first
		String strErrMsg = new String("");
		try{
			strErrMsg =  strErrMsg + "QUERY_ID:" + vObject.getQueryId();
			// Now concatenate the error message that has been sent
			if ("Approve".equalsIgnoreCase(strOperation))
				strErrMsg = strErrMsg + " failed during approve Operation. Bulk Approval aborted !!";
			else
				strErrMsg = strErrMsg + " failed during reject Operation. Bulk Rejection aborted !!";
		}catch(Exception ex){
			strErrorDesc = ex.getMessage();
			strErrMsg = strErrMsg + strErrorDesc;
			logger.error(strErrMsg, ex);
		}
		// Return back the error message string
		return strErrMsg;
	}
	
	@Override
	protected String getAuditString(DCManualQueryVb vObject){
		final String auditDelimiter = vObject.getAuditDelimiter();
		final String auditDelimiterColVal = vObject.getAuditDelimiterColVal();
		StringBuffer strAudit = new StringBuffer("");
		if(ValidationUtil.isValid(vObject.getQueryId()))
			strAudit.append("QUERY_ID"+auditDelimiterColVal+vObject.getQueryId().trim());
		else
			strAudit.append("QUERY_ID"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);
		
		if(ValidationUtil.isValid(vObject.getQueryDescription()))
			strAudit.append("QUERY_DESCRIPTION"+auditDelimiterColVal+vObject.getQueryDescription().trim());
		else
			strAudit.append("QUERY_DESCRIPTION"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);
		
		strAudit.append("DATABASE_TYPE_AT"+auditDelimiterColVal+vObject.getDatabaseTypeAt());
		strAudit.append(auditDelimiter);
		
		if(ValidationUtil.isValid(vObject.getDatabaseType()))
			strAudit.append("DATABASE_TYPE"+auditDelimiterColVal+vObject.getDatabaseType().trim());
		else
			strAudit.append("DATABASE_TYPE"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);
		
		if(ValidationUtil.isValid(vObject.getDatabaseConnectivityDetails()))
			strAudit.append("DATABASE_CONNECTIVITY_DETAILS"+auditDelimiterColVal+vObject.getDatabaseConnectivityDetails().trim());
		else
			strAudit.append("DATABASE_CONNECTIVITY_DETAILS"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);

		strAudit.append("LOOKUP_DATA_LOADING_AT"+auditDelimiterColVal+vObject.getLookupDataLoadingAt());
		strAudit.append(auditDelimiter);
		
		if(ValidationUtil.isValid(vObject.getLookupDataLoading()))
			strAudit.append("LOOKUP_DATA_LOADING"+auditDelimiterColVal+vObject.getLookupDataLoading().trim());
		else
			strAudit.append("LOOKUP_DATA_LOADING"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);
		
		/*if(ValidationUtil.isValid(vObject.getSqlQuery()))
			strAudit.append("SQL_QUERY"+auditDelimiterColVal+(vObject.getSqlQuery().length()>99?vObject.getSqlQuery().substring(0, 100):vObject.getSqlQuery()));
		else
			strAudit.append("SQL_QUERY"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);
		
		if(ValidationUtil.isValid(vObject.getStgQuery1()))
			strAudit.append("SQL_QUERY1"+auditDelimiterColVal+(vObject.getStgQuery1().length()>99?vObject.getStgQuery1().substring(0, 100):vObject.getStgQuery1()));
		else
			strAudit.append("STG_QUERY1"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);
		
		if(ValidationUtil.isValid(vObject.getStgQuery2()))
			strAudit.append("SQL_QUERY2"+auditDelimiterColVal+(vObject.getStgQuery2().length()>99?vObject.getStgQuery2().substring(0, 100):vObject.getStgQuery2()));
		else
			strAudit.append("STG_QUERY2"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);
		
		if(ValidationUtil.isValid(vObject.getStgQuery3()))
			strAudit.append("SQL_QUERY3"+auditDelimiterColVal+(vObject.getStgQuery3().length()>99?vObject.getStgQuery3().substring(0, 100):vObject.getStgQuery3()));
		else
			strAudit.append("STG_QUERY3"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);
		
		if(ValidationUtil.isValid(vObject.getPostQuery()))
			strAudit.append("POST_QUERY"+auditDelimiterColVal+(vObject.getSqlQuery().length()>99?vObject.getPostQuery().substring(0, 100):vObject.getPostQuery()));
		else
			strAudit.append("POST_QUERY"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);*/
		
		strAudit.append("VCQ_STATUS_NT"+auditDelimiterColVal+vObject.getVcqStatusNt());
		strAudit.append(auditDelimiter);
		strAudit.append("VCQ_STATUS"+auditDelimiterColVal+vObject.getVcqStatus());
		strAudit.append(auditDelimiter);
		strAudit.append("RECORD_INDICATOR_NT"+auditDelimiterColVal+vObject.getRecordIndicatorNt());
		strAudit.append(auditDelimiter);
		if(vObject.getRecordIndicator() == -1)
			vObject.setRecordIndicator(0);
		strAudit.append("RECORD_INDICATOR"+auditDelimiterColVal+vObject.getRecordIndicator());
		strAudit.append(auditDelimiter);
		strAudit.append("MAKER"+auditDelimiterColVal+vObject.getMaker());
		strAudit.append(auditDelimiter);
		strAudit.append("VERIFIER"+auditDelimiterColVal+vObject.getVerifier());
		strAudit.append(auditDelimiter);
		strAudit.append("INTERNAL_STATUS"+auditDelimiterColVal+vObject.getInternalStatus());
		strAudit.append(auditDelimiter);
		if(ValidationUtil.isValid(vObject.getDateLastModified()))
			strAudit.append("DATE_LAST_MODIFIED"+auditDelimiterColVal+vObject.getDateLastModified().trim());
		else
			strAudit.append("DATE_LAST_MODIFIED"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);

		if(ValidationUtil.isValid(vObject.getDateCreation()))
			strAudit.append("DATE_CREATION"+auditDelimiterColVal+vObject.getDateCreation().trim());
		else
			strAudit.append("DATE_CREATION"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);
		
		/*if(ValidationUtil.isValid(vObject.getQueryValidFlag()))
			strAudit.append("QUERY_VALID_FLAG"+auditDelimiterColVal+vObject.getQueryValidFlag().trim());
		else
			strAudit.append("QUERY_VALID_FLAG"+auditDelimiterColVal+"NULL");
		strAudit.append(auditDelimiter);*/
		
		/*if(ValidationUtil.isValid(vObject.getQueryColumnXML()))
			strAudit.append("COLUMNS_METADATA"+auditDelimiterColVal+vObject.getQueryColumnXML().trim());
		else
			strAudit.append("COLUMNS_METADATA"+auditDelimiterColVal+"NULL");*/
		strAudit.append(auditDelimiter);
	
		return strAudit.toString();
	}

	@Override
	protected ExceptionCode doInsertApprRecordForNonTrans(DCManualQueryVb vObject) throws RuntimeCustomException {
		List<DCManualQueryVb> collTemp = null;
		ExceptionCode exceptionCode = null;
		strCurrentOperation = Constants.ADD;
		strApproveOperation =Constants.ADD;		
		setServiceDefaults();
		if("RUNNING".equalsIgnoreCase(getBuildStatus(vObject))){
			exceptionCode = getResultObject(Constants.BUILD_IS_RUNNING);
			throw buildRuntimeCustomException(exceptionCode);
		}
		vObject.setMaker(getIntCurrentUserId());
		collTemp = selectApprovedRecord(vObject);
		if (collTemp == null){
			logger.error("Collection is null for Select Approved Record");
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		// If record already exists in the approved table, reject the addition
		if (collTemp.size() > 0 ){
			int intStaticDeletionFlag = getStatus(((ArrayList<DCManualQueryVb>)collTemp).get(0));
			if (intStaticDeletionFlag == Constants.PASSIVATE){
				logger.error("Collection size is greater than zero - Duplicate record found, but inactive");
				exceptionCode = getResultObject(Constants.RECORD_ALREADY_PRESENT_BUT_INACTIVE);
				throw buildRuntimeCustomException(exceptionCode);
			}else{
				logger.error("Collection size is greater than zero - Duplicate record found");
				exceptionCode = getResultObject(Constants.DUPLICATE_KEY_INSERTION);
				throw buildRuntimeCustomException(exceptionCode);
			}
		}
		// Try inserting the record
		vObject.setRecordIndicator(Constants.STATUS_ZERO);
		vObject.setVerifier(getIntCurrentUserId());
		retVal = doInsertionAppr(vObject);
		if (retVal != Constants.SUCCESSFUL_OPERATION){
			exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
		
	    retVal = doInsertionApprForLOD(vObject);
		if (retVal != Constants.SUCCESSFUL_OPERATION){
			exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
		
		String systemDate = getSystemDate();
		vObject.setDateLastModified(systemDate);
		vObject.setDateCreation(systemDate);
		exceptionCode = writeAuditLog(vObject, null);
		if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
			exceptionCode = getResultObject(Constants.AUDIT_TRAIL_ERROR);
			throw buildRuntimeCustomException(exceptionCode);
		}
		return exceptionCode;
	}

	private int doInsertionApprForLOD(DCManualQueryVb vObject) {

		String query = "INSERT INTO VCQD_QUERIES_ACCESS ( VCQD_QUERY_ID,  VCQDA_STATUS_NT, VCQDA_STATUS, RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER, "+
				" DATE_LAST_MODIFIED, DATE_CREATION, USER_GROUP_AT, USER_GROUP, USER_PROFILE, USER_PROFILE_AT, QUERY_TYPE_NT, QUERY_TYPE) VALUES "+
				" (?, ?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?, ?)";
		Object[] args = {vObject.getQueryId(),  1, 0, 7, 0, intCurrentUserId, intCurrentUserId,1,"NA","NA",2, 2009, 0};
		int result=0;
		try{
			result = getJdbcTemplate().update(query, args);
		}catch (Exception e) {
			e.printStackTrace();
			return Constants.ERRONEOUS_OPERATION;
		}
		return result;
	
	}

	public ExceptionCode deleteManualQuery(DSConnectorVb dsConnectorVb) {
		ExceptionCode exceptioncode = new ExceptionCode();
		try {
			moveMainDataToAD(dsConnectorVb.getMacroVar());
		 getJdbcTemplate().execute("DELETE from VC_QUERIES where QUERY_ID ='"+dsConnectorVb.getMacroVar()+"'");
		 getJdbcTemplate().execute("DELETE from VCQD_QUERIES_ACCESS where VCQD_QUERY_ID ='"+dsConnectorVb.getMacroVar()+"'");
		 exceptioncode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
		 exceptioncode.setErrorMsg("DELETE successful");
		}catch (Exception e ) {
			 exceptioncode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			 exceptioncode.setErrorMsg("Error While deleting the Record");
		}
		return exceptioncode;
	}

	public synchronized int getMaxVersionNumber(String macroVar) {
		String sql = "SELECT CASE WHEN MAX(VERSION_NO) IS NULL THEN 0 ELSE MAX(VERSION_NO) END VERSION_NO FROM VC_QUERIES_AD WHERE QUERY_ID = ?";
		Object args[] = {macroVar};
		return getJdbcTemplate().queryForObject(sql, args, Integer.class);
	}
	private void moveMainDataToAD(String macroVar) {
		Integer versionNo = getMaxVersionNumber(macroVar)+1;
		getJdbcTemplate().update("INSERT INTO VC_QUERIES_AD(QUERY_ID, DATABASE_TYPE_AT,DATABASE_TYPE,DATABASE_CONNECTIVITY_DETAILS,LOOKUP_DATA_LOADING_AT, " + 
				"       LOOKUP_DATA_LOADING,SQL_QUERY,STG_QUERY1,STG_QUERY2,STG_QUERY3,POST_QUERY,VCQ_STATUS_NT,VCQ_STATUS,RECORD_INDICATOR_NT,RECORD_INDICATOR, " + 
				"       MAKER,VERIFIER,INTERNAL_STATUS,DATE_LAST_MODIFIED,DATE_CREATION,QUERY_DESCRIPTION,QUERY_VALID_FLAG,COLUMNS_METADATA,HASH_VARIABLE_SCRIPT, " + 
				"       QUERY_CATEGORY_NT,QUERY_CATEGORY, VERSION_NO )SELECT QUERY_ID,DATABASE_TYPE_AT,DATABASE_TYPE,DATABASE_CONNECTIVITY_DETAILS,LOOKUP_DATA_LOADING_AT, " + 
				"       LOOKUP_DATA_LOADING,SQL_QUERY,STG_QUERY1,STG_QUERY2,STG_QUERY3,POST_QUERY,VCQ_STATUS_NT,VCQ_STATUS,RECORD_INDICATOR_NT,RECORD_INDICATOR,MAKER, " + 
				"       VERIFIER,INTERNAL_STATUS,DATE_LAST_MODIFIED,DATE_CREATION,QUERY_DESCRIPTION,QUERY_VALID_FLAG,COLUMNS_METADATA,HASH_VARIABLE_SCRIPT,QUERY_CATEGORY_NT, " + 
				"       QUERY_CATEGORY,'"+versionNo+"' From VC_QUERIES WHERE QUERY_ID='"+macroVar+"' ");
	}
	
	@SuppressWarnings("unchecked")
	public List<DCManualQueryVb> getAllManualQueryDetails(DCManualQueryVb vObj) throws DataAccessException {
		setServiceDefaults();
		Vector<Object> params = new Vector<Object>();
		StringBuffer sql =new StringBuffer("SELECT T1.QUERY_ID QUERY_ID, T1.QUERY_DESCRIPTION QUERY_DESCRIPTION, 'M_QUERY' DATABASE_TYPe," + 
				"       (SELECT NUM_SUBTAB_DESCRIPTION FROM NUM_SUB_TAB" + 
				"         WHERE NUM_TAB = VCQ_STATUS_NT AND VCQ_STATUS = NUM_SUB_TAB) AS STATUS_DESC," + 
				"       (SELECT USER_NAME FROM VISION_USERS WHERE VISION_ID = T1.MAKER) AS MAKER_NAME, T1.MAKER, T1.VERIFIER," + 
				"       (SELECT USER_NAME FROM VISION_USERS WHERE VISION_ID = T1.VERIFIER) AS VERIFIER_NAME," + 
				"       TO_CHAR (T1.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED," + 
				"       TO_CHAR (T1.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION," + 
				"       CASE QUERY_VALID_FLAG WHEN 'S' THEN 'TRUE' ELSE 'FALSE'" + 
				"       END VALIDFLAG FROM VC_QUERIES T1 LEFT JOIN VCQD_QUERIES_ACCESS T2" + 
				"       ON T1.QUERY_ID = T2.VCQD_QUERY_ID" + 
				"      WHERE (   T1.MAKER = '"+intCurrentUserId+"'OR (T2.USER_GROUP = '"+userGroup+"' AND T2.USER_PROFILE = '"+userProfile+"'))");
	
		return getQueryPopupResultsWithPend(vObj, null, sql, "", "", params, getallMQMapper());

	}

	@SuppressWarnings("rawtypes")
	protected RowMapper getallMQMapper() {
		RowMapper mapper = new RowMapper() {
			@SuppressWarnings("deprecation")
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				DCManualQueryVb vObj = new DCManualQueryVb();
				vObj.setQueryId(rs.getString("QUERY_ID"));
				vObj.setQueryDescription(rs.getString("QUERY_DESCRIPTION"));
				vObj.setDatabaseType(rs.getString("DATABASE_TYPE"));
				vObj.setStatusDesc(rs.getString("status_desc"));
				vObj.setMaker(rs.getLong("MAKER"));
				vObj.setMakerName(rs.getString("maker_name"));
				vObj.setVerifier(rs.getLong("VERIFIER"));
				vObj.setVerifierName(rs.getString("verifier_name"));
				vObj.setDateCreation(rs.getString("DATE_CREATION"));
				vObj.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				vObj.setVerificationRequired(Boolean.getBoolean(rs.getString("validflag")));
				/*				
				vObj.setDatabaseConnectivityDetails(rs.getString("DATABASE_CONNECTIVITY_DETAILS"));
				vObj.setLookupDataLoading(rs.getString("LOOKUP_DATA_LOADING"));
				vObj.setSqlQuery(ValidationUtil.isValid(rs.getString("SQL_QUERY")) ? rs.getString("SQL_QUERY"): "");
				vObj.setStgQuery1(ValidationUtil.isValid(rs.getString("STG_QUERY1"))? rs.getString("STG_QUERY1"): "");
				vObj.setStgQuery2(ValidationUtil.isValid(rs.getString("STG_QUERY2"))? rs.getString("STG_QUERY2"): "");
				vObj.setStgQuery3(ValidationUtil.isValid(rs.getString("STG_QUERY3"))? rs.getString("STG_QUERY3"): "");
				vObj.setPostQuery(ValidationUtil.isValid(rs.getString("POST_QUERY"))? rs.getString("POST_QUERY"): "");
				vObj.setVcqStatusNt(rs.getInt("VCQ_STATUS_NT"));
				vObj.setVcqStatus(rs.getInt("VCQ_STATUS"));
				vObj.setDbStatus(rs.getInt("VCQ_STATUS"));
				vObj.setRecordIndicatorNt(rs.getInt("RECORD_INDICATOR_NT"));
				vObj.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
				vObj.setInternalStatus(rs.getInt("INTERNAL_STATUS"));	
                vObj.setQueryColumnXML(rs.getString("COLUMNS_METADATA"));
				vObj.setHashVariableScript(rs.getString("HASH_VARIABLE_SCRIPT"));*/
				return vObj;
			}
		};
		return mapper;
	}
	
	

	public List<DCManualQueryVb> getQuerySmartSearchFilter(DCManualQueryVb dObj) {
		setServiceDefaults();
		Vector<Object> params = new Vector<Object>();

		StringBuffer strApprove =new StringBuffer("SELECT T1.QUERY_ID QUERY_ID, T1.QUERY_DESCRIPTION QUERY_DESCRIPTION, 'M_QUERY' DATABASE_TYPe," + 
				"       (SELECT NUM_SUBTAB_DESCRIPTION FROM NUM_SUB_TAB" + 
				"         WHERE NUM_TAB = VCQ_STATUS_NT AND VCQ_STATUS = NUM_SUB_TAB) AS STATUS_DESC," + 
				"       (SELECT NUM_SUB_TAB FROM NUM_SUB_TAB "+
			    "       WHERE NUM_TAB = VCQ_STATUS_NT AND VCQ_STATUS = NUM_SUB_TAB) as STATUS_CODE, "+
				"       (SELECT USER_NAME FROM VISION_USERS WHERE VISION_ID = T1.MAKER) AS MAKER_NAME, T1.MAKER, T1.VERIFIER," + 
				"       (SELECT USER_NAME FROM VISION_USERS WHERE VISION_ID = T1.VERIFIER) AS VERIFIER_NAME," + 
				"       TO_CHAR (T1.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED," + 
				"       TO_CHAR (T1.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION," + 
				"       CASE QUERY_VALID_FLAG WHEN 'S' THEN 'TRUE' ELSE 'FALSE'" + 
				"       END VALIDFLAG FROM VC_QUERIES T1 LEFT JOIN VCQD_QUERIES_ACCESS T2" + 
				"       ON T1.QUERY_ID = T2.VCQD_QUERY_ID" + 
				"      WHERE (   T1.MAKER = '"+intCurrentUserId+"'OR (T2.USER_GROUP = '"+userGroup+"' AND T2.USER_PROFILE = '"+userProfile+"'))");
		strApprove = new StringBuffer("select * from ("+strApprove+")");

		StringBuffer strBufPending = null;
		
		try {
			String orderBy = " Order By QUERY_ID ";

			if (dObj.getSmartSearchVb().size() > 0) {
				int count = 1;
				for (SmartSearchVb data: dObj.getSmartSearchVb()){
					if(count == dObj.getSmartSearchVb().size()) {
						data.setJoinType("");
					}
					String val = CommonUtils.criteriaBasedVal(data.getCriteria(), data.getValue());
					switch (data.getObject()) {
					case "queryId":
						CommonUtils.addToQuerySearch(" upper(QUERY_ID) "+ val, strApprove, data.getJoinType());
						break;
	
					case "queryDescription":
						CommonUtils.addToQuerySearch(" upper(QUERY_DESCRIPTION) "+ val, strApprove, data.getJoinType());
						break;

					case "dateCreation":
						CommonUtils.addToQuerySearch(" (DATE_CREATION) "+ val, strApprove, data.getJoinType());
						break;

					case "dateLastModified":
						CommonUtils.addToQuerySearch(" (DATE_LAST_MODIFIED) "+ val, strApprove, data.getJoinType());
						break;

					case "statusDesc":
						List<NumSubTabVb> numSTData = numSubTabDao.findNumSubTabDesc(val);
						String actData="";
						for(int k=0; k< numSTData.size(); k++) {
							int numsubtab = numSTData.get(k).getNumSubTab();
							if(!ValidationUtil.isValid(actData)) {
								actData = "'"+Integer.toString(numsubtab)+"'";
							}else {
								actData =actData+ ","+ "'"+Integer.toString(numsubtab)+"'";
							}
						}
						CommonUtils.addToQuerySearch(" (STATUS_CODE) IN ("+ actData+") ", strApprove, data.getJoinType());
						break; 
						
					default:
					}
					count++;
				}
			}
			
			return getQueryPopupResultsWithPend(dObj, strBufPending, strApprove, "", orderBy, params,getallMQMapper());
		} catch (Exception ex) {
			ex.printStackTrace();
			if (params != null)
				for (int i = 0; i < params.size(); i++)
					logger.error("objParams[" + i + "]" + params.get(i).toString());
			return null;
		}
	}
}