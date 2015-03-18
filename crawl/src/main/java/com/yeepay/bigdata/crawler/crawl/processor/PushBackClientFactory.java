package com.yeepay.bigdata.crawler.crawl.processor;

import com.yeepay.bigdata.crawler.schedule.thrift.client.CrawlerTaskResultPushClient;
import com.yeepay.bigdata.crawler.schedule.thrift.client.ThriftTransportPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * CrawlerTaskResultPushClient 工厂
 */
public class PushBackClientFactory {

    private static GenericObjectPool.Config config = new GenericObjectPool.Config();

    static {
        config.maxActive = 20;
        config.maxIdle = 5;
        config.minIdle = 1;
        config.maxWait = 60000;
        config.minEvictableIdleTimeMillis = 300000;
        config.timeBetweenEvictionRunsMillis = 300000;
    }

    public static CrawlerTaskResultPushClient getClient(String host, int port) {
        return new CrawlerTaskResultPushClient(host, port);
    }

    public static CrawlerTaskResultPushClient getPooledClient(String host, int port) {
        return new CrawlerTaskResultPushClient(new ThriftTransportPool(config, host, port));
    }

    public static CrawlerTaskResultPushClient getPooledClient(String host, int port, GenericObjectPool.Config customConfig) {
        return new CrawlerTaskResultPushClient(new ThriftTransportPool(customConfig, host, port));
    }

}
