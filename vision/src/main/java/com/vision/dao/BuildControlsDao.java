package com.vision.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.springframework.jdbc.core.RowMapper;

import com.vision.authentication.SessionContextHolder;
import com.vision.exception.ExceptionCode;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.BuildControlsVb;
import com.vision.vb.BuildSchedulesDetailsVb;
import com.vision.vb.BuildSchedulesVb;

public class BuildControlsDao extends AbstractDao<BuildControlsVb> {
	public RowMapper getQueryPopupMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				BuildControlsVb buildControlsVb = new BuildControlsVb();
				buildControlsVb.setCountry(rs.getString("COUNTRY"));
				buildControlsVb.setLeBook(rs.getString("LE_BOOK"));
				buildControlsVb.setBuild(rs.getString("BUILD"));
/*				buildControlsVb.setSubBuildNumber(rs.getString("SUB_BUILD_NUMBER"));
				buildControlsVb.setBcSequence(rs.getString("BC_SEQUENCE"));
				buildControlsVb.setBuildModule(rs.getString("BUILD_MODULE"));*/
				return buildControlsVb;
			}
		};
		return mapper;
	}
	@Override
	protected RowMapper getMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				BuildControlsVb buildControlsVb = new BuildControlsVb();
				buildControlsVb.setCountry(rs.getString("COUNTRY"));
				buildControlsVb.setLeBook(rs.getString("LE_BOOK"));
				buildControlsVb.setBuild(rs.getString("BUILD"));
				buildControlsVb.setSubBuildNumber(rs.getString("SUB_BUILD_NUMBER"));
				buildControlsVb.setBcSequence(rs.getString("BC_SEQUENCE"));
				buildControlsVb.setBuildModule(rs.getString("BUILD_MODULE"));
				buildControlsVb.setRunItAt(rs.getInt("RUN_IT_AT"));
				buildControlsVb.setRunIt(rs.getString("RUN_IT"));
				buildControlsVb.setLastStartDate(rs.getString("LAST_START_DATE"));
				buildControlsVb.setLastBuildDate(rs.getString("LAST_BUILD_DATE"));
				buildControlsVb.setBuildControlsStatusAt(rs.getInt("BUILD_CONTROLS_STATUS_AT"));
				buildControlsVb.setBuildControlsStatus(rs.getString("BUILD_CONTROLS_STATUS"));
				buildControlsVb.setSubmitterId(rs.getString("SUBMITTER_ID"));
				buildControlsVb.setBuildNumber(rs.getString("BUILD_NUMBER"));
				buildControlsVb.setExpandFlag(rs.getString("EXPAND_FLAG"));
				buildControlsVb.setRecordIndicatorNt(rs.getInt("RECORD_INDICATOR_NT"));
				buildControlsVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
				buildControlsVb.setMaker(rs.getInt("MAKER"));
				buildControlsVb.setVerifier(rs.getInt("VERIFIER"));
				buildControlsVb.setInternalStatus(rs.getInt("INTERNAL_STATUS"));
				buildControlsVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				buildControlsVb.setDateCreation(rs.getString("DATE_CREATION"));
				return buildControlsVb;
			}
		};
		return mapper;
	}

	public List<BuildControlsVb> getQueryPopupResults(BuildControlsVb dObj){
		Vector<Object> params = new Vector<Object>();
		String CalLeBook = removeDescLeBook(dObj.getLeBook());
		StringBuffer strBufApprove = new StringBuffer("Select Distinct TAppr.COUNTRY, "
				+ "TAppr.LE_BOOK||' - '||(Select Leb_description from LE_Book where LE_Book.LE_Book = TAppr.LE_Book and LE_Book.Country = TAppr.Country) as LE_BOOK," 
				+ " TAppr.BUILD From BUILD_CONTROLS TAppr ");
		try
		{
			if (ValidationUtil.isValid(dObj.getCountry()))
			{
				params.addElement("%" + dObj.getCountry().toUpperCase() + "%" );
				CommonUtils.addToQuery("UPPER(TAppr.COUNTRY) like ?", strBufApprove);
			}
			if (ValidationUtil.isValid(CalLeBook))
			{
				params.addElement("%" + CalLeBook.toUpperCase() + "%" );
				CommonUtils.addToQuery("UPPER(TAppr.LE_BOOK) like ?", strBufApprove);
			}
			if (ValidationUtil.isValid(SessionContextHolder.getContext().getUpdateRestrictionLeBook()))
			{
				CommonUtils.addToQuery("TAppr.COUNTRY||'-'||TAppr.LE_BOOK IN("+SessionContextHolder.getContext().getUpdateRestrictionLeBook().toUpperCase()+") ", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getBuild()))
			{
				params.addElement("%" + dObj.getBuild().toUpperCase() + "%" );
				CommonUtils.addToQuery("UPPER(TAppr.BUILD) like ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getSubBuildNumber()))
			{
				params.addElement(dObj.getSubBuildNumber());
				CommonUtils.addToQuery("TAppr.SUB_BUILD_NUMBER = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getBcSequence()))
			{
				params.addElement(dObj.getBcSequence());
				CommonUtils.addToQuery("TAppr.BC_SEQUENCE = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getBuildModule()))
			{
				params.addElement("%" + dObj.getBuildModule().toUpperCase() + "%" );
				CommonUtils.addToQuery("UPPER(TAppr.BUILD_MODULE) like ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getRunIt()) && !"-1".equalsIgnoreCase(dObj.getRunIt()))
			{
				params.addElement(dObj.getRunIt());
				CommonUtils.addToQuery("UPPER(TAppr.RUN_IT) like ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getLastStartDate()))
			{
				params.addElement(dObj.getLastStartDate());
				CommonUtils.addToQuery("TAppr.LAST_START_DATE = To_Date(?, 'DD-MM-YYYY')", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getLastBuildDate()))
			{
				params.addElement(dObj.getLastBuildDate());
				CommonUtils.addToQuery("TAppr.LAST_BUILD_DATE = To_Date(?, 'DD-MM-YYYY')", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getBuildControlsStatus()) && !"-1".equalsIgnoreCase(dObj.getBuildControlsStatus()))
			{
				params.addElement(dObj.getBuildControlsStatus());
				CommonUtils.addToQuery("TAppr.BUILD_CONTROLS_STATUS = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getBuildNumber()))
			{
				params.addElement(dObj.getBuildNumber());
				CommonUtils.addToQuery("TAppr.BUILD_NUMBER = ?", strBufApprove);
			}
			//check if the column [RECORD_INDICATOR] should be included in the query
			if (dObj.getRecordIndicator() != -1){
				if (dObj.getRecordIndicator() > 3){
					params.addElement(new Integer(0));
					CommonUtils.addToQuery("TAppr.RECORD_INDICATOR > ?", strBufApprove);
				}else{
					params.addElement(new Integer(dObj.getRecordIndicator()));
					CommonUtils.addToQuery("TAppr.RECORD_INDICATOR = ?", strBufApprove);
				}
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			logger.error(((strBufApprove==null)? "strBufApprove is null":strBufApprove.toString()));

			if (params != null)
				for(int i=0 ; i< params.size(); i++)
					logger.error("objParams[" + i + "]" + params.get(i).toString());
			return null;
		}

		String orderBy = " Order By COUNTRY, LE_BOOK, BUILD";
		return getQueryPopupResults(dObj,new StringBuffer(), strBufApprove, new String(), orderBy, params, getQueryPopupMapper());
	}

	public List<BuildControlsVb> getQueryResults(BuildControlsVb dObj, int intStatus){
		setServiceDefaults();
		Vector<Object> params = new Vector<Object>();
		String CalLeBook = removeDescLeBook(dObj.getLeBook());
		StringBuffer strBufApprove = new StringBuffer("Select TAppr.COUNTRY,TAppr.LE_BOOK, " + 
			"TAppr.BUILD,TAppr.SUB_BUILD_NUMBER,TAppr.BC_SEQUENCE,TAppr.BUILD_MODULE,TAppr.RUN_IT_AT, " + 
			"TAppr.RUN_IT,To_Char(TAppr.LAST_START_DATE, 'DD-MM-YYYY') LAST_START_DATE,To_Char(TAppr.LAST_BUILD_DATE, " + 
			" 'DD-MM-YYYY') LAST_BUILD_DATE,TAppr.BUILD_CONTROLS_STATUS_AT,TAppr.BUILD_CONTROLS_STATUS, " + 
			"TAppr.SUBMITTER_ID,TAppr.BUILD_NUMBER,TAppr.EXPAND_FLAG,TAppr.RECORD_INDICATOR_NT, " + 
			"TAppr.RECORD_INDICATOR,TAppr.MAKER,TAppr.VERIFIER,TAppr.INTERNAL_STATUS,To_Char(TAppr.DATE_LAST_MODIFIED, " + 
			" 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED,To_Char(TAppr.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION From BUILD_CONTROLS TAppr ");

		try
		{
			if (ValidationUtil.isValid(dObj.getCountry()))
			{
				params.addElement(dObj.getCountry());
				CommonUtils.addToQuery("TAppr.COUNTRY = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(CalLeBook))
			{
				params.addElement(CalLeBook);
				CommonUtils.addToQuery("TAppr.LE_BOOK = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getBuild()))
			{
				params.addElement(dObj.getBuild());
				CommonUtils.addToQuery("TAppr.BUILD = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getSubBuildNumber()))
			{
				params.addElement(dObj.getSubBuildNumber());
				CommonUtils.addToQuery("TAppr.SUB_BUILD_NUMBER = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getBcSequence()))
			{
				params.addElement(dObj.getBcSequence());
				CommonUtils.addToQuery("TAppr.BC_SEQUENCE = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getBuildModule()))
			{
				params.addElement("%" + dObj.getBuildModule().toUpperCase() + "%" );
				CommonUtils.addToQuery("UPPER(TAppr.BUILD_MODULE) like ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getRunIt()) && !"-1".equalsIgnoreCase(dObj.getRunIt()))
			{
				params.addElement(dObj.getRunIt());
				CommonUtils.addToQuery("TAppr.RUN_IT = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getLastStartDate()))
			{
				params.addElement(dObj.getLastStartDate());
				CommonUtils.addToQuery("TAppr.LAST_START_DATE = To_Date(?, 'DD-MM-YYYY')", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getLastBuildDate()))
			{
				params.addElement(dObj.getLastBuildDate());
				CommonUtils.addToQuery("TAppr.LAST_BUILD_DATE = To_Date(?, 'DD-MM-YYYY')", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getBuildControlsStatus()) && !"-1".equalsIgnoreCase(dObj.getBuildControlsStatus()))
			{
				params.addElement(dObj.getBuildControlsStatus());
				CommonUtils.addToQuery("TAppr.BUILD_CONTROLS_STATUS = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getSubmitterId()))
			{
				params.addElement(dObj.getSubmitterId());
				CommonUtils.addToQuery("TAppr.SUBMITTER_ID = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getBuildNumber()))
			{
				params.addElement(dObj.getBuildNumber());
				CommonUtils.addToQuery("TAppr.BUILD_NUMBER = ?", strBufApprove);
			}
/*			if (ValidationUtil.isValid(dObj.getExpandFlag()))
			{
				params.addElement("%" + dObj.getExpandFlag().toUpperCase() + "%" );
				CommonUtils.addToQuery("UPPER(TAppr.EXPAND_FLAG) like ?", strBufApprove);
			}
*/			//check if the column [RECORD_INDICATOR] should be included in the query
			if (dObj.getRecordIndicator() != -1){
				if (dObj.getRecordIndicator() > 3){
					params.addElement(new Integer(0));
					CommonUtils.addToQuery("TAppr.RECORD_INDICATOR > ?", strBufApprove);
				}else{
					params.addElement(new Integer(dObj.getRecordIndicator()));
					CommonUtils.addToQuery("TAppr.RECORD_INDICATOR = ?", strBufApprove);
				}
			}

			String orderBy = " Order By COUNTRY, LE_BOOK, BUILD, SUB_BUILD_NUMBER, BC_SEQUENCE";
			return getQueryPopupResults(dObj,new StringBuffer(), strBufApprove, new String(), orderBy, params);
		}
		catch(Exception ex){
			ex.printStackTrace();
			logger.error(((strBufApprove==null)? "strBufApprove is null":strBufApprove.toString()));

			if (params != null)
				for(int i=0 ; i< params.size(); i++)
					logger.error("objParams[" + i + "]" + params.get(i).toString());
			return null;
		}
	}

	public List<BuildControlsVb> getQueryResultsForReview(BuildControlsVb dObj, int intStatus){

		List<BuildControlsVb> collTemp = null;
		final int intKeyFieldsCount = 5;
		String CalLeBook = removeDescLeBook(dObj.getLeBook());
		StringBuffer strQueryAppr = new StringBuffer("Select TAppr.COUNTRY,TAppr.LE_BOOK, " + 
			"TAppr.BUILD,TAppr.SUB_BUILD_NUMBER,TAppr.BC_SEQUENCE,TAppr.BUILD_MODULE,TAppr.RUN_IT_AT, " + 
			"TAppr.RUN_IT,To_Char(TAppr.LAST_START_DATE, 'DD-MM-YYYY') LAST_START_DATE," +
			"To_Char(TAppr.LAST_BUILD_DATE, 'DD-MM-YYYY') LAST_BUILD_DATE," +
			"TAppr.BUILD_CONTROLS_STATUS_AT,TAppr.BUILD_CONTROLS_STATUS, " + 
			"TAppr.SUBMITTER_ID,TAppr.BUILD_NUMBER,TAppr.EXPAND_FLAG,TAppr.RECORD_INDICATOR_NT, " + 
			"TAppr.RECORD_INDICATOR,TAppr.MAKER,TAppr.VERIFIER,TAppr.INTERNAL_STATUS,To_Char(TAppr.DATE_LAST_MODIFIED, " + 
			" 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED," +
			"To_Char(TAppr.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION" +
			" From BUILD_CONTROLS TAppr " + 
			"Where TAppr.COUNTRY = ?  And TAppr.LE_BOOK = ?  And TAppr.BUILD = ?  And TAppr.SUB_BUILD_NUMBER = ?  And TAppr.BC_SEQUENCE = ? ");

		Object objParams[] = new Object[intKeyFieldsCount];
		objParams[0] = new String(dObj.getCountry());	//[COUNTRY]
		objParams[1] = new String(CalLeBook);	//[LE_BOOK]
		objParams[2] = new String(dObj.getBuild());	//[BUILD]
		objParams[3] = new Integer(dObj.getSubBuildNumber());	//[SUB_BUILD_NUMBER]
		objParams[4] = new Integer(dObj.getBcSequence());	//[BC_SEQUENCE]
		try
		{
			if(!dObj.isVerificationRequired()){intStatus =0;}
			if(intStatus == 0)
			{
				logger.info("Executing approved query");
				collTemp = getJdbcTemplate().query(strQueryAppr.toString(),objParams,getMapper());
			}
			return collTemp;
		}
		catch(Exception ex){
			ex.printStackTrace();
			logger.error("Error: getQueryResultsForReview Exception :   ");
			if(intStatus == 0)
				logger.error(((strQueryAppr == null) ? "strQueryAppr is null" : strQueryAppr.toString()));

			return null;
		}
	}
	protected RowMapper getQueryListMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				BuildControlsVb buildControlsVb = new BuildControlsVb();
				buildControlsVb.setCountry(rs.getString("COUNTRY"));
				buildControlsVb.setLeBook(rs.getString("LE_BOOK"));
				buildControlsVb.setBuild(rs.getString("BUILD"));
				buildControlsVb.setSubBuildNumber(rs.getString("SUB_BUILD_NUMBER"));
				buildControlsVb.setBcSequence(rs.getString("BC_SEQUENCE"));
				buildControlsVb.setBuildModule(rs.getString("BUILD_MODULE"));
				buildControlsVb.setProgramDescription(rs.getString("PROGRAM_DESCRIPTION"));
				buildControlsVb.setRunItAt(207);
				buildControlsVb.setRunIt(rs.getString("RUN_IT"));
				buildControlsVb.setLastBuildDate(rs.getString("LAST_BUILD_DATE"));
				buildControlsVb.setBuildLevel(rs.getInt("BUILD_LEVEL"));
				buildControlsVb.setProgramType(rs.getString("PROGRAM_TYPE"));
				buildControlsVb.setExpandFlag(rs.getString("EXPAND_FLAG"));
				buildControlsVb.setFullBuildAllowed(rs.getString("FULL_BUILD_ALLOWED"));
				return buildControlsVb;
			}
		};
		return mapper;
	}

	public List<BuildControlsVb>  getQueryList(BuildSchedulesVb buildSchedulesVb) // Build Controls Details
	{
		List<BuildControlsVb> collTemp = null;
		final int intKeyFieldsCount = 18;
		String CalLeBook = removeDescLeBook(buildSchedulesVb.getLeBook());
		String query = new String("Select T1.COUNTRY, T1.LE_BOOK, T1.BUILD, T1.BUILD_NUMBER , T1.SUB_BUILD_NUMBER, T1.BC_SEQUENCE, T1.BUILD_MODULE, T7.PROGRAM_DESCRIPTION, " +
				"T1.RUN_IT, To_Char(T1.LAST_BUILD_DATE, 'DD-MM-YYYY HH24:MI:SS') as LAST_BUILD_DATE, 1 BUILD_LEVEL, T7.PROGRAM_TYPE, T1.EXPAND_FLAG, T7.FULL_BUILD_ALLOWED   " +
				"FROM BUILD_CONTROLS T1, PROGRAMS T7 " +
				"WHERE T1.COUNTRY = ? AND T1.LE_BOOK = ? AND T1.BUILD = T1.BUILD_MODULE AND " + //1,2
				"T1.BUILD = ? And T1.BUILD_MODULE = T7.PROGRAM " +  //3
				"UNION " +
				"Select T1.COUNTRY, T1.LE_BOOK, T1.BUILD, T1.BUILD_NUMBER , T1.SUB_BUILD_NUMBER, T1.BC_SEQUENCE, T1.BUILD_MODULE, T7.PROGRAM_DESCRIPTION, " +
				"T1.RUN_IT, To_Char(T1.LAST_BUILD_DATE, 'DD-MM-YYYY HH24:MI:SS') as LAST_BUILD_DATE, 1 BUILD_LEVEL, T7.PROGRAM_TYPE, T1.EXPAND_FLAG, T7.FULL_BUILD_ALLOWED  " +
				"FROM BUILD_CONTROLS T1 , PROGRAMS T7 " +
				"WHERE T1.COUNTRY = ? AND T1.LE_BOOK = ? AND " +  //4,5
				"T1.BUILD = ? And T1.BUILD_MODULE = T7.PROGRAM " + //6
				"UNION " +
				"Select T2.COUNTRY, T2.LE_BOOK, T2.BUILD, T2.BUILD_NUMBER , T2.SUB_BUILD_NUMBER, T2.BC_SEQUENCE, T2.BUILD_MODULE, T7.PROGRAM_DESCRIPTION, " +
				"T2.RUN_IT, To_Char(T2.LAST_BUILD_DATE, 'DD-MM-YYYY HH24:MI:SS') as LAST_BUILD_DATE, 2 BUILD_LEVEL, T7.PROGRAM_TYPE, T2.EXPAND_FLAG, T7.FULL_BUILD_ALLOWED   " +
				"FROM BUILD_CONTROLS T2, BUILD_CONTROLS T3, PROGRAMS T7 " +
				"WHERE T2.COUNTRY = ? AND T2.LE_BOOK = ? AND " + //7,8
				"T3.COUNTRY = ? AND T3.LE_BOOK = ? AND " + //9,10
				"T2.BUILD = T3.BUILD_MODULE AND T2.BUILD != T2.BUILD_MODULE AND " +
				"T3.BUILD = ?  And T2.BUILD_MODULE = T7.PROGRAM " + //11
				"UNION " +
				"Select T4.COUNTRY, T4.LE_BOOK, T4.BUILD, T4.BUILD_NUMBER , T4.SUB_BUILD_NUMBER, T4.BC_SEQUENCE, T4.BUILD_MODULE, T7.PROGRAM_DESCRIPTION, " +
				"T4.RUN_IT, To_Char(T4.LAST_BUILD_DATE, 'DD-MM-YYYY HH24:MI:SS') as LAST_BUILD_DATE, 3 BUILD_LEVEL, T7.PROGRAM_TYPE, T4.EXPAND_FLAG, T7.FULL_BUILD_ALLOWED " +
				"FROM BUILD_CONTROLS T4, BUILD_CONTROLS T5, BUILD_CONTROLS T6, PROGRAMS T7 " +
				"WHERE T4.COUNTRY = ? AND T4.LE_BOOK = ? AND " +  //12,13
				"T5.COUNTRY = ? AND T5.LE_BOOK = ? AND " +  //14,15
				"T6.COUNTRY = ? AND T6.LE_BOOK = ? AND " + //16,17
				"T4.BUILD = T5.BUILD_MODULE AND T4.BUILD != T4.BUILD_MODULE AND " +
				"T5.BUILD = T6.BUILD_MODULE AND T5.BUILD != T5.BUILD_MODULE AND  " +
				"T6.BUILD = ?  And T4.BUILD_MODULE = T7.PROGRAM " + //18
				"ORDER BY 5, 11 ,6 ");
		
		
		Object objParams[] = new Object[intKeyFieldsCount];
		objParams[0] = buildSchedulesVb.getCountry(); //1 
		objParams[1] = CalLeBook; //2
		objParams[2] = buildSchedulesVb.getBuild(); //3
		objParams[3] = buildSchedulesVb.getCountry(); //4
		objParams[4] = CalLeBook; //5
		objParams[5] = buildSchedulesVb.getBuild(); //6
		objParams[6] = buildSchedulesVb.getCountry(); //7
		objParams[7] = CalLeBook; //8
		objParams[8] = buildSchedulesVb.getCountry(); //9
		objParams[9] = CalLeBook; //10
		objParams[10] = buildSchedulesVb.getBuild(); //11
		objParams[11] = buildSchedulesVb.getCountry(); //12
		objParams[12] = CalLeBook; //13
		objParams[13] = buildSchedulesVb.getCountry(); //14
		objParams[14] = CalLeBook; //15
		objParams[15] = buildSchedulesVb.getCountry(); //16
		objParams[16] = CalLeBook; //17
		objParams[17] = buildSchedulesVb.getBuild(); //18
		
		try{
			collTemp = getJdbcTemplate().query(query, objParams, getQueryListMapper());
			return collTemp;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Error: getQueryResultsForReview Exception :   ");
			logger.error(((query == null) ? "strQueryAppr is null" : query.toString()));
			return null;
		}
	}
	
	
	public List<BuildControlsVb>  getQueryListForFin(BuildSchedulesVb buildSchedulesVb) // Build Controls Details
	{
		List<BuildControlsVb> collTemp = null;
		final int intKeyFieldsCount = 18;
		String CalLeBook = removeDescLeBook(buildSchedulesVb.getLeBook());
		String query = new String("SELECT COUNTRY, LE_BOOK, BUILD, BUILD_NUMBER, SUB_BUILD_NUMBER, BC_SEQUENCE, BUILD_MODULE, PROGRAM_DESCRIPTION, RUN_IT, "+
                 " LAST_BUILD_DATE, BUILD_LEVEL, PROGRAM_TYPE, EXPAND_FLAG FROM "+
                 " (Select T1.COUNTRY, T1.LE_BOOK, T1.BUILD, T1.BUILD_NUMBER , T1.SUB_BUILD_NUMBER, T1.BC_SEQUENCE, T1.BUILD_MODULE, T7.PROGRAM_DESCRIPTION, " +
                 " T1.RUN_IT, To_Char(T1.LAST_BUILD_DATE, 'DD-MM-YYYY HH24:MI:SS') as LAST_BUILD_DATE, 1 BUILD_LEVEL, T7.PROGRAM_TYPE, T1.EXPAND_FLAG, T7.FULL_BUILD_ALLOWED   " +
                 " FROM BUILD_CONTROLS T1, PROGRAMS T7 " +
                 " WHERE T1.COUNTRY = ? AND T1.LE_BOOK = ? AND T1.BUILD = T1.BUILD_MODULE AND " + //1,2
                 " T1.BUILD = ? And T1.BUILD_MODULE = T7.PROGRAM " +  //3
                 " UNION " +
                   " Select T1.COUNTRY, T1.LE_BOOK, T1.BUILD, T1.BUILD_NUMBER , T1.SUB_BUILD_NUMBER, T1.BC_SEQUENCE, T1.BUILD_MODULE, T7.PROGRAM_DESCRIPTION, " +
                   " T1.RUN_IT, To_Char(T1.LAST_BUILD_DATE, 'DD-MM-YYYY HH24:MI:SS') as LAST_BUILD_DATE, 1 BUILD_LEVEL, T7.PROGRAM_TYPE, T1.EXPAND_FLAG, T7.FULL_BUILD_ALLOWED  " +
                   " FROM BUILD_CONTROLS T1 , PROGRAMS T7 " +
                   " WHERE T1.COUNTRY = ? AND T1.LE_BOOK = ? AND " +  //4,5
                   " T1.BUILD = ? And T1.BUILD_MODULE = T7.PROGRAM " + //6
                 " UNION " +
				   " Select T2.COUNTRY, T2.LE_BOOK, T2.BUILD, T2.BUILD_NUMBER , T2.SUB_BUILD_NUMBER, T2.BC_SEQUENCE, T2.BUILD_MODULE, T7.PROGRAM_DESCRIPTION, " +
				   " T2.RUN_IT, To_Char(T2.LAST_BUILD_DATE, 'DD-MM-YYYY HH24:MI:SS') as LAST_BUILD_DATE, 2 BUILD_LEVEL, T7.PROGRAM_TYPE, T2.EXPAND_FLAG, T7.FULL_BUILD_ALLOWED   " +
				   " FROM BUILD_CONTROLS T2, BUILD_CONTROLS T3, PROGRAMS T7 " +
				   " WHERE T2.COUNTRY = ? AND T2.LE_BOOK = ? AND " + //7,8
				   " T3.COUNTRY = ? AND T3.LE_BOOK = ? AND " + //9,10
				   " T2.BUILD = T3.BUILD_MODULE AND T2.BUILD != T2.BUILD_MODULE AND " +
				   " T3.BUILD = ?  And T2.BUILD_MODULE = T7.PROGRAM " + //11
				" UNION " +
				   " Select T4.COUNTRY, T4.LE_BOOK, T4.BUILD, T4.BUILD_NUMBER , T4.SUB_BUILD_NUMBER, T4.BC_SEQUENCE, T4.BUILD_MODULE, T7.PROGRAM_DESCRIPTION, " +
				   " T4.RUN_IT, To_Char(T4.LAST_BUILD_DATE, 'DD-MM-YYYY HH24:MI:SS') as LAST_BUILD_DATE, 3 BUILD_LEVEL, T7.PROGRAM_TYPE, T4.EXPAND_FLAG, T7.FULL_BUILD_ALLOWED  " +
				   " FROM BUILD_CONTROLS T4, BUILD_CONTROLS T5, BUILD_CONTROLS T6, PROGRAMS T7 " +
				   " WHERE T4.COUNTRY = ? AND T4.LE_BOOK = ? AND " +  //12,13
				   " T5.COUNTRY = ? AND T5.LE_BOOK = ? AND " +  //14,15
				   " T6.COUNTRY = ? AND T6.LE_BOOK = ? AND " + //16,17
				   " T4.BUILD = T5.BUILD_MODULE AND T4.BUILD != T4.BUILD_MODULE AND " +
				   " T5.BUILD = T6.BUILD_MODULE AND T5.BUILD != T5.BUILD_MODULE AND  " +
				   " T6.BUILD = ?  And T4.BUILD_MODULE = T7.PROGRAM ) T1 Where BUILD_MODULE IN ('MGTFCAPX','MGTFHRB','MGTFNFI','MGTFSBU',' MGTPROJ','MGTFMRG')" + //18
				"ORDER BY 5, 11 ,6 ");
		
		Object objParams[] = new Object[intKeyFieldsCount];
		objParams[0] = buildSchedulesVb.getCountry(); //1 
		objParams[1] = CalLeBook; //2
		objParams[2] = buildSchedulesVb.getBuild(); //3
		objParams[3] = buildSchedulesVb.getCountry(); //4
		objParams[4] = CalLeBook; //5
		objParams[5] = buildSchedulesVb.getBuild(); //6
		objParams[6] = buildSchedulesVb.getCountry(); //7
		objParams[7] = CalLeBook; //8
		objParams[8] = buildSchedulesVb.getCountry(); //9
		objParams[9] = CalLeBook; //10
		objParams[10] = buildSchedulesVb.getBuild(); //11
		objParams[11] = buildSchedulesVb.getCountry(); //12
		objParams[12] = CalLeBook; //13
		objParams[13] = buildSchedulesVb.getCountry(); //14
		objParams[14] = CalLeBook; //15
		objParams[15] = buildSchedulesVb.getCountry(); //16
		objParams[16] = CalLeBook; //17
		objParams[17] = buildSchedulesVb.getBuild(); //18
		
		try{
			collTemp = getJdbcTemplate().query(query, objParams, getQueryListMapper());
			return collTemp;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Error: getQueryResultsForReview Exception :   ");
			logger.error(((query == null) ? "strQueryAppr is null" : query.toString()));
			return null;
		}
	}
	protected RowMapper getCtryLeBookMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				BuildControlsVb buildControlsVb = new BuildControlsVb();
				buildControlsVb.setCountry(rs.getString("COUNTRY"));
				buildControlsVb.setLeBook(rs.getString("LE_BOOK"));
				return buildControlsVb;
			}
		};
		return mapper;
	}
	/**
	 * returns distinct country and Le Book from Build Controls for given Build Name.
	 * @param buildName
	 * @return
	 */
	public List<BuildControlsVb> getDistinctCtryFromBuildControls(String buildName){
		
		List<BuildControlsVb> collTemp = null;
		final int intKeyFieldsCount = 1;
		String query = new String("SELECT DISTINCT COUNTRY, LE_BOOK FROM BUILD_CONTROLS "+
					"WHERE BUILD = ? ORDER BY COUNTRY, LE_BOOK"); 
		Object objParams[] = new Object[intKeyFieldsCount];
		objParams[0] = buildName; //1 
		try{
			collTemp = getJdbcTemplate().query(query, objParams, getCtryLeBookMapper());
			return collTemp;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Error: getQueryResultsForReview Exception :   ");
			logger.error(((query == null) ? "strQueryAppr is null" : query.toString()));
			return null;
		}

	}
	public List<BuildControlsVb> getDistCountryLeBook(String build) // Build Controls Details
	{
		List<BuildControlsVb> collTemp = null;
		final int intKeyFieldsCount = 1;
		String query = new String("SELECT DISTINCT COUNTRY, LE_BOOK FROM BUILD_CONTROLS "+
					"WHERE BUILD = ?  AND COUNTRY <>'ZZ' AND LE_BOOK <> '99' "+
					"ORDER BY COUNTRY, LE_BOOK"); 
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = build; //1 
		try
		{
			collTemp = getJdbcTemplate().query(query, params, getCtryLeBookMapper());
			return collTemp;
		}
		catch(Exception ex){
			ex.printStackTrace();
			logger.error(((query==null)? "strBufApprove is null":query.toString()));

			if (params != null)
				for(int i=0 ; i< params.length; i++)
					logger.error("objParams[" + i + "]" + params[i]);
			return null;
		}

	}
	public int getParallelProcessCount(BuildSchedulesVb oBuildSchedulesVb){
		final int intKeyFieldsCount = 1;
		String query = new String("select count(1) PARALLEL_PROCESS_COUNT from (select DISTINCT sub_build_number from BUILD_CONTROLS where build = ? )");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = new String(oBuildSchedulesVb.getBuild()); //[BUILD]
		try{
			return  getJdbcTemplate().queryForObject(query,params, Integer.class);
		}catch(Exception ex){
			return 0;
		}
	}
	public Long getMaxBuildNumber(){
		String query = new String("Select max(BUILD_NUMBER) From BUILD_SCHEDULES");
		try{
			return getJdbcTemplate().queryForObject(query, Long.class);
		}catch(Exception ex){
			return 0l;
		}
	}
	@Override
	protected List<BuildControlsVb> selectApprovedRecord(BuildControlsVb vObject){
		return getQueryResultsForReview(vObject, Constants.STATUS_ZERO);
	}

	@Override
	protected List<BuildControlsVb> doSelectPendingRecord(BuildControlsVb vObject){
		return getQueryResultsForReview(vObject, Constants.STATUS_PENDING);
	}

	@Override
	protected void setServiceDefaults(){
		serviceName = "BuildControls";
		serviceDesc = CommonUtils.getResourceManger().getString("buildControls");
		tableName = "BUILD_CONTROLS";
		childTableName = "BUILD_CONTROLS";
		intCurrentUserId = SessionContextHolder.getContext().getVisionId();
	}


	@Override
	protected int doUpdateAppr(BuildControlsVb vObject){
		String CalLeBook = removeDescLeBook(vObject.getLeBook());

		String query = "Update BUILD_CONTROLS Set " + 
		"BUILD_MODULE = ?, RUN_IT = ?,  BUILD_CONTROLS_STATUS = ?, " + 
		" MAKER = ?, DATE_LAST_MODIFIED = SysDate " + 
		"Where COUNTRY = ? " +
		" And LE_BOOK = ? " +
		" And BUILD = ? " +
		" And SUB_BUILD_NUMBER = ? " +
		" And BC_SEQUENCE = ? ";

		Object args[] = {vObject.getBuildModule(), vObject.getRunIt(),  vObject.getBuildControlsStatus(), vObject.getMaker(), 
		vObject.getCountry(), CalLeBook, vObject.getBuild(), vObject.getSubBuildNumber(),	vObject.getBcSequence()};
		return getJdbcTemplate().update(query,args);
	}

	@Override
	protected String frameErrorMessage(BuildControlsVb vObject, String strOperation)
	{
		// specify all the key fields and their values first
		String strErrMsg = new String("");
		try {
			String CalLeBook = removeDescLeBook(vObject.getLeBook());

			strErrMsg =  strErrMsg + " COUNTRY:" + vObject.getCountry();
			strErrMsg =  strErrMsg + " LE_BOOK:" + CalLeBook;
			strErrMsg =  strErrMsg + " BUILD:" + vObject.getBuild();
			strErrMsg =  strErrMsg + " SUB_BUILD_NUMBER:" + vObject.getSubBuildNumber();
			strErrMsg =  strErrMsg + " BC_SEQUENCE:" + vObject.getBcSequence();
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
	protected String getAuditString(BuildControlsVb vObject){
		final String auditDelimiter = vObject.getAuditDelimiter();
		final String auditDelimiterColVal = vObject.getAuditDelimiterColVal();
		StringBuffer strAudit = new StringBuffer("");
		String CalLeBook = removeDescLeBook(vObject.getLeBook());

		try{
			if(ValidationUtil.isValid(vObject.getCountry()))
				strAudit.append("COUNTRY"+auditDelimiterColVal+vObject.getCountry().trim());
			else
				strAudit.append("COUNTRY"+auditDelimiterColVal+"NULL");
			strAudit.append(auditDelimiter);
			
			if(ValidationUtil.isValid(CalLeBook))
				strAudit.append("LE_BOOK"+auditDelimiterColVal+CalLeBook.trim());
			else
				strAudit.append("LE_BOOK"+auditDelimiterColVal+"NULL");
			strAudit.append(auditDelimiter);

			if(ValidationUtil.isValid(vObject.getBuild()))
				strAudit.append("BUILD"+auditDelimiterColVal+vObject.getBuild().trim());
			else
				strAudit.append("BUILD"+auditDelimiterColVal+"NULL");
			strAudit.append(auditDelimiter);

			strAudit.append("SUB_BUILD_NUMBER"+auditDelimiterColVal+vObject.getSubBuildNumber());
			strAudit.append(auditDelimiter);
			strAudit.append("BC_SEQUENCE"+auditDelimiterColVal+vObject.getBcSequence());
			strAudit.append(auditDelimiter);
			if(ValidationUtil.isValid(vObject.getBuildModule()))
				strAudit.append("BUILD_MODULE"+auditDelimiterColVal+vObject.getBuildModule().trim());
			else
				strAudit.append("BUILD_MODULE"+auditDelimiterColVal+"NULL");
			strAudit.append(auditDelimiter);

			strAudit.append("RUN_IT_AT"+auditDelimiterColVal+vObject.getRunItAt());
			strAudit.append(auditDelimiter);
			strAudit.append("RUN_IT"+auditDelimiterColVal+vObject.getRunIt());
			strAudit.append(auditDelimiter);

			if(ValidationUtil.isValid(vObject.getLastStartDate()))
				strAudit.append("LAST_START_DATE"+auditDelimiterColVal+vObject.getLastStartDate().trim());
			else
				strAudit.append("LAST_START_DATE"+auditDelimiterColVal+"NULL");
			strAudit.append(auditDelimiter);

			if(ValidationUtil.isValid(vObject.getLastBuildDate()))
				strAudit.append("LAST_BUILD_DATE"+auditDelimiterColVal+vObject.getLastBuildDate().trim());
			else
				strAudit.append("LAST_BUILD_DATE"+auditDelimiterColVal+"NULL");
			strAudit.append(auditDelimiter);

			strAudit.append("BUILD_CONTROLS_STATUS_AT"+auditDelimiterColVal+vObject.getBuildControlsStatusAt());
			strAudit.append(auditDelimiter);
			strAudit.append("BUILD_CONTROLS_STATUS"+auditDelimiterColVal+vObject.getBuildControlsStatus());
			strAudit.append(auditDelimiter);
			
			if(ValidationUtil.isValid(vObject.getSubmitterId()))
				strAudit.append("SUBMITTER_ID"+auditDelimiterColVal+vObject.getSubmitterId().trim());
			else
				strAudit.append("SUBMITTER_ID"+auditDelimiterColVal+"NULL");
			strAudit.append(auditDelimiter);
			
			if(ValidationUtil.isValid(vObject.getBuildNumber()))
				strAudit.append("BUILD_NUMBER"+auditDelimiterColVal+vObject.getBuildNumber().trim());
			else
				strAudit.append("BUILD_NUMBER"+auditDelimiterColVal+"NULL");
			strAudit.append(auditDelimiter);
			
			if(ValidationUtil.isValid(vObject.getExpandFlag()))
				strAudit.append("EXPAND_FLAG"+auditDelimiterColVal+vObject.getExpandFlag().trim());
			else
				strAudit.append("EXPAND_FLAG"+auditDelimiterColVal+"NULL");
			strAudit.append(auditDelimiter);
			strAudit.append("RECORD_INDICATOR_NT"+auditDelimiterColVal+vObject.getRecordIndicatorNt());
			strAudit.append(auditDelimiter);
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

		}catch(Exception ex){
			strErrorDesc = ex.getMessage();
			strAudit = strAudit.append(strErrorDesc);
			ex.printStackTrace();
		}
		return strAudit.toString();
	}
	protected ExceptionCode updateBuildControls(BuildSchedulesDetailsVb buildSchedulesDetailsVb) {
		final int intKeyFieldsCount = 1;
		String query = new String("Update BUILD_CONTROLS T1 Set Build_Controls_Status = 'C' Where Build_Controls_Status Not In ('I', 'W') "+
	      "And Exists (Select 'X' From Build_Schedules_Details T2  Where T2.Build_Number = ?  And T1.Build = T2.Associated_Build "+
	      "And T1.Build_Module = T2.Build_Module And Module_Status Not In ('P', 'C') AND T1.COUNTRY = T2.COUNTRY AND T1.LE_BOOK = T2.LE_BOOK)");
		Object params[] = new Object[intKeyFieldsCount];
		params[0] = new String(buildSchedulesDetailsVb.getBuildNumber()); //[BUILD]
		try
		{
			getJdbcTemplate().update(query,params);
			return getResultObject(Constants.SUCCESSFUL_OPERATION);
		}catch(Exception ex){
			strErrorDesc = ex.getMessage();
			ExceptionCode eceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			return eceptionCode;
		}
	}

}
