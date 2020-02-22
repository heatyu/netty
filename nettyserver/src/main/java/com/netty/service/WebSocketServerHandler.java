package com.netty.service;

import java.net.InetSocketAddress;


import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.netty.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.StringUtil;

@Sharable
@Service
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
	private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private WebSocketServerHandshaker handshaker;
	private static String websocketUrl;
	private int lossConnectCount = 0;
	
	
	public static String getWebsocketUrl() {
		return websocketUrl;
	}


	public static void setWebsocketUrl(String websocketUrl) {
		WebSocketServerHandler.websocketUrl = websocketUrl;
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		lossConnectCount = 0;
		if (msg instanceof FullHttpRequest) {
			handlerHttpRequest(ctx, (FullHttpRequest)msg);
		}else if(msg instanceof WebSocketFrame){
			handlerWebSocketFrame(ctx,(WebSocketFrame)msg);
		}
	}
	
	public void handlerHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
		String upgrate = request.headers().get(Constants.UPGRADE_STR);
		String webSocketStr = Constants.WEBSOCKET_STR;
		if(!request.decoderResult().isSuccess()||!webSocketStr.equals(upgrate)) {
			sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(websocketUrl, null, false);
		handshaker = wsFactory.newHandshaker(request);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		}else {
			handshaker.handshake(ctx.channel(), request);
		}
	}
	
	public void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, DefaultFullHttpResponse response) {
		if(response.status().code()!=Constants.OK_CODE) {
			ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
			response.content().writeBytes(buf);
			buf.release();
		}
		
		ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
		if (!HttpUtil.isKeepAlive(request)||response.status().code()!=Constants.OK_CODE) {
			channelFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	public void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		Channel channel = ctx.channel();
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame);
		}
		
		if(frame instanceof PingWebSocketFrame) {
			channel.write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		
		System.out.println("收到客户端信息=====》"+((TextWebSocketFrame)frame).text());
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("serverMsg", "bbb");
		if (channel!=null&&channel.isActive()) {
			channel.writeAndFlush(new TextWebSocketFrame(jsonObject.toJSONString()));
		}else {
			System.out.println("offline or msg not send");
		}
	}
	
	public Channel getChannel(String ip) {
		for (Channel channel : channelGroup) {
			InetSocketAddress ipsSocketAddress = (InetSocketAddress)channel.remoteAddress();
			String clientIp = ipsSocketAddress.getAddress().getHostAddress();
			if (ip.equals(clientIp)) {
				return channel;
			}
		}
		return null;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		channelGroup.add(channel);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if(evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent)evt;
			if(event.state()==IdleState.READER_IDLE) {
				lossConnectCount++;
				if(lossConnectCount>2) {
					ctx.channel().close();
				}
			}
		}else {
			super.userEventTriggered(ctx, evt);
		}
	}
}
