package com.yeepay.bigdata.crawler.manager.utils;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 类MultiPageLock.java的实现描述：对于detail页面分页情况处理: 1.tryLock, pageIndex=1,nextURL!=null 2.freeLock, pageIndex>1,nextURL==null
 * 
 */
public class MultiPageLock {

    private static final int                           TTL              = 30 * 60 * 1000;

    private static final int                           RELIABLE_TIME    = 60 * 1000;

    private static final long                          ELAPSEPERIODTIME = 2 * 60 * 60 * 1000;

    // elapse time stamp
    private final ConcurrentHashMap<String, LockEntry> multiPageLock    = new ConcurrentHashMap<String, LockEntry>();

    private Queue<String>                              lockIds          = new ConcurrentLinkedQueue<String>();
    private long                                       firstElapseTime  = System.currentTimeMillis() + TTL;

    private Timer                                      timer            = new Timer(MultiPageLock.class.getName(), true);

    {
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (firstElapseTime < now) {// has elapse locks
                    for (String lockId : lockIds) {
                        LockEntry entry = multiPageLock.get(lockId);
                        if (entry == null) {// maybe concurrent modified
                            continue;
                        }
                        if (entry.timeStamp >= now) {// lock is valid
                            firstElapseTime = entry.timeStamp;
                            return;
                        }
                        // remove elapse lock
                        multiPageLock.remove(lockId);
                        lockIds.remove(lockId);//timeout unlock
                        MultiPageCacheManager.getMemCache().getCacheAndClear(lockId);//timeout multiPageCache unlock
                    }
                    // 所有项都过期
                    firstElapseTime = now;

                }
            }
        }, ELAPSEPERIODTIME, ELAPSEPERIODTIME);
    }

    public Pair tryLock(String firstPageID) {
        if (multiPageLock.contains(firstPageID)) { // pretest
            return new Pair(LockEntry.NULL, false);
        }
        LockEntry lock = multiPageLock.putIfAbsent(firstPageID, new LockEntry());
        if (lock == null) {// get lock
            lockIds.add(firstPageID);
            return new Pair(multiPageLock.get(firstPageID), true);
        }
        return new Pair(LockEntry.NULL, false);// fail to get lock
    }

    public int size(){
    	return lockIds.size();
    }
    public boolean freeLock(String firstPageID, LockEntry entry) {
        boolean free = multiPageLock.remove(firstPageID, entry);
        if (free) {
            lockIds.remove(firstPageID);
        }
        return free;
    }

    /**
     * compare same lock and reliable lock(失效时间在1分钟以上):冻结缓存数据
     * 
     * @param firstPageID
     * @param entry
     * @return
     */
    public boolean isSameLockAndReliable(String firstPageID, LockEntry entry) {
        return multiPageLock.replace(firstPageID, entry, entry) && entry.isReliable();
    }

    public static class Pair {

        LockEntry entry;
        boolean   lock;

        public Pair(LockEntry entry, boolean lock){
            super();
            this.entry = entry;
            this.lock = lock;
        }

        public LockEntry getEntry() {
            return entry;
        }

        public void setEntry(LockEntry entry) {
            this.entry = entry;
        }

        public boolean isLock() {
            return lock;
        }

        public void setLock(boolean lock) {
            this.lock = lock;
        }

    }

    public static class LockEntry {

        private static final AtomicInteger count     = new AtomicInteger();

        private static final LockEntry     NULL      = new LockEntry();

        private final int                  lockId    = count.getAndAdd(1);
        private final long                 timeStamp = System.currentTimeMillis() + TTL;

        public static LockEntry nextLockEntry() {
            return new LockEntry();
        }

        public boolean isNullLock() {
            return this == NULL;
        }

        boolean isReliable() {
            return this.timeStamp > System.currentTimeMillis() + RELIABLE_TIME;
        }
    }
}
