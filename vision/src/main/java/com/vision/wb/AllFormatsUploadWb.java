package com.vision.wb;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bouncycastle.tsp.GenTimeAccuracy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.vision.dao.DSConnectorDao;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.FileInfoVb;
import com.vision.vb.VcMainDataSourceMetaDataVb;
import com.vision.vb.XmlJsonUploadVb;

@Component
public class AllFormatsUploadWb{
	
	@Autowired
	DSConnectorDao dSConnectorDao;
	int YES =0;
	int NO =1;

	/*-------------------------------------Process file based on extension-------------------------------------------*/
	public JSONExceptionCode processFile(FileInfoVb fileInfoVb, String method) throws DataAccessException, SAXException, IOException, ParserConfigurationException {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			String content = new String(fileInfoVb.getData(), "UTF-8");
			
			if("json".equalsIgnoreCase(fileInfoVb.getExtension())) {
				jsonExceptionCode = getJSONDataString(content, method);
			}else if("xml".equalsIgnoreCase(fileInfoVb.getExtension())){
				content = content.replaceAll("\n", "").replaceAll("\r", "");
				content = content.replaceAll(">\\s*<", "><");
				jsonExceptionCode = getXMLDataString(content, method);
			}else if("txt".equalsIgnoreCase(fileInfoVb.getExtension()) || "csv".equalsIgnoreCase(fileInfoVb.getExtension())){
				jsonExceptionCode = getTxtCsvDataString(content,fileInfoVb, method);
			}else if("XLSX".equalsIgnoreCase(fileInfoVb.getExtension()) || "XLS".equalsIgnoreCase(fileInfoVb.getExtension())){
				jsonExceptionCode = getXlDataString(content, fileInfoVb, method);
			}
		} catch(RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", rex.getMessage());
		} catch(SAXException e) {
            Exception x = e;
            if (e.getException() != null) {
                  x = e.getException();
            }
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", x);
		 }catch (IOException e) {
            e.printStackTrace();
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", "Input Output Error");
		 }
		return jsonExceptionCode;
	}
	
	public JSONExceptionCode processFileForDataInsert(FileInfoVb fileInfoVb, String method){
		try {
			JSONExceptionCode jsonExceptionCode = null;
			String content = new String(fileInfoVb.getData(), "UTF-8");
			
			if("json".equalsIgnoreCase(fileInfoVb.getExtension())) {
				jsonExceptionCode = getJSONDataStringForDataInsert(content,fileInfoVb, method); //verified -Testing
			}else if("xml".equalsIgnoreCase(fileInfoVb.getExtension())){
				content = content.replaceAll("\n", "").replaceAll("\r", "");
				content = content.replaceAll(">\\s*<", "><");
				jsonExceptionCode = getXMLDataStringForDataInsert(content,fileInfoVb, method);
			}else if("txt".equalsIgnoreCase(fileInfoVb.getExtension()) || "csv".equalsIgnoreCase(fileInfoVb.getExtension())){
				jsonExceptionCode = getTxtCsvDataStringForDataInsert(content,fileInfoVb, method);
			}else if("XLSX".equalsIgnoreCase(fileInfoVb.getExtension()) || "XLS".equalsIgnoreCase(fileInfoVb.getExtension())){
				jsonExceptionCode = getXlDataStringForDataInsert(content, fileInfoVb, method);
			}
			return jsonExceptionCode;
		} catch(Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	/*-------------------------------------XLS/XL Data Parsing-------------------------------------------*/
	
	public JSONExceptionCode getXlDataString(String content, FileInfoVb fileInfoVb, String method) throws DataAccessException {
		JSONExceptionCode jsonExceptionCode = null;
		List<VcMainDataSourceMetaDataVb> returnList = new ArrayList<VcMainDataSourceMetaDataVb>();;
		if (ValidationUtil.isValid(content)) {
			LinkedHashMap< String, Object> hashMap = new LinkedHashMap < String, Object> ();
			int rowCount = 0;
			String tableName =fileInfoVb.getSheetName();
			try {
				InputStream fileReader = new ByteArrayInputStream(fileInfoVb.getData());
				Workbook workbook = new XSSFWorkbook(fileReader);
				 //Get all sheet names in the workbook
				int sheetIndex=0;
				Sheet firstSheet = workbook.getSheetAt(sheetIndex);
				List < Object > collsheetNames = new ArrayList < Object > ();
				
				for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
	            	collsheetNames.add(workbook.getSheetName(s));
	            	if(!ValidationUtil.isValid(method)) {
	            		firstSheet = workbook.getSheetAt(s);		
		            	if(ValidationUtil.isValid(tableName) && tableName.equalsIgnoreCase(workbook.getSheetName(s))) {
		            		sheetIndex= s;
		            		firstSheet = workbook.getSheetAt(sheetIndex);
		            	}
					}else {
		            	if(ValidationUtil.isValid(tableName) && tableName.equalsIgnoreCase(workbook.getSheetName(s))) {
		            		sheetIndex= s;
		            	}else if(ValidationUtil.isValid(tableName) && !tableName.equalsIgnoreCase(workbook.getSheetName(s))) {
		            		continue;
		            	}
		            	firstSheet = workbook.getSheetAt(sheetIndex);
		            }
            	hashMap.put("TABLENAMES", collsheetNames);
				hashMap.put("TABLENAME", firstSheet.getSheetName());
				System.out.println("TableNames: "+firstSheet.getSheetName());
				List colHeaderList = new ArrayList();
				Iterator<Row> iterator = firstSheet.iterator();
				List<VcMainDataSourceMetaDataVb> childList = new ArrayList<VcMainDataSourceMetaDataVb>();
				while (iterator.hasNext()) {
					Row nextRow = iterator.next();
					if(nextRow.getRowNum() != 0){
						break;
					}
					Iterator<Cell> cellIterator = nextRow.cellIterator();
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						if(ValidationUtil.isValid(cell.getStringCellValue()) && !cell.getStringCellValue().matches("[ ]*")){
							XmlJsonUploadVb XmlJsonUploadVb = new XmlJsonUploadVb();
			        		XmlJsonUploadVb.setData(cell.getStringCellValue());
			        		System.out.print(cell.getStringCellValue() +"  ");
			        		colHeaderList.add(XmlJsonUploadVb);
			        		if(!ValidationUtil.isValid(method)) {
			        			childList.add(new VcMainDataSourceMetaDataVb(workbook.getSheetName(s), cell.getStringCellValue(), null));
			        		}
						} else {
							return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", "Column headings are not valid for the provided file");
						}
					}
					if(!ValidationUtil.isValid(method)) {
						returnList.add(new VcMainDataSourceMetaDataVb(workbook.getSheetName(s), "", childList));
					}
				}
				System.out.println();
				fileReader.close();
				int colCount = colHeaderList.size();
				hashMap.put("COLUMNS", colHeaderList);
	            
            	if(ValidationUtil.isValid(method) && "PREVIEW".equalsIgnoreCase(method)) {
					// Get a list of all rows in the document
					rowCount = firstSheet.getLastRowNum();
					FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
					int exactrowNum=1;
					LinkedHashMap< String, Object> rowMap = new LinkedHashMap < String, Object> ();
					for(int rowIndex=1; rowIndex <= rowCount; rowIndex++){
						Row rowData =firstSheet.getRow(rowIndex);
						if(ValidationUtil.isValid(rowData)){
							int rowColumnCnt=firstSheet.getRow(rowIndex).getLastCellNum();
							if(rowColumnCnt > colCount){
		 						return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", "Invalid no. of Columns found on the Line ["+exactrowNum+"] in file");
							}else {
								rowColumnCnt=colCount;
							}
							List colDataList = new ArrayList();
			           		for(int colIndex = 0;colIndex < rowColumnCnt;colIndex++){
			           			Cell cell = rowData.getCell(colIndex);
			           			String specificData="";
			           			
						        if(ValidationUtil.isValid(cell) && ValidationUtil.isValid(evaluator.evaluate(cell))){
						        	int cellType = evaluator.evaluate(cell).getCellType();
						        	if (cellType == Cell.CELL_TYPE_STRING) {
						        		specificData=  cell.getStringCellValue();
						        	}else if(cellType == Cell.CELL_TYPE_NUMERIC || cellType == Cell.CELL_TYPE_FORMULA){
						        		if (DateUtil.isCellDateFormatted(cell)) {
						        				 try{
						        					 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
						        					 specificData =  String.valueOf(sdf.format(cell.getDateCellValue()));
						        				 }catch(Exception e){
						        					 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
						        					 specificData =  String.valueOf(sdf.format(cell.getDateCellValue()));
						        				 }
						        		}else{
											 specificData=  String.valueOf(cell.getNumericCellValue());
						        			 if(specificData.contains("E")){
						        				 double num = rowData.getCell(colIndex).getNumericCellValue();	
						        				 DecimalFormat pattern = new DecimalFormat("##########");	
						        				 NumberFormat testNumberFormat = NumberFormat.getNumberInstance(); 
						        				 specificData = testNumberFormat.format(num).replaceAll(",", "");	
						        			 }
											 String[] TempspecificData = specificData.split(Pattern.quote("."));
											 String TempspecificData1 = TempspecificData[0];
											 String TempspecificData2 = "";
											 if(TempspecificData.length > 1)
												 TempspecificData2 = TempspecificData[1];
											 int part =0;
											 try{
												 part=Integer.valueOf(TempspecificData2);
												 if(part<=0)
													 specificData =  String.valueOf((int) Math.round(rowData.getCell(colIndex).getNumericCellValue()));
											 }catch(Exception e){
												 if(TempspecificData2.equalsIgnoreCase("0"))
													 specificData =  String.valueOf((int) Math.round(rowData.getCell(colIndex).getNumericCellValue()));
											}
											 
										 }
									 }
						        }else{
						        	specificData = " ";
						        }
			           			XmlJsonUploadVb XmlJsonUploadVb = new XmlJsonUploadVb();
				        		XmlJsonUploadVb.setData(specificData);
				        		System.out.print(specificData);
				        		colDataList.add(XmlJsonUploadVb);
			           		}
			           		rowMap.put("ROW" + exactrowNum + "", colDataList);
							exactrowNum++;
						}
					}
					hashMap.put("ROWS", rowMap);
				  }
				}
	    		if(!ValidationUtil.isValid(method)) {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", returnList);
	    		}else {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", hashMap);
	    		}
			}  catch(Exception e) {
				return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", e.getMessage());
			}
		}
		return jsonExceptionCode;
	}
	
	public JSONExceptionCode getXlDataStringForDataInsert(String content, FileInfoVb fileInfoVb, String method) {
		JSONExceptionCode jsonExceptionCode = null;
		List<VcMainDataSourceMetaDataVb> returnList = new ArrayList<VcMainDataSourceMetaDataVb>();
		
		if (ValidationUtil.isValid(content)) {
			int rowCount = 0;
            List<String> tableNames = new ArrayList<String>();
			try {
				InputStream fileReader = new ByteArrayInputStream(fileInfoVb.getData());
				Workbook workbook = null;
				if("xlsx".equalsIgnoreCase(fileInfoVb.getExtension()))
					workbook = new XSSFWorkbook(fileReader);
				else
					workbook = new HSSFWorkbook(fileReader);
				// Get all sheet names in the workbook
				int sheetIndex = 0;
		        int row=0;
				// Dynamic Table Source Creation Object
				StringBuffer tableCreationScript = null;

				// dynamic Table Name Creation Object
				StringBuffer dynamicTableName = null;

				// dynamic insert query Creation Object
				StringBuffer dynamicInsertPrefixQuery = null;
				StringBuffer dynamicInsertSufixQuery = null;
				DSConnectorVb dsConnectorVb = new DSConnectorVb();
				dsConnectorVb.setMacroVar(fileInfoVb.getMacroVar());
				long numberIncr = 0;

				for (int s = 0; s < workbook.getNumberOfSheets(); s++) {

					Sheet firstSheet = workbook.getSheetAt(s);
					Iterator<Row> iterator = firstSheet.iterator();
					if(iterator.hasNext()) {
						dynamicTableName = new StringBuffer("VCDF_A");
						dsConnectorVb.setTableName(firstSheet.getSheetName());
						numberIncr = dSConnectorDao.generateTableSequence();
						dSConnectorDao.dynamicTableNameCreator(dynamicTableName, numberIncr);
						tableNames.add(dynamicTableName.toString());
						dSConnectorDao.doInsertOperationForUploadMappingTable(dsConnectorVb, dynamicTableName);
					}
					int colCount = 0;
					while (iterator.hasNext()) {
						Row nextRow = iterator.next();
						if (nextRow.getRowNum() != 0) {
							break;
						}
						Iterator<Cell> cellIterator = nextRow.cellIterator();
						tableCreationScript = new StringBuffer("Create table ");
						dynamicInsertPrefixQuery = new StringBuffer("Insert into " + dynamicTableName + " (");
						dynamicInsertSufixQuery = new StringBuffer("values (");

						tableCreationScript.append(dynamicTableName + " ( ");
				if(fileInfoVb.getHeaderCheck() =='Y')
					{	
						while (cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							if (ValidationUtil.isValid(cell.getStringCellValue())
									&& !cell.getStringCellValue().matches("[ ]*")) {
								
							  	String columnStr = cell.getStringCellValue().replaceAll("\\W", "_"); 
			 					if(columnStr.length() >30)
			 					{
			 						columnStr =columnStr.substring(0, 30);
			 					}
			 					
			 					
								if (colCount != 0) {
									tableCreationScript
											.append(", " + columnStr + " VARCHAR2(4000 Byte)");
									dynamicInsertPrefixQuery.append(", " +columnStr);
									dynamicInsertSufixQuery.append(", ?");
								} else {
									tableCreationScript.append(columnStr + " VARCHAR2(4000 Byte)");
									dynamicInsertPrefixQuery.append(columnStr);
									dynamicInsertSufixQuery.append(" ?");
								}

							} else {
								throw new RuntimeCustomException("Column headings are not valid for the provided file");
							}
							colCount++;
						}
						row=1;
					}
				else {		
					row=0;
					int colFlagcnt =0;
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
							if (colFlagcnt != 0) {
								tableCreationScript.append(",COLUMN"+(colFlagcnt+1)+" VARCHAR2(4000 Byte)");
								dynamicInsertPrefixQuery.append(",COLUMN"+(colFlagcnt+1));
								dynamicInsertSufixQuery.append(", ?");
							} else {
								tableCreationScript.append("COLUMN"+(colFlagcnt+1) + " VARCHAR2(4000 Byte)");
								dynamicInsertPrefixQuery.append("COLUMN"+(colFlagcnt+1));
								dynamicInsertSufixQuery.append(" ?");
							}
							colFlagcnt++;

					}
					colCount= colFlagcnt;
				}
						tableCreationScript.append(")");
						dynamicInsertPrefixQuery.append(")");
						dynamicInsertSufixQuery.append(")");
						dynamicInsertPrefixQuery.append(dynamicInsertSufixQuery);

						dSConnectorDao.createDynamicTableandMappingfields(tableCreationScript.toString());
					}
					fileReader.close();

					if (ValidationUtil.isValid(method) && "PREVIEW".equalsIgnoreCase(method)) {
						// Get a list of all rows in the document
						rowCount = firstSheet.getLastRowNum();
						FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
						int exactrowNum = 1;
						try (Connection conn = dSConnectorDao.returnConnection();
								PreparedStatement prpStmnt = conn.prepareStatement(dynamicInsertPrefixQuery.toString());) {
							int batchIndex = 1;
						       
							for (int rowIndex = row; rowIndex <= rowCount; rowIndex++) {
								Row rowData = firstSheet.getRow(rowIndex);
								if (ValidationUtil.isValid(rowData)) {
									int rowColumnCnt = firstSheet.getRow(rowIndex).getLastCellNum();
									if (rowColumnCnt > colCount) {
										deleteDynamicTables(tableNames);
										throw new RuntimeCustomException("Invalid no. of Columns found on the Line ["+ exactrowNum + "] in file");
									} else {
										rowColumnCnt = colCount;
									}
									for (int colIndex = 0; colIndex < rowColumnCnt; colIndex++) {
										Cell cell = rowData.getCell(colIndex);
										String specificData = "";

										if (ValidationUtil.isValid(cell)
												&& ValidationUtil.isValid(evaluator.evaluate(cell))) {
											int cellType = evaluator.evaluate(cell).getCellType();
											if (cellType == Cell.CELL_TYPE_STRING) {
												specificData = cell.getStringCellValue();
											} else if (cellType == Cell.CELL_TYPE_NUMERIC
													|| cellType == Cell.CELL_TYPE_FORMULA) {
												if (DateUtil.isCellDateFormatted(cell)) {
													try {
														SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
														specificData = String
																.valueOf(sdf.format(cell.getDateCellValue()));
													} catch (Exception e) {
														SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
														specificData = String
																.valueOf(sdf.format(cell.getDateCellValue()));
													}
												} else {
													specificData = String.valueOf(cell.getNumericCellValue());
													if (specificData.contains("E")) {
														double num = rowData.getCell(colIndex).getNumericCellValue();
														DecimalFormat pattern = new DecimalFormat("##########");
														NumberFormat testNumberFormat = NumberFormat
																.getNumberInstance();
														specificData = testNumberFormat.format(num).replaceAll(",", "");
													}
													String[] TempspecificData = specificData.split(Pattern.quote("."));
													String TempspecificData1 = TempspecificData[0];
													String TempspecificData2 = "";
													if (TempspecificData.length > 1)
														TempspecificData2 = TempspecificData[1];
													int part = 0;
													try {
														part = Integer.valueOf(TempspecificData2);
														if (part <= 0)
															specificData = String.valueOf((int) Math.round(
																	rowData.getCell(colIndex).getNumericCellValue()));
													} catch (Exception e) {
														if (TempspecificData2.equalsIgnoreCase("0"))
															specificData = String.valueOf((int) Math.round(
																	rowData.getCell(colIndex).getNumericCellValue()));
													}
												}
											}
										} else {
											specificData = " ";
										}
										prpStmnt.setString(colIndex + 1, specificData);
//										System.out.print(specificData);
									}
									prpStmnt.addBatch();
									exactrowNum++;
								}
								if (batchIndex == 5000) {
									prpStmnt.executeBatch();
									batchIndex = 0;
								}
								batchIndex++;
							}
							prpStmnt.executeBatch();
					
						} catch (Exception ex) {
                            deleteDynamicTables(tableNames);
                            throw new RuntimeCustomException(ex.getMessage());
						}

					}
				}
			} catch (Exception e) {
				deleteDynamicTables(tableNames);
				throw new RuntimeCustomException(e.getMessage());
			}
		}
		jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", "Success");
		return jsonExceptionCode;
	}
	/*-------------------------------------TXT/CSV Data Parsing-------------------------------------------*/
	
	private void deleteDynamicTables(List<String> tableNames) {
		if (ValidationUtil.isValidList(tableNames)) {
			tableNames.forEach(tableName -> {
				dSConnectorDao.dropTablesForUpload(tableName);
			});		
	    }
	}
	public int chkForAllSpaces(String paramStrTxt){
		char[] paramStr = paramStrTxt.toCharArray();
		int returnValue = NO;
		for(int Ctr1=0;  Ctr1 < paramStr.length; Ctr1++)
		{	if(paramStr[Ctr1]==' ')
				returnValue =YES;
			if(!ValidationUtil.isValid(paramStr[Ctr1]))
				returnValue = YES;
			return returnValue;
		}
		return returnValue;
	}

	public JSONExceptionCode getTxtCsvDataString(String content, FileInfoVb fileInfoVb, String method) throws DataAccessException {
		JSONExceptionCode jsonExceptionCode = null;
		List<VcMainDataSourceMetaDataVb> returnList = new ArrayList<VcMainDataSourceMetaDataVb>();;
		if (ValidationUtil.isValid(content)) {
			String fileExtension= fileInfoVb.getExtension();
			String delimiter= fileInfoVb.getDelimiter();
			String fileName= fileInfoVb.getName();
			try {
				String headerRow = "";
				InputStream fileReader = new ByteArrayInputStream(fileInfoVb.getData());
				BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(fileReader,"UTF8"));

				bufferedReader1.mark(1); //If there is a Persian Words the First Char in the Data File is reading some Junk Chars- To avoid these lines are used.
	            if(bufferedReader1.read()!=0xFEFF)
	            	bufferedReader1.reset();
	            headerRow = bufferedReader1.readLine();
	            if(headerRow == "" || headerRow == null){
	            	return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", "COLUMNS not specified");
				}
	            
	        	int colCount = 0;
				LinkedHashMap< String, Object> hashMap = new LinkedHashMap < String, Object> ();
				int longestColumn=0;
				String msg="";
				String tempStr="";
	 			char firstTimeFlag = 'Y';
	 			ArrayList fileColumnsNames = new ArrayList();

	 			String splitBy = "\\t";
	 				splitBy=""+delimiter+"(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";

				// Get a list of all column names in the document
				String[] columnDataArray = headerRow.split(splitBy);
				colCount = columnDataArray.length;

				for (int Ctr1 = 0; Ctr1 < colCount; Ctr1++) {
 					tempStr = columnDataArray[Ctr1];
 					tempStr = tempStr.toUpperCase();
 					if (tempStr.length() > longestColumn)
 						longestColumn = tempStr.length();
 					
 					if (tempStr.length() == 0){
 						msg ="An extra tab or space(s) is found in the header lines !!";
 					}else if (chkForAllSpaces(tempStr) == YES){
 						msg =tempStr+" - A column heading with just spaces is found in the header line !!";
 					}
 					/*if(Arrays.asList(exceptionColList).contains(tempStr)){
 						if(firstTimeFlag== 'Y'){
 	 						msg =tempStr + " - The following columns should not be given in the input file !!";
							firstTimeFlag = 'N';
 						}
 					}*/
 					if(ValidationUtil.isValid(msg)) {
 						return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", msg);
 					}
					fileColumnsNames.add(tempStr); // add [tempStr] to [fileColumns] array list
 					tempStr = "";
 				}
				hashMap.put("TABLENAMES", new String[] {fileName});
				hashMap.put("TABLENAME", fileName);
	     		List < Object > colHeaderList = new ArrayList < Object > ();
				List<VcMainDataSourceMetaDataVb> childList = new ArrayList<VcMainDataSourceMetaDataVb>();
				for(int i=0; i<columnDataArray.length; i++) {
					XmlJsonUploadVb columnVb = new XmlJsonUploadVb();
	            	columnVb.setData(columnDataArray[i]);
					colHeaderList.add(columnVb);
					colCount++;
					if(!ValidationUtil.isValid(method)) {
	        			childList.add(new VcMainDataSourceMetaDataVb(fileName, columnDataArray[i], null));
	        		}
				}
				if(!ValidationUtil.isValid(method)) {
					returnList.add(new VcMainDataSourceMetaDataVb(fileName, "", childList));
				}
				hashMap.put("COLUMNS", colHeaderList);
				
				// Get a list of all rows in the document
				ArrayList<String> lines = new ArrayList<String>();
		        String lineFetched = null;
		        String[] wordsArray = null;
		        while(true){
		        	lineFetched = bufferedReader1.readLine();
		            if(lineFetched == null){  
		                break;
		            }else{
		                wordsArray = lineFetched.split("\n");
		                for(String each : wordsArray){
		                	lines.add(each);
		                }
		            }
		        }
	            bufferedReader1.close();
            	if(ValidationUtil.isValid(method) && "PREVIEW".equalsIgnoreCase(method)) {
					int exactrowNum = 1;
					LinkedHashMap< String, Object> rowMap = new LinkedHashMap < String, Object> ();
			        for(int Ctr1=0;Ctr1 < lines.size();Ctr1++){
			        	String[] rowDataArray = lines.get(Ctr1).split(splitBy);
			        	if(rowDataArray.length > fileColumnsNames.size()){
			        		msg ="An Extra Column found on the Header Line ["+Ctr1+"] in file";
			        	}
			        	if(ValidationUtil.isValid(msg)) {
	 						return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", msg);
	 					}
						List < Object > colDataList = new ArrayList < Object > ();
			        	for(int k=0; k<rowDataArray.length; k++) {
			        		XmlJsonUploadVb XmlJsonUploadVb = new XmlJsonUploadVb();
			        		XmlJsonUploadVb.setData(rowDataArray[k]);
			        		colDataList.add(XmlJsonUploadVb);
			        	}
			        	rowMap.put("ROW" + exactrowNum + "", colDataList);
						exactrowNum++;
			        }
					hashMap.put("ROWS",rowMap);
			    }
				if(!ValidationUtil.isValid(method)) {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", returnList);
	    		}else {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", hashMap);
	    		}
			}  catch(Exception e) {
				return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", e.getMessage());
			}
		}
		return jsonExceptionCode;
	}
	
	
	public JSONExceptionCode getTxtCsvDataStringForDataInsert(String content, FileInfoVb fileInfoVb, String method) {
		JSONExceptionCode jsonExceptionCode = null;
		List<VcMainDataSourceMetaDataVb> returnList = new ArrayList<VcMainDataSourceMetaDataVb>();;
		if (ValidationUtil.isValid(content)) {
			String fileExtension= fileInfoVb.getExtension();
			String delimiter= (fileInfoVb.getDelimiter().trim().equals("")?" ":fileInfoVb.getDelimiter().trim()) ;
			delimiter = "\\"+delimiter+"+";
			String fileName= fileInfoVb.getName();
            List<String> tableNames = new ArrayList<String>();
			try {
				String headerRow = "";
				InputStream fileReader = new ByteArrayInputStream(fileInfoVb.getData());
				BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(fileReader,"UTF8"));

				bufferedReader1.mark(1); //If there is a Persian Words the First Char in the Data File is reading some Junk Chars- To avoid these lines are used.
	            if(bufferedReader1.read()!=0xFEFF)
	            	bufferedReader1.reset();
	            headerRow = bufferedReader1.readLine();
	            if(headerRow == "" || headerRow == null){
	            	deleteDynamicTables(tableNames);
	            	throw new RuntimeCustomException("COLUMNS not specified");
				}
	            
	        	int colCount = 0;
				int longestColumn=0;
				String msg="";
				String tempStr="";
	 			char firstTimeFlag = 'Y';
	 			ArrayList<String> fileColumnsNames = new ArrayList<String>();

				// Dynamic Table Source Creation Object
				StringBuffer tableCreationScript = null;

				// dynamic Table Name Creation Object
				StringBuffer dynamicTableName = null;

				// dynamic insert query Creation Object
				StringBuffer dynamicInsertPrefixQuery = null;
				StringBuffer dynamicInsertSufixQuery = null;
				DSConnectorVb dsConnectorVb = new DSConnectorVb();
				dsConnectorVb.setMacroVar(fileInfoVb.getMacroVar());
				long numberIncr = 0;
				
				// Get a list of all column names in the document
				String[] columnDataArray = headerRow.split(delimiter);
				colCount = columnDataArray.length;
				for (int Ctr1 = 0; Ctr1 < colCount; Ctr1++) {
					String hdrContent= columnDataArray[Ctr1];
 					tempStr = hdrContent.replaceAll("\\W", "_"); 
 				
 					tempStr = tempStr.toUpperCase();
 					if(tempStr.length() >30)
 					{
 						tempStr =tempStr.substring(0, 30);
 					}
 					columnDataArray[Ctr1] = tempStr;
 					if (tempStr.length() == 0){
 						msg ="An extra tab or space(s) is found in the header lines !!";
 					}else if (chkForAllSpaces(tempStr) == YES){
 						msg =tempStr+" - A column heading with just spaces is found in the header line !!";
 					}
 					/*if(Arrays.asList(exceptionColList).contains(tempStr)){
 						if(firstTimeFlag== 'Y'){
 	 						msg =tempStr + " - The following columns should not be given in the input file !!";
							firstTimeFlag = 'N';
 						}
 					}*/
 					if(ValidationUtil.isValid(msg)) {
 						deleteDynamicTables(tableNames);
 		            	throw new RuntimeCustomException(msg);
 					}
					fileColumnsNames.add(tempStr); // add [tempStr] to [fileColumns] array list
 					tempStr = "";
 				}
				List<VcMainDataSourceMetaDataVb> childList = new ArrayList<VcMainDataSourceMetaDataVb>();
				dynamicTableName = new StringBuffer("VCDF_A");
				dsConnectorVb.setTableName(fileName);
				numberIncr = dSConnectorDao.generateTableSequence();
				dSConnectorDao.dynamicTableNameCreator(dynamicTableName, numberIncr);
				tableNames.add(dynamicTableName.toString());
				dSConnectorDao.doInsertOperationForUploadMappingTable(dsConnectorVb, dynamicTableName);
			  	tableCreationScript = new StringBuffer("Create table ");
				dynamicInsertPrefixQuery = new StringBuffer("Insert into " + dynamicTableName + " (");
				dynamicInsertSufixQuery = new StringBuffer("values (");
				tableCreationScript.append(dynamicTableName + " ( ");
		if(fileInfoVb.getHeaderCheck() == 'Y')
	    	{
				if(columnDataArray.length == 0 )
				{
					deleteDynamicTables(tableNames);
					throw new RuntimeCustomException("Please provide the header columns");
				}
				for(int i=0; i<columnDataArray.length; i++) {
	            	if (i != 0) {
						tableCreationScript
								.append(", " +columnDataArray[i] + " VARCHAR2(4000 Byte)");
						dynamicInsertPrefixQuery.append(", " + columnDataArray[i]);
						dynamicInsertSufixQuery.append(", ?");
					} else {
						tableCreationScript.append(columnDataArray[i]+ " VARCHAR2(4000 Byte)");
						dynamicInsertPrefixQuery.append(columnDataArray[i]);
						dynamicInsertSufixQuery.append(" ?");
					}
					if(!ValidationUtil.isValid(method)) {
	        			childList.add(new VcMainDataSourceMetaDataVb(fileName, columnDataArray[i], null));
	        		}
				}
				if(!ValidationUtil.isValid(method)) {
					returnList.add(new VcMainDataSourceMetaDataVb(fileName, "", childList));
				}
				
			}else
			  {
				if(columnDataArray.length == 0 )
				{
					deleteDynamicTables(tableNames);
					throw new RuntimeCustomException("Please provide the header columns");
				}
				for(int i =0; i <columnDataArray.length;i++)
				{
					if (i != 0) {
						tableCreationScript
								.append(", column"+(i+1) + " VARCHAR2(4000 Byte)");
						dynamicInsertPrefixQuery.append(",  column"+(i+1));
						dynamicInsertSufixQuery.append(", ?");
					} else {
						tableCreationScript.append("column"+(i+1)+ " VARCHAR2(4000 Byte)");
						dynamicInsertPrefixQuery.append("column"+(i+1));
						dynamicInsertSufixQuery.append(" ?");
					}
					
				}
				
			 	
			  }

				tableCreationScript.append(")");
				dynamicInsertPrefixQuery.append(")");
				dynamicInsertSufixQuery.append(")");
				dynamicInsertPrefixQuery.append(dynamicInsertSufixQuery);

				dSConnectorDao.createDynamicTableandMappingfields(tableCreationScript.toString());
				
				// Get a list of all rows in the document
				ArrayList<String> lines = new ArrayList<String>();
		        String lineFetched = null;
		        String[] wordsArray = null;
		        while(true){
		        	lineFetched = bufferedReader1.readLine();
		            if(lineFetched == null){  
		                break;
		            }else{
		                wordsArray = lineFetched.split("\n");
		                for(String each : wordsArray){
		                	lines.add(each);
		                }
		            }
		        }
	            bufferedReader1.close();
            	if(ValidationUtil.isValid(method) && "PREVIEW".equalsIgnoreCase(method)) {
					try (Connection conn = dSConnectorDao.returnConnection();
							PreparedStatement prpStmnt = conn.prepareStatement(dynamicInsertPrefixQuery.toString());) {
						int batchIndex = 1;
						if(fileInfoVb.getHeaderCheck() !='Y' )
						{ 
							for(int i=0;i<fileColumnsNames.size();i++)
							{
								prpStmnt.setString((i+1), fileColumnsNames.get(i));
							}
							prpStmnt.addBatch();
							batchIndex++;
						}
						for (int Ctr1 = 0; Ctr1 < lines.size(); Ctr1++) {
							String[] rowDataArray = lines.get(Ctr1).split(delimiter);
							if (rowDataArray.length > fileColumnsNames.size()) {
								msg = "An Extra Column found on the Header Line [" +(Ctr1 + 1) + "] in file";
							}
							if (ValidationUtil.isValid(msg)) {
								deleteDynamicTables(tableNames);
								throw new RuntimeCustomException(msg);
							}
							for (int k = 0; k < rowDataArray.length; k++) {
								prpStmnt.setString(k + 1, rowDataArray[k]);
//							System.out.print(specificData);
							}
							prpStmnt.addBatch();
							if (batchIndex == 5000) {
								prpStmnt.executeBatch();
								batchIndex = 0;
							}
							batchIndex++;
						}
						prpStmnt.executeBatch();
					} catch (Exception e) {
						deleteDynamicTables(tableNames);
						throw new RuntimeCustomException(e.getMessage());
					}
			    }
				if(!ValidationUtil.isValid(method)) {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", returnList);
	    		}else {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", "Success");
	    		}
			}  catch(Exception e) {
			     deleteDynamicTables(tableNames); 	
			     throw new RuntimeCustomException(e.getMessage());
			}
		}
		return jsonExceptionCode;
	}
	
	
	/*-------------------------------------XML Data Parsing-------------------------------------------*/
	public JSONExceptionCode getXMLDataString(String getData, String method) throws DataAccessException, SAXException {
		JSONExceptionCode jsonExceptionCode = null;
		List<VcMainDataSourceMetaDataVb> returnList = new ArrayList<VcMainDataSourceMetaDataVb>();;
		if (ValidationUtil.isValid(getData)) {

			try {
				DocumentBuilderFactory dbfactorySubT = DocumentBuilderFactory.newInstance();
				dbfactorySubT.setValidating(false);
				
				String tbNameTag = "<table_name>(.*?)</table_name>";
				String clNameTag = "<columns>(.*?)</columns>";
				String roDataTag = "<row>(.*?)</row>";
				
				Pattern pattern = Pattern.compile(tbNameTag, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				Matcher matcher = pattern.matcher(getData);
			    while(matcher.find()) {
			    	getData = getData.replaceFirst(tbNameTag, "<TABLE_NAME>"+matcher.group(1)+"</TABLE_NAME>");
			    }
				    
			    pattern = Pattern.compile(clNameTag, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			    matcher = pattern.matcher(getData);
			    while(matcher.find()) {
			    	getData = getData.replaceFirst(clNameTag, "<COLUMNS>"+matcher.group(1)+"</COLUMNS>");
			    }
			    
			    pattern = Pattern.compile(roDataTag, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			    matcher = pattern.matcher(getData);
			    while(matcher.find()) {
			    	getData = getData.replaceFirst( "<row>"+matcher.group(1)+"</row>", "<ROW>"+matcher.group(1)+"</ROW>");
			    }

				DocumentBuilder dbSubT = dbfactorySubT.newDocumentBuilder();
				InputSource isSubT = new InputSource();
				isSubT.setCharacterStream(new StringReader(getData));
				Document domSubT = dbSubT.parse(isSubT);
				domSubT.getDocumentElement().normalize();

				String tableName ="";
				int colCount = 0;
				LinkedHashMap< String, Object> hashMap = new LinkedHashMap < String, Object> ();
				NodeList tableNameDef = domSubT.getElementsByTagName("TABLE_NAME");
				if (tableNameDef != null) {
					for (int i = 0; i < tableNameDef.getLength(); i++) {
						tableName = tableNameDef.item(i).getTextContent();
						hashMap.put("TABLENAMES", new String[] {tableName});
						hashMap.put("TABLENAME", tableName);
					}
				}
			
				// Get a list of all column names in the document
				NodeList colData = domSubT.getElementsByTagName("COLUMNS");
				if (colData != null) {
					List<VcMainDataSourceMetaDataVb> vbchildList = new ArrayList<VcMainDataSourceMetaDataVb>();
					for (int i = 0; i < colData.getLength(); i++) {
						NodeList childList = colData.item(i).getChildNodes();
						colCount = childList.getLength();
						int exactcolNum = 1;
						List < Object > colHeaderList = new ArrayList < Object > ();
						for (int j = 0; j < colCount; j++) {
							Node childNode = childList.item(j);
							if ("COLUMN".equalsIgnoreCase(childNode.getNodeName())) {
								XmlJsonUploadVb columnVb = new XmlJsonUploadVb();
				            	columnVb.setData(childNode.getTextContent());
								colHeaderList.add(columnVb);
								if(!ValidationUtil.isValid(method)) {
									vbchildList.add(new VcMainDataSourceMetaDataVb(tableName, childNode.getTextContent(), null));
				        		}
							} else {
								return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", "<" + childNode.getNodeName() + "> tag has been specified instead of <COLUMN> tag in <COLUMNS> tag list [" + exactcolNum + "]");
							}
							exactcolNum++;
						}
						hashMap.put("COLUMNS", colHeaderList);
						if(!ValidationUtil.isValid(method)) {
							returnList.add(new VcMainDataSourceMetaDataVb(tableName, "", vbchildList));
						}
					}
				}
				if (hashMap.size() == 0 || hashMap.size() == 1) {
					return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", "COLUMNS not specified");
				}
            	if(ValidationUtil.isValid(method) && "PREVIEW".equalsIgnoreCase(method)) {
					// Get a list of all rows in the document
					NodeList rowData = domSubT.getElementsByTagName("ROW");
					if (rowData != null) {
						int exactrowNum = 1;
						LinkedHashMap< String, Object> rowMap = new LinkedHashMap < String, Object> ();
						for (int i = 0; i < rowData.getLength(); i++) {
							NodeList childList = rowData.item(i).getChildNodes();
							int eachRowDataCount = childList.getLength();
							if (colCount == eachRowDataCount) {
								List < Object > collTemp = new ArrayList < Object > ();
								for (int j = 0; j < childList.getLength(); j++) {
									XmlJsonUploadVb XmlJsonUploadVb = new XmlJsonUploadVb();
									Node childNode = childList.item(j);
									if ("DCOLUMN".equalsIgnoreCase(childNode.getNodeName())) {
										XmlJsonUploadVb.setData(childNode.getTextContent());
										collTemp.add(XmlJsonUploadVb);
									} else {
										return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", "<" + childNode.getNodeName() + "> tag has been specified instead of <DCOLUMN> tag in <ROW> tag list [" + exactrowNum + "]");
									}
								}
								rowMap.put("ROW" + exactrowNum + "", collTemp);
							} else {
								return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", "Issue in (" + exactrowNum + ") row number. Row data is insufficient");
							}
							exactrowNum++;
						}
						hashMap.put("ROWS",rowMap);
					}
            	}
				if(!ValidationUtil.isValid(method)) {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", returnList);
	    		}else {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", hashMap);
	    		}
			} catch(SAXException e) {
				Exception x = e;
	            if (e.getException() != null) {
	                  x = e.getException();
	            }
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", x);
			} catch(Exception e) {
				return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", e.getMessage());
			}
		}
		return jsonExceptionCode;
	}
	
	
	public JSONExceptionCode getXMLDataStringForDataInsert(String getData, FileInfoVb fileInfoVb, String method) {
		JSONExceptionCode jsonExceptionCode = null;
		List<VcMainDataSourceMetaDataVb> returnList = new ArrayList<VcMainDataSourceMetaDataVb>();;
		if (ValidationUtil.isValid(getData)) {
            List<String> tableNames = new ArrayList<String>();
			try {
				DocumentBuilderFactory dbfactorySubT = DocumentBuilderFactory.newInstance();
				dbfactorySubT.setValidating(false);
				
				String tbNameTag = "<table_name>(.*?)</table_name>";
				String clNameTag = "<columns>(.*?)</columns>";
				String roDataTag = "<row>(.*?)</row>";
				
				Pattern pattern = Pattern.compile(tbNameTag, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				Matcher matcher = pattern.matcher(getData);
			    while(matcher.find()) {
			    	getData = getData.replaceFirst(tbNameTag, "<TABLE_NAME>"+matcher.group(1)+"</TABLE_NAME>");
			    }
				    
			    pattern = Pattern.compile(clNameTag, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			    matcher = pattern.matcher(getData);
			    while(matcher.find()) {
			    	getData = getData.replaceFirst(clNameTag, "<COLUMNS>"+matcher.group(1)+"</COLUMNS>");
			    }
			    
			    pattern = Pattern.compile(roDataTag, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			    matcher = pattern.matcher(getData);
			    while(matcher.find()) {
			    	getData = getData.replaceFirst( "<row>"+matcher.group(1)+"</row>", "<ROW>"+matcher.group(1)+"</ROW>");
			    }

				DocumentBuilder dbSubT = dbfactorySubT.newDocumentBuilder();
				InputSource isSubT = new InputSource();
				isSubT.setCharacterStream(new StringReader(getData));
				Document domSubT = dbSubT.parse(isSubT);
				domSubT.getDocumentElement().normalize();

				String tableName ="";
				int colCount = 0;
				NodeList tableNameDef = domSubT.getElementsByTagName("TABLE_NAME");
				if (tableNameDef != null) {
					for (int i = 0; i < tableNameDef.getLength(); i++) {
						tableName = tableNameDef.item(i).getTextContent();
					}
				}

				// Dynamic Table Source Creation Object
				StringBuffer tableCreationScript = null;

				// dynamic Table Name Creation Object
				StringBuffer dynamicTableName = null;

				// dynamic insert query Creation Object
				StringBuffer dynamicInsertPrefixQuery = null;
				StringBuffer dynamicInsertSufixQuery = null;
				DSConnectorVb dsConnectorVb = new DSConnectorVb();
				dsConnectorVb.setMacroVar(fileInfoVb.getMacroVar());
				long numberIncr = 0;
			  	
				dynamicTableName = new StringBuffer("VCDF_A");
				dsConnectorVb.setTableName(tableName);
				numberIncr = dSConnectorDao.generateTableSequence();
				dSConnectorDao.dynamicTableNameCreator(dynamicTableName, numberIncr);
				tableNames.add(dynamicTableName.toString());
				dSConnectorDao.doInsertOperationForUploadMappingTable(dsConnectorVb, dynamicTableName);
				tableCreationScript = new StringBuffer("Create table ");
				dynamicInsertPrefixQuery = new StringBuffer("Insert into " + dynamicTableName + " (");
				dynamicInsertSufixQuery = new StringBuffer("values (");
				tableCreationScript.append(dynamicTableName + " ( ");
				// Get a list of all column names in the document
				NodeList colData = domSubT.getElementsByTagName("COLUMNS");
				
				if (fileInfoVb.getHeaderCheck() == 'Y') {
					
					List<VcMainDataSourceMetaDataVb> vbchildList = new ArrayList<VcMainDataSourceMetaDataVb>();
	
					if(colData.getLength() ==0)
					{
						deleteDynamicTables(tableNames);
						throw new RuntimeCustomException("'COLUMNS' Tag is not available as per the format ");
					}
					for (int i = 0; i < colData.getLength(); i++) {
						NodeList childList = colData.item(i).getChildNodes();
						colCount = childList.getLength();
						int exactcolNum = 1;


						for (int j = 0; j < colCount; j++) {
							Node childNode = childList.item(j);
							if ("COLUMN".equalsIgnoreCase(childNode.getNodeName())) {
								String textContent = childNode.getTextContent().replaceAll("\\W", "_"); 
			 					if(textContent.length() >30)
			 					{
			 						textContent =textContent.substring(0, 30);
			 					}
				            	if (j != 0) {
									tableCreationScript
											.append(", " +textContent+ " VARCHAR2(4000 Byte)");
									dynamicInsertPrefixQuery.append(", " + textContent);
									dynamicInsertSufixQuery.append(", ?");
								} else {
									tableCreationScript.append(textContent+ " VARCHAR2(4000 Byte)");
									dynamicInsertPrefixQuery.append(textContent);
									dynamicInsertSufixQuery.append(" ?");
								}

								if(!ValidationUtil.isValid(method)) {
									vbchildList.add(new VcMainDataSourceMetaDataVb(tableName, textContent, null));
				        		}
							} else {
								deleteDynamicTables(tableNames);
								throw new RuntimeCustomException("<" + childNode.getNodeName() + "> tag has been specified instead of <COLUMN> tag in <COLUMNS> tag list [" + exactcolNum + "]");
							}
							exactcolNum++;
						}

		
						if(!ValidationUtil.isValid(method)) {
							returnList.add(new VcMainDataSourceMetaDataVb(tableName, "", vbchildList));
						}
					}
					
				}
				else {
					NodeList rowData = domSubT.getElementsByTagName("ROW");
					if(rowData.getLength() == 0) {
						
						deleteDynamicTables(tableNames);
						throw new RuntimeCustomException(" No Datas Found in ROW TAG");
					}
					NodeList childList = rowData.item(0).getChildNodes();
				    if(childList.getLength() ==0)
				    {
				    	deleteDynamicTables(tableNames);
						throw new RuntimeCustomException(" No Datas Found in Row: 1");
				    }
				    colCount = childList.getLength();
					for (int j = 0; j < childList.getLength(); j++) {
						Node childNode = childList.item(j);

						if ("DCOLUMN".equalsIgnoreCase(childNode.getNodeName())) {
							System.out.println(childNode.getTextContent());
						   	if (j != 0) {
								tableCreationScript
										.append(", COLUMN" +(j+1)+ " VARCHAR2(4000 Byte)");
								dynamicInsertPrefixQuery.append(",  COLUMN" +(j+1));
								dynamicInsertSufixQuery.append(", ?");
							} else {
								tableCreationScript.append(" COLUMN" +(j+1)+ " VARCHAR2(4000 Byte)");
								dynamicInsertPrefixQuery.append(" COLUMN" +(j+1));
								dynamicInsertSufixQuery.append(" ?");
							}
							
						} else {
							deleteDynamicTables(tableNames);
							throw new RuntimeCustomException("<"+ childNode.getNodeName()+ "> tag has been specified instead of <DCOLUMN> tag in <ROW> tag list ["+ 1 + "]");
						}
					}
				}
				
				tableCreationScript.append(")");
				dynamicInsertPrefixQuery.append(")");
				dynamicInsertSufixQuery.append(")");
				dynamicInsertPrefixQuery.append(dynamicInsertSufixQuery);

				dSConnectorDao.createDynamicTableandMappingfields(tableCreationScript.toString());
				
				
            	if(ValidationUtil.isValid(method) && "PREVIEW".equalsIgnoreCase(method)) {
					// Get a list of all rows in the document
					NodeList rowData = domSubT.getElementsByTagName("ROW");
	               if(rowData.getLength() == 0) {
						
						deleteDynamicTables(tableNames);
						throw new RuntimeCustomException(" No Datas Found in ROW TAG");
					}
					if (rowData != null) {
						int exactrowNum = 1;
						LinkedHashMap< String, Object> rowMap = new LinkedHashMap < String, Object> ();
						try (Connection conn = dSConnectorDao.returnConnection();
								PreparedStatement prpStmnt = conn
										.prepareStatement(dynamicInsertPrefixQuery.toString());) {
							int batchIndex = 1;
							for (int i = 0; i < rowData.getLength(); i++) {
								NodeList childList = rowData.item(i).getChildNodes();
								int eachRowDataCount = childList.getLength();
								if (colCount == eachRowDataCount) {
									List<Object> collTemp = new ArrayList<Object>();
									for (int j = 0; j < childList.getLength(); j++) {
										Node childNode = childList.item(j);
										if ("DCOLUMN".equalsIgnoreCase(childNode.getNodeName())) {
											System.out.println(childNode.getTextContent());
											prpStmnt.setString(j + 1, childNode.getTextContent());
										} else {
											deleteDynamicTables(tableNames);
											throw new RuntimeCustomException("<"+ childNode.getNodeName()+ "> tag has been specified instead of <DCOLUMN> tag in <ROW> tag list ["+ exactrowNum + "]");
										}
									}
								} else {
									deleteDynamicTables(tableNames);
									throw new RuntimeCustomException("Issue in (" + exactrowNum + ") row number. Row data is insufficient");
								}
								prpStmnt.addBatch();
								exactrowNum++;

								if (batchIndex == 5000) {
									prpStmnt.executeBatch();
									batchIndex = 0;
								}
								batchIndex++;
							}
							prpStmnt.executeBatch();

						} catch (Exception e) {
							deleteDynamicTables(tableNames);
							throw e;
						}
					}
				}
				if(!ValidationUtil.isValid(method)) {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", returnList);
	    		}else {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", "Success");
	    		}
			} catch(Exception e) {
				deleteDynamicTables(tableNames);
				throw new RuntimeCustomException(e.getMessage());
			}
		}
		return jsonExceptionCode;
	}
	
	/*-------------------------------------JSON Convert JSON Keys to Uppercase-------------------------------------------*/
	public static String changeJSONKeysUppercase(JSONObject obj) {
	    String jsonString = obj.toString();
	    for(int i = 0; i<obj.names().length(); i++){
	        try{
	            jsonString=jsonString.replace(obj.names().getString(i),
	            obj.names().getString(i).toUpperCase());
	        } catch(JSONException e) {
	            e.printStackTrace();
	        }
	    }
	    return jsonString;
	}
	
	/*-------------------------------------JSON Data Parsing-------------------------------------------*/
	public JSONExceptionCode getJSONDataString(String getData, String method) throws DataAccessException, SAXException {
		JSONExceptionCode jsonExceptionCode = null;
		List<VcMainDataSourceMetaDataVb> returnList = new ArrayList<VcMainDataSourceMetaDataVb>();;
		if (ValidationUtil.isValid(getData)) {

			try {
				int colCount = 0;
				LinkedHashMap < String,Object > hashMap = new LinkedHashMap < String, Object > ();
				
				JSONObject loop1 = new JSONObject(getData);
				String obj1  = changeJSONKeysUppercase(loop1);

				JSONObject loop = new JSONObject(obj1);
				JSONObject loop2 = (JSONObject) loop.get("TABLE");
				String obj2 = changeJSONKeysUppercase(loop2);
				
				JSONObject exactData = new JSONObject(obj2);
				
				String tableName = (String) exactData.get("TABLE_NAME");
				hashMap.put("TABLENAMES", new String[] {tableName});
				hashMap.put("TABLENAME", tableName);

				// Get a list of all column names in the document
				JSONObject objCols = (JSONObject) exactData.get("COLUMNS");
	            if(objCols.length()>0) {
					List<VcMainDataSourceMetaDataVb> vbchildList = new ArrayList<VcMainDataSourceMetaDataVb>();
					String objCl = changeJSONKeysUppercase(objCols);
					JSONObject exactColData = new JSONObject(objCl);
	            	JSONArray eachColData = (JSONArray) exactColData.getJSONArray("COLUMN");
		            if(eachColData.length()>0) {
		            	Iterator<Object> iterator = eachColData.iterator();
						List < XmlJsonUploadVb > collTemp = new ArrayList < XmlJsonUploadVb > ();
			            while (iterator.hasNext()) {
			            	XmlJsonUploadVb columnVb = new XmlJsonUploadVb();
			            	columnVb.setData((String) iterator.next());
							collTemp.add(columnVb);
			                colCount++;
			                if(!ValidationUtil.isValid(method)) {
								vbchildList.add(new VcMainDataSourceMetaDataVb(tableName, columnVb.getData(), null));
			        		}
			                
			            }
			            if(!ValidationUtil.isValid(method)) {
							returnList.add(new VcMainDataSourceMetaDataVb(tableName, "", vbchildList));
						}
			            hashMap.put("COLUMNS", collTemp);
		            }
	            }
				if (hashMap.size() == 0 || hashMap.size() == 1) {
					return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", "COLUMNS not specified");
				}
				
            	if(ValidationUtil.isValid(method) && "PREVIEW".equalsIgnoreCase(method)) {
					LinkedHashMap < String,Object > rowMap = new LinkedHashMap < String, Object > ();
		            // Get a list of all rows in the document
		            JSONArray rowExists = (JSONArray) exactData.get("ROW");
		            if(rowExists.length()>0) {
						int exactrowNum = 1;
						for (int i = 0; i < rowExists.length(); i++) {
							JSONObject objDataCol = (JSONObject) rowExists.get(i);
							String objDCl = changeJSONKeysUppercase(objDataCol);
							JSONObject exactDataCol = new JSONObject(objDCl);
				            JSONArray eachr = (JSONArray) exactDataCol.getJSONArray("DCOLUMN");
				        	int eachRowDataCount = eachr.length();
							if (colCount == eachRowDataCount) {
								List < XmlJsonUploadVb > collTemp = new ArrayList < XmlJsonUploadVb > ();
								Iterator<Object> iterator = eachr.iterator();
	   				              while (iterator.hasNext()) {
	   				            	XmlJsonUploadVb XmlJsonUploadVb = new XmlJsonUploadVb();
	   				            	String data=(String)iterator.next();
									XmlJsonUploadVb.setData(data);
									collTemp.add(XmlJsonUploadVb);
						          }
	   				           rowMap.put("ROW" + exactrowNum + "", collTemp);
							}else {
								return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", "Issue in (" + exactrowNum + ") row number. Row data is insufficient");
							}
							exactrowNum++;
						}
		            }
		            hashMap.put("ROWS", rowMap);
            	}
	            if(!ValidationUtil.isValid(method)) {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", returnList);
	    		}else {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", hashMap);
	    		}
	         
			} catch(Exception e) {
				return new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "Failure", e.getMessage());
			}
		}
		return jsonExceptionCode;
	}
	
	
	/*-------------------------------------JSON Data Parsing-------------------------------------------*/
	public JSONExceptionCode getJSONDataStringForDataInsert(String getData, FileInfoVb fileInfoVb, String method) {
		JSONExceptionCode jsonExceptionCode = null;
		List<VcMainDataSourceMetaDataVb> returnList = new ArrayList<VcMainDataSourceMetaDataVb>();
		List<String> tableNames = new ArrayList<String>();
		try {
			if(ValidationUtil.isValid(getData)){
				int colCount = 0;
				
				JSONObject loop1 = new JSONObject(getData);
				String obj1  = changeJSONKeysUppercase(loop1);

				JSONObject loop = new JSONObject(obj1);
				if( loop.opt("TABLE") == null ){
					deleteDynamicTables(tableNames);
					throw new RuntimeCustomException(" 'TABLE' datas are not present");
				}
				JSONObject loop2 = (JSONObject) loop.get("TABLE");
				String obj2 = changeJSONKeysUppercase(loop2);
				
				JSONObject exactData = new JSONObject(obj2);
				// Dynamic Table Source Creation Object
				StringBuffer tableCreationScript = null;

				// dynamic Table Name Creation Object
				StringBuffer dynamicTableName = null;

				// dynamic insert query Creation Object
				StringBuffer dynamicInsertPrefixQuery = null;
				StringBuffer dynamicInsertSufixQuery = null;
				DSConnectorVb dsConnectorVb = new DSConnectorVb();
				dsConnectorVb.setMacroVar(fileInfoVb.getMacroVar());
				if( exactData.opt("TABLE_NAME") == null ){
					deleteDynamicTables(tableNames);
					throw new RuntimeCustomException(" 'TABLE_NAME' datas are not present");
				}
				String tableName = (String) exactData.get("TABLE_NAME");
				long numberIncr = 0;
				dynamicTableName = new StringBuffer("VCDF_A");
				dsConnectorVb.setTableName(tableName);
				numberIncr = dSConnectorDao.generateTableSequence();
				dSConnectorDao.dynamicTableNameCreator(dynamicTableName, numberIncr);
				tableNames.add(dynamicTableName.toString());
				dSConnectorDao.doInsertOperationForUploadMappingTable(dsConnectorVb, dynamicTableName);
				
	        	tableCreationScript = new StringBuffer("Create table ");
	        	tableCreationScript.append(dynamicTableName + " ( ");
				dynamicInsertPrefixQuery = new StringBuffer("Insert into " + dynamicTableName + " (");
				dynamicInsertSufixQuery = new StringBuffer("values (");
		
				// Get a list of all column names in the document
				if(fileInfoVb.getHeaderCheck() == 'Y')
				{
					if( exactData.opt("COLUMNS") == null ){
						deleteDynamicTables(tableNames);
						throw new RuntimeCustomException(" 'COLUMNS' datas are not present");
					}
				JSONObject  objCols = (JSONObject) exactData.get("COLUMNS");
	              if(objCols.length()>0) {
					List<VcMainDataSourceMetaDataVb> vbchildList = new ArrayList<VcMainDataSourceMetaDataVb>();
					String objCl = changeJSONKeysUppercase(objCols);
					JSONObject exactColData = new JSONObject(objCl);
					if( exactColData.opt("COLUMN") == null ){
						deleteDynamicTables(tableNames);
						throw new RuntimeCustomException(" 'COLUMN' datas are not present");
					}
	            	JSONArray eachColData = (JSONArray) exactColData.getJSONArray("COLUMN");
		            if(eachColData.length()>0) {
		            	Iterator<Object> iterator = eachColData.iterator();

		            	while (iterator.hasNext()) {
			            	String columnStr = ((String) iterator.next()).replaceAll("\\W", "_"); 
		 					if(columnStr.length() >30)
		 					{
		 						columnStr =columnStr.substring(0, 30);
		 					}
			            	if (colCount != 0) {
								tableCreationScript
										.append(", " +columnStr + " VARCHAR2(4000 Byte)");
								dynamicInsertPrefixQuery.append(", " + columnStr);
								dynamicInsertSufixQuery.append(", ?");
							} else {
								tableCreationScript.append(columnStr + " VARCHAR2(4000 Byte)");
								dynamicInsertPrefixQuery.append(columnStr);
								dynamicInsertSufixQuery.append(" ?");
							}
			                colCount++;
			                if(!ValidationUtil.isValid(method)) {
								vbchildList.add(new VcMainDataSourceMetaDataVb(tableName, columnStr, null));
			        		}
			            }

			    		tableCreationScript.append(")");
						dynamicInsertPrefixQuery.append(")");
						dynamicInsertSufixQuery.append(")");
						dynamicInsertPrefixQuery.append(dynamicInsertSufixQuery);

						dSConnectorDao.createDynamicTableandMappingfields(tableCreationScript.toString());
			            if(!ValidationUtil.isValid(method)) {
							returnList.add(new VcMainDataSourceMetaDataVb(tableName, "", vbchildList));
						}
		            }
	            }
			}		
		else {
			if( exactData.opt("ROW") == null ){
				deleteDynamicTables(tableNames);
				throw new RuntimeCustomException(" 'ROW' datas are not present");
			}
				JSONArray rowExists = (JSONArray) exactData.get("ROW");
				JSONObject objDataCol = (JSONObject) rowExists.get(0);
				String objDCl = changeJSONKeysUppercase(objDataCol);
				JSONObject exactDataCol = new JSONObject(objDCl);
				
				if( exactDataCol.opt("DCOLUMN") == null ){
					deleteDynamicTables(tableNames);
					throw new RuntimeCustomException(" 'DCOLUMN' datas are not present in the Row: 1");
				}
				JSONArray eachr = (JSONArray) exactDataCol.getJSONArray("DCOLUMN");
					Iterator<Object> iterator = eachr.iterator();
					int colIndex = 0;
					while (iterator.hasNext()) {
						       iterator.next();
								if (colIndex != 0) {
									tableCreationScript
											.append(", Column"+ (colIndex+1) + " VARCHAR2(4000 Byte)");
									dynamicInsertPrefixQuery.append(", Column"+ (colIndex+1));
									dynamicInsertSufixQuery.append(", ?");
								} else {
									tableCreationScript.append("Column"+ (colIndex+1) + " VARCHAR2(4000 Byte)");
									dynamicInsertPrefixQuery.append("Column"+ (colIndex+1));
									dynamicInsertSufixQuery.append(" ?");
								}
								colIndex++;
							    colCount++;	
					}
		    		tableCreationScript.append(")");
					dynamicInsertPrefixQuery.append(")");
					dynamicInsertSufixQuery.append(")");
					dynamicInsertPrefixQuery.append(dynamicInsertSufixQuery);
					dSConnectorDao.createDynamicTableandMappingfields(tableCreationScript.toString());
				
				
			}
				try (Connection conn = dSConnectorDao.returnConnection();
						PreparedStatement prpStmnt = conn.prepareStatement(dynamicInsertPrefixQuery.toString());) {
					if (ValidationUtil.isValid(method) && "PREVIEW".equalsIgnoreCase(method)) {
						LinkedHashMap<String, Object> rowMap = new LinkedHashMap<String, Object>();
						// Get a list of all rows in the document
						if( exactData.opt("ROW") == null ){
							deleteDynamicTables(tableNames);
							throw new RuntimeCustomException(" 'ROW' datas are not present");
						}
						JSONArray rowExists = (JSONArray) exactData.get("ROW");
						if (rowExists.length() > 0) {
							int exactrowNum = 1;
							int batchIndex = 1;
							for (int i = 0; i < rowExists.length(); i++) {
								JSONObject objDataCol = (JSONObject) rowExists.get(i);
								String objDCl = changeJSONKeysUppercase(objDataCol);
								JSONObject exactDataCol = new JSONObject(objDCl);
								if( exactDataCol.opt("DCOLUMN") == null ){
									deleteDynamicTables(tableNames);
									throw new RuntimeCustomException(" 'DCOLUMN' datas are not present in the Row: "+(i+1));
								}
								JSONArray eachr = (JSONArray) exactDataCol.getJSONArray("DCOLUMN");
								int eachRowDataCount = eachr.length();
								if (colCount == eachRowDataCount ) {
									Iterator<Object> iterator = eachr.iterator();
									int colIndex =1;
									while (iterator.hasNext()) {
										String data = (String) iterator.next();
										prpStmnt.setString(colIndex, data);
//										System.out.print(specificData);
										colIndex++;
									}
								} else {
									deleteDynamicTables(tableNames);
									throw new RuntimeCustomException("Issue in (" + exactrowNum + ") row number. Row data is insufficient");
								}
								prpStmnt.addBatch();
								exactrowNum++;
								if (batchIndex == 5000) {
									prpStmnt.executeBatch();
									batchIndex = 0;
								}
								batchIndex++;
							}
							prpStmnt.executeBatch();
						}
					}
				}catch (Exception ex) {
	                deleteDynamicTables(tableNames); 							
					throw ex;
				}
				
	            if(!ValidationUtil.isValid(method)) {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", returnList);
	    		}else {
	    			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Success", "Success");
	    		}
			}
		} catch(Exception e) {
			deleteDynamicTables(tableNames);
			throw new RuntimeCustomException(e.getMessage());
		}
		return jsonExceptionCode;
	}
	
}