package com.vision.vb;

import java.util.List;

public class WidgetVb extends CommonVb{

	private String	product = "";
	private int	openedToday =  0;
	private int	closedToday =  0;
	private int	openedMTD =  0;
	private int	closedMTD =  0;
	private String promptId1 ; 

	private String dataSource = "";
	private String visionSBU = "";
	private int	balance = 0;
	private String procedure = "";
	private String promptString1 = "";
	private String promptString2 = "";
	private String reportTitle = "";

	private String category = "";
	private String dividend1;
	private String dividend2;
	private String dividend3;
	private String divisor1;
	private String divisor2;
	private String divisor3;
	private String businessDate = "";//VISION_BUSINESS_DATE
	private String status = "";
	private String errorMessage = "";
	
	private String reportId= "";//Report_ID - Key Field
	private int	visionId =  0;//VISION_ID - Key Field
	private int	userPrefReportStatusNt = 7;//USER_PREF_STATUS_NT
	private int	userPrefReportStatus = 0;//USER_PREF_STATUS
	private int	sequenceNo =  0;//SEQUENCE_NO - Key Field
	private int recordIndicatr = 0;
	private String sessionId = "";
	private String filterString = "";
	private String tableName = "";
	private String outputTab = "";

	public String getReportTitle() {
		return reportTitle;
	}
	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}
	public String getPromptId1() {
		return promptId1;
	}
	public void setPromptId1(String promptId1) {
		this.promptId1 = promptId1;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getFilterString() {
		return filterString;
	}
	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public int getOpenedToday() {
		return openedToday;
	}
	public void setOpenedToday(int openedToday) {
		this.openedToday = openedToday;
	}
	public int getClosedToday() {
		return closedToday;
	}
	public void setClosedToday(int closedToday) {
		this.closedToday = closedToday;
	}
	public int getOpenedMTD() {
		return openedMTD;
	}
	public void setOpenedMTD(int openedMTD) {
		this.openedMTD = openedMTD;
	}
	public int getClosedMTD() {
		return closedMTD;
	}
	public void setClosedMTD(int closedMTD) {
		this.closedMTD = closedMTD;
	}
	public String getDataSource() {
		return dataSource;
	}
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	public String getVisionSBU() {
		return visionSBU;
	}
	public void setVisionSBU(String visionSBU) {
		this.visionSBU = visionSBU;
	}
	public int getBalance() {
		return balance;
	}
	public void setBalance(int balance) {
		this.balance = balance;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDividend1() {
		return dividend1;
	}
	public void setDividend1(String dividend1) {
		this.dividend1 = dividend1;
	}
	public String getDividend2() {
		return dividend2;
	}
	public void setDividend2(String dividend2) {
		this.dividend2 = dividend2;
	}
	public String getDividend3() {
		return dividend3;
	}
	public void setDividend3(String dividend3) {
		this.dividend3 = dividend3;
	}
	public String getDivisor1() {
		return divisor1;
	}
	public void setDivisor1(String divisor1) {
		this.divisor1 = divisor1;
	}
	public String getDivisor2() {
		return divisor2;
	}
	public void setDivisor2(String divisor2) {
		this.divisor2 = divisor2;
	}
	public String getDivisor3() {
		return divisor3;
	}
	public void setDivisor3(String divisor3) {
		this.divisor3 = divisor3;
	}
	public String getBusinessDate() {
		return businessDate;
	}
	public void setBusinessDate(String businessDate) {
		this.businessDate = businessDate;
	}
	public int getVisionId() {
		return visionId;
	}
	public void setVisionId(int visionId) {
		this.visionId = visionId;
	}
	public int getUserPrefReportStatusNt() {
		return userPrefReportStatusNt;
	}
	public void setUserPrefReportStatusNt(int userPrefReportStatusNt) {
		this.userPrefReportStatusNt = userPrefReportStatusNt;
	}
	public int getUserPrefReportStatus() {
		return userPrefReportStatus;
	}
	public void setUserPrefReportStatus(int userPrefReportStatus) {
		this.userPrefReportStatus = userPrefReportStatus;
	}
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	public int getSequenceNo() {
		return sequenceNo;
	}
	public void setSequenceNo(int sequenceNo) {
		this.sequenceNo = sequenceNo;
	}
	public int getRecordIndicatr() {
		return recordIndicatr;
	}
	public void setRecordIndicatr(int recordIndicatr) {
		this.recordIndicatr = recordIndicatr;
	}
	public String getProcedure() {
		return procedure;
	}
	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}
	public String getPromptString1() {
		return promptString1;
	}
	public void setPromptString1(String promptString1) {
		this.promptString1 = promptString1;
	}
	public String getPromptString2() {
		return promptString2;
	}
	public void setPromptString2(String promptString2) {
		this.promptString2 = promptString2;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getOutputTab() {
		return outputTab;
	}
	public void setOutputTab(String outputTab) {
		this.outputTab = outputTab;
	}
}