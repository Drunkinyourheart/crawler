package com.yeepay.bigdata.crawler.manager.handler;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.manager.constants.SchedulerConstants;
import com.yeepay.bigdata.crawler.manager.model.SeedInfoType;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.manager.utils.LogFormat;
import com.yeepay.bigdata.crawler.manager.utils.StatisticsLogger;
import com.yeepay.bigdata.crawler.manager.utils.TaskUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * 分为两类处理：1.处理 种子页面爬取结果；2.处理其他页面爬取结果
 */
public abstract class CrawlerTaskResultProcessor {

    private static Logger LOGGER = Logger.getLogger(CrawlerTaskResultProcessor.class);

    /**
     * 是否需要前置处理
     */
    public boolean isNeedPrerequisite() {
        return true;
    }

    /**
     * 设置抓取时间，来方便统计每个种子从抓取到入库耗时
     */
    public long getSeedStartCrawlerTsFromCrawlerTs(Task task) {

        Map<String, String> ctxMap = task.getCtxMap();

        long startCrawlerTs = 0;

        if (ctxMap != null && ctxMap.containsKey(SchedulerConstants.START_CRAWLER_TS)) {
            try {
                startCrawlerTs = Long.valueOf(ctxMap.get(SchedulerConstants.START_CRAWLER_TS));
            } catch (Exception e) {
                LOGGER.error("get startCrawlerTs exeception : ", e);
            }
        }
        if (task.getStartCrawlerTs() > 0) {
            startCrawlerTs = task.getStartCrawlerTs();
        }
        return startCrawlerTs > 0 ? startCrawlerTs : System.currentTimeMillis();
    }

    public abstract void processSucceedResult(CrawlerTaskResult result, Task task);

    public void processFailureResult(CrawlerTaskResult result, Task task) {
        LOGGER.warn(String.format("Crawle Result Failure : TaskId : %s ; URL : %s ; Msg : %s", result.getId(), result.getUrl(), result.getMsg()));
        statLog(result, task);
    }

    public void processErrorResult(CrawlerTaskResult result, Task task) {
        LOGGER.warn(String.format("Crawle Result Error : TaskId : %s ; URL : %s ; Msg : %s", result.getId(), result.getUrl(), result.getMsg()));
        statLog(result, task);
    }

    void statLog(CrawlerTaskResult result, Task task) {
        StatisticsLogger.log(LogFormat.STAT_CRAWLER_RESULT, "crawlerResult", getSeedInfoType(result).getType(),
                result.getUrl(), getHttpStatus(result), result.getStatus().name(), getFromURL(result),
                TaskUtils.getFirstPageURL(task), TaskUtils.getPageIndex(task));
    }

    void statOtherLog(CrawlerTaskResult result, Boolean exist, Task task) {
        StatisticsLogger.log(LogFormat.STAT_CRAWLER_DUPLICATE, "crawlerResult", getSeedInfoType(result).getType(),
                result.getUrl(), getHttpStatus(result), result.getStatus().name(), getFromURL(result),
                exist.toString(), TaskUtils.getFirstPageURL(task), TaskUtils.getPageIndex(task));
    }

    private static SeedInfoType getSeedInfoType(CrawlerTaskResult result) {
        Map<String, String> ctxMap = result.getCtxMap();
        if (ctxMap != null && ctxMap.containsKey(SchedulerConstants.SEED_INFO_TYPE)) {
            return SeedInfoType.getSeedType(ctxMap.get(SchedulerConstants.SEED_INFO_TYPE));
        }
        return SeedInfoType.DETAIL;
    }

    private static String getHttpStatus(CrawlerTaskResult result) {
        Map<String, String> ctxMap = result.getCtxMap();
        if (ctxMap != null && ctxMap.containsKey(SchedulerConstants.HTTP_STATUS)) {
            return ctxMap.get(SchedulerConstants.HTTP_STATUS);
        }
        return "null";
    }

    private static String getFromURL(CrawlerTaskResult result) {
        Map<String, String> ctxMap = result.getCtxMap();
        if (ctxMap != null && ctxMap.containsKey(SchedulerConstants.FROM_URL)) {
            return ctxMap.get(SchedulerConstants.FROM_URL);
        }
        return "null";
    }



}

