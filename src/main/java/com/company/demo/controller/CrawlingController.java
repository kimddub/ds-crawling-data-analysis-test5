package com.company.demo.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.company.demo.DsCrawlingDataAnalysisTest5Application;
import com.company.demo.crawler.CrawlingInfo;
import com.company.demo.crawler.DaumCrawler;
import com.company.demo.crawler.TmpCrawler;
import com.company.demo.dto.Article;
import com.company.demo.service.CrawlingService;

@Controller
public class CrawlingController {
	@Autowired
	CrawlingService crawlingService;
	@Autowired
	TmpCrawler tmpCrawler;
	@Autowired
	DaumCrawler daumCrawler;
	
	private static Logger logger = LoggerFactory.getLogger(DsCrawlingDataAnalysisTest5Application.class);
	
	private static List<Map<String,Object>> sourceInfoList;
	
	private int CODE_Daum_Society = 1;
	
	@RequestMapping("DaumCrawling")
	@ResponseBody
	public String executeDaumCrawling() {
			
		/*
		 각 사이트 크롤링 시 진행상황
		 - 사용자 파라미터 : 사이트, 카테고리, 매체 (,조회시작날/시간, 조회끝/시간)
		 
		 *사용자 입력값에 해당하는 DB 데이터에서 다음 파라미터들을 꺼내온다.
		 - 크롤러 셋팅시 파라미터1 : 카테고리 리스트 URL & 페이지 & 이외 변수 & 게시글 셀렉터
		 
		 *리스트에서 딴 본문 URL로 접속한다.
		 - 크롤러 셋팅시 파라미터2 : 게시글 본문에서 내용 딸 셀렉터들
		 - 크롤러에서 필요한 텍스트만 받는다.
		 */
		
		
		tmpCrawler.getArticlesFromOnePage(null);
		
		
		
		/*
		daumCrawler.setSourceInfoList(sourceInfoList);
		
		// DB의 최신 데이터 날짜 셋팅해줌 (null: 일주일 전부터, lastDate: 이어서)
		Date lastDate = crawlingService.getLastDate(CODE_Daum_Society);
		daumCrawler.setLimitDate(lastDate);
		
		daumCrawler.setSourceInfo(CODE_Daum_Society);
		
		List<Article> articles = daumCrawler.crawling();
		
		try {
			
			if (articles.size() == 0) {
				// 수집한 기사 없음
				logger.info(daumCrawler.getErrorMsg());
				
			} else if (articles != null && articles.size() != 0) { 
				// 수집한 기사 있음
				crawlingService.collectData(articles);
				
			} 
		} catch (Exception e) {
			
			// 크롤링 설정 에러
			logger.error(daumCrawler.getErrorMsg());
		}
		
		// -------------인사이트-푸드 크롤링-----------------
		
		// DB의 최신 데이터 날짜 셋팅해줌 (null: 일주일 전부터, lastDate: 이어서)
		Date lastDate2 = crawlingService.getLastDate(CODE_Daum_Society);
		daumCrawler.setLimitDate(lastDate2);
		
		daumCrawler.setSourceInfo(CODE_Daum_Society);
		
		List<Article> articles2 = daumCrawler.crawling();
		
		try {
			
			if (articles2.size() == 0) {
				// 수집한 기사 없음
				logger.info(daumCrawler.getErrorMsg());
				
			} else if (articles2 != null && articles2.size() != 0) { 
				// 수집한 기사 있음
				crawlingService.collectData(articles2);
				
			} 
		} catch (Exception e) {
			
			// 크롤링 설정 에러
			logger.error(daumCrawler.getErrorMsg());
		}
		*/
		return "Crawling done successfully";
	}

	/*
	@EventListener(ApplicationReadyEvent.class)
	public void executeCrawlingAfterStartup() {
		sourceInfoList = crawlingService.getAllSourceInfo();
		System.out.println(sourceInfoList);
		
		//crawlingService.resetDB();
		
		logger.info("Started initial web Crawling to insigt / thread : {}",Thread.currentThread().getName());
		executeInsightCrawling();
	} 
	 
	@Scheduled(cron = "0 0/10 * * * MON-FRI") // 평일 정각마다 돌아감
	public void executeInsightCrawlingEveryHour() {
		
		logger.info("Start InsightCrawling Scheduling Thread : {}", Thread.currentThread().getName());
		
		executeInsightCrawling();

		logger.info("End InsightCrawling Scheduling Thread");
	}
	
	@Scheduled(cron = "0 0/10 * * * MON-FRI") // 평일 정각마다 돌아감
	public void executeWikitreeCrawlingEveryHour() {

		logger.info("Start WikitreeCrawling Scheduling Thread : {}", Thread.currentThread().getName());
		
		executeWikitreeCrawling();
		
		logger.info("End WikitreeCrawling Scheduling Thread");
	}

	@Scheduled(fixedDelay = 172800000) // 2틀마다 실행 (수정 가능성 큰 주기)
	public void executeUpdateCrawlingEveryTwoDays() {
		
		logger.info("Start Scheduling Thread : {}", Thread.currentThread().getName());
		
		// 모든 사이트 수정 크롤러
		// 그냥 정해진 시작점부터 쭉 수정해나가는 크롤러 (경우의 수? 또는 모두 업데이트 해버리기?)
		// 끼어들기, 삭제, 수정

		logger.info("End Scheduling Thread");
	}
	
	@Scheduled(cron = " 0 0 10 ? * 6")  // 매월 마지막 금요일 아무날이나 10시에 실행 (수정 가능성 희박한 주기)
	public void executeUpdateCrawling1stDayOfEveryMonth() {
		
		logger.info("Start Scheduling Thread : {}", Thread.currentThread().getName());
		
		// 모든 사이트 수정 크롤러

		logger.info("End Scheduling Thread");
	}
	
	@RequestMapping("checkAll")
	@ResponseBody
	public Map<String,Object> showAllCrawlingProgress() {
		
//	작동하고 있는 스케줄링된 인사이트 크롤러들의 히스토리를 모두 체크
		
		Map<String,Object> allProgress = new HashMap<>();
		
		allProgress.put("insight-history",insightCrawler.getHistory());
		allProgress.put("wikitree-history",wikitreeCrawler.getHistory());
		
		return allProgress;
	}
	
	@RequestMapping("checkInsight")
	@ResponseBody
	public List<CrawlingInfo> showInsightCrawlingProgress() {
		
//		작동하고 있는 스케줄링된 인사이트 크롤러들의 히스토리를 모두 체크
		
		return insightCrawler.getHistory();
	}
	
	@RequestMapping("checkWikitree")
	@ResponseBody
	public List<CrawlingInfo> showWikitreeCrawlingProgress() {
		
//		작동하고 있는 스케줄링된 위키트리 크롤러들의 히스토리를 모두 체크
		System.out.println(wikitreeCrawler.getHistory());
		
		return wikitreeCrawler.getHistory();
	}
	*/
}