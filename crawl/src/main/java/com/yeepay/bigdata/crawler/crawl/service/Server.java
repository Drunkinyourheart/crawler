package com.yeepay.bigdata.crawler.crawl.service;

import java.io.IOException;

/**
 * Jerry
 */
public abstract class Server {
    public abstract void start() throws IOException;
    public abstract void stop();
}
