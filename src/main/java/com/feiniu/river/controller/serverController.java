package com.feiniu.river.controller;

import java.net.URLEncoder;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.feiniu.river.config.GlobalConfig;
import com.feiniu.river.service.RiverCenter;
import com.feiniu.river.util.Folder;
import com.feiniu.river.util.HttpClient;
import com.feiniu.river.util.MD5Util;
import com.feiniu.river.util.ZKUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/server")
public class serverController {
	
	
	@RequestMapping(value = "/getHosts", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String getHosts() { 
		HashMap<String, Object> res = new HashMap<String, Object>(); 
		res.put("data", GlobalConfig.riverUrls);
		return JSONArray.fromObject(res).toString(); 
	}
	
	
	@RequestMapping(value = "/getInstance", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String getInstance() {
		String _res = "400";
		_res = JSONArray.fromObject(RiverCenter.getAllInstances()).toString();
		return _res;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getStatus", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String getStatus() {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>();
		HashMap<String, Object> data = new HashMap<String, Object>();
		HashMap<String, Object> tmp = RiverCenter.getAllInstances();
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
		case "runCode":
			_res = runCode(instance,ip);
			break;
		case "backup_config":
			_res = backup_config(instance,ip);
			break; 
		case "resetInstanceState":
			_res = resetInstanceState(instance,ip);
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
	
	private String runCode(String codes,String ip) {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>();  
		if(ip.length()>5) {
			try {
				String str = HttpClient.sendGet("http://" + ip
						+ "/search.doaction", "ac=runCode&code="+getCode("runCode")+"&script="+URLEncoder.encode(codes, "utf-8"));
				res.put("data", str);
				_res = JSONArray.fromObject(res).toString();
			}catch (Exception e) {
				// TODO: handle exception
			} 
		} 
		return _res;
	}
	
	private String backup_config(String instance,String ip) {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>(); 
		RiverCenter.backup(instance);
		res.put("data", "success!");
		_res = JSONArray.fromObject(res).toString();
		return _res;
	}
	
	private String resetInstanceState(String instance,String ip) {
		String _res = "400";
		HashMap<String, Object> res = new HashMap<String, Object>(); 
		String str = HttpClient.sendGet("http://" + ip
				+ "/search.doaction", "ac=resetInstanceState&code="+getCode("resetInstanceState")+"&instance="+instance);
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
	 
	
	private String getCode(String ac){ 
		return MD5Util.SaltMd5(ac);
	}
}
