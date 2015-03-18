package com.yeepay.bigdata.crawler.crawl.demo;

import com.yeepay.bigdata.crawler.crawl.monitor.Dumpable;

import java.io.IOException;

/**
 * Created by yp-tc-m-2505 on 11/5/14.
 */
public class App extends Thread implements Dumpable {
    @Override
    public String dump() {
        return null;
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException, InterruptedException {

        for (int i = 0; i < 1000; ++i) {
            out.append("i : " + i);
            Thread.sleep(1000 * 3);
        }
    }

    public void run() {
        for (int i = 0; i < 100000; ++i) {
            try {
                Thread.sleep(1000 * 5);
                System.out.println(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
