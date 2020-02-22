package com.netty.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Component
public class NettyServer implements Runnable {
	private int port;
	private Thread server;
	
	@Autowired
	private NettyServerInitializer nettyServerInitializer;
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@PostConstruct
	public void initMethod() {
		System.out.println("start");
		server = new Thread(this);
		server.start();
	}
	
	public void bind() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					 .option(ChannelOption.SO_KEEPALIVE, true)
					 .channel(NioServerSocketChannel.class)
					 .handler(new LoggingHandler(LogLevel.INFO))
					 .childHandler(nettyServerInitializer);
			ChannelFuture channelFuture = bootstrap.bind(port).sync();
			channelFuture.channel().closeFuture().sync();
		}catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	@Override
	public void run() {
		bind();
	}
	
}
