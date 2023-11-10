package com.jason.repository;

import org.springframework.data.repository.CrudRepository;

import com.jason.entity.ExcelDataInfo;


public interface ExcelDataRepository extends CrudRepository<ExcelDataInfo, Long> {

}
