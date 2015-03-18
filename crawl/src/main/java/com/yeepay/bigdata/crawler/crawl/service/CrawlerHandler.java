package com.yeepay.bigdata.crawler.crawl.service;

import com.yeepay.bigdata.crawler.crawl.crawler.Crawler;
import com.yeepay.bigdata.crawler.crawl.crawler.SeedTypeEnum;
import com.yeepay.bigdata.crawler.crawl.fetcher.HttpClientFetcher;
import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;
import com.yeepay.bigdata.crawler.crawl.model.FetchResult;
import com.yeepay.bigdata.crawler.crawl.processor.PushBackProcessor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.util.Map;

public class CrawlerHandler implements Handler {

    private HttpClientFetcher httpClientFetcher;
    private Crawler crawler;
    private static final Logger LOGGER = Logger.getLogger(CrawlerHandler.class);

    public CrawlerHandler(HttpClientFetcher httpClientFetcher, Crawler crawler) {
        this.httpClientFetcher = httpClientFetcher;
        this.crawler = crawler;
    }

    public String doGet(Map<String, String> map) {
//        String seedType = map.get("type");
        String url = map.get("url");
//        String id  = map.get("id");
        if (StringUtils.isBlank(url)) {
            return "error parameter value for 'url'";
        }
        CrawlURL crawlURL = new CrawlURL(DigestUtils.md5Hex(url), url, "static");
//        crawlURL.setSeedType("hotNewsDetail");
        crawlURL.setSeedType(SeedTypeEnum.HOTNEWSDETAIL.name());
        crawlURL.setFromURL(url);
        Boolean result = crawler.addURL(crawlURL);

//        return result == true ? url + "成功加入抓取队列" : url + "加入抓取队列失败";
        /** ------------------------------------------------------------------------------------------------------------------------- */
        /** 重新定义返回结果 */
        String fetchResult = httpClientFetcher.fetchHtml(crawlURL);
        return result == true ? url + "成功加入抓取队列" + "\n" + fetchResult : url + "加入抓取队列失败";
    }

    @Override
    public String doPost(Map<String, String> map, String content) {
        return "";
    }

}
