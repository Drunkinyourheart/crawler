package com.yeepay.bigdata.crawler.manager.selector;

import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtractResultType;
import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtractTask;
import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtractTaskResponse;
import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtracteService;
import com.yeepay.bigdata.crawler.schedule.thrift.Response;
import com.yeepay.bigdata.crawler.schedule.thrift.ResponseStatus;
import com.yeepay.bigdata.crawler.schedule.thrift.server.ExtractorThriftServer;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;

public class ExtractorServer {

	/**
	 * @param args
	 * @throws org.apache.thrift.transport.TTransportException
	 * @throws java.io.IOException
	 */
	public static void main(String[] args) throws TTransportException,
			IOException {
		ExtractorThriftServer server = new ExtractorThriftServer(9090,
				new ExtracteService.Iface() {

					@Override
					public ExtractTaskResponse extract(ExtractTask task)
							throws TException {
						System.out.println(task);
						return new ExtractTaskResponse(new Response(
								task.getId(), ResponseStatus.Success),
								task.url, "hello", ExtractResultType.PAGE);
					}
				});
		server.start();
	}

}
