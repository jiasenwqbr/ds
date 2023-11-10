package com.jason.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

import com.jason.entity.DataSourceInfo;


@Transactional
public interface DataSourceInfoRepository extends CrudRepository<DataSourceInfo, Long>{
	
	public List<DataSourceInfo> findAll();
	
	public List<DataSourceInfo> findByDbType(String dbType);
	
	public List<DataSourceInfo> findByDbTypeNotOrderById(String dbType);
	
	public DataSourceInfo findById(Long id);
	
	public DataSourceInfo findByTreeId(Long treeId);
	
	public DataSourceInfo findByInstanceAndIp(String instance,String ip);
	
	public DataSourceInfo save(DataSourceInfo dataSourceInfo);
	
	public void deleteByTreeId(Long treeId);
	
	public List<DataSourceInfo> findByIpAndInstanceAndUsernameAndDbType(String ip , String instance ,String username ,String dbType);

	public List<DataSourceInfo> findByUsername(String database);
	
	
}
