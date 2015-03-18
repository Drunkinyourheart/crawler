package com.yeepay.bigdata.crawler.manager.scheduler;

import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTask;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskAssignService;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResponse;
import com.yeepay.bigdata.crawler.manager.dao.NewsContentDao;
import com.yeepay.bigdata.crawler.manager.handler.CrawlerTaskResultHandler;
import com.yeepay.bigdata.crawler.manager.model.SeedInfo;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.manager.model.TaskWrapper;
import com.yeepay.bigdata.crawler.manager.monitor.Dumpable;
import com.yeepay.bigdata.crawler.manager.utils.MultiPageCacheManager;
import com.yeepay.bigdata.crawler.manager.worker.*;
import com.yeepay.bigdata.crawler.schedule.thrift.Response;
import com.yeepay.bigdata.crawler.schedule.thrift.ResponseStatus;
import com.yeepay.bigdata.crawler.schedule.thrift.server.CrawlerAssignTaskThriftServer;
import com.yeepay.bigdata.crawler.schedule.thrift.server.CrawlerTaskResultPushThriftServer;
import com.yeepay.bigdata.crawler.schedule.thrift.server.Server;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class SeedProcessEngine implements Dumpable {

    private static final Logger logger = Logger.getLogger(SeedProcessEngine.class);

    // 待爬取种子
    private BlockingQueue<SeedInfo> seedInfos;

    // 初始任务
    private BlockingQueue<Task> initTaskQueue;

    // 爬取中的任务
    private ConcurrentMap<String, TaskWrapper> crawlingTaskMap;

    // 超时任务跟踪
    private BlockingQueue<TaskWrapper> timeoutTaskQueue;

    // 已爬取任务
    private BlockingQueue<Task> crawledTaskQueue;

    // extractor 后的任务
    private BlockingQueue<SeedInfo> resultSeedInfoQueue;

    private ConcurrentLinkedQueue<SeedInfo> seedPageInfoQueue;

    private ExecutorService executorService;

    // 接收 爬取结果
    private CrawlerTaskResultPushThriftServer server;

    private boolean firstStart = true;

    private volatile boolean running = false;

    private List<TaskBuilderWorker> taskBuilderWorkers;

    private List<CrawlerTaskAssignWorker> crawlerTaskAssignWorkers;

    private List<TimeoutWorker> timeoutWorkers;

    private List<ExtractorWorker> extractorWorkers;

    private List<URLFilterWorker> urlFilerWorkers;

    private List<ScheduleWorker> scheduleWorkers;

    private SeedProcessEngineConfig config;

    private ScheduledExecutorService scheduledExecutorService;

    private WorkerMonitor workerMonitor;

    private ApplicationContext context;

    private static int crawledQueueSize = 0;

    /**
     * Constructure
     */
    public SeedProcessEngine(SeedProcessEngineConfig config) throws TTransportException {

        this.config = config;

        initQueues();
        initWorkers();

        executorService = Executors.newCachedThreadPool();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);

        System.out.println("---------------------- start --------------------------");
//        System.out.println("---------------------- : " + config.getPageSelector());
        System.out.println(config.getServerPort());
        this.server = new CrawlerTaskResultPushThriftServer(config.getServerPort(),
                new CrawlerTaskResultHandler(crawlingTaskMap,
                        crawledTaskQueue,
                        timeoutTaskQueue,
                        config.getPageSelector()));

//        System.out.println("---------------------- start --------------------------");

        this.context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");

    }

    public synchronized void start() throws IOException {

        if (firstStart) {
            running = true;
            server.start();
            if (workerMonitor == null) {
                // 启动worker监控任务
                workerMonitor = new WorkerMonitor();
                scheduledExecutorService.scheduleWithFixedDelay(workerMonitor, 0,
                        config.getTimeBetweenEvictionRunsMills(),
                        TimeUnit.MICROSECONDS);
            }
            firstStart = false;
        }
    }

    public void submitSeed(SeedInfo seedInfo) throws InterruptedException {
        if (!running) {
            logger.error("SeedProcessEngine is not running....");
            throw new RuntimeException("SeedProcessEngine is not running....");
        }
        seedInfos.put(seedInfo);
    }

    public synchronized void stop() {
        if (!firstStart) {
            if (running) {
                executorService.shutdown();
                try {
                    executorService.awaitTermination(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server.stop();
                scheduledExecutorService.shutdown();
                running = false;
            }
        }
    }

    @Override
    public void dump() {

    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        out.append("seed process engine works : \n");
        // worker info
        out.append("taskBuilderWorkers : " + taskBuilderWorkers.size() + "\n");
        out.append("crawlerTaskAssignWorkers : " + crawlerTaskAssignWorkers.size() + "\n");
        out.append("extractorWorkers : " + extractorWorkers.size() + "\n");
        out.append("urlFilerWorkers : " + urlFilerWorkers.size() + "\n");
        out.append("timeoutWorkers : " + timeoutWorkers.size() + "\n");
        out.append("scheduleWorkers : " + scheduleWorkers.size() + "\n");

        out.append(indent);

        // queue info
        out.append("task queue info: \n ");
        out.append("init seedInfos size : " + seedInfos.size() + "\n");
        out.append("initTaskQueue size : " + initTaskQueue.size() + "\n");
        out.append("timeoutTaskQueue size : " + timeoutTaskQueue.size() + "\n");
        out.append("crawledTaskQueue size : " + crawledTaskQueue.size() + "\n");
        out.append("resultSeedInfoQueue size : " + resultSeedInfoQueue.size() + "\n");
        out.append("seedPageInfoQueue size : " + seedPageInfoQueue.size() + "\n");
        out.append("memMutipagecache size :" + MultiPageCacheManager.getMemCache().size() + "\n");
        out.append("memMutipageLock size :" + MultiPageCacheManager.getMultiPageLock().size() + "\n");
        out.append(indent);
        setCrawledQueueSize(crawledTaskQueue.size());

    }

    public static int getCrawledQueueSize() {
        return crawledQueueSize;
    }

    public static void setCrawledQueueSize(int crawledQueueSize) {
        SeedProcessEngine.crawledQueueSize = crawledQueueSize;
    }

    public SeedProcessEngine() throws TTransportException {
        this(new SeedProcessEngineConfig());
    }

    private class WorkerMonitor implements Runnable {

        @Override
        public void run() {
            // expunge
            expungeWorker(taskBuilderWorkers);
            expungeWorker(crawlerTaskAssignWorkers);
            expungeWorker(extractorWorkers);
            expungeWorker(urlFilerWorkers);
            expungeWorker(timeoutWorkers);
            expungeWorker(scheduleWorkers);

            startWorker();
        }
    }

    private void expungeWorker(List<? extends Alive> workers) {
        List<Alive> deadWorkers = statsDeadWorker(workers);
        workers.removeAll(deadWorkers);
    }

    private List<Alive> statsDeadWorker(List<? extends Alive> workers) {
        List<Alive> deadWorkers = new ArrayList<Alive>();
        for (Alive alive : workers) {
            if (!alive.isAlive()) {
                deadWorkers.add(alive);
            }
        }
        return deadWorkers;
    }

    private void startWorker() {
        if (!running) {
            return;
        }
        if (taskBuilderWorkers.size() < config.getTaskBuilderWorkers()) {
            int num = config.getTaskBuilderWorkers() - taskBuilderWorkers.size();
            for (int i = 0; i < num; i++) {
                TaskBuilderWorker worker = createTaskBuilderWorker();
                taskBuilderWorkers.add(worker);
                executorService.submit(worker);
            }
        }

        if (crawlerTaskAssignWorkers.size() < config.getCrawlingWorkers()) {
            int num = config.getCrawlingWorkers() - crawlerTaskAssignWorkers.size();
            for (int i = 0; i < num; i++) {
                CrawlerTaskAssignWorker worker = createCrawlerTaskAssignWorker();
                crawlerTaskAssignWorkers.add(worker);
                executorService.submit(worker);
            }
        }

        if (extractorWorkers.size() < config.getExtractorWorkers()) {
            int num = config.getExtractorWorkers() - extractorWorkers.size();
            for (int i = 0; i < num; i++) {
                ExtractorWorker worker = createExtractorWorker();
                extractorWorkers.add(worker);
                executorService.submit(worker);
            }
        }

        if (this.urlFilerWorkers.size() < this.config.getURLFilterWorkers()) {
            int num = this.config.getURLFilterWorkers() - this.urlFilerWorkers.size();
            for (int i = 0; i < num; i++) {
                URLFilterWorker worker = createURLFilterWorker();
                this.urlFilerWorkers.add(worker);
                this.executorService.submit(worker);
            }
        }

        if (timeoutWorkers.size() < config.getTimeoutWorkers()) {
            int num = config.getTimeoutWorkers() - timeoutWorkers.size();
            for (int i = 0; i < num; i++) {
                TimeoutWorker worker = createTimeoutWorker();
                timeoutWorkers.add(worker);
                executorService.submit(worker);
            }
        }

        if (scheduleWorkers.size() < 1) {
            for (int i = 0; i < 1; i++) {
                ScheduleWorker worker = createScheduleWorker();
                scheduleWorkers.add(worker);
                executorService.submit(worker);
            }
        }
    }

    private TaskBuilderWorker createTaskBuilderWorker() {
        return new TaskBuilderWorker(seedInfos, initTaskQueue);
    }

    private CrawlerTaskAssignWorker createCrawlerTaskAssignWorker() {
        return new CrawlerTaskAssignWorker(initTaskQueue, crawlingTaskMap, timeoutTaskQueue,
                config.getCrawlerSelector());
    }

    private ExtractorWorker createExtractorWorker() {
        return new ExtractorWorker(crawledTaskQueue, this.resultSeedInfoQueue, config.getExtractorSelector(),
                (NewsContentDao) this.context.getBean("newsContentDao"));
    }

    private URLFilterWorker createURLFilterWorker() {
        return new URLFilterWorker(this.resultSeedInfoQueue, this.seedPageInfoQueue);
    }

    private TimeoutWorker createTimeoutWorker() {
        return new TimeoutWorker(timeoutTaskQueue, initTaskQueue, crawlingTaskMap);
    }

    private ScheduleWorker createScheduleWorker() {
        return new ScheduleWorker(this.seedPageInfoQueue, this);
    }

    private void initQueues() {
        /** 种子队列 原有种子是没有任何特征的 */
        this.seedInfos = new LinkedBlockingQueue<SeedInfo>();
        /** TaskWorker 线程处理后，seed -> task（有状态的） */
        this.initTaskQueue = new LinkedBlockingQueue<Task>();
        /** assignTask 调用 */
        this.crawlingTaskMap = new ConcurrentHashMap<String, TaskWrapper>();

        this.resultSeedInfoQueue = new LinkedBlockingQueue<SeedInfo>();

        this.seedPageInfoQueue = new ConcurrentLinkedQueue<SeedInfo>();
        /** relay queue */
        this.timeoutTaskQueue = new DelayQueue<TaskWrapper>();

//        this.crawledTaskQueue = new LinkedBlockingQueue<Task>();
//        this.crawledTaskQueue =  new LinkedBlockingQueue<Task>(40000);//限制抓取队列
        this.crawledTaskQueue = new LinkedBlockingQueue<Task>(40);//限制抓取队列

    }

    private void initWorkers() {
        // 线程池
        taskBuilderWorkers = new ArrayList<TaskBuilderWorker>();
        crawlerTaskAssignWorkers = new ArrayList<CrawlerTaskAssignWorker>();
        extractorWorkers = new ArrayList<ExtractorWorker>();
        this.urlFilerWorkers = new ArrayList<URLFilterWorker>();
        timeoutWorkers = new ArrayList<TimeoutWorker>();
        this.scheduleWorkers = new ArrayList<ScheduleWorker>();

    }
}
