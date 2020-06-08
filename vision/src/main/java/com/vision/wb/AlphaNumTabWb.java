package com.vision.wb;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.vision.dao.AbstractDao;
import com.vision.dao.AlphaNumTabDao;
import com.vision.dao.AlphaSubTabDao;
import com.vision.dao.NumSubTabDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.DeepCopy;
import com.vision.vb.AlphaSubTabVb;
import com.vision.vb.CommonVb;
import com.vision.vb.NumSubTabVb;
import com.vision.vb.ReviewResultVb;
import com.vision.vb.TabVb;

@Component
public class AlphaNumTabWb extends AbstractDynaWorkerBean<TabVb> {

	private AlphaNumTabDao alphaNumTabDao;
	private NumSubTabDao numSubTabDao;
	private AlphaSubTabDao alphaSubTabDao;
	
	@Override
	protected AbstractDao<TabVb> getScreenDao() {
		return alphaNumTabDao;
	}

	@Override
	protected void setAtNtValues(TabVb tabVb) {
		tabVb.setRecordIndicatorNt(7);
		tabVb.setTabStatusNt(1);
		if(tabVb.getAlphaSubTabs() != null && !tabVb.getAlphaSubTabs().isEmpty()){
			for(AlphaSubTabVb alphaSubTabVb: tabVb.getAlphaSubTabs()){
				alphaSubTabVb.setRecordIndicatorNt(7);
				alphaSubTabVb.setAlphaSubTabStatusNt(1);
			}
		}
		if(tabVb.getNumSubTabs() != null && !tabVb.getNumSubTabs().isEmpty()){
			for(NumSubTabVb numSubTabVb: tabVb.getNumSubTabs()){
				numSubTabVb.setRecordIndicatorNt(7);
				numSubTabVb.setNumSubTabStatusNt(1);
			}
		}
	}

