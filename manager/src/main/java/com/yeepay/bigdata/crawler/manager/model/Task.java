package com.yeepay.bigdata.crawler.manager.model;


import com.yeepay.bigdata.crawler.manager.utils.MultiPageLock;

import java.util.Date;
import java.util.Map;

public class Task {

    private String              id;

    private String              url;

    private String              title;

    private TaskStatus          status;

    private String              domain;

    private String              crawleData;

    private String              extractData;

    private Map<String, String> ctxMap    = null;

    private int                 retryTimes;

    private boolean             isDynamic;

    private SeedInfoType        seedType;

    private Date                crawleTime;

    private Date                extractTime;

    private Date                startTime;

    private String              fromURL;

    private int                 pageIndex = 1;

    private String              firstPageId;

    private String              firstPageURL;

    private MultiPageLock.LockEntry lock;

    private long startCrawlerTs; //设置抓取时间，来方便统计每个种子从抓取到入库耗时

    public long getStartCrawlerTs() {
        return startCrawlerTs;
    }

    public void setStartCrawlerTs(long startCrawlerTs) {
        this.startCrawlerTs = startCrawlerTs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCrawleData() {
        return crawleData;
    }

    public void setCrawleData(String crawleData) {
        this.crawleData = crawleData;
    }

    public String getExtractData() {
        return extractData;
    }

    public void setExtractData(String extractData) {
        this.extractData = extractData;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public void setDynamic(boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    public SeedInfoType getSeedType() {
        return seedType;
    }

    public void setSeedType(SeedInfoType seedType) {
        this.seedType = seedType;
    }

    public Map<String, String> getCtxMap() {
        return ctxMap;
    }

    public void setCtxMap(Map<String, String> ctxMap) {
        this.ctxMap = ctxMap;
    }

    public Date getCrawleTime() {
        return crawleTime;
    }

    public void setCrawleTime(Date crawleTime) {
        this.crawleTime = crawleTime;
    }

    public Date getExtractTime() {
        return extractTime;
    }

    public void setExtractTime(Date extractTime) {
        this.extractTime = extractTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFromURL() {
        return fromURL;
    }

    public void setFromURL(String fromURL) {
        this.fromURL = fromURL;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public String getFirstPageId() {
        return firstPageId;
    }

    public void setFirstPageId(String firstPageId) {
        this.firstPageId = firstPageId;
    }

    public String getFirstPageURL() {
        return firstPageURL;
    }

    public void setFirstPageURL(String firstPageURL) {
        this.firstPageURL = firstPageURL;
    }

    public MultiPageLock.LockEntry getLock() {
        return lock;
    }

    public void setLock(MultiPageLock.LockEntry lock) {
        this.lock = lock;
    }

    @Override
    public String toString() {
        return "Task [id=" + id + ", url=" + url + ", title=" + title + ", status=" + status + ", domain=" + domain
               + ", crawleData=" + crawleData + ", extractData=" + extractData + ", ctxMap=" + ctxMap + ", retryTimes="
               + retryTimes + ", isDynamic=" + isDynamic + ", seedType=" + seedType + ", crawleTime=" + crawleTime
               + ", extractTime=" + extractTime + ", startTime=" + startTime + ", fromURL=" + fromURL + ", pageIndex="
               + pageIndex + ", firstPageId=" + firstPageId + ", firstPageURL=" + firstPageURL + ", lock=" + lock + "]";
    }
}
