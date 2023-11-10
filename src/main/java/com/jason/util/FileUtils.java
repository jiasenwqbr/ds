package com.jason.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class FileUtils {
	
	/**
	 * 是否为excel文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static Boolean isExcel(String fileName){
		String pattern = "^.+\\.(?:xls|xlsx)$";

		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(fileName);
		return m.matches();
	}
	
	/**
	 * 获取文件扩展名
	 * @param fileName
	 * @return
	 */
	public static String getFileExtension(String fileName){
		if(StringUtils.isEmpty(fileName))
			return "";
		return fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
	}
	
	/**
	 * 是否为xml文件
	 * 
	 * @param fileName
	 * @return
	 */
	public Boolean isXml(String fileName){
		String pattern = "^.+\\.(?:xml)$";

		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(fileName);
		return m.matches();
	}
	
}
