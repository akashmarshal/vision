package com.vision.vb;

public class VcReportGenerationVb extends CommonVb{

	private static final long serialVersionUID = 1410935578921368062L;
	private String reportId = "";
	private String reportDescription = "";
	private int visionId = 0;
	private String subReportId = "";
	private String sqlQuery = "";
	private String conditionalFormatting = "";
	private int reportTypeAt =713;
    private String reportType="";
    private int pageId=1;
    private String reportContext ="";
    private int vrdReportStatusNt=1;
    private int vrdReportStatus=0;
    private String queryId ="";
    private String condId ="";
    private String queryXml ="" ;
    private String conditionalXml ="" ;
    private String columnList = "";
    private String tabId ="";
    private String colId ="";
    private String catalogId ="" ;
    private String attributeType ="";
    private String columnNameStyle ="";
    private String colDisplay ="";
    private String groupBy ="N";
    private String condition ="";
    private String tableAlias ="";
    private String aliasName ="";
    private String sortList = "";
    private String columnName="";
    private String tableName ="";
    private String frmTableId ="";
    private String toTableId ="";
    private int joinTypeNt =41;
    private int joinType =1;
    private String joinString ="";
    private String filterCondition ="";
    private String rowSpanColumnList = "";
	private String subTotalList = "";
	private String sessionId = "";
	private String dataToDisplay="";
	private String styleForData =""; //vc_list_vb
	private String subHeaderList = "";
	private String subTitleList = "";

	
	private int promptPageId =0; //VC_PRompt_design;
	private String  promptPageTitle =""; //VC_PRompt_design;
	private String  promptXmlContent =""; //VC_PRompt_design;
	private String promptPageSort =""; //VC_PRompt_design;
	private int vrdPromptStatusNt =1; //VC_PRompt_design;
	private int vrdPromptStatus =0; //VC_PRompt_design; 
	private boolean scalingFactor=false;
	private boolean autoSubmit=false;
	
	private String pageNo = "";
	private String usedCol;
    private String displayCol;
    private String hashTag;
    private String hashTagVal;
    
    private String pageTitle="";
    private String headerXmlContent="";
    private String footerXmlContent="";
    private String reportXmlContent;
    private int pageSort =1;
    
    private String vrdQueryXml = "";
    private String vrdConditionalXml = "";
	private String reportTitle = "";
    
	private String databaseConnectivityDetails = "";
	private String lookupDataLoading = "";
	private String stgQuery1 = "";
	private String stgQuery2 = "";
	private String stgQuery3 = "";
	private String postQuery = "";	
	
	private String subTotalXml = "";
	private String proDimensions = "";
	private String proMeasures = "";
	private String proSortOrder = "";
	private String proTableColumns = "";
	private String subTotalLevelCount = "";
	private boolean isSubTotalAvailable = false;
	private String columnsMetadataXML = "";
	
	private String chartType = "";
	private String chartXML = "";
	private String swfFileName = "";
	
	private String gridLevel = "";
	private String requestType = "";
	
	private String scalingFactorValue = "";
	
	private boolean isDebugEnabled = true;
	
	// for SelfbI Mail Schedule report
	private String scheduleType = "";
	private String scheduleStartDate = "";
	private String scheduleEndDate = "";
	private String formatType = "";
	private String emailTo = "";
	private String emailCc = "";
	private String scheduleStatus = "";
	private String emailStatus = "";
	private int rsScheduleStatusNt = 1;
	private int rsScheduleStatus = 0;
	private String emailFrom = "";
	private String nextScheduleDate = "";
	private String scheduleSequenceNo = "";
	private String burstId = "";
	private String oldFlag ="N";
	private String newFlag ="N";
	private String blankReportFlag = "N";
	private int burstSequenceNo = 0;
	private int burstFlagCount = 0;
	private long userId = 0;
	private String promptHashVar ="";
	private String burstFlag = "";
	private int errorCount = 0;
	private String promptValue1 = "";
	private String promptValue2 = "";
	private String promptValue3 = "";
	private String promptValue4 = "";
	private String promptValue5 = "";
	private String promptValue6 = "";
	private String promptValue1Desc = "";
	private String promptValue2Desc = "";
	private String promptValue3Desc = "";
	private String promptValue4Desc = "";
	private String promptValue5Desc = "";
	private String promptValue6Desc = "";
	private String scallingFactor = "";
	private String scheduleOption = "E";
	private String ftpVarName ="";
	private int sourceScriptTypeAt = 1083;
	private String ScriptType =""; 
	
