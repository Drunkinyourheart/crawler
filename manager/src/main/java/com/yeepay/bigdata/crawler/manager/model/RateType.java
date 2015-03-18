package com.yeepay.bigdata.crawler.manager.model;

public enum RateType {

	CRON("cron", "cron expression"), FIXEDRATE("rate", "按照固定频率执行爬取任务；毫秒单位的数字"), FIXEDDELAY(
			"delay", "固定延迟执行爬取任务；毫秒单位的数字"), IMMEDIATELY("immediately",
			"execute directly ; 用户抽取到的URL爬取");

	private RateType(String value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	private String value;

	private String desc;

	public String getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}

}
