package com.lfx.rpc.service.app;

import org.springframework.context.support.ClassPathXmlApplicationContext;
/**
 * 
 *
 */
public class RpcServiceBootstrap {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
