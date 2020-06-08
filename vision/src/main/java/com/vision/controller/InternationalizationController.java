package com.vision.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vision.exception.JSONExceptionCode;

@RestController
public class InternationalizationController {

	@Autowired
	private Environment env;
	
	@RequestMapping(path="/sdssd" , method=RequestMethod.GET)
	public String getLanguages(@RequestHeader("Accept-Language") String locale){
		
		String dynamicLanguage=env.getProperty("greeting");
		System.out.println(env.getProperty("greeting"));
		System.out.println("...."+("greeting")+"...");
		return dynamicLanguage;
		
	}
	
	
}
