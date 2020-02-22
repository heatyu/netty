package com.netty.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

@Sharable
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
	private WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakerFuture;
	private NettyClient client;
	
	public NettyClient getClient() {
		return client;
	}


	public void setClient(NettyClient client) {
		this.client = client;
	}
	
	public void setHandshaker(WebSocketClientHandshaker handshaker) {
		this.handshaker = handshaker;
	}
	
	public WebSocketClientHandshaker getHandshaker() {
		return handshaker;
	}
	
	public ChannelFuture getHandshakerFuture() {
		return handshakerFuture;
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		handshakerFuture = ctx.newPromise();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel channel = ctx.channel();
		if(!handshaker.isHandshakeComplete()) {
			try {
				handshaker.finishHandshake(channel, (FullHttpResponse)msg);
				handshakerFuture.setSuccess();
			} catch (Exception e) {
				e.printStackTrace();
				handshakerFuture.setFailure(e);
			}
			return;
		}
		
		WebSocketFrame frame = (WebSocketFrame)msg;
		if(frame instanceof TextWebSocketFrame) {
			System.out.println("收到服务端信息=====》"+((TextWebSocketFrame)frame).text());
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		if(!handshakerFuture.isDone()) {
			handshakerFuture.setFailure(cause);
		}
		ctx.close();
	}

}
