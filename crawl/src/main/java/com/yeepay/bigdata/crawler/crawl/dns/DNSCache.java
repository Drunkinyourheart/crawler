package com.yeepay.bigdata.crawler.crawl.dns;

import com.yeepay.bigdata.crawler.crawl.monitor.Dumpable;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class DNSCache implements Dumpable {

    static class DNSCacheEntry {

        public DNSCacheEntry(Object entry, long expiration){
            super();
            this.entry = entry;
            this.expiration = expiration;
        }

        Object entry;
        long   expiration;
    }

    public static final long                     NEGATIVE_TTL      = 60 * 60 * 1000;

    public static final long                     POSITIVE_TTL      = 24 * 60 * 60 * 1000;

    // 缓存时间
    private long                                 positiveTTL       = POSITIVE_TTL;
    private long                                 negativeTTL       = NEGATIVE_TTL;

    private ConcurrentMap<String, DNSCacheEntry> positiveCache     = new ConcurrentHashMap<String, DNSCacheEntry>();
    private ConcurrentMap<String, DNSCacheEntry> negativeCache     = new ConcurrentHashMap<String, DNSCacheEntry>();

    private ConcurrentLinkedQueue<String>        positiveCacheKeys = new ConcurrentLinkedQueue<String>();
    private ConcurrentLinkedQueue<String>        negativeCacheKeys = new ConcurrentLinkedQueue<String>();

    public DNSCache(){
        super();
    }

    public DNSCache(long positiveTTL, long negativeTTL){
        super();
        this.positiveTTL = positiveTTL;
        this.negativeTTL = negativeTTL;
    }

    public void put(String hostname, Object addresses, boolean success) {
        hostname = StringUtils.lowerCase(hostname);
        if (success) {
            DNSCacheEntry cacheEntry = new DNSCacheEntry(addresses, System.currentTimeMillis() + positiveTTL);
            positiveCacheKeys.add(hostname);
            positiveCache.putIfAbsent(hostname, cacheEntry);
        } else {
            DNSCacheEntry cacheEntry = new DNSCacheEntry(addresses, System.currentTimeMillis() + negativeTTL);
            negativeCacheKeys.add(hostname);
            negativeCache.putIfAbsent(hostname, cacheEntry);
        }
    }

    public Object get(String hostname) {
        hostname = StringUtils.lowerCase(hostname);
        DNSCacheEntry cacheEntry = positiveCache.get(hostname);
        if (cacheEntry == null) {
            cacheEntry = negativeCache.get(hostname);
        }
        if (cacheEntry != null) {
            return cacheEntry.entry;
        }
        return null;
    }

    // purge expiration cache
    private long  purgeTime = 30 * 60 * 1000;

    private Timer timer     = new Timer(DNSCache.class.getSimpleName(), true);

    {
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                purgePositive();
                purgeNegative();
            }
        }, purgeTime, purgeTime);
    }

    private void purgePositive() {
        Iterator<String> hostnameItr = positiveCacheKeys.iterator();
        long now = System.currentTimeMillis();
        while (hostnameItr.hasNext()) {
            String hostname = hostnameItr.next();
            DNSCacheEntry cacheEntry = positiveCache.get(hostname);
            if (cacheEntry.expiration > now) {
                break;
            }
            positiveCache.remove(hostname);
            hostnameItr.remove();
        }
    }

    private void purgeNegative() {
        Iterator<String> hostnameItr = negativeCacheKeys.iterator();
        long now = System.currentTimeMillis();
        while (hostnameItr.hasNext()) {
            String hostname = hostnameItr.next();
            DNSCacheEntry cacheEntry = negativeCache.get(hostname);
            if (cacheEntry.expiration > now) {
                break;
            }
            negativeCache.remove(hostname);
            hostnameItr.remove();
        }
    }

    @Override
    public String dump() {
        return null;
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        out.append(String.format("%-50s", ("positive dns cache : ")) + "[" + positiveCacheKeys.size() + "]").append("    " + System.getProperty("line.separator"));
        out.append(String.format("%-50s", ("negative dns cache : ")) + "[" + negativeCacheKeys.size() + "]").append("    " + System.getProperty("line.separator"));
    }

}
