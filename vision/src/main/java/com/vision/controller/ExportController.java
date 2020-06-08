package com.vision.controller;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vision.wb.ExportWb;


@RestController
@RequestMapping(value = "exportFiles")
public class ExportController {
	
	@Autowired
	ExportWb exportWb;

	@RequestMapping(path="/generateReport", method=RequestMethod.GET)
	public HttpServletResponse generateReport(HttpServletRequest request,HttpServletResponse response) throws FileNotFoundException, IOException {
	     
		
		 String excelPath = exportWb.generateExcel();
	     response.setContentType("application/vnd.ms-excel");
	     response.setHeader("Content-Disposition", "attachment; filename="+excelPath);
		return response;
	}
	
}
