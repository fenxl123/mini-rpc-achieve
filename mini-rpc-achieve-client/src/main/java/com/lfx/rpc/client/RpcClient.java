package com.lfx.rpc.client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lfx.rpc.common.DefinitionDecoder;
import com.lfx.rpc.common.DefinitionEncoder;
import com.lfx.rpc.common.Request;
import com.lfx.rpc.common.Response;


/**
 * 自定义RPC框架的客户端
 *
 */
public class RpcClient extends SimpleChannelInboundHandler<Response> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
	private String host;
	private int port;
    private Response response;
    private final Object obj = new Object();
    public RpcClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * 使用netty 发送request
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Response sendRequest(Request request) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel channel)
								throws Exception {
							channel.pipeline()
							.addLast(new DefinitionDecoder(Response.class)) //IN - 1
							.addLast(new DefinitionEncoder(Request.class))  //OUT - 1
							.addLast(RpcClient.this);                   //IN - 2	
									
						}
					}).option(ChannelOption.SO_KEEPALIVE, true);
			// // 连接接服务端
			ChannelFuture future = bootstrap.connect(host, port).sync();
			///将request对象写入outbundle处理后发出（也就是先进行编码）
			future.channel().writeAndFlush(request).sync();

			//先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接
			synchronized (obj) {
				obj.wait();
			}
			if (response != null) {
				future.channel().closeFuture().sync();
			}
			return response;
		} finally {
			group.shutdownGracefully();
		}
	}

	/**
	 * 读取response
	 */
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Response response)
			throws Exception {
		this.response = response;

		synchronized (obj) {
			obj.notifyAll();
		}
	}

	/**
	 *返回结果的异常处理 
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		LOGGER.error("client caught exception", cause);
		ctx.close();
	}


}
