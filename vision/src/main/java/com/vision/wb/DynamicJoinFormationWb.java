/**
 * @author DD
 */

package com.vision.wb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vision.util.Node;
import com.vision.util.ValidationUtil;

import edu.emory.mathcs.backport.java.util.Collections;

public class DynamicJoinFormationWb{
	
	static HashMap<String,String> linkedTblsForIndividualTblHM = null;
	static HashMap<String,String> relationHM = null;
	static LinkedHashSet<String> directLinkKeysAL = null;
	static ArrayList<String> tableIdArr = new ArrayList<String>();
	static StringBuffer finishedTableIds = new StringBuffer();
	static StringBuffer joinWhereString = new StringBuffer();
	static ArrayList<String> doneTableIdsWithDirectLinkAL = null;
	static boolean isLinkFound = false;
	static boolean isTrackBackLinkCompleted = false;
	static String trackBackLink = "";
	static List<Node<String>> currentLevelChildList = null;
	static Node<String> finalTargetNode = null;
	static List<String> unDoneTableIdsWithDirectLinkAL = null;
	static HashSet<String> externalIncludedTables = new HashSet<String>();
	static String catalogId = "";
	static Integer baseTable = null;
	private static void initializeVariables(){
		linkedTblsForIndividualTblHM = null;
		relationHM = null;
		directLinkKeysAL = null;
		tableIdArr = new ArrayList<String>();
		finishedTableIds = new StringBuffer();
		joinWhereString = new StringBuffer();
		doneTableIdsWithDirectLinkAL = null;
		isLinkFound = false;
		isTrackBackLinkCompleted = false;
		trackBackLink = "";
		currentLevelChildList = null;
		finalTargetNode = null;
		unDoneTableIdsWithDirectLinkAL = null;
		externalIncludedTables = new HashSet<String>();
		catalogId = "";
		baseTable = null;
	}
	
