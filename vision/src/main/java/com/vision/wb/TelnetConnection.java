package com.vision.wb;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.telnet.TelnetClient;


public class TelnetConnection
{
	private TelnetClient telnet = null;
	private InputStream in;
	private OutputStream out;
	private char prompt='$';
	private static final String LINE_FEED = "\r\n"; 
	private String hostName;
	private String userName;
	private String password;
	private String processDir = "/home/oracle/execs";
	URLConnection urlConnection = null;
	private String scriptsProcessDir = "/home/vision/scripts";

  	public TelnetConnection(String hostName, String userName, String password, char prompt){
  		this.hostName = hostName;
  		this.userName = userName;
  		this.password = password;
  		this.prompt = prompt;
  	}
  	public TelnetConnection(String hostName, String userName, String password, String processDir, char prompt){
  		this.hostName = hostName;
  		this.userName = userName;
  		this.password = password;
  		this.processDir = processDir;
  		this.prompt = prompt;
  	}
  	public void connect() throws Exception{
  	/*	System.out.println("Begin telnet connection.....");
  		URL url;
  		url = new URL("telnet", hostName, 23, "", new thor.net.URLStreamHandler());
        urlConnection = url.openConnection();
        urlConnection.connect();
        if (urlConnection instanceof TelnetURLConnection) {
        	((TelnetURLConnection)urlConnection).setTelnetTerminalHandler(new DefaultTelnetTerminalHandler());
        }
        out = urlConnection.getOutputStream();
        in = urlConnection.getInputStream();
        readUntil( "login: " );
		write( userName +LINE_FEED);
		readUntil( "Password:" );
		write( password +LINE_FEED);
		readUntil( prompt + " " );*/
		System.out.println("telnet Connected.....!");
  	}
	public void connect(String serverType) throws Exception{
		// Connect to the specified server
		telnet = new TelnetClient();
		//telnet.setDefaultTimeout(100*60*2);
		telnet.setConnectTimeout(100*60*5);
		telnet.connect( hostName, 23 );
		// Get input and output stream references
		in = telnet.getInputStream();
		out = new PrintStream( telnet.getOutputStream() );
		// Log the user on
		//System.out.println("Login");
		readUntil( "login: " );
		//System.out.println("After Login");
		write( userName );
		//System.out.println("After UserNam");
		readUntil( "Password:" );
		//System.out.println("After password");
		write( password );
		// Advance to a prompt
		if(FTPClientConfig.SYST_NT.equalsIgnoreCase(serverType)){
			prompt='>';
		}
		readUntil( prompt + " " );
	}
	public boolean isConnected(){
		if(telnet == null && urlConnection == null) return false;
		if(telnet != null)
			return telnet.isConnected();
		/*if(urlConnection != null)
			return ((TelnetURLConnection)urlConnection).connected();*/
		return false;
	}
	public String readUntil( String pattern ) throws Exception{
		try {
			char lastChar = pattern.charAt( pattern.length() - 1 );
			StringBuffer sb = new StringBuffer();
			char ch = ( char )in.read();
			while( true ) {
				//System.out.println("----------------------------------");
				//System.out.println(sb);
				//System.out.println("----------------------------------");
				sb.append( ch );
				if( ch == lastChar ) {
					if(sb.toString().endsWith(pattern)) {
						return sb.toString();
					}
				}else if(sb.toString().contains("Denying new connections")){
					throw new RuntimeException("Service un-available.");
				}
				ch = ( char )in.read();
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	public void write( String value ) {
		try {
			out.write( value.getBytes());
			out.flush();
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
	}

	public String sendCommand( String command ) {
		try {
			write( command +LINE_FEED);
			return readUntil( prompt + " " );
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}
	public void disconnect() {
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if(telnet != null)
				telnet.disconnect();
			/*if(urlConnection != null)
				((TelnetURLConnection)urlConnection).disconnect();*/
		}catch( Exception e ) {
			e.printStackTrace();
		}
	}
  
	public String killBuild(String buildName){
		String lResponse = "";
		try{
			sendCommand("cd "+processDir+LINE_FEED);
		}catch (Exception e) {
			return "Invalid process directory :"+processDir;
		}
		try{
			lResponse = sendCommand("killbuild "+buildName+LINE_FEED);
			if(lResponse != null && !lResponse.isEmpty()){
				lResponse = lResponse.replaceAll("killbuild "+buildName+"\r\n\r\n        ", "");
				lResponse = lResponse.substring(0,lResponse.indexOf("\r\n\r\n[7mvision:[m"+processDir+prompt+" "));
			}
		}catch (Exception e) {
			return "Exception initializing the Build Cron Process for Build Number:"+buildName;
		}
		return lResponse;
	}
	public String startCronAdf(){
		String lResponse = "";
		try{
			sendCommand("cd "+scriptsProcessDir+LINE_FEED);
		}catch (Exception e) {
			return "Invalid process directory :"+scriptsProcessDir;
		}
		try{
			lResponse = sendCommand("startADFCron.sh "+LINE_FEED);
			if(lResponse != null && !lResponse.isEmpty()){
				lResponse = lResponse.replaceAll("startADFCron.sh "+"\r\n\r\n        ", "");
				lResponse = lResponse.substring(0,lResponse.indexOf("\r\n\r\n[7mvision:[m"+scriptsProcessDir+prompt+" "));
			}
		}catch (Exception e) {
			return "Exception initializing the ADF Cron Process ";
		}
		return lResponse;
	}
	public String startCron(){
		String lResponse = "";
		try{
			sendCommand("cd "+processDir+LINE_FEED);
		}catch (Exception e) {
			return "Invalid process directory :"+processDir;
		}
		try{
			lResponse = sendCommand("runcron.sh "+LINE_FEED);
			if(lResponse != null && !lResponse.isEmpty()){
				lResponse = lResponse.replaceAll("runcron.sh "+"\r\n\r\n        ", "");
				lResponse = lResponse.substring(0,lResponse.indexOf("\r\n\r\n[7mvision:[m"+processDir+prompt+" "));
			}
		}catch (Exception e) {
			return "Exception initializing the Build Cron Process ";
		}
		return lResponse;
	}
	public String stopCron(){
		String lResponse = "";
		try{
			sendCommand("cd "+processDir+LINE_FEED);
		}catch (Exception e) {
			return "Invalid process directory :"+processDir;
		}
		try{
			lResponse = sendCommand("stopcron.sh "+LINE_FEED);
			if(lResponse != null && !lResponse.isEmpty()){
				lResponse = lResponse.replaceAll("runcron.sh "+"\r\n\r\n        ", "");
				lResponse = lResponse.substring(0,lResponse.indexOf("\r\n\r\n[7mvision:[m"+processDir+prompt+" "));
			}
		}catch (Exception e) {
			return "Exception initializing the Build Cron Process ";
		}
		return lResponse;
	}
	public String getHostName() {
		return hostName;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	public String startCronStatus(String cronName){//Start Cron for Cron Status screen
		String lResponse = "";
		String cronName1 = "";
		
		if("BUILD_CRON".equalsIgnoreCase(cronName)){
			cronName1="startBuildCron.sh";
		}else if("ADF_CRON".equalsIgnoreCase(cronName)){
			cronName1="startADFCron.sh";
		}else if("NON_ADF_CRON".equalsIgnoreCase(cronName)){
			cronName1="startNonADFCron.sh";
		}else if("PING_CRON".equalsIgnoreCase(cronName)){
			cronName1="startPingCron.sh";
		}
		try{
			sendCommand("cd "+processDir+LINE_FEED);
		}catch (Exception e) {
			return "Invalid process directory :"+processDir;
		}
		try{
			lResponse = sendCommand(cronName1+LINE_FEED);
			if(lResponse != null && !lResponse.isEmpty()){
				lResponse = lResponse.replaceAll(cronName1+"\r\n\r\n        ", "");
				lResponse = lResponse.substring(0,lResponse.indexOf("\r\n\r\n[7mvision:[m"+processDir+prompt+" "));
			}
		}catch (Exception e) {
			return "Exception initializing the Build Cron Process ";
		}
		return lResponse;
	}
	public String stopCronStatus(String cronName){//Stop Cron for Cron Status screen
		String lResponse = "";
		String cronName1 = "";
		
		if("BUILD_CRON".equalsIgnoreCase(cronName)){
			cronName1="stopBuildCron.sh";
		}else if("ADF_CRON".equalsIgnoreCase(cronName)){
			cronName1="stopADFCron.sh";
		}else if("NON_ADF_CRON".equalsIgnoreCase(cronName)){
			cronName1="stopNonADFCron.sh";
		}else if("PING_CRON".equalsIgnoreCase(cronName)){
			cronName1="stopPingCron.sh";
		}
		try{
			sendCommand("cd "+processDir+LINE_FEED);
		}catch (Exception e) {
			return "Invalid process directory :"+processDir;
		}
		try{
			lResponse = sendCommand(cronName1+LINE_FEED);
			if(lResponse != null && !lResponse.isEmpty()){
				lResponse = lResponse.replaceAll(cronName1+"\r\n\r\n        ", "");
				lResponse = lResponse.substring(0,lResponse.indexOf("\r\n\r\n[7mvision:[m"+processDir+prompt+" "));
			}
		}catch (Exception e) {
			return "Exception initializing the Build Cron Process ";
		}
		return lResponse;
	}
	public String killAdf(String adfNumber){
		String lResponse = "";
		try{
			sendCommand("cd "+processDir+LINE_FEED);
		}catch (Exception e) {
			return "Invalid process directory :"+processDir;
		}
		try{
			lResponse = sendCommand("adfKillbuild "+adfNumber+LINE_FEED);
			if(lResponse != null && !lResponse.isEmpty()){
				lResponse = lResponse.replaceAll("adfKillbuild "+adfNumber+"\r\n\r\n        ", "");
				lResponse = lResponse.substring(0,lResponse.indexOf("\r\n\r\n[7mvision:[m"+processDir+prompt+" "));
			}
		}catch (Exception e) {
			return "Exception initializing the ADF Cron Process for ADF Number:"+adfNumber;
		}
		return lResponse;
	}
	public String killNonAdf(String adfNumber){
		String lResponse = "";
		try{
			sendCommand("cd "+processDir+LINE_FEED);
		}catch (Exception e) {
			return "Invalid process directory :"+processDir;
		}
		try{
			lResponse = sendCommand("nonADFKillbuild "+adfNumber+LINE_FEED);
			if(lResponse != null && !lResponse.isEmpty()){
				lResponse = lResponse.replaceAll("adfKillbuild "+adfNumber+"\r\n\r\n        ", "");
				lResponse = lResponse.substring(0,lResponse.indexOf("\r\n\r\n[7mvision:[m"+processDir+prompt+" "));
			}
		}catch (Exception e) {
			return "Exception initializing the ADF Cron Process for ADF Number:"+adfNumber;
		}
		return lResponse;
	}	
	public String killRealTime(String adfNumber){
		String lResponse = "";
		try{
			sendCommand("cd "+processDir+LINE_FEED);
		}catch (Exception e) {
			return "Invalid process directory :"+processDir;
		}
		try{
			lResponse = sendCommand("realTimeFTKillbuild "+adfNumber+LINE_FEED);
			if(lResponse != null && !lResponse.isEmpty()){
				lResponse = lResponse.replaceAll("adfKillbuild "+adfNumber+"\r\n\r\n        ", "");
				lResponse = lResponse.substring(0,lResponse.indexOf("\r\n\r\n[7mvision:[m"+processDir+prompt+" "));
			}
		}catch (Exception e) {
			return "Exception initializing the ADF Cron Process for ADF Number:"+adfNumber;
		}
		return lResponse;
	}	
	
}