package com.vision.wb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.vision.dao.CommonDao;
import com.vision.dao.ReportWriterDao;
import com.vision.dao.SbiReportGenerationDao;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.DeepCopy;
import com.vision.util.ValidationUtil;
import com.vision.vb.AlphaSubTabVb;
import com.vision.vb.DCManualQueryVb;
import com.vision.vb.PromptIdsVb;
import com.vision.vb.PromptTreeVb;
import com.vision.vb.SbiReportPromptsVb;
import com.vision.vb.VcReportGenerationVb;

import edu.emory.mathcs.backport.java.util.Arrays;

@Component
public class SbiReportGenerationWb {

	@Autowired
	SbiReportGenerationDao sbiReportGenerationDao;

	@Autowired
	ReportWriterDao reportWriterDao;

	@Autowired
	CommonDao commonDao;

	/* Fetch Prompt XMl and Parse the value in VB */
	public ExceptionCode getVrdPromptData(VcReportGenerationVb vObject)
			throws ParserConfigurationException, SAXException, IOException {
		ExceptionCode exceptionCode = new ExceptionCode();
		SbiReportPromptsVb promptsVb = new SbiReportPromptsVb();
		try {
			/* Fetch Prompt Record from VRD_PROMPT_DESIGN */
			promptsVb = sbiReportGenerationDao.getVrdPromptDesign(vObject);

			if (promptsVb != null) {
				String promptPropertiesXml = promptsVb.getPromptXmlContent();

				String autoSubmit = CommonUtils.getValueForXmlTag(promptPropertiesXml, "auto_submit");
				promptsVb.setAutoSubmitFlag(
						(ValidationUtil.isValid(autoSubmit) && "Y".equalsIgnoreCase(autoSubmit)) ? true : false);

				String scalingFactor = CommonUtils.getValueForXmlTag(promptPropertiesXml, "scaling_factor");
				promptsVb.setScalingFactorFlag(
						(ValidationUtil.isValid(scalingFactor) && "Y".equalsIgnoreCase(scalingFactor)) ? true : false);

				Pattern promptPattern = Pattern.compile("<promptValue>(.*?)</promptValue>");
				Matcher promptMatcher = promptPattern.matcher(promptPropertiesXml);

				List<SbiReportPromptsVb> children = new ArrayList<SbiReportPromptsVb>();
				while (promptMatcher.find()) {
					SbiReportPromptsVb promptsPrpVb = new SbiReportPromptsVb();
					String promptXml = promptMatcher.group(1);
					promptsPrpVb.setPromptKey(CommonUtils.getValueForXmlTag(promptXml, "prompt"));
					promptsPrpVb.setPromptLabel(CommonUtils.getValueForXmlTag(promptXml, "label"));
					promptsPrpVb
							.setPromptSort(Integer.parseInt(CommonUtils.getValueForXmlTag(promptXml, "sort_order")));
					promptsPrpVb.setPromptCategory(CommonUtils.getValueForXmlTag(promptXml, "prompt_category"));
					promptsPrpVb.setPromptType(CommonUtils.getValueForXmlTag(promptXml, "prompt_type"));
					promptsPrpVb.setPromptId(CommonUtils.getValueForXmlTag(promptXml, "prompt_id"));
					promptsPrpVb.setPromptUseValueColumn(CommonUtils.getValueForXmlTag(promptXml, "use_column"));
					promptsPrpVb
							.setPromptDisplayValueColumn(CommonUtils.getValueForXmlTag(promptXml, "display_column"));
					promptsPrpVb.setHashVariable(CommonUtils.getValueForXmlTag(promptXml, "hash_arr"));
					promptsPrpVb.setHashValue(CommonUtils.getValueForXmlTag(promptXml, "hash_arrval"));

					if ("M".equalsIgnoreCase(promptsPrpVb.getPromptCategory())) {
						getManualPromptData(promptsPrpVb);
					} else {
						getDefaultPromptData(promptsPrpVb);
					}
					children.add(promptsPrpVb);
				}
				promptsVb.setChildren(children);

				exceptionCode = CommonUtils.getResultObject("VRD Prompt Design", Constants.SUCCESSFUL_OPERATION,
						"Query", "");
				exceptionCode.setOtherInfo(vObject);
				exceptionCode.setRequest(promptsVb);
			} else {
				exceptionCode.setErrorCode(404);
				exceptionCode.setErrorMsg("Report ID does not exist in VRD Prompt Design");
				exceptionCode.setRequest(vObject);
				exceptionCode.setOtherInfo(vObject);
			}
			return exceptionCode;
		} catch (RuntimeCustomException rex) {
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg("Fetching prompt data - failed - Cause:" + rex.getMessage());
			return exceptionCode;
		} catch (Exception e) {
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg("Fetching prompt data - failed - Cause:" + e.getMessage());
			return exceptionCode;
		}
	}

