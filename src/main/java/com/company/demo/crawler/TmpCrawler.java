package com.company.demo.crawler;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.groovy.util.Maps;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.company.demo.dto.Article;

@Component
public class TmpCrawler {
	private final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

	
	public void getArticlesFromOnePage(Map<String,Object> param) {
		// 크롤링 전 셋팅

		
		boolean needToCollectDataOnNextPage = true; 
		int crawlingPage = 1;
		
		String dateFormat= "yyyyMMdd";
		String timeFormat = "hh:mm";
		
		Date today = new Date();
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		String crawlingDate = dateFormatter.format(today);

		
		String limitDate = "20191001";
		String limitTime = "12:00";
		
		
		// 페이지 넘겨가며 찾을 때 없는 페이지 입력하면 1/또는 끝페이지로 처리
		// 페이지 넘겨가며 찾을 때 없는 날짜 입력하면 가까운 존재하는 날짜로 처리
		// 페이지 버튼으로 마지막 페이지 얻어야함
		/*
		네이버 - https://news.naver.com/
		사회 - https://news.naver.com/main/main.nhn?mode=LSD&mid=shm&sid1=102
		인물 - https://news.naver.com/main/list.nhn?mode=LS2D&mid=shm&sid1=102&sid2=276&date=20191008&page=6
		 	*없는 파라미터 입력시 셀렉터가 널 엘리먼트인지도 예외처리*
		목록-각 게시글 셀렉터 - #main_content > div.list_body.newsflash_body > ul > li > dl > dt.photo > a
			*&amp; 로 가져오는거 무엇?
			속성-본문URL - <a href="https://news.naver.com/main/read.nhn?mode=LS2D&amp;mid=shm&amp;sid1=102&amp;sid2=276&amp;oid=087&amp;aid=0000772525">...</a>
		목록-각 게시글 시간(몇분전) 셀렉터 - #main_content > div.list_body.newsflash_body > ul.type06_headline > li > dl > dd > span.date.is_new
		목록-언론사 셀렉터 - #main_content > div.list_body.newsflash_body > ul > li > dl > dd > span.writing
		목록-마지막페이지 셀렉터 - #main_content > div.paging > a:last-child, #main_content > div.paging > strong:last-child
		본문 - https://news.naver.com/main/read.nhn?mode=LS2D&mid=shm&sid1=102&sid2=276&oid=087&aid=0000772525
		제목 셀렉터 - #articleTitle
		시간 셀렉터 - #main_content > div.article_header > div.article_info > div > span
		내용 셀렉터 - #articleBodyContents
		*/
		
		//페이지 넘겨가며 수집할 때 없는 페이지나 날짜엔 데이터가 없다
		//목록에서 수집 날짜 시간으로 거를 수 있다.
		/* 
		다음 - https://media.daum.net/
		사회 - https://news.daum.net/breakingnews/society
		인물 - https://news.daum.net/breakingnews/society/people?page=2&regDate=20191008
		 	*없는 파라미터 입력시 셀렉터가 널 엘리먼트인지도 예외처리*
		목록-각 게시글 셀렉터 - #mArticle > div.box_etc > ul > li > div > strong > a
			속성-본문URL - <a href="http://v.media.daum.net/v/20191008125615004"... </a>
		목록-각 게시글 시간 셀렉터 - #mArticle > div.box_etc > ul > li > div > strong > span > span.info_time
		목록-언론사 셀렉터 - #mArticle > div.box_etc > ul > li > div > strong > span
		본문 - http://v.media.daum.net/v/20191008125615004
		제목 셀렉터 - #cSub > div > h3
		시간 셀렉터 - #cSub > div > span > span:nth-child(2)
		언론사 셀렉터 - #cSub > div > span > span:nth-child(1)
		내용 셀렉터 - #harmonyContainer > section > p
		*/
		

        List<Map<String,Object>> articleInfoList = new ArrayList<>();

		int idx = 0;
		while (needToCollectDataOnNextPage) {
			idx++;
			
			if (idx > 10) {
				break;
			}
			

	        Document listPage = null;
	        Elements detailUrlList = null;

	        System.out.println("크롤링 할 일자: " + crawlingDate + ", 페이지: " + crawlingPage);
			
			// 크롤링 입력값 셋팅
			
			String listUrl = "https://news.naver.com/main/list.nhn?mode=LS2D&mid=shm&sid1=102&sid2=276";
			String pageUrl = "&page=" + crawlingPage;
			String dateUrl = "&date=" + crawlingDate;
			
	        
			try {
				
				listPage = Jsoup.connect(listUrl+pageUrl+dateUrl)
								.userAgent(USER_AGENT)
								.get();
				
//				.header("Content-Type", "application/json;charset=UTF-8")
//	            .userAgent(USER_AGENT)
//	            .method(Connection.Method.GET)
//	            .ignoreContentType(true);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// 리스트 셀렉터 및 요소 접근
			String detailUrlSelector = "#main_content > div.list_body.newsflash_body > ul > li > dl > dt.photo > a";
			detailUrlList = listPage.select(detailUrlSelector);
	        
			if ( detailUrlList == null ) {
				crawlingPage = 1;
				Date prevDate = null;
				
		        try {
		        	
					prevDate = dateFormatter.parse(crawlingDate);
					
					Calendar cal = Calendar.getInstance();
					
					cal.setTime(prevDate);
					cal.add(Calendar.DATE, -1);
					crawlingDate = dateFormatter.format(cal.getTime());
					
				} catch (ParseException e) {
					e.printStackTrace();
				}

				System.out.println("요청한 페이지에 기사 정보가 없습니다.");
				break;
				
			}
	        
	        //int dateListIdx = 0;
	        for (Element detailUrl : detailUrlList) {
	        	
	        	String url = detailUrl.attr("href").trim();
	        	
	        	System.out.println(url);
	        }
	        
	        crawlingPage++;
	        
		}
       
        //List<Article> collectedArticles = collectArticles(articleInfoList);
        
		//return "";
	}

}
