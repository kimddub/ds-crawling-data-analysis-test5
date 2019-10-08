package com.company.demo.crawler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.groovy.util.Maps;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.company.demo.dto.Article;

abstract class Crawler { 
	public String errorMsg;
	
	// 크롤링을 위한 정보
	public int sourceId; // DB에서 사용하는 site-category 코드
	public Date limitDate; 
	public int crawlingPage;
	public boolean needToCollectDataOnNextPage;
	
	// 크롤링 이력 정보
	public List<CrawlingInfo> history;

	// 크롤링시 사용하는 값
	public List<Map<String, Object>> sourceInfoList;
	public String site;
	public String category;
	public String siteUrl; 
	public String listUrl; 
	public String dateFormat;
	
	private final int SOURCEID_insight_fashion = 1;
	private final int SOURCEID_insight_food = 2;
	private final int SOURCEID_wikitree_fashion = 3;
	private final int SOURCEID_wikitree_food = 4;
	private final int SOURCEID_wikitree_life = 5;
	
	public abstract List<Article> getArticlesFromOnePage(int page);

	// 수집 주기의 시작날을 설정
	public void setLimitDate(Date testDate) {
		
		if (testDate != null) {
			
			this.limitDate = testDate;
			
		} else {
		
			Date currentDate = new Date();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDate);
			cal.add(Calendar.DATE, -7);
			Date limitDate = cal.getTime();
			
			this.limitDate = limitDate;
		}
	}
	
	// 수집 사이트의 id 만으로 사이트 양식 자동 셋팅
	public void setSourceId(int sourceId) {
		
		// 소스 설정
		//setSourceInfo(사이트명, 카테고리명, 사이트url, 리스트url(사이트url하위), 날짜형식 )
		if (sourceId == SOURCEID_insight_fashion) {
			
			this.sourceId = sourceId;
			setSourceInfo("인사이트","패션","https://www.insight.co.kr","/section/fashion","yyyy-MM-dd hh:mm:ss");
		
		} else if (sourceId == SOURCEID_insight_food) {
			
			this.sourceId = sourceId;
			setSourceInfo("인사이트","음식·맛집","https://www.insight.co.kr","/section/food","yyyy-MM-dd hh:mm:ss");
		
		} else if (sourceId == SOURCEID_wikitree_fashion) {

			this.sourceId = sourceId;
			setSourceInfo("위키트리","패션뷰티","https://www.wikitree.co.kr","/main/list.php?nc_id=82","yyyy-MM-dd hh:mm:ss");
		} else if (sourceId == SOURCEID_wikitree_food) {

			this.sourceId = sourceId;
			setSourceInfo("위키트리","푸드","https://www.wikitree.co.kr","/main/list.php?nc_id=81","yyyy-MM-dd hh:mm:ss");
		} else if (sourceId == SOURCEID_wikitree_life) {
			
			this.sourceId = sourceId;
			setSourceInfo("위키트리","라이프-일반","https://www.wikitree.co.kr","/main/list.php?nc_id=80","yyyy-MM-dd hh:mm:ss");
		} else {
			this.sourceId = -1;
		}
	}
	
	// 사이트 양식 리스트 세팅
	public void setSourceInfoList(List<Map<String, Object>> sourceInfoList) {

		this.sourceInfoList = sourceInfoList;
		
	}
	
	// 외부 사이트 양식 셋팅
	public void setSourceInfo(int sourceId) {
		
		this.sourceId = sourceId;
		
		// 소스 아이디로 리스트의 소스정보 인덱스를 호출할 수 있게 한다.
		this.site = (String)sourceInfoList.get(sourceId-1).get("site");
		this.category = (String)sourceInfoList.get(sourceId-1).get("category");
		
		this.siteUrl = (String)sourceInfoList.get(sourceId-1).get("siteUrl");
		this.listUrl = (String)sourceInfoList.get(sourceId-1).get("listUrl");

		this.dateFormat = (String)sourceInfoList.get(sourceId-1).get("dateFormat");
		
	}
	
	// 내부 사이트 양식 셋팅
	public void setSourceInfo(String site, String category, String siteUrl, String listUrl, String dateFormat) {

		this.site = site;
		this.category = category;
		
		this.siteUrl = siteUrl;
		this.listUrl = siteUrl + listUrl;

		this.dateFormat = dateFormat;
		
	}
	
	public List<Article> crawling() {
		// 크롤링을 위한 수집기간 설정이 되어있어야 작동
		if (limitDate == null) {
			
			errorMsg = "수집기간 미설정 상태";
			return null;
		}
		
		if (sourceId == -1) {
			
			errorMsg = "사이트 정보 미설정 상태";
			return null;
		}
		
		// 크롤링 주기마다 갱신되는 정보
		crawlingPage = 1;
        needToCollectDataOnNextPage = true;
        
        // 크롤링 한 기사들 목록
        List<Article> collectedArticles = null;
        List<Article> collectedAllArticles = new ArrayList<>();
        
		// 스탑워치 
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		 // --------------크롤링---------------
        // 다음 페이지를 리스팅 할 필요가 있으면
        while(needToCollectDataOnNextPage) {
        	
        	collectedArticles = getArticlesFromOnePage(crawlingPage++);
        	collectedAllArticles.addAll(collectedArticles);
        }

        stopWatch.stop();
        
        double crawlingTime = (stopWatch.getTotalTimeMillis()/1000.0 );
        int articleCount = collectedAllArticles.size();
        
        // ---------크롤링 정보 보관----------------

		// 크롤링 기록 :site, category, period, crawlingTime, articleCount
		CrawlingInfo info = new CrawlingInfo();
		
		info.setSite(site);
		info.setCategory(category);
		info.setStartOfPeriodToCrawling(limitDate);
		info.setEndOfPeriodToCrawling(new Date());
		info.setCrawlingTime(crawlingTime);
		info.setArticleCount(articleCount);
        history.add(info);
     		
        // ---------크롤러 초기 상태로 셋팅----------------
 		sourceId = -1; 
 		limitDate = null; 
 		
 		needToCollectDataOnNextPage = true;
 		crawlingPage = 1;
 		errorMsg = "새로운 기사 없음";

 		 // ---------크롤링 정보 로깅----------------
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		System.out.println(formatter.format(new Date()) + " [CRAWLER] - The crawl done successfully");
		System.out.println(formatter.format(new Date()) + " [CRAWLER] - " + info);
		
		return collectedAllArticles;
	}
	
	public List<CrawlingInfo> getHistory() {
		return history;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	
}
