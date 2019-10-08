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

@Component
public class DaumCrawler extends Crawler{
	
	DaumCrawler() {
		
		// 최초에 한번 갱신
		history = new ArrayList<>();
		
		// 초기 셋팅 (셋팅안하면 크롤링 못함)
		sourceId = -1; 
		limitDate = null; 
		
		// 크롤링 셋팅
		needToCollectDataOnNextPage = true;
		crawlingPage = 1;
		errorMsg = "새로운 기사 없음"; // 크롤링 끝날 때까지 다른 에러메시지 리턴안되면  크롤링 할 기사 null인 것
		
		// 크롤러에 lastDate, sourceId 셋팅해줘야 크롤링 가능
	}
	
	@Override
	public List<Article> getArticlesFromOnePage(int page) {

        List<Map<String,Object>> articleInfoList = new ArrayList<>();
		
        Document listPage = null;
        
		try {
			
			listPage = Jsoup.connect(siteUrl + listUrl + "?page=" + page).get();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ------ 리스트 셀렉터 및 요소 접근 (사이트 별 커스텀 필요)-------
		String articleListSelector = "body > div.content > div > div.section-wrap > div.section-list > ul > li > div";
		Elements articleList = listPage.select(articleListSelector);
		
		String detailUrlSelector = "a.section-list-article-title";
		Elements detailUrlList = articleList.select(detailUrlSelector);
		
		String articleDateSelector = "span.section-list-article-byline";
        Elements articleDateList = articleList.select(articleDateSelector);
        
        
        int dateListIdx = 0;
        for (Element detailUrl : detailUrlList) {
        	
        	String url = detailUrl.attr("href").trim();
        	String dateStr = articleDateList.get(dateListIdx++).text();
        	dateStr = dateStr.split("·")[1].trim();
        	
        	SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
    		Date date = null;
    		
			try {
				date = dateFormatter.parse(dateStr);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
        	
        	if (date.compareTo(limitDate) > 0) {

            	articleInfoList.add(Maps.of("detailUrl", url,"articleDate",date));
            	
            	//System.out.println("날짜 조건 만족 (" + dateStr.toString() + ") -> 수집할 데이터 : " + url);
            	
        	} else {
        		
        		//System.out.println("날짜 조건 불만족 (" + dateStr.toString() + ") -> 더이상 데이터를 수집하지 않음");
        		
        		// 더 이상 기사를 크롤링하지 않으므로 다음 페이지로 넘어가지 않는다.
        		needToCollectDataOnNextPage = false;
        		
        		// DB의 마지막 데이터가 limitDate 이므로 동일 페이지에서 일치하거나 이전의 일자 기사는 더이상 가져오지 않는다.
        		break;
        	}
        }
       
        List<Article> collectedArticles = collectArticles(articleInfoList);
        
		return collectedArticles;
	}
	
	public List<Article> collectArticles(List<Map<String,Object>> articleInfoList) {
		// param 에 담겨온 수집할 기사들의 url 들을 방문해서 필요 데이터 수집 후 반환해준다.
		
        // 크롤링 한 기사들 목록
        List<Article> collectedArticles = new ArrayList<>();
        
        String detailUrl = "";
        Document detailPage = null;
        
        for (Map<String,Object> articleInfo : articleInfoList) {
        	
        	// 기사 본문 스크래핑
            try {
            	detailUrl = (String)articleInfo.get("detailUrl");
            	
        		detailPage = Jsoup.connect(detailUrl).get();
        		detailPage.select("br").append("\\n");
        		
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        	
        	String articleSelector = "body > div.content > div > div.news-container > div";
        	Elements articleHtml = detailPage.select(articleSelector);
        	
        	// header - title
        	String articleHeaderSelector = "div.news-header > h1";
        	Elements articleHeaderHtml = articleHtml.select(articleHeaderSelector);
        	String title = articleHeaderHtml.text().trim();
        	
        	// byline - writer, updateDate???
        	String articleWriterSelecetor = "div.news-byline > span.news-byline-writer, div.news-byline > span.news-byline-mail";
        	Elements articleWriterHtml = articleHtml.select(articleWriterSelecetor); //김천 기자 cheon@insight.co.kr 입력 : 2019.10.02 14:00
        	String writer = articleWriterHtml.text().trim();
        	
        	// body
        	String articleBodySelector = "div.news-article-memo > p";
        	Elements articleBodyHtml = articleHtml.select(articleBodySelector);
        	articleBodyHtml.select("p > img, p > span").remove(); // body에서 img 및 img 출처 요소 제거
        	String body = articleBodyHtml.text().replaceAll("\\\\n", "\n").trim();
        	// DB에는 실제 new line만 저장 (<br>제거했음), HTML에서 줄바꿈 원하면 출력전 body에 .replaceAll("\n", "<br>") 추가

      		// <Article> 
        	// 1.id | 2.regDate | 3.sourceId | 4.webPath (detailUrl) | 5.writer | 6.title | 7.body
        	Article collectedArticle = new Article();
        	
        	String[] detailUrlArr = detailUrl.split("/");
        	String id = detailUrlArr[detailUrlArr.length - 1].trim();

        	collectedArticle.setId(id);
        	collectedArticle.setRegDate((Date)articleInfo.get("articleDate"));
        	collectedArticle.setSourceId(sourceId); // 1: 인사이트-패션
        	collectedArticle.setWebPath(detailUrl); 
        	collectedArticle.setTitle(title);
        	collectedArticle.setWriter(writer);
        	collectedArticle.setBody(body); 
        	
        	collectedArticles.add(collectedArticle);
        }
		
		return collectedArticles;
	}
	
}
