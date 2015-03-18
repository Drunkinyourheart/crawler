package com.yeepay.bigdata.crawler.manager.model;

import java.util.Date;

public class NewsContent {
    private String id;
    private String content;
    private Date createTime;

    public NewsContent(String id, String content) {
        super();
        this.id = id;
        this.content = content;
    }

    public NewsContent() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
