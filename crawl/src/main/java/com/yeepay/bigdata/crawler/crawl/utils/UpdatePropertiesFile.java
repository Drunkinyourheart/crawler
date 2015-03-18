package com.yeepay.bigdata.crawler.crawl.utils;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.Set;


public class UpdatePropertiesFile implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(UpdatePropertiesFile.class);
	private static Set<String> blackList= new HashSet<String>();
	
	public static Set<String> getBlackList() {
		return blackList;
	}
	public static void setBlackList(Set<String> blackList) {
		UpdatePropertiesFile.blackList = blackList;
	}
	public static boolean  loadPropertiesFile(String filePath){
		blackList.clear();
		File file = new File(filePath);
		try{
			if (file.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String temp = "";
				while ((temp = reader.readLine()) != null) {
					blackList.add(temp);
				}
				LOGGER.info("propertiesfile exists.filepath="+file.getAbsolutePath());
				return true;
			}
            LOGGER.info("propertiesfile isnot exist.filepath:"+file.getAbsolutePath());
			InputStream in = UpdatePropertiesFile.class.getResourceAsStream("/blackmingdan.properties");
			BufferedReader br;
			if (in == null) {
				throw new IOException("load "+filePath+" failed!");
			}
			br = new BufferedReader(new InputStreamReader(in));
			String temp = "";
			while((temp=br.readLine())!=null){
				if(temp.trim().equals("")){
					continue;
				}
				blackList.add(temp.trim());
			}
			return true;
		}catch(IOException e){
			LOGGER.error("load properFile exception",e);
		}
		return false;
	}
	public static boolean isBlackUrl(String url){
		   if(url==null)return false;
	       Set<String> blackList  = UpdatePropertiesFile.getBlackList();
	       for(String black:blackList){
	    	   if(url.contains(black)){
	    		   return true;
	    	   }
	       }
	       return false;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		loadPropertiesFile("C:\\Users\\jun\\Desktop\\blackmingdan.propertie");

	}
	@Override
	public void run() {
		while(true){
			loadPropertiesFile("conf/blackmingdan.properties");
			try {
				Thread.sleep(2*60*60*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
