package com.yeepay.bigdata.crawler.crawl.crawler;

import org.apache.commons.lang3.StringUtils;

public enum SeedTypeEnum {

    LIST("list"), RSSLIST("rsslist"), EPAPER("epaper"), HOTNEWSDETAIL("hotNewsDetail");

    private String type;

    private SeedTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * 默认页面类型：List
     *
     */
    public static SeedTypeEnum getSeedType(String type) {
        for (SeedTypeEnum seedType : values()) {
            if (StringUtils.equalsIgnoreCase(type, seedType.type)) {
                return seedType;
            }
        }
        return LIST;
    }
}
