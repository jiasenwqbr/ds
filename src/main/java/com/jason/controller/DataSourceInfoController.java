package com.jason.controller;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.jason.entity.DataSourceInfo;
import com.jason.entity.DataSourceParam;
import com.jason.entity.DataSummery;
import com.jason.entity.NodeInfo;
import com.jason.repository.DataSourceInfoRepository;
import com.jason.repository.DataSummeryRepository;
import com.jason.repository.NodeInfoRepository;
import com.jason.util.DBUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.sql.PagerUtils;
import com.cgs.db.meta.schema.Column;
import com.cgs.db.meta.schema.Table;
import com.jason.service.HiveService;
import com.jason.service.IDbService;
import com.jason.service.MysqlService;
import com.jason.service.OracleService;
import com.jason.service.SqlServerService;

@RestController
public class DataSourceInfoController {

	@Resource
	DataSourceInfoRepository dataSourceInfoRepository;
	
	@Resource
	NodeInfoRepository nodeRepository;
	@Resource
	DataSummeryRepository dataSummeryRepository;
	@PersistenceContext
	EntityManager entityManager;
	private IDbService service;
	
	/**
     * 查询数据中心数据池中所有数据库信息  pl使用
     * @return
     */
	@RequestMapping(value = "/dataSource",method = RequestMethod.POST)
    @ResponseBody
	public List<DataSourceInfo> getDataSource() {
		List<DataSourceInfo> ds = dataSourceInfoRepository.findByDbTypeNotOrderById("ftp");
//		Collections.sort(ds, new DSComparator());
		return ds;
	}
	
	static class DSComparator implements Comparator {  
        public int compare(Object object1, Object object2) {// 实现接口中的方法  
        	DataSourceInfo p1 = (DataSourceInfo) object1; // 强制转换  
        	DataSourceInfo p2 = (DataSourceInfo) object2;  
        	return Collator.getInstance(Locale.CHINESE).compare(p1.getDatabaseDesc(), p2.getDatabaseDesc());
//            return p2.getDatabaseDesc().compareTo(p1.getDatabaseDesc()); 
            
        }  
    }  
	
	/**
     * 查询数据中心数据池中所有数据库信息
     * @return
     */
	@RequestMapping(value = "/delds",method = RequestMethod.POST)
    @ResponseBody
    public void delDataSource(Long id) {
      dataSourceInfoRepository.delete(id);
    }
	
	/**
     * 根据数据源的id查询数据源的信息
     * @param id 节点id
     * @return
     */
    @RequestMapping(value="/dataSource/getdsinfo",method = {RequestMethod.POST})
    @ResponseBody
    public DataSourceInfo findByTreeId(Long id){
    	DataSourceInfo dataSourceInfo = dataSourceInfoRepository.findByTreeId(id);
    	return dataSourceInfo;
    }
    
    /**
     * 根据数据源的id查询数据源的信息
     * @param id
     * @return
     */
    @RequestMapping(value="/savedatasource",method = {RequestMethod.POST})
    @ResponseBody
    public void saveDataSource(DataSourceInfo dataSourceInfo){
    	if(null !=dataSourceInfo){
            dataSourceInfoRepository.save(dataSourceInfo);
    	}
    }
    
