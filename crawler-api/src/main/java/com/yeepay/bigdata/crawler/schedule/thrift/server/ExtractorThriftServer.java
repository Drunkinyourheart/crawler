/**
 *
 */
package com.yeepay.bigdata.crawler.schedule.thrift.server;

import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtracteService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jerry
 *
 */
public class ExtractorThriftServer extends Server {

	protected final InetSocketAddress addr;

	private final TServer server;

	private final ExecutorService executorService;

	public ExtractorThriftServer(int port, ExtracteService.Iface handler)
			throws TTransportException {
		this(new InetSocketAddress(port), handler);
	}

	public ExtractorThriftServer(InetSocketAddress addr, ExtracteService.Iface handler)
			throws TTransportException {
		super();
		this.addr = addr;
		TNonblockingServerTransport transport = new TNonblockingServerSocket(
				addr);

		server = new TThreadedSelectorServer(new TThreadedSelectorServer.Args(
				transport).executorService(Executors.newCachedThreadPool())
				.processor(new ExtracteService.Processor<ExtracteService.Iface>(handler)));

		executorService = Executors.newFixedThreadPool(1);

	}

	@Override
	public void start() throws IOException {
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				server.serve();
			}
		});
	}

	@Override
	public void stop() {
		server.stop();
		executorService.shutdown();
	}

}
