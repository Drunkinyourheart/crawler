package com.yeepay.bigdata.crawler.crawl.processor;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultStatus;
import com.yeepay.bigdata.crawler.schedule.thrift.client.CrawlerTaskResultPushClient;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

class ThriftClientManager {
    private static final Logger logger = Logger.getLogger(ThriftClientManager.class);
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(ClassLoader.getSystemResourceAsStream("crawler.properties"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private static CrawlerTaskResultPushClient client = PushBackClientFactory.getPooledClient(properties.getProperty("ip"),
            Integer.parseInt(properties.getProperty("port")));

    public static CrawlerTaskResultPushClient getPooledClient() {
        return client;
    }

    public static CrawlerTaskResultPushClient getClient() {
        System.out.println("ip : port = " +  (properties.getProperty("ip") + properties.getProperty("port")));
        return PushBackClientFactory.getClient(properties.getProperty("ip"), Integer.parseInt(properties.getProperty("port")));
    }

    public static void main(String[] args) throws TException {
        CrawlerTaskResult c = new CrawlerTaskResult();
        c.setCtxMap(new HashMap<String, String>());
        c.setCtxMapIsSet(true);
        c.setData("ddd");
        c.setDataIsSet(true);
        c.setId("id");
        c.setUrl("abcwww.");
        c.setStatus(CrawlerTaskResultStatus.Error);


        ThriftClientManager.getClient().pushCrawlerTaskResult(c);
        System.out.println("emit");
    }

}
