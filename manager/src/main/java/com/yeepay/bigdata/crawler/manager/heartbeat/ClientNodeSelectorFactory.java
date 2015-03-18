package com.yeepay.bigdata.crawler.manager.heartbeat;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class ClientNodeSelectorFactory {

	private static final Logger logger = Logger.getLogger(ClientNodeSelectorFactory.class);

	private static final String CRAWLER_NODE_KEY = "crawler_node";

	private static final String EXTRACTOR_NODE_KEY = "extractor_node";

	private static final String PAGE_NODE_KEY = "page_node";

	private static CrawlerNodeSelector crawlerNodeSelector;

	private static ExtractorNodeSelector extractorNodeSelector;

	private static PageNodeSelector pageNodeSelector;

	static {
		try {
			init();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private static final void init() throws IOException {
		crawlerNodeSelector = new CrawlerNodeSelector();
		extractorNodeSelector = new ExtractorNodeSelector();
		pageNodeSelector = new PageNodeSelector();

		Properties properties = new Properties();
		properties.load(ClassLoader.getSystemResourceAsStream("111scheduler.properties"));
		String crawlerValue = (String) properties.get(CRAWLER_NODE_KEY);
		String extractorValue = (String) properties.get(EXTRACTOR_NODE_KEY);
		String pageValue = (String) properties.get(PAGE_NODE_KEY);

		if (StringUtils.isNotBlank(crawlerValue)) {
			String[] addresses = StringUtils.split(crawlerValue, ",");
			for (String address : addresses) {
				String[] hostParts = StringUtils.split(address, ":");
				String host = hostParts[0];
				String port = hostParts[1];
				crawlerNodeSelector.addClientNode(createCrawlerNode(host, Integer.parseInt(port)));
			}
		}

		if (StringUtils.isNotBlank(extractorValue)) {
			String[] addresses = StringUtils.split(extractorValue, ",");
			for (String address : addresses) {
				String[] hostParts = StringUtils.split(address, ":");
				String host = hostParts[0];
				String port = hostParts[1];
				extractorNodeSelector.addClientNode(createExtractorNode(host, Integer.parseInt(port)));
			}
		}

		if (StringUtils.isNotBlank(pageValue)) {
			String[] addresses = StringUtils.split(pageValue, ",");
			for (String address : addresses) {
				String[] hostParts = StringUtils.split(address, ":");
				String host = hostParts[0];
				String port = hostParts[1];
				pageNodeSelector.addClientNode(createPageClientNode(host,
						Integer.parseInt(port)));
			}
		}
	}

	private static final CrawlerAssignClientNode createCrawlerNode(String host, int port) {
		CrawlerAssignClientNode node = new CrawlerAssignClientNode();
		node.setHost(host);
		node.setPort(port);
		node.createClient();
		return node;
	}

	private static final ExtractorClientNode createExtractorNode(String host, int port) {
		ExtractorClientNode node = new ExtractorClientNode();
		node.setHost(host);
		node.setPort(port);
		node.createClient();
		return node;
	}

	private static final PageClientNode createPageClientNode(String host,
			int port) {
		PageClientNode node = new PageClientNode();
		node.setHost(host);
		node.setPort(port);
		node.createClient();
		return node;
	}

	public static CrawlerNodeSelector createCrawlerNodeSelector() {
		return crawlerNodeSelector;
	}

	public static ExtractorNodeSelector createExtractorNodeSelector() {
		return extractorNodeSelector;
	}

	public static PageNodeSelector createPageNodeSelector() {
		return pageNodeSelector;
	}

}