	@Override
	protected void setVerifReqDeleteType(TabVb tabVb) {
		if("numTab".equalsIgnoreCase(tabVb.getRequestType())){
			ArrayList<CommonVb> lCommVbList =(ArrayList<CommonVb>) getCommonDao().findVerificationRequiredAndStaticDelete("NUM_TAB");
			tabVb.setStaticDelete(lCommVbList.get(0).isStaticDelete());
			tabVb.setVerificationRequired(lCommVbList.get(0).isVerificationRequired());
		}else{
			ArrayList<CommonVb> lCommVbList =(ArrayList<CommonVb>) getCommonDao().findVerificationRequiredAndStaticDelete("ALPHA_TAB");
			tabVb.setStaticDelete(lCommVbList.get(0).isStaticDelete());
			tabVb.setVerificationRequired(lCommVbList.get(0).isVerificationRequired());
		}
	}
	public ArrayList getPageLoadValues(TabVb tabVb){
		List collTemp = null;
		ArrayList<Object> arrListLocal = new ArrayList<Object>();
		try{
			collTemp = getNumSubTabDao().findActiveNumSubTabsByNumTab(1);
			arrListLocal.add(collTemp);
			collTemp = getNumSubTabDao().findActiveNumSubTabsByNumTab(7);
			arrListLocal.add(collTemp);
			if("numTab".equalsIgnoreCase(tabVb.getRequestType())){
				collTemp = getCommonDao().findVerificationRequiredAndStaticDelete("NUM_TAB");
				arrListLocal.add(collTemp);
			}else{
				collTemp = getCommonDao().findVerificationRequiredAndStaticDelete("ALPHA_TAB");
				arrListLocal.add(collTemp);
			}
			return arrListLocal;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Exception in getting the Page load values.", ex);
			return null;
		}
	}
	@Override
	public ExceptionCode getQueryResults(TabVb vObject){
		int intStatus = 1;
		setVerifReqDeleteType(vObject);
		List<TabVb> collTemp = getScreenDao().getQueryResults(vObject,intStatus);
		if (collTemp.size() == 0 && vObject.isVerificationRequired()){
			intStatus = 0;
			collTemp = getScreenDao().getQueryResults(vObject,intStatus);
		}
		if(collTemp.size() == 0){
			ExceptionCode exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), 41, Constants.QUERY, "");
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}else{
			TabVb vObjectTemp = collTemp.get(0);
			vObjectTemp.setRequestType(vObject.getRequestType());
			doSetDesctiptionsAfterQuery(vObjectTemp);
			ExceptionCode exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), 1, Constants.QUERY, "");
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				if(vObject.getNumSubTabs() != null && !vObject.getNumSubTabs().isEmpty()){
					vObject.getNumSubTabs().get(0).setNumTab(vObjectTemp.getTab());
					vObject.getNumSubTabs().get(0).setMaxRecords(20);
					vObject.getNumSubTabs().get(0).setCurrentPage(vObject.getCurrentPage());
					vObject.getNumSubTabs().get(0).setTotalRows(vObject.getTotRecordsCount());
					List<NumSubTabVb> lResult = getNumSubTabDao().getQueryPopupResults(vObject.getNumSubTabs().get(0));
					exceptionCode.setResponse(lResult);
					vObjectTemp.setNumSubTabs(new ArrayList<NumSubTabVb>(1));
					vObjectTemp.getNumSubTabs().add(vObject.getNumSubTabs().get(0));
					getNumSubTabDao().setPendingRecordCount(vObjectTemp);
					vObjectTemp.setTotRecordsCount(vObject.getNumSubTabs().get(0).getTotalRows());
					vObjectTemp.setTotalRows(vObject.getNumSubTabs().get(0).getTotalRows());
					vObjectTemp.setCurrentPage(vObject.getNumSubTabs().get(0).getCurrentPage());
					vObjectTemp.setInternalStatus(vObject.getInternalStatus());
				}
			}else{
				if(vObject.getAlphaSubTabs() != null && !vObject.getAlphaSubTabs().isEmpty()){
					vObject.getAlphaSubTabs().get(0).setAlphaTab(vObjectTemp.getTab());
					vObject.getAlphaSubTabs().get(0).setStaticDelete(vObject.isStaticDelete());
					vObject.getAlphaSubTabs().get(0).setVerificationRequired(vObject.isVerificationRequired());
					vObject.getAlphaSubTabs().get(0).setMaxRecords(20);
					vObject.getAlphaSubTabs().get(0).setCurrentPage(vObject.getCurrentPage());
					vObject.getAlphaSubTabs().get(0).setTotalRows(vObject.getTotRecordsCount());					
					List<AlphaSubTabVb> lResult = getAlphaSubTabDao().getQueryPopupResults(vObject.getAlphaSubTabs().get(0));
					exceptionCode.setResponse(lResult);
					vObjectTemp.setAlphaSubTabs(new ArrayList<AlphaSubTabVb>(1));
					vObjectTemp.getAlphaSubTabs().add(vObject.getAlphaSubTabs().get(0));
					getAlphaSubTabDao().setPendingRecordCount(vObjectTemp);
					vObjectTemp.setTotRecordsCount(vObject.getAlphaSubTabs().get(0).getTotalRows());
					vObjectTemp.setTotalRows(vObject.getAlphaSubTabs().get(0).getTotalRows());
					vObjectTemp.setCurrentPage(vObject.getAlphaSubTabs().get(0).getCurrentPage());
					vObjectTemp.setInternalStatus(vObject.getInternalStatus());
				}
			}
			getScreenDao().fetchMakerVerifierNames(vObjectTemp);
			vObjectTemp.setVerificationRequired(vObject.isVerificationRequired());
			vObjectTemp.setStaticDelete(vObject.isStaticDelete());
			exceptionCode.setOtherInfo(vObjectTemp);
			return exceptionCode;
		}
	}
	/**
	 * Inserts a new record to the approved or pending tables depending on Verification Required flag of the 
	 * object. 
	 * @param vObject
	 * @return 
	 */
	@Override
	public ExceptionCode insertRecord(TabVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<TabVb> deepCopy = new DeepCopy<TabVb>();
		TabVb clonedObject = null;
		try
		{
			setAtNtValues(vObject);
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copy(vObject);
			if(!vObject.isVerificationRequired()){
				exceptionCode = getScreenDao().doInsertApprRecord(vObject);
			}else{
				exceptionCode = getScreenDao().doInsertRecord(vObject);
			}
			exceptionCode.setOtherInfo(vObject);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				exceptionCode.setResponse(vObject.getNumSubTabs());
			}else{
				exceptionCode.setResponse(vObject.getAlphaSubTabs());
			}
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				exceptionCode.setResponse(clonedObject.getNumSubTabs());
			}else{
				exceptionCode.setResponse(clonedObject.getAlphaSubTabs());
			}
			return exceptionCode;
		}
	}
	/**
	 * updates the existing record in the approved or pending tables depending on Verification Required flag of the 
	 * object. 
	 * @param vObject
	 * @return 
	 */
	public ExceptionCode modifyRecord(TabVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<TabVb> deepCopy = new DeepCopy<TabVb>();
		TabVb clonedObject = null;
		try{
			setAtNtValues(vObject);
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copy(vObject);
			if(!vObject.isVerificationRequired()){
				exceptionCode = getScreenDao().doUpdateApprRecord(vObject);
			}else{
				exceptionCode = getScreenDao().doUpdateRecord(vObject);
			}
			exceptionCode.setOtherInfo(vObject);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				exceptionCode.setResponse(vObject.getNumSubTabs());
			}else{
				exceptionCode.setResponse(vObject.getAlphaSubTabs());
			}
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Modify Exception " + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				exceptionCode.setResponse(clonedObject.getNumSubTabs());
			}else{
				exceptionCode.setResponse(clonedObject.getAlphaSubTabs());
			}
			return exceptionCode;
		}
	}
	/**
	 * Delete the existing record in the approved or pending tables depending on Verification Required flag of the 
	 * object. 
	 * @param vObject
	 * @return 
	 */
	@Override
	public ExceptionCode deleteRecord(TabVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<TabVb> deepCopy = new DeepCopy<TabVb>();
		TabVb clonedObject = null;
		try{
			setAtNtValues(vObject);
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copy(vObject);
			if(!vObject.isVerificationRequired()){
				exceptionCode = getScreenDao().doDeleteApprRecord(vObject);
			}else{
				exceptionCode = getScreenDao().doDeleteRecord(vObject);
			}
			exceptionCode.setOtherInfo(vObject);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				exceptionCode.setResponse(vObject.getNumSubTabs());
			}else{
				exceptionCode.setResponse(vObject.getAlphaSubTabs());
			}
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Delete Exception " + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				exceptionCode.setResponse(clonedObject.getNumSubTabs());
			}else{
				exceptionCode.setResponse(clonedObject.getAlphaSubTabs());
			}
			return exceptionCode;
		}
	}
	/**
	 * Approve to be called from other Business classes 
	 * object. 
	 * @param vObject
	 * @return 
	 */
	public ExceptionCode approve(TabVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<TabVb> deepCopy = new DeepCopy<TabVb>();
		TabVb clonedObject = null;
		try{
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copy(vObject);
			exceptionCode = getScreenDao().doApproveForTransaction(vObject,vObject.isStaticDelete());
			exceptionCode.setOtherInfo(vObject);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				exceptionCode.setResponse(vObject.getNumSubTabs());
			}else{
				exceptionCode.setResponse(vObject.getAlphaSubTabs());
			}
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Approve Exception " + rex.getCode().getErrorMsg());
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				exceptionCode.setResponse(clonedObject.getNumSubTabs());
			}else{
				exceptionCode.setResponse(clonedObject.getAlphaSubTabs());
			}
			return exceptionCode;
		}
	}
	/**
	 * Approve to be called from other Business classes 
	 * object. 
	 * @param vObject
	 * @return 
	 */
	public ExceptionCode reject(TabVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<TabVb> deepCopy = new DeepCopy<TabVb>();
		TabVb clonedObject = null;
		try{
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copy(vObject);
			exceptionCode = getScreenDao().doRejectForTransaction(vObject);
			exceptionCode.setOtherInfo(vObject);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				exceptionCode.setResponse(vObject.getNumSubTabs());
			}else{
				exceptionCode.setResponse(vObject.getAlphaSubTabs());
			}
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Reject Exception " + rex.getCode().getErrorMsg());
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				exceptionCode.setResponse(clonedObject.getNumSubTabs());
			}else{
				exceptionCode.setResponse(clonedObject.getAlphaSubTabs());
			}
			return exceptionCode;
		}
	}
	@Override
	public List<ReviewResultVb> reviewRecord(TabVb vObject){
		try{
			List<TabVb> approvedCollection = getScreenDao().getQueryResultsForReview(vObject,1);
			List<TabVb> pendingCollection = getScreenDao().getQueryResultsForReview(vObject,0);
			if("numTab".equalsIgnoreCase(vObject.getRequestType())){
				return transformToReviewResultsNumTab(approvedCollection,pendingCollection);
			}else{
				return transformToReviewResultsAlphaTab(approvedCollection,pendingCollection);
			}
		}catch(Exception ex){
			return null;
		}
	}
	private List<ReviewResultVb> transformToReviewResultsNumTab(List<TabVb> approvedCollection, List<TabVb> pendingCollection) {
		ResourceBundle rsb = CommonUtils.getResourceManger();
		ArrayList collTemp = getPageLoadValues(new TabVb());
		if(pendingCollection != null)
			getScreenDao().fetchMakerVerifierNames(pendingCollection.get(0));
		if(approvedCollection != null)
			getScreenDao().fetchMakerVerifierNames(approvedCollection.get(0));
		ArrayList<ReviewResultVb> lResult = new ArrayList<ReviewResultVb>();
		ReviewResultVb lTab = new ReviewResultVb(rsb.getString("numTab"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getTab() == -1?"":String.valueOf(pendingCollection.get(0).getTab()),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getTab() == -1?"":String.valueOf(approvedCollection.get(0).getTab()));
		lResult.add(lTab);
		ReviewResultVb lDescription = new ReviewResultVb(rsb.getString("numTabDescription"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getTabDescription(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getTabDescription());
		lResult.add(lDescription);
		ReviewResultVb lStatus = new ReviewResultVb(rsb.getString("numTabStatus"),(pendingCollection == null || pendingCollection.isEmpty())?"":getNtDescription((List<NumSubTabVb>) collTemp.get(0),pendingCollection.get(0).getTabStatus()),
				(approvedCollection == null || approvedCollection.isEmpty())?"":getNtDescription((List<NumSubTabVb>) collTemp.get(0),approvedCollection.get(0).getTabStatus()));
		lResult.add(lStatus);
		ReviewResultVb lRecordIndicator = new ReviewResultVb(rsb.getString("recordIndicator"),(pendingCollection == null || pendingCollection.isEmpty())?"":getNtDescription((List<NumSubTabVb>) collTemp.get(1),pendingCollection.get(0).getRecordIndicator()),
				(approvedCollection == null || approvedCollection.isEmpty())?"":getNtDescription((List<NumSubTabVb>) collTemp.get(1),approvedCollection.get(0).getRecordIndicator()));
		lResult.add(lRecordIndicator);
		ReviewResultVb lMaker = new ReviewResultVb(rsb.getString("maker"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getMaker() == 0?"":pendingCollection.get(0).getMakerName(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getMaker() == 0?"":approvedCollection.get(0).getMakerName());
		lResult.add(lMaker);
		ReviewResultVb lVerifier = new ReviewResultVb(rsb.getString("verifier"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getVerifier() == 0?"":pendingCollection.get(0).getVerifierName(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getVerifier() == 0?"":approvedCollection.get(0).getVerifierName());
		lResult.add(lVerifier);
		ReviewResultVb lDateLastModified = new ReviewResultVb(rsb.getString("dateLastModified"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getDateLastModified(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getDateLastModified());
		lResult.add(lDateLastModified);
		ReviewResultVb lDateCreation = new ReviewResultVb(rsb.getString("dateCreation"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getDateCreation(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getDateCreation());
		lResult.add(lDateCreation);
		return lResult;
	}
	private List<ReviewResultVb> transformToReviewResultsAlphaTab(List<TabVb> approvedCollection, List<TabVb> pendingCollection) {
		ResourceBundle rsb = CommonUtils.getResourceManger();
		ArrayList collTemp = getPageLoadValues(new TabVb());
		if(pendingCollection != null)
			getScreenDao().fetchMakerVerifierNames(pendingCollection.get(0));
		if(approvedCollection != null)
			getScreenDao().fetchMakerVerifierNames(approvedCollection.get(0));
		ArrayList<ReviewResultVb> lResult = new ArrayList<ReviewResultVb>();
		ReviewResultVb lTab = new ReviewResultVb(rsb.getString("alphaTab"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getTab() == -1?"":String.valueOf(pendingCollection.get(0).getTab()),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getTab() == -1?"":String.valueOf(approvedCollection.get(0).getTab()));
		lResult.add(lTab);
		ReviewResultVb lDescription = new ReviewResultVb(rsb.getString("alphaTabDescription"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getTabDescription(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getTabDescription());
		lResult.add(lDescription);
		ReviewResultVb lStatus = new ReviewResultVb(rsb.getString("alphaTabStatus"),(pendingCollection == null || pendingCollection.isEmpty())?"":getNtDescription((List<NumSubTabVb>) collTemp.get(0),pendingCollection.get(0).getTabStatus()),
				(approvedCollection == null || approvedCollection.isEmpty())?"":getNtDescription((List<NumSubTabVb>) collTemp.get(0),approvedCollection.get(0).getTabStatus()));
		lResult.add(lStatus);
		ReviewResultVb lRecordIndicator = new ReviewResultVb(rsb.getString("recordIndicator"),(pendingCollection == null || pendingCollection.isEmpty())?"":getNtDescription((List<NumSubTabVb>) collTemp.get(1),pendingCollection.get(0).getRecordIndicator()),
				(approvedCollection == null || approvedCollection.isEmpty())?"":getNtDescription((List<NumSubTabVb>) collTemp.get(1),approvedCollection.get(0).getRecordIndicator()));
		lResult.add(lRecordIndicator);
		ReviewResultVb lMaker = new ReviewResultVb(rsb.getString("maker"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getMaker() == 0?"":pendingCollection.get(0).getMakerName(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getMaker() == 0?"":approvedCollection.get(0).getMakerName());
		lResult.add(lMaker);
		ReviewResultVb lVerifier = new ReviewResultVb(rsb.getString("verifier"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getVerifier() == 0?"":pendingCollection.get(0).getVerifierName(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getVerifier() == 0?"":approvedCollection.get(0).getVerifierName());
		lResult.add(lVerifier);
		ReviewResultVb lDateLastModified = new ReviewResultVb(rsb.getString("dateLastModified"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getDateLastModified(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getDateLastModified());
		lResult.add(lDateLastModified);
		ReviewResultVb lDateCreation = new ReviewResultVb(rsb.getString("dateCreation"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getDateCreation(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getDateCreation());
		lResult.add(lDateCreation);
		return lResult;
	}
	public AlphaNumTabDao getAlphaNumTabDao() {
		return alphaNumTabDao;
	}

	public void setAlphaNumTabDao(AlphaNumTabDao alphaNumTabDao) {
		this.alphaNumTabDao = alphaNumTabDao;
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