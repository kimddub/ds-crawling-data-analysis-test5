package com.company.demo.crawler;
//크롤링 기록 :site, category, period1, period2, crawlingTime, articleCount

import java.util.Date;

import com.company.demo.dto.Article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlingInfo {
	private String site;
	private String category;
	private int articleCount;
	private double crawlingTime;
	private Date startOfPeriodToCrawling; // 수집 할 범위의 첫 시간
	private Date endOfPeriodToCrawling;
}
