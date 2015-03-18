package com.yeepay.bigdata.crawler.crawl.service;

import java.util.Map;

/**
 * Jerry
 */
public interface Handler {

    public String doGet(Map<String, String> map);

    public String doPost(Map<String, String> map, String content);
}
