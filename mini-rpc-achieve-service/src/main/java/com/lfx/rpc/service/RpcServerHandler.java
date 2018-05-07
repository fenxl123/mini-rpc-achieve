package com.lfx.rpc.service;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.util.Map;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lfx.rpc.common.Request;
import com.lfx.rpc.common.Response;

/**
 * 处理具体的业务请求
 *
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<Request> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerHandler.class);
	private final Map<String, Object> handlerMap;
	public RpcServerHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}

	/**
	 * 读取
	 */
	@Override
	public void channelRead0(final ChannelHandlerContext ctx, Request request)
			throws Exception {
		Response response = new Response();
		response.setRequestId(request.getRequestId());
		try {
			//处理业务请求
			Object result = handle(request);
			response.setResult(result);
		} catch (Throwable t) {
			response.setError(t);
		}
		////写入 outbundle进行下一步处理（即编码）后发送到channel中给客户端
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 业务调用是通过反射的方式来完成
	 */
	private Object handle(Request request) throws Throwable {
		
		String interfaceName = request.getInterfaceName();
		
		//根据接口名获取其实现类
		Object serviceBean = handlerMap.get(interfaceName);
		
		//获取方法名
		String methodName = request.getMethodName();
		//获取参数类型
		Class<?>[] parameterTypes = request.getParameterTypes();
		//获取参数
		Object[] parameters = request.getParameters();
		Class<?> forName = Class.forName(interfaceName);
		
		//执行方法
		Method method = forName.getMethod(methodName, parameterTypes);
		return method.invoke(serviceBean, parameters);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOGGER.error("server caught exception", cause);
		ctx.close();
	}
}
