package com.vision.wb;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vision.dao.AbstractDao;
import com.vision.dao.CommonDao;
import com.vision.dao.SbiReportGenerationDao;
import com.vision.dao.WidgetDesignDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.DeepCopy;
import com.vision.util.ValidationUtil;
import com.vision.vb.DesignAnalysisVb;
import com.vision.vb.VrdObjectPropVb;
import com.vision.vb.WidgetDesignVb;
import com.vision.vb.WidgetFilterVb;
import com.vision.vb.WidgetLODWrapperVb;
import com.vision.vb.WidgetWrapperVb;
import com.vision.vb.XmlJsonUploadVb;

@Service
public class WidgetDesignWb extends AbstractWorkerBean<WidgetDesignVb>{
	
	public static Logger logger = LoggerFactory.getLogger(WidgetDesignWb.class);

	@Autowired
	private SbiReportGenerationDao reportGenerationDao;
	
	@Autowired
	private WidgetDesignDao widgetDesignDao;
	
	@Autowired
	private CommonDao commonDao;
		
	
	public ExceptionCode returnTableForFilter(List<WidgetFilterVb> widgetFilterList, String srcTblName) {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			StringBuffer filterWhereClause = new StringBuffer();
			if(ValidationUtil.isValidList(widgetFilterList)){
				int filterIndex = 1;
				for(WidgetFilterVb filterVb : widgetFilterList) {

					if("D".equalsIgnoreCase(filterVb.getType()))
					{
						filterWhereClause.append(" TO_DATE("+filterVb.getColumnName()+",'DD-MON-RRRR')");
					}else if (filterVb.getType().equalsIgnoreCase("number")){
						filterWhereClause.append(filterVb.getColumnName());
					} else {
						filterWhereClause.append("UPPER("+filterVb.getColumnName()+") ");
					}
					//filterWhereClause.append(("D".equalsIgnoreCase(filterVb.getType()))?"TO_DATE("+filterVb.getColumnName()+",'DD-MON-RRRR')":"UPPER("+filterVb.getColumnName()+")");
					switch (filterVb.getCondition()) {
					case "=" :
						filterWhereClause.append(" = "+filterVb.getValue1().toUpperCase());
						break;
					case "between" :
						filterWhereClause.append(" between "+filterVb.getValue1().toUpperCase()+" AND "+filterVb.getValue2().toUpperCase());
						break;
					case "in" :
						filterWhereClause.append(" in ("+filterVb.getValue1().toUpperCase()+") ");
						break;
					}
					if(filterIndex != (widgetFilterList).size()) {
						filterWhereClause.append(" AND ");
					}
					filterIndex++;
				}
			}
			String sql = "";
			if(ValidationUtil.isValid(filterWhereClause+"")) {
				sql = "CREATE TABLE "+srcTblName+"_F as SELECT * from "+srcTblName+" WHERE "+filterWhereClause;
			}
			exceptionCode = widgetDesignDao.executeSql(sql, null);
			
			if(exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION) {
				exceptionCode.setResponse(srcTblName+"_F");
			} else {
				return exceptionCode;
			}
			
		} catch(Exception e) {
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg(e.getMessage());
		}
		return exceptionCode;
	}
	
	private String returnDecimalFormat(String count) {
		String returnVal = "99";
		switch (count) {
		case "1":
			returnVal = "9";
			break;
		case "2":
			returnVal = "99";
			break;
		case "3":
			returnVal = "999";
			break;
		case "4":
			returnVal = "9999";
			break;
		case "5":
			returnVal = "99999";
			break;
		default:
			break;
		}
		return returnVal;
	}
	
	public ExceptionCode getGridResponseWithChartXML(WidgetWrapperVb vObject,String inputChartXML, String dataTable) {
		ExceptionCode exceptionCode = new ExceptionCode();
		boolean isGroupByEnabled = false;
		try {
			StringBuffer selectStr = new StringBuffer("SELECT ");
			StringBuffer orderByStr = new StringBuffer();
			StringBuffer groupByStr = new StringBuffer();
			
			String columnCountStr = CommonUtils.getValueForXmlTag(inputChartXML, "ColumnCount");
			int colCount = Integer.parseInt(columnCountStr);
			String allColumnsXml = CommonUtils.getValueForXmlTag(inputChartXML, "COLUMNS");
			List<XmlJsonUploadVb> columnNameList = new ArrayList<XmlJsonUploadVb>();
			List<XmlJsonUploadVb> headerNameList = new ArrayList<XmlJsonUploadVb>();
			for(int colIndex = 1; colIndex<=colCount;colIndex++) {
				String columnXml = CommonUtils.getValueForXmlTag(allColumnsXml, "COLUMN"+colIndex);
				
				String colDisplayType = CommonUtils.getValueForXmlTag(columnXml, "colDisplayType");
				String columnName = CommonUtils.getValueForXmlTag(columnXml, "colName");
				String columnAlias = ValidationUtil.isValid(CommonUtils.getValueForXmlTag(columnXml, "alias"))?(CommonUtils.getValueForXmlTag(columnXml, "alias")).toUpperCase():columnName;
				String colDisplayName = ValidationUtil.isValid(CommonUtils.getValueForXmlTag(columnXml, "colDisplayName"))?CommonUtils.getValueForXmlTag(columnXml, "colDisplayName"):columnAlias;
				String scalingFlag = ValidationUtil.isValid(CommonUtils.getValueForXmlTag(columnXml, "scalingFlag"))?CommonUtils.getValueForXmlTag(columnXml, "scalingFlag"):"N";
				String scalingFormat = ValidationUtil.isValid(CommonUtils.getValueForXmlTag(columnXml, "scalingFormat"))?CommonUtils.getValueForXmlTag(columnXml, "scalingFormat"):"1000000";
				String numberFormat = ValidationUtil.isValid(CommonUtils.getValueForXmlTag(columnXml, "numberFormat"))?CommonUtils.getValueForXmlTag(columnXml, "numberFormat"):"N";
				String decimalFlag = ValidationUtil.isValid(CommonUtils.getValueForXmlTag(columnXml, "decimalFlag"))?CommonUtils.getValueForXmlTag(columnXml, "decimalFlag"):"N";
				String decimalCount = ValidationUtil.isValid(CommonUtils.getValueForXmlTag(columnXml, "decimalCount"))?CommonUtils.getValueForXmlTag(columnXml, "decimalCount"):"2";
				String Dateformat = ValidationUtil.isValid(CommonUtils.getValueForXmlTag(columnXml, "formatTypeDesc"))?CommonUtils.getValueForXmlTag(columnXml, "formatTypeDesc"):"DD-Mon-RRRR";
				
				
				Map<String, Object> propertyMap = new HashMap<String, Object>();
				propertyMap.put("colDisplayType",colDisplayType);
				columnNameList.add(new XmlJsonUploadVb(columnAlias, propertyMap));
				headerNameList.add(new XmlJsonUploadVb(colDisplayName, propertyMap));
				
				String aggFunction = CommonUtils.getValueForXmlTag(columnXml, "aggFunction");
				if(ValidationUtil.isValid(aggFunction) && !"null".equalsIgnoreCase(aggFunction)) {
//					selectStr.append("TO_CHAR("+aggFunction+"("+columnAlias+") /"+scalingFormat+", '999,999,999,999.90') "+columnAlias+", ");
					String tempColumn = aggFunction+"("+columnName+")";
					if ("y".equalsIgnoreCase(scalingFlag)) {
						tempColumn = tempColumn+"/"+scalingFormat;
					}
					if("y".equalsIgnoreCase(numberFormat)) {
						if("y".equalsIgnoreCase(decimalFlag)) {
							String decimalFormat = returnDecimalFormat(decimalCount);
							tempColumn = "trim(TO_CHAR("+tempColumn+", '999,999,999,990."+decimalFormat+"'))";
						} else {
							tempColumn = "trim(TO_CHAR("+tempColumn+", '999,999,999,990'))";
						}
					}
					selectStr.append(tempColumn+" "+columnAlias+", ");
					isGroupByEnabled = true;
				} else {
					if("N".equalsIgnoreCase(colDisplayType)) {
						String tempColumn = columnName;
						if ("y".equalsIgnoreCase(scalingFlag)) {
							tempColumn = tempColumn+"/"+scalingFormat;
						}
						if("y".equalsIgnoreCase(numberFormat)) {
							if("y".equalsIgnoreCase(decimalFlag)) {
								String decimalFormat = returnDecimalFormat(decimalCount);
								tempColumn = "TO_CHAR("+tempColumn+", '999,999,999,990."+decimalFormat+"')";
							} else {
								tempColumn = "TO_CHAR("+tempColumn+", '999,999,999,990')";
							}
						}
						selectStr.append(tempColumn+" "+columnAlias+", ");
					}else if("d".equalsIgnoreCase(colDisplayType)){
						selectStr.append("TO_char("+columnName+",'"+Dateformat+"')" +columnAlias+", ");
				    } else {
						selectStr.append(columnName + " " +columnAlias+", ");
					}
					groupByStr.append(columnName+", ");
				}
				
				if(ValidationUtil.isValid(CommonUtils.getValueForXmlTag(columnXml, "sortType")) && !"null".equalsIgnoreCase(CommonUtils.getValueForXmlTag(columnXml, "sortType")))
					orderByStr.append(columnName+" "+CommonUtils.getValueForXmlTag(columnXml, "sortType")+", ");
				
				if("Y".equalsIgnoreCase(CommonUtils.getValueForXmlTag(columnXml, "groupBy")))
					isGroupByEnabled = true;
			}
			
			
			StringBuffer executableQuery = new StringBuffer(selectStr.substring(0, (selectStr.length()-2)));
			executableQuery.append(" FROM "+dataTable);
			if(isGroupByEnabled && groupByStr.length()>0)
				executableQuery.append(" GROUP BY "+ groupByStr.substring(0, (groupByStr.length()-2)));
			if(orderByStr.length()>0) {
				executableQuery.append(" ORDER BY "+ orderByStr.substring(0, (orderByStr.length()-2)));
			}else if((ValidationUtil.isValid(vObject.getMainModel().getOrderBy())) && !(isGroupByEnabled && groupByStr.length()>0)) {
				executableQuery .append(" ORDER BY "+vObject.getMainModel().getOrderBy());
			}
			
			return widgetDesignDao.formResponceJsonForGridWidget(vObject, headerNameList, columnNameList, String.valueOf(executableQuery), dataTable);
		} catch (Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg(e.getMessage());
			return exceptionCode;
		}
	}
	
	public ExceptionCode getChartResponseWithChartXML(String inputChartXML, String dataTable) {
		
		ExceptionCode exceptionCode = new ExceptionCode();
		Connection con = null;
    	Statement stmt = null;
    	ResultSet rs = null;
		try {
			boolean multiY_NoSeries = false;
		    boolean onlyX_NoSeries = false;
		    boolean onlyY_onlyMeasure = false;
		    boolean onlyY_WithSeries = false;
		    String xAxisCol = CommonUtils.getValueForXmlTag(inputChartXML, "X_AXIS");
		    String yAxisCol = CommonUtils.getValueForXmlTag(inputChartXML, "Y_AXIS");
		    String zAxisCol = CommonUtils.getValueForXmlTag(inputChartXML, "Z_AXIS");
		    String seriesCol = CommonUtils.getValueForXmlTag(inputChartXML, "SERIES");
		    String measureProp = CommonUtils.getValueForXmlTag(inputChartXML, "MEASURE_PROP");
		    String chartType = CommonUtils.getValueForXmlTag(inputChartXML, "ChartType");
		    String isCustomColor = CommonUtils.getValueForXmlTag(inputChartXML, "isCustomColor");
		    String isRadiantColor = CommonUtils.getValueForXmlTag(inputChartXML, "isRadiantColor");
		    String colorPalette = CommonUtils.getValueForXmlTag(inputChartXML, "ColorPalette");
		    colorPalette = ValidationUtil.isValid(colorPalette)?colorPalette:"Default";
		    String genricChartProperties = CommonUtils.getValueForXmlTag(inputChartXML, "GenericAttributes");
		    String sortingPropXML = CommonUtils.getValueForXmlTag(inputChartXML, "SORT_PROP");
		    StringBuffer sortStr = new StringBuffer();
		    
		    if(ValidationUtil.isValid(sortingPropXML)) {
		    	try {
		    		int columnCount = Integer.parseInt(CommonUtils.getValueForXmlTag(sortingPropXML, "ColumnCount"));
		    		boolean isConcatAggrigateWithMeasure = yAxisCol.contains(",");
		    		for(int colIndex = 1 ; colIndex <= columnCount ; colIndex++) {
		    			String colSortProp = CommonUtils.getValueForXmlTag(sortingPropXML, "COLUMN"+colIndex);
		    			String sortType = CommonUtils.getValueForXmlTag(colSortProp, "sortType");
		    			String columnName = CommonUtils.getValueForXmlTag(colSortProp, "columnName");
		    			String aggFunction = CommonUtils.getValueForXmlTag(colSortProp, "aggFunction");
		    			if("ASC".equalsIgnoreCase(sortType) || "DESC".equalsIgnoreCase(sortType)){
		    				if(isConcatAggrigateWithMeasure && ValidationUtil.isValid(aggFunction))
		    					sortStr.append(columnName+"_"+aggFunction+" "+sortType+", ");
		    				else
		    					sortStr.append(columnName+" "+sortType+", ");
		    			}
		    			if(colIndex == columnCount && sortStr.length()>3) {
		    				sortStr = new StringBuffer(sortStr.substring(0, (sortStr.length()-2)));
		    			}
		    		}
		    	} catch (Exception e) {
		    		sortStr = null;
		    	}
		    }

		    String singleColor = "000000";
		    List<String> colorAL = new ArrayList<String>();
		    try{
		    	if(ValidationUtil.isValid(colorPalette)){
			    	if("true".equalsIgnoreCase(isCustomColor)){
				    	String initArray[] = colorPalette.split("\\@\\|\\@");
				    	singleColor = initArray[0];
				    	colorAL = Arrays.asList(initArray[1].split(","));
				    }else{
				    	String htmlXml = "";
				    	List<VrdObjectPropVb> objectPropVbList = reportGenerationDao.findActiveColorPaletteFromObjProperties(colorPalette);
				    	if(objectPropVbList.size()>0){
				    		htmlXml = objectPropVbList.get(0).getHtmlTagProperty();
				    	}else{
				    		objectPropVbList = reportGenerationDao.findActiveColorPaletteFromObjProperties("Default");
				    		htmlXml = objectPropVbList.get(0).getHtmlTagProperty();
				    	}
				    	String initArray[] = htmlXml.split("\\@\\|\\@");
				    	singleColor = initArray[0];
				    	colorAL = Arrays.asList(initArray[1].split(","));
				    }
			    }
		    }catch(Exception e){
		    	e.printStackTrace();
		    	singleColor = "000000";
		    	String color[] = {"000000","ba40c3","b95c65","bac883","b96f13","bb5509","b9e5bb","bae653","ba36a1","ba5e94","bb425b","b965ef","ba8e7a","baddc8","b9048c","ba7242","b948b7","bafa01","ba989d","baf076","bb9a35","baca1b","b9abb3","baac4b","ba06bb","b9a090","bb69b6","babff7","b98b4b","bbde60","b9d10e","b90d17","ba7bcc","b9be61","b9789d","b92ae7","bb7c64","b9efde","b9ee47","ba8f12","b9215c","bab5d5","ba671f","b9b43e","baa228","b99706","b93e95","ba0524","ba494f","bb8687","b9bdc9","b86359","ba1a69","ba4be6","b94720","b9da99","bbade1","b98de2","ba0eae","b920c4","bb11dd","ba23f3","bb248b","ba0f46","b9966e","bae7ea","b982c0","b8a884","bad3a5","b902f4","bac18f","b9a91c","ba85ef","bab66d","ba2c7f","ba53d9","ba5471","bb3739","ba5dfc","b9c7eb","b96687","b9350a","bad43d","bbfc30","b97935","b93372","ba3f2d","ba2d17","baa3bf","bbcbb2","ba18d1","bafb98","b95143","ba8458","b9dc31","b9173a","b8edaf","ba70aa","b81f2d","bbf20e"};
		    	colorAL = Arrays.asList(color[1].split(","));
		    }
			
		    if(ValidationUtil.isValid(xAxisCol) && !ValidationUtil.isValid(yAxisCol) && !ValidationUtil.isValid(seriesCol))
		    	onlyX_NoSeries = true;
		    else if(ValidationUtil.isValid(xAxisCol) && ValidationUtil.isValid(yAxisCol) && yAxisCol.indexOf(",")!=-1 && !ValidationUtil.isValid(seriesCol))
		    	multiY_NoSeries = true;
		    else if(!ValidationUtil.isValid(xAxisCol) && ValidationUtil.isValid(yAxisCol) && yAxisCol.indexOf(",")!=-1 && !ValidationUtil.isValid(seriesCol))
		    	onlyY_onlyMeasure = true;
		    else if(!ValidationUtil.isValid(xAxisCol) && ValidationUtil.isValid(yAxisCol) && yAxisCol.indexOf(",")!=-1 && ValidationUtil.isValid(seriesCol))
		    	onlyY_WithSeries = true;
	    	con = reportGenerationDao.returnConnection();
	    	stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	    	String aggrigateFunction = "SUM";
	    	String query = "select * from "+dataTable;
	    	StringBuffer yAxisQueryString = new StringBuffer();
	    	if(yAxisCol.indexOf(",")!=-1) {
    			String yAxisColArr[] = yAxisCol.split(",");
    			yAxisCol = "";
    			for(String yCol:yAxisColArr){
    				aggrigateFunction = getAggrigateFunctionForMeasure(yCol, measureProp);
    				measureProp = measureProp.replaceAll("<"+yCol+"><FUNCTION>"+aggrigateFunction+"<\\/FUNCTION><\\/"+yCol+">", "");
    				yAxisQueryString.append(aggrigateFunction+"("+yCol+") "+yCol+"_"+aggrigateFunction+",");
    				yAxisCol = yAxisCol + yCol +"_"+aggrigateFunction+",";
    			}
    			yAxisQueryString = new StringBuffer(yAxisQueryString.subSequence(0, (yAxisQueryString.length()-1)));
    			yAxisCol = yAxisCol.substring(0, (yAxisCol.length()-1));
			} else {
    			aggrigateFunction = getAggrigateFunctionForMeasure(yAxisCol, measureProp);
    			yAxisQueryString.append(aggrigateFunction+"("+yAxisCol+") "+yAxisCol);
    		}
	    	
	    	if(ValidationUtil.isValid(seriesCol) && ValidationUtil.isValid(xAxisCol) && ValidationUtil.isValid(yAxisCol)){
	    		query = "create table "+dataTable+"_GRP as "+
	    				"select "+seriesCol+","+xAxisCol+", "+yAxisQueryString+
	    				" from ( "+query+") "+
	    				"group by "+seriesCol+","+xAxisCol;
	    				
	    		if(sortStr != null && sortStr.length()>0)
	    			query = query + " order by "+sortStr;
	    				
	    		stmt.executeQuery(query);
	    		query = "select t100."+seriesCol+",t100."+xAxisCol+",t200."+yAxisCol+" from "+
	    				"(select * from (select distinct "+seriesCol+" "+
	    				"from "+dataTable+"_GRP) t1, ( "+
	    				"select distinct "+xAxisCol+" "+
	    				"from "+dataTable+"_GRP) t2) t100, "+
	    				""+dataTable+"_GRP t200 "+
	    				"where t100."+seriesCol+" = t200."+seriesCol+"(+) "+
	    				"and t100."+xAxisCol+" = t200."+xAxisCol+"(+) "+
	    				"order by 1,2";
	    	}else if(ValidationUtil.isValid(xAxisCol) && ValidationUtil.isValid(yAxisCol)){
	    		query = "create table "+dataTable+"_GRP as "+
	    				"select "+xAxisCol+", "+yAxisQueryString+
	    				" from ( "+query+") "+
	    				"group by "+xAxisCol;
	    		
	    		if(sortStr != null && sortStr.length()>0)
	    			query = query + " order by "+sortStr;
	    		
	    		stmt.executeQuery(query);
	    		query = "select * from "+dataTable+"_GRP";
	    	}else if(onlyY_onlyMeasure){
	    		query = "create table "+dataTable+"_GRP as "+
	    				"select "+yAxisQueryString+
	    				" from ( "+query+") ";
	    		if(sortStr != null && sortStr.length()>0)
	    			query = query + " order by "+sortStr;
	    		stmt.executeQuery(query);
	    		query = "select * from "+dataTable+"_GRP";
	    	}else if(onlyY_WithSeries){
	    		query = "create table "+dataTable+"_GRP as "+
	    				"select "+seriesCol+", "+yAxisQueryString+
	    				" from ( "+query+") "+
	    				"group by "+seriesCol;
	    		if(sortStr != null && sortStr.length()>0)
	    			query = query + " order by "+sortStr;
	    		stmt.executeQuery(query);
	    		query = "select t100."+seriesCol+",t200."+yAxisCol+" from "+
	    				"(select * from (select distinct "+seriesCol+" "+
	    				"from "+dataTable+"_GRP) t1) t100, "+
	    				""+dataTable+"_GRP t200 "+
	    				"where t100."+seriesCol+" = t200."+seriesCol+"(+) "+
	    				"order by 1";
	    	}
	    	rs = stmt.executeQuery(query);
	    	 
	    	RowSetFactory factory = RowSetProvider.newFactory();
	    	CachedRowSet rsChild = factory.createCachedRowSet();
	    	rsChild.populate(rs);
	    	
	    	List<String> repeatTagList = new ArrayList<String>();
	    	String chartMataDataXml = reportGenerationDao.getChartXmlFormObjProperties(chartType);
	    	
	    	if(!ValidationUtil.isValid(chartMataDataXml)) {
	    		exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg("No proper maintenance for the chart type ["+chartType+"]");
	    	}
	    	
	    	String chartXML = "";
	    	String swfFile = CommonUtils.getValueForXmlTag(chartMataDataXml, "SWF_FILE_NAME");
	    	
	    	Matcher matcherObj = Pattern.compile("\\<REPEATING_TAG\\>(.*?)\\<\\/REPEATING_TAG\\>",Pattern.DOTALL).matcher(chartMataDataXml);
	    	while(matcherObj.find()){
	    		String repeatTags = matcherObj.group(1).replaceAll("\n", "").replaceAll("\r", "").replaceAll("\\s+", " ").trim();
	    		repeatTagList.add(repeatTags);
	    	}
	    	matcherObj = Pattern.compile("\\<chart(.*?)\\<\\/chart\\>",Pattern.DOTALL).matcher(chartMataDataXml);
	    	if(matcherObj.find()){
	    		String tempChartXml = matcherObj.group(1);
	    		String postChartXml = tempChartXml.substring(tempChartXml.indexOf('>'),tempChartXml.length());
	    		tempChartXml = tempChartXml.substring(0, tempChartXml.indexOf('>'));
	    		if(ValidationUtil.isValid(genricChartProperties)){
		    		Matcher attributeMatcherObj = Pattern.compile("</(.*?)>",Pattern.DOTALL).matcher(genricChartProperties);
					while(attributeMatcherObj.find()){
						if(tempChartXml.indexOf(" "+attributeMatcherObj.group(1))==-1)
							if("exportFileName".equalsIgnoreCase(attributeMatcherObj.group(1))){
								String value = CommonUtils.getValueForXmlTag(genricChartProperties, attributeMatcherObj.group(1));
								if(!ValidationUtil.isValid(value)){
									value = ValidationUtil.isValid(CommonUtils.getValueForXmlTag(genricChartProperties, "caption"))
													? CommonUtils.getValueForXmlTag(genricChartProperties,"caption")
													: ValidationUtil.isValid(CommonUtils.getValueForXmlTag(genricChartProperties, "subcaption"))
																	? CommonUtils.getValueForXmlTag(genricChartProperties, "subcaption")
																	: "VisionCharts";
								}
								tempChartXml = tempChartXml + " exportFileName=\"" + value + "\"";
							}else
								tempChartXml = tempChartXml + " " + attributeMatcherObj.group(1) + "=\"" +CommonUtils.getValueForXmlTag(genricChartProperties, attributeMatcherObj.group(1)) + "\"";
					}
	    		}
				tempChartXml = tempChartXml + postChartXml;
	    		
	    		chartXML = "<chart"+tempChartXml+"</chart>";
	    	}
	    	
	    	String returnChartXml = chartXML;
	    	if(ValidationUtil.isValid(chartXML)){
	    		for(String repeatTagMain:repeatTagList){
	    			StringBuffer dataExistCheck = new StringBuffer();
	    			if(onlyX_NoSeries && repeatTagList.size()==1){
	    				returnChartXml = updateReturnXmlForSingleRepeatTag(repeatTagMain, chartXML, xAxisCol, yAxisCol, zAxisCol, seriesCol, returnChartXml, rsChild, dataExistCheck);
	    			}else if(multiY_NoSeries && repeatTagList.size()==2){
	    				if(repeatTagMain.indexOf(",")==-1){
							returnChartXml = updateReturnXmlForSingleRepeatTag(repeatTagMain, chartXML, xAxisCol, yAxisCol, zAxisCol, seriesCol, returnChartXml, rsChild, dataExistCheck);
						}else{
							returnChartXml = updateReturnXmlForMultipleRepeatTagMultiY_NoSeries(repeatTagMain, xAxisCol, yAxisCol, zAxisCol, seriesCol, chartXML, rs, dataExistCheck, rsChild, returnChartXml);
						}
	    			}else if(onlyY_onlyMeasure){
	    				returnChartXml = updateReturnXmlForSingleRepeatTag_OnlyMeasure(repeatTagMain, chartXML, xAxisCol, yAxisCol, zAxisCol, seriesCol, returnChartXml, rsChild, dataExistCheck);
	    			}else if(onlyY_WithSeries){
	    				if(repeatTagMain.indexOf(",")==-1){
							returnChartXml = updateReturnXmlForSingleRepeatTag_OnlyMeasure(repeatTagMain, chartXML, xAxisCol, yAxisCol, zAxisCol, seriesCol, returnChartXml, rsChild, dataExistCheck);
						}else{
							returnChartXml = updateReturnXmlForMultipleRepeatTagMultiY(repeatTagMain, xAxisCol, yAxisCol, zAxisCol, seriesCol, chartXML, rs, dataExistCheck, rsChild, returnChartXml);
						}
	    			}else{
						if(repeatTagMain.indexOf(",")==-1){
							returnChartXml = updateReturnXmlForSingleRepeatTag(repeatTagMain, chartXML, xAxisCol, yAxisCol, zAxisCol, seriesCol, returnChartXml, rsChild, dataExistCheck);
						}else{
							returnChartXml = updateReturnXmlForMultipleRepeatTag(repeatTagMain, xAxisCol, yAxisCol, zAxisCol, seriesCol, chartXML, rs, dataExistCheck, rsChild, returnChartXml);
						}
	    			}
		    	}
	    	}
	    	returnChartXml = returnChartXml.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\\s+", " ");
//		    System.out.println("java:"+returnChartXml+":java");
		    if(ValidationUtil.isValid(isRadiantColor) && "true".equalsIgnoreCase(isRadiantColor)){
		    	int colorReplaceIndex = 0;
		    	matcherObj = Pattern.compile("\\#COLOR\\_CODE\\#",Pattern.DOTALL).matcher(returnChartXml);
		    	while(matcherObj.find()){
		    		colorReplaceIndex++;
		    	}
		    	colorAL = returnColorListBasedOnColorCount(colorReplaceIndex,singleColor);
		    	colorReplaceIndex = 0;
		    	matcherObj = Pattern.compile("\\#COLOR\\_CODE\\#",Pattern.DOTALL).matcher(returnChartXml);
				while (matcherObj.find()) {
					try {
		    			returnChartXml = returnChartXml.replaceFirst("\\#COLOR\\_CODE\\#", colorAL.get(colorReplaceIndex));
					} catch (Exception e) {
		    			returnChartXml = returnChartXml.replaceFirst("\\#COLOR\\_CODE\\#", colorAL.get(0));
		    			colorReplaceIndex = 0;
		    		}
		    		colorReplaceIndex++;
		    	}
			} else {
		    	int colorReplaceIndex = 0;
			    matcherObj = Pattern.compile("\\#COLOR\\_CODE\\#",Pattern.DOTALL).matcher(returnChartXml);
				while (matcherObj.find()) {
			    	returnChartXml = returnChartXml.replaceFirst("\\#COLOR\\_CODE\\#", colorAL.get(colorReplaceIndex));
			    	colorReplaceIndex++;
					if (colorReplaceIndex > 99)
			    		colorReplaceIndex=0;
			    }
		    }
		    exceptionCode.setResponse(returnChartXml);
//		    exceptionCode.setOtherInfo(dataTable);
		    exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
		    return exceptionCode;
		} catch (Exception e) {
			e.printStackTrace();
//			exceptionCode.setOtherInfo(dataTable);
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg(e.getMessage());
		} finally {
			try { stmt.execute("DROP TABLE "+dataTable+"_GRP PURGE"); }catch(Exception e) {}
			try {if(rs!=null)rs.close();}catch(Exception e) {}
			try {if(stmt!=null)stmt.close();}catch(Exception e) {}
			try {if(con!=null)con.close();}catch(Exception e) {}
		}
		
		return exceptionCode;
	}
	
	protected String getAggrigateFunctionForMeasure(String colName, String srcXml){
		try{
			String columnXml = CommonUtils.getValueForXmlTag(srcXml, colName);
			String returnString = CommonUtils.getValueForXmlTag(columnXml, "FUNCTION");
			return ValidationUtil.isValid(returnString)?returnString:"SUM";
		}catch(Exception e){
			e.printStackTrace();
			return "SUM";
		}
	}
	
	public String updateReturnXmlForSingleRepeatTag(String repeatTagMain, String chartXML, String xAxisCol, String yAxisCol, String zAxisCol, String seriesCol, String returnChartXml, ResultSet rs, StringBuffer dataExistCheck) throws SQLException{
		Matcher matcherObj = Pattern.compile("\\<"+repeatTagMain+"(.*?)\\<\\/"+repeatTagMain+"\\>",Pattern.DOTALL).matcher(chartXML);
		if(matcherObj.find()){
			String tagString = "<"+repeatTagMain+matcherObj.group(1)+"</"+repeatTagMain+">";
			ArrayList<String> patternStrAL = new ArrayList<String>();
			ArrayList<String> patternStrColNameAL = new ArrayList<String>();
			Matcher valColMatcher = Pattern.compile("\\!\\@\\#(.*?)\\!\\@\\#",Pattern.DOTALL).matcher(tagString);
			
			while(valColMatcher.find()){
				patternStrAL.add(valColMatcher.group(1));
				patternStrColNameAL.add(getColumnName(valColMatcher.group(1), xAxisCol, yAxisCol, zAxisCol, seriesCol));
			}
			
			rs.beforeFirst();
			StringBuffer formedXmlSB = new StringBuffer();
	    	while(rs.next()){
	    		if(patternStrColNameAL.size()==1){
		    		if(dataExistCheck.indexOf(","+rs.getObject(patternStrColNameAL.get(0))+",")==-1){
		    			String value = String.valueOf(rs.getObject(patternStrColNameAL.get(0)));
		    			value = ValidationUtil.isValid(value)?value.replaceAll("\"", ""):value;
		    			formedXmlSB.append(tagString.replaceAll("\\!\\@\\#"+patternStrAL.get(0)+"\\!\\@\\#", ValidationUtil.isValid(value)?value:""));
			    		dataExistCheck.append(","+value+",");
		    		}
	    		}else if(patternStrColNameAL.size()>1){
	    			int arrListIndex = 0;
	    			String tempTagString = tagString;
	    			for(String colName:patternStrColNameAL){
	    				String value = String.valueOf(rs.getObject(colName));
						value = ValidationUtil.isValid(value)?value.replaceAll("\"", ""):value;
	    				tempTagString = tempTagString.replaceAll("\\!\\@\\#"+patternStrAL.get(arrListIndex)+"\\!\\@\\#", ValidationUtil.isValid(value)?value:"");
	    				arrListIndex++;
	    			}
	    			formedXmlSB.append(tempTagString);
	    		}
	    	}
	    	/* Update to return XML */
	    	Matcher returnMatcher = Pattern.compile("^(.*?)\\<"+repeatTagMain+"(.*?)\\<\\/"+repeatTagMain+"\\>(.*?)$",Pattern.DOTALL).matcher(returnChartXml);
	    	if(returnMatcher.find()){
	    		returnChartXml = returnMatcher.group(1)+formedXmlSB+returnMatcher.group(3);
	    	}
		}
		return returnChartXml;
	}
	
	public String updateReturnXmlForSingleRepeatTag_OnlyMeasure(String repeatTagMain, String chartXML, String xAxisCol, String yAxisCol, String zAxisCol, String seriesCol, String returnChartXml, ResultSet rs, StringBuffer dataExistCheck) throws SQLException{
		Matcher matcherObj = Pattern.compile("\\<"+repeatTagMain+"(.*?)\\<\\/"+repeatTagMain+"\\>",Pattern.DOTALL).matcher(chartXML);
		if(matcherObj.find()){
			String tagString = "<"+repeatTagMain+matcherObj.group(1)+"</"+repeatTagMain+">";
			String replaceTagString = "";
			Matcher valColMatcher = Pattern.compile("\\!\\@\\#(.*?)\\!\\@\\#",Pattern.DOTALL).matcher(tagString);
			while(valColMatcher.find()){
				replaceTagString = "Y_AXIS".equalsIgnoreCase(valColMatcher.group(1))?replaceTagString:valColMatcher.group(1);
			}
			rs.beforeFirst();
			StringBuffer formedXmlSB = new StringBuffer();
			while(rs.next()){
				String columnArr[] = yAxisCol.split(",");
				for(String columnName:columnArr){
					String value = String.valueOf(rs.getObject(columnName));
					value = ValidationUtil.isValid(value)?value.replaceAll("\"", ""):value;
					value = tagString.replaceAll("\\!\\@\\#"+replaceTagString+"\\!\\@\\#", columnName).replaceAll("\\!\\@\\#Y_AXIS\\!\\@\\#", ValidationUtil.isValid(value)?value:"");
					if(formedXmlSB.indexOf(value)==-1)
						formedXmlSB.append(value);
				}
			}
	    	/* Update to return XML */
	    	Matcher returnMatcher = Pattern.compile("^(.*?)\\<"+repeatTagMain+"(.*?)\\<\\/"+repeatTagMain+"\\>(.*?)$",Pattern.DOTALL).matcher(returnChartXml);
	    	if(returnMatcher.find()){
	    		returnChartXml = returnMatcher.group(1)+formedXmlSB+returnMatcher.group(3);
	    	}
		}
		return returnChartXml;
	}
	
	public String updateReturnXmlForMultipleRepeatTagMultiY_NoSeries(String repeatTagMain, String xAxisCol, String yAxisCol, String zAxisCol, String seriesCol, String chartXML, ResultSet rs, StringBuffer dataExistCheck, CachedRowSet rsChild, String returnChartXml) throws SQLException{
		String[] repeatTagArr = repeatTagMain.split(",");
		if(repeatTagArr.length==2){
			Matcher matcherObj = Pattern.compile("\\<"+repeatTagArr[0]+"(.*?)\\<\\/"+repeatTagArr[0]+"\\>",Pattern.DOTALL).matcher(chartXML);
			if(matcherObj.find()){
				String fullTagString = "<"+repeatTagArr[0]+matcherObj.group(1)+"</"+repeatTagArr[0]+">";
				String parentTagStr = "";
				String childTagString = "";
				String parentReplaceString = "";
				String childReplaceString = "";
				StringBuffer formedXmlSB = new StringBuffer();
				
				/* Form parent tag String */
				Matcher parentTagMatcher = Pattern.compile("\\<"+repeatTagArr[0]+"(.*?)\\>",Pattern.DOTALL).matcher(fullTagString);
				if(parentTagMatcher.find()){
					parentTagStr = "<"+repeatTagArr[0]+parentTagMatcher.group(1)+">";
				}
				
				
				/* Get exact pattern from parent tag to be replaced with Y-Axis column name */
				String yAxisColArr[] = yAxisCol.split(",");
				Matcher replaceMatcher = Pattern.compile("\\!\\@\\#(.*?)\\!\\@\\#",Pattern.DOTALL).matcher(parentTagStr);
				if(replaceMatcher.find()){
					parentReplaceString = replaceMatcher.group(1);
				}
				
				/* Form child tag String */
				Matcher matcherChildObj = Pattern.compile("\\<"+repeatTagArr[1]+"(.*?)\\<\\/"+repeatTagArr[1]+"\\>",Pattern.DOTALL).matcher(fullTagString);
				if(matcherChildObj.find()){
					childTagString = "<"+repeatTagArr[1]+matcherChildObj.group(1)+"</"+repeatTagArr[1]+">";
				}
				
				replaceMatcher = Pattern.compile("\\!\\@\\#(.*?)\\!\\@\\#",Pattern.DOTALL).matcher(childTagString);
				if(replaceMatcher.find()){
					childReplaceString = replaceMatcher.group(1);
				}
				
				/* For every Y-Axis column Name from parent tag string [dataset] */
				for(String yCol:yAxisColArr){
					formedXmlSB.append(parentTagStr.replaceAll("\\!\\@\\#"+parentReplaceString+"\\!\\@\\#", yCol));
					
					rs.beforeFirst();
					while(rs.next()){
						String value = String.valueOf(rs.getObject(yCol));
						value = ValidationUtil.isValid(value)?value.replaceAll("\"", ""):value;
						formedXmlSB.append(childTagString.replaceAll("\\!\\@\\#"+childReplaceString+"\\!\\@\\#", value));
					}
					formedXmlSB.append("</"+repeatTagArr[0]+">");
				}
				
		    	/* Update return XML */
		    	Matcher returnMatcher = Pattern.compile("^(.*?)\\<"+repeatTagArr[0]+"(.*?)\\<\\/"+repeatTagArr[0]+"\\>(.*?)$",Pattern.DOTALL).matcher(returnChartXml);
		    	if(returnMatcher.find()){
		    		returnChartXml = returnMatcher.group(1)+formedXmlSB+returnMatcher.group(3);
		    	}
			}
		}
		return returnChartXml;
	}

	public String updateReturnXmlForMultipleRepeatTagMultiY(String repeatTagMain, String xAxisCol, String yAxisCol, String zAxisCol, String seriesCol, String chartXML, ResultSet rs, StringBuffer dataExistCheck, CachedRowSet rsChild, String returnChartXml) throws SQLException{
		String[] repeatTagArr = repeatTagMain.split(",");
		if(repeatTagArr.length==2){
			Matcher matcherObj = Pattern.compile("\\<"+repeatTagArr[0]+"(.*?)\\<\\/"+repeatTagArr[0]+"\\>",Pattern.DOTALL).matcher(chartXML);
			if(matcherObj.find()){
				String fullTagString = "<"+repeatTagArr[0]+matcherObj.group(1)+"</"+repeatTagArr[0]+">";
				String parentTagStr = "";
				String colNameForParent = "";
				Matcher parentTagMatcher = Pattern.compile("\\<"+repeatTagArr[0]+"(.*?)\\>",Pattern.DOTALL).matcher(fullTagString);
				if(parentTagMatcher.find()){
					parentTagStr = "<"+repeatTagArr[0]+parentTagMatcher.group(1)+">";
				}
				Matcher valColMatcher = Pattern.compile("\\!\\@\\#(.*?)\\!\\@\\#",Pattern.DOTALL).matcher(parentTagStr);
				if(valColMatcher.find()){
					colNameForParent = getColumnName(valColMatcher.group(1), xAxisCol, yAxisCol, zAxisCol, seriesCol);
				}
				rs.beforeFirst();
				StringBuffer formedXmlSB = new StringBuffer();
				while(rs.next()){
		    		if(dataExistCheck.indexOf(","+rs.getObject(colNameForParent)+",")==-1){
		    			String value = String.valueOf(rs.getObject(colNameForParent));
		    			value = ValidationUtil.isValid(value)?value.replaceAll("\"", ""):value;
		    			formedXmlSB.append(parentTagStr.replaceAll("\\!\\@\\#"+valColMatcher.group(1)+"\\!\\@\\#", ValidationUtil.isValid(value)?value:"" ));
		    			
		    			Matcher matcherChildObj = Pattern.compile("\\<"+repeatTagArr[1]+"(.*?)\\<\\/"+repeatTagArr[1]+"\\>",Pattern.DOTALL).matcher(fullTagString);
						if(matcherChildObj.find()){
							String childTagString = "<"+repeatTagArr[1]+matcherChildObj.group(1)+"</"+repeatTagArr[1]+">";
							
							ArrayList<String> patternStrAL = new ArrayList<String>();
							ArrayList<String> patternStrColNameAL = new ArrayList<String>();
							Matcher valColMatcherChild = Pattern.compile("\\!\\@\\#(.*?)\\!\\@\\#",Pattern.DOTALL).matcher(childTagString);
							
							while(valColMatcherChild.find()){
								patternStrAL.add(valColMatcherChild.group(1));
								patternStrColNameAL.add(getColumnName(valColMatcherChild.group(1), xAxisCol, yAxisCol, zAxisCol, seriesCol));
							}
							for(String pattern:patternStrAL){
								rsChild.beforeFirst();
								while(rsChild.next()){
									if(String.valueOf(rsChild.getObject(colNameForParent)).equalsIgnoreCase(String.valueOf(rs.getObject(colNameForParent))) ){
										for(String colName:patternStrColNameAL.get(0).split(",")){
											String valueChild = String.valueOf(rsChild.getObject(colName));
											valueChild = ValidationUtil.isValid(valueChild)?valueChild.replaceAll("\"", ""):valueChild;
											formedXmlSB.append(childTagString.replaceAll("\\!\\@\\#"+pattern+"\\!\\@\\#", ValidationUtil.isValid(valueChild)?valueChild:""));
						    			}
									}
						    	}
							}
						}
						
						formedXmlSB.append("</"+repeatTagArr[0]+">");
			    		dataExistCheck.append(","+rs.getObject(colNameForParent)+",");
		    		}
		    	}
		    	/* Update return XML */
		    	Matcher returnMatcher = Pattern.compile("^(.*?)\\<"+repeatTagArr[0]+"(.*?)\\<\\/"+repeatTagArr[0]+"\\>(.*?)$",Pattern.DOTALL).matcher(returnChartXml);
		    	if(returnMatcher.find()){
		    		returnChartXml = returnMatcher.group(1)+formedXmlSB+returnMatcher.group(3);
		    	}
			}
		}
		return returnChartXml;
	}
	
	public String updateReturnXmlForMultipleRepeatTag(String repeatTagMain, String xAxisCol, String yAxisCol, String zAxisCol, String seriesCol, String chartXML, ResultSet rs, StringBuffer dataExistCheck, CachedRowSet rsChild, String returnChartXml) throws SQLException{
		String[] repeatTagArr = repeatTagMain.split(",");
		if(repeatTagArr.length==2){
			Matcher matcherObj = Pattern.compile("\\<"+repeatTagArr[0]+"(.*?)\\<\\/"+repeatTagArr[0]+"\\>",Pattern.DOTALL).matcher(chartXML);
			if(matcherObj.find()){
				String fullTagString = "<"+repeatTagArr[0]+matcherObj.group(1)+"</"+repeatTagArr[0]+">";
				String parentTagStr = "";
				String colNameForParent = "";
				Matcher parentTagMatcher = Pattern.compile("\\<"+repeatTagArr[0]+"(.*?)\\>",Pattern.DOTALL).matcher(fullTagString);
				if(parentTagMatcher.find()){
					parentTagStr = "<"+repeatTagArr[0]+parentTagMatcher.group(1)+">";
				}
				Matcher valColMatcher = Pattern.compile("\\!\\@\\#(.*?)\\!\\@\\#",Pattern.DOTALL).matcher(parentTagStr);
				if(valColMatcher.find()){
					colNameForParent = getColumnName(valColMatcher.group(1), xAxisCol, yAxisCol, zAxisCol, seriesCol);
				}
				rs.beforeFirst();
				StringBuffer formedXmlSB = new StringBuffer();
				while(rs.next()){
		    		if(dataExistCheck.indexOf(","+rs.getObject(colNameForParent)+",")==-1){
		    			String value = String.valueOf(rs.getObject(colNameForParent));
		    			value = ValidationUtil.isValid(value)?value.replaceAll("\"", ""):value;
		    			formedXmlSB.append(parentTagStr.replaceAll("\\!\\@\\#"+valColMatcher.group(1)+"\\!\\@\\#", ValidationUtil.isValid(value)?value:"" ));
		    			
		    			Matcher matcherChildObj = Pattern.compile("\\<"+repeatTagArr[1]+"(.*?)\\<\\/"+repeatTagArr[1]+"\\>",Pattern.DOTALL).matcher(fullTagString);
						if(matcherChildObj.find()){
							String childTagString = "<"+repeatTagArr[1]+matcherChildObj.group(1)+"</"+repeatTagArr[1]+">";
							
							ArrayList<String> patternStrAL = new ArrayList<String>();
							ArrayList<String> patternStrColNameAL = new ArrayList<String>();
							Matcher valColMatcherChild = Pattern.compile("\\!\\@\\#(.*?)\\!\\@\\#",Pattern.DOTALL).matcher(childTagString);
							
							while(valColMatcherChild.find()){
								patternStrAL.add(valColMatcherChild.group(1));
								patternStrColNameAL.add(getColumnName(valColMatcherChild.group(1), xAxisCol, yAxisCol, zAxisCol, seriesCol));
							}
							
							rsChild.beforeFirst();
							while(rsChild.next()){
								String tempTagString = childTagString;
								if(String.valueOf(rsChild.getObject(colNameForParent)).equalsIgnoreCase(String.valueOf(rs.getObject(colNameForParent))) ){
									int arrListIndex = 0;
									for(String colName:patternStrColNameAL){
										String valueChild = String.valueOf(rsChild.getObject(colName));
										valueChild = ValidationUtil.isValid(valueChild)?valueChild.replaceAll("\"", ""):valueChild;
					    				tempTagString = tempTagString.replaceAll("\\!\\@\\#"+patternStrAL.get(arrListIndex)+"\\!\\@\\#", ValidationUtil.isValid(valueChild)?valueChild:"");
					    				arrListIndex++;
					    			}
									formedXmlSB.append(tempTagString);
								}
					    	}
						}
						
						formedXmlSB.append("</"+repeatTagArr[0]+">");
			    		dataExistCheck.append(","+rs.getObject(colNameForParent)+",");
		    		}
		    	}
		    	/* Update return XML */
		    	Matcher returnMatcher = Pattern.compile("^(.*?)\\<"+repeatTagArr[0]+"(.*?)\\<\\/"+repeatTagArr[0]+"\\>(.*?)$",Pattern.DOTALL).matcher(returnChartXml);
		    	if(returnMatcher.find()){
		    		returnChartXml = returnMatcher.group(1)+formedXmlSB+returnMatcher.group(3);
		    	}
			}
		}
		return returnChartXml;
	}
	
	public String getColumnName(String pattern, String xAxisCol, String yAxisCol, String zAxisCol, String seriesCol){
		switch(pattern){
			case "X_AXIS":
				return xAxisCol;
			case "Y_AXIS":
				return yAxisCol;
			case "Z-AXIS":
				return zAxisCol;
			case "SERIES_AXIS":
				return seriesCol;
			default:
				return ".";
		}
	}
	
	public List<String> returnColorListBasedOnColorCount(int colorCount,String singleColor){
		Color randomColor = null;
		List<String> colorList = new ArrayList<String>();
		final int maxLimit = 255;
		colorList.add(singleColor);
		int difference = calculateDifferenceCountBase(colorCount);
		int breakMark = difference;
		if("ff0000".equalsIgnoreCase(singleColor)){
			do{
				randomColor = new Color(maxLimit, breakMark, breakMark);
				colorList.add(CommonUtils.rgb2Hex(randomColor));
				breakMark+=difference;
			}while(breakMark<maxLimit);
		}else if("00ff00".equalsIgnoreCase(singleColor)){
			do{
				randomColor = new Color(breakMark, maxLimit, breakMark);
				colorList.add(CommonUtils.rgb2Hex(randomColor));
				breakMark+=difference;
			}while(breakMark<maxLimit);
		}else if("0000ff".equalsIgnoreCase(singleColor)){
			do{
				randomColor = new Color(breakMark, breakMark, maxLimit);
				colorList.add(CommonUtils.rgb2Hex(randomColor));
				breakMark+=difference;
			}while(breakMark<maxLimit);
		}else{
			colorList = new ArrayList<String>();
			colorList.add("000000");
			do{
				randomColor = new Color(breakMark, breakMark, breakMark);
				colorList.add(CommonUtils.rgb2Hex(randomColor));
				breakMark+=difference;
			}while(breakMark<maxLimit);
		}
		return colorList;
	}
	
	public int calculateDifferenceCountBase(int count){
		if(count<5)return 50; if(count<10)return 25; if(count<12)return 20; if(count<15)return 15;
		if(count<25)return 10; if(count<50)return 5; if(count<85)return 3; if(count<125)return 2;
		return  1;
	}
	
	public List<WidgetDesignVb> getWidgetListing(WidgetDesignVb vObj) {
		return widgetDesignDao.getQueryResults(vObj);
	}
	
	public ExceptionCode insertRecord(WidgetDesignVb vObject){
		ExceptionCode exceptionCode  = new ExceptionCode();
		DeepCopy<WidgetDesignVb> deepCopy = new DeepCopy<WidgetDesignVb>();
		WidgetDesignVb clonedObject = null;
		try{
			clonedObject = deepCopy.copy(vObject);
			exceptionCode = widgetDesignDao.doInsertApprRecord(vObject);
			widgetDesignDao.fetchMakerVerifierNames(vObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			return exceptionCode;
		}
	}
	
	public ExceptionCode modifyRecord(WidgetDesignVb vObject){
		ExceptionCode exceptionCode  = null;
		DeepCopy<WidgetDesignVb> deepCopy = new DeepCopy<WidgetDesignVb>();
		WidgetDesignVb clonedObject = null;
		try{
			clonedObject = deepCopy.copy(vObject);
			exceptionCode = widgetDesignDao.doUpdateApprRecord(vObject);
			widgetDesignDao.fetchMakerVerifierNames(vObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}catch(RuntimeCustomException rex){
			logger.error("Modify Exception " + rex.getCode().getErrorMsg());
			logger.error( ((vObject==null)? "vObject is Null":vObject.toString()));
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(clonedObject);
			return exceptionCode;
		}
	}
	
	public ExceptionCode getAllWidgetsList() {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			List<WidgetDesignVb> widgetList = widgetDesignDao.getQueryResults(new WidgetDesignVb());
			exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
			exceptionCode.setResponse(widgetList);
		} catch(Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg(e.getMessage());
		}
		return exceptionCode;
	}
	
	public ExceptionCode getWidget(WidgetDesignVb vObject) {
		ExceptionCode exceptionCode = new ExceptionCode();
		DeepCopy<WidgetDesignVb> deepCopy = new DeepCopy<WidgetDesignVb>();
		WidgetDesignVb clonedObject = null;
		try {
			clonedObject = deepCopy.copy(vObject);
			List<WidgetDesignVb> widgetList = widgetDesignDao.getQueryResults(vObject);
			exceptionCode.setErrorCode( ValidationUtil.isValidList(widgetList)?Constants.SUCCESSFUL_OPERATION:Constants.ERRONEOUS_OPERATION);
			exceptionCode.setResponse( ValidationUtil.isValidList(widgetList)?widgetList.get(0):null);
		} catch(Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg(e.getMessage());
		}
		exceptionCode.setOtherInfo(clonedObject);
		return exceptionCode;
	}
	
	@Transactional(rollbackForClassName = { "com.vision.exception.RuntimeCustomException" })
	public ExceptionCode doInsertRecordForAccessControl(WidgetLODWrapperVb widgetDesiignLodVb, boolean isMain) throws RuntimeCustomException {
		ExceptionCode exceptionCode = new ExceptionCode();
		int result = Constants.ERRONEOUS_OPERATION;
		try {
			String sysDate = commonDao.getSystemDate();
			widgetDesiignLodVb.getMainModel().setDateCreation(sysDate);
			widgetDesiignLodVb.getMainModel().setDateLastModified(sysDate);
			result = widgetDesignDao.doInsertRecordForWidgetAccess(widgetDesiignLodVb, isMain);
			exceptionCode.setErrorCode(result);
			return exceptionCode;
		} catch (RuntimeCustomException rcException) {
			throw rcException;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	public ExceptionCode getLODForWidget(WidgetLODWrapperVb widgetLobVb) throws RuntimeCustomException {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			String sysDate = commonDao.getSystemDate();
			widgetLobVb.getMainModel().setDateCreation(sysDate);
			widgetLobVb.getMainModel().setDateLastModified(sysDate);
			exceptionCode.setResponse(widgetDesignDao.getRecordForWidgetLOD(widgetLobVb));
			exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
			return exceptionCode;
		} catch (RuntimeCustomException rcException) {
			throw rcException;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}


	public ExceptionCode deleteWidgetData(WidgetDesignVb widgetDesignVb) {
		ExceptionCode exceptionCode = widgetDesignDao.ValidatingDashboardWidgetData(widgetDesignVb.getWidgetId());
		if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
			widgetDesignDao.moveMainDataToAD(widgetDesignVb.getWidgetId());
		    widgetDesignDao.deleteRecords("VWC_MAIN_WIDGETS","WIDGET_ID",widgetDesignVb.getWidgetId());
		}
		return exceptionCode;
	}

	@Override
	protected AbstractDao<WidgetDesignVb> getScreenDao() {
		//	TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setAtNtValues(WidgetDesignVb vObject) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setVerifReqDeleteType(WidgetDesignVb vObject) {
		// TODO Auto-generated method stub
	}

}
