package com.yeepay.bigdata.crawler.schedule.thrift.client;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultPushService;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultResponse;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;

public class CrawlerTaskResultPushClient extends
        BasicThriftClient<TFramedTransport> implements
        CrawlerTaskResultPushService.Iface {

    public CrawlerTaskResultPushClient() {
        super();
    }

    public CrawlerTaskResultPushClient(String host, int port) {
        super(new ThriftTransportPool.TFramedTransportFactory(host, port));
    }

    public CrawlerTaskResultPushClient(
            ThriftTransportPool<TFramedTransport> tTransportPool) {
        super(tTransportPool);
    }

    @Override
    public CrawlerTaskResultResponse pushCrawlerTaskResult(
            CrawlerTaskResult result) throws TException {

        TFramedTransport transport = getThriftTransportPool().getResource();
        boolean broken = false;
        try {
            CrawlerTaskResultPushService.Iface client = new CrawlerTaskResultPushService.Client(new TBinaryProtocol(transport));
            CrawlerTaskResultResponse response = client
                    .pushCrawlerTaskResult(result);
            return response;
        } catch (TException e) {
            broken = true;
            throw e;
        } finally {
            if (broken) {
                getThriftTransportPool().returnBrokenResourceObject(transport);
            } else {
                getThriftTransportPool().returnResource(transport);
            }
        }
    }

}
