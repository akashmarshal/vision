package com.vision.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vision.dao.BuildSchedulesDao;
import com.vision.wb.StaggingSchedulerWb;

/**
 * @author akash.marshall
 *  
 */
@Component
public class StaggingSchedulerController { 
	
	@Autowired
	StaggingSchedulerWb staggingSchedulerWb;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Scheduled(fixedRate = 5000)
	public void schedulerProcess() { 
		/*System.out.println("Scheduled");
		System.out.println("The time is now {}"+ dateFormat.format(new Date()));*/
		staggingSchedulerWb.doBulkDelete();
		
	}
}
