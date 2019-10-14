package com.company.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

@Controller
public class TextProcessorController {
	
	@RequestMapping("test1")
	@ResponseBody
	public String test1() {
		 Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
        String strToAnalyze = "이영자가 먹었던 초코쿠키칩이 대세다.";

        KomoranResult analyzeResultList = komoran.analyze(strToAnalyze);
        
        for (String noun:analyzeResultList.getNouns()) {
        	System.out.println(noun);
        }
        
		return "";
	}
}
