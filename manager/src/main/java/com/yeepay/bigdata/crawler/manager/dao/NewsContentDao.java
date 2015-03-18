package com.yeepay.bigdata.crawler.manager.dao;

import com.yeepay.bigdata.crawler.manager.model.NewsContent;
import com.yeepay.bigdata.crawler.manager.model.NewsIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("newsContentDao")
public class NewsContentDao {

	@Autowired
	private JdbcTemplate jdbcTemplateSpider;

	@Transactional(propagation=Propagation.REQUIRED)
	public void insertNewsContent(NewsContent content,NewsIndex index){
		String newsSql = "insert into news_content(id,content,createTime) values(?,?,now());";
		String indexSql = "insert into news_index(id,url,docid,startTime,crawleTime,extractTime,createTime) values(?,?,?,?,?,?,now());";
		jdbcTemplateSpider.update(newsSql, new Object[]{content.getId(),content.getContent()});
		jdbcTemplateSpider.update(indexSql, index.getId(),index.getUrl(),index.getDocId(),index.getStartTime(),index.getCrawleTime(),index.getExtractTime());
	}
}
