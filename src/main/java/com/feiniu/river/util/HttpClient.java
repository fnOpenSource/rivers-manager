package com.feiniu.river.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 
public class HttpClient {  
	
	private static Logger log = LoggerFactory.getLogger(HttpClient.class); 
    
    /**
	 * GET 请求
	 * @param param  name1=value1&name2=value2 
	 */
	public static String sendGet(String url, String param) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url + "?" + param;
			URL realUrl = new URL(urlNameString);
			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect(); 
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			log.error("发送GET请求出现异常:" + e);
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}
	
	public static String sendGetWithProxy(String url, String isproxy) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = null;
			if(isproxy.equals(true)){
				InetSocketAddress addr = new InetSocketAddress("proxy2.fn.com", 8080);
		        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
		        connection = realUrl.openConnection(proxy);
			}else{
				connection = realUrl.openConnection();
			}
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送GET请求出现异常" + e);
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}
	
    /**
	 * GET DOC 请求
	 * @param param  name1=value1&name2=value2 
	 */
	public static void sendGetDoc(String url, String param, HttpServletResponse response) {
		ServletOutputStream out = null;
		BufferedInputStream in = null;
		try {		
			String urlNameString = url + "?" + param;
			URL realUrl = new URL(urlNameString);
			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect(); 
			in = new BufferedInputStream (connection.getInputStream());			
			out = response.getOutputStream();
			out.flush();
			byte[] buffer = new byte[1];
			while(in.read(buffer, 0, 1) != -1) {
				out.write(buffer, 0, 1);
			}
			in.close();
			out.close();
		} catch (Exception e) {
			log.error("发送GET DOC请求出现异常:" + e);
			e.printStackTrace();
		} 
	}

	/**
	 * POST 请求
	 * @param param  name1=value1&name2=value2  
	 */
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			out = new PrintWriter(conn.getOutputStream());
			out.print(param);
			out.flush();
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			log.error("发送POST请求出现异常:" + e);
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
    
	 /** 
     * 发送 post请求 
     */  
    @SuppressWarnings("unchecked")
    public String  sendPostWithMap(String url,Map<String, String> map) {  
    	String httpResponseString=""; 
        CloseableHttpClient httpclient = HttpClients.createDefault();   
        HttpPost httppost = new HttpPost(url); 
        try {
        	
		    UrlEncodedFormEntity uefEntity=null;  
		    if(map!=null&&map.size()!=0){ 
		    	List<NameValuePair> formparams = new ArrayList<NameValuePair>();  
				Iterator<?> iter = map.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					formparams.add(new BasicNameValuePair(key,value));  
				}
		        
		        uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");  
		    }
            httppost.setEntity(uefEntity);

            CloseableHttpResponse response = httpclient.execute(httppost);  
            try{
                HttpEntity entity = response.getEntity();  
                if (entity != null) {   
                     httpResponseString =EntityUtils.toString(entity, "UTF-8"); 
                }  
            } finally {  
                response.close();  
            }  
        } catch (ClientProtocolException e) {  
            log.error("HttpClient ClientProtocolException:",e);  
        } catch (UnsupportedEncodingException e1) {  
        	log.error("HttpClient UnsupportedEncodingException:",e1);  
        } catch (IOException e2) {  
        	log.error("HttpClient IOException:",e2);   
        } finally {    
            try {  
                httpclient.close();  
            } catch (IOException e) {  
            	log.error("HttpClient IOException:",e);   
            }  
        }  
        
        return httpResponseString;
    }   
}