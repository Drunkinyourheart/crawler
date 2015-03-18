package com.yeepay.bigdata.crawler.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class XinMin {

	public XinMin() {
		// TODO Auto-generated constructor stub
	}

	public String extractArticle(String page) {
		Document doc = Jsoup.parse(page);
		Element e = doc.getElementById("MP_article");
		
		return e.outerHtml();
	}

}
