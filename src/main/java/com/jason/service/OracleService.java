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
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import com.cgs.db.meta.core.MetaLoader;
import com.cgs.db.meta.core.MetaLoaderImpl;
import com.cgs.db.meta.schema.Column;
import com.cgs.db.meta.schema.Table;

public class OracleService implements IDbService {
    public static final Logger LOG = LoggerFactory.getLogger(OracleService.class);
    private DriverManagerDataSource dataSource;
    private MetaLoader metaLoader;

    private OracleService(){

    }

    /**
     *
     * @param ip_address
     * @param port
     * @param database
     * @param user_name
     * @param password
     */
    public OracleService(String ip_address, String port, String database, String user_name, String password){
        try {
            if(dataSource==null){
                dataSource = new DriverManagerDataSource();
            }
            dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
            dataSource.setUrl("jdbc:oracle:thin:@"+ip_address+":"+port+"/"+database);
            dataSource.setUsername(user_name);
            dataSource.setPassword(password);
            metaLoader = new MetaLoaderImpl(dataSource);
        }
        catch (Exception ex){
            ex.printStackTrace();
            LOG.error(ex.getMessage());
        }
    }

	public List<com.jason.entity.Table> getTables() {
		List<com.jason.entity.Table> tables = new ArrayList<com.jason.entity.Table>();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();

			Statement stmt = connection.createStatement();
			String sql = "select table_name,comments from user_tab_comments ";
			ResultSet tabrs = stmt.executeQuery(sql);
			while (tabrs.next()) {
				com.jason.entity.Table t = new com.jason.entity.Table();
				t.setTableName(tabrs.getString("table_name"));
				t.setComment(tabrs.getString("comments"));
				tables.add(t);
			}
//			sql = "select view_name  from user_views";
//			ResultSet viewrs = stmt.executeQuery(sql);
//			while (viewrs.next()) {
//				com.jusfoun.entity.Table t = new com.jusfoun.entity.Table();
//				t.setTableName(viewrs.getString("view_name"));
//				tables.add(t);
//			}

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
    public Table getTableMeta(String tableName) throws Exception{
        Table table = metaLoader.getTable(tableName.toUpperCase());
        return table;
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
			String sql = "SELECT TABLE_NAME,COLUMN_NAME,COMMENTS FROM user_col_comments WHERE table_name='"+tableName.toUpperCase()+"'";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				com.jason.entity.Column c = new com.jason.entity.Column();
				c.setColumnName(rs.getString("COLUMN_NAME"));
				String comments = rs.getString("COMMENTS");
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
			String sql = "SELECT COLUMN_NAME,DATA_TYPE,DATA_LENGTH,NULLABLE,DATA_DEFAULT "
					+ "FROM user_tab_cols WHERE table_name='"+tableName+"' and column_name='"+columnName+"'";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				column.setColumnName(rs.getString("COLUMN_NAME"));
				column.setColumnType(rs.getString("DATA_TYPE"));
				column.setLength(rs.getInt("DATA_LENGTH"));
				String nullAble = rs.getString("NULLABLE");
				column.setNullable("Y".equals(nullAble)?true:false);
				column.setDefaultValue(rs.getString("DATA_DEFAULT"));
			}
			sql = "select * from user_constraints a, user_ind_columns b "
					+ "where a.index_name = b.index_name "
					+ "and a.table_name='"+tableName+"' and b.column_name='"+columnName+"' and constraint_type='P'";
			rs = stmt.executeQuery(sql);
			if(rs.next()) {
				column.setPrimaryKey(true);
			}else{
				column.setPrimaryKey(false);
			}
			sql = "SELECT  COMMENTS FROM user_col_comments WHERE table_name='"+tableName+"' and column_name='"+columnName+"'";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				column.setComment(rs.getString("COMMENTS"));
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
