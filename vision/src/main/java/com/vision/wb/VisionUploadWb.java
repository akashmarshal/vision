package com.vision.wb;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import com.vision.authentication.SessionContextHolder;
import com.vision.dao.AbstractDao;
import com.vision.dao.BuildSchedulesDao;
import com.vision.dao.CommonDao;
import com.vision.dao.VisionUploadDao;
import com.vision.download.ExportXlsServlet;
import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.DeepCopy;
import com.vision.util.ValidationUtil;
import com.vision.vb.BuildSchedulesVb;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.FileInfoVb;
import com.vision.vb.VisionUploadVb;

@Component
public class VisionUploadWb extends AbstractDynaWorkerBean<VisionUploadVb> implements ApplicationContextAware {

	@Autowired
	private VisionUploadDao visionUploadDao;
	
	@Autowired
	private CommonDao commonDao;

	@Autowired
	private ExportXlsServlet exportXlsServlet;
	
	@Value("${ftp.hostName}")
	private String hostName;

	@Value("${ftp.userName}")
	private String userName;

	@Value("${ftp.password}")
	private String password;

	@Value("${ftp.xlUploadhostName}")
	private String xlUploadhostName;

	@Value("${ftp.xlUploaduserName}")
	private String xlUploaduserName;

	@Value("${ftp.xlUploadpassword}")
	private String xlUploadpassword;

	private String knownHostsFileName;

	private String cbDir;
	private int blockSize;
	private String uploadDir;
	private String uploadDirExl;
	private String downloadDir;
	private String buildLogsDir;
	private String timezoneId;
	private String dateFormate;
	private String serverType;
	private String scriptDir;
	private int fileType;
	private char prompt;
	private boolean securedFtp = true;
	private String fileExtension;
	private String connectorUploadDir;
	private String connectorADUploadDir;
	private int uploadFileChkIntervel = 30;
	private static final String SERVICE_NAME = "Vision Upload";
	private static final int DIR_TYPE_DOWNLOAD = 1;
	private WebApplicationContext webApplicationContext = null;
	private BuildSchedulesDao buildSchedulesDao;

	public VisionUploadDao getVisionUploadDao() {
		return visionUploadDao;
	}

