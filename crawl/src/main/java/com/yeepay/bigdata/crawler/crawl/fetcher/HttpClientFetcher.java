package com.yeepay.bigdata.crawler.crawl.fetcher;

import com.yeepay.bigdata.crawler.crawl.dns.NonBlockingDNSResolver;
import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;
import com.yeepay.bigdata.crawler.crawl.model.FetchResult;
import com.yeepay.bigdata.crawler.crawl.monitor.Dumpable;
import com.yeepay.bigdata.crawler.crawl.processor.PushBackProcessor;
import com.yeepay.bigdata.crawler.crawl.processor.ResultProcessor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class HttpClientFetcher extends FetcherResultWrapper implements Fetcher {

    private static final Logger LOGGER = Logger.getLogger(HttpClientFetcher.class);
    private final FetchConfig fetchConfig;
    /**
     * 抓取结果回调访问
     */
    private final ResultProcessor<FetchResult> fetchResultVisitor;
    private PoolingHttpClientConnectionManager poolingmgr;
    private final HttpClient httpClient;

    public HttpClientFetcher() {
        this(FetchConfig.DEFAULT, new PushBackProcessor());
    }

    public HttpClientFetcher(FetchConfig fetchConfigValue, ResultProcessor<FetchResult> fetchResultVisitor) {
        this.fetchConfig = fetchConfigValue;
        this.fetchResultVisitor = fetchResultVisitor;
        httpClient = buildHttpClient(fetchConfig);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Boolean fetch(CrawlURL crawlURL) throws Exception {
        FetchResult fetchResult = null;
        HttpContext httpContext = new BasicHttpContext();
        HttpResponse httpResponse;
        HttpGet httpGet = null;
        try {
            try {
                crawlURL.setFetchTs(System.currentTimeMillis());
                httpGet = new HttpGet(crawlURL.getUrl());
                httpResponse = httpClient.execute(httpGet, httpContext);
            } catch (Exception e) {
                fetchResult = wrapperResult(crawlURL, httpContext, null, e, null);
                fetchResult.setMsg("error when execute url " + crawlURL.getUrl() + " " + ExceptionUtils.getMessage(e));
                fetchResultVisitor.processResult(fetchResult);
                LOGGER.error("error when execute url " + crawlURL.getUrl() + " " + ExceptionUtils.getMessage(e), e);
                return false;
            }
            fetchResult = wrapperResult(crawlURL, httpContext, httpResponse, null, null);
/** ------------------------------ 用于测试 ---------------------------------------------------------------- */
//            InputStreamReader inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent());
//            InputStreamReader inputStreamReader1 = new InputStreamReader(httpResponse.getEntity().getContent(), Charset.forName("GBK"));
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//            String line = null;
//            StringBuffer sb = new StringBuffer();
//            while ((line = bufferedReader.readLine()) != null) {
//                sb.append(line + "\n");
//            }
//            System.out.println("sb : " + sb.toString());
/** ------------------------------------------------------------------------------------------------------ */
            fetchResultVisitor.processResult(fetchResult);
        } finally {
            if (httpGet != null) {
                // 抓取站点每次都还回链接
                httpGet.abort();
            }
        }
        return true;
    }

    public Boolean fetch(CrawlURL crawlURL, ResultProcessor<FetchResult> processCallback) {
        FetchResult fetchResult = null;
        HttpContext httpContext = new BasicHttpContext();
        HttpResponse httpResponse;
        HttpGet httpGet = null;
        try {
            try {
                crawlURL.setFetchTs(System.currentTimeMillis());
                httpGet = new HttpGet(crawlURL.getUrl());
                httpResponse = httpClient.execute(httpGet, httpContext);
            } catch (Exception e) {
                fetchResult = wrapperResult(crawlURL, httpContext, null, e, null);
                fetchResult.setMsg("error when execute url " + crawlURL.getUrl() + " " + ExceptionUtils.getMessage(e));
                processCallback.processResult(fetchResult);
                LOGGER.error("error when execute url " + crawlURL.getUrl() + " " + ExceptionUtils.getMessage(e), e);
                return false;
            }
            fetchResult = wrapperResult(crawlURL, httpContext, httpResponse, null, null);
            System.out.println("HttpClientFetcher : " + fetchResult);
            processCallback.processResult(fetchResult);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (httpGet != null) {
                httpGet.abort();
            }
        }
        return true;
    }

    @Override
    public void destroy() throws Exception {
    }

    private HttpClient buildHttpClient(final FetchConfig fetchConfig) {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(fetchConfig.getConnectionTimeOut()).setSocketTimeout(fetchConfig.getSocketTimeOut()).build();
        poolingmgr = new PoolingHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create().register("http",
                        PlainConnectionSocketFactory.getSocketFactory()).build(),
                NonBlockingDNSResolver.getInstance());
        poolingmgr.setDefaultMaxPerRoute(100);
        poolingmgr.setMaxTotal(500);
        HttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setUserAgent(fetchConfig.getUserAgent()).addInterceptorFirst(new HttpRequestInterceptor() {

            @Override
            public void process(HttpRequest request,
                                HttpContext context)
                    throws HttpException,
                    IOException {
                /** 默认使用gzip */
                request.addHeader("Accept-Encoding",
                        "gzip");
            }
        }).addInterceptorFirst(new HttpResponseInterceptor() {

            @Override
            public void process(final HttpResponse response,
                                final HttpContext context)
                    throws HttpException,
                    IOException {
                HttpEntity entity = response.getEntity();
                Header contentEncoding = entity.getContentEncoding();
                if (contentEncoding != null) {
                    HeaderElement[] codecs = contentEncoding.getElements();
                    for (HeaderElement codec : codecs) {
                        if (codec.getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new GzipDecompressingEntity(
                                    response.getEntity()));
                            return;
                        }
                    }
                }
            }
        }).addInterceptorFirst(new HttpResponseInterceptor() {

            @Override
            public void process(HttpResponse response,
                                HttpContext context)
                    throws HttpException,
                    IOException {
                HttpEntity entity = response.getEntity();
                long len = entity.getContentLength();
                if (len > fetchConfig.getMaxdownloadSize()) {
                    throw new ContentTooLongException(
                            "Entity content is too long: "
                                    + len);
                }
                if (len < 0) {
                    len = 4096;
                }
            }
        }).setConnectionManager(poolingmgr).setConnectionReuseStrategy(new NoConnectionReuseStrategy()).build();
        return httpclient;
    }

    /** -------------------------------------------------------------------------------------------------------------------------- */
    /**
     * the method used by netty-http, 添加（2014-11-07）
     */

    public String fetchHtml(CrawlURL crawlURL) {

        HttpContext httpContext = new BasicHttpContext();
        HttpResponse httpResponse = null;
        HttpGet httpGet = null;
        try {
            crawlURL.setFetchTs(System.currentTimeMillis());
            httpGet = new HttpGet(crawlURL.getUrl());
            httpResponse = httpClient.execute(httpGet, httpContext);
            InputStream inputStream = httpResponse.getEntity().getContent();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (httpGet != null) {
                httpGet.abort();
            }
        }
        return "";
    }

    @Override
    public String dump() {
        return null;
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException, InterruptedException {
//        out.append("poolingManager status is " + poolingmgr.getTotalStats()).append(" ").append("    "
//                + System.getProperty("line.separator"));

        out.append(String.format("%-50s", ("poolingManager status is ")) + "[" + poolingmgr.getTotalStats() + "]").append("    " + System.getProperty("line.separator"));

        ((Dumpable) NonBlockingDNSResolver.getInstance()).dump(out, indent);
    }

}
