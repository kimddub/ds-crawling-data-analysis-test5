package com.company.demo.crawler;

import java.io.IOException;
import java.sql.Time;
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
public class DaumCrawler {
	private final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

	List<Article> uncrawlingArticles;
	SimpleDateFormat dateFormatter;
	boolean continueToCrawling;

	Date limitDate;
	String sectionId;
	String subSectionId;
	String categoryUrl;
	
	//DB site에서 꺼내올것
	String listUrl = "https://news.naver.com/main/list.nhn?mode=LS2D&mid=shm";
	String dateUrl = "&date=";
	String categoryUrl1 = "&sid1=";
	String categoryUrl2 = "&sid2=";
	String timeFormat = "yyyy.MM.dd. a h:mm";
	String lastPageSelector = "#main_content > div.paging > a:last-child, #main_content > div.paging > strong:last-child";
	String mediaSelector = "#main_content > div.list_body.newsflash_body > ul > li > dl > dd > span.writing";
	String detailPageSelector = "#main_content > div.list_body.newsflash_body > ul > li > dl > dt:not(.photo) > a";
	String dateSelector = "#main_content > div.article_header > div.article_info > div > span:last-of-type, #content > div.end_ct > div > div.article_info > span:last-of-type > em";
	String bodySelector = "#articleBodyContents, #articeBody"; //
	String idSelectorInUrl = "aid=";
	
	
	DaumCrawler() {
		//기본 셋팅(공통)
		String dateFormat= "yyyyMMdd";
		dateFormatter = new SimpleDateFormat(dateFormat);
				
	}
	
	// 아직 기사 중복 해결은 안함 (리셋후 넣거나, 이 전 날짜의 기사 읽어오는 로직이 없는 상태이므로)
	public List<Article> getUncrawlingArticles() {
		return uncrawlingArticles;
	}
	
