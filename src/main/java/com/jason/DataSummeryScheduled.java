package com.jason;

import java.util.Date;

import javax.annotation.Resource;

import com.jason.repository.DataSummeryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jason.entity.DataSummery;
import com.jason.util.HttpHelper;

@Component
public class DataSummeryScheduled {
	private static final Logger logger = LoggerFactory
			.getLogger(DataSummeryScheduled.class);
	@Resource
    DataSummeryRepository dataSummeryRepository;
	

	//@Scheduled(cron = "0 0/720 8-20 * * ?")
	public void executeDataSumTask() {

		// 查询三大库及资源目录库数据量
		System.out.println("定时任务开始:");
		
		String url = "http://172.19.116.161:9014/ds/tableAndRowCount/";
		saveSum("base",url);
		saveSum("zt",url);
		saveSum("land",url);
		
		
		
		
		
		System.out.println("定时任务结束:");
	}
	
	
	public void saveSum(String database,String url){
		
		System.out.println("..........................:"+database);
		
		String str=HttpHelper.sendGetByUrlConnection(url+database, null, "UTF-8");
		System.out.println(str);
    	JSONArray obj = JSONArray.parseArray(str);
    	for (int i = 0;i<obj.size();i++){
    		JSONObject jobj = (JSONObject) obj.get(i);
    		System.out.println(jobj.get("tableCount"));
    		System.out.println(jobj.get("rowCount"));
    		System.out.println(jobj.get("datasourceName"));
    		if(jobj.get("datasourceName")==null||"".equals(jobj.get("datasourceName"))){
    			dataSummeryRepository.deleteByDatasourceName("LAND");
    		}else{
    			dataSummeryRepository.deleteByDatasourceName((String) jobj.get("datasourceName"));
    		}
    		
    		DataSummery dataSummery = new DataSummery();
    		if(jobj.get("datasourceName")==null||"".equals(jobj.get("datasourceName"))){
    			dataSummery.setDatasourceName("LAND");
    		}else{
    			dataSummery.setDatasourceName((String) jobj.get("datasourceName"));
    		}
    		
    		dataSummery.setRowCount(Long.valueOf(jobj.get("rowCount").toString()) );
    		dataSummery.setTableCount(Long.valueOf(jobj.get("tableCount").toString()) );
    		dataSummery.setUpdateTime(new Date());
    		
    		dataSummeryRepository.save(dataSummery);
    	}
		
		
		
	}
	
	
	
	
	

}
