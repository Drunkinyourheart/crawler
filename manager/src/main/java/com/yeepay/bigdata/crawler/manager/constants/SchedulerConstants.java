package com.yeepay.bigdata.crawler.manager.constants;

public interface SchedulerConstants {

    public static final int    RETRY_TIMES         = 1;
    public static final int    SIGINAL_THRESHOLD_INITTASKQUEUE         = 20000;
    /**
     * dest url for URL redirected
     */
    public static final String DEST_URL            = "destURL";

    public static final String SEED_INFO_TYPE      = "seedType";

    public static final String HTTP_STATUS         = "httpStatus";

    public static final String FROM_URL            = "fromURL";

    public static final String DETAIL_NEXT_INDEX   = "IsNext";

    public static final String DETAIL_NEXT_URL_KEY = "NextURL";

    public static final String NEWS_URL_TITLE = "title";//目前在判断热闻中用到，热闻分页时，第一页的task没有title

    public static final String VALIDATE_MIDDLE_URL_KEY = "FirstURL";//第一次抽取识别为中间页的标识

    public static final String START_CRAWLER_TS = "startCrawlerTs";////设置抓取时间，来方便统计每个种子从抓取到入库耗时

    /**
     * 分页新闻：第一页index值
     */
    public static final int    FIRST_PAGE_INDEX    = 1;

    /**
     * 分页新闻：最大index值
     */
    public static final int    MAX_PAGE_INDEX      = 100;

    public static final String MERGE_IMAGE_KEY     = "image";

    public static final String MERGE_CONTENT_KEY   = "content";
    /**
     * 内网，外网，精细化配置标识前缀标记
     */
    public static final String  NEWSLEVEL_PREFIX = "0_";
}
