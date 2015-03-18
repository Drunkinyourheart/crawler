package com.yeepay.bigdata.crawler.manager.handler;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.manager.constants.SchedulerConstants;
import com.yeepay.bigdata.crawler.manager.model.SeedInfoType;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.manager.model.TaskStatus;
import com.yeepay.bigdata.crawler.manager.utils.DomainUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class HotNewsProcessor extends CrawlerTaskResultProcessor {

    private static Logger LOGGER = Logger.getLogger(HotNewsProcessor.class);
    private BlockingQueue<Task> outputQueue;

    public HotNewsProcessor(BlockingQueue<Task> outputQueue) {
        this.outputQueue = outputQueue;
    }

        @Override
        public boolean isNeedPrerequisite() {
            return false;
        }

        @Override
        public void processSucceedResult(CrawlerTaskResult result, Task task) {
            try {
                task = new Task();
                task.setId(result.getId());
                task.setUrl(result.getUrl());
                task.setCrawleData(result.getData());
                task.setDomain(DomainUtils.getTopPrivateDomain(result.getUrl()));
                task.setStatus(TaskStatus.CRAWLED);
                task.setSeedType(SeedInfoType.DETAIL);//此处热闻转化成普通的DETAIL
                task.setFromURL(result.getUrl());
                task.setCtxMap(result.getCtxMap());

                //下面两个属性比较特殊
                task.setFirstPageId(DigestUtils.md5Hex(result.getUrl()));
                task.setFirstPageURL(result.getUrl());//此处热闻转化成普通的DETAIL,第一次转换时，firstPageUrl就是url
                /** 暂不考虑 */

//                Boolean exist = PageClientUtils.filterURL(task.getUrl(), task);
                        Boolean exist = false;
                if (!exist) {
                    task.setCrawleData(result.getData());
                    task.setStatus(TaskStatus.CRAWLED);
                    long startCrawlerTs = getSeedStartCrawlerTsFromCrawlerTs(task);
                    Map<String, String> ctxMap = result.getCtxMap();
                    if (ctxMap != null) {
                        ctxMap.put(SchedulerConstants.SEED_INFO_TYPE, SeedInfoType.DETAIL.name());
                        ctxMap.put(SchedulerConstants.FROM_URL, result.getUrl());
                    }
                    task.setCtxMap(ctxMap);
                    if (task.getCtxMap() != null) {
                        try {
                            task.getCtxMap().put(SchedulerConstants.START_CRAWLER_TS, startCrawlerTs + "");
                        } catch (Exception e) {
                            LOGGER.error("get startCrawlerTs exeception : ", e);
                        }
                    }
                    /** --- 待优化 */
//                    PageClientUtils.savePage(task);

                    task.setCrawleData("");
                    outputQueue.put(task);
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

