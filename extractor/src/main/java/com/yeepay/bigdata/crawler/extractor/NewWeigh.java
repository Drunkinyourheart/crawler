package com.yeepay.bigdata.crawler.extractor;

import org.jsoup.nodes.Element;

public class NewWeigh {


	public double getWeigtht(Element aBlock) {
		double weight = 0.0;

		/* 计算标签密度 计算公式 : 标签密度 = 标签数 * 1000 / 文本数 */
		double tagDensity = 0L;

		int textNum = aBlock.text().length();
		int tagNum = getTagNum(aBlock);

		if (textNum == 0)
			return Double.MAX_VALUE;
		tagDensity = tagNum * 1000.0 / textNum;
		weight = tagDensity;
		return weight;
	}
	
	public int getTagNum(Element aBlock) {
		int tag = aBlock.getAllElements().size() * 2;
		return tag;
	}
	
}
