package com.vision.wb;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.vision.dao.AbstractDao;
import com.vision.dao.ColorPaletteDao;
import com.vision.exception.ExceptionCode;
import com.vision.vb.VrdObjectPropertiesVb;

@Component
public class ColorPaletteWb extends AbstractWorkerBean<VrdObjectPropertiesVb>{
	
    @Autowired
    ColorPaletteDao colorPaletteDao;
	
	@Override
	protected AbstractDao<VrdObjectPropertiesVb> getScreenDao() {
		return colorPaletteDao;
	}
	
	@Override
	protected void setAtNtValues(VrdObjectPropertiesVb vObject) {
		vObject.setRecordIndicatorNt(7);
		vObject.setRecordIndicator(0);
	}
	
	@Override
	protected void setVerifReqDeleteType(VrdObjectPropertiesVb vObject) {
		vObject.setStaticDelete(false);
		vObject.setVerificationRequired(false);
	}
	

	public JSONArray findChartTypes(VrdObjectPropertiesVb designVb) {
		List<VrdObjectPropertiesVb> designVbList = colorPaletteDao.findChartTypes(designVb);
		JSONObject jsonObject = null;
		JSONArray jsonarray = new JSONArray();
		for (VrdObjectPropertiesVb designObj : designVbList) {
			jsonObject = new JSONObject();
			jsonObject.put("id", designObj.getObjPaletteID());
			jsonObject.put("text", designObj.getVrdObjectName());
			jsonObject.put("type", designObj.getVrdObjectID());
			try {
				generateAxisParameter(jsonObject, designObj.getHtmlTagProperty());
			} catch (Exception e) {
				e.printStackTrace();
			}
			jsonarray.put(jsonObject);
		}
		return jsonarray;
	}

	private void generateAxisParameter(JSONObject jsonObject, String designObj) throws Exception {

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource src = new InputSource();
		src.setCharacterStream(new StringReader(designObj));
		Document doc = builder.parse(src);
		JSONObject jsonAxisObj = null;
		NodeList nodeList = doc.getElementsByTagName("CHART_XML");
		if (nodeList != null && nodeList.getLength() > 0) {
			for (int loop = 0; loop < nodeList.getLength(); loop++) {
				if (nodeList.item(loop).getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) nodeList.item(loop);

					NodeList xList = element.getElementsByTagName("X_AXIS");
					Element elementChild = null;
					if (xList != null && xList.getLength() > 0) {
						jsonAxisObj = new JSONObject();
						elementChild = (Element) xList.item(0);
						jsonAxisObj.put("multi",
								((elementChild.getElementsByTagName("Multi_Flag") != null)
										? elementChild.getElementsByTagName("Multi_Flag").item(0).getTextContent()
										: ""));
						jsonAxisObj.put("column_type",
								((elementChild.getElementsByTagName("selection") != null)
										? elementChild.getElementsByTagName("selection").item(0).getTextContent()
										: ""));
						jsonAxisObj.put("enable",((elementChild.getElementsByTagName("enable") != null)
								? elementChild.getElementsByTagName("enable").item(0).getTextContent()
								: "N") ) ;
						jsonObject.put("x_axis", jsonAxisObj);
					}
					NodeList yList = element.getElementsByTagName("Y_AXIS");
					if (yList != null && yList.getLength() > 0) {
						jsonAxisObj = new JSONObject();
						elementChild = (Element) yList.item(0);
						jsonAxisObj.put("multi",
								((elementChild.getElementsByTagName("Multi_Flag") != null)
										? elementChild.getElementsByTagName("Multi_Flag").item(0).getTextContent()
										: ""));
						jsonAxisObj.put("column_type",
								((elementChild.getElementsByTagName("selection") != null)
										? elementChild.getElementsByTagName("selection").item(0).getTextContent()
										: ""));
						jsonAxisObj.put("enable",((elementChild.getElementsByTagName("enable") != null)
								? elementChild.getElementsByTagName("enable").item(0).getTextContent()
								: "N") ) ;
						jsonObject.put("y_axis", jsonAxisObj);
					}
					NodeList zList = element.getElementsByTagName("Z_AXIS");
					if (zList != null && zList.getLength() > 0) {
						jsonAxisObj = new JSONObject();
						elementChild = (Element) zList.item(0);
						jsonAxisObj.put("multi",
								((elementChild.getElementsByTagName("Multi_Flag") != null)
										? elementChild.getElementsByTagName("Multi_Flag").item(0).getTextContent()
										: ""));
						jsonAxisObj.put("column_type",
								((elementChild.getElementsByTagName("selection") != null)
										? elementChild.getElementsByTagName("selection").item(0).getTextContent()
										: ""));
						jsonAxisObj.put("enable",((elementChild.getElementsByTagName("enable") != null)
								? elementChild.getElementsByTagName("enable").item(0).getTextContent()
								: "N") ) ;
						jsonObject.put("z_axis", jsonAxisObj);

					}
					NodeList seiesList = element.getElementsByTagName("SERIES_AXIS");
					if (seiesList != null && seiesList.getLength() > 0) {
						jsonAxisObj = new JSONObject();
						elementChild = (Element) seiesList.item(0);
						jsonAxisObj.put("multi",
								((elementChild.getElementsByTagName("Multi_Flag") != null)
										? elementChild.getElementsByTagName("Multi_Flag").item(0).getTextContent()
										: ""));
						jsonAxisObj.put("column_type",
								((elementChild.getElementsByTagName("selection") != null)
										? elementChild.getElementsByTagName("selection").item(0).getTextContent()
										: ""));
						jsonAxisObj.put("enable",((elementChild.getElementsByTagName("enable") != null)
								? elementChild.getElementsByTagName("enable").item(0).getTextContent()
								: "N") ) ;
						
						jsonObject.put("series", jsonAxisObj);
					}

				}
			}
		}

	}

	public List<VrdObjectPropertiesVb> findActiveColorPaletteFromObjProperties() {
		return colorPaletteDao.findActiveColorPaletteFromObjProperties();
	}

	public ExceptionCode addColorPaletteFromObjProperties(VrdObjectPropertiesVb vcReportObj) {
		return colorPaletteDao.addColorPaletteFromObjProperties(vcReportObj);
	}

	public ExceptionCode updateColorPaletteFromObjProperties(VrdObjectPropertiesVb vcReportObj) {
	 return	 colorPaletteDao.updateColorPaletteFromObjProperties(vcReportObj);
	}

}
