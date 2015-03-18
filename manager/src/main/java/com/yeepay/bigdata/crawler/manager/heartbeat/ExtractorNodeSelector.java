package com.yeepay.bigdata.crawler.manager.heartbeat;

import com.yeepay.bigdata.crawler.schedule.thrift.client.ExtractorClient;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExtractorNodeSelector implements ClientNodeSelector<ExtractorClientNode, ExtractorClient> {

    private CopyOnWriteArrayList<ExtractorClientNode> extractorClientNodes;
    private Random random = new Random();

    public ExtractorNodeSelector() {
        this.extractorClientNodes = new CopyOnWriteArrayList<ExtractorClientNode>();
    }

    @Override
    public ExtractorClientNode selectClientNode(String domain) {
        int index = random.nextInt(extractorClientNodes.size());
        return extractorClientNodes.get(index);
    }

    @Override
    public void addClientNode(ExtractorClientNode clientNode) {
        extractorClientNodes.add(clientNode);
    }

}
