package com.vision.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;




import com.vision.authentication.SessionContextHolder;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.vb.BuildSchedulesDetailsVb;

public class BuildSchedulesDetailsDao extends AbstractDao<BuildSchedulesDetailsVb> {
	
	protected RowMapper getQueryResultsMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				BuildSchedulesDetailsVb buildSchedulesVb = new BuildSchedulesDetailsVb();
				buildSchedulesVb.setBuildNumber(rs.getString("BUILD_NUMBER"));
				buildSchedulesVb.setSubBuildNumber(rs.getString("SUB_BUILD_NUMBER"));
				buildSchedulesVb.setBsdSequence(rs.getString("BSD_SEQUENCE"));
				buildSchedulesVb.setCountry(rs.getString("COUNTRY"));
				buildSchedulesVb.setLeBook(rs.getString("LE_BOOK"));
				buildSchedulesVb.setAssociatedBuild(rs.getString("ASSOCIATED_BUILD"));
				buildSchedulesVb.setBuildModule(rs.getString("BUILD_MODULE"));
				buildSchedulesVb.setRunItAt(rs.getInt("RUN_IT_AT"));
				buildSchedulesVb.setRunIt(rs.getString("RUN_IT"));
				buildSchedulesVb.setProgramTypeAt(rs.getInt("PROGRAM_TYPE_AT"));
				buildSchedulesVb.setProgramType(rs.getString("PROGRAM_TYPE"));
				buildSchedulesVb.setModuleStatusAt(rs.getInt("MODULE_STATUS_AT"));
				buildSchedulesVb.setModuleStatus(rs.getString("MODULE_STATUS"));
				buildSchedulesVb.setDebugMode(rs.getString("DEBUG_MODE"));
				buildSchedulesVb.setStartTime(rs.getString("START_TIME"));
				buildSchedulesVb.setEndTime(rs.getString("END_TIME"));
				buildSchedulesVb.setDuration(rs.getString("Duration"));
				buildSchedulesVb.setProgressPercent(rs.getString("PROGRESS_PERCENT"));
				buildSchedulesVb.setProgramDescription(rs.getString("PROGRAM_DESCRIPTION"));
				buildSchedulesVb.setDateCreation(rs.getString("DATE_CREATION"));
				buildSchedulesVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				return buildSchedulesVb;
			}
		};
		return mapper;
	}
	protected RowMapper getApprovedRecordMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				BuildSchedulesDetailsVb buildSchedulesVb = new BuildSchedulesDetailsVb();
				buildSchedulesVb.setBuildNumber(rs.getString("BUILD_NUMBER"));
				buildSchedulesVb.setSubBuildNumber(rs.getString("SUB_BUILD_NUMBER"));
				buildSchedulesVb.setBsdSequence(rs.getString("BSD_SEQUENCE"));
				buildSchedulesVb.setCountry(rs.getString("COUNTRY"));
				buildSchedulesVb.setLeBook(rs.getString("LE_BOOK"));
				buildSchedulesVb.setAssociatedBuild(rs.getString("ASSOCIATED_BUILD"));
				buildSchedulesVb.setBuildModule(rs.getString("BUILD_MODULE"));
				buildSchedulesVb.setRunItAt(rs.getInt("RUN_IT_AT"));
				buildSchedulesVb.setRunIt(rs.getString("RUN_IT"));
				buildSchedulesVb.setProgramTypeAt(rs.getInt("PROGRAM_TYPE_AT"));
				buildSchedulesVb.setProgramType(rs.getString("PROGRAM_TYPE"));
				buildSchedulesVb.setModuleStatusAt(rs.getInt("MODULE_STATUS_AT"));
				buildSchedulesVb.setModuleStatus(rs.getString("MODULE_STATUS"));
				buildSchedulesVb.setDebugMode(rs.getString("DEBUG_MODE"));
				buildSchedulesVb.setStartTime(rs.getString("START_TIME"));
				buildSchedulesVb.setEndTime(rs.getString("END_TIME"));
				buildSchedulesVb.setProgressPercent(rs.getString("PROGRESS_PERCENT"));
				buildSchedulesVb.setDateCreation(rs.getString("DATE_CREATION"));
				buildSchedulesVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				return buildSchedulesVb;
			}
		};
		return mapper;
	}
	public List<BuildSchedulesDetailsVb> getQueryDisplayResults(String buildNo){
		
		final int intKeyFieldsCount = 1;
		String query = new String("SELECT TAppr.BUILD_NUMBER, TAppr.SUB_BUILD_NUMBER, TAppr.BSD_SEQUENCE, TAppr.COUNTRY, TAppr.LE_BOOK," +   
				"TAppr.ASSOCIATED_BUILD, TAppr.BUILD_MODULE, TAppr.RUN_IT_AT, " +  
				"TAppr.RUN_IT, TAppr.PROGRAM_TYPE_AT, TAppr.PROGRAM_TYPE, " + 
				"TAppr.MODULE_STATUS_AT, TAppr.MODULE_STATUS, TAppr.DEBUG_MODE, " +  
				"TAppr.START_TIME, TAppr.END_TIME, TAppr.DATE_LAST_MODIFIED, " +  
				"TAppr.DATE_CREATION, TAppr.PROGRESS_PERCENT, TPAppr.PROGRAM_DESCRIPTION, " +
				" round((decode(MODULE_STATUS,'I',sysdate,end_time) - start_time) * (24 * 60))  Duration " + 
				"FROM BUILD_SCHEDULES_DETAILS TAppr, PROGRAMS TPAppr " + 
				"Where TAppr.BUILD_NUMBER = ? AND TAppr.BUILD_MODULE = TPAppr.PROGRAM " + 
				"AND TAppr.BUILD_MODULE = TPAppr.PROGRAM AND TAppr.PROGRAM_TYPE IN ('MODULE','BUILD') " +
				"ORDER BY TAppr.SUB_BUILD_NUMBER, TAppr.BSD_SEQUENCE");

		Object params[] = new Object[intKeyFieldsCount];
		params[0] = buildNo; 
		try
		{
			return getJdbcTemplate().query(query, params, getQueryResultsMapper());
			
		}catch(Exception ex){
			
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query.toString()));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i]);
			return null;
		}
	}
	public Integer getSubbildCountForMajorBuild(String buildNo){
		
		final int intKeyFieldsCount = 1;
		String query = new String("Select count(1) From BUILD_SCHEDULES_DETAILS Where BUILD_NUMBER = ? ");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = buildNo; 
		try{
			return getJdbcTemplate().queryForObject(query, params, Integer.class);
		}catch(Exception ex){
			
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query.toString()));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i]);
			return 0;
		}
	}
	@Override
	protected ExceptionCode doInsertApprRecordForNonTrans(BuildSchedulesDetailsVb vObject) throws RuntimeCustomException {
		List<BuildSchedulesDetailsVb> collTemp = null;
		strApproveOperation ="Add";
		strErrorDesc  = "";
		strCurrentOperation = "Add";
		setServiceDefaults();
		collTemp = selectApprovedRecord(vObject);
		if (collTemp == null)
		{
			return getResultObject(Constants.ERRONEOUS_OPERATION);
		}
		// If record already exists in the approved table, reject the addition
		if (collTemp.size() > 0 )
		{
			return getResultObject(Constants.DUPLICATE_KEY_INSERTION);
		}
		retVal = doInsertionAppr(vObject);
		if(retVal != Constants.SUCCESSFUL_OPERATION)
		{
			return getResultObject(Constants.ERRONEOUS_OPERATION);
		}
		return getResultObject(retVal);
	}
	@Override
	protected ExceptionCode doDeleteApprRecordForNonTrans(BuildSchedulesDetailsVb vObject) throws RuntimeCustomException {
		ExceptionCode exceptionCode = null;
		strApproveOperation ="Add";
		strErrorDesc  = "";
		strCurrentOperation = "Add";
		setServiceDefaults();
		
		int recordCount = getSubbildCountForMajorBuild(vObject.getBuildNumber());
		retVal = doDeleteAppr(vObject);
		if(retVal != recordCount){
			strErrorDesc  = "Unable to delete all the detail records.";
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	@Override
	protected int doInsertionAppr(BuildSchedulesDetailsVb vObject){
		String CalLeBook = removeDescLeBook(vObject.getLeBook());
		String query = "Insert Into BUILD_SCHEDULES_DETAILS ( COUNTRY, LE_BOOK, BUILD_NUMBER, SUB_BUILD_NUMBER, " +
				"BSD_SEQUENCE, ASSOCIATED_BUILD, BUILD_MODULE, RUN_IT_AT, RUN_IT, PROGRAM_TYPE_AT, PROGRAM_TYPE, " +
				"PROGRESS_PERCENT, MODULE_STATUS_AT, MODULE_STATUS, DEBUG_MODE, START_TIME, END_TIME, " +
				"DATE_LAST_MODIFIED, DATE_CREATION, FULL_BUILD_FLAG) Values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, NULL, NULL, SysDate, SysDate, ?)";
		Object[] args = {vObject.getCountry(), CalLeBook, vObject.getBuildNumber(), vObject.getSubBuildNumber(),
				vObject.getBsdSequence(), vObject.getAssociatedBuild(), vObject.getBuildModule(), vObject.getRunItAt(), vObject.getRunIt(),
				vObject.getProgramTypeAt(),vObject.getProgramType(), vObject.getModuleStatusAt(), 
				vObject.getModuleStatus(),vObject.getDebugMode(), vObject.getFullBuildFlag()};
		return getJdbcTemplate().update(query,args);
	}
	@Override
	protected List<BuildSchedulesDetailsVb> selectApprovedRecord(BuildSchedulesDetailsVb vObject) {
		final int intKeyFieldsCount = 3;
		String query = new String("Select COUNTRY, LE_BOOK, BUILD_NUMBER, SUB_BUILD_NUMBER,BSD_SEQUENCE, ASSOCIATED_BUILD, BUILD_MODULE, "+
				"RUN_IT_AT, RUN_IT, PROGRAM_TYPE_AT, PROGRAM_TYPE, PROGRESS_PERCENT, MODULE_STATUS_AT, MODULE_STATUS, "+
				"DEBUG_MODE, To_Char(START_TIME, 'DD-MM-YYYY HH24:MI:SS') as START_TIME, To_Char(END_TIME, 'DD-MM-YYYY HH24:MI:SS') AS END_TIME, "+
				"To_Char(DATE_LAST_MODIFIED, 'DD/MM/YYYY HH24:MI:SS') as DATE_LAST_MODIFIED, To_Char(DATE_CREATION, 'DD/MM/YYYY HH24:MI:SS') as DATE_CREATION "+
				"From BUILD_SCHEDULES_DETAILS Where BUILD_NUMBER = ? AND SUB_BUILD_NUMBER = ? AND BSD_SEQUENCE = ?");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = vObject.getBuildNumber();
		params[1] = vObject.getSubBuildNumber();
		params[2] = vObject.getBsdSequence();
		try
		{
			return getJdbcTemplate().query(query, params, getApprovedRecordMapper());
			
		}catch(Exception ex){
			
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query.toString()));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i]);
			return null;
		}
	}
	@Override
	protected int doDeleteAppr(BuildSchedulesDetailsVb vObject){
		String query = "Delete From BUILD_SCHEDULES_DETAILS Where BUILD_NUMBER = ? ";
		Object[] args = {vObject.getBuildNumber()};
		return getJdbcTemplate().update(query,args);
	}
	private int getRecordCountByStatus(BuildSchedulesDetailsVb buildSchedulesDetailsVb, String pStatus){
		String query = new String("SELECT COUNT(1) as RCCOUNT FROM BUILD_SCHEDULES_DETAILS WHERE BUILD_NUMBER = ? AND MODULE_STATUS IN ("+pStatus+")");
		Object[] args = {buildSchedulesDetailsVb.getBuildNumber()};
		return getJdbcTemplate().queryForObject(query,args,Integer.class);
	}
	private int deleteRecordsByStatus(BuildSchedulesDetailsVb buildSchedulesDetailsVb){
		String query = new String("Delete From BUILD_SCHEDULES_DETAILS Where BUILD_NUMBER = ? AND MODULE_STATUS = ?");
		Object[] args = {buildSchedulesDetailsVb.getBuildNumber(), buildSchedulesDetailsVb.getModuleStatus()};
		return getJdbcTemplate().update(query,args);
	}
	private int updatePendingRecordsStatus(BuildSchedulesDetailsVb buildSchedulesDetailsVb){
		// Change done by Prakash
//		String query = new String("UPDATE BUILD_SCHEDULES_DETAILS SET MODULE_STATUS = ? Where BUILD_NUMBER = ?");
		String query = new String("UPDATE BUILD_SCHEDULES_DETAILS SET MODULE_STATUS = ?, START_TIME = ?, END_TIME = ? Where BUILD_NUMBER = ?");
		Object[] args = {buildSchedulesDetailsVb.getModuleStatus(), buildSchedulesDetailsVb.getStartTime(), buildSchedulesDetailsVb.getEndTime(), buildSchedulesDetailsVb.getBuildNumber()};
		return getJdbcTemplate().update(query,args);
	}
	@Override
	protected void setServiceDefaults(){
		serviceName = "BuildSchedules";
		serviceDesc = CommonUtils.getResourceManger().getString("BuildSchedules");
		tableName = "BUILD_SCHEDULES";
		childTableName = "BUILD_SCHEDULES_DETAILS";
		intCurrentUserId = SessionContextHolder.getContext().getVisionId();
	}
	protected ExceptionCode updateStatusOfNonCompletedBuilds(BuildSchedulesDetailsVb buildSchedulesDetailsVb) {
		ExceptionCode exceptionCode = null;
		setServiceDefaults();
		strErrorDesc  = "";
		strCurrentOperation = "Re-Initiate";
		try
		{	
			BuildSchedulesDetailsVb lBuildSchedulesDetailsVb = new BuildSchedulesDetailsVb();
			lBuildSchedulesDetailsVb.setBuildNumber(buildSchedulesDetailsVb.getBuildNumber());
			lBuildSchedulesDetailsVb.setModuleStatus("C");
			int recordCount = getRecordCountByStatus(buildSchedulesDetailsVb,"'C'");
			retVal = deleteRecordsByStatus(lBuildSchedulesDetailsVb);
			if(retVal != recordCount){
				strErrorDesc  = "Unable to delete all the detail records.";
				exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION); 
				throw buildRuntimeCustomException(exceptionCode);
			}
			recordCount = getRecordCountByStatus(buildSchedulesDetailsVb,"'E','I','K','P','W'");
			lBuildSchedulesDetailsVb.setModuleStatus("P");
			// Added by Prakash
			lBuildSchedulesDetailsVb.setStartTime("");
			lBuildSchedulesDetailsVb.setEndTime("");
			retVal = updatePendingRecordsStatus(lBuildSchedulesDetailsVb);
			if(retVal != recordCount){
				strErrorDesc  = "Unable to update all the detail records.";
				exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION); 
				throw buildRuntimeCustomException(exceptionCode);
			}
			return getResultObject(Constants.SUCCESSFUL_OPERATION);
		}
		catch(Exception ex)
		{
			strErrorDesc = ex.getMessage().trim();
			ex.printStackTrace();
			logger.error(strErrorDesc, ex);
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
	}
}
