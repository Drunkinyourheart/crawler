package com.yeepay.bigdata.crawler.manager.utils;

import org.apache.log4j.Logger;

public class MonitorSenderDriverLogger {
    /**
     * 用于统计的日志
     */
    private static final Logger logger = Logger
            .getLogger(MonitorSenderDriverLogger.class);

    public static void log(String logContent) {
        logger.info(logContent);
    }
}
