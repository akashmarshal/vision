package com.vision.wb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vision.dao.StaggingSchedulerDao;

@Component
public class StaggingSchedulerWb {

	@Autowired
	StaggingSchedulerDao staggingScheduleDao; 
	 
	
	/**
	 * deletes VWC_STAGGING_TABLE_LOGGING Table Datas 
	 */
	public void doBulkDelete() {
		staggingScheduleDao.doBulkDelete();
	}
}
