package com.yeepay.bigdata.crawler.manager.handler;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.manager.constants.SchedulerConstants;
import com.yeepay.bigdata.crawler.manager.model.SeedInfoType;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.manager.model.TaskStatus;
import com.yeepay.bigdata.crawler.manager.utils.DomainUtils;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * 处理seed页: 种子页面无跟踪信息,生成task任务
 */
public class SeedProcessor extends CrawlerTaskResultProcessor {

    private static Logger LOGGER = Logger.getLogger(HotNewsProcessor.class);
    private BlockingQueue<Task> outputQueue;

    /**
     * Constructure
     */
    public SeedProcessor(BlockingQueue<Task> outputQueue) {
        this.outputQueue = outputQueue;
    }

    /**
     * 种子页面，不需要预处理
     */
    @Override
    public boolean isNeedPrerequisite() {
        return false;
    }

    @Override
    public void processSucceedResult(CrawlerTaskResult result, Task task) {

        task = new Task();
        task.setId(result.getId());
        task.setUrl(result.getUrl());
        task.setCrawleData(result.getData());
        task.setDomain(DomainUtils.getTopPrivateDomain(result.getUrl()));
        task.setStatus(TaskStatus.CRAWLED);
        task.setSeedType(getSeedInfoType(result));
        task.setFromURL(result.getCtxMap().get(SchedulerConstants.FROM_URL));
        // result.getCtxMap()
        // .put(SchedulerConstants.FROM_URL, result.getUrl());

        task.setCtxMap(result.getCtxMap());
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
        try {
            outputQueue.put(task);
        } catch (InterruptedException e) {
            try {
                outputQueue.put(task);
            } catch (InterruptedException e1) {
                LOGGER.error("crawled result is failed..", e1);
            }
        }
        statLog(result, task);
    }

    private static SeedInfoType getSeedInfoType(CrawlerTaskResult result) {
        Map<String, String> ctxMap = result.getCtxMap();
        if (ctxMap != null && ctxMap.containsKey(SchedulerConstants.SEED_INFO_TYPE)) {
            return SeedInfoType.getSeedType(ctxMap.get(SchedulerConstants.SEED_INFO_TYPE));
        }
        return SeedInfoType.DETAIL;
    }

}

