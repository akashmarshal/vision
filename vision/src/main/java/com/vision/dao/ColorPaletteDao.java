package com.vision.dao;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.vision.authentication.SessionContextHolder;
import com.vision.exception.ExceptionCode;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.VrdObjectPropertiesVb;
import com.vision.vb.VisionUsersVb;

@Component
public class ColorPaletteDao  extends AbstractDao<VrdObjectPropertiesVb>{
	

	@Override
	public void setServiceDefaults() {
		serviceName = "Widget Configuration";
		serviceDesc = "Widget Configuration";
		VisionUsersVb vObj = SessionContextHolder.getContext();
		intCurrentUserId = vObj.getVisionId();
		userGroup = vObj.getUserGroup();
		userProfile = vObj.getUserProfile();
	}


	public List<VrdObjectPropertiesVb> findChartTypes(VrdObjectPropertiesVb designVb) {
		String sql = "SELECT A.VRD_OBJECT_ID,B.OBJ_TAG_ID, B.OBJ_TAG_DESC,b.HTML_TAG_PROPERTY FROM VRD_RS_OBJECTS A,VRD_OBJECT_PROPERTIES B "
				+ " WHERE A.VRD_OBJECT_ID =B.VRD_OBJECT_ID AND A.VRD_OBJECT_CATEGORY='" + designVb.getVrdObjectID()
				+ "' AND A.VRD_OBJECT_STATUS=0 AND B.OBJ_TAG_STATUS=0 ORDER BY A.VRD_OBJECT_ID";
		return getJdbcTemplate().query(sql, getChartMapper());
	}

