package com.yeepay.bigdata.crawler.crawl.fetcher;


import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpClientFetchWrapper implements Fetcher {

    private Fetcher fetcher;
    private int processor_count = Runtime.getRuntime().availableProcessors() + 1;
    // private int processor_count = 40 ;
    private ExecutorService executorService;
    private BlockingQueue<Runnable> blockingQueue;

    public HttpClientFetchWrapper(Fetcher fetcher) {
        this.fetcher = fetcher;
        blockingQueue = new LinkedBlockingQueue<Runnable>(200000);
        this.executorService = new ThreadPoolExecutor(10 * processor_count, 15 * processor_count, 60L,
                TimeUnit.SECONDS, blockingQueue, new ThreadFactory() {

            private final AtomicInteger threadNumber = new AtomicInteger(
                    1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "httpClientFetcher-" + threadNumber.incrementAndGet());
                return thread;
            }
        }, new CallerRunsPolicy());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Boolean fetch(final CrawlURL crawlURL) throws Exception {
        executorService.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                try {
                    fetcher.fetch(crawlURL);
                } catch (Exception e) {
                    return false;
                }
                return true;
            }
        });
        // 一律返回true
        return true;
    }

    @Override
    public void destroy() throws Exception {
        executorService.shutdown();
        fetcher.destroy();
    }

    @Override
    public String dump() {
        return null;
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException, InterruptedException {
//        out.append("httpClientFetcher blocking size is " + blockingQueue.size()).append(" ").append("    "
//                + System.getProperty("line.separator"));
        ;

        out.append(String.format("%-50s", ("httpClientFetcher blocking size is : ")) + "[" + blockingQueue.size() + "]").append("    " + System.getProperty("line.separator"));

        fetcher.dump(out, indent);
    }

}
