package com.vision.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vision.authentication.SessionContextHolder;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.VisionUploadVb;

@Component
public class VisionUploadDao extends AbstractDao<VisionUploadVb> {
	
	@Override
	protected RowMapper getMapper(){
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				VisionUploadVb visionUploadVb = new VisionUploadVb();
				visionUploadVb.setTableName(rs.getString("TABLE_NAME").trim());
				visionUploadVb.setFileName(rs.getString("FILE_NAME").trim());
				visionUploadVb.setUploadSequence(rs.getString("UPLOAD_SEQUENCE"));
				visionUploadVb.setUploadDate(rs.getString("UPLOAD_DATE").trim());
				visionUploadVb.setUploadStatusNt(rs.getInt("UPLOAD_STATUS_NT"));
				visionUploadVb.setUploadStatus(rs.getInt("UPLOAD_STATUS"));
				visionUploadVb.setMaker(rs.getLong("MAKER"));
				visionUploadVb.setDateLastModified(rs.getString("DATE_LAST_MODIFIED").trim());
				visionUploadVb.setDateCreation(rs.getString("DATE_CREATION").trim());
				return visionUploadVb;
			}
		};
		return mapper;
	}
	public List<VisionUploadVb> getQueryResults(VisionUploadVb dObj,int intStatus)
	{
		setServiceDefaults();
		Vector<Object> params = new Vector<Object>();
		StringBuffer strBufApprove = new StringBuffer("Select TAppr.TABLE_NAME," +
			"TAppr.FILE_NAME, TAppr.UPLOAD_SEQUENCE, To_Char(TAppr.UPLOAD_DATE, 'DD-MM-YYYY') UPLOAD_DATE," +
			"TAppr.UPLOAD_STATUS_NT, TAppr.UPLOAD_STATUS, TAppr.MAKER," +
			"To_Char(TAppr.DATE_LAST_MODIFIED, 'DD/MM/YYYY HH24:MI:SS') DATE_LAST_MODIFIED, To_Char(TAppr.DATE_CREATION, 'DD/MM/YYYY HH24:MI:SS') DATE_CREATION" +
			" From VISION_UPLOAD TAppr " );
	
		try
		{

			logger.info("Forming Dynamic query ");

			//check if the column [TABLE_STATUS] should be included in the query
			if (dObj.getUploadStatus() != -1)
			{
				params.addElement(new Integer(dObj.getUploadStatus()));
				CommonUtils.addToQuery("TAppr.UPLOAD_STATUS = ?", strBufApprove);
			}
			if (ValidationUtil.isValid(dObj.getUploadDate()))
			{
				
				String endDate=dObj.getUploadDate();
				if(endDate.trim().indexOf(" ")>-1){
					params.addElement(dObj.getUploadDate());
					CommonUtils.addToQuery("TAppr.UPLOAD_DATE = To_Date(?, 'DD-MM-YYYY HH24:MI:SS') ", strBufApprove);
				}else{
					params.addElement(endDate.trim()+" 00:00:00");
					params.addElement(endDate.trim()+" 23:59:59");
					CommonUtils.addToQuery("TAppr.UPLOAD_DATE BETWEEN To_Date(?, 'DD-MM-YYYY HH24:MI:SS') AND To_Date(?, 'DD-MM-YYYY HH24:MI:SS') ", strBufApprove);
				}
			}
			//check if the column [TABLE_NAME] should be included in the query
			if (ValidationUtil.isValid(dObj.getTableName()))
			{
				params.addElement("%" + dObj.getTableName().toUpperCase() + "%");
				CommonUtils.addToQuery("UPPER(TAppr.TABLE_NAME) LIKE ?", strBufApprove);
			}

			//check if the column [TABLE_DESCRIPTION] should be included in the query
			if (ValidationUtil.isValid(dObj.getFileName()))
			{
				params.addElement("%" + dObj.getFileName().toUpperCase() + "%");
				CommonUtils.addToQuery("UPPER(TAppr.FILE_NAME) LIKE ?", strBufApprove);
			}

			//check if the column [DB_NAME] should be included in the query
			if (ValidationUtil.isValidId(dObj.getUploadSequence()))
			{
				params.addElement(dObj.getUploadSequence());
				CommonUtils.addToQuery("TAppr.UPLOAD_SEQUENCE = ?", strBufApprove);
			}

			//check if the column [DB_OWNER] should be included in the query
			if (dObj.getMaker() > 0)
			{
				params.addElement(dObj.getMaker());
				CommonUtils.addToQuery("TAppr.MAKER = ?", strBufApprove);
			}

			String orderBy=" Order By UPLOAD_SEQUENCE ";
			return getQueryPopupResults(dObj, new StringBuffer(), strBufApprove, "", orderBy, params);
			
		}catch(Exception ex){
			
			ex.printStackTrace();
			logger.error(((strBufApprove==null)? "strBufApprove is Null":strBufApprove.toString()));
			if (params != null)
				for(int i=0 ; i< params.size(); i++)
					logger.error("objParams[" + i + "]" + params.get(i).toString());
			return null;
		}
	}
	/**
	 * 
	 * @param dObj
	 * @return count
	 */
	public int getCountOfUploadTables(VisionUploadVb dObj)
	{
		Object objParams[]=new Object[2];;
		StringBuffer strBufApprove = new StringBuffer("select count(1) from VISION_TABLES MAXTABLES, PROFILE_PRIVILEGES PP, VISION_USERS MU " +
				"where MAXTABLES.TABLE_NAME =? AND PP.MENU_GROUP =MAXTABLES.MENU_GROUP"+ 
				" AND MAXTABLES.UPLOAD_ALLOWED  ='Y' AND PP.P_EXCEL_UPLOAD ='Y' AND MU.USER_GROUP=PP.USER_GROUP " +
				" AND MU.USER_PROFILE=PP.USER_PROFILE AND MU.VISION_ID = ? ");
		try{
			objParams[0] = dObj.getTableName() == null? "" : dObj.getTableName().toUpperCase();
			objParams[1] = dObj.getMaker();
			return getJdbcTemplate().queryForObject(strBufApprove.toString(), objParams, Integer.class);
		}catch(Exception ex){
			ex.printStackTrace();
			return 1;
		}
	}
	/**
	 * 
	 * @return
	 */
	private int getMaxSequence(){
		StringBuffer strBufApprove = new StringBuffer("Select MAX(TAppr.UPLOAD_SEQUENCE) From VISION_UPLOAD TAppr ");
		try{
			return getJdbcTemplate().queryForObject(strBufApprove.toString(), Integer.class);
		}catch(Exception ex){
			ex.printStackTrace();
			return 1;
		}
	}
	@Transactional(rollbackForClassName={"com.vision.exception.RuntimeCustomException"})
	public ExceptionCode doInsertRecord(List<VisionUploadVb> vObjects) throws RuntimeCustomException {
		ExceptionCode exceptionCode = null;
		strErrorDesc  = "";
		strCurrentOperation = "Add";
		setServiceDefaults();
		try {
			int maxSequence = getMaxSequence();
			if(maxSequence <= 0) maxSequence =1;
			else maxSequence++;
			for(VisionUploadVb vObject : vObjects){
				if(vObject.isChecked()){
					List<VisionUploadVb> collTemp = selectApprovedRecord(vObject);
				    if (collTemp == null){
				    	throw new RuntimeCustomException(getResultObject(Constants.ERRONEOUS_OPERATION));
				    }
				    // if record already exists in pending table, modify the record
				    if (vObject.getUploadStatus() != 2 && collTemp != null && !collTemp.isEmpty()){
				    	VisionUploadVb vObjectPersis = collTemp.get(0);
				    	vObjectPersis.setMaker(intCurrentUserId);
				    	if(vObjectPersis.getUploadStatus() == 2){
				    		strErrorDesc="File Upload in Progress for Table: "+vObjectPersis.getTableName();
				    		throw new RuntimeCustomException(getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION));
				    	}
				    	vObjectPersis.setUploadStatus(1);
				    	vObjectPersis.setFileName(vObject.getFileName());
						retVal = doUpdateAppr(vObjectPersis);
						if (retVal != Constants.SUCCESSFUL_OPERATION)
						{
							throw new RuntimeCustomException(getResultObject(retVal));
						}
						vObject.setMaker(intCurrentUserId);
						vObject.setUploadStatus(1);
				    }
				    else
					{
				    	//Try inserting the record
				    	vObject.setUploadStatus(1);
				        vObject.setMaker(intCurrentUserId);
				        String strDBDate = getSystemDate();
				        vObject.setDateCreation(strDBDate);
				        vObject.setDateLastModified(strDBDate);
						vObject.setUploadSequence(maxSequence+"");
						maxSequence++;
						retVal = doInsertionAppr(vObject); 
						if (retVal != Constants.SUCCESSFUL_OPERATION)
						{
							throw new RuntimeCustomException(getResultObject(retVal));
						}
					}
				}
			}
			return getResultObject(Constants.SUCCESSFUL_OPERATION);
		}catch (RuntimeCustomException rcException) {
			throw rcException;
		}catch (UncategorizedSQLException uSQLEcxception) {
			strErrorDesc = parseErrorMsg(uSQLEcxception);
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}catch(Exception ex){
			logger.error("Error in Add.",ex);
			strErrorDesc = ex.getMessage();
			exceptionCode = getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
			throw buildRuntimeCustomException(exceptionCode);
		}
	}
	@Override
	protected int doUpdateAppr(VisionUploadVb vObject){
		String query = "Update VISION_UPLOAD SET UPLOAD_DATE = To_Date(?, 'DD-MM-YYYY'), " +
			"UPLOAD_STATUS_NT = ?, UPLOAD_STATUS = ?, DATE_LAST_MODIFIED = SysDate,	FILE_NAME = ? " +
			"Where TABLE_NAME = ? AND UPLOAD_SEQUENCE = ? AND MAKER = ?";
		Object[] args = {vObject.getUploadDate(),vObject.getUploadStatusNt(),vObject.getUploadStatus(),
			vObject.getFileName(),vObject.getTableName(),vObject.getUploadSequence(), vObject.getMaker()};
		return getJdbcTemplate().update(query,args);
	}
	@Override
	protected int doInsertionAppr(VisionUploadVb vObject){
		String query = "Insert Into VISION_UPLOAD ( TABLE_NAME, FILE_NAME, UPLOAD_SEQUENCE, UPLOAD_DATE, " +
			"UPLOAD_STATUS_NT, UPLOAD_STATUS, MAKER, DATE_LAST_MODIFIED, DATE_CREATION) "+
			"Values (?, ?, ?, To_Date(?, 'DD-MM-YYYY'), ?, ?, ?, SysDate, SysDate)";
		Object[] args = {vObject.getTableName(),vObject.getFileName(),vObject.getUploadSequence(),
			vObject.getUploadDate(), vObject.getUploadStatusNt(),vObject.getUploadStatus(),vObject.getMaker()};  
		return getJdbcTemplate().update(query,args);
	}
	@Override
	protected void setServiceDefaults(){
		serviceName = "VisionUpload";
		serviceDesc = CommonUtils.getResourceManger().getString("VisionUpload");
		tableName = "VISION_UPLOAD";
		childTableName = "VISION_UPLOAD";
		intCurrentUserId = SessionContextHolder.getContext().getVisionId();
	}
	@Override
	protected List<VisionUploadVb> selectApprovedRecord(VisionUploadVb vObject){
		return getQueryResults(vObject, Constants.STATUS_ZERO);
	}

}