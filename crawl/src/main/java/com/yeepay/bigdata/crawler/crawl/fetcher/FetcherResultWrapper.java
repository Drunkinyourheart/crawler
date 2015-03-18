package com.yeepay.bigdata.crawler.crawl.fetcher;

import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;
import com.yeepay.bigdata.crawler.crawl.model.FetchResult;
import com.yeepay.bigdata.crawler.crawl.model.FetchStatus;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.apache.commons.lang3.exception.ExceptionUtils;

public abstract class FetcherResultWrapper {

    private static final Logger LOGGER = Logger.getLogger(FetcherResultWrapper.class);

    /**
     * 构建fetchResult
     */
    protected FetchResult wrapperResult(CrawlURL crawlURL, HttpContext httpContext, HttpResponse response, Exception ex, FetchStatus fetchStatus) {

        FetchResult fetchResult = new FetchResult();
        fetchResult.setCrawlURL(crawlURL);

        try {
            if (ex == null && response == null) {
                fetchResult.setStatusCode(fetchStatus);
            }
            if (ex != null) {
                if (ex instanceof SocketTimeoutException || ex instanceof ConnectTimeoutException) {
                    fetchResult.setStatusCode(FetchStatus.timeout);
                } else if (ex instanceof ConnectException) {
                    fetchResult.setStatusCode(FetchStatus.connectexception);
                } else {
                    fetchResult.setStatusCode(FetchStatus.other);
                }
                fetchResult.setMsg(ExceptionUtils.getMessage(ex));
            }
            if (response != null) {
                fetchResult.setEntity(response.getEntity());
                fetchResult.setResponseHeaders(response.getAllHeaders());
                int statusCode = response.getStatusLine().getStatusCode();
                fetchResult.setHttpstatus(statusCode);
                if (statusCode != 200) {
                    fetchResult.setStatusCode(FetchStatus.not200);
                    fetchResult.setMsg("fetch http status is not 200 and statusCode is " + statusCode);
                } else {
                    fetchResult.setStatusCode(FetchStatus.fetched);
                    fetchResult.setMsg("fetch success");
                }
            }
            if (httpContext == null) {
                return fetchResult;
            }
            HttpUriRequest currentReq = (HttpUriRequest) httpContext.getAttribute(HttpClientContext.HTTP_REQUEST);
            HttpHost currentHost = (HttpHost) httpContext.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
            String currentUrl = crawlURL.getUrl();
            if (currentHost != null && currentReq != null) {
                currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq.getURI().toString() : (currentHost.toURI() + currentReq.getURI());
            }
            crawlURL.setDestURL(currentUrl);
            return fetchResult;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage() + " crawlURL is " + crawlURL, e);
            fetchResult.setMsg("erro when wrapper fetch result with url " + crawlURL.getUrl());
            return fetchResult;
        }
    }
}
