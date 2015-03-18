package com.yeepay.bigdata.crawler.crawl.bootstrap;

import com.yeepay.bigdata.crawler.crawl.crawler.Crawler;
import com.yeepay.bigdata.crawler.crawl.crawler.SeedTypeEnum;
import com.yeepay.bigdata.crawler.crawl.fetcher.HttpClientFetcher;
import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;
import com.yeepay.bigdata.crawler.crawl.monitor.Monitor;
import com.yeepay.bigdata.crawler.crawl.service.CrawlerHandler;
import com.yeepay.bigdata.crawler.crawl.service.HttpServer;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTask;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskAssignService;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResponse;
import com.yeepay.bigdata.crawler.crawl.utils.UpdatePropertiesFile;
import com.yeepay.bigdata.crawler.schedule.thrift.Response;
import com.yeepay.bigdata.crawler.schedule.thrift.ResponseStatus;
import com.yeepay.bigdata.crawler.schedule.thrift.server.CrawlerAssignTaskThriftServer;
import com.yeepay.bigdata.crawler.schedule.thrift.server.Server;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * 抓取启动类
 */
public class BootStrap {

    private static final Logger LOGGER = Logger.getLogger(BootStrap.class);

    public static void main(String[] args) throws TTransportException, IOException, InterruptedException {
        //以下两行用于测试
        String[] argss = {"8181"};
        if (args.length == 0 ) {
            args = argss;
        }

        if (args.length < 1) {
            System.err.println("thrift port must be given");
            System.exit(1);
        }
        //add blackList file
        UpdatePropertiesFile blackProper = new UpdatePropertiesFile();
        Thread blackProperThread = new Thread(blackProper);
        blackProperThread.start();

        final Crawler crawler = new Crawler(200000, 2);
        crawler.start();

//        final  ListMsgContext listMsgContext = new ListMsgContext(crawler);
//        listMsgContext.start();
        final Monitor monitor = new Monitor();
        monitor.addMonitored(crawler);
        monitor.start();

        int thriftPort = Integer.parseInt(args[0]);
/** 用于测试 */
        crawler.addURL(new CrawlURL("idiasdsaddqghjgwewqqwqweqwee", "http://hotashang.baijia.baidu.com/article/15236", "static"));//TODO

        final Server server = new CrawlerAssignTaskThriftServer(thriftPort, new CrawlerTaskAssignService.Iface() {

            @Override
            public CrawlerTaskResponse assignCrawlerTask(CrawlerTask task) throws TException {

                CrawlURL crawlURL = null;
                System.out.println(task.getCtxMap());
                String seedType=task.getCtxMap().get("seedType");
                if (seedType == null) {
                    seedType = SeedTypeEnum.HOTNEWSDETAIL.name();
                }
                //处理热闻
//                Map<String, String> map = new HashMap<String, String>();
//                map.put("seedType", SeedTypeEnum.HOTNEWSDETAIL.name());
//                task.setCtxMap(map);
//                String seedType = task.getCtxMap().get("seedType");
                System.out.println("seedtype : " + seedType);
                if (seedType.equalsIgnoreCase(SeedTypeEnum.HOTNEWSDETAIL.name())) {
                    if (StringUtils.isBlank(task.getId())) {
                        task.setId(DigestUtils.md5Hex(task.getUrl()));
                    }
                    crawlURL = new CrawlURL(task.getId(), task.getUrl(), "static");
                    crawlURL.setSeedType(seedType);
                    crawlURL.setFromURL(task.getUrl());
                    crawlURL.setTimestamp(System.currentTimeMillis());
                } else {
                    crawlURL = new CrawlURL(task.getId(), task.getUrl(), task.isIsDynamic() ? "dynamic" : "static");
                    crawlURL.setSeedType(task.getCtxMap().get("seedType"));
                    crawlURL.setFromURL(task.getCtxMap().get("fromURL"));
                    if (StringUtils.isNotBlank(task.getCtxMap().get("startCrawlerTs"))) {//统计抓取时间的
                        crawlURL.setTimestamp(Long.valueOf(task.getCtxMap().get("startCrawlerTs")));
                    } else {
                        crawlURL.setTimestamp(System.currentTimeMillis());
                    }
                    //接收刊物，频道
                    crawlURL.setPublicationName(task.getCtxMap().get("publicationName"));
                    crawlURL.setTypeArea(task.getCtxMap().get("typeArea"));
                    crawlURL.setChannel(task.getCtxMap().get("channel"));
                    crawlURL.setSub_channel(task.getCtxMap().get("sub_channel"));
                    crawlURL.setSourceSeedType(task.getCtxMap().get("sourceSeedType"));
                }

                LOGGER.info("receive crawlURL " + crawlURL.getId() + ", and url is " + crawlURL.getUrl());
                boolean result = crawler.addURL(crawlURL);
                ResponseStatus status = result ? ResponseStatus.Success : ResponseStatus.Failure;
                if (status.getValue() == ResponseStatus.Failure.getValue()) {
                    LOGGER.error("queue size exceeded!!crawl url failer with crawlURL " + task);
                }
                Response response = new Response(task.getId(), status);
                System.out.println("BootStrap : " + response);
                return new CrawlerTaskResponse(response);
            }
        });
        server.start();
        final HttpServer httpServer = new HttpServer(new InetSocketAddress(8800), new CrawlerHandler(new HttpClientFetcher(), crawler));
        httpServer.start();
        // http://localhost:8800/?url=http://www.huxiu.com/article/100861/1.html
        System.out.println("crawler http server is started!port is " + 8800);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                crawler.stop();
                server.stop();
                monitor.destroy();
//                httpServer.stop();
//                listMsgContext.stop();
//                zRegister.unregister();
            }

        });
        crawler.join();
    }

}
