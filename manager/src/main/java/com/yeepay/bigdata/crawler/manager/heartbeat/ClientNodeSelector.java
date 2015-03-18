package com.yeepay.bigdata.crawler.manager.heartbeat;

import com.yeepay.bigdata.crawler.schedule.thrift.client.BasicThriftClient;

/**
 * @param <K>
 * @param <T>
 *
 */
public interface ClientNodeSelector<K extends ClientNode<T>, T extends BasicThriftClient> {

	public K selectClientNode(String domain);

	public void addClientNode(K clientNode);
}
