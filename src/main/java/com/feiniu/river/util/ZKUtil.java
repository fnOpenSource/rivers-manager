package com.feiniu.river.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKUtil { 
	private final static int BUFFER_LEN = 1024;
	private final static int END = -1;
	private static final int CONNECTION_TIMEOUT = 50000;
	private final static CountDownLatch connectedSemaphore = new CountDownLatch(
			1);
	private static String zkHost = null;
	private static String ZKConfigPath = null;
	private static ZooKeeper zk = null;
	private static Watcher watcher = null;
	private final static Logger log = LoggerFactory.getLogger(ZKUtil.class);

	private static ZooKeeper getZk() { 
		synchronized (ZKUtil.class) {
			if (zk == null || zk.getState().equals(States.CLOSED)) {
				connection();
			}
		} 
		return zk;
	}

	private static void connection() {
		try {
			watcher = new Watcher() {
				public void process(WatchedEvent event) {
					connectedSemaphore.countDown();
				}
			};
			zk = new ZooKeeper(zkHost, CONNECTION_TIMEOUT, watcher);
			connectedSemaphore.await();
		} catch (Exception e) {
			log.error("connection Exception", e);
		}
	}

	public static void setZkHost(String zkString) {
		zkHost = zkString;
	}
	
	public static void setZkConfigPath(String zkConfigPath) {
		ZKConfigPath = zkConfigPath;
	}

	public static void setData(String filename, String Content,boolean addDefault) {
		byte[] bt = Content.getBytes();
		try {
			getZk().setData((addDefault?ZKConfigPath:"")+filename, bt, -1);
		} catch (Exception e) {
			log.error("setData Exception", e);
		} 
	}
	
	public static boolean fileExists(String file){
		Stat stat = null;
		try {
			stat = getZk().exists(file, watcher);
		}catch(Exception e){
			log.error("fileExists Exception", e);
		}
		if (null == stat) {
			return false;
		}
		return true;
	}
	
	public static void create(String file){
		try {
			getZk().create(file, "".getBytes(), Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT); 
		}catch(Exception e){
			log.error("fileExists Exception", e);
		}
	}

	public static byte[] getData(String filename,boolean addDefault) {
		try {
			return getZk().getData((addDefault?ZKConfigPath:"")+filename, watcher, null);
		} catch (Exception e) {
			log.error("getData Exception", e);
			return null;
		} 
	}
	/**
	 * 
	 * @param sourceFolder source Folder
	 * @param destinationFolder upload destination Folder
	 */
	public static void upload(String sourceFolder,String destinationFolder) {  
		if (!fileExists(destinationFolder)) {
			create(destinationFolder);
		}
		moveFile(sourceFolder, destinationFolder);  
	}

	private static void moveFile(String sourceAdd, String destinationAdd) { 
		InputStream in = null; 
		String remoteAdd = null;
		try {
			File file = new File(sourceAdd);  
			if (!file.isDirectory()) {
				in = new FileInputStream(sourceAdd);
				remoteAdd = destinationAdd + "/" + file.getName(); 
				if (!fileExists(remoteAdd)) {
					create(remoteAdd); 
				}
				StringBuffer sb = new StringBuffer();
				byte[] buffer = new byte[BUFFER_LEN];
				while (true) {
					int byteRead = in.read(buffer);
					if (byteRead == END)
						break;
					sb.append(new String(buffer, 0, byteRead));
				}
				setData(remoteAdd, sb.toString(),false);
			} else {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File readfile = new File(sourceAdd + "/"
							+ filelist[i]);
					if (!readfile.isDirectory()) {
						in = new FileInputStream(sourceAdd + "/"
								+ filelist[i]);  
						if (!fileExists(destinationAdd)) {
							create(destinationAdd);  
						}
						remoteAdd = destinationAdd + "/"
								+ file.getName(); 
						if(!fileExists(remoteAdd)){
							create(remoteAdd);  
						} 
						remoteAdd = destinationAdd + "/"
								+ file.getName() + "/"
								+ readfile.getName();
						if(!fileExists(remoteAdd)){
							create(remoteAdd);  
						}
 
						StringBuffer sb = new StringBuffer();
						byte[] buffer = new byte[BUFFER_LEN];
						while (true) {
							int byteRead = in.read(buffer);
							if (byteRead == END)
								break;
							sb.append(new String(buffer, 0, byteRead));
						}
						setData(remoteAdd, sb.toString(),false);  
					} else if (readfile.isDirectory()) { 
						String sourceAdd2 = sourceAdd + "/"
								+ readfile.getName();
						String destinationAdd2 = destinationAdd + "/"
								+ file.getName(); 
						moveFile(sourceAdd2, destinationAdd2);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}  
	} 
}