	protected RowMapper getChartMapper() {
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				VrdObjectPropertiesVb vObj = new VrdObjectPropertiesVb();
				vObj.setVrdObjectID(rs.getString("VRD_OBJECT_ID"));
				vObj.setVrdObjectName(rs.getString("OBJ_TAG_DESC"));
				vObj.setObjPaletteID(rs.getString("OBJ_TAG_ID"));
				vObj.setHtmlTagProperty(rs.getString("HTML_TAG_PROPERTY"));
				return vObj;
			}
		};
		return mapper;
	}
	
	

	public List<VrdObjectPropertiesVb> findActiveColorPaletteFromObjProperties() throws DataAccessException {
		String sql = "SELECT * FROM VRD_OBJECT_PROPERTIES WHERE VRD_OBJECT_ID='Palette' AND OBJ_TAG_STATUS=0 ORDER BY OBJ_TAG_ID";
		RowMapper mapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				VrdObjectPropertiesVb vObj = new VrdObjectPropertiesVb();
				vObj.setVrdObjectID(rs.getString("VRD_OBJECT_ID"));
				vObj.setObjPaletteID(rs.getString("OBJ_TAG_ID"));
				if (ValidationUtil.isValid(rs.getString("HTML_TAG_PROPERTY"))) {
					vObj.setHtmlTagProperty(
							CommonUtils.getValueForXmlTag(rs.getString("HTML_TAG_PROPERTY"), "COLOR_CODE"));
					vObj.setRadiantColor(
							CommonUtils.getValueForXmlTag(rs.getString("HTML_TAG_PROPERTY"), "IS_RADIANT"));
				}
				vObj.setObjPaletteStatus(rs.getInt("OBJ_TAG_STATUS"));
				vObj.setObjPaletteDesc(rs.getString("OBJ_TAG_DESC"));
				vObj.setObjPaletteIconLink(rs.getString("OBJ_TAG_ICON_LINK"));
				return vObj;
			}
		};
		return getJdbcTemplate().query(sql, mapper);
	}
	
	

	public ExceptionCode updateColorPaletteFromObjProperties(VrdObjectPropertiesVb vObject) {
		setServiceDefaults();

		String query = "UPDATE VRD_OBJECT_PROPERTIES SET HTML_TAG_PROPERTY = ?, OBJ_TAG_DESC = ?, "
				+ "MAKER = ?, VERIFIER = ?, DATE_LAST_MODIFIED = sysdate "
				+ "WHERE VRD_OBJECT_ID = ? AND OBJ_TAG_ID = ?";
		Object[] args = { vObject.getObjPaletteDesc(), intCurrentUserId, intCurrentUserId, vObject.getVrdObjectID(),
				vObject.getObjPaletteID() };
		try {
			getJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					int psIndex = 0;

					PreparedStatement ps = connection.prepareStatement(query);
					String clobData = (ValidationUtil.isValid(vObject.getRadiantColor())
							&& ("true".equalsIgnoreCase(vObject.getRadiantColor())
									|| "false".equalsIgnoreCase(vObject.getRadiantColor())))
											? "<COLOR_PROP><IS_RADIANT>" + vObject.getRadiantColor() + "</IS_RADIANT>"
											: "<COLOR_PROP><IS_RADIANT>false</IS_RADIANT>";
					clobData += ValidationUtil.isValid(vObject.getHtmlTagProperty())
							? "<COLOR_CODE>" + vObject.getHtmlTagProperty() + "</COLOR_CODE></COLOR_PROP>"
							: "<COLOR_CODE></COLOR_CODE></COLOR_PROP>"; // HTML_TAG_PROPERTY
					ps.setCharacterStream(++psIndex, new StringReader(clobData), clobData.length());

					for (int i = 1; i <= args.length; i++) {
						ps.setObject(++psIndex, args[i - 1]);
					}
					return ps;
				}
			});
			return getResultObject(Constants.SUCCESSFUL_OPERATION);
		} catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			return getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
		}
	}


	public ExceptionCode addColorPaletteFromObjProperties(VrdObjectPropertiesVb vcReportObj) {
		setServiceDefaults();
		String query = "Insert into VRD_OBJECT_PROPERTIES "
				+ "(VRD_OBJECT_ID, OBJ_TAG_ID, OBJ_TAG_STATUS_NT, OBJ_TAG_STATUS, RECORD_INDICATOR_NT, "
				+ "RECORD_INDICATOR, MAKER, VERIFIER, INTERNAL_STATUS, DATE_LAST_MODIFIED, "
				+ "DATE_CREATION, OBJ_TAG_DESC, OBJ_TAG_ICON_LINK, HTML_TAG_PROPERTY)" + " Values "
				+ "(?, ?, 1, 0, 7, 1, ?, ?, 0, sysdate, sysdate, ?, ?, ?)";
		Object[] args = { vcReportObj.getVrdObjectID(), vcReportObj.getObjPaletteID(), intCurrentUserId, intCurrentUserId,
				vcReportObj.getObjPaletteDesc(), vcReportObj.getObjPaletteIconLink() };
	

		try {
		
			getJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					int argumentLength = args.length;					
					PreparedStatement ps = connection.prepareStatement(query);
					for (int i = 1; i <= argumentLength; i++) {
						ps.setObject(i, args[i - 1]);
					}
					String clobData = (ValidationUtil.isValid(vcReportObj.getRadiantColor())
							&& ("true".equalsIgnoreCase(vcReportObj.getRadiantColor())
									|| "false".equalsIgnoreCase(vcReportObj.getRadiantColor())))
											? "<COLOR_PROP><IS_RADIANT>" + vcReportObj.getRadiantColor() + "</IS_RADIANT>"
											: "<COLOR_PROP><IS_RADIANT>false</IS_RADIANT>";
					clobData += ValidationUtil.isValid(vcReportObj.getHtmlTagProperty())
							? "<COLOR_CODE>" + vcReportObj.getHtmlTagProperty() + "</COLOR_CODE></COLOR_PROP>"
							: "<COLOR_CODE></COLOR_CODE></COLOR_PROP>"; // HTML_TAG_PROPERTY
					ps.setCharacterStream(++argumentLength, new StringReader(clobData), clobData.length());
				return ps;
				}
			});
			return getResultObject(Constants.SUCCESSFUL_OPERATION);
		} catch (Exception e) {
			e.printStackTrace();
			strErrorDesc = e.getMessage();
			return getResultObject(Constants.WE_HAVE_ERROR_DESCRIPTION);
		}
	}

	
	

}
