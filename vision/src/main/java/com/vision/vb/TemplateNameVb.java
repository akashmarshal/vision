package com.vision.vb;

public class TemplateNameVb extends CommonVb{

	private static final long serialVersionUID = 2942783983913844700L;
	
	private String templateName = "";
	private String generalDescription = "";
	private String feedCategory = "";
	private String program = "";
	private String programDesc = "";
	private int defaultAcquProcessTypeAT= 1079;
	private String defaultAcquProcessType= "";
	private int templateStatusNt = 0;
	private int templateStatus = -1;
	private String feedStgName = "";
	private String viewName = "";
	private String processSequence = "";
	private String filePattern="";
	private String excelFilePattern="";
	private String excelTemplateId = "";
	private String effectiveDate="";
	private String currVersionNo="";
	private String mainVersionNo="";
	private String versionType="";
	
	//	private String templateName = "";
	private String columnName = "";
	private String columnId = "";
	private String dataType = ""; 
	private String charUsed = ""; 
	private String dataLength  = "";
	private String dataScaling = ""; 
	private String dataIndex  = "";
	private String guidelines = "";
	private int mandatoryFlag  = -1;
	private String defaultValues  = "";
	private String columnDescription  = "";
	private String dataSample  = "";
//	private String effectiveDate = "";
	private int templateDesignStatusNt = 1;
	private int templateDesignStatus = 0;
	private int mandatoryFlagNt  = 1013;
	private String errorDescription  = "";
	private String refTable = "";
	private String refColumn = "";
	private int numberOfCols = 2;
	/*private int internalStatus  = 0;*/
	
	private String country = "";
	private String leBook = "";
	private String executionSequence = "";
	private int executionTypeAt = 1092;
	private String executionType = "";
	private String mainQuery = "";
	private String dml1 = "";
	private String fatalFlag1 = "";
	private String logTextFlag1 = "";
	private String dmlStatus1 = "";
	private String dml2 = "";
	private String fatalFlag2 = "";
	private String logTextFlag2 = "";
	private String dmlStatus2 = "";
	private String dml3 = "";
	private String fatalFlag3 = "";
	private String logTextFlag3 = "";
	private String dmlStatus3 = "";
	private String dml4 = "";
	private String fatalFlag4 = "";
	private String logTextFlag4 = "";
	private String dmlStatus4 = "";
	private String dml5 = "";
	private String fatalFlag5 = "";
	private String logTextFlag5 = "";
	private String dmlStatus5 = "";
	private String dml6 = "";
	private String fatalFlag6 = "";
	private String logTextFlag6 = "";
	private String dmlStatus6 = "";
	private String dml7 = "";
	private String fatalFlag7 = "";
	private String logTextFlag7 = "";
	private String dmlStatus7 = "";
	private String dml8 = "";
	private String fatalFlag8 = "";
	private String logTextFlag8 = "";
	private String dmlStatus8 = "";
	private String dml9 = "";
	private String fatalFlag9 = "";
	private String logTextFlag9 = "";
	private String dmlStatus9 = "";
	private String dml10 = "";
	private String fatalFlag10 = "";
	private String logTextFlag10 = "";
	private String dmlStatus10 = "";
	private int fatalFlagAt = 1096;
	private int logTextFlagAt = 1098;
	private int dmlStatusNt = 1;
	private int mappingStatusNt = -1;
	private int mappingStatus = 0;
	private int subAdfNumber = -1;
	private String dml = "";
	private String fatalFlag = "";
	private String logTextFlag = "";
	private String dmlStatus = "";
	private String valueTrimFlag = "";
	private int valueTrimFlagAt = 1183;
	private String dataColumn1 = "";
	private String dataColumn2 = "";
	private String dataColumn3 = "";
	private String dataColumn4 = "";
	private String dataColumn5 = "";
	private String dataColumn6 = "";
	private String dataColumn7 = "";
	private String sheetName = "";
	private String previousAction = "";
	
