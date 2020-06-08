package com.vision.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.springframework.jdbc.core.RowMapper;

import com.vision.authentication.SessionContextHolder;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.BuildSchedulesDetailsVb;
import com.vision.vb.BuildSchedulesVb;

public class BuildSchedulesDao extends AbstractDao<BuildSchedulesVb> {

	private BuildControlsDao buildControlsDao;
	private BuildSchedulesDetailsDao buildSchedulesDetailsDao;
	
	@Override
	protected void setServiceDefaults(){
		serviceName = "BuildSchedules";
		serviceDesc = CommonUtils.getResourceManger().getString("BuildSchedules");
		tableName = "BUILD_SCHEDULES";
		childTableName = "BUILD_SCHEDULES";
		intCurrentUserId = SessionContextHolder.getContext().getVisionId();
	}
	
	public long getLastFetchIntervel(){
		String strBufApprove = new String("SELECT round((sysdate - LAST_FETCH_TIME) * (24 * 60))  Fetch_Intvl " +
				" FROM BUILD_CRON_FETCH_DET ");
		try
		{
			return getJdbcTemplate().queryForObject(strBufApprove.toString(),Long.class);
			
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((strBufApprove==null)? "strBufApprove is Null":strBufApprove.toString()));
			return 3;
		}
	}
	protected RowMapper getQueryResultsMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				BuildSchedulesVb buildSchedulesVb = new BuildSchedulesVb();
				buildSchedulesVb.setBuild(rs.getString("BUILD"));
				buildSchedulesVb.setScheduledDate(rs.getString("SCHEDULED_DATE"));
				buildSchedulesVb.setBuildNumber(rs.getString("BUILD_NUMBER"));
				buildSchedulesVb.setSubmitterId(rs.getInt("SUBMITTER_ID"));
				buildSchedulesVb.setSupportContact(rs.getString("SUPPORT_CONTACT"));
				buildSchedulesVb.setNotify(rs.getString("NOTIFY"));
				buildSchedulesVb.setBuildScheduleStatusAt(rs.getInt("BUILD_SCHEDULE_STATUS_AT"));
				buildSchedulesVb.setBuildScheduleStatus(rs.getString("BUILD_SCHEDULE_STATUS"));
				buildSchedulesVb.setParallelProcsCount(rs.getInt("PARALLEL_PROCS_COUNT"));
				buildSchedulesVb.setRecurringFrequencyAt(rs.getInt("RECURRING_FREQUENCY_AT"));
				buildSchedulesVb.setRecurringFrequency(rs.getString("RECURRING_FREQUENCY"));
				buildSchedulesVb.setStartTime(rs.getString("START_TIME"));
				buildSchedulesVb.setEndTime(rs.getString("END_TIME"));
				buildSchedulesVb.setDuration(rs.getString("Duration"));
				buildSchedulesVb.setCountry(rs.getString("COUNTRY"));
				buildSchedulesVb.setLeBook(rs.getString("LE_BOOK"));
				buildSchedulesVb.setDateCreation(rs.getString("DATE_CREATION"));
				buildSchedulesVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				buildSchedulesVb.setNode(rs.getString("NODE"));
				buildSchedulesVb.setBusinessDate(rs.getString("BUSINESS_DATE"));
				buildSchedulesVb.setFeedDate(rs.getString("FEED_DATE"));
				
				return buildSchedulesVb;
			}
		};
		return mapper;
	}
	public List<BuildSchedulesVb> getQueryResults(BuildSchedulesVb dObj, int status)
	{
		setServiceDefaults();
		Vector<Object> params = new Vector<Object>();
		
		String CalLeBook = removeDescLeBook(dObj.getLeBook());

		StringBuffer strBufApprove = new StringBuffer("Select TAppr.BUILD," +
				"To_Char(TAppr.SCHEDULED_DATE, 'DD-MM-RRRR HH24:MI:SS') SCHEDULED_DATE, TAppr.BUILD_NUMBER," +
				"TAppr.PARALLEL_PROCS_COUNT, TAppr.NOTIFY, TAppr.SUPPORT_CONTACT," +
				"TAppr.SUBMITTER_ID, To_Char(TAppr.START_TIME, 'DD-MM-RRRR HH24:MI:SS') START_TIME," +
				"To_Char(TAppr.END_TIME, 'DD-MM-RRRR HH24:MI:SS') END_TIME, TAppr.BUILD_SCHEDULE_STATUS_AT," +
				"TAppr.BUILD_SCHEDULE_STATUS, To_Char(TAppr.DATE_LAST_MODIFIED, 'DD/MM/RRRR HH24:MI:SS') DATE_LAST_MODIFIED," +
				"To_Char(TAppr.DATE_CREATION, 'DD/MM/RRRR HH24:MI:SS') DATE_CREATION, TAppr.COUNTRY," +
				"TAppr.LE_BOOK, TAppr.RECURRING_FREQUENCY_AT, TAppr.RECURRING_FREQUENCY, " +
				" round((sysdate - start_time) * (24 * 60))  Duration , NVL(TAppr.NODE_OVERRIDE,TAppr.NODE_REQUEST) NODE, To_Char(TAppr.BUSINESS_DATE, 'DD-MM-RRRR') BUSINESS_DATE, To_Char(TAppr.FEED_DATE, 'DD-MM-RRRR') FEED_DATE " + 
				" From BUILD_SCHEDULES TAppr, PROGRAMS TPAppr Where TAppr.BUILD = TPAppr.PROGRAM and TPAppr.PROGRAM_TYPE = 'MAJORBLD'");

		try
		{
			
			if (ValidationUtil.isValid(dObj.getCountry()))
			{
				params.addElement(dObj.getCountry().toUpperCase());
				strBufApprove.append(" AND TAppr.COUNTRY = ?");
			}

			if (ValidationUtil.isValid(CalLeBook))
			{
				params.addElement(CalLeBook);
				strBufApprove.append(" AND TAppr.LE_BOOK = ?");
			}
			
			if (ValidationUtil.isValid(dObj.getBuild()))
			{
				params.addElement("%"+ dObj.getBuild().toUpperCase() +"%");
				strBufApprove.append(" AND UPPER(TAppr.BUILD) LIKE ?");
			}
			if (ValidationUtil.isValid(dObj.getBuildNumber()))
			{
				params.addElement(dObj.getBuildNumber());
				strBufApprove.append(" AND TAppr.BUILD_NUMBER = ?");
			}
			if (ValidationUtil.isValid(dObj.getBuildScheduleStatus()) && !"-1".equalsIgnoreCase(dObj.getBuildScheduleStatus())){
				params.addElement(dObj.getBuildScheduleStatus());
				strBufApprove.append(" AND TAppr.BUILD_SCHEDULE_STATUS = ?");
			}
			String orderBy = " Order By BUILD, BUILD_NUMBER, SCHEDULED_DATE";
			return getQueryPopupResults(dObj,new StringBuffer(), strBufApprove, "", orderBy, params, getQueryResultsMapper());
			
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((strBufApprove==null)? "strBufApprove is Null":strBufApprove.toString()));
			if (params != null)
				for(int i=0 ; i< params.size(); i++)
					logger.error("objParams[" + i + "]" + params.get(i).toString());
			return null;
		}
	}
	
	public BuildSchedulesVb getQueryResultsForDetails(String buildNumber, String build)
	{
		
		final int intKeyFieldsCount = 2;
		String strBufApprove = new String("Select TAppr.BUILD," +
			"To_Char(TAppr.SCHEDULED_DATE, 'DD-MM-RRRR HH24:MI:SS') SCHEDULED_DATE, TAppr.BUILD_NUMBER," +
			"TAppr.PARALLEL_PROCS_COUNT, TAppr.NOTIFY, TAppr.SUPPORT_CONTACT," +
			"TAppr.SUBMITTER_ID, To_Char(TAppr.START_TIME, 'DD-MM-RRRR HH24:MI:SS') START_TIME," +
			"To_Char(TAppr.END_TIME, 'DD-MM-RRRR HH24:MI:SS') END_TIME, TAppr.BUILD_SCHEDULE_STATUS_AT," +
			"TAppr.BUILD_SCHEDULE_STATUS, To_Char(TAppr.DATE_LAST_MODIFIED, 'DD/MM/RRRR HH24:MI:SS') DATE_LAST_MODIFIED," +
			"To_Char(TAppr.DATE_CREATION, 'DD/MM/RRRR HH24:MI:SS') DATE_CREATION, TAppr.COUNTRY," +
			"TAppr.LE_BOOK, TAppr.RECURRING_FREQUENCY_AT, TAppr.RECURRING_FREQUENCY, " +
			" round((decode(BUILD_SCHEDULE_STATUS,'I',sysdate,end_time) - start_time) * (24 * 60))  Duration , NVL(TAppr.NODE_OVERRIDE,TAppr.NODE_REQUEST) NODE, To_Char(TAppr.BUSINESS_DATE, 'DD-MM-RRRR') BUSINESS_DATE, To_Char(TAppr.FEED_DATE, 'DD-MM-RRRR') FEED_DATE " +  
			" From BUILD_SCHEDULES TAppr " +
			"Where TAppr.BUILD_NUMBER = ? AND TAppr.BUILD = ?");

		Object params[] = new Object[intKeyFieldsCount];
		params[0] = buildNumber;//[BUILD_NUMBER]
		params[1] = build;//[BUILD]

		try
		{
			List<BuildSchedulesVb> result = getJdbcTemplate().query(strBufApprove, params, getQueryResultsMapper());
			if(result == null || result.isEmpty()){
				return null;
			}else{
				return result.get(0);
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			logger.error(((strBufApprove==null)? "strBufApprove is Null":strBufApprove));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			return null;
		}
	}
	@Override
	protected ExceptionCode doUpdateApprRecordForNonTrans(BuildSchedulesVb buildSchedulesVb) throws RuntimeCustomException{
		ExceptionCode exceptionCode = null;
		buildSchedulesVb.setMaker(intCurrentUserId);
		String CalLeBook = removeDescLeBook(buildSchedulesVb.getLeBook());
		int recCount = checkBuildForCtryLeBook(buildSchedulesVb.getBuild(),buildSchedulesVb.getCountry(),CalLeBook);
		if(recCount <= 0){
			strErrorDesc = "Cannot run this build["+buildSchedulesVb.getBuild()+"] for Country["+buildSchedulesVb.getCountry()+"] and LE Book["+CalLeBook+"]";
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		if(!("P".equalsIgnoreCase(buildSchedulesVb.getBuildScheduleStatus()) || "E".equalsIgnoreCase(buildSchedulesVb.getBuildScheduleStatus()))){
			strErrorDesc = "Build must be in Pending or Error Status. " ;
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode); 
		}
		if("P".equalsIgnoreCase(buildSchedulesVb.getBuildScheduleStatus())){
			long lScheduleDate = getDateTimeInMS(buildSchedulesVb.getScheduledDate(),"dd-MM-yyyy HH:mm:ss");
			long lCurrentDate = getDateTimeInMS(getSystemDate(),"dd/MM/yyyy HH:mm:ss");
			if( (lScheduleDate - lCurrentDate) <  (2*60*1000) ){
				strErrorDesc = "Cannot modify build, scheduled to run in less than 2 minutes.";
				exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
				throw buildRuntimeCustomException(exceptionCode);
			}
		}
		buildSchedulesVb.setBuildScheduleStatus("P");
		retVal = doUpdateAppr(buildSchedulesVb);
	    if(retVal != Constants.SUCCESSFUL_OPERATION){
	    	exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
	    }
	    
	    //Delete ALL Detail records before insert.
	    BuildSchedulesDetailsVb lBuildSchedulesDetailsVb = new BuildSchedulesDetailsVb();
	    lBuildSchedulesDetailsVb.setBuildNumber(buildSchedulesVb.getBuildNumber());
	    exceptionCode = getBuildSchedulesDetailsDao().doDeleteApprRecordForNonTrans(lBuildSchedulesDetailsVb);
	    
	    if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
			strErrorDesc = exceptionCode.getErrorMsg();
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode); 
		}
	    //Insert the Detail records
	    for (BuildSchedulesDetailsVb vBCObject: buildSchedulesVb.getBuildSchedulesDetails()) {
	    	vBCObject.setBuildNumber(buildSchedulesVb.getBuildNumber());
	    	exceptionCode = getBuildSchedulesDetailsDao().doInsertApprRecordForNonTrans(vBCObject);
	    	if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
				throw buildRuntimeCustomException(exceptionCode); 
			}
		}
		
		if(retVal != Constants.SUCCESSFUL_OPERATION){
			exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
		return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	@Override
	protected ExceptionCode doInsertApprRecordForNonTrans(BuildSchedulesVb buildSchedulesVb) throws RuntimeCustomException{
		ExceptionCode exceptionCode = null;
		buildSchedulesVb.setMaker(intCurrentUserId);
		String CalLeBook = removeDescLeBook(buildSchedulesVb.getLeBook());
		int recCount = checkBuildForCtryLeBook(buildSchedulesVb.getBuild(),buildSchedulesVb.getCountry(),CalLeBook);
		if(recCount <= 0){
			strErrorDesc = "Cannot run this build["+buildSchedulesVb.getBuild()+"] for Country["+buildSchedulesVb.getCountry()+"] and LE Book["+CalLeBook+"]";
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		Long seq = getBuildControlsDao().getMaxBuildNumber();
		seq++;
		buildSchedulesVb.setBuildNumber(seq+"");
		retVal = doInsertionAppr(buildSchedulesVb);
		
		if(retVal != Constants.SUCCESSFUL_OPERATION){
			exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
		for (BuildSchedulesDetailsVb vBCObject: buildSchedulesVb.getBuildSchedulesDetails()) {
			vBCObject.setBuildNumber(seq+"");
			exceptionCode = getBuildSchedulesDetailsDao().doInsertApprRecordForNonTrans(vBCObject);
			if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
				throw buildRuntimeCustomException(exceptionCode); 
			}
		}
		return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	@Override
	protected ExceptionCode doDeleteApprRecordForNonTrans(BuildSchedulesVb vObject) throws RuntimeCustomException {
		ExceptionCode exceptionCode = null;
		BuildSchedulesVb vObjectlocal = null;
		vObjectlocal = getQueryResultsForDetails(vObject.getBuildNumber(), vObject.getBuild());
		if (vObjectlocal == null){
			exceptionCode = getResultObject(Constants.ATTEMPT_TO_DELETE_UNEXISTING_RECORD);
			throw buildRuntimeCustomException(exceptionCode);
		}
		long lScheduleDate = getDateTimeInMS(vObjectlocal.getScheduledDate(),"dd-MM-yyyy HH:mm:ss");
		long lCurrentDate = getDateTimeInMS(getSystemDate(),"dd/MM/yyyy HH:mm:ss");
		if( !"E".equalsIgnoreCase(vObjectlocal.getBuildScheduleStatus()) && !"K".equalsIgnoreCase(vObjectlocal.getBuildScheduleStatus())
				&& (lScheduleDate - lCurrentDate) <  (2*60*1000)){
			strErrorDesc = "Cannot delete build, scheduled to run in less than 2 minutes.";
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode); 
		}
		BuildSchedulesDetailsVb lBuildSchedulesDetailsVb = new BuildSchedulesDetailsVb();
		lBuildSchedulesDetailsVb.setBuildNumber(vObject.getBuildNumber());
		
		exceptionCode = getBuildControlsDao().updateBuildControls(lBuildSchedulesDetailsVb);
		if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
			throw buildRuntimeCustomException(exceptionCode); 
		}
		ExceptionCode lResultObject = getBuildSchedulesDetailsDao().doDeleteApprRecordForNonTrans(lBuildSchedulesDetailsVb);
		if(lResultObject.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
			strErrorDesc = lResultObject.getErrorMsg();
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		retVal = doDeleteAppr(vObjectlocal);
		if(retVal != Constants.SUCCESSFUL_OPERATION){
			strErrorDesc = "Unable to delete the record.";
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	
	public ExceptionCode reInitiate(List<BuildSchedulesVb> vObjects){
		BuildSchedulesVb vObjectlocal = null;
		setServiceDefaults();
		ExceptionCode exceptionCode = null;
		strCurrentOperation = "Re-Initiate";
		try
		{
			
			for(BuildSchedulesVb vObject : vObjects){
				if(vObject.isChecked()){
					// check to see if the record already exists in the approved table
					vObjectlocal = getQueryResultsForDetails(vObject.getBuildNumber(),vObject.getBuild());

					// If records are there, check for the status and decide what error to return back
					if (vObjectlocal == null){
						exceptionCode = getResultObject(Constants.ATTEMPT_TO_DELETE_UNEXISTING_RECORD);
						throw buildRuntimeCustomException(exceptionCode);
					}	
					if(!("E".equalsIgnoreCase(vObjectlocal.getBuildScheduleStatus()) || "K".equalsIgnoreCase(vObjectlocal.getBuildScheduleStatus()))){
						strErrorDesc = "You can only Re-Initiate builds with status Errored or Terminated.";
						exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
						throw buildRuntimeCustomException(exceptionCode);
					}
					//First run the build Control Query
					BuildSchedulesDetailsVb lBuildSchedulesDetailsVb = new BuildSchedulesDetailsVb();
					lBuildSchedulesDetailsVb.setBuildNumber(vObject.getBuildNumber());
					exceptionCode = getBuildControlsDao().updateBuildControls(lBuildSchedulesDetailsVb);
					if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						throw buildRuntimeCustomException(exceptionCode); 
					}
					lBuildSchedulesDetailsVb = new BuildSchedulesDetailsVb();
					lBuildSchedulesDetailsVb.setBuildNumber(vObject.getBuildNumber());
					exceptionCode = getBuildSchedulesDetailsDao().updateStatusOfNonCompletedBuilds(lBuildSchedulesDetailsVb);
					if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						throw buildRuntimeCustomException(exceptionCode); 
					}
					vObjectlocal.setSubmitterId((int)intCurrentUserId);
					vObjectlocal.setBuildScheduleStatus("P");
					retVal = updateBuildSchedulesStatus(vObjectlocal);
					if(retVal != Constants.SUCCESSFUL_OPERATION){
						strErrorDesc = "Unable to reset the record status to 'P'.";
						exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
						throw buildRuntimeCustomException(exceptionCode);
					}
				}
			}
			return getResultObject(Constants.SUCCESSFUL_OPERATION);
		}catch(Exception ex){
			strErrorDesc = ex.getMessage().trim();
			ex.printStackTrace();
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
	}
	private long getDateTimeInMS(String date, String formate){
		DateFormat lFormat = new SimpleDateFormat(formate);
		try {
			Date lDate = lFormat.parse(date);
			return lDate.getTime();
		} catch (Exception e) {
			return System.currentTimeMillis();
		}
	}
	private int checkBuildForCtryLeBook(String buildName,String country,String leBook) // Build Controls Details
	{
		final int intKeyFieldsCount = 3;
		String query = new String("SELECT count(1) FROM BUILD_CONTROLS WHERE BUILD = ? AND COUNTRY = ? AND LE_BOOK = ?"); 
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = buildName; 
		params[1] = country;
		params[2] = leBook; 
		try{
			return  getJdbcTemplate().queryForObject(query,params, Integer.class);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			return 0;
		}
	}
	public int checkExpandFlagFor(BuildSchedulesVb vObject){
		final int intKeyFieldsCount = 1;
		String query = new String("select count(1) from BUILD_SCHEDULES_DETAILS BSD JOIN BUILD_CONTROLS  BC ON ASSOCIATED_BUILD = BUILD AND " +
				"BC.BUILD_MODULE=BSD.BUILD_MODULE WHERE BSD.BUILD_NUMBER=? AND EXPAND_FLAG = 'Y'"); 
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = vObject.getBuildNumber(); 
		try{
			return  getJdbcTemplate().queryForObject(query,params, Integer.class);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			return 0;
		}
	}
	@Override
	protected int doInsertionAppr(BuildSchedulesVb vObject){
		String CalLeBook = removeDescLeBook(vObject.getLeBook());
		String query = "Insert Into BUILD_SCHEDULES ( BUILD, SCHEDULED_DATE, BUILD_NUMBER, PARALLEL_PROCS_COUNT,"+
			"NOTIFY, SUPPORT_CONTACT, SUBMITTER_ID, START_TIME, END_TIME, BUILD_SCHEDULE_STATUS_AT, "+
			"BUILD_SCHEDULE_STATUS, DATE_LAST_MODIFIED, DATE_CREATION, COUNTRY, LE_BOOK, RECURRING_FREQUENCY_AT,"+
			"RECURRING_FREQUENCY, NODE_REQUEST, BUSINESS_DATE, FEED_DATE) Values (?, To_Date(?, 'DD-MM-YYYY HH24:MI:SS'), ?, ?, ?, ?, ?, ?, ?, ?, ?, "+
			"SysDate, SysDate, ?, ?, ?, ?, ?, To_Date(?, 'DD-MM-YYYY'), To_Date(?, 'DD-MM-YYYY'))";
		Object[] args = {vObject.getBuild(), vObject.getScheduledDate(), vObject.getBuildNumber(),
			vObject.getParallelProcsCount(), vObject.getNotify(), vObject.getSupportContact(),
			vObject.getSubmitterId(),vObject.getStartTime(),vObject.getEndTime(),vObject.getBuildScheduleStatusAt(),
			vObject.getBuildScheduleStatus(),vObject.getCountry(),CalLeBook,vObject.getRecurringFrequencyAt(),
			vObject.getRecurringFrequency(),vObject.getNode(),vObject.getBusinessDate(),vObject.getFeedDate()};
		return getJdbcTemplate().update(query,args);
	}
	@Override
	protected int doUpdateAppr(BuildSchedulesVb vObject){
		String CalLeBook = removeDescLeBook(vObject.getLeBook());
		String query = "Update BUILD_SCHEDULES Set SCHEDULED_DATE = To_Date(?, 'DD-MM-YYYY HH24:MI:SS'),"+
			"PARALLEL_PROCS_COUNT = ?, NOTIFY = ?, SUPPORT_CONTACT = ?, SUBMITTER_ID = ?, START_TIME = NULL,"+
			"END_TIME = NULL, BUILD_SCHEDULE_STATUS = ?, DATE_LAST_MODIFIED = SysDate, COUNTRY = ?, LE_BOOK = ?,"+
			"RECURRING_FREQUENCY = ?, NODE_REQUEST = ?, BUSINESS_DATE = To_Date(?, 'DD-MM-YYYY'), FEED_DATE = = To_Date(?, 'DD-MM-YYYY') Where BUILD = ? AND BUILD_NUMBER = ?";
		Object[] args = {vObject.getScheduledDate(), vObject.getParallelProcsCount(), vObject.getNotify(), vObject.getSupportContact(),
			vObject.getSubmitterId(), vObject.getBuildScheduleStatus(), vObject.getCountry(), CalLeBook, 
			vObject.getRecurringFrequency(), vObject.getNode(), vObject.getBusinessDate(), vObject.getFeedDate(), vObject.getBuild(), vObject.getBuildNumber()};
		return getJdbcTemplate().update(query,args);
	}
	private int updateBuildSchedulesStatus(BuildSchedulesVb buildSchedulesDetailsVb){
		final int intKeyFieldsCount = 4;
		String query = new String("Update BUILD_SCHEDULES Set SUBMITTER_ID = ?, BUILD_SCHEDULE_STATUS = ?, DATE_LAST_MODIFIED = SysDate "+
				"Where BUILD = ? AND BUILD_NUMBER = ?");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = buildSchedulesDetailsVb.getSubmitterId();
		params[1] = buildSchedulesDetailsVb.getBuildScheduleStatus();
		params[2] = buildSchedulesDetailsVb.getBuild();
		params[3] = buildSchedulesDetailsVb.getBuildNumber();
		try
		{
			return getJdbcTemplate().update(query,params);
		}catch(Exception ex){
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			logger.error(ex.getMessage(), ex);
			return 0;
		}
		
	}
	private int updateBuildSchedulesNode(BuildSchedulesVb buildSchedulesDetailsVb){
		final int intKeyFieldsCount = 5;
		String query = new String("Update BUILD_SCHEDULES Set SUBMITTER_ID = ?,NODE_OVERRIDE = ?, BUILD_SCHEDULE_STATUS = ?, DATE_LAST_MODIFIED = SysDate "+
				"Where BUILD = ? AND BUILD_NUMBER = ?");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = buildSchedulesDetailsVb.getSubmitterId();
		params[1] = buildSchedulesDetailsVb.getNode();
		params[2] = buildSchedulesDetailsVb.getBuildScheduleStatus();
		params[3] = buildSchedulesDetailsVb.getBuild();
		params[4] = buildSchedulesDetailsVb.getBuildNumber();
		try
		{
			return getJdbcTemplate().update(query,params);
		}catch(Exception ex){
			logger.error(((query==null)? "query is Null":query));
			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i].toString());
			logger.error(ex.getMessage(), ex);
			return 0;
		}
		
	}
	@Override
	protected int doDeleteAppr(BuildSchedulesVb vObject){
		String query = "Delete From BUILD_SCHEDULES Where BUILD = ? AND BUILD_NUMBER = ?";
		Object[] args = {vObject.getBuild(),vObject.getBuildNumber()};
		return getJdbcTemplate().update(query,args);
	}
	public BuildControlsDao getBuildControlsDao() {
		return buildControlsDao;
	}

	public void setBuildControlsDao(BuildControlsDao buildControlsDao) {
		this.buildControlsDao = buildControlsDao;
	}

	public BuildSchedulesDetailsDao getBuildSchedulesDetailsDao() {
		return buildSchedulesDetailsDao;
	}

	public void setBuildSchedulesDetailsDao(
			BuildSchedulesDetailsDao buildSchedulesDetailsDao) {
		this.buildSchedulesDetailsDao = buildSchedulesDetailsDao;
	}
	public long findCronCount(String environmentVariable){
		String sql="SELECT COUNT(1) FROM VISION_VARIABLES WHERE VARIABLE IN ( " +
				" SELECT 'BUILD_CRON_'||SERVER_NAME||'_'||NODE_NAME FROM VISION_NODE_CREDENTIALS WHERE SERVER_ENVIRONMENT='"+environmentVariable+"' AND NODE_STATUS=0)";
		return getJdbcTemplate().queryForObject(sql, Long.class);
	}
	
	public List<BuildSchedulesVb> findCronName(BuildSchedulesVb dObj,String enironmentVariable){
		List<BuildSchedulesVb> collTemp = null;
		final int intKeyFieldsCount = 1;
		String trimmedLeBook=removeDescLeBook(dObj.getLeBook());
		String strQueryAppr = new String("SELECT (substr(variable,12)) AS NAME FROM VISION_VARIABLES WHERE VARIABLE IN (  " +
				" SELECT 'BUILD_CRON_'||SERVER_NAME||'_'||NODE_NAME FROM VISION_NODE_CREDENTIALS WHERE SERVER_ENVIRONMENT=? AND NODE_STATUS=0)");

		Object objParams[] = new Object[intKeyFieldsCount];
		objParams[0] = enironmentVariable;
		try
		{
			logger.info("Executing approved query");
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					BuildSchedulesVb buildSchedulesVb = new BuildSchedulesVb();
					buildSchedulesVb.setCronName(rs.getString("NAME"));
					return buildSchedulesVb;
				}
			};
			collTemp = getJdbcTemplate().query(strQueryAppr.toString(),objParams, mapper);
			
			return collTemp;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Error: getQueryResults Exception :   ");
			logger.error(((strQueryAppr == null) ? "strQueryAppr is Null" : strQueryAppr.toString()));

			if (objParams != null)
				for(int i=0 ; i< objParams.length; i++)
					logger.error("objParams[" + i + "]" + objParams[i].toString());
			return null;
		}
	}
	public List<BuildSchedulesVb> findCronStatus(BuildSchedulesVb dObj,String enironmentVariable){
		List<BuildSchedulesVb> collTemp = null;
		final int intKeyFieldsCount = 1;
		String trimmedLeBook=removeDescLeBook(dObj.getLeBook());
		String strQueryAppr = new String("SELECT VALUE AS STATUS FROM VISION_VARIABLES WHERE VARIABLE IN ( " +
				" SELECT 'BUILD_CRON_'||SERVER_NAME||'_'||NODE_NAME FROM VISION_NODE_CREDENTIALS WHERE SERVER_ENVIRONMENT=? AND NODE_STATUS=0)");

		Object objParams[] = new Object[intKeyFieldsCount];
		objParams[0] = enironmentVariable;
		try
		{
			logger.info("Executing approved query");
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					BuildSchedulesVb buildSchedulesVb = new BuildSchedulesVb();
					buildSchedulesVb.setCronStatus(rs.getString("STATUS"));
					return buildSchedulesVb;
				}
			};
			collTemp = getJdbcTemplate().query(strQueryAppr.toString(),objParams, mapper);
			
			return collTemp;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Error: getQueryResults Exception :   ");
			logger.error(((strQueryAppr == null) ? "strQueryAppr is Null" : strQueryAppr.toString()));

			if (objParams != null)
				for(int i=0 ; i< objParams.length; i++)
					logger.error("objParams[" + i + "]" + objParams[i].toString());
			return null;
		}
	}
	/*public String getAccurateHostName(String enironmentVariable, String node) {
		String sql = "SELECT VALUE FROM VISION_VARIABLES WHERE VARIABLE IN ( " +
				"SELECT 'BUILD_CRON_'||SERVER_NAME||'_'||NODE_NAME FROM VISION_NODE_CREDENTIALS WHERE SERVER_ENVIRONMENT=? AND NODE_STATUS=0)";
		 String sql = "SELECT NODE_IP FROM VISION_NODE_CREDENTIALS WHERE SERVER_ENVIRONMENT='"+enironmentVariable+"' AND NODE_NAME='"+node+"' AND ROWNUM<2";
		 return getJdbcTemplate().queryForObject(sql, String.class);
	}*/
	
	public ExceptionCode transfer(List<BuildSchedulesVb> vObjects, String updateNode){
		BuildSchedulesVb vObjectlocal = null;
		setServiceDefaults();
		ExceptionCode exceptionCode = null;
		strCurrentOperation = "Transfer";
		try
		{
			for(BuildSchedulesVb vObject : vObjects){
				if(vObject.isChecked()){
					// check to see if the record already exists in the approved table
					vObjectlocal = getQueryResultsForDetails(vObject.getBuildNumber(),vObject.getBuild());

					// If records are there, check for the status and decide what error to return back
					if (vObjectlocal == null){
						exceptionCode = getResultObject(Constants.ATTEMPT_TO_DELETE_UNEXISTING_RECORD);
						throw buildRuntimeCustomException(exceptionCode);
					}
					if(!("E".equalsIgnoreCase(vObjectlocal.getBuildScheduleStatus()) || "K".equalsIgnoreCase(vObjectlocal.getBuildScheduleStatus()))){
						strErrorDesc = "You can only Transfer builds with status Errored or Terminated.";
						exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
						throw buildRuntimeCustomException(exceptionCode);
					}
					//First run the build Control Query
					BuildSchedulesDetailsVb lBuildSchedulesDetailsVb = new BuildSchedulesDetailsVb();
					lBuildSchedulesDetailsVb.setBuildNumber(vObject.getBuildNumber());
					exceptionCode = getBuildControlsDao().updateBuildControls(lBuildSchedulesDetailsVb);
					if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						throw buildRuntimeCustomException(exceptionCode); 
					}
					lBuildSchedulesDetailsVb = new BuildSchedulesDetailsVb();
					lBuildSchedulesDetailsVb.setBuildNumber(vObject.getBuildNumber());
					exceptionCode = getBuildSchedulesDetailsDao().updateStatusOfNonCompletedBuilds(lBuildSchedulesDetailsVb);
					if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						throw buildRuntimeCustomException(exceptionCode); 
					}
					vObjectlocal.setSubmitterId((int)intCurrentUserId);
					vObjectlocal.setBuildScheduleStatus("P");
					vObjectlocal.setNode(updateNode);
					
					retVal = updateBuildSchedulesNode(vObjectlocal);
					if(retVal != Constants.SUCCESSFUL_OPERATION){
						strErrorDesc = "Unable to reset the record status to 'P'.";
						exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
						throw buildRuntimeCustomException(exceptionCode);
					}
				}
			}
			return getResultObject(Constants.SUCCESSFUL_OPERATION);
		}catch(Exception ex){
			strErrorDesc = ex.getMessage().trim();
			ex.printStackTrace();
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
	}
	
	/*public  String  getDateForBusinessFeedDate(String Country,String fullLeBook, String majorBuild){
		String CalLeBook = removeDescLeBook(fullLeBook);
		
		String query="select  "+
						" Case When '"+majorBuild+"' = 'ACQMTHFEED' "+
						" Then LAST_DAY(TO_DATE(VBD.BUSINESS_YEAR_MONTH,'RRRRMM')) "+ 
						" Else VBD.Business_Date End "+
						" From VISION_BUSINESS_DAY VBD "+
						" where VBD.Le_Book = '"+CalLeBook+"'";
		String query="SELECT "+ 
					 " CASE WHEN X1.Frequency_Process = 'DLY' THEN TO_CHAR(X7.BUSINESS_DATE, 'DD-MM-RRRR') "+ 
		             " WHEN X1.Frequency_Process = 'MTH' THEN TO_CHAR(LAST_DAY(TO_DATE(X7.BUSINESS_YEAR_MONTH,'RRRRMM')), 'DD-MM-RRRR') "+ 
		             " WHEN X1.Frequency_Process = 'WKY' THEN TO_CHAR(X7.BUSINESS_WEEK_DATE, 'DD-MM-RRRR') "+ 
		             " WHEN X1.Frequency_Process = 'QTR' THEN TO_CHAR(LAST_DAY(TO_DATE(X7.BUSINESS_QTR_YEAR_MONTH,'RRRRMM')), 'DD-MM-RRRR') "+ 
		             " WHEN X1.Frequency_Process = 'HYR' THEN TO_CHAR(LAST_DAY(TO_DATE(X7.BUSINESS_HYR_YEAR_MONTH,'RRRRMM')), 'DD-MM-RRRR') "+ 
		             " WHEN X1.Frequency_Process = 'ANN' THEN TO_CHAR(TO_DATE('31-DEC-'||To_Char(X7.BUSINESS_YEAR),'DD-MON-RRRR'), 'DD-MM-RRRR') "+ 
		             " END BUSINESS_DATE, FREQUENCY_PROCESS  FROM VISION_BUSINESS_DAY X7 ,(Select NVL(MIN(T1.Frequency_Process),'DLY') Frequency_Process "+
		             " From ADF_Data_Acquisition T1 Where T1.Country = '"+Country+"' And T1.Le_Book = '"+CalLeBook+"' And NVL(T1.Major_Build,'ACQ'||T1.Frequency_Process||'FEED') = '"+majorBuild+"') X1 "+
		             " WHERE X7.COUNTRY = '"+Country+"' And X7.LE_BOOK = '"+majorBuild+"'";
		
		try{
			return getJdbcTemplate().queryForObject(query, String.class);
		}catch(Exception ex){
			ex.printStackTrace();
			return "";
		}
	}*/
	public List<BuildSchedulesVb> getDateForBusinessFeedDate(String Country,String fullLeBook, String majorBuild){
		List<BuildSchedulesVb> collTemp = null;
		String CalLeBook = removeDescLeBook(fullLeBook);
		final int intKeyFieldsCount = 1;
		
/*		String query="SELECT "+ 
				 " CASE WHEN X1.Frequency_Process = 'DLY' THEN TO_CHAR(X7.BUSINESS_DATE, 'DD-MM-RRRR') "+ 
	             " WHEN X1.Frequency_Process = 'MTH' THEN TO_CHAR(LAST_DAY(TO_DATE(X7.BUSINESS_YEAR_MONTH,'RRRRMM')), 'DD-MM-RRRR') "+ 
	             " WHEN X1.Frequency_Process = 'WKY' THEN TO_CHAR(X7.BUSINESS_WEEK_DATE, 'DD-MM-RRRR') "+ 
	             " WHEN X1.Frequency_Process = 'QTR' THEN TO_CHAR(LAST_DAY(TO_DATE(X7.BUSINESS_QTR_YEAR_MONTH,'RRRRMM')), 'DD-MM-RRRR') "+ 
	             //" WHEN X1.Frequency_Process = 'HYR' THEN TO_CHAR(LAST_DAY(TO_DATE(X7.BUSINESS_HYR_YEAR_MONTH,'RRRRMM')), 'DD-MM-RRRR') "+ 
	             //" WHEN X1.Frequency_Process = 'ANN' THEN TO_CHAR(TO_DATE('31-DEC-'||To_Char(X7.BUSINESS_YEAR),'DD-MON-RRRR'), 'DD-MM-RRRR') "+ 
	             " END BUSINESS_DATE, FREQUENCY_PROCESS  FROM VISION_BUSINESS_DAY X7 ,(Select NVL(MIN(T1.Frequency_Process),'DLY') Frequency_Process "+
	             " From ADF_Data_Acquisition T1 Where T1.Country = '"+Country+"' And T1.Le_Book = '"+CalLeBook+"' And NVL(T1.Major_Build,'ACQ'||T1.Frequency_Process||'FEED') = '"+majorBuild+"') X1 "+
	             " WHERE X7.COUNTRY = '"+Country+"' And X7.LE_BOOK = '"+CalLeBook+"'";*/
		String query="SELECT CASE "
				+ " WHEN X10.DATE_SELECTION = 'CM' "
				+ " THEN "
				+ " TO_CHAR (X8.CM, 'DD-MM-RRRR') "
				+ " ELSE "
				+ " TO_CHAR (X7.BUSINESS_DATE, 'DD-MM-RRRR') "
				+ " END "
				+ " BUSINESS_DATE "
				+ " FROM VISION_BUSINESS_DAY X7,"
				+ " (select last_day(to_date(x1.value||lpad(x2.value,2,'0'),'RRRRMM')) CM from vision_variables x1,"
				+ " vision_variables x2 where x1.variable = 'CURRENT_YEAR' and x2.variable = 'CURRENT_MONTH') X8,"
				+ " PROGRAMS X10"
				+ " WHERE X7.COUNTRY = '"+Country+"' AND X7.LE_BOOK = '"+CalLeBook+"'"
				+ " AND X10.program = '"+majorBuild+"'";		

/*		Object objParams[] = new Object[intKeyFieldsCount];
		objParams[0] = enironmentVariable;*/
		try
		{
			logger.info("Executing approved query");
			RowMapper mapper = new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					BuildSchedulesVb buildSchedulesVb = new BuildSchedulesVb();
					buildSchedulesVb.setBusinessDate(rs.getString("BUSINESS_DATE"));
					//buildSchedulesVb.setFrequencyProcess(rs.getString("FREQUENCY_PROCESS"));;
					return buildSchedulesVb;
				}
			};
			collTemp = getJdbcTemplate().query(query.toString(), mapper);
			
			return collTemp;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Error: getQueryResults Exception :   ");
			logger.error(((query == null) ? "strQueryAppr is Null" : query.toString()));
			return null;
		}
	}	
}
