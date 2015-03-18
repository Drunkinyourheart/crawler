package com.yeepay.bigdata.crawler.manager.selector;

import com.yeepay.bigdata.crawler.crawl.thrift.service.*;
import com.yeepay.bigdata.crawler.schedule.thrift.Response;
import com.yeepay.bigdata.crawler.schedule.thrift.ResponseStatus;
import com.yeepay.bigdata.crawler.schedule.thrift.client.CrawlerTaskResultPushClient;
import com.yeepay.bigdata.crawler.schedule.thrift.server.CrawlerAssignTaskThriftServer;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CrawlerServer {

    /**
     * @param args
     * @throws java.io.IOException
     * @throws org.apache.thrift.TException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, TException,
            InterruptedException {

        CrawlerAssignTaskThriftServer server = new CrawlerAssignTaskThriftServer(8080, new CrawlerTaskAssignService.Iface() {

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

            CrawlerTaskResultPushClient client = new CrawlerTaskResultPushClient("localhost", 6080);

            @Override
            public CrawlerTaskResponse assignCrawlerTask(CrawlerTask task) throws TException {
                final String[] fields = new String[2];

                fields[0] = task.getId();
                fields[1] = task.getUrl();
                executor.schedule(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            client.pushCrawlerTaskResult(new CrawlerTaskResult(
                                    fields[0], fields[1], "hello",
                                    CrawlerTaskResultStatus.Succeed));
                        } catch (TException e) {
                            e.printStackTrace();
                        }
                    }
                }, 100, TimeUnit.MILLISECONDS);
                System.out.println(task.toString());
                return new CrawlerTaskResponse(new Response(task
                        .getId(), ResponseStatus.Success));
            }
        });

        server.start();

    }
}
