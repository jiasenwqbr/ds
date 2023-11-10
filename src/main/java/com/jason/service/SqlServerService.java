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

public class SqlServerService implements IDbService {
    public static final Logger LOG = LoggerFactory.getLogger(SqlServerService.class);
    private DriverManagerDataSource dataSource;
    private MetaLoader metaLoader;

    private SqlServerService(){

    }

    /**
     *
     * @param ip_address
     * @param port
     * @param database
     * @param user_name
     * @param password
     */
    public SqlServerService(String ip_address, String port, String database, String user_name, String password){
        try {
            if(dataSource==null){
                dataSource = new DriverManagerDataSource();
            }
            dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            dataSource.setUrl("jdbc:sqlserver://"+ip_address+":"+port+";DatabaseName="+database);
            dataSource.setUsername(user_name);
            dataSource.setPassword(password);
            metaLoader = new MetaLoaderImpl(dataSource);
        }
        catch (Exception ex){
            ex.printStackTrace();
            LOG.error(ex.getMessage());
        }
    }

    public List<com.jason.entity.Table> getTables() throws Exception {
		List<com.jason.entity.Table> tables = new ArrayList<com.jason.entity.Table>();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();

			Statement stmt = connection.createStatement();
			String sql = "select d.name,cast(f.[value] as varchar(500)) value "
					+ "FROM  sysobjects d  left join sys.extended_properties f  on"
					+ " d.id=f.major_id and f.minor_id=0 "
					+ "where (d.xtype='V' or d.xtype='U')";
			ResultSet tabrs = stmt.executeQuery(sql);
			while (tabrs.next()) {
				com.jason.entity.Table t = new com.jason.entity.Table();
				t.setTableName(tabrs.getString("name"));
				t.setComment(tabrs.getString("value"));
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
			String sql = "select a.name,cast(g.[value] as varchar(500)) as value "
					+ "FROM syscolumns a "
					+ " inner join sysobjects d "
					+ " on a.id=d.id and  (d.xtype='V' or d.xtype='U') "
					+ "left join sys.extended_properties g  "
					+ " on a.id=g.major_id AND a.colid = g.minor_id   "
					+ "where  d.name='"+tableName+"' ";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				com.jason.entity.Column c = new com.jason.entity.Column();
				c.setColumnName(rs.getString("name"));
				String comments = rs.getString("value");
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
			String sql = "SELECT COLUMN_NAME,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,IS_NULLABLE,COLUMN_DEFAULT "
					+ "FROM INFORMATION_SCHEMA.COLUMNS "
					+ "where TABLE_NAME = '"+tableName+"' and COLUMN_NAME = '"+columnName+"'";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				column.setColumnName(rs.getString("COLUMN_NAME"));
				column.setColumnType(rs.getString("DATA_TYPE"));
				column.setLength(rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
				String nullAble = rs.getString("IS_NULLABLE");
				column.setNullable("YES".equals(nullAble)?true:false);
				column.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
//				column.setComment(rs.getString("COLUMN_COMMENT"));
//				String key = rs.getString("COLUMN_KEY");
//				column.setPrimaryKey("PRI".equalsIgnoreCase(key)?true:false);
			}
			sql = "SELECT CONSTRAINT_NAME FROM information_schema.key_column_usage "
					+ "where TABLE_NAME = '"+tableName+"' and COLUMN_NAME = '"+columnName+"'";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String key = rs.getString("CONSTRAINT_NAME");
				column.setPrimaryKey(key.startsWith("PK_")?true:false);
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