	/*private static void getData(){
		try{
			ArrayList getDataAL = getVcReportGenerationDao().getData(catalogId);
			linkedTblsForIndividualTblHM = (HashMap<String,String>) getDataAL.get(0);
			relationHM = (HashMap<String,String>) getDataAL.get(1);
		}catch(Exception e){
			e.printStackTrace();
		}
	}*/
	private static void checkDirectLinksWithBaseTbl(){
		boolean isDirectLinkAvailable = false;
		directLinkKeysAL = new LinkedHashSet<String>();
		String key = "";
		for(int iIndex=0;iIndex<tableIdArr.size();iIndex++){
			String val1 = tableIdArr.get(iIndex);
			String val2 = String.valueOf(baseTable);
			if(!val1.equalsIgnoreCase(val2)){
				if(relationHM.get(val1+"-"+val2)!=null){
					isDirectLinkAvailable = true;
					key = val1+"-"+val2;
				}else if(relationHM.get(val2+"-"+val1)!=null){
					isDirectLinkAvailable = true;
					key = val2+"-"+val1;
				}else{
					isDirectLinkAvailable = false;
				}
				if(isDirectLinkAvailable){
					directLinkKeysAL.add(key);
				}
			}
		}
	}
	private static void checkDirectLinks(){
		boolean isDirectLinkAvailable = false;
		directLinkKeysAL = new LinkedHashSet<String>();
		String key = "";
		for(int iIndex=0;iIndex<tableIdArr.size();iIndex++){
			String val1 = tableIdArr.get(iIndex);
			for(int jIndex=0;jIndex<tableIdArr.size();jIndex++){
				String val2 = tableIdArr.get(jIndex);
				if(!val1.equalsIgnoreCase(val2)){
					if(relationHM.get(val1+"-"+val2)!=null){
						isDirectLinkAvailable = true;
						key = val1+"-"+val2;
					}else if(relationHM.get(val2+"-"+val1)!=null){
						isDirectLinkAvailable = true;
						key = val2+"-"+val1;
					}else{
						isDirectLinkAvailable = false;
					}
					if(isDirectLinkAvailable){
						directLinkKeysAL.add(key);
					}
				}
			}
		}
	}
	private static void formDirectLinksJoinString(Integer joinType){
//		System.out.println(directLinkKeysAL.size());
		for(String linkKey:directLinkKeysAL){
			String[] keyArr = linkKey.split("-");
			if(finishedTableIds.indexOf(","+keyArr[0]+",")==-1 || finishedTableIds.indexOf(","+keyArr[1]+",")==-1){
				if(finishedTableIds.indexOf(","+keyArr[0]+",")==-1)
					finishedTableIds.append(","+keyArr[0]+",");
				if(finishedTableIds.indexOf(","+keyArr[1]+",")==-1)
					finishedTableIds.append(","+keyArr[1]+",");
				if(!ValidationUtil.isValid(String.valueOf(joinWhereString))){
					joinWhereString.append(relationHM.get(linkKey));
				} else {
					if(joinType==1) {
						joinWhereString.append(relationHM.get(linkKey));
					} else {
						joinWhereString.append(" AND "+relationHM.get(linkKey));
					}
				}
			}
		}
//		System.out.println(joinWhereString);
		if(ValidationUtil.isValid(String.valueOf(finishedTableIds))){
			doneTableIdsWithDirectLinkAL = new ArrayList<String>();
			String[] finishedTableIdsArr = finishedTableIds.toString().split(",");
			for(String tableId:finishedTableIdsArr){
				if(ValidationUtil.isValid(tableId)){
					doneTableIdsWithDirectLinkAL.add(tableId);
				}
			}
		}
	}
	private static String createLinkForUndoneTblIds(String tableId){
		HashMap<String, String> linkListHM = new HashMap<String, String>();
		int count=0;
		String finalLink = "";
		if(doneTableIdsWithDirectLinkAL!=null && doneTableIdsWithDirectLinkAL.size()>0){
			for(String pairTblId:doneTableIdsWithDirectLinkAL){
				trackBackLink = "";
				isLinkFound = false;
				isTrackBackLinkCompleted = false;
				finalTargetNode = null;
				Node<String> root = new Node<String>(tableId);
				if(tableId!=pairTblId){
					Node<String> targetNode = findLinkForNode(root, pairTblId);
					if(targetNode!=null){
						trackBackForNode(targetNode);
						linkListHM.put(pairTblId, trackBackLink);
					}
//					System.out.println("With Direct Link from "+tableId+" to "+pairTblId+" - ["+trackBackLink+"]");
//					System.out.println("---------------");
				}
			}
		}
		if(!isLinkFound){
			if(baseTable==null){
				for(String pairTblId:unDoneTableIdsWithDirectLinkAL){
					if(!isExistInExternalIncludedTbl(tableId)){
						trackBackLink = "";
						isLinkFound = false;
						isTrackBackLinkCompleted = false;
						finalTargetNode = null;
						Node<String> root = new Node<String>(tableId);
						if(tableId!=pairTblId){
							Node<String> targetNode = findLinkForNode(root, pairTblId);
							if(targetNode!=null){
								trackBackForNode(targetNode);
								linkListHM.put(pairTblId, trackBackLink);
							}
//							System.out.println("Not with Direct Link from "+tableId+" to "+pairTblId+" - ["+trackBackLink+"]");
//							System.out.println("---------------");
						}
					}
				}
			}else{
				if(!isExistInExternalIncludedTbl(tableId)){
					trackBackLink = "";
					isLinkFound = false;
					isTrackBackLinkCompleted = false;
					finalTargetNode = null;
					Node<String> root = new Node<String>(tableId);
					if(tableId!=String.valueOf(baseTable)){
						Node<String> targetNode = findLinkForNode(root, String.valueOf(baseTable));
						if(targetNode!=null){
							trackBackForNode(targetNode);
							linkListHM.put(String.valueOf(baseTable), trackBackLink);
						}
//						System.out.println("Not with Direct Link from "+tableId+" to "+pairTblId+" - ["+trackBackLink+"]");
//						System.out.println("---------------");
					}
				}
			}
		}
		Iterator linkListItr = linkListHM.entrySet().iterator();
		while(linkListItr.hasNext()){
			Map.Entry mapEntry = (Map.Entry)linkListItr.next();
			String pairedTableId = (String) mapEntry.getKey();
			String link = (String) mapEntry.getValue();
			int localCount = StringUtils.countMatches(link, "-");
			if(count!=0){
				if(localCount<count){
					count = localCount;
					finalLink = link;
				}
			}else{
				count = localCount;
				finalLink = link;
			}
		}
		if(ValidationUtil.isValid(finalLink) && finalLink.indexOf("-")!=-1){
			String externalIncludedArray[] = finalLink.split("-");
			if(externalIncludedArray.length>0){
				externalIncludedTables.addAll(Arrays.asList(externalIncludedArray));
				if(doneTableIdsWithDirectLinkAL!=null)
					externalIncludedTables.removeAll(doneTableIdsWithDirectLinkAL);
			}
		}
		return finalLink;
	}
	private static Node<String> findLinkForNode(Node<String> node, String destTblId){
		Node<String> targetChild = null;
		addChildForNode(node);
		List<Node<String>> childNodeList = node.getChildren();
		for(Node<String> each:childNodeList){
			if(!isLinkFound && each.getData().equalsIgnoreCase(destTblId)){
				isLinkFound = true;
				finalTargetNode = each;
			}
		}
		if(!isLinkFound){
			currentLevelChildList = node.getChildren();
			findLinkForNodeList(destTblId);
			targetChild = finalTargetNode;
		}else{
			targetChild = finalTargetNode;
		}
		return targetChild;
	}
	private static void findLinkForNodeList(String destTblId){
		List<Node<String>> localCurrentLevelChildList = new ArrayList<Node<String>>();
		addChildForNodeList(currentLevelChildList);
		for(Node<String> eachParent:currentLevelChildList){
			List<Node<String>> childNode = eachParent.getChildren();
			for(Node<String> eachChild:childNode){
				if(!isLinkFound && eachChild.getData().equalsIgnoreCase(destTblId)){
					isLinkFound = true;
					finalTargetNode = eachChild;
				}
				localCurrentLevelChildList.add(eachChild);
			}
		}
		if(!isLinkFound && localCurrentLevelChildList.size()>0){
			currentLevelChildList = localCurrentLevelChildList;
			findLinkForNodeList(destTblId);
		}
	}
	private static void addChildForNode(Node<String> node){
		String tableId = node.getData();
		/*if(!ValidationUtil.isValid(linkedTblsForIndividualTblHM.get(tableId))) {
			System.out.println("Issue in getting link for "+tableId);
		}else {*/
			String linkedTablesArr[] = linkedTblsForIndividualTblHM.get(tableId).split(",");
			List<Node<String>> childList = new ArrayList<Node<String>>();
			for(String tabId:linkedTablesArr){
				childList.add(new Node<String>(tabId));
			}
			node.addChildren(childList);
		//}
	}
	private static void addChildForNodeList(List<Node<String>> nodeList){
		Iterator<Node<String>> nodeItr = nodeList.iterator();
		while(nodeItr.hasNext()){
			Node<String> each = nodeItr.next();
			String tableId = each.getData();
			String linkedTablesArr[] = linkedTblsForIndividualTblHM.get(tableId).split(",");
			List<Node<String>> childList = new ArrayList<Node<String>>();
			for(String tabId:linkedTablesArr){
				childList.add(new Node<String>(tabId));
			}
			each.addChildren(childList);
		}
	}
	private static boolean isExistInExternalIncludedTbl(String tableId){
		if(externalIncludedTables!=null && externalIncludedTables.size()>0){
			for(String includedTblId:externalIncludedTables){
				if(tableId.equalsIgnoreCase(includedTblId))
					return true;
			}
		}
		return false;
	}
	private static void trackBackForNode(Node<String> node){
		if(!isTrackBackLinkCompleted){
			trackBackLink = node.getData()+"-"+trackBackLink;
			if(node.getParent()!=null){
				trackBackForNode(node.getParent());
			}else{
				isTrackBackLinkCompleted = true;
			}
		}
	}
	private static void formJoinStringForLink(String link, Integer joinType){
		if(ValidationUtil.isValid(link)){
			String[] tblIdArr = link.split("-");
			for(int index=0;index<(tblIdArr.length-1);index++){
				String key = tblIdArr[index]+"-"+tblIdArr[index+1];
				if(relationHM!=null){
					if(relationHM.get(key)!=null){
						if(!ValidationUtil.isValid(String.valueOf(joinWhereString))){
							joinWhereString.append(relationHM.get(key));
						}else{
							if(joinType == 1) {
								joinWhereString.append(relationHM.get(key));	
							}else {
								joinWhereString.append(" AND "+relationHM.get(key));
							}
						}
					}else{
						key = tblIdArr[index+1]+"-"+tblIdArr[index];
						if(relationHM.get(key)!=null){
							if(!ValidationUtil.isValid(String.valueOf(joinWhereString))){
								joinWhereString.append(relationHM.get(key));
							}else{
								if(joinType == 1) {
									joinWhereString.append(relationHM.get(key));	
								}else {
									joinWhereString.append(" AND "+relationHM.get(key));
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static synchronized ArrayList formDynamicJoinString(String catalogIdIp, Integer joinType, ArrayList<String> tableIdArrIp, ArrayList relationMetaData, Integer baseTableId, String baseTableAliasName){
		initializeVariables();
		String returnJoinString = null;
		tableIdArr = tableIdArrIp;
		catalogId = catalogIdIp;
		/* Query needed data from Vc_Relations Table */
		linkedTblsForIndividualTblHM = (HashMap<String,String>) relationMetaData.get(0);
		relationHM = (HashMap<String,String>) relationMetaData.get(1);
		baseTable = baseTableId;
		/* Identify direct links among the input-set of tables */
		if(baseTable==null){
			checkDirectLinks();
		}else{
			checkDirectLinksWithBaseTbl();
		}			
		if(directLinkKeysAL!=null){
			/* Form join-string avoiding repetition of tables */
			formDirectLinksJoinString(joinType);
		}
		unDoneTableIdsWithDirectLinkAL = new ArrayList<String>(tableIdArr);
		if(doneTableIdsWithDirectLinkAL!=null)
			unDoneTableIdsWithDirectLinkAL.removeAll(doneTableIdsWithDirectLinkAL);
		if(unDoneTableIdsWithDirectLinkAL.size()>0){
			for(String tableId:unDoneTableIdsWithDirectLinkAL){
				trackBackLink = "";
				isLinkFound = false;
				isTrackBackLinkCompleted = false;
				finalTargetNode = null;
				String localLink = createLinkForUndoneTblIds(tableId);
				if(ValidationUtil.isValid(localLink)){
					formJoinStringForLink(localLink, joinType);
				}
			}
		}
		if(joinWhereString!=null && !"".equals(String.valueOf(joinWhereString))){
			returnJoinString = new String();
			returnJoinString = String.valueOf(joinWhereString);
		}
		externalIncludedTables.addAll(tableIdArr);
		if(baseTable!=null){
			externalIncludedTables.add(String.valueOf(baseTable));
		}
		ArrayList returnAL = new ArrayList();
		
		 if(baseTableAliasName.length()>0 && joinType==1) {
	        	String[] getVal= baseTableAliasName.split("@-@");
	        	returnJoinString = " FROM "+ getVal[0] + " "+ getVal[1] + " "+returnJoinString;
	        }
		
		returnAL.add(returnJoinString);
		if(externalIncludedTables!=null && externalIncludedTables.size()>0){
			List<Integer> returnTblIdList = new ArrayList<Integer>();
			for(String tblIdStr:externalIncludedTables){
				returnTblIdList.add(Integer.parseInt(tblIdStr));
			}
			Collections.sort(returnTblIdList);
			returnAL.add(returnTblIdList);
		}else{
			returnAL.add(externalIncludedTables);
		}
		return returnAL;
	}
	
	
	
}
