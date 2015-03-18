package com.yeepay.bigdata.crawler.crawl.utils;

import com.netflix.curator.x.discovery.ServiceType;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 类ZooDiscoveryServer.java的实现描述：TODO 类实现描述
 *
 */
public class ZooDiscoveryServerRegister {

    private static final Logger  LOGGER       = Logger.getLogger(ZooDiscoveryServerRegister.class);

    private DiscoveryServiceUtil discoveryServiceUtil;

    private static final String  SERVICE_NAME = "crawler";

    private static final String  basepath     = "/smc/services";

    private static final String  description  = "crawler node";

    private int                  port         = -1;

    private String               address      = null;

    public ZooDiscoveryServerRegister(int port,String zkAddress){
        try {
            if (port < 0) {
                throw new RuntimeException("zookeeper config error service port is " + port);
            }
            address = InetAddress.getLocalHost().getHostAddress();
            this.port = port;
            discoveryServiceUtil = new DiscoveryServiceUtil(ServiceType.DYNAMIC, basepath,zkAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException("zookeeper config error host address is " + address, e);
        }
    }

    public boolean register() {
        String zkenable = System.getProperty("zk.enable");
        if (StringUtils.isBlank(zkenable)||!zkenable.equals("true")) {
            return false;
        }
//        if (!address.startsWith("10.1.")&&!address.startsWith("127.0.")&&!address.startsWith("localhost")) {
            if (!address.startsWith("xxxxxx.")&&!address.startsWith("xxxxx.")&&!address.startsWith("xxxxxx")) {
            return discoveryServiceUtil.regist(SERVICE_NAME, address, port, description);
        }
        return false;
    }

    public boolean unregister() {
        boolean flag = discoveryServiceUtil.removeService(SERVICE_NAME, address, port);
        LOGGER.info("zookeeper remove gateway service : host:" + address + " port: " + port);
        return flag;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }

}
