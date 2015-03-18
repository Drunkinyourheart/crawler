package com.yeepay.bigdata.crawler.manager.worker;

import com.yeepay.bigdata.crawler.manager.constants.SchedulerConstants;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.manager.model.TaskStatus;
import com.yeepay.bigdata.crawler.manager.model.TaskWrapper;
import com.yeepay.bigdata.crawler.manager.scheduler.SeedProcessEngine;
import com.yeepay.bigdata.crawler.manager.utils.LogFormat;
import com.yeepay.bigdata.crawler.manager.utils.StatisticsLogger;
import com.yeepay.bigdata.crawler.manager.utils.TaskUtils;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class TimeoutWorker implements Runnable, Alive {

    private static final Logger LOGGER = Logger.getLogger(TimeoutWorker.class);

    private BlockingQueue<TaskWrapper> inputQueue;

    private ConcurrentMap<String, TaskWrapper> crawlingTaskMap;

    private BlockingQueue<Task> outputQueue;

    private boolean alive;

    /**
     * Constructor
     *
     * @param inputQueue         timeoutQueue
     * @param outputQueue        initQueue
     * @param crawlingTaskMap
     */
    public TimeoutWorker(BlockingQueue<TaskWrapper> inputQueue,
                         BlockingQueue<Task> outputQueue,
                         ConcurrentMap<String, TaskWrapper> crawlingTaskMap) {
        super();
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.crawlingTaskMap = crawlingTaskMap;
        this.alive = true;
    }

    @Override
    public void run() {

        while (alive) {
            TaskWrapper taskWrapper = null;
            try {
                taskWrapper = inputQueue.take();

                Task task = taskWrapper.getTask();
                //
                TaskWrapper taskWrapperFromMap = crawlingTaskMap.remove(task.getId());
                // task is processed already. or
                if (taskWrapperFromMap == null) {
                    continue;
                }

                if (taskWrapperFromMap == taskWrapper) {

                    if (task.getRetryTimes() >= SchedulerConstants.RETRY_TIMES) {

                        LOGGER.info(task.getId() + " : " + task.getUrl() + " : " + task.getRetryTimes());

                        // log timeout task
                        StatisticsLogger.log(
                                LogFormat.STAT_CRAWLER_RESULT,
                                "crawlerResult",
                                task.getSeedType().getType(),
                                task.getUrl(),
                                "null",
                                "timeout",
                                task.getFromURL(),
                                TaskUtils.getFirstPageURL(task),
                                TaskUtils.getPageIndex(task));
                        continue;
                    }

                    if (SeedProcessEngine.getCrawledQueueSize() >= SchedulerConstants.SIGINAL_THRESHOLD_INITTASKQUEUE) {

                        LOGGER.info(task.getId() + " : " + task.getUrl() + " : " + task.getRetryTimes() + ":" + outputQueue.size());

                        // log scheduleOverLoading signal
                        StatisticsLogger.log(
                                LogFormat.STAT_CRAWLER_RESULT,
                                "crawlerResult",
                                task.getSeedType().getType(),
                                task.getUrl(),
                                "null",
                                "scheduleOverloading",
                                task.getFromURL(),
                                TaskUtils.getFirstPageURL(task),
                                TaskUtils.getPageIndex(task));
                        continue;
                    }  // if
                    task.setStatus(TaskStatus.INIT);
                    task.setRetryTimes(task.getRetryTimes() + 1);
                    outputQueue.put(task);
                }
            } catch (Throwable e) {

                LOGGER.error(e.getMessage(), e);

                if (taskWrapper != null) {
                    try {
                        inputQueue.put(taskWrapper);
                    } catch (InterruptedException e1) {
                        LOGGER.error("timeout worker put back is interrupted..", e);
                        alive = false;
                    }
                }
                if (e instanceof InterruptedException) {
                    alive = false;
                }
            }  // catch
        }  // while

    }  // run


    @Override
    public boolean isAlive() {
        return alive;
    }


}
