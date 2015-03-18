package com.yeepay.bigdata.crawler.schedule.thrift.client;

import com.yeepay.bigdata.rpc.PageService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;

public class PageClient extends BasicThriftClient<TSocket> implements PageService.Iface {

	public PageClient() {
		super();
	}

	public PageClient(String host, int port) {
		super(new ThriftTransportPool.TSocketFactory(host, port));
	}

	public PageClient(ThriftTransportPool<TSocket> tTransportPool) {
		super(tTransportPool);
	}

	@Override
	public boolean exists(String url) throws TException {
		TSocket transport = getThriftTransportPool().getResource();
		boolean broken = false;
		try {
			PageService.Iface client = new PageService.Client(new TBinaryProtocol(transport));
			boolean result = client.exists(url);
			return result;
		} catch (TException e) {
			broken = true;
			throw e;
		} finally {
			if (broken) {
				getThriftTransportPool().returnBrokenResourceObject(transport);
			} else {
				getThriftTransportPool().returnResource(transport);
			}
		}
	}

	@Override
	public boolean save(String url, String page) throws TException {
		TSocket transport = getThriftTransportPool().getResource();
		boolean broken = false;
		try {
			PageService.Iface client = new PageService.Client(new TBinaryProtocol(transport));
			boolean result = client.save(url, page);
			return result;
		} catch (TException e) {
			broken = true;
			throw e;
		} finally {
			if (broken) {
				getThriftTransportPool().returnBrokenResourceObject(transport);
			} else {
				getThriftTransportPool().returnResource(transport);
			}
		}
	}

	@Override
	public String getPage(String url) throws TException {
		TSocket transport = getThriftTransportPool().getResource();
		boolean broken = false;
		try {
			PageService.Iface client = new PageService.Client(new TBinaryProtocol(transport));
			String result = client.getPage(url);
			return result;
		} catch (TException e) {
			broken = true;
			throw e;
		} finally {
			if (broken) {
				getThriftTransportPool().returnBrokenResourceObject(transport);
			} else {
				getThriftTransportPool().returnResource(transport);
			}
		}
	}
}
