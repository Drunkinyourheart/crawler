package com.yeepay.bigdata.crawler.schedule.thrift.client;

import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtractTask;
import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtractTaskResponse;
import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtracteService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;

/**
 * @author Jerry
 * 
 */
public class ExtractorClient extends BasicThriftClient<TSocket> implements
        ExtracteService.Iface {

	public ExtractorClient() {
		super();
	}

	public ExtractorClient(String host, int port) {
		super(new ThriftTransportPool.TSocketFactory(host, port));
	}

	public ExtractorClient(ThriftTransportPool<TSocket> tTransportPool) {
		super(tTransportPool);
	}

	@Override
	public ExtractTaskResponse extract(ExtractTask task) throws TException {
		TSocket transport = getThriftTransportPool().getResource();
		boolean broken = false;
		try {
			ExtracteService.Iface client = new ExtracteService.Client(new TBinaryProtocol(transport));
			ExtractTaskResponse response = client.extract(task);
			return response;
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
