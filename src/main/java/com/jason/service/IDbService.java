package com.jason.service;

import java.util.List;

import com.cgs.db.meta.schema.Column;
import com.cgs.db.meta.schema.Table;

public interface IDbService {
	
    //取得表信息
    List<com.jason.entity.Table> getTables() throws Exception;
    
    //取得字段列表
    List<com.jason.entity.Column> getColumns(String tableName) throws Exception;
    
    com.jason.entity.Column getColumnInfo(String tableName, String columnName) throws Exception;
    
    List<String> getTableByName(String tableName) throws Exception;
    //取得字段列表
    List<Column> getColumnsByTable(String tableName) throws Exception;
    //获取table属性
    Table getTableMeta(String tableName) throws Exception;


}
