package com.yeepay.bigdata.crawler.manager.handler;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.manager.constants.SchedulerConstants;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.manager.model.TaskStatus;
import com.yeepay.bigdata.crawler.manager.utils.HotNewsLogger;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;

public class OtherProcessor extends CrawlerTaskResultProcessor {


    private static Logger LOGGER = Logger.getLogger(OtherProcessor.class);
    private BlockingQueue<Task> outputQueue;

    /**
     * Constructure
     */
    public OtherProcessor(BlockingQueue<Task> outputQueue) {
        this.outputQueue = outputQueue;
    }

    @Override
    public void processSucceedResult(CrawlerTaskResult result, Task task) {
        try {
//                Boolean exist = PageClientUtils.filterURL(task.getUrl(), task);
            Boolean exist = false;
            if (!exist) {
                task.setCrawleData(result.getData());
                task.setStatus(TaskStatus.CRAWLED);
                long startCrawlerTs = getSeedStartCrawlerTsFromCrawlerTs(task);
                task.setCtxMap(result.getCtxMap());
                if (startCrawlerTs > 0) {
                    if (task.getCtxMap() != null && task.getCtxMap().containsKey(SchedulerConstants.START_CRAWLER_TS)) {
                        try {
                            task.getCtxMap().put(SchedulerConstants.START_CRAWLER_TS, startCrawlerTs + "");
                        } catch (Exception e) {
                            LOGGER.error("get startCrawlerTs exeception : ", e);
                        }
                    }
                }
//                PageClientUtils.savePage(task);
                task.setCrawleData("");
                outputQueue.put(task);
                HotNewsLogger.logSource(task);//打一下指定URL的抓取的结果日志
            }
            statOtherLog(result, exist, task);
        } catch (InterruptedException e) {
            try {
                outputQueue.put(task);
            } catch (InterruptedException e1) {
                LOGGER.error("crawled result is failed..", e1);
            }
        }
    }
}

