package com.lfx.rpc.client.app;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lfx.rpc.achieve.inter.RpcHelloService;
import com.lfx.rpc.client.RpcServerProxy;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class HelloServiceTest {
	@Autowired
	private RpcServerProxy rpcServerProxy;
	@Test
	public void demoTest1() {
		//创建RpcHelloService的代理对象
		
		RpcHelloService helloService = rpcServerProxy.createProxy(RpcHelloService.class);
		// 调用代理对象的方法，执行invoke
		String result = helloService.say("World");
		System.out.println("服务端返回的结果："+result);
		
	}

}
