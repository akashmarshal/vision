package examples;

import java.util.ArrayList;
import java.util.List;

public class TemptCheck {

	public static void main1(String[] args) {

		StringBuffer finalTest = new StringBuffer();
     	String addText ="akash , akash,akash,akash,akash,akash,akash,akash,"
     			+ " akash,akash,akash,akash,akash,akash,akash,akash,akash,"
     			+ "akash,akash,akash,akash,akash,akash,akash";
     	
     	int tmpCount=1;
     	for(int i=1;i<=3750000; i++)
     	{
     		 if(tmpCount == 1000)
     		 {
     			tmpCount=0;
     			System.out.println("Completed Records :"+i);
     		 }
     		 
     		finalTest.append(addText+"/n"); 
     		tmpCount++;
     	}
     	
     	System.out.println("completed All Recrods");

 }
	
	
	public static void main(String[] args) {

		StringBuffer finalTest = new StringBuffer();
     	String addText ="akash , akash,akash,akash,akash,akash,akash,akash,"
     			+ " akash,akash,akash,akash,akash,akash,akash,akash,akash,"
     			+ "akash,akash,akash,akash,akash,akash,akash";
     	List<StringBuffer> tmpList = new ArrayList<StringBuffer>();
     	int tmpCount=1;
     	for(int i=1;i<=5715000; i++)
     	{
     		 if(tmpCount == 1000)
     		 {
     			tmpList.add(finalTest);
     			finalTest = new StringBuffer();
     			tmpCount=0;
     			System.out.println("Completed Records :"+i);
     		 }
     		 
     		finalTest.append(addText+"/n"); 
     		tmpCount++;
     	}
     /*	List<StringBuffer> tmpList2 = new ArrayList<StringBuffer>();
    	int tmpCount1=1;
     	for(int i=5715000;i<=11430000; i++)
     	{
     		 if(tmpCount == 1000)
     		 {
     			tmpList.add(finalTest);
     			finalTest = new StringBuffer();
     			tmpCount=0;
     			System.out.println("Completed Records :"+i);
     		 }
     		 
     		finalTest.append(addText+"/n"); 
     		tmpCount1++;
     	}
     	*/
     	System.out.println("completed All Recrods");

 }
	
	
	
}