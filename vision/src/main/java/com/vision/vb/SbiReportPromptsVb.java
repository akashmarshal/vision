package com.vision.vb;

import java.util.List;

public class SbiReportPromptsVb extends CommonVb {

	private String reportId = "";
	private String promptXmlContent = "";
	private int vrdPromptStatusNt = 1;
	private int vrdPromptStatus = 0;

	private boolean scalingFactorFlag = false;
	private boolean autoSubmitFlag = false;

	private List<SbiReportPromptsVb> children = null;

	private String promptKey = ""; // pattern to be replaced
	private String hashVariable = "";
	private String hashValue = "";
	private String promptLabel = ""; // Display label for prompt key
	private int promptSort = 0;
	private String promptCategory = "";
	private String promptId = "";
	private String promptType = "";
	private String promptUseValueColumn = "";
	private String promptDisplayValueColumn = "";
	private String promptStartIdx = ""; //Range prompt Start Index
	private String promptEndIdx = ""; //Range prompt End Index
	private Object promptResponse = null;

	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
	}

	public String getPromptXmlContent() {
		return promptXmlContent;
	}

	public void setPromptXmlContent(String promptXmlContent) {
		this.promptXmlContent = promptXmlContent;
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

	public boolean isScalingFactorFlag() {
		return scalingFactorFlag;
	}

	public void setScalingFactorFlag(boolean scalingFactorFlag) {
		this.scalingFactorFlag = scalingFactorFlag;
	}

	public boolean isAutoSubmitFlag() {
		return autoSubmitFlag;
	}

	public void setAutoSubmitFlag(boolean autoSubmitFlag) {
		this.autoSubmitFlag = autoSubmitFlag;
	}

	public List<SbiReportPromptsVb> getChildren() {
		return children;
	}

	public void setChildren(List<SbiReportPromptsVb> children) {
		this.children = children;
	}

	public String getPromptKey() {
		return promptKey;
	}

	public void setPromptKey(String promptKey) {
		this.promptKey = promptKey;
	}

	public String getHashVariable() {
		return hashVariable;
	}

	public void setHashVariable(String hashVariable) {
		this.hashVariable = hashVariable;
	}

	public String getHashValue() {
		return hashValue;
	}

	public void setHashValue(String hashValue) {
		this.hashValue = hashValue;
	}

	public String getPromptLabel() {
		return promptLabel;
	}

	public void setPromptLabel(String promptLabel) {
		this.promptLabel = promptLabel;
	}

	public int getPromptSort() {
		return promptSort;
	}

	public void setPromptSort(int promptSort) {
		this.promptSort = promptSort;
	}

	public String getPromptCategory() {
		return promptCategory;
	}

	public void setPromptCategory(String promptCategory) {
		this.promptCategory = promptCategory;
	}

	public String getPromptId() {
		return promptId;
	}

	public void setPromptId(String promptId) {
		this.promptId = promptId;
	}

	public String getPromptType() {
		return promptType;
	}

	public void setPromptType(String promptType) {
		this.promptType = promptType;
	}

	public String getPromptUseValueColumn() {
		return promptUseValueColumn;
	}

	public void setPromptUseValueColumn(String promptUseValueColumn) {
		this.promptUseValueColumn = promptUseValueColumn;
	}

	public String getPromptDisplayValueColumn() {
		return promptDisplayValueColumn;
	}

	public void setPromptDisplayValueColumn(String promptDisplayValueColumn) {
		this.promptDisplayValueColumn = promptDisplayValueColumn;
	}

	public String getPromptStartIdx() {
		return promptStartIdx;
	}

	public void setPromptStartIdx(String promptStartIdx) {
		this.promptStartIdx = promptStartIdx;
	}

	public String getPromptEndIdx() {
		return promptEndIdx;
	}

	public void setPromptEndIdx(String promptEndIdx) {
		this.promptEndIdx = promptEndIdx;
	}

	public Object getPromptResponse() {
		return promptResponse;
	}

	public void setPromptResponse(Object promptResponse) {
		this.promptResponse = promptResponse;
	}

}