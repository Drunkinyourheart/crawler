package com.yeepay.bigdata.crawler.crawl.fetcher;

import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;
import com.yeepay.bigdata.crawler.crawl.monitor.Dumpable;

public interface Fetcher extends Dumpable {

    public <T>  T fetch(CrawlURL crawlURL) throws Exception;

    public void destroy() throws Exception;
}
