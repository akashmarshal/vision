package com.vision.wb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vision.dao.AbstractDao;
import com.vision.dao.CommonDao;
import com.vision.dao.DashboardDesignDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.Constants;
import com.vision.util.DeepCopy;
import com.vision.util.ValidationUtil;
import com.vision.vb.DashboardDesignVb;
import com.vision.vb.DashboardLODWrapperVb;
import com.vision.vb.DsConnectorLODWrapperVb;
import com.vision.vb.VcConfigMainCRUDVb;
import com.vision.vb.VcConfigMainVb;
import com.vision.vb.DashboardDesignVb;
import com.vision.vb.WidgetDesignVb;

@Component
public class DashboardDesignWb  extends AbstractWorkerBean<DashboardDesignVb>{
 
	public static Logger logger = LoggerFactory.getLogger(DashboardDesignVb.class);


	@Autowired
	private CommonDao commonDao;
	
	@Autowired
	DashboardDesignDao dashboardDao;

	@Override
	protected void setVerifReqDeleteType(DashboardDesignVb vObject){
		vObject.setStaticDelete(false);
		vObject.setVerificationRequired(false);
	}
	@Override
	protected void setAtNtValues(DashboardDesignVb vObject){
		vObject.setRecordIndicatorNt(7);
		vObject.setRecordIndicator(0);
		vObject.setRsdStatus(2000);
	}
	@Override
	protected AbstractDao<DashboardDesignVb> getScreenDao() {
		return dashboardDao;
	}
	
