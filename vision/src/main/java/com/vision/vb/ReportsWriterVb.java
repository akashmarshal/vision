package com.vision.vb;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class ReportsWriterVb extends CommonVb {

	private static final long serialVersionUID = 1L;
	
	private int pdfWidth = 842;
	private int pdfHeight = 595;
	private int labelRowCount = 0; 
	private int labelColCount = 0; 
	private int captionLabelColCount = 0;
	private int rwStatusNt = 0;
	private int rwStatus = -1;
	private int  displayOrder=-1;
	
	private boolean level1 = false;
	private boolean level2 = false;
	private boolean level3 = false;	
	private boolean drillFlag = false;
	private boolean levelScreen = false;
	private boolean cascadePrompt = false;

	private String promptId1; 
	private String promptId2;
	private String promptId3;
	private String promptId4;
	private String promptId5;
	private String promptId6;
	
	private String parameter1;
	private String parameter2;
	private String parameter3;
	private String parameter4;
	private String parameter5;
	
	private String promptId1Desc; 
	private String promptId2Desc;
	private String promptId3Desc;
	private String promptId4Desc;
	private String promptId5Desc;
	private String promptId6Desc;
	
	private String promptValue1 = "";
	private String promptValue2 = "";
	private String promptValue3 = "";
	private String promptValue4 = "";
	private String promptValue5 = "";
	private String promptValue6 = "";
	
	private String promptLabel1 = "";
	private String promptLabel2 = "";
	private String promptLabel3 = "";
	private String promptLabel4 = "";
	private String promptLabel5 = "";
	private String promptLabel6 = "";
	
	private String promptMacroMappings1 = "";
	private String promptMacroMappings2 = "";
	private String promptMacroMappings3 = "";
	private String promptMacroMappings4 = "";
	private String promptMacroMappings5 = "";
	private String promptMacroMappings6 = "";
	
	private String promptValue1Desc = "";
	private String promptValue2Desc = "";
	private String promptValue3Desc = "";
	private String promptValue4Desc = "";
	private String promptValue5Desc = "";
	private String promptValue6Desc = "";
	
	private String promptPosition1="";
	private String promptPosition2="";
	private String promptPosition3="";
	private String promptPosition4="";
	private String promptPosition5="";
	private String promptPosition6="";

	
	private String caption1;
	private String caption2;
	private String caption3;
	
	private String ddFlag = "N";
	private String newChkFlag = "N";
	private String blankReportFlag = "N";
	private String supressZeroFlag = "N";
	private String countryFlag = "N"; 
	private String leBookFlag = "N";
	private String legalVehiclesFlag = "N";
	private String regionProvinceFlag = "N";
	private String businessGroupFlag = "N";
	private String productSuperGroupFlag = "N";
	private String oucAttributeFlag = "N";
	private String sbuCodeFlag = "N";
	private String productAttributeFlag = "N";
	private String accountOfficerFlag = "N";
	
	private String reportId="";
	private String reportDescription;
	private String reportTitle;
	private String reportCategory;
	private String reportCategoryAt = "451";
	private String subCategory;
	private String subCategoryAt = "453";
	private String procedure1; 
	private String filterString;
	private String reportCategoryDesc;
	private String orientation;
	private String scalingFactor;
	private String reportType;
	private String sessionId =  "";
	private String displayType = "-1";
	private String templateName = "";
	private String errorStatus;
	private String errorMessage;
	private String drillDownId;
	private String burstFlag = "";
	private String refrenceId = "";
	private String runType = "";
	private String scallingFactor ="";
	private String scheduleType = "";
	private String formatType = "";
	private String emailTo = "";
	private String emailCc = "";
	private String catalogQuery = "";
	private String ddReportId = "";
	private String reportSubject = "";
	private String reportBody = "";
	private String frlMemoMapSampleFrlLine = "";
	private String frlMemoMapSampleBalType = "";
	
	private MultipartFile file;
	private RsDashboardVb dashboards;
	private List<ReportsWriterVb> drilDownList;
	private List<ReportsWriterVb> children;
	private List<ReportStgVb> reportsData = null;
	private List<ColumnHeadersVb> columnHeaders = null;
	private List<RsChartsVb> chartTypes = null;
	
  	private String userName;
    private String password;
    private String nodeName;
    private String hostName;
    private String serverName;
    
    private String usedCol;
    private String displayCol;
    private String hashTag;
    private String hashTagVal;
    
    private int pageId=1;
    private String pageTitle="";
    private String headerXmlContent="";
    private String footerXmlContent="";
    private String reportXmlContent;
    private int pageSort =1;
    
    private boolean isNewHeaderMethod = false;
    private String csvSeparator="";
    
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
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public int getPdfWidth() {
		return pdfWidth;
	}
	public void setPdfWidth(int pdfWidth) {
		this.pdfWidth = pdfWidth;
	}
	public int getPdfHeight() {
		return pdfHeight;
	}
	public void setPdfHeight(int pdfHeight) {
		this.pdfHeight = pdfHeight;
	}
	public int getLabelRowCount() {
		return labelRowCount;
	}
	public void setLabelRowCount(int labelRowCount) {
		this.labelRowCount = labelRowCount;
	}
	public int getLabelColCount() {
		return labelColCount;
	}
	public void setLabelColCount(int labelColCount) {
		this.labelColCount = labelColCount;
	}
	public int getCaptionLabelColCount() {
		return captionLabelColCount;
	}
	public void setCaptionLabelColCount(int captionLabelColCount) {
		this.captionLabelColCount = captionLabelColCount;
	}
	public int getRwStatusNt() {
		return rwStatusNt;
	}
	public void setRwStatusNt(int rwStatusNt) {
		this.rwStatusNt = rwStatusNt;
	}
	public int getRwStatus() {
		return rwStatus;
	}
	public void setRwStatus(int rwStatus) {
		this.rwStatus = rwStatus;
	}
	public int getDisplayOrder() {
		return displayOrder;
	}
	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
	public boolean isLevel1() {
		return level1;
	}
	public void setLevel1(boolean level1) {
		this.level1 = level1;
	}
	public boolean isLevel2() {
		return level2;
	}
	public void setLevel2(boolean level2) {
		this.level2 = level2;
	}
	public boolean isLevel3() {
		return level3;
	}
	public void setLevel3(boolean level3) {
		this.level3 = level3;
	}
	public boolean isDrillFlag() {
		return drillFlag;
	}
	public void setDrillFlag(boolean drillFlag) {
		this.drillFlag = drillFlag;
	}
	public boolean isLevelScreen() {
		return levelScreen;
	}
	public void setLevelScreen(boolean levelScreen) {
		this.levelScreen = levelScreen;
	}
	public boolean isCascadePrompt() {
		return cascadePrompt;
	}
	public void setCascadePrompt(boolean cascadePrompt) {
		this.cascadePrompt = cascadePrompt;
	}
	public String getDdFlag() {
		return ddFlag;
	}
	public void setDdFlag(String ddFlag) {
		this.ddFlag = ddFlag;
	}
	public String getNewChkFlag() {
		return newChkFlag;
	}
	public void setNewChkFlag(String newChkFlag) {
		this.newChkFlag = newChkFlag;
	}
	public String getBlankReportFlag() {
		return blankReportFlag;
	}
	public void setBlankReportFlag(String blankReportFlag) {
		this.blankReportFlag = blankReportFlag;
	}
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	public String getReportDescription() {
		return reportDescription;
	}
	public void setReportDescription(String reportDescription) {
		this.reportDescription = reportDescription;
	}
	public String getReportTitle() {
		return reportTitle;
	}
	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}
	public String getReportCategory() {
		return reportCategory;
	}
	public void setReportCategory(String reportCategory) {
		this.reportCategory = reportCategory;
	}
	public String getReportCategoryAt() {
		return reportCategoryAt;
	}
	public void setReportCategoryAt(String reportCategoryAt) {
		this.reportCategoryAt = reportCategoryAt;
	}
	public String getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}
	public String getSubCategoryAt() {
		return subCategoryAt;
	}
	public void setSubCategoryAt(String subCategoryAt) {
		this.subCategoryAt = subCategoryAt;
	}
	public String getProcedure1() {
		return procedure1;
	}
	public void setProcedure1(String procedure1) {
		this.procedure1 = procedure1;
	}
	public String getFilterString() {
		return filterString;
	}
	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}
	public String getPromptId1() {
		return promptId1;
	}
	public void setPromptId1(String promptId1) {
		this.promptId1 = promptId1;
	}
	public String getPromptId2() {
		return promptId2;
	}
	public void setPromptId2(String promptId2) {
		this.promptId2 = promptId2;
	}
	public String getPromptId3() {
		return promptId3;
	}
	public void setPromptId3(String promptId3) {
		this.promptId3 = promptId3;
	}
	public String getPromptId4() {
		return promptId4;
	}
	public void setPromptId4(String promptId4) {
		this.promptId4 = promptId4;
	}
	public String getPromptId5() {
		return promptId5;
	}
	public void setPromptId5(String promptId5) {
		this.promptId5 = promptId5;
	}
	public String getPromptId6() {
		return promptId6;
	}
	public void setPromptId6(String promptId6) {
		this.promptId6 = promptId6;
	}
	public String getCountryFlag() {
		return countryFlag;
	}
	public void setCountryFlag(String countryFlag) {
		this.countryFlag = countryFlag;
	}
	public String getLeBookFlag() {
		return leBookFlag;
	}
	public void setLeBookFlag(String leBookFlag) {
		this.leBookFlag = leBookFlag;
	}
	public String getLegalVehiclesFlag() {
		return legalVehiclesFlag;
	}
	public void setLegalVehiclesFlag(String legalVehiclesFlag) {
		this.legalVehiclesFlag = legalVehiclesFlag;
	}
	public String getRegionProvinceFlag() {
		return regionProvinceFlag;
	}
	public void setRegionProvinceFlag(String regionProvinceFlag) {
		this.regionProvinceFlag = regionProvinceFlag;
	}
	public String getBusinessGroupFlag() {
		return businessGroupFlag;
	}
	public void setBusinessGroupFlag(String businessGroupFlag) {
		this.businessGroupFlag = businessGroupFlag;
	}
	public String getProductSuperGroupFlag() {
		return productSuperGroupFlag;
	}
	public void setProductSuperGroupFlag(String productSuperGroupFlag) {
		this.productSuperGroupFlag = productSuperGroupFlag;
	}
	public String getOucAttributeFlag() {
		return oucAttributeFlag;
	}
	public void setOucAttributeFlag(String oucAttributeFlag) {
		this.oucAttributeFlag = oucAttributeFlag;
	}
	public String getSbuCodeFlag() {
		return sbuCodeFlag;
	}
	public void setSbuCodeFlag(String sbuCodeFlag) {
		this.sbuCodeFlag = sbuCodeFlag;
	}
	public String getProductAttributeFlag() {
		return productAttributeFlag;
	}
	public void setProductAttributeFlag(String productAttributeFlag) {
		this.productAttributeFlag = productAttributeFlag;
	}
	public String getAccountOfficerFlag() {
		return accountOfficerFlag;
	}
	public void setAccountOfficerFlag(String accountOfficerFlag) {
		this.accountOfficerFlag = accountOfficerFlag;
	}
	public String getReportCategoryDesc() {
		return reportCategoryDesc;
	}
	public void setReportCategoryDesc(String reportCategoryDesc) {
		this.reportCategoryDesc = reportCategoryDesc;
	}
	public String getOrientation() {
		return orientation;
	}
	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}
	public String getScalingFactor() {
		return scalingFactor;
	}
	public void setScalingFactor(String scalingFactor) {
		this.scalingFactor = scalingFactor;
	}
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getDisplayType() {
		return displayType;
	}
	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public String getCaption1() {
		return caption1;
	}
	public void setCaption1(String caption1) {
		this.caption1 = caption1;
	}
	public String getCaption2() {
		return caption2;
	}
	public void setCaption2(String caption2) {
		this.caption2 = caption2;
	}
	public String getCaption3() {
		return caption3;
	}
	public void setCaption3(String caption3) {
		this.caption3 = caption3;
	}
	public String getParameter1() {
		return parameter1;
	}
	public void setParameter1(String parameter1) {
		this.parameter1 = parameter1;
	}
	public String getParameter2() {
		return parameter2;
	}
	public void setParameter2(String parameter2) {
		this.parameter2 = parameter2;
	}
	public String getParameter3() {
		return parameter3;
	}
	public void setParameter3(String parameter3) {
		this.parameter3 = parameter3;
	}
	public String getParameter4() {
		return parameter4;
	}
	public void setParameter4(String parameter4) {
		this.parameter4 = parameter4;
	}
	public String getParameter5() {
		return parameter5;
	}
	public void setParameter5(String parameter5) {
		this.parameter5 = parameter5;
	}
	public String getSupressZeroFlag() {
		return supressZeroFlag;
	}
	public void setSupressZeroFlag(String supressZeroFlag) {
		this.supressZeroFlag = supressZeroFlag;
	}
	public String getPromptId1Desc() {
		return promptId1Desc;
	}
	public void setPromptId1Desc(String promptId1Desc) {
		this.promptId1Desc = promptId1Desc;
	}
	public String getPromptId2Desc() {
		return promptId2Desc;
	}
	public void setPromptId2Desc(String promptId2Desc) {
		this.promptId2Desc = promptId2Desc;
	}
	public String getPromptId3Desc() {
		return promptId3Desc;
	}
	public void setPromptId3Desc(String promptId3Desc) {
		this.promptId3Desc = promptId3Desc;
	}
	public String getPromptId4Desc() {
		return promptId4Desc;
	}
	public void setPromptId4Desc(String promptId4Desc) {
		this.promptId4Desc = promptId4Desc;
	}
	public String getPromptId5Desc() {
		return promptId5Desc;
	}
	public void setPromptId5Desc(String promptId5Desc) {
		this.promptId5Desc = promptId5Desc;
	}
	public String getPromptId6Desc() {
		return promptId6Desc;
	}
	public void setPromptId6Desc(String promptId6Desc) {
		this.promptId6Desc = promptId6Desc;
	}
	public String getErrorStatus() {
		return errorStatus;
	}
	public void setErrorStatus(String errorStatus) {
		this.errorStatus = errorStatus;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getDrillDownId() {
		return drillDownId;
	}
	public void setDrillDownId(String drillDownId) {
		this.drillDownId = drillDownId;
	}
	public String getBurstFlag() {
		return burstFlag;
	}
	public void setBurstFlag(String burstFlag) {
		this.burstFlag = burstFlag;
	}
	public String getRefrenceId() {
		return refrenceId;
	}
	public void setRefrenceId(String refrenceId) {
		this.refrenceId = refrenceId;
	}
	public String getRunType() {
		return runType;
	}
	public void setRunType(String runType) {
		this.runType = runType;
	}
	public String getScallingFactor() {
		return scallingFactor;
	}
	public void setScallingFactor(String scallingFactor) {
		this.scallingFactor = scallingFactor;
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
	public String getPromptLabel1() {
		return promptLabel1;
	}
	public void setPromptLabel1(String promptLabel1) {
		this.promptLabel1 = promptLabel1;
	}
	public String getPromptLabel2() {
		return promptLabel2;
	}
	public void setPromptLabel2(String promptLabel2) {
		this.promptLabel2 = promptLabel2;
	}
	public String getPromptLabel3() {
		return promptLabel3;
	}
	public void setPromptLabel3(String promptLabel3) {
		this.promptLabel3 = promptLabel3;
	}
	public String getPromptLabel4() {
		return promptLabel4;
	}
	public void setPromptLabel4(String promptLabel4) {
		this.promptLabel4 = promptLabel4;
	}
	public String getPromptLabel5() {
		return promptLabel5;
	}
	public void setPromptLabel5(String promptLabel5) {
		this.promptLabel5 = promptLabel5;
	}
	public String getPromptLabel6() {
		return promptLabel6;
	}
	public void setPromptLabel6(String promptLabel6) {
		this.promptLabel6 = promptLabel6;
	}
	public String getPromptMacroMappings1() {
		return promptMacroMappings1;
	}
	public void setPromptMacroMappings1(String promptMacroMappings1) {
		this.promptMacroMappings1 = promptMacroMappings1;
	}
	public String getPromptMacroMappings2() {
		return promptMacroMappings2;
	}
	public void setPromptMacroMappings2(String promptMacroMappings2) {
		this.promptMacroMappings2 = promptMacroMappings2;
	}
	public String getPromptMacroMappings3() {
		return promptMacroMappings3;
	}
	public void setPromptMacroMappings3(String promptMacroMappings3) {
		this.promptMacroMappings3 = promptMacroMappings3;
	}
	public String getPromptMacroMappings4() {
		return promptMacroMappings4;
	}
	public void setPromptMacroMappings4(String promptMacroMappings4) {
		this.promptMacroMappings4 = promptMacroMappings4;
	}
	public String getPromptMacroMappings5() {
		return promptMacroMappings5;
	}
	public void setPromptMacroMappings5(String promptMacroMappings5) {
		this.promptMacroMappings5 = promptMacroMappings5;
	}
	public String getPromptMacroMappings6() {
		return promptMacroMappings6;
	}
	public void setPromptMacroMappings6(String promptMacroMappings6) {
		this.promptMacroMappings6 = promptMacroMappings6;
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
	public String getScheduleType() {
		return scheduleType;
	}
	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
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
	public String getCatalogQuery() {
		return catalogQuery;
	}
	public void setCatalogQuery(String catalogQuery) {
		this.catalogQuery = catalogQuery;
	}
	public String getDdReportId() {
		return ddReportId;
	}
	public void setDdReportId(String ddReportId) {
		this.ddReportId = ddReportId;
	}
	public String getReportSubject() {
		return reportSubject;
	}
	public void setReportSubject(String reportSubject) {
		this.reportSubject = reportSubject;
	}
	public String getReportBody() {
		return reportBody;
	}
	public void setReportBody(String reportBody) {
		this.reportBody = reportBody;
	}
	public String getFrlMemoMapSampleFrlLine() {
		return frlMemoMapSampleFrlLine;
	}
	public void setFrlMemoMapSampleFrlLine(String frlMemoMapSampleFrlLine) {
		this.frlMemoMapSampleFrlLine = frlMemoMapSampleFrlLine;
	}
	public String getPromptPosition1() {
		return promptPosition1;
	}
	public void setPromptPosition1(String promptPosition1) {
		this.promptPosition1 = promptPosition1;
	}
	public String getPromptPosition2() {
		return promptPosition2;
	}
	public void setPromptPosition2(String promptPosition2) {
		this.promptPosition2 = promptPosition2;
	}
	public String getPromptPosition3() {
		return promptPosition3;
	}
	public void setPromptPosition3(String promptPosition3) {
		this.promptPosition3 = promptPosition3;
	}
	public String getPromptPosition4() {
		return promptPosition4;
	}
	public void setPromptPosition4(String promptPosition4) {
		this.promptPosition4 = promptPosition4;
	}
	public String getPromptPosition5() {
		return promptPosition5;
	}
	public void setPromptPosition5(String promptPosition5) {
		this.promptPosition5 = promptPosition5;
	}
	public String getPromptPosition6() {
		return promptPosition6;
	}
	public void setPromptPosition6(String promptPosition6) {
		this.promptPosition6 = promptPosition6;
	}
	public MultipartFile getFile() {
		return file;
	}
	public void setFile(MultipartFile file) {
		this.file = file;
	}
	public RsDashboardVb getDashboards() {
		return dashboards;
	}
	public void setDashboards(RsDashboardVb dashboards) {
		this.dashboards = dashboards;
	}
	public List<ReportsWriterVb> getDrilDownList() {
		return drilDownList;
	}
	public void setDrilDownList(List<ReportsWriterVb> drilDownList) {
		this.drilDownList = drilDownList;
	}
	public List<ReportsWriterVb> getChildren() {
		return children;
	}
	public void setChildren(List<ReportsWriterVb> children) {
		this.children = children;
	}
	public List<ReportStgVb> getReportsData() {
		return reportsData;
	}
	public void setReportsData(List<ReportStgVb> reportsData) {
		this.reportsData = reportsData;
	}
	public List<ColumnHeadersVb> getColumnHeaders() {
		return columnHeaders;
	}
	public void setColumnHeaders(List<ColumnHeadersVb> columnHeaders) {
		this.columnHeaders = columnHeaders;
	}
	public List<RsChartsVb> getChartTypes() {
		return chartTypes;
	}
	public void setChartTypes(List<RsChartsVb> chartTypes) {
		this.chartTypes = chartTypes;
	}
	public String getFrlMemoMapSampleBalType() {
		return frlMemoMapSampleBalType;
	}
	public void setFrlMemoMapSampleBalType(String frlMemoMapSampleBalType) {
		this.frlMemoMapSampleBalType = frlMemoMapSampleBalType;
	}
	public int getPageId() {
		return pageId;
	}
	public void setPageId(int pageId) {
		this.pageId = pageId;
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
	public boolean isNewHeaderMethod() {
		return isNewHeaderMethod;
	}
	public void setNewHeaderMethod(boolean isNewHeaderMethod) {
		this.isNewHeaderMethod = isNewHeaderMethod;
	}
	public String getCsvSeparator() {
		return csvSeparator;
	}
	public void setCsvSeparator(String csvSeparator) {
		this.csvSeparator = csvSeparator;
	}
		
}
