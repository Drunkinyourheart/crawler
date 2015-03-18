package com.yeepay.bigdata.crawler.crawl.crawler;

import com.yeepay.bigdata.crawler.crawl.fetcher.FetcherCollection;
import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;
import com.yeepay.bigdata.crawler.crawl.monitor.Dumpable;
import com.yeepay.bigdata.crawler.crawl.monitor.LifeCycle;
import com.yeepay.bigdata.crawler.crawl.utils.UpdatePropertiesFile;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 抓取类负责抓取线程启动，fetcher设定
 *
 *  主要负责维护 queue
 *
 */
public class Crawler implements Dumpable, LifeCycle {

    private static final Logger LOGGER = Logger.getLogger(Crawler.class);
    private static final int DEFAULT_THREAD_NUMBER = 2;
    private final BlockingQueue<CrawlURL> blockingQueue;
    private final FetcherCollection fetcherCollection;
    private ExecutorService executorService;
    private volatile int phase = LifeNode.STOPED;
    private AtomicInteger threadIndex = new AtomicInteger(0);
    private int threadNum = DEFAULT_THREAD_NUMBER;

    /**
     * -----------------------------  Constructur ---------------------------------------------------
     */
    public Crawler(int threadNum) {
        this(new LinkedBlockingQueue<CrawlURL>(Integer.MAX_VALUE), threadNum, new FetcherCollection());
    }

    public Crawler(int queueSize, int threadNum) {
        this(new LinkedBlockingQueue<CrawlURL>(queueSize), threadNum, new FetcherCollection());
    }

    public Crawler(BlockingQueue<CrawlURL> blockingQueue, int threadNum) {
        this(blockingQueue, threadNum, new FetcherCollection());
    }

    public Crawler(BlockingQueue<CrawlURL> blockingQueue, int threadNum, FetcherCollection fetcherCollection) {
        this.blockingQueue = blockingQueue;
        this.fetcherCollection = fetcherCollection;
        this.threadNum = threadNum;
    }

    public synchronized void start() {
        if (phase != LifeNode.STOPED) {
            throw new RuntimeException("crawler already started");
        }
        setPhase(LifeNode.STARTING);
        int threadNum = this.threadNum;
        executorService = Executors.newFixedThreadPool(threadNum, new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "crawl-Thread#" + threadIndex.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        });
        for (int i = 0; i < threadNum; i++) {
            executorService.submit(innerProcessor);
        }
        setPhase(LifeNode.STARTED);
    }

    /**
     * 添加爬取链接URL
     */
    public boolean addURL(CrawlURL crawlURL) {
        if (isStoped()) {
            return false;
        }
        if (crawlURL != null && crawlURL.getTimestamp() <= 0) {
            crawlURL.setTimestamp(System.currentTimeMillis());
            LOGGER.info("热闻的初始抓取时间为：" + crawlURL.getTimestamp() + " url=" + crawlURL.getUrl());
//            crawlURL.setSeedType(SeedTypeEnum.HOTNEWSDETAIL.name());//热闻
        }
        if (UpdatePropertiesFile.isBlackUrl(crawlURL.getUrl())) {
            LOGGER.info("this url is in blackList. url=" + crawlURL.getUrl());
            return false;
        }
        boolean result = blockingQueue.offer(crawlURL);
        LOGGER.info("add crawlurl-" + crawlURL.getUrl());
        if (result) {
            crawlURL.setAccessQueueTs(System.currentTimeMillis());
        }
        return result;
    }

    /**
     *  相当于处理器
     */
    private Runnable innerProcessor = new Runnable() {
        @Override
        public void run() {
            try {
                CrawlURL crawlURL = blockingQueue.poll();
                while (isRunning()) {
                    while (crawlURL != null && isRunning()) {
                        LOGGER.info("crawlURL fetching is started " + crawlURL);
                        try {
                            fetcherCollection.fetch(crawlURL);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        crawlURL = blockingQueue.poll();
                    }
                    while (isRunning() && crawlURL == null) {
                        crawlURL = blockingQueue.take();
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
    };

    public synchronized void stop() {
        if (executorService != null) {
            executorService.shutdown();
        }
        try {
            fetcherCollection.destroy();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        setPhase(LifeNode.STOPED);
        long randomKey = new Random(100000000).nextLong();
        LOGGER.info("crawl stoped at time: " + System.currentTimeMillis() + " token is " + randomKey);
        CrawlURL crawlURL = null;
        //以后用scribe 代替
        while ((crawlURL = blockingQueue.poll()) != null) {
            LOGGER.info("token:" + randomKey + ", unfetched url is " + crawlURL.getId());
        }
    }

    @Override
    public String dump() {
        return null;
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException, InterruptedException {
        out.append(String.format("%-50s", ("URLS to crawl count : ")) + "[" + blockingQueue.size() + "]").append("    " + System.getProperty("line.separator"));
        fetcherCollection.dump(out, indent);
    }

    public BlockingQueue<CrawlURL> getBlockingQueue() {
        return blockingQueue;
    }

    private void setPhase(int phaseValue) {
        phase = phaseValue;
    }

    public boolean isStoped() {
        return phase == LifeNode.STOPED;
    }

    public boolean isRunning() {
        final int _phase = phase;
        return _phase == LifeNode.STARTING || _phase == LifeNode.STARTED;
    }

    public void join() throws InterruptedException {
        if (executorService != null) {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
    }

    private static final class LifeNode {
        static final int STARTING = 1;
        static final int STARTED = 2;
        static final int STOPED = 0;
    }

    @Override
    public String toString() {
        return "Crawler [blockingQueue=" + blockingQueue + ", fetcherCollection="
                + fetcherCollection + ", phase=" + phase
                + ", threadNum=" + threadNum + "]";
    }

}
