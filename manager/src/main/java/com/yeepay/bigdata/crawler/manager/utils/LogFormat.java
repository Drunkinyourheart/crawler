package com.yeepay.bigdata.crawler.manager.utils;

import java.util.ArrayList;
import java.util.List;

public enum LogFormat {

    STAT_ASSIGN_CRAWLER(
                        builder().addKey(LogKey.action).addKey(LogKey.seedTypeInfo).addKey(LogKey.url).addKey(LogKey.status).addKey(LogKey.firstPageURL).addKey(LogKey.pageIndex).build()), // 分派抓取任务统计日志
    STAT_CRAWLER_RESULT(
                        builder().addKey(LogKey.action).addKey(LogKey.seedTypeInfo).addKey(LogKey.url).addKey(LogKey.httpStatus).addKey(LogKey.status).addKey(LogKey.fromURL).addKey(LogKey.firstPageURL).addKey(LogKey.pageIndex).build()), // 抓取结果统计日志
    STAT_CRAWLER_DUPLICATE(
                           builder().addKey(LogKey.action).addKey(LogKey.seedTypeInfo).addKey(LogKey.url).addKey(LogKey.httpStatus).addKey(LogKey.status).addKey(LogKey.fromURL).addKey(LogKey.duplicate).addKey(LogKey.firstPageURL).addKey(LogKey.pageIndex).build()), // 抓取结果统计日志
    STAT_EXTRACT_FAILURE(
                         builder().addKey(LogKey.action).addKey(LogKey.seedTypeInfo).addKey(LogKey.url).addKey(LogKey.status).addKey(LogKey.fromURL).addKey(LogKey.domain).addKey(LogKey.firstPageURL).addKey(LogKey.pageIndex).build()), // 抽取结果日志
    STAT_EXTRACT_SUCCESS(
                         builder().addKey(LogKey.action).addKey(LogKey.seedTypeInfo).addKey(LogKey.url).addKey(LogKey.status).addKey(LogKey.fromURL).addKey(LogKey.domain).addKey(LogKey.validate).addKey(LogKey.delayTime).addKey(LogKey.firstPageURL).addKey(LogKey.pageIndex).build()),
    STAT_EXTRACT_DETAIL_SUCCESS(
                                builder().addKey(LogKey.action).addKey(LogKey.seedTypeInfo).addKey(LogKey.url).addKey(LogKey.status).addKey(LogKey.fromURL).addKey(LogKey.domain).addKey(LogKey.validate).addKey(LogKey.delayTime).addKey(LogKey.multiDetailPage).addKey(LogKey.nextURL).addKey(LogKey.firstPageURL).addKey(LogKey.pageIndex).build()),
    STAT_BETWEEN_CRAWLER_EXTRACT_SUCCESS_TIME(//用于统计一个URL从开始抓取到准备入库之间的耗时
            builder().addKey(LogKey.action).addKey(LogKey.seedTypeInfo).addKey(LogKey.url).addKey(LogKey.fromURL).addKey(LogKey.domain).addKey(LogKey.firstPageURL).addKey(LogKey.pageIndex).addKey(LogKey.theWholeTimeForCrawlerStartToExtractResultEnd).addKey(LogKey.theWholeTimeForCrawlerStartToExtractResultEndBeforeInDb).addKey(LogKey.startCrawlerTime).build());
    private String format;

    private LogFormat(String format){
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static class Builder {

        public static final String SEPARATOR           = " -|- ";

        public static final String KEY_VALUE_SEPARATOR = " : ";

        private List<String>       keyList             = new ArrayList<String>();

        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append(SEPARATOR);
            for (String key : keyList) {
                sb.append(key).append(KEY_VALUE_SEPARATOR).append("%s").append(SEPARATOR);
            }
            return sb.toString();
        }

        public Builder addKey(LogKey key) {
            keyList.add(key.name());
            return this;
        }

        private Builder(){
        }
    }

    private enum LogKey {
        action, seedTypeInfo, url, httpStatus, status, fromURL, validate, duplicate, delayTime, domain,
        multiDetailPage, firstPageURL, pageIndex, nextURL,theWholeTimeForCrawlerStartToExtractResultEndBeforeInDb,theWholeTimeForCrawlerStartToExtractResultEnd,startCrawlerTime;
        //从开始抓取到抓取结果抽取完毕准备入库所耗的时间，theWholeTimeForCrawlerStartToExtractResultEnd
    }
}
