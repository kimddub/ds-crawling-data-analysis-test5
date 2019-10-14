package com.company.demo.dto;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {
	private int code;
	private String id;
	private int siteCode;
	private int mediaCode;
	private String webPath;
	private Date regDate;
	private Date colDate;
	private String body;
	private boolean analysisState;
	private Map<String,Object> extra;
}
