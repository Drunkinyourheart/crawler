package com.yeepay.bigdata.crawler.crawl.processor;

import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;
import com.yeepay.bigdata.crawler.crawl.model.FetchResult;
import com.yeepay.bigdata.crawler.crawl.model.FetchStatus;
import com.yeepay.bigdata.crawler.crawl.model.Page;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回结果调度中心
 */
public class PushBackProcessor implements ResultProcessor<FetchResult> {

    private static final Logger logger = Logger.getLogger(PushBackProcessor.class);

    @Override
    public boolean shouldFetch(CrawlURL crawlURL) {
        return true;
    }

    @Override
    public void processResult(FetchResult result) {
        System.out.println("PushBackProcessor ................... ");
        Page page = new Page();
        try {
            page.load(result);
        } catch (Throwable e) {
            page.setStatus(FetchStatus.other.getIntValue());
            page.setPageString("");
            page.setMsg(ExceptionUtils.getMessage(e));
            logger.error("crawl url failer with " + page.getCrawlURL() + " " + e.getMessage(), e);
        }
        try {

            CrawlerTaskResult crawlerTaskResult = new CrawlerTaskResult(adaptCrawlResult(page));
            System.out.println("PushBackProcessor " + crawlerTaskResult);
            ThriftClientManager.getPooledClient().pushCrawlerTaskResult(crawlerTaskResult);
            logger.info("static crawl url fetch is success with page " + page);
        } catch (Throwable e) {
            logger.error("push page error with crawlURL is page " + page + " -" + e.getMessage(), e);
        } finally {
            CrawlURL crawlURL = page.getCrawlURL();
            if (crawlURL != null) {
                crawlURL.setPushTs(System.currentTimeMillis());
                logger.info("crawl url " + crawlURL.getUrl() + " statistics: {addQueueTs="
                        + crawlURL.getAccessQueueTs() + ", fetchTs=" + crawlURL.getFetchTs() + ", pushTs="
                        + crawlURL.getPushTs() + ", fetchTimeSpare=" + (crawlURL.getPushTs() - crawlURL.getFetchTs()) + ", totalTimeSpare=" + (crawlURL.getPushTs() - crawlURL.getAccessQueueTs()) + " }");
            }
        }
    }

    private CrawlerTaskResult adaptCrawlResult(Page page) {
        CrawlerTaskResultStatus status = CrawlerTaskResultStatus.Error;
        if (page.getStatus() == FetchStatus.timeout.getIntValue()) {
            status = CrawlerTaskResultStatus.Failure;
        } else if (page.getStatus() == FetchStatus.fetched.getIntValue()) {
            status = CrawlerTaskResultStatus.Succeed;
        }
        CrawlerTaskResult crawlerTaskResult = new CrawlerTaskResult(page.getCrawlURL().getId(), page.getCrawlURL().getUrl(), page.getPageString(),
                status);
        if (StringUtils.isNotBlank(page.getMsg())) {
            crawlerTaskResult.setMsg(page.getMsg());
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("destURL", page.getCrawlURL().getDestURL());
        map.put("seedType", page.getCrawlURL().getSeedType());
        map.put("fromURL", page.getCrawlURL().getFromURL());
        map.put("httpStatus", "" + page.getHttpStatus());
        map.put("startCrawlerTs", page.getCrawlURL().getTimestamp() + "");//设置抓取时间，来方便统计每个种子从抓取到入库耗时
        map.put("publicationName", page.getCrawlURL().getPublicationName() + "");
        map.put("typeArea", page.getCrawlURL().getTypeArea() + "");
        map.put("channel", page.getCrawlURL().getChannel() + "");
        map.put("sub_channel", page.getCrawlURL().getSub_channel() + "");
        map.put("sourceSeedType", page.getCrawlURL().getSourceSeedType() + "");
        crawlerTaskResult.setCtxMap(map);
        return crawlerTaskResult;
    }

}
