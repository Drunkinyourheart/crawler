package com.yeepay.bigdata.crawler.manager.utils;

import com.yeepay.bigdata.crawler.manager.constants.SchedulerConstants;
import com.yeepay.bigdata.crawler.manager.heartbeat.ClientNodeSelectorFactory;
import com.yeepay.bigdata.crawler.manager.model.Task;
import com.yeepay.bigdata.crawler.schedule.thrift.client.PageClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

public class PageClientUtils {
    private static final Logger logger = Logger.getLogger(PageClientUtils.class);

    public static boolean filterURL(String url) throws TException {
        return ClientNodeSelectorFactory.createPageNodeSelector().selectClientNode(DomainUtils.getTopPrivateDomain(url)).getClient().exists(url);
    }

    /**
     * 功能：如果Leveldb不存在url,
     *
     */
    public static boolean filterURL(String url, Task task) throws TException {
        PageClient pageClient = ClientNodeSelectorFactory.createPageNodeSelector().selectClientNode(DomainUtils.getTopPrivateDomain(url)).getClient();
        boolean flag;
        String publicationName = task.getCtxMap().get("publicationName");

        if (publicationName == null || publicationName.equals("")) {//判断是外网
            flag = pageClient.exists(url);
            if (!flag) {//内网有，外网不入抓取队列
                flag = pageClient.exists(SchedulerConstants.NEWSLEVEL_PREFIX + url);
            }
        } else {//判断是内网
            flag = pageClient.exists(SchedulerConstants.NEWSLEVEL_PREFIX + url);
            if (!flag) {
                logger.info("the inner url of " + SchedulerConstants.NEWSLEVEL_PREFIX + url + "  is coming--");
            }
        }
        return flag;
    }
    /**
     * old : don't distinguish the way of crawler ,inner or Internet
     * @param task
     * @throws org.apache.thrift.TException
     */
//    public static void savePage(Task task) throws TException {
//    	ClientNodeSelectorFactory.createPageNodeSelector().selectClientNode(task.getDomain()).getClient().save(task.getUrl(),
//                                                                                                               task.getCrawleData());
//
//    }

    /**
     * now:distinguish inner or Internet
     *
     * @param task
     * @throws org.apache.thrift.TException
     */
    public static void savePage(Task task) throws TException {
        PageClient pageClient = ClientNodeSelectorFactory.createPageNodeSelector().selectClientNode(task.getDomain()).getClient();
        String publicationName = task.getCtxMap().get("publicationName");
        if (publicationName == null || publicationName.equals("")) {//判断是外网
            pageClient.save(task.getUrl(), task.getCrawleData());
        } else {//判断是内网
            pageClient.save(SchedulerConstants.NEWSLEVEL_PREFIX + task.getUrl(), task.getCrawleData());
        }
    }


    public static String getPage(String url) throws TException {
        String page = ClientNodeSelectorFactory.createPageNodeSelector().selectClientNode(DomainUtils.getTopPrivateDomain(url)).getClient().getPage(url);
        return page;

    }

    public static void main(String[] args) throws TException {
        System.out.println(filterURL("http://gd.sina.com.cn/news/b/2014-07-29/0736115929.html"));
    }

}
