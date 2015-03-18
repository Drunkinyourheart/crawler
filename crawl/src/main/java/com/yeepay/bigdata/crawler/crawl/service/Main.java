package com.yeepay.bigdata.crawler.crawl.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * @author Jerry Deng
 * @date 3/4/15.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Handler handler = new Handler() {
            @Override
            public String doGet(Map<String, String> map) {
                return doPost(map, null);
            }

            @Override
            public String doPost(Map<String, String> map, String content) {
                return null;
            }
        };
        final HttpServer httpServer = new HttpServer(new InetSocketAddress(8800), handler);
        httpServer.start();
    }

}
