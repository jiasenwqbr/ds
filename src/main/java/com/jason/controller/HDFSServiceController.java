package com.jason.controller;

import java.util.List;

import javax.annotation.Resource;

import com.jason.entity.DataSourceInfo;
import com.jason.repository.DataSourceInfoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HDFSServiceController {
	
	@Resource
    DataSourceInfoRepository dataSourceInfoRepository;
	
	/**
     * 查询hdfs服务器信息
     * 
     * pl使用
     * @return
     */
	@RequestMapping(value = "/hdfsdataSource",method = RequestMethod.POST)
    @ResponseBody
	public List<DataSourceInfo> getDataSource() {
		List<DataSourceInfo> ds = dataSourceInfoRepository.findByDbType("hdfs");
		return ds;
	}

}
