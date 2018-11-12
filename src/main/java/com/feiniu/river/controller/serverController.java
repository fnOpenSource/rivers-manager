package com.feiniu.river.controller;

import com.feiniu.river.config.GlobalConfig;
import com.feiniu.river.service.RiverCenter;
import com.feiniu.river.util.Folder;
import com.feiniu.river.util.HttpClient;
import com.feiniu.river.util.MD5Util;
import com.feiniu.river.util.ZKUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Controller
@RequestMapping("/server")
public class serverController {
    private final static Logger log = LoggerFactory.getLogger(serverController.class);


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
                + "datas/" + instance, GlobalConfig.zkInstancePath);

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

    @RequestMapping(value = "/getResourceXMLConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public byte[] getResourceXMLConfig(@RequestParam String resource) {
        byte[]  resource_xml = ZKUtil.getData("/resource.xml", true);
        return resource_xml;
    }

    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getResourceConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getResourceConfig(@RequestParam String resource) {
        String _res = "400";
        String[] resourcecontent = resource.split(",");
        //返回map [sql:{xxx},nosql:{xxx}]
        HashMap<String, Object> res = new HashMap<String, Object>();
        //存储 sql下的list socket

        SAXReader reader = new SAXReader();
        try {
            byte[]  resource_xml = ZKUtil.getData("/resource.xml", true);
            Document doc = reader.read(new ByteArrayInputStream(resource_xml));
            Element root = doc.getRootElement();
//			Map<String,LinkedHashMap<String,String>> sockets_map = null;
            List sockets_map = null;
            // 分别获取 sql 和 nosql 下的节点存入返回的map中
            for(int r=0; r<resourcecontent.length; r++){
//				sockets_map =  new LinkedHashMap<String,LinkedHashMap<String,String>>();
                sockets_map = new ArrayList<String>();
                Element sql=root.element(resourcecontent[r]);
                List socket = sql.elements();
                int i = 0;
//				String name ="";
                //存储 socket下的list node
                LinkedHashMap<String,String> socket_content_map = null;
                for (Iterator<?> it = socket.iterator(); it.hasNext(); ) {
                    socket_content_map =  new LinkedHashMap<String,String>();

                    i++;
                    Element elm = (Element) it.next();
                    List<?> socket_contents = elm.elements();
                    for (Iterator<?> socket_item = socket_contents.iterator(); socket_item.hasNext(); ) {
                        Element socket_content = (Element) socket_item.next();
                        socket_content_map.put(socket_content.getName(), socket_content.getText());
                    }
//					sockets_map.put(name , socket_content_map);
                    sockets_map.add(socket_content_map);
                }
                String sqljson = JSONArray.fromObject(sockets_map).toString();
                res.put(resourcecontent[r],sqljson);
            }
        } catch (Exception e) {
            res.put("data",e.getMessage());
            e.printStackTrace();
        }
        _res =  JSONArray.fromObject(res).toString();
        return _res;
    }

    @RequestMapping(value = "/getResourceElementConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getResourceElementConfig(@RequestParam String resourcetype,String elementname){
        String _res = "400";
        //返回map [sql:{xxx},nosql:{xxx}]
        HashMap<String, Object> res = new HashMap<String, Object>();
        SAXReader reader = new SAXReader();
        try {
            byte[]  resource_xml = ZKUtil.getData("/resource.xml", true);
            Document doc = reader.read(new ByteArrayInputStream(resource_xml));
            Element root = doc.getRootElement();
            Element sql=root.element(resourcetype);
            List socket = sql.elements();
            //存储 socket下的list node
            LinkedHashMap<String,String> socket_content_map = null;
            for (Iterator it = socket.iterator(); it.hasNext();) {
                Element elm = (Element) it.next();
                if(elementname.equals(elm.element("name").getText().trim())){
                    socket_content_map =  new LinkedHashMap<String,String>();
                    List socket_contents = elm.elements();
                    for (Iterator socket_item = socket_contents.iterator(); socket_item.hasNext();) {
                        Element socket_content = (Element) socket_item.next();
                        socket_content_map.put(socket_content.getName(), socket_content.getText());
                    }
                    break;
                }else{
                    continue;
                }
            }
            String sqljson = JSONArray.fromObject(socket_content_map).toString();
            res.put("data",sqljson);
        } catch (Exception e) {
            res.put("data",e.getMessage());
            e.printStackTrace();
        }
        _res =  JSONArray.fromObject(res).toString();
        return _res;
    }
    @RequestMapping(value = "/DeletResourceElementConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String DeletResourceElementConfig(@RequestParam String resourcetype,String elementname) {
        String _res = "400";
        HashMap<String, Object> res = new HashMap<String, Object>();
        SAXReader reader = new SAXReader();
        try {
            byte[]  resource_xml = ZKUtil.getData("/resource.xml", true);
            Document doc = reader.read(new ByteArrayInputStream(resource_xml));
            Element root = doc.getRootElement();
            Element sql=root.element(resourcetype);
            List socket = sql.elements();
            //存储 socket下的list node
            for (Iterator it = socket.iterator(); it.hasNext();) {
                Element elm = (Element) it.next();
                if(elementname.equals(elm.element("name").getText().trim())){
                    System.out.print("==== delete Element <socket> name:" + elementname +" =====");
                    sql.remove(elm);
                    break;
                }else{
                    continue;
                }
            }
            ZKUtil.setData("/resource.xml", doc.asXML(), true);
            res.put("data","success");
        } catch (Exception e) {
            res.put("data",e.getMessage());
            e.printStackTrace();
        }
        _res =  JSONArray.fromObject(res).toString();
        return _res;
    }
    @RequestMapping(value = "/updateResourceXml", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateResourceXml(@RequestParam String resourcetype,String resourcejson) {
        String _res = "400";
        HashMap<String, String> resourcemap = new HashMap<String, String>();
        HashMap<String, Object> res = new HashMap<String, Object>();
        String[]  resourcelist = resourcejson.split("&");
        for (int i =0;i<resourcelist.length;i++){
            String[] resource = resourcelist[i].split("=");
            String resourcekey = resource[0];
            String resourcevalue = resource[1];
            resourcemap.put(resourcekey,resourcevalue);
        }
        byte[]  resource_xml = ZKUtil.getData("/resource.xml", true);
        Map<String,String> copydate = resourcemap;
        String sqlmapname = resourcemap.get("name");
        String sqlmaptype = resourcemap.get("type");
        SAXReader reader = new SAXReader();
        try {
            //获取到每个标签 若没有对应name的资源，那么就新增一个节点
            Document doc = reader.read(new ByteArrayInputStream(resource_xml));
            Element root = doc.getRootElement();
            Element sql=root.element(resourcetype);
            List socketlist = sql.elements();
            boolean isexist = false;
            for (Iterator it = socketlist.iterator(); it.hasNext();) {
                Element socket = (Element) it.next();
                if(sqlmapname.equals(socket.element("name").getTextTrim())){
                    isexist =true;
                    List socket_contentlist = socket.elements();
                    for (Iterator socket_item = socket_contentlist.iterator(); socket_item.hasNext();) {
                        Element socket_content = (Element) socket_item.next();
                        // 传入值得对应标签 在 socket中原本就存在
                        if(resourcemap.get(socket_content.getName())!=null&&resourcemap.get(socket_content.getName())!=""){
                            // 并且  传入值得对应标签内容 ！= socket中对应标签的内容
                            if(socket_content.getText()!=resourcemap.get(socket_content.getName())
                                    && !socket_content.getText().equals(resourcemap.get(socket_content.getName()))){
                                //更新<socket>节点下<name>/<type>等的value
                                System.out.println("===== update Element <socket> name: " +socket_content.getName() +"："+socket_content.getText()+ "--->" +resourcemap.get(socket_content.getName()) );
                                socket_content.setText(resourcemap.get(socket_content.getName()));
                            }
                            resourcemap.remove(socket_content.getName());

                        }else if(resourcemap.get(socket_content.getName())==null || resourcemap.get(socket_content.getName())==""){
                            //删除<socket>节点下<name>/<type>等节点
                            System.out.println("===== delete Element <socket>  name:" + socket_content.getName() +" 节点 ======");
                            socket.remove(socket_content);
                            resourcemap.remove(socket_content.getName());
                        }
                    }
                    if(resourcemap.size()>0){
                        //说明有新增标签
                        for (Map.Entry<String, String> entry : resourcemap.entrySet()) {
                            System.out.println("=====  Element <socket> Nodename: " + sqlmapname  + "  add :" + entry.getKey()+" ，value=" +entry.getValue()+"======") ;
                            Element socketinfo = socket.addElement(entry.getKey());
                            socketinfo.setText(entry.getValue());
                        }
                    }
                }else{
                    continue;
                }
            }
            //循环后都不存在 则新增
            if(!isexist){
                //在NoSql下 或者在Sql下新建<socket>节点
                Element newelement = sql.addElement("socket");
                System.out.println("===== add  <socket> name:"+ sqlmapname +"=====");
                for (Map.Entry<String, String> entry : resourcemap.entrySet()) {
                    Element socketinfo = newelement.addElement(entry.getKey());
                    socketinfo.setText(entry.getValue());
                }
            }
//			System.out.println(doc.asXML());
            //更新zk 以及 post到每台机器上加载
            ZKUtil.setData("/resource.xml", doc.asXML(), true);
            if(isexist){
                String jsonStr = JSONArray.fromObject(copydate).toString();
                for (String path : GlobalConfig.riverUrls.split(",")) {
                    String str = HttpClient.sendGet("http://" + path + "/search.doaction",
                            "ac=addResource&type=" + sqlmaptype
                                    + "&code="+getCode("updateResourceXml")) + "&socket="+jsonStr;
                }
            }
            res.put("data", "success");
        } catch (Exception e) {
            res.put("data",e.getMessage());
            e.printStackTrace();
        }
        _res =  JSONArray.fromObject(res).toString();
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

    /**
     *
     * @param instance 创建实例名
     * @param data xml
     * @return
     */
    @RequestMapping(value = "/createInstanceConfig", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String createInstanceConfig(@RequestParam String instance,
                                       @RequestParam String data,
                                       @RequestParam String type) {
        String _res = "100";
        try {
            data = data.trim();
            instance = instance.trim();
            type = type.trim();
            SAXReader reader = new SAXReader();
            Document doc = reader.read(new ByteArrayInputStream(data.getBytes("utf-8")));
            Element root = doc.getRootElement();
            String seq="";
            try {
                seq = root.element("dataflow").element("ReadParam").element("sql").attribute("seq").getValue();
            }catch (Exception e){

            }

            String folder = GlobalConfig.zkInstancePath + "/" + instance;
            if ("".equals(seq)){
                String file=folder+ "/"+ instance + ".xml";
                ZKUtil.createPath(folder,true);
                ZKUtil.create(file);
                ZKUtil.setData(file, data, false);
                file = folder + "/batch";
                ZKUtil.create(file);
            }else {
                String[] seqs=seq.split(",");
                ZKUtil.createPath(folder,true);
                for(String i:seqs){
                    String folderSeq=folder+"/"+i;
                    String file=folderSeq+ "/"+ instance + ".xml";
                    ZKUtil.createPath(folderSeq,true);
                    ZKUtil.create(file);
                    ZKUtil.setData(file, data, false);
                    file=folderSeq+ "/batch";
                    ZKUtil.create(file);
                }

            }
            if (!"".equals(type)) {
                pushInstanceToNode(instance + ":" + type);
            }

        } catch (Exception e) {
            _res = "400";
            log.error(""+e);
        }
        return _res;
    }

    @RequestMapping(value = "/InstancesAction", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String InstancesAction(@RequestParam String action,
                                  @RequestParam String instance,@RequestParam String ip) {
        String _res = "100";
        switch (action) {
            case "removeInstanceFromNode":
                removeInstanceFromNode(instance, ip);
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
                             @RequestParam String instancetype, @RequestParam String instance, @RequestParam String type) {
        return HttpClient.sendGet("http://" + ip + "/search.doaction",
                "ac=resumeInstance&instance=" + instance
                        + "&code="+getCode("resumeInstance")+ "&type="+type);
    }

    @RequestMapping(value = "/stop_job", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String stop_job(@RequestParam String ip,
                           @RequestParam String instancetype, @RequestParam String instance, @RequestParam String type) {
        return HttpClient.sendGet("http://" + ip + "/search.doaction",
                "ac=stopInstance&instance=" + instance
                        + "&code="+getCode("stopInstance")+ "&type="+type);
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

    /**
     *
     * @param instance ex: instance=robot:1  do index,2 do rabitmq ,4 do kafka
     * @return
     */
    private String pushInstanceToNode(String instance) {
        String _res = "400";
        HashMap<String, Object> res = new HashMap<String, Object>();
        String ip = getMaster();
        String str = HttpClient.sendGet("http://" + ip
                + "/search.doaction", "ac=addInstance&code="+getCode("addInstance")+"&instance="+instance);
        res.put("data", str);
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

    private String removeInstanceFromNode(String instance, String ip) {
        String _res = "400";
        HashMap<String, Object> res = new HashMap<String, Object>();
        String str = HttpClient.sendGet("http://" + ip
                + "/search.doaction", "ac=deleteInstanceData&code=" + getCode("deleteInstanceData") + "&instance=" + instance);
        str += HttpClient.sendGet("http://" + ip
                + "/search.doaction", "ac=removeInstance&code=" + getCode("removeInstance") + "&instance=" + instance);
        res.put("data", str);
        _res = JSONArray.fromObject(res).toString();
        return _res;
    }

    private String getMaster() {
        String _res = "";
        for (String path : GlobalConfig.riverUrls.split(",")) {
            try {
                String str = HttpClient.sendGet("http://" + path
                        + "/search.doaction", "ac=getStatus&code="+getCode("getStatus"));
                JSONObject jr = JSONObject.fromObject(str);
                if(JSONObject.fromObject(jr.get("info")).getString(
                        "NODE_TYPE").toLowerCase().equals("master")) {
                    _res = path;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return _res;
    }

    private String getCode(String ac){
        return MD5Util.SaltMd5(ac);
    }
}
