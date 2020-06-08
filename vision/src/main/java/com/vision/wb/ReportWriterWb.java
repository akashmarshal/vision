package com.vision.wb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.context.WebApplicationContext;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import com.vision.authentication.SessionContextHolder;
import com.vision.dao.AbstractDao;
import com.vision.dao.ReportWriterDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.CreateCsv;
import com.vision.util.DeepCopy;
import com.vision.util.ExcelExportUtil;
import com.vision.util.PDFExportUtil;
import com.vision.util.ValidationUtil;
import com.vision.vb.ColumnHeadersVb;
import com.vision.vb.CustomersDlyVb;
import com.vision.vb.FileInfoVb;
import com.vision.vb.PromptIdsVb;
import com.vision.vb.PromptTreeVb;
import com.vision.vb.RCReportFieldsVb;
import com.vision.vb.RWAccessVb;
import com.vision.vb.ReportStgVb;
import com.vision.vb.ReportsWriterVb;
import com.vision.vb.RsDashboardVb;
import com.vision.vb.TemplateStgVb;

import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

@Component
public class ReportWriterWb extends AbstractWorkerBean<ReportsWriterVb> implements ApplicationContextAware{

	String xbrlEndDate = "";
	public static Logger logger = Logger.getLogger(ReportWriterWb.class);
	private ReportWriterDao reportWriterDao;
	public ApplicationContext applicationContext;
	private boolean securedFtp;
    private String knownHostsFileName;
    private String userName;
    private String password;
    private String serverType;
    private String hostName;
    private String serverName;
    private int fileType;
    private String dateFormate;
	private char prompt;
	private String timezoneId;
	private static final String SERVICE_NAME = "Report Writer";
	
