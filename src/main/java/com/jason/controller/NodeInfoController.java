package com.jason.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.jason.entity.NodeInfo;
import com.jason.repository.DataSourceInfoRepository;
import com.jason.repository.NodeInfoRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NodeInfoController {
	@Resource
    NodeInfoRepository nodeRepository;
	
	@Resource
    DataSourceInfoRepository dataSourceInfoRepository;
	
	/**
	 * 根据节点id查询节点信息
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getinfobyid",method = RequestMethod.POST)
    @ResponseBody
    public NodeInfo getById(Long id) {
      NodeInfo info = nodeRepository.findById(id);
      return info;
    }
	
	/**
	 * 保存节点信息
	 * @param info
	 */
	@RequestMapping(value = "/savenode",method = RequestMethod.POST)
    @ResponseBody
	public NodeInfo saveNode(NodeInfo info) {
		NodeInfo node = nodeRepository.save(info);
		return node;
	}
	
	/**
	 * 新增多个子节点
	 * @param info
	 */
	@RequestMapping(value = "/savenodes",method = RequestMethod.POST)
    @ResponseBody
	public void saveNodes(@RequestBody ArrayList<NodeInfo> infos) {
		for (NodeInfo info : infos){
			nodeRepository.save(info);
		}
	}
	
	/**
	 * 根据父节点查询子节点
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/findbypid",method = RequestMethod.POST)
    @ResponseBody
    public List<NodeInfo> findbyPidAndName(Long id,String userName) {
		if(StringUtils.isNotEmpty(userName)){
			return nodeRepository.findByPidAndNameLikeOrderById(id,userName);
		}else{
			return nodeRepository.findByPidOrderById(id);
		}
    }
	
	/**
	 * 根据父节点查询子节点
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/deletenode",method = RequestMethod.POST)
    @ResponseBody
    public void deleteNode(Long id) {
		nodeRepository.delete(id);
		dataSourceInfoRepository.deleteByTreeId(id);
    }
	
  }
