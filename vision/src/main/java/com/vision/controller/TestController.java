package com.vision.controller;

import org.json.JSONObject;
import org.json.XML;

public class TestController {

	/*public static void main(String[] args) {
System.out.println("inside");
		
		String jsonStr = "{\r\n" + 
				"	\"table\": {\r\n" + 
				"		\"table_name\": \"ERROR_CODES\",\r\n" + 
				"		\"columns\": {\r\n" + 
				"			\"column\": [\r\n" + 
				"				\"ERROR_CODE\",\r\n" + 
				"				\"ERROR_DESCRIPTION\",\r\n" + 
				"				\"ERROR_TYPE_NT\",\r\n" + 
				"				\"ERROR_TYPE\",\r\n" + 
				"				\"ERROR_STATUS_NT\",\r\n" + 
				"				\"ERROR_STATUS\"\r\n" + 
				"			]\r\n" + 
				"		},\r\n" + 
				"		\"row\": [\r\n" + 
				"			{\r\n" + 
				"				\"dcolumn\": [\r\n" + 
				"					\"888\",\r\n" + 
				"					\"Test 888 Description\",\r\n" + 
				"					\"3\",\r\n" + 
				"					\"1\",\r\n" + 
				"					\"1\",\r\n" + 
				"					\"0\"\r\n" + 
				"				]\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"DCOLUMN\": [\r\n" + 
				"					\"999\",\r\n" + 
				"					\"Test 999 Description\",\r\n" + 
				"					\"3\",\r\n" + 
				"					\"2\",\r\n" + 
				"					\"1\",\r\n" + 
				"					\"0\"\r\n" + 
				"				]\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"DCOLUMN\": [\r\n" + 
				"					\"2222\",\r\n" + 
				"					\"Test 2222 Description\",\r\n" + 
				"					\"3\",\r\n" + 
				"					\"2\",\r\n" + 
				"					\"1\",\r\n" + 
				"					\"0\"\r\n" + 
				"				]\r\n" + 
				"			}\r\n" + 
				"		]\r\n" + 
				"	\r\n" + 
				"		\"table_name\": \"ERROR_CODES1111\",\r\n" + 
				"		\"columns\": {\r\n" + 
				"			\"column\": [\r\n" + 
				"				\"ERROR_CODE1\",\r\n" + 
				"				\"ERROR_DESCRIPTION1\",\r\n" + 
				"				\"ERROR_TYPE_NT1\",\r\n" + 
				"				\"ERROR_TYPE1\",\r\n" + 
				"				\"ERROR_STATUS_NT1\",\r\n" + 
				"				\"ERROR_STATUS1\"\r\n" + 
				"			]\r\n" + 
				"		},\r\n" + 
				"		\"row\": [\r\n" + 
				"			{\r\n" + 
				"				\"dcolumn\": [\r\n" + 
				"					\"111\",\r\n" + 
				"					\"Test 888 Description\",\r\n" + 
				"					\"3\",\r\n" + 
				"					\"1\",\r\n" + 
				"					\"1\",\r\n" + 
				"					\"0\"\r\n" + 
				"				]\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"DCOLUMN\": [\r\n" + 
				"					\"222\",\r\n" + 
				"					\"Test 999 Description\",\r\n" + 
				"					\"3\",\r\n" + 
				"					\"2\",\r\n" + 
				"					\"1\",\r\n" + 
				"					\"0\"\r\n" + 
				"				]\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"DCOLUMN\": [\r\n" + 
				"					\"333\",\r\n" + 
				"					\"Test 2222 Description\",\r\n" + 
				"					\"3\",\r\n" + 
				"					\"2\",\r\n" + 
				"					\"1\",\r\n" + 
				"					\"0\"\r\n" + 
				"				]\r\n" + 
				"			}\r\n" + 
				"		]\r\n" + 
				"	\r\n" + 
				"\r\n" + 
				"  }\r\n" + 
				"}";
		
		
		JSONObject jsonObj = new JSONObject(jsonStr);
		String xml_data = XML.toString(jsonObj);
		
		System.out.println(xml_data);
	}*/
	
	
	
	
	public static void main(String...s){
		String xml_data = "<root><table index=\"0\" index1=\"0\">sample0</table><table index=\"1\">sample1</table><table index=\"2\">sample2</table></root>";
 
		//converting xml to json
		JSONObject obj = XML.toJSONObject(xml_data);
		
		System.out.println(obj.toString());
	}

}
