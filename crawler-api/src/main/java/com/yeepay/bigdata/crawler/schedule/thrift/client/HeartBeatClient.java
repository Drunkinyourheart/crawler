package com.yeepay.bigdata.crawler.schedule.thrift.client;

import com.yeepay.bigdata.crawler.heartbeat.thrift.service.HeartBeatParam;
import com.yeepay.bigdata.crawler.heartbeat.thrift.service.HeartBeatProtocal;
import com.yeepay.bigdata.crawler.heartbeat.thrift.service.HeartBeatResponse;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

/**
 * @author Jerry
 *
 */
public class HeartBeatClient implements HeartBeatProtocal.Iface {

	private HeartBeatProtocal.Iface client = null;

	public HeartBeatClient(String host, int port) {
		TFramedTransport transport = new TFramedTransport(new TSocket(host,
				port));
		TBinaryProtocol protocol = new TBinaryProtocol(transport);
		client = new HeartBeatProtocal.Client(protocol);
	}

	@Override
	public HeartBeatResponse heartbeatCheck(HeartBeatParam param)
			throws TException {
		return client.heartbeatCheck(param);
	}

}
