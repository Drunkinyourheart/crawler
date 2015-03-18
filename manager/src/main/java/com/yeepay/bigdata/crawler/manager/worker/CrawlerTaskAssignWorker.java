package com.yeepay.bigdata.crawler.manager.worker;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTask;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResponse;
import com.yeepay.bigdata.crawler.manager.constants.SchedulerConstants;
import com.yeepay.bigdata.crawler.manager.heartbeat.ClientNodeSelector;
import com.yeepay.bigdata.crawler.manager.heartbeat.CrawlerAssignClientNode;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.manager.model.TaskStatus;
import com.yeepay.bigdata.crawler.manager.model.TaskWrapper;
import com.yeepay.bigdata.crawler.manager.utils.LogFormat;
import com.yeepay.bigdata.crawler.manager.utils.StatisticsLogger;
import com.yeepay.bigdata.crawler.manager.utils.TaskUtils;
import com.yeepay.bigdata.crawler.schedule.thrift.ResponseStatus;
import com.yeepay.bigdata.crawler.schedule.thrift.client.CrawlerAssignTaskClient;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 *       作为一个独立的线程对外提供服务。
 */
public class CrawlerTaskAssignWorker implements Runnable, Alive {

    private static final Logger                                                  LOGGER    = Logger.getLogger(CrawlerTaskAssignWorker.class);

    private BlockingQueue<Task>                                                  inputQueue;

    private ConcurrentMap<String, TaskWrapper>                                   outputMap;

    private BlockingQueue<TaskWrapper>                                           timeOutQueue;

    private ClientNodeSelector<CrawlerAssignClientNode, CrawlerAssignTaskClient> selector;

    private long                                                                 delayTime = 4 * 60 * 1000;    // 4m

    private long                                                                 stepTime  = 20 * 1000;        // 20s

    private boolean                                                              isAlive   = true;

    /** Contructure 1 */
    public CrawlerTaskAssignWorker(BlockingQueue<Task> inputQueue,
                                   ConcurrentMap<String, TaskWrapper> outputMap,
                                   BlockingQueue<TaskWrapper> timeOutQueue,
                                   ClientNodeSelector<CrawlerAssignClientNode, CrawlerAssignTaskClient> selector){
        this.inputQueue = inputQueue;
        this.outputMap = outputMap;
        this.timeOutQueue = timeOutQueue;
        this.selector = selector;
    }

    @Override
    public void run() {
        while (isAlive) {
            Task task = null;
            try {
                task = inputQueue.take();
                task.setCrawleTime(Calendar.getInstance().getTime());
                CrawlerAssignClientNode clientNode = selector.selectClientNode(task.getDomain());

                /** create Task */
                CrawlerTask crawlerTask = createCrawlerTask(task);
                /** invoke crawler */
                CrawlerTaskResponse response = clientNode.getClient().assignCrawlerTask(crawlerTask);

                /** process response */
                if (response.getResponse().getStatus() == ResponseStatus.Success) {

                    task.setStatus(TaskStatus.CRAWLING);

                    TaskWrapper taskWrapper = new TaskWrapper(delayTime + task.getRetryTimes() * stepTime, TimeUnit.MILLISECONDS, task);

                    timeOutQueue.put(taskWrapper);
                    outputMap.put(task.getId(), taskWrapper);
                    statLog(task, response);
                } else {
                    // retry invoke
                    LOGGER.warn(String.format("Assign Crawler Failure: taskID : %s ; URL: %s Type: %s;  msg : %s",
                                              task.getId(), task.getUrl(), task.getSeedType().name(),
                                              response.getResponse().getMsg()));
                    // inputQueue.put(task);
                    statLog(task, response);
                }
            } catch (Throwable e) {
                LOGGER.error("invoke error: "+e.getMessage(), e);
                if (e instanceof InterruptedException) {
                    isAlive = false;
                } else {
                    if (task != null) {
                        try {
                            inputQueue.put(task);
                        } catch (InterruptedException e1) {
                            LOGGER.error("invoke crawler error and putting to inputQueue is  interrupted : ", e);
                            isAlive = false;
                        }
                    }
                }
            }
        }
    }

    private void statLog(Task task, CrawlerTaskResponse response) {
        StatisticsLogger.log(LogFormat.STAT_ASSIGN_CRAWLER, "assignTask", task.getSeedType().getType(), task.getUrl(),
                response.getResponse().getStatus().name(), TaskUtils.getFirstPageURL(task),
                TaskUtils.getPageIndex(task));
    }

    private CrawlerTask createCrawlerTask(Task task) {
        Map<String, String> ctxMap = new HashMap<String, String>();
        ctxMap.put(SchedulerConstants.SEED_INFO_TYPE, task.getSeedType().getType());
        ctxMap.put(SchedulerConstants.FROM_URL, task.getFromURL() == null ? "" : task.getFromURL());
        ctxMap.put(SchedulerConstants.START_CRAWLER_TS, task.getStartCrawlerTs() + "");
        ctxMap.putAll(task.getCtxMap());
//        ctxMap.putAll(task.getCtxMap());



//        System.out.println("------------------------------------------------------------------------------");
//        for (String key : task.getCtxMap().keySet()) {
//        }
//        System.out.println(task.getCtxMap());
//        System.out.println("------------------------------------------------------------------------------");

        CrawlerTask crawlerTask = new CrawlerTask(task.getId(), task.getUrl(), task.isDynamic());
        crawlerTask.setCtxMap(ctxMap);
        crawlerTask.setCtxMapIsSet(true);

        return crawlerTask;

//        CrawlerTask crawlerTask1 =  new CrawlerTask("1", "http://www.huxiu.com/article/100861/1.html", false);
//        System.out.println("111 : " + crawlerTask1);
//        return crawlerTask1;
//        return new CrawlerTask("1", "http://www.huxiu.com/article/100861/1.html", false);
//        return new CrawlerTask(task.getId(), task.getUrl(), task.isDynamic()).setCtxMap(ctxMap);
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

}
