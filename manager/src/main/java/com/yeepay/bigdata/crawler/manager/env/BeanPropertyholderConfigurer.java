package com.yeepay.bigdata.crawler.manager.env;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 可以按照不同的运行模式启用相应的配置
 * 
 */
public class BeanPropertyholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {
    private static final String PRODUCTION_MODE = "production.mode";

    //设置运行程序的模式，默认是开发模式，当进入测试或者发布上线就修改为其他模式
    private String              mode            = "DEV";

    //缓存所有的属性配置
    private Properties          properties;

    //配置文件存放路径
    private String              path            = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                                                        + "/config/*.properties";

    private static final Logger       logger    = Logger.getLogger(BeanPropertyholderConfigurer.class);

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /*
     * 按照设置的运行模式读取相应的配置文件，如果没有就读取默认的配置
     * @see
     * org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
     * #resolvePlaceholder(java.lang.String, java.util.Properties)
     */
    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {
        String result = "";
        String place = StringUtils.isNotBlank(mode)&&mode.equals("ONLINE")?placeholder:placeholder + "_" + mode.toUpperCase().trim();
        result = super.resolvePlaceholder(place, props);
        if (StringUtils.isNotBlank(result)) {
            return result;
        }       
        return  super.resolvePlaceholder(placeholder, props); 
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] userResources = resourcePatternResolver.getResources(path);

            List<Resource> resourceList = new ArrayList<Resource>();
            if (userResources != null) {
                resourceList.addAll(Arrays.asList(userResources));
            }

            super.setLocations(resourceList.toArray(new Resource[] {}));

            Properties result = this.mergeProperties();
            //系统属性优先
            mode = System.getProperty(PRODUCTION_MODE);
            if (StringUtils.isBlank(mode)) {
                String str = super.resolvePlaceholder("mode", result);
                if (str != null) {
                    this.mode = str;
                } else {
                    mode = "DEV";
                }
            }
            this.properties = result;
            if (logger.isDebugEnabled()) {
                logger.debug("production model is " + mode);
            }
        } catch (IOException e) {
            logger.error("load resource error ", e);
            throw new RuntimeException(e);
        }
    }

    public String getProperty(String key) {
        return resolvePlaceholder(key, properties);
    }
}
