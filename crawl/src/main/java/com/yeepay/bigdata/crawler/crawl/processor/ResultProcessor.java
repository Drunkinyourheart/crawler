package com.yeepay.bigdata.crawler.crawl.processor;


import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;

/**
* 抓取结果访问接口
*/
public interface  ResultProcessor<T> {

    public boolean shouldFetch(CrawlURL crawlURL);

    public void    processResult(T t);
}
