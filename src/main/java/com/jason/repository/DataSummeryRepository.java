package com.jason.repository;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.jason.entity.DataSummery;

@Transactional
public interface DataSummeryRepository  extends CrudRepository<DataSummery, Long>{
	public DataSummery save(DataSummery dataSummery);
	public List<DataSummery> findByUpdateTime(Date updateTime);
	public int deleteByDatasourceName(String datasourceName);
	public DataSummery findByDatasourceName(String datasourceName);
	@Query(value = "select * from DG_DS_DATA_SUMMERY  where DATASOURCE_NAME in ('FRK','RK','HGJJ','DLXX') ", nativeQuery = true)
	public List<DataSummery> findByBase();
	@Query(value = "select * from DG_DS_DATA_SUMMERY  where DATASOURCE_NAME in ('XYZT','JYZT','HBZT','LYZT','RKZT') ", nativeQuery = true)
	public List<DataSummery> findByZt();
}
