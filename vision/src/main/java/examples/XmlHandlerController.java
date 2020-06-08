package examples;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlHandlerController {

	public static void main(String [] args) throws Exception
		{
		
		
		JSONObject Object = new JSONObject();
		Object.put("id", "007");
		Object.put("text", "007text");
		Object.put("type", "007type");
		
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource(new FileInputStream(new File("E:/akash.xml")));
			Document doc = builder.parse(src);
			
			JSONObject jsonAxisObj = null;
			
			NodeList nodeList =  doc.getElementsByTagName("CHART_XML");
			System.out.println(nodeList.getLength());
			if(nodeList != null && nodeList.getLength() > 0) {
				for(int loop=0; loop<nodeList.getLength();loop++)
				{
					System.out.println(nodeList.item(loop).toString());
					if(nodeList.item(loop).getNodeType() == Node.ELEMENT_NODE){
					Element element = ((Element)((Element)nodeList.item(loop)).getElementsByTagName("TAB_PROPERTIES").item(0));
//					TAB_PROPERTIES
					
					NodeList xList = element.getElementsByTagName("X_AXIS");
					System.out.println(xList.getLength());
					jsonAxisObj = new JSONObject();
					if(xList != null && xList.getLength() >0)
					{
						Element elementChild= (Element)xList.item(0);
						System.out.println(elementChild.getElementsByTagName("Multi_Flag"));
						jsonAxisObj.put("multi",((elementChild.getElementsByTagName("Multi_Flag")!=null) ? elementChild.getElementsByTagName("Multi_Flag").item(0).getTextContent():""))   ;
						jsonAxisObj.put("column_type",((elementChild.getElementsByTagName("selection")!=null) ? elementChild.getElementsByTagName("selection").item(0).getTextContent():""))   ;
						jsonAxisObj.put("enable", "Y");	
						Object.put("x_axis", jsonAxisObj);
					}
					NodeList yList = element.getElementsByTagName("Y_AXIS");
					if(yList != null && yList.getLength() >0)
					{
						jsonAxisObj = new JSONObject();
						Element elementChild= (Element)yList.item(0);
						System.out.println(elementChild.getTextContent());
						System.out.println(elementChild.getElementsByTagName("Multi_Flag").item(0));
						System.out.println(elementChild.getElementsByTagName("Multi_Flag").item(0).getTextContent());
						jsonAxisObj.put("multi",((elementChild.getElementsByTagName("Multi_Flag")!=null) ? elementChild.getElementsByTagName("Multi_Flag").item(0).getTextContent():""))   ;
						jsonAxisObj.put("column_type",((elementChild.getElementsByTagName("selection")!=null) ? elementChild.getElementsByTagName("selection").item(0).getTextContent():""))   ;
						jsonAxisObj.put("enable", "Y");	
						Object.put("y_axis", jsonAxisObj);
					}
					
					NodeList zList = element.getElementsByTagName("Z_AXIS");
					if(zList != null && zList.getLength() >0)
					{
						jsonAxisObj = new JSONObject();
						Element elementChild= (Element)zList.item(0);
						System.out.println(elementChild.getTextContent());
						jsonAxisObj.put("multi",((elementChild.getElementsByTagName("Multi_Flag")!=null) ? elementChild.getElementsByTagName("Multi_Flag").item(0).getTextContent():""))   ;
						jsonAxisObj.put("column_type",((elementChild.getElementsByTagName("selection")!=null) ? elementChild.getElementsByTagName("selection").item(0).getTextContent():""))   ;
						jsonAxisObj.put("enable", "Y");	
						Object.put("z_axis", jsonAxisObj);
						
					}
					
					NodeList seiesList = element.getElementsByTagName("SERIES_AXIS");
					if(seiesList != null && seiesList.getLength() >0)
					{
						jsonAxisObj = new JSONObject();
						Element elementChild= (Element)seiesList.item(0);
						jsonAxisObj.put("multi",((elementChild.getElementsByTagName("Multi_Flag")!=null) ? elementChild.getElementsByTagName("Multi_Flag").item(0).getTextContent():""))   ;
						jsonAxisObj.put("column_type",((elementChild.getElementsByTagName("selection")!=null) ? elementChild.getElementsByTagName("selection").item(0).getTextContent():""))   ;
						jsonAxisObj.put("enable", "Y");	
						Object.put("series", jsonAxisObj);
					}
					
				}
		}
			}
			
			
			System.out.println(Object.toString());
		}	
	
	
	
	public static String getValueForXmlTag(String source, String tagName){
		try {
			Matcher regexMatcher = Pattern.compile("\\<"+tagName+"\\>(.*?)\\<\\/"+tagName+"\\>", Pattern.DOTALL).matcher(source);
			return regexMatcher.find()? regexMatcher.group(1):null;
		}catch(Exception e){
			return null;
		}
	}
	
}
