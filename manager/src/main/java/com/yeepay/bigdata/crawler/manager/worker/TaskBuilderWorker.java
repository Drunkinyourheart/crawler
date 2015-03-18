package com.yeepay.bigdata.crawler.manager.worker;

import com.yeepay.bigdata.crawler.manager.model.SeedInfo;
import com.yeepay.bigdata.crawler.manager.model.SeedInfoType;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.manager.model.TaskStatus;
import com.yeepay.bigdata.crawler.manager.utils.DomainUtils;
import com.yeepay.bigdata.crawler.manager.utils.PageClientUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class TaskBuilderWorker implements Runnable, Alive {

    private static final Logger     LOGGER  = Logger.getLogger(TaskBuilderWorker.class);

    private BlockingQueue<SeedInfo> inputQueue;  // 消费队列中的 种子信息

    private BlockingQueue<Task>     outputQueue;  // 种子 -> 任务

    private boolean                 isAlive = true;

    /** Constructure ：
     *
     *      1. inputQueue  --> TaskBuilderWorker  --> outputQueue
     *
     */
    public TaskBuilderWorker(BlockingQueue<SeedInfo> inputQueue, BlockingQueue<Task> outputQueue){
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {

        while (isAlive) {
            SeedInfo info = null;
            try {
                info = inputQueue.take();
//                System.out.println("info : " + info + "\n");
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("seed info : " + info);
                }
                Task task = null ;
                if(info!=null){
                	task = createTask(info);
                }
//                if (info != null && !info.isList() && PageClientUtils.filterURL(info.getUrl(), task)) {
//                	continue;
//                }
                outputQueue.put(task);
            } catch (Throwable e) {
                LOGGER.error("invoke error: "+e.getMessage(), e);
                if (info != null) {
                    try {
                        inputQueue.put(info);
                    } catch (InterruptedException e1) {
                        LOGGER.error("invoke taskbuilder error and putting to inputQueue is  interrupted : ", e);
                        isAlive = false;
                    }
                }
                if (e instanceof InterruptedException) {
                    isAlive = false;
                }
            }
        }
    }

    public boolean isAlive() {
        return isAlive;
    }

    private Task createTask(SeedInfo info) {

        String url = info.getUrl();
        Task task = new Task();
        task.setUrl(url);
        task.setId(DigestUtils.md5Hex(url));
        task.setDomain(DomainUtils.getTopPrivateDomain(url));
        task.setStartTime(Calendar.getInstance().getTime());
        task.setStatus(TaskStatus.INIT);
        task.setSeedType(info.getSeedType());
        task.setFromURL(info.getFromURL());
        task.setStartCrawlerTs(info.getStartCrawlerTs());

        if (info.getSeedType().equals(SeedInfoType.DETAIL)) {
            task.setTitle(info.getTitle());
            task.setPageIndex(info.getPageIndex());
            task.setFirstPageId(info.getFirstPageID());
            task.setFirstPageURL(info.getFirstPageURL());
            task.setLock(info.getLock());
        }

        task.setDynamic(info.isDynamic());
        //添加精细属性信号
        Map<String,String> map = new HashMap<String, String>();
        map.put("publicationName", info.getPublicationName());
        map.put("typeArea", info.getTypeArea());
        map.put("channel", info.getChannel());
        map.put("sub_channel", info.getSub_channel());
        map.put("sourceSeedType", info.getSourceSeedType()+"");
        task.setCtxMap(map);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("create Task : " + task.toString());
        }
        return task;
    }

    public static void main(String[] args) {
		SeedInfo seed = new SeedInfo("url", SeedInfoType.EPAPER, "", false);
		System.out.println(seed.getSeedType());
	}
}
