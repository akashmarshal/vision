package com.vision.vb;

import java.util.List;

public class DSLODWrapperVb {
	DSConnectorVb mainModel = null;
	List<LevelOfDisplayVb> lodList = null;

	public DSConnectorVb getMainModel() {
		return mainModel;
	}

	public void setMainModel(DSConnectorVb mainModel) {
		this.mainModel = mainModel;
	}

	public List<LevelOfDisplayVb> getLodList() {
		return lodList;
	}

	public void setLodList(List<LevelOfDisplayVb> lodList) {
		this.lodList = lodList;
	}
}
