package com.yeepay.bigdata.crawler.schedule.thrift.driver;

import com.yeepay.bigdata.crawler.schedule.thrift.server.ExtractorThriftServer;
import com.yeepay.bigdata.crawler.schedule.thrift.server.Server;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;

public class MainDriver {

	// private static ClassPathXmlApplicationContext context = null;

	/**
	 * @param args
	 * @throws org.apache.thrift.transport.TTransportException
	 * @throws java.io.IOException
	 */
	public static void main(String[] args) throws TTransportException,
			IOException {

		if (args.length < 1) {
			System.err.println("thrift port must be given");
			return;
		}

		int thriftPort = Integer.parseInt(args[0]);

		// int nettyPort = Integer.parseInt(args[1]);

		// context = new ClassPathXmlApplicationContext(
		// "classpath:spring/applicationContext.xml");

		// Iface handler = (Iface) context.getBean("documentConvertHandler");

		final Server server = new ExtractorThriftServer(thriftPort, null);
		server.start();

		// Handler nettyHandler= (Handler) context.getBean("defaultHandler");
		//
		// final Server nettyServer = new HttpServer(new InetSocketAddress(
		// nettyPort), nettyHandler);
		// nettyServer.start();

		/**
		 * 回调：删除zookeeper instance 实例,使用kill pid不要加-9 参数
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				server.stop();
			}

		});
	}

}
