package com.vision.vb;

import java.util.ArrayList;
import java.util.List;

public class VcForCatalogTableVb extends CommonVb{
	
	private static final long serialVersionUID = 1L;
	private String catalogId;
	private String tableId;
	private String tableName;
	private String tblAliasName;
	private String baseTableJoinFlag = "";
	private String baseTableFlag;
	private String tableSourceType = "";
	private String queryId = "";
	private int databaseTypeAt = 1082;
	private String databaseType = "";
	private String databaseConnectivityDetails = "";
	private String sortTree = "";
	private int vctStatusNt = 2000;
	private int vctStatus = 0;
	private String accessControlScript = "";

    private List<VcForCatalogTableColumnsVb> vcForCatalogTableColumnsVb;

	public List<VcForCatalogTableColumnsVb> getVcForCatalogTableColumnsVb() {
		return vcForCatalogTableColumnsVb;
	}
	public void setVcForCatalogTableColumnsVb(List<VcForCatalogTableColumnsVb> vcForCatalogTableColumnsVb) {
		this.vcForCatalogTableColumnsVb = vcForCatalogTableColumnsVb;
	}
	public String getBaseTableJoinFlag() {
		return baseTableJoinFlag;
	}
	public void setBaseTableJoinFlag(String baseTableJoinFlag) {
		this.baseTableJoinFlag = baseTableJoinFlag;
	}
	public String getQueryId() {
		return queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	public int getDatabaseTypeAt() {
		return databaseTypeAt;
	}
	public void setDatabaseTypeAt(int databaseTypeAt) {
		this.databaseTypeAt = databaseTypeAt;
	}
	public String getDatabaseType() {
		return databaseType;
	}
	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}
	public String getDatabaseConnectivityDetails() {
		return databaseConnectivityDetails;
	}
	public void setDatabaseConnectivityDetails(String databaseConnectivityDetails) {
		this.databaseConnectivityDetails = databaseConnectivityDetails;
	}
	public String getSortTree() {
		return sortTree;
	}
	public void setSortTree(String sortTree) {
		this.sortTree = sortTree;
	}
	public int getVctStatusNt() {
		return vctStatusNt;
	}
	public void setVctStatusNt(int vctStatusNt) {
		this.vctStatusNt = vctStatusNt;
	}
	public int getVctStatus() {
		return vctStatus;
	}
	public void setVctStatus(int vctStatus) {
		this.vctStatus = vctStatus;
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
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getTblAliasName() {
		return tblAliasName;
	}
	public void setTblAliasName(String tblAliasName) {
		this.tblAliasName = tblAliasName;
	}
	public String getBaseTableFlag() {
		return baseTableFlag;
	}
	public void setBaseTableFlag(String baseTableFlag) {
		this.baseTableFlag = baseTableFlag;
	}
	public String getTableSourceType() {
		return tableSourceType;
	}
	public void setTableSourceType(String tableSourceType) {
		this.tableSourceType = tableSourceType;
	}
	public String getAccessControlScript() {
		return accessControlScript;
	}
	public void setAccessControlScript(String accessControlScript) {
		this.accessControlScript = accessControlScript;
	}
	
}