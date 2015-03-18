namespace java com.yeepay.bigdata.crawler.extractor.thrift.service

include "schedule.thrift"
/**
* 提取结果类型
*/
enum ExtractResultType{
	PAGE, //页面
	LIST,	//新闻list页面
	RSSLIST //Rss list页面
}

/**
* 提取任务分配参数
*/
struct ExtractTask{
	1: required string id;
	2: required string url;
	3: required string data;
	4: required ExtractResultType type;
	5: optional map<string,string> ctxMap;
}


/**
* 提取任务相应结果
*/
struct ExtractTaskResponse{
	1: required schedule.Response response;
	2: required string url;
	3: required string result;
	4: required ExtractResultType type;
}


/**
* 提取任务服务接口
*/
service ExtracteService{
	ExtractTaskResponse extract(1:ExtractTask task);
}
