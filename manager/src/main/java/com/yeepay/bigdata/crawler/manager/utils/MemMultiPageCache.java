package com.yeepay.bigdata.crawler.manager.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 类MemMultiPageCache.java的实现描述：no thread safe; should use lock
 * 
 */
public class MemMultiPageCache implements MultiPageCache {

    private ConcurrentMap<String, List<String>> cache = new ConcurrentHashMap<String, List<String>>(1000);
    private AtomicInteger size = new AtomicInteger(0);

    @Override
    public void initAndStore(String key, String result) {
        List<String> values = cache.get(key);
        if (values != null) {
            values.clear();
        } else {
            values = new LinkedList<String>();
            cache.put(key, values);
        }
	size.incrementAndGet();
        values.add(result);
    }

    @Override
    public void appendStore(String key, String result) {
        List<String> values = cache.get(key);
        values.add(result);
    }

    @Override
    public List<String> getCacheAndClear(String key) {
        List<String> values = cache.get(key);
        cache.remove(key);
	size.decrementAndGet();;
        return values;
    }

    @Override
    public int size() {
        return size.get();
    }

}
