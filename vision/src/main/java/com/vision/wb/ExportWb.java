package com.vision.wb;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
public class ExportWb {

	public String generateExcel() throws FileNotFoundException, IOException {
		    XSSFWorkbook workbook = new XSSFWorkbook();
	        XSSFSheet sheet = workbook.createSheet("Example");
	        Object[][] bookData = {
	                {"Head First Java", "Kathy Serria", 79},
	                {"Effective Java", "Joshua Bloch", 36},
	                {"Clean Code", "Robert martin", 42},
	                {"Thinking in Java", "Bruce Eckel", 35},
	        };
	 
	        int rowCount = 0;
	        for (Object[] aBook : bookData) {
	            Row row = sheet.createRow(++rowCount);
	             
	            int columnCount = 0;
	            for (Object field : aBook) {
	                Cell cell = row.createCell(++columnCount);
	                if (field instanceof String) {
	                    cell.setCellValue((String) field);
	                } else if (field instanceof Integer) {
	                    cell.setCellValue((Integer) field);
	                }
	            }
	        }
	        try (FileOutputStream outputStream = new FileOutputStream("C:/SSBI_TEMP_DIR/TempExample.xlsx")) {
	            workbook.write(outputStream);
	        }
         return "C:/SSBI_TEMP_DIR/TempExample.xlsx";
          }
}