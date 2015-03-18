package com.yeepay.bigdata.crawler.manager.model;

import java.util.Date;

public class NewsIndex {

    private String id;
    private String url;
    private String docId;
    private Date startTime;
    private Date crawleTime;
    private Date extractTime;
    private Date createTime;

    public NewsIndex(String id, String url, String docId, Date startTime, Date crawleTime,
                     Date extractTime) {
        super();
        this.id = id;
        this.url = url;
        this.docId = docId;
        this.startTime = startTime;
        this.crawleTime = crawleTime;
        this.extractTime = extractTime;
    }

    public NewsIndex() {
        super();
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

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getCrawleTime() {
        return crawleTime;
    }

    public void setCrawleTime(Date crawleTime) {
        this.crawleTime = crawleTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getExtractTime() {
        return extractTime;
    }

    public void setExtractTime(Date extractTime) {
        this.extractTime = extractTime;
    }
}
