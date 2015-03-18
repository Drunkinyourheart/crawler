package com.yeepay.bigdata.crawler.manager.worker;

import com.yeepay.bigdata.crawler.manager.model.SeedInfo;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class URLFilterWorker implements Runnable, Alive {

    private static final Logger             logger       = Logger.getLogger(URLFilterWorker.class);
    private BlockingQueue<SeedInfo> inputQueue;
    private ConcurrentLinkedQueue<SeedInfo> outpuQueue;
    private boolean                         isAlive      = true;
    private UrlValidator                    urlValidator = new UrlValidator(new String[] { "http", "https" }, 4L);

    public URLFilterWorker(BlockingQueue<SeedInfo> inputQueue, ConcurrentLinkedQueue<SeedInfo> outpuQueue){
        this.inputQueue = inputQueue;
        this.outpuQueue = outpuQueue;
    }

    public void run() {
        while (this.isAlive) {
            SeedInfo info = inputQueue.poll();
            logger.info("isAlive--"+inputQueue.size()+"--"+this.outpuQueue.size());
            try {
                while (info != null && isAlive) {
                    if (!this.urlValidator.isValid(info.getUrl())) {
                    	info = (SeedInfo) this.inputQueue.poll();
                    	logger.info(String.format("invalid url : %s", new Object[] { info }));
                        continue;
                    } 
                    this.outpuQueue.add(info);
//                    else if (!PageClientUtils.filterURL(info.getUrl())) {
//                    	this.outpuQueue.add(info);
//                        logger.info("output pool "+inputQueue.size()+"--\t"+this.outpuQueue.size());
//                    }else{
//                    	logger.info("otherE"+inputQueue.size()+"--\t"+this.outpuQueue.size());
////                    	 logger.info("elseotherend ");
//                    }
//                   
                    info = (SeedInfo) this.inputQueue.poll();
                }

                while (isAlive && info == null) {
                    info = inputQueue.take();
                    if(info!=null){
                    	this.outpuQueue.add(info);
                    }
                }
            } catch (Throwable e) {
                logger.error("invoke error: ", e);
                if (info != null) {
                    this.inputQueue.add(info);
                }
                if ((e instanceof InterruptedException)) {
                    this.isAlive = false;
                }
            }
        }
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public static void main(String[] args) {
        UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" }, 4L);
        System.out.println(urlValidator.isValid("http://news.zz.soufun.com/2014-04-16/12557698_9.html"));
    }
}