    /**
     * 根据数据源树节点查找未添加表列表
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/dataSource/unselectedtables/{index}/{pageSize}", method = RequestMethod.POST)
    @ResponseBody
	public Map<String, Object> queryUnselectedTables(@PathVariable int index, @PathVariable int pageSize, String ip,
			String ds, String dbType, String tableName) throws Exception {
		String flag = dbType + "|" + ip + "|" + ds;
		List<NodeInfo> infos = nodeRepository.findByFlagAndType(flag, "2");
		if (infos.size() <= 0) {
			return new HashMap<>();
		}
		// 根据树节点id查询数据源信息
		DataSourceInfo dataSourceInfo = dataSourceInfoRepository.findByTreeId(infos.get(0).getId());
		if(null == dataSourceInfo){
    		return new HashMap<>();
    	}  
		// 链接数据源
		initService(dataSourceInfo);
		List<NodeInfo> selectedTabsNodes = nodeRepository
				.findByFlagLikeAndType(nodeRepository.findById(infos.get(0).getId()).getFlag() + ".%","3");
		List<String> selectedTabs = new ArrayList<String>();

		for (NodeInfo node : selectedTabsNodes) {
			String f = node.getDefname();
			selectedTabs.add(f);
		}
		List<com.jason.entity.Table> alltabs = service.getTables();
		List<com.jason.entity.Table> selectedTable = new ArrayList<com.jason.entity.Table>();
		Collections.sort(alltabs, new TableNameComparator());
		for(String tab : selectedTabs){
			for(com.jason.entity.Table t:alltabs){
				if(t.getTableName().equalsIgnoreCase(tab)){
					selectedTable.add(t);
				}
			}
		}
		
		alltabs.removeAll(selectedTable);
		
		if (!StringUtils.isEmpty(tableName)) {
			List<com.jason.entity.Table> tableNames = new ArrayList<com.jason.entity.Table>();
			for (com.jason.entity.Table tab : alltabs) {
				if (tab.getTableName().toUpperCase().contains(tableName.trim().toUpperCase())) {
					tableNames.add(tab);
				}
			}
			alltabs = tableNames;
		}
		int size = alltabs.size();
		Map<String, Object> map = new HashMap<>();
		double ceil = Math.ceil(alltabs.size() * 1.0 / pageSize);
		Object pageCount = ceil;
		map.put("pagecount", pageCount);
		map.put("rowslist", alltabs.subList(pageSize * (index-1),
				(pageSize * (index-1) + 10) < (size - 1) ? (pageSize * (index-1) + 10) : (size)));
		return map;
	}
    
    static class TableNameComparator implements Comparator {  
        public int compare(Object object1, Object object2) {// 实现接口中的方法  
        	com.jason.entity.Table p1 = (com.jason.entity.Table) object1; // 强制转换
        	com.jason.entity.Table p2 = (com.jason.entity.Table) object2;
            return p1.getTableName().compareTo(p2.getTableName());  
        }  
    }  
    
    /**
     * 根据数据源树节点查找表列表
     * pl使用
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/dataSource/tables", method = RequestMethod.POST)
    @ResponseBody
	public List<NodeInfo> queryTableInfos(Long id, String tableName) throws Exception {

		if (null == id) {
			return new ArrayList<NodeInfo>();
		}

		List<NodeInfo> tableInfos = new ArrayList<NodeInfo>();
		NodeInfo dsinfo = nodeRepository.findById(id);
		if (StringUtils.isEmpty(tableName)) {
			tableInfos = nodeRepository.findByFlagLikeAndTypeOrderByName(dsinfo.getFlag() + ".%", "3");
		} else {
			List<NodeInfo> infos1 = new ArrayList<NodeInfo>();
			List<NodeInfo> infos2 = new ArrayList<NodeInfo>();
			infos1 = nodeRepository.findByFlagLikeAndTypeAndNameLikeOrderByName(dsinfo.getFlag() + ".%", "3",
					"%"+tableName+"%");
			infos2 = nodeRepository.findByFlagLikeAndTypeAndDefnameLikeOrderByDefname(dsinfo.getFlag() + ".%", "3",
					"%"+tableName+"%");
			tableInfos.addAll(infos1);
			if(infos1.size()>0){
				tableInfos.removeAll(infos2);
				tableInfos.addAll(infos2);
			}else{
				tableInfos.addAll(infos2);
			}
		}

		return tableInfos;

	}
    
    
    
    /**
     * 根据字段的节点id获取字段信息      ip|实例名.用户名.表.字段
     *gai
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/dataSource/table/column", method = RequestMethod.POST)
    @ResponseBody
    public com.jason.entity.Column queryColumnsByTreeId(Long id) throws Exception {
    	NodeInfo nodeInfo = nodeRepository.findById(id);
    	String flag = nodeInfo.getFlag();
    	String dbType = flag.split("\\|")[0];
    	String IP = flag.split("\\|")[1];
    	String dataFlag = flag.split("\\|")[2];
    	String instance = dataFlag.split("\\.")[0];
    	String username = dataFlag.split("\\.")[1];
    	// 查询数据源信息
		List<DataSourceInfo> dataSourceInfos = dataSourceInfoRepository.findByIpAndInstanceAndUsernameAndDbType(IP,
				instance, username, dbType);
		if(dataSourceInfos.size()<=0){
    		return new com.jason.entity.Column();
    	}
    	// 链接数据源
        initService(dataSourceInfos.get(0));
        
        String columnName = dataFlag.split("\\.")[3];
        
        String tableName = dataFlag.split("\\.")[2];
        com.jason.entity.Column jusColumn = new com.jason.entity.Column();
        // mod hive
        if("hive".equals(dbType)){
        	List<Column> columnsByTable = service.getColumnsByTable(tableName);
        	for(Column c : columnsByTable){
        		if(columnName.equals(c.getName())){
        			jusColumn.setColumnName(c.getName());
        			jusColumn.setColumnType(c.getTypeName());
        			// 元数据管理中的数据地图需要增加展示属性
        			jusColumn.setComment(c.getComment());
        		}
        	}
		} else {
			// mod hive
			jusColumn = service.getColumnInfo(tableName, columnName);

		}
        return jusColumn;
    }
    
    /**
     * 
     * @param flag 数据库类型|ip|实例名.用户名.表名
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/dataSource/tablename/columninfos", method = RequestMethod.POST)
    @ResponseBody
    public List<com.jason.entity.Column> queryColumnInfos(String flag) throws Exception {
    	if(flag!=null){
    		String tabName = flag.substring(flag.lastIndexOf(".")+1, flag.length());
    		List<NodeInfo> nodes = nodeRepository.findByFlagLike(flag+".%");
    		//		mysql|58.240.82.126|test.test.t_test
    		List<com.jason.entity.Column> columnList = new ArrayList<com.jason.entity.Column>();
            for(NodeInfo n : nodes){
            	com.jason.entity.Column c = new com.jason.entity.Column();
            	c.setColumnId(tabName+"."+n.getDefname());
            	c.setComment(n.getName());
            	c.setColumnName(n.getDefname());
            	c.setTableName(tabName);
            	columnList.add(c);
            }
//            Collections.sort(columnList, new ColumnNameComparator());
            return columnList;
    	}else{
    		return new ArrayList<com.jason.entity.Column>();
    	}
    }
    /**
     * 根据表名(nodeinfo表中flag的格式：192.168.100.34|tbdpods.tongcheng)查找列集   
     *	
     * @param detailTabName 格式：192.168.100.34|tbdpods.tongcheng
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/dataSource/tablename/columns", method = RequestMethod.POST)
    @ResponseBody
    public List<com.jason.entity.Column> queryColumnsByTabName(String detailTabName, String username, String dbType) throws Exception {
    	if(detailTabName!=null){
    		String ip = detailTabName.split("\\|")[0];
    		String tabName = detailTabName.split("\\|")[1];
			List<DataSourceInfo> dataSourceInfos = dataSourceInfoRepository.findByIpAndInstanceAndUsernameAndDbType(ip,
					tabName.substring(0, tabName.indexOf(".")), username, dbType);
			if(dataSourceInfos.size()<=0){
	    		return new ArrayList<>();
	    	}
    		// 链接数据源
            initService(dataSourceInfos.get(0));
    		
            String tableName = tabName.substring(tabName.lastIndexOf(".")+1, tabName.length());
            Table tableMeta = service.getTableMeta(tableName);
            Map<String, Column> columnMap = tableMeta.getColumns();
            Column columnInit = null;
            List<Column> columns = new ArrayList<Column>();
            for (Map.Entry<String,Column> entry:columnMap.entrySet()){
                columnInit = entry.getValue();
                columns.add(columnInit);
            }
            //查询主键列
            List<String> primaryKeyColumns = tableMeta.getPrimaryKey().getColumns();
            Boolean hasPrimaryKey = false;
            if (CollectionUtils.isNotEmpty(primaryKeyColumns)){
                hasPrimaryKey = true;
            }
            List<com.jason.entity.Column> columnList
                    = new ArrayList<com.jason.entity.Column>();

            for (int i = 0; i < columns.size(); i++) {
            	com.jason.entity.Column column = new com.jason.entity.Column();
                column.setColumnName(columns.get(i).getName());
                column.setColumnType(columns.get(i).getTypeName());
                // 元数据管理中的数据地图需要增加展示属性
                column.setComment(columns.get(i).getComment());
                column.setDefaultValue(columns.get(i).getDefaultValue());
                column.setLength(columns.get(i).getLength());
                column.setNullable(columns.get(i).isNullable());
                //增加结束
                column.setTableName(tableName);
                column.setColumnId(tableName + "." + columns.get(i).getName());
                column.setPrimaryKey(false);
                //设置主键列
                if (hasPrimaryKey){
                    for(String str:primaryKeyColumns){
                        if (column.getColumnName().equals(str)){
                            column.setPrimaryKey(true);
                            break;
                        }
                    }
                }

                columnList.add(column);
            }
            Collections.sort(columnList, new ColumnNameComparator());
            return columnList;
    		
    	}else{
    		return new ArrayList<com.jason.entity.Column>();
    	}
    	
    }
    
    
    /**
     * 根据表节点id查找未添加列集
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/dataSource/table/unselectedcolumns", method = RequestMethod.POST)
    @ResponseBody
    public List<com.jason.entity.Column> queryUnselectedColumns(Long id) throws Exception {
    	NodeInfo nodeInfo = nodeRepository.findById(id);
    	String flag = nodeInfo.getFlag();
    	String dbType = flag.split("\\|")[0];
    	String IP = flag.split("\\|")[1];
    	String dataFlag = flag.split("\\|")[2];
    	String instance = dataFlag.split("\\.")[0];
    	String username = dataFlag.split("\\.")[1];
    	// 查询数据源信息
		List<DataSourceInfo> dataSourceInfos = dataSourceInfoRepository.findByIpAndInstanceAndUsernameAndDbType(IP,
				instance, username, dbType);
		if(dataSourceInfos.size()<=0){
    		return new ArrayList<>();
    	}    	
    	// 链接数据源
    	initService(dataSourceInfos.get(0));
    	        
        
        String tableName = dataFlag.split("\\.")[2];
        List<com.jason.entity.Column> columnList = new ArrayList<com.jason.entity.Column>();
        // mod 添加hive
        if("hive".equals(dbType)){
        	List<Column> columnsByTable = service.getColumnsByTable(tableName);
        	for(Column c : columnsByTable){
        		com.jason.entity.Column column = new com.jason.entity.Column();
        		column.setColumnName(c.getName());
        		if(StringUtils.isEmpty(c.getComment())){
        			column.setComment(c.getComment());
        		}
        		columnList.add(column);
        		
        	}
		} else {
			columnList = service.getColumns(tableName);
		}
        // mod 添加hive

        List<NodeInfo> selectedColInfos = nodeRepository.findByFlagLike(nodeRepository.findById(id).getFlag()+".%");
        List<String> selectedCols = new ArrayList<String>();
        
        for (NodeInfo node : selectedColInfos){
        	String f = node.getFlag();
        	selectedCols.add(f.substring(f.lastIndexOf(".")+1, f.length()));
        }
        List<com.jason.entity.Column> unSelectedColumnList
        = new ArrayList<com.jason.entity.Column>();
        for(com.jason.entity.Column c:columnList){
        	if(!selectedCols.contains(c.getColumnName())){
        		unSelectedColumnList.add(c);
        	}
        }
        //Collections.sort(unSelectedColumnList, new ColumnNameComparator());
        return unSelectedColumnList;
    }
    
    static class ColumnNameComparator implements Comparator {  
        public int compare(Object object1, Object object2) {// 实现接口中的方法  
        	com.jason.entity.Column p1 = (com.jason.entity.Column) object1; // 强制转换
        	com.jason.entity.Column p2 = (com.jason.entity.Column) object2;
            return p1.getColumnName().compareTo(p2.getColumnName());  
        }  
    }  
    
    /**
     * 根据表节点id查找列集
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/dataSource/table/columns", method = RequestMethod.POST)
    @ResponseBody
    public List<com.jason.entity.Column> queryColumnsByTableTreeId(Long id) throws Exception {
    	NodeInfo nodeInfo = nodeRepository.findById(id);
    	String flag = nodeInfo.getFlag();
    	String dbType = flag.split("\\|")[0];
    	String IP = flag.split("\\|")[1];
    	String dataFlag = flag.split("\\|")[2];
    	String instance = dataFlag.split("\\.")[0];
    	String username = dataFlag.split("\\.")[1];
    	// 查询数据源信息
		List<DataSourceInfo> dataSourceInfos = dataSourceInfoRepository.findByIpAndInstanceAndUsernameAndDbType(IP,
				instance, username, dbType);
		if(dataSourceInfos.size()<=0){
    		return new ArrayList<>();
    	}    	
    	// 链接数据源
    	initService(dataSourceInfos.get(0));
    	        
    	
        
        String tableName = dataFlag.split("\\.")[2];
        Table tableMeta = service.getTableMeta(tableName);
        Map<String, Column> columnMap = tableMeta.getColumns();
        
        
        Column columnInit = null;
        List<Column> columns = new ArrayList<Column>();
        for (Map.Entry<String,Column> entry:columnMap.entrySet()){
            columnInit = entry.getValue();
            columns.add(columnInit);
        }
        // ---oracle 描述
        String comment = "";
        List<Map<String, Object>> comments = new ArrayList<Map<String,Object>>();
        if("oracle".equals(dataSourceInfos.get(0).getDbType())){
        	JdbcTemplate dbUtil = DBUtil.getDBUtil(dataSourceInfos.get(0));
        	comment = "select column_name,comments from user_col_comments where Table_Name= '"+tableName+"'";
        	comments = dbUtil.queryForList(comment);
        	if(comments.size()>0){
        		for(Map<String, Object> m : comments){
            		for(Column c:columns){
						if (c.getName().equals(m.get("COLUMN_NAME")) && null != m.get("COMMENTS")) {
							c.setComment(m.get("COMMENTS").toString());
						}
            		}

                }
        	}
        }
        
        
        // ---oracle 描述
        //查询主键列
        List<String> primaryKeyColumns = tableMeta.getPrimaryKey().getColumns();
        Boolean hasPrimaryKey = false;
        if (CollectionUtils.isNotEmpty(primaryKeyColumns)){
            hasPrimaryKey = true;
        }
        List<com.jason.entity.Column> columnList
                = new ArrayList<com.jason.entity.Column>();

        for (int i = 0; i < columns.size(); i++) {
        	com.jason.entity.Column column = new com.jason.entity.Column();
            column.setColumnName(columns.get(i).getName());
            column.setColumnType(columns.get(i).getTypeName());
            // 元数据管理中的数据地图需要增加展示属性
            column.setComment(columns.get(i).getComment());
            column.setDefaultValue(columns.get(i).getDefaultValue());
            column.setLength(columns.get(i).getLength());
            column.setNullable(columns.get(i).isNullable());
            //增加结束
            column.setTableName(tableName);
            column.setColumnId(tableName + "\\." + columns.get(i).getName());
            column.setPrimaryKey(false);
            //设置主键列
            if (hasPrimaryKey){
                for(String str:primaryKeyColumns){
                    if (column.getColumnName().equals(str)){
                        column.setPrimaryKey(true);
                        break;
                    }
                }
            }

            columnList.add(column);
        }
        return columnList;
    }
    
    @RequestMapping(value = "/db/queryTableData/{index}/{pageSize}", method = RequestMethod.POST)
    @ResponseBody
	public Map<String, Object> queryTableData(@PathVariable int index, @PathVariable int pageSize, Long id,
			String columnName, String columnValue) {

		NodeInfo nodeInfo = nodeRepository.findById(id);
		String flag = nodeInfo.getFlag();
		String dbType = flag.split("\\|")[0];
		String IP = flag.split("\\|")[1];
		String dataFlag = flag.split("\\|")[2];
		String instance = dataFlag.split("\\.")[0];
    	String username = dataFlag.split("\\.")[1];
    	// 查询数据源信息
		List<DataSourceInfo> dataSourceInfos = dataSourceInfoRepository.findByIpAndInstanceAndUsernameAndDbType(IP,
				instance, username, dbType);
    	if(dataSourceInfos.size()<=0){
    		return new HashMap<>();
    	}
    	// 链接数据源
    	initService(dataSourceInfos.get(0));
		String tableName = dataFlag.split("\\.")[2];
		// 判断表名称参数
		if (StringUtils.isEmpty(tableName)) {
			return null;
		}
		JdbcTemplate dbUtil = DBUtil.getDBUtil(dataSourceInfos.get(0));
		List<List<Object>> rowsLists = new ArrayList<List<Object>>();
		/*
		 * 分页查询表数据
		 */
		String sql = "select * from " + tableName;
		/* 过滤字段 */
		if (StringUtils.isEmpty(columnName) || StringUtils.isEmpty(columnValue)) {

		} else {
			//sql = "select * from " + tableName + " where " + columnName + " like '%" + columnValue + "%'";
			sql = "select * from " + tableName + " where " + columnName + " = '" + columnValue + "'";
		}
		int offset = (index - 1) * 10;
		/* 参数说明 offset指偏移量即要开始的地方，pagesize指每页数据量 */
		String limitSql = PagerUtils.limit(sql, dataSourceInfos.get(0).getDbType(), offset, pageSize);// 获取分页sql
		List<Map<String, Object>> maps = dbUtil.queryForList(limitSql);
		List<Object> columnList = new ArrayList<Object>();
		List<Object> columnComments = new ArrayList<Object>();
		// 转换map集合
		for (int i = 0; i < maps.size(); i++) {
			// 1、添加字段
			List<NodeInfo> nodeinfos = nodeRepository.findByFlagLike(flag+".%");
			Map<Object,NodeInfo> zhName = new HashMap<Object,NodeInfo>();
			for(NodeInfo n:nodeinfos){
				zhName.put(n.getDefname(), n);
			}
			Map<String, Object> map = maps.get(i);// 行数据
			List<Object> rowList = new ArrayList<Object>();
			rowList.add(i);// 作为字段索引加入进去
			if (i == 0) {
				columnList.add("rowIndex");
				columnComments.add("序号");
			}
			for (String key : map.keySet()) {
				// 获取字段信息
				if (i == 0) {
					columnList.add(key);
					if(zhName.get(key)==null||"1".equals(zhName.get(key).getShowStatus())){
					}else if (!StringUtils.isEmpty(zhName.get(key).getName())) {
						columnComments.add(zhName.get(key).getName() + "(" + key + ")");
					} else{
						columnComments.add(key);
					}
				}
				if(!"RN".equals(key)){
					Object v = map.get(key);
					if(zhName.get(key)==null){
					}else if("0".equals(zhName.get(key).getShowStatus())){//顯示
						if(v instanceof Date){
							Date d = (Date) v;
							SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
							String str=sdf.format(d);
							rowList.add(str);
						}else rowList.add(v);
					}else if("2".equals(zhName.get(key).getShowStatus())){//脫敏
						rowList.add("***");
					}else{//隱藏
					}
				}
			}
			// 2、添加转换后的字段值数据
			rowsLists.add(rowList);
		}
		rowsLists.add(0, columnList);
		rowsLists.add(1, columnComments);
		// 记录一个分页数
		String sizeSql = "select count(1) T_SIZE from " + tableName;
		if (!StringUtils.isEmpty(columnName) && !StringUtils.isEmpty(columnValue)) {
			//sizeSql = "select count(1) T_SIZE from " + tableName + " where " + columnName + " like '%" + columnValue
					//+ "%'";
			sizeSql = "select count(1) T_SIZE from " + tableName + " where " + columnName + " = '" + columnValue
					+ "'";
			
		}
		List<Map<String, Object>> maps1 = dbUtil.queryForList(sizeSql);
		Object t_size = maps1.get(0).get("T_SIZE");
		double ceil = Math.ceil(Integer.parseInt(t_size.toString()) * 1.0 / pageSize);
		Object pageCount = ceil;
//		Object[] obj = { rowsLists, pageCount };
		// 字段显示中文
		// 结果为空返回字段
		if(rowsLists.size()==0){
			Table tableMeta;
			try {
				tableMeta = service.getTableMeta(tableName);
				Map<String, Column> columnMap = tableMeta.getColumns();
				for (Column v : columnMap.values()) {
					columnList.add(v.getName());
				}
				rowsLists.add(columnList);
				for (Column v : columnMap.values()) {
					List<NodeInfo> nodes = nodeRepository.findByFlag(flag+"."+v.getName());
					if(nodes.size()>0){
						if(!StringUtils.isEmpty(nodes.get(0).getDefname())){
							columnComments.add(nodes.get(0).getName()+"("+nodes.get(0).getDefname()+")");
						}else{
							columnComments.add(nodes.get(0).getName());
						}
					}else{
						columnComments.add(v.getName());
					}
				}
				rowsLists.add(columnComments);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
		}
		
		Map<String, Object> map = new HashMap<>();
		map.put("pagecount", pageCount);
		map.put("rowslist", rowsLists);
		return map;
	}
    
	
    private void initService(DataSourceInfo dataSourceInfo) {
		switch (dataSourceInfo.getDbType().toLowerCase()) {
		case "oracle":
			service = new OracleService(dataSourceInfo.getIp(), dataSourceInfo.getPort(), dataSourceInfo.getInstance(),
					dataSourceInfo.getUsername(), dataSourceInfo.getPassword());
			break;
		case "mysql":
			service = new MysqlService(dataSourceInfo.getIp(), dataSourceInfo.getPort(), dataSourceInfo.getInstance(),
					dataSourceInfo.getUsername(), dataSourceInfo.getPassword());
			break;
		case "hive":
			service = new HiveService(dataSourceInfo.getIp(), dataSourceInfo.getPort(), dataSourceInfo.getInstance(),
					dataSourceInfo.getUsername(), dataSourceInfo.getPassword());
			break;
		case "sqlserver":
			service = new SqlServerService(dataSourceInfo.getIp(), dataSourceInfo.getPort(), dataSourceInfo.getInstance(),
					dataSourceInfo.getUsername(), dataSourceInfo.getPassword());
			break;
			
		}
    }
    
