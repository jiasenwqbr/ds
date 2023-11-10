package com.jason.repository;

import org.springframework.data.repository.CrudRepository;

import com.jason.entity.ExcelUpdateErrorInfo;

public interface ErrorInorRe extends CrudRepository<ExcelUpdateErrorInfo, Long> {

}
