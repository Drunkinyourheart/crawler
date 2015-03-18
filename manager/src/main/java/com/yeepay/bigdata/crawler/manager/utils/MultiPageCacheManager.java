package com.yeepay.bigdata.crawler.manager.utils;

/**
 * 类MultiPageCacheManager.java的实现描述：TODO 类实现描述
 * 
 */
public class MultiPageCacheManager {

    private static MultiPageCache memCache = new MemMultiPageCache();

    private static MultiPageLock  pageLock = new MultiPageLock();

    public static MultiPageCache getMemCache() {
        return memCache;
    }

    public static MultiPageLock getMultiPageLock() {
        return pageLock;
    }
}
