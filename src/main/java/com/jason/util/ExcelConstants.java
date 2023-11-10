package com.jason.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jason.entity.ExcelTemplateInfo;

public class ExcelConstants {
	public static List<String> depts = Arrays.asList("安监局,发改委,工商局,供电公司,国税局,地税局,经信委,科技局,商务局,国土局,住建局,质监局,统计局".split(","));
	public static List<String> updateCycles = Arrays.asList("年,半年,季,月,周,日".split(","));
	public static String FILE_CNT_KEY = "fileContent";
	public static String FILE_NAME_KEY = "fileName";
	public static String FILE_EXT_KEY = "fileExt";
	public static ReentrantLock LOCK = new ReentrantLock();

	/**
	 * 根据更新周期获取天数
	 * 
	 * @param cycle
	 *            更新周期
	 * @return 获取天数
	 */
	public static int getTypeDays(String cycle) {
		switch (cycle) {
		case "年":
			return 365;
		case "半年":
			return 180;
		case "季":
			return 90;
		case "月":
			return 30;
		case "周":
			return 7;
		case "日":
			return 1;
		default:
			return -1;

		}
	}

	public static boolean shouldSupply(ExcelTemplateInfo info) {
		boolean isSupply = false;
		Date beginDate = cycleMiniDate(info.getUpdateCycle());
		if (info.getLastUpdate() == null) {
			isSupply = true;
		} else if (info.getLastUpdate().compareTo(beginDate) < 0) {
			isSupply = true;
		}
		return isSupply;
	}