	public void setVisionUploadDao(VisionUploadDao visionUploadDao) {
		this.visionUploadDao = visionUploadDao;
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

	public String getConnectorUploadDir() {
		return connectorUploadDir;
	}

	public void setConnectorUploadDir(String connectorUploadDir) {
		this.connectorUploadDir = connectorUploadDir;
	}

	public String getConnectorADUploadDir() {
		return connectorADUploadDir;
	}

	public void setConnectorADUploadDir(String connectorADUploadDir) {
		this.connectorADUploadDir = connectorADUploadDir;
	}

	public String getUploadDir() {
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}

	public String getDownloadDir() {
		return downloadDir;
	}

	public void setDownloadDir(String downloadDir) {
		this.downloadDir = downloadDir;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public String getTimezoneId() {
		return timezoneId;
	}

	public void setTimezoneId(String timezoneId) {
		this.timezoneId = timezoneId;
	}

	public String getDateFormate() {
		return dateFormate;
	}

	public void setDateFormate(String dateFormate) {
		this.dateFormate = dateFormate;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public String getBuildLogsDir() {
		return buildLogsDir;
	}

	public void setBuildLogsDir(String buildLogsDir) {
		this.buildLogsDir = buildLogsDir;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.webApplicationContext = (WebApplicationContext) applicationContext;
	}

	public void setBuildSchedulesDao(BuildSchedulesDao buildSchedulesDao) {
		this.buildSchedulesDao = buildSchedulesDao;
	}

	public BuildSchedulesDao getBuildSchedulesDao() {
		return buildSchedulesDao;
	}

	public String getScriptDir() {
		return scriptDir;
	}

	public void setScriptDir(String scriptDir) {
		this.scriptDir = scriptDir;
	}

	public boolean isSecuredFtp() {
		return securedFtp;
	}

	public void setSecuredFtp(boolean securedFtp) {
		this.securedFtp = securedFtp;
	}

	public WebApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}

	public void setWebApplicationContext(WebApplicationContext webApplicationContext) {
		this.webApplicationContext = webApplicationContext;
	}

	public String getKnownHostsFileName() {
		return knownHostsFileName;
	}

	public void setKnownHostsFileName(String knownHostsFileName) {
		this.knownHostsFileName = knownHostsFileName;
	}

	public char getPrompt() {
		return prompt;
	}

	public void setPrompt(char prompt) {
		this.prompt = prompt;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getXlUploadhostName() {
		return xlUploadhostName;
	}

	public void setXlUploadhostName(String xlUploadhostName) {
		this.xlUploadhostName = xlUploadhostName;
	}

	public String getXlUploaduserName() {
		return xlUploaduserName;
	}

	public void setXlUploaduserName(String xlUploaduserName) {
		this.xlUploaduserName = xlUploaduserName;
	}

	public String getXlUploadpassword() {
		return xlUploadpassword;
	}

	public void setXlUploadpassword(String xlUploadpassword) {
		this.xlUploadpassword = xlUploadpassword;
	}

	public String getUploadDirExl() {
		return uploadDirExl;
	}

	public void setUploadDirExl(String uploadDirExl) {
		this.uploadDirExl = uploadDirExl;
	}

	public class MyUserInfo implements UserInfo {
		public String getPassword() {
			return password;
		}

		public boolean promptYesNo(String str) {
			return false;
		}

		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return true;
		}

		public boolean promptPassword(String message) {
			return false;
		}

		public void showMessage(String message) {
			return;
		}
	}

	public ExceptionCode doUpload(String fileName, byte[] data, String fileExtension) {
		ExceptionCode exceptionCode = null;
		try {
			setUploadDownloadDirFromDB();
			fileName = fileName.toUpperCase(); // Changes done by Deepak for Bank One
			if (fileName.contains(".XLSX") || fileName.contains(".XLS")) {
				fileExtension = "xlsx";
				setUserName(getXlUploaduserName());
				setPassword(getXlUploadpassword());
				setHostName(getXlUploadhostName());
				setUploadDir(getUploadDirExl());
			}
			if (securedFtp) {
				JSch jsch = new JSch();
				jsch.setKnownHosts(getKnownHostsFileName());
				Session session = jsch.getSession(getUserName(), getHostName());
				{ // "interactive" version // can selectively update specified known_hosts file
					// need to implement UserInfo interface
					// MyUserInfo is a swing implementation provided in
					// examples/Sftp.java in the JSch dist
					UserInfo ui = new MyUserInfo();
					session.setUserInfo(ui); // OR non-interactive version. Relies in host key being in known-hosts file
					session.setPassword(getPassword());
				}
				session.setConfig("StrictHostKeyChecking", "no");
				session.connect();
				Channel channel = session.openChannel("sftp");
				channel.connect();
				ChannelSftp sftpChannel = (ChannelSftp) channel;
				fileName = fileName.substring(0, fileName.lastIndexOf('.')) + "_"
						+ SessionContextHolder.getContext().getVisionId() + "." + fileExtension + "";
				sftpChannel.cd(uploadDir);
				InputStream in = new ByteArrayInputStream(data);
				sftpChannel.put(in, fileName);
				sftpChannel.exit();
				channel = session.openChannel("shell");
				OutputStream inputstream_for_the_channel = channel.getOutputStream();
				PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
				channel.connect();
				commander.println("cd " + scriptDir);
				StringBuilder cmd = new StringBuilder();
				if (!"xlsx".equalsIgnoreCase(fileExtension)) {
					if ('/' != uploadDir.charAt(uploadDir.length() - 1)) {
						cmd.append("./remSpecialChar.sh " + uploadDir + "/" + fileName);
					} else {
						cmd.append("./remSpecialChar.sh " + uploadDir + fileName);
					}
				}
				commander.println(cmd);
				commander.println("exit");
				commander.close();
				do {
					Thread.sleep(1000);
				} while (!channel.isEOF());
				session.disconnect();
			} else {
				FTPClient ftpClient = getConnection();
				ftpClient.connect(getHostName());
				boolean response = ftpClient.login(getUserName(), getPassword());
				if (!response) {
					ftpClient.disconnect();
					exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR,
							"Upload", "");
					exceptionCode.setResponse(fileName);
					throw new RuntimeCustomException(exceptionCode);
				}
				ftpClient.setFileType(fileType);
				response = ftpClient.changeWorkingDirectory(uploadDir);
				if (!response) {
					ftpClient.disconnect();
					exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR,
							"Upload", "");
					exceptionCode.setResponse(fileName);
					throw new RuntimeCustomException(exceptionCode);
				}
				fileName = fileName.toUpperCase();
				fileName = fileName.substring(0, fileName.lastIndexOf('.')) + "_"
						+ SessionContextHolder.getContext().getVisionId() + "." + fileExtension + "";
				InputStream in = new ByteArrayInputStream(data);
				ftpClient.storeFile(fileName, in);
				in.close(); // close the io streams
				ftpClient.disconnect();
			}
		} catch (FileNotFoundException e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		} catch (IOException e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		} catch (Exception e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		}
		exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.SUCCESSFUL_OPERATION, "Upload", "");
		exceptionCode.setOtherInfo(fileName);
		return exceptionCode;
	}

	public ExceptionCode getQueryResults(VisionUploadVb vObject) {
		setVerifReqDeleteType(vObject);
		List<VisionUploadVb> collTemp = getScreenDao().getQueryResults(vObject, 1);
		doSetDesctiptionsAfterQuery(collTemp);
		ExceptionCode exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.SUCCESSFUL_OPERATION, "Query",
				"");
		exceptionCode.setOtherInfo(vObject);
		exceptionCode.setResponse(collTemp);
		return exceptionCode;
	}

	public ArrayList getPageLoadValues() {
		List collTemp = null;
		ArrayList<Object> arrListLocal = new ArrayList<Object>();
		try {
			collTemp = getNumSubTabDao().findActiveNumSubTabsByNumTab(10);
			arrListLocal.add(collTemp);
			return arrListLocal;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Exception in getting the Page load values.", ex);
			return null;
		}
	}

	public ExceptionCode listFilesFromFtpServer(int dirType, String orderBy) {
		ExceptionCode exceptionCode = null;
		TelnetConnection telnetConnection = null;
		try {
			setUploadDownloadDirFromDB();
			if (isSecuredFtp()) {
				return listFilesFromSFtpServer(dirType, orderBy);
			}
			List<FileInfoVb> lFileList = new ArrayList<FileInfoVb>();
			telnetConnection = new TelnetConnection(hostName, userName, password, prompt);
			// telnetConnection.connect(getServerType());
			telnetConnection.connect();
			if (dirType == DIR_TYPE_DOWNLOAD)
				telnetConnection.sendCommand("cd " + downloadDir);
			else
				telnetConnection.sendCommand("cd " + buildLogsDir);
			String responseStr = telnetConnection.sendCommand("ls -ltc ");
			String[] fileEntryArray = responseStr.split("\r\n");
			FTPClientConfig conf = new FTPClientConfig(getServerType());
			conf.setServerTimeZoneId(getTimezoneId());
			UnixFTPEntryParser unixFtpEntryParser = new UnixFTPEntryParser(conf);
			List<String> fileEntryList = unixFtpEntryParser
					.preParse(new LinkedList<String>(Arrays.asList(fileEntryArray)));
			List<FTPFile> lfiles = new ArrayList<FTPFile>(fileEntryList.size());
			for (String fileEntry : fileEntryList) {
				FTPFile ftpFile = unixFtpEntryParser.parseFTPEntry(fileEntry);
				if (ftpFile != null)
					lfiles.add(ftpFile);
			}
			for (FTPFile fileName : lfiles) {
				if (fileName.getName().endsWith(".zip") || fileName.getName().endsWith(".tar"))
					continue;
				FileInfoVb fileInfoVb = new FileInfoVb();
				fileInfoVb.setName(fileName.getName());
				fileInfoVb.setSize(formatFileSize(fileName.getSize()));
				/*
				 * if(dirType == 1){ fileInfoVb.setDate(formatDate(fileName));
				 * lFileList.add(fileInfoVb); }
				 */
				if (dirType == 1 && (fileInfoVb.getName().startsWith("VISION_UPLOAD")
						&& StringUtils.countOccurrencesOf(fileInfoVb.getName(), "_") > 0)) {
					Calendar cal = new GregorianCalendar();
					int month = cal.get(Calendar.MONTH) + 1;
					int year = cal.get(Calendar.YEAR);
					int day = cal.get(Calendar.DAY_OF_MONTH);
					fileInfoVb.setDate(CommonUtils.getFixedLength(String.valueOf(day), "0", 2) + "-"
							+ CommonUtils.getFixedLength(String.valueOf(month), "0", 2) + "-" + year);
					lFileList.add(fileInfoVb);
				} else if (dirType == 2 && (fileInfoVb.getName().startsWith("BUILDCRON")
						&& StringUtils.countOccurrencesOf(fileInfoVb.getName(), "-") <= 0)) {
					Calendar cal = new GregorianCalendar();
					int month = cal.get(Calendar.MONTH) + 1;
					int year = cal.get(Calendar.YEAR);
					int day = cal.get(Calendar.DAY_OF_MONTH);
					fileInfoVb.setDate(CommonUtils.getFixedLength(String.valueOf(day), "0", 2) + "-"
							+ CommonUtils.getFixedLength(String.valueOf(month), "0", 2) + "-" + year);
					lFileList.add(fileInfoVb);
				} else if ((dirType == 2 || dirType == 1)
						&& (StringUtils.countOccurrencesOf(fileInfoVb.getName(), "-") > 0)) {
					fileInfoVb.setDate(formatDate1(fileName));
					lFileList.add(fileInfoVb);
				}
			}
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.SUCCESSFUL_OPERATION, "Download", "");
//	    	exceptionCode.setResponse(createParentChildRelations(createData(),orderBy,dirType));
			exceptionCode.setResponse(createParentChildRelations(lFileList, orderBy, dirType));
		} catch (FileNotFoundException e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download",
					"");
			throw new RuntimeCustomException(exceptionCode);
		} catch (IOException e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download",
					"");
			throw new RuntimeCustomException(exceptionCode);
		} catch (Exception e) {
			e.printStackTrace();
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download",
					"");
			throw new RuntimeCustomException(exceptionCode);
		} finally {
			if (telnetConnection != null && telnetConnection.isConnected()) {
				telnetConnection.disconnect();
				telnetConnection = null;
			}
		}
		return exceptionCode;
	}

	public ExceptionCode listFilesFromSFtpServer(int dirType, String orderBy) {
		ExceptionCode exceptionCode = null;
		try {
			setUploadDownloadDirFromDB();
			JSch jsch = new JSch();
			// jsch.setKnownHosts( getKnownHostsFileName() );
			Session session = jsch.getSession(getUserName(), getHostName());
			{ // "interactive" version // can selectively update specified known_hosts file
				// need to implement UserInfo interface
				// MyUserInfo is a swing implementation provided in
				// examples/Sftp.java in the JSch dist
				UserInfo ui = new MyUserInfo();
				session.setUserInfo(ui); // OR non-interactive version. Relies in host key being in known-hosts file
				session.setPassword(getPassword());
			}
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			if (dirType == DIR_TYPE_DOWNLOAD) {
				sftpChannel.cd(downloadDir); // downloadDir
			} else {
				sftpChannel.cd(buildLogsDir);
			}
			Vector<ChannelSftp.LsEntry> vtc = sftpChannel.ls("*.*");
			sftpChannel.disconnect();
			session.disconnect();
			List<FileInfoVb> lFileList = new ArrayList<FileInfoVb>();
			FTPClientConfig conf = new FTPClientConfig(getServerType());
			conf.setServerTimeZoneId(getTimezoneId());
			UnixFTPEntryParser unixFtpEntryParser = new UnixFTPEntryParser(conf);
			List<FTPFile> lfiles = new ArrayList<FTPFile>(vtc.size());
			for (ChannelSftp.LsEntry lsEntry : vtc) {
				FTPFile file = unixFtpEntryParser.parseFTPEntry(lsEntry.getLongname());
				if (file != null)
					lfiles.add(file);
			}
			for (FTPFile fileName : lfiles) {
				if (fileName.getName().endsWith(".zip") || fileName.getName().endsWith(".tar"))
					continue;
				FileInfoVb fileInfoVb = new FileInfoVb();
				fileInfoVb.setName(fileName.getName());
				fileInfoVb.setSize(formatFileSize(fileName.getSize()));
				/*
				 * if(dirType == 1){ fileInfoVb.setDate(formatDate(fileName));
				 * lFileList.add(fileInfoVb); }
				 */
				if (dirType == 1 && (fileInfoVb.getName().startsWith("VISION_UPLOAD")
						&& StringUtils.countOccurrencesOf(fileInfoVb.getName(), "_") > 0)) {
					Calendar cal = new GregorianCalendar();
					int month = cal.get(Calendar.MONTH) + 1;
					int year = cal.get(Calendar.YEAR);
					int day = cal.get(Calendar.DAY_OF_MONTH);
					fileInfoVb.setDate(CommonUtils.getFixedLength(String.valueOf(day), "0", 2) + "-"
							+ CommonUtils.getFixedLength(String.valueOf(month), "0", 2) + "-" + year);
					lFileList.add(fileInfoVb);
				} else if (dirType == 2 && (fileInfoVb.getName().startsWith("BUILDCRON")
						&& StringUtils.countOccurrencesOf(fileInfoVb.getName(), "-") <= 0)) {
					Calendar cal = new GregorianCalendar();
					int month = cal.get(Calendar.MONTH) + 1;
					int year = cal.get(Calendar.YEAR);
					int day = cal.get(Calendar.DAY_OF_MONTH);
					fileInfoVb.setDate(CommonUtils.getFixedLength(String.valueOf(day), "0", 2) + "-"
							+ CommonUtils.getFixedLength(String.valueOf(month), "0", 2) + "-" + year);
					lFileList.add(fileInfoVb);
				} else if ((dirType == 2 || dirType == 1)
						&& (StringUtils.countOccurrencesOf(fileInfoVb.getName(), "-") > 0)) {
					fileInfoVb.setDate(formatDate1(fileName));
					lFileList.add(fileInfoVb);
				}
			}
			exceptionCode = new ExceptionCode();
			exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
			exceptionCode.setErrorMsg("Download");
			// exceptionCode = CommonUtils.getResultObject(SERVICE_NAME,
			// Constants.SUCCESSFUL_OPERATION, "Download", "");
			exceptionCode.setResponse(createParentChildRelations(lFileList, orderBy, dirType));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download",
					"");
			throw new RuntimeCustomException(exceptionCode);
		} catch (IOException e) {
			e.printStackTrace();
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download",
					"");
			throw new RuntimeCustomException(exceptionCode);
		} catch (Exception e) {
			e.printStackTrace();
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download",
					"");
			throw new RuntimeCustomException(exceptionCode);
		}
		return exceptionCode;
	}

	private String formatFileSize(long numSize) {
		String strReturn;
		BigDecimal lSize = BigDecimal.valueOf((numSize)).divide(BigDecimal.valueOf(1024), 1, BigDecimal.ROUND_HALF_UP);
		if (lSize.floatValue() <= 0) {
			lSize = BigDecimal.valueOf(1);
		}
		strReturn = lSize + " KB";
		if (lSize.floatValue() > 1024) {
			lSize = lSize.divide(BigDecimal.valueOf(1024), 1, BigDecimal.ROUND_HALF_UP);
			strReturn = lSize + " MB";
			if (lSize.floatValue() > 1024) {
				lSize = lSize.divide(BigDecimal.valueOf(1024), 1, BigDecimal.ROUND_HALF_UP);
				strReturn = lSize + " GB";
			}
		}
		return strReturn;
	}

	private String formatDate1(FTPFile fileName) {
		String fileName1 = fileName.getName();
		String year = fileName1.substring(fileName1.length() - 14, fileName1.length() - 10);
		String month = fileName1.substring(fileName1.length() - 9, fileName1.length() - 7);
		String day = fileName1.substring(fileName1.length() - 6, fileName1.length() - 4);
		return CommonUtils.getFixedLength(day, "0", 2) + "-" + CommonUtils.getFixedLength(month, "0", 2) + "-" + year;
	}

	private FTPClient getConnection() throws IOException {
		FTPClient ftpClient = new FTPClient();
		FTPClientConfig conf = new FTPClientConfig(getServerType());
		conf.setServerTimeZoneId(getTimezoneId());
		ftpClient.configure(conf);
		return ftpClient;
	}

	public void setUploadDownloadDirFromDB() {
		String uploadLogFilePathFromDB = getVisionVariableValue("VISION_XLUPL_LOG_FILE_PATH");
		if (uploadLogFilePathFromDB != null && !uploadLogFilePathFromDB.isEmpty()) {
			downloadDir = uploadLogFilePathFromDB;
		}
		String uploadDataFilePathFromDB = getVisionVariableValue("VISION_XLUPL_DATA_FILE_PATH");
		if (uploadDataFilePathFromDB != null && !uploadDataFilePathFromDB.isEmpty()) {
			uploadDir = uploadDataFilePathFromDB;
		}
		String buildLogsFilePathFromDB = getVisionVariableValue("BUILDCRON_LOG_FILE_PATH");
		if (buildLogsFilePathFromDB != null && !buildLogsFilePathFromDB.isEmpty()) {
			buildLogsDir = buildLogsFilePathFromDB;
		}
		String scriptPathFromDB = getVisionVariableValue("BUILDCRON_SCRIPTS_PATH");
		if (scriptPathFromDB != null && !scriptPathFromDB.isEmpty()) {
			scriptDir = scriptPathFromDB;
		}
		String uploadFileChkIntervelFromDB = getVisionVariableValue("VISION_XLUPL_FILE_CHK_INTVL");
		if (uploadFileChkIntervelFromDB != null && !uploadFileChkIntervelFromDB.isEmpty()) {
			if (ValidationUtil.isValidId(uploadFileChkIntervelFromDB))
				uploadFileChkIntervel = Integer.valueOf(uploadFileChkIntervelFromDB);
		}
		String uploadDataFilePathFromCB = getVisionVariableValue("VISION_XLCB_DATA_FILE_PATH");
		if (uploadDataFilePathFromCB != null && !uploadDataFilePathFromCB.isEmpty()) {
			cbDir = uploadDataFilePathFromCB;
		}
		String excelUploadPathDB = getVisionVariableValue("VISION_XLUPL_PATH");
		if (excelUploadPathDB != null && !excelUploadPathDB.isEmpty()) {
			uploadDirExl = excelUploadPathDB;
		}
		String connectorUploadPathDB = getVisionVariableValue("VISION_CONNECTOR_DATA_FILE_PATH");
//		connectorUploadPathDB="C:\\connectorsData\\";
		if (connectorUploadPathDB != null && !connectorUploadPathDB.isEmpty()) {
			connectorUploadDir = connectorUploadPathDB;
		}
		String connectorADUploadPathDB = getVisionVariableValue("VISION_CONNECTOR_AD_DATA_FILE_PATH");
//		connectorADUploadPathDB="C:\\connectorsDataAD\\";
		if (connectorADUploadPathDB != null && !connectorADUploadPathDB.isEmpty()) {
			connectorADUploadDir = connectorADUploadPathDB;
		}
	}

	public ExceptionCode insertRecord(ExceptionCode pRequestCode, List<VisionUploadVb> vObjects) {
		ExceptionCode exceptionCode = null;
		DeepCopy<VisionUploadVb> deepCopy = new DeepCopy<VisionUploadVb>();
		List<VisionUploadVb> clonedObject = null;
		VisionUploadVb vObject = null;
		try {
			setAtNtValues(vObjects);
			vObject = (VisionUploadVb) pRequestCode.getOtherInfo();
			setVerifReqDeleteType(vObject);
			clonedObject = deepCopy.copyCollection(vObjects);
			doFormateData(vObjects);
			// exceptionCode = doValidate(vObject, vObjects);
			if (exceptionCode != null && exceptionCode.getErrorMsg() != "") {
				return exceptionCode;
			}
			exceptionCode = getScreenDao().doInsertRecord(vObjects);
			exceptionCode.setOtherInfo(vObject);
			exceptionCode.setResponse(vObjects);
			return exceptionCode;
		} catch (RuntimeCustomException rex) {
			logger.error("Insert Exception " + rex.getCode().getErrorMsg());
			exceptionCode = rex.getCode();
			exceptionCode.setResponse(clonedObject);
			exceptionCode.setOtherInfo(vObject);
			return exceptionCode;
		}
	}

	private BufferedInputStream downloadFilesFromFTP(String pFileNames, int dirType) {
		BufferedInputStream bufferedInputStream = null;
		TelnetConnection telnetConnection = null;
		String[] fileNames = pFileNames.split(" @- ");
		setUploadDownloadDirFromDB();
		FTPClient ftpClient = null;
		try {
			ftpClient = getConnection();
			ftpClient.connect(getHostName());
			boolean response = ftpClient.login(getUserName(), getPassword());
			if (!response) {
				ftpClient.disconnect();
				throw new FTPConnectionClosedException("Unable to connect to Remote Computer");
			}
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			if (dirType == DIR_TYPE_DOWNLOAD)
				response = ftpClient.changeWorkingDirectory(downloadDir);
			else
				response = ftpClient.changeWorkingDirectory(buildLogsDir);
			if (!response) {
				ftpClient.disconnect();
				throw new FTPConnectionClosedException("Unable to connect to Remote Computer");
			}
			if (fileNames.length == 1) {
				bufferedInputStream = new BufferedInputStream(ftpClient.retrieveFileStream(fileNames[0]));
			} else if (fileNames.length > 1) {
				telnetConnection = new TelnetConnection(hostName, userName, password, prompt);
				telnetConnection.connect();
				if (dirType == DIR_TYPE_DOWNLOAD)
					telnetConnection.sendCommand("cd " + downloadDir);
				else
					telnetConnection.sendCommand("cd " + buildLogsDir);
				// for(int i=0;i<fileNames.length;i++){
				for (int i = 0; i < fileNames.length; i++) {
					telnetConnection.sendCommand("echo " + fileNames[i] + " >> example.txt");
				}
				telnetConnection.sendCommand("tar cvf logs.tar `cat example.txt`");
				telnetConnection.sendCommand("rm example.txt");
				ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
				if (dirType == DIR_TYPE_DOWNLOAD)
					response = ftpClient.changeWorkingDirectory(downloadDir);
				else
					response = ftpClient.changeWorkingDirectory(buildLogsDir);
				if (!response) {
					ftpClient.disconnect();
					throw new FTPConnectionClosedException();
				}
				bufferedInputStream = new BufferedInputStream(ftpClient.retrieveFileStream("logs.tar"));
			} else {
				throw new FileNotFoundException("File not found on the Server.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ftpClient != null && ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ftpClient = null;
			}
			if (telnetConnection != null && telnetConnection.isConnected()) {
				telnetConnection.disconnect();
			}
		}
		return bufferedInputStream;
	}

	public BufferedInputStream downloadFilesFromSFTP(String pFileNames, int dirType) {
		BufferedInputStream bufferedInputStream = null;
		String[] fileNames = pFileNames.split(" @- ");
		setUploadDownloadDirFromDB();
		Session session = null;
		try {
			JSch jsch = new JSch();
			// jsch.setKnownHosts(getKnownHostsFileName());
			session = jsch.getSession(getUserName(), getHostName());
			{ // "interactive" version // can selectively update specified known_hosts file
				// need to implement UserInfo interface
				// MyUserInfo is a swing implementation provided in
				// examples/Sftp.java in the JSch dist
				UserInfo ui = new MyUserInfo();
				session.setUserInfo(ui); // OR non-interactive version. Relies in host key being in known-hosts file
				session.setPassword(getPassword());
			}
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			if (dirType == DIR_TYPE_DOWNLOAD)
				sftpChannel.cd(downloadDir);
			else
				sftpChannel.cd(buildLogsDir);
			if (fileNames.length == 1) {
				try {
					InputStream ins = sftpChannel.get(fileNames[0]);
					bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(IOUtils.toByteArray(ins)));
				} catch (SftpException e) {
				}
			} else if (fileNames.length > 1) {
				channel = session.openChannel("shell");
				OutputStream inputstream_for_the_channel = channel.getOutputStream();
				PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
				channel.connect();
				if (dirType == DIR_TYPE_DOWNLOAD)
					commander.println("cd " + downloadDir + "\n");
				else
					commander.println("cd " + buildLogsDir + "\n");
				// for(int i=0;i<fileNames.length;i++){
				for (int i = 0; i < fileNames.length; i++) {
					commander.println("echo " + fileNames[i] + " >> example.txt" + "\n");
				}
				commander.println("tar cvf logs.tar `cat example.txt`" + "\n");
				commander.println("rm example.txt");
				commander.println("exit");
				commander.close();
				do {
					Thread.sleep(1000);
				} while (!channel.isEOF());
				try {
					InputStream ins = sftpChannel.get("logs.tar");
					bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(IOUtils.toByteArray(ins)));
				} catch (SftpException ex) {
				}
			} else {
				throw new FileNotFoundException("File not found on the Server.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		return bufferedInputStream;
	}

	protected ExceptionCode doValidate(VisionUploadVb pObject, List<VisionUploadVb> vObjects) {
		ExceptionCode exceptionCode = null;
		long currentUser = SessionContextHolder.getContext().getVisionId();
		FTPClient ftpClient;
		try {
			if (isSecuredFtp()) {
				return doValidateSFtp(pObject, vObjects);
			}
			ftpClient = getConnection();
			ftpClient.connect(getHostName());
			boolean response = ftpClient.login(getUserName(), getPassword());
			if (!response) {
				ftpClient.disconnect();
				exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Add","");
				throw new RuntimeCustomException(exceptionCode);
			}
			ftpClient.setFileType(fileType);
			response = ftpClient.changeWorkingDirectory(uploadDir);
			if (!response) {
				ftpClient.disconnect();
				exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Add","");
				throw new RuntimeCustomException(exceptionCode);
			}
			long maxIntervel = 0;
			for (VisionUploadVb lVUploadVb : vObjects) {
				if (lVUploadVb.isChecked()) {
					setVerifReqDeleteType(lVUploadVb);
					int countOfUplTables = getVisionUploadDao().getCountOfUploadTables(lVUploadVb);
					lVUploadVb.setMaker(currentUser);
					if (countOfUplTables <= 0) {
						String strErrorDesc = "No sufficient privileges to upload for the Table["
								+ lVUploadVb.getTableName() + "]";
						exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.WE_HAVE_ERROR_DESCRIPTION,
								"Insert", strErrorDesc);
						throw new RuntimeCustomException(exceptionCode);
					}
					FTPFile file = isFileExists(lVUploadVb.getFileName().toUpperCase() + "_" + currentUser + ".txt",
							ftpClient);
					if (file == null) {
						String strErrorDesc = lVUploadVb.getFileName()
								+ ".txt does not exists. Please upload the file first.";
						exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.WE_HAVE_ERROR_DESCRIPTION,
								"Insert", strErrorDesc);
						throw new RuntimeCustomException(exceptionCode);
					}
					maxIntervel = Math.max(
							(((System.currentTimeMillis() / 1000) - file.getTimestamp().getTimeInMillis()) / 60),
							maxIntervel);
				}
			}
			if (maxIntervel >= uploadFileChkIntervel && pObject.getCheckUploadInterval() == false) {
				String strErrorDesc = "Upload file(s) is more than " + uploadFileChkIntervel
						+ " mins old. Do you want to continue with upload?";
				exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.WE_HAVE_ERROR_DESCRIPTION, "Insert",
						strErrorDesc);
				throw new RuntimeCustomException(exceptionCode);
			}
			ftpClient.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Add", "");
			throw new RuntimeCustomException(exceptionCode);
		}
		return exceptionCode;
	}

	protected ExceptionCode doValidateSFtp(VisionUploadVb pObject, List<VisionUploadVb> vObjects) {
		ExceptionCode exceptionCode = null;
		long currentUser = SessionContextHolder.getContext().getVisionId();
		try {
			JSch jsch = new JSch();
			jsch.setKnownHosts(getKnownHostsFileName());
			Session session = jsch.getSession(getUserName(), getHostName());
			{ // "interactive" version // can selectively update specified known_hosts file
				// need to implement UserInfo interface
				// MyUserInfo is a swing implementation provided in
				// examples/Sftp.java in the JSch dist
				UserInfo ui = new MyUserInfo();
				session.setUserInfo(ui); // OR non-interactive version. Relies in host key being in known-hosts file
				session.setPassword(getPassword());
			}
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.cd(uploadDir);
			long maxIntervel = 0;
			for (VisionUploadVb lVUploadVb : vObjects) {
				if (lVUploadVb.isChecked()) {
					setVerifReqDeleteType(lVUploadVb);
					int countOfUplTables = getVisionUploadDao().getCountOfUploadTables(lVUploadVb);
					lVUploadVb.setMaker(currentUser);
					if (countOfUplTables <= 0) {
						String strErrorDesc = "No sufficient privileges to upload for the Table["
								+ lVUploadVb.getTableName() + "]";
						exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.WE_HAVE_ERROR_DESCRIPTION,
								"Insert", strErrorDesc);
						throw new RuntimeCustomException(exceptionCode);
					}
					ChannelSftp.LsEntry entry = isFileExists(
							lVUploadVb.getFileName().toUpperCase() + "_" + currentUser + ".txt", sftpChannel);
					if (entry == null) {
						String strErrorDesc = lVUploadVb.getFileName()
								+ ".txt does not exists. Please upload the file first.";
						exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.WE_HAVE_ERROR_DESCRIPTION,
								"Insert", strErrorDesc);
						throw new RuntimeCustomException(exceptionCode);
					}
					maxIntervel = Math.max((((System.currentTimeMillis() / 1000) - entry.getAttrs().getMTime()) / 60),
							maxIntervel);
				}
			}
			if (maxIntervel >= uploadFileChkIntervel && pObject.getCheckUploadInterval() == false) {
				String strErrorDesc = "Upload file(s) is more than " + uploadFileChkIntervel
						+ " mins old. Do you want to continue with upload?";
				exceptionCode = new ExceptionCode();
				exceptionCode.setErrorSevr("W");
				exceptionCode.setErrorCode(Constants.WE_HAVE_ERROR_DESCRIPTION);
				exceptionCode.setErrorMsg(strErrorDesc);
				throw new RuntimeCustomException(exceptionCode);
			}
			sftpChannel.disconnect();
			session.disconnect();
		} catch (SftpException e) {
			e.printStackTrace();
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Add", "");
			;
			throw new RuntimeCustomException(exceptionCode);
		} catch (JSchException e) {
			e.printStackTrace();
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Add", "");
			;
			throw new RuntimeCustomException(exceptionCode);
		}
		return exceptionCode;
	}

	private List<FileInfoVb> createParentChildRelations(List<FileInfoVb> legalTreeList, String orderBy, int dirType)
			throws IOException {
		List<FileInfoVb> lResult = new ArrayList<FileInfoVb>(0);
		Set set = new HashSet<FileInfoVb>();
		// Top Roots are added.
		for (FileInfoVb fileVb : legalTreeList) {
			if ("Date".equalsIgnoreCase(orderBy)) {
				// String date = fileVb.getName().substring(fileVb.getName().length()-14,
				// fileVb.getName().length()-4);
				if (fileVb.getDate() != null && fileVb.getDate() != "") {
					set.add(fileVb.getDate());
				}
			} else {
				String fileNeme = "";
				if (dirType == 2) {
					int cout = StringUtils.countOccurrencesOf(fileVb.getName(), "_");
					if (cout == 0) {
						int cout1 = StringUtils.countOccurrencesOf(fileVb.getName(), "-");
						if (cout1 == 0)
							fileNeme = fileVb.getName().substring(0, fileVb.getName().length() - 4);
						else
							fileNeme = fileVb.getName().substring(0, fileVb.getName().length() - 15);
					} else if (cout > 1)
						fileNeme = fileVb.getName().substring(0, fileVb.getName().length() - 21);
				} else {
					int cout = StringUtils.countOccurrencesOf(fileVb.getName(), "_");
					if (cout == 1) {
						fileNeme = fileVb.getName().substring(0, fileVb.getName().length() - 4);
					} else if (cout > 1) {
						fileNeme = fileVb.getName().substring(0, fileVb.getName().length() - 20);
					}
				}
				if (fileNeme != null && fileNeme.length() > 0) {
					set.add(fileNeme);
				}
			}
		}
		for (Iterator iterator = set.iterator(); iterator.hasNext();) {
			String date = (String) iterator.next();
			FileInfoVb fileVb = new FileInfoVb();
			fileVb.setDescription(date);
			lResult.add(fileVb);
		}
		// For each top node add all child's and to that child's add sub child's
		// recursively.
		for (FileInfoVb legalVb : lResult) {
			addChilds(legalVb, legalTreeList, orderBy, dirType);
		}
		if ("Date".equalsIgnoreCase(orderBy)) {
			final SimpleDateFormat dtF = new SimpleDateFormat("dd-MM-yyyy");
			// set the empty lists to null. this is required for UI to display the leaf
			// nodes properly.
			Collections.sort(lResult, new Comparator<FileInfoVb>() {
				public int compare(FileInfoVb m1, FileInfoVb m2) {
					try {
						return dtF.parse(m1.getDescription()).compareTo(dtF.parse(m2.getDescription()));
					} catch (ParseException e) {
						return 0;
					}
				}
			});
			Collections.reverse(lResult);
		} else {
			Collections.sort(lResult, new Comparator<FileInfoVb>() {
				public int compare(FileInfoVb m1, FileInfoVb m2) {
					return m1.getDescription().compareTo(m2.getDescription());
				}
			});
		}
		return lResult;
	}

	public void addChilds(FileInfoVb vObject, List<FileInfoVb> legalTreeListCopy, String orderBy, int dirType) {
		for (FileInfoVb fileTreeVb : legalTreeListCopy) {
			if ("Date".equalsIgnoreCase(orderBy)) {
				// String date =
				// fileTreeVb.getName().substring(fileTreeVb.getName().length()-14,
				// fileTreeVb.getName().length()-4);
				if (vObject.getDescription().equalsIgnoreCase(fileTreeVb.getDate())) {
					if (vObject.getChildren() == null) {
						vObject.setChildren(new ArrayList<FileInfoVb>(0));
					}
					fileTreeVb.setDescription(fileTreeVb.getName());
					vObject.getChildren().add(fileTreeVb);
				}
			} else {
				String fileNeme = "";
				if (dirType == 2) {
					int cout = StringUtils.countOccurrencesOf(fileTreeVb.getName(), "_");
					if (cout == 0) {
						int cout1 = StringUtils.countOccurrencesOf(fileTreeVb.getName(), "-");
						if (cout1 == 0)
							fileNeme = fileTreeVb.getName().substring(0, fileTreeVb.getName().length() - 4);
						else
							fileNeme = fileTreeVb.getName().substring(0, fileTreeVb.getName().length() - 15);
					} else if (cout > 1)
						fileNeme = fileTreeVb.getName().substring(0, fileTreeVb.getName().length() - 21);
				} else {
					int cout = StringUtils.countOccurrencesOf(fileTreeVb.getName(), "_");
					if (cout == 1) {
						fileNeme = fileTreeVb.getName().substring(0, fileTreeVb.getName().length() - 4);
					} else if (cout > 1) {
						fileNeme = fileTreeVb.getName().substring(0, fileTreeVb.getName().length() - 20);
					}
				}
				if (vObject.getDescription().equalsIgnoreCase(fileNeme)) {
					if (vObject.getChildren() == null) {
						vObject.setChildren(new ArrayList<FileInfoVb>(0));
					}
					fileTreeVb.setDescription(fileTreeVb.getName());
					vObject.getChildren().add(fileTreeVb);
				}
			}
		}
	}

	public ExceptionCode fileDownload(int dirType, String fileNames, String strBuildNumber, String strBuild,
			String fileExtension) {
		ExceptionCode exceptionCode = null;
		try {
			String filePath = System.getProperty("java.io.tmpdir");
			if (!ValidationUtil.isValid(filePath)) {
				filePath = System.getenv("TMP");
			}
			if (ValidationUtil.isValid(filePath)) {
				filePath = filePath + File.separator;
			}
			System.out.print("File Path:" + filePath);
			if (dirType == 3) {
				BufferedInputStream in = null;
				File file = null;
				FileOutputStream fos = null;
				String extension = "";
				FileInfoVb fileInfoVb = new FileInfoVb();
				BuildSchedulesVb buildSchedulesVb = getBuildSchedulesDao().getQueryResultsForDetails(strBuildNumber,
						strBuild);
				String startTime = buildSchedulesVb.getStartTime();
				String status = buildSchedulesVb.getBuildScheduleStatus();
				if (startTime != null && !startTime.isEmpty()
						&& ("E".equalsIgnoreCase(status) || "I".equalsIgnoreCase(status))) {
					if (startTime.indexOf(" ") > 0) {
						startTime = startTime.substring(0, startTime.indexOf(" "));
					}
					startTime = startTime.substring(6) + "-" + startTime.substring(3, 5) + "-"
							+ startTime.substring(0, 2);
					if ("ZZ".equalsIgnoreCase(buildSchedulesVb.getCountry())
							&& buildSchedulesDao.checkExpandFlagFor(buildSchedulesVb) > 1) {
						StringBuffer fileName = new StringBuffer();
						fileName.append(buildSchedulesVb.getBuild()).append("_").append(buildSchedulesVb.getCountry())
								.append("_").append(buildSchedulesVb.getLeBook()).append("_").append(startTime)
								.append(".zip");
						StringBuffer command = new StringBuffer();
						command.append(buildSchedulesVb.getBuild()).append("_").append("*").append("_")
								.append(startTime).append(".log");
						fileInfoVb = new FileInfoVb();
						fileInfoVb.setName(fileName.toString());
						in = getLogFiles(fileInfoVb, command.toString());
						file = new File(filePath + fileInfoVb.getName());
						fos = new FileOutputStream(file);
					} else {
						StringBuffer fileName = new StringBuffer();
						fileName.append(buildSchedulesVb.getBuild()).append("_").append(buildSchedulesVb.getCountry())
								.append("_").append(buildSchedulesVb.getLeBook()).append("_").append(startTime)
								.append(".log");
						extension = ".txt";
						fileInfoVb.setName(fileName.toString());
						in = getLogFile(fileInfoVb);
						file = new File(filePath + fileInfoVb.getName() + extension);
						fos = new FileOutputStream(file);
					}
					int bit = 4096;
					while ((bit) >= 0) {
						bit = in.read();
						fos.write(bit);
					}
					fos.close();
					exceptionCode = CommonUtils.getResultObject("", 1, "", "");
					exceptionCode.setResponse(1);
					exceptionCode.setRequest(fileInfoVb.getName() + extension);
					in.close();
				} else {
					exceptionCode = CommonUtils.getResultObject("", Constants.ERRONEOUS_OPERATION, "", "");
					exceptionCode.setResponse(1);
				}
			} else {
				BufferedInputStream in = null;
				File file = null;
				FileOutputStream fos = null;
				String fName = "";
				int number = 0;
				// fileNames = fileNames.substring(0,fileNames.length()-4);
				String[] arrFileNames = fileNames.split(" @- ");
				if (arrFileNames.length == 1) {
					fName = arrFileNames[0] + ".txt";
					// fName = arrFileNames[0];
					file = new File(filePath + fName);
					fos = new FileOutputStream(file);
					if (isSecuredFtp())
						in = downloadFilesFromSFTP(fileNames, dirType);
					else
						in = downloadFilesFromFTP(fileNames, dirType);
					number = 1;
				} else if (arrFileNames.length != 1) {
					if (isSecuredFtp())
						in = downloadFilesFromSFTP(fileNames, dirType);
					else
						in = downloadFilesFromFTP(fileNames, dirType);
					file = new File(filePath + "logs.tar");
					fos = new FileOutputStream(file);
					fName = "logs.tar";
					number = 2;
				} else {
					throw new FileNotFoundException("File not found on the Server.");
				}
				int bit = 4096;
				while ((bit) >= 0) {
					bit = in.read();
					fos.write(bit);
				}
				in.close();
				fos.close();
				// exceptionCode = CommonUtils.getResultObject("", 1, "", "");
				exceptionCode = new ExceptionCode();
				exceptionCode.setErrorCode(1);
				exceptionCode.setErrorMsg("Sucess");
				exceptionCode.setRequest(fName);
				exceptionCode.setResponse(number);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return exceptionCode;
	}

	public BufferedInputStream getLogFile(FileInfoVb fileInfoVb) {
		FTPClient ftpClient = null;
		BufferedInputStream input = null;
		try {
			setUploadDownloadDirFromDB();
			if (isSecuredFtp()) {
				return getLogFileFromSFTP(fileInfoVb);
			}
			ftpClient = getConnection();
			ftpClient.connect(getHostName());
			boolean response = ftpClient.login(getUserName(), getPassword());
			if (!response) {
				logger.error("Unable to login to the FTP Server.");
				return null;
			}
			response = ftpClient.changeWorkingDirectory(buildLogsDir);
			if (!response) {
				logger.error("Unable to login to the FTP Server.");
				return null;
			}
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			input = new BufferedInputStream(ftpClient.retrieveFileStream(fileInfoVb.getName()));
			if (input == null) {
				input = new BufferedInputStream(ftpClient.retrieveFileStream("BUILDCRON.log"));
				fileInfoVb.setName("BUILDCRON.log");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Unable to login to the FTP Server.");
			return null;
		} finally {
			if (ftpClient != null)
				try {
					ftpClient.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return input;
	}

	public BufferedInputStream getLogFileFromSFTP(FileInfoVb fileInfoVb) {
		BufferedInputStream input = null;
		Session session = null;
		try {
			setUploadDownloadDirFromDB();
			JSch jsch = new JSch();
			jsch.setKnownHosts(getKnownHostsFileName());
			session = jsch.getSession(getUserName(), getHostName());
			{ // "interactive" version // can selectively update specified known_hosts file
				// need to implement UserInfo interface
				// MyUserInfo is a swing implementation provided in
				// examples/Sftp.java in the JSch dist
				UserInfo ui = new MyUserInfo();
				session.setUserInfo(ui); // OR non-interactive version. Relies in host key being in known-hosts file
				session.setPassword(getPassword());
			}
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.cd(buildLogsDir);
			try {
				InputStream ins = sftpChannel.get(fileInfoVb.getName());
				input = new BufferedInputStream(new ByteArrayInputStream(IOUtils.toByteArray(ins)));
			} catch (SftpException ex) {
				try {
					if (input == null) {
						InputStream ins = sftpChannel.get("BUILDCRON.log");
						input = new BufferedInputStream(new ByteArrayInputStream(IOUtils.toByteArray(ins)));
						fileInfoVb.setName("BUILDCRON.log");
					}
				} catch (SftpException ex1) {

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Unable to login to the FTP Server.");
			return null;
		} finally {
			if (session != null)
				session.disconnect();
		}
		return input;
	}

	public BufferedInputStream getLogFilesFromSFTP(FileInfoVb fileInfoVb, String command) {
		BufferedInputStream input = null;
		Session session = null;
		Channel shellChannel = null;
		try {
			setUploadDownloadDirFromDB();
			JSch jsch = new JSch();
			jsch.setKnownHosts(getKnownHostsFileName());
			session = jsch.getSession(getUserName(), getHostName());
			{ // "interactive" version // can selectively update specified known_hosts file
				// need to implement UserInfo interface
				// MyUserInfo is a swing implementation provided in
				// examples/Sftp.java in the JSch dist
				UserInfo ui = new MyUserInfo();
				session.setUserInfo(ui); // OR non-interactive version. Relies in host key being in known-hosts file
				session.setPassword(getPassword());
			}
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			shellChannel = session.openChannel("shell");
			OutputStream inputstream_for_the_channel = shellChannel.getOutputStream();
			PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
			shellChannel.connect();
			commander.println("cd " + buildLogsDir);
			commander.println("tar -cvf " + fileInfoVb.getName() + " " + command);
			commander.println("exit");
			commander.close();
			do {
				Thread.sleep(1000);
			} while (!shellChannel.isEOF());
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.cd(buildLogsDir);
			try {
				InputStream ins = sftpChannel.get(fileInfoVb.getName());
				input = new BufferedInputStream(new ByteArrayInputStream(IOUtils.toByteArray(ins)));
			} catch (SftpException exp) {
				try {
					InputStream ins = sftpChannel.get("BUILDCRON.log");
					input = new BufferedInputStream(new ByteArrayInputStream(IOUtils.toByteArray(ins)));
					fileInfoVb.setName("BUILDCRON.log");
				} catch (SftpException ex) {

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Unable to login to the FTP Server.");
			return null;
		} finally {
			if (session != null)
				session.disconnect();
		}
		return input;
	}

	public BufferedInputStream getLogFiles(FileInfoVb fileInfoVb, String command) {
		FTPClient ftpClient = null;
		TelnetConnection telnetConnection = null;
		BufferedInputStream input = null;
		try {
			setUploadDownloadDirFromDB();
			if (isSecuredFtp()) {
				return getLogFilesFromSFTP(fileInfoVb, command);
			}
			telnetConnection = new TelnetConnection(hostName, userName, password, prompt);
			telnetConnection.connect();
			telnetConnection.sendCommand("cd " + buildLogsDir);
			telnetConnection.sendCommand("tar -cvf " + fileInfoVb.getName() + " " + command);
			ftpClient = getConnection();
			ftpClient.connect(getHostName());
			boolean response = ftpClient.login(getUserName(), getPassword());
			if (!response) {
				logger.error("Unable to login to the FTP Server.");
				return null;
			}
			response = ftpClient.changeWorkingDirectory(buildLogsDir);
			if (!response) {
				logger.error("Unable to login to the FTP Server.");
				return null;
			}
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			input = new BufferedInputStream(ftpClient.retrieveFileStream(fileInfoVb.getName()));
			if (input == null) {
				input = new BufferedInputStream(ftpClient.retrieveFileStream("BUILDCRON.log"));
				fileInfoVb.setName("BUILDCRON.log");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Unable to login to the FTP Server.");
			return null;
		} finally {
			if (ftpClient != null)
				try {
					ftpClient.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (telnetConnection != null && telnetConnection.isConnected()) {
				telnetConnection.disconnect();
			}
		}
		return input;
	}

	private FTPFile isFileExists(String strFileName, FTPClient ftpClient) throws IOException {
		FTPFile[] tmp = ftpClient.listFiles(strFileName);
		if (tmp != null && tmp.length > 0)
			return tmp[0];
		return null;
	}

	private ChannelSftp.LsEntry isFileExists(String strFileName, ChannelSftp ftpClient) throws SftpException {
		try {
			Vector<ChannelSftp.LsEntry> lvec = ftpClient.ls(strFileName);
			return (lvec != null && lvec.size() > 0) ? lvec.get(0) : null;
		} catch (SftpException e) {
		}
		return null;
	}

	@Override
	protected AbstractDao<VisionUploadVb> getScreenDao() {
		return visionUploadDao;
	}

	@Override
	protected void setAtNtValues(VisionUploadVb object) {
		object.setUploadStatusNt(10);
	}

	@Override
	protected void setVerifReqDeleteType(VisionUploadVb object) {
		object.setVerificationRequired(false);
		object.setStaticDelete(false);
	}

	public ExceptionCode doTemplateUpload(String fileName, byte[] data) {
		ExceptionCode exceptionCode = null;
		File lfile = null;
		FileChannel source = null;
		FileChannel destination = null;
		try {
			setUploadDownloadDirFromDB();
			if (securedFtp) {
				JSch jsch = new JSch();
				jsch.setKnownHosts(getKnownHostsFileName());
				Session session = jsch.getSession(getUserName(), getHostName());
				{ // "interactive" version // can selectively update specified known_hosts file
					// need to implement UserInfo interface
					// MyUserInfo is a swing implementation provided in
					// examples/Sftp.java in the JSch dist
					UserInfo ui = new MyUserInfo();
					session.setUserInfo(ui); // OR non-interactive version. Relies in host key being in known-hosts file
					session.setPassword(getPassword());
				}
				session.setConfig("StrictHostKeyChecking", "no");
				session.connect();
				Channel channel = session.openChannel("sftp");
				channel.connect();
				ChannelSftp sftpChannel = (ChannelSftp) channel;
				fileName = fileName.toUpperCase();
				sftpChannel.cd(cbDir);
				InputStream in = new ByteArrayInputStream(data);
				sftpChannel.put(in, fileName);
				sftpChannel.exit();
				channel = session.openChannel("shell");
				OutputStream inputstream_for_the_channel = channel.getOutputStream();
				PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
				channel.connect();
				commander.println("cd " + scriptDir);
				StringBuilder cmd = new StringBuilder();
				commander.println(cmd);
				commander.println("exit");
				commander.close();
				do {
					Thread.sleep(1000);
				} while (!channel.isEOF());
				session.disconnect();
			} else {
				FTPClient ftpClient = getConnection();
				ftpClient.connect(getHostName());
				boolean response = ftpClient.login(getUserName(), getPassword());
				if (!response) {
					ftpClient.disconnect();
					exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR,
							"Upload", "");
					;
					exceptionCode.setResponse(fileName);
					throw new RuntimeCustomException(exceptionCode);
				}
				ftpClient.setFileType(fileType);
				response = ftpClient.changeWorkingDirectory(cbDir);
				if (!response) {
					ftpClient.disconnect();
					exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR,
							"Upload", "");
					;
					exceptionCode.setResponse(fileName);
					throw new RuntimeCustomException(exceptionCode);
				}
				fileName = fileName.toUpperCase();
				fileName = fileName.substring(0, fileName.lastIndexOf('.')) + "_"
						+ SessionContextHolder.getContext().getVisionId() + "." + getFileExtension() + "";
				InputStream in = new ByteArrayInputStream(data);
				ftpClient.storeFile(fileName, in);
				in.close(); // close the io streams
				ftpClient.disconnect();
			}
		} catch (FileNotFoundException e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			;
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		} catch (IOException e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			;
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		} catch (Exception e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			;
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		}
		exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.SUCCESSFUL_OPERATION, "Upload", "");
		exceptionCode.setOtherInfo(fileName);
		return exceptionCode;
	}

	public long getDateTimeInMS(String date, String formate) {
		SimpleDateFormat lFormat = new SimpleDateFormat(formate);
		try {
			Date lDate = lFormat.parse(date);
			return lDate.getTime();
		} catch (Exception e) {
			return System.currentTimeMillis();
		}
	}

	public ExceptionCode uploadMultipartFile(MultipartFile[] files, DSConnectorVb lsData, String macroVar, String curDate) throws IOException {
		ExceptionCode exceptionCode = null;
		FileInfoVb fileInfoVb = new FileInfoVb();
		try {
			for (MultipartFile uploadedFile : files) {
				String extension = FilenameUtils.getExtension(uploadedFile.getOriginalFilename());
				fileInfoVb.setName(uploadedFile.getOriginalFilename().toUpperCase());
				fileInfoVb.setData(uploadedFile.getBytes());
				fileInfoVb.setExtension(extension);
				fileInfoVb.setDate(curDate);
				exceptionCode = doDataConnectorUpload(fileInfoVb, lsData, macroVar);
			}
		} catch (RuntimeCustomException rex) {
			logger.error("Error in upload" + rex.getCode().getErrorMsg());
			logger.error(((fileInfoVb == null) ? "vObject is Null" : fileInfoVb.toString()));
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileInfoVb);
			throw new RuntimeCustomException(exceptionCode);
		}
		return exceptionCode;
	}
	
	public ExceptionCode doDataConnectorUpload(FileInfoVb fileInfoVb, DSConnectorVb lsData, String macroVar) {
		ExceptionCode exceptionCode = new ExceptionCode();
		String fileName = fileInfoVb.getName();
		try {
			setUploadDownloadDirFromDB();
			if(lsData!=null && ValidationUtil.isValid(lsData.getMacroVarScript())) {/*Move File to AD*/
				String upfileName = CommonUtils.getValue(lsData.getMacroVarScript(), "NAME");
				String extension = CommonUtils.getValue(lsData.getMacroVarScript(), "EXTENSION");
				System.out.println("EXISTS DATE:"+lsData.getDateLastModified());
				long lCurrentDate = getDateTimeInMS(lsData.getDateLastModified(), "dd-M-yyyy hh:mm:ss");
				System.out.println("lCurrentDate EXISTS DATE:"+lCurrentDate);
				fileName = upfileName + "_" + lsData.getMaker() + "_" + lsData.getMacroVar() + "_" + lCurrentDate + "." + extension;
				System.out.println("fileName EXISTS DATE:"+fileName);
				File lFile = new File(getConnectorUploadDir() + fileName);
				if(lFile.exists()){
					if (lFile.renameTo(new File(getConnectorADUploadDir()+fileName))) {
						lFile.delete();
					} else {
						System.out.println("Problem during backup process");
					}
				}
			}
			long lCurrentDate = getDateTimeInMS(fileInfoVb.getDate(), "dd-M-yyyy hh:mm:ss");
			fileName = fileInfoVb.getName().substring(0, fileInfoVb.getName().lastIndexOf('.')) + "_" + SessionContextHolder.getContext().getVisionId() + "_" + macroVar + "_" + lCurrentDate + "." + fileInfoVb.getExtension();
			System.out.println("NEW DATE:"+fileInfoVb.getDate());
			System.out.println("lCurrentDate NEW DATE:"+lCurrentDate);
			System.out.println("fileName NEW DATE:"+fileName);
			File lFile = new File(getConnectorUploadDir() + fileName);
			if(lFile.exists()){
				if (lFile.renameTo(new File(getConnectorADUploadDir()+fileName))) {
					lFile.delete();
				} else {
					System.out.println("Problem during backup process");
				}
			}
			lFile.createNewFile();
			FileOutputStream fileOuputStream = null;
		    fileOuputStream = new FileOutputStream(lFile);
		    fileOuputStream.write(fileInfoVb.getData());
			fileOuputStream.flush();
			fileOuputStream.close();
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.SUCCESSFUL_OPERATION, "Upload", "");
			exceptionCode.setOtherInfo(fileInfoVb);
		    return exceptionCode;
		} catch (FileNotFoundException e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		} catch (IOException e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		} catch (Exception e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		}
	}
	
	public ExceptionCode doDataConnectorUploadOld(FileInfoVb fileInfoVb, List<DSConnectorVb> lsData, String macroVar) {
		ExceptionCode exceptionCode = new ExceptionCode();
		String fileName = fileInfoVb.getName();
		String fileExtension = fileInfoVb.getExtension();
		try {
			setUploadDownloadDirFromDB();
			fileName = fileName.toUpperCase(); // Changes done by Deepak for Bank One
			if (fileName.contains(".XLSX") || fileName.contains(".XLS")) {
				fileExtension = "xlsx";
				setUserName(getXlUploaduserName());
				setPassword(getXlUploadpassword());
				setHostName(getXlUploadhostName());
				setUploadDir(getUploadDirExl());
			}

			if (securedFtp) {
				fileName = fileName.toUpperCase();
				JSch jsch = new JSch();
				// jsch.setKnownHosts(getKnownHostsFileName());
				Session session = jsch.getSession(getUserName(), getHostName());
				{
					UserInfo ui = new MyUserInfo();
					session.setUserInfo(ui);
					session.setPassword(getPassword());
				}
				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);
				session.connect();

				Channel channel = session.openChannel("sftp");
				channel = session.openChannel("shell");
				OutputStream inputstream_for_the_channel = channel.getOutputStream();
				PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
				channel.connect();
				commander.println("cd " + connectorUploadDir);
				StringBuilder cmd = new StringBuilder();

				/* Move file to AD */
				if (lsData.size() > 0) {
					String upfileName = CommonUtils.getValue(lsData.get(0).getMacroVarScript(), "NAME");
					String extension = CommonUtils.getValue(lsData.get(0).getMacroVarScript(), "EXTENSION");
					long lCurrentDate = getDateTimeInMS(lsData.get(0).getDateLastModified(), "dd-M-yyyy hh:mm:ss");
					String sourceFile = upfileName.toUpperCase() + "_" + lsData.get(0).getMaker() + "_" + lsData.get(0).getMacroVar() + "_" + lCurrentDate + "." + extension;

					if ('/' != connectorUploadDir.charAt(connectorUploadDir.length() - 1)) {
						cmd.append(" mv " + sourceFile + " " + connectorADUploadDir + "/" + sourceFile);
					} else {
						cmd.append(" mv " + sourceFile + " " + connectorADUploadDir + "/" + sourceFile);
						
					}
				}
				/* Move file to AD */

				commander.println(cmd);
				commander.println("exit");
				commander.close();

				do {
					Thread.sleep(500);
				} while (!channel.isEOF());

				long lCurrentDate = getDateTimeInMS(fileInfoVb.getDate(), "dd-M-yyyy hh:mm:ss");
				fileName = fileName.substring(0, fileName.lastIndexOf('.')) + "_"
						+ SessionContextHolder.getContext().getVisionId() + "_" + macroVar + "_" + lCurrentDate + "." + fileExtension
						+ "";
				channel = session.openChannel("sftp");
				channel.connect();
				ChannelSftp sftpChannel = (ChannelSftp) channel;
				sftpChannel.cd(connectorUploadDir);
				InputStream in = new ByteArrayInputStream(fileInfoVb.getData());
				sftpChannel.put(in, fileName);
				sftpChannel.exit();

				channel = session.openChannel("shell");
				inputstream_for_the_channel = channel.getOutputStream();
				commander = new PrintStream(inputstream_for_the_channel, true);
				channel.connect();
				commander.println("cd " + scriptDir);
				cmd = new StringBuilder();
				if ('/' != connectorUploadDir.charAt(connectorUploadDir.length() - 1)) {
					cmd.append("./remSpecialChar.sh " + connectorUploadDir + "/" + fileName);
				} else {
					cmd.append("./remSpecialChar.sh " + connectorUploadDir + fileName);
				}
				commander.println(cmd);
				commander.println("exit");
				commander.close();
				do {
					Thread.sleep(1000);
				} while (!channel.isEOF());
				session.disconnect();
			} else {
				FTPClient ftpClient = getConnection();
				ftpClient.connect(getHostName());
				boolean response = ftpClient.login(getUserName(), getPassword());
				if (!response) {
					ftpClient.disconnect();
					exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR,
							"Upload", "");
					exceptionCode.setResponse(fileName);
					throw new RuntimeCustomException(exceptionCode);
				}
				ftpClient.setFileType(fileType);
				response = ftpClient.changeWorkingDirectory(connectorUploadDir);
				if (!response) {
					ftpClient.disconnect();
					exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR,
							"Upload", "");
					exceptionCode.setResponse(fileName);
					throw new RuntimeCustomException(exceptionCode);
				}
				fileName = fileName.toUpperCase();
				fileName = fileName.substring(0, fileName.lastIndexOf('.')) + "_"
						+ SessionContextHolder.getContext().getVisionId() + "." + fileExtension + "";
				InputStream in = new ByteArrayInputStream(fileInfoVb.getData());
				ftpClient.storeFile(fileName, in);
				in.close(); // close the io streams
				ftpClient.disconnect();
			}
		} catch (FileNotFoundException e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		} catch (IOException e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		} catch (Exception e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		}
		exceptionCode.setErrorCode(Constants.SUCCESSFUL_OPERATION);
		exceptionCode.setOtherInfo(fileInfoVb);
        return exceptionCode;
	}
	
	public ExceptionCode listFilesFromConnectors(String macroVar) {
		ExceptionCode exceptionCode = null;
		try {
			setUploadDownloadDirFromDB();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			
			/*Files From ConnectorsAD*/
			File[] files = new File(connectorADUploadDir).listFiles();
			List<FileInfoVb> collTemp = new ArrayList<FileInfoVb>();
			if(ValidationUtil.isValid(files)) {
				for (File file : files) {
					String fileName=file.getName();
					if(fileName.indexOf("_" +SessionContextHolder.getContext().getVisionId() + "_" +macroVar+"_")!=-1) {
						FileInfoVb fileInfoVb = new FileInfoVb();
						fileInfoVb.setName(file.getName());
						fileInfoVb.setSize(formatFileSize(file.length()));
						fileInfoVb.setDate(sdf.format(file.lastModified()));
						fileInfoVb.setExtension(fileName.substring(fileName.lastIndexOf(".")+1));
						fileInfoVb.setGroupBy("connectorADUploadDir");
						collTemp.add(fileInfoVb);
					}
				}
			}
		
			/*Files From ConnectorsData*/
			File[] filesData = new File(connectorUploadDir).listFiles();
			if(ValidationUtil.isValid(filesData)) {
				for (File file : filesData) {
					String fileName1=file.getName();
					if(fileName1.indexOf("_" +SessionContextHolder.getContext().getVisionId() + "_" +macroVar+"_")!=-1) {
						FileInfoVb fileInfoVb = new FileInfoVb();
						fileInfoVb.setName(file.getName());
						fileInfoVb.setSize(formatFileSize(file.length()));
						fileInfoVb.setDate(sdf.format(file.lastModified()));
						fileInfoVb.setExtension(fileName1.substring(fileName1.lastIndexOf(".")+1));
						fileInfoVb.setGroupBy("connectorUploadDir");
						collTemp.add(fileInfoVb);
					}
				}
			}
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME,Constants.SUCCESSFUL_OPERATION, "List Files Success", "");
			exceptionCode.setResponse(createParentChildRelations(collTemp, "Date", 1));
		} catch (Exception e) {
			e.printStackTrace();
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Download","");
			throw new RuntimeCustomException(exceptionCode);
		}
		return exceptionCode;
	}

	public ExceptionCode fileDownloadFromConnector(FileInfoVb fileInfoVb,String connectorDir) {
		ExceptionCode exceptionCode = null;
		File file = null;
		FileOutputStream fos = null;
		BufferedInputStream bufferedInputStream = null;
		String searchDir="";
		try {
			String fileName = fileInfoVb.getName();
			setUploadDownloadDirFromDB();
			String filePath = System.getProperty("java.io.tmpdir");
			file = new File(filePath + fileName);
			fos = new FileOutputStream(file);
			if(connectorDir.equalsIgnoreCase("connectorUploadDir")) {
				searchDir=connectorUploadDir+fileName;
			}else {
				searchDir=connectorADUploadDir+fileName;

			}
			InputStream ins = new FileInputStream(searchDir);
			bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(IOUtils.toByteArray(ins)));
			
			int bit = 4096;
			while ((bit) >= 0) {
				bit = bufferedInputStream.read();
				fos.write(bit);
			}
			bufferedInputStream.close();
			fos.close();
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.SUCCESSFUL_OPERATION, "Sucess", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return exceptionCode;
	}

	public HttpServletResponse setExportXlsServlet(HttpServletRequest request, HttpServletResponse response) {
		try {
			exportXlsServlet.doPost(request, response);
			return response;
		} catch (Exception e) {
			throw new RuntimeCustomException(e.getMessage());
		}
	}

	public ExceptionCode xmlToJson(MultipartFile[] files) throws IOException {
		ExceptionCode exceptionCode = null;
		try {
			for (MultipartFile uploadedFile : files) {
				String content = new String(uploadedFile.getBytes(), "UTF-8");
				exceptionCode = CommonUtils.XmlToJson(content);
			}
		} catch (RuntimeCustomException rex) {
			exceptionCode = CommonUtils.getResultObject("xmlToJson",Constants.WE_HAVE_ERROR_DESCRIPTION  , "XML To JSON Conversion", rex.getMessage());
			exceptionCode.setResponse("");
			return exceptionCode;
		}
		return exceptionCode;
	}
	public ExceptionCode doDeleteDataConnectorUpload(FileInfoVb fileInfoVb, DSConnectorVb lsData, String macroVar) {
		ExceptionCode exceptionCode = new ExceptionCode();
		String fileName = fileInfoVb.getName();
		try {
			setUploadDownloadDirFromDB();
			if(lsData!=null && ValidationUtil.isValid(lsData.getMacroVarScript())) {/*Move File to AD*/
				String upfileName = CommonUtils.getValue(lsData.getMacroVarScript(), "NAME");
				String extension = CommonUtils.getValue(lsData.getMacroVarScript(), "EXTENSION");
				System.out.println("EXISTS DATE:"+lsData.getDateLastModified());
				long lCurrentDate = getDateTimeInMS(lsData.getDateLastModified(), "dd-M-yyyy hh:mm:ss");
				System.out.println("lCurrentDate EXISTS DATE:"+lCurrentDate);
				fileName = upfileName + "_" + lsData.getMaker() + "_" + lsData.getMacroVar() + "_" + lCurrentDate + "." + extension;
				System.out.println("fileName EXISTS DATE:"+fileName);
				File lFile = new File(getConnectorUploadDir() + fileName);
				if(lFile.exists()){
					if (lFile.renameTo(new File(getConnectorADUploadDir()+fileName))) {
						lFile.delete();
					} else {
						System.out.println("Problem during backup process");
					}
				}
			}
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.SUCCESSFUL_OPERATION, "Upload", "");
			exceptionCode.setOtherInfo(fileInfoVb);
		    return exceptionCode;
		}catch (Exception e) {
			exceptionCode = CommonUtils.getResultObject(SERVICE_NAME, Constants.FILE_UPLOAD_REMOTE_ERROR, "Upload", "");
			exceptionCode.setResponse(fileName);
			throw new RuntimeCustomException(exceptionCode);
		}
	}	
}