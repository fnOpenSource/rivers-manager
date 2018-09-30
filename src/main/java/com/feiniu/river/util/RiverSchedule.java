package com.feiniu.river.util;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.feiniu.river.schedule.JobModel;
import com.feiniu.river.schedule.RiverJob;

public class RiverSchedule {

	private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();
	private static String JOB_GROUP_NAME = "RIVER_JOBGROUP"; 

	public static void addJob(JobModel job) throws SchedulerException {
		Scheduler scheduler = schedulerFactory.getScheduler();
		JobDetail jobDetail = JobBuilder.newJob(RiverJob.class).withIdentity(job.getJobName(), JOB_GROUP_NAME).build();
		Trigger trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(job.getCron())).build();
		jobDetail.getJobDataMap().put("RIVER", job);
		scheduler.scheduleJob(jobDetail, trigger);
		scheduler.start();
	}
}