    private void initService(DataSourceParam dataSourceParam) {
        switch(dataSourceParam.dbType.toLowerCase()){
            case "oracle":
                service = new OracleService(dataSourceParam.ip, dataSourceParam.port, dataSourceParam.database, dataSourceParam.username, dataSourceParam.password);
                break;
            case "mysql":
                service = new MysqlService(dataSourceParam.ip, dataSourceParam.port, dataSourceParam.database, dataSourceParam.username, dataSourceParam.password);
                break;
            case "hive":
                service = new HiveService(dataSourceParam.ip, dataSourceParam.port, dataSourceParam.database, dataSourceParam.username, dataSourceParam.password);
                break;
            case "sqlserver":
                service = new SqlServerService(dataSourceParam.ip, dataSourceParam.port, dataSourceParam.database, dataSourceParam.username, dataSourceParam.password);
                break;
        }
    }
    /**
     * 查询base库中所有表数据量的和
     * @param database
     * @return
     */
    @RequestMapping(value = "/rowCount/{database}")
	@ResponseBody
	public Map<String, Object> rowCount(@PathVariable String database) {
    	List<String> list = new ArrayList<String>();
    	list.add("c##land");
    	list.add("c##base");
    	list.add("c##zt");
    	int num = 0;
    	for (String db : list){
    		DataSourceInfo sdp = dataSourceInfoRepository.findByUsername(db).get(0);
    		JdbcTemplate dbUtil = DBUtil.getDBUtil(sdp);
    		List<NodeInfo> nodes = nodeRepository.findByFlagLikeAndTypeOrderByName("%.c##land.%", "3");
    		if (nodes.size()>0){
    			String Tables = "";
        		for(NodeInfo node :nodes){
        			if(!node.getDefname().startsWith("BMK")&&!node.getDefname().startsWith("TMP")&&"3".equals(node.getType())){
        				Tables+=","+node.getDefname();
        			}
        		}
        		Tables =Tables.replaceFirst(",", "");
        		//String sqlForRow="select calallcrows_025('"+Tables+"') as dataRow from dual";
        		String sqlForRow="select sum(t.num_rows) as dataRow from user_tables t ";
        		Map<String, Object> maps = dbUtil.queryForMap(sqlForRow);
        		if (maps.get("DATAROW")!=null){
        			num += Integer.valueOf( String.valueOf(maps.get("DATAROW")));
        		}
        		
    			
    		}
    		
    	}
    	Map<String, Object> m = new HashMap<String,Object>();
    	m.put("DATAROW", num);
    	System.out.println(",.......................数据量之和："+num);
		return m;
	}
    
