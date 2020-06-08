package com.vision.vb;

import java.util.List;

public class WidgetWrapperVb {
	private WidgetDesignVb mainModel;
	private List<VcForQueryReportFieldsVb> reportFields = null;
	private String[] hashArray = null;
	private String[] hashValueArray = null;
	private String dataViewName = null;

	public WidgetDesignVb getMainModel() {
		return mainModel;
	}

	public void setMainModel(WidgetDesignVb mainModel) {
		this.mainModel = mainModel;
	}

	public List<VcForQueryReportFieldsVb> getReportFields() {
		return reportFields;
	}

	public void setReportFields(List<VcForQueryReportFieldsVb> reportFields) {
		this.reportFields = reportFields;
	}

	public String[] getHashArray() {
		return hashArray;
	}

	public void setHashArray(String[] hashArray) {
		this.hashArray = hashArray;
	}

	public String[] getHashValueArray() {
		return hashValueArray;
	}

	public void setHashValueArray(String[] hashValueArray) {
		this.hashValueArray = hashValueArray;
	}

	public String getDataViewName() {
		return dataViewName;
	}

	public void setDataViewName(String dataViewName) {
		this.dataViewName = dataViewName;
	}

}
