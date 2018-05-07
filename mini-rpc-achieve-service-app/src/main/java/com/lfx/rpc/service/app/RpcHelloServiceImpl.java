package com.lfx.rpc.service.app;
import com.lfx.rpc.achieve.inter.RpcHelloService;
import com.lfx.rpc.service.RpcDefinitionService;

@RpcDefinitionService(RpcHelloService.class)
public class RpcHelloServiceImpl implements RpcHelloService {
   public String say(String name) {
		System.out.println("调用服务端接口的实现");
    	System.out.println("Hello! " + name);
        return "Hello! " + name;
	}
}
