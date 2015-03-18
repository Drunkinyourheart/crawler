//package com.yeepay.bigdata.crawler.manager.sync;
//
//import kafka.javaapi.producer.Producer;
//import kafka.producer.ProducerConfig;
//import org.apache.log4j.Logger;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
///**
// * 类KafkaFactory.java的实现描述：TODO 类实现描述
// * @author hongfengwang 2014-2-24 下午09:26:05
// */
//
//public class ProducerCache {
//
//    private static final Logger                                     logger = Logger.getLogger(ProducerCache.class);
//
//    @SuppressWarnings("rawtypes")
//	private static final ConcurrentMap<String, Producer> producer_instance = new ConcurrentHashMap<String, Producer>();
//
//
//	@SuppressWarnings("unchecked")
//	public static Producer getProducer(String topic,KafkaConfig kafkaConfig) {
//          if (producer_instance.containsKey(topic)) {
//                return producer_instance.get(topic);
//            }
//          Producer<Integer, String> producer = null;
//          try{
//            ProducerConfig proConfig =new ProducerConfig(kafkaConfig.getProperties());
//            producer = new Producer(proConfig);
//            producer_instance.putIfAbsent(topic, producer);
//
//          }catch(Exception e){
//        	  logger.error("producerConfig error",e);
//          }
//        return producer;
//
//    }
//
//    }
//
