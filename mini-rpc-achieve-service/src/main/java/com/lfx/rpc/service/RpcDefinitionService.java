package com.lfx.rpc.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * rpc的 自定义注解
 */
@Target({ ElementType.TYPE })//注解用在接口上
@Retention(RetentionPolicy.RUNTIME)///VM将在运行期也保留注释，因此可以通过反射机制读取注解的信息
@Component
public @interface RpcDefinitionService {

	Class<?> value();
}
