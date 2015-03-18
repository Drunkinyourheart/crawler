//package com.yeepay.bigdata.crawler.extractor;
//
////import cn.html2me.extract.content.ExtractContent;
////import cn.html2me.extract.navigator.NavigatorExtractor;
////import cn.html2me.extract.source.MatchSource;
////import cn.html2me.extract.time.RegexTime;
////import cn.html2me.extract.title.TitleTagExtractTitle;
//
//import com.alibaba.fastjson.JSON;
//import com.sun.syndication.io.FeedException;
//import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtractTask;
//import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtractTaskResponse;
//import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtracteService.Iface;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.log4j.Logger;
//import org.apache.thrift.TException;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
////import com.sohu.smc.spider.constants.ExtractorConstants;
////import com.sohu.smc.spider.extractor.model.NewsInfo;
////import com.sohu.smc.spider.extractor.text.ListExtractor;
////import com.sohu.smc.spider.extractor.text.NewsEx_Tri;
////import com.sohu.smc.spider.extractor.text.RssExtractor;
////import com.sohu.smc.spider.extractor.thrift.service.ExtractResultType;
////import com.sohu.smc.spider.extractor.thrift.service.ExtractTask;
////import com.sohu.smc.spider.extractor.thrift.service.ExtractTaskResponse;
////import com.sohu.smc.spider.extractor.thrift.service.ExtracteService.Iface;
////import com.sohu.smc.spider.extractor.util.DateTimeStampUtils;
////import com.sohu.smc.spider.thrift.Response;
////import com.sohu.smc.spider.thrift.ResponseStatus;
//
//public class ExtractorHandler implements Iface {
//
//	Logger log = Logger.getLogger(ExtractorHandler.class.getName());
//
//	// get the xpath infos of all source uris from database
//
//	@Override
//	public ExtractTaskResponse extract(ExtractTask task) throws TException {
//
//		// ExtractTaskResponse res = originMethod(task);
//
//		String id = task.getId();
//		String url = task.getUrl();
//		String data = task.getData();
//		String destURL = url;
//
//		// set redirect url address
//		if (task.isSetCtxMap()) {
//			String otherURL = task.getCtxMap().get(ExtractorConstants.DEST_URL);
//			// url == destURL
//			if (StringUtils.isNotBlank(otherURL)) {
//				destURL = otherURL;
//			}
//		}
//		ExtractResultType type = task.getType();
//		try {
//			if (type.equals(ExtractResultType.PAGE)) {
//				return extractDetailWithNative(id, url,task.getCtxMap().get(ExtractorConstants.TITLE), data);
//			} else {
//				return extractList(type, id, url, destURL, data);
//			}
//		} catch (Throwable e) {
//			log.error(String.format("%s : %s : %s : dest %s", id, url,type.name(), destURL), e);
//			log.error(String.format("%s : %s : %s : %s", id, url, e,
//					StringUtils.substring(data, 0, 400)));
//		}
//
//		ExtractTaskResponse res = new ExtractTaskResponse();
//		Response response = new Response(id, ResponseStatus.Failure);
//		response.setMsg("some exception is thrown!!!!");
//		res.setResponse(response);
//		res.setResult("");
//		res.setType(type);
//		res.setUrl(url);
//		return res;
//	}
//
//	private ExtractTaskResponse extractList(ExtractResultType type, String id,
//			String url, String destURL, String data)
//			throws IllegalArgumentException, FeedException, IOException {
//		ExtractTaskResponse res = new ExtractTaskResponse();
//		Set<NewsInfo> resultList = null;
//		if (type.equals(ExtractResultType.LIST)) {
//			resultList = ListExtractor.extractList(destURL, data);
//		} else {
//			resultList = RssExtractor.extractList(destURL, data);
//		}
//		Response r = new Response();
//		r.setId(id);
//
//		if (resultList != null && !resultList.isEmpty()) {
//			r.setStatus(ResponseStatus.Success);
//			Map<String, Set<NewsInfo>> resultsMap = new HashMap<String, Set<NewsInfo>>();
//			resultsMap.put("newsInfos", resultList);
//			String result = JSON.toJSONString(resultsMap);
//			res.setResult(result);
//		} else {
//			r.setStatus(ResponseStatus.Failure);
//			r.setMsg("extract list result is null.");
//			res.setResult("");
//		}
//
//		res.setType(type);
//		res.setUrl(url);
//		res.setResponse(r);
//
//		// 日志跟踪
//		if (r.getStatus().equals(ResponseStatus.Failure)) {
//			log.warn(String.format("%s : %s : %s : %s : %s", id, url,
//					ExtractResultType.PAGE.name(),
//					StringUtils.substring(data, 0, 200), res.result));
//		} else {
//			log.info(String.format("%s : %s : %s : %s", id, url,
//					ExtractResultType.LIST.name(), res.result));
//		}
//
//		return res;
//	}
//
//	private ExtractTaskResponse extractDetailWithNative(String id, String url,
//			String articleTitle, String data) {
//		ExtractTaskResponse res = new ExtractTaskResponse();
//
//		NewsEx_Tri extractor = new NewsEx_Tri();
//		extractor.Open();
//		extractor.set(data, url);
//		String json = extractor.get();
//		extractor.Close();
//
//		Response r = new Response();
//		r.setId(id);
//
//		if (StringUtils.isNotBlank(json)) {
//			r.setStatus(ResponseStatus.Success);
//		} else {
//			r.setStatus(ResponseStatus.Failure);
//		}
//
//		res.setResponse(r);
//		res.setUrl(url);
//		res.setType(ExtractResultType.PAGE);
//		res.setResult(json);
//
//		// 日志跟踪
//		if (r.getStatus().equals(ResponseStatus.Failure)) {
//			log.warn(String.format("%s : %s : %s : %s : %s", id, url,
//					ExtractResultType.PAGE.name(),
//					StringUtils.substring(data, 0, 200), res.result));
//		} else {
//			log.info(String.format("%s : %s : %s : %s", id, url,
//					ExtractResultType.PAGE.name(), res.result));
//		}
//
//		return res;
//	}
//
//	private ExtractTaskResponse extractDetail(String id, String url,
//			String articleTitle, String data) {
//		ExtractTaskResponse res = new ExtractTaskResponse();
//		String content = new ExtractContent().extractArticle(data);
//		String title = new TitleTagExtractTitle().extractTitle(data);
//		String time = new RegexTime().extractTime(data);
//		String source = new MatchSource().extractSource(data);
//		String[] navigator = new NavigatorExtractor().extractNavigator(data);
//
//		Response r = new Response();
//		r.setId(id);
//
//		if (title == null || title.equals("") || time == null
//				|| time.equals("") || source == null || source.equals("")
//				|| content == null || content.equals("")) {
//			r.setStatus(ResponseStatus.Failure);
//			r.setMsg("");
//		} else {
//			r.setStatus(ResponseStatus.Success);
//		}
//
//		Map<String, Object> resultMap = new HashMap<String, Object>();
//		resultMap.put("title", title);
//		resultMap.put("content", content);
//		resultMap.put("time", time);
//		resultMap.put("source", source);
//		resultMap.put("navigator", navigator);
//		resultMap.put("url", url);
//		resultMap.put("ts", DateTimeStampUtils.parseDate(time));
//
//		String json = JSON.toJSONString(resultMap);
//
//		res.setResponse(r);
//		res.setUrl(url);
//		res.setType(ExtractResultType.PAGE);
//		res.setResult(json);
//
//		// 日志跟踪
//		if (r.getStatus().equals(ResponseStatus.Failure)) {
//			log.warn(String.format("%s : %s : %s : %s : %s", id, url,
//					ExtractResultType.PAGE.name(),
//					StringUtils.substring(data, 0, 200), res.result));
//		} else {
//			log.info(String.format("%s : %s : %s : %s", id, url,
//					ExtractResultType.PAGE.name(), res.result));
//		}
//		return res;
//	}
//}
