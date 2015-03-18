package com.yeepay.bigdata.crawler.manager.heartbeat;


import com.yeepay.bigdata.crawler.schedule.thrift.client.PageClient;

public class PageClientNode extends ClientNode<PageClient> {

    @Override
    protected PageClient createClient() {
        return new PageClient(host, port);
    }

}