	public void getManualPromptData(SbiReportPromptsVb promptsPrpVb) throws SQLException, Exception {
		if ("COMBO".equalsIgnoreCase(promptsPrpVb.getPromptType())
				|| "MAGNIFIER".equalsIgnoreCase(promptsPrpVb.getPromptType())) {
			String[] otherColTag = promptsPrpVb.getHashVariable().split("@~@,");
			String[] hashArr = new String[otherColTag.length];
			for (int h = 0; h < otherColTag.length; h++) {
				hashArr[h] = otherColTag[h].replaceAll("@~@", "");
			}
			String[] otherColValue = promptsPrpVb.getHashValue().split("@~@,");
			String[] hashValArr = new String[otherColValue.length];
			for (int h = 0; h < otherColValue.length; h++) {
				hashValArr[h] = otherColValue[h].replaceAll("@~@", "");
			}

			List<DCManualQueryVb> promptsQ = null;
			promptsQ = sbiReportGenerationDao.findActiveVcQueries(promptsPrpVb.getPromptId());

			DCManualQueryVb vObjDesign = new DCManualQueryVb();
			vObjDesign.setDatabaseConnectivityDetails(promptsQ.get(0).getDatabaseConnectivityDetails());
			vObjDesign.setSqlQuery(promptsQ.get(0).getSqlQuery());
			vObjDesign.setStgQuery1(promptsQ.get(0).getStgQuery1());
			vObjDesign.setStgQuery2(promptsQ.get(0).getStgQuery2());
			vObjDesign.setStgQuery3(promptsQ.get(0).getStgQuery3());
			vObjDesign.setPostQuery(promptsQ.get(0).getPostQuery());
			vObjDesign.setPostQuery(promptsQ.get(0).getPostQuery());

			String dbScript = commonDao.getScriptValue(vObjDesign.getDatabaseConnectivityDetails());

			promptsPrpVb.setPromptResponse(validateSqlQuery(vObjDesign, dbScript, hashArr, hashValArr,
					promptsPrpVb.getPromptUseValueColumn(), promptsPrpVb.getPromptDisplayValueColumn()));

		} else if ("CALENDAR".equalsIgnoreCase(promptsPrpVb.getPromptType())) {
			/*
			 * We have range to get least and greatest date from vision business day
			 */
			String[] manualPrompt = promptsPrpVb.getPromptId().split("@-@");
			String manualCalendarRange = sbiReportGenerationDao.getManualCalendarRange(manualPrompt[0],
					manualPrompt[1]);
			String[] manual = manualCalendarRange.split("@-@");
			// promptsPrpVb.setPromptResponse(Arrays.asList(manual));
			List<AlphaSubTabVb> promptDataList = new ArrayList<AlphaSubTabVb>();
			for (int i = 0; i < manual.length; i++) {
				AlphaSubTabVb promptVb = new AlphaSubTabVb();
				promptVb.setAlphaSubTab(manual[i]);
				promptVb.setDescription(manual[i]);
				promptDataList.add(promptVb);
			}
			promptsPrpVb.setPromptResponse(promptDataList);
		}

	}

