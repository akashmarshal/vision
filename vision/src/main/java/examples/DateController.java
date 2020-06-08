package examples;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateController {

	public static void main(String[] args) {

		String date ="d";
		Integer operator= +2;
		String dateFormat ="dd-MMM-YYYY";
		SimpleDateFormat df = new SimpleDateFormat(dateFormat);
		Calendar cal = Calendar.getInstance(); 
		System.out.println(df.format(cal.getTime()));
		switch(date) {
		
		case("d"):{
			cal.add(Calendar.DATE, operator);
			break;
		}
		case("m"):{
			cal.add(Calendar.MONTH,operator);
			break;
		}
		case("y"):{
			cal.add(Calendar.YEAR, operator);
			break;
		  }
		}
		System.out.println(df.format(cal.getTime()));
	}

}
