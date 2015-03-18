package com.yeepay.bigdata.crawler.schedule.thrift.client;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.thrift.transport.TTransport;

public abstract class BasicThriftClient<T extends TTransport> {

	private ThriftTransportPool<T> thriftTransportPool;

	public BasicThriftClient() {
		super();
	}

	public BasicThriftClient(ThriftTransportPool<T> tTransportPool) {
		super();
		this.thriftTransportPool = tTransportPool;
	}

	public BasicThriftClient(PoolableObjectFactory factory) {
		super();
		this.thriftTransportPool = new ThriftTransportPool<T>(
				new TTransportPoolConfig(), factory);
	}

	public ThriftTransportPool<T> getThriftTransportPool() {
		return thriftTransportPool;
	}

	public void setThriftTransportPool(ThriftTransportPool<T> tTransportPool) {
		this.thriftTransportPool = tTransportPool;
	}

}
