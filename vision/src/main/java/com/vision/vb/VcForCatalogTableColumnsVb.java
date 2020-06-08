package com.vision.vb;

import java.util.List;

public class VcForCatalogTableColumnsVb  extends CommonVb {
	
	private static final long serialVersionUID = 1L;
	private String catalogId = "";
	private String tableId = "";
	private String tblAliasName="";
	private String tableName="";
	private String tableSourceType = "";
	private String queryId = "";
	private String columnName = "";
	private String colId = "";
	private int colTypeAt = 2001;
	private String colType = "";
	private String colAliasName = "";
	private String sortColumn = "";
	private int colDisplayTypeAt = 2000;
	private String colDisplayType = "-1";
	private int colAttributeTypeAt = 2002;
	private String colAttributeType = "-1";
	private int colExperssionTypeAt = 2003;
	private String colExperssionType = "-1";
	private int formatTypeNt = 40;
	private String formatType = "";
	private int magTypeNt = 2002;
	private String magType = "";
	private String magEnableFlag = "";
	private int magSelectionTypeAt = 2007;
	private String magSelectionType = "-1";
	private String experssionText = "";
	private String magDefault = "";
	private String magQueryId = "";
	private String magDisplayColumn = "";
	private String magUseColumn = "";
	private String folderIds = "";
	private int vccStatusNt = 2000;
	private int vccStatus = 0;
	private String joinCondition;
	
	
	private List<VcForCatalogTableColumnsVb> children = null;
	
	public List<VcForCatalogTableColumnsVb> getChildren() {
		return children;
	}
	public void setChildren(List<VcForCatalogTableColumnsVb> children) {
		this.children = children;
	}
	
	public String getCatalogId() {
		return catalogId;
	}
	public void setCatalogId(String catalogId) {
		this.catalogId = catalogId;
	}
	public String getTableId() {
		return tableId;
	}
	public void setTableId(String tableId) {
		this.tableId = tableId;
	}
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColId() {
		return colId;
	}
	public void setColId(String colId) {
		this.colId = colId;
	}
	public String getColType() {
		return colType;
	}
	public void setColType(String colType) {
		this.colType = colType;
	}
	public String getColAliasName() {
		return colAliasName;
	}
	public void setColAliasName(String colAliasName) {
		this.colAliasName = colAliasName;
	}
	public String getSortColumn() {
		return sortColumn;
	}
	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}
	public int getColDisplayTypeAt() {
		return colDisplayTypeAt;
	}
	public void setColDisplayTypeAt(int colDisplayTypeAt) {
		this.colDisplayTypeAt = colDisplayTypeAt;
	}
	public String getColDisplayType() {
		return colDisplayType;
	}
	public void setColDisplayType(String colDisplayType) {
		this.colDisplayType = colDisplayType;
	}
	public int getColAttributeTypeAt() {
		return colAttributeTypeAt;
	}
	public void setColAttributeTypeAt(int colAttributeTypeAt) {
		this.colAttributeTypeAt = colAttributeTypeAt;
	}
	public String getColAttributeType() {
		return colAttributeType;
	}
	public void setColAttributeType(String colAttributeType) {
		this.colAttributeType = colAttributeType;
	}
	public int getColExperssionTypeAt() {
		return colExperssionTypeAt;
	}
	public void setColExperssionTypeAt(int colExperssionTypeAt) {
		this.colExperssionTypeAt = colExperssionTypeAt;
	}
	public String getColExperssionType() {
		return colExperssionType;
	}
	public void setColExperssionType(String colExperssionType) {
		this.colExperssionType = colExperssionType;
	}
	public int getFormatTypeNt() {
		return formatTypeNt;
	}
	public void setFormatTypeNt(int formatTypeNt) {
		this.formatTypeNt = formatTypeNt;
	}
	public String getFormatType() {
		return formatType;
	}
	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}
	public int getMagTypeNt() {
		return magTypeNt;
	}
	public void setMagTypeNt(int magTypeNt) {
		this.magTypeNt = magTypeNt;
	}
	public String getMagType() {
		return magType;
	}
	public void setMagType(String magType) {
		this.magType = magType;
	}
	public String getMagEnableFlag() {
		return magEnableFlag;
	}
	public void setMagEnableFlag(String magEnableFlag) {
		this.magEnableFlag = magEnableFlag;
	}
	public int getMagSelectionTypeAt() {
		return magSelectionTypeAt;
	}
	public void setMagSelectionTypeAt(int magSelectionTypeAt) {
		this.magSelectionTypeAt = magSelectionTypeAt;
	}
	public String getMagSelectionType() {
		return magSelectionType;
	}
	public void setMagSelectionType(String magSelectionType) {
		this.magSelectionType = magSelectionType;
	}
	public String getExperssionText() {
		return experssionText;
	}
	public void setExperssionText(String experssionText) {
		this.experssionText = experssionText;
	}
	public String getMagDefault() {
		return magDefault;
	}
	public void setMagDefault(String magDefault) {
		this.magDefault = magDefault;
	}
	public String getMagQueryId() {
		return magQueryId;
	}
	public void setMagQueryId(String magQueryId) {
		this.magQueryId = magQueryId;
	}
	public String getMagDisplayColumn() {
		return magDisplayColumn;
	}
	public void setMagDisplayColumn(String magDisplayColumn) {
		this.magDisplayColumn = magDisplayColumn;
	}
	public String getMagUseColumn() {
		return magUseColumn;
	}
	public void setMagUseColumn(String magUseColumn) {
		this.magUseColumn = magUseColumn;
	}
	public String getFolderIds() {
		return folderIds;
	}
	public void setFolderIds(String folderIds) {
		this.folderIds = folderIds;
	}
	public int getVccStatusNt() {
		return vccStatusNt;
	}
	public void setVccStatusNt(int vccStatusNt) {
		this.vccStatusNt = vccStatusNt;
	}
	public int getVccStatus() {
		return vccStatus;
	}
	public void setVccStatus(int vccStatus) {
		this.vccStatus = vccStatus;
	}
	public int getColTypeAt() {
		return colTypeAt;
	}
	public void setColTypeAt(int colTypeAt) {
		this.colTypeAt = colTypeAt;
	}
	public String getTblAliasName() {
		return tblAliasName;
	}
	public void setTblAliasName(String tblAliasName) {
		this.tblAliasName = tblAliasName;
	}
	public String getJoinCondition() {
		return joinCondition;
	}
	public void setJoinCondition(String joinCondition) {
		this.joinCondition = joinCondition;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getTableSourceType() {
		return tableSourceType;
	}
	public void setTableSourceType(String tableSourceType) {
		this.tableSourceType = tableSourceType;
	}
	public String getQueryId() {
		return queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

}