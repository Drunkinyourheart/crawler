package com.yeepay.bigdata.crawler.manager.worker;

import com.yeepay.bigdata.crawler.manager.heartbeat.ClientNodeSelector;
import com.yeepay.bigdata.crawler.manager.heartbeat.PageClientNode;
import com.yeepay.bigdata.crawler.manager.model.SeedInfo;
import com.yeepay.bigdata.crawler.schedule.thrift.client.PageClient;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class URLFilerWorker implements Runnable, Alive {
	private static final Logger logger = Logger.getLogger(URLFilerWorker.class);
	private BlockingQueue<SeedInfo> inputQueue;
	private ConcurrentLinkedQueue<SeedInfo> outpuQueue;
	private ClientNodeSelector<PageClientNode, PageClient> pageNodeSelector;
	private boolean isAlive = true;
	private UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" }, 4L);

	public URLFilerWorker(BlockingQueue<SeedInfo> inputQueue,
			ConcurrentLinkedQueue<SeedInfo> outpuQueue,
			ClientNodeSelector<PageClientNode, PageClient> pageNodeSelector) {
		this.inputQueue = inputQueue;
		this.outpuQueue = outpuQueue;
		this.pageNodeSelector = pageNodeSelector;
	}

	public void run() {
		while (this.isAlive) {
			SeedInfo info = inputQueue.poll();
			try {
				while (info != null && isAlive) {
					if (!this.urlValidator.isValid(info.getUrl())) {
						logger.info(String.format("invalid url : %s", new Object[] { info }));
					} else if (!filterURL(info)) {
						this.outpuQueue.add(info);
					}
					info = (SeedInfo) this.inputQueue.poll();
				}
				
				while(isAlive&& info==null){
					info = inputQueue.take();
				}
			} catch (Throwable e) {
				logger.error("invoke error: ", e);
				if (info != null) {
					this.inputQueue.add(info);
				}
				if ((e instanceof InterruptedException)) {
					this.isAlive = false;
				}
			}
		}
	}

	private boolean filterURL(SeedInfo info) throws TException {
		return ((PageClient) ((PageClientNode) this.pageNodeSelector
				.selectClientNode(info.getUrl())).getClient()).exists(info
				.getUrl());
	}

	public boolean isAlive() {
		return this.isAlive;
	}
}
