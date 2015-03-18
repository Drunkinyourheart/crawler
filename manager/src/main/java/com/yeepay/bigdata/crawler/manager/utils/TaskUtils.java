package com.yeepay.bigdata.crawler.manager.utils;

import com.yeepay.bigdata.crawler.manager.model.SeedInfoType;
import com.yeepay.bigdata.crawler.manager.model.Task;
import org.apache.commons.lang3.StringUtils;

public class TaskUtils {

    public static String getFirstPageURL(Task task) {
        if (task == null) {
            return "";
        }
        return StringUtils.isNotEmpty(task.getFirstPageURL()) ? task.getFirstPageURL() : "";
    }

    public static String getPageIndex(Task task) {
        if (task == null) {
            return "0";
        }
        if (task.getSeedType() == SeedInfoType.DETAIL) {
            return task.getPageIndex() + "";
        }
        return "0";
    }
}
