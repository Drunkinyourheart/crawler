package com.yeepay.bigdata.crawler.manager.model;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class TaskWrapper implements Delayed {

	private static final long ORIGIN = System.currentTimeMillis();

	private long delay; // milli second

	private Task task;
	
	private long traceDelay;

	/**
	 * Constructure
	 *
	 * @param delay
	 * @param unit
	 * @param task
	 */
	public TaskWrapper(long delay, TimeUnit unit, Task task) {
		this.delay = now() + unit.toMillis(delay);
		this.traceDelay = unit.toMillis(delay);
		this.task = task;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	@Override
	public int compareTo(Delayed o) {
		if (this == o) {
			return 0;
		}
		if (o == null) {
			return 1;
		}
		if (o instanceof TaskWrapper) {
			TaskWrapper t = (TaskWrapper) o;
			long diff = this.delay - t.delay;
			if (diff < 0) {
				return -1;
			} else if (diff > 0) {
				return 1;
			}
		}
		return 0;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(delay - now(), TimeUnit.MILLISECONDS);
	}
	
	public long getConsumeTime(){
		return traceDelay - getDelay(TimeUnit.MILLISECONDS);
	}

	private long now() {
		return System.currentTimeMillis()- ORIGIN;
	}
}
