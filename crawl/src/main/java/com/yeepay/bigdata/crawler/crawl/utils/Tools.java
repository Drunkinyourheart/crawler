package com.yeepay.bigdata.crawler.crawl.utils;

import com.google.common.net.InternetDomainName;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * 类Tools.java的实现描述：TODO 类实现描述
 */

public class Tools {
    private static final Logger LOGGER = Logger.getLogger(Tools.class);

    public static String getFirstDomain(String url){
        String domain = "";
        if (StringUtils.isBlank(url)) {
            return domain;
        }
        url = url.replaceAll("http://", "").replaceAll("https://", "");
        url = StringUtils.substringBefore(url, "/");
        try {
           domain =  InternetDomainName.from(url).topPrivateDomain().name();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return domain;
    }
    
    public static void main(String[] args) {
        String domain = getFirstDomain("http://www.baidu.com");
        System.out.println(domain);
    }
    

}