	public void getDefaultPromptData(SbiReportPromptsVb promptsPrpVb) throws SQLException, Exception {
		List<PromptIdsVb> prompts = reportWriterDao.getQueryForPrompts(promptsPrpVb.getPromptId());
		if (prompts != null && !prompts.isEmpty()) {
			PromptIdsVb prompt = prompts.get(0);
			if (ValidationUtil.isValid(prompt.getPromptLogic())
					&& "DYNAMIC".equalsIgnoreCase(prompt.getPromptLogic())) {
				prompt.setCascadePrompt(true);
			}
			List<AlphaSubTabVb> promptTree = null;
			List<PromptTreeVb> promptTreeData = null;

			if (ValidationUtil.isValid(prompt.getPromptLogic())
					&& "DYNAMIC".equalsIgnoreCase(prompt.getPromptLogic())) {
				// PromptTreeVb promptInputVb = new PromptTreeVb();
				// promptTree = reportWriterDao.getCascadePromptData(prompt, promptInputVb,
				// vObj);
			} else if ("COMBO".equalsIgnoreCase(prompt.getPromptType())) {
				promptTree = sbiReportGenerationDao.getComboPromptData(prompt, null);
			} else if ("LABEL".equalsIgnoreCase(prompt.getPromptType())) {
				promptTree = sbiReportGenerationDao.getComboPromptData(prompt, null);
				if (promptTree != null && !promptTree.isEmpty()) {
					// prompt.setSelectedValue1(promptTree.get(0));
				}
			} else if ("CALENDAR".equalsIgnoreCase(prompt.getPromptType())) {
				promptTree = sbiReportGenerationDao.getComboPromptData(prompt, null);
				if (promptTree != null && !promptTree.isEmpty()) {
					// prompt.setSelectedValue1(promptTree.get(0));
				}
			}
			promptsPrpVb.setPromptResponse(promptTree);
			if ("TREE".equalsIgnoreCase(prompt.getPromptType())) {
				promptTreeData = sbiReportGenerationDao.getTreePromptData(prompt);
				promptTreeData = createParentChildRelations(promptTreeData, prompt.getFilterStr());
				promptsPrpVb.setPromptResponse(promptTreeData);
			}
		}
	}

