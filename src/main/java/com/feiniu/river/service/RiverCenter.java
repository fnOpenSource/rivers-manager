package com.feiniu.river.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	public static void backup() {
		HashMap<String, Object> tmp = getAllInstances();
		HashMap<String, HashMap<String, HashMap<String, Object>>> instances = (HashMap<String, HashMap<String, HashMap<String, Object>>>) tmp
				.get("data");
		Iterator<String> iter = instances.keySet().iterator();
		while (iter.hasNext()) {
			String dt = iter.next();
			try {
				HashMap<String, HashMap<String, Object>> instance = instances.get(dt);
				Iterator<String> _iter = instance.keySet().iterator();
				while (_iter.hasNext()) {
					backup(_iter.next());
				}
			} catch (Exception e) { 
				e.printStackTrace();
			} 
		}
	}

	public static void backup(String instance) {
		try {
			byte[] b = ZKUtil.getData("/" + instance + "/" + instance + ".xml", true);
			String dt = "";
			if (b.length > 0) {
				if (b != null && b.length > 0) {
					dt = new String(b);
					createFile(GlobalConfig.instanceFilePath+"/"+instance,instance + ".xml",dt);
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

	private static void createFile(String filePath, String fileName, String contents) throws IOException {
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (fileName != null) {
			File checkFile = new File(filePath +"/"+ fileName);
			FileWriter writer = null;
			try {
				if (!checkFile.exists()) {
					checkFile.createNewFile();
				}
				writer = new FileWriter(checkFile, true);
				writer.append(contents);
				writer.flush();
			} finally {
				if (null != writer)
					writer.close();
			}
		}
	}
}
