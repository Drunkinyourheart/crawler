package com.yeepay.bigdata.crawler.schedule.thrift.client;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTask;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskAssignService;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResponse;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jerry
 */
public class CrawlerAssignTaskClient extends BasicThriftClient<TFramedTransport> implements CrawlerTaskAssignService.Iface {

    public CrawlerAssignTaskClient() {
        super();
    }

    public CrawlerAssignTaskClient(String host, int port) {
		super(new ThriftTransportPool.TFramedTransportFactory(host, port));
//        super(new ThriftTransportPool.TFramedTransportFactory("localhost", 8181));
    }

    public CrawlerAssignTaskClient(
            ThriftTransportPool<TFramedTransport> tTransportPool) {
        super(tTransportPool);
    }

    @Override
    public CrawlerTaskResponse assignCrawlerTask(CrawlerTask task)
            throws TException {

        TFramedTransport transport = getThriftTransportPool().getResource();
        boolean broken = false;
        try {
            CrawlerTaskAssignService.Iface client = new CrawlerTaskAssignService.Client(new TBinaryProtocol(transport));
//            CrawlerAssignTaskClient client = new CrawlerAssignTaskClient("localhost", 8181);
            CrawlerTaskResponse response = client.assignCrawlerTask(task);
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

    public static void main(String[] args) throws TException {

        Map<String, String> ctxMap = new HashMap<String, String>();
        ctxMap.put("a", "b");
        ctxMap.put("c", "c");

        CrawlerAssignTaskClient c  =  new CrawlerAssignTaskClient("localhost", 8181);
        CrawlerTask crawlerTask = new CrawlerTask("1", "http://www.huxiu.com/article/100861/1.html", false);
        crawlerTask.setCtxMap(ctxMap);
        Object o = c.assignCrawlerTask(crawlerTask);
        System.out.println("o : " + o);
    }

}
