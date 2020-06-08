package com.vision.vb;

import java.util.ArrayList;
import java.util.List;

public class VcReportGenerationArgumentHolderVb {
	ArrayList<String> hashVarAL ;
	ArrayList<String> hashVarValueAL ;
	String sessionId = "";
	ArrayList<PromptIdsVb> promptAL ;
	String saclingFactor = "";
	Object reportPageDesign;
	List<String> gridBaseColLvlAL;
	
	public ArrayList<String> getHashVarAL() {
		return hashVarAL;
	}
	public void setHashVarAL(ArrayList<String> hashVarAL) {
		this.hashVarAL = hashVarAL;
	}
	public ArrayList<String> getHashVarValueAL() {
		return hashVarValueAL;
	}
	public void setHashVarValueAL(ArrayList<String> hashVarValueAL) {
		this.hashVarValueAL = hashVarValueAL;
	}	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public ArrayList<PromptIdsVb> getPromptAL() {
		return promptAL;
	}
	public void setPromptAL(ArrayList<PromptIdsVb> promptAL) {
		this.promptAL = promptAL;
	}
	public String getSaclingFactor() {
		return saclingFactor;
	}
	public void setSaclingFactor(String saclingFactor) {
		this.saclingFactor = saclingFactor;
	}
	public Object getReportPageDesign() {
		return reportPageDesign;
	}
	public void setReportPageDesign(Object reportPageDesign) {
		this.reportPageDesign = reportPageDesign;
	}
	public List<String> getGridBaseColLvlAL() {
		return gridBaseColLvlAL;
	}
	public void setGridBaseColLvlAL(List<String> gridBaseColLvlAL) {
		this.gridBaseColLvlAL = gridBaseColLvlAL;
	}
}
