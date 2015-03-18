package com.yeepay.bigdata.crawler.manager.utils;

import java.util.List;

/**
 * 类MultiPageCache.java的实现描述：存储分页新闻内容
 * 
 */
public interface MultiPageCache {

    /**
     */
    public void initAndStore(String key, String result);

    /**
     * 追加缓存数据
     * 
     */
    public void appendStore(String key, String result);

    /**
     * 获取所有的缓存数据
     * 
     */
    public List<String> getCacheAndClear(String key);

    public int size();
}
