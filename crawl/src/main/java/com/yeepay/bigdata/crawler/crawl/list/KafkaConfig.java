package com.yeepay.bigdata.crawler.crawl.list;

import java.util.Properties;

/**
 * kafka的配置，只支持high level api
 */
public class KafkaConfig {
    private String topic;
    private String group;
    private String zkConnect;
    private Properties properties;
    
    public KafkaConfig(String topic,String group,String zkConnect,Properties properties){
        assert topic!=null&&group!=null&&zkConnect!=null;
        this.topic = topic;
        this.group = group;
        this.zkConnect = zkConnect;
        this.properties = properties;
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }
    
    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }
    
    /**
     * @return the zkConnect
     */
    public String getZkConnect() {
        return zkConnect;
    }

    /**
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }
}

