package com.yeepay.bigdata.crawler.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ExtractContent_Old {

	public String extractArticle(String pageHtml) {
		// ------------------------------------------------------- step 1:预处理
		String page = new PreProcess().preProcess(pageHtml);
		Weigh w = new Weigh();
		w.initWeightParameters(page);

		// ------------------------------------------------------- step2:建立DOM
		Document doc = Jsoup.parse(page);
		
		// 为 xinmin 特殊处理的
		if (page.contains("MP_article")) {
			
			return doc.getElementById("MP_article").outerHtml();
		}
		// wei xinmin teshuchuli end!
		Elements allElements = doc.body().getAllElements();

		List<Element> elementList = new ArrayList<Element>();

		int allDomNum = allElements.size();
		for (int i = 2, len = allElements.size(); i < len - 2; ++i) {
			Element e = allElements.get(i);
			e.attr("id", "" + i); // new
			if (e.isBlock() && !e.tagName().trim().equals("p")	&& e.text().length() > 50) {

				// calculate density; if < 150, then add it.
				int tNum = e.text().length();  // 字数
				int tagNum = (int) ((int)e.getAllElements().size() * 0.9);  // 标签数
				double density = (1000.0 * tagNum) / tNum;  // 公式 ： 密度  = (1000 * 标签数 / 字数);
				if (density < 150.0) {
					elementList.add(e);  // 通过标签密度，现在入选是符合文本要求的。
				}
			}
		}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
		/*
		 *找到符合要求的断点，将后面的footer去掉。
		 */
		// process 找到最大的差距dis，作为： a - b > dis
		int maxDistance = 0;
		List<Integer> distList = new ArrayList<Integer>();
		// use heap in another version............................................
		for (int i = 1; i < elementList.size(); ++i) {
			
			String a = elementList.get(i-1).id();
			int d1 = Integer.parseInt(a);
			String b = elementList.get(i).id();
			int d2 = Integer.parseInt(b);
			if (d2 > allDomNum / 3 * 2)
				break;
			if (d2 - d1 > maxDistance) {
				maxDistance = d2 - d1;
				distList.add(maxDistance);
//System.out.println("distance " + d2 + " : " + d1);
			}
		}
		
//System.out.println("all : " + allDomNum);
//System.out.println("mmm : " + maxDistance);
		maxDistance = maxDistance > 50 ? 50 : maxDistance;  // 如果大于 50 ， 以 50 为主。
		maxDistance = maxDistance < 20 ? 20 : maxDistance;
		
		int lastId = 0;
		List<Element> preparedList = new ArrayList<Element>();
		for (int i = 1; i < elementList.size(); ++i) {
			String lastStringId = elementList.get(i-1).id();
			int lastIntId = Integer.parseInt(lastStringId);
			String stringId = elementList.get(i).id();
			int intId = Integer.parseInt(stringId);
//System.out.println("preid : " + lastIntId);
//System.out.println("id : " + intId);
			if (intId - lastIntId >= maxDistance) {
				break;
			}
			preparedList.add(elementList.get(i));
		}

		
///////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// --------------------------------------
		
		double max = Double.MIN_VALUE;
		int index = -1;
		// ------------------------------------------------------- step 3:计算权值
		try {
			for (int i = 0, len = preparedList.size(); i < len; ++i) {

				double currentWeigthValue = 0;
				Element div = preparedList.get(i);
				if (div.text().trim().equals("")) {
					currentWeigthValue = Double.MIN_VALUE;
				} else {
					currentWeigthValue = w.getWeight(div);
				}

				if (currentWeigthValue > max) {
					max = currentWeigthValue;
					index = i;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (index == -1) {
			// System.out.println("error");
			return null;
		} else {
			// System.out.println(max);
			Element rs = preparedList.get(index);
			// System.out.println(rs.text());
			// return rs.text().trim();
			return rs.html().trim();
		}

	}

}
