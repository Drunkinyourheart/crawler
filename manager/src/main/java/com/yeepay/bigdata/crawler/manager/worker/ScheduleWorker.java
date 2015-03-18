package com.yeepay.bigdata.crawler.manager.worker;

import com.yeepay.bigdata.crawler.manager.model.SeedInfo;
import com.yeepay.bigdata.crawler.manager.scheduler.SeedProcessEngine;
import com.yeepay.bigdata.crawler.manager.utils.DomainUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class ScheduleWorker implements Runnable, Alive {

	private static final Logger 			logger = Logger.getLogger(ScheduleWorker.class);

	private ConcurrentLinkedQueue<SeedInfo> pageSeeds = null;

	private Map<String, Queue<SeedInfo>> 	taskMap = new HashMap<String, Queue<SeedInfo>>();

	private Set<SeedInfo> 					scheduleTasks = new HashSet<SeedInfo>();

	private Map<String, Long> 				domainStamps = new HashMap<String, Long>();

	private static final Long 				interval = 3000L;

	private boolean isAlive = false;

	private SeedProcessEngine seedProcessEngine;

	/**
	 *
	 * Constructure
	 *
	 * @param pageSeeds
	 * @param seedProcessEngine
	 */
	public ScheduleWorker(ConcurrentLinkedQueue<SeedInfo> pageSeeds,
							SeedProcessEngine seedProcessEngine) {
		this.pageSeeds = pageSeeds;
		this.seedProcessEngine = seedProcessEngine;
		this.isAlive = true;
	}

	@Override
	public void run() {
		while (isAlive) {
			try {
				long start = System.currentTimeMillis();
				transferSeed();
				long interval = 2 - (System.currentTimeMillis() - start);
				// 间隔时间
				if (interval < 0) {
					TimeUnit.MILLISECONDS.sleep(interval);
				}

				for (String domain : domainStamps.keySet()) {
					Long timestamp = System.currentTimeMillis();
					Long laststamp = domainStamps.get(domain);
					if (laststamp < timestamp - interval) {
						Queue<SeedInfo> queue = taskMap.get(domain);
						if (queue.isEmpty()) {
							continue;
						}
						SeedInfo seedInfo = queue.poll();
						seedProcessEngine.submitSeed(seedInfo);
						scheduleTasks.remove(seedInfo);
						domainStamps.put(domain, timestamp);
					}
				}
				// TimeUnit.MILLISECONDS.sleep(2);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				if (e instanceof InterruptedException) {
					isAlive = false;
				}
			}
		}
	}

	private void transferSeed() {
		while (!pageSeeds.isEmpty()) {
			SeedInfo seedInfo = pageSeeds.poll();

			if (seedInfo == null) {
				continue;
			}

			if (scheduleTasks.contains(seedInfo)) {
				continue;
			}
			String domain = DomainUtils.getTopPrivateDomain(seedInfo.getUrl());
			if (taskMap.containsKey(domain)) {
				taskMap.get(domain).offer(seedInfo);
			} else {
				Queue<SeedInfo> queue = new LinkedList<SeedInfo>();
				queue.offer(seedInfo);
				taskMap.put(domain, queue);
			}

			scheduleTasks.add(seedInfo);
			if (!domainStamps.containsKey(domain)) {
				domainStamps.put(domain, 0L);
			}
		}
	}

	@Override
	public boolean isAlive() {
		return isAlive;
	}

}
