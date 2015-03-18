package com.yeepay.bigdata.crawler.crawl.fetcher;

import com.yeepay.bigdata.crawler.crawl.fetcher.assist.CookiesRepo;
import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;
import com.yeepay.bigdata.crawler.crawl.model.FetchResult;
import com.yeepay.bigdata.crawler.crawl.model.FetchStatus;
import com.yeepay.bigdata.crawler.crawl.processor.PushBackProcessor;
import com.yeepay.bigdata.crawler.crawl.processor.ResultProcessor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.*;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleInputBuffer;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
* 基于httpAsyncClient NIO抓取
*/
public class HttpClientNIOFetcher extends FetcherResultWrapper implements Fetcher{
    private static final Logger LOGGER = Logger.getLogger(HttpClientNIOFetcher.class);
    private  final CloseableHttpAsyncClient httpclient ;
    private  final FetchConfig fetchConfig;
    /** 抓取结果回调访问 */
    private  final ResultProcessor<FetchResult> fetchResultVisitor;

    public HttpClientNIOFetcher(){
        this(FetchConfig.DEFAULT,new PushBackProcessor());
    }

    /**
     *
     */
    public HttpClientNIOFetcher(FetchConfig fetchConfigValue,ResultProcessor<FetchResult> fetchResultVisitor){
       this.fetchConfig = fetchConfigValue;
       this.fetchResultVisitor = fetchResultVisitor;
       RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(fetchConfig.getConnectionTimeOut()).setSocketTimeout(fetchConfig.getSocketTimeOut()).build();
       httpclient = HttpAsyncClients.custom().setUserAgent(fetchConfig.getUserAgent()).setDefaultRequestConfig(requestConfig).addInterceptorFirst(new HttpRequestInterceptor() {
              @Override
              public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                          /** 默认使用gzip */
                          request.addHeader("Accept-Encoding", "gzip");
             }
       }).build();
       /** 启动httpclient 默认会启动4个异步线程 */
       httpclient.start();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<HttpResponse> fetch(final CrawlURL crawlURL) {
        try {
            final HttpGet httpGet = new HttpGet(crawlURL.getUrl());
            HttpAsyncRequestProducer  producer=  HttpAsyncMethods.create(httpGet);
            final  HttpContext httpContext = buildHttpContext(crawlURL);
            Future<HttpResponse> future = httpclient.execute(producer,new MyAsyncResponseConsumer(),httpContext,new FutureCallback<HttpResponse>(){
                @Override
                public void completed(HttpResponse result) {
                    //自行try catch 否则异常会 不会抛出
                    fetchResultVisitor.processResult(wrapperResult(crawlURL,httpContext,result, null,null));
                }

                @Override
                public void failed(Exception ex) {
                    //自行try catch 否则异常会 不会抛出
                    LOGGER.error("crawl url failer with crawlURL " +crawlURL+" "+ex.getMessage(),ex);
                    fetchResultVisitor.processResult(wrapperResult(crawlURL,httpContext,null, ex,null));
                }

                @Override
                public void cancelled() {
                    //自行try catch 否则异常会 不会抛出
                    LOGGER.error("crawl url failer with crawlURL " +crawlURL);
                    fetchResultVisitor.processResult(wrapperResult(crawlURL,httpContext,null, null,FetchStatus.canceled));
                }

            });
            return future;
        } catch (Throwable e) {
            if (crawlURL!=null) {
                LOGGER.error("crawl url failer with crawlURL " +crawlURL+" "+ExceptionUtils.getMessage(e), e);
                FetchResult fetchResult = wrapperResult(crawlURL,null,null, null, FetchStatus.other);
                fetchResult.setMsg("fetch error before try send fetch request"+ExceptionUtils.getMessage(e));
                fetchResultVisitor.processResult(fetchResult);
            }
            return null;
        }


    }


    /**
     *
     * httpresponse consumer 替换basicAsyncResponseConsumer
     * 否则gzip无法解压
     */
    @ThreadSafe
    class MyAsyncResponseConsumer extends AbstractAsyncResponseConsumer<HttpResponse>{
        private volatile HttpResponse response;
        private volatile SimpleInputBuffer buf;


        @Override
        protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
            this.response = response;
        }

        @Override
        protected void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException {
            this.buf.consumeContent(decoder);
        }

        @Override
        protected void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException {
            long len = entity.getContentLength();
            if (len > fetchConfig.getMaxdownloadSize()) {
                throw new ContentTooLongException("Entity content is too long: " + len);
            }
            if (len < 0) {
                len = 4096;
            }
            this.buf = new SimpleInputBuffer((int) len, new HeapByteBufferAllocator());
            HttpEntity httpEntity = new ContentBufferEntity(entity, this.buf);
            if (isGzipFormat(httpEntity)) {
                this.response.setEntity(new GzipDecompressingEntity(httpEntity));
            }else {
                this.response.setEntity(httpEntity);
            }
        }

        /**
         * @param entity
         */
        private boolean isGzipFormat(HttpEntity entity) {
            Header contentEncoding = entity.getContentEncoding();
            if (contentEncoding != null) {
                HeaderElement[] codecs = contentEncoding.getElements();
                for (HeaderElement codec : codecs) {
                    if (codec.getName().equalsIgnoreCase("gzip")) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        protected HttpResponse buildResult(HttpContext context) throws Exception {
           return response;
        }

        @Override
        protected void releaseResources() {
            this.response = null;
            this.buf= null;
        }

    }

    /**
     * 销毁httpclient，释放线程资源
     */
    @Override
    public void destroy() throws IOException  {
        if (httpclient!=null) {
            httpclient.close();
        }

    }

    private HttpContext buildHttpContext(CrawlURL crawlURL){
        HttpContext httpContext = new BasicHttpContext();
        if (crawlURL.getCookieKey()==null) {
            return httpContext;
        }
        CookieStore cookieStore   = new BasicCookieStore();
        List<Cookie> list = CookiesRepo.getCookies(crawlURL.getCookieKey());
        if (list==null||list.isEmpty()) {
            throw new IllegalStateException(crawlURL.getCookieKey() +" cookies can't be null");
        }
        for (Cookie cookie: list) {
             cookieStore.addCookie(cookie);
        }
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        return httpContext;
    }

    @Override
    public String dump() {
        return null;
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
    }


}
