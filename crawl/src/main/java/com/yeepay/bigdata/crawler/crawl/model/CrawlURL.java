package com.yeepay.bigdata.crawler.crawl.model;

import org.apache.http.cookie.Cookie;

import java.util.List;

/**
 * 抓取URL
 */
public class CrawlURL {

    private String id;
    private String url;
    private String domain;
    private String path;
    private String type;
    private List<Cookie> cookies;
    private String cookieKey;
    private String destURL;
    private long timestamp;
    private long accessQueueTs;
    private long fetchTs;
    private long pushTs;

    private int httpStatus;
    private String seedType = "";
    private String fromURL = "";
    private String publicationName = "";//刊物名称
    private String typeArea = "";//版面列表，以逗号分隔
    private String channel = "";//频道
    private String sub_channel = "";//子频道
    private String sourceSeedType = "";

    public CrawlURL(String id, String url, String type) {
        this.id = id;
        this.url = url;
        this.type = type;
    }

    /**
     * ListUrl刊物，版面添加
     */
    public CrawlURL(String id, String url, String type, String publicationName, String typeArea) {
        super();
        this.id = id;
        this.url = url;
        this.type = type;
        this.publicationName = publicationName;
        this.typeArea = typeArea;
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    public String getCookieKey() {
        return cookieKey;
    }

    public String getDestURL() {
        return destURL;
    }

    public void setDestURL(String destURL) {
        this.destURL = destURL;
    }

    public void setCookieKey(String cookieKey) {
        this.cookieKey = cookieKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getAccessQueueTs() {
        return accessQueueTs;
    }

    public void setAccessQueueTs(long accessQueueTs) {
        this.accessQueueTs = accessQueueTs;
    }

    public long getFetchTs() {
        return fetchTs;
    }

    public void setFetchTs(long fetchTs) {
        this.fetchTs = fetchTs;
    }

    public long getPushTs() {
        return pushTs;
    }

    public void setPushTs(long pushTs) {
        this.pushTs = pushTs;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getSeedType() {
        return seedType;
    }

    public void setSeedType(String seedType) {
        this.seedType = seedType;
    }

    public String getFromURL() {
        return fromURL;
    }

    public void setFromURL(String fromURL) {
        this.fromURL = fromURL;
    }

    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }

    public String getTypeArea() {
        return typeArea;
    }

    public void setTypeArea(String typeArea) {
        this.typeArea = typeArea;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSub_channel() {
        return sub_channel;
    }

    public void setSub_channel(String sub_channel) {
        this.sub_channel = sub_channel;
    }

    public String getSourceSeedType() {
        return sourceSeedType;
    }

    public void setSourceSeedType(String sourceSeedType) {
        this.sourceSeedType = sourceSeedType;
    }

    @Override
    public String toString() {
        return "CrawlURL [id=" + id + ", url=" + url + ", domain=" + domain
                + ", path=" + path + ", type=" + type + ", cookies=" + cookies
                + ", cookieKey=" + cookieKey + ", destURL=" + destURL
                + ", timestamp=" + timestamp + ", accessQueueTs="
                + accessQueueTs + ", fetchTs=" + fetchTs + ", pushTs=" + pushTs
                + ", httpStatus=" + httpStatus + ", seedType=" + seedType
                + ", fromURL=" + fromURL + ", publicationName="
                + publicationName + ", typeArea=" + typeArea + ", channel="
                + channel + ", sub_channel=" + sub_channel + "]";
    }

}
