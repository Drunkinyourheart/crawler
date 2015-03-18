package com.yeepay.bigdata.crawler.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * 每一处都值得进一步改进。
 * 
 * @author zhuyudeng
 * 
 */
public class EC {

	List<List> firstDir = new ArrayList<List>();
	List<Integer> secondDir = new ArrayList<Integer>();

	int[] arr = new int[1000];

	public String extractArticle(String pageHtml) {
		// ------------------------------------------------------- step 1:预处理
		String page = new PreProcess().preProcess(pageHtml);
		Weigh w = new Weigh();
		Fat fat = new Fat();
		fat.init(page);
		w.initWeightParameters(page);

		// ------------------------------------------------------- step2:建立DOM
		Document doc = Jsoup.parse(page);

		/*
		 * Get all elements
		 */

		Elements allElements = doc.body().getAllElements();

		// Algorithm Description
		/*
		 * 实现归并 1. 计算每个结点的文本密度 （针对所有结点） 2. 使用并查集Union-Find Set 完成归并 （形成若干 森林结点 ）
		 * 3. 选择父节点最大的 4. 进一步区分，head， article， footer，广告位等等。 5. 可能会用到 最小公共祖先算法。
		 */

		List<Element> elementList = new ArrayList<Element>();
		int allDomNum = allElements.size();
		Map<Integer, Element> elementMap = new HashMap<Integer, Element>();

		/*
		 * step 1. calculate text density and collect element which is needed.
		 */

		List<Integer> curIdList = new ArrayList<Integer>();
		List<Integer> parentIdList = new ArrayList<Integer>();
		Map<Integer, Integer> sonParentMap = new HashMap<Integer, Integer>();

		for (int i = 0, len = allElements.size(); i < len - 2; ++i) {

			Element e = allElements.get(i);

			/*
			 * 每个元素保持了原有的顺序。目的在于为每个元素赋予一个 id，方便寻找继承关系。
			 */
			e.attr("id", "" + i);
			elementMap.put(i, e);

			/*
			 * 目的：选择可能的 元素。 1.calculate each node' text density 公式 ： 密度 = (1000
			 * * 标签数 / 字数); 2.a比例高的，去掉。
			 */
			if (e.isBlock() && e.text().length() > 50) { // if (e.isBlock() &&
															// !e.tagName().trim().equals("p")
															// &&
															// e.text().length()
															// > 50) {
				/*
				 * 注意这里的阈值 50 关于阈值的选择，保证了正文最起码大于50字，但同时，保证合并块后的正文块，这个值的大小很重要。
				 */
				// 2014 / 2 / 13
				/*
				 * 目的：去除 a 多的元素。链接块
				 */
				Elements es = e.getAllElements();
				int aNum = 0;
				int esLen = es.size();
				for (int j = 0; j < esLen; ++j) {
					Element child = es.get(j);
					if (child.tagName().equals("a")) {
						aNum++;
					}
				}
				if (esLen != 0 && aNum * 1.0 / esLen >= 0.5) {
					continue;
				}

				/*
				 * 目的：找到可能的文本块。
				 */
				// calculate density; if < 150, then add it.0
				int tNum = e.text().length();
				int tagNum = (int) ((int) e.getAllElements().size() * 2 * 0.9);
				double density = (1000.0 * tagNum) / tNum;

				if (density < 150.0) {

					elementList.add(e); // 通过标签密度，现在入选是符合文本要求的。

					/*
					 * collect id and parent id;
					 */
					String id = e.id().trim();
					Integer curId = Integer.parseInt(id);
					curIdList.add(curId);
					id = e.parent().id().trim();
					Integer parentId = -1;
					if (id.length() != 0) {
						parentId = Integer.parseInt(id);
					}
					parentIdList.add(parentId);

					/*
					 * 保留 子-父 的映射关系。
					 */
					sonParentMap.put(curId, parentId);
				}
			}
		}

		/*
		 * step 2. 数据块合并。 要求：拥有相同的父节点，同时2个结点相似性符合要求。 （1）2个块元素相似则归并，保存父节点。
		 * （2）2个块元素不相似，分别保存，
		 */
		Set<Integer> parentSet = new HashSet<Integer>();
		Set<Integer> maybeSet = new HashSet<Integer>();
		for (int i = 0, len = elementList.size(); i < len; ++i) {
			// COMMENT
			// System.out.println("cur    id : " + elementList.get(i).id() +
			// " ; " + curIdList.get(i));
			// System.out.println("parent id : " +
			// elementList.get(i).parent().id() + " ; " + parentIdList.get(i));
			// System.out.println(elementList.get(i).text());
			// System.out.println("==============================================");

			int curId = curIdList.get(i);
			int parentId = parentIdList.get(i);
			int num = 0;
			for (int j = 0; j < parentIdList.size(); ++j) {
				if (parentId == parentIdList.get(j))
					num++;
			}
			if (num > 1) {
				maybeSet.add(parentId);
				// save parent
			} else {
				// save self
				maybeSet.add(curId);
			}
			// parentSet.add(parentIdList.get(i));
		}

		Iterator<Integer> it = maybeSet.iterator();
		List<Integer> forSortList = new ArrayList<Integer>();
		int max = -1;
		while (it.hasNext()) {
			int tmp = it.next();
			forSortList.add(tmp);
			// System.out.println(" : " + tmp);

		}

		// COMMENT
		// System.out.println("aaaaaaaaaaaaaa");
		// Collections.sort(forSortList);
		// for (Integer x : forSortList) {
		// System.out.println(x);
		// }

		// COMMENT
		// System.out.println("?????????????????????????????????????????????????????");
		// System.out.println(forSortList.size());
		for (int i = forSortList.size() - 1; i >= 0; --i) {
			int id = forSortList.get(i); // get current node id.
			Element e = elementMap.get(id); // get node by id;
			// //////////////////////////////////////////////////////////////////////////
			String tmp = "-1";
			try {
				tmp = e.parent().id(); // get parent id;
			} catch (Exception eee) {
				continue;
			}
			// ///////////////////////////////////////////////////////////////////////////
			int parentId = -1;
			if (!tmp.equals("")) {
				parentId = Integer.parseInt(tmp);
			}

			arr[id] = parentId;
			// System.out.println("id : pid " + id + ":" + parentId);
		}

		for (int i = 0; i < 1000; ++i) {
			// System.out.println(i + ": " + arr[i]);
			if (arr[i] == -1) {
				List<Integer> list = new ArrayList<Integer>();
				list.add(i);
				firstDir.add(list);
				arr[i] = 0;
			}
			// System.out.println(i + " : " + arr[i]);
		}

		for (int i = 1000 - 1; i > 0; --i) {

			int father = arr[i];
			List<Integer> tmp = new ArrayList<Integer>();
			arr[i] = 0;
			// if (father != 0 && father != -1) {
			if (father != 0) {
				tmp.add(i);
				tmp.add(father);
				int x = father;
				father = arr[father];
				arr[x] = 0;
				// while (father != 0 && father != -1) {
				while (father != 0) {
					tmp.add(father);
					int k = father;
					father = arr[father];
					arr[k] = 0;
				}
			}
			if (tmp.size() > 0)
				firstDir.add(tmp);
		}

		// COMMENT
		List<Integer> numList = new ArrayList<Integer>();
		Set<Integer> numSet = new HashSet<Integer>();

//		System.out.println(firstDir.size());
		for (int i = 0; i < firstDir.size(); ++i) {
			List<Integer> list = firstDir.get(i);
			for (Integer x : list) {
				if (x == 0)
					continue;
				Element e = elementMap.get(x);
				numList.add(x);
//				System.out.println(x);
				// ------------------------------------------------
				Elements es = e.parents();
				for (Element element : es) {
					String id = element.id().trim();
					if (id.equals(""))
						continue;
					numSet.add(Integer.parseInt(id));
//					System.out.print(id + ",");
				}
				// ------------------------------------------------

				// System.out.println("v : " + w.getWeight(e));
				// System.out.println("v : " + fat.getWeight(e));
//				System.out.println(e.text());
				// System.out.println(e.outerHtml());
//				System.out.println("---------------------------------------");
				// break;
				// System.out.print(x + ", ");
			}
//			System.out.println();
		}
//		System.out.println(numList.toString());
		List<Integer> finalList = new ArrayList<Integer>();
		for (int i = 0; i < numList.size(); ++i) {
			Integer cur = numList.get(i);
			if (!numSet.contains(cur)) {
				finalList.add(cur);
//				System.out.println("cur : " + cur);
			}
		}
		double MM = 0L;
		int index = -1;
		Element e = null;
		for (int i = 0, len = finalList.size(); i < len; ++i) {
			int id = finalList.get(i);
			double v = fat.getWeight(elementMap.get(id));
			if (v > MM) {
				MM = v;
				index = id;
				e = elementMap.get(id);
			}
		}
//		System.out.println(e.text());
		// return null; // use strategy one.
		if (e == null) {
			System.out.println("ok");
			return new ExtractContent().extractArticle(page);
		} else {
			return e.outerHtml();
		}
		/*
		 * 从每个list找到祖先，然后计算丰富度。
		 */
		// double currentMax = 0;
		// int currentIndex = -1;
		// List<Integer> rsList = null;
		// for (int i = 0; i < firstDir.size(); ++i) {
		// List<Integer> list = firstDir.get(i);
		// double value = 0L;
		// /*
		// *
		// */
		// int index = list.get(list.size() - 1);
		// if (index == 0 || index == 1) {
		// if (list.size() > 3) {
		// index = list.get(list.size() - 2);
		// if (index == 1) {
		// index = list.get(list.size() - 3);
		// }
		// value = fat.getWeight(elementMap.get(index));
		// } else {
		// index = list.get(0);
		// value = fat.getWeight(elementMap.get(index));
		// }
		// } else {
		// value = fat.getWeight(elementMap.get(index));
		// }
		// if (value > currentMax) {
		// currentMax = value;
		// // currentIndex = index;
		// currentIndex = list.get(0);
		// rsList = list;
		// }
		// }
		// System.out.println("cuI" + currentIndex);
		// for (int i = 0; i < rsList.size(); ++i) {
		// System.out.println(elementMap.get(currentIndex));
		// }
		// System.out.println("fat : " + elementMap.get(currentIndex).text());
		// List<Element> last = new ArrayList<Element>();
		// for (int i = 0; i < firstDir.size(); ++i) {
		// List<Integer> list = firstDir.get(i);
		// int num = list.get(0);
		// // 判断，留还是去掉

		// if (num == 0)
		// continue;
		// if (num > allDomNum / 2)
		// continue;
		// // System.out.println(list.get(0));
		// Element e = elementMap.get(num);
		// Elements eP = e.parents();
		//
		//
		//
		// for (int j = 0; j < eP.size(); ++j) {
		// Element p = eP.get(j);
		// System.out.print(p.id() + ", ");
		// }
		// System.out.println();
		// last.add(e);
		// System.out.println("txt : " + list.get(0) + ": " + e.text().trim());
		// }
		//
		// -----------------------------------------------------------------------------
		// version 1
		// double maxValue = 0;
		// Element e = null;
		// for (int i = 0; i < last.size(); ++i) {
		// double v = w.getWeight(last.get(i));
		// System.out.println("value :" + v);
		// System.out.println(last.get(i).text());
		// if (v > maxValue) {
		// maxValue = v;
		// e = last.get(i);
		// }
		// }

		// -----------------------------------------------------------------------------
		// version 2
		// double maxValue = 0;
		// Element e = null;
		// for (int i = 0; i < last.size(); ++i) {
		// double v = fat.getWeight(last.get(i));
		// System.out.println("value :" + v);
		// System.out.println(last.get(i).text());
		// if (v > maxValue) {
		// maxValue = v;
		// e = last.get(i);
		// }
		// }
		// System.out.println("====================");
		// System.out.println(e.text());
		// System.out.println(allDomNum);
		// System.out.println(" : " + max);
		// return null;
		// for (int i = 0; i < elementList.size(); ++i) {
		// if (elementList.get(i).id().equals(String.valueOf(max)))
		// return elementList.get(i).text();
		// }
		// return null;

	}

	public static void main(String[] args) throws IOException {



        String cnt = Jsoup.connect("http://news.sina.com.cn/c/2014-11-04/080631090122.shtml").get().outerHtml();
        String rs = new EC().extractArticle(cnt);
        System.out.println("rs : " + rs);
    }

}
