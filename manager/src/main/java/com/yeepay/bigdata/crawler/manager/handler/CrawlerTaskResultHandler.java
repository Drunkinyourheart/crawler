package com.yeepay.bigdata.crawler.manager.handler;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultPushService;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultResponse;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultStatus;
import com.yeepay.bigdata.crawler.manager.constants.SchedulerConstants;
import com.yeepay.bigdata.crawler.manager.heartbeat.ClientNodeSelector;
import com.yeepay.bigdata.crawler.manager.heartbeat.PageClientNode;
import com.yeepay.bigdata.crawler.manager.model.SeedInfoType;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.manager.model.TaskStatus;
import com.yeepay.bigdata.crawler.manager.model.TaskWrapper;
import com.yeepay.bigdata.crawler.schedule.thrift.Response;
import com.yeepay.bigdata.crawler.schedule.thrift.ResponseStatus;
import com.yeepay.bigdata.crawler.schedule.thrift.client.PageClient;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class CrawlerTaskResultHandler implements CrawlerTaskResultPushService.Iface {

    private static final Logger logger = Logger.getLogger(CrawlerTaskResultHandler.class);

    private ConcurrentMap<String, TaskWrapper> taskMap;

    private BlockingQueue<Task> outputQueue;

    // 超时队列
    private BlockingQueue<TaskWrapper> timeoutQueue;

    private ClientNodeSelector<PageClientNode, PageClient> pageNodeSelector;

    private CrawlerTaskResultFlow flow;

    /**
     * Constructure
     */
    public CrawlerTaskResultHandler(ConcurrentMap<String, TaskWrapper> taskMap,
                                    BlockingQueue<Task> outputQueue,
                                    BlockingQueue<TaskWrapper> timeoutQueue,
                                    ClientNodeSelector<PageClientNode, PageClient> pageNodeSelector) {
        this.taskMap = taskMap;
        this.outputQueue = outputQueue;
        this.timeoutQueue = timeoutQueue;
        this.pageNodeSelector = pageNodeSelector;
//        initFlow();
    }

    /**
     * 对外服务：被crawl调用接口
     *
     * @param result
     * @return
     * @throws TException
     */
    @Override
    public CrawlerTaskResultResponse pushCrawlerTaskResult(CrawlerTaskResult result) throws TException {
        String taskId = result.getId();
//        String taskId = deprecateprocess(result);

        /** ------------------------------------------------------------------------ */
        CrawlerTaskResultStatus status = result.getStatus();

        if (status.equals(CrawlerTaskResultStatus.Succeed)) {
            TaskWrapper taskWrapper = null;
            Task task = null;
            try {
                taskWrapper = taskMap.remove(taskId);
                // task is not timeout.
                if (taskWrapper != null) {
                    logger.info("url request time : " + taskWrapper.getConsumeTime());
                    timeoutQueue.remove(taskWrapper);

                    // 已经爬取数据
                    task = taskWrapper.getTask();
                    task.setCrawleData(result.getData());
                    task.setStatus(TaskStatus.CRAWLED);
                    task.setCtxMap(result.getCtxMap());
                    outputQueue.put(task);
                    // back up crawlerData
                    // ResultUtils.write(task.getDomain());
                }
            } catch (Exception e) {

            }
        }
        /** ------------------------------------------------------------------------ */
/**
        flow.process(result);
        // response message
 */
        CrawlerTaskResultResponse response = new CrawlerTaskResultResponse();
        response.setResponse(new Response(taskId, ResponseStatus.Success));
        return response;
    }

/** ---------------------------------------------------------------------------------------------------------------- */
    @Deprecated
    private String deprecateprocess(CrawlerTaskResult result) {
        String taskId = result.getId();
        CrawlerTaskResultStatus status = result.getStatus();
        if (status.equals(CrawlerTaskResultStatus.Succeed)) {
            TaskWrapper taskWrapper = null;
            Task task = null;
            try {
                taskWrapper = taskMap.remove(taskId);
                // task is not timeout.
                if (taskWrapper != null) {
                    logger.info("url request time : " + taskWrapper.getConsumeTime());
                    timeoutQueue.remove(taskWrapper);

                    // 已经爬取数据
                    task = taskWrapper.getTask();
                    task.setCrawleData(result.getData());
                    task.setStatus(TaskStatus.CRAWLED);
                    task.setCtxMap(result.getCtxMap());
                    outputQueue.put(task);
                    // back up crawlerData
                    // ResultUtils.write(task.getDomain());
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    try {
                        // taskMap is interrupted... retry crawling
                        if (taskWrapper == null) {
                            // doing nothing
                        } else if (task == null) {
                            timeoutQueue.put(taskWrapper);
                        }
                        // outputQueue is interrupted..
                        if (taskWrapper != null && task != null) {
                            task.setStatus(TaskStatus.CRAWLING);
                            outputQueue.put(task);
                        }
                    } catch (InterruptedException e1) {
                        logger.error("crawled result is failed..", e1);
                    }
                }
            }
        } else if (status.equals(CrawlerTaskResultStatus.Failure)) {
            // doing nothing. waiting timeout to retry crawling.
            logger.warn(String.format("Crawle Result Failure : TaskId : %s ; URL : %s ; Msg : %s", result.getId(),
                    result.getUrl(), result.getMsg()));
        } else if (status.equals(CrawlerTaskResultStatus.Error)) {
            TaskWrapper taskWrapper = null;
            try {
                taskWrapper = taskMap.remove(taskId);
                if (taskWrapper != null) {
                    timeoutQueue.remove(taskWrapper);
                }

                // tracker infomation

                logger.warn(String.format("Crawle Result Error : TaskId : %s ; URL : %s ; Type: %s; Msg : %s",
                        result.getId(), result.url,
                        taskWrapper != null ? taskWrapper.getTask().getSeedType().name() : "unknown",
                        result.getMsg()));
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    try {
                        // taskMap is interrupted
                        if (taskWrapper == null) {
                            taskWrapper = taskMap.remove(taskId);
                            timeoutQueue.remove(taskWrapper);
                        } else {
                            // timeoutQueue is interrupted...
                            timeoutQueue.remove(taskWrapper);
                        }
                    } catch (Exception e2) {
                        logger.error(String.format("Crawle Error : TaskId : %s ; URL : %s ; Crawle Error : %s ; Crawle Task will retry.",
                                result.getId(), result.url, result.getMsg()), e2);
                    }
                }
            }
        }
        return taskId;
    }

//    private void initFlow() {
//        flow = new CrawlerTaskResultFlow();
//        CrawlerTaskResultProcessor seedProcessor = new SeedProcessor();
//        CrawlerTaskResultProcessor otherProcessor = new OtherProcessor();
//        CrawlerTaskResultProcessor hotNewsProcessor = new HotNewsProcessor();
//        // 处理list页面 ： 没有跟踪信息
//        flow.addProcessor(SeedInfoType.LIST, seedProcessor);
//        flow.addProcessor(SeedInfoType.RSSLIST, seedProcessor);
//        flow.addProcessor(SeedInfoType.EPAPER, seedProcessor);
//

//        flow.addProcessor(SeedInfoType.EPAPERLAYOUT, otherProcessor);
//        flow.addProcessor(SeedInfoType.DETAIL, otherProcessor);
//        flow.addProcessor(SeedInfoType.HOTNEWSDETAIL, hotNewsProcessor);
//    }
//
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
//