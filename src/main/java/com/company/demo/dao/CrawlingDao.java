package com.company.demo.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.company.demo.dto.Article;

@Mapper
public interface CrawlingDao {
	public void insert(List<Article> articles);

	public Date getLastDate(int sourcId);

	public void truncate();

	public List<Map<String, Object>> getAllSourceInfo();
}
