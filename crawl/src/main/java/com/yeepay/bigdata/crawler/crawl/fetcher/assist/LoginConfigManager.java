package com.yeepay.bigdata.crawler.crawl.fetcher.assist;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 类LoginConfigManager.java的实现描述：TODO 类实现描述 
 */
final class LoginConfigManager {
    private static final Logger LOGGER = Logger.getLogger(LoginConfigManager.class);
    private static final Properties properties = new Properties();
    private static final Map<String, LoginConfig> CHECKCONFIG_MAP =new HashMap<String, LoginConfig>();
    
    static{
        try {
            properties.load(ClassLoader.getSystemResourceAsStream("cookieCheck.properties"));
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                CHECKCONFIG_MAP.put(entry.getKey().toString(), LoginConfig.getLoginModel(entry.getValue().toString()));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    static Set<String> getSiteKeySet(){
        return CHECKCONFIG_MAP.keySet();
    }
    
    static LoginConfig getLoginConfig(String key){
        return CHECKCONFIG_MAP.get(key);
    }
    

}
