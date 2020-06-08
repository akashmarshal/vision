package com.vision.wb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vision.dao.AbstractDao;
import com.vision.dao.NumSubTabDao;
import com.vision.exception.RuntimeCustomException;
import com.vision.vb.CommonVb;
import com.vision.vb.NumSubTabVb;

@Component
public class NumSubTabWb extends AbstractDynaWorkerBean<NumSubTabVb> {

	@Autowired
	NumSubTabDao numSubTabDao;
	
	@Override
	protected AbstractDao<NumSubTabVb> getScreenDao() {
		return numSubTabDao;
	}
	
	@Override
	protected void setAtNtValues(NumSubTabVb object) {
		object.setRecordIndicatorNt(7);
		object.setNumSubTabStatusNt(1);
	}

	@Override
	protected void setVerifReqDeleteType(NumSubTabVb object) {
		ArrayList<CommonVb> lCommVbList =(ArrayList<CommonVb>) getCommonDao().findVerificationRequiredAndStaticDelete("NUM_TAB");
		object.setStaticDelete(lCommVbList.get(0).isStaticDelete());
		object.setVerificationRequired(lCommVbList.get(0).isVerificationRequired());
	}
	
	public List<NumSubTabVb> findActiveNumSubTabsByNumTab(int pNumTab) {
		try {
			return numSubTabDao.findActiveNumSubTabsByNumTab(pNumTab);
		}catch(Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	public List<NumSubTabVb> findActiveNumSubTabsByNumTabCols(int pNumTab, String columns) {
		try {
			return numSubTabDao.findActiveNumSubTabsByNumTabCols(pNumTab, columns);
		}catch(Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
}