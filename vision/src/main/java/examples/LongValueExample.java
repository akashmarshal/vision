package examples;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LongValueExample {

	public static void main(String[] args) {
		
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		  Date date = new Date(); 
		  System.out.println(df.format(date));
		

		try (Connection conn = returnConnection() ;
				Statement prpStmnt = 
						conn.createStatement();){

		ResultSet Value = prpStmnt.executeQuery("select MAX(TO_NUMBER(substr(DASHBOARD_ID,INSTR(DASHBOARD_ID, '_')+1,LENGTH(DASHBOARD_ID)))) as uniqueValue from  VDD_RS_DASHBOARDS_AD");
		while (Value.next()) {
		Long value = Value.getLong(1);
		System.out.println(value);
		 }
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}


	protected static Connection returnConnection() throws Exception {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			return DriverManager.getConnection("jdbc:oracle:thin:@10.16.1.101:1521:VISION", "VISIONBI", "vision123");
		} catch (Exception e) {
			throw e;
		}
   } 
	
}