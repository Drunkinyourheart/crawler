package com.yeepay.bigdata.crawler.manager.heartbeat;

import com.yeepay.bigdata.crawler.schedule.thrift.client.BasicThriftClient;

/**
 * 分布式客户节点管理： CrawlerTaskAssignClient && ExtractorClient
 * 
 */
public abstract class ClientNode<T extends BasicThriftClient> {
	protected String host;
	protected int port;

	private long ts;// 更新timestamp

	protected T client;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public T getClient() {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					client = createClient();
				}
			}
		}
		return client;
	}

	protected abstract T createClient();
}
