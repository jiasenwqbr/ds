package com.jason.util;

import com.jason.entity.DataSourceInfo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.HashMap;
import java.util.Map;

public class DBUtil extends JdbcTemplate{
    private static Map<String,String> dbDrivers;
    static {
        dbDrivers = new HashMap<String, String>();
        dbDrivers.put("mysql","com.mysql.jdbc.Driver");
        dbDrivers.put("oracle","oracle.jdbc.driver.OracleDriver");
        dbDrivers.put("sqlserver","com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dbDrivers.put("hive","org.apache.hive.jdbc.HiveDriver");
    }
    private static DBUtil dbUtil=null;
    /*获取实例对象*/
    public static JdbcTemplate getDBUtil(DataSourceInfo sdp){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();


        String dbType = sdp.getDbType();
        dataSource.setDriverClassName(dbDrivers.get(dbType));
        //拼接url地址
        String url = null;
        //注意检查拼写是否规范
        if(dbType.equals("mysql")) {
            url = "jdbc:"+dbType+"://"+sdp.getIp()+":"+sdp.getPort()+"/"+sdp.getInstance();
        }
        else if (dbType.equals("oracle")) {
            url = "jdbc:" + dbType + ":thin:@" + sdp.getIp() + ":" + sdp.getPort() + "/" + sdp.getInstance();
        }
        else if (dbType.equals("sqlserver")) {
            url = "jdbc:" + dbType + "://" + sdp.getIp() + ":" + sdp.getPort() + ";DatabaseName=" + sdp.getInstance();
        }
        else if (dbType.equals("hive")) {//"jdbc:hive2://"+ip+":"+port+"/"+database
            url = "jdbc:hive2://" + sdp.getIp() + ":" + sdp.getPort() + "/" + sdp.getInstance();
        }
        dataSource.setUrl(url);
        dataSource.setUsername(sdp.getUsername());

        dataSource.setPassword(sdp.getPassword());
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        return jdbc;
    }
}
