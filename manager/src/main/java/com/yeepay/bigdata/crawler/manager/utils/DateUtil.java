package com.yeepay.bigdata.crawler.manager.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static String default_format = "yyyy-MM-dd HH:mm:ss";

    public static String getDate2String(Date date, String format) {
        SimpleDateFormat sformat = new SimpleDateFormat(format);
        return sformat.format(date);
    }

    public static Date getDateFromString(String date) throws Exception {
        SimpleDateFormat sformat = new SimpleDateFormat(default_format);
        return sformat.parse(date);
    }

    public static Date getString2Date(String date, String format) throws Exception {
        SimpleDateFormat sformat = new SimpleDateFormat(format);
        return sformat.parse(date);
    }
}
