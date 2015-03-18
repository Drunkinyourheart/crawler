package com.yeepay.bigdata.crawler.crawl.crawler;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTask;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultPushService;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultResponse;
import com.yeepay.bigdata.crawler.schedule.thrift.Response;
import com.yeepay.bigdata.crawler.schedule.thrift.ResponseStatus;
import com.yeepay.bigdata.crawler.schedule.thrift.client.CrawlerAssignTaskClient;
import com.yeepay.bigdata.crawler.schedule.thrift.server.CrawlerTaskResultPushThriftServer;
import org.apache.thrift.TException;

import java.io.IOException;

/**
 *      测试使用
 */
public class Client {
	public static void main(String[] args) throws TException, InterruptedException, IOException {
//		CrawlerAssignTaskClient client = new CrawlerAssignTaskClient("127.0.0.1",8181);
		CrawlerAssignTaskClient client = new CrawlerAssignTaskClient("localhost",8181);
//		CrawlerTaskResultPushThriftServer server = new CrawlerTaskResultPushThriftServer(8889,new CrawlerTaskResultPushService.Iface(){
//
//			@Override
//			public CrawlerTaskResultResponse pushCrawlerTaskResult(
//					CrawlerTaskResult paramCrawlerTaskResult) throws TException {
//				System.out.println(paramCrawlerTaskResult);
//				return new CrawlerTaskResultResponse(new Response(paramCrawlerTaskResult.getId(), ResponseStatus.Success));
//			}
//
//		});
//		server.start();
//		System.out.println(client.assignCrawlerTask(new CrawlerTask("1", "http://www.sohu.com/")));
//		System.out.println(client.assignCrawlerTask(new CrawlerTask("1", "http://www.sohu.com/", false)));
		System.out.println(client.assignCrawlerTask(new CrawlerTask("1", "http://www.huxiu.com/article/100861/1.html", false)));
		System.out.println(client.assignCrawlerTask(new CrawlerTask("111", "http://tech.sina.com.cn/i/2014-11-07/08439769884.shtml", false)));
//		Thread.sleep(1000000);
		Thread.sleep(1000);
//		client.assignCrawlerTask(new CrawlerTask("2", "http://www.sohu.com"));
//		client.assignCrawlerTask(new CrawlerTask("3", "http://www.sohu.com"));
	}

}