    /**
     * 查询库中部分表的个数
     * @param database
     * @param prefix
     * @return
     */
    @RequestMapping(value = "/tableCount/{database}")
	@ResponseBody
	public Map<String, Object> tableCount(@PathVariable String database, String prefix) {
    	if(prefix==null)prefix="";
    	DataSourceInfo sdp = dataSourceInfoRepository.findByUsername(database).get(0);
		JdbcTemplate dbUtil = DBUtil.getDBUtil(sdp);
		String sqlForTable="SELECT count(t.TABLE_NAME) as tableCount FROM user_tables t where t.TABLE_NAME like '"+prefix+"%'";
		Map<String, Object> mapTable = dbUtil.queryForMap(sqlForTable);
		return mapTable;
	}
    /**
     * 查询库中部分表的个数及数据量的和
     * @param database
     * @param prefix
     * @return
     */
    @RequestMapping(value = "/tableAndRowCount1/{database}")
	@ResponseBody
	public ArrayList<Map<String, String>> tableAndRowCount1(@PathVariable String database, String simpleCode) {
    	if(simpleCode == null){
    		simpleCode = "";
    	}
    	database = "c##"+database;
    	List<DataSourceInfo> sdps = dataSourceInfoRepository.findByUsername(database);
    	for (DataSourceInfo di :sdps)
    	{
    		System.out.println(di.getDatasourceName()+".........."+di.getIp()+"....."+di.getUsername());
    	}
		JdbcTemplate dbUtil = DBUtil.getDBUtil(sdps.get(0));
		String prefix = "";
		ArrayList<Map<String, String>> results = new ArrayList<Map<String, String>>();
		for(DataSourceInfo sdp:sdps){
			Map<String, String> mapTable = new HashMap<>();
			switch (sdp.getDatasourceName()){
				case "企业法人库": prefix = "FRK";break;
				case "自然人库": prefix = "RK";break;
				case "非企业法人库": prefix = "HGJJ";break;
				case "资源目录库": prefix = "DLXX";break;
				case "人口专题库": prefix = "RKZT";break;
				case "重点人群专题库": prefix = "LYZT";break;
				case "信用信息专题库": prefix = "XYZT";break;
				case "税务专题库": prefix = "JYZT";break;
				case "环保专题库": prefix = "HBZT";break;
				default:prefix = "";
			}
			if(sdps.size()>1&&"".equals(prefix)){
				continue;
			}
			Session session = entityManager.unwrap(org.hibernate.Session.class); 
			String sSql = "select {t.*} from dg_ds_nodeinfo  t where t.type=3  start with t.id="+sdp.getTreeId()+" connect by prior t.id = t.pid";
			System.out.println(".............:"+sSql);
			List <NodeInfo> nodeInfos = session.createSQLQuery(sSql).addEntity("t", NodeInfo.class).list();
			mapTable.put("tableCount",nodeInfos.size()+"");
			String Tables = "";
			BigDecimal row = new BigDecimal("0");
			if (nodeInfos.size()>0){
				for(NodeInfo node :nodeInfos){
					if(Tables.length()+node.getDefname().length()>1000){
						Tables =Tables.replaceFirst(",", "");
						String sqlForRow="select calallcrows_025('"+Tables+"') as dataRow from dual";
						Map<String, Object> maps = dbUtil.queryForMap(sqlForRow);
						System.out.println(".............sqlForRow:"+sqlForRow);
						row = row.add((BigDecimal)maps.get("DATAROW"));
						Tables = "";
					}
					Tables+=","+node.getDefname();
				}
				Tables =Tables.replaceFirst(",", "");
				String sqlForRow="select calallcrows_025('"+Tables+"') as dataRow from dual";
				System.out.println(".............sqlForRow1:"+sqlForRow);
				Map<String, Object> maps = dbUtil.queryForMap(sqlForRow);
				row = row.add((BigDecimal)maps.get("DATAROW"));
				
				mapTable.put("datasourceName",prefix);
				mapTable.put("rowCount",row.toString() );
				results.add(mapTable);
			}else{
				mapTable.put("datasourceName",prefix);
				mapTable.put("rowCount","0" );
				results.add(mapTable);
				
			}
			
		}
		/*if("base".equals(database)){
			Map<String, String> mapTable = new HashMap<>();
			mapTable.put("tableCount","0");
			mapTable.put("datasourceName","DLXX");
			mapTable.put("rowCount","0");
			results.add(mapTable);
		}*/
		return results;
	}
    