	public String getPreviousAction() {
		return previousAction;
	}
	public void setPreviousAction(String previousAction) {
		this.previousAction = previousAction;
	}
	public String getDataColumn1() {
		return dataColumn1;
	}
	public void setDataColumn1(String dataColumn1) {
		this.dataColumn1 = dataColumn1;
	}
	public String getDataColumn2() {
		return dataColumn2;
	}
	public void setDataColumn2(String dataColumn2) {
		this.dataColumn2 = dataColumn2;
	}
	public String getDataColumn3() {
		return dataColumn3;
	}
	public void setDataColumn3(String dataColumn3) {
		this.dataColumn3 = dataColumn3;
	}
	public String getDataColumn4() {
		return dataColumn4;
	}
	public void setDataColumn4(String dataColumn4) {
		this.dataColumn4 = dataColumn4;
	}
	public String getDataColumn5() {
		return dataColumn5;
	}
	public void setDataColumn5(String dataColumn5) {
		this.dataColumn5 = dataColumn5;
	}
	public String getDataColumn6() {
		return dataColumn6;
	}
	public void setDataColumn6(String dataColumn6) {
		this.dataColumn6 = dataColumn6;
	}
	public String getDataColumn7() {
		return dataColumn7;
	}
	public void setDataColumn7(String dataColumn7) {
		this.dataColumn7 = dataColumn7;
	}
	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	public String getValueTrimFlag() {
		return valueTrimFlag;
	}
	public void setValueTrimFlag(String valueTrimFlag) {
		this.valueTrimFlag = valueTrimFlag;
	}
	public int getValueTrimFlagAt() {
		return valueTrimFlagAt;
	}
	public void setValueTrimFlagAt(int valueTrimFlagAt) {
		this.valueTrimFlagAt = valueTrimFlagAt;
	}
	public String getProcessSequence() {
		return processSequence;
	}
	public void setProcessSequence(String processSequence) {
		this.processSequence = processSequence;
	}
	public String getFilePattern() {
		return filePattern;
	}
	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}
	public String getExcelFilePattern() {
		return excelFilePattern;
	}
	public void setExcelFilePattern(String excelFilePattern) {
		this.excelFilePattern = excelFilePattern;
	}
	public String getExcelTemplateId() {
		return excelTemplateId;
	}
	public void setExcelTemplateId(String excelTemplateId) {
		this.excelTemplateId = excelTemplateId;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public String getGeneralDescription() {
		return generalDescription;
	}
	public void setGeneralDescription(String generalDescription) {
		this.generalDescription = generalDescription;
	}
	public String getFeedCategory() {
		return feedCategory;
	}
	public void setFeedCategory(String feedCategory) {
		this.feedCategory = feedCategory;
	}
	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
	public String getProgramDesc() {
		return programDesc;
	}
	public void setProgramDesc(String programDesc) {
		this.programDesc = programDesc;
	}
	public int getTemplateStatusNt() {
		return templateStatusNt;
	}
	public void setTemplateStatusNt(int templateStatusNt) {
		this.templateStatusNt = templateStatusNt;
	}
	public int getTemplateStatus() {
		return templateStatus;
	}
	public void setTemplateStatus(int templateStatus) {
		this.templateStatus = templateStatus;
	}
	public int getDefaultAcquProcessTypeAT() {
		return defaultAcquProcessTypeAT;
	}
	public void setDefaultAcquProcessTypeAT(int defaultAcquProcessTypeAT) {
		this.defaultAcquProcessTypeAT = defaultAcquProcessTypeAT;
	}
	public String getDefaultAcquProcessType() {
		return defaultAcquProcessType;
	}
	public void setDefaultAcquProcessType(String defaultAcquProcessType) {
		this.defaultAcquProcessType = defaultAcquProcessType;
	}
	public String getFeedStgName() {
		return feedStgName;
	}
	public void setFeedStgName(String feedStgName) {
		this.feedStgName = feedStgName;
	}
	public String getViewName() {
		return viewName;
	}
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	public String getEffectiveDate() {
		return effectiveDate;
	}
	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnId() {
		return columnId;
	}
	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getCharUsed() {
		return charUsed;
	}
	public void setCharUsed(String charUsed) {
		this.charUsed = charUsed;
	}
	public String getDataLength() {
		return dataLength;
	}
	public void setDataLength(String dataLength) {
		this.dataLength = dataLength;
	}
	public String getDataScaling() {
		return dataScaling;
	}
	public void setDataScaling(String dataScaling) {
		this.dataScaling = dataScaling;
	}
	public String getDataIndex() {
		return dataIndex;
	}
	public void setDataIndex(String dataIndex) {
		this.dataIndex = dataIndex;
	}
	public String getGuidelines() {
		return guidelines;
	}
	public void setGuidelines(String guidelines) {
		this.guidelines = guidelines;
	}
	public String getDefaultValues() {
		return defaultValues;
	}
	public void setDefaultValues(String defaultValues) {
		this.defaultValues = defaultValues;
	}
	public String getColumnDescription() {
		return columnDescription;
	}
	public void setColumnDescription(String columnDescription) {
		this.columnDescription = columnDescription;
	}
	public String getDataSample() {
		return dataSample;
	}
	public void setDataSample(String dataSample) {
		this.dataSample = dataSample;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getLeBook() {
		return leBook;
	}
	public void setLeBook(String leBook) {
		this.leBook = leBook;
	}
	public String getExecutionSequence() {
		return executionSequence;
	}
	public void setExecutionSequence(String executionSequence) {
		this.executionSequence = executionSequence;
	}
	public int getExecutionTypeAt() {
		return executionTypeAt;
	}
	public void setExecutionTypeAt(int executionTypeAt) {
		this.executionTypeAt = executionTypeAt;
	}
	public String getExecutionType() {
		return executionType;
	}
	public void setExecutionType(String executionType) {
		this.executionType = executionType;
	}
	public String getMainQuery() {
		return mainQuery;
	}
	public void setMainQuery(String mainQuery) {
		this.mainQuery = mainQuery;
	}
	public String getDml1() {
		return dml1;
	}
	public void setDml1(String dml1) {
		this.dml1 = dml1;
	}
	public String getFatalFlag1() {
		return fatalFlag1;
	}
	public void setFatalFlag1(String fatalFlag1) {
		this.fatalFlag1 = fatalFlag1;
	}
	public String getLogTextFlag1() {
		return logTextFlag1;
	}
	public void setLogTextFlag1(String logTextFlag1) {
		this.logTextFlag1 = logTextFlag1;
	}
	public String getDmlStatus1() {
		return dmlStatus1;
	}
	public void setDmlStatus1(String dmlStatus1) {
		this.dmlStatus1 = dmlStatus1;
	}
	public String getDml2() {
		return dml2;
	}
	public void setDml2(String dml2) {
		this.dml2 = dml2;
	}
	public String getFatalFlag2() {
		return fatalFlag2;
	}
	public void setFatalFlag2(String fatalFlag2) {
		this.fatalFlag2 = fatalFlag2;
	}
	public String getLogTextFlag2() {
		return logTextFlag2;
	}
	public void setLogTextFlag2(String logTextFlag2) {
		this.logTextFlag2 = logTextFlag2;
	}
	public String getDmlStatus2() {
		return dmlStatus2;
	}
	public void setDmlStatus2(String dmlStatus2) {
		this.dmlStatus2 = dmlStatus2;
	}
	public String getDml3() {
		return dml3;
	}
	public void setDml3(String dml3) {
		this.dml3 = dml3;
	}
	public String getFatalFlag3() {
		return fatalFlag3;
	}
	public void setFatalFlag3(String fatalFlag3) {
		this.fatalFlag3 = fatalFlag3;
	}
	public String getLogTextFlag3() {
		return logTextFlag3;
	}
	public void setLogTextFlag3(String logTextFlag3) {
		this.logTextFlag3 = logTextFlag3;
	}
	public String getDmlStatus3() {
		return dmlStatus3;
	}
	public void setDmlStatus3(String dmlStatus3) {
		this.dmlStatus3 = dmlStatus3;
	}
	public String getDml4() {
		return dml4;
	}
	public void setDml4(String dml4) {
		this.dml4 = dml4;
	}
	public String getFatalFlag4() {
		return fatalFlag4;
	}
	public void setFatalFlag4(String fatalFlag4) {
		this.fatalFlag4 = fatalFlag4;
	}
	public String getLogTextFlag4() {
		return logTextFlag4;
	}
	public void setLogTextFlag4(String logTextFlag4) {
		this.logTextFlag4 = logTextFlag4;
	}
	public String getDmlStatus4() {
		return dmlStatus4;
	}
	public void setDmlStatus4(String dmlStatus4) {
		this.dmlStatus4 = dmlStatus4;
	}
	public String getDml5() {
		return dml5;
	}
	public void setDml5(String dml5) {
		this.dml5 = dml5;
	}
	public String getFatalFlag5() {
		return fatalFlag5;
	}
	public void setFatalFlag5(String fatalFlag5) {
		this.fatalFlag5 = fatalFlag5;
	}
	public String getLogTextFlag5() {
		return logTextFlag5;
	}
	public void setLogTextFlag5(String logTextFlag5) {
		this.logTextFlag5 = logTextFlag5;
	}
	public String getDmlStatus5() {
		return dmlStatus5;
	}
	public void setDmlStatus5(String dmlStatus5) {
		this.dmlStatus5 = dmlStatus5;
	}
	public String getDml6() {
		return dml6;
	}
	public void setDml6(String dml6) {
		this.dml6 = dml6;
	}
	public String getFatalFlag6() {
		return fatalFlag6;
	}
	public void setFatalFlag6(String fatalFlag6) {
		this.fatalFlag6 = fatalFlag6;
	}
	public String getLogTextFlag6() {
		return logTextFlag6;
	}
	public void setLogTextFlag6(String logTextFlag6) {
		this.logTextFlag6 = logTextFlag6;
	}
	public String getDmlStatus6() {
		return dmlStatus6;
	}
	public void setDmlStatus6(String dmlStatus6) {
		this.dmlStatus6 = dmlStatus6;
	}
	public String getDml7() {
		return dml7;
	}
	public void setDml7(String dml7) {
		this.dml7 = dml7;
	}
	public String getFatalFlag7() {
		return fatalFlag7;
	}
	public void setFatalFlag7(String fatalFlag7) {
		this.fatalFlag7 = fatalFlag7;
	}
	public String getLogTextFlag7() {
		return logTextFlag7;
	}
	public void setLogTextFlag7(String logTextFlag7) {
		this.logTextFlag7 = logTextFlag7;
	}
	public String getDmlStatus7() {
		return dmlStatus7;
	}
	public void setDmlStatus7(String dmlStatus7) {
		this.dmlStatus7 = dmlStatus7;
	}
	public String getDml8() {
		return dml8;
	}
	public void setDml8(String dml8) {
		this.dml8 = dml8;
	}
	public String getFatalFlag8() {
		return fatalFlag8;
	}
	public void setFatalFlag8(String fatalFlag8) {
		this.fatalFlag8 = fatalFlag8;
	}
	public String getLogTextFlag8() {
		return logTextFlag8;
	}
	public void setLogTextFlag8(String logTextFlag8) {
		this.logTextFlag8 = logTextFlag8;
	}
	public String getDmlStatus8() {
		return dmlStatus8;
	}
	public void setDmlStatus8(String dmlStatus8) {
		this.dmlStatus8 = dmlStatus8;
	}
	public String getDml9() {
		return dml9;
	}
	public void setDml9(String dml9) {
		this.dml9 = dml9;
	}
	public String getFatalFlag9() {
		return fatalFlag9;
	}
	public void setFatalFlag9(String fatalFlag9) {
		this.fatalFlag9 = fatalFlag9;
	}
	public String getLogTextFlag9() {
		return logTextFlag9;
	}
	public void setLogTextFlag9(String logTextFlag9) {
		this.logTextFlag9 = logTextFlag9;
	}
	public String getDmlStatus9() {
		return dmlStatus9;
	}
	public void setDmlStatus9(String dmlStatus9) {
		this.dmlStatus9 = dmlStatus9;
	}
	public String getDml10() {
		return dml10;
	}
	public void setDml10(String dml10) {
		this.dml10 = dml10;
	}
	public String getFatalFlag10() {
		return fatalFlag10;
	}
	public void setFatalFlag10(String fatalFlag10) {
		this.fatalFlag10 = fatalFlag10;
	}
	public String getLogTextFlag10() {
		return logTextFlag10;
	}
	public void setLogTextFlag10(String logTextFlag10) {
		this.logTextFlag10 = logTextFlag10;
	}
	public String getDmlStatus10() {
		return dmlStatus10;
	}
	public void setDmlStatus10(String dmlStatus10) {
		this.dmlStatus10 = dmlStatus10;
	}
	public int getFatalFlagAt() {
		return fatalFlagAt;
	}
	public void setFatalFlagAt(int fatalFlagAt) {
		this.fatalFlagAt = fatalFlagAt;
	}
	public int getLogTextFlagAt() {
		return logTextFlagAt;
	}
	public void setLogTextFlagAt(int logTextFlagAt) {
		this.logTextFlagAt = logTextFlagAt;
	}
	public int getDmlStatusNt() {
		return dmlStatusNt;
	}
	public void setDmlStatusNt(int dmlStatusNt) {
		this.dmlStatusNt = dmlStatusNt;
	}
	public int getMappingStatusNt() {
		return mappingStatusNt;
	}
	public void setMappingStatusNt(int mappingStatusNt) {
		this.mappingStatusNt = mappingStatusNt;
	}
	public int getMappingStatus() {
		return mappingStatus;
	}
	public void setMappingStatus(int mappingStatus) {
		this.mappingStatus = mappingStatus;
	}
	public int getSubAdfNumber() {
		return subAdfNumber;
	}
	public void setSubAdfNumber(int subAdfNumber) {
		this.subAdfNumber = subAdfNumber;
	}
	public String getDml() {
		return dml;
	}
	public void setDml(String dml) {
		this.dml = dml;
	}
	public String getFatalFlag() {
		return fatalFlag;
	}
	public void setFatalFlag(String fatalFlag) {
		this.fatalFlag = fatalFlag;
	}
	public String getLogTextFlag() {
		return logTextFlag;
	}
	public void setLogTextFlag(String logTextFlag) {
		this.logTextFlag = logTextFlag;
	}
	public String getDmlStatus() {
		return dmlStatus;
	}
	public void setDmlStatus(String dmlStatus) {
		this.dmlStatus = dmlStatus;
	}
	public int getTemplateDesignStatusNt() {
		return templateDesignStatusNt;
	}
	public void setTemplateDesignStatusNt(int templateDesignStatusNt) {
		this.templateDesignStatusNt = templateDesignStatusNt;
	}
	public int getTemplateDesignStatus() {
		return templateDesignStatus;
	}
	public void setTemplateDesignStatus(int templateDesignStatus) {
		this.templateDesignStatus = templateDesignStatus;
	}
	public int getMandatoryFlagNt() {
		return mandatoryFlagNt;
	}
	public void setMandatoryFlagNt(int mandatoryFlagNt) {
		this.mandatoryFlagNt = mandatoryFlagNt;
	}
	public void setMandatoryFlag(int mandatoryFlag) {
		this.mandatoryFlag = mandatoryFlag;
	}
	public int getMandatoryFlag() {
		return mandatoryFlag;
	}
	public String getErrorDescription() {
		return errorDescription;
	}
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	/*public int getInternalStatus() {
		return internalStatus;
	}
	public void setInternalStatus(int internalStatus) {
		this.internalStatus = internalStatus;
	}*/
	public String getRefTable() {
		return refTable;
	}
	public void setRefTable(String refTable) {
		this.refTable = refTable;
	}
	public String getRefColumn() {
		return refColumn;
	}
	public void setRefColumn(String refColumn) {
		this.refColumn = refColumn;
	}
	public int getNumberOfCols() {
		return numberOfCols;
	}
	public void setNumberOfCols(int numberOfCols) {
		this.numberOfCols = numberOfCols;
	}	
	public boolean compare(TemplateNameVb pObject){
		if(!this.templateName.equals(pObject.templateName)
				|| ! this.columnName.trim().equals(pObject.columnName)
				|| ! this.columnDescription.trim().equals(pObject.columnDescription)
				|| ! this.columnId.trim().equals(pObject.columnId)
				|| ! this.dataType.trim().equals(pObject.dataType)
				|| ! this.charUsed.equals(pObject.charUsed)
				|| ! this.dataLength.equals(pObject.dataLength)
				|| ! this.dataScaling.equals(pObject.dataScaling)
				|| ! this.dataIndex.equals(pObject.dataIndex)
				|| ! this.guidelines.equals(pObject.guidelines)
				|| this.mandatoryFlag!=pObject.mandatoryFlag
				|| this.templateStatus!=pObject.templateStatus
				|| ! this.defaultValues.equals(pObject.defaultValues)
				|| ! this.dataSample.equals(pObject.dataSample)
				|| ! this.effectiveDate.equals(pObject.effectiveDate)){
			return false;
		}
		return true;
	}
	public String getCurrVersionNo() {
		return currVersionNo;
	}
	public void setCurrVersionNo(String currVersionNo) {
		this.currVersionNo = currVersionNo;
	}
	public String getMainVersionNo() {
		return mainVersionNo;
	}
	public void setMainVersionNo(String mainVersionNo) {
		this.mainVersionNo = mainVersionNo;
	}
	public String getVersionType() {
		return versionType;
	}
	public void setVersionType(String versionType) {
		this.versionType = versionType;
	}
}