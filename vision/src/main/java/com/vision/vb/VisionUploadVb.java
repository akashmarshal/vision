package com.vision.vb;

public class VisionUploadVb extends CommonVb {

	private static final long serialVersionUID = 1L;
	private String tableName = "";
	private String fileName = "";
	private String uploadSequence = "";
	private String uploadDate = "";
	private int uploadStatusNt = 0;
	private int uploadStatus = -1;
	private int uploadSequenceOld = -1;
	private Boolean checkUploadInterval = false; 
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUploadSequence() {
		return uploadSequence;
	}
	public void setUploadSequence(String uploadSequence) {
		this.uploadSequence = uploadSequence;
	}
	public String getUploadDate() {
		return uploadDate;
	}
	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}
	public int getUploadStatusNt() {
		return uploadStatusNt;
	}
	public void setUploadStatusNt(int uploadStatusNt) {
		this.uploadStatusNt = uploadStatusNt;
	}
	public int getUploadStatus() {
		return uploadStatus;
	}
	public void setUploadStatus(int uploadStatus) {
		this.uploadStatus = uploadStatus;
	}
	public int getUploadSequenceOld() {
		return uploadSequenceOld;
	}
	public void setUploadSequenceOld(int uploadSequenceOld) {
		this.uploadSequenceOld = uploadSequenceOld;
	}
	public Boolean getCheckUploadInterval() {
		return checkUploadInterval;
	}
	public void setCheckUploadInterval(Boolean checkUploadInterval) {
		this.checkUploadInterval = checkUploadInterval;
	}

}