    @RequestMapping(value = "/tableAndRowCount/{database}")
   	@ResponseBody
   	public ArrayList<Map<String, String>> tableAndRowCount(@PathVariable String database, String simpleCode) {
    	ArrayList<Map<String, String>> results = new ArrayList<Map<String, String>>();
    	if (database.equals("base")){
    		/*Iterator<DataSummery>   list = dataSummeryRepository.findAll().iterator();
        	while(list.hasNext()){     
        		DataSummery ds = list.next();
        		Map<String, String> map = new HashMap<String, String>();
        		map.put("datasourceName", ds.getDatasourceName());
        		map.put("rowCount", String.valueOf(ds.getRowCount()));
        		map.put("tableCount", String.valueOf(ds.getTableCount()));
        		results.add(map);
        	}
        	*/
        	
    		DataSummery ds_frk = dataSummeryRepository.findByDatasourceName("FRK");
    		Map<String, String> map_frk = new HashMap<String, String>();
			map_frk.put("datasourceName", ds_frk.getDatasourceName());
			map_frk.put("rowCount", String.valueOf(ds_frk.getRowCount()));
			map_frk.put("tableCount", String.valueOf(ds_frk.getTableCount()));
    		results.add(map_frk);
    		
    		DataSummery ds_rk = dataSummeryRepository.findByDatasourceName("RK");
    		Map<String, String> map_rk = new HashMap<String, String>();
			map_rk.put("datasourceName", ds_rk.getDatasourceName());
			map_rk.put("rowCount", String.valueOf(ds_rk.getRowCount()));
			map_rk.put("tableCount", String.valueOf(ds_rk.getTableCount()));
    		results.add(map_rk);
    		
    		DataSummery ds_hgjj = dataSummeryRepository.findByDatasourceName("HGJJ");
    		Map<String, String> map_hgjj = new HashMap<String, String>();
    		map_hgjj.put("datasourceName", ds_hgjj.getDatasourceName());
    		map_hgjj.put("rowCount", String.valueOf(ds_hgjj.getRowCount()));
    		map_hgjj.put("tableCount", String.valueOf(ds_hgjj.getTableCount()));
    		results.add(map_hgjj);
    		
    		DataSummery ds_dlxx = dataSummeryRepository.findByDatasourceName("DLXX");
    		Map<String, String> map_dlxx = new HashMap<String, String>();
    		map_dlxx.put("datasourceName", ds_dlxx.getDatasourceName());
    		map_dlxx.put("rowCount", String.valueOf(ds_dlxx.getRowCount()));
    		map_dlxx.put("tableCount", String.valueOf(ds_dlxx.getTableCount()));
    		results.add(map_dlxx);
    		
        	
        	
    	} else if (database.equals("zt")){
    		
    		
    		
    		DataSummery ds_xyzt = dataSummeryRepository.findByDatasourceName("XYZT");
    		Map<String, String> map_xyzt = new HashMap<String, String>();
			map_xyzt.put("datasourceName", ds_xyzt.getDatasourceName());
			map_xyzt.put("rowCount", String.valueOf(ds_xyzt.getRowCount()));
			map_xyzt.put("tableCount", String.valueOf(ds_xyzt.getTableCount()));
    		results.add(map_xyzt);
    		
    		
    		DataSummery ds_jyzt = dataSummeryRepository.findByDatasourceName("JYZT");
    		Map<String, String> map_jyzt = new HashMap<String, String>();
    		map_jyzt.put("datasourceName", ds_jyzt.getDatasourceName());
    		map_jyzt.put("rowCount", String.valueOf(ds_jyzt.getRowCount()));
    		map_jyzt.put("tableCount", String.valueOf(ds_jyzt.getTableCount()));
    		results.add(map_jyzt);
    		
    		DataSummery ds_hbzt = dataSummeryRepository.findByDatasourceName("HBZT");
    		Map<String, String> map_hbzt = new HashMap<String, String>();
    		map_hbzt.put("datasourceName", ds_hbzt.getDatasourceName());
    		map_hbzt.put("rowCount", String.valueOf(ds_hbzt.getRowCount()));
    		map_hbzt.put("tableCount", String.valueOf(ds_hbzt.getTableCount()));
    		results.add(map_hbzt);
    		
    		DataSummery ds_lyzt = dataSummeryRepository.findByDatasourceName("LYZT");
    		Map<String, String> map_lyzt = new HashMap<String, String>();
    		map_lyzt.put("datasourceName", ds_lyzt.getDatasourceName());
    		map_lyzt.put("rowCount", String.valueOf(ds_lyzt.getRowCount()));
    		map_lyzt.put("tableCount", String.valueOf(ds_lyzt.getTableCount()));
    		results.add(map_lyzt);
    		
    		DataSummery ds_rkzt = dataSummeryRepository.findByDatasourceName("RKZT");
    		Map<String, String> map_rkzt = new HashMap<String, String>();
    		map_rkzt.put("datasourceName", ds_rkzt.getDatasourceName());
    		map_rkzt.put("rowCount", String.valueOf(ds_rkzt.getRowCount()));
    		map_rkzt.put("tableCount", String.valueOf(ds_rkzt.getTableCount()));
    		results.add(map_rkzt);
    		
    		
    		
    		
    		/*List<DataSummery> list = dataSummeryRepository.findByZt();
    		for (DataSummery ds : list){
    			Map<String, String> map = new HashMap<String, String>();
        		map.put("datasourceName", ds.getDatasourceName());
        		map.put("rowCount", String.valueOf(ds.getRowCount()));
        		map.put("tableCount", String.valueOf(ds.getTableCount()));
        		results.add(map);
    		}*/
        	
    	} else if (database.equals("land")){
    		DataSummery ds = dataSummeryRepository.findByDatasourceName("LAND");
    		Map<String, String> map = new HashMap<String, String>();
    		map.put("datasourceName", ds.getDatasourceName());
    		map.put("rowCount", String.valueOf(ds.getRowCount()));
    		map.put("tableCount", String.valueOf(ds.getTableCount()));
    		results.add(map);
    	}
    	return results;
    }
    
    
}
