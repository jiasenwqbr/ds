package com.jason.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

import com.jason.entity.NodeInfo;

@Transactional
public interface NodeInfoRepository extends CrudRepository<NodeInfo, Long> {
	
	public NodeInfo findById(Long id);
	
	public NodeInfo save(NodeInfo nodeInfo);
	
	public List<NodeInfo> findByPidOrderById(Long pid);
	
	public void delete(Long id);
	
	public List<NodeInfo> findByFlag(String flag);
	
	public List<NodeInfo> findByFlagLike(String flag);
	
	public List<NodeInfo> findByFlagLikeAndType(String flag, String type);
	
	public List<NodeInfo> findByFlagLikeAndTypeOrderByName(String flag,String type);
	
	public List<NodeInfo> findByFlagLikeAndTypeAndNameLikeOrDefnameLikeOrderByName(String flag,String type,String name,String defname);
	public List<NodeInfo> findByFlagLikeAndTypeAndNameLikeOrderByName(String flag,String type,String name);
	
	public List<NodeInfo> findByFlagLikeAndTypeAndDefnameLikeOrderByDefname(String flag,String type,String defname);

	
	public List<NodeInfo> findByFlagAndType(String flag,String type);

	public List<NodeInfo> findByPidAndNameLikeOrderById(Long id, String userName);


	
	
	
} 

