package com.yeepay.bigdata.crawler.manager.scheduler;

import com.yeepay.bigdata.crawler.manager.heartbeat.ClientNodeSelectorFactory;
import com.yeepay.bigdata.crawler.manager.seed.SeedManager;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.Random;

public class SpiderScheduler {

	public static void main(String[] args) throws IOException, TTransportException {

		SeedProcessEngineConfig seedProcessEngineConfig = new SeedProcessEngineConfig();

//		seedProcessEngineConfig.setCrawlingWorkers(4);//assignCrawlWorker
		seedProcessEngineConfig.setCrawlingWorkers(1);//assignCrawlWorker
//		seedProcessEngineConfig.setExtractorWorkers(40);
		seedProcessEngineConfig.setExtractorWorkers(1);
//		seedProcessEngineConfig.setURLFilterWorkers(2);
		seedProcessEngineConfig.setURLFilterWorkers(1);
//		seedProcessEngineConfig.setTaskBuilderWorkers(2);
		seedProcessEngineConfig.setTaskBuilderWorkers(1);

		seedProcessEngineConfig.setCrawlerSelector(ClientNodeSelectorFactory.createCrawlerNodeSelector());
		seedProcessEngineConfig.setExtractorSelector(ClientNodeSelectorFactory.createExtractorNodeSelector());
		seedProcessEngineConfig.setPageSelector(ClientNodeSelectorFactory.createPageNodeSelector());

		final SeedProcessEngine seedProcessEngine = new SeedProcessEngine(seedProcessEngineConfig);

		final SeedManager seedManager = new SeedManager(seedProcessEngine);

		seedManager.start();

		System.out.println("-------------start ------------------------");
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				seedManager.stop();
			}
		});
	}
}
