package com.yeepay.bigdata.crawler.crawl.model;

import com.yeepay.bigdata.crawler.crawl.utils.CharsetDetectorUtils;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* 页面对象
*
*/
public class Page {

    private byte[]               pageData;
    private String               pageString;
    private CrawlURL             crawlURL;
    private String               contentType;
    private String               contentCharset;
    private int                  status;
    private String               msg;
    private int                  httpStatus = 0;

    private static final Pattern pattern    = Pattern.compile(".*meta.*charset=(.*)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern xmlPattern = Pattern.compile("^<\\?xml.*encoding=(.*)\\?>", Pattern.CASE_INSENSITIVE);
    private static final Logger  LOGGER     = Logger.getLogger(Page.class);

    /**
     * @return the pageData
     */
    public byte[] getPageData() {
        return pageData;
    }

    /**
     * @param pageData the pageData to set
     */
    public void setPageData(byte[] pageData) {
        this.pageData = pageData;
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
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the contentCharset
     */
    public String getContentCharset() {
        return contentCharset;
    }

    /**
     * @param contentCharset the contentCharset to set
     */
    public void setContentCharset(String contentCharset) {
        this.contentCharset = contentCharset;
    }

    public synchronized void load(FetchResult fetchResult) throws IOException {
        HttpEntity httpEntity = fetchResult.getEntity();
        setCrawlURL(fetchResult.getCrawlURL());
        setStatus(fetchResult.getStatusCode().getIntValue());
        setMsg(fetchResult.getMsg());
        setHttpStatus(fetchResult.getHttpstatus());
        if (httpEntity == null) {
            setPageString("");
            setMsg("httpEntity is null and " + fetchResult.getMsg());
            if (getStatus() == 0) {
                setStatus(FetchStatus.other.getIntValue());
            }
            LOGGER.error("httpEntity is null and url is " + fetchResult.getCrawlURL() + " "
                         + ";crawl url failer with page ");
            return;
        }
        Charset charset = ContentType.getOrDefault(httpEntity).getCharset();
        if (charset != null) {
            contentCharset = charset.displayName();
            if (contentCharset != null
                && (contentCharset.equalsIgnoreCase("gb2312") || contentCharset.equalsIgnoreCase("gbk"))) {
                contentCharset = "gb18030";
            }
        }
        contentType = null;
        Header type = httpEntity.getContentType();
        if (type != null) {
            contentType = type.getValue();
        }
        pageData = EntityUtils.toByteArray(httpEntity);
        if (pageData == null) {
            setStatus(FetchStatus.other.getIntValue());
            setPageString("");
            LOGGER.error("url is " + fetchResult.getCrawlURL() + " " + ";crawl url failer with page ");
            return;
        }
        pageString = new String(pageData, contentCharset == null ? "utf-8" : contentCharset);
        if (contentCharset != null) {
            return;
        }
        // contentCharset = getContentCharsetInText(pageString,contentCharset);
        Charset detectCharset = CharsetDetectorUtils.detect(pageData);
        contentCharset = adaptCharset(detectCharset);
        if (Strings.isNullOrEmpty(contentCharset)&& !contentCharset.equalsIgnoreCase("utf-8")) {
            pageString = new String(pageData, contentCharset);
        }
    }

    public static void main(String[] args) {
        Pattern xmlPattern = Pattern.compile("^<\\?xml.*encoding=(.*)\\?>", Pattern.CASE_INSENSITIVE);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        Matcher matcher = null;
        String xmlCharset = "";
        if ((matcher = xmlPattern.matcher(xml)) != null && matcher.find()) {
            xmlCharset = matcher.group(1);
        }
        xmlCharset = StringUtils.replace(xmlCharset, "\"", "");
        if (StringUtils.containsIgnoreCase(xmlCharset, "utf-8") || StringUtils.containsIgnoreCase(xmlCharset, "UTF8")) {
            xmlCharset = "utf-8";
        }
    }

    private String adaptCharset(Charset charset) {
        if (charset == null) {
            return null;
        }
        String name = charset.displayName();
        if (StringUtils.containsIgnoreCase(name, "utf-8") || StringUtils.containsIgnoreCase(name, "UTF8")) {
            return "utf-8";
        }
        if (StringUtils.containsIgnoreCase(name, "gbk") || StringUtils.containsIgnoreCase(name, "gb")) {
            return "gb18030";
        }
        return name;
    }

    /**
     * 不使用了，太耗时
     *
     * @param pageString
     * @param contentCharset
     * @return
     */
    private String getContentCharsetInText(String pageString, String contentCharset) {
        Matcher matcher = null;
        if (contentCharset == null && (matcher = pattern.matcher(pageString)) != null && matcher.find()) {
            String metaCharset = matcher.group(1);
            metaCharset = StringUtils.replace(metaCharset, "\"", "");
            metaCharset = StringUtils.replace(metaCharset, "/>", "");
            metaCharset = StringUtils.replace(metaCharset, " ", "");
            metaCharset = StringUtils.replace(metaCharset, "/", "");
            if (StringUtils.isNotBlank(metaCharset)) {
                contentCharset = metaCharset;
                if (contentCharset.equalsIgnoreCase("gbk") || StringUtils.containsIgnoreCase(contentCharset, "gb")) {
                    contentCharset = "gbk";
                }
                if (StringUtils.containsIgnoreCase(contentCharset, "utf-8")
                    || StringUtils.containsIgnoreCase(contentCharset, "UTF8")) {
                    contentCharset = "utf-8";
                }
            }
        } else if (contentCharset == null && (matcher = xmlPattern.matcher(pageString)) != null && matcher.find()) {
            String xmlCharset = matcher.group(1);
            xmlCharset = StringUtils.replace(xmlCharset, "\"", "");
            if (StringUtils.containsIgnoreCase(xmlCharset, "gb")) {
                contentCharset = "gbk";
            }
            if (StringUtils.containsIgnoreCase(xmlCharset, "utf-8")
                || StringUtils.containsIgnoreCase(xmlCharset, "UTF8")) {
                contentCharset = "utf-8";
            }
        }
        return contentCharset;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return the pageString
     */
    public String getPageString() {
        return pageString;
    }

    /**
     * @param pageString the pageString to set
     */
    public void setPageString(String pageString) {
        this.pageString = pageString;
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
     * @return the httpStatus
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * @param httpStatus the httpStatus to set
     */
    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public String toString() {
        return "Page [" + "crawlURL=" + crawlURL + ", contentType=" + contentType + ", contentCharset="
                + contentCharset + ", status=" + status + ", msg=" + msg + "]";
    }


}
