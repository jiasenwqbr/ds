package com.jason.entity;

public class DataSourceParam {
    public String dbType;
    public String ip;
    public String port;
    public String database;
    public String datasourceName;
    public String username;
    public String password;

    //FTP_remote_path
    public String path;

    public String tableName;

    public DataSourceParam(String dbType, String ip, String port, String database, String datasourceName,
			String username, String password, String path, String tableName) {
		super();
		this.dbType = dbType;
		this.ip = ip;
		this.port = port;
		this.database = database;
		this.datasourceName = datasourceName;
		this.username = username;
		this.password = password;
		this.path = path;
		this.tableName = tableName;
	}

	public DataSourceParam() {
		super();
	}

	public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public String getUsername() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassWord(String password) {
        this.password = password;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;

    }

}
