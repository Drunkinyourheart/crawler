package com.yeepay.bigdata.crawler.crawl;

//import com.google.common.primitives.Bytes;
//import org.jerry.com.common.jqueue.JerryQueue;
//import sun.jvmstat.perfdata.monitor.PerfStringVariableMonitor;

import org.jerry.common.jqueue.JerryQueue;

/**
 * @author Jerry Deng
 * @date 12/16/14.
 */
public class A {


    public static void main(String[] args) throws Exception {
        JerryQueue jerryQueue = new JerryQueue();
        for (int i = 0; i < 10; ++i) {
            jerryQueue.offer(String.valueOf(i).getBytes());
        }
        System.out.println(jerryQueue.size());
        jerryQueue.close();
    }

}