	/**
	 * 获取周期开始时间
	 * 
	 * @param cycle
	 * @return
	 * @throws Exception
	 */
	public static Date cycleMiniDate(String cycle) {
		Date current = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentStr = sdf.format(current);
		String[] dataInfo = currentStr.split("-");
		int month = Integer.parseInt(dataInfo[1]);
		try {
			switch (cycle) {
			case "年":
				return sdf.parse(dataInfo[0] + "-01-01");
			case "半年":
				if (Integer.parseInt(dataInfo[1]) > 6) {
					return sdf.parse(dataInfo[0] + "-07-01");
				} else {
					return sdf.parse(dataInfo[0] + "-01-01");
				}
			case "季":
				if (month <= 3) {
					return sdf.parse(dataInfo[0] + "-01-01");
				} else if (month > 3 && month <= 6) {
					return sdf.parse(dataInfo[0] + "-04-01");
				} else if (month > 6 && month <= 9) {
					return sdf.parse(dataInfo[0] + "-07-01");
				} else if (month > 9 && month <= 12) {
					return sdf.parse(dataInfo[0] + "-10-01");
				}
			case "月":
				return sdf.parse(dataInfo[0] + "-" + dataInfo[1] + "-01");
			case "周":
				int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek == 1)
					dayOfWeek = 6;
				else
					dayOfWeek = dayOfWeek - 2;
				if (dayOfWeek == 0) {
					return sdf.parse(currentStr);
				}
				Calendar now = Calendar.getInstance();
				now.setTime(new Date());
				now.set(Calendar.DATE, now.get(Calendar.DATE) - dayOfWeek);
				return sdf.parse(sdf.format(now.getTime()));
			case "日":
				return sdf.parse(currentStr);
			default:
				return sdf.parse("1949-10-01");

			}
		} catch (Exception e) {
			e.printStackTrace();
			return current;
		}

	}

	/**
	 * 获取上传文件信息
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, Object> getFileInfo(HttpServletRequest request) {
		MultipartHttpServletRequest mulReq = ((MultipartHttpServletRequest) request);
		List<MultipartFile> files = mulReq.getFiles("file");
		try {
			MultipartFile file = files.get(0);
			String orginalFileName=new String(file.getOriginalFilename().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
			if (!FileUtils.isExcel(orginalFileName)) {
				throw new RuntimeException("上传的文件不是excel文件");
			}
			Map<String, Object> data = new HashMap<>();
			data.put(FILE_NAME_KEY, orginalFileName);
			data.put(FILE_CNT_KEY, file.getBytes());
			data.put(FILE_EXT_KEY, FileUtils.getFileExtension(orginalFileName));
			return data;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	public static MultipartFile getHttpFile(HttpServletRequest req) {
		MultipartHttpServletRequest mulReq = ((MultipartHttpServletRequest) req);
		List<MultipartFile> files = mulReq.getFiles("file");
		return (files == null || files.isEmpty()) ? null : files.get(0);
	}

	public static void checkUploadInfo(Map<String, Object> fileInfo) {
		if (fileInfo == null || fileInfo.isEmpty()) {
			throw new RuntimeException("上传文件无效");
		}
	}

	public static String getFileName() {
		try {
			LOCK.lock();
			return UUID.randomUUID().toString();
		} finally {
			LOCK.unlock();
		}
	}

	public static Map<String, String> checkFile(ExcelTemplateInfo template, MultipartFile file,String fileName) {
		Map<String, String> result = new HashMap<>();
		if (template == null || template.getContent() == null) {
			result.put("flag", "false");
			result.put("msg", fileName + "无法获取到模板文件或模板文件为空");
			return result;
		}
		if (StringUtils.isEmpty(template.getTemplateName())
				|| !fileName.contains(template.getTemplateName())) {
			result.put("flag", "false");
			result.put("msg", fileName + "文件名与模板文件名不一致");
			return result;
		}
		String templateExt = FileUtils.getFileExtension(template.getFileName());
		String fileExt = FileUtils.getFileExtension(fileName);

		if (StringUtils.isEmpty(fileExt) || !fileExt.equals(templateExt)) {
			result.put("flag", "false");
			result.put("msg", fileName + "文件名与模板文件类型不一致");
			return result;
		}
		if (!FileUtils.isExcel(fileName)) {
			result.put("flag", "false");
			result.put("msg", fileName + "不是Excel文件");
			return result;
		}
		Workbook templateWork = null;
		Workbook fileWork = null;
		if ("xls".equals(fileExt)) {
			try {
				templateWork = new HSSFWorkbook(new ByteArrayInputStream(template.getContent()));
				fileWork = new HSSFWorkbook(file.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				templateWork = new XSSFWorkbook(new ByteArrayInputStream(template.getContent()));
				fileWork = new XSSFWorkbook(file.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (templateWork == null || fileWork == null) {
			result.put("flag", "false");
			result.put("msg", fileName + "模板或数据文件破损无法读取");
			return result;
		}
		Sheet templateSheet = templateWork.getSheetAt(0);
		Sheet fileSheet = fileWork.getSheetAt(0);
		if (templateSheet == null || fileSheet == null) {
			result.put("flag", "false");
			result.put("msg", fileName + "模板或数据文件无sheet数据");
			return result;
		}
		Row temRow = templateSheet.getRow(0);
		Row fileRow = fileSheet.getRow(0);
		if (temRow == null || fileRow == null) {
			result.put("flag", "false");
			result.put("msg", fileName + "模板或数据文件sheet无表头");
			return result;
		}
		int temCols = temRow.getPhysicalNumberOfCells();
		int fileCols = fileRow.getPhysicalNumberOfCells();
		if(fileCols <= 0 || temCols <= 0){
			result.put("flag", "false");
			result.put("msg", fileName + "模板或数据文件列数为0");
			return result;
		}
		if (temCols != fileCols) {
			result.put("flag", "false");
			result.put("msg", fileName + "模板或数据文件列数不一致");
			return result;
		}
		for (int i = 0; i < temCols; i++) {
			String temValue = temRow.getCell(i).getStringCellValue();
			String fileValue = fileRow.getCell(i).getStringCellValue();
			if (StringUtils.isEmpty(fileValue) || StringUtils.isEmpty(temValue)
					|| !fileValue.equalsIgnoreCase(temValue)) {
				result.put("flag", "false");
				result.put("msg", fileName + "模板或数据文件列数名不一致");
				return result;
			}
		}
		result.put("flag", "true");
		return result;
	}
	

}
