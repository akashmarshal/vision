package examples;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BatchController {

	public static void main(String[] args) {
		
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		  Date date = new Date(); 
		  System.out.println(df.format(date));
		

		try (Connection conn = returnConnection() ;
				PreparedStatement prpStmnt = 
						conn.prepareStatement("INSERT INTO SAMPLE_TEST( COLUMN1,"
								+ " COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, "
								+ " COLUMN8,  COLUMN9, COLUMN10,  COLUMN11,  COLUMN12, COLUMN13, "
								+ " COLUMN14,  COLUMN15,  COLUMN16)"
								+ "VALUES(  ?, ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?)");) {

			int batchLmt =1;
			int prntLmt = 1;
			for(int i=0;i<5000000;i++)
			{
				 int col=1;
			    	while (col<=16) {
					prpStmnt.setString(col, "aaaaaaaaaaaaaaaaaaaaaaaaa");
					col++;
					}
			    	prpStmnt.addBatch();	
				if(5000 == batchLmt)
				{
					batchLmt =0;
					prpStmnt.executeBatch();
//					System.out.println("BAtch Exceution :"+(i+1));
				}
				if(prntLmt == 1000000)
				{
					System.out.println("Print million "+prntLmt);
					prntLmt=0;
					date = new Date();
					System.out.println(df.format(date));
				}
				prntLmt++;
				batchLmt++;
			}
			date = new Date();
			System.out.println(df.format(date));		}
		catch(Exception e)
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