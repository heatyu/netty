package com.netty.service;

import java.net.URI;
import java.util.Properties;

import javax.enterprise.inject.New;

import com.netty.Constants;
import com.netty.util.ReadFromProperties;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.StringUtil;

public class NettyClient {
	private EventLoopGroup client;
	private Channel channel;
	
	public Channel getChannel() {
		return channel;
	}
	
	public boolean online() {
		return getChannel()!=null&&getChannel().isActive();
	}
	
	public void sendText(String msg) {
		if (online()&&!StringUtil.isNullOrEmpty(msg)) {
			channel.writeAndFlush(new TextWebSocketFrame(msg));
		}else {
			System.out.println("offline or msg not send");
		}
	}
	
	public void connect() {
		client = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		try {
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
					 .group(client)
					 .channel(NioSocketChannel.class)
					 .handler(new LoggingHandler(LogLevel.INFO))
					 .handler(new WebSocketClientInitializer());
			Properties properties = ReadFromProperties.getProperties();
			String ip = properties.getProperty("serverWebsocketIp"); 
			URI uri = new URI(ip);
			DefaultHttpHeaders headers = new DefaultHttpHeaders();
			WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true, headers, Constants.MAX_CONTENT_LENGTH);
			channel = bootstrap.connect(uri.getHost(),uri.getPort()).sync().channel();
			WebSocketClientHandler handler = (WebSocketClientHandler)channel.pipeline().get(Constants.HANDLE);
			handler.setHandshaker(handshaker);
			handler.setClient(this);
			handler.getHandshaker().handshake(channel);
			handler.getHandshakerFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void destory() {
		client.shutdownGracefully();
	}
}
