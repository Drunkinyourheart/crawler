package com.yeepay.bigdata.crawler.manager.heartbeat;


import com.yeepay.bigdata.crawler.schedule.thrift.client.CrawlerAssignTaskClient;

public class CrawlerAssignClientNode extends ClientNode<CrawlerAssignTaskClient> {
    @Override
    protected CrawlerAssignTaskClient createClient() {
        return new CrawlerAssignTaskClient(host, port);
    }
}
