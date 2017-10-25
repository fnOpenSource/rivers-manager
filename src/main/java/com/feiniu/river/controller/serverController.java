package com.feiniu.river.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.feiniu.river.config.GlobalConfig;
import com.feiniu.river.util.Folder;
import com.feiniu.river.util.HttpClient;
import com.feiniu.river.util.MD5Util;
import com.feiniu.river.util.ZKUtil;

@Controller
@RequestMapping("/server")
public class serverController {

	@RequestMapping(value = "/getInstance", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String getInstance() {
		String _res = "400";
		_res = JSONArray.fromObject(getAllInstances()).toString();
		return _res;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getStatus", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String getStatus() {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>();
		HashMap<String, Object> data = new HashMap<String, Object>();
		HashMap<String, Object> tmp = getAllInstances();
		data.put(
				"instances",
				((HashMap<String, HashMap<String, HashMap<String, Object>>>) tmp
						.get("data")).size());
		data.put("status", tmp.get("ServerDown"));
		data.put("servers", GlobalConfig.riverUrls.split(",").length);
		String version = "";
		HashMap<String, Object> instance_status = new HashMap<String, Object>();
		for (String path : GlobalConfig.riverUrls.split(",")) {
			try {
				String str = HttpClient.sendGet("http://" + path
						+ "/search.doaction", "ac=getStatus&code="+getCode("getStatus"));
				JSONObject jr = JSONObject.fromObject(str);
				version = JSONObject.fromObject(jr.get("info")).getString(
						"VERSION");
				instance_status.put(path, jr.get("info"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		data.put("version", version);
		res.put("instance_status", instance_status);
		res.put("data", data);
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}

	@RequestMapping(value = "/getCurrentConfigInstance", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String getCurrentConfigInstance(@RequestParam String instance) {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>();

		res.put("data", "");
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}

	@RequestMapping(value = "/reloadInstanceConfig", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String reloadInstanceConfig(@RequestParam String instance) {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>();
		ZKUtil.upload(this.getClass().getProtectionDomain().getCodeSource()
				.getLocation().getPath()
				+ "datas/" + instance, GlobalConfig.zkConfigPath);
		res.put("data", "");
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}

	@RequestMapping(value = "/addNewInstanceConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String addNewInstanceConfig(@RequestParam String instance) {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>();

		res.put("data", "");
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}
 
	

	@RequestMapping(value = "/getAllInstancesFolder", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String getAllInstancesFolder() {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>();
		res.put("data", Folder.getAllFiles(GlobalConfig.instanceFilePath, true));
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}

	@RequestMapping(value = "/getInstanceConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String getInstanceConfig(@RequestParam String instance) {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>();
		String dt = "";
		try {
			byte[] b = ZKUtil.getData("/" + instance + "/" + instance + ".xml",
					true);
			if (b.length > 0) {
				if (b != null && b.length > 0) {
					dt = new String(b);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		res.put("data", dt);
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}

	@RequestMapping(value = "/setInstanceConfig", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String setInstanceConfig(@RequestParam String instance,
			@RequestParam String data) {
		String _res = "100";
		try {
			data = data.trim();
			ZKUtil.setData("/" + instance + "/" + instance + ".xml", data, true);
		} catch (Exception e) {
			_res = "400";
		}
		return _res;
	}

	@RequestMapping(value = "/InstancesAction", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String InstancesAction(@RequestParam String action,
			@RequestParam String instance,@RequestParam String ip) {
		String _res = "100";
		switch (action) {
		case "del":
			removeInstanceFromNode(instance);
			break;
		case "stop":
			
			break;
		case "getNodeConfig":
			_res=getNodeConfig(ip);
			break; 
		case "stopHttpReader":
			stopHttpReaderFromNode(ip);
			break;
		case "startHttpReader":
			startHttpReaderFromNode(ip);
			break;
		case "stopSearcher":
			stopSearcherFromNode(ip);
			break;
		case "startSearcher":
			startSearcherFromNode(ip);
			break;
		case "edit":

			break;
		case "download":

			break;
		case "push":
			pushInstanceToNode(instance);
			break;
		}
		return _res;
	}

	@RequestMapping(value = "/resume_job", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String resume_job(@RequestParam String ip,
			@RequestParam String instancetype, @RequestParam String instance) {
		return HttpClient.sendGet("http://" + ip + "/search.doaction",
				"ac=resumeInstance&instance=" + instance  
						+ "&code="+getCode("resumeInstance"));
	}

	@RequestMapping(value = "/stop_job", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String stop_job(@RequestParam String ip,
			@RequestParam String instancetype, @RequestParam String instance) {
		return HttpClient.sendGet("http://" + ip + "/search.doaction",
				"ac=stopInstance&instance=" + instance  
						+ "&code="+getCode("stopInstance"));
	}

	@RequestMapping(value = "/reload_config", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String reload_config(@RequestParam String ip,
			@RequestParam String instancetype, @RequestParam String instance) {
		return HttpClient.sendGet("http://" + ip + "/search.doaction",
				"ac=reloadConfig&instance=" + instance  
						+ "&code="+getCode("reloadConfig"));
	}

	@RequestMapping(value = "/run_job", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String run_job(@RequestParam String ip,
			@RequestParam String jobtype, @RequestParam String instance) {
		return HttpClient.sendGet("http://" + ip + "/search.doaction",
				"ac=runNow&jobtype=" + jobtype + "&instance=" + instance
						+ "&code="+getCode("runNow"));
	}

	@RequestMapping(value = "/get_info", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String get_info(@RequestParam String ip, @RequestParam String instance) {
		return HttpClient.sendGet("http://" + ip + "/search.doaction",
				"ac=getInstanceInfo&instance=" + instance + "&code="+getCode("getInstanceInfo"));
	}
	
	/**
	 * @param type
	 *            set or remove
	 * @return
	 */
	@RequestMapping(value = "/saveNodeConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	private String saveNodeConfig(String ip,String type,String k,String v){
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>(); 
		String str = HttpClient.sendGet("http://" + ip
				+ "/search.doaction", "ac=setNodeConfig&code="+getCode("setNodeConfig")+"&type="+type+"&k="+k+"&v="+v);
		res.put("data", str);
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}
	
	private String pushInstanceToNode(String instance) {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>();
		
		res.put("data", "");
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}
	
	private String stopHttpReaderFromNode(String ip){
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>(); 
		String str = HttpClient.sendGet("http://" + ip
				+ "/search.doaction", "ac=stopHttpReaderServiceService&code="+getCode("stopHttpReaderServiceService"));
		res.put("data", str);
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}
	
	private String startHttpReaderFromNode(String ip){
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>(); 
		String str = HttpClient.sendGet("http://" + ip
				+ "/search.doaction", "ac=startHttpReaderServiceService&code="+getCode("startHttpReaderServiceService"));
		res.put("data", str);
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}

	
	private String getNodeConfig(String ip){
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>(); 
		String str = HttpClient.sendGet("http://" + ip
				+ "/search.doaction", "ac=getNodeConfig&code="+getCode("getNodeConfig"));
		res.put("data", str);
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}
	
	private String stopSearcherFromNode(String ip){
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>(); 
		String str = HttpClient.sendGet("http://" + ip
				+ "/search.doaction", "ac=stopSearcherService&code="+getCode("stopSearcherService"));
		res.put("data", str);
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}
	
	private String startSearcherFromNode(String ip){
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>(); 
		String str = HttpClient.sendGet("http://" + ip
				+ "/search.doaction", "ac=startSearcherService&code="+getCode("startSearcherService"));
		res.put("data", str);
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}
	
	private String removeInstanceFromNode(String instance) {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>(); 
		res.put("data", "");
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}

	private HashMap<String, Object> getAllInstances() {
		HashMap<String, Object> res = new HashMap<String, Object>();
		HashMap<String, HashMap<String, HashMap<String, Object>>> _tmp = new HashMap<String, HashMap<String, HashMap<String, Object>>>();
		List<String> downServer = new ArrayList<String>();
		for (String path : GlobalConfig.riverUrls.split(",")) {
			try {
				String str = HttpClient.sendGet("http://" + path
						+ "/search.doaction", "ac=getInstances&code="+getCode("getInstances"));
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
							instances.put(instanceName,
									new HashMap<String, Object>());
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
									instances.get(instanceName).put(
											k,
											instances.get(instanceName).get(k)
													+ "," + path + "|" + v);
								} else {
									instances.get(instanceName).put(k,
											path + "|" + v);
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
	
	private String getCode(String ac){ 
		return MD5Util.SaltMd5(ac);
	}
}
