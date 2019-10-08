package com.company.demo.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {
	private int id;
	private Date regDate;
	private int sourceId;
	private String webPath;
	private String title;
	private String body;
	private String writer;
	private Date colDate;
	
	// <Article> Id | regDate | sourceId | WebPath (detailUrl) | title | body | writer | colDate
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setId(String id) {
		this.id = Integer.parseInt(id);
	}
	
	
}
