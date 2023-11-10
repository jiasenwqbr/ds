package com.jason.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@Aspect
public class DsAop {

	@Around("@annotation(mapping)")
	public Object dsAop(ProceedingJoinPoint pjp, RequestMapping mapping) {
		try {
			return pjp.proceed();
		} catch (Throwable e) {
			e.printStackTrace();
			//return null;
			throw new RuntimeException(e.getMessage());
		}
	}
}
