package com.yeepay.bigdata.crawler.manager.scheduler;

import com.yeepay.bigdata.crawler.manager.heartbeat.ClientNodeSelector;
import com.yeepay.bigdata.crawler.manager.heartbeat.CrawlerAssignClientNode;
import com.yeepay.bigdata.crawler.manager.heartbeat.ExtractorClientNode;
import com.yeepay.bigdata.crawler.manager.heartbeat.PageClientNode;
import com.yeepay.bigdata.crawler.schedule.thrift.client.CrawlerAssignTaskClient;
import com.yeepay.bigdata.crawler.schedule.thrift.client.ExtractorClient;
import com.yeepay.bigdata.crawler.schedule.thrift.client.PageClient;

public class SeedProcessEngineConfig {

	public static final int DEFAULT_TASK_BUILDER_WORKDER_SIZE = 5;

	public static final int DEFAULT_TASK_CRAWLING_WORKDER_SIZE = 5 * 2;

	public static final int DEFAULT_TASK_EXTRACTOR_WORKDER_SIZE = 5;

	public static final int DEFAULT_URL_FILTER_WORKDER_SIZE = 1;

	public static final int DEFAULT_TIMEOUT_WORKDER_SIZE = 1;

	public static final int DEFAULT_SERVER_PORT = 6080;
//	public static final int DEFAULT_SERVER_PORT = 8999;

	public static final int DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILL = 10000;

	private long timeBetweenEvictionRunsMills = DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILL;

	private int taskBuilderWorkers;

	private int crawlingWorkers;

	private int extractorWorkers;

	private int URLFilterWorkers;

	private int timeoutWorkers;

	private int serverPort;

	private ClientNodeSelector<CrawlerAssignClientNode, CrawlerAssignTaskClient> crawlerSelector;

	private ClientNodeSelector<ExtractorClientNode, ExtractorClient> extractorSelector;

	private ClientNodeSelector<PageClientNode, PageClient> pageSelector;

	/** Contructure 1 */
	public SeedProcessEngineConfig() {
		this(DEFAULT_TASK_BUILDER_WORKDER_SIZE,
				DEFAULT_TASK_CRAWLING_WORKDER_SIZE,
				DEFAULT_TASK_EXTRACTOR_WORKDER_SIZE,
				DEFAULT_URL_FILTER_WORKDER_SIZE, DEFAULT_TIMEOUT_WORKDER_SIZE,
				DEFAULT_SERVER_PORT);
	}

	/** Contructure 2 */
	public SeedProcessEngineConfig(int taskBuilderWorkers, int crawlingWorkers,
			int extractorWorkers, int urlFilterWorkers, int timeoutWorkers,
			int serverPort) {
		this.taskBuilderWorkers = taskBuilderWorkers;
		this.crawlingWorkers = crawlingWorkers;
		this.extractorWorkers = extractorWorkers;
		this.URLFilterWorkers = urlFilterWorkers;
		this.timeoutWorkers = timeoutWorkers;
		this.serverPort = serverPort;
	}

	public int getTaskBuilderWorkers() {
		return taskBuilderWorkers;
	}

	public void setTaskBuilderWorkers(int taskBuilderWorkers) {
		this.taskBuilderWorkers = taskBuilderWorkers;
	}

	public int getCrawlingWorkers() {
		return crawlingWorkers;
	}

	public void setCrawlingWorkers(int crawlingWorkers) {
		this.crawlingWorkers = crawlingWorkers;
	}

	public int getExtractorWorkers() {
		return extractorWorkers;
	}

	public void setExtractorWorkers(int extractorWorkers) {
		this.extractorWorkers = extractorWorkers;
	}

	public int getTimeoutWorkers() {
		return timeoutWorkers;
	}

	public void setTimeoutWorkers(int timeoutWorkers) {
		this.timeoutWorkers = timeoutWorkers;
	}

	public ClientNodeSelector<CrawlerAssignClientNode, CrawlerAssignTaskClient> getCrawlerSelector() {
		return crawlerSelector;
	}

	public void setCrawlerSelector(
			ClientNodeSelector<CrawlerAssignClientNode, CrawlerAssignTaskClient> crawlerSelector) {
		this.crawlerSelector = crawlerSelector;
	}

	public ClientNodeSelector<ExtractorClientNode, ExtractorClient> getExtractorSelector() {
		return extractorSelector;
	}

	public void setExtractorSelector(
			ClientNodeSelector<ExtractorClientNode, ExtractorClient> extractorSelector) {
		this.extractorSelector = extractorSelector;
	}

	public ClientNodeSelector<PageClientNode, PageClient> getPageSelector() {
		return pageSelector;
	}

	public void setPageSelector(
			ClientNodeSelector<PageClientNode, PageClient> pageSelector) {
		this.pageSelector = pageSelector;
	}

	public long getTimeBetweenEvictionRunsMills() {
		return timeBetweenEvictionRunsMills;
	}

	public void setTimeBetweenEvictionRunsMills(
			long timeBetweenEvictionRunsMills) {
		this.timeBetweenEvictionRunsMills = timeBetweenEvictionRunsMills;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getURLFilterWorkers() {
		return this.URLFilterWorkers;
	}

	public void setURLFilterWorkers(int urlFilterWorkers) {
		this.URLFilterWorkers = urlFilterWorkers;
	}

}
