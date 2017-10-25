package com.feiniu.river.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Folder {
	
	/**
	 * 
	 * @param path
	 * @param onlyFolder true only folder
	 * @return
	 */
	public static List<String> getAllFiles(String path,boolean onlyFolder) {
		File dir = new File(path);
		List<String> res = new ArrayList<String>();
		for(File f:dir.listFiles()){
			if(onlyFolder && f.isDirectory())
				res.add(f.getName());
		}
		return res;
	}
}
