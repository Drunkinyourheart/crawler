package com.yeepay.bigdata.crawler.manager.handler;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultStatus;
import com.yeepay.bigdata.crawler.manager.constants.SchedulerConstants;
import com.yeepay.bigdata.crawler.manager.model.SeedInfoType;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.manager.model.TaskWrapper;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 内部处理类
 */
public class CrawlerTaskResultFlow {

    private ConcurrentMap<String, TaskWrapper> taskMap;

    private BlockingQueue<TaskWrapper> timeoutQueue;


    private ConcurrentMap<SeedInfoType, CrawlerTaskResultProcessor> processors = new ConcurrentHashMap<SeedInfoType, CrawlerTaskResultProcessor>(SeedInfoType.values().length);

    public void addProcessor(SeedInfoType type, CrawlerTaskResultProcessor processor) {
        processors.putIfAbsent(type, processor);
    }

    /**
     * Constructure
     */
    public CrawlerTaskResultFlow(BlockingQueue<TaskWrapper> timeoutQueue, ConcurrentMap<String, TaskWrapper> taskMap) {
        this.timeoutQueue = timeoutQueue;
        this.taskMap = taskMap;
    }

    /**
     * 对外提供服务
     */
    public void process(CrawlerTaskResult result) {

        CrawlerTaskResultStatus status = result.getStatus();
        /** retrieve processor */
        CrawlerTaskResultProcessor processor = processors.get(getSeedInfoType(result));

        Task task = null;
        if (processor.isNeedPrerequisite()) {
            task = prerequisite(result);
            // prerequisite task must be not null
            if (task == null) {
                return;
            }
        }

        if (status.equals(CrawlerTaskResultStatus.Succeed)) {
            processor.processSucceedResult(result, task);
        } else if (status.equals(CrawlerTaskResultStatus.Failure)) {
            processor.processFailureResult(result, task);
        } else if (status.equals(CrawlerTaskResultStatus.Error)) {
            processor.processErrorResult(result, task);
        }
    }

    /**
     * remove task from timeout queue and return
     */
    protected Task prerequisite(CrawlerTaskResult result) {
        String taskId = result.getId();
        TaskWrapper taskWrapper = taskMap.remove(taskId);
        Task task = null;
        if (taskWrapper != null) {
            // remove task from timeout queue
            timeoutQueue.remove(taskWrapper);
            task = taskWrapper.getTask();
        }
        return task;
    }

    private static SeedInfoType getSeedInfoType(CrawlerTaskResult result) {
        Map<String, String> ctxMap = result.getCtxMap();
        if (ctxMap != null && ctxMap.containsKey(SchedulerConstants.SEED_INFO_TYPE)) {
            return SeedInfoType.getSeedType(ctxMap.get(SchedulerConstants.SEED_INFO_TYPE));
        }
        return SeedInfoType.DETAIL;
    }

}
