package com.jason.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.jason.entity.DataSourceInfo;
import com.jason.entity.FileInfos;
import com.jason.entity.NodeInfo;
import com.jason.repository.DataSourceInfoRepository;
import com.jason.repository.NodeInfoRepository;
import com.jason.util.ExcelParser;
import com.jason.util.FileUtils;
import com.jason.util.FtpUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
public class FTPServiceController {

	@Resource
    DataSourceInfoRepository dataSourceInfoRepository;

	@Resource
    NodeInfoRepository nodeRepository;

	/**
	 * 查询ftp服务器信息
	 * 
	 * pl使用
	 * 
	 * @return
	 */
	@RequestMapping(value = "/ftpdataSource", method = RequestMethod.POST)
	@ResponseBody
	public List<DataSourceInfo> getDataSource() {
		List<DataSourceInfo> ds = dataSourceInfoRepository.findByDbType("ftp");
		return ds;
	}

	/**
	 * 根据ftp服务器的treeid获取文件名（pl）
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dataSource/files", method = RequestMethod.POST)
	@ResponseBody
	public List<NodeInfo> queryFileInfos(Long id, String filename) throws Exception {

		if (null == id) {
			return new ArrayList<NodeInfo>();
		}

		List<NodeInfo> tableInfos = new ArrayList<NodeInfo>();
		NodeInfo dsinfo = nodeRepository.findById(id);
		if (StringUtils.isEmpty(filename)) {
			tableInfos = nodeRepository.findByFlagLikeAndTypeOrderByName(dsinfo.getFlag() + "|%", "6");
		} else {
			tableInfos = nodeRepository.findByFlagLikeAndTypeAndNameLikeOrderByName(dsinfo.getFlag() + "|%", "6",
					"%" + filename + "%");
		}

		return tableInfos;

	}

	/**
	 * 根据数据源树节点查找表列表(pl)
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dataSource/sheets", method = RequestMethod.POST)
	@ResponseBody
	public List<NodeInfo> querySheets(Long id) throws Exception {

		if (null == id) {
			return new ArrayList<NodeInfo>();
		}

		List<NodeInfo> tableInfos = new ArrayList<NodeInfo>();
		NodeInfo dsinfo = nodeRepository.findById(id);
		tableInfos = nodeRepository.findByFlagLikeAndTypeOrderByName(dsinfo.getFlag() + "|%", "7");

		return tableInfos;

	}

	/**
	 * 根据数据源树节点查找表列表(pl)
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dataSource/columns", method = RequestMethod.POST)
	@ResponseBody
	public List<NodeInfo> queryColumns(Long id) throws Exception {

		if (null == id) {
			return new ArrayList<NodeInfo>();
		}

		List<NodeInfo> tableInfos = new ArrayList<NodeInfo>();
		NodeInfo dsinfo = nodeRepository.findById(id);
		tableInfos = nodeRepository.findByFlagLikeAndTypeOrderByName(dsinfo.getFlag() + "|%", "8");

		return tableInfos;

	}

	/**
	 * 查询数据中心数据池中所有数据库信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getFtpFiles", method = RequestMethod.POST)
	@ResponseBody
	public List<FileInfos> getFtpFiles(Long treeId) {

		DataSourceInfo ds = dataSourceInfoRepository.findByTreeId(treeId);
		NodeInfo info = nodeRepository.findById(treeId);
		if (null == ds) {
			return new ArrayList<>();
		}
		FtpUtils ftp = new FtpUtils(ds.getIp(), Integer.parseInt(ds.getPort()), ds.getUsername(), ds.getPassword(),
				ds.getFtpPath());
		List<FileInfos> infos = new ArrayList<FileInfos>();
		ftp.getFtpFileList(infos);
		return infos;
	}

	/**
	 * 查询sheet信息 192.168.100.20|端口|用户名|密码
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getSheetInfos", method = RequestMethod.POST)
	@ResponseBody
	public List<String> getSheetInfos(DataSourceInfo info, String fileName, HttpServletRequest request) {
		List<String> sheets = new ArrayList<String>();
		FtpUtils ftp = new FtpUtils(info.getIp(), Integer.parseInt(info.getPort()), info.getUsername(),
				info.getPassword(), info.getFtpPath());

		String path = request.getSession().getServletContext().getRealPath("/");// 文件保存目录，也可自定为绝对路径
		path = path + "/excel";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		FileInfos remoteFile = new FileInfos();
		remoteFile.setName(fileName);
		remoteFile.setPath(info.getFtpPath());
		remoteFile.setType("1");
		Boolean success = false;
		if (!StringUtils.isEmpty(fileName)) {
			success = ftp.downloadFile(remoteFile, path, fileName);
		}

		// 获取workbook对象
		Workbook workbook = null;
		InputStream is = null;
		File file = new File(path + "/" + fileName);
		try {
			is = new FileInputStream(file);
			if (fileName.endsWith(ExcelParser.EXTENSION_XLS)) {
				workbook = new HSSFWorkbook(is);
			} else if (fileName.endsWith(ExcelParser.EXTENSION_XLSX)) {
				workbook = new XSSFWorkbook(is);
			}
			sheets = new ExcelParser().getSheets(workbook);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 判断目录或文件是否存在
		if (file.exists()) { // 不存在返回 false
			file.delete();
		}
		return sheets;
	}

	/**
	 * 查询数据中心数据池中所有数据库信息 ip|目录
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getExcelHeaders", method = RequestMethod.POST)
	@ResponseBody
	public List<String> getExcelHeaders(DataSourceInfo info, String fileName, String sheetName,
			HttpServletRequest request) {
		List<String> headers = new ArrayList<String>();
		FtpUtils ftp = new FtpUtils(info.getIp(), Integer.parseInt(info.getPort()), info.getUsername(),
				info.getPassword(), info.getFtpPath());
		String path = request.getSession().getServletContext().getRealPath("/");// 文件保存目录，也可自定为绝对路径
		path = path + "/excel";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		FileInfos remoteFile = new FileInfos();
		remoteFile.setName(fileName);
		remoteFile.setPath(info.getFtpPath());
		remoteFile.setType("0");
		Boolean success = false;
		if (!StringUtils.isEmpty(fileName)) {
			success = ftp.downloadFile(remoteFile, path, fileName);
		}

		// 获取workbook对象
		Workbook workbook = null;
		InputStream is = null;
		File file = new File(path + "/" + fileName);
		try {
			is = new FileInputStream(file);
			if (fileName.endsWith(ExcelParser.EXTENSION_XLS)) {
				workbook = new HSSFWorkbook(is);
			} else if (fileName.endsWith(ExcelParser.EXTENSION_XLSX)) {
				workbook = new XSSFWorkbook(is);
			}
			headers = new ExcelParser().getHeaders(workbook, sheetName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 判断目录或文件是否存在
		if (file.exists()) { // 不存在返回 false
			file.delete();
		}
		if (true == success) {
			return headers;
		} else {
			return new ArrayList<String>();
		}
	}

	@RequestMapping(value = "/db/queryExcelData/{index}/{pageSize}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> queryExcelData(@PathVariable int index, @PathVariable int pageSize, DataSourceInfo info,
			String fileName, String sheetName, HttpServletRequest request) {
		FtpUtils ftp = new FtpUtils(info.getIp(), Integer.parseInt(info.getPort()), info.getUsername(),
				info.getPassword(), info.getFtpPath());
		String path = request.getSession().getServletContext().getRealPath("/");// 文件保存目录，也可自定为绝对路径
		path = path + "/excel";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		FileInfos remoteFile = new FileInfos();
		remoteFile.setName(fileName);
		remoteFile.setPath(info.getFtpPath());
		remoteFile.setType("0");
		Boolean success = false;
		if (!StringUtils.isEmpty(fileName)) {
			success = ftp.downloadFile(remoteFile, path, fileName);
		}

		// 获取workbook对象
		Workbook workbook = null;
		List<String> headers = null;
		List<Map<String, String>> readExcel = new ArrayList<Map<String, String>>();
		List<Map<String, String>> nreadExcel = new ArrayList<Map<String, String>>();
		InputStream is = null;
		File file = new File(path + "/" + fileName);
		try {
			is = new FileInputStream(file);
			if (fileName.endsWith(ExcelParser.EXTENSION_XLS)) {
				workbook = new HSSFWorkbook(is);
			} else if (fileName.endsWith(ExcelParser.EXTENSION_XLSX)) {
				workbook = new XSSFWorkbook(is);
			}
			headers = new ExcelParser().getHeaders(workbook, sheetName);
			readExcel = new ExcelParser().readExcel(workbook, sheetName);
			for (Map<String, String> m : readExcel) {
				Map<String, String> nm = new LinkedHashMap<String, String>();
				;
				for (String h : headers) {
					if (!StringUtils.isEmpty(h)) {
						nm.put(h, m.get(h));
					}
				}
				nreadExcel.add(nm);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 判断目录或文件是否存在
		if (file.exists()) { // 不存在返回 false
			file.delete();
		}
		Map<String, Object> map = new HashMap<>();
		int size = nreadExcel.size();
		double ceil = Math.ceil(readExcel.size() * 1.0 / pageSize);
		Object pageCount = ceil;
		map.put("pagecount", pageCount);
		map.put("headers", headers);
		map.put("rowslist", nreadExcel.subList(pageSize * index,
				(pageSize * index + 10) < (size - 1) ? (pageSize * index + 10) : (size)));
		return map;
	}

	@RequestMapping(value = "/uploadExcel")
	@ResponseBody
	public String uploadConfigFile(Long id,String ftpFilePath,HttpServletRequest request) {

		List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
		DataSourceInfo info = dataSourceInfoRepository.findByTreeId(id);
		String ftpPath = info.getFtpPath();
		if(!StringUtils.isEmpty(ftpFilePath)){
			ftpPath += "//"+ftpFilePath;
		}
		FtpUtils ftp = new FtpUtils(info.getIp(), Integer.parseInt(info.getPort()), info.getUsername(),
				info.getPassword(), ftpPath);
		String isExcelFlag="0";
		if (files.size() > 0) {
			for (MultipartFile f : files) {
				InputStream is = null;
				// 设置上传目录
				try {
					String fileName = new String(f.getOriginalFilename().getBytes("GBK"), "iso-8859-1");
					if (FileUtils.isExcel(fileName)) {
						is = f.getInputStream();
						ftp.upLoadFile(ftpPath, fileName, is);
					} else {
						isExcelFlag = "1";
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (null != is) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return isExcelFlag;
	}

	@RequestMapping(value = "getSubDir", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public List<NodeInfo> getSubDir(Long treeId) {
		NodeInfo parentNode = nodeRepository.findById(treeId);
		if(parentNode==null)
			return new ArrayList<>();
		List<NodeInfo> nodeInfos =nodeRepository.findByFlagLikeAndType(parentNode.getFlag() + "|%", "6");
		if (nodeInfos == null)
			nodeInfos = new ArrayList<>();
		return nodeInfos;
	}

	@RequestMapping(value = "getFtpDirs", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public List<Map<String, String>> getFtpDirs(Long treeId) {
		NodeInfo parentNode = nodeRepository.findById(treeId);
		DataSourceInfo ds = dataSourceInfoRepository.findByTreeId(treeId);
		List<Map<String, String>> lists = new ArrayList<>();
		if (ds == null || parentNode == null)
			return lists;
		List<NodeInfo> nodeInfos = nodeRepository.findByFlagLikeAndType(parentNode.getFlag() + "|%", "6");
		String rootPre = ds.getFtpPath();
		if (StringUtils.isEmpty(rootPre))
			rootPre = "";
		if (!rootPre.endsWith("/")) {
			rootPre += "/";
		}
		for (NodeInfo info : nodeInfos) {
			if (StringUtils.isEmpty(info.getDefname()))
				continue;
			Map<String, String> mapDt = new HashMap<>();
			mapDt.put("value", rootPre + info.getDefname());
			mapDt.put("txt", info.getDefname());
			lists.add(mapDt);
		}
		return lists;

	}

	@RequestMapping(value = "getUnselectedSubDir", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public List<FileInfos> getUnselectedSubDir(Long treeId) throws Exception {
		DataSourceInfo ds = dataSourceInfoRepository.findByTreeId(treeId);
		NodeInfo parentNode = nodeRepository.findById(treeId);
		if (ds == null || parentNode == null)
			return new ArrayList<>();
		List<NodeInfo> nodeInfos = nodeRepository.findByFlagLikeAndType(parentNode.getFlag() + "|%", "6");
		FtpUtils ftp = new FtpUtils(ds.getIp(), Integer.parseInt(ds.getPort()), ds.getUsername(), ds.getPassword(),
				ds.getFtpPath());
		List<FileInfos> infos = new ArrayList<FileInfos>();
		ftp.getFtpFileList(infos);
		if (infos != null && !infos.isEmpty()) {
			List<FileInfos> fileInfo = new ArrayList<>();
			for (FileInfos file : infos) {
				boolean isSelected = false;
				for (NodeInfo node : nodeInfos) {
					if (node.getName().equals(file.getName())) {
						isSelected = true;
						break;
					}
				}
				if (!isSelected) {
					fileInfo.add(file);
				}
			}
			return fileInfo;
		} else {
			return new ArrayList<>();
		}

	}

}
