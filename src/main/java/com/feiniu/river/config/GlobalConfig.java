package com.feiniu.river.config;

import com.feiniu.river.util.ZKUtil;

public class GlobalConfig {
	public static String riverUrls = "";
	public static String zkhosts = "";
	public static String zkConfigPath = "";
	public static String zkConnectionTimeout = "";
	public static String instanceFilePath = "";
	public static String backupCron="0 0 2 * * ? *";

	public GlobalConfig(String _riverUrls, String _zkhosts,
			String _zkConfigPath,String _instancePath,String _backupCron) {
		riverUrls = _riverUrls; 
		zkhosts = _zkhosts;
		zkConfigPath = _zkConfigPath;
		instanceFilePath = _instancePath;
		backupCron = _backupCron;
		ZKUtil.setZkHost(zkhosts);
		ZKUtil.setZkConfigPath(zkConfigPath);
	}
}
