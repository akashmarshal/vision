package com.vision.vb;

import java.util.ArrayList;
import java.util.List;

public class BuildControlsVb extends CommonVb{

	private static final long serialVersionUID = -7135158174947618107L;
	private String country = "";
	private String leBook = "";
	private String build = "";
	private String subBuildNumber = "";
	private String bcSequence =  "";
	private String buildModule = "";
	private int	runItAt =  0;
	private String runIt = "";
	private String lastStartDate = "";
	private String lastBuildDate = "";
	private int	buildControlsStatusAt  =  0;
	private String buildControlsStatus = "";
	private String submitterId = "";
	private String buildNumber = "";
	private String expandFlag = "";
	private String programDescription;
	private int buildLevel;
	private int programTypeAt =  201;
	private String programType = "";
	private String debug = "";
	private String fullBuild = "";
	private List<BuildControlsVb> children = new ArrayList<BuildControlsVb>(0);
	private BuildControlsVb parent = null;
	private String fullBuildAllowed = "N";
	
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
	public String getBuild() {
		return build;
	}
	public void setBuild(String build) {
		this.build = build;
	}
	public String getSubBuildNumber() {
		return subBuildNumber;
	}
	public void setSubBuildNumber(String subBuildNumber) {
		this.subBuildNumber = subBuildNumber;
	}
	public String getBcSequence() {
		return bcSequence;
	}
	public void setBcSequence(String bcSequence) {
		this.bcSequence = bcSequence;
	}
	public String getBuildModule() {
		return buildModule;
	}
	public void setBuildModule(String buildModule) {
		this.buildModule = buildModule;
	}
	public int getRunItAt() {
		return runItAt;
	}
	public void setRunItAt(int runItAt) {
		this.runItAt = runItAt;
	}
	public String getRunIt() {
		return runIt;
	}
	public void setRunIt(String runIt) {
		this.runIt = runIt;
	}
	public String getLastStartDate() {
		return lastStartDate;
	}
	public void setLastStartDate(String lastStartDate) {
		this.lastStartDate = lastStartDate;
	}
	public String getLastBuildDate() {
		return lastBuildDate;
	}
	public void setLastBuildDate(String lastBuildDate) {
		this.lastBuildDate = lastBuildDate;
	}
	public int getBuildControlsStatusAt() {
		return buildControlsStatusAt;
	}
	public void setBuildControlsStatusAt(int buildControlsStatusAt) {
		this.buildControlsStatusAt = buildControlsStatusAt;
	}
	public String getBuildControlsStatus() {
		return buildControlsStatus;
	}
	public void setBuildControlsStatus(String ontrolbuildControlsStatussStatus) {
		this.buildControlsStatus = ontrolbuildControlsStatussStatus;
	}
	public String getSubmitterId() {
		return submitterId;
	}
	public void setSubmitterId(String submitterId) {
		this.submitterId = submitterId;
	}
	public String getBuildNumber() {
		return buildNumber;
	}
	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}
	public String getExpandFlag() {
		return expandFlag;
	}
	public void setExpandFlag(String expandFlag) {
		this.expandFlag = expandFlag;
	}
	public String getProgramDescription() {
		return programDescription;
	}
	public void setProgramDescription(String programDescription) {
		this.programDescription = programDescription;
	}
	public int getBuildLevel() {
		return buildLevel;
	}
	public void setBuildLevel(int buildLevel) {
		this.buildLevel = buildLevel;
	}
	public int getProgramTypeAt() {
		return programTypeAt;
	}
	public void setProgramTypeAt(int programTypeAt) {
		this.programTypeAt = programTypeAt;
	}
	public String getProgramType() {
		return programType;
	}
	public void setProgramType(String programType) {
		this.programType = programType;
	}
	public String getDebug() {
		return debug;
	}
	public void setDebug(String debug) {
		this.debug = debug;
	}
	public List<BuildControlsVb> getChildren() {
		return children;
	}
	public void setChildren(List<BuildControlsVb> children) {
		this.children = children;
	}
	public BuildControlsVb getParent() {
		return parent;
	}
	public void setParent(BuildControlsVb parent) {
		this.parent = parent;
	}
	public String getFullBuild() {
		return fullBuild;
	}
	public void setFullBuild(String fullBuild) {
		this.fullBuild = fullBuild;
	}
	public String getFullBuildAllowed() {
		return fullBuildAllowed;
	}
	public void setFullBuildAllowed(String fullBuildAllowed) {
		this.fullBuildAllowed = fullBuildAllowed;
	}
}
