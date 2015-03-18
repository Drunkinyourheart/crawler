package com.yeepay.bigdata.crawler.crawl.monitor;

import java.io.IOException;

public interface Dumpable {

    String dump();
    void dump(Appendable out, String indent) throws IOException, InterruptedException;

}