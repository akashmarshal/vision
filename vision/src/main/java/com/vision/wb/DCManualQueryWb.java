package com.vision.wb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.vision.dao.AbstractDao;
import com.vision.dao.CommonDao;
import com.vision.dao.DCManualQueryDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.AlphaSubTabVb;
import com.vision.vb.DCManualQueryVb;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.VcConfigMainVb;

@Component
public class DCManualQueryWb extends AbstractWorkerBean<DCManualQueryVb>{

	@Autowired
	private DCManualQueryDao dcManualQueryDao;
	
	@Autowired
	private CommonDao commonDao;
	
	@Override
	protected AbstractDao<DCManualQueryVb> getScreenDao() {
		return dcManualQueryDao;
	}

	@Override
	protected void setAtNtValues(DCManualQueryVb vObject) {
		vObject.setRecordIndicatorNt(7);
		vObject.setVcqStatusNt(1);
		vObject.setLookupDataLoadingAt(1088);
		vObject.setDatabaseTypeAt(1082);
	}

	@Override
	protected void setVerifReqDeleteType(DCManualQueryVb vObject) {
		vObject.setStaticDelete(true);
		vObject.setVerificationRequired(false);
	}


	public String getDbScript(String getMacroVar) throws DataAccessException{
		try {
			return commonDao.getScriptValue(getMacroVar);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public List<DCManualQueryVb> getAllDcManualQuery(DCManualQueryVb vObj) {
		try {
			return dcManualQueryDao.getAllDcManualQueryDetails(vObj);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public List<DCManualQueryVb> getSpecificManualQuery(DCManualQueryVb vObj) {
		try {
			return dcManualQueryDao.getSpecificManualQueryDetails(vObj);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public List<DCManualQueryVb> getAllDcManualQueryBasedOnQueryType(Integer queryType) {
		try {
			return dcManualQueryDao.getAllDcManualQueryBasedOnQueryType(queryType);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public String getColsBasedOnDcManualQuery(String queryID) throws DataAccessException{
		try {
			return dcManualQueryDao.getColumnListing(queryID);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public List<AlphaSubTabVb> getQuerySmartSearchResults(DCManualQueryVb queryPopupObj){
		try{
			setVerifReqDeleteType(queryPopupObj);
			doFormateDataForQuery(queryPopupObj);
			List<AlphaSubTabVb> arrListResult = dcManualQueryDao.getQuerySmartSearchResults(queryPopupObj);
			return arrListResult;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Exception in getting the VISION_DYNAMIC_HASH_VAR results.", ex);
			return null;
		}
	}
	
	public ExceptionCode validateSqlQuery(DCManualQueryVb vObj, String dbScript, String[] hashArr, String[] hashValArr){
		ExceptionCode exceptionCode = new ExceptionCode();
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		String level = "";
		exceptionCode = CommonUtils.getConnection(dbScript);
		if(exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION){
			con = (Connection) exceptionCode.getResponse();
		}else{
			return exceptionCode;
		}
		String dbSetParam1 = CommonUtils.getValue(dbScript, "DB_SET_PARAM1");
		String dbSetParam2 = CommonUtils.getValue(dbScript, "DB_SET_PARAM2");
		String dbSetParam3 = CommonUtils.getValue(dbScript, "DB_SET_PARAM3");
		String stgQuery = "";
		String sessionId = String.valueOf(System.currentTimeMillis());
		String stgTableName1 = "TVC_"+sessionId+"_STG_1";
		String stgTableName2 = "TVC_"+sessionId+"_STG_2";
		String stgTableName3 = "TVC_"+sessionId+"_STG_3";
		String sqlMainQuery = "";
		try{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			if(ValidationUtil.isValid(dbSetParam1)){
				level = "DB Param 1";
				stmt.executeUpdate(dbSetParam1);
			}
			if(ValidationUtil.isValid(dbSetParam2)){
				level = "DB Param 2";
				stmt.executeUpdate(dbSetParam2);
			}
			if(ValidationUtil.isValid(dbSetParam3)){
				level = "DB Param 3";
				stmt.executeUpdate(dbSetParam3);
			}
			Pattern pattern = Pattern.compile("#(.*?)#");
			Matcher matcher = null;
			if(ValidationUtil.isValid(vObj.getStgQuery1())){
				stgQuery = vObj.getStgQuery1();
				matcher = pattern.matcher(stgQuery);
				while(matcher.find()){
					if("TVC_SESSIONID_STG_1".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName1);
					if("TVC_SESSIONID_STG_2".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName2);
					if("TVC_SESSIONID_STG_3".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName3);
				}
				level = "Staging 1";
				stgQuery = CommonUtils.replaceHashTag(stgQuery, hashArr, hashValArr);
				stmt.executeUpdate(stgQuery);
			}
			if(ValidationUtil.isValid(vObj.getStgQuery2())){
				stgQuery = vObj.getStgQuery2();
				matcher = pattern.matcher(stgQuery);
				while(matcher.find()){
					if("TVC_SESSIONID_STG_1".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName1);
					if("TVC_SESSIONID_STG_2".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName2);
					if("TVC_SESSIONID_STG_3".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName3);
				}
				level = "Staging 2";
				stgQuery = CommonUtils.replaceHashTag(stgQuery, hashArr, hashValArr);
				stmt.executeUpdate(stgQuery);
			}
			if(ValidationUtil.isValid(vObj.getStgQuery3())){
				stgQuery = vObj.getStgQuery3();
				matcher = pattern.matcher(stgQuery);
				while(matcher.find()){
					if("TVC_SESSIONID_STG_1".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName1);
					if("TVC_SESSIONID_STG_2".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName2);
					if("TVC_SESSIONID_STG_3".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName3);
				}
				level = "Staging 3";
				stgQuery = CommonUtils.replaceHashTag(stgQuery, hashArr, hashValArr);
				stmt.executeUpdate(stgQuery);
			}
			sqlMainQuery = vObj.getSqlQuery();
			matcher = pattern.matcher(sqlMainQuery);
			while(matcher.find()){
				if("TVC_SESSIONID_STG_1".equalsIgnoreCase(matcher.group(1)))
					sqlMainQuery = sqlMainQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName1);
				if("TVC_SESSIONID_STG_2".equalsIgnoreCase(matcher.group(1)))
					sqlMainQuery = sqlMainQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName2);
				if("TVC_SESSIONID_STG_3".equalsIgnoreCase(matcher.group(1)))
					sqlMainQuery = sqlMainQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName3);
				else
					sqlMainQuery = sqlMainQuery.replaceAll("#"+matcher.group(1)+"#", "#"+String.valueOf(matcher.group(1)).toUpperCase().replaceAll("\\s", "\\_")+"#");
			}
			level = "Main Query";
			
			LinkedHashMap<String,String> before2AfterChangeWordMap = new LinkedHashMap<String,String>();
			LinkedHashMap<String,String> before2AfterChangeIndexMap = new LinkedHashMap<String,String>();
			int index = 0;
			
			if(hashArr!=null && hashValArr!=null && hashArr.length==hashValArr.length){
				for(String variable:hashArr){
					int varGuessIndex = sqlMainQuery.indexOf("#"+variable+"#");
					while(varGuessIndex >= 0){
						String preString = sqlMainQuery.substring(0, varGuessIndex);
						String wholeWordBefChange = "";
						String wholeWordAfterChange = "";
						int startIndexOfMainSQL = 0;
						startIndexOfMainSQL = preString.indexOf(" ")!=-1?preString.lastIndexOf(" "):-1;
						int endIndexOfMainSQL = 0;
						endIndexOfMainSQL = sqlMainQuery.indexOf(" ",varGuessIndex+1);
						
						/* Get whole word  */
						wholeWordBefChange = (startIndexOfMainSQL != -1 && endIndexOfMainSQL != -1)
								? sqlMainQuery.substring(startIndexOfMainSQL, endIndexOfMainSQL)
								: (startIndexOfMainSQL == -1 && endIndexOfMainSQL != -1)
										? sqlMainQuery.substring(0, endIndexOfMainSQL)
										: sqlMainQuery.substring(startIndexOfMainSQL, sqlMainQuery.length());
						wholeWordBefChange = wholeWordBefChange.trim();
						wholeWordAfterChange = wholeWordBefChange.replaceFirst("#"+hashArr[index]+"#", hashValArr[index]).trim();
						
						String storingIndex = "";
						if(before2AfterChangeIndexMap.get(wholeWordAfterChange)!=null)
							storingIndex = before2AfterChangeIndexMap.get(wholeWordAfterChange);
						storingIndex = storingIndex + varGuessIndex + ",";
						before2AfterChangeIndexMap.put(wholeWordAfterChange.toUpperCase(),storingIndex);
						before2AfterChangeWordMap.put(wholeWordAfterChange.toUpperCase(), wholeWordBefChange);
						
						/* change the value in main query */
						if(startIndexOfMainSQL!=-1  && endIndexOfMainSQL!=-1)
							sqlMainQuery = sqlMainQuery.substring(0,startIndexOfMainSQL) + " " + wholeWordAfterChange + " " + sqlMainQuery.substring(endIndexOfMainSQL,sqlMainQuery.length());
						else if(startIndexOfMainSQL==-1)
							sqlMainQuery = wholeWordAfterChange + " " + sqlMainQuery.substring(endIndexOfMainSQL,sqlMainQuery.length());
						else if(endIndexOfMainSQL==-1)
							sqlMainQuery = sqlMainQuery.substring(0,startIndexOfMainSQL) + " " + wholeWordAfterChange;
						
						varGuessIndex = sqlMainQuery.indexOf("#"+variable+"#", varGuessIndex + 1);
					}
					index++;
				}
			}
			sqlMainQuery = CommonUtils.replaceHashTag(sqlMainQuery, hashArr, hashValArr);
			rs = stmt.executeQuery(sqlMainQuery);
			
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
		    LinkedHashMap<String, String> columnsMetaData = new LinkedHashMap<String, String>();
		    for (int i = 1; i <= columnCount; i++) {
		    	String columnName = before2AfterChangeWordMap.get(metaData.getColumnName(i).toUpperCase());
		    	String colType = "D";
		    	if(2 == metaData.getColumnType(i)) colType = "M";
		    	if(columnName!=null)
		    		columnsMetaData.put(columnName, colType);
		    	else
		    		columnsMetaData.put(metaData.getColumnName(i), colType);
		    }
			exceptionCode.setRequest(columnsMetaData);
			
			if(ValidationUtil.isValid(vObj.getPostQuery())){
				stgQuery = vObj.getPostQuery();
				matcher = pattern.matcher(stgQuery);
				while(matcher.find()){
					if("TVC_SESSIONID_STG_1".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName1);
					if("TVC_SESSIONID_STG_2".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName2);
					if("TVC_SESSIONID_STG_3".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#"+matcher.group(1)+"#", stgTableName3);
				}
				level = "Post Query";
				try{
					stgQuery = CommonUtils.replaceHashTag(stgQuery, hashArr, hashValArr);
					stmt.executeUpdate(stgQuery);
				}catch(Exception e){
					e.printStackTrace();
					exceptionCode.setOtherInfo(vObj);
					exceptionCode.setErrorCode(9999);
					exceptionCode.setErrorMsg("Warning - Post Query Execution Failed - Cause:"+e.getMessage());
					return exceptionCode;
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg("Validation failed at level - "+level+" - Cause:"+e.getMessage());
			exceptionCode.setOtherInfo(vObj);
			return exceptionCode;
		}catch(Exception e){
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg("Validation failed at level - "+level+" - Cause:"+e.getMessage());
			exceptionCode.setOtherInfo(vObj);
			return exceptionCode;
		}finally{
			try{
				if(ValidationUtil.isValid(stgTableName1))
					stmt.executeUpdate("DROP TABLE "+stgTableName1+" PURGE");
			}catch(Exception e){}
			try{
				if(ValidationUtil.isValid(stgTableName2))
					stmt.executeUpdate("DROP TABLE "+stgTableName2+" PURGE");
			}catch(Exception e){}
			try{
				if(ValidationUtil.isValid(stgTableName3))
					stmt.executeUpdate("DROP TABLE "+stgTableName3+" PURGE");
			}catch(Exception e){}
			try{
				if(rs!=null)
					rs.close();
				if(stmt!=null)
					stmt.close();
				if(con!=null)
					con.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
		return exceptionCode;
	}
	
	@Override
	public ExceptionCode doValidate(DCManualQueryVb vObject){
		ExceptionCode exceptionCode = new ExceptionCode();
		boolean isNotValid = false;
		try{
			if("isNotValid".equalsIgnoreCase(vObject.getPreviousActionType())){
				return null;
			}else {
				exceptionCode = CommonUtils.formHashList(vObject);
				if(exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION){
					String[] hashSetFromQuery = (String[])exceptionCode.getOtherInfo();
					String hashVariableScript = vObject.getHashVariableScript();
					hashVariableScript = hashVariableScript.replaceAll("@HASH@", "#");
					if(ValidationUtil.isValid(hashVariableScript)){
						Matcher matObj = Pattern.compile("\\{(.*?):#",Pattern.DOTALL).matcher(hashVariableScript);
						for(String variable:hashSetFromQuery){
							if(!isNotValid){
								boolean isMatched = false;
								matObj.reset();
								while(matObj.find()){
									if(!isMatched){
										isMatched = variable.equalsIgnoreCase(matObj.group(1));
									}
								}
								if(!isMatched)
									isNotValid = true;
							}
						}
					}else if(!ValidationUtil.isValid(hashVariableScript) && (hashSetFromQuery==null || hashSetFromQuery.length==0)){
						isNotValid = false;
					}else{
						isNotValid = true;
					}
				}
				if(isNotValid)
					return CommonUtils.getResultObject("VC Query", Constants.WE_HAVE_ERROR_DESCRIPTION, "validate", "Validate before proceeding to save your changes");
				else{
					String dbScript = getDbScript(vObject.getDatabaseConnectivityDetails());
					String[] hashArr = (String[])exceptionCode.getOtherInfo();
					String[] hashValArr = (String[])exceptionCode.getRequest();
					LinkedHashMap<String, String> columnMetaDataHM = (LinkedHashMap<String, String>)validateSqlQuery(vObject, dbScript, hashArr, hashValArr).getRequest();
					Set<String> currentColumnSet = columnMetaDataHM.keySet();
					Set<String> storedColumnSet = new HashSet<String>(); 
					Matcher matObj = Pattern.compile("\\<name\\>(.*?)\\<\\/name\\>",Pattern.DOTALL).matcher(vObject.getQueryColumnXML());
					while(matObj.find()){
						storedColumnSet.add(matObj.group(1));
					}
					currentColumnSet.removeAll(storedColumnSet);
					if(currentColumnSet.size()>0){
						exceptionCode = CommonUtils.getResultObject("VC Query", Constants.WE_HAVE_ERROR_DESCRIPTION, "validate", "Validate before proceeding to save your changes");
						exceptionCode.setOtherInfo(vObject);
						return exceptionCode;
					}else{
						exceptionCode.setErrorMsg("");
						return exceptionCode;					
					}
				}
			}
				
		}catch(Exception e){
			e.printStackTrace();
			return CommonUtils.getResultObject("VC Query", Constants.WE_HAVE_ERROR_DESCRIPTION, "validate", e.getMessage());
		}
	}

	public ExceptionCode deleteManualQuery(DSConnectorVb dsConnectorVb) {
		return dcManualQueryDao.deleteManualQuery(dsConnectorVb);
	}

	public List<DCManualQueryVb> getQuerySmartSearchFilter(DCManualQueryVb queryPopupObj){
		try{
			setVerifReqDeleteType(queryPopupObj);
			doFormateDataForQuery(queryPopupObj);
			return dcManualQueryDao.getQuerySmartSearchFilter(queryPopupObj);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Exception in getting the Catalog Smart Search Filter results.", ex);
			return null;
		}
	}
	
}
