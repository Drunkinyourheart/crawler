package com.yeepay.bigdata.crawler.schedule.thrift.server;

import java.io.IOException;

public abstract class Server {
	public abstract void start() throws IOException;

	public abstract void stop();
}