	@Autowired
	Configuration config;

	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public int getFileType() {
		return fileType;
	}
	public void setFileType(int fileType) {
		this.fileType = fileType;
	}
	public String getDateFormate() {
		return dateFormate;
	}
	public void setDateFormate(String dateFormate) {
		this.dateFormate = dateFormate;
	}
	public String getTimezoneId() {
		return timezoneId;
	}
	public void setTimezoneId(String timezoneId) {
		this.timezoneId = timezoneId;
	}
	public char getPrompt() {
		return prompt;
	}
	public void setPrompt(char prompt) {
		this.prompt = prompt;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getKnownHostsFileName() {
		return knownHostsFileName;
	}
	public void setKnownHostsFileName(String knownHostsFileName) {
		this.knownHostsFileName = knownHostsFileName;
	}
	
	public boolean isSecuredFtp() {
		return securedFtp;
	}
	public void setSecuredFtp(boolean securedFtp) {
		this.securedFtp = securedFtp;
	}
	public ReportWriterDao getReportWriterDao() {
		return reportWriterDao;
	}
	public void setReportWriterDao(ReportWriterDao reportWriterDao) {
		this.reportWriterDao = reportWriterDao;
	}

	@Override
	protected AbstractDao<ReportsWriterVb> getScreenDao() {
		return reportWriterDao;
	}
	@Override
	protected void setAtNtValues(ReportsWriterVb object) {
	}

	@Override
	protected void setVerifReqDeleteType(ReportsWriterVb object) {
	}
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public ArrayList getPageLoadValues(){
		try{
			List<ReportsWriterVb> lResult = getReportWriterDao().getDistinctReportCategoryForUser();
			for(ReportsWriterVb reportWriterVb:lResult){
				List<ReportsWriterVb> lResultTemp = getReportWriterDao().getDistinctSubCategoryForUserByReportCat(reportWriterVb.getReportId());
				for(ReportsWriterVb reportWriterVbTemp:lResultTemp){
					List<RWAccessVb> lResultTemp1 = getReportWriterDao().getRwAccessForUserBy(reportWriterVb.getReportId(), reportWriterVbTemp.getReportId());
					if(lResultTemp1 == null || lResultTemp1.isEmpty()){
						List<ReportsWriterVb> lResultTemp2 = getReportWriterDao().getReportsBy(reportWriterVb.getReportId(), reportWriterVbTemp.getReportId());
						reportWriterVbTemp.setChildren(lResultTemp2);
					}else if(ValidationUtil.isValid(lResultTemp1.get(0).getReportId()) && !"NA".equalsIgnoreCase(lResultTemp1.get(0).getReportId())){
						for(RWAccessVb rwAccessVbTemp3:lResultTemp1){
							List<ReportsWriterVb> lResultTemp2 = getReportWriterDao().getReportsBy(reportWriterVb.getReportId(), reportWriterVbTemp.getReportId(), rwAccessVbTemp3.getReportId());
							if(reportWriterVbTemp.getChildren() == null)reportWriterVbTemp.setChildren(new ArrayList<ReportsWriterVb>(0));
							reportWriterVbTemp.getChildren().addAll(lResultTemp2);
						}
					}else{
						List<ReportsWriterVb> lResultTemp2 = getReportWriterDao().getReportsBy(reportWriterVb.getReportId(), reportWriterVbTemp.getReportId());
						reportWriterVbTemp.setChildren(lResultTemp2);
					}
				}
				reportWriterVb.setChildren(lResultTemp);
			}
			return (ArrayList) lResult;
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("Exception in getting the Page load values.", ex);
			return null;
		}
	}
	public ExceptionCode getQueryForPrompts(ReportsWriterVb vObject){
		List<PromptIdsVb> prompts = null;
		List<PromptTreeVb> getPromptCascadeDefVal = null;
		ExceptionCode exceptionCode = null;
		try{
			List<List<PromptTreeVb>> promptData = new ArrayList<List<PromptTreeVb>>(0);
			if(ValidationUtil.isValid(vObject.getPromptId1())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				vObject.setTemplateName("PROMPT_MACRO_MAPPINGS_1");
				prompts.addAll(getPromptData(vObject.getPromptId1(), promptData, vObject.getPromptLabel1(), vObject.getPromptPosition1(), vObject.getPromptMacroMappings1(),vObject));
				if(prompts!=null){
					getPromptCascadeDefVal =promptData.get(0);
					if(getPromptCascadeDefVal!=null && getPromptCascadeDefVal.size()>0){
						vObject.setPromptValue1(getPromptCascadeDefVal.get(0).getField1());
					}
					if("DYNAMIC".equalsIgnoreCase(prompts.get(0).getPromptLogic())){
						vObject.setCascadePrompt(true);
					}
				}
			}
			if(ValidationUtil.isValid(vObject.getPromptId2())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				vObject.setTemplateName("PROMPT_MACRO_MAPPINGS_2");
				prompts.addAll(getPromptData(vObject.getPromptId2(), promptData, vObject.getPromptLabel2(), vObject.getPromptPosition2(), vObject.getPromptMacroMappings2(),vObject));
				if(prompts!=null){
					getPromptCascadeDefVal =promptData.get(1);
					if(getPromptCascadeDefVal!=null && getPromptCascadeDefVal.size()>0){
						vObject.setPromptValue2(getPromptCascadeDefVal.get(0).getField1());
					}
					if("DYNAMIC".equalsIgnoreCase(prompts.get(1).getPromptLogic())){
						vObject.setCascadePrompt(true);
					}
				}
			}
			if(ValidationUtil.isValid(vObject.getPromptId3())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				vObject.setTemplateName("PROMPT_MACRO_MAPPINGS_3");
				prompts.addAll(getPromptData(vObject.getPromptId3(), promptData, vObject.getPromptLabel3(), vObject.getPromptPosition3(), vObject.getPromptMacroMappings3(),vObject));
				if(prompts!=null){
					getPromptCascadeDefVal =promptData.get(2);
					if(getPromptCascadeDefVal!=null && getPromptCascadeDefVal.size()>0){
						vObject.setPromptValue3(getPromptCascadeDefVal.get(0).getField1());
					}
					if("DYNAMIC".equalsIgnoreCase(prompts.get(2).getPromptLogic())){
						vObject.setCascadePrompt(true);
					}
				}
			}
			if(ValidationUtil.isValid(vObject.getPromptId4())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				vObject.setTemplateName("PROMPT_MACRO_MAPPINGS_4");
				prompts.addAll(getPromptData(vObject.getPromptId4(), promptData, vObject.getPromptLabel4(), vObject.getPromptPosition4(), vObject.getPromptMacroMappings4(),vObject));
				if(prompts!=null){
					getPromptCascadeDefVal =promptData.get(3);
					if(getPromptCascadeDefVal!=null && getPromptCascadeDefVal.size()>0){
						vObject.setPromptValue4(getPromptCascadeDefVal.get(0).getField1());
					}
					if("DYNAMIC".equalsIgnoreCase(prompts.get(3).getPromptLogic())){
						vObject.setCascadePrompt(true);
					}
				}
			}
			if(ValidationUtil.isValid(vObject.getPromptId5())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				vObject.setTemplateName("PROMPT_MACRO_MAPPINGS_5");
				prompts.addAll(getPromptData(vObject.getPromptId5(), promptData, vObject.getPromptLabel5(), vObject.getPromptPosition5(), vObject.getPromptMacroMappings5(),vObject));
				if(prompts!=null){
					getPromptCascadeDefVal =promptData.get(4);
					if(getPromptCascadeDefVal!=null && getPromptCascadeDefVal.size()>0){
						vObject.setPromptValue5(getPromptCascadeDefVal.get(0).getField1());
					}
					if("DYNAMIC".equalsIgnoreCase(prompts.get(4).getPromptLogic())){
						vObject.setCascadePrompt(true);
					}
				}
			}
			if(ValidationUtil.isValid(vObject.getPromptId6())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				vObject.setTemplateName("PROMPT_MACRO_MAPPINGS_6");
				prompts.addAll(getPromptData(vObject.getPromptId6(), promptData, vObject.getPromptLabel6(), vObject.getPromptPosition6(), vObject.getPromptMacroMappings6(),vObject));
				if(prompts!=null){
					getPromptCascadeDefVal =promptData.get(5);
					if(getPromptCascadeDefVal!=null && getPromptCascadeDefVal.size()>0){
						vObject.setPromptValue6(getPromptCascadeDefVal.get(0).getField1());
					}
					if("DYNAMIC".equalsIgnoreCase(prompts.get(5).getPromptLogic())){
						vObject.setCascadePrompt(true);
					}
				}
			}
			
		
			exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.SUCCESSFUL_OPERATION, "Query", "");
			exceptionCode.setRequest(prompts);
			exceptionCode.setOtherInfo(vObject);
			exceptionCode.setResponse(promptData);
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			exceptionCode = rex.getCode();
			return exceptionCode;
		}
		return exceptionCode;
	}
	private List<PromptIdsVb> getPromptData(String promptId, List<List<PromptTreeVb>> promptData, String promptLabel,String promptPosition, String dependentPrompt, ReportsWriterVb vObj){
		List<PromptIdsVb> prompts = null;
		prompts = getReportWriterDao().getQueryForPrompts(promptId);
		if(prompts != null && !prompts.isEmpty()){
			for(PromptIdsVb prompt: prompts){
				if(ValidationUtil.isValid(promptLabel)){
					prompt.setPromptString(promptLabel);
				}
				if(ValidationUtil.isValid(promptPosition)){
					prompt.setPromptPosition(promptPosition);
				}
				if(ValidationUtil.isValid(prompt.getPromptLogic()) && "DYNAMIC".equalsIgnoreCase(prompt.getPromptLogic())){
					prompt.setCascadePrompt(true);
				}
				List<PromptTreeVb> promptTree = null;

				if(ValidationUtil.isValid(prompt.getPromptLogic()) && "DYNAMIC".equalsIgnoreCase(prompt.getPromptLogic())){
					PromptTreeVb promptInputVb =new PromptTreeVb();
					promptTree = getReportWriterDao().getCascadePromptData(prompt, promptInputVb, vObj);
				}else if("TREE".equalsIgnoreCase(prompt.getPromptType())){
					promptTree = getReportWriterDao().getTreePromptData(prompt);
					promptTree = createPraentChildRelations(promptTree, prompt.getFilterStr());
				}else if("COMBO".equalsIgnoreCase(prompt.getPromptType())){
					promptTree = getReportWriterDao().getComboPromptData(prompt, null);
				}else if("LABEL".equalsIgnoreCase(prompt.getPromptType())){
					promptTree = getReportWriterDao().getComboPromptData(prompt, null);
					if(promptTree != null && !promptTree.isEmpty()){
						prompt.setSelectedValue1(promptTree.get(0));
					}
				}else if("CALENDAR".equalsIgnoreCase(prompt.getPromptType())){
					promptTree = getReportWriterDao().getComboPromptData(prompt, null);
					if(promptTree != null && !promptTree.isEmpty()){
						prompt.setSelectedValue1(promptTree.get(0));
					}
				}else if("MAGNIFIER".equalsIgnoreCase(prompt.getPromptType())){
					promptTree = getReportWriterDao().getComboPromptData(prompt, null);
				}
				promptData.add(promptTree);
			}
		}	
		return prompts;
	}

	
	public ExceptionCode getComboPromptData(PromptIdsVb prompt, PromptTreeVb promptInputVb){
		ExceptionCode exceptionCode = null;
		try{
			List<PromptTreeVb> promptTree = getReportWriterDao().getComboPromptData(prompt, promptInputVb);
			exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.SUCCESSFUL_OPERATION, "Query", "");
			exceptionCode.setOtherInfo(prompt);
			exceptionCode.setResponse(promptTree);
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			exceptionCode = rex.getCode();
			return exceptionCode;
		}
		return exceptionCode;
	}
	public void deletePromptData(){
		getReportWriterDao().deletePromptData();
	}
	public List<PromptTreeVb> createPraentChildRelations(List<PromptTreeVb> promptTreeList, String filterString) {
		DeepCopy<PromptTreeVb> deepCopy = new DeepCopy<PromptTreeVb>();
		List<PromptTreeVb> lResult = new ArrayList<PromptTreeVb>(0);
		List<PromptTreeVb> promptTreeListCopy = new CopyOnWriteArrayList<PromptTreeVb>(deepCopy.copyCollection(promptTreeList));
		//Top Roots are added.
		for(PromptTreeVb promptVb:promptTreeListCopy){
			if(promptVb.getField1().equalsIgnoreCase(promptVb.getField3())){
				lResult.add(promptVb);
				promptTreeListCopy.remove(promptVb);
			}
		}
		//For each top node add all child's and to that child's add sub child's recursively.
		for(PromptTreeVb promptVb:lResult){
			addChilds(promptVb,promptTreeListCopy);
		}
		//Get the sub tree from the filter string if filter string is not null.
		if(ValidationUtil.isValid(filterString)){
			lResult = getSubTreeFrom(filterString, lResult);
		}
		//set the empty lists to null. this is required for UI to display the leaf nodes properly.
		nullifyEmptyList(lResult);
		return lResult;
	}
	private void addChilds(PromptTreeVb vObject, List<PromptTreeVb> promptTreeListCopy) {
		for(PromptTreeVb promptTreeVb:promptTreeListCopy){
			if(vObject.getField1().equalsIgnoreCase(promptTreeVb.getField3())){
				if(vObject.getChildren() == null){
					vObject.setChildren(new ArrayList<PromptTreeVb>(0));
				}
				vObject.getChildren().add(promptTreeVb);
				addChilds(promptTreeVb, promptTreeListCopy);
			}
		}
	}
	private List<PromptTreeVb> getSubTreeFrom(String filterString, List<PromptTreeVb> result) {
		List<PromptTreeVb> lResult = new ArrayList<PromptTreeVb>(0);
		for(PromptTreeVb promptTreeVb:result){
			if(promptTreeVb.getField1().equalsIgnoreCase(filterString)){
				lResult.add(promptTreeVb);
				return lResult;
			}else if(promptTreeVb.getChildren() != null){
				lResult = getSubTreeFrom(filterString, promptTreeVb.getChildren());
				if(lResult != null && !lResult.isEmpty()) return lResult;
			}
		}
		return lResult;
	}
	private void nullifyEmptyList(List<PromptTreeVb> lResult){
		for(PromptTreeVb promptTreeVb:lResult){
			if(promptTreeVb.getChildren() != null){
				nullifyEmptyList(promptTreeVb.getChildren());
			}
			if(promptTreeVb.getChildren() != null && promptTreeVb.getChildren().isEmpty()){
				promptTreeVb.setChildren(null);
			}
		}
	}
	public ReportsWriterVb drillDownReport(String drillDownId){
		ReportsWriterVb vObjVb = new ReportsWriterVb();
			List<ReportsWriterVb> list = getReportWriterDao().getReportSuiteDataByDDReportId(drillDownId);
			if(list != null)
				vObjVb = list.get(0);
		return vObjVb;
	} 
	public boolean findRowAndColumnSpan(List<ColumnHeadersVb> columnHeaders){
		for(ColumnHeadersVb headersVb: columnHeaders){
			if(headersVb.getColSpanNum() !=0 || headersVb.getRowSpanNum() != 0){
				return true;
			}
		}
		return false;
	}
	public ExceptionCode getReportData(ReportsWriterVb reportsWriterVb, List<PromptIdsVb> prompts){
		ExceptionCode exceptionCode = null;
		List<ReportStgVb> reportsStg =null;
		List<ColumnHeadersVb> columnHeaders = null;
		//ReportsWriterVb vObj=null;
		try{
			logger.info("Report Start at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
			PromptTreeVb promptTree = getReportWriterDao().callProcToPopulateDrillDownReportData(reportsWriterVb, prompts);
			logger.info("Report End at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
			if(promptTree == null || "1".equalsIgnoreCase(promptTree.getStatus())){
				exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.NO_RECORDS_FOUND, "Query", "");
				exceptionCode.setOtherInfo(reportsWriterVb);
				return exceptionCode;
			}
			List<ReportsWriterVb> list = getReportWriterDao().getReportSuiteDataByDDReportId(reportsWriterVb.getDrillDownId());
			if(list.size()>0){
				reportsWriterVb.setDrilDownList(list);
				reportsWriterVb.setDrillFlag(true);
			}else{
				reportsWriterVb.setDrillFlag(false); 
			}
			columnHeaders = getReportWriterDao().getColumnHeaders(promptTree);
			boolean isNewHeaderMethod = findRowAndColumnSpan(columnHeaders);
			reportsWriterVb.setNewHeaderMethod(isNewHeaderMethod);
			if(isNewHeaderMethod){
				int maxOfRowLevels = getReportWriterDao().getMaxOfRowsInHeader(promptTree);
				int maxOfColumnCount = getReportWriterDao().getMaxOfColumCountInHeader(promptTree);
				reportsWriterVb.setLabelRowCount(maxOfRowLevels);
				reportsWriterVb.setLabelColCount(maxOfColumnCount);
				int captoinColumnCount = getReportWriterDao().getCaptionColumnCount(reportsWriterVb);
				reportsWriterVb.setCaptionLabelColCount(captoinColumnCount);
			}
			if(!"Export".equalsIgnoreCase(reportsWriterVb.getMakerName())){
				reportsStg = getReportWriterDao().getReportsStgMaxData(promptTree);
				reportsWriterVb.setTotalRows(promptTree.getTotalRows());
			}else{
				reportsStg = getReportWriterDao().getReportsStgData(promptTree);
			}
			reportsWriterVb.setSessionId(promptTree.getSessionId());
			getReportWriterDao().callProcToCleanUpTables(reportsWriterVb);
			exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.SUCCESSFUL_OPERATION, "Query", "");
			exceptionCode.setOtherInfo(reportsWriterVb);
			exceptionCode.setRequest(columnHeaders);
			exceptionCode.setResponse(reportsStg);
			int retVal = getReportWriterDao().InsetAuditTrialDataForReports(reportsWriterVb, prompts);
			if(retVal!=Constants.SUCCESSFUL_OPERATION){
				logger.error("Error  inserting into rs_Schedule Audit");
			}
			
			logger.info("Report Response at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			logger.error("Report Exception at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(reportsWriterVb);
			exceptionCode.setRequest(columnHeaders);
			exceptionCode.setResponse(reportsStg);
			return exceptionCode;
		}
		return exceptionCode;
	}
	
	public ExceptionCode getListReportData(ReportsWriterVb reportsWriterVb, List<PromptIdsVb> prompts){
		ExceptionCode exceptionCode = null;
		List<ColumnHeadersVb> columnHeaders = null;
		String reportsStg = "";
		try{
			String sessionId= String.valueOf(System.currentTimeMillis());
			reportsWriterVb.setSessionId(sessionId);
			logger.info("List Report Start at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
			PromptTreeVb promptTree = getReportWriterDao().callProcToPopulateListReportData(reportsWriterVb, prompts);
			logger.info("List Report End at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
			if(promptTree == null || "1".equalsIgnoreCase(promptTree.getStatus())){
				exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.NO_RECORDS_FOUND, "Query", "");
				exceptionCode.setOtherInfo(reportsWriterVb);
				return exceptionCode;
			}
			if(ValidationUtil.isValid(reportsWriterVb.getDrillDownId())){
				List<ReportsWriterVb> list = getReportWriterDao().getReportSuiteDataByDDReportId(reportsWriterVb.getDrillDownId());
				if(list.size()>0){
					reportsWriterVb.setDrilDownList(list);
					reportsWriterVb.setDrillFlag(true);
				}else{
					reportsWriterVb.setDrillFlag(false); 
				}
			}else{
				reportsWriterVb.setDrillFlag(false); 
			}
			int captoinColumnCount = getReportWriterDao().getCaptionColumnCount(reportsWriterVb);
			reportsWriterVb.setCaptionLabelColCount(captoinColumnCount);
			columnHeaders = getReportWriterDao().getColumnHeaders(promptTree);
			List<String> coloumNames = new ArrayList<String>(columnHeaders.size());
			List<Integer> coloumNumbers = new ArrayList<Integer>(columnHeaders.size());
			for(ColumnHeadersVb colH:columnHeaders){
				if(colH.getColSpanNum() == 0){
					if(ValidationUtil.isValid(colH.getDbColumnName())){
						coloumNumbers.add(colH.getLabelColNum());
					}else{
						coloumNumbers.add(colH.getLabelColNum());
					}
				}
			}
			Collections.sort(coloumNumbers);
			for(int colNumber:coloumNumbers){
				for(ColumnHeadersVb colH:columnHeaders){
					if(colNumber == colH.getLabelColNum()){
						if(ValidationUtil.isValid(colH.getDbColumnName())){
							if(colH.getColSpanNum() == 0)
								coloumNames.add(colH.getDbColumnName());
						}else{
							if(colH.getColSpanNum() == 0)
								coloumNames.add(colH.getCaption());
						}
					}
				}
			}
			int maxOfRowLevels = getReportWriterDao().getMaxOfRowsInHeader(promptTree);
			int maxOfColumnCount = getReportWriterDao().getMaxOfColumCountInHeader(promptTree);
			reportsWriterVb.setLabelRowCount(maxOfRowLevels);
			reportsWriterVb.setLabelColCount(maxOfColumnCount);
			reportsStg = getReportWriterDao().getListReportDataAsXMLString(promptTree, coloumNames);
			reportsWriterVb.setTotalRows(promptTree.getTotalRows());
			getReportWriterDao().callProcToCleanUpTables(reportsWriterVb);
			exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.SUCCESSFUL_OPERATION, "Query", "");
			exceptionCode.setRequest(columnHeaders);
			exceptionCode.setOtherInfo(reportsWriterVb);
			exceptionCode.setResponse(reportsStg);
			int retVal = getReportWriterDao().InsetAuditTrialDataForReports(reportsWriterVb, prompts);
			if(retVal!=Constants.SUCCESSFUL_OPERATION){
				logger.error("Error  inserting into rs_Schedule Audit");
			}
			logger.info("List Report Response at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			logger.error("List Report Exception at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(reportsWriterVb);
			exceptionCode.setRequest(columnHeaders);
			exceptionCode.setResponse(reportsStg);			
			return exceptionCode;
		}
		return exceptionCode;
	}
	public ExceptionCode exportListDataToCSV(ReportsWriterVb reportsWriterVb, List<PromptIdsVb> prompts, int currentUserId){
		ExceptionCode exceptionCode = null;
		long min = 0 ;
		long max = 5000;
		PrintWriter out = null;
		FileWriter fw = null;
		CreateCsv csv = new CreateCsv();
		String csvSeparator = ",";
		String lineSeparator = "\n";
		try{
			String filePath = System.getProperty("java.io.tmpdir");
			if(!ValidationUtil.isValid(filePath)){
				filePath = System.getenv("TMP");
			}
			if(ValidationUtil.isValid(filePath)){
				filePath = filePath + File.separator;
			}
			fw = new FileWriter(filePath+ValidationUtil.encode(reportsWriterVb.getReportTitle())+"_"+currentUserId+".csv");
			out = new PrintWriter(fw);
			PromptTreeVb promptTree = getReportWriterDao().callProcToPopulateListReportData(reportsWriterVb, prompts);

			if(promptTree == null || "1".equalsIgnoreCase(promptTree.getStatus())){
				exceptionCode = CommonUtils.getResultObject(getReportWriterDao().getServiceName(), Constants.NO_RECORDS_FOUND, "Query", "");
				exceptionCode.setOtherInfo(reportsWriterVb);
				return exceptionCode;
			}
			List<ColumnHeadersVb> columnHeadersTemp = getReportWriterDao().getColumnHeaders(promptTree);
			List<ColumnHeadersVb> columnHeaders = new ArrayList<ColumnHeadersVb>();
			List<Integer> coloumNumbers = new ArrayList<Integer>(columnHeaders.size());
			for(ColumnHeadersVb columnHeadersVb:columnHeadersTemp){
				if(columnHeadersVb.getCaption().contains("<br/>")){
					String value=columnHeadersVb.getCaption().replaceAll("<br/>"," ");
					//value =  "\""+value+"\"";
					columnHeadersVb.setCaption(value);
				}
				if(!"DD_KEY_ID".equalsIgnoreCase(columnHeadersVb.getCaption()) 
						&& !"DD_KEY_VALUE".equalsIgnoreCase(columnHeadersVb.getCaption()) 
						&& !"SORT_FIELD".equalsIgnoreCase(columnHeadersVb.getCaption()))
					if(columnHeadersVb.getColSpanNum() == 0){
						columnHeaders.add(columnHeadersVb);
						coloumNumbers.add(columnHeadersVb.getLabelColNum());
					}
			}
			Collections.sort(coloumNumbers);
			List<ColumnHeadersVb> columnHeadersSorted = new ArrayList<ColumnHeadersVb>();
			for(int colNumber:coloumNumbers){
				for(ColumnHeadersVb colH:columnHeaders){
					if(colNumber == colH.getLabelColNum()){
						if(ValidationUtil.isValid(colH.getDbColumnName())){
							columnHeadersSorted.add(colH);
						}else{
							columnHeadersSorted.add(colH);
						}
					}
				}
			}
			List<String> colTyps = new ArrayList<String>(columnHeadersSorted.size());
			for(ColumnHeadersVb columnHeadersVb:columnHeadersSorted){
					out.print(columnHeadersVb.getCaption().replaceAll("_", " "));
					out.print(csvSeparator);
					colTyps.add(columnHeadersVb.getColType());
			}
			out.print(lineSeparator);
			String reportsStg = getReportWriterDao().getListReportDataForExportAsXMLStringWithMultipleHeaders(promptTree, columnHeadersSorted, min, max);
			do{
				csv.writeDataToCSV(reportsStg, out, csvSeparator, lineSeparator, colTyps );
				min = max;
				max += 5000;
				reportsStg = getReportWriterDao().getListReportDataForExportAsXMLStringWithMultipleHeaders(promptTree, columnHeadersSorted, min, max);
			}while(!"<tableData></tableData>".equalsIgnoreCase(reportsStg));
			out.flush();
			out.close();
			fw.close();
			reportsWriterVb.setSessionId(promptTree.getSessionId());
			getReportWriterDao().callProcToCleanUpTables(reportsWriterVb);
			exceptionCode = CommonUtils.getResultObject(getReportWriterDao().getServiceName(), Constants.SUCCESSFUL_OPERATION, "Query", "");
		}catch(RuntimeCustomException rex){
			logger.error("Export Exception " + rex.getCode().getErrorMsg());
			getReportWriterDao().callProcToCleanUpTables(reportsWriterVb);
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(reportsWriterVb);
			return exceptionCode;
		} catch (Exception e) {
			e.printStackTrace();
			return CommonUtils.getResultObject("", Constants.ERRONEOUS_OPERATION, "Query", e.getMessage());
		}
		return exceptionCode;
	}

	public ExceptionCode exportToPdf(List<ColumnHeadersVb> columnHeaders, 
			List<ReportStgVb> reportsStgs, ReportsWriterVb reportWriterVb, List<PromptIdsVb> prompts){
		try {
			PDFExportUtil exportUtil = new PDFExportUtil();
			String imageFolderUrl = ((WebApplicationContext)applicationContext).getServletContext().getRealPath("images");
			reportWriterVb.setMaker(SessionContextHolder.getContext().getVisionId());
			reportWriterVb.setVerifier(SessionContextHolder.getContext().getVisionId());
			getScreenDao().fetchMakerVerifierNames(reportWriterVb);
//			exportUtil.exportToPdf(columnHeaders, reportsStgs, reportWriterVb, prompts, imageFolderUrl, null, null);
			if(reportWriterVb.isNewHeaderMethod())
				exportUtil.exportToPdf(columnHeaders, reportsStgs, reportWriterVb, prompts, imageFolderUrl, null, null);
			else
				exportUtil.exportToPdfOld(columnHeaders, reportsStgs, reportWriterVb, prompts, imageFolderUrl, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
		return CommonUtils.getResultObject("", 1, "", "");
	}
	public ExceptionCode exportToXls(List<ColumnHeadersVb> columnHeaders, 
			List<ReportStgVb> reportsStgs, ReportsWriterVb reportWriterVb, List<PromptIdsVb> prompts, int currentUserId){
		int  rowNum = 10;
		try{
			List<String> colTypes = new ArrayList<String>();
			SXSSFWorkbook workBook = new SXSSFWorkbook(500);
			SXSSFSheet sheet = (SXSSFSheet) workBook.createSheet();
			Map<Integer, XSSFCellStyle>  styls = ExcelExportUtil.createStyles(workBook);
			Map<Integer,Integer> columnWidths = new HashMap<Integer,Integer>(columnHeaders.size());
			for(int loopCnt= 0; loopCnt < columnHeaders.size(); loopCnt++){
				columnWidths.put(Integer.valueOf(loopCnt), Integer.valueOf(-1));
			}
			int headerCnt  = 0;
			logger.info("Report Export Excel Starts at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
			if(reportWriterVb.isNewHeaderMethod())
				headerCnt = ExcelExportUtil.writeHeaders(reportWriterVb, columnHeaders, rowNum, sheet, styls, colTypes,columnWidths);
			else
				headerCnt = ExcelExportUtil.writeHeadersOld(reportWriterVb, columnHeaders, rowNum, sheet, styls, colTypes,columnWidths);
			if(reportWriterVb.getLabelRowCount() < 2){
				columnWidths.put(0, (columnWidths.get(0)<5000?5000:columnWidths.get(0)));
				if(columnWidths.get(6)!=null)
					columnWidths.put(6, (columnWidths.get(6)<5000?5000:columnWidths.get(6)));
			}
			String assetFolderUrl = ((WebApplicationContext)applicationContext).getServletContext().getRealPath("images");
			reportWriterVb.setMaker(SessionContextHolder.getContext().getVisionId());
			getScreenDao().fetchMakerVerifierNames(reportWriterVb);			
			ExcelExportUtil.createPrompts(reportWriterVb, prompts, sheet, workBook, assetFolderUrl, styls, headerCnt);
			if(reportWriterVb.getLabelRowCount() >= 2){rowNum++;}
			if(reportWriterVb.isNewHeaderMethod())
				sheet.createFreezePane(reportWriterVb.getCaptionLabelColCount(), rowNum+1);
			else
				sheet.createFreezePane(reportWriterVb.getLabelColCount(), rowNum+1);
			++rowNum;
			if(reportWriterVb.isNewHeaderMethod())
				rowNum = ExcelExportUtil.writeReportData(reportsStgs, reportWriterVb, sheet, rowNum, headerCnt, styls, colTypes, columnWidths);
			else
				rowNum = ExcelExportUtil.writeReportDataOld(reportsStgs, reportWriterVb, sheet, rowNum, headerCnt, styls, colTypes, columnWidths);
			if(reportWriterVb.isNewHeaderMethod()){
				headerCnt = reportWriterVb.getLabelColCount();
			}
			for(int loopCount=0; loopCount<headerCnt;loopCount++){
				sheet.autoSizeColumn(loopCount, true);
			}
/*			XSSFClientAnchor anchor = new XSSFClientAnchor(0,0,0,0,(short)0, 0, (short)getColumnNoForWidth(sheet,headerCnt), (short)4);
			anchor.setAnchorType(3);
			int colorIndex = workBook.addPicture(Base64.decodeBase64(imageData),SXSSFWorkbook.PICTURE_TYPE_PNG);
			Drawing drawing = sheet.createDrawingPatriarch();
			drawing.createPicture(anchor, colorIndex);*/
			workBook.setPrintArea(workBook.getActiveSheetIndex(), 0, headerCnt +1, 0, rowNum +1);
			String filePath = System.getProperty("java.io.tmpdir");
			if(!ValidationUtil.isValid(filePath)){
				filePath = System.getenv("TMP");
			}
			if(ValidationUtil.isValid(filePath)){
				filePath = filePath + File.separator;
			}
			File lFile = new File(filePath+ValidationUtil.encode(reportWriterVb.getReportTitle())+"_"+currentUserId+".xlsx");
			if(lFile.exists()){
				lFile.delete();
			}
			lFile.createNewFile();
			FileOutputStream fileOS = new FileOutputStream(lFile);
			workBook.write(fileOS);
			fileOS.flush();
			fileOS.close();
			logger.info("Report Export Excel End at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Report Export Excel Exception at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
			throw new RuntimeCustomException(e.getMessage());
		}
		return CommonUtils.getResultObject("", 1, "", "");
	}
	
	public ExceptionCode listExportToXls(List<PromptIdsVb> prompts, ReportsWriterVb reportWriterVb){
		FileOutputStream fileOS = null;
		try{
			int maxOfColumnCount = 0;
			logger.info("List Report Export Excel Procedure Start at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
			PromptTreeVb promptTree = getReportWriterDao().callProcToPopulateListReportData(reportWriterVb, prompts);
			logger.info("List Report Export Excel Procedure End at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
			List<ColumnHeadersVb> columnHeadersTemp = getReportWriterDao().getColumnHeaders(promptTree);
			List<ColumnHeadersVb> columnHeaders = new ArrayList<ColumnHeadersVb>();
			List<Integer> coloumNumbers = new ArrayList<Integer>(columnHeaders.size());
			for(ColumnHeadersVb columnHeadersVb:columnHeadersTemp){
				if(!"DD_KEY_ID".equalsIgnoreCase(columnHeadersVb.getCaption()) && 
					!"DD_KEY_VALUE".equalsIgnoreCase(columnHeadersVb.getCaption()) && 
					!"DD_KEY_LABEL".equalsIgnoreCase(columnHeadersVb.getCaption()) && 
						!"SORT_FIELD".equalsIgnoreCase(columnHeadersVb.getCaption()))
						if(columnHeadersVb.getColSpanNum() == 0 || "N".equalsIgnoreCase(columnHeadersVb.getColType())){
							columnHeaders.add(columnHeadersVb);
							if(columnHeadersVb.getColSpanNum() == 0){
								coloumNumbers.add(columnHeadersVb.getLabelColNum());
								maxOfColumnCount++;
							}
						}
			}
			Collections.sort(coloumNumbers);
			List<ColumnHeadersVb> columnHeadersSorted = new ArrayList<ColumnHeadersVb>();
			for(int colNumber:coloumNumbers){
				for(ColumnHeadersVb colH:columnHeaders){
					if(colNumber == colH.getLabelColNum()){
						if(ValidationUtil.isValid(colH.getDbColumnName())){
							columnHeadersSorted.add(colH);
						}else if(colH.getColSpanNum() == 0 ){
							columnHeadersSorted.add(colH);
						}
					}
				}
			}
			int maxOfRowLevels = getReportWriterDao().getMaxOfRowsInHeader(promptTree);
			//int maxOfColumnCount = getReportWriterDao().getMaxOfColumCountInHeader(promptTree);
			//int maxOfColumnCount = columnHeaders.size(); /*It shows drill down DD-KEY and DD_KEY_VALUE column in header in Excel sheet*/
			reportWriterVb.setLabelRowCount(maxOfRowLevels);
			reportWriterVb.setLabelColCount(maxOfColumnCount);
			Map<Integer,Integer> columnWidths = new HashMap<Integer,Integer>(columnHeadersSorted.size());
			List<String> colTyps = new ArrayList<String>(columnHeadersSorted.size());
			for(int loopCnt= 0; loopCnt < columnHeadersSorted.size(); loopCnt++){
				ColumnHeadersVb columnHeadersVb = columnHeadersSorted.get(loopCnt); 
				colTyps.add(columnHeadersVb.getColType());
				columnWidths.put(Integer.valueOf(loopCnt), Integer.valueOf(-1));
			}
			String filePath = System.getProperty("java.io.tmpdir");
			if(!ValidationUtil.isValid(filePath)){
				filePath = System.getenv("TMP");
			}
			if(ValidationUtil.isValid(filePath)){
				filePath = filePath + File.separator;
			}
			  
			File lFile = new File(filePath + ValidationUtil.encode(reportWriterVb.getReportTitle())+"_"+SessionContextHolder.getContext().getVisionId()+".xlsx");	
			if("ADF".equalsIgnoreCase(reportWriterVb.getReportCategory())){
				lFile = new File(filePath + ValidationUtil.encode(reportWriterVb.getReportCategoryDesc())+"_"+SessionContextHolder.getContext().getVisionId()+".xlsx");	
			}
			
			ExcelExportUtil.createTemplateFile(lFile);
			long min = 0;
			long max = ExcelExportUtil.MAX_FETCH_RECORDS;
			int rowNum = 10;
			boolean createHeadersAndFooters = true;
			logger.info("List Report Export Excel Start at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
			OPCPackage pkg = OPCPackage.open(new FileInputStream(lFile.getAbsolutePath()));
			XSSFWorkbook workbook = new XSSFWorkbook(pkg);
			SXSSFWorkbook workBook = new SXSSFWorkbook(workbook, 500);
			Map<Integer, XSSFCellStyle> styls = ExcelExportUtil.createStyles(workbook);
			SXSSFSheet sheet = (SXSSFSheet)workBook.getSheetAt(workBook.getActiveSheetIndex());
			int headerCnt  = colTyps.size();
			columnWidths.put(0, 4800);
			columnWidths.put(6, 5000);
			String assetFolderUrl = ((WebApplicationContext)applicationContext).getServletContext().getRealPath("images");
			reportWriterVb.setMaker(SessionContextHolder.getContext().getVisionId());
			getScreenDao().fetchMakerVerifierNames(reportWriterVb);
			PromptTreeVb promptTreeVb = new PromptTreeVb();
			for(int i=0;i<prompts.size();i++){
				PromptIdsVb promptIdsVb = prompts.get(i);	
				if(promptIdsVb.getPromptId().equalsIgnoreCase("PR059")){
					promptTreeVb = promptIdsVb.getSelectedValue1();
					if(ValidationUtil.isValid(promptTreeVb.getField1())){
						CustomersDlyVb customersDlyVb = new CustomersDlyVb();
						customersDlyVb.setCustomerId(promptTreeVb.getField1());
						String customerName = getReportWriterDao().getCustomerName(customersDlyVb);
						if(ValidationUtil.isValid(customerName)){
							promptIdsVb.getSelectedValue1().setField1(promptTreeVb.getField1()+" - "+customerName);
							promptIdsVb.getSelectedValue1().setField2(promptTreeVb.getField2()+" - "+customerName);
						}
						break;
					} 
				}
			}
			ExcelExportUtil.createPrompts(reportWriterVb, prompts, sheet, workBook, assetFolderUrl, styls, headerCnt);
			String xmlData = getReportWriterDao().getListReportDataForExportAsXMLStringWithMultipleHeaders(promptTree, columnHeadersSorted, min, max);
			/*Checks the format type is FHT*/
			String xmlDataHeader = getReportWriterDao().getListReportDataForExportAsXMLStringHeader(promptTree, columnHeadersSorted, min, max);
			do{
				if((max % SpreadsheetVersion.EXCEL2007.getMaxRows())==0){
					rowNum = 6;
					sheet = (SXSSFSheet)workBook.createSheet();
					createHeadersAndFooters = true;
				}
				//Add headers
				if(createHeadersAndFooters && !"FHT".equalsIgnoreCase(xmlDataHeader)){
					//rowNum = ExcelExportUtil.writeHeadersForListData(reportWriterVb, sheet,columnHeadersTemp, styls, rowNum, columnWidths);
					/*Blank Excel eill get exported. bcoz we are getting DD_KEY_ID and DD_KEY_Value also */
					rowNum = ExcelExportUtil.writeHeadersForListData(reportWriterVb, sheet,columnHeaders, styls, rowNum, columnWidths);
					++rowNum;
				}
				rowNum = ExcelExportUtil.writeListDataModify(reportWriterVb, sheet, colTyps, styls, xmlData, rowNum, columnWidths,columnHeadersSorted);				
				createHeadersAndFooters = false;
				min = max;
				max += ExcelExportUtil.MAX_FETCH_RECORDS;
				xmlData = getReportWriterDao().getListReportDataForExportAsXMLStringWithMultipleHeaders(promptTree, columnHeadersSorted, min, max);
			}while(!"<tableData></tableData>".equalsIgnoreCase(xmlData));
			for(int loopCount=0; loopCount<headerCnt;loopCount++){
				sheet.setColumnWidth(loopCount, columnWidths.get(loopCount));
			}
			for(int loopCount=0; loopCount<headerCnt;loopCount++){
				sheet.autoSizeColumn(loopCount, true);
			}
			fileOS = new FileOutputStream(lFile);
			workBook.write(fileOS);
			fileOS.flush();
			fileOS.close();
			logger.info("List Report Export Excel End at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
		}catch (Exception e) {
			if(fileOS != null){
				try{fileOS.close();}catch(Exception ex){}
			}
			logger.error("List Report Export Excel Exception at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}finally{
			try{getReportWriterDao().callProcToCleanUpTables(reportWriterVb);}catch (Exception ex){}
		}
		return CommonUtils.getResultObject("", 1, "", "");
	}
	
	public ExceptionCode getQueryForSchedulePrompts(ReportsWriterVb vObject){
		List<PromptIdsVb> prompts = null;
		ExceptionCode exceptionCode = null;
		try{
			List<List<PromptTreeVb>> promptData = new ArrayList<List<PromptTreeVb>>(0);
			if(ValidationUtil.isValid(vObject.getPromptId1())){
				prompts = new ArrayList<PromptIdsVb>();
				prompts.addAll(getSchedulePromptData(vObject.getPromptId1(), promptData, vObject.getPromptLabel1()));
			}
			if(ValidationUtil.isValid(vObject.getPromptId2())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				prompts.addAll(getSchedulePromptData(vObject.getPromptId2(), promptData, vObject.getPromptLabel2()));
			}
			if(ValidationUtil.isValid(vObject.getPromptId3())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				List<PromptIdsVb> lPrompts = getSchedulePromptData(vObject.getPromptId3(), promptData, vObject.getPromptLabel3());
				prompts.addAll(lPrompts);
			}
			if(ValidationUtil.isValid(vObject.getPromptId4())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				prompts.addAll(getSchedulePromptData(vObject.getPromptId4(), promptData, vObject.getPromptLabel4()));
			}
			if(ValidationUtil.isValid(vObject.getPromptId5())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				prompts.addAll(getSchedulePromptData(vObject.getPromptId5(), promptData, vObject.getPromptLabel5()));
			}
			if(ValidationUtil.isValid(vObject.getPromptId6())){
				if(prompts == null){
					prompts = new ArrayList<PromptIdsVb>();
				}
				prompts.addAll(getSchedulePromptData(vObject.getPromptId6(), promptData, vObject.getPromptLabel6()));
			}
			exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.SUCCESSFUL_OPERATION, "Query", "");
			exceptionCode.setRequest(prompts);
			exceptionCode.setOtherInfo(vObject);
			exceptionCode.setResponse(promptData);
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			exceptionCode = rex.getCode();
			return exceptionCode;
		}
		return exceptionCode;
	}
	private List<PromptIdsVb> getSchedulePromptData(String promptId, List<List<PromptTreeVb>> promptData, String promptLabel){
		List<PromptIdsVb> prompts = null;
		prompts = getReportWriterDao().getQueryForPrompts(promptId);
		if(prompts != null && !prompts.isEmpty()){
			for(PromptIdsVb prompt: prompts){
				if(ValidationUtil.isValid(promptLabel)){
					prompt.setPromptString(promptLabel);
				}				
				List<PromptTreeVb> promptTree = null;
				if("TREE".equalsIgnoreCase(prompt.getPromptType()) && ValidationUtil.isValid(prompt.getScheduleScript())){
					promptTree = getReportWriterDao().getScheduleTreePromptData(prompt);
					promptTree = createPraentChildRelations(promptTree, prompt.getFilterStr());
				}else if("TREE".equalsIgnoreCase(prompt.getPromptType()) && !ValidationUtil.isValid(prompt.getScheduleScript())){
					promptTree = getReportWriterDao().getTreePromptData(prompt);
					promptTree = createPraentChildRelations(promptTree, prompt.getFilterStr());
				}else if("COMBO".equalsIgnoreCase(prompt.getPromptType()) && ValidationUtil.isValid(prompt.getScheduleScript())){
					promptTree = getReportWriterDao().getScheduleComboPromptData(prompt, null);
				}else if("COMBO".equalsIgnoreCase(prompt.getPromptType()) && !ValidationUtil.isValid(prompt.getScheduleScript())){
					promptTree = getReportWriterDao().getComboPromptData(prompt, null);
				}else if("LABEL".equalsIgnoreCase(prompt.getPromptType()) && ValidationUtil.isValid(prompt.getScheduleScript())){
					promptTree = getReportWriterDao().getScheduleComboPromptData(prompt, null);
					if(promptTree != null && !promptTree.isEmpty()){
						prompt.setSelectedValue1(promptTree.get(0));
					}
				}else if("LABEL".equalsIgnoreCase(prompt.getPromptType()) && !ValidationUtil.isValid(prompt.getScheduleScript())){
					promptTree = getReportWriterDao().getComboPromptData(prompt, null);
					if(promptTree != null && !promptTree.isEmpty()){
						prompt.setSelectedValue1(promptTree.get(0));
					}
				}
				promptData.add(promptTree);
			}
		}
		return prompts;
	}
	
	public ExceptionCode getScheduleComboPromptData(PromptIdsVb prompt, PromptTreeVb promptInputVb){
		ExceptionCode exceptionCode = null;
		try{
			List<PromptTreeVb> promptTree = null;
			if(ValidationUtil.isValid(prompt.getScheduleScript()))
				promptTree = getReportWriterDao().getScheduleComboPromptData(prompt, promptInputVb);
			else
				promptTree = getReportWriterDao().getComboPromptData(prompt, promptInputVb);
			
			exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.SUCCESSFUL_OPERATION, "Query", "");
			exceptionCode.setOtherInfo(prompt);
			exceptionCode.setResponse(promptTree);
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			exceptionCode = rex.getCode();
			return exceptionCode;
		}
		return exceptionCode;
	}
	public ExceptionCode createCBKReport(ReportsWriterVb reportWriterVb, List<PromptIdsVb> prompts){
		ExceptionCode exceptionCode = null;
		FileOutputStream fileOS = null;
		File lfile =  null;
		File lfileRs = null;
		String fileNames = "";
		String tmpFileName = "";
		String filesNameslst[] = null;
		String destFilePath = System.getProperty("java.io.tmpdir");
		if(!ValidationUtil.isValid(destFilePath))
			destFilePath = System.getenv("TMP");
		if(ValidationUtil.isValid(destFilePath))
			destFilePath = destFilePath + File.separator;
		try{
			PromptTreeVb promptTree = getReportWriterDao().callProcToPopulateDrillDownReportData(reportWriterVb, prompts);
			if(promptTree == null || "1".equalsIgnoreCase(promptTree.getStatus())){
				exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.NO_RECORDS_FOUND, "Query", "");
				exceptionCode.setOtherInfo(reportWriterVb);
				return exceptionCode;
			}
			lfile = createTemplateFile(reportWriterVb);	
			lfileRs = lfile;
			OPCPackage pkg = OPCPackage.open( new FileInputStream(lfile.getAbsolutePath()));
			XSSFWorkbook workbook = new XSSFWorkbook(pkg);
			
			XSSFCellStyle cs = (XSSFCellStyle)workbook.createCellStyle();
			cs.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		    cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
		    XSSFFont font= workbook.createFont();
		    font.setColor(IndexedColors.WHITE.getIndex());
		    font.setBold(true);
		    cs.setFont(font);

		    long min = 0;
			long max = 1000;
			workbook.setForceFormulaRecalculation(true);
			List<RCReportFieldsVb> results = null;
			results = getReportWriterDao().getCBKReportData(reportWriterVb,prompts,promptTree, min, max);
			String tabId = "";
			do{
				 for(RCReportFieldsVb result:results){
					 String tmpFile[] = lfile.getName().split("_");
					 String fileName = tmpFile[0];
					 if(ValidationUtil.isValid(result.getExcelFileName()) && !result.getExcelFileName().equalsIgnoreCase(fileName)){
						fileOS = new FileOutputStream(lfile);
						workbook.write(fileOS);
						
						lfile = new File(destFilePath+result.getExcelFileName()+"_"+SessionContextHolder.getContext().getVisionId()+".xlsx");
						String filePath = lfile.getAbsolutePath();
						filePath = filePath.substring(0, filePath.indexOf(result.getExcelFileName()));
						if(filePath.contains("temp"))
							filePath = filePath.substring(0, filePath.indexOf("temp"));
						if(!lfile.exists())
							ExcelExportUtil.createTemplateFile(lfile);
						
						if(!tmpFileName.contains(result.getExcelFileName())){
							fileNames = fileNames+"|"+lfile.toString();
							tmpFileName = tmpFileName+"|"+result.getExcelFileName();
						}
						pkg = OPCPackage.open( new FileInputStream(lfile.getAbsolutePath()));
						workbook = new XSSFWorkbook(pkg);
						cs = (XSSFCellStyle)workbook.createCellStyle();
						cs.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
					    cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
					    font= workbook.createFont();
					    font.setColor(IndexedColors.WHITE.getIndex());
					    font.setBold(true);
					    cs.setFont(font);
					 }
					 int noOfsheets = workbook.getNumberOfSheets();
					 tabId = result.getTabelId();
					 if(Integer.parseInt(result.getTabelId()) > (noOfsheets-1)){
						workbook.createSheet(result.getTabelId());
					 }
					 Sheet sheet = workbook.getSheetAt(Integer.parseInt(result.getTabelId()));
					 Row row = null;
				     row = sheet.getRow(Integer.parseInt(result.getRowId())-1);
				     if(row == null){
				    	 row = sheet.createRow(Integer.parseInt(result.getRowId())-1);
				     }
				     Cell cell = row.getCell(Integer.parseInt(result.getColId()));
				     if(cell == null){
				    	cell = row.createCell(Integer.parseInt(result.getColId()));
				     }
				     if(cell == null || row == null){
						throw new RuntimeCustomException("Invalid Report data,Tab ID["+result.getTabelId()+"] Row Id["+Integer.parseInt(result.getRowId())+"], Col Id["+Integer.parseInt(result.getColId())+"] does not exists in template.");
					 }
					if(!ValidationUtil.isValid(result.getValue1())){
						
					}else if(("C".equalsIgnoreCase(result.getColType()) || "N".equalsIgnoreCase(result.getColType()))){
						cell.setCellValue(Double.parseDouble(result.getValue1()));
					}else if("F".equalsIgnoreCase(result.getColType())){
						cell.setCellType(Cell.CELL_TYPE_FORMULA);
						cell.setCellFormula(result.getValue1());
					}else{
						cell.setCellValue(result.getValue1());
					}
					if(ValidationUtil.isValid(result.getSheetName())){
				    	workbook.setSheetName(Integer.parseInt(result.getTabelId()),result.getSheetName());
				    }
					if(ValidationUtil.isValid(result.getRowStyle())){
						if("FHT".equalsIgnoreCase(result.getRowStyle())){
							cell.setCellStyle(cs);
						}if("FHTF".equalsIgnoreCase(result.getRowStyle())){
							sheet.createFreezePane(0, Integer.parseInt(result.getRowId()));
							cell.setCellStyle(cs);
						}
					}
				}
				min = max;
				max += 1000;
				results = getReportWriterDao().getCBKReportData(reportWriterVb,prompts,promptTree, min, max);
			}while(!results.isEmpty());
				fileOS = new FileOutputStream(lfile);
				workbook.write(fileOS);
			//add list of files to Zip
			String fileslst[] = fileNames.split("\\|");
			filesNameslst = tmpFileName.split("\\|");
			if(fileslst.length > 1){
				File f= new File(destFilePath+"\\"+reportWriterVb.getReportTitle()+".zip");
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
				for(int a = 1;a < fileslst.length;a++){
					FileInputStream fis = new FileInputStream(fileslst[a]);
					File f1 = new File(fileslst[a]);
					String tmpfileName = filesNameslst[a]+".xlsx";
					ZipEntry e = new ZipEntry(""+tmpfileName);
					out.putNextEntry(e);
					byte[] bytes = new byte[1024];
					int length;
					while ((length = fis.read(bytes)) >= 0) {
						out.write(bytes, 0, length);
					}
					fis.close();
					f1.delete();
				}
				out.closeEntry();
				out.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in CBK Report Generation", e);
			throw new RuntimeCustomException(e.getMessage());
		}finally{
			try{
				getReportWriterDao().callProcToCleanUpTables(reportWriterVb);
				if(fileOS != null){
					fileOS.flush();
					fileOS.close();
					fileOS = null;
				}
			}catch (Exception ex){}
		}
		exceptionCode =  CommonUtils.getResultObject("", 1, "", "");
		exceptionCode.setResponse(reportWriterVb.getTemplateName());
		if(filesNameslst != null && filesNameslst.length > 1){
			exceptionCode.setRequest(reportWriterVb.getReportTitle());
			if(lfileRs.exists()){
				lfileRs.delete();
			}
		}
		return exceptionCode;
	}
	public File createTemplateFile(ReportsWriterVb reportWriterVb){
		File lfile =  null;
		FileChannel source = null;
		FileChannel destination = null;
		try {
			String fileName = reportWriterVb.getTemplateName();
			fileName = ValidationUtil.encode(fileName.substring(0, fileName.indexOf(".xlsx")));
			String destFilePath = System.getProperty("java.io.tmpdir");
			if(!ValidationUtil.isValid(destFilePath)){
				destFilePath = System.getenv("TMP");
			}
			if(ValidationUtil.isValid(destFilePath)){
				destFilePath = destFilePath + File.separator;
			}
			lfile = new File(destFilePath+fileName+"_"+SessionContextHolder.getContext().getVisionId()+".xlsx");
			String filePath = lfile.getAbsolutePath();
			filePath = filePath.substring(0, filePath.indexOf(fileName));
			if(filePath.contains("temp")){
				filePath = filePath.substring(0, filePath.indexOf("temp"));
			}
			if(lfile.exists()){
				lfile.delete();
			}
			File lSourceFile = new File(filePath+"CB/"+reportWriterVb.getTemplateName());
			if(!lSourceFile.exists()){
				throw new RuntimeCustomException("Invalid Report Configuration, Invalid file name or file does not exists @ "+filePath);
			}
			source = new RandomAccessFile(lSourceFile,"rw").getChannel();
			destination = new RandomAccessFile(lfile,"rw").getChannel();
			long position = 0;
			long count = source.size();
			source.transferTo(position, count, destination);
	     }catch(Exception e){
	    	 throw new RuntimeCustomException("Invalid Report Configuration, Invalid file name or file does not exists");
	     }finally {
	    	 if(source != null) {
	    		 try{source.close();}catch(Exception ex){}
	    	 }
	    	 if(destination != null) {
	    		 try{destination.close();}catch(Exception ex){}
	    	 }
	    	 logger.info("Template File Successfully Created");
	    }
		return lfile;
	}
	
	public ExceptionCode createXBLRReport(ReportsWriterVb reportWriterVb, List<PromptIdsVb> prompts){
		ExceptionCode exceptionCode = null;
		FileOutputStream fileOS = null;
		try{
			String parentReportId = reportWriterVb.getReportId();
			logger.info("Started the extraction for XBRL");
			
			List<ReportsWriterVb> subReports = null;
			List<TemplateStgVb> reportsStg = null;
			
			List<TemplateStgVb> childDatalst = null;
			reportWriterVb.setSessionId("SC"+SessionContextHolder.getContext().getVisionId());
			
			logger.info("Start Parent Report......");
			PromptTreeVb promptTree = getReportWriterDao().callProcToPopulateDrillDownReportData(reportWriterVb, prompts);
			if(promptTree != null && !"1".equalsIgnoreCase(promptTree.getStatus()))
				reportsStg = getReportWriterDao().getTemplateStgData(promptTree);
			
			logger.info("End Parent Report......");
			subReports = getReportWriterDao().getSubReportsForReport(reportWriterVb);
			if(subReports != null && subReports.size() > 0){
				if(subReports == null || subReports.isEmpty()){
					exceptionCode = new ExceptionCode();
					exceptionCode.setErrorMsg("Invalid report configuration for Report Id:"+reportWriterVb.getReportId());
					exceptionCode.setOtherInfo(reportWriterVb);
					exceptionCode.setErrorCode(2);
					return exceptionCode;
				}
				if(subReports != null && subReports.size() > 0){
					for(ReportsWriterVb subReportsVb : subReports){
						childDatalst = null;
						promptTree = getReportWriterDao().callProcToPopulateDrillDownReportData(subReportsVb, prompts);
						if(promptTree == null || "1".equalsIgnoreCase(promptTree.getStatus())){
							exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.NO_RECORDS_FOUND, "Query", "");
							exceptionCode.setOtherInfo(reportWriterVb);
							return exceptionCode;
						}
						childDatalst = getReportWriterDao().getTemplateStgData(promptTree);
						if(!childDatalst.isEmpty() && childDatalst.size() > 0){
							for(TemplateStgVb tempStgVb : childDatalst){
								reportsStg.add(tempStgVb);
							}
						}
					}
				}
			}
			
			logger.info("Ended the extraction for XBLR");
			String data= genrateReport(reportWriterVb, prompts, reportsStg,parentReportId);
			
			String filePath = System.getProperty("java.io.tmpdir");
			if(!ValidationUtil.isValid(filePath)){
				filePath = System.getenv("TMP");
			}
			if(ValidationUtil.isValid(filePath)){
				filePath = filePath + File.separator;
			}
		    File lFile = new File(filePath+ValidationUtil.encode(reportWriterVb.getReportId())+"_BBON_"+xbrlEndDate+".xml");
			File lFileZip = new File(filePath+ValidationUtil.encode(reportWriterVb.getReportId())+"_BBON_"+xbrlEndDate+".zip");
			if(lFile.exists()){
				//lFile.delete();
			}
			fileOS = new FileOutputStream(lFile);
			fileOS.write(data.getBytes());
			FileOutputStream fos = new FileOutputStream(lFileZip);
			ZipOutputStream zos = new ZipOutputStream(fos);
			ZipEntry ze = new ZipEntry(lFile.getName());
			zos.putNextEntry(ze);
			
			FileInputStream fis = new FileInputStream(lFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
			zos.closeEntry();
			zos.close();
			fos.close();
			fis.close();
		}catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeCustomException(e.getMessage());
		}finally{
			try{
				getReportWriterDao().callProcToCleanUpTables(reportWriterVb);
				if(fileOS != null){
					fileOS.flush();
				    fileOS.close();
				    fileOS = null;
				}
			}catch (Exception ex){}													
		}
		exceptionCode =  CommonUtils.getResultObject("", 1, "", "");
		exceptionCode.setResponse(reportWriterVb.getReportId());
		exceptionCode.setRequest(xbrlEndDate);
		return exceptionCode;
	}
	private Map<String , Object> prepareData(ReportsWriterVb reportWriterVb, List<PromptIdsVb> prompts, List<TemplateStgVb> reportsStg){
		Map<String , Object> map = new HashMap<String, Object>();
		SimpleDateFormat formatter = new SimpleDateFormat("MMM-yyyy");
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatter3 = new SimpleDateFormat("dd-MMM-yyyy");
		
		Date endDate = null;
		Date startDate = null;
		Date prevDate = null;
		Date halfStartDate = null;
		Date quatStartDate = null;
		Date PrevQuatEndDate= null;
		Date PrevHalfEndDate =null;
		Calendar cal = Calendar.getInstance();
		String promptDate = prompts.get(prompts.size() - 1).getSelectedValue1().getField1();
		if(promptDate.length() > 8){
		try {
				startDate = formatter3.parse(promptDate);//Start Date
				cal.setTime(startDate); 
				cal.add(Calendar.DATE, -1);
				prevDate = cal.getTime();//Previous Date
				
				cal.setTime(startDate);
				cal.add(Calendar.MONTH, +1);
				cal.add(Calendar.DATE, -1);
				endDate = cal.getTime();//End Date
				
				cal.setTime(endDate);
				cal.add(Calendar.MONTH, -3);
				cal.add(Calendar.DATE, +1);
				quatStartDate = cal.getTime();//Quaterly Start Date
	
				cal.setTime(quatStartDate);
				cal.add(Calendar.DATE, -1);
				PrevQuatEndDate= cal.getTime();
				
				cal.setTime(endDate);
				cal.add(Calendar.MONTH, -6);
				cal.add(Calendar.DATE, +1);
				halfStartDate = cal.getTime();
			
				cal.setTime(halfStartDate);
				cal.add(Calendar.DATE, -1);
				PrevHalfEndDate= cal.getTime();
			}catch (ParseException e) {
				e.printStackTrace();
			}
		}else{
			try {
				/*	Use StartDate,EndDate,PrevDate for Montly Report
					Use halfStartDate,halfEndDate,halfPrevDate for Half-Yearly Report
					Use quatStartDate,quatEndDate,quatPrevDate for Quaterly Report
				*/
				startDate = formatter.parse(promptDate);//Start Date
				cal.setTime(startDate); 
				cal.add(Calendar.DATE, -1);
				prevDate = cal.getTime();//Previous Date
				
				cal.setTime(startDate);
				cal.add(Calendar.MONTH, +1);
				cal.add(Calendar.DATE, -1);
				endDate = cal.getTime();//End Date
				
				cal.setTime(startDate);
				cal.add(Calendar.MONTH, -2);
				quatStartDate = cal.getTime();//Quaterly Start Date

				cal.setTime(quatStartDate);
				cal.add(Calendar.DATE, -1);
				PrevQuatEndDate= cal.getTime();
				
				cal.setTime(startDate);
				cal.add(Calendar.MONTH, -5);
				halfStartDate = cal.getTime();
			
				cal.setTime(halfStartDate);
				cal.add(Calendar.DATE, -1);
				PrevHalfEndDate= cal.getTime();
			}catch (ParseException e) {
				e.printStackTrace();
			}
		}
		map.put("bankName", getReportWriterDao().findVisionVariableValueLoc("BANK_NAME"));
		map.put("bankTitle", getReportWriterDao().findVisionVariableValueLoc("BANK_TITLE"));
		
		map.put("startDate", formatter1.format(startDate));
		map.put("startDateORCLFrmt", formatter2.format(startDate));
		map.put("endDate", formatter1.format(endDate));
		map.put("endDateORCLFrmt", formatter2.format(endDate));
		map.put("prevDate", formatter1.format(prevDate));
		map.put("prevDateORCLFrmt", formatter2.format(prevDate));
		
		map.put("quatStartDate", formatter1.format(quatStartDate));
		map.put("quatStartDateORCLFrmt", formatter2.format(quatStartDate));
		map.put("quatEndDate", formatter1.format(endDate));
		map.put("quatEndDateORCLFrmt", formatter2.format(endDate));
		map.put("PrevQuatEndDate", formatter1.format(PrevQuatEndDate));
		map.put("PrevQuatEndDateORCLFrmt", formatter2.format(PrevQuatEndDate));
		
		map.put("halfStartDate", formatter1.format(halfStartDate));
		map.put("halfStartDateORCLFrmt", formatter2.format(halfStartDate));
		map.put("halfEndDate", formatter1.format(endDate));
		map.put("halfEndDateORCLFrmt", formatter2.format(endDate));
		map.put("PrevHalfEndDate", formatter1.format(PrevHalfEndDate));
		map.put("PrevHalfEndDateORCLFrmt", formatter2.format(PrevHalfEndDate));
		
		map.put("promptIdsVb", prompts.get(0));
		map.put("reportWriterVb", reportWriterVb);
		map.put("reportsStgList", reportsStg);
		map.put("shortDesc", prompts.get(0).getSelectedValue1().getField1().substring(0,4));
		Boolean endDateFlag = false;
		int recCounterTemp1 = 0;
		int recCounterTemp2 = 0;
		int recCounterTemp3 = 0;
		if(reportsStg != null && reportsStg.size() > 0){
			for(int  ctr = 0;ctr < reportsStg.size();ctr++){
				TemplateStgVb tempVb = reportsStg.get(ctr);
				if( "7".equalsIgnoreCase(tempVb.getRowId())){
					xbrlEndDate =  tempVb.getCellData();
					endDateFlag = true;
				}
				if("0".equalsIgnoreCase(tempVb.getColId()) && "14".equalsIgnoreCase(tempVb.getTemplateId())){
					recCounterTemp1++;
				}
				if("0".equalsIgnoreCase(tempVb.getColId()) && "15".equalsIgnoreCase(tempVb.getTemplateId())){
					recCounterTemp2++;
				}
				if("0".equalsIgnoreCase(tempVb.getColId()) && "16".equalsIgnoreCase(tempVb.getTemplateId())){
					recCounterTemp3++;
				}
			}
		}
		map.put("recCounterTemp1", recCounterTemp1);
		map.put("recCounterTemp2", recCounterTemp2);
		map.put("recCounterTemp3", recCounterTemp3);
		if(!endDateFlag)
			xbrlEndDate =  formatter2.format(endDate);
		
		xbrlEndDate = xbrlEndDate.replaceAll("/", "_");
		return map;
	}
	private String genrateReport(ReportsWriterVb reportWriterVb, List<PromptIdsVb> prompts, List<TemplateStgVb> reportsStg,String reportId) throws TemplateNotFoundException, MalformedTemplateNameException, freemarker.core.ParseException, IOException, TemplateException{
		Map<String , Object> map = prepareData(reportWriterVb, prompts, reportsStg);
		String result = FreeMarkerTemplateUtils.processTemplateIntoString(config.getTemplate("SR_EMAIL_FORGOT_USERNAME.vm"), map);
		return result;
	}
	
	public String getBusinessPromptData(String countryId){
		String promptBusinessDate = "";
		try{
			promptBusinessDate = getReportWriterDao().getVisionBusinessDate(countryId);
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			return promptBusinessDate;
		}
		return promptBusinessDate;
	}
	public List<PromptTreeVb> getCascadePromptData(PromptIdsVb prompt, PromptTreeVb promptInputVb){
		List<PromptTreeVb> promptTree = null;
		ReportsWriterVb vObj=new ReportsWriterVb();
		try{
			promptTree = getReportWriterDao().getCascadePromptData(prompt, promptInputVb,vObj);
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			return promptTree;
		}
		return promptTree;
	}
	
	public ExceptionCode getListReportDataToPdf(ReportsWriterVb reportsWriterVb, List<PromptIdsVb> prompts){
		ExceptionCode exceptionCode = null;
		List<ColumnHeadersVb> columnHeaders = null;
		String reportsStg = "";
		try{
			String sessionId= String.valueOf(System.currentTimeMillis());
			reportsWriterVb.setSessionId(sessionId);
			logger.info("List Report Start at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
			PromptTreeVb promptTree = getReportWriterDao().callProcToPopulateListReportData(reportsWriterVb, prompts);
			logger.info("List Report End at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
			if(promptTree == null || "1".equalsIgnoreCase(promptTree.getStatus())){
				exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.NO_RECORDS_FOUND, "Query", "");
				exceptionCode.setOtherInfo(reportsWriterVb);
				return exceptionCode;
			}
			if(ValidationUtil.isValid(reportsWriterVb.getDrillDownId())){
				List<ReportsWriterVb> list = getReportWriterDao().getReportSuiteDataByDDReportId(reportsWriterVb.getDrillDownId());
				if(list.size()>0){
					reportsWriterVb.setDrilDownList(list);
					reportsWriterVb.setDrillFlag(true);
				}else{
					reportsWriterVb.setDrillFlag(false); 
				}
			}else{
				reportsWriterVb.setDrillFlag(false); 
			}
			int captoinColumnCount = getReportWriterDao().getCaptionColumnCount(reportsWriterVb);
			reportsWriterVb.setCaptionLabelColCount(captoinColumnCount);
			columnHeaders = getReportWriterDao().getColumnHeaders(promptTree);
			List<String> coloumNames = new ArrayList<String>(columnHeaders.size());
			List<Integer> coloumNumbers = new ArrayList<Integer>(columnHeaders.size());
			for(ColumnHeadersVb colH:columnHeaders){
				if(colH.getColSpanNum() == 0){
					if(ValidationUtil.isValid(colH.getDbColumnName())){
						coloumNumbers.add(colH.getLabelColNum());
					}else{
						coloumNumbers.add(colH.getLabelColNum());
					}
				}
			}
			Collections.sort(coloumNumbers);
			for(int colNumber:coloumNumbers){
				for(ColumnHeadersVb colH:columnHeaders){
					if(colNumber == colH.getLabelColNum()){
						if(ValidationUtil.isValid(colH.getDbColumnName())){
							if(colH.getColSpanNum() == 0)
								coloumNames.add(colH.getDbColumnName());
						}else{
							if(colH.getColSpanNum() == 0)
								coloumNames.add(colH.getCaption());
						}
					}
				}
			}
			int maxOfRowLevels = getReportWriterDao().getMaxOfRowsInHeader(promptTree);
			int maxOfColumnCount = getReportWriterDao().getMaxOfColumCountInHeader(promptTree);
			reportsWriterVb.setLabelRowCount(maxOfRowLevels);
			reportsWriterVb.setLabelColCount(maxOfColumnCount);
			reportsStg = getReportWriterDao().getFullListReportDataAsXMLString(promptTree, coloumNames);
			reportsWriterVb.setTotalRows(promptTree.getTotalRows());
			getReportWriterDao().callProcToCleanUpTables(reportsWriterVb);
			exceptionCode = CommonUtils.getResultObject(getScreenDao().getServiceName(), Constants.SUCCESSFUL_OPERATION, "Query", "");
			exceptionCode.setRequest(columnHeaders);
			exceptionCode.setOtherInfo(reportsWriterVb);
			exceptionCode.setResponse(reportsStg);
			int retVal = getReportWriterDao().InsetAuditTrialDataForReports(reportsWriterVb, prompts);
			if(retVal!=Constants.SUCCESSFUL_OPERATION){
				logger.error("Error  inserting into rs_Schedule Audit");
			}
			logger.info("List Report Response at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
		}catch(RuntimeCustomException rex){
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			logger.error("List Report Exception at: " + reportsWriterVb.getReportId()+" : "+reportsWriterVb.getReportTitle());
			exceptionCode = rex.getCode();
			exceptionCode.setOtherInfo(reportsWriterVb);
			exceptionCode.setRequest(columnHeaders);
			exceptionCode.setResponse(reportsStg);			
			return exceptionCode;
		}
		return exceptionCode;
	}
	
	public ExceptionCode exportListRepoprtDataToPdf(List<ColumnHeadersVb> columnHeaders, 
			String reportsStgs, ReportsWriterVb reportWriterVb, List<PromptIdsVb> prompts){
		try {
			PDFExportUtil exportUtil = new PDFExportUtil();
			String imageFolderUrl = ((WebApplicationContext)applicationContext).getServletContext().getRealPath("images");
			reportWriterVb.setMaker(SessionContextHolder.getContext().getVisionId());
			reportWriterVb.setVerifier(SessionContextHolder.getContext().getVisionId());
			getScreenDao().fetchMakerVerifierNames(reportWriterVb);
			
			List<Integer> coloumNumbers = new ArrayList<Integer>(columnHeaders.size());
			for(ColumnHeadersVb colH:columnHeaders){
				if(colH.getColSpanNum() == 0){
					if(ValidationUtil.isValid(colH.getDbColumnName())){
						coloumNumbers.add(colH.getLabelColNum());
					}else{
						coloumNumbers.add(colH.getLabelColNum());
					}
				}
			}
			Collections.sort(coloumNumbers);			
			List<ColumnHeadersVb> columnHeadersSorted = new ArrayList<ColumnHeadersVb>();
			for(int colNumber:coloumNumbers){
				for(ColumnHeadersVb colH:columnHeaders){
					if(colNumber == colH.getLabelColNum()){
						if(ValidationUtil.isValid(colH.getDbColumnName())){
							columnHeadersSorted.add(colH);
						}else{
							columnHeadersSorted.add(colH);
						}
					}
				}
			}

			List<String> colTyps = new ArrayList<String>(columnHeadersSorted.size());
			for(int loopCnt= 0; loopCnt < columnHeadersSorted.size(); loopCnt++){
				ColumnHeadersVb columnHeadersVb = columnHeadersSorted.get(loopCnt); 
				colTyps.add(columnHeadersVb.getColType());
			}			
			
			exportUtil.exportListReportToPdf(columnHeaders, reportsStgs, reportWriterVb, prompts, imageFolderUrl, null, null, colTyps);
		} catch (Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
		return CommonUtils.getResultObject("", 1, "", "");
	}
	
	
	public ExceptionCode listReportFilesFromFtpServer(String node, String file, String ext, String path, String serverName, int currentUserId){
		List collTemp = null;
		ReportsWriterVb vObjTab   = null;
		ArrayList credentialsList = null;
		ArrayList<Object> arrListLocal = new ArrayList<Object>();
		ExceptionCode exceptionCode  = null;
		TelnetConnection telnetConnection = null;
        try{ 
        	String environmentNode  = System.getenv("VISION_NODE_NAME");
        		/*if(!ValidationUtil.isValid(environmentNode))
        			environmentNode="DD";*/
        		if(ValidationUtil.isValid(node)){
         			environmentNode = node;
             	}
            String environmentParam = System.getenv("VISION_SERVER_ENVIRONMENT");
     			if(!ValidationUtil.isValid(environmentParam))
     				environmentParam="PRODUCTION";
			
     		if(serverName.contains(",")){
     			serverName =serverName.replaceAll(",","','");
     		}
			 if(ValidationUtil.isValid(environmentNode) && !"ALL".equalsIgnoreCase(environmentNode)){
				 collTemp = getReportWriterDao().findServerCredentials(environmentParam, environmentNode, "");
			 }else{
				collTemp = getReportWriterDao().findServerCredentials(environmentParam, "", serverName);
			 }
			 arrListLocal.add(collTemp);

			if(arrListLocal.size()==0){
		       exceptionCode = CommonUtils.getResultObject(SERVICE_NAME+" - Environment :"+environmentParam+" - Node :"+environmentNode, Constants.NO_RECORDS_FOUND, "","");
			   exceptionCode.setRequest(exceptionCode.getErrorMsg());
			   return exceptionCode; 
			}
			if(isSecuredFtp()){
				exceptionCode =listFilesFromSFtpServer(arrListLocal, credentialsList, ext, file, path, currentUserId);
        	}else{
				String getExacthostName="";
				String getExactuserName="";
				String getExactpassWord="";
				String getExactFile="";
				String getExactExtension="";
				try{
					for(int arr=0; arr< arrListLocal.size(); arr++){
						credentialsList = (ArrayList) arrListLocal.get(arr);
					  if(credentialsList!=null && credentialsList.size()>0){
						Calendar timestamp = null;
						for (int Ctr = 0; Ctr < credentialsList.size(); Ctr++){
							vObjTab = (ReportsWriterVb) credentialsList.get(Ctr);
							setUserName(vObjTab.getUserName());
							setPassword(vObjTab.getPassword());
							setHostName(vObjTab.getHostName());
					
							telnetConnection = new TelnetConnection(getHostName(),getUserName(),getPassword(), prompt);
							telnetConnection.connect();
							telnetConnection.sendCommand("cd "+path);
							String responseStr = telnetConnection.sendCommand("ls -ltc ");
							String[] fileEntryArray  = responseStr.split("\r\n");
							FTPClientConfig conf = new FTPClientConfig(getServerType());
							conf.setServerTimeZoneId(getTimezoneId());
							UnixFTPEntryParser unixFtpEntryParser = new UnixFTPEntryParser(conf);
							List<String> fileEntryList = unixFtpEntryParser.preParse(new LinkedList<String>(Arrays.asList(fileEntryArray)));
							List<FTPFile> lfiles = new ArrayList<FTPFile>(fileEntryList.size());
							for(String fileEntry:fileEntryList){
								FTPFile ftpFile = unixFtpEntryParser.parseFTPEntry(fileEntry);
								if(ftpFile != null)
									lfiles.add(ftpFile);
							}
							
							for(FTPFile fileName:lfiles){
								if(fileName.getName().endsWith(".zip") || fileName.getName().endsWith(".tar")) continue;
								FileInfoVb fileInfoVb = new FileInfoVb();
								fileInfoVb.setName(fileName.getName());
				
								String[] extList = ext.split(",");
								int t = -1;
								for(int ex=0; ex<extList.length; ex++){
								 	String searchFile =file+"."+extList[ex];
				
								 	if(searchFile.equalsIgnoreCase(fileName.getName())){
								 		if(ValidationUtil.isValid(timestamp)){
											t=timestamp.compareTo(fileName.getTimestamp());
											if(t == -1){
												timestamp = fileName.getTimestamp();
											}
										}else{
											timestamp = fileName.getTimestamp();
										}
										
									 	if(t == -1){
									 		getExactFile =file;
									 		getExactExtension =fileInfoVb.getName().substring(fileInfoVb.getName().lastIndexOf(".") + 1);;
									 		getExacthostName =getHostName();
									 		getExactuserName =getUserName();
									 		getExactpassWord =getPassword();
									 	}
								 	}
								}
							}
						}
				  	  }else{
		        		 if(!ValidationUtil.isValid(getExactFile)){
		        			 exceptionCode = CommonUtils.getResultObject(SERVICE_NAME+" - Credentials", Constants.NO_RECORDS_FOUND, "","");
		        			 exceptionCode.setRequest(exceptionCode.getErrorMsg());
		        			 return exceptionCode; 
		        		 }
		        	 }
					}
					
					if(ValidationUtil.isValid(getExacthostName) && ValidationUtil.isValid(getExactuserName) && ValidationUtil.isValid(getExactpassWord) && ValidationUtil.isValid(getExactFile)){
						exceptionCode =downloadLatestFilesFromSFtpServer(getExacthostName, getExactuserName, getExactpassWord, getExactFile,getExactExtension, path, currentUserId);
					}else{
		       			 exceptionCode = CommonUtils.getResultObject(SERVICE_NAME+" - File not found ["+file+"-"+ext+"]", Constants.NO_RECORDS_FOUND, "","");
		       			 exceptionCode.setRequest(exceptionCode.getErrorMsg());
		       			 return exceptionCode; 
					}
				}catch (Exception e) {
					e.printStackTrace();
		        	exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download", "@-:@FTP -"+getHostName()+"@-:@");
					throw new RuntimeCustomException(exceptionCode);
				}
        	}
        }catch (Exception e) {
        	exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download","");
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String res=errors.toString();
			String segments[] = res.split("@-:@");
			String errorDesc = segments[1];
			
			if(ValidationUtil.isValid(errorDesc)){
	        	exceptionCode = CommonUtils.getResultObject(SERVICE_NAME+" - "+errorDesc, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download","");
			}
			throw new RuntimeCustomException(exceptionCode);
		}finally{
        	if(telnetConnection != null && telnetConnection.isConnected()){
        		telnetConnection.disconnect();
        		telnetConnection = null;
        	}
        }
        return exceptionCode;
	}
	public class MyUserInfo implements UserInfo{
	    public String getPassword(){ return password; }
	    public boolean promptYesNo(String str){
	      return false;
	    }
	    public String getPassphrase(){ return null; }
	    public boolean promptPassphrase(String message){ return true; }
	    public boolean promptPassword(String message){ return false; }
	    
	    public void showMessage(String message){
	      return;
	    }
	}
	
	public ExceptionCode listFilesFromSFtpServer(ArrayList<Object> arrListLocal, ArrayList credentialsList, String ext, String exactFile, String path, int currentUserId){
		ExceptionCode exceptionCode  = null;
		ReportsWriterVb vObjTab   = null;
		String getExacthostName="";
		String getExactuserName="";
		String getExactpassWord="";
		String getExactFile="";
		String getExactExtension="";
        try{
        	for(int arr=0; arr< arrListLocal.size(); arr++){
				credentialsList = (ArrayList) arrListLocal.get(arr);
		  	  if(credentialsList!=null && credentialsList.size()>0){
				Calendar timestamp = null;
				for (int Ctr = 0; Ctr < credentialsList.size(); Ctr++){
					vObjTab = (ReportsWriterVb) credentialsList.get(Ctr);
					setUserName(vObjTab.getUserName());
					setPassword(vObjTab.getPassword());
					setHostName(vObjTab.getHostName());
					
					JSch jsch = new JSch();  
					jsch.setKnownHosts( getKnownHostsFileName() );
					Session session = jsch.getSession( getUserName(), getHostName() ); 
					{   // "interactive" version   // can selectively update specified known_hosts file    
						// need to implement UserInfo interface  
						// MyUserInfo is a swing implementation provided in    
						//  examples/Sftp.java in the JSch dist   
						UserInfo ui = new MyUserInfo();   
						session.setUserInfo(ui);    // OR non-interactive version. Relies in host key being in known-hosts file   
						session.setPassword( getPassword());
					}

					session.setConfig("StrictHostKeyChecking", "no");
					session.connect();
					Channel channel = session.openChannel( "sftp" ); 
					channel.connect();

					ChannelSftp sftpChannel = (ChannelSftp) channel;
					sftpChannel.cd(path);
					
					Vector<ChannelSftp.LsEntry> vtc=  sftpChannel.ls("*.*");
					
					FTPClientConfig conf = new FTPClientConfig(getServerType());
					conf.setServerTimeZoneId(getTimezoneId());
					UnixFTPEntryParser unixFtpEntryParser = new UnixFTPEntryParser(conf);
					List<FTPFile> lfiles = new ArrayList<FTPFile>(vtc.size());
					for(ChannelSftp.LsEntry lsEntry :  vtc) {
						FTPFile file= unixFtpEntryParser.parseFTPEntry(lsEntry.getLongname());
						if(file != null)
							lfiles.add(file);
					}

					for(FTPFile fileName:lfiles){
						if(fileName.getName().endsWith(".zip") || fileName.getName().endsWith(".tar")) continue;
						FileInfoVb fileInfoVb = new FileInfoVb();
						fileInfoVb.setName(fileName.getName());
		
						String[] extList = ext.split(",");
						int t = -1;
						for(int ex=0; ex<extList.length; ex++){
						 	String searchFile =exactFile+"."+extList[ex];
						 	if(searchFile.equalsIgnoreCase(fileName.getName())){
								if(ValidationUtil.isValid(timestamp)){
									t=timestamp.compareTo(fileName.getTimestamp());
									if(t == -1){
										timestamp = fileName.getTimestamp();
									}
								}else{
									timestamp = fileName.getTimestamp();
								}
							 	if(t == -1){
							 		getExactFile =exactFile;
							 		getExactExtension =fileInfoVb.getName().substring(fileInfoVb.getName().lastIndexOf(".") + 1);;
							 		getExacthostName =getHostName();
							 		getExactuserName =getUserName();
							 		getExactpassWord =getPassword();
							 	}
						 	}
						}
					}
					sftpChannel.disconnect();
					session.disconnect();
				}
        	 }else{
        		 if(!ValidationUtil.isValid(getExactFile)){
        			 exceptionCode = CommonUtils.getResultObject(SERVICE_NAME+" - Credentials", Constants.NO_RECORDS_FOUND, "","");
        			 exceptionCode.setRequest(exceptionCode.getErrorMsg());
        			 return exceptionCode; 
        		 }
        	 }
        	}
        	if(ValidationUtil.isValid(getExacthostName) && ValidationUtil.isValid(getExactuserName) && ValidationUtil.isValid(getExactpassWord) && ValidationUtil.isValid(getExactFile)){
				exceptionCode =downloadLatestFilesFromSFtpServer(getExacthostName, getExactuserName, getExactpassWord, getExactFile,getExactExtension, path, currentUserId);
			}else{
       			 exceptionCode = CommonUtils.getResultObject(SERVICE_NAME+" - File not found ["+exactFile+"-"+ext+"]", Constants.NO_RECORDS_FOUND, "","");
       			 exceptionCode.setRequest(exceptionCode.getErrorMsg());
       			 return exceptionCode; 
			}
        }catch (Exception e) {
			e.printStackTrace();
        	exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download", "@-:@FTP -"+getHostName()+"@-:@");
			throw new RuntimeCustomException(exceptionCode);
		}
        return exceptionCode;
	}
	
	
	public ExceptionCode downloadLatestFilesFromSFtpServer(String getExacthostName,String getExactuserName,String getExactpassWord,String getExactFile, String getExactExtension, String path, int currentUserId){
		ExceptionCode exceptionCode  = null;
        try{
        	String filePath = System.getProperty("java.io.tmpdir");
			if(!ValidationUtil.isValid(filePath)){
				filePath = System.getenv("TMP");
			}
			if(ValidationUtil.isValid(filePath)){
				filePath = filePath + File.separator;
			}
        	JSch jsch = new JSch();  
			jsch.setKnownHosts( getKnownHostsFileName() );
			Session session = jsch.getSession( getExactuserName, getExacthostName); 
			{   
				UserInfo ui = new MyUserInfo();   
				session.setUserInfo(ui);       
				session.setPassword( getExactpassWord);
			}
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			Channel channel = session.openChannel( "sftp" ); 
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.cd(path);
			
			Vector<ChannelSftp.LsEntry> vtc=  sftpChannel.ls("*.*");
			
			int number = 0;
				
			String destPath =filePath+ValidationUtil.encode(getExactFile)+"_"+currentUserId+"."+getExactExtension;
			try{
				sftpChannel.get(getExactFile+"."+getExactExtension , destPath);
			}catch(SftpException e){
				sftpChannel.exit();
				sftpChannel.disconnect();
            	exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download", "@-:@"+getExacthostName+"@-:@");
    			exceptionCode.setRequest(e.getMessage());
                return exceptionCode;
    		}
			sftpChannel.exit();
			sftpChannel.disconnect();
			exceptionCode = CommonUtils.getResultObject("", 1, "", "");
			exceptionCode.setRequest(getExactFile+"."+getExactExtension);
			exceptionCode.setResponse(number);

       }catch (Exception e) {
			e.printStackTrace();
        	exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download", "@-:@"+getExacthostName+"@-:@");
			throw new RuntimeCustomException(exceptionCode);
		}
        return exceptionCode;
	}
	
	public ExceptionCode DasboardExportToXls(List<PromptIdsVb> prompts, ReportsWriterVb reportWriterVb, ExceptionCode exceptionCode, int currentUserId,List<String> imgStringList){
		int  rowNum = 10;
		int columnNum = 0;
		ReportsWriterVb rwTempVb = null;
		List<ReportsWriterVb> listDashBoards = null;
		String isPreviousRightPanelFlag = "";
		String prevDoubleWidthFlag = "";
		String curDoubleWidthFlag = "";
		String ddFlag = "";
		int tempRowNum = 0;
		int tempColumnNum = 0;
		try{
			rwTempVb = (ReportsWriterVb) exceptionCode.getOtherInfo();
			listDashBoards = rwTempVb.getChildren();
			
			if(listDashBoards == null){
				ExceptionCode exceptionCode2 = CommonUtils.getResultObject("", Constants.ERRONEOUS_OPERATION, "Excel - Export", "");
				return exceptionCode2;
			}
			SXSSFWorkbook workBook = new SXSSFWorkbook(500);
			SXSSFSheet sheet = (SXSSFSheet) workBook.createSheet();
			Map<Integer, XSSFCellStyle>  styls = ExcelExportUtil.createStyles(workBook);
			int maxHeaderCount = 0;
			logger.info("Report Export Excel Starts at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
			String assetFolderUrl = ((WebApplicationContext)applicationContext).getServletContext().getRealPath("images");
			reportWriterVb.setMaker(SessionContextHolder.getContext().getVisionId());
			getScreenDao().fetchMakerVerifierNames(reportWriterVb);			
			int cnt = 0;
			int maxRows = 0;
			int chartImgCount = 0;
			int minColumnNumForRightPanel = 8;
			int columnCount = 0; 
			for(ReportsWriterVb vObject : listDashBoards){
				columnCount = columnCount + vObject.getColumnHeaders().size();
			}
			columnCount= columnCount+1;
			for(ReportsWriterVb vObject : listDashBoards){
				if(vObject.getReportsData() != null){
					maxRows = maxRows+vObject.getReportsData().size();
				}
			}
			if((maxRows*2) > 200){
				for(int rowNumberTmpe = 1; rowNumberTmpe <=(maxRows*2); rowNumberTmpe++){
					sheet.createRow(rowNumberTmpe);
				}	
			}else{
				for(int rowNumberTmpe = 1; rowNumberTmpe <=200; rowNumberTmpe++){
					sheet.createRow(rowNumberTmpe);
				}
			}
			ExcelExportUtil.createPrompts(reportWriterVb, prompts, sheet, workBook, assetFolderUrl, styls, maxHeaderCount);
		    for(ReportsWriterVb vObject : listDashBoards){
		    	maxHeaderCount = 0;
		    	RsDashboardVb rasDashboardvbTemp1  = vObject.getDashboards();
		    	curDoubleWidthFlag = rasDashboardvbTemp1.getDoubleWidthFlag();
		    	List<ColumnHeadersVb> columnHeaders = vObject.getColumnHeaders();
				List<ReportStgVb> reportsStgs = vObject.getReportsData();
				List<String> colTypes = new ArrayList<String>();
				Row row = null;
				Cell cell = null;
				String DataURI="";
				if("".equals(columnHeaders) || "null".equals(columnHeaders) || columnHeaders.isEmpty()){
					 continue;
				}
		    	if("".equals(prevDoubleWidthFlag)){
		    		if("Y".equals(curDoubleWidthFlag)){
			    		isPreviousRightPanelFlag="Y";
		    		}else{
			    		isPreviousRightPanelFlag="N";
			    		tempRowNum = rowNum;
		    		}
		    		//Write excel data -> start column=0 and row=10
		    		maxHeaderCount = 6;
		    		if("C".equals(vObject.getDisplayType())){
		    			int colNum = columnNum; 
						int titleRowNum = rowNum-1;
						int titleColumnNum = colNum;
						row = sheet.getRow(titleRowNum);
						cell = row.createCell(colNum);
		    			cell.setCellValue(vObject.getReportDescription());
		    			cell.setCellStyle(styls.get(ExcelExportUtil.CELL_STYLE_TITLES));
		    			int headerCnt = 0;
						if(reportsStgs != null){
							headerCnt = columnHeaders.size();
							if(maxHeaderCount < headerCnt){
								maxHeaderCount = headerCnt;
							}
							Map<Integer,Integer> columnWidths = new HashMap<Integer,Integer>(columnHeaders.size());
							for(int loopCnt= 0; loopCnt < columnHeaders.size(); loopCnt++){
								columnWidths.put(Integer.valueOf(loopCnt), Integer.valueOf(-1));
							}
							if(reportWriterVb.getLabelRowCount() >= 2){rowNum++;}
							columnNum = headerCnt;
							++rowNum; 
						}
						sheet.addMergedRegion(new CellRangeAddress(titleRowNum,titleRowNum,titleColumnNum,maxHeaderCount-1));
						tempColumnNum = columnNum; 
			    		prevDoubleWidthFlag = curDoubleWidthFlag;
						if(imgStringList != null){
							DataURI = imgStringList.get(chartImgCount).replaceFirst("data:image/png;base64,", "");
						}
						int pictureIdx = workBook.addPicture(com.itextpdf.text.pdf.codec.Base64.decode(DataURI), Workbook.PICTURE_TYPE_PNG);
						Drawing drawing = sheet.createDrawingPatriarch();
						XSSFClientAnchor anchor = new XSSFClientAnchor(0,0,0,0,(short)0, 0, (short)getColumnNoForWidth(sheet,columnNum), (short)4);
						anchor.setAnchorType(ClientAnchor.MOVE_DONT_RESIZE);
						anchor.setCol1(0);
						anchor.setRow1(rowNum); 			
						Picture pic = drawing.createPicture(anchor, pictureIdx);
						ClientAnchor pref = pic.getPreferredSize();
						anchor.setDx2(pref.getDx2() + anchor.getDx1());
						anchor.setDy2(pref.getDy2() + anchor.getDy1());
						pic.resize(1);
						int EndRowNum = anchor.getRow2();
		    			chartImgCount++;
		    			if("Y".equals(curDoubleWidthFlag)){
		    				tempRowNum = EndRowNum+4;
		    			}
		    		}else{
							if(reportsStgs != null){
								int headerCnt = ExcelExportUtil.writeHeadersDashBoard(vObject, columnHeaders, rowNum, columnNum, sheet, styls, colTypes);
								if(maxHeaderCount < headerCnt){
									maxHeaderCount = headerCnt;
								}
								if(vObject.getLabelRowCount() >= 2){
									rowNum = rowNum+1;
								}
								Map<Integer,Integer> columnWidths = new HashMap<Integer,Integer>(columnHeaders.size());
								for(int loopCnt= 0; loopCnt < columnHeaders.size(); loopCnt++){
									columnWidths.put(Integer.valueOf(loopCnt), Integer.valueOf(-1));
								}
								rowNum++;
								rowNum = ExcelExportUtil.writeReportDataDashBoard(reportsStgs, reportWriterVb, sheet, rowNum, columnNum, headerCnt, styls, colTypes);
								for(int loopCount=0; loopCount<headerCnt;loopCount++){
									sheet.autoSizeColumn(loopCount, true);
								}
								if(reportWriterVb.getLabelRowCount() >= 2){rowNum++;}
								columnNum = headerCnt;
								++rowNum; 
								if(!"N".equalsIgnoreCase(isPreviousRightPanelFlag))
									tempRowNum = rowNum+2;
								
							}
							tempColumnNum = columnNum; 
				    		prevDoubleWidthFlag = curDoubleWidthFlag;
		    		}
		    	}else{
		    		if("Y".equals(prevDoubleWidthFlag)){
		    			//Write excel data -> start column=0 and row=10
		    			int colNum = 0; 
		    			rowNum = tempRowNum;
		    			int titleRowNum = rowNum-1;
		    			int titleColumnNum = colNum;
						tempColumnNum = colNum;
						columnNum = colNum;
		    			maxHeaderCount = 6;
						if("C".equals(vObject.getDisplayType())){
							row = sheet.getRow(titleRowNum);
							cell = row.createCell(colNum);
			    			cell.setCellValue(vObject.getReportDescription());
			    			cell.setCellStyle(styls.get(ExcelExportUtil.CELL_STYLE_TITLES));
			    			int headerCnt = 0;
							if(reportsStgs != null){
								headerCnt = columnHeaders.size();
								if(maxHeaderCount < headerCnt){
									maxHeaderCount = headerCnt;
								}
								Map<Integer,Integer> columnWidths = new HashMap<Integer,Integer>(columnHeaders.size());
								for(int loopCnt= 0; loopCnt < columnHeaders.size(); loopCnt++){
									columnWidths.put(Integer.valueOf(loopCnt), Integer.valueOf(-1));
								}
								if(reportWriterVb.getLabelRowCount() >= 2){rowNum++;}
								columnNum = headerCnt;
								++rowNum; 
							}
							sheet.addMergedRegion(new CellRangeAddress(titleRowNum,titleRowNum,titleColumnNum,maxHeaderCount-1));
				    		prevDoubleWidthFlag = curDoubleWidthFlag;
							if(imgStringList != null){
								DataURI = imgStringList.get(chartImgCount).replaceFirst("data:image/png;base64,", "");
							}
							int pictureIdx = workBook.addPicture(com.itextpdf.text.pdf.codec.Base64.decode(DataURI), Workbook.PICTURE_TYPE_PNG);
							Drawing drawing = sheet.createDrawingPatriarch();
							XSSFClientAnchor anchor = new XSSFClientAnchor(0,0,0,0,(short)0, 0, (short)getColumnNoForWidth(sheet,maxHeaderCount), (short)4);
							anchor.setAnchorType(ClientAnchor.MOVE_DONT_RESIZE);
							anchor.setCol1(colNum);
							anchor.setRow1(titleRowNum+1); 			
							Picture pic = drawing.createPicture(anchor, pictureIdx);
							ClientAnchor pref = pic.getPreferredSize();
							anchor.setDx2(pref.getDx2() + anchor.getDx1());
							anchor.setDy2(pref.getDy2() + anchor.getDy1());
							pic.resize(1);
							int EndRowNum = anchor.getRow2();
			    			chartImgCount++;
			    			if("Y".equals(curDoubleWidthFlag)){
			    				tempRowNum = EndRowNum+4;
			    			}else{
			    				if(colNum < minColumnNumForRightPanel){
			    					tempColumnNum = minColumnNumForRightPanel;	
			    				}else{
			    					tempColumnNum = colNum;
			    				}
			    			}
			    			 
			    		}else{
								if(reportsStgs != null){
									int headerCnt = ExcelExportUtil.writeHeadersDashBoard(vObject, columnHeaders, rowNum, columnNum, sheet, styls, colTypes);
									if(maxHeaderCount < headerCnt){
										maxHeaderCount = headerCnt;
									}
									if(vObject.getLabelRowCount() >= 2){
										rowNum = rowNum+1;
									}
									Map<Integer,Integer> columnWidths = new HashMap<Integer,Integer>(columnHeaders.size());
									for(int loopCnt= 0; loopCnt < columnHeaders.size(); loopCnt++){
										columnWidths.put(Integer.valueOf(loopCnt), Integer.valueOf(-1));
									}
									rowNum++;
									rowNum = ExcelExportUtil.writeReportDataDashBoard(reportsStgs, reportWriterVb, sheet, rowNum, columnNum, headerCnt, styls, colTypes);
									for(int loopCount=0; loopCount<headerCnt;loopCount++){
										sheet.autoSizeColumn(loopCount, true);
									}
									if(reportWriterVb.getLabelRowCount() >= 2){rowNum++;}
									columnNum = headerCnt;
									++rowNum; 
								}
								tempRowNum = rowNum+2;
								tempColumnNum = columnNum; 
			    		}
			    		prevDoubleWidthFlag = curDoubleWidthFlag;
			    		if("Y".equals(curDoubleWidthFlag)){
			    			isPreviousRightPanelFlag ="Y";
						}else{
							isPreviousRightPanelFlag ="N";
						}
		    		}else if("N".equals(prevDoubleWidthFlag) && "Y".equals(isPreviousRightPanelFlag)){
		    			//Write excel data -> start column=0 and row=10
			    			if("Y".equals(curDoubleWidthFlag)){
			    				columnNum = 0;		    				
			    			}else{
			    				columnNum = tempColumnNum;
			    			}
			    			rowNum = tempRowNum-1;
			    			tempRowNum = rowNum; 
			    			maxHeaderCount = 6;
							if("C".equals(vObject.getDisplayType())){
								int colNum = columnNum; 
				    			int titleRowNum = rowNum-1;
				    			int titleColumnNum = colNum;
				    			row = sheet.getRow(rowNum);
				    			cell = row.createCell(colNum);
				    			cell.setCellValue(vObject.getReportDescription());
				    			cell.setCellStyle(styls.get(ExcelExportUtil.CELL_STYLE_TITLES));
				    			int headerCnt = 0;
								if(reportsStgs != null){
									headerCnt = columnHeaders.size();
									if(maxHeaderCount < headerCnt){
										maxHeaderCount = headerCnt;
									}
									Map<Integer,Integer> columnWidths = new HashMap<Integer,Integer>(columnHeaders.size());
									for(int loopCnt= 0; loopCnt < columnHeaders.size(); loopCnt++){
										columnWidths.put(Integer.valueOf(loopCnt), Integer.valueOf(-1));
									}
									if(reportWriterVb.getLabelRowCount() >= 2){rowNum++;}
									columnNum = headerCnt;
									++rowNum; 
								}
								sheet.addMergedRegion(new CellRangeAddress(tempRowNum,tempRowNum,titleColumnNum,maxHeaderCount-1));
								if(imgStringList != null){
									DataURI = imgStringList.get(chartImgCount).toString();
									DataURI = DataURI.replaceFirst("data:image/png;base64,", "");
								}
								int pictureIdx = workBook.addPicture(com.itextpdf.text.pdf.codec.Base64.decode(DataURI), Workbook.PICTURE_TYPE_PNG);
								Drawing drawing = sheet.createDrawingPatriarch();
								XSSFClientAnchor anchor = new XSSFClientAnchor(0,0,0,0,(short)0, 0, (short)getColumnNoForWidth(sheet,maxHeaderCount), (short)4);
								anchor.setAnchorType(ClientAnchor.MOVE_DONT_RESIZE);
								anchor.setCol1(colNum);
								anchor.setRow1(rowNum); 			
								Picture pic = drawing.createPicture(anchor, pictureIdx);
								ClientAnchor pref = pic.getPreferredSize();
								anchor.setDx2(pref.getDx2() + anchor.getDx1());
								anchor.setDy2(pref.getDy2() + anchor.getDy1());
								pic.resize(1);
								int EndRowNum = anchor.getRow2();
				    			chartImgCount++;
				    			
								if("Y".equals(curDoubleWidthFlag)){
									tempRowNum = EndRowNum+4;
				    			}else{
				    				if(colNum < minColumnNumForRightPanel){
				    					tempColumnNum = minColumnNumForRightPanel;	
				    				}else{
				    					tempColumnNum = colNum;
				    				}
				    			}
						}else{
							if(reportsStgs != null){
								int headerCnt = ExcelExportUtil.writeHeadersDashBoard(vObject, columnHeaders, rowNum, columnNum, sheet, styls, colTypes);
								if(maxHeaderCount < headerCnt){
									maxHeaderCount = headerCnt;
								}
								if(vObject.getLabelRowCount() >= 2){
									rowNum = rowNum+1;
								}
								Map<Integer,Integer> columnWidths = new HashMap<Integer,Integer>(columnHeaders.size());
								for(int loopCnt= 0; loopCnt < columnHeaders.size(); loopCnt++){
									columnWidths.put(Integer.valueOf(loopCnt), Integer.valueOf(-1));
								}
								rowNum++;
								rowNum = ExcelExportUtil.writeReportDataDashBoard(reportsStgs, reportWriterVb, sheet, rowNum, columnNum, headerCnt, styls, colTypes);
								for(int loopCount=0; loopCount<headerCnt;loopCount++){
									sheet.autoSizeColumn(loopCount, true);
								}
								if(reportWriterVb.getLabelRowCount() >= 2){rowNum++;}
								columnNum = headerCnt;
								++rowNum; 
							}
						}
						tempColumnNum = columnNum; 
			    		prevDoubleWidthFlag = curDoubleWidthFlag;
			    		isPreviousRightPanelFlag="N";
		    		}else if("N".equals(prevDoubleWidthFlag) && "N".equals(isPreviousRightPanelFlag)){
		    			//Write excel data -> start column=(PreviousColumn end value + 3)  and row=(PreviousDataColumn Start value row)
		    			System.out.println(" N N - tempRowNum : "+tempRowNum);
		    			rowNum = tempRowNum;
		    			if("Y".equals(curDoubleWidthFlag)){
			    			columnNum = 0;
		    			}else{
			    			columnNum = tempColumnNum;
			    			columnNum++;
				    		if(columnNum < minColumnNumForRightPanel){
				    			columnNum = minColumnNumForRightPanel;
				    		}
		    			}
		    			maxHeaderCount = 6;
						if("C".equals(vObject.getDisplayType())){
							int colNum = columnNum; 
			    			int titleRowNum = rowNum-1;
			    			int titleColumnNum = colNum;
			    			row = sheet.getRow(titleRowNum);
			    			cell = row.createCell(colNum);
				    			cell.setCellValue(vObject.getReportDescription());
				    			cell.setCellStyle(styls.get(ExcelExportUtil.CELL_STYLE_TITLES));
				    			int headerCnt = 0;
								if(reportsStgs != null){
									headerCnt = columnHeaders.size();
									if(maxHeaderCount < headerCnt){
										maxHeaderCount = headerCnt;
									}
									Map<Integer,Integer> columnWidths = new HashMap<Integer,Integer>(columnHeaders.size());
									for(int loopCnt= 0; loopCnt < columnHeaders.size(); loopCnt++){
										columnWidths.put(Integer.valueOf(loopCnt), Integer.valueOf(-1));
									}
									if(reportWriterVb.getLabelRowCount() >= 2){rowNum++;}
									columnNum = headerCnt;
									++rowNum; 
								}
								maxHeaderCount = titleColumnNum+5;
								sheet.addMergedRegion(new CellRangeAddress(titleRowNum,titleRowNum,titleColumnNum,maxHeaderCount-1));
								if(imgStringList != null){
									DataURI = imgStringList.get(chartImgCount).toString();
									DataURI = DataURI.replaceFirst("data:image/png;base64,", "");
								}
								int pictureIdx = workBook.addPicture(com.itextpdf.text.pdf.codec.Base64.decode(DataURI), Workbook.PICTURE_TYPE_PNG);
								Drawing drawing = sheet.createDrawingPatriarch();
								XSSFClientAnchor anchor = new XSSFClientAnchor(0,0,0,0,(short)0, 0, (short)getColumnNoForWidth(sheet,titleColumnNum), (short)4);
								anchor.setAnchorType(ClientAnchor.MOVE_DONT_RESIZE);
								anchor.setCol1(titleColumnNum);
								anchor.setRow1(tempRowNum+1);
								Picture pic = drawing.createPicture(anchor, pictureIdx);
								ClientAnchor pref = pic.getPreferredSize();
								anchor.setDx2(pref.getDx2() + anchor.getDx1());
								anchor.setDy2(pref.getDy2() + anchor.getDy1());
								pic.resize(1);
								int EndRowNum = anchor.getRow2();
				    			chartImgCount++; 
								tempRowNum = EndRowNum+4;
								tempColumnNum = 0;
						}else{
							if(reportsStgs != null){
								int headerCnt = ExcelExportUtil.writeHeadersDashBoard(vObject, columnHeaders, rowNum, columnNum, sheet, styls, colTypes);
								if(columnNum < headerCnt){
									headerCnt = headerCnt - columnNum;  
								}
								if(maxHeaderCount < headerCnt){
									maxHeaderCount = headerCnt;
								}
								if(vObject.getLabelRowCount() >= 2){
									rowNum = rowNum+1;
								}
								Map<Integer,Integer> columnWidths = new HashMap<Integer,Integer>(columnHeaders.size());
								for(int loopCnt= 0; loopCnt < columnHeaders.size(); loopCnt++){
									columnWidths.put(Integer.valueOf(loopCnt), Integer.valueOf(-1));
								}
								rowNum++;
								rowNum = ExcelExportUtil.writeReportDataDashBoard(reportsStgs, reportWriterVb, sheet, rowNum, columnNum, headerCnt, styls, colTypes);
								for(int loopCount=0; loopCount<headerCnt;loopCount++){
									sheet.autoSizeColumn(columnNum+loopCount, true);
								}
								if(reportWriterVb.getLabelRowCount() >= 2){rowNum++;}
								columnNum = headerCnt;
								++rowNum; 
							}
							tempRowNum = rowNum+2;
							tempColumnNum = 0;
			    		}
						prevDoubleWidthFlag = curDoubleWidthFlag;
			    		isPreviousRightPanelFlag="Y";
		    		}
		    	}	    	
				cnt++;
			}// End For Loop
		    for(int loopCount=0; loopCount<=columnCount;loopCount++){
		    	sheet.autoSizeColumn(loopCount, true);				
		    	//System.out.println("Beofe Column ["+loopCount+"] Width:"+sheet.getColumnWidth(loopCount)); 
				sheet.setColumnWidth(loopCount, (sheet.getColumnWidth(loopCount)+1000));
				//System.out.println("After Column ["+loopCount+"] Width:"+sheet.getColumnWidth(loopCount));
			}
			workBook.setPrintArea(workBook.getActiveSheetIndex(), 0, maxHeaderCount +1, 0, rowNum +1);
			String filePath = System.getProperty("java.io.tmpdir");
			if(!ValidationUtil.isValid(filePath)){
				filePath = System.getenv("TMP");
			}
			if(ValidationUtil.isValid(filePath)){
				filePath = filePath + File.separator;
			}
			File lFile = new File(filePath+ValidationUtil.encode(rwTempVb.getReportTitle())+"_"+currentUserId+".xlsx");
			if(lFile.exists()){
				lFile.delete();
			}
			lFile.createNewFile();
			FileOutputStream fileOS = new FileOutputStream(lFile);
			workBook.write(fileOS);
			fileOS.flush();
			fileOS.close();
			logger.info("Report Export Excel End at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Report Export Excel Exception at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
			throw new RuntimeCustomException(e.getMessage());
		}
		return CommonUtils.getResultObject("", 1, "", "");
	}
	private static int getColumnNoForWidth(Sheet sheet, int headerCnt) {
		float width = 0;
		for(int loopCnt =0 ; loopCnt<headerCnt; loopCnt++){
			width += sheet.getColumnWidth(loopCnt);
			if(width >=32000){
				return loopCnt;
			}
		}
		return headerCnt;
	}	

	public ExceptionCode imagesExportToXls(ReportsWriterVb reportWriterVb, int currentUserId,List<String> imgStringList){
		int  rowNum = 10;
		int columnNum = 0;
		List<ReportsWriterVb> listDashBoards = null;
		String endFlag = "";
		String prevFlag = "";
		String curFlag = "";
		String ddFlag = "";
		int tempRowNum = 0;
		int tempColumnNum = 0;
		try{
			SXSSFWorkbook workBook = new SXSSFWorkbook(500);
			SXSSFSheet sheet = (SXSSFSheet) workBook.createSheet();
			Map<Integer, XSSFCellStyle>  styls = ExcelExportUtil.createStyles(workBook);
			int maxHeaderCount = 0;
			logger.info("Report Export Excel Starts at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
			String assetFolderUrl = ((WebApplicationContext)applicationContext).getServletContext().getRealPath("images");
			reportWriterVb.setMaker(SessionContextHolder.getContext().getVisionId());
			getScreenDao().fetchMakerVerifierNames(reportWriterVb);			
			int cnt = 0;
			int maxRows = 100;
			Row row = null;
			Cell cell = null;
			int chartImgCount = 0;
			int minColumnNumForRightPanel = 8;
			
			if((maxRows*2) > 200){
				for(int rowNumberTmpe = 1; rowNumberTmpe <=(maxRows*2); rowNumberTmpe++){
					sheet.createRow(rowNumberTmpe);
				}	
			}else{
				for(int rowNumberTmpe = 1; rowNumberTmpe <=200; rowNumberTmpe++){
					sheet.createRow(rowNumberTmpe);
				}
			}
//			ExcelExportUtil.createPrompts(reportWriterVb, prompts, sheet, workBook, assetFolderUrl, styls, maxHeaderCount);
		    for(int loopCount =0 ; loopCount<imgStringList.size(); loopCount++){
		    	String DataURI = "";
				int colNum = columnNum; 
    			int titleRowNum = rowNum-1;
    			int titleColumnNum = colNum+2;
    			//row = sheet.getRow(titleRowNum);
    			//cell = row.createCell(colNum);
	    			//cell.setCellValue("Title");
	    			//cell.setCellStyle(styls.get(ExcelExportUtil.CELL_STYLE_TITLES));
	    			int headerCnt = 0;
					maxHeaderCount = titleColumnNum+5;
					sheet.addMergedRegion(new CellRangeAddress(titleRowNum,titleRowNum,titleColumnNum,maxHeaderCount-1));
					DataURI = imgStringList.get(loopCount).toString();
					DataURI = DataURI.replaceFirst("data:image/png;base64,", "");
					int pictureIdx = workBook.addPicture(com.itextpdf.text.pdf.codec.Base64.decode(DataURI), Workbook.PICTURE_TYPE_PNG);
					Drawing drawing = sheet.createDrawingPatriarch();
					XSSFClientAnchor anchor = new XSSFClientAnchor(0,0,0,0,(short)0, 0, (short)getColumnNoForWidth(sheet,titleColumnNum), (short)4);
					anchor.setAnchorType(ClientAnchor.MOVE_DONT_RESIZE);
					anchor.setCol1(titleColumnNum);
					anchor.setRow1(tempRowNum+1);
					Picture pic = drawing.createPicture(anchor, pictureIdx);
					ClientAnchor pref = pic.getPreferredSize();
					anchor.setDx2(pref.getDx2() + anchor.getDx1());
					anchor.setDy2(pref.getDy2() + anchor.getDy1());
					pic.resize(1);
					int EndRowNum = anchor.getRow2();
					tempRowNum = EndRowNum+4;
					tempColumnNum = 0;
					rowNum = rowNum+10;
		    }
			
			workBook.setPrintArea(workBook.getActiveSheetIndex(), 0, maxHeaderCount +1, 0, rowNum +1);
			String filePath = System.getProperty("java.io.tmpdir");
			if(!ValidationUtil.isValid(filePath)){
				filePath = System.getenv("TMP");
			}
			if(ValidationUtil.isValid(filePath)){
				filePath = filePath + File.separator;
			}
			File lFile = new File(filePath+ValidationUtil.encode(reportWriterVb.getReportTitle())+"_"+currentUserId+".xlsx");
			if(lFile.exists()){
				lFile.delete();
			}
			lFile.createNewFile();
			FileOutputStream fileOS = new FileOutputStream(lFile);
			workBook.write(fileOS);
			fileOS.flush();
			fileOS.close();
			logger.info("Report Export Excel End at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Report Export Excel Exception at: " + reportWriterVb.getReportId()+" : "+reportWriterVb.getReportTitle());
			throw new RuntimeCustomException(e.getMessage());
		}
		return CommonUtils.getResultObject("", 1, "", "");
	}
	
	public ExceptionCode exportDashboardToPdf(List<String> imageData, ReportsWriterVb reportWriterVb, List<PromptIdsVb> prompts){
		try {
			List<String> imageDataNew = new ArrayList<>();
			PDFExportUtil exportUtil = new PDFExportUtil();
			for(String imgDat:imageData){
				String expoString0[] = imgDat.split(",");
				String imageDataList = expoString0[1];
				imageDataNew.add(imageDataList);
			}
			String imageFolderUrl = ((WebApplicationContext)applicationContext).getServletContext().getRealPath("images");
			reportWriterVb.setMaker(SessionContextHolder.getContext().getVisionId());
			getScreenDao().fetchMakerVerifierNames(reportWriterVb);
			exportUtil.exportToPdf4DashboardMultiple(imageDataNew, reportWriterVb, prompts, imageFolderUrl);
		} catch (Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
		return CommonUtils.getResultObject("", 1, "", "");
	}
	
	
}