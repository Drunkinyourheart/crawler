package com.yeepay.bigdata.crawler.manager.utils;

import com.yeepay.bigdata.crawler.manager.model.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * 类StatisticsLogger.java的实现描述：热闻日志输出
 * 
 */
public class HotNewsLogger {

	/**
	 * 用于统计的日志
	 */
	private static final Logger logger = Logger
			.getLogger(HotNewsLogger.class);

	public static void log(Task task) {
        if (isConfuseCode(task)) {
            logger.info("抓取的URL："+task.getFirstPageURL()+"=======>抽取结果content为："+task.getExtractData());
        }
    }
    public static void logFilter(Task task) {
        if (isConfuseCode(task)) {
            logger.info("抓取的URL："+task.getFirstPageURL()+"=======>入库前抽取结果content为："+task.getExtractData());
        }
    }
    public static void logSource(Task task) {
        if (isConfuseCode(task)) {
            logger.info("抓取的URL："+TaskUtils.getFirstPageURL(task)+"=======>初次抓取结果content为："+task.getCrawleData());
        }
    }

    /**
     * 乱码网站
     * @return
     */
    private static boolean isConfuseCode(Task task){
//http://www.chinairn.com/news/20140613/082842815.shtml
        if(StringUtils.isNotBlank(task.getFirstPageURL())&& task.getFirstPageURL().contains(".chinairn.com")){
            return true;
        }
        return false;
    }
}
