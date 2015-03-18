package com.yeepay.bigdata.crawler.manager.heartbeat;

import com.yeepay.bigdata.crawler.schedule.thrift.client.PageClient;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class PageNodeSelector implements ClientNodeSelector<PageClientNode, PageClient> {

    private CopyOnWriteArrayList<PageClientNode> pageNodes;

    private Random random = new Random();

    public PageNodeSelector() {
        pageNodes = new CopyOnWriteArrayList<PageClientNode>();
    }

    @Override
    public PageClientNode selectClientNode(String domain) {
        int index = random.nextInt(pageNodes.size());
        return pageNodes.get(index);
    }

    @Override
    public void addClientNode(PageClientNode clientNode) {
        pageNodes.add(clientNode);
    }

}
