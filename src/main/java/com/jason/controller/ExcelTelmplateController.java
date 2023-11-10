package com.jason.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jason.entity.DataSourceInfo;
import com.jason.entity.ExcelDataInfo;
import com.jason.entity.ExcelTemplateInfo;
import com.jason.entity.ExcelUpdateErrorInfo;
import com.jason.repository.DataSourceInfoRepository;
import com.jason.repository.ErrorInorRe;
import com.jason.repository.ExcelDataRepository;
import com.jason.repository.TemplateRepository;
import com.jason.util.ExcelConstants;
import com.jason.util.FtpUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 提供excel文件的上传下载和更新API接口
 * 
 * @author lijinghua
 *
 */
@RestController
@RequestMapping(value = "/excel")
public class ExcelTelmplateController {

	@Resource
	private TemplateRepository templateResp;
	@Resource
	private ExcelDataRepository dataResp;
	@Resource
	private DataSourceInfoRepository dataSourceInfoRepository;
	@PersistenceContext
	private EntityManager emg;
	@Resource
	private ErrorInorRe errorResp;

	/**
	 * 保存模板文件
	 * 
	 * @param info
	 * @param req
	 */
	@RequestMapping(value = "/saveTemplate", method = { RequestMethod.POST })
	public void saveTemplate(ExcelTemplateInfo info, HttpServletRequest req) {
		Map<String, Object> fileInfo = ExcelConstants.getFileInfo(req);
		ExcelConstants.checkUploadInfo(fileInfo);
		info.setContent((byte[]) fileInfo.get(ExcelConstants.FILE_CNT_KEY));
		info.setUpdateCount(0);
		//info.setFileName((String) fileInfo.get(ExcelConstants.FILE_NAME_KEY));
		templateResp.save(info);
	}

	/**
	 * 更新模板文件
	 * 
	 * @param info
	 * @param request
	 */
	@RequestMapping(value = "updateTemplate", method = { RequestMethod.POST })
	public void updateTemplate(ExcelTemplateInfo info, HttpServletRequest request) {
		Map<String, Object> fileInfo = ExcelConstants.getFileInfo(request);
		ExcelConstants.checkUploadInfo(fileInfo);
		ExcelTemplateInfo excelInfo = templateResp.findOne(info.getId());
		if (excelInfo == null) {
			throw new RuntimeException("未找到模板文件id=" + info.getId());
		}
		info.setContent((byte[]) fileInfo.get(ExcelConstants.FILE_CNT_KEY));
		info.setUpdateCount(excelInfo.getUpdateCount() + 1);
		info.setTemplateName((String) fileInfo.get(ExcelConstants.FILE_NAME_KEY));
		info.setLastUpdate(new Date());
		templateResp.save(info);
	}

