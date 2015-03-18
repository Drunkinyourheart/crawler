package com.yeepay.bigdata.crawler.extractor;

import java.util.regex.Pattern;

public class PreProcess {

	private static int FREQUENT_URL = 30;
	private static Pattern links = Pattern
			.compile(
					"<[aA]\\s+[Hh][Rr][Ee][Ff]=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</a>(\\s*.{0,"
							+ FREQUENT_URL
							+ "}\\s*<a\\s+href=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</[aA]>){2,100}",
					Pattern.DOTALL);

	// ---------------------------------------------- 主要处理函数

	public String preProcess(String source) {

		source = source.replaceAll("(?is)<!DOCTYPE.*?>", "");
		source = source.replaceAll("(?is)<!--.*?-->", "");
		source = source.replaceAll("(?is)<!-.*?->", "");
		source = source.replaceAll("<script.*?>.*?</script>", "");
		source = source.replaceAll("(?is)<script.*?>.*?</script>", "");
		source = source.replaceAll("(?is)<style.*?>.*?</style>", "");
		source = source.replaceAll("&.{2,5};|&#.{2,5};", " ");

		// --------------------------------------------------------------------------------------------------
		// source = source.replaceAll("(?is)<meta.*?/?.*?>", "");
		source = source.replaceAll("(?is)<meta.*?/?.*?>", "");
		source = source.replaceAll("(?is)<base.*?/?.*?>", "");
		source = source.replaceAll("(?is)<link.*?/?.*?>", "");
		source = source.replaceAll("(?is)<input.*?/?.*?>", "");
		source = source.replaceAll("(?is)<param.*?/?.*?>", "");
		source = source.replaceAll("(?is)<object.*?/?.*?>", "");
		source = source.replaceAll("(?is)<textarea.*?/?.*?>", "");
		source = source.replaceAll("<br.*?/?>", "");
		// care for order of how to deal with html. <a href="#"
		// source = source.replaceAll("<a.*?href=['|\"]#['|\"].*?>.*?(</a>)?",
		// "");
		source = source.replaceAll("<li.*?>.*?(</li>)?", "");
		source = source.replaceAll("<li.*?>.*?", "");
		source = source.replaceAll("(?is)<li.*?>.*?</li>", "");
		
		// System.out.println(source);
		source = source.replaceAll("<ul.*?>.*?</ul>", "");
		source = source.replaceAll("(?is)<ul.*?>.*?</ul>", "");

		source = source.replaceAll("<label.*?>.*?</label>", "");
		source = source.replaceAll("<button.*?>.*?</button>", "");
//		source = source.replaceAll("<em.*?>.*?</em>", "");

		source = source.replaceAll("<iframe.*?>.*?</iframe>", ""); // 正文可能嵌套
		source = source.replaceAll("(?is)<iframe.*?>.*?</iframe>", "");

		source = source.replaceAll("<form.*?>.*?</form>", "");
		source = source.replaceAll("(?is)<form.*?>.*?</form>", "");
//		 source = source.replaceAll("<span.*?>.*?</span>", "");
//		 source = source.replaceAll("(?is)<span.*?>.*?</span>", "");

//		source = source.replaceAll("<span.*?></span>", "");
//		source = source.replaceAll("(?is)<span.*?>.*?</span>", "");

		// clear page once more.
		// source = source.replaceAll("(?is)<div.*?>//s*</div>", "");
		// source = source.replaceAll("<br.*?>.*?</br>", "");
		// source = source.replaceAll("<meta.*?/>", "");
		// source = clearSourceCloseClose(source, "textarea");
		// source = source.replaceAll("(?is)<ul.*?>.*?</ul>", ""); // remove
		// -----------------------------------------------------------------------------------------------------

		// 剔除连续成片的超链接文本（认为是，广告或噪音）,超链接多藏于span中
		source = source.replaceAll("<[sS][pP][aA][nN].*?>", "");
		source = source.replaceAll("</[sS][pP][aA][nN]>", "");

		int len = source.length();
		// while ((source = links.matcher(source).replaceAll("")).length() !=
		// len) {
		// len = source.length();
		// }
		// source = links.matcher(source).replaceAll(""); // 不要动

		// 防止html中在<>中包括大于号的判断
		// source = source.replaceAll("<[^>'\"]*['\"].*['\"].*?>", "");

		// source = source.replaceAll("<.*?>", "");
		// source = source.replaceAll("<.*?>", "");
		source = source.replaceAll("\r\n", "\n");

		// --------------------------------------------------------------------------------------------------
		// PostProcess dzy 注释
		// source = clearSourceCloseClose(source, "div");
		// source = clearSourceCloseOpen(source, "div");
		// source.replaceAll("(?is)<div .*?>.*?</div>", ""); // remove
		// javascript
		// --------------------------------------------------------------------------------------------------
		// PostProcess
		// System.out.println("source" + source);
		return source;
	}

}
