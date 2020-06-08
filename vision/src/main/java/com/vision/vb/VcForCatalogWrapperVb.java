package com.vision.vb;

import java.util.List;

public class VcForCatalogWrapperVb {

	private static final long serialVersionUID = 1L;

	VcConfigMainVb mainModel = null;
	private List<VcForCatalogTableVb> addModifyMetadata  = null;
	private List<VcForCatalogTableVb> deleteTableMetadata = null;
	private List<VcForCatalogTableVb> deleteColumnMetadata = null;
	private List<VcForCatalogTableRelationVb> relationaddModifyMetadata = null;
	private List<VcForCatalogTableRelationVb> deleteTableRelationMetadata = null;
	
	public VcConfigMainVb getMainModel() {
		return mainModel;
	}

	public void setMainModel(VcConfigMainVb mainModel) {
		this.mainModel = mainModel;
	}


	public List<VcForCatalogTableVb> getAddModifyMetadata() {
		return addModifyMetadata;
	}

	public void setAddModifyMetadata(List<VcForCatalogTableVb> addModifyMetadata) {
		this.addModifyMetadata = addModifyMetadata;
	}

	public List<VcForCatalogTableVb> getDeleteTableMetadata() {
		return deleteTableMetadata;
	}

	public void setDeleteTableMetadata(List<VcForCatalogTableVb> deleteTableMetadata) {
		this.deleteTableMetadata = deleteTableMetadata;
	}

	public List<VcForCatalogTableVb> getDeleteColumnMetadata() {
		return deleteColumnMetadata;
	}

	public void setDeleteColumnMetadata(List<VcForCatalogTableVb> deleteColumnMetadata) {
		this.deleteColumnMetadata = deleteColumnMetadata;
	}

	
	public List<VcForCatalogTableRelationVb> getRelationaddModifyMetadata() {
		return relationaddModifyMetadata;
	}

	public void setRelationaddModifyMetadata(List<VcForCatalogTableRelationVb> relationaddModifyMetadata) {
		this.relationaddModifyMetadata = relationaddModifyMetadata;
	}

	public List<VcForCatalogTableRelationVb> getDeleteTableRelationMetadata() {
		return deleteTableRelationMetadata;
	}

	public void setDeleteTableRelationMetadata(List<VcForCatalogTableRelationVb> deleteTableRelationMetadata) {
		this.deleteTableRelationMetadata = deleteTableRelationMetadata;
	}

}