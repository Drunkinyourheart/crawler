package com.yeepay.bigdata.crawler.manager.heartbeat;


import com.yeepay.bigdata.crawler.schedule.thrift.client.CrawlerAssignTaskClient;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class CrawlerNodeSelector implements ClientNodeSelector<CrawlerAssignClientNode, CrawlerAssignTaskClient> {

    private CopyOnWriteArrayList<CrawlerAssignClientNode> crawlerAssignClientNodes;
    private Random random = new Random();

    /**
     * Constructure
     */
    public CrawlerNodeSelector() {
        this.crawlerAssignClientNodes = new CopyOnWriteArrayList<CrawlerAssignClientNode>();
    }

    @Override
    public CrawlerAssignClientNode selectClientNode(String domain) {
        int index = random.nextInt(crawlerAssignClientNodes.size());
        return crawlerAssignClientNodes.get(index);
    }

    @Override
    public void addClientNode(CrawlerAssignClientNode clientNode) {
        crawlerAssignClientNodes.add(clientNode);
    }

}
