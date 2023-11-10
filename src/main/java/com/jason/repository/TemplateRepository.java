package com.jason.repository;

import org.springframework.data.repository.CrudRepository;

import com.jason.entity.ExcelTemplateInfo;

public interface TemplateRepository extends CrudRepository<ExcelTemplateInfo, Long> {

}