	public boolean isAutoSubmit() {
		return autoSubmit;
	}
	public void setAutoSubmit(boolean autoSubmit) {
		this.autoSubmit = autoSubmit;
	}
	public boolean isScalingFactor() {
		return scalingFactor;
	}
	public void setScalingFactor(boolean scalingFactor) {
		this.scalingFactor = scalingFactor;
	}
	public int getPromptPageId() {
		return promptPageId;
	}
	public void setPromptPageId(int promptPageId) {
		this.promptPageId = promptPageId;
	}
	public String getPromptPageTitle() {
		return promptPageTitle;
	}
	public void setPromptPageTitle(String promptPageTitle) {
		this.promptPageTitle = promptPageTitle;
	}
	public String getPromptXmlContent() {
		return promptXmlContent;
	}
	public void setPromptXmlContent(String promptXmlContent) {
		this.promptXmlContent = promptXmlContent;
	}
	public String getPromptPageSort() {
		return promptPageSort;
	}
	public void setPromptPageSort(String promptPageSort) {
		this.promptPageSort = promptPageSort;
	}
	public int getVrdPromptStatusNt() {
		return vrdPromptStatusNt;
	}
	public void setVrdPromptStatusNt(int vrdPromptStatusNt) {
		this.vrdPromptStatusNt = vrdPromptStatusNt;
	}
	public int getVrdPromptStatus() {
		return vrdPromptStatus;
	}
	public void setVrdPromptStatus(int vrdPromptStatus) {
		this.vrdPromptStatus = vrdPromptStatus;
	}
	public String getSubHeaderList() {
		return subHeaderList;
	}
	public void setSubHeaderList(String subHeaderList) {
		this.subHeaderList = subHeaderList;
	}
	public String getSubTitleList() {
		return subTitleList;
	}
	public void setSubTitleList(String subTitleList) {
		this.subTitleList = subTitleList;
	}
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	public int getVisionId() {
		return visionId;
	}
	public void setVisionId(int visionId) {
		this.visionId = visionId;
	}
	public String getSubReportId() {
		return subReportId;
	}
	public void setSubReportId(String subReportId) {
		this.subReportId = subReportId;
	}
	public String getSqlQuery() {
		return sqlQuery;
	}
	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}
	public String getConditionalFormatting() {
		return conditionalFormatting;
	}
	public void setConditionalFormatting(String conditionalFormatting) {
		this.conditionalFormatting = conditionalFormatting;
	}
	public String getReportDescription() {
		return reportDescription;
	}
	public void setReportDescription(String reportDescription) {
		this.reportDescription = reportDescription;
	}
	public int getReportTypeAt() {
		return reportTypeAt;
	}
	public void setReportTypeAt(int reportTypeAt) {
		this.reportTypeAt = reportTypeAt;
	}
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	public int getPageId() {
		return pageId;
	}
	public void setPageId(int pageId) {
		this.pageId = pageId;
	}
	public String getReportContext() {
		return reportContext;
	}
	public void setReportContext(String reportContext) {
		this.reportContext = reportContext;
	}
	public int getVrdReportStatusNt() {
		return vrdReportStatusNt;
	}
	public void setVrdReportStatusNt(int vrdReportStatusNt) {
		this.vrdReportStatusNt = vrdReportStatusNt;
	}
	public int getVrdReportStatus() {
		return vrdReportStatus;
	}
	public void setVrdReportStatus(int vrdReportStatus) {
		this.vrdReportStatus = vrdReportStatus;
	}
	public String getQueryId() {
		return queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	public String getCondId() {
		return condId;
	}
	public void setCondId(String condId) {
		this.condId = condId;
	}
	public String getQueryXml() {
		return queryXml;
	}
	public void setQueryXml(String queryXml) {
		this.queryXml = queryXml;
	}
	public String getConditionalXml() {
		return conditionalXml;
	}
	public void setConditionalXml(String conditionalXml) {
		this.conditionalXml = conditionalXml;
	}
	public String getColumnList() {
		return columnList;
	}
	public void setColumnList(String columnList) {
		this.columnList = columnList;
	}
	public String getTabId() {
		return tabId;
	}
	public void setTabId(String tabId) {
		this.tabId = tabId;
	}
	public String getColId() {
		return colId;
	}
	public void setColId(String colId) {
		this.colId = colId;
	}
	public String getCatalogId() {
		return catalogId;
	}
	public void setCatalogId(String catalogId) {
		this.catalogId = catalogId;
	}
	public String getAttributeType() {
		return attributeType;
	}
	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}
	public String getColumnNameStyle() {
		return columnNameStyle;
	}
	public void setColumnNameStyle(String columnNameStyle) {
		this.columnNameStyle = columnNameStyle;
	}
	public String getColDisplay() {
		return colDisplay;
	}
	public void setColDisplay(String colDisplay) {
		this.colDisplay = colDisplay;
	}
	public String getGroupBy() {
		return groupBy;
	}
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getTableAlias() {
		return tableAlias;
	}
	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}
	public String getAliasName() {
		return aliasName;
	}
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}
	public String getSortList() {
		return sortList;
	}
	public void setSortList(String sortList) {
		this.sortList = sortList;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getFrmTableId() {
		return frmTableId;
	}
	public void setFrmTableId(String frmTableId) {
		this.frmTableId = frmTableId;
	}
	public String getToTableId() {
		return toTableId;
	}
	public void setToTableId(String toTableId) {
		this.toTableId = toTableId;
	}
	public int getJoinTypeNt() {
		return joinTypeNt;
	}
	public void setJoinTypeNt(int joinTypeNt) {
		this.joinTypeNt = joinTypeNt;
	}
	public int getJoinType() {
		return joinType;
	}
	public void setJoinType(int joinType) {
		this.joinType = joinType;
	}
	public String getJoinString() {
		return joinString;
	}
	public void setJoinString(String joinString) {
		this.joinString = joinString;
	}
	public String getFilterCondition() {
		return filterCondition;
	}
	public void setFilterCondition(String filterCondition) {
		this.filterCondition = filterCondition;
	}
	public String getRowSpanColumnList() {
		return rowSpanColumnList;
	}
	public void setRowSpanColumnList(String rowSpanColumnList) {
		this.rowSpanColumnList = rowSpanColumnList;
	}
	public String getSubTotalList() {
		return subTotalList;
	}
	public void setSubTotalList(String subTotalList) {
		this.subTotalList = subTotalList;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getDataToDisplay() {
		return dataToDisplay;
	}
	public void setDataToDisplay(String dataToDisplay) {
		this.dataToDisplay = dataToDisplay;
	}
	public String getStyleForData() {
		return styleForData;
	}
	public void setStyleForData(String styleForData) {
		this.styleForData = styleForData;
	}
	public String getPageNo() {
		return pageNo;
	}
	public void setPageNo(String pageNo) {
		this.pageNo = pageNo;
	}
	public String getUsedCol() {
		return usedCol;
	}
	public void setUsedCol(String usedCol) {
		this.usedCol = usedCol;
	}
	public String getDisplayCol() {
		return displayCol;
	}
	public void setDisplayCol(String displayCol) {
		this.displayCol = displayCol;
	}
	public String getHashTag() {
		return hashTag;
	}
	public void setHashTag(String hashTag) {
		this.hashTag = hashTag;
	}
	public String getHashTagVal() {
		return hashTagVal;
	}
	public void setHashTagVal(String hashTagVal) {
		this.hashTagVal = hashTagVal;
	}
	public String getPageTitle() {
		return pageTitle;
	}
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	public String getHeaderXmlContent() {
		return headerXmlContent;
	}
	public void setHeaderXmlContent(String headerXmlContent) {
		this.headerXmlContent = headerXmlContent;
	}
	public String getFooterXmlContent() {
		return footerXmlContent;
	}
	public void setFooterXmlContent(String footerXmlContent) {
		this.footerXmlContent = footerXmlContent;
	}
	public String getReportXmlContent() {
		return reportXmlContent;
	}
	public void setReportXmlContent(String reportXmlContent) {
		this.reportXmlContent = reportXmlContent;
	}
	public int getPageSort() {
		return pageSort;
	}
	public void setPageSort(int pageSort) {
		this.pageSort = pageSort;
	}
	public String getVrdQueryXml() {
		return vrdQueryXml;
	}
	public void setVrdQueryXml(String vrdQueryXml) {
		this.vrdQueryXml = vrdQueryXml;
	}
	public String getVrdConditionalXml() {
		return vrdConditionalXml;
	}
	public void setVrdConditionalXml(String vrdConditionalXml) {
		this.vrdConditionalXml = vrdConditionalXml;
	}
	public String getReportTitle() {
		return reportTitle;
	}
	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}
	public String getDatabaseConnectivityDetails() {
		return databaseConnectivityDetails;
	}
	public void setDatabaseConnectivityDetails(String databaseConnectivityDetails) {
		this.databaseConnectivityDetails = databaseConnectivityDetails;
	}
	public String getLookupDataLoading() {
		return lookupDataLoading;
	}
	public void setLookupDataLoading(String lookupDataLoading) {
		this.lookupDataLoading = lookupDataLoading;
	}
	public String getStgQuery1() {
		return stgQuery1;
	}
	public void setStgQuery1(String stgQuery1) {
		this.stgQuery1 = stgQuery1;
	}
	public String getStgQuery2() {
		return stgQuery2;
	}
	public void setStgQuery2(String stgQuery2) {
		this.stgQuery2 = stgQuery2;
	}
	public String getStgQuery3() {
		return stgQuery3;
	}
	public void setStgQuery3(String stgQuery3) {
		this.stgQuery3 = stgQuery3;
	}
	public String getPostQuery() {
		return postQuery;
	}
	public void setPostQuery(String postQuery) {
		this.postQuery = postQuery;
	}
	public String getSubTotalXml() {
		return subTotalXml;
	}
	public void setSubTotalXml(String subTotalXml) {
		this.subTotalXml = subTotalXml;
	}
	public String getProDimensions() {
		return proDimensions;
	}
	public void setProDimensions(String proDimensions) {
		this.proDimensions = proDimensions;
	}
	public String getProMeasures() {
		return proMeasures;
	}
	public void setProMeasures(String proMeasures) {
		this.proMeasures = proMeasures;
	}
	public String getProSortOrder() {
		return proSortOrder;
	}
	public void setProSortOrder(String proSortOrder) {
		this.proSortOrder = proSortOrder;
	}
	public String getProTableColumns() {
		return proTableColumns;
	}
	public void setProTableColumns(String proTableColumns) {
		this.proTableColumns = proTableColumns;
	}
	public String getSubTotalLevelCount() {
		return subTotalLevelCount;
	}
	public void setSubTotalLevelCount(String subTotalLevelCount) {
		this.subTotalLevelCount = subTotalLevelCount;
	}
	public boolean isSubTotalAvailable() {
		return isSubTotalAvailable;
	}
	public void setSubTotalAvailable(boolean isSubTotalAvailable) {
		this.isSubTotalAvailable = isSubTotalAvailable;
	}
	public String getColumnsMetadataXML() {
		return columnsMetadataXML;
	}
	public void setColumnsMetadataXML(String columnsMetadataXML) {
		this.columnsMetadataXML = columnsMetadataXML;
	}
	public String getChartType() {
		return chartType;
	}
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	public String getChartXML() {
		return chartXML;
	}
	public void setChartXML(String chartXML) {
		this.chartXML = chartXML;
	}
	public String getSwfFileName() {
		return swfFileName;
	}
	public void setSwfFileName(String swfFileName) {
		this.swfFileName = swfFileName;
	}
	public String getGridLevel() {
		return gridLevel;
	}
	public void setGridLevel(String gridLevel) {
		this.gridLevel = gridLevel;
	}
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getScalingFactorValue() {
		return scalingFactorValue;
	}
	public void setScalingFactorValue(String scalingFactorValue) {
		this.scalingFactorValue = scalingFactorValue;
	}
	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}
	public void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}
	public String getScheduleType() {
		return scheduleType;
	}
	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}
	public String getScheduleStartDate() {
		return scheduleStartDate;
	}
	public void setScheduleStartDate(String scheduleStartDate) {
		this.scheduleStartDate = scheduleStartDate;
	}
	public String getScheduleEndDate() {
		return scheduleEndDate;
	}
	public void setScheduleEndDate(String scheduleEndDate) {
		this.scheduleEndDate = scheduleEndDate;
	}
	public String getFormatType() {
		return formatType;
	}
	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}
	public String getEmailTo() {
		return emailTo;
	}
	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}
	public String getEmailCc() {
		return emailCc;
	}
	public void setEmailCc(String emailCc) {
		this.emailCc = emailCc;
	}
	public String getScheduleStatus() {
		return scheduleStatus;
	}
	public void setScheduleStatus(String scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}
	public String getEmailStatus() {
		return emailStatus;
	}
	public void setEmailStatus(String emailStatus) {
		this.emailStatus = emailStatus;
	}
	public int getRsScheduleStatusNt() {
		return rsScheduleStatusNt;
	}
	public void setRsScheduleStatusNt(int rsScheduleStatusNt) {
		this.rsScheduleStatusNt = rsScheduleStatusNt;
	}
	public int getRsScheduleStatus() {
		return rsScheduleStatus;
	}
	public void setRsScheduleStatus(int rsScheduleStatus) {
		this.rsScheduleStatus = rsScheduleStatus;
	}
	public String getEmailFrom() {
		return emailFrom;
	}
	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}
	public String getNextScheduleDate() {
		return nextScheduleDate;
	}
	public void setNextScheduleDate(String nextScheduleDate) {
		this.nextScheduleDate = nextScheduleDate;
	}
	public String getScheduleSequenceNo() {
		return scheduleSequenceNo;
	}
	public void setScheduleSequenceNo(String scheduleSequenceNo) {
		this.scheduleSequenceNo = scheduleSequenceNo;
	}
	public String getBurstId() {
		return burstId;
	}
	public void setBurstId(String burstId) {
		this.burstId = burstId;
	}
	public String getOldFlag() {
		return oldFlag;
	}
	public void setOldFlag(String oldFlag) {
		this.oldFlag = oldFlag;
	}
	public String getNewFlag() {
		return newFlag;
	}
	public void setNewFlag(String newFlag) {
		this.newFlag = newFlag;
	}
	public String getBlankReportFlag() {
		return blankReportFlag;
	}
	public void setBlankReportFlag(String blankReportFlag) {
		this.blankReportFlag = blankReportFlag;
	}
	public int getBurstSequenceNo() {
		return burstSequenceNo;
	}
	public void setBurstSequenceNo(int burstSequenceNo) {
		this.burstSequenceNo = burstSequenceNo;
	}
	public int getBurstFlagCount() {
		return burstFlagCount;
	}
	public void setBurstFlagCount(int burstFlagCount) {
		this.burstFlagCount = burstFlagCount;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getPromptHashVar() {
		return promptHashVar;
	}
	public void setPromptHashVar(String promptHashVar) {
		this.promptHashVar = promptHashVar;
	}
	public String getBurstFlag() {
		return burstFlag;
	}
	public void setBurstFlag(String burstFlag) {
		this.burstFlag = burstFlag;
	}
	public String getPromptValue1() {
		return promptValue1;
	}
	public void setPromptValue1(String promptValue1) {
		this.promptValue1 = promptValue1;
	}
	public String getPromptValue2() {
		return promptValue2;
	}
	public void setPromptValue2(String promptValue2) {
		this.promptValue2 = promptValue2;
	}
	public String getPromptValue3() {
		return promptValue3;
	}
	public void setPromptValue3(String promptValue3) {
		this.promptValue3 = promptValue3;
	}
	public String getPromptValue4() {
		return promptValue4;
	}
	public void setPromptValue4(String promptValue4) {
		this.promptValue4 = promptValue4;
	}
	public String getPromptValue5() {
		return promptValue5;
	}
	public void setPromptValue5(String promptValue5) {
		this.promptValue5 = promptValue5;
	}
	public String getPromptValue6() {
		return promptValue6;
	}
	public void setPromptValue6(String promptValue6) {
		this.promptValue6 = promptValue6;
	}
	public String getPromptValue1Desc() {
		return promptValue1Desc;
	}
	public void setPromptValue1Desc(String promptValue1Desc) {
		this.promptValue1Desc = promptValue1Desc;
	}
	public String getPromptValue2Desc() {
		return promptValue2Desc;
	}
	public void setPromptValue2Desc(String promptValue2Desc) {
		this.promptValue2Desc = promptValue2Desc;
	}
	public String getPromptValue3Desc() {
		return promptValue3Desc;
	}
	public void setPromptValue3Desc(String promptValue3Desc) {
		this.promptValue3Desc = promptValue3Desc;
	}
	public String getPromptValue4Desc() {
		return promptValue4Desc;
	}
	public void setPromptValue4Desc(String promptValue4Desc) {
		this.promptValue4Desc = promptValue4Desc;
	}
	public String getPromptValue5Desc() {
		return promptValue5Desc;
	}
	public void setPromptValue5Desc(String promptValue5Desc) {
		this.promptValue5Desc = promptValue5Desc;
	}
	public String getPromptValue6Desc() {
		return promptValue6Desc;
	}
	public void setPromptValue6Desc(String promptValue6Desc) {
		this.promptValue6Desc = promptValue6Desc;
	}
	public int getErrorCount() {
		return errorCount;
	}
	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}
	public String getScallingFactor() {
		return scallingFactor;
	}
	public void setScallingFactor(String scallingFactor) {
		this.scallingFactor = scallingFactor;
	}
	public String getScheduleOption() {
		return scheduleOption;
	}
	public void setScheduleOption(String scheduleOption) {
		this.scheduleOption = scheduleOption;
	}
	public int getSourceScriptTypeAt() {
		return sourceScriptTypeAt;
	}
	public void setSourceScriptTypeAt(int sourceScriptTypeAt) {
		this.sourceScriptTypeAt = sourceScriptTypeAt;
	}
	public String getScriptType() {
		return ScriptType;
	}
	public void setScriptType(String scriptType) {
		ScriptType = scriptType;
	}
	public String getFtpVarName() {
		return ftpVarName;
	}
	public void setFtpVarName(String ftpVarName) {
		this.ftpVarName = ftpVarName;
	}
}
