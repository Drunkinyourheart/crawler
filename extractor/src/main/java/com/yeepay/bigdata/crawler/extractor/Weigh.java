package com.yeepay.bigdata.crawler.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Weigh {

	int textNum = 0;
	int linkNum = 0;
	int punctuationNum = 0;
	int pTagNum = 0;
	int brTagNum = 0;
	int imgTagNum = 0;
	Document thisDoc;

	// ----------------------------------------------------------- 初始化 全文参数
	public void initWeightParameters(String fullPageHtmlString) {
		Document doc = Jsoup.parse(fullPageHtmlString);
		thisDoc = doc;
		String pageC = doc.text();

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

	public double getWeight(Element divDom) {
//System.out.println("p name : " + divDom.parent().tagName() + "; id : " + divDom.parent().id());
//System.out.println("p name : " + divDom.parent().tagName() + "; cur id : " + divDom.id());
//System.out.println("parent id : " + divDom.parent().id());

//System.out.println(divDom.text().substring(0, 100));
//System.out.println(divDom.text());  // .substring(0, 100)
//System.out.println("==============");
		double weight = 100.0;

		String pureText = divDom.text();
		// String divHtml = divDom.html();
		/* 修改于2014/2/8 */
		String divHtml = divDom.outerHtml();
		Document doc = Jsoup.parse(divHtml);
		String domContent = doc.text();

		// -------------------------------------------------------------------------
		int imgNum = doc.getElementsByTag("img").size();
		if (imgTagNum != 0) {
			weight = weight + ((double) imgNum / imgTagNum) * 100; // 图片标签密度
		}
		// -------------------------------------------------------------------------

		int pNum = doc.getElementsByTag("p").size();
		if (pTagNum != 0) {
			weight = weight + ((double) pNum / pTagNum) * 50; // p标签密度
		}

		int brNum = doc.getElementsByTag("br").size();
		if (brTagNum != 0) {
			weight = weight + ((double) brNum / brTagNum) * 50; // br标签密度
		}

		// ------------------------------------------------------------------------------------
		int htmllen = divDom.outerHtml().length();
		int textLen = domContent.length(); // 100
		weight = weight + ((double) textLen / htmllen) * 200; // 计算标签密度,
																// 主要用于区分包含标题和文本的dom和文本的dom。

		// ---------------------------------------------------------------------------------
		Elements as = doc.getElementsByTag("a");
		int currentA = as.size();
		weight = weight - ((double) currentA / linkNum) * 100; // 计算链接密度 链接
		for (int i = 0, len = as.size(); i < len; ++i) {
			Element a = as.get(i);
			if (a.attr("href").contains("#"))
				weight = weight - 20;
		}
		// -----------------------------------------------------------------------------------------
		int text = domContent.length();
		weight = weight + ((double) text / textNum) * 200; // 计算文本密度

		// -----------------------------------------------------------------------------------------------
		int punctuation = 0;
		for (int i = 0; i < domContent.length() - 1; ++i) {
			String subPageContent = domContent.substring(i, i + 1);
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
				punctuation++;
			}
		}
		weight = weight + ((double) punctuation / punctuationNum) * 200; // 150
		weight = weight + ((double) punctuation / divDom.outerHtml().length())
				* 200; // 150
		if (domContent.contains("ICP") || domContent.contains("Copyright"))
			return 0;
		if (domContent.contains("上一篇") || domContent.contains("上一页")
				|| domContent.contains("下一篇") || domContent.contains("下一页"))
			return 0;
		if (domContent.contains("版权声明"))
			return 0;

		// ----------------------------------------------------------------------------------------------------------------------------------
		if (doc.getElementsByTag("p").size() == 0
				&& doc.getElementsByTag("tr").size() == 0) // 目的在于区分摘要和正文，很有用。
			weight = weight - 50;

		// 2014/2/7
		double tmpWeight = new NewWeigh().getWeigtht(divDom);
		if (tmpWeight < 100) {
			weight += 100;
			// System.out.println(divDom.outerHtml());
		}
		// System.out.println(tmpWeight);
		// ----------------------------- 新版本开发的代码均在下面添加代码。
		if (pureText.length() < 10) {
			weight = weight - 50;
		}
		String title = null;
		try {
			Elements titleE = thisDoc.getElementsByTag("title");
			if (titleE != null && titleE.size() != 0) {
				title = titleE.get(0).text();
			}

			if (title != null) {
				title = title.substring(0, title.length() / 2);
				if (domContent.contains(title)) {
					weight -= 100;
				}
			}

			// original
			// title = thisDoc.head().child(0).text();
			// title = title.substring(0, title.length() / 2);
			// if (domContent.contains(title)) {
			// weight = 100;
			// }
			// System.out.println("title : " + title);

		} catch (Exception e) {
			e.printStackTrace();
			// System.out.println("============================================");
		}
		// -------------------------------------------------------------- 2014 / 2/ 17
		String REGEX2 = "[0-9]{4}[年|\\-|/][0-9]{1,2}[月|\\-|/][0-9]{1,2}[日|\\s?][\\s]*\\d{1,2}:\\d{1,2}(:\\d{1,2})?";

		Pattern p = Pattern.compile(REGEX2);
		Matcher mm = p.matcher(pureText);
		if (mm.find()) {
			weight -= 100;
		}
		// 新版本的代码在上面添加
//System.out.println(weight);
		// System.out.println(divDom.text());
		return weight + 0L;
	}

	public double getWeight_New(Element divDom) {

		double weight = 100.0;

		String pureText = divDom.text();
		String divHtml = divDom.outerHtml();
		Document doc = Jsoup.parse(divHtml);
		int htmllen = divDom.outerHtml().length();
		int textLen = pureText.length(); // 100

		double tmpWeight = new NewWeigh().getWeigtht(divDom);  // 可能包含正文块的 DOM 结点 突显出来。
		if (tmpWeight < 100) {
			weight += 100;
		}

		weight = weight + ((double) textLen / htmllen) * 100; // 计算标签密度, 主要用于区分包含标题和文本的dom和文本的dom。

		// -------------------------------------------------------------------------
		int imgNum = doc.getElementsByTag("img").size();
		if (imgTagNum != 0) {
			weight = weight + ((double) imgNum / imgTagNum) * 100; // 图片标签密度
		}
		// -------------------------------------------------------------------------

		int pNum = doc.getElementsByTag("p").size();
		if (pTagNum != 0) {
			weight = weight + ((double) pNum / pTagNum) * 100; // p标签密度
		}

		int brNum = doc.getElementsByTag("br").size();
		if (brTagNum != 0) {
			weight = weight + ((double) brNum / brTagNum) * 100; // br标签密度
		}

		// ---------------------------------------------------------------------------------
		Elements as = doc.getElementsByTag("a");
		int currentA = as.size();
		weight = weight - ((double) currentA / linkNum) * 100; // 计算链接密度 链接
		for (int i = 0, len = as.size(); i < len; ++i) {
			Element a = as.get(i);
			if (a.attr("href").contains("#"))
				weight = weight - 20;
		}
		// -----------------------------------------------------------------------------------------
		int text = pureText.length();
		weight = weight + ((double) text / textNum) * 100; // 计算文本密度

		// -----------------------------------------------------------------------------------------------
		int punctuation = 0;
		for (int i = 0; i < pureText.length() - 1; ++i) {
			String subPageContent = pureText.substring(i, i + 1);
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
				punctuation++;
			}
		}
		weight = weight + ((double) punctuation / punctuationNum) * 200; // 150
		weight = weight + ((double) punctuation / divDom.outerHtml().length())
				* 200; // 150
		if (pureText.contains("ICP") || pureText.contains("Copyright"))
			return 0;
		if (pureText.contains("上一篇") || pureText.contains("上一页")
				|| pureText.contains("下一篇") || pureText.contains("下一页"))
			return 0;
		if (pureText.contains("版权声明"))
			return 0;

		// ----------------------------------------------------------------------------------------------------------------------------------
		if (doc.getElementsByTag("p").size() == 0
				&& doc.getElementsByTag("tr").size() == 0) // 目的在于区分摘要和正文，很有用。
			weight = weight - 50;

		// System.out.println(tmpWeight);
		// ----------------------------- 新版本开发的代码均在下面添加代码。
		if (pureText.length() < 10) {
			weight = weight - 50;
		}
		String title = null;
		try {
			Elements titleE = thisDoc.getElementsByTag("title");
			if (titleE != null && titleE.size() != 0) {
				title = titleE.get(0).text();
			}

			if (title != null) {
				title = title.substring(0, title.length() / 2);
				if (pureText.contains(title)) {
					weight -= 100;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			// System.out.println("============================================");
		}
		// --------------------------------------------------------------

		// 新版本的代码在上面添加
		// System.out.println(weight);
		// System.out.println(divDom.text());
		return weight + 0L;
	}

	public void getContent(String url) {
		// String page = new ExtractPage().extractPage(url);
		// String text = justDoIt(page);
		// System.out.println(text);
	}

}
