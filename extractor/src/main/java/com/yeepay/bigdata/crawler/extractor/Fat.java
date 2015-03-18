package com.yeepay.bigdata.crawler.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Fat {
	int textNum = 0;
	int linkNum = 0;
	int punctuationNum = 0;
	int pTagNum = 0;
	int brTagNum = 0;
	int imgTagNum = 0;
	Document thisDoc;

	public Fat() {
		// TODO Auto-generated constructor stub
	}

	public void init(String pageHtml) {
		Document doc = Jsoup.parse(pageHtml);
		thisDoc = doc;
		String pageC = doc.text().toLowerCase();

		imgTagNum = doc.getElementsByTag("img").size();
		textNum = pageC.length();
		for (int i = 0, len = pageC.length() - 1; i < len; ++i) {
			String subPageContent = pageC.substring(i, i + 1);
			if (subPageContent.contains(",") || subPageContent.contains("，")
					|| subPageContent.contains(".")
					|| subPageContent.contains("。")
					|| subPageContent.contains("!")
					|| subPageContent.contains("！")
					|| subPageContent.contains("?")
					|| subPageContent.contains("?")
					|| subPageContent.contains("？")
					|| subPageContent.contains(":")
					|| subPageContent.contains("：")
					|| subPageContent.contains("'")
					|| subPageContent.contains("\"")
					|| subPageContent.contains("”")
					|| subPageContent.contains("”")) {
				punctuationNum++;
			}
		}

		Elements as = doc.getElementsByTag("a");
		for (int i = 0, len = as.size(); i < len; ++i) {
			if (!as.get(i).text().trim().equals("")) {
				linkNum++;
			}
		}
		pTagNum = doc.getElementsByTag("p").size();
		brTagNum = doc.getElementsByTag("br").size();
		if (pTagNum == 0)
			pTagNum = 1;
		if (brTagNum == 0)
			brTagNum = 1;
		// System.out.println("textNum" + textNum);
		// System.out.println("linkNum" + linkNum);
		// System.out.println("punctuationNum" + punctuationNum);
		// System.out.println("pTagNum" + pTagNum);
		// System.out.println("brTagNum" + brTagNum);
		// System.out.println("imgTagNum" + imgTagNum);
		// System.out.println();
	}

	public double getWeight(Element e) {

		String text = e.text().trim();
		
		if (text.contains("copyright") || text.contains("Copyright")
				|| text.contains("COPYRIGHT") || text.contains("版权所有")
				|| text.contains("免责声明")
				|| text.contains("均转载自其它媒体")) {
			return 0;
		}

		
		double value = 0.0;
		int imgNum = e.getElementsByTag("img").size();
		value += 100 * imgNum;
		int aNum = e.getElementsByTag("a").size();
		value += 100 * aNum;
		int txtNum = e.text().length();
		value += 100 * txtNum / textNum;
		double tagNum = e.getAllElements().size() * 0.9;
		value += tagNum;
//		System.out.println("value : " + value);
		return value;

	}

}
