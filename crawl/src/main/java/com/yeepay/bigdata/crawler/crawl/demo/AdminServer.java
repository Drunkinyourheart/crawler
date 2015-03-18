package com.yeepay.bigdata.crawler.crawl.demo;

import com.yeepay.bigdata.crawler.crawl.monitor.Monitor;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class AdminServer {
    public static void main(String[] args) {

        Monitor monitor = new Monitor();

        App p = new App();
        monitor.addMonitored(p);
        p.start();
        monitor.start();

        start(8080);
    }

    public static void start(int port) {
        // 配置服务器-使用java线程池作为解释线程
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        // 设置 pipeline factory.
        bootstrap.setPipelineFactory(new ServerPipelineFactory());
        // 绑定端口
        bootstrap.bind(new InetSocketAddress(port));
        System.out.println("admin start on " + port);
    }

}