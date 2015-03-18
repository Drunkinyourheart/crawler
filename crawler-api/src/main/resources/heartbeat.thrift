namespace java com.yeepay.bigdata.crawler.heartbeat.thrift.service

typedef i32 int
typedef i64 long

/**
* worker 类型： 目前包括：
*/
enum WorkerType{
	CRAWLER, // 爬取器
	EXTRACTOR //提取器
}


struct WorkerInfo{
	1: required string host; // 爬虫 or extractor 主机IP
	2: required int port; // 爬虫 or extractor 服务端口
	3: required WorkerType type;
	4: required long timestamp;
}
/**
* 心跳参数:
*/
struct HeartBeatParam{
	1: required WorkerInfo info;
	2: optional int totalTaskNum; // 当前任务总数
	3: optional int waitingTaskNum; //等待执行任务数
	4: optional int activeTaskNum; //等待执行任务数
}

/**
*
*/
struct HeartBeatResponse{
	1: required string msg;
}
/*
*/
service HeartBeatProtocal{
	HeartBeatResponse heartbeatCheck(1:HeartBeatParam param);
}
