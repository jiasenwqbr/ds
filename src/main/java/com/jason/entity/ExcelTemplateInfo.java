package com.jason.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "DG_DS_TEMPLATE")
public class ExcelTemplateInfo {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String deptName;
	private String templateName;
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@javax.persistence.Column(name = "CONTENT", columnDefinition = "BLOB", nullable = true)
	private byte[] content;
	private String updateCycle;
	private Date lastUpdate;
	private Integer updateCount;
	private String fileName;
	
	

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getUpdateCycle() {
		return updateCycle;
	}

	public void setUpdateCycle(String updateCycle) {
		this.updateCycle = updateCycle;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Integer getUpdateCount() {
		if (updateCount == null) {
			return 0;
		}
		return updateCount;
	}

	public void setUpdateCount(Integer updateCount) {
		this.updateCount = updateCount;
	}

	public ExcelTemplateInfo() {

	}

	public ExcelTemplateInfo(Long id, String deptName, String fileName, String cycle, Date lastDate, Integer count) {
		this.id = id;
		this.deptName = deptName;
		this.templateName = fileName;
		this.updateCycle = cycle;
		this.lastUpdate = lastDate;
		this.updateCount = count;
	}

}
