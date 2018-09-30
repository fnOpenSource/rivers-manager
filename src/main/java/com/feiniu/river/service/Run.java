package com.feiniu.river.service;

import org.quartz.SchedulerException;

import com.feiniu.river.config.GlobalConfig;
import com.feiniu.river.schedule.JobModel;
import com.feiniu.river.util.RiverSchedule;

public class Run {

	public void start() {
		JobModel job = new JobModel("AutoBackUp",GlobalConfig.backupCron, "com.feiniu.river.service.RiverCenter", "backup",
				null);
		try {
			RiverSchedule.addJob(job);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

}
