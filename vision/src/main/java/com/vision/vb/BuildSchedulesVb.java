package com.vision.vb;

import java.util.ArrayList;
import java.util.List;

public class BuildSchedulesVb extends CommonVb {

	private static final long serialVersionUID = 1L;
	private String build = "";
	private String scheduledDate = "";
	private String buildNumber = "";
	private int parallelProcsCount = 0;
	private String notify = "";
	private String supportContact = "";
	private String supportContactNumber = "";
	private int submitterId = 0;
	private String startTime = "";
	private String endTime = "";
	private int	buildScheduleStatusAt = 206;
	private String buildScheduleStatus = "";
	private String country = "";
	private String leBook = "";
	private int recurringFrequencyAt = 213;
	private String recurringFrequency = "N";
	private String expandFlag = "";
	private String duration = "";
	private String dbDateTime = "";
	private List<BuildSchedulesDetailsVb> buildSchedulesDetails = new ArrayList<BuildSchedulesDetailsVb>(0);
	private List<BuildControlsVb> buildControls = new ArrayList<BuildControlsVb>(0);
	private FileInfoVb fileInfoVb;
	
	private String node = "";
	private String businessDate = "";
	private String feedDate = "";
	private long cronCount = 1;
	private String cronName = "";
	private String cronStatus = "";
	private String startCronNode = "";
	private String startCronHostName = "";
	private String frequencyProcess = "";
	
	public String getBuild() {
		return build;
	}
	public void setBuild(String build) {
		this.build = build;
	}
	public String getScheduledDate() {
		return scheduledDate;
	}
	public void setScheduledDate(String scheduledDate) {
		this.scheduledDate = scheduledDate;
	}
	public String getBuildNumber() {
		return buildNumber;
	}
	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}
	public int getParallelProcsCount() {
		return parallelProcsCount;
	}
	public void setParallelProcsCount(int parallelProcsCount) {
		this.parallelProcsCount = parallelProcsCount;
	}
	public String getNotify() {
		return notify;
	}
	public void setNotify(String notify) {
		this.notify = notify;
	}
	public String getSupportContact() {
		return supportContact;
	}
	public void setSupportContact(String supportContact) {
		this.supportContact = supportContact;
	}
	public String getSupportContactNumber() {
		return supportContactNumber;
	}
	public void setSupportContactNumber(String supportContactNumber) {
		this.supportContactNumber = supportContactNumber;
	}
	public int getSubmitterId() {
		return submitterId;
	}
	public void setSubmitterId(int submitterId) {
		this.submitterId = submitterId;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public int getBuildScheduleStatusAt() {
		return buildScheduleStatusAt;
	}
	public void setBuildScheduleStatusAt(int buildScheduleStatusAt) {
		this.buildScheduleStatusAt = buildScheduleStatusAt;
	}
	public String getBuildScheduleStatus() {
		return buildScheduleStatus;
	}
	public void setBuildScheduleStatus(String buildScheduleStatus) {
		this.buildScheduleStatus = buildScheduleStatus;
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
	public int getRecurringFrequencyAt() {
		return recurringFrequencyAt;
	}
	public void setRecurringFrequencyAt(int recurringFrequencyAt) {
		this.recurringFrequencyAt = recurringFrequencyAt;
	}
	public String getRecurringFrequency() {
		return recurringFrequency;
	}
	public void setRecurringFrequency(String recurringFrequency) {
		this.recurringFrequency = recurringFrequency;
	}
	public String getExpandFlag() {
		return expandFlag;
	}
	public void setExpandFlag(String expandFlag) {
		this.expandFlag = expandFlag;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}
	public String getCronStatus() {
		return cronStatus;
	}
	public void setCronStatus(String cronStatus) {
		this.cronStatus = cronStatus;
	}
	public List<BuildSchedulesDetailsVb> getBuildSchedulesDetails() {
		return buildSchedulesDetails;
	}
	public void setBuildSchedulesDetails(
			List<BuildSchedulesDetailsVb> buildSchedulesDetails) {
		this.buildSchedulesDetails = buildSchedulesDetails;
	}
	public FileInfoVb getFileInfoVb() {
		return fileInfoVb;
	}
	public void setFileInfoVb(FileInfoVb fileInfoVb) {
		this.fileInfoVb = fileInfoVb;
	}
	public List<BuildControlsVb> getBuildControls() {
		return buildControls;
	}
	public void setBuildControls(List<BuildControlsVb> buildControls) {
		this.buildControls = buildControls;
	}
	public String getDbDateTime() {
		return dbDateTime;
	}
	public void setDbDateTime(String dbDateTime) {
		this.dbDateTime = dbDateTime;
	}
	public long getCronCount() {
		return cronCount;
	}
	public void setCronCount(long cronCount) {
		this.cronCount = cronCount;
	}
	public String getCronName() {
		return cronName;
	}
	public void setCronName(String cronName) {
		this.cronName = cronName;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getBusinessDate() {
		return businessDate;
	}
	public void setBusinessDate(String businessDate) {
		this.businessDate = businessDate;
	}
	public String getFeedDate() {
		return feedDate;
	}
	public void setFeedDate(String feedDate) {
		this.feedDate = feedDate;
	}
	public String getStartCronNode() {
		return startCronNode;
	}
	public void setStartCronNode(String startCronNode) {
		this.startCronNode = startCronNode;
	}
	public String getStartCronHostName() {
		return startCronHostName;
	}
	public void setStartCronHostName(String startCronHostName) {
		this.startCronHostName = startCronHostName;
	}
	public String getFrequencyProcess() {
		return frequencyProcess;
	}
	public void setFrequencyProcess(String frequencyProcess) {
		this.frequencyProcess = frequencyProcess;
	}
}