	private void setLimitDate(Date limitDate) {
		if (limitDate == null) {
			Date currentDate = new Date();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDate);
			cal.add(Calendar.DATE, -7);
			
			this.limitDate = cal.getTime();
			
		} else {
		
			this.limitDate = limitDate;
		}
	}
	
	public List<Article> crawling(String sectionId, String subSectionId, Date limitDate) {
		uncrawlingArticles = new ArrayList<>();
		
		this.sectionId = sectionId;
		this.subSectionId = subSectionId;
		
		categoryUrl = categoryUrl1 + sectionId + categoryUrl2 + subSectionId;
		
		setLimitDate(limitDate);
		
		// 업데이트 되도 그 자리에 있다.
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
		//목록에서 수집 날짜 시간으로 거를 수 있다. (근데 원문 날짜다. 수정날짜로 정렬은 하지만 목록엔 원문시간이 표시되있다.)
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

		String limitDateStr = dateFormatter.format(limitDate); // 페이지 url 파라미터 및 크롤링 멈출 날짜
		Date limitTime = (Date)limitDate.clone(); // 크롤링 멈출 날짜 페이지에서 시간 비교
		int limitPage = 1; // 날짜 당기거나 크롤링 멈출 마지막 페이지
		
		Date today = new Date();
		String crawlingDate = dateFormatter.format(today); // 오늘 날짜부터 url 파라미터에서 크롤링 시작
		int crawlingPage = 0; // 최근 페이지부터 크롤링 시작

		continueToCrawling = true;
		boolean isLastDate = false; // 현재 날짜 조건이 크롤링 멈출 날짜일 때
		boolean isLastPage = false; // 현재 페이지 조건이 마지막 페이지이거나, 크롤링 멈출 마지막 페이지일 때

		List<Article> articles = new ArrayList<>(); // 크롤링 담을 게시물 리스트
        List<Map<String,String>> articleInfoList = null; // 본문 크롤링을 위한 url, 언론사 등 정보 리스트
        
        while(continueToCrawling) {
        	
        	if (crawlingPage == limitPage) { // 마지막 페이지를 크롤링 후
        		isLastPage = true;
        	}
        	
        	if (isLastDate && isLastPage) { // 크롤링 마지막 날짜에 마지막 페이지까지 크롤링 후
        		
        		System.out.println("이 에러가 나오면 예외처리 해야함");
        		break;
        	}
        	
        	if (isLastPage) { // 마지막 페이지이면 날짜를 당긴다
        		Date prevDate = null;
				
		        try {
		        	
					prevDate = dateFormatter.parse(crawlingDate);
					
					Calendar cal = Calendar.getInstance();
					
					cal.setTime(prevDate);
					cal.add(Calendar.DATE, -1);
					crawlingDate = dateFormatter.format(cal.getTime());

	        		crawlingPage = 0; // 크롤링 전 페이지 넘기므로
	        		isLastPage = false;
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
        	}
        	
        	// 크롤링 멈출 날짜의 리스트
        	if (crawlingDate.contains(limitDateStr) || crawlingDate.trim().equals(limitDateStr)) {
        		isLastDate = true;
        	}

        	crawlingPage++; // 페이지를 넘김
        	
        	// 1. 조건의 페이지를 크롤링
        	Document listPage = getListPage(crawlingDate, crawlingPage);
        	
        	// 날짜별로 마지막 페이지가 다르므로 한번 가져옴
        	if (crawlingPage == 1) {
        		limitPage = getLastPageNum(listPage); 
        	}
        	
        	// 2. 현재 페이지의 게시글 본문 크롤링을 위한 정보를 담아옴
        	articleInfoList = getArticleInfoListFromOnePage(listPage);
        	
        	
        	if (isLastDate) {
        		
        		// 3. 마지막 날짜일때만 게시물 본문에서 멈출 시간의 기사인지 비교하면서 담아옴
        		articles.addAll(getArticlesFromDetailUrlWithinDate(articleInfoList, limitTime)); // 기사 목록에 쌓기
        		
        	} else {
        		
        		// 3. 페이지의 기사를 모두 담아옴 -> 모두 insert/ update
        		articles.addAll(getArticlesFromDetailUrl(articleInfoList)); // 기사 목록에 쌓기
        		
        		/*
        		 오늘이 11일 이고, 최근 가져온 기사의 날짜가 10일 일때,
        		 네이버는 오늘 날짜의 기사목록에 오늘날의 기사가 없다면
        		 첫페이지는 이전 일자의 기사들을 가져와서 20개 목록을 채운다.
        		 그래서 나의 로직은 10일의 기사목록 전에는 모든 리스트를 가져와서 담으므로
        		 DB insert시 중복 기사 자동 업데이트를 해준다.
        		 */
        	}
        }
        
        return articles;
	}
	
	private Document getPage(String url) {
		Document page = null;
		try {
			
			page = Jsoup.connect(url)
							.userAgent(USER_AGENT)
							.get();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return page;
	}
	
	// 1. 기사목록 페이지 크롤링
	private Document getListPage(String crawlingDate, int crawlingPage) {
		// System.out.println("크롤링 할 일자: " + crawlingDate + ", 페이지: " + crawlingPage);
		
		// 크롤링 입력값 셋팅
		String pageUrl = "&page=" + crawlingPage;
		String dateUrl = "&date=" + crawlingDate;
		
		return getPage(listUrl+categoryUrl+pageUrl+dateUrl);
	}

	private int getLastPageNum(Document listPage) {
		Elements lastPageNums = listPage.select(lastPageSelector);
		
		int lastPageNum = 1;
		
		for (Element PageNum:lastPageNums) {
			lastPageNum = Integer.parseInt(lastPageNums.text().trim());
		}
		
		return lastPageNum;
	}
	
	// 3. 기사본문 크롤링 (시간비교)
	private List<Article> getArticlesFromDetailUrlWithinDate(List<Map<String,String>> articleInfoList, Date limitTime) {
		List<Article> articles = new ArrayList<>();
		
		Document detailPage = null;
		String detailPageUrl = "";
		
		for (Map<String,String> articleInfo:articleInfoList) {

			detailPageUrl = (String)articleInfo.get("detailPageUrl");
			detailPage = getPage(detailPageUrl);
			
			Elements dateElement = detailPage.select(dateSelector);
			String dateStr = dateElement.text().trim();
			
			Date date = null;
			
			if (dateStr.length() != 0) {
				// 기사 본문 시간과 크롤링 멈출 시간인지 비교위해 date로 변환
				SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
				
				try {
					date = timeFormatter.parse(dateStr);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			
			// 최신 데이터의 시간과 같거나 이전 시간의 기사 나타남 
			if (date.compareTo(limitTime) < 1) {
				System.out.println("stop to crawling - already crawling data time");
				
				continueToCrawling = false;
				return articles;
			}
			
			//계속 기사 긁음
			String body = detailPage.select(bodySelector).text();
			
			String id = detailPageUrl.split(idSelectorInUrl)[1];
			
			Article article = new Article();
			
			article.setId(id);
			article.setExtra(Maps.of("media",(String)articleInfo.get("media")));
			article.setWebPath(detailPageUrl);
			article.setRegDate(date);
//			article.setTitle(title);
			article.setBody(body);
			
			// 크롤링한 기사의 본문이 긁히지 않았다면 DB에 입력하지 않는다.
			if (article.getBody().length()==0 || article.getRegDate()==null) {
				uncrawlingArticles.add(article);
			} else {
				articles.add(article);
			}
		}
		
		return articles;
		
	}
	
	// 3. 기사본문 크롤링
	private List<Article> getArticlesFromDetailUrl(List<Map<String,String>> articleInfoList) {
		List<Article> articles = new ArrayList<>();
		
		Document detailPage = null;
		String detailPageUrl = "";
		
		for (Map<String,String> articleInfo:articleInfoList) {

			detailPageUrl = (String)articleInfo.get("detailPageUrl");
			detailPage = getPage(detailPageUrl);
			
			Elements dateElement = detailPage.select(dateSelector);
			String dateStr = dateElement.text().trim();
			
			Date date = null;
			
			if (dateStr.length() != 0) {
				// 기사 본문 시간과 크롤링 멈출 시간인지 비교위해 date로 변환
				SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
				
				try {
					date = timeFormatter.parse(dateStr);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			
			//계속 기사 긁음
			String body = detailPage.select(bodySelector).text();
			
			String id = detailPageUrl.split(idSelectorInUrl)[1];
			
			Article article = new Article();
			
			article.setId(id);
			article.setExtra(Maps.of("media",(String)articleInfo.get("media")));
			article.setWebPath(detailPageUrl);
			article.setRegDate(date);
//			article.setTitle(title);
			article.setBody(body);
			
			// 크롤링한 기사의 본문이 긁히지 않았다면 DB에 입력하지 않는다.
			if (article.getBody().length()==0 || article.getRegDate()==null) {
				uncrawlingArticles.add(article);
			} else {
				articles.add(article);
			}
		}
		
		return articles;
		
	}
	
	// 2. 기사 정보 크롤링
	private List<Map<String,String>> getArticleInfoListFromOnePage(Document listPage) {
		List<Map<String,String>> ArticleInfoList = new ArrayList<>();
		
		List<String> mediaList = new ArrayList<>();
        List<String> detailPageList = new ArrayList<>();

		Elements mediaListHTML = listPage.select(mediaSelector);
		
		for (Element media:mediaListHTML) {
			mediaList.add(media.text().trim());
		}
		
		Elements articleList = listPage.select(detailPageSelector);
		
		int idx = 0;
        for (Element detailUrl : articleList) {
        	
        	String url = detailUrl.attr("href").trim();
        	
        	ArticleInfoList.add(Maps.of("detailPageUrl",url,"media",mediaList.get(idx++)));
        }
        
        return ArticleInfoList;
	}

	
	
}
