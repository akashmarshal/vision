package examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vision.dao.AbstractDao;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.VcConfigMainVb;
import com.vision.vb.XmlJsonUploadVb;

public class MyTempDaoAccess   {
	
	public static void main(String [] args)
	{
		
		MyTempDaoAccess daoObj = new MyTempDaoAccess();
		daoObj.getDatas();
		
	}

	private void getDatas() {

		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArry = new JSONArray();
		try (Connection con = returnConnection();
				Statement stmt = con.createStatement();
				ResultSet rsltSet = stmt.executeQuery("select * from ERROR_CODES where rownum < 5");) {
			
			ResultSetMetaData rsmd = rsltSet.getMetaData();
			List<XmlJsonUploadVb> xmlUploadVb = new ArrayList<XmlJsonUploadVb>();
			JSONObject rowObj = new JSONObject();
			// putting column header datas
			XmlJsonUploadVb columnObj = null;
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				columnObj = new XmlJsonUploadVb();
				columnObj.setData(rsmd.getColumnLabel(i));
				xmlUploadVb.add(columnObj);
			}
			jsonObj.put("HEADERS", xmlUploadVb);
			

			// fetching row datas with specific BeanObj
			int rownum = 1;
			while (rsltSet.next()) {
				List<XmlJsonUploadVb> rowList = new ArrayList<XmlJsonUploadVb>();
				for (int i = 1; i <=rsmd.getColumnCount(); i++) {
					XmlJsonUploadVb rowBeanObj = new XmlJsonUploadVb();
					rowBeanObj.setData(String.valueOf(rsltSet.getObject(i)));
					rowList.add(rowBeanObj);
				}

				rowObj.put("ROW" + rownum, rowList);
				rownum++;
			}
			jsonObj.put("BODY", rowObj);

			System.out.println(jsonObj.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	public Connection returnConnection() throws Exception {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			return DriverManager.getConnection("jdbc:oracle:thin:@10.16.1.101:1521:VISION", "VISIONBI", "vision123");
		} catch (Exception e) {
			throw e;
		}
	}
	
}