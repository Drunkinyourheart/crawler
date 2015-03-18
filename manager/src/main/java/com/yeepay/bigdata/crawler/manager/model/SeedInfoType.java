package com.yeepay.bigdata.crawler.manager.model;

import org.apache.commons.lang3.StringUtils;

public enum SeedInfoType {

	HOTNEWSDETAIL("hotNewsDetail", null),DETAIL("detail", null), LIST("list", DETAIL), RSSLIST("rsslist", DETAIL), EPAPERLAYOUT(
			"epaperLayout", DETAIL), EPAPER("epaper", EPAPERLAYOUT);

	private String type;
	private SeedInfoType subType;

	private SeedInfoType(String type, SeedInfoType subType) {
		this.type = type;
		this.subType = subType;
	}

	public String getType() {
		return type;
	}

	public SeedInfoType getSubType() {
		return subType;
	}

	/**
	 * 返回调度爬取种子类型：默认返回DETAIL
	 * 
	 * @param type
	 * @return
	 */
	public static SeedInfoType getSeedType(String type) {
		for (SeedInfoType seedType : values()) {
			if (StringUtils.equalsIgnoreCase(type, seedType.type)) {
				return seedType;
			}
		}
		return DETAIL;

	}

	public static void main(String[] args) {
		System.out.println(SeedInfoType.DETAIL);
	}
}
