package com.jason.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cgs.db.meta.core.MetaLoader;
import com.cgs.db.meta.core.MetaLoaderImpl;
import com.cgs.db.meta.schema.Column;
import com.cgs.db.meta.schema.Table;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class MysqlService implements IDbService {

    public static final Logger LOG = LoggerFactory.getLogger(MysqlService.class);
    private MysqlDataSource dataSource;
    private MetaLoader metaLoader;

   private MysqlService(){

   }

    /**
     *
     * @param ip_address
     * @param port
     * @param database
     * @param user_name
     * @param password
     */
    public MysqlService(String ip_address, String port, String database, String user_name, String password){
//        try {
            dataSource = new MysqlDataSource();
            dataSource.setServerName(ip_address);
            dataSource.setPort(Integer.parseInt(port));
            dataSource.setDatabaseName(database);
            dataSource.setUser(user_name);
            dataSource.setPassword(password);
            metaLoader = new MetaLoaderImpl(dataSource);
//        }
//        catch (Exception ex){
//            ex.printStackTrace();
//            LOG.error(ex.getMessage());
//        }
    }

    public List<com.jason.entity.Table> getTables() throws Exception {
		List<com.jason.entity.Table> tables = new ArrayList<com.jason.entity.Table>();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();

			Statement stmt = connection.createStatement();
			String sql = "select table_name,table_comment from information_schema.tables where table_schema = '"+dataSource.getDatabaseName()+"'";
			ResultSet tabrs = stmt.executeQuery(sql);
			while (tabrs.next()) {
				com.jason.entity.Table t = new com.jason.entity.Table();
				t.setTableName(tabrs.getString("table_name"));
				t.setComment(tabrs.getString("table_comment"));
				tables.add(t);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != connection) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return tables;
	}

    //获取table属性
    public Table getTableMeta(String tableName) throws Exception {
        Table tableMeta = metaLoader.getTable(tableName);
        return tableMeta;
    }

    public List<Column> getColumnsByTable(String tableName) throws Exception {
        List<Column> columns = null;
        if(metaLoader!=null){
            Table table = metaLoader.getTable(tableName);
            columns = new ArrayList<Column>();
            Column column = null;
            Map<String, Column> columnMap = table.getColumns();
            for (Map.Entry<String,Column> entry:columnMap.entrySet()){
                column = entry.getValue();
                columns.add(column);
            }
        }
        return columns;
    }

    @Override
    public List<String> getTableByName(String tableName) throws Exception {
        List<String> tables = null;
        if(metaLoader!=null){
            tables = new ArrayList<String>();
            Table table = metaLoader.getTable(tableName);
            if(table!=null)
                tables.add(table.getName());
        }
        return tables;
    }

	@Override
	public List<com.jason.entity.Column> getColumns(String tableName) throws Exception {
		List<com.jason.entity.Column> columns = new ArrayList<com.jason.entity.Column>();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();

			Statement stmt = connection.createStatement();
			String sql = "select COLUMN_NAME,COLUMN_COMMENT from information_schema.`COLUMNS` where "
					+ "table_schema = '"+dataSource.getDatabaseName()+"' and table_name = '"+tableName+"'";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				com.jason.entity.Column c = new com.jason.entity.Column();
				c.setColumnName(rs.getString("COLUMN_NAME"));
				String comments = rs.getString("COLUMN_COMMENT");
				if(!StringUtils.isEmpty(comments)){
					c.setComment(comments);
				}
				columns.add(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != connection) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return columns;
	}

	@Override
	public com.jason.entity.Column getColumnInfo(String tableName, String columnName) throws Exception {
		com.jason.entity.Column column = new com.jason.entity.Column();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();

			Statement stmt = connection.createStatement();
			String sql = "select COLUMN_NAME,COLUMN_COMMENT,DATA_TYPE,CHARACTER_OCTET_LENGTH,COLUMN_KEY,IS_NULLABLE,COLUMN_DEFAULT "
					+ "from information_schema.`COLUMNS` where table_schema = '"+dataSource.getDatabaseName()+"' and table_name = '"+tableName+"' and COLUMN_NAME = '"+columnName+"'";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				column.setColumnName(rs.getString("COLUMN_NAME"));
				column.setColumnType(rs.getString("DATA_TYPE"));
				column.setLength(rs.getInt("CHARACTER_OCTET_LENGTH"));
				String nullAble = rs.getString("IS_NULLABLE");
				column.setNullable("YES".equals(nullAble)?true:false);
				column.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
				column.setComment(rs.getString("COLUMN_COMMENT"));
				String key = rs.getString("COLUMN_KEY");
				column.setPrimaryKey("PRI".equalsIgnoreCase(key)?true:false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != connection) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return column;
	}
}