	/**
	 * 下载模板文件
	 * 
	 * @param id
	 * @param resp
	 */
	@RequestMapping(value = "downloadTemplate", method = { RequestMethod.GET, RequestMethod.POST })
	public void downLoadTemplate(Long id, HttpServletResponse resp) throws Exception {
		ExcelTemplateInfo excelInfo = templateResp.findOne(id);
		if (excelInfo == null) {
			throw new RuntimeException("未找到模板文件id=" + id);
		}
		if (excelInfo.getContent() == null || excelInfo.getContent().length == 0) {
			throw new RuntimeException("模板文件内容为空");
		}
		byte[] buffer = new byte[1024];
		InputStream is = (InputStream) new ByteArrayInputStream(excelInfo.getContent());
		resp.setContentType("applicatoin/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		String fileName = URLEncoder.encode(excelInfo.getFileName(), "UTF-8");
		resp.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
		resp.setContentLength(is.available());
		try {
			OutputStream os = resp.getOutputStream();
			while ((is.read(buffer)) > 0) {
				os.write(buffer);
			}
			os.flush();

		} finally {
			is.close();
		}
	}

	/**
	 * 上传数据文件
	 * 
	 * @param Id
	 * @param request
	 */
	@RequestMapping(value = "/uploadeDataFile", method = { RequestMethod.POST })
	public void uploadDataFile(Long id, Long temId,String fileName,String path, HttpServletRequest request) {
		InputStream is = null;
		try {
			DataSourceInfo info = dataSourceInfoRepository.findByTreeId(id);
			if (info == null) {
				throw new RuntimeException("无效FTP服务器信息");
			}
			ExcelTemplateInfo template = templateResp.findOne(temId);
			if (template == null) {
				throw new RuntimeException("未找到模板,id为" + temId);
			}
			MultipartFile contentFile=ExcelConstants.getHttpFile(request);
			Map<String,String>checkResult=ExcelConstants.checkFile(template,contentFile,fileName);
			if(!"true".equals(checkResult.get("flag"))){
				ExcelUpdateErrorInfo error=new ExcelUpdateErrorInfo();
				error.setErrorMsg(checkResult.get("msg"));
				error.setCreateDate(new Date());
				errorResp.save(error);
				throw new RuntimeException(checkResult.get("msg"));
			}
			Map<String, Object> fileInfo = ExcelConstants.getFileInfo(request);
			ExcelConstants.checkUploadInfo(fileInfo);
			String ftpPath = info.getFtpPath();
			if(!StringUtils.isEmpty(path)){
				ftpPath = ftpPath+"//"+path;
			}
			FtpUtils ftp = new FtpUtils(info.getIp(), Integer.parseInt(info.getPort()), info.getUsername(),
					info.getPassword(), ftpPath);
			is = new ByteArrayInputStream((byte[]) fileInfo.get(ExcelConstants.FILE_CNT_KEY));
			boolean flag = ftp.upLoadExcelFile(ftpPath,
					ExcelConstants.getFileName() + "." + fileInfo.get(ExcelConstants.FILE_EXT_KEY), is);
			if (flag) {
				ExcelDataInfo dataInfo = new ExcelDataInfo();
				dataInfo.setCreateDate(new Date());
				dataInfo.setFileName(fileName);
				dataInfo.setTemplateId(temId);
				dataResp.save(dataInfo);
				template.setLastUpdate(new Date());
				template.setUpdateCount(template.getUpdateCount() + 1);
				templateResp.save(template);
			} else {
				throw new RuntimeException("FTP上传文件失败");
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 获取模板文件列表按照模板文件创建时间和更新时间倒叙
	 * 
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping(value = "/getTemplateInfo", method = { RequestMethod.GET, RequestMethod.POST })
	public Page<ExcelTemplateInfo> getTemplateInfo(Integer page, Integer size) {
		if (page == null || page <= 0) {
			page = 1;
		}
		if (size == null) {
			size = 10;
		}
		CriteriaBuilder criteraBuilder = emg.getCriteriaBuilder();
		CriteriaQuery<ExcelTemplateInfo> query = criteraBuilder.createQuery(ExcelTemplateInfo.class);
		Root<ExcelTemplateInfo> root = query.from(ExcelTemplateInfo.class);
		List<Order> orders = new ArrayList<>();
		orders.add(criteraBuilder.asc(root.get("id")));
		query.select(criteraBuilder.construct(ExcelTemplateInfo.class, root.get("id").alias("id"),
				root.get("deptName").alias("deptName"), root.get("templateName").alias("templateName"),
				root.get("updateCycle").alias("updateCycle"), root.get("lastUpdate").alias("lastUpdate"),
				root.get("updateCount").alias("updateCount"))).orderBy(orders);
		Pageable pageInfo = new PageRequest(page - 1, size);
		TypedQuery<ExcelTemplateInfo> pageCountQuery = emg.createQuery(query).setFirstResult(pageInfo.getOffset())
				.setMaxResults(pageInfo.getPageSize());
		TypedQuery<ExcelTemplateInfo> countQuery = emg.createQuery(query);
		Page<ExcelTemplateInfo> pageResutl = new PageImpl<ExcelTemplateInfo>(pageCountQuery.getResultList(), pageInfo,
				countQuery.getResultList().size());
		return pageResutl;
	}

	/**
	 * 获取数据文件列表按照上传文件的时间倒叙取数据
	 * 
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping(value = "/getDataInfo", method = { RequestMethod.GET, RequestMethod.POST })
	public Page<ExcelDataInfo> getDataInfo(Integer page, Integer size) {
		if (page == null || page <= 0) {
			page = 1;
		}
		if (size == null) {
			size = 10;
		}
		CriteriaBuilder criteraBuilder = emg.getCriteriaBuilder();
		CriteriaQuery<ExcelDataInfo> query = criteraBuilder.createQuery(ExcelDataInfo.class);
		Root<ExcelDataInfo> root = query.from(ExcelDataInfo.class);
		List<Order> orders = new ArrayList<>();
		orders.add(criteraBuilder.desc(root.get("createDate")));
		query.select(criteraBuilder.construct(ExcelDataInfo.class, root.get("id").alias("id"),
				root.get("fileName").alias("fileName"), root.get("createDate").alias("createDate"))).orderBy(orders);
		Pageable pageInfo = new PageRequest(page - 1, size);
		TypedQuery<ExcelDataInfo> pageCountQuery = emg.createQuery(query).setFirstResult(pageInfo.getOffset())
				.setMaxResults(pageInfo.getPageSize());
		TypedQuery<ExcelDataInfo> countQuery = emg.createQuery(query);
		Page<ExcelDataInfo> pageResutl = new PageImpl<ExcelDataInfo>(pageCountQuery.getResultList(), pageInfo,
				countQuery.getResultList().size());
		return pageResutl;
	}
	
	@RequestMapping(value="/getErrorInfo", method = { RequestMethod.GET, RequestMethod.POST })
	public Page<ExcelUpdateErrorInfo> getErrorInfo(Integer page,Integer size){
		if (page == null || page <= 0) {
			page = 1;
		}
		if (size == null) {
			size = 10;
		}
		CriteriaBuilder criteraBuilder = emg.getCriteriaBuilder();
		CriteriaQuery<ExcelUpdateErrorInfo> query = criteraBuilder.createQuery(ExcelUpdateErrorInfo.class);
		Root<ExcelUpdateErrorInfo> root = query.from(ExcelUpdateErrorInfo.class);
		List<Order> orders = new ArrayList<>();
		orders.add(criteraBuilder.desc(root.get("createDate")));
		query.select(criteraBuilder.construct(ExcelUpdateErrorInfo.class, root.get("id").alias("id"),
				root.get("errorMsg").alias("errorMsg"), root.get("createDate").alias("createDate"))).orderBy(orders);
		Pageable pageInfo = new PageRequest(page - 1, size);
		TypedQuery<ExcelUpdateErrorInfo> pageCountQuery = emg.createQuery(query).setFirstResult(pageInfo.getOffset())
				.setMaxResults(pageInfo.getPageSize());
		TypedQuery<ExcelUpdateErrorInfo> countQuery = emg.createQuery(query);
		Page<ExcelUpdateErrorInfo> pageResutl = new PageImpl<ExcelUpdateErrorInfo>(pageCountQuery.getResultList(), pageInfo,
				countQuery.getResultList().size());
		return pageResutl;
	}

	/**
	 * 获取部门信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getDept", method = { RequestMethod.GET, RequestMethod.POST })
	public List<String> getDept() {
		return ExcelConstants.depts;
	}

	/**
	 * 获取更新周期信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getUpdateCycle", method = { RequestMethod.GET, RequestMethod.POST })
	public List<String> getUpdateCycle() {
		return ExcelConstants.updateCycles;
	}

	@RequestMapping(value = "/tipInfo", method = { RequestMethod.GET, RequestMethod.POST })
	public Map<String, String> tipInfo() {
		Map<String, String> tipInfo = new HashMap<>();
		String zero="0";
		tipInfo.put("shouldSupply", zero);
		tipInfo.put("supplied", zero);
		tipInfo.put("notSupply", zero);
		tipInfo.put("allSupplied", zero);
		tipInfo.put("allNotSupply", zero);
		int allSupplied=0;
		int allNotSupply=0;
		int shoudSupply=0;
		int supplied=0;
		Iterable<ExcelTemplateInfo> templates = templateResp.findAll();
		if (templates == null || !templates.iterator().hasNext())
			return tipInfo;
		Iterator<ExcelTemplateInfo> templateIe=templates.iterator();
		while(templateIe.hasNext()){
			ExcelTemplateInfo template=templateIe.next();
			if(template.getUpdateCount()>0){
				allSupplied++;
			}else{
				allNotSupply++;
			}
			shoudSupply++;
			if(!ExcelConstants.shouldSupply(template)){
				supplied++;
			}
		}
		tipInfo.put("allSupplied", String.valueOf(allSupplied));
		tipInfo.put("allNotSupply", String.valueOf(allNotSupply));
		tipInfo.put("shouldSupply", String.valueOf(shoudSupply));
		tipInfo.put("supplied", String.valueOf(supplied));
		tipInfo.put("notSupply", String.valueOf((shoudSupply-supplied)));
		return tipInfo;
	}

}
