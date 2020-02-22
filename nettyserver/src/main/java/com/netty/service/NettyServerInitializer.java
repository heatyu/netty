package com.netty.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.netty.Constants;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

@Service
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
	
	@Autowired
	private WebSocketServerHandler webSocketServerHandler;
	
	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast(Constants.HTTP_CODEC, new HttpServerCodec());
		pipeline.addLast(Constants.APPGREGATOR, new HttpObjectAggregator(Constants.MAX_CONTENT_LENGTH));
		pipeline.addLast(Constants.HTTP_CHUNK, new ChunkedWriteHandler());
		pipeline.addLast(new IdleStateHandler(1, 0, 0, TimeUnit.HOURS));
		pipeline.addLast(webSocketServerHandler);
	}

}
