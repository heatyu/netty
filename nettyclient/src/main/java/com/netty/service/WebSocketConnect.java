package com.netty.service;


import com.alibaba.fastjson.JSONObject;

public class WebSocketConnect implements Runnable{
	private Thread clientThread;
	private NettyClient client;
	
	public void destroy() {
		client.destory();
	}
	
	public void init() {
		System.out.println("start");
		clientThread = new Thread(this);
		clientThread.start();
	}
	
	public void connect() {
		client = new NettyClient();
		client.connect();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("clientMsg", "aaa");
		client.sendText(jsonObject.toJSONString());
	}
	
	@Override
	public void run() {
		try {
			connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
