namespace java com.yeepay.bigdata.crawler.crawl.thrift.service

include "schedule.thrift"


/**
*	抓取任务分配请求参数
*/
struct CrawlerTask{
	1: required string id;
	2: required string url;
	3: required bool isDynamic;
	4: optional map<string,string> ctxMap;
}

/**
* 抓取任务分配响应
*/
struct CrawlerTaskResponse{
	1: required schedule.Response response;
}

/**
*	抓取任务结果状态
*/
enum CrawlerTaskResultStatus{
	Succeed, // 抓取成功
	Failure, //抓取失败
	Error   //抓取错误
}

/**
* 抓取任务执行结果参数
*/
struct CrawlerTaskResult{
	1: required string id;
	2: required string url;
	3: required string data;
	4: required CrawlerTaskResultStatus status;
	5: optional string msg;
	6: optional map<string,string> ctxMap;
}

/**
* 抓取任务执行结果响应
*/
struct CrawlerTaskResultResponse{
	1: required schedule.Response response;
}

// -------------------------------------------------------------------------------------- 以下为服务接口

/**
* 抓取任务分派服务
*/
service CrawlerTaskAssignService{
	CrawlerTaskResponse assignCrawlerTask(1:CrawlerTask task);
}

/**
* 抓取任务结果返回
*/
service CrawlerTaskResultPushService{
	CrawlerTaskResultResponse pushCrawlerTaskResult(1:CrawlerTaskResult result);
}
