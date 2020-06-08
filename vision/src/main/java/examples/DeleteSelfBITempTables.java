package examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DeleteSelfBITempTables {

	public static void main(String args[]) {
		try (Connection con = getConnection(); Statement stmt = con.createStatement();) {
			deleteData(con, stmt, "RS_RPT_LIST");
			Thread.currentThread().sleep(1000);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		String jdbcUrl = "jdbc:oracle:thin:@10.16.1.101:1521:VISION";
		return DriverManager.getConnection(jdbcUrl, "VISIONBI", "vision123");
	}

	private static void deleteData(Connection con, Statement stmt, String tableName) {
		try (ResultSet rs = stmt.executeQuery("select unique (session_ID) session_ID from " + tableName);) {
			List<String> uniqueTablePatternList = new ArrayList<String>();
			while (rs.next()) {
				/*dropTables(rs.getString(1));
				stmt.executeQuery("delete from " + tableName + " where session_ID = '" + rs.getString(1) + "'");*/
				uniqueTablePatternList.add(rs.getString(1));
			}
			for(String uniqueTablePattern : uniqueTablePatternList) {
				dropTables(uniqueTablePattern);
				stmt.executeQuery("delete from " + tableName + " where session_ID = '" + uniqueTablePattern + "'");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void dropTables(String tablePattern) {
		try (Connection con1 = getConnection();
				Statement stmt1 = con1.createStatement();
				ResultSet rs1 = stmt1.executeQuery(
						"select distinct(TABLE_NAME) as TABLE_NAME from USER_TABLES where TABLE_NAME like '%"
								+ tablePattern + "%'");) {
			while (rs1.next()) {
				try (Statement stmt2 = con1.createStatement();){
					stmt2.executeQuery("drop table " + rs1.getString(1) + " purge");
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
