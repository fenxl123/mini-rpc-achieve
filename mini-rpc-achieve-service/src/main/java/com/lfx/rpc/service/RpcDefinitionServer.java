package com.lfx.rpc.service;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.lfx.rpc.common.DefinitionDecoder;
import com.lfx.rpc.common.DefinitionEncoder;
import com.lfx.rpc.common.Request;
import com.lfx.rpc.common.Response;
import com.lfx.rpc.registry.RegistryService;

public class RpcDefinitionServer implements ApplicationContextAware,InitializingBean{

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcDefinitionServer.class);
	private String serverAddress;
	private RegistryService serviceRegistry;
	
	private Map<String, Object> handlerMap = new HashMap<String, Object>();

	public RpcDefinitionServer(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	
	public RpcDefinitionServer(String serverAddress, RegistryService serviceRegistry) {
		this.serverAddress = serverAddress;
		this.serviceRegistry = serviceRegistry;
	}
	/*
	 * 通过注解，获取标注了rpc服务注解的业务类的----接口及impl对象，将它放到handlerMap中，
	 * 开启netty
	 * 向zookeeper注册服务*/
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		 //1.暴露接口以及实现类在本地
		
		Map<String, Object> serviceBeanMap = ctx
				.getBeansWithAnnotation(RpcDefinitionService.class);
		if (MapUtils.isNotEmpty(serviceBeanMap)) {
			for (Object serviceBean : serviceBeanMap.values()) {
				//获取自定义注解的value,也就是接口的全名
				String interfaceName = serviceBean.getClass()
						.getAnnotation(RpcDefinitionService.class).value().getName();
				//把接口全名，以及实现类缓存到map集合中
				handlerMap.put(interfaceName, serviceBean);
			}
		}	
	
	    
		} 
	
	
	public void registryService(RegistryService serviceRegistry,String interfaceName, String host){
		if (serviceRegistry != null) {
			
			serviceRegistry.register(serverAddress,interfaceName);
		}
	}

	//开启netty，并注册到注册中心
	public void afterPropertiesSet() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap
					.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel channel)
								throws Exception {
							channel.pipeline()
									.addLast(new DefinitionDecoder(Request.class))// 注册解码 IN-1
									.addLast(new DefinitionEncoder(Response.class))// 注册编码 OUT
									.addLast(new RpcServerHandler(handlerMap));//注册RpcHandler IN-2
						}
					}).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			
			String[] array = serverAddress.split(":");
			String host = array[0];
			int port = Integer.parseInt(array[1]);
			//向注册中心zookeeper注册服务(类dubbo向zookeeper注册的服务一样的形式：服务名+主机名)
			while(handlerMap.keySet().iterator().hasNext()){
				String interfaceName=(String) handlerMap.keySet().iterator().next();
				registryService(serviceRegistry,interfaceName,host);
			}
			
			ChannelFuture channel = bootstrap.bind(host, port).sync();
			LOGGER.debug("server started on port {}", port);
			channel.channel().closeFuture().sync();
		}finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
		
		
 }
}
