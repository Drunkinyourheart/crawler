package com.yeepay.bigdata.crawler.crawl.model;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

/**
 * 对应fetch抓取结果
 */
public class FetchResult {
    
    private HttpEntity entity = null;
    private Header[] responseHeaders = null;
    private FetchStatus statusCode;
    private String msg;
    private CrawlURL crawlURL;
    private int httpstatus;
    
    /**
     * @return the entity
     */
    public HttpEntity getEntity() {
        return entity;
    }
    
    /**
     * @param entity the entity to set
     */
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }
    
    /**
     * @return the responseHeaders
     */
    public Header[] getResponseHeaders() {
        return responseHeaders;
    }
    
    /**
     * @param responseHeaders the responseHeaders to set
     */
    public void setResponseHeaders(Header[] responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
    
    
    /**
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }
    
    /**
     * @param msg the msg to set
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    
    /**
     * @return the crawlURL
     */
    public CrawlURL getCrawlURL() {
        return crawlURL;
    }

    
    /**
     * @param crawlURL the crawlURL to set
     */
    public void setCrawlURL(CrawlURL crawlURL) {
        this.crawlURL = crawlURL;
    }

    
    /**
     * @return the statusCode
     */
    public FetchStatus getStatusCode() {
        return statusCode;
    }

    
    
    /**
     * @return the httpstatus
     */
    public int getHttpstatus() {
        return httpstatus;
    }

    
    /**
     * @param httpstatus the httpstatus to set
     */
    public void setHttpstatus(int httpstatus) {
        this.httpstatus = httpstatus;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(FetchStatus statusCode) {
        this.statusCode = statusCode;
    }
}
