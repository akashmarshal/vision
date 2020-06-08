package com.vision.dao;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.vision.authentication.SessionContextHolder;
import com.vision.exception.ExceptionCode;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.DashboardDesignVb;
import com.vision.vb.LevelOfDisplayVb;
import com.vision.vb.VcConfigMainLODWrapperVb;
import com.vision.vb.VcConfigMainVb;
import com.vision.vb.WidgetDesignVb;
import com.vision.vb.WidgetLODWrapperVb;
import com.vision.vb.WidgetWrapperVb;
import com.vision.vb.XmlJsonUploadVb;

@Component
public class WidgetDesignDao extends AbstractDao<WidgetDesignVb> {
	
	@Override
	protected void setServiceDefaults() {
		serviceName = "VC Configuration";
		serviceDesc = "VC Configuration";
		tableName = "VISION_CATALOG_WIP";
		childTableName = "VISION_CATALOG_WIP";
		intCurrentUserId = SessionContextHolder.getContext().getVisionId();
		userGroup = SessionContextHolder.getContext().getUserGroup();
		userProfile = SessionContextHolder.getContext().getUserProfile();
	}
	
	public List<WidgetDesignVb> getQueryResults(WidgetDesignVb vObj) {
		setServiceDefaults();
		String sql = " select MAINWID.WIDGET_CONTEXT,MAINWID.FILTER_CONTEXT,WIDGETSUBTABLE.* from (   SELECT  DISTINCT T1.WIDGET_ID, T1.DESCRIPTION, T1.QUERY_ID, T1.WIDGET_STATUS_NT, T1.WIDGET_STATUS, "+ 
                " T1.RECORD_INDICATOR_NT, T1.RECORD_INDICATOR, T1.MAKER, T1.VERIFIER, T1.INTERNAL_STATUS,  "+
                " TO_CHAR(T1.DATE_LAST_MODIFIED, 'DD-MM-YYYY HH24:MI:SS') DATE_LAST_MODIFIED,  "+
                " TO_CHAR(T1.DATE_CREATION, 'DD-MM-YYYY HH24:MI:SS') DATE_CREATION  "+
                " FROM VWC_MAIN_WIDGETS T1 LEFT JOIN VDD_RS_WIDGETS_LOD T2  ON T1.WIDGET_ID = T2.WIDGET_ID "+ 
                " WHERE (T1.MAKER ='"+intCurrentUserId+"' OR (T2.USER_GROUP = '"+userGroup+"' AND T2.USER_PROFILE ='"+userProfile+"' ))  ) WIDGETSUBTABLE "+
                " INNER JOIN VWC_MAIN_WIDGETS MAINWID "+ 
                " ON MAINWID.WIDGET_ID  = WIDGETSUBTABLE.WIDGET_ID ";
		         
		if(ValidationUtil.isValid(vObj.getWidgetId())) {
			sql += "AND UPPER(WIDGETSUBTABLE.WIDGET_ID) = UPPER('"+vObj.getWidgetId()+"') ";
		}
		sql += "ORDER BY WIDGETSUBTABLE.WIDGET_ID, WIDGETSUBTABLE.DESCRIPTION";
		try {
			return getJdbcTemplate().query(sql, new RowMapper<WidgetDesignVb>(){
				@Override
				public WidgetDesignVb mapRow(ResultSet rs, int rowNum) throws SQLException {
					WidgetDesignVb widgetVb = new WidgetDesignVb();
					widgetVb.setWidgetId(rs.getString("WIDGET_ID"));
					widgetVb.setDescription(rs.getString("DESCRIPTION"));
					widgetVb.setQueryId(rs.getString("QUERY_ID"));
					if(ValidationUtil.isValid(vObj.getWidgetId())) {
						widgetVb.setWidgetContext(rs.getString("WIDGET_CONTEXT"));
						widgetVb.setWidgetContextJson(CommonUtils.XmlToJson(widgetVb.getWidgetContext()).getResponse()+"");;
						widgetVb.setFilterContext(rs.getString("FILTER_CONTEXT"));
					}
					return widgetVb;
				}
			});
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int doInsertionAppr (WidgetDesignVb vObj) {
		
		setServiceDefaults();
		
		String sql = "INSERT INTO VWC_MAIN_WIDGETS "
				+ " (WIDGET_ID, DESCRIPTION, QUERY_ID, WIDGET_STATUS_NT, WIDGET_STATUS, "
				+ " RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, "
				+ " DATE_LAST_MODIFIED, DATE_CREATION, WIDGET_CONTEXT, FILTER_CONTEXT) "
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?)";
		try {
			Object[] args = { vObj.getWidgetId(), vObj.getDescription(), vObj.getQueryId(), vObj.getWidgetStatusNt(), vObj.getWidgetStatus(), 
					vObj.getRecordIndicatorNt(), vObj.getRecordIndicator(), intCurrentUserId, intCurrentUserId, 0};
			
			return getJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					int argumentLength = args.length;
					PreparedStatement ps = connection.prepareStatement(sql);
					for (int i = 1; i <= argumentLength; i++) {
						ps.setObject(i, args[i - 1]);
					}

					String clobData = ValidationUtil.isValid(vObj.getWidgetContext()) ? vObj.getWidgetContext() : "";
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObj.getFilterContext()) ? vObj.getFilterContext() : "";
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
					return ps;
				}
			});
		} catch(Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			return Constants.ERRONEOUS_OPERATION;
		}
	}
	
	public int doUpdateAppr (WidgetDesignVb vObj) {
		
		setServiceDefaults();
		
		String sql = "UPDATE VWC_MAIN_WIDGETS SET WIDGET_CONTEXT = ?, FILTER_CONTEXT = ?, DESCRIPTION = ?, QUERY_ID = ?, "
				+ " WIDGET_STATUS = ?, RECORD_INDICATOR = ?, MAKER = ?, VERIFIER = ?, "
				+ " DATE_LAST_MODIFIED = SYSDATE WHERE WIDGET_ID = ? ";
		try {
			Object[] args = {vObj.getDescription(), vObj.getQueryId(), vObj.getWidgetStatus(), 
					vObj.getRecordIndicator(), intCurrentUserId, intCurrentUserId, vObj.getWidgetId()};
			
			return getJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					int psIndex = 0;
					PreparedStatement ps = connection.prepareStatement(sql);

					String clobData = ValidationUtil.isValid(vObj.getWidgetContext()) ? vObj.getWidgetContext() : "";
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					clobData = ValidationUtil.isValid(vObj.getFilterContext()) ? vObj.getFilterContext() : "";
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());
					
					for (int i = 1; i <= args.length; i++) {
						ps.setObject(++psIndex, args[i - 1]);
					}
					return ps;
				}
			});
		} catch(Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			return Constants.ERRONEOUS_OPERATION;
		}
	}

	@Override
	protected List<WidgetDesignVb> selectApprovedRecord(WidgetDesignVb vObject){
		return getQueryResults(vObject);
	}
	
	public ExceptionCode executeSql(String sql, Object[] args){
		ExceptionCode exceptionCode = new ExceptionCode();
		try{
			if(args==null)
				getJdbcTemplate().execute(sql);
			else
				getJdbcTemplate().update(sql,args);
			exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
		}catch(Exception e){
			if(args!=null)
				e.printStackTrace();
			exceptionCode.setErrorMsg(e.getMessage());
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
		}
		return exceptionCode;
	}
	
	public ExceptionCode formResponceJsonForGridWidget(WidgetWrapperVb vObject, List<XmlJsonUploadVb> headerNameList, List<XmlJsonUploadVb> columnNameList, String sql,String dataTable) {
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			JSONObject headerObj = new JSONObject();
			headerObj.put("ROW1", headerNameList);
			List<String> rowStringList = new ArrayList<String>();
			Long totalRows =0l;
			if(vObject.getMainModel().getTotalRows() == 0l)
			totalRows = getJdbcTemplate().queryForObject("SELECT  COUNT(*) FROM "+dataTable,Long.class);
			else 
			totalRows = vObject.getMainModel().getTotalRows();
			 
			vObject.getMainModel().setTotalRows(totalRows);
			sql = "SELECT * FROM (SELECT temp.*, ROWNUM num FROM ("+sql+") temp where ROWNUM <="+vObject.getMainModel().getLastIndex()+") WHERE num > "+vObject.getMainModel().getStartIndex();
			getJdbcTemplate().query(sql, new ResultSetExtractor<Object>() {
				@Override
				public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
					int rowIndex = 1;
					rowLoop:while(rs.next()) {
						List<XmlJsonUploadVb> cellValueList = new ArrayList<XmlJsonUploadVb>();
						for(XmlJsonUploadVb headerVb:columnNameList) {
							cellValueList.add(new XmlJsonUploadVb(rs.getString((headerVb.getData()).toUpperCase())));
						}
						JSONObject rowObj = new JSONObject();
						rowObj.put("Row"+rowIndex, cellValueList);
						rowStringList.add(rowObj.toString());
						if(rowIndex==10000) {
							break rowLoop;
						}
						rowIndex++;
					}
					return null;
				}
			});
			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("HEADER", headerObj.toString());
			returnMap.put("BODY", rowStringList);
			exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
			exceptionCode.setResponse(returnMap);
		} catch(Exception e) {
			e.printStackTrace();
			exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
			exceptionCode.setErrorMsg(e.getMessage());
		}
		return exceptionCode;
	}
	
	public int doInsertRecordForWidgetAccess(WidgetLODWrapperVb widgetDesiignLodVb, boolean isMain) throws Exception {
		WidgetDesignVb mainObject = widgetDesiignLodVb.getMainModel();
		String tableName = "VDD_RS_Widgets_LOD";
		String sql = "DELETE FROM " + tableName + " WHERE WIDGET_ID = '" + mainObject.getWidgetId() + "'";

		try {
			getJdbcTemplate().update(sql);
		} catch (Exception e) {
		}

		try {
			if(ValidationUtil.isValidList(widgetDesiignLodVb.getLodProfileList())) {
				sql = "INSERT INTO " + tableName
						+ " (WIDGET_ID,  USER_GROUP_AT, USER_GROUP, USER_PROFILE_AT, USER_PROFILE, WG_STATUS, "
						+ "RECORD_INDICATOR_NT, RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, DATE_CREATION) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, To_Date(?, 'DD-MM-YYYY HH24:MI:SS'), To_Date(?, 'DD-MM-YYYY HH24:MI:SS'))";
				for (LevelOfDisplayVb vObject : widgetDesiignLodVb.getLodProfileList()) {
					Object[] args = { mainObject.getWidgetId(), vObject.getUserGroupAt(), vObject.getUserGroup(),
							vObject.getUserProfileAt(), vObject.getUserProfile(), "0", mainObject.getRecordIndicatorNt(),
							mainObject.getRecordIndicator(), mainObject.getMaker(), mainObject.getVerifier(),
							mainObject.getInternalStatus(), mainObject.getDateLastModified(),
							mainObject.getDateCreation() };
					getJdbcTemplate().update(sql, args);
				}
			}
			
			return Constants.SUCCESSFUL_OPERATION;
		} catch (Exception e) {
			throw e;
		}
	}

	public WidgetLODWrapperVb getRecordForWidgetLOD(WidgetLODWrapperVb widgetLobVb) {
		WidgetDesignVb mainObject = widgetLobVb.getMainModel();
		try {
			String sql = "SELECT USER_GROUP, USER_PROFILE FROM VDD_RS_WIDGETS_LOD " +
					" WHERE UPPER(WIDGET_ID) = UPPER('"+mainObject.getWidgetId()+"') " +
					" ORDER BY USER_GROUP, USER_PROFILE";
			List<LevelOfDisplayVb> profileList = getJdbcTemplate().query(sql, new RowMapper<LevelOfDisplayVb>() {
				@Override
				public LevelOfDisplayVb mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new LevelOfDisplayVb(rs.getString("USER_GROUP"), rs.getString("USER_PROFILE"), null);
				}
			});
			widgetLobVb.setLodProfileList(ValidationUtil.isValidList(profileList)?profileList:null);
			return widgetLobVb;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ExceptionCode ValidatingDashboardWidgetData(String WidgetId) {
		ExceptionCode exceptionCode =new ExceptionCode();
    List<DashboardDesignVb> mainList = WidgetLevelValidaton("VDD_RS_WIDGETS", "VDD_RS_DASHBOARDS",WidgetId);
	List<DashboardDesignVb> wipList  = WidgetLevelValidaton("VDD_RS_WIDGETS_WIP", "VDD_RS_DASHBOARDS_WIP",WidgetId);
	mainList.addAll(wipList);
	  if(mainList.size() >0 || wipList.size()>0) {
		  exceptionCode.setErrorMsg("WIDGET DATA PRESENT IN DASHBOARD CANNOT DELETE");
		  exceptionCode.setResponse(mainList);
		  exceptionCode.setErrorCode(Constants.VALIDATION_ERRORS_FOUND);
	  }else {
	    exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
	  }
	  return exceptionCode;
	}
	
	public List<DashboardDesignVb> WidgetLevelValidaton(String WidgettableName,String dashboardTableName, String widgetID) {
		return getJdbcTemplate().query("SELECT DASHBOARD_ID,DASHBOARD_DESC FROM "+dashboardTableName+" WHERE DASHBOARD_ID in (select DASHBOARD_ID from "+WidgettableName+" WHERE WIDGETS_ID ='"+widgetID+"' and RSW_STATUS !=9) ", new RowMapper<DashboardDesignVb>() {
			@Override
			public DashboardDesignVb mapRow(ResultSet rs, int rowNum) throws SQLException {
				DashboardDesignVb dashboardVb = new DashboardDesignVb();
				dashboardVb.setDashboardId(rs.getString("DASHBOARD_ID"));
				dashboardVb.setDashboardDesc(rs.getString("DASHBOARD_DESC"));
				return dashboardVb;
			}
		});
		
	}
	public synchronized int getMaxVersionNumber(String widgetId) {
		String sql = "SELECT CASE WHEN MAX(VERSION_NO) IS NULL THEN 0 ELSE MAX(VERSION_NO) END VERSION_NO FROM VWC_MAIN_WIDGETS_AD WHERE WIDGET_ID = ?";
		Object args[] = {widgetId};
		return getJdbcTemplate().queryForObject(sql, args, Integer.class);
	}

	public void moveMainDataToAD(String widgetId) {
		Integer versionNo = getMaxVersionNumber(widgetId)+1;
		getJdbcTemplate().update(" INSERT INTO VWC_MAIN_WIDGETS_AD(WIDGET_ID,DESCRIPTION,QUERY_ID,WIDGET_CONTEXT,FILTER_CONTEXT,WIDGET_STATUS_NT,WIDGET_STATUS, " + 
				" RECORD_INDICATOR_NT,RECORD_INDICATOR,MAKER,VERIFIER,INTERNAL_STATUS,DATE_LAST_MODIFIED,DATE_CREATION,VERSION_NO) " + 
				" select WIDGET_ID,DESCRIPTION,QUERY_ID,WIDGET_CONTEXT,FILTER_CONTEXT,WIDGET_STATUS_NT,WIDGET_STATUS,RECORD_INDICATOR_NT, " + 
				" RECORD_INDICATOR,MAKER,VERIFIER,INTERNAL_STATUS,DATE_LAST_MODIFIED,DATE_CREATION, "+versionNo+" FROM VWC_MAIN_WIDGETS WHERE WIDGET_ID = '"+widgetId+"' ");
	}
	

	
}
