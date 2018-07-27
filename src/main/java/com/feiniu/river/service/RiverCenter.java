package com.feiniu.river.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.feiniu.river.config.GlobalConfig;
import com.feiniu.river.util.HttpClient;
import com.feiniu.river.util.MD5Util;
import com.feiniu.river.util.ZKUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RiverCenter {

	@SuppressWarnings("unchecked")
	public void backup() {
		HashMap<String, Object> tmp = getAllInstances();
		HashMap<String, HashMap<String, HashMap<String, Object>>> instances = (HashMap<String, HashMap<String, HashMap<String, Object>>>) tmp
				.get("data");
		Iterator<String> iter = instances.keySet().iterator();
		while (iter.hasNext()) {
			String ip = iter.next();
			String instance = "";
			 
		} 
	}
	
	public static void backup(String instance) {
		try {
			byte[] b = ZKUtil.getData("/" + instance + "/" + instance + ".xml", true);
			String dt = "";
			if (b.length > 0) {
				if (b != null && b.length > 0) {
					dt = new String(b);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void downLoadConfig() {

	}

	public static HashMap<String, Object> getAllInstances() {
		HashMap<String, Object> res = new HashMap<String, Object>();
		HashMap<String, HashMap<String, HashMap<String, Object>>> _tmp = new HashMap<String, HashMap<String, HashMap<String, Object>>>();
		List<String> downServer = new ArrayList<String>();
		for (String path : GlobalConfig.riverUrls.split(",")) {
			try {
				String str = HttpClient.sendGet("http://" + path + "/search.doaction",
						"ac=getInstances&code=" + getCode("getInstances"));
				JSONObject jr = JSONObject.fromObject(str);
				jr = JSONObject.fromObject(jr.get("info"));
				Iterator<?> itr = jr.keys();
				while (itr.hasNext()) {
					String alias = (String) itr.next();
					JSONArray ja = jr.getJSONArray(alias);
					HashMap<String, HashMap<String, Object>> instances = new HashMap<String, HashMap<String, Object>>();
					if (_tmp.containsKey(alias)) {
						instances = _tmp.get(alias);
					}
					for (int j = 0; j < ja.size(); j++) {
						String nodes = ja.getString(j);
						String instanceName = nodes.split(":")[0];
						if (!instances.containsKey(instanceName)) {
							instances.put(instanceName, new HashMap<String, Object>());
						}
						String[] tmps = nodes.split(":");
						for (int i = 0; i < tmps.length; i++) {
							if (i > 0) {
								String k = tmps[i].split("]")[0].substring(1);
								if (tmps[i].split("]").length != 2) {
									continue;
								}
								String v = tmps[i].split("]")[1];
								if (instances.get(instanceName).containsKey(k)) {
									instances.get(instanceName).put(k,
											instances.get(instanceName).get(k) + "," + path + "|" + v);
								} else {
									instances.get(instanceName).put(k, path + "|" + v);
								}
							}
						}
					}
					_tmp.put(alias, instances);
				}
			} catch (Exception e) {
				downServer.add(path);
			}
		}
		res.put("ServerDown", downServer);
		res.put("data", _tmp);
		return res;
	}

	private static String getCode(String ac) {
		return MD5Util.SaltMd5(ac);
	}

}
