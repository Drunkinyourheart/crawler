package com.yeepay.bigdata.crawler.manager.heartbeat;

import com.yeepay.bigdata.crawler.schedule.thrift.client.ExtractorClient;

public class ExtractorClientNode extends ClientNode<ExtractorClient> {

	@Override
	protected ExtractorClient createClient() {
		return new ExtractorClient(host, port);
	}

}
