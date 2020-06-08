package com.vision.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.RowMapper;

import com.vision.authentication.SessionContextHolder;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.Paginationhelper;
import com.vision.util.ValidationUtil;
import com.vision.vb.AlphaSubTabVb;
import com.vision.vb.NumSubTabVb;
import com.vision.vb.TabVb;

public class AlphaNumTabDao extends AbstractDao<TabVb> {
	
	private NumSubTabDao numSubTabDao;
	private AlphaSubTabDao alphaSubTabDao;
	
	public RowMapper getQueryPopupMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				TabVb tabVb = new TabVb();
				tabVb.setTab(rs.getInt(1)); 
				tabVb.setTabDescription(rs.getString(2));
				return tabVb;
			}
		};
		return mapper;
	}
	public RowMapper getMapperAt(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				TabVb tabVb = new TabVb();
				tabVb.setTab(rs.getInt("ALPHA_TAB"));
				tabVb.setTabDescription(rs.getString("ALPHA_TAB_DESCRIPTION"));
				tabVb.setTabStatusNt(rs.getInt("ALPHA_TAB_STATUS_NT"));
				tabVb.setTabStatus(rs.getInt("ALPHA_TAB_STATUS"));
				tabVb.setDbStatus(rs.getInt("ALPHA_TAB_STATUS"));
				tabVb.setRecordIndicatorNt(rs.getInt("RECORD_INDICATOR_NT"));
				tabVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
				tabVb.setMaker(rs.getLong("MAKER"));
				tabVb.setVerifier(rs.getLong("VERIFIER"));
				tabVb.setInternalStatus(rs.getInt("INTERNAL_STATUS"));
				tabVb.setDateCreation(rs.getString("DATE_CREATION"));
				tabVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				tabVb.setReadOnly(rs.getString("READ_ONLY"));
				tabVb.setRequestType("alphaTab");
				return tabVb;
			}
		};
		return mapper;
	}
	public RowMapper getMapperNt(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				TabVb tabVb = new TabVb();
				tabVb.setTab(rs.getInt("NUM_TAB"));
				tabVb.setTabDescription(rs.getString("NUM_TAB_DESCRIPTION"));
				tabVb.setTabStatusNt(rs.getInt("NUM_TAB_STATUS_NT"));
				tabVb.setTabStatus(rs.getInt("NUM_TAB_STATUS"));
				tabVb.setDbStatus(rs.getInt("NUM_TAB_STATUS"));
				tabVb.setRecordIndicatorNt(rs.getInt("RECORD_INDICATOR_NT"));
				tabVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
				tabVb.setMaker(rs.getLong("MAKER"));
				tabVb.setVerifier(rs.getLong("VERIFIER"));
				tabVb.setInternalStatus(rs.getInt("INTERNAL_STATUS"));
				tabVb.setDateCreation(rs.getString("DATE_CREATION"));
				tabVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED"));
				tabVb.setReadOnly(rs.getString("READ_ONLY"));
				tabVb.setRequestType("numTab");
				return tabVb;
			}
		};
		return mapper;
	}
	@Override
	protected int getStatus(TabVb records){return records.getTabStatus();}
	@Override
	protected void setStatus(TabVb vObject,int status){vObject.setTabStatus(status);}
	@Override
	public List<TabVb> getQueryResultsForReview(TabVb dObj, int status){
		return getQueryResults(dObj, status);
	}
	@Override
	public List<TabVb> getQueryPopupResults(TabVb tabVb){
		if("numTab".equalsIgnoreCase(tabVb.getRequestType())){
			setServiceDefaultsNt();
			return getQueryPopupResultsNt(tabVb);
		}else{
			setServiceDefaultsAt();
			return getQueryPopupResultsAt(tabVb);
		}
	}
	public List<TabVb> getQueryResults(TabVb tabVb, int intStatus){
		if("numTab".equalsIgnoreCase(tabVb.getRequestType())){
			setServiceDefaultsNt();
			return getQueryResultsNt(tabVb, intStatus);
		}else{
			setServiceDefaultsAt();
			return getQueryResultsAt(tabVb, intStatus);
		}
	}
	
	private List<TabVb> getQueryResultsAt(TabVb tabVb, int intStatus) {
		List<TabVb> collTemp = null;
		final int intKeyFieldsCount = 1;
		setServiceDefaultsAt();
		String strQueryAppr = new String("Select ALPHA_TAB, ALPHA_TAB_DESCRIPTION, ALPHA_TAB_STATUS_NT, ALPHA_TAB_STATUS, MAKER, " +
				"RECORD_INDICATOR_NT, RECORD_INDICATOR, VERIFIER, To_Char(DATE_LAST_MODIFIED, 'DD-MM-RRRR HH24:MI:SS') DATE_LAST_MODIFIED, " +
				"To_Char(DATE_CREATION, 'DD-MM-RRRR HH24:MI:SS') DATE_CREATION, INTERNAL_STATUS, READ_ONLY From ALPHA_TAB Where ALPHA_TAB = ? ");
		String strQueryPend = new String("Select ALPHA_TAB, ALPHA_TAB_DESCRIPTION, ALPHA_TAB_STATUS_NT, ALPHA_TAB_STATUS, MAKER, " +
				"RECORD_INDICATOR_NT, RECORD_INDICATOR, VERIFIER, To_Char(DATE_LAST_MODIFIED, 'DD-MM-RRRR HH24:MI:SS') DATE_LAST_MODIFIED, " +
				"To_Char(DATE_CREATION, 'DD-MM-RRRR HH24:MI:SS') DATE_CREATION, INTERNAL_STATUS, READ_ONLY From ALPHA_TAB_PEND Where ALPHA_TAB = ? ");

		Object objParams[] = new Object[intKeyFieldsCount];
		objParams[0] = tabVb.getTab();
		try
		{
			if(!tabVb.isVerificationRequired()){intStatus =0;}
			if(intStatus == 0){
				logger.info("Executing approved query");
				collTemp = getJdbcTemplate().query(strQueryAppr.toString(),objParams,getMapperAt());
			}else{
				logger.info("Executing pending query");
				collTemp = getJdbcTemplate().query(strQueryPend.toString(),objParams,getMapperAt());
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
	private List<TabVb> getQueryResultsNt(TabVb tabVb,int intStatus) {
		List<TabVb> collTemp = null;
		final int intKeyFieldsCount = 1;
		setServiceDefaultsNt();
		String strQueryAppr = new String("Select NUM_TAB, NUM_TAB_DESCRIPTION, NUM_TAB_STATUS_NT, NUM_TAB_STATUS, MAKER, " +
				"RECORD_INDICATOR_NT, RECORD_INDICATOR, VERIFIER, To_Char(DATE_LAST_MODIFIED, 'DD-MM-RRRR HH24:MI:SS') DATE_LAST_MODIFIED, " +
				"To_Char(DATE_CREATION, 'DD-MM-RRRR HH24:MI:SS') DATE_CREATION, INTERNAL_STATUS, READ_ONLY From NUM_TAB Where NUM_TAB = ? ");
		String strQueryPend = new String("Select NUM_TAB, NUM_TAB_DESCRIPTION, NUM_TAB_STATUS_NT, NUM_TAB_STATUS, MAKER, " +
				"RECORD_INDICATOR_NT, RECORD_INDICATOR, VERIFIER, To_Char(DATE_LAST_MODIFIED, 'DD-MM-RRRR HH24:MI:SS') DATE_LAST_MODIFIED, " +
				"To_Char(DATE_CREATION, 'DD-MM-RRRR HH24:MI:SS') DATE_CREATION, INTERNAL_STATUS, READ_ONLY From NUM_TAB_PEND Where NUM_TAB = ? ");

		Object objParams[] = new Object[intKeyFieldsCount];
		objParams[0] = tabVb.getTab();
		try
		{
			if(!tabVb.isVerificationRequired()){intStatus =0;}
			if(intStatus == 0){
				logger.info("Executing approved query");
				collTemp = getJdbcTemplate().query(strQueryAppr.toString(),objParams,getMapperNt());
			}else{
				logger.info("Executing pending query");
				collTemp = getJdbcTemplate().query(strQueryPend.toString(),objParams,getMapperNt());
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
	private List<TabVb> getQueryPopupResultsAt(TabVb tabVb) {
		setServiceDefaultsAt();
		Vector<Object> params = new Vector<Object>();
		int Ctr = 0;
		List<TabVb> result = null;
		Object objParams[] = null;
		StringBuffer strNtApprove = new StringBuffer(" SELECT ATTL.ALPHA_TAB, ATTL.ALPHA_TAB_STATUS , ATTL.ALPHA_TAB_DESCRIPTION FROM ALPHA_TAB ATTL ");
		StringBuffer strNtPend = new StringBuffer(" SELECT ATTL.ALPHA_TAB, ATTL.ALPHA_TAB_STATUS , ATTL.ALPHA_TAB_DESCRIPTION FROM ALPHA_TAB_PEND ATTL ");
		StringBuffer strNstApprove = new StringBuffer(" ");
		StringBuffer strNstPend = new StringBuffer("  ");
		String strNstAppFromCond = "SELECT DISTINCT  ATT.ALPHA_TAB, X.ALPHA_TAB_DESCRIPTION  FROM ALPHA_SUB_TAB ATT";
		String strNstPendFromCond = "SELECT DISTINCT ATT.ALPHA_TAB, X.ALPHA_TAB_DESCRIPTION  FROM ALPHA_SUB_TAB_PEND ATT";
		String strNtNotExists = " NOT EXISTS (SELECT 'X' FROM ALPHA_TAB_PEND P WHERE P.ALPHA_TAB=ATTL.ALPHA_TAB) ";
		String strNstNotExists = " NOT EXISTS (SELECT 'X' FROM ALPHA_SUB_TAB_PEND P WHERE P.ALPHA_SUB_TAB=ATT.ALPHA_SUB_TAB AND P.ALPHA_TAB = ATT.ALPHA_TAB) ";
		String strSubQryJoinCond = " ATT.ALPHA_TAB=X.ALPHA_TAB";
		String orderBy = " ORDER BY ALPHA_TAB";
		//check if the column [ALPHA_TAB] should be included in the query
		if (tabVb.getTab() != 0){
			params.addElement(tabVb.getTab());
			CommonUtils.addToQuery("ATTL.ALPHA_TAB = ?", strNtApprove);
			CommonUtils.addToQuery("ATTL.ALPHA_TAB = ?", strNtPend);
		}
		//check if the column [ALPHA_TAB_DESCRIPTION] should be included in the query
		if (ValidationUtil.isValid(tabVb.getTabDescription()))
		{
			params.addElement("%" + tabVb.getTabDescription().toUpperCase().trim() + "%");
			CommonUtils.addToQuery("UPPER(ATTL.ALPHA_TAB_DESCRIPTION) LIKE ?", strNtApprove);
			CommonUtils.addToQuery("UPPER(ATTL.ALPHA_TAB_DESCRIPTION) LIKE ?", strNtPend);
		}

		//check if the column [RECORD_INDICATOR] should be included in the query
		if (tabVb.getRecordIndicator() != -1)
		{
			if (tabVb.getRecordIndicator() > 3)
			{
				params.addElement(new Integer(0));
				CommonUtils.addToQuery("ATTL.RECORD_INDICATOR > ?", strNtApprove);
				CommonUtils.addToQuery("ATTL.RECORD_INDICATOR > ?", strNtPend);
			}
			else
			{
				params.addElement(new Integer(tabVb.getRecordIndicator()));
				CommonUtils.addToQuery("ATTL.RECORD_INDICATOR = ?", strNtApprove);
				CommonUtils.addToQuery("ATTL.RECORD_INDICATOR = ?", strNtPend);
			}
		}
		AlphaSubTabVb vObject = tabVb.getAlphaSubTabs().get(0);
		if(vObject != null){
			//check if the column [ALPHA_SUB_TAB] should be included in the query
			if (ValidationUtil.isValid(vObject.getAlphaSubTab()) && !"-1".equalsIgnoreCase(vObject.getAlphaSubTab()))
			{
				params.addElement(vObject.getAlphaSubTab());
				CommonUtils.addToQuery("ATT.ALPHA_SUB_TAB = ?", strNstApprove);
				CommonUtils.addToQuery("ATT.ALPHA_SUB_TAB = ?", strNstPend);
			}
			//check if the column [ALPHA_SUBTAB_DESCRIPTION] should be included in the query
			if (ValidationUtil.isValid(vObject.getDescription()))
			{
				params.addElement("%" + vObject.getDescription() + "%");
				CommonUtils.addToQuery("ATT.ALPHA_SUBTAB_DESCRIPTION LIKE ?", strNstApprove);
				CommonUtils.addToQuery("ATT.ALPHA_SUBTAB_DESCRIPTION LIKE ?", strNstPend);
			}
			//check if the column [ALPHA_SUBTAB_STATUS] should be included in the query
			if (vObject.getAlphaSubTabStatus() != -1)
			{
				params.addElement(vObject.getAlphaSubTabStatus());
				CommonUtils.addToQuery("ATT.ALPHA_SUBTAB_STATUS = ?", strNstApprove);
				CommonUtils.addToQuery("ATT.ALPHA_SUBTAB_STATUS = ?", strNstPend);
			}
			//check if the column [RECORD_INDICATOR] should be included in the query
			if (vObject.getRecordIndicator() != -1)
			{
				if (vObject.getRecordIndicator() > 3)
				{
					params.addElement(new Integer(0));
					CommonUtils.addToQuery("ATT.RECORD_INDICATOR > ?", strNstApprove);
					CommonUtils.addToQuery("ATT.RECORD_INDICATOR > ?", strNstPend);
				}
				else
				{
					params.addElement(new Integer(vObject.getRecordIndicator()));
					CommonUtils.addToQuery("ATT.RECORD_INDICATOR = ?", strNstApprove);
					CommonUtils.addToQuery("ATT.RECORD_INDICATOR = ?", strNstPend);
				}
			}
		}
		if(tabVb.isVerificationRequired()){
			objParams = new Object[params.size()*4];
		}else{
			objParams = new Object[params.size()];
		}
		for(Ctr=0; Ctr < params.size(); Ctr++)
			objParams[Ctr] = (Object) params.elementAt(Ctr);
		Paginationhelper<TabVb> paginationhelper = new Paginationhelper<TabVb>();
		if(tabVb.isVerificationRequired()){
			for(int Ctr2=0 ; Ctr2 < params.size(); Ctr2++, Ctr++)
				objParams[Ctr] = (Object) params.elementAt(Ctr2);
			for(int Ctr2=0 ; Ctr2 < params.size(); Ctr2++, Ctr++)
				objParams[Ctr] = (Object) params.elementAt(Ctr2);
			for(int Ctr2=0 ; Ctr2 < params.size(); Ctr2++, Ctr++)
				objParams[Ctr] = (Object) params.elementAt(Ctr2);

			/* NST From Cond + (NT Approve + Not Exists) + 'Alias Name' + Join Cond + NST Where Cond
			UNION
			NST PEND From Cond + (NT Approve + Not Exists) + 'Alias Name' + Join Cond + NST Where Cond
			UNION
			NST From Cond + (NT Pend + Not Exists) + 'Alias Name' + Join Cond + NST Where Cond
			UNION
			NST PEND From Cond + (NT Pend + Not Exists) + 'Alias Name' + Join Cond + NST Where Cond
			Order Cond*/
			CommonUtils.addToQuery(strNtNotExists, strNtApprove);
			StringBuffer lPendQuery1 = new StringBuffer(strNstAppFromCond +" ,("+ strNtApprove +") X " + strNstApprove);
			CommonUtils.addToQuery(strNstNotExists, lPendQuery1);
			CommonUtils.addToQuery(strSubQryJoinCond, lPendQuery1);
			StringBuffer lPendQuery2 = new StringBuffer(strNstAppFromCond +" ,("+ strNtPend +") X " + strNstApprove);
			CommonUtils.addToQuery(strNstNotExists, lPendQuery2);
			CommonUtils.addToQuery(strSubQryJoinCond, lPendQuery2);
			StringBuffer lPendQuery3 = new StringBuffer(strNstPendFromCond +" ,("+ strNtApprove +") X " + strNstPend);
			CommonUtils.addToQuery(strSubQryJoinCond, lPendQuery3);
			StringBuffer lPendQuery4 = new StringBuffer(strNstPendFromCond +" ,("+ strNtPend +") X " + strNstPend);
			CommonUtils.addToQuery(strSubQryJoinCond, lPendQuery4);
			String lPendQuery = lPendQuery1 + " UNION " + lPendQuery2 + " UNION " +lPendQuery3 + " UNION " + lPendQuery4 + orderBy;
			if(tabVb.getTotalRows()  <= 0){
				result = paginationhelper.fetchPage(getJdbcTemplate(), lPendQuery, 
						objParams, tabVb.getCurrentPage(),tabVb.getMaxRecords(), getQueryPopupMapper());
				tabVb.setTotalRows(paginationhelper.getTotalRows());
			}else{
				result = paginationhelper.fetchPage(getJdbcTemplate(), lPendQuery, 
						objParams, tabVb.getCurrentPage(),tabVb.getMaxRecords(), tabVb.getTotalRows(), getQueryPopupMapper()); 
			}
		}else{
			//Main Select + Attribute From Cond + (Level Query) + 'Alias Name' + Join Cond + Approve Where Cond + Order
			StringBuffer lApproveQuery = new StringBuffer(strNstAppFromCond +" ,("+ strNtApprove +") X " + strNstApprove);
			CommonUtils.addToQuery(strSubQryJoinCond, lApproveQuery);
			lApproveQuery.append(orderBy);
			if(tabVb.getTotalRows()  <= 0){
				result = paginationhelper.fetchPage(getJdbcTemplate(), lApproveQuery.toString(), 
						objParams, tabVb.getCurrentPage(),tabVb.getMaxRecords(), getQueryPopupMapper());
				tabVb.setTotalRows(paginationhelper.getTotalRows());
			}else{
				result = paginationhelper.fetchPage(getJdbcTemplate(), lApproveQuery.toString(), 
						objParams, tabVb.getCurrentPage(),tabVb.getMaxRecords(), tabVb.getTotalRows(), getQueryPopupMapper()); 
			}
		}
		return result;
	}

	private List<TabVb> getQueryPopupResultsNt(TabVb tabVb) {
		setServiceDefaultsNt();
		Vector<Object> params = new Vector<Object>();
		int Ctr = 0;
		List<TabVb> result = null;
		Object objParams[] = null;
		StringBuffer strNtApprove = new StringBuffer(" SELECT ATTL.NUM_TAB, ATTL.NUM_TAB_STATUS , ATTL.NUM_TAB_DESCRIPTION FROM NUM_TAB ATTL ");
		StringBuffer strNtPend = new StringBuffer(" SELECT ATTL.NUM_TAB, ATTL.NUM_TAB_STATUS , ATTL.NUM_TAB_DESCRIPTION FROM NUM_TAB_PEND ATTL ");
		StringBuffer strNstApprove = new StringBuffer(" ");
		StringBuffer strNstPend = new StringBuffer("  ");
		String strNstAppFromCond = "SELECT DISTINCT  ATT.NUM_TAB, X.NUM_TAB_DESCRIPTION  FROM NUM_SUB_TAB ATT";
		String strNstPendFromCond = "SELECT DISTINCT ATT.NUM_TAB, X.NUM_TAB_DESCRIPTION  FROM NUM_SUB_TAB_PEND ATT";
		String strNtNotExists = " NOT EXISTS (SELECT 'X' FROM NUM_TAB_PEND P WHERE P.NUM_TAB=ATTL.NUM_TAB) ";
		String strNstNotExists = " NOT EXISTS (SELECT 'X' FROM NUM_SUB_TAB_PEND P WHERE P.NUM_SUB_TAB=ATT.NUM_SUB_TAB AND P.NUM_TAB = ATT.NUM_TAB) ";
		String strSubQryJoinCond = " ATT.NUM_TAB=X.NUM_TAB";
		String orderBy=" ORDER BY NUM_TAB";
		//check if the column [NUM_TAB] should be included in the query
		if (tabVb.getTab() != 0){
			params.addElement(tabVb.getTab());
			CommonUtils.addToQuery("ATTL.NUM_TAB = ?", strNtApprove);
			CommonUtils.addToQuery("ATTL.NUM_TAB = ?", strNtPend);
		}
		//check if the column [NUM_TAB_DESCRIPTION] should be included in the query
		if (ValidationUtil.isValid(tabVb.getTabDescription()))
		{
			params.addElement("%" + tabVb.getTabDescription().toUpperCase().trim() + "%");
			CommonUtils.addToQuery("UPPER(ATTL.NUM_TAB_DESCRIPTION) LIKE ?", strNtApprove);
			CommonUtils.addToQuery("UPPER(ATTL.NUM_TAB_DESCRIPTION) LIKE ?", strNtPend);
		}

		//check if the column [RECORD_INDICATOR] should be included in the query
		if (tabVb.getRecordIndicator() != -1)
		{
			if (tabVb.getRecordIndicator() > 3)
			{
				params.addElement(new Integer(0));
				CommonUtils.addToQuery("ATTL.RECORD_INDICATOR > ?", strNtApprove);
				CommonUtils.addToQuery("ATTL.RECORD_INDICATOR > ?", strNtPend);
			}
			else
			{
				params.addElement(new Integer(tabVb.getRecordIndicator()));
				CommonUtils.addToQuery("ATTL.RECORD_INDICATOR = ?", strNtApprove);
				CommonUtils.addToQuery("ATTL.RECORD_INDICATOR = ?", strNtPend);
			}
		}
		NumSubTabVb vObject = tabVb.getNumSubTabs().get(0);
		if(vObject != null){
			//check if the column [NUM_SUB_TAB] should be included in the query
			if (vObject.getNumSubTab() != -1)
			{
				params.addElement(new Integer(vObject.getNumSubTab()));
				CommonUtils.addToQuery("ATT.NUM_SUB_TAB = ?", strNstApprove);
				CommonUtils.addToQuery("ATT.NUM_SUB_TAB = ?", strNstPend);
			}
			//check if the column [NUM_SUBTAB_DESCRIPTION] should be included in the query
			if (ValidationUtil.isValid(vObject.getDescription()))
			{
				params.addElement("%" + vObject.getDescription() + "%");
				CommonUtils.addToQuery("ATT.NUM_SUBTAB_DESCRIPTION LIKE ?", strNstApprove);
				CommonUtils.addToQuery("ATT.NUM_SUBTAB_DESCRIPTION LIKE ?", strNstPend);
			}
			//check if the column [NUM_SUBTAB_STATUS] should be included in the query
			if (vObject.getNumSubTabStatus() != -1)
			{
				params.addElement(vObject.getNumSubTabStatus());
				CommonUtils.addToQuery("ATT.NUM_SUBTAB_STATUS = ?", strNstApprove);
				CommonUtils.addToQuery("ATT.NUM_SUBTAB_STATUS = ?", strNstPend);
			}
			//check if the column [RECORD_INDICATOR] should be included in the query
			if (vObject.getRecordIndicator() != -1)
			{
				if (vObject.getRecordIndicator() > 3)
				{
					params.addElement(new Integer(0));
					CommonUtils.addToQuery("ATT.RECORD_INDICATOR > ?", strNstApprove);
					CommonUtils.addToQuery("ATT.RECORD_INDICATOR > ?", strNstPend);
				}
				else
				{
					params.addElement(new Integer(vObject.getRecordIndicator()));
					CommonUtils.addToQuery("ATT.RECORD_INDICATOR = ?", strNstApprove);
					CommonUtils.addToQuery("ATT.RECORD_INDICATOR = ?", strNstPend);
				}
			}
		}
		if(tabVb.isVerificationRequired()){
			objParams = new Object[params.size()*4];
		}else{
			objParams = new Object[params.size()];
		}
		for(Ctr=0; Ctr < params.size(); Ctr++)
			objParams[Ctr] = (Object) params.elementAt(Ctr);
		Paginationhelper<TabVb> paginationhelper = new Paginationhelper<TabVb>();
		if(tabVb.isVerificationRequired()){
			for(int Ctr2=0 ; Ctr2 < params.size(); Ctr2++, Ctr++)
				objParams[Ctr] = (Object) params.elementAt(Ctr2);
			for(int Ctr2=0 ; Ctr2 < params.size(); Ctr2++, Ctr++)
				objParams[Ctr] = (Object) params.elementAt(Ctr2);
			for(int Ctr2=0 ; Ctr2 < params.size(); Ctr2++, Ctr++)
				objParams[Ctr] = (Object) params.elementAt(Ctr2);

			/* NST From Cond + (NT Approve + Not Exists) + 'Alias Name' + Join Cond + NST Where Cond
			UNION
			NST PEND From Cond + (NT Approve + Not Exists) + 'Alias Name' + Join Cond + NST Where Cond
			UNION
			NST From Cond + (NT Pend + Not Exists) + 'Alias Name' + Join Cond + NST Where Cond
			UNION
			NST PEND From Cond + (NT Pend + Not Exists) + 'Alias Name' + Join Cond + NST Where Cond
			Order Cond*/
			CommonUtils.addToQuery(strNtNotExists, strNtApprove);
			StringBuffer lPendQuery1 = new StringBuffer(strNstAppFromCond +" ,("+ strNtApprove +") X " + strNstApprove);
			CommonUtils.addToQuery(strNstNotExists, lPendQuery1);
			CommonUtils.addToQuery(strSubQryJoinCond, lPendQuery1);
			StringBuffer lPendQuery2 = new StringBuffer(strNstAppFromCond +" ,("+ strNtPend +") X " + strNstApprove);
			CommonUtils.addToQuery(strNstNotExists, lPendQuery2);
			CommonUtils.addToQuery(strSubQryJoinCond, lPendQuery2);
			StringBuffer lPendQuery3 = new StringBuffer(strNstPendFromCond +" ,("+ strNtApprove +") X " + strNstPend);
			CommonUtils.addToQuery(strSubQryJoinCond, lPendQuery3);
			StringBuffer lPendQuery4 = new StringBuffer(strNstPendFromCond +" ,("+ strNtPend +") X " + strNstPend);
			CommonUtils.addToQuery(strSubQryJoinCond, lPendQuery4);
			String lPendQuery = lPendQuery1 + " UNION " + lPendQuery2 + " UNION " +lPendQuery3 + " UNION " + lPendQuery4 + orderBy;
			if(tabVb.getTotalRows()  <= 0){
				result = paginationhelper.fetchPage(getJdbcTemplate(), lPendQuery, 
						objParams, tabVb.getCurrentPage(), tabVb.getMaxRecords(), getQueryPopupMapper());
				tabVb.setTotalRows(paginationhelper.getTotalRows());
			}else{
				result = paginationhelper.fetchPage(getJdbcTemplate(), lPendQuery, 
						objParams, tabVb.getCurrentPage(), tabVb.getMaxRecords(), tabVb.getTotalRows(), getQueryPopupMapper()); 
			}
		}else{
			//Main Select + Attribute From Cond + (Level Query) + 'Alias Name' + Join Cond + Approve Where Cond + Order
			StringBuffer lApproveQuery = new StringBuffer(strNstAppFromCond +" ,("+ strNtApprove +") X " + strNstApprove);
			CommonUtils.addToQuery(strSubQryJoinCond, lApproveQuery);
			lApproveQuery.append(orderBy);
			if(tabVb.getTotalRows()  <= 0){
				result = paginationhelper.fetchPage(getJdbcTemplate(), lApproveQuery.toString(), 
						objParams, tabVb.getCurrentPage(), tabVb.getMaxRecords(), getQueryPopupMapper());
				tabVb.setTotalRows(paginationhelper.getTotalRows());
			}else{
				result = paginationhelper.fetchPage(getJdbcTemplate(), lApproveQuery.toString(), 
						objParams, tabVb.getCurrentPage(), tabVb.getMaxRecords(), tabVb.getTotalRows(), getQueryPopupMapper()); 
			}
		}
		return result;
	}
	protected void setServiceDefaultsNt(){
		serviceName = "NumTab";
		//serviceDesc = CommonUtils.getResourceManger().getString("numTab");
		serviceDesc = "NumTab";
		tableName = "NUM_TAB";
		childTableName = "NUM_TAB";
		intCurrentUserId = SessionContextHolder.getContext().getVisionId();
	}
	protected void setServiceDefaultsAt(){
		serviceName = "AlphaTab";
		serviceDesc = CommonUtils.getResourceManger().getString("alphaTab");;
		tableName = "ALPHA_TAB";
		childTableName = "ALPHA_TAB";
		intCurrentUserId = SessionContextHolder.getContext().getVisionId();
	}
	@Override
	protected List<TabVb> selectApprovedRecord(TabVb vObject){
		return getQueryResults(vObject, Constants.STATUS_ZERO);
	}

	@Override
	protected List<TabVb> doSelectPendingRecord(TabVb vObject){
		return getQueryResults(vObject, Constants.STATUS_PENDING);
	}
	@Override
	protected ExceptionCode doInsertRecordForNonTrans(TabVb vObject) throws RuntimeCustomException{
		List<TabVb> collTemp = null;
		ExceptionCode exceptionCode = null;
		setServiceDefaults();
		vObject.setMaker(intCurrentUserId);
		collTemp = selectApprovedRecord(vObject);
		if (collTemp == null){
			logger.error("Collection is null for Select Approved Record");
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}

		// If record already exists in the approved table, reject the addition
		if (collTemp.size() > 0 ){
			int intStaticDeletionFlag = getStatus(collTemp.get(0));
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
		// Try to see if the record already exists in the pending table, but not in approved table
		collTemp = null;
		collTemp = doSelectPendingRecord(vObject);

		// The collTemp variable could not be null.  If so, there is no problem fetching data
		// return back error code to calling routine
		if (collTemp == null){
			logger.error("Collection is null for Select Pending Record");
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}

		// if record already exists in pending table, modify the record
		if (collTemp.size() > 0)
		{
			if (getStatus(collTemp.get(0)) == Constants.STATUS_INSERT){
				exceptionCode = getResultObject(Constants.PENDING_FOR_ADD_ALREADY);
				throw buildRuntimeCustomException(exceptionCode);
			}
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		else
		{

			// Try inserting the record
			vObject.setRecordIndicator(Constants.STATUS_INSERT);
			retVal = doInsertionPend(vObject);
			if (retVal != Constants.SUCCESSFUL_OPERATION){
				exceptionCode = getResultObject(retVal);
				throw buildRuntimeCustomException(exceptionCode);
			}
		}
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			for(NumSubTabVb lObject : vObject.getNumSubTabs()){
				if(lObject.isChecked()){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setNumTab(vObject.getTab());
					exceptionCode = getNumSubTabDao().doInsertRecordForNonTrans(lObject);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index = exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Add - Failed - ");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Add - Failed - ", " ");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
				}
			}
		}else{
			for(AlphaSubTabVb lObject : vObject.getAlphaSubTabs()){
				if(lObject.isChecked()){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setAlphaTab(vObject.getTab());
					exceptionCode = getAlphaSubTabDao().doInsertRecordForNonTrans(lObject);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index = exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Add - Failed - ");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Add - Failed - ", " ");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
				}
			}
		}
	   	if (retVal != Constants.SUCCESSFUL_OPERATION){
	   		exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
	   	return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	@Override
	protected ExceptionCode doInsertApprRecordForNonTrans(TabVb vObject) throws RuntimeCustomException{
		List<TabVb> collTemp = null;
		ExceptionCode exceptionCode = null;
		vObject.setMaker(getIntCurrentUserId());
		if("RUNNING".equalsIgnoreCase(getBuildStatus(vObject))){
			exceptionCode = getResultObject(Constants.BUILD_IS_RUNNING);
			throw buildRuntimeCustomException(exceptionCode);
		}
		collTemp = selectApprovedRecord(vObject);
		if (collTemp == null){
			logger.error("Collection is null for Select Approved Record");
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}

		// If record already exists in the approved table, reject the addition
		if (collTemp.size() > 0 ){
			int intStaticDeletionFlag = getStatus(collTemp.get(0));
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
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			for(NumSubTabVb lObject : vObject.getNumSubTabs()){
				if(lObject.isChecked()){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setNumTab(vObject.getTab());
					exceptionCode = getNumSubTabDao().doInsertApprRecordForNonTrans(lObject);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index = exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Add - Failed - ");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Add - Failed - ", " ");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
				}
			}
		}else{
			for(AlphaSubTabVb lObject : vObject.getAlphaSubTabs()){
				if(lObject.isChecked()){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setAlphaTab(vObject.getTab());
					exceptionCode = getAlphaSubTabDao().doInsertApprRecordForNonTrans(lObject);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index = exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Add - Failed - ");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Add - Failed - ", " ");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
				}
			}
		}
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
	@Override
	protected ExceptionCode doUpdateApprRecordForNonTrans(TabVb vObject) throws RuntimeCustomException {
		List<TabVb> collTemp = null;
		strApproveOperation ="Modify";
		strErrorDesc  = "";
		strCurrentOperation = "Modify";
		ExceptionCode exceptionCode = null;
		TabVb vObjectLocal = null;
		boolean updateMain = false;
		boolean isChildSelected = false;
		vObject.setMaker(intCurrentUserId);
		if("RUNNING".equalsIgnoreCase(getBuildStatus(vObject))){
			exceptionCode = getResultObject(Constants.BUILD_IS_RUNNING);
			throw buildRuntimeCustomException(exceptionCode);
		}
		collTemp = selectApprovedRecord(vObject);
		if (collTemp == null){
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		// Even if record is not there in Appr. table reject the record
		if (collTemp.size() == 0){
			exceptionCode = getResultObject(Constants.ATTEMPT_TO_MODIFY_UNEXISTING_RECORD);
			throw buildRuntimeCustomException(exceptionCode);
		}
		vObjectLocal = collTemp.get(0);
		if(!vObjectLocal.compare(vObject)){
			vObject.setRecordIndicator(Constants.STATUS_ZERO);
			vObject.setVerifier(intCurrentUserId);
			vObject.setDateCreation(vObjectLocal.getDateCreation());
			retVal = doUpdateAppr(vObject);
			if (retVal != Constants.SUCCESSFUL_OPERATION){
				exceptionCode = getResultObject(retVal);
				throw buildRuntimeCustomException(exceptionCode);
			}
		}
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			for(NumSubTabVb lObject : vObject.getNumSubTabs()){
				lObject.setVerificationRequired(vObject.isVerificationRequired());
				lObject.setStaticDelete(vObject.isStaticDelete());
				lObject.setNumTab(vObject.getTab());
				if(lObject.isChecked()){
					isChildSelected = true;
					if(lObject.isNewRecord()){
						exceptionCode = getNumSubTabDao().doInsertApprRecordForNonTrans(lObject);
						if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
							int index = exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Add - Failed - ");
							if(index >=0 ){
								strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Add - Failed - ", " ");
							}else{
								strErrorDesc +=" "+exceptionCode.getErrorMsg();
							}
							exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
							throw buildRuntimeCustomException(exceptionCode);
						}
						retVal = exceptionCode.getErrorCode();
					}else{
						exceptionCode = getNumSubTabDao().doUpdateApprRecordForNonTrans(lObject);
						if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
							int index = exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Add - Failed -");
							if(index >=0 ){
								strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Add - Failed -", "-");
							}else{
								strErrorDesc +=" "+exceptionCode.getErrorMsg();
							}
							exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
							throw buildRuntimeCustomException(exceptionCode);
						}
						retVal = exceptionCode.getErrorCode();
					}
				}
			}
		}else{
			for(AlphaSubTabVb lObject : vObject.getAlphaSubTabs()){
				lObject.setVerificationRequired(vObject.isVerificationRequired());
				lObject.setStaticDelete(vObject.isStaticDelete());
				lObject.setAlphaTab(vObject.getTab());
				if(lObject.isChecked()){
					isChildSelected = true;
					if(lObject.isNewRecord()){
						exceptionCode = getAlphaSubTabDao().doInsertApprRecordForNonTrans(lObject);
						if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
							int index = exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Add - Failed - ");
							if(index >=0 ){
								strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Add - Failed - ", " ");
							}else{
								strErrorDesc +=" "+exceptionCode.getErrorMsg();
							}
							exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
							throw buildRuntimeCustomException(exceptionCode);
						}
						retVal = exceptionCode.getErrorCode();
					}else{
						exceptionCode = getAlphaSubTabDao().doUpdateApprRecordForNonTrans(lObject);
						if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
							int index = exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Add - Failed -");
							if(index >=0 ){
								strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Add - Failed -", "-");
							}else{
								strErrorDesc +=" "+exceptionCode.getErrorMsg();
							}
							exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
							throw buildRuntimeCustomException(exceptionCode);
						}
						retVal = exceptionCode.getErrorCode();
					}
				}
			}
		}

		//if no child is selected and header is not changed then just return success 
		if(!isChildSelected && vObjectLocal.compare(vObject))
			retVal = Constants.SUCCESSFUL_OPERATION;
	   	if (retVal != Constants.SUCCESSFUL_OPERATION){
	   		exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
		String systemDate = getSystemDate();
		vObject.setDateLastModified(systemDate);
		if(updateMain){
			exceptionCode =  writeAuditLog(vObject, vObjectLocal);
			if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
				exceptionCode = getResultObject(Constants.AUDIT_TRAIL_ERROR);
				throw buildRuntimeCustomException(exceptionCode);
			}
			return exceptionCode;
		}
		return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	@Override
	protected ExceptionCode doUpdateRecordForNonTrans(TabVb vObject) throws RuntimeCustomException {
		List<TabVb> collTemp = null;
		TabVb vObjectlocal = null;
		ExceptionCode exceptionCode =null;
		setServiceDefaults();
		vObject.setMaker(intCurrentUserId);
		boolean isChildSelected = false;
		// Search if record already exists in pending.  If it already exists, check for status
		collTemp = doSelectPendingRecord(vObject);
		if (collTemp == null){
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		if (collTemp.size() > 0){
			vObjectlocal = collTemp.get(0);
			// Check if the record is pending for deletion. If so return the error
			if (vObjectlocal.getRecordIndicator() == Constants.STATUS_DELETE){
				exceptionCode = getResultObject(Constants.RECORD_PENDING_FOR_DELETION);
				throw buildRuntimeCustomException(exceptionCode);
			}
			vObject.setDateCreation(vObjectlocal.getDateCreation());
			if(!vObjectlocal.compare(vObject)){
				if (vObjectlocal.getRecordIndicator() == Constants.STATUS_INSERT){
					vObject.setVerifier(0);
					vObject.setRecordIndicator(Constants.STATUS_INSERT);
					retVal = doUpdatePend(vObject);
				}else{
					vObject.setVerifier(0);
					vObject.setRecordIndicator(Constants.STATUS_UPDATE);
					retVal = doUpdatePend(vObject);
				}
				if (retVal != Constants.SUCCESSFUL_OPERATION){
					exceptionCode = getResultObject(retVal);
					throw buildRuntimeCustomException(exceptionCode);
				}
			}
		}else{
			collTemp = null;
			collTemp = selectApprovedRecord(vObject);
			if (collTemp == null){
				exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
				throw buildRuntimeCustomException(exceptionCode);
			}
			// Even if record is not there in Appr. table reject the record
			if (collTemp.size() == 0){
				exceptionCode = getResultObject(Constants.ATTEMPT_TO_MODIFY_UNEXISTING_RECORD);
				throw buildRuntimeCustomException(exceptionCode);
			}
			vObjectlocal = collTemp.get(0);
			if(!vObjectlocal.compare(vObject)){
				// Record is there in approved, but not in pending. So add it to pending
				vObject.setRecordIndicator(Constants.STATUS_UPDATE);
				vObject.setVerifier(0);
				retVal = doInsertionPendWithDc(vObject); // For dateCreation Logic
				if (retVal != Constants.SUCCESSFUL_OPERATION){
					exceptionCode = getResultObject(retVal);
					throw buildRuntimeCustomException(exceptionCode);
				}
			}
		}
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			for(NumSubTabVb lObject : vObject.getNumSubTabs()){
				lObject.setVerificationRequired(vObject.isVerificationRequired());
				lObject.setStaticDelete(vObject.isStaticDelete());
				lObject.setNumTab(vObject.getTab());
				if(lObject.isChecked()){
					isChildSelected = true;
					if(lObject.isNewRecord()){
						exceptionCode = getNumSubTabDao().doInsertRecordForNonTrans(lObject);
						if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
							int index = exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Add - Failed - ");
							if(index >=0 ){
								strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Add - Failed - ", " ");
							}else{
								strErrorDesc +=" "+exceptionCode.getErrorMsg();
							}
							exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
							throw buildRuntimeCustomException(exceptionCode);
						}
						retVal = exceptionCode.getErrorCode();
					}else{
						lObject.setVerificationRequired(vObject.isVerificationRequired());
						lObject.setStaticDelete(vObject.isStaticDelete());
						lObject.setNumTab(vObject.getTab());
						exceptionCode = getNumSubTabDao().doUpdateRecordForNonTrans(lObject);
						if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
							int index = exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Modify - Failed -");
							if(index >=0 ){
								strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Modify - Failed -", "-");
							}else{
								strErrorDesc +=" "+exceptionCode.getErrorMsg();
							}
							exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
							throw buildRuntimeCustomException(exceptionCode);
						}
						retVal = exceptionCode.getErrorCode();
					}
				}
			}
		}else{
			for(AlphaSubTabVb lObject : vObject.getAlphaSubTabs()){
				lObject.setVerificationRequired(vObject.isVerificationRequired());
				lObject.setStaticDelete(vObject.isStaticDelete());
				lObject.setAlphaTab(vObject.getTab());
				if(lObject.isChecked()){
					isChildSelected = true;
					if(lObject.isNewRecord()){
						exceptionCode = getAlphaSubTabDao().doInsertRecordForNonTrans(lObject);
						if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
							int index = exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Add - Failed - ");
							if(index >=0 ){
								strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Add - Failed - ", " ");
							}else{
								strErrorDesc +=" "+exceptionCode.getErrorMsg();
							}
							exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
							throw buildRuntimeCustomException(exceptionCode);
						}
						retVal = exceptionCode.getErrorCode();
					}else{
						exceptionCode = getAlphaSubTabDao().doUpdateRecordForNonTrans(lObject);
						if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
							int index = exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Modify - Failed -");
							if(index >=0 ){
								strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Modify - Failed -", "-");
							}else{
								strErrorDesc +=" "+exceptionCode.getErrorMsg();
							}
							exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
							throw buildRuntimeCustomException(exceptionCode);
						}
						retVal = exceptionCode.getErrorCode();
					}
				}
			}
		}
		//if no child is selected and header is not changed then just return success 
		if(!isChildSelected && vObjectlocal.compare(vObject))
			retVal = Constants.SUCCESSFUL_OPERATION;
	   	if (retVal != Constants.SUCCESSFUL_OPERATION){
	   		exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
		String systemDate = getSystemDate();
		vObject.setDateLastModified(systemDate);
		return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	@Override
	protected ExceptionCode doDeleteApprRecordForNonTrans(TabVb vObject){
		ExceptionCode exceptionCode = null;
		strApproveOperation ="Delete";
		strErrorDesc  = "";
		strCurrentOperation = "Delete";
		if("RUNNING".equalsIgnoreCase(getBuildStatus(vObject))){
			exceptionCode = getResultObject(Constants.BUILD_IS_RUNNING);
			throw buildRuntimeCustomException(exceptionCode);
		}
		vObject.setMaker(intCurrentUserId);
		boolean isChildSelected = isChildSelected(vObject); 
		if(isChildSelected)
			return doDeleteApprChildRecord(vObject);
		else
			return doDeleteApprParentRecord(vObject);
	}
	private boolean isChildSelected(TabVb vObject){
		boolean isChildSelected = false;
//		strApproveOperation ="Delete";
		strErrorDesc  = "";
//		strCurrentOperation = "Delete";
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			for(NumSubTabVb lObject :vObject.getNumSubTabs()){
				if(lObject.isChecked()){
					isChildSelected = true;
				}
			}

		}else{
			for(AlphaSubTabVb lObject :vObject.getAlphaSubTabs()){
				if(lObject.isChecked()){
					isChildSelected = true;
				}
			}
		}
		return isChildSelected;
	}
	@Override
	protected ExceptionCode doDeleteRecordForNonTrans(TabVb vObject){
		vObject.setMaker(intCurrentUserId);
		boolean isChildSelected = isChildSelected(vObject);
		if(isChildSelected)
			return doDeleteChildRecord(vObject);
		else
			return doDeleteParentRecord(vObject);
	}
	private ExceptionCode doDeleteParentRecord(TabVb vObject) {
		List<TabVb> collTemp = null;
		ExceptionCode exceptionCode = null;
		TabVb vObjectLocal = null;
		collTemp = selectApprovedRecord(vObject);
		if (collTemp == null){
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		if (collTemp.isEmpty()){
			exceptionCode = getResultObject(Constants.ATTEMPT_TO_DELETE_UNEXISTING_RECORD);
			throw buildRuntimeCustomException(exceptionCode);

		}
		else{
			vObjectLocal = collTemp.get(0);
			if (getStatus(vObjectLocal) == Constants.PASSIVATE){
				exceptionCode = getResultObject(Constants.CANNOT_DELETE_AN_INACTIVE_RECORD);
				throw buildRuntimeCustomException(exceptionCode);
			}
		}
		// check to see if the record already exists in the pending table
		collTemp = doSelectPendingRecord(vObject);
		if (collTemp == null){
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}

		// If records are there, check for the status and decide what error to return back
		if (collTemp.size() > 0){
			exceptionCode = getResultObject(Constants.TRYING_TO_DELETE_APPROVAL_PENDING_RECORD);
			throw buildRuntimeCustomException(exceptionCode);
		}

		// insert the record into pending table with status 3 - deletion
		vObject.setRecordIndicator(Constants.STATUS_DELETE);
		vObject.setVerifier(0);
		vObjectLocal.setVerifier(0);
		vObjectLocal.setMaker(intCurrentUserId);
		vObjectLocal.setRecordIndicator(Constants.STATUS_DELETE);
		vObjectLocal.setRequestType(vObject.getRequestType());
		retVal = doInsertionPendWithDc(vObjectLocal);
	   	if (retVal != Constants.SUCCESSFUL_OPERATION){
	   		exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
	   	if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			NumSubTabVb dObj = new NumSubTabVb();
			dObj.setNumTab(vObject.getTab());
			dObj.setRecordIndicator(0);
			dObj.setVerificationRequired(vObject.isVerificationRequired());
			dObj.setStaticDelete(vObject.isStaticDelete());
			List<NumSubTabVb> numSubTabs = getNumSubTabDao().getQueryResultsByParent(dObj, Constants.STATUS_ZERO);
			for(NumSubTabVb lObject : numSubTabs){
				lObject.setVerificationRequired(vObject.isVerificationRequired());
				lObject.setStaticDelete(vObject.isStaticDelete());
				exceptionCode = getNumSubTabDao().doDeleteRecordForNonTrans(lObject);
				if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
					int index =exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Delete - Failed -");
					if(index >=0 ){
						strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Delete - Failed -", "-");
					}else{
						strErrorDesc +=" "+exceptionCode.getErrorMsg();
					}
					exceptionCode = getResultObject(exceptionCode.getErrorCode());
					throw buildRuntimeCustomException(exceptionCode);
				}
				retVal = exceptionCode.getErrorCode();
				updateRIofChildDelete(lObject, vObject.getNumSubTabs());
			}
	   	}else{
			AlphaSubTabVb dObj = new AlphaSubTabVb();
			dObj.setAlphaTab(vObject.getTab());
			dObj.setRecordIndicator(0);
			dObj.setVerificationRequired(vObject.isVerificationRequired());
			dObj.setStaticDelete(vObject.isStaticDelete());
			List<AlphaSubTabVb> alphaSubTabs = getAlphaSubTabDao().getQueryResultsByParent(dObj, Constants.STATUS_ZERO);
			for(AlphaSubTabVb lObject : alphaSubTabs){
				lObject.setVerificationRequired(vObject.isVerificationRequired());
				lObject.setStaticDelete(vObject.isStaticDelete());
				exceptionCode = getAlphaSubTabDao().doDeleteRecordForNonTrans(lObject);
				if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
					int index =exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Delete - Failed -");
					if(index >=0 ){
						strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Delete - Failed -", "-");
					}else{
						strErrorDesc +=" "+exceptionCode.getErrorMsg();
					}
					exceptionCode = getResultObject(exceptionCode.getErrorCode());
					throw buildRuntimeCustomException(exceptionCode);
				}
				retVal = exceptionCode.getErrorCode();
				updateRIofChildDelete(lObject, vObject.getAlphaSubTabs());
			}
	   	}
	   	if (retVal != Constants.SUCCESSFUL_OPERATION){
	   		exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
	   	return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	private void updateRIofChildDelete(AlphaSubTabVb pObject,List<AlphaSubTabVb> alphaSubTabs){
		if(alphaSubTabs == null || alphaSubTabs.isEmpty() || pObject == null)
			return;
		for(AlphaSubTabVb lObject :alphaSubTabs){
			if(lObject.getAlphaSubTab().equalsIgnoreCase(pObject.getAlphaSubTab())){
				lObject.setRecordIndicator(pObject.getRecordIndicator());
				lObject.setAlphaSubTabStatus(pObject.getAlphaSubTabStatus());
				lObject.setMaker(intCurrentUserId);
				lObject.setVerifier(0);
			}
		}
	}
	private void updateRIofChildDelete(NumSubTabVb pObject,List<NumSubTabVb> numSubTabs){
		if(numSubTabs == null || numSubTabs.isEmpty() || pObject == null)
			return;
		for(NumSubTabVb lObject :numSubTabs){
			if(lObject.getNumSubTab() == pObject.getNumSubTab()){
				lObject.setRecordIndicator(pObject.getRecordIndicator());
				lObject.setNumSubTabStatus(pObject.getNumSubTabStatus());
				lObject.setMaker(intCurrentUserId);
				lObject.setVerifier(0);
			}
		}
	}
	private ExceptionCode doDeleteChildRecord(TabVb vObject) {
		ExceptionCode exceptionCode = null;
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			for(NumSubTabVb lObject : vObject.getNumSubTabs()){
				if(lObject.isChecked()){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setNumTab(vObject.getTab());
					exceptionCode = getNumSubTabDao().doDeleteRecordForNonTrans(lObject);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index =exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Delete - Failed -");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Delete - Failed -", "-");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(exceptionCode.getErrorCode());
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
				}
			}
		}else{
			for(AlphaSubTabVb lObject : vObject.getAlphaSubTabs()){
				if(lObject.isChecked()){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setAlphaTab(vObject.getTab());
					exceptionCode = getAlphaSubTabDao().doDeleteRecordForNonTrans(lObject);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index =exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Delete - Failed -");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Delete - Failed -", "-");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(exceptionCode.getErrorCode());
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
				}
			}
		}
	   	if (retVal != Constants.SUCCESSFUL_OPERATION){
	   		exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
	   	return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	private ExceptionCode doDeleteApprParentRecord(TabVb vObject){
		List<TabVb> collTemp = null;
		ExceptionCode exceptionCode = null;
		TabVb vObjectLocal = null;
		collTemp = selectApprovedRecord(vObject);
		if (collTemp == null){
			exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
			throw buildRuntimeCustomException(exceptionCode);
		}
		// If record already exists in the approved table, reject the addition
		if (collTemp.isEmpty()){
			exceptionCode = getResultObject(Constants.ATTEMPT_TO_DELETE_UNEXISTING_RECORD);
			throw buildRuntimeCustomException(exceptionCode);

		}else{
			vObjectLocal = collTemp.get(0);
			if (getStatus(vObjectLocal) == Constants.PASSIVATE){
				exceptionCode = getResultObject(Constants.CANNOT_DELETE_AN_INACTIVE_RECORD);
				throw buildRuntimeCustomException(exceptionCode);
			}
		}
		if(vObject.isStaticDelete()){
			vObjectLocal.setMaker(intCurrentUserId);
			vObjectLocal.setVerifier(intCurrentUserId);
			vObjectLocal.setRecordIndicator(Constants.STATUS_ZERO);
			setStatus(vObjectLocal, Constants.PASSIVATE);
			setStatus(vObject, Constants.PASSIVATE);
			vObject.setRecordIndicator(Constants.STATUS_ZERO);
			vObject.setDateCreation(vObjectLocal.getDateCreation());
			retVal = doUpdateAppr(vObjectLocal);
			String systemDate = getSystemDate();
			vObject.setDateLastModified(systemDate);
		}else{
			//delete the record from the Approve Table
			retVal = doDeleteAppr(vObject);
			vObject.setRecordIndicator(-1);
			String systemDate = getSystemDate();
			vObject.setDateLastModified(systemDate);
		}
		if (retVal != Constants.SUCCESSFUL_OPERATION){
			exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
	   	if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			NumSubTabVb dObj = new NumSubTabVb();
			dObj.setNumTab(vObject.getTab());
			dObj.setRecordIndicator(0);
			dObj.setVerificationRequired(vObject.isVerificationRequired());
			dObj.setStaticDelete(vObject.isStaticDelete());
			List<NumSubTabVb> numSubTabs = getNumSubTabDao().getQueryResults(dObj, Constants.STATUS_ZERO);
			for(NumSubTabVb lObject : numSubTabs){
				lObject.setVerificationRequired(vObject.isVerificationRequired());
				lObject.setStaticDelete(vObject.isStaticDelete());
				exceptionCode = getNumSubTabDao().doDeleteApprRecordForNonTrans(lObject);
				if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
					int index =exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Delete - Failed -");
					if(index >=0 ){
						strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Delete - Failed -", "-");
					}else{
						strErrorDesc +=" "+exceptionCode.getErrorMsg();
					}
					exceptionCode = getResultObject(exceptionCode.getErrorCode());
					throw buildRuntimeCustomException(exceptionCode);
				}
				retVal = exceptionCode.getErrorCode();
				updateofChildDelete(lObject, vObject.getNumSubTabs());
			}
	   	}else{
			AlphaSubTabVb dObj = new AlphaSubTabVb();
			dObj.setAlphaTab(vObject.getTab());
			dObj.setRecordIndicator(0);
			dObj.setVerificationRequired(vObject.isVerificationRequired());
			dObj.setStaticDelete(vObject.isStaticDelete());
			List<AlphaSubTabVb> alphaSubTabs = getAlphaSubTabDao().getQueryResults(dObj, Constants.STATUS_ZERO);
			for(AlphaSubTabVb lObject : alphaSubTabs){
				lObject.setVerificationRequired(vObject.isVerificationRequired());
				lObject.setStaticDelete(vObject.isStaticDelete());
				exceptionCode = getAlphaSubTabDao().doDeleteApprRecordForNonTrans(lObject);
				if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
					int index =exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Delete - Failed -");
					if(index >=0 ){
						strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Delete - Failed -", "-");
					}else{
						strErrorDesc +=" "+exceptionCode.getErrorMsg();
					}
					exceptionCode = getResultObject(exceptionCode.getErrorCode());
					throw buildRuntimeCustomException(exceptionCode);
				}
				retVal = exceptionCode.getErrorCode();
				updateofChildDelete(lObject, vObject.getAlphaSubTabs());
			}
	   	}
	   	if (retVal != Constants.SUCCESSFUL_OPERATION){
			exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
	   	return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	private void updateofChildDelete(NumSubTabVb vObject,List<NumSubTabVb> numSubTabs){
		if(numSubTabs == null || numSubTabs.isEmpty() || vObject == null)
			return;
		for(NumSubTabVb lObject :numSubTabs){
			if(lObject.getNumSubTab() ==vObject.getNumSubTab()){
				lObject.setRecordIndicator(vObject.getRecordIndicator());
				lObject.setNumSubTabStatus(vObject.getNumSubTabStatus());
			}
		}
	}
	private void updateofChildDelete(AlphaSubTabVb vObject,List<AlphaSubTabVb> alphaSubTabs){
		if(alphaSubTabs == null || alphaSubTabs.isEmpty() || vObject == null)
			return;
		for(AlphaSubTabVb lObject :alphaSubTabs){
			if(lObject.getAlphaSubTab().equalsIgnoreCase(vObject.getAlphaSubTab())){
				lObject.setRecordIndicator(vObject.getRecordIndicator());
				lObject.setAlphaSubTabStatus(vObject.getAlphaSubTabStatus());
			}
		}
	}
	private ExceptionCode doDeleteApprChildRecord(TabVb vObject){
		ExceptionCode exceptionCode = null;
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			for(NumSubTabVb lObject : vObject.getNumSubTabs()){
				if(lObject.isChecked()){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setNumTab(vObject.getTab());
					exceptionCode = getNumSubTabDao().doDeleteApprRecordForNonTrans(lObject);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index =exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Delete - Failed -");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Delete - Failed -", "-");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(exceptionCode.getErrorCode());
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
				}
			}
		}else{
			for(AlphaSubTabVb lObject : vObject.getAlphaSubTabs()){
				if(lObject.isChecked()){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setAlphaTab(vObject.getTab());
					exceptionCode = getAlphaSubTabDao().doDeleteApprRecordForNonTrans(lObject);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index =exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Delete - Failed -");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Delete - Failed -", "-");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(exceptionCode.getErrorCode());
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
				}
			}
		}
	   	if (retVal != Constants.SUCCESSFUL_OPERATION){
	   		exceptionCode = getResultObject(retVal);
			throw buildRuntimeCustomException(exceptionCode);
		}
	   	return getResultObject(Constants.SUCCESSFUL_OPERATION);
	}
	@Override
	public ExceptionCode doApproveRecord(TabVb vObject, boolean staticDelete) throws RuntimeCustomException {
		TabVb oldContents = null;
		TabVb vObjectlocal = null;
		List<TabVb> collTemp = null;
		ExceptionCode exceptionCode = null;
		strCurrentOperation = "Approve";
		setServiceDefaults();
		try {
			if("RUNNING".equalsIgnoreCase(getBuildStatus(vObject))){
				exceptionCode = getResultObject(Constants.BUILD_IS_RUNNING_APPROVE);
				throw buildRuntimeCustomException(exceptionCode);
			}
			boolean isChildSelected = isChildSelected(vObject);
			if(isChildSelected && vObject.getRecordIndicator() != 2){
				if("numTab".equalsIgnoreCase(vObject.getRequestType())){
					for(NumSubTabVb lObject : vObject.getNumSubTabs()){
						if(lObject.isChecked()){
							lObject.setVerificationRequired(vObject.isVerificationRequired());
							lObject.setStaticDelete(vObject.isStaticDelete());
							lObject.setNumTab(vObject.getTab());
							exceptionCode = getNumSubTabDao().doApproveRecord(lObject,staticDelete);
							if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
								int index =exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Approve - Failed -");
								if(index >=0 ){
									strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Approve - Failed -", "-");
								}else{
									strErrorDesc +=" "+exceptionCode.getErrorMsg();
								}
								exceptionCode = getResultObject(exceptionCode.getErrorCode());
								throw buildRuntimeCustomException(exceptionCode);
							}
							retVal = exceptionCode.getErrorCode();
						}
					}
				}else{
					for(AlphaSubTabVb lObject : vObject.getAlphaSubTabs()){
						if(lObject.isChecked()){
							lObject.setVerificationRequired(vObject.isVerificationRequired());
							lObject.setStaticDelete(vObject.isStaticDelete());
							lObject.setAlphaTab(vObject.getTab());
							exceptionCode = getAlphaSubTabDao().doApproveRecord(lObject,staticDelete);
							if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
								int index =exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Approve - Failed -");
								if(index >=0 ){
									strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Approve - Failed -", "-");
								}else{
									strErrorDesc +=" "+exceptionCode.getErrorMsg();
								}
								exceptionCode = getResultObject(exceptionCode.getErrorCode());
								throw buildRuntimeCustomException(exceptionCode);
							}
							retVal = exceptionCode.getErrorCode();
						}
					}
				}
			   	if (retVal != Constants.SUCCESSFUL_OPERATION){
			   		exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
					throw buildRuntimeCustomException(exceptionCode);
				}
			   	return getResultObject(Constants.SUCCESSFUL_OPERATION);
			}
			
			// See if such a pending request exists in the pending table
			collTemp = doSelectPendingRecord(vObject);
			
			if (collTemp == null){
				exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
				throw buildRuntimeCustomException(exceptionCode);
			}
			// If record already exists in the approved table, reject the addition
			if (collTemp.isEmpty()){
				exceptionCode = getResultObject(Constants.NO_SUCH_PENDING_RECORD);
				throw buildRuntimeCustomException(exceptionCode);
			}

			vObjectlocal = collTemp.get(0);
			vObjectlocal.setRequestType(vObject.getRequestType());
			vObjectlocal.setVerifier(intCurrentUserId);
			if (vObjectlocal.getMaker() == intCurrentUserId){
				exceptionCode = getResultObject(Constants.MAKER_CANNOT_APPROVE);
				throw buildRuntimeCustomException(exceptionCode);
			}
			// If it's NOT addition, collect the existing record contents from the
			// Approved table and keep it aside, for writing audit information later.
			if (vObjectlocal.getRecordIndicator() != Constants.STATUS_INSERT)
			{
				collTemp = selectApprovedRecord(vObject);
				if (collTemp == null){
					exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
					throw buildRuntimeCustomException(exceptionCode);
				}
				oldContents = collTemp.get(0);
			}
			if (vObject.getRecordIndicator() == Constants.STATUS_INSERT){  // Add authorization
				// Write the contents of the Pending table record to the Approved table
				vObjectlocal.setRecordIndicator(Constants.STATUS_ZERO);
				vObjectlocal.setVerifier(getIntCurrentUserId());
				retVal = doInsertionAppr(vObjectlocal);
				if (retVal != Constants.SUCCESSFUL_OPERATION){
					exceptionCode = getResultObject(retVal);
					throw buildRuntimeCustomException(exceptionCode);
				}
				String systemDate = getSystemDate();
				vObject.setDateLastModified(systemDate);
				vObject.setDateCreation(systemDate);
				// Set the current operation to write to audit log
				strApproveOperation = "Add";
			}
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				List<NumSubTabVb> numSubTabs =  vObject.getNumSubTabs();
				//If None of the child is selected make a query and get all leboffsets for lebook.
				if(!isChildSelected){
					NumSubTabVb dObj = new NumSubTabVb();
					dObj.setNumTab(vObject.getTab());
					dObj.setRecordIndicator(4);
					dObj.setVerificationRequired(vObject.isVerificationRequired());
					dObj.setStaticDelete(vObject.isStaticDelete());
					numSubTabs = getNumSubTabDao().getQueryPopupResults(dObj);
				}
				for(NumSubTabVb lObject : numSubTabs){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setNumTab(vObject.getTab());
					exceptionCode = getNumSubTabDao().doApproveRecord(lObject,staticDelete);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index =exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Approve - Failed -");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Approve - Failed -", "-");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(exceptionCode.getErrorCode());
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
					if(!isChildSelected){
						updateofChildDelete(lObject, vObject.getNumSubTabs());
					}
				}
				
			}else{
				List<AlphaSubTabVb> alphaSubTabs =  vObject.getAlphaSubTabs();
				//If None of the child is selected make a query and get all leboffsets for lebook.
				if(!isChildSelected){
					AlphaSubTabVb dObj = new AlphaSubTabVb();
					dObj.setAlphaTab(vObject.getTab());
					dObj.setRecordIndicator(4);
					dObj.setVerificationRequired(vObject.isVerificationRequired());
					dObj.setStaticDelete(vObject.isStaticDelete());
					alphaSubTabs = getAlphaSubTabDao().getQueryPopupResults(dObj);
				}
				for(AlphaSubTabVb lObject : alphaSubTabs){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setAlphaTab(vObject.getTab());
					exceptionCode = getAlphaSubTabDao().doApproveRecord(lObject,staticDelete);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index =exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Approve - Failed -");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Approve - Failed -", "-");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(exceptionCode.getErrorCode());
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
					if(!isChildSelected){
						updateofChildDelete(lObject, vObject.getAlphaSubTabs());
					}
				}
			}
			if (vObject.getRecordIndicator() == Constants.STATUS_INSERT){  // Add authorization
				//Empty condition to handle if condition. This is already handled above.  
			}else if (vObject.getRecordIndicator() == Constants.STATUS_UPDATE){  // Modify authorization
				collTemp = selectApprovedRecord(vObject);
				if (collTemp == null){
					exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
					throw buildRuntimeCustomException(exceptionCode);
				}
				// If record already exists in the approved table, reject the addition
				if (collTemp.size() > 0 ){
					vObjectlocal.setRecordIndicator(Constants.STATUS_ZERO);
					vObjectlocal.setVerifier(getIntCurrentUserId());
					retVal = doUpdateAppr(vObjectlocal);
				}

				// Modify the existing contents of the record in Approved table

				if (retVal != Constants.SUCCESSFUL_OPERATION){
					exceptionCode = getResultObject(retVal);
					throw buildRuntimeCustomException(exceptionCode);
				}
				String systemDate = getSystemDate();
				vObject.setDateLastModified(systemDate);
				// Set the current operation to write to audit log
				strApproveOperation = "Modify";
			}else if (vObject.getRecordIndicator() == Constants.STATUS_DELETE){ // Delete authorization
				if(staticDelete){
					// Update the existing record status in the Approved table to delete
					vObjectlocal.setRecordIndicator(Constants.STATUS_ZERO);
					setStatus(vObjectlocal, Constants.PASSIVATE);
					retVal = doUpdateAppr(vObjectlocal);
					setStatus(vObject, Constants.PASSIVATE);
					String systemDate = getSystemDate();
					vObject.setDateLastModified(systemDate);
				}else{
					// Delete the existing record from the Approved table 
					retVal = doDeleteAppr(vObjectlocal);
					vObject.setRecordIndicator(-1);//monday
					String systemDate = getSystemDate();
					vObject.setDateLastModified(systemDate);
				}
				if (retVal != Constants.SUCCESSFUL_OPERATION){
					exceptionCode = getResultObject(retVal);
					throw buildRuntimeCustomException(exceptionCode);
				}
				// Set the current operation to write to audit log
				strApproveOperation = "Delete";
			}
			else{
				exceptionCode = getResultObject(Constants.INVALID_STATUS_FLAG_IN_DATABASE);
				throw buildRuntimeCustomException(exceptionCode);
			}

			// Delete the record from the Pending table
			retVal = deletePendingRecord(vObjectlocal);
			// Set the internal status to Approved
			vObject.setInternalStatus(0);
			vObject.setRecordIndicator(Constants.STATUS_ZERO);
			if (vObjectlocal.getRecordIndicator() == Constants.STATUS_DELETE && !vObject.isStaticDelete()){
				exceptionCode = writeAuditLog(null, oldContents);
				vObject.setRecordIndicator(-1);
			}
			else
				exceptionCode = writeAuditLog(vObjectlocal, oldContents);

			if(exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
				exceptionCode = getResultObject(Constants.AUDIT_TRAIL_ERROR);
				throw buildRuntimeCustomException(exceptionCode);
			}
			return getResultObject(Constants.SUCCESSFUL_OPERATION);
		}catch (UncategorizedSQLException uSQLEcxception) {
			strErrorDesc = parseErrorMsg(uSQLEcxception);
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}catch(Exception ex){
			logger.error("Error in Approve.",ex);
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			strErrorDesc = ex.getMessage();
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
	}
	@Override
	public ExceptionCode doRejectRecord(TabVb vObject)throws RuntimeCustomException {
		TabVb vObjectlocal = null;
		List<TabVb> collTemp = null;
		ExceptionCode exceptionCode = null;
		strCurrentOperation = "Reject";
		try {
			boolean isChildSelected = isChildSelected(vObject);
			if(isChildSelected || vObject.getRecordIndicator() == 0 ){
				if("numTab".equalsIgnoreCase(vObject.getRequestType())){
					for(NumSubTabVb lObject : vObject.getNumSubTabs()){
						if(lObject.isChecked() && lObject.getRecordIndicator() != 0){
							lObject.setVerificationRequired(vObject.isVerificationRequired());
							lObject.setStaticDelete(vObject.isStaticDelete());
							lObject.setNumTab(vObject.getTab());
							exceptionCode = getNumSubTabDao().doRejectRecord(lObject);
							if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
								int index =exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Reject - Failed -");
								if(index >=0 ){
									strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Reject - Failed -", "-");
								}else{
									strErrorDesc +=" "+exceptionCode.getErrorMsg();
								}
								exceptionCode = getResultObject(exceptionCode.getErrorCode());
								throw buildRuntimeCustomException(exceptionCode);
							}
							retVal = exceptionCode.getErrorCode();
						}
					}
				}else{
					for(AlphaSubTabVb lObject : vObject.getAlphaSubTabs()){
						if(lObject.isChecked() && lObject.getRecordIndicator() != 0){
							lObject.setVerificationRequired(vObject.isVerificationRequired());
							lObject.setStaticDelete(vObject.isStaticDelete());
							lObject.setAlphaTab(vObject.getTab());
							exceptionCode = getAlphaSubTabDao().doRejectRecord(lObject);
							if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
								int index =exceptionCode.getErrorMsg().trim().indexOf("Alpha SUb Tab - Reject - Failed -");
								if(index >=0 ){
									strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha SUb Tab - Reject - Failed -", "-");
								}else{
									strErrorDesc +=" "+exceptionCode.getErrorMsg();
								}
								exceptionCode = getResultObject(exceptionCode.getErrorCode());
								throw buildRuntimeCustomException(exceptionCode);
							}
							retVal = exceptionCode.getErrorCode();
						}
					}
				}
			   	if (retVal != Constants.SUCCESSFUL_OPERATION){
			   		exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
					throw buildRuntimeCustomException(exceptionCode);
				}
			   	return getResultObject(Constants.SUCCESSFUL_OPERATION);
			}
			if(vObject.getRecordIndicator() == 1 || vObject.getRecordIndicator() == 3 )
				vObject.setRecordIndicator(0);
			else
				vObject.setRecordIndicator(-1);
			// See if such a pending request exists in the pending table
			collTemp = doSelectPendingRecord(vObject);
			if (collTemp == null){
				exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
				throw buildRuntimeCustomException(exceptionCode);
			}
			if (collTemp.size() == 0){
				exceptionCode = getResultObject(Constants.NO_SUCH_PENDING_RECORD);
				throw buildRuntimeCustomException(exceptionCode);
			}
			vObjectlocal = collTemp.get(0);
			vObjectlocal.setRequestType(vObject.getRequestType());
			// Delete the record from the Pending table
			if (deletePendingRecord(vObjectlocal) == 0){
				exceptionCode = getResultObject(Constants.ERRONEOUS_OPERATION);
				throw buildRuntimeCustomException(exceptionCode);
			}
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				NumSubTabVb dObj = new NumSubTabVb();
				dObj.setNumTab(vObject.getTab());
				dObj.setRecordIndicator(4);
				dObj.setVerificationRequired(vObject.isVerificationRequired());
				dObj.setStaticDelete(vObject.isStaticDelete());
				List<NumSubTabVb> numSubTabs = getNumSubTabDao().getQueryPopupResults(dObj);
				for(NumSubTabVb lObject : numSubTabs){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					lObject.setNumTab(vObject.getTab());
					exceptionCode = getNumSubTabDao().doRejectRecord(lObject);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index =exceptionCode.getErrorMsg().trim().indexOf("Num Sub Tab - Reject - Failed -");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Num Sub Tab - Reject - Failed -", "-");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(exceptionCode.getErrorCode());
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
					updateRIofChildDelete(lObject, vObject.getNumSubTabs());
				}
				
			}else{
				AlphaSubTabVb dObj = new AlphaSubTabVb();
				dObj.setAlphaTab(vObject.getTab());
				dObj.setRecordIndicator(4);
				dObj.setVerificationRequired(vObject.isVerificationRequired());
				dObj.setStaticDelete(vObject.isStaticDelete());
				List<AlphaSubTabVb> alphaSubTabs = getAlphaSubTabDao().getQueryPopupResults(dObj);
				for(AlphaSubTabVb lObject : alphaSubTabs){
					lObject.setVerificationRequired(vObject.isVerificationRequired());
					lObject.setStaticDelete(vObject.isStaticDelete());
					exceptionCode = getAlphaSubTabDao().doRejectRecord(lObject);
					if (exceptionCode.getErrorCode() != Constants.SUCCESSFUL_OPERATION){
						int index =exceptionCode.getErrorMsg().trim().indexOf("Alpha Sub Tab - Reject - Failed -");
						if(index >=0 ){
							strErrorDesc +=" "+exceptionCode.getErrorMsg().replaceFirst("Alpha Sub Tab - Reject - Failed -", "-");
						}else{
							strErrorDesc +=" "+exceptionCode.getErrorMsg();
						}
						exceptionCode = getResultObject(exceptionCode.getErrorCode());
						throw buildRuntimeCustomException(exceptionCode);
					}
					retVal = exceptionCode.getErrorCode();
					updateRIofChildDelete(lObject, vObject.getAlphaSubTabs());
				}
				
			}
		   	if (retVal != Constants.SUCCESSFUL_OPERATION){
		   		exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
				throw buildRuntimeCustomException(exceptionCode);
			}
			return getResultObject(Constants.SUCCESSFUL_OPERATION);
		}catch (UncategorizedSQLException uSQLEcxception) {
			strErrorDesc = parseErrorMsg(uSQLEcxception);
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}catch(Exception ex){
			logger.error("Error in Reject.",ex);
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			strErrorDesc = ex.getMessage();
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
	}
	@Override
	protected int doInsertionAppr(TabVb vObject){
		String queryAlpha = "Insert Into ALPHA_TAB( " + 
			"ALPHA_TAB, ALPHA_TAB_DESCRIPTION, ALPHA_TAB_STATUS_NT, ALPHA_TAB_STATUS, RECORD_INDICATOR_NT, " + 
			" RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION) " + 
			" Values (?, ?, ?, ?, ?, ?, ?, ?, ?, SysDate, SysDate)";
		String query = "Insert Into NUM_TAB( " + 
			"NUM_TAB, NUM_TAB_DESCRIPTION, NUM_TAB_STATUS_NT, NUM_TAB_STATUS, RECORD_INDICATOR_NT, " + 
			" RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION) " + 
			" Values (?, ?, ?, ?, ?, ?, ?, ?, ?, SysDate, SysDate)";

		Object args[] = {vObject.getTab(), vObject.getTabDescription(), vObject.getTabStatusNt(),
			vObject.getTabStatus(), vObject.getRecordIndicatorNt(), vObject.getRecordIndicator(),
			vObject.getMaker(), vObject.getVerifier(), vObject.getInternalStatus()};
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			return getJdbcTemplate().update(query,args);
		}else{
			return getJdbcTemplate().update(queryAlpha,args);
		}

	}

	@Override
	protected int doInsertionPend(TabVb vObject){
		String queryAlpha = "Insert Into ALPHA_TAB_PEND( " + 
			"ALPHA_TAB, ALPHA_TAB_DESCRIPTION, ALPHA_TAB_STATUS_NT, ALPHA_TAB_STATUS, RECORD_INDICATOR_NT, " + 
			" RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION) " + 
			" Values (?, ?, ?, ?, ?, ?, ?, ?, ?, SysDate, SysDate)";
		String query = "Insert Into NUM_TAB_PEND( " + 
			"NUM_TAB, NUM_TAB_DESCRIPTION, NUM_TAB_STATUS_NT, NUM_TAB_STATUS, RECORD_INDICATOR_NT, " + 
			" RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION) " + 
			" Values (?, ?, ?, ?, ?, ?, ?, ?, ?, SysDate, SysDate)";
		Object args[] = {vObject.getTab(), vObject.getTabDescription(), vObject.getTabStatusNt(),
			 vObject.getTabStatus(), vObject.getRecordIndicatorNt(), vObject.getRecordIndicator(),
			 vObject.getMaker(), vObject.getVerifier(), vObject.getInternalStatus()};

		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			return getJdbcTemplate().update(query,args);
		}else{
			return getJdbcTemplate().update(queryAlpha,args);
		}
	}

	@Override
	protected int doInsertionPendWithDc(TabVb vObject){
		String queryAlpha = "Insert Into ALPHA_TAB_PEND( " + 
			"ALPHA_TAB, ALPHA_TAB_DESCRIPTION, ALPHA_TAB_STATUS_NT, ALPHA_TAB_STATUS, RECORD_INDICATOR_NT, " + 
			" RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION) " + 
			"Values (?, ?, ?, ?, ?, ?, ?, ?, ?, SysDate,  " + 
			"To_Date(?, 'DD-MM-RRRR HH24:MI:SS'))";
		String query = "Insert Into NUM_TAB_PEND( " + 
			"NUM_TAB, NUM_TAB_DESCRIPTION, NUM_TAB_STATUS_NT, NUM_TAB_STATUS, RECORD_INDICATOR_NT, " + 
			" RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION) " + 
			"Values (?, ?, ?, ?, ?, ?, ?, ?, ?, SysDate,  " + 
			"To_Date(?, 'DD-MM-RRRR HH24:MI:SS'))";
		Object args[] = {vObject.getTab(), vObject.getTabDescription(), vObject.getTabStatusNt(), 
			vObject.getTabStatus(), vObject.getRecordIndicatorNt(), vObject.getRecordIndicator(), 
			vObject.getMaker(), vObject.getVerifier(), vObject.getInternalStatus(), vObject.getDateCreation()};
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			return getJdbcTemplate().update(query,args);
		}else{
			return getJdbcTemplate().update(queryAlpha,args);
		}
	}

	@Override
	protected int doUpdateAppr(TabVb vObject){
		String queryAlpha = "Update ALPHA_TAB Set " + 
			"ALPHA_TAB_DESCRIPTION = ?, ALPHA_TAB_STATUS_NT = ?, ALPHA_TAB_STATUS = ?, RECORD_INDICATOR_NT = ?, " + 
			" RECORD_INDICATOR = ?, MAKER = ?, VERIFIER = ?, INTERNAL_STATUS = ?, DATE_LAST_MODIFIED = SysDate " + 
			"Where ALPHA_TAB = ? ";
		String query = "Update NUM_TAB Set " + 
			"NUM_TAB_DESCRIPTION = ?, NUM_TAB_STATUS_NT = ?, NUM_TAB_STATUS = ?, RECORD_INDICATOR_NT = ?, " + 
			" RECORD_INDICATOR = ?, MAKER = ?, VERIFIER = ?, INTERNAL_STATUS = ?, DATE_LAST_MODIFIED = SysDate " + 
			"Where NUM_TAB = ? ";
		Object args[] = {vObject.getTabDescription(), vObject.getTabStatusNt(),
			vObject.getTabStatus(), vObject.getRecordIndicatorNt(), vObject.getRecordIndicator(),
			vObject.getMaker(), vObject.getVerifier(), vObject.getInternalStatus(), 
			vObject.getTab()};
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			return getJdbcTemplate().update(query,args);
		}else{
			return getJdbcTemplate().update(queryAlpha,args);
		}
	}

	@Override
	protected int doUpdatePend(TabVb vObject){
		String queryAlpha = "Update ALPHA_TAB_PEND Set " + 
			"ALPHA_TAB_DESCRIPTION = ?, ALPHA_TAB_STATUS_NT = ?, ALPHA_TAB_STATUS = ?, RECORD_INDICATOR_NT = ?, " +
			"RECORD_INDICATOR = ?, MAKER = ?, VERIFIER = ?, INTERNAL_STATUS = ?, DATE_LAST_MODIFIED = SysDate " + 
			"Where ALPHA_TAB = ? ";
		String query = "Update NUM_TAB_PEND Set " + 
			"NUM_TAB_DESCRIPTION = ?, NUM_TAB_STATUS_NT = ?, NUM_TAB_STATUS = ?, RECORD_INDICATOR_NT = ?, " +
			"RECORD_INDICATOR = ?, MAKER = ?, VERIFIER = ?, INTERNAL_STATUS = ?, DATE_LAST_MODIFIED = SysDate " + 
			"Where NUM_TAB = ? ";
		Object args[] = {vObject.getTabDescription(), vObject.getTabStatusNt(),
			vObject.getTabStatus(), vObject.getRecordIndicatorNt(), vObject.getRecordIndicator(), 
			vObject.getMaker(), vObject.getVerifier(), vObject.getInternalStatus(),
			vObject.getTab()};
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			return getJdbcTemplate().update(query,args);
		}else{
			return getJdbcTemplate().update(queryAlpha,args);
		}
	}

	@Override
	protected int doDeleteAppr(TabVb vObject){
		String queryAlpha = "Delete From ALPHA_TAB Where ALPHA_TAB = ? " ;
		String query = "Delete From NUM_TAB Where NUM_TAB = ? " ;
		Object args[] = {vObject.getTab()};
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			return getJdbcTemplate().update(query,args);
		}else{
			return getJdbcTemplate().update(queryAlpha,args);
		}
	}

	@Override
	protected int deletePendingRecord(TabVb vObject){
		String queryAlpha = "Delete From ALPHA_TAB_PEND Where ALPHA_TAB = ? " ;
		String query = "Delete From NUM_TAB_PEND Where NUM_TAB = ? " ;
		Object args[] = {vObject.getTab()};
		if("numTab".equalsIgnoreCase(vObject.getRequestType())){
			return getJdbcTemplate().update(query,args);
		}else{
			return getJdbcTemplate().update(queryAlpha,args);
		}
	}
	@Override
	protected String getAuditString(TabVb vObject)
	{
		StringBuffer strAudit = new StringBuffer("");
		try
		{
			strAudit.append(vObject.getTab());
			strAudit.append("!|#");
			if(vObject.getTabDescription() != null)
				strAudit.append(vObject.getTabDescription().trim());
			else
				strAudit.append("NULL");
			strAudit.append("!|#");
			strAudit.append(vObject.getTabStatusNt());
			strAudit.append("!|#");
			strAudit.append(vObject.getTabStatus());
			strAudit.append("!|#");
			strAudit.append(vObject.getRecordIndicatorNt());
			strAudit.append("!|#");
			strAudit.append(vObject.getRecordIndicator());
			strAudit.append("!|#");
			strAudit.append(vObject.getMaker());
			strAudit.append("!|#");
			strAudit.append(vObject.getVerifier());
			strAudit.append("!|#");
			strAudit.append(vObject.getInternalStatus());
			strAudit.append("!|#");
			if(vObject.getDateLastModified() != null)
				strAudit.append(vObject.getDateLastModified().trim());
			else
				strAudit.append("NULL");
			strAudit.append("!|#");

			if(vObject.getDateCreation() != null)
				strAudit.append(vObject.getDateCreation().trim());
			else
				strAudit.append("NULL");
			strAudit.append("!|#");
		}
		catch(Exception ex)
		{
			strErrorDesc = ex.getMessage();
			strAudit = strAudit.append(strErrorDesc);
			ex.printStackTrace();
		}
		return strAudit.toString();
	}
	public NumSubTabDao getNumSubTabDao() {
		return numSubTabDao;
	}
	public void setNumSubTabDao(NumSubTabDao numSubTabDao) {
		this.numSubTabDao = numSubTabDao;
	}
	public AlphaSubTabDao getAlphaSubTabDao() {
		return alphaSubTabDao;
	}
	public void setAlphaSubTabDao(AlphaSubTabDao alphaSubTabDao) {
		this.alphaSubTabDao = alphaSubTabDao;
	}
}
