package com.jason.entity;

public class Column {

    private String columnId;
    //字段名
    private String columnName;
    //字段类型
    private String columnType;
    //表达式
    private int expression;
    //表名
    private String tableName;
    //说明
    private String comment;
    //是否为空
    private boolean nullable;
    //长度
    private int length;
    //精度
    private int precision;
    //规模
    private int scale;
    //默认值
    private String defaultValue;
    //主键
    private boolean primaryKey;
    
    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public int getExpression() {
        return expression;
    }

    public void setExpression(int expression) {
        this.expression = expression;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getComment() {
        return comment;
    }

    public boolean isNullable() {
        return nullable;
    }

    public int getLength() {
        return length;
    }

    public int getPrecision() {
        return precision;
    }

    public int getScale() {
        return scale;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }


}
