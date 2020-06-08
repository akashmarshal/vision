package com.vision.dao;

import java.io.IOException;
import java.net.SocketException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.vision.util.ValidationUtil;
import com.vision.vb.CommonVb;
import com.vision.vb.LevelOfDisplayVb;
import com.vision.vb.MenuVb;
import com.vision.vb.ProfileData;
import com.vision.vb.VisionUsersVb;

@Component
public class CommonDao {
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<CommonVb> findVerificationRequiredAndStaticDelete(String pTableName) throws DataAccessException {
		
		String sql = "select DELETE_TYPE,VERIFICATION_REQD FROM VISION_TABLES where UPPER(TABLE_NAME) = UPPER(?)";
		Object[] lParams = new Object[1];
		lParams[0] = pTableName;
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				CommonVb commonVb = new CommonVb();
				commonVb.setStaticDelete(rs.getString("DELETE_TYPE") == null || rs.getString("DELETE_TYPE").equalsIgnoreCase("S") ? true : false);
				commonVb.setVerificationRequired(rs.getString("VERIFICATION_REQD") == null || rs.getString("VERIFICATION_REQD").equalsIgnoreCase("Y") ? true : false);
				return commonVb;
			}
		};
		List<CommonVb> commonVbs = getJdbcTemplate().query(sql, lParams, mapper);
		if(commonVbs == null || commonVbs.isEmpty()){
			commonVbs = new ArrayList<CommonVb>();
			CommonVb commonVb = new CommonVb();
			commonVb.setStaticDelete(true);
			commonVb.setVerificationRequired(true);
			commonVbs.add(commonVb);
		}
		return commonVbs;
	}

	public List<ProfileData> getTopLevelMenu(int visionId) throws DataAccessException{
			
		String sql = "SELECT distinct NST.NUM_SUBTAB_DESCRIPTION, PP.MENU_GROUP,PP.MENU_ICON ,"+
 					 "PP.P_ADD, PP.P_MODIFY, PP.P_DELETE, PP.P_INQUIRY, PP.P_VERIFICATION , PP.P_EXCEL_UPLOAD "+
 					 "FROM PROFILE_PRIVILEGES PP, VISION_USERS MU, NUM_SUB_TAB NST "+ 
 					 "where PP.USER_GROUP = MU.USER_GROUP and PP.USER_PROFILE = MU.USER_PROFILE "+
 					 "and  NST.NUM_SUB_TAB = PP.MENU_GROUP and MU.VISION_ID = ? AND PP.PROFILE_STATUS = 0 AND NST.NUM_SUBTAB_STATUS=0 "+
 					 "and num_tab = 176 order by PP.MENU_GROUP";
		Object[] lParams = new Object[1];
		lParams[0] = visionId;
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProfileData profileData = new ProfileData();
				profileData.setMenuItem(rs.getString("NUM_SUBTAB_DESCRIPTION"));
				profileData.setMenuGroup(rs.getInt("MENU_GROUP"));
				profileData.setProfileAdd(rs.getString("P_ADD"));
				profileData.setProfileModify(rs.getString("P_MODIFY"));
				profileData.setProfileDelete(rs.getString("P_DELETE"));
				profileData.setProfileInquiry(rs.getString("P_INQUIRY"));
				profileData.setProfileVerification(rs.getString("P_VERIFICATION"));
				profileData.setProfileUpload(rs.getString("P_EXCEL_UPLOAD"));
				profileData.setMenuIcon(rs.getString("MENU_ICON"));
				return profileData;
			}
		};
		List<ProfileData> profileData = getJdbcTemplate().query(sql, lParams, mapper);
		return profileData;
	}
	public ArrayList<MenuVb> getSubMenuItemsForMenuGroup(int menuGroup) throws DataAccessException{
		
		String sql = "SELECT * FROM VISION_MENU WHERE MENU_GROUP = ? AND MENU_STATUS = 0 AND UPPER(MENU_NAME) != 'SEPERATOR' ORDER BY PARENT_SEQUENCE, MENU_SEQUENCE";
		Object[] lParams = new Object[1];
		lParams[0] = menuGroup;
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				MenuVb menuVb = new MenuVb();
				menuVb.setMenuProgram(rs.getString("MENU_PROGRAM"));
				menuVb.setMenuName(rs.getString("MENU_NAME"));
				menuVb.setMenuSequence(rs.getInt("MENU_SEQUENCE"));
				menuVb.setParentSequence(rs.getInt("PARENT_SEQUENCE"));
				menuVb.setSeparator(rs.getString("SEPARATOR"));
				menuVb.setMenuGroup(rs.getInt("MENU_GROUP"));
				menuVb.setMenuStatus(rs.getInt("MENU_STATUS"));
//				menuVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
				return menuVb;
			}
		};
		ArrayList<MenuVb> menuList = (ArrayList<MenuVb>)getJdbcTemplate().query(sql, lParams, mapper);
		return menuList;
	}
	public String findVisionVariableValue(String pVariableName) throws DataAccessException {
		if(!ValidationUtil.isValid(pVariableName)){
			return null;
		}
		String sql = "select VALUE FROM VISION_VARIABLES where UPPER(VARIABLE) = UPPER(?)";
		Object[] lParams = new Object[1];
		lParams[0] = pVariableName;
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				CommonVb commonVb = new CommonVb();
				commonVb.setMakerName(rs.getString("VALUE"));
				return commonVb;
			}
		};
		List<CommonVb> commonVbs = getJdbcTemplate().query(sql, lParams, mapper);
		if(commonVbs != null && !commonVbs.isEmpty()){
			return commonVbs.get(0).getMakerName();
		}
		return null;
	}
	public void findDefaultHomeScreen(VisionUsersVb vObject) throws DataAccessException {
		int count = 0;
		String sql = "SELECT COUNT(1) FROM PWT_REPORT_SUITE WHERE VISION_ID = "+vObject.getVisionId();
		count = getJdbcTemplate().queryForObject(sql, Integer.class);
		/*if(count>0){
			vObject.setDefaultHomeScreen(true);
		}*/
	}
	public int getMaxOfId(){
		String sql = "select max(vision_id) from (Select max(vision_id) vision_id from vision_users UNION ALL select Max(vision_id) from vision_users_pend)";
		int i = getJdbcTemplate().queryForObject(sql, Integer.class);
		return i;
	}
	public String getVisionBusinessDayForExpAnalysis(String countryLeBook){
		Object args[] = {countryLeBook};
		return getJdbcTemplate().queryForObject("select TO_CHAR(BUSINESS_DATE,'Mon-RRRR') BUSINESS_DATE  from Vision_Business_Day  WHERE COUNTRY ||'-'|| LE_BOOK=?",
				args,String.class);
	}
	
	public String getyearMonthForTop10Deals(String countryLeBook){
			Object args[] = {countryLeBook};
			return getJdbcTemplate().queryForObject("select TO_CHAR(BUSINESS_DATE,'RRRRMM') BUSINESS_DATE  from Vision_Business_Day  WHERE COUNTRY ||'-'|| LE_BOOK=?",
					args,String.class);
	}
	public String getVisionBusinessDate(String countryLeBook){
		Object args[] = {countryLeBook};
		return getJdbcTemplate().queryForObject("select TO_CHAR(BUSINESS_DATE,'DD-Mon-RRRR') BUSINESS_DATE  from Vision_Business_Day  WHERE COUNTRY ||'-'|| LE_BOOK=?",
				args,String.class);
	}
	
	public String getVisionCurrentYearMonth(){
		return getJdbcTemplate().queryForObject("select to_char(to_date(CURRENT_YEAR_MONTH,'RRRRMM'),'Mon-RRRR') CURRENT_YEAR_MONTH  from V_Curr_Year_Month",
				String.class);
	}
	public int getUploadCount(){
		  String sql = "Select count(1) from Vision_Upload where Upload_Status = 1 AND FILE_NAME LIKE '%XLSX'";
		  int i = getJdbcTemplate().queryForObject(sql, Integer.class);
		  return i;
	}
	public int doPasswordResetInsertion(VisionUsersVb vObject){
    	Date oldDate = new Date(); 
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss") ;
        String resetValidity= df.format(oldDate);
    	if(ValidationUtil.isValid(vObject.getPwdResetTime())){
    	    Date newDate = DateUtils.addHours(oldDate, Integer.parseInt(vObject.getPwdResetTime()));
            resetValidity= df.format(newDate);
	    }
		String query = "Insert Into FORGOT_PASSWORD ( VISION_ID, RESET_DATE, RESET_VALIDITY, RS_STATUS_NT, RS_STATUS)" +
			"Values (?, SysDate, To_Date(?, 'DD-MM-YYYY HH24:MI:SS'), ?, ?)";
		Object[] args = {vObject.getVisionId(), resetValidity, vObject.getUserStatusNt(), vObject.getUserStatus()};  
		return getJdbcTemplate().update(query,args);
	}
	
	public List<LevelOfDisplayVb> getQueryUserGroupProfile() throws DataAccessException{
		String sql  = "SELECT USER_GROUP, USER_PROFILE FROM PROFILE_PRIVILEGES " + 
				" GROUP BY USER_GROUP, USER_PROFILE";
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				LevelOfDisplayVb lodVb = new LevelOfDisplayVb();
				lodVb.setUserGroup(rs.getString("USER_GROUP"));
				lodVb.setUserProfile(rs.getString("USER_PROFILE"));
				return lodVb;
			}
		};
		List<LevelOfDisplayVb> lodVbList = getJdbcTemplate().query(sql, mapper);
		return lodVbList;
	}
	
	public List<ProfileData> getQueryUserGroup() throws DataAccessException{
		String sql  = "SELECT DISTINCT USER_GROUP FROM PROFILE_PRIVILEGES ORDER BY USER_GROUP";
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProfileData profileData = new ProfileData();
				profileData.setUserGroup(rs.getString("USER_GROUP"));
				return profileData;
			}
		};
		List<ProfileData> profileData = getJdbcTemplate().query(sql, mapper);
		return profileData;
	}
	
	public List<ProfileData> getQueryUserGroupBasedProfile(String userGroup) throws DataAccessException{
		String sql  = "SELECT DISTINCT USER_PROFILE,USER_GROUP FROM PROFILE_PRIVILEGES where USER_GROUP ='"+userGroup+"' ORDER BY USER_GROUP";
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProfileData profileData = new ProfileData();
				profileData.setUserGroup(rs.getString("USER_GROUP"));
				profileData.setUserProfile(rs.getString("USER_PROFILE"));
				return profileData;
			}
		};
		List<ProfileData> profileData = getJdbcTemplate().query(sql, mapper);
		return profileData;
	}
	
	public String getSystemDate() {
		String sql = "SELECT To_Char(SysDate, 'DD-MM-YYYY HH24:MI:SS') AS SYSDATE1 FROM DUAL";
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return (rs.getString("SYSDATE1"));
			}
		};
		return (String) getJdbcTemplate().queryForObject(sql, null, mapper);
	}
	public String getSystemDate12Hr() {
		String sql = "SELECT To_Char(SysDate, 'DD-MM-YYYY HH:MI:SS') AS SYSDATE1 FROM DUAL";
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return (rs.getString("SYSDATE1"));
			}
		};
		return (String) getJdbcTemplate().queryForObject(sql, null, mapper);
	}
	
	public String getScriptValue(String pVariableName) throws DataAccessException, Exception{
		Object params[] = {pVariableName};
		String sql = new String("select VARIABLE_SCRIPT from VISION_DYNAMIC_HASH_VAR WHERE VARIABLE_TYPE = 2  AND UPPER(VARIABLE_NAME)=UPPER(?)");
		return getJdbcTemplate().queryForObject(sql,params,String.class);	}
	
	public List<ProfileData> getTopLevelMenu(int visionId, String adMembers) throws DataAccessException{

		adMembers = adMembers.replaceAll("DC=ubagroup,DC=com", "").replaceAll("OU=Groups", "").replaceAll("CN=", "").replaceAll("", "").replaceAll("OU=", "");
		String arrayMember [] = adMembers.split(",");
		StringBuffer memberOf = new StringBuffer();
		int count = 0;
		for(String member:arrayMember){
			if(ValidationUtil.isValid(member)){
				memberOf.append("'"+member.trim().toUpperCase()+"'");
				if(count+1!=arrayMember.length){
					memberOf.append(",");
				}
			}
			count++;
		}
		String node = System.getenv("VISION_NODE_NAME");
		if(!ValidationUtil.isValid(node)){
			node="A1";
		}
		String sql = "WITH  "+
				" VISION_USER_PROFILE_V1 AS "+
				" ( "+
				" SELECT TRIM(VU.USER_GRP_PROFILE) VU_GRP_PROFILE FROM VISION_USERS VU WHERE VU.VISION_ID = '"+visionId+"' "+
				" UNION ALL "+
				" SELECT TRIM(VU.AUTO_GRP_PROFILE) VU_GRP_PROFILE FROM VISION_USERS VU WHERE VU.VISION_ID = '"+visionId+"' AND VU.ALLOW_AUTO_PROFILE_FLAG = 'Y' "+
				" UNION ALL "+
				" SELECT VULINK.USER_GROUP||'-'||VULINK.USER_PROFILE VU_GRP_PROFILE "+ 
				" FROM VISION_AD_PROFILE_LINK VULINK  "+
				" WHERE UPPER(VULINK.AD_MEMBER_GROUP) IN ("+memberOf.toString()+") "+
				"  AND (SELECT VU.ALLOW_AD_PROFILE_FLAG FROM VISION_USERS VU WHERE VU.VISION_ID = '"+visionId+"') = 'Y' "+
				" AND VULINK.PROFILE_LINKER_STATUS = 0 "+
				" ), "+
				" VISION_USER_PROFILE_V2 AS "+
				" ( "+
				" SELECT LISTAGG(V1.VU_GRP_PROFILE,',') WITHIN GROUP (ORDER BY NULL) VU_GRP_PROFILE FROM VISION_USER_PROFILE_V1 V1 "+
				" ), "+
				" VISION_USER_PROFILE_V3 AS "+
				" ( "+
				" SELECT FN_RS_PARSESTRING(FN_RS_PARSESTRING(V2.VU_GRP_PROFILE,LEVEL,','),1,'-') VU_GRP, "+
				" FN_RS_PARSESTRING(FN_RS_PARSESTRING(V2.VU_GRP_PROFILE,LEVEL,','),2,'-') VU_PROFILE "+
				" FROM VISION_USER_PROFILE_V2 V2 "+
				" CONNECT BY LEVEL <= (LENGTH(V2.VU_GRP_PROFILE)-LENGTH(REPLACE(V2.VU_GRP_PROFILE,',',''))+1) "+
				" ), "+
				" VISION_USER_PROFILE_V4 AS "+
				" ( "+
				" SELECT DISTINCT V3.VU_GRP,V3.VU_PROFILE FROM VISION_USER_PROFILE_V3 V3 "+
				" ), "+
				" VISION_USER_PROFILE AS "+
				" ( "+
				" SELECT NST.NUM_SUBTAB_DESCRIPTION MENU_GROUP_DESC, "+
				"       PP.MENU_GROUP, "+
				"       VM.MENU_PROGRAM, "+
				"       VM.PARENT_SEQUENCE, "+
				"       VM.MENU_SEQUENCE,"
				+ "		VM.MENU_NAME, VM.SEPARATOR, "+
				"       MAX(PP.MENU_ICON) MENU_ICON, "+ 
				"       MAX(PP.P_ADD) P_ADD, "+
				"       MAX(PP.P_MODIFY) P_MODIFY, "+
				"       MAX(PP.P_DELETE) P_DELETE, "+
				"       MAX(PP.P_INQUIRY) P_INQUIRY, "+ 
				"       MAX(PP.P_VERIFICATION) P_VERIFICATION, "+
				"       MAX(PP.P_EXCEL_UPLOAD) P_EXCEL_UPLOAD, "+
				"       ROW_NUMBER() OVER (PARTITION BY VM.MENU_PROGRAM ORDER BY PP.MENU_GROUP) LOCAL_ROWNUM "+
				" FROM VISION_USER_PROFILE_V4 V4,  "+
				"     PROFILE_PRIVILEGES PP, "+
				"     VISION_MENU VM, "+
				"     NUM_SUB_TAB NST "+
				" WHERE V4.VU_GRP = PP.USER_GROUP "+
				"  AND V4.VU_PROFILE = PP.USER_PROFILE "+
				"  AND NST.NUM_TAB = PP.MENU_GROUP_NT "+
				"  AND NST.NUM_SUB_TAB = PP.MENU_GROUP "+
				"  AND PP.MENU_GROUP = VM.MENU_GROUP "
				+ " and VM.MENU_STATUS = 0 "+
				//"  AND VM.SEPARATOR !='Y' "+
				"  AND INSTR(','||NVL(PP. EXCLUDE_MENU_PROGRAM_LIST,'@!XYZ!@')||',',','||VM.MENU_PROGRAM||',') = 0 "+
				"  AND INSTR(','||NVL(VM.MENU_NODE_VISIBILITY,'@!XYZ!@')||',',',"+node+",') > 0 "+
				" GROUP BY NST.NUM_SUBTAB_DESCRIPTION, "+
				"         PP.MENU_GROUP, "+
				"         VM.MENU_PROGRAM, "+
				"         VM.PARENT_SEQUENCE, "+
				"         VM.MENU_SEQUENCE, "+
				"		  VM.MENU_NAME, VM.SEPARATOR "+
				" ) "+
				" SELECT * FROM VISION_USER_PROFILE VUP WHERE VUP.LOCAL_ROWNUM = 1 "+
				" ORDER BY VUP.MENU_GROUP,VUP.PARENT_SEQUENCE,VUP.MENU_SEQUENCE";
		Object[] lParams = new Object[1];
		lParams[0] = visionId;
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProfileData profileData = new ProfileData();
				profileData.setMenuItem(rs.getString("MENU_GROUP_DESC"));
				profileData.setMenuGroup(rs.getInt("MENU_GROUP"));
				profileData.setMakerName(rs.getString("MENU_PROGRAM"));
				profileData.setVerifierName(rs.getString("MENU_NAME"));
				profileData.setMaker(rs.getInt("PARENT_SEQUENCE"));
				profileData.setVerifier(rs.getInt("MENU_SEQUENCE"));
				profileData.setProfileAdd(rs.getString("P_ADD"));
				profileData.setProfileModify(rs.getString("P_MODIFY"));
				profileData.setProfileDelete(rs.getString("P_DELETE"));
				profileData.setProfileInquiry(rs.getString("P_INQUIRY"));
				profileData.setProfileVerification(rs.getString("P_VERIFICATION"));
				profileData.setProfileUpload(rs.getString("P_EXCEL_UPLOAD"));
				profileData.setMenuIcon(rs.getString("MENU_ICON"));
				profileData.setDateCreation(rs.getString("SEPARATOR"));
				return profileData;
			}
		};
		List<ProfileData> profileData = getJdbcTemplate().query(sql, mapper);
		return profileData;
	
	}
	public static String getMacAddress(String ip) throws IOException {
        String address = null;
        String str = "";
        String macAddress = "";
        try {
        	
        	 String cmd = "arp -a " + ip;
        	    Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream());
        	    Pattern pattern = Pattern.compile("(([0-9A-Fa-f]{2}[-:]){5}[0-9A-Fa-f]{2})|(([0-9A-Fa-f]{4}\\.){2}[0-9A-Fa-f]{4})");
        	    try {
        	        while (s.hasNext()) {
        	            str = s.next();
        	            Matcher matcher = pattern.matcher(str);
        	            if (matcher.matches()){
        	                break;
        	            }
        	            else{
        	                str = null;
        	            }
        	        }
        	    }
        	    finally {
        	        s.close();
        	    }
        	    if(!ValidationUtil.isValid(str)){
        	    	return ip;
        	    }
        	    return (str != null) ? str.toUpperCase(): null;
        	
        	/*
        	
        	InetAddress inetAddress = InetAddress.getByName(ip);
        	 NetworkInterface network = NetworkInterface.getByInetAddress(inetAddress.getLoopbackAddress());
             byte[] mac = network.getHardwareAddress();

             StringBuilder sb = new StringBuilder();
             if(mac != null){
             	for (int i = 0; i < mac.length; i++) {
                     sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                 }
             	macAddress = sb.toString();
             }*/
        } catch (SocketException ex) {
            ex.printStackTrace();
            return ip;
        }
    }
	public ArrayList<MenuVb> getSubMenuItemsForMenuGroup(int menuGroup, String excludeMenuProgramList) throws DataAccessException{
		StringBuffer sql = new StringBuffer("SELECT * FROM VISION_MENU WHERE MENU_GROUP = ? AND MENU_STATUS = 0 ");
		String  execludeMneu = "";		
		if(excludeMenuProgramList.contains(",")){
			String accounts [] = excludeMenuProgramList.split(",");
			int count = 0;
			for(String menuProgram : accounts){
				if(ValidationUtil.isValid(menuProgram)){
					//sql.append(" AND (ACCOUNT_NUMBER LIKE '%"+acctNumber+"%' OR ACCOUNT_NAME LIKE '%"+acctNumber+"%') ");
					execludeMneu = execludeMneu+"'"+menuProgram+"' ";
					if(count +1!= accounts.length){
						execludeMneu= execludeMneu+",";
					}
				}
				count++;
			}
			sql.append(" AND MENU_PROGRAM not in ("+execludeMneu+")");
		}else if(ValidationUtil.isValid(excludeMenuProgramList)){
			sql.append(" AND MENU_PROGRAM not in ('"+excludeMenuProgramList+"')");
		}
		sql.append(" ORDER BY PARENT_SEQUENCE, MENU_SEQUENCE");
		
		Object[] lParams = new Object[1];
		lParams[0] = menuGroup;
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				MenuVb menuVb = new MenuVb();
				menuVb.setMenuProgram(rs.getString("MENU_PROGRAM"));
				menuVb.setMenuName(rs.getString("MENU_NAME"));
				menuVb.setMenuSequence(rs.getInt("MENU_SEQUENCE"));
				menuVb.setParentSequence(rs.getInt("PARENT_SEQUENCE"));
				menuVb.setSeparator(rs.getString("SEPARATOR"));
				menuVb.setMenuGroup(rs.getInt("MENU_GROUP"));
				menuVb.setMenuStatus(rs.getInt("MENU_STATUS"));
				menuVb.setRecordIndicator(rs.getInt("RECORD_INDICATOR"));
				return menuVb;
			}
		};
		ArrayList<MenuVb> menuList = (ArrayList<MenuVb>)getJdbcTemplate().query(sql.toString(), lParams, mapper);
		return menuList;
	}
	
	public int updateWidgetCreationStagingTable(String tableName) {
		String sql = "UPDATE VWC_STAGGING_TABLE_LOGGING SET PROCESSED = 'Y' , DATE_LAST_MODIFIED = sysdate WHERE TABLE_NAME = '"+tableName+"'";
		return getJdbcTemplate().update(sql);
	}
}