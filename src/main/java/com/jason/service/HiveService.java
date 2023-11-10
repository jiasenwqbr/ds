package com.jason.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.cgs.db.meta.core.MetaLoader;
import com.cgs.db.meta.core.MetaLoaderImpl;
import com.cgs.db.meta.schema.Column;
import com.cgs.db.meta.schema.Table;

public class HiveService implements IDbService{

    public static final Logger LOG = LoggerFactory.getLogger(HiveService.class);

    private DriverManagerDataSource dataSource;
    
    private MetaLoader metaLoader;

    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    public HiveService(String ip,String port,String database,String user_name,String password) {
        dataSource =  new DriverManagerDataSource();
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl("jdbc:hive2://"+ip+":"+port+"/"+database);
//        dataSource.setUsername(user_name);
//        dataSource.setPassword(password);
        metaLoader = new MetaLoaderImpl(dataSource);

    }


    @Override
    public List<com.jason.entity.Table> getTables() {
        List<com.jason.entity.Table> tables = new ArrayList<com.jason.entity.Table>();
        Connection conn = null;
        try{
        	conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
            String sql = "show tables";
            ResultSet res = stmt.executeQuery(sql);

            while (res.next()) {
            	com.jason.entity.Table t = new com.jason.entity.Table();
            	t.setTableName(res.getString(1));
                tables.add(t);
            }
        }catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=conn){
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
        return tables;
    }

    @Override
    public List<String> getTableByName(String tableName) throws Exception {
        List<String> list = new ArrayList<String>();
        list.add(tableName);
        return list;
    }

    @Override
    public List<Column> getColumnsByTable(String tableName) throws Exception {
        List<Column> columns = new ArrayList<>();
        Connection conn = null;
        try{
        	conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "describe "+tableName ;
            ResultSet res = stmt.executeQuery(sql);

            while (res.next()) {
                Column c = new Column();
                c.setName(res.getString(1));
                c.setTypeName(res.getString(2));
                c.setComment(res.getString(3));
                columns.add(c);
            }
        } catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=conn){
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

        return columns;
    }

    @Override
	public Table getTableMeta(String tableName) throws Exception {
    	return null;
	}


	@Override
	public List<com.jason.entity.Column> getColumns(String tableName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public com.jason.entity.Column getColumnInfo(String tableName, String columnName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


}
