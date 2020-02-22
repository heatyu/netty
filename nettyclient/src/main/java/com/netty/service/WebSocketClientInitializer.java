package com.netty.service;

import com.netty.Constants;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

public class WebSocketClientInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast(new ChannelHandler[] {new HttpClientCodec(), new HttpObjectAggregator(Constants.MAX_CONTENT_LENGTH)});
		pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
		pipeline.addLast(Constants.HANDLE, new WebSocketClientHandler());
	}
}