	public List validateSqlQuery(DCManualQueryVb vObj, String dbScript, String[] hashArr, String[] hashValArr,
			String useCol, String disCol) throws SQLException, Exception {
		String level = "";
		String dbSetParam1 = CommonUtils.getValueForXmlTag(dbScript, "DB_SET_PARAM1");
		String dbSetParam2 = CommonUtils.getValueForXmlTag(dbScript, "DB_SET_PARAM2");
		String dbSetParam3 = CommonUtils.getValueForXmlTag(dbScript, "DB_SET_PARAM3");
		String stgQuery = "";
		String sessionId = ValidationUtil.generateRandomNumberForVcReport();
		String stgTableName1 = "TVC_" + sessionId + "_STG_1";
		String stgTableName2 = "TVC_" + sessionId + "_STG_2";
		String stgTableName3 = "TVC_" + sessionId + "_STG_3";
		String sqlMainQuery = "";
		try (Connection con = CommonUtils.getDBConnection(dbScript);
				Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

			if (ValidationUtil.isValid(dbSetParam1)) {
				level = "DB Param 1";
				stmt.executeUpdate(dbSetParam1);
			}
			if (ValidationUtil.isValid(dbSetParam2)) {
				level = "DB Param 2";
				stmt.executeUpdate(dbSetParam2);
			}
			if (ValidationUtil.isValid(dbSetParam3)) {
				level = "DB Param 3";
				stmt.executeUpdate(dbSetParam3);
			}
			Pattern pattern = Pattern.compile("#(.*?)#");
			Matcher matcher = null;
			if (ValidationUtil.isValid(vObj.getStgQuery1())) {
				stgQuery = vObj.getStgQuery1();
				matcher = pattern.matcher(stgQuery);
				while (matcher.find()) {
					if ("TVC_SESSIONID_STG_1".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName1);
					if ("TVC_SESSIONID_STG_2".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName2);
					if ("TVC_SESSIONID_STG_3".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName3);
				}
				level = "Staging 1";
				stgQuery = CommonUtils.replaceHashTag(stgQuery, hashArr, hashValArr);
				stmt.executeUpdate(stgQuery);
			}
			if (ValidationUtil.isValid(vObj.getStgQuery2())) {
				stgQuery = vObj.getStgQuery2();
				matcher = pattern.matcher(stgQuery);
				while (matcher.find()) {
					if ("TVC_SESSIONID_STG_1".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName1);
					if ("TVC_SESSIONID_STG_2".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName2);
					if ("TVC_SESSIONID_STG_3".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName3);
				}
				level = "Staging 2";
				stgQuery = CommonUtils.replaceHashTag(stgQuery, hashArr, hashValArr);
				stmt.executeUpdate(stgQuery);
			}
			if (ValidationUtil.isValid(vObj.getStgQuery3())) {
				stgQuery = vObj.getStgQuery3();
				matcher = pattern.matcher(stgQuery);
				while (matcher.find()) {
					if ("TVC_SESSIONID_STG_1".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName1);
					if ("TVC_SESSIONID_STG_2".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName2);
					if ("TVC_SESSIONID_STG_3".equalsIgnoreCase(matcher.group(1)))
						stgQuery = stgQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName3);
				}
				level = "Staging 3";
				stgQuery = CommonUtils.replaceHashTag(stgQuery, hashArr, hashValArr);
				stmt.executeUpdate(stgQuery);
			}
			sqlMainQuery = vObj.getSqlQuery();
			matcher = pattern.matcher(sqlMainQuery);
			while (matcher.find()) {
				if ("TVC_SESSIONID_STG_1".equalsIgnoreCase(matcher.group(1)))
					sqlMainQuery = sqlMainQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName1);
				if ("TVC_SESSIONID_STG_2".equalsIgnoreCase(matcher.group(1)))
					sqlMainQuery = sqlMainQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName2);
				if ("TVC_SESSIONID_STG_3".equalsIgnoreCase(matcher.group(1)))
					sqlMainQuery = sqlMainQuery.replaceAll("#" + matcher.group(1) + "#", stgTableName3);
			}
			level = "Main Query";
			sqlMainQuery = CommonUtils.replaceHashTag(sqlMainQuery, hashArr, hashValArr);
			try (ResultSet rs = stmt.executeQuery(sqlMainQuery);) {
				/* Get used and display column */
				List<AlphaSubTabVb> promptDataList = new ArrayList<AlphaSubTabVb>();
				while (rs.next()) {
					AlphaSubTabVb promptVb = new AlphaSubTabVb();
					promptVb.setAlphaSubTab(rs.getString(useCol));
					promptVb.setDescription(rs.getString(disCol));
					promptDataList.add(promptVb);
				}
				return promptDataList;
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public List<PromptTreeVb> createParentChildRelations(List<PromptTreeVb> promptTreeList, String filterString) {
		DeepCopy<PromptTreeVb> deepCopy = new DeepCopy<PromptTreeVb>();
		List<PromptTreeVb> lResult = new ArrayList<PromptTreeVb>(0);
		List<PromptTreeVb> promptTreeListCopy = new CopyOnWriteArrayList<PromptTreeVb>(
				deepCopy.copyCollection(promptTreeList));
		// Top Roots are added.
		for (PromptTreeVb promptVb : promptTreeListCopy) {
			if (promptVb.getField1().equalsIgnoreCase(promptVb.getField3())) {
				lResult.add(promptVb);
				promptTreeListCopy.remove(promptVb);
			}
		}
		// For each top node add all child's and to that child's add sub child's
		// recursively.
		for (PromptTreeVb promptVb : lResult) {
			addChilds(promptVb, promptTreeListCopy);
		}
		// Get the sub tree from the filter string if filter string is not null.
		if (ValidationUtil.isValid(filterString)) {
			lResult = getSubTreeFrom(filterString, lResult);
		}
		// set the empty lists to null. this is required for UI to display the leaf
		// nodes properly.
		nullifyEmptyList(lResult);
		return lResult;
	}

	private void addChilds(PromptTreeVb vObject, List<PromptTreeVb> promptTreeListCopy) {
		for (PromptTreeVb promptTreeVb : promptTreeListCopy) {
			if (vObject.getField1().equalsIgnoreCase(promptTreeVb.getField3())) {
				if (vObject.getChildren() == null) {
					vObject.setChildren(new ArrayList<PromptTreeVb>(0));
				}
				vObject.getChildren().add(promptTreeVb);
				addChilds(promptTreeVb, promptTreeListCopy);
			}
		}
	}

	private List<PromptTreeVb> getSubTreeFrom(String filterString, List<PromptTreeVb> result) {
		List<PromptTreeVb> lResult = new ArrayList<PromptTreeVb>(0);
		for (PromptTreeVb promptTreeVb : result) {
			if (promptTreeVb.getField1().equalsIgnoreCase(filterString)) {
				lResult.add(promptTreeVb);
				return lResult;
			} else if (promptTreeVb.getChildren() != null) {
				lResult = getSubTreeFrom(filterString, promptTreeVb.getChildren());
				if (lResult != null && !lResult.isEmpty())
					return lResult;
			}
		}
		return lResult;
	}

	private void nullifyEmptyList(List<PromptTreeVb> lResult) {
		for (PromptTreeVb promptTreeVb : lResult) {
			if (promptTreeVb.getChildren() != null) {
				nullifyEmptyList(promptTreeVb.getChildren());
			}
			if (promptTreeVb.getChildren() != null && promptTreeVb.getChildren().isEmpty()) {
				promptTreeVb.setChildren(null);
			}
		}
	}

	public ExceptionCode getVcReportGenerationPageDesign(VcReportGenerationVb vObject) {
		ExceptionCode exceptionCode = new ExceptionCode();
		String sessionId = ValidationUtil.generateRandomNumberForVcReport();
		vObject.setSessionId(sessionId);
		exceptionCode.setOtherInfo(vObject);
		VcReportGenerationVb pageDesignVb = sbiReportGenerationDao.getVrdPageDesign(vObject);
		if (pageDesignVb != null) {
			exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
			exceptionCode.setResponse(pageDesignVb);
		} else {
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
		}
		return exceptionCode;
	}

	public ExceptionCode transformDataToRPT(VcReportGenerationVb vObject) {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			VcReportGenerationVb mainReportData = getMainReportData(vObject);
			VcReportGenerationVb subReportData = getSubReportData(vObject);
			if (mainReportData != null || subReportData != null) {

			} else {
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg("Error in report maintenance");
			}
		} catch (Exception e) {
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg(e.getMessage());
		}
		return exceptionCode;
	}

	private VcReportGenerationVb getMainReportData(VcReportGenerationVb vObject) {
		return sbiReportGenerationDao.getVrdMainReportData(vObject);
	}

	private VcReportGenerationVb getSubReportData(VcReportGenerationVb vObject) {
		return sbiReportGenerationDao.getVrdMainReportData(vObject);
	}

	private ExceptionCode transformDataForList(VcReportGenerationVb mainReportData, VcReportGenerationVb subReportData) {
		ExceptionCode exceptionCode = new ExceptionCode();
		return exceptionCode;
	}
}