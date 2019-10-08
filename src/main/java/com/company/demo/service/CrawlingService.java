package com.company.demo.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.company.demo.dto.Article;

public interface CrawlingService {
	public void collectData(List<Article> articles);

	public Date getLastDate(int sourcId);

	public void resetDB();

	public List<Map<String, Object>> getAllSourceInfo();
}
