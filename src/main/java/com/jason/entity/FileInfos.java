package com.jason.entity;

public class FileInfos {
    //文件或者文件夹的名称
    private String name;
    //文件的类型 1：文件夹
    private String type;
    //文件在FTP中的路径
    private String path;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPath(String path) {
        this.path = path;
    }
}


