namespace java com.yeepay.bigdata.crawler.schedule.thrift

/**
*	服务响应状态值
*/
enum ResponseStatus{
	Success,	//
	Failure	//执行失败 or 拒绝任务
}

/**
*	服务响应结果
*/
struct Response{
	1: required string id;
	2: required ResponseStatus status;
	3: optional string msg;
}
