package com.yeepay.bigdata.crawler.manager.utils;

import org.apache.log4j.Logger;

/**
 * 统计日志输出
 * 
 */
public class StatisticsLogger {

	/**
	 * 用于统计的日志
	 */
	private static final Logger logger = Logger
			.getLogger(StatisticsLogger.class);

	public static void log(LogFormat format, String... args) {
		logger.info(String.format(format.getFormat(), args));
	}
}