	public ExceptionCode insertRecord(DashboardDesignVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<DashboardDesignVb> deepCopy = new DeepCopy<DashboardDesignVb>();
		DashboardDesignVb clonedObject = null;
		try{
			setAtNtValues(vObject);
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copy(vObject);
			doFormateData(vObject);
			exceptionCode = doValidate(vObject);
			if(exceptionCode!=null && exceptionCode.getErrorMsg()!=""){
				return exceptionCode;
			}
			exceptionCode = dashboardDao.doInsertApprRecordforDashboardWIP(vObject);
			getScreenDao().fetchMakerVerifierNames(vObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception In Vision Catalog" + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			return exceptionCode;
		}
	}
	
	public ExceptionCode doSaveOperationsWIP(DashboardDesignVb vcMainVb){
		ExceptionCode exceptionCode  = null;
		try{
			vcMainVb.setRecordIndicator(0);
			vcMainVb.setStaticDelete(false);
			vcMainVb.setVerificationRequired(false);
			String sysDate = dashboardDao.getSystemDate();
			vcMainVb.setDateCreation(sysDate);
			vcMainVb.setDateLastModified(sysDate);

			exceptionCode = dashboardDao.doSaveOperationsWIP(vcMainVb);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception In Save All" + rex.getCode().getErrorMsg());
			exceptionCode = rex.getCode();
			return exceptionCode;
		}
	}
	

	public ExceptionCode modifyRecord(DashboardDesignVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<DashboardDesignVb> deepCopy = new DeepCopy<DashboardDesignVb>();
		DashboardDesignVb clonedObject = null;
		try{
			clonedObject = deepCopy.copy(vObject);
			exceptionCode = dashboardDao.doUpdateRecordDashboardIntoWIP(vObject);
			dashboardDao.fetchMakerVerifierNames(vObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Modify Exception " + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			return exceptionCode;
		}
	}
	

	public ExceptionCode getDashboard(DashboardDesignVb vObject) {
		ExceptionCode exceptionCode = new ExceptionCode();
		int intStatus = 1;
		DeepCopy<DashboardDesignVb> deepCopy = new DeepCopy<DashboardDesignVb>();
		DashboardDesignVb clonedObject = null;
		DashboardDesignVb dashboardvb=null ;
		try {
			clonedObject = deepCopy.copy(vObject);
			if(0 == vObject.getRsdStatus() || 9 == vObject.getRsdStatus()) {
				intStatus = 0;
				dashboardvb = dashboardDao.getQueryResults(vObject, intStatus, true);
			} else {
				dashboardvb = dashboardDao.getQueryResults(vObject, intStatus, true);
			}
			
			exceptionCode.setErrorCode( (dashboardvb != null)?Constants.SUCCESSFUL_OPERATION:Constants.ERRONEOUS_OPERATION);
			exceptionCode.setResponse(  (dashboardvb != null)?dashboardvb:null);
		} catch(Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg(e.getMessage());
		}
		exceptionCode.setOtherInfo(clonedObject);
		return exceptionCode;
	}


	public ExceptionCode getAllDashboardList() {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			List<DashboardDesignVb> dashboardList = dashboardDao.getAllDashboardList();
			exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
			exceptionCode.setResponse(dashboardList);
			if(!ValidationUtil.isValidList(dashboardList)) {
				exceptionCode.setErrorMsg("Dashboard Records Not Found ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg(e.getMessage());
		}
		return exceptionCode;
	}
	
	public ExceptionCode deleteRecord(DashboardDesignVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<DashboardDesignVb> deepCopy = new DeepCopy<DashboardDesignVb>();
		DashboardDesignVb clonedObject = null;
		try{
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copy(vObject);
			exceptionCode = dashboardDao.doDeleteRecordDashboardFromPend(vObject);
			getScreenDao().fetchMakerVerifierNames(vObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Delete Exception In Vision Catalog" + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			rex.printStackTrace();
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			return exceptionCode;
		}
	}

	public ExceptionCode doPublishToMain(DashboardDesignVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<DashboardDesignVb> deepCopy = new DeepCopy<DashboardDesignVb>();
		DashboardDesignVb clonedObject = null;
		try{
			setAtNtValues(vObject);
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copy(vObject);
			exceptionCode = dashboardDao.doPublishToMain(vObject);
			getScreenDao().fetchMakerVerifierNames(vObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception In Catalog Publish" + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			return exceptionCode;
		}
	}
	

	@Transactional(rollbackForClassName = { "com.vision.exception.RuntimeCustomException" })
	public ExceptionCode doInsertRecordForAccessControl(DashboardLODWrapperVb dsLODWrapperVb) throws RuntimeCustomException {
		ExceptionCode exceptionCode = new ExceptionCode();
		int result = Constants.ERRONEOUS_OPERATION;
		try {
			result = dashboardDao.doInsertRecordForDashboardLOD(dsLODWrapperVb);
			exceptionCode.setErrorCode(result);
			return exceptionCode;
		} catch (RuntimeCustomException rcException) {
			throw rcException;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	@Transactional(rollbackForClassName= {"com.vision.exception.RuntimeCustomException"})
	public ExceptionCode doInsertRecordForPrivileges(DashboardDesignVb dashboardVb) {
		ExceptionCode exceptionCode = new ExceptionCode();
		int result = Constants.ERRONEOUS_OPERATION;
		DashboardDesignVb dashboardvb= null;
		try {
			if ("accept".contains(dashboardVb.getActionType().toLowerCase())) {
				result = dashboardDao.acceptDashboard(dashboardVb);
				dashboardvb = dashboardDao.getQueryResults(dashboardVb, 1, true);
			} else {
				result = dashboardDao.rejectDashboard(dashboardVb);
			}
			exceptionCode.setErrorCode(result);
			exceptionCode.setResponse(dashboardvb);
			return exceptionCode;
		} catch (RuntimeCustomException runExc) {
			throw runExc;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	public ExceptionCode getLODForDashboard(DashboardLODWrapperVb dsLODWrapperVb) throws RuntimeCustomException {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			String sysDate = commonDao.getSystemDate();
			dsLODWrapperVb.getMainModel().setDateCreation(sysDate);
			dsLODWrapperVb.getMainModel().setDateLastModified(sysDate);
			exceptionCode.setResponse(dashboardDao.getRecordForDashboardLOD(dsLODWrapperVb));
			exceptionCode.setErrorCode(1);
			return exceptionCode;
		} catch (RuntimeCustomException rcException) {
			throw rcException;
		} catch (Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorMsg(e.getMessage());
			exceptionCode.setErrorCode(0);
			throw new RuntimeCustomException(e.getMessage());
		}
	}


	public ExceptionCode getuserListExcludingUserGrpAndProf(DashboardLODWrapperVb DashboardMain) {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode.setResponse(dashboardDao.getuserListExcludingUserGrpAndProf(DashboardMain));
			exceptionCode.setErrorCode(1);
			return exceptionCode;
		} catch (RuntimeCustomException rcException) {
			throw rcException;
		} catch (Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorMsg(e.getMessage());
			exceptionCode.setErrorCode(0);
			throw new RuntimeCustomException(e.getMessage());
		}
	}


}
