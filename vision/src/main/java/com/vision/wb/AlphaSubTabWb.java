package com.vision.wb;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vision.dao.AbstractDao;
import com.vision.dao.AlphaSubTabDao;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.vb.AlphaSubTabVb;
import com.vision.vb.CommonVb;
import com.vision.vb.NumSubTabVb;
import com.vision.vb.ReviewResultVb;

@Component
public class AlphaSubTabWb extends AbstractDynaWorkerBean<AlphaSubTabVb> {

	@Autowired
	AlphaSubTabDao alphaSubTabDao;
	
	@Override
	protected AbstractDao<AlphaSubTabVb> getScreenDao() {
		return alphaSubTabDao;
	}
	@Override
	protected void setAtNtValues(AlphaSubTabVb object) {
		object.setRecordIndicatorNt(7);
		object.setAlphaSubTabStatusNt(1);
	}

	@Override
	protected void setVerifReqDeleteType(AlphaSubTabVb object) {
		ArrayList<CommonVb> lCommVbList =(ArrayList<CommonVb>) getCommonDao().findVerificationRequiredAndStaticDelete("ALPHA_TAB");
		object.setStaticDelete(lCommVbList.get(0).isStaticDelete());
		object.setVerificationRequired(lCommVbList.get(0).isVerificationRequired());
	}
	public List<AlphaSubTabVb> findActiveAlphaSubTabsByAlphaTab(int pAlphaTab) {
		try {
			return alphaSubTabDao.findActiveAlphaSubTabsByAlphaTab(pAlphaTab);
		}catch(Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	public List<AlphaSubTabVb> findActiveAlphaSubTabsByAlphaTabCols(int pNumTab, String columns) {
		try {
			return alphaSubTabDao.findActiveAlphaSubTabsByAlphaTabCols(pNumTab, columns);
		}catch(Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	public ArrayList getPageLoadValues(){
		List collTemp = null;
		ArrayList<Object> arrListLocal = new ArrayList<Object>();
		try{
			collTemp = getNumSubTabDao().findActiveNumSubTabsByNumTab(1);
			arrListLocal.add(collTemp);
			collTemp = getNumSubTabDao().findActiveNumSubTabsByNumTab(7);
			arrListLocal.add(collTemp);
			collTemp = getCommonDao().findVerificationRequiredAndStaticDelete("ALPHA_SUB_TAB");
			arrListLocal.add(collTemp);
			return arrListLocal;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Exception in getting the Page load values.", ex);
			return null;
		}
	}
	
	@Override
	protected List<ReviewResultVb> transformToReviewResults(List<AlphaSubTabVb> approvedCollection, List<AlphaSubTabVb> pendingCollection) {
		ResourceBundle rsb = CommonUtils.getResourceManger();
		ArrayList collTemp = getPageLoadValues();
		if(pendingCollection != null)
			getScreenDao().fetchMakerVerifierNames(pendingCollection.get(0));
		if(approvedCollection != null)
			getScreenDao().fetchMakerVerifierNames(approvedCollection.get(0));
		ArrayList<ReviewResultVb> lResult = new ArrayList<ReviewResultVb>();
		ReviewResultVb lTab = new ReviewResultVb(rsb.getString("alphaTab"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getAlphaTab() == -1?"":String.valueOf(pendingCollection.get(0).getAlphaTab()),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getAlphaTab() == -1?"":String.valueOf(approvedCollection.get(0).getAlphaTab()));
		lResult.add(lTab);
		ReviewResultVb lSubTab = new ReviewResultVb(rsb.getString("subTab"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getAlphaSubTab(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getAlphaSubTab());
		lResult.add(lSubTab);
		ReviewResultVb lDescription = new ReviewResultVb(rsb.getString("description"),(pendingCollection == null || pendingCollection.isEmpty())?"":pendingCollection.get(0).getDescription(),
				(approvedCollection == null || approvedCollection.isEmpty())?"":approvedCollection.get(0).getDescription());
		lResult.add(lDescription);
		ReviewResultVb lStatus = new ReviewResultVb(rsb.getString("status"),(pendingCollection == null || pendingCollection.isEmpty())?"":getNtDescription((List<NumSubTabVb>) collTemp.get(0),pendingCollection.get(0).getAlphaSubTabStatus()),
				(approvedCollection == null || approvedCollection.isEmpty())?"":getNtDescription((List<NumSubTabVb>) collTemp.get(0),approvedCollection.get(0).getAlphaSubTabStatus()));
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
	
}