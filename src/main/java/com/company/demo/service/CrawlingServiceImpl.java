package com.company.demo.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.company.demo.dao.CrawlingDao;
import com.company.demo.dto.Article;

@Service
public class CrawlingServiceImpl implements CrawlingService {
	@Autowired
	CrawlingDao crawlingDao;
	
	public List<Map<String, Object>> getAllSourceInfo() {
		return crawlingDao.getAllSourceInfo();
	}
	
	public void collectData(List<Article> articles) {
		crawlingDao.insert(articles);
	}
	
	public Date getLastDate(int sourcId) {
		return crawlingDao.getLastDate(sourcId);
	}
	
	public void resetDB() {
		crawlingDao.truncate();
	}
}
