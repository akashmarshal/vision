package com.vision.vb;

import java.util.List;

public class WidgetLODWrapperVb {

	
	WidgetDesignVb mainModel = null;
	List<LevelOfDisplayVb> lodProfileList = null;
	List<LevelOfDisplayUserVb> lodUserList = null;
	
	public WidgetDesignVb getMainModel() {
		return mainModel;
	}
	public void setMainModel(WidgetDesignVb mainModel) {
		this.mainModel = mainModel;
	}
	public List<LevelOfDisplayVb> getLodProfileList() {
		return lodProfileList;
	}
	public void setLodProfileList(List<LevelOfDisplayVb> lodProfileList) {
		this.lodProfileList = lodProfileList;
	}
	public List<LevelOfDisplayUserVb> getLodUserList() {
		return lodUserList;
	}
	public void setLodUserList(List<LevelOfDisplayUserVb> lodUserList) {
		this.lodUserList = lodUserList;
	}


	

}
