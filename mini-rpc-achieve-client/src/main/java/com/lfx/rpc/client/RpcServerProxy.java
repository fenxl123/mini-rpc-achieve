package com.lfx.rpc.client;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import com.lfx.rpc.common.Request;
import com.lfx.rpc.common.Response;
import com.lfx.rpc.registry.DiscoveryService;


/**
 * 创建服务的代理对象
 *
 */
public class RpcServerProxy {
	private String serverAddress="";
	private DiscoveryService discoveryservice;
	private String interfaceName;
	public RpcServerProxy(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	public RpcServerProxy(DiscoveryService discoveryservice) {
		this.discoveryservice = discoveryservice;
	}
	/**
	 * 创建代理
	 */
	@SuppressWarnings("unchecked")
	public <T> T createProxy(Class<?> interfaceClass) {
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class<?>[] { interfaceClass }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						//发送request请求
						Request request = new Request();
						request.setRequestId(UUID.randomUUID().toString());
						request.setInterfaceName(method.getDeclaringClass()
								.getName());
						request.setMethodName(method.getName());
						request.setParameterTypes(method.getParameterTypes());
						request.setParameters(args);
						interfaceName=method.getDeclaringClass().getName();
								
						if (discoveryservice != null) {
							serverAddress = discoveryservice.discover(interfaceName);
						}
						String[] array = serverAddress.split(":");
						String host = array[0];
						int port = Integer.parseInt(array[1]);
						RpcClient client = new RpcClient(host, port);
						Response response = client.sendRequest(request);
						if (response.isError()) {
							throw response.getError();
						} else {
							return response.getResult();
						}
					}
				});
	}
}
