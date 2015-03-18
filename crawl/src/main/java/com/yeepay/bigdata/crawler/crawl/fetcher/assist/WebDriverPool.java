package com.yeepay.bigdata.crawler.crawl.fetcher.assist;

import org.apache.log4j.Logger;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.openqa.selenium.WebDriver;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 
 * webDriver对象池，webDriver的启动与渲染非常耗时，数量过小无法满足需求
 */
public class WebDriverPool {
    private static final Logger LOGGER = Logger.getLogger(WebDriver.class);

    /**保证实例不会少于线程数量 */
    private final static int DEFAULT_CAPACITY = Runtime.getRuntime().availableProcessors()+5;
    
    private final int capacity;
    private final static int STAT_RUNNING = 1;
    private final static int STAT_CLODED = 2;
    private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);    
    /** 记录池大小 */
    private final AtomicInteger _size = new AtomicInteger();
    private final int browse_threadHold = 30;
    private final Queue<WebDriver> _webDrivers;
    private final ConcurrentHashMap<WebDriver, AtomicInteger> browseCountMap = new ConcurrentHashMap<WebDriver, AtomicInteger>();
    private final WebDriverFactory webDriverFactory;
    
    
   public WebDriverPool(int capacity,WebDriverFactory webDriverFactory) {
        if (capacity<1) {
            throw new IllegalArgumentException("webDriverPool capacity  at lease 1");
        }
        this.capacity = capacity;
        this._webDrivers = new ConcurrentLinkedQueue<WebDriver>();
        this.webDriverFactory = webDriverFactory;
    }

   public WebDriverPool() {
        this(DEFAULT_CAPACITY,FirefoxDriverFactory.getInstance());
    }
    
    
    public WebDriver getDriver() throws Exception  {
        checkRunning();
        AtomicInteger count = null;
        WebDriver webDriver = _webDrivers.poll();
        if (webDriver==null){
            webDriver=webDriverFactory.makeDriver();
            browseCountMap.put(webDriver, new AtomicInteger(0));
        }
        else{
            if ((count=browseCountMap.get(webDriver))!=null) {
                count = new AtomicInteger(0);
                browseCountMap.put(webDriver, count);
            }
            browseCountMap.get(webDriver).incrementAndGet();
            _size.decrementAndGet();
        }
        return webDriver;
    }

    public void returnDriver(WebDriver webDriver,boolean broken)  {
        checkRunning();
        if (broken) {
            try {
                webDriverFactory.destroyDriver(webDriver);
            } catch (Exception e) {
               LOGGER.error(e.getMessage(), e);
            }
            return;
        }
        if (_size.incrementAndGet() > capacity){
            _size.decrementAndGet();
            try {
                webDriverFactory.destroyDriver(webDriver);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        else{
            AtomicInteger count = null;
            if ((count=browseCountMap.get(webDriver))!=null&&count.get()>browse_threadHold) {
                try {
                    webDriverFactory.destroyDriver(webDriver);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            _webDrivers.add(webDriver);
        }
    }

    protected void checkRunning() {
        if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
            throw new IllegalStateException("Already closed!");
        }
    }

    public void closeAll() {
        boolean b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED);
        if (!b) {
            throw new IllegalStateException("Already closed!");
        }
        for (WebDriver webDriver : _webDrivers) {
             try {
                 webDriverFactory.destroyDriver(webDriver);
            } catch (Throwable e) {
                 LOGGER.error(e.getMessage(), e);
            }
        }
    }
    
    
    
    public Integer getSize(){
        return _size.get();
    }
    
    
    protected static abstract class WebDriverFactory{
        abstract WebDriver makeDriver()   throws Exception;
        void destroyDriver(WebDriver webDriver) throws Exception  {}
        boolean validateDriver(WebDriver webDriver){return true;};
    }
}
