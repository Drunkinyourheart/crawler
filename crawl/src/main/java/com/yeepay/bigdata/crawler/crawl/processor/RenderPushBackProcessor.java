package com.yeepay.bigdata.crawler.crawl.processor;

import com.yeepay.bigdata.crawler.crawl.model.CrawlURL;
import com.yeepay.bigdata.crawler.crawl.model.Page;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResult;
import com.yeepay.bigdata.crawler.crawl.thrift.service.CrawlerTaskResultStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 类RenderPushBackProcessor.java的实现描述：TODO 类实现描述
 */
public class RenderPushBackProcessor implements ResultProcessor<Page> {

    private static final Logger logger = Logger.getLogger(PushBackProcessor.class);


    @Override
    public boolean shouldFetch(CrawlURL crawlURL) {
        return true;
    }

    @Override
    public void processResult(Page page) {
        try {
            CrawlerTaskResult crawlerTaskResult = new CrawlerTaskResult(adaptCrawlResult(page));
            ThriftClientManager.getPooledClient().pushCrawlerTaskResult(crawlerTaskResult);
            logger.info("dynamic crawl url fetch is success with page " + page);
        } catch (Exception e) {
            logger.error("url push error with page " + page + " " + ExceptionUtils.getMessage(e), e);
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
        CrawlerTaskResultStatus status = CrawlerTaskResultStatus.Succeed;
        if (StringUtils.isBlank(page.getPageString())) {
            status = CrawlerTaskResultStatus.Failure;
        }
        CrawlerTaskResult crawlerTaskResult = new CrawlerTaskResult(page.getCrawlURL().getId(), page.getCrawlURL().getUrl(), page.getPageString(), status);
        if (StringUtils.isNotBlank(page.getMsg())) {
            crawlerTaskResult.setMsg(page.getMsg());
        } else {
            crawlerTaskResult.setMsg("");
        }
        Map<String, String> map = new HashMap<String, String>();
        String destURL = page.getCrawlURL().getDestURL();
        if (StringUtils.isBlank(destURL)) {
            destURL = page.getCrawlURL().getUrl();
        }
        map.put("destURL", destURL);
        map.put("seedType", page.getCrawlURL().getSeedType());
        map.put("fromURL", page.getCrawlURL().getFromURL());
        map.put("httpStatus", "" + page.getHttpStatus());
        map.put("publicationName", page.getCrawlURL().getPublicationName());
        map.put("typeArea", page.getCrawlURL().getTypeArea() + "");
        map.put("channel", page.getCrawlURL().getChannel() + "");
        map.put("sub_channel", page.getCrawlURL().getSub_channel() + "");
        map.put("sourceSeedType", page.getCrawlURL().getSourceSeedType() + "");
        crawlerTaskResult.setCtxMap(map);
        return crawlerTaskResult;
    }
}
