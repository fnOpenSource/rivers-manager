package com.feiniu.river.schedule;

import java.lang.reflect.Method;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class RiverJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobModel job = (JobModel) context.getMergedJobDataMap().get("RIVER");
		invokeMethod(job);
	}

	private boolean invokeMethod(JobModel jobModel) {
		Object object = jobModel.getObject();
		try {
			Method method = null;
			Class<?> css = null;
			if (object == null) { 
				css = Class.forName(jobModel.getClassName());
				method = css.getMethod(jobModel.getMethodName());
				method.invoke(null);
			} else {
				css = object.getClass(); 
				method = css.getDeclaredMethod(jobModel.getMethodName());
				method.invoke(object);
				return true; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return false;
	}
}
