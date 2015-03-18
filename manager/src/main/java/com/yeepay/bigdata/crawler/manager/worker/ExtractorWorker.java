package com.yeepay.bigdata.crawler.manager.worker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtractResultType;
import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtractTask;
import com.yeepay.bigdata.crawler.extractor.thrift.service.ExtractTaskResponse;
import com.yeepay.bigdata.crawler.manager.constants.SchedulerConstants;
import com.yeepay.bigdata.crawler.manager.dao.NewsContentDao;
import com.yeepay.bigdata.crawler.manager.heartbeat.ClientNodeSelector;
import com.yeepay.bigdata.crawler.manager.heartbeat.ExtractorClientNode;
import com.yeepay.bigdata.crawler.manager.model.*;
import com.yeepay.bigdata.crawler.manager.scheduler.SeedProcessEngine;
import com.yeepay.bigdata.crawler.manager.sync.KafkaConfig;
//import com.yeepay.bigdata.crawler.manager.sync.ProducerCache;
import com.yeepay.bigdata.crawler.manager.utils.*;
import com.yeepay.bigdata.crawler.manager.validator.ExtractorValidator;
import com.yeepay.bigdata.crawler.manager.validator.ValidateResult;
import com.yeepay.bigdata.crawler.manager.validator.ValidatorBuilder;
import com.yeepay.bigdata.crawler.schedule.thrift.ResponseStatus;
import com.yeepay.bigdata.crawler.schedule.thrift.client.ExtractorClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExtractorWorker implements Runnable, Alive {

    private static final Logger                                      logger  = Logger.getLogger(ExtractorWorker.class);

    private BlockingQueue<Task>                                      inputQueue;

    private BlockingQueue<SeedInfo>                                  outputQueue;

    private ClientNodeSelector<ExtractorClientNode, ExtractorClient> selector;

    private boolean                                                  isAlive = true;

    private NewsContentDao newsContentDao;

    private ExtractResultFlow                                        extractResultFlow;
    private ClassPathXmlApplicationContext                           context;
//    @Autowired(required=true)
    private KafkaConfig kafkaConfig;

    /**
     *  Constructure
     */
    public ExtractorWorker() {
    	this.context = new ClassPathXmlApplicationContext("/spring/applicationContext.xml");
    }

    /**
     *
     *   Constructure
     *
     * @param inputQueue
     * @param outputQueue
     * @param selector
     * @param newsContentDao
     */
	public ExtractorWorker(BlockingQueue<Task> inputQueue,
                           BlockingQueue<SeedInfo> outputQueue,
                           ClientNodeSelector<ExtractorClientNode, ExtractorClient> selector,
                           NewsContentDao newsContentDao){
        super();
        this.outputQueue = outputQueue;
        this.inputQueue = inputQueue;
        this.selector = selector;
        this.newsContentDao = newsContentDao;
        this.context = new ClassPathXmlApplicationContext("/spring/applicationContext.xml");
//        System.out.println("extract init() ");
        init();
    }

    @Override
    public void run() {
        while (isAlive) {
            Task task = null;
            try {
        System.out.println("extract run()");
                System.out.println("inputQueue : " + inputQueue);
                task = inputQueue.take();  // 被 crawler 推送回来的结果。
                System.out.println("ttt : " + task);
                SeedProcessEngine.setCrawledQueueSize(inputQueue.size());
                // if (logger.isInfoEnabled()) {
                // logger.info("invoke extrackor : " + task);
                // }

                task.setExtractTime(Calendar.getInstance().getTime());

                ExtractorClientNode clientNode = selector.selectClientNode(task.getDomain());
                SeedInfoType seedType = task.getSeedType();
                if(seedType.getType().equals("detail")||seedType.getType().equals("epaperLayout")){
                	if(task.getCtxMap().get("publicationName")!=null&&!task.getCtxMap().get("publicationName").equals("")){
                		task.setCrawleData(PageClientUtils.getPage(SchedulerConstants.NEWSLEVEL_PREFIX+task.getUrl()));
                	}else{
                		task.setCrawleData(PageClientUtils.getPage(task.getUrl()));
                	}
                }
                if(seedType.getType().equals("detail")&&task.getTitle().contains("EPAPERLAYOUT")){
                	int index = "epaperLayout".length();
                	String tmpTitle = task.getTitle().substring(index);
                	task.setTitle(tmpTitle);//复原title给抽取模块
                	task.getCtxMap().put("AnchorTitle", tmpTitle);
                	
                }
                //注释日志
//                if(task.getCrawleData().equals("")||task.getCrawleData()==null){
//                	logger.info("extractWorker-data= null"+task.getCrawleData());
//                }else{
//                	logger.info("extractWorker-data is not null");
//                }
                
                ExtractTask extractTask = createExtractTask(task);

                // 被解析后的正文
                ExtractTaskResponse response = clientNode.getClient().extract(extractTask);

//                logger.info("response-"+response);
//                //title is too short retrying
//                String shortTitleTag="First time:The length of title is too short!";
//                if(response!=null&&response.getResponse().getMsg()!=null&&response.getResponse().getMsg().contains(shortTitleTag)){
//                	logger.info("LongTitleAdd-"+task.getTitle());
//                	task.getCtxMap().put("AnchorTitle", task.getTitle());
//                	extractTask = createExtractTask(task);
//                    response = clientNode.getClient().extract(extractTask);
//                }else{
//                	logger.info("else-LongTitleAdd" );
//                }
                extractResultFlow.process(task, response);
                // oldExtract(task, response);
            } catch (Throwable e) {
                // task == null : interrupted inputQueue ; doing nothing
                // interrupted outputQueue
                logger.error(task.getUrl() + task.getExtractData(), e);
                // will not retry
                // if (task != null) {
                // if (task.getRetryTimes() > 10) {
                // logger.error("invoke extractor times : "
                // + task.getRetryTimes());
                // continue;
                // }
                // task.setRetryTimes(task.getRetryTimes() + 1);
                // try {
                // inputQueue.put(task);
                // } catch (InterruptedException e1) {
                // logger.error(
                // "outputQueue putAll is interrupted; put task back to extractor inputQueue is interrupted",
                // e1);
                // isAlive = false;
                // }
                // }
                if (e instanceof InterruptedException) {
                    isAlive = false;
                }
            }
        }
    }

    private ExtractTask createExtractTask(Task task) {
        ExtractTask extractTask = new ExtractTask(task.getId(), task.getUrl(), task.getCrawleData(),
                                                  convert(task.getSeedType()));

        if (task.getCtxMap() != null && !task.getCtxMap().isEmpty()) {
            extractTask.setCtxMap(task.getCtxMap());
        }

        // init map
        if (extractTask.getCtxMap() == null) {
            extractTask.setCtxMap(new HashMap<String, String>());
        }

        if (task.getSeedType().equals(SeedInfoType.DETAIL)) {
            extractTask.getCtxMap().put("title", StringUtils.isNotBlank(task.getTitle())?task.getTitle():"defaultTitle");
            extractTask.getCtxMap().put(SchedulerConstants.DETAIL_NEXT_INDEX, task.getPageIndex() + "");
        }

        return extractTask;
    }

    @SuppressWarnings("unchecked")
	private void savePage(Task task) {
        System.out.println("=================================================================================================");
        System.out.println("task : " + task);
        System.out.println("=================================================================================================");
    }
//	private void savePage(Task task) {
//       //热闻时startTime和crawlerTime都为空
//        long crawlerTime=Long.valueOf(task.getCtxMap().get(SchedulerConstants.START_CRAWLER_TS));
//        Date date=new Date(crawlerTime);
//        if(task.getStartTime()==null){
//           task.setStartTime(date);
//        }
//        if(task.getCrawleTime()==null){
//            task.setCrawleTime(date);
//            //此种情况为热闻
//            HotNewsLogger.log(task);
//        }
//        HotNewsLogger.logFilter(task);
//        logger.info("保存数据库存抓取URL="+task.getUrl()+"结束的时间："+crawlerTime);
//        Long beforeInDbTime=System.currentTimeMillis();//入库前的时间
//        newsContentDao.insertNewsContent(new NewsContent(task.getId(), task.getExtractData()),
//                                         new NewsIndex(task.getId(), task.getFirstPageURL(), null, task.getStartTime(),
//                                                       task.getCrawleTime(), task.getExtractTime()));
//        //种子入库并同步发送
//        try {
//            KafkaConfig kafkaConfig = (KafkaConfig) this.context.getBean("kafkaConfig");
//            String topic = kafkaConfig.getTopic();
//            String content =task.getExtractData();
//            content = StringUtils.substringBeforeLast(content, "}");
//            content = content +",\"sendTs\":\""+System.currentTimeMillis()+"\"}";
///** Jerry Comment ------------------------- kafka ---------------------------- */
////			ProducerCache.getProducer(topic, kafkaConfig).send(new ProducerData<String, Message>(topic,new Message(content.getBytes("utf-8"))));
//
//			logSendContentRecord(content);//记录日志
///** Jerry Comment ------------------------- kafka ---------------------------- */
////        } catch (UnsupportedEncodingException e) {
//        } catch (Exception e) {
//			logger.error("UnsupportedEncodingException",e);
///** Jerry Comment ------------------------- kafka ---------------------------- */
////		}catch(Exception e ){
//			logger.error("sys error:",e);
//		}
//
//        //
//        //"startCrawlerTs";////设置抓取时间，来方便统计每个种子从抓取到入库耗时
//        statCrawler2extractTime(task,beforeInDbTime);
//    }
    //解析content的json串，并打日志
    private void logSendContentRecord(String content) {
//        Map obj= (Map) JSONObject.parse(content);
//        System.out.println(content);
        JSONObject obj = JSON.parseObject(content);
        String title= (String) obj.get("title");
        String url= (String) obj.get("url");
        String time= (String) obj.get("sendTs");
        String logContent="发送的URL:"+url+" 此url的title="+title+" content内容发送kafka消息的时间："+DateUtil.getDate2String(new Date(Long.valueOf(time)), "yyyy-MM-dd HH:mm:ss");
        MonitorSenderDriverLogger.log(logContent);
    }
    void statCrawler2extractTime(Task task,Long beforeInDbTime) { //STAT_BETWEEN_CRAWLER_EXTRACT_SUCCESS_TIME(//用于统计一个URL从开始抓取到准备入库之间的耗时
        StatisticsLogger.log(LogFormat.STAT_BETWEEN_CRAWLER_EXTRACT_SUCCESS_TIME, "crawler2extractTime", task.getSeedType().name(),
                task.getUrl(), task.getFromURL(), task.getDomain(), TaskUtils.getFirstPageURL(task), TaskUtils.getPageIndex(task),(System.currentTimeMillis()-Long.valueOf(task.getCtxMap().get(SchedulerConstants.START_CRAWLER_TS)))+"ms",(System.currentTimeMillis()-beforeInDbTime)+"ms", DateUtil.getDate2String(new Date(Long.valueOf(task.getCtxMap().get(SchedulerConstants.START_CRAWLER_TS))), "yyyy-MM-dd HH:mm:ss")+"");
    }
   @SuppressWarnings("unchecked")
public static void main(String[] args) {
//	   ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/spring/applicationContext.xml");
	   ExtractorWorker work =new ExtractorWorker();
	   //种子入库并同步发送
	   KafkaConfig kafkaConfig = (KafkaConfig) work.context.getBean("kafkaConfig");
	   if(kafkaConfig==null){
		   System.out.println("null");
	   }
       String topic = kafkaConfig.getTopic();
       System.out.println( kafkaConfig.getProperties());
       System.out.println(topic);
       String content ="{\"a\":\"content\"}";
       content = StringUtils.substringBeforeLast(content, "}");
       content = content +",\"sendTs\":\""+System.currentTimeMillis()+"\"}";
       try {
/** Jerry Comment ------------------------- kafka ---------------------------- */
//			ProducerCache.getProducer(topic,kafkaConfig).send(new ProducerData<String, Message>(topic,new Message(content.getBytes("utf-8"))));
			work.logSendContentRecord(content);
			System.out.println("success");
/** Jerry Comment ------------------------- kafka ---------------------------- */
       } catch (Exception e) {
			logger.error("UnsupportedEncodingException",e);
		}
   }
    private Set<SeedInfo> getSeedInfoForMiddle(Task task, String content) {

        if (StringUtils.isBlank(content)) {
            return Collections.emptySet();
        }
        URLList list = JSON.parseObject(content, URLList.class);
        if (list == null || list.getNewsInfos() == null || list.getNewsInfos().isEmpty()) {
            return Collections.emptySet();
        }

        Set<SeedInfo> seedInfos = new LinkedHashSet<SeedInfo>();
        for (NewsInfo url : list.getNewsInfos()) {
            seedInfos.add(createSeedInfoForMiddle(task, url));
        }

        return seedInfos;
    }

    // 中间页面下，detail页面的firstpage是他自身
    private SeedInfo createSeedInfoForMiddle(Task task, NewsInfo url) {

    	//不带新闻精细属性字段
//        SeedInfo seed=new SeedInfo(url.getTitle(), url.getUrl(), task.getSeedType().getSubType(), null, false,
//                task.getFromURL(), SchedulerConstants.FIRST_PAGE_INDEX, DigestUtils.md5Hex(url.getUrl()),
//                url.getUrl(), null);
    	//添加新闻精细属性字段
//        SeedInfo seed = new SeedInfo(url.getTitle(), url.getUrl(), task.getSeedType().getSubType(), null, false,
//                task.getFromURL(), SchedulerConstants.FIRST_PAGE_INDEX, DigestUtils.md5Hex(url.getUrl()),
//                url.getUrl(), null, 
//                task.getCtxMap().get("publicationName"), task.getCtxMap().get("typeArea"), 
//                task.getCtxMap().get("channel"), task.getCtxMap().get("sub_channel"));
        SeedInfo seed = new SeedInfo(url.getTitle(), url.getUrl(), task.getSeedType().getSubType(), null, false,
                task.getFromURL(), SchedulerConstants.FIRST_PAGE_INDEX, DigestUtils.md5Hex(url.getUrl()),
                url.getUrl(), null, 
                url.getPublicationName(), url.getTypeArea(), 
                url.getChannel(), url.getSub_channel());
        seed.setStartCrawlerTs(getSeedStartCrawlerTsFromCrawlerTs(task));
        seed.setSourceSeedType(task.getSeedType());
        return seed;
//        return new SeedInfo(url.getTitle(), url.getUrl(), task.getSeedType().getSubType(), null, false,
//                            task.getFromURL(), SchedulerConstants.FIRST_PAGE_INDEX, DigestUtils.md5Hex(url.getUrl()),
//                            url.getUrl(), null);
    }
    /**
     * ////设置抓取时间，来方便统计每个种子从抓取到入库耗时
     * @param task
     * @return
     */
    public  long getSeedStartCrawlerTsFromCrawlerTs(Task task) {
        Map<String, String> ctxMap = task.getCtxMap();
        long startCrawlerTs=0;
        if (ctxMap != null && ctxMap.containsKey(SchedulerConstants.START_CRAWLER_TS)) {
            try {
                startCrawlerTs=Long.valueOf(ctxMap.get(SchedulerConstants.START_CRAWLER_TS));
            } catch (Exception e) {
                logger.error("get startCrawlerTs exeception : ",e);
            }
        }
        return startCrawlerTs>0?startCrawlerTs:System.currentTimeMillis();
    }

    private ExtractResultType convert(SeedInfoType type) {
        switch (type) {
            case LIST:
                return ExtractResultType.LIST;
            case RSSLIST:
                return ExtractResultType.RSSLIST;
            case DETAIL:
                return ExtractResultType.PAGE;
            default:
                return ExtractResultType.PAGE;
        }
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    private class ExtractResultFlow {

        private ConcurrentMap<SeedInfoType, ExtractResultProcessor> processors = new ConcurrentHashMap<SeedInfoType, ExtractResultProcessor>();

        public void addProcessor(SeedInfoType type, ExtractResultProcessor processor) {
            processors.putIfAbsent(type, processor);
        }

        public void process(Task task, ExtractTaskResponse response) throws TException {
            ResponseStatus status = response.getResponse().getStatus();
            ExtractResultProcessor processor = processors.get(task.getSeedType());
            if (status.equals(ResponseStatus.Success)) {
                processor.processSuccess(task, response);
            } else if (status.equals(ResponseStatus.Failure)) {
                processor.processFailure(task, response);
            }
        }
    }

    private abstract class ExtractResultProcessor {

        public void processSuccess(Task task, ExtractTaskResponse response) throws TException {
        }

        public void processFailure(Task task, ExtractTaskResponse response) {
            logger.warn(String.format("Extracting Error : taskID : %s ; URL: %s ; destURL: %s ; Type: %s ; msg : %s; Data: %s",
                                      response.getResponse().getId(),
                                      response.getUrl(),
                                      task.getCtxMap() != null ? task.getCtxMap().get(SchedulerConstants.DEST_URL) : null,
                                      response.getType().name(), response.getResponse().getMsg(),
                                      StringUtils.substring(task.getCrawleData(), 0, 200)));
            statLog(task, response);
        }

        void statLog(Task task, ExtractTaskResponse response) {
            StatisticsLogger.log(LogFormat.STAT_EXTRACT_FAILURE, "extract", task.getSeedType().name(), task.getUrl(),
                                 response.getResponse().getStatus().name(), task.getFromURL(), task.getDomain(),
                                 TaskUtils.getFirstPageURL(task), TaskUtils.getPageIndex(task));
        }

        void statValidate(Task task, ExtractTaskResponse response, ValidateResult validate, long delayTime) {
            StatisticsLogger.log(LogFormat.STAT_EXTRACT_SUCCESS, "extract", task.getSeedType().name(), task.getUrl(),
                                 response.getResponse().getStatus().name(), task.getFromURL(), task.getDomain(),
                                 validate.toString(), delayTime + "", TaskUtils.getFirstPageURL(task),
                                 TaskUtils.getPageIndex(task));
        }

        void statValidateDetail(Task task, ExtractTaskResponse response, ValidateResult validate, long delayTime,
                                HandlerResult result, String nextURL) {
            StatisticsLogger.log(LogFormat.STAT_EXTRACT_DETAIL_SUCCESS, "extract", task.getSeedType().name(),
                                 task.getUrl(), response.getResponse().getStatus().name(), task.getFromURL(),
                                 task.getDomain(), validate.toString(), delayTime + "", result.name(), nextURL,
                                 TaskUtils.getFirstPageURL(task), TaskUtils.getPageIndex(task));
        }
        void statErrorLogDetail(Task task, ExtractTaskResponse response,String errorLog, long delayTime,
                                HandlerResult result, String nextURL) {
            StatisticsLogger.log(LogFormat.STAT_EXTRACT_DETAIL_SUCCESS, "extract", task.getSeedType().name(),
                    task.getUrl(), response.getResponse().getStatus().name(), task.getFromURL(),
                    task.getDomain(), errorLog, delayTime + "", result.name(), nextURL,
                    TaskUtils.getFirstPageURL(task), TaskUtils.getPageIndex(task));
        }
        long getDelayTime(Map<String, Object> newsObject) {
            return -1;
        }
    }

    private class ExtractDetailProcessor extends ExtractResultProcessor {

        private ExtractorValidator validator = ValidatorBuilder.getDetailValidator();

        private DetailHandler      handler;

        public ExtractDetailProcessor(DetailHandler handler){
            this.handler = handler;
        }

        @Override
        public void processSuccess(Task task, ExtractTaskResponse response) throws TException {
            // validate message
            String result = response.getResult();
            Map<String, Object> newsObject = JSON.parseObject(result, HashMap.class);
            ValidateResult validatorResult = validator.validate(newsObject);

            HandlerResult handleResult = null;
            String middleUrlError=(String) newsObject.get(SchedulerConstants.VALIDATE_MIDDLE_URL_KEY);
            if(StringUtils.isNotBlank(middleUrlError)){//第一次抽取识别为中间页的标识,直接丢弃
                String errorLog="crawler the first is middle page .it will be give up";
                statErrorLogDetail(task, response, errorLog, getDelayTime(newsObject), HandlerResult.MULTIGIVEUP, middleUrlError);
                return;
            };

            if (validatorResult.isValid()) {
                // oldSave(task, response);
                handleResult = handler.handle(task, newsObject, result);
            }
            String nextURL = (String) newsObject.get(SchedulerConstants.DETAIL_NEXT_URL_KEY);
            nextURL = StringUtils.isEmpty(nextURL) ? "" : nextURL;

            statValidateDetail(task, response, validatorResult, getDelayTime(newsObject), handleResult, nextURL);
        }

        private void oldSave(Task task, ExtractTaskResponse response) {
            task.setExtractData(response.getResult());
            savePage(task);
        }

        long getDelayTime(Map<String, Object> result) {
            Object tsValue = result.get("ts");
            if (tsValue != null) {
                long delayTime = System.currentTimeMillis() / 1000 - (Integer) tsValue;
                return delayTime;
            }
            return -1;
        }
    }

    private class ExtractMiddlePageProcessor extends ExtractResultProcessor {

        @Override
        public void processSuccess(Task task, ExtractTaskResponse response) throws TException {
            String result = response.getResult();
            Set<SeedInfo> seedInfos = getSeedInfoForMiddle(task, result);
            if (logger.isInfoEnabled()) {
                logger.info(task.getId() + " : " + task.getUrl()+ " : " + task.getSeedType() + " extractor seeds: " + result);
            }
            //添加papaerlayout标识
            boolean isLayout = false;
            if(task.getSeedType().toString().equals("EPAPERLAYOUT")){
//            	logger.info("the type is EPAPERLAYOUT");
            	isLayout=true;
            }
            
            for(SeedInfo seedInfo:seedInfos){
                try {
					if(!PageClientUtils.filterURL(seedInfo.getUrl(), task)){
						if(isLayout){
							seedInfo.setTitle(task.getSeedType()+seedInfo.getTitle());
						}
						outputQueue.add(seedInfo);
					}
				} catch (TException e) {
					logger.error("when filter middle url exception",e);
				}
            }
//            outputQueue.addAll(seedInfos);
            statValidate(task, response, new ValidateResult(), getDelayTime(null));
        }

    }

    private enum HandlerResult {
        SINGLE, MULTISTART, MULTIINPROCESS, MULTIFINISH, MULTIGIVEUP;
    }

    private class DetailHandler {

        private MultiPageCache cache    = MultiPageCacheManager.getMemCache();

        private MultiPageLock pageLock = MultiPageCacheManager.getMultiPageLock();

        public HandlerResult handle(Task task, Map<String, Object> extractResult, String originalResult) {
            if (task.getPageIndex() == 1) {
                if (hasNextPage(extractResult)) {
                    MultiPageLock.Pair pair = pageLock.tryLock(task.getFirstPageId());
                    logger.info("url="+task.getUrl()+" time="+task.getStartCrawlerTs()+" maptime="+task.getCtxMap().get(SchedulerConstants.START_CRAWLER_TS));
                    if (pair.isLock()) {
                        // store data and clear already exist data
                        cache.initAndStore(task.getFirstPageId(), originalResult);
                        task.setLock(pair.getEntry());
                        if(StringUtils.isBlank(task.getTitle())&& StringUtils.isNotBlank((String) extractResult.get(SchedulerConstants.NEWS_URL_TITLE))){
                            task.setTitle((String) extractResult.get(SchedulerConstants.NEWS_URL_TITLE));//目前在判断热闻中用到，热闻分页时，第一页的task没有title
                        }
                        // create seed
                        SeedInfo seedInfo = createSeedInfoForDetail(task, getNextURL(extractResult));
                        logger.info("第一次分页url="+task.getUrl()+" time="+task.getStartCrawlerTs()+" maptime="+task.getCtxMap().get(SchedulerConstants.START_CRAWLER_TS));
                        try {
							if(!PageClientUtils.filterURL(seedInfo.getUrl(),task)){
								logger.info("add seed"+seedInfo.getUrl());
								outputQueue.add(seedInfo);
							}
						} catch (TException e) {
							logger.error("when filter the second page url exception",e);
						}
                        
                        return HandlerResult.MULTISTART;
                    }
                } else {// single page: 保存页面
                	logger.info("single page is saving");
                    task.setExtractData(originalResult);
                    ExtractorWorker.this.savePage(task);
                    return HandlerResult.SINGLE;
                }
            } else {
                if (hasNextPage(extractResult)) {// middle page in multipage
                    if (pageLock.isSameLockAndReliable(task.getFirstPageId(), task.getLock())) {
                        if (task.getPageIndex() > SchedulerConstants.MAX_PAGE_INDEX) {// excess max pages
                            pageLock.freeLock(task.getFirstPageId(), task.getLock());
                            return HandlerResult.MULTIGIVEUP;
                        }
                        // store data
                        cache.appendStore(task.getFirstPageId(), originalResult);
                        // create seed
                        SeedInfo seedInfo = createSeedInfoForDetail(task, getNextURL(extractResult));
                        logger.info("第二次分页url="+task.getUrl()+" time="+task.getStartCrawlerTs()+" maptime="+task.getCtxMap().get(SchedulerConstants.START_CRAWLER_TS));
                        try {
							if(!PageClientUtils.filterURL(seedInfo.getUrl(),task)){
								logger.info("add seed"+seedInfo.getUrl());
								outputQueue.add(seedInfo);
							}
						} catch (TException e) {
							logger.error("when filter middle url exception",e);
						}
//                        outputQueue.add(seedInfo);
                        return HandlerResult.MULTIINPROCESS;
                    }
                } else {// last page
                    if (pageLock.isSameLockAndReliable(task.getFirstPageId(), task.getLock())) {
                        logger.info("第三次分页url="+task.getUrl()+" time="+task.getStartCrawlerTs()+" maptime="+task.getCtxMap().get(SchedulerConstants.START_CRAWLER_TS));

                        // merge data and store data to db
                        List<String> pages = cache.getCacheAndClear(task.getFirstPageId());
                        pages.add(originalResult);
                        storeToDb(pages, task);
                        // free lock
                        pageLock.freeLock(task.getFirstPageId(), task.getLock());
                        return HandlerResult.MULTIFINISH;
                    }
                }
            }
            return HandlerResult.MULTIGIVEUP;
        }

        private SeedInfo createSeedInfoForDetail(Task task, String nextURL) {

//            SeedInfo seed=new SeedInfo(task.getTitle(), nextURL, SeedInfoType.DETAIL, null, false, task.getFromURL(),
//                    task.getPageIndex() + 1, task.getFirstPageId(), task.getFirstPageURL(), task.getLock());
            SeedInfo seed = new SeedInfo(task.getTitle(), nextURL, SeedInfoType.DETAIL, null, false, task.getFromURL(),
                    task.getPageIndex() + 1, task.getFirstPageId(), task.getFirstPageURL(), task.getLock(), 
                    task.getCtxMap().get("publicationName"), task.getCtxMap().get("typeArea"), 
                    task.getCtxMap().get("channel"), task.getCtxMap().get("sub_channel"));
            seed.setStartCrawlerTs(getSeedStartCrawlerTsFromCrawlerTs(task));

            return seed;
//            return new SeedInfo(task.getTitle(), nextURL, SeedInfoType.DETAIL, null, false, task.getFromURL(),
//                                task.getPageIndex() + 1, task.getFirstPageId(), task.getFirstPageURL(), task.getLock());
        }

        /**
         * ////设置抓取时间，来方便统计每个种子从抓取到入库耗时
         * @param task
         * @return
         */
        public  long getSeedStartCrawlerTsFromCrawlerTs(Task task) {
            Map<String, String> ctxMap = task.getCtxMap();
            long startCrawlerTs=0;
            if (ctxMap != null && ctxMap.containsKey(SchedulerConstants.START_CRAWLER_TS)) {
                try {
                    startCrawlerTs=Long.valueOf(ctxMap.get(SchedulerConstants.START_CRAWLER_TS));
                } catch (Exception e) {
                    logger.error("get startCrawlerTs exeception : ",e);
                };
            }
            if(task.getStartCrawlerTs()>0){
            	startCrawlerTs=task.getStartCrawlerTs();
            }
            return startCrawlerTs>0?startCrawlerTs:System.currentTimeMillis();
        }


        private void storeToDb(List<String> pages, Task task) {
            Map<String, Object> pageResult = new HashMap<String, Object>();
            for (String page : pages) {
                mergePages(pageResult, JSON.parseObject(page, HashMap.class));
            }
            String page = JSON.toJSONString(pageResult);
            task.setExtractData(page);
            savePage(task);
        }

        private void mergePages(Map<String, Object> result, Map<String, Object> page) {
            if (result.isEmpty()) {
                result.putAll(page);
                return;
            }
            for (String key : page.keySet()) {
                if (!result.containsKey(key)) {
                    result.put(key, page.get(key));
                } else {
                    if (StringUtils.equals(SchedulerConstants.MERGE_IMAGE_KEY, key)) {
                        Collection<String> images = (Collection<String>) result.get(key);
                        images.addAll((Collection<? extends String>) page.get(key));
                    }
                    if (StringUtils.equals(SchedulerConstants.MERGE_CONTENT_KEY, key)) {
                        String content = (String) result.get(key) + page.get(key);
                        result.put(key, content);
                    }
                }
            }
        }

        private boolean hasNextPage(Map<String, Object> extractResult) {
            return StringUtils.isNotBlank((String) extractResult.get(SchedulerConstants.DETAIL_NEXT_URL_KEY));
        }

        private String getNextURL(Map<String, Object> extractResult) {
            return (String) extractResult.get(SchedulerConstants.DETAIL_NEXT_URL_KEY);
        }
    }


    private void init() {
        extractResultFlow = new ExtractResultFlow();
        ExtractResultProcessor detailProcessor = new ExtractDetailProcessor(new DetailHandler());
        extractResultFlow.addProcessor(SeedInfoType.DETAIL, detailProcessor);

        // middle page
        ExtractResultProcessor middleProcessor = new ExtractMiddlePageProcessor();
        extractResultFlow.addProcessor(SeedInfoType.LIST, middleProcessor);
        extractResultFlow.addProcessor(SeedInfoType.RSSLIST, middleProcessor);
        extractResultFlow.addProcessor(SeedInfoType.EPAPER, middleProcessor);
        extractResultFlow.addProcessor(SeedInfoType.EPAPERLAYOUT, middleProcessor);

    }


}
