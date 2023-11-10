package com.jason.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.jason.entity.FileInfos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Way on 2017/1/16.
 */
public class FtpUtils {
	final Logger LOG=Logger.getLogger(FtpUtils.class);
	
	public FtpUtils(String host, int port, String user, String password, String path) {
		this.FTP_HOST = host;
		this.FTP_PORT = port;
		this.FTP_USER = user;
		this.FTP_PASSWORD = password;
		this.FTP_REMOTE_PATH = path;
	}

	// FTP服务器IP地址
	public String FTP_HOST;
	// FTP服务器端口
	public int FTP_PORT = 21;
	// FTP服务器用户名
	public String FTP_USER ;
	// FTP用户密码
	public String FTP_PASSWORD;
	// FTP内部的路径
	public String FTP_REMOTE_PATH;

	/**
	 * 获取连接
	 * 
	 * @return
	 */
	public FTPClient getConnect() {
		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(FTP_HOST,FTP_PORT);
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				return null;
			}
			ftpClient.setControlEncoding("GBK");
			boolean login = ftpClient.login(FTP_USER, FTP_PASSWORD);
			if (!login)
				return null;
			ftpClient.changeWorkingDirectory(FTP_REMOTE_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ftpClient;
	}

	/*
	 * FTP getFileList
	 * 
	 * @param filenames 保存遍历的文件名
	 */
	public void getFtpFileList(List<FileInfos> list) {
		FTPClient ftpClient = getConnect();
		if (ftpClient == null)
			return;
		try {
			List<FileInfos> dirList = new ArrayList<FileInfos>();
			List<FileInfos> fileList = new ArrayList<FileInfos>();
			// 获取所有文件和文件夹的名字
			FTPFile[] files = ftpClient.listDirectories(new String(FTP_REMOTE_PATH.getBytes("GBK"), "iso-8859-1"));
			for (FTPFile file : files) {
				FileInfos fileInfo = new FileInfos();
				fileInfo.setName(file.getName());
				fileInfo.setType(String.valueOf(file.getType()));
				String remotePath = FTP_REMOTE_PATH.endsWith("/") ? file.getName() : "/" + file.getName();
				fileInfo.setPath(FTP_REMOTE_PATH + remotePath);
				dirList.add(fileInfo);
//				if (file.isDirectory() && file.is) {
//					
//				}
			}
			list.addAll(dirList);
			list.addAll(fileList);
			// 注销登录
			boolean logout = ftpClient.logout();
			if (logout) {
				LOG.info("注销成功!");
			} else {
				LOG.error("注销失败!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeFtp(ftpClient);
		}
	}

	/**
	 * 删除文件或者目录
	 * 
	 * @return
	 */
	public boolean deleteFileOrRemoveDir(FileInfos file) {
		FTPClient ftpClient = getConnect();
		boolean flag = false;
		// type=1 是文件夹，需要调用删除目录的方法
		try {
			if (file.getType() == "1") {
				flag = ftpClient.removeDirectory(file.getPath());
			} else {
				flag = ftpClient.deleteFile(file.getPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeFtp(ftpClient);
		}
		return flag;
	}

	/**
	 * 创建文件或者目录
	 * 
	 * @param file
	 * @return
	 */
	public boolean createFileOrDir(FileInfos file) {
		FTPClient ftpClient = getConnect();
		boolean flag = false;
		try {
			flag = ftpClient.makeDirectory(file.getPath() + "/" + file.getName());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeFtp(ftpClient);
		}
		return flag;
	}

	/**
	 * 下载文件
	 * 
	 * @param hostname
	 *            FTP服务器地址
	 * @param port
	 *            FTP服务器端口号
	 * @param username
	 *            FTP登录帐号
	 * @param password
	 *            FTP登录密码
	 * @param pathname
	 *            FTP服务器文件目录
	 * @param filename
	 *            文件名称
	 * @param localpath
	 *            下载后的文件路径
	 * @return
	 */
	public boolean downloadFile(FileInfos remoteFile, String localPath, String fileName) {
		boolean success = false;
		FTPClient ftp = null;
		try {
			int reply;
			ftp = getConnect();
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return success;
			}
			ftp.changeWorkingDirectory(remoteFile.getPath());// 转移到FTP服务器目录
			FTPFile[] fs = ftp.listFiles();

			for (FTPFile ff : fs) {
				if (ff.getName().equals(fileName)) {
					File localFile = new File(localPath + "/" + ff.getName());
					OutputStream is = new FileOutputStream(localFile);
					ftp.retrieveFile(ff.getName(), is);
					is.close();
				}
			}
			ftp.logout();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeFtp(ftp);
		}
		return success;
	}

	/**
	 * 
	 * @return
	 */
	public boolean upLoadFile(String remotePath, String upLoadFileName, InputStream is) {
		boolean success = false;
		FTPClient ftp = null;
		try {
			int reply;
			ftp = getConnect();
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return success;
			}
			ftp.changeWorkingDirectory(remotePath);// 转移到FTP服务器目录
			FTPFile[] fs = ftp.listFiles();

			for (FTPFile ff : fs) {
				if (ff.getName().equals(upLoadFileName)) {
					for (int i = 0; i < fs.length; i++) {
						if (fs[i].getName().equals(upLoadFileName)) {
							ftp.deleteFile(fs[i].getName());
							break;
						}
					}
				}
			}
			ftp.storeFile(upLoadFileName, is);
			ftp.logout();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeFtp(ftp);
		}
		return success;

	}
	
	public boolean upLoadExcelFile(String remotePath, String upLoadFileName, InputStream is) {
		boolean success = false;
		FTPClient ftp = null;
		try {
			int reply;
			ftp = getConnect();
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return success;
			}
			ftp.changeWorkingDirectory(remotePath);// 转移到FTP服务器目录
			ftp.storeFile(upLoadFileName, is);
			ftp.logout();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeFtp(ftp);
		}
		return success;

	}

	private void closeFtp(FTPClient ftp) {
		if (ftp == null)
			return;
		if (ftp.isConnected()) {
			try {
				ftp.disconnect();
			} catch (IOException ioe) {
			}
		}
	}
	
	

}